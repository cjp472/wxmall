/**
 * Copyright (c) 成都次时代信息科技有限公司 2016-2017, 33732992@qq.com.
 *
 * Licensed under the GNU Lesser General Public License (LGPL) ,Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/lgpl-3.0.txt
 *	    http://www.ybwqy.com
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dbumama.market.task;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.alibaba.fastjson.JSONObject;
import com.dbumama.market.model.BuyerUser;
import com.dbumama.market.model.Order;
import com.dbumama.market.model.OrderGheader;
import com.dbumama.market.model.OrderGuser;
import com.dbumama.market.model.OrderPressRcd;
import com.dbumama.market.queue.Controller;
import com.dbumama.market.queue.ControllerImp;
import com.dbumama.market.queue.Handler;
import com.dbumama.market.sched.Minute;
import com.dbumama.market.service.enmu.OrderType;
import com.dbumama.market.service.enmu.PaymentStatus;
import com.dbumama.market.service.utils.DateTimeUtil;
import com.jfinal.kit.StrKit;
import com.jfinal.log.Log;
import com.weixin.sdk.api.ApiResult;
import com.weixin.sdk.api.TemplateData;
import com.weixin.sdk.api.TemplateMsgApi;
import com.weixin.sdk.kit.WxSdkPropKit;

/**
 * 订单到时催付通知提醒
 * 超过半小时未支付的订单，提醒催付
 * @author yangzy
 *
 */
public class OrderUnpayPressTask extends Minute{

	private static final Set<Long> itemSet = new HashSet<Long>();
	private static final Log log = Log.getLog(OrderUnpayPressTask.class);
	private static Controller controller;
	private int dieCount = 0; //死锁计数
	private static final BuyerUser buyerUserdao = new BuyerUser().dao();
	private static final Order orderdao = new Order().dao();
	private static final OrderGheader orderGheaderdao = new OrderGheader().dao();
	private static final OrderGuser orderGuserdao = new OrderGuser().dao();
	private static final OrderPressRcd orderPressRcddao = new OrderPressRcd().dao();
	
	/**
	 * 订单催付
	 */
	public OrderUnpayPressTask(){
		controller = new ControllerImp("OrderUnpayPressTask", 6);
		controller.addDefaultHandler(new OrderUnpayPressHandler());
		controller.create();
		
		setScheduleId("orderUnpayPressTask");
		setFirstExecute(false);
	}
	
	@Override
	public int execute() {
		if(dieCount == 5){
    		controller.destroy();
            controller = new ControllerImp("OrderUnpayPressTask", 6);
            controller.addDefaultHandler(new OrderUnpayPressHandler());
            controller.create();     	
            itemSet.clear();
            dieCount = 0;
    	}
		
		//找出没有支付的所有订单
		List<Order> orders = null; 
		
		if(controller == null || controller.getMessageQueueSize()<100){
			//未支付，未取消的订单，需要催付
			orders = orderdao.find("select * from " + Order.table + " where payment_status=0 and order_status !=3");
			dieCount = 0;
		}else {
			dieCount ++;
		}
		
		if(orders == null || orders.size() <=0){
			log.info("==================本次没有未支付订单需要催付处理");
			return SUCC;
		}
		
		for(Order item : orders){
			if(itemSet.contains(item.getId()))
				continue;
        	if (controller.isExist(item))
        		continue;
        	controller.postRequest(item);	
		}
			
		return SUCC;
	}
	
	class OrderUnpayPressHandler implements Handler{

