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

import com.dbumama.market.model.Order;
import com.dbumama.market.model.OrderGheader;
import com.dbumama.market.model.OrderGuser;
import com.dbumama.market.queue.Controller;
import com.dbumama.market.queue.ControllerImp;
import com.dbumama.market.queue.Handler;
import com.dbumama.market.sched.TenMinute;
import com.dbumama.market.service.enmu.OrderStatus;
import com.dbumama.market.service.enmu.OrderType;
import com.dbumama.market.service.utils.DateTimeUtil;
import com.jfinal.log.Log;

/**
 * 自动取消未支付订单
 * 超过24小时未支付订单系统自动取消
 * @author yangzy
 *
 */
public class OrderUnpayAutoCancelTask extends TenMinute{

	private static final Set<Long> itemSet = new HashSet<Long>();
	private static final Log log = Log.getLog(OrderUnpayAutoCancelTask.class);
	private static Controller controller;
	private int dieCount = 0; //死锁计数
	private static final Order orderdao = new Order().dao();
	private static final OrderGheader orderGheaderdao = new OrderGheader().dao();
	private static final OrderGuser orderGuserdao = new OrderGuser().dao();
	
	/**
	 * 自动取消未支付订单
	 */
	public OrderUnpayAutoCancelTask(){
		controller = new ControllerImp("OrderUnpayAutoCancelTask", 6);
		controller.addDefaultHandler(new OrderUnpayAutoCancelHandler());
		controller.create();
		
		setScheduleId("orderUnpayAutoCancelTask");
		setFirstExecute(false);
	}
	
	@Override
	public int execute() {
		if(dieCount == 5){
    		controller.destroy();
            controller = new ControllerImp("OrderUnpayAutoCancelTask", 6);
            controller.addDefaultHandler(new OrderUnpayAutoCancelHandler());
            controller.create();     	
            itemSet.clear();
            dieCount = 0;
    	}
		
		//找出没有支付的所有订单
		List<Order> orders = null; 
		
		if(controller == null || controller.getMessageQueueSize()<100){
			//查询未取消，未支付的订单
			orders = orderdao.find("select * from " + Order.table + " where payment_status=0 and order_status !=3");
			dieCount = 0;
		}else {
			dieCount ++;
		}
		
		if(orders == null || orders.size() <=0){
			log.info("==================本次没有未支付订单需要处理");
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
	
	class OrderUnpayAutoCancelHandler implements Handler{

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
					log.info("====处理拼团订单:" + item.getId());
					//处理拼团订单
					OrderGheader gheader = orderGheaderdao.findFirst("select * from " + OrderGheader.table + " where order_id=? and active=1", item.getId());
					if(gheader != null){
						//未支付的订单
						List<OrderGuser> gusers = orderGuserdao.find("select * from " + OrderGuser.table + " where gheader_id=? and payment_status=0", gheader.getId());
						for(OrderGuser guser : gusers){
							//拼团订单20分钟未支付，即取消订单
							//团长未支付也不取消订单
							if(guser.getBuyerId() != gheader.getBuyerId()
									&& DateTimeUtil.compareMinute(new Date(), guser.getCreated()) >=20){
								guser.delete();
								log.info("================拼团订单，参团用户： "+guser.getId()+", order_id:" + item.getId() + "超过20分钟未支付被取消");
							}
						}
					}
				}else{
					//普通订单
					if(DateTimeUtil.compareHour(new Date(), item.getCreated()) >= 24){
						item.setOrderStatus(OrderStatus.cancelled.ordinal());
						item.update();
						log.info("================order_id:" + item.getId() + "超过24小时未支付被取消");
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
		
	}

}
