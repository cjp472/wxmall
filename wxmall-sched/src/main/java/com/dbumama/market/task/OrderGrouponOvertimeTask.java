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

import com.dbumama.market.model.BuyerUser;
import com.dbumama.market.model.MultiGroup;
import com.dbumama.market.model.MultiGroupSet;
import com.dbumama.market.model.Order;
import com.dbumama.market.model.OrderGheader;
import com.dbumama.market.model.OrderGuser;
import com.dbumama.market.model.Product;
import com.dbumama.market.model.Refund;
import com.dbumama.market.queue.Controller;
import com.dbumama.market.queue.ControllerImp;
import com.dbumama.market.queue.Handler;
import com.dbumama.market.sched.Minute;
import com.dbumama.market.service.api.ump.UmpException;
import com.dbumama.market.service.enmu.GroupStatus;
import com.dbumama.market.service.enmu.PaymentStatus;
import com.dbumama.market.service.utils.DateTimeUtil;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.weixin.sdk.pay.RefundApi;
import com.weixin.sdk.pay.RefundReqData;
import com.weixin.sdk.pay.RefundResData;

/**
 * 拼团订单超时处理
 * 1.拼团订单
 * 2.拼团订单活动设置情况
 * 3.比如72小时内，5人成团，到时间后，只有3人支付入团，这个时候如果配置3个人也能成团的话，该拼团订单算成功，否则拼团失败
 * @author yangzy
 *
 */
public class OrderGrouponOvertimeTask extends Minute{

	private static final Set<Long> itemSet = new HashSet<Long>();
	private static final Log log = Log.getLog(OrderGrouponOvertimeTask.class);
	private static Controller controller;
	private int dieCount = 0; //死锁计数
	private static final BuyerUser buyerUserdao = new BuyerUser().dao();
	private static final MultiGroup multiGroupdao = new MultiGroup().dao();
	private static final MultiGroupSet multiGroupSetdao = new MultiGroupSet().dao();
	private static final Order orderdao = new Order().dao();
	private static final OrderGheader orderGheaderdao = new OrderGheader().dao();
	private static final OrderGuser orderGuserdao = new OrderGuser().dao();
	private static final Product productDao = new Product().dao();
	private static final Refund refunddao = new Refund().dao();
	
	public OrderGrouponOvertimeTask(){
		controller = new ControllerImp("OrderGrouponOvertimeTask", 6);
		controller.addDefaultHandler(new OrderGrouponOvertimeHandler());
		controller.create();
		
		setScheduleId("orderGrouponOvertimeTask");
		setFirstExecute(false);
	}
	