		@Override
		public void process(Object obj) {
			Order item = (Order) obj;
			synchronized(itemSet) {
        		if (itemSet.contains(item.getId()))
        			return;
        		itemSet.add(item.getId());
            }
			try{
				if(item.getOrderType() != null && item.getOrderType() == OrderType.pintuan.ordinal()){
					//处理拼团订单
					OrderGheader gheader = orderGheaderdao.findFirst("select * from " + OrderGheader.table + " where order_id=? and active=1 ", item.getId());
					if(gheader == null) return;
					List<OrderGuser> gusers = orderGuserdao.find("select * from " + OrderGuser.table + " where gheader_id=?", gheader.getId());
					for(OrderGuser guser : gusers){
						if(guser.getPaymentStatus() == PaymentStatus.unpaid.ordinal()
								&& DateTimeUtil.compareMinute(new Date(), guser.getCreated()) >= 10){
							//拼团订单超过10分钟未支付，即提醒
							//超过10分钟未支付的订单
							//拼团订单催付处理
							OrderPressRcd orderPressRcd = orderPressRcddao.findFirst("select * from " + OrderPressRcd.table 
									+ " where order_id=? and gheader_id=? and buyer_id=? ", 
									item.getId(), gheader.getId(), guser.getBuyerId());
							if(orderPressRcd == null){
								BuyerUser buyer = buyerUserdao.findById(guser.getBuyerId());
								if(buyer != null && buyer.getSubscribe() != null && buyer.getSubscribe() == 1){
									try {
										sendMsg(buyer, item.getId(), guser.getCreated(), item.getOrderSn(), 2);
										orderPressRcd = new OrderPressRcd();
										orderPressRcd.setOrderId(item.getId());
										orderPressRcd.setGheaderId(gheader.getId());
										orderPressRcd.setBuyerId(buyer.getId());
										orderPressRcd.setActive(true);
										orderPressRcd.setCreated(new Date());
										orderPressRcd.setUpdated(new Date());
										orderPressRcd.save();
									} catch (Exception e) {
										log.error(e.getMessage());
									}
								}
							}
						}
					}
				}else{
					//普通订单
					if(DateTimeUtil.compareMinute(new Date(), item.getCreated()) >= 30){
						OrderPressRcd orderPressRcd = orderPressRcddao.findFirst("select * from " + OrderPressRcd.table + " where order_id=?", item.getId());
						if(orderPressRcd == null){
							BuyerUser buyer = buyerUserdao.findById(item.getBuyerId());

							if(buyer != null && buyer.getSubscribe() != null && buyer.getSubscribe() == 1){
								try {
									sendMsg(buyer, item.getId(), item.getCreated(), item.getOrderSn(), 0);
									orderPressRcd = new OrderPressRcd();
									orderPressRcd.setOrderId(item.getId());
									orderPressRcd.setActive(true);
									orderPressRcd.setCreated(new Date());
									orderPressRcd.setUpdated(new Date());
									orderPressRcd.save();
								} catch (Exception e) {
									log.error(e.getMessage());
								}
							}
						}
					}
				}
			}catch(Exception e){
				log.error(e.getMessage());
			}finally {
				synchronized(itemSet) {
					itemSet.remove(item.getId());
	            }
			}
		}
		
		/**
		 * @param buyer
		 * @param orderId
		 * @param created
		 * @param orderSn
		 * @param type		2为拼团订单 0或其他为普通订单
		 * @throws Exception
		 */
		private void sendMsg(BuyerUser buyer, Long orderId, Date created, String orderSn, int type) throws Exception{
			JSONObject json = new JSONObject();
			json.put("template_id_short", "TM00184");
			String templateId = TemplateMsgApi.getTemplateId("TM00184", json.toString());
			if(StrKit.isBlank(templateId)) throw new Exception("订单类型："+type+",给" + buyer.getNickname() + "的订单["+orderId+"]，发送模板消息失败，templateId is null");
			String remark = type == 2 ? "10分钟" : "24小时";
			TemplateData templateData = TemplateData.New().setTemplate_id(templateId)
					.setTouser(buyer.getOpenId())
					.setUrl("http://"+WxSdkPropKit.get("wx_domain")+"/order/detail/?orderId="+orderId)
					.add("first",  buyer.getNickname() + "，您好！您的订单未支付，即将关闭。", "#173177")
					.add("ordertape", DateTimeUtil.toDateTimeString(created), "#173177")
					.add("ordeID", orderSn, "#173177")
					.add("remark", "未付款订单"+remark+"内关闭，请及时付款。感谢您的支持！点击查看详情", "#173177");
				
			ApiResult apiResult = TemplateMsgApi.send(templateData.build());
			if(!apiResult.isSucceed()) throw new Exception("订单类型："+type+",给" + buyer.getNickname() + "的订单["+orderId+"]，"
					+ "发送模板消息失败，code:" + apiResult.getErrorCode() + ",error:" + apiResult.getErrorMsg());
		}
		
	}

}