	@Override
	public int execute() {
		if(dieCount == 5){
    		controller.destroy();
            controller = new ControllerImp("OrderGrouponOvertimeTask", 6);
            controller.addDefaultHandler(new OrderGrouponOvertimeHandler());
            controller.create();     	
            itemSet.clear();
            dieCount = 0;
    	}
		
		//找出所有拼团中的拼团订单
		List<Order> orders = null; 
		
		if(controller == null || controller.getMessageQueueSize()<100){
			orders = orderdao.find("select * from " + Order.table + " where order_type=2 and group_status=0 ");
			dieCount = 0;
		}else {
			dieCount ++;
		}
		
		if(orders == null || orders.size() <=0){
			log.info("==================本次拼团订单需要处理");
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
	
	class OrderGrouponOvertimeHandler implements Handler {

		@Override
		public void process(Object obj) {
			Order item = (Order) obj;
			synchronized(itemSet) {
        		if (itemSet.contains(item.getId()))
        			return;
        		itemSet.add(item.getId());
            }
			try{
				OrderGheader gheader = orderGheaderdao.findFirst("select * from " + OrderGheader.table + " where order_id=? and active=1 ", item.getId());
				if(gheader != null){
					Product product = productDao.findById(gheader.getProductId());
					
					//获取商品拼团信息
					MultiGroup multiGroup = getProductMultiGroup(product);
					//说明是同一个拼团活动中产生的订单，并且已超时
					if(multiGroup.getId() == gheader.getGroupId()
							&& DateTimeUtil.compareHour(new Date(), item.getCreated()) >= multiGroup.getValidTime()){
						log.info("=============order_id:" + item.getId() + "，拼团订单已超时：" + DateTimeUtil.compareHour(new Date(), item.getCreated()) + "小时");
						//使团失效
						gheader.setActive(false);
						gheader.update();
						//已支付订单的拼团用户
						if(multiGroup.getReUnionNum() != null && multiGroup.getReUnionNum()>0){
							//超时后拼团人数调整值，达到这个调整值后，拼团也算成功，已支付用户无需退款，删除未支付用户拼团订单数据
							Long guserPayedCount = Db.queryLong("select count(id) from " + OrderGuser.table + " where gheader_id=? and payment_status=2", gheader.getId());
							if(guserPayedCount != null && guserPayedCount.intValue() == multiGroup.getReUnionNum()){
								//拼团算成功
								item.setGroupStatus(GroupStatus.success.ordinal());		//拼团成功
								item.setPaymentStatus(PaymentStatus.paid.ordinal());	//已支付
								item.update();
								//除开团者外，其他未支付用户订单删除掉
								List<OrderGuser> gusers = orderGuserdao.find("select * from " + OrderGuser.table + " where gheader_id=? and payment_status=0", gheader.getId());
								for(OrderGuser guser : gusers){
									if(guser.getBuyerId() != gheader.getBuyerId()){
										//开团者允许不支付
										guser.delete();
									}
								}
							}else{
								//没有设置拼团失败调整值的情况
								item.setGroupStatus(GroupStatus.fail.ordinal());		//拼团失败
								item.update();
								//查询出所有已支付用户，进行退款
								List<OrderGuser> gusers = orderGuserdao.find("select * from " + OrderGuser.table + " where gheader_id=? and payment_status=2", gheader.getId());
								for(OrderGuser guser : gusers){
									
									BuyerUser buyer = buyerUserdao.findById(guser.getBuyerId());
									if(buyer == null || buyer.getSubscribe() == null || buyer.getSubscribe() !=1){
										log.info("用户:" + buyer == null ? null : buyer.getOpenId() + ",未关注公众号，不可退款");
										continue;
									}
									/*AuthCert cert = AccessTokenUtil.getAuthCert(buyer.getAuthAppId());
									if(cert == null) {
										log.info("用户:" + buyer.getOpenId() + ",未上传支付凭证，不可退款");
										continue;	
									}*/
									
									Refund refund = refunddao.findFirst("select * from " + Refund.table 
											+ " where gheader_id=? and buyer_id=? and order_id=? "
											,guser.getGheaderId(), guser.getBuyerId(), item.getId());
									
									if(refund == null){
										//直接调用接口退款
										RefundReqData refundReqData = new RefundReqData(
												guser.getId().toString(), "", guser.getPayFee().toString(), guser.getPayFee().toString(), guser.getTransactionId());
										RefundApi refundApi = new RefundApi();
										RefundResData refundResData = (RefundResData) refundApi.post(refundReqData);
										if("SUCCESS".equals(refundResData.getResult_code())){
											log.info("=====用户：" + guser.getId() + "，已退款" + guser.getPayFee());
											refund = new Refund();
											refund.setOrderId(item.getId());
											refund.setGheaderId(guser.getGheaderId());
											refund.setBuyerId(guser.getBuyerId());
											refund.setCash(guser.getPayFee());
											refund.setTransactionId(guser.getTransactionId());
											refund.setCreated(new Date());
											refund.setUpdated(new Date());
											refund.setActive(true);
											refund.save();
										}	
									}
								}
							}
						}
					}
				}
			}catch(Exception e){
				e.printStackTrace();
				log.error(e.getMessage());
			}finally {
				synchronized(itemSet) {
					itemSet.remove(item.getId());
	            }
			}
		}
		
		/**
		 * 获取当前订单商品对于的拼团信息
		 * @param product
		 * @return
		 * @throws UmpException
		 */
		private MultiGroup getProductMultiGroup(Product product) throws UmpException {
			List<MultiGroup> groups = multiGroupdao.find("select * from " + MultiGroup.table + 
					" where seller_id=? and active=1 and start_date<=? and end_date>=?", product.getSellerId(), new Date(), new Date());
			
			for(MultiGroup group : groups){
				List<MultiGroupSet> groupSets = multiGroupSetdao.find(" select * from " + MultiGroupSet.table + " where multi_group_id=? ", group.getId());
				for(MultiGroupSet groupSet : groupSets){
					if(groupSet.getProductId() == product.getId())
						return group;
				}
			}
			
			return null;
		}
		
	}

}
