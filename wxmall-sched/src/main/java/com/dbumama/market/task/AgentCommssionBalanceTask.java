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

import com.dbumama.market.model.AgentCommRcd;
import com.dbumama.market.model.AgentCommssion;
import com.dbumama.market.model.Order;
import com.dbumama.market.queue.Controller;
import com.dbumama.market.queue.ControllerImp;
import com.dbumama.market.queue.Handler;
import com.dbumama.market.sched.Day;
import com.dbumama.market.service.enmu.OrderStatus;
import com.jfinal.log.Log;

/**
 * 分销商佣金结算任务，每天结算一次
 * @author yangzy
 *
 */
public class AgentCommssionBalanceTask extends Day{

	private static final Set<Long> itemSet = new HashSet<Long>();
	private static final Log log = Log.getLog(AgentCommssionBalanceTask.class);
	private static Controller controller;
	private int dieCount = 0; //死锁计数
	private static final AgentCommRcd agentCommRcdDao = new AgentCommRcd().dao();
	private static final AgentCommssion agentCommssionDao = new AgentCommssion().dao();
	private static final Order orderdao = new Order().dao();
	
	public AgentCommssionBalanceTask(){
		controller = new ControllerImp("AgentCommssionBalanceTask", 6);
		controller.addDefaultHandler(new CommssionBalanceHandler());
		controller.create();
		
		setScheduleId("agentCommssionBalanceTask");
		setFirstExecute(false);
	}
	
	@Override
	public int execute() {
		if(dieCount == 5){
    		controller.destroy();
            controller = new ControllerImp("AgentCommssionBalanceTask", 6);
            controller.addDefaultHandler(new CommssionBalanceHandler());
            controller.create();     	
            itemSet.clear();
            dieCount = 0;
    	} 
		
		List<AgentCommRcd> itemList = null;
		if(controller == null || controller.getMessageQueueSize()<100){
			//查询出所有未结算的佣金记录
			itemList = agentCommRcdDao.find("select * from " + AgentCommRcd.table + " where active=0");
			dieCount = 0;
		}else {
			dieCount++;
		}
		
		if(itemList == null || itemList.size()<=0){
			log.info("==================没有需要结算的佣金数据...");
			return SUCC;
		}
		
		log.info("===================本次共查询出[" + itemList.size() + "]条待结算的佣金数据，待处理...");
		for(AgentCommRcd item : itemList){
			if(itemSet.contains(item.getId()))
				continue;
        	if (controller.isExist(item))
        		continue;
        	controller.postRequest(item);	
		}
		
		return SUCC;
	}
	
	public class CommssionBalanceHandler implements Handler{

		@Override
		public void process(Object obj) {
			AgentCommRcd item = (AgentCommRcd) obj;
			synchronized(itemSet) {
        		if (itemSet.contains(item.getId()))
        			return;
        		itemSet.add(item.getId());
            }
			
			try{
				//根据订单情况结算佣金
				Order order = orderdao.findById(item.getOrderId());
				if(order.getOrderStatus() == OrderStatus.completed.ordinal()){
					item.setActive(true);
					item.update();
					//需要已完成的订单才能结算佣金
					AgentCommssion agentCommssion = agentCommssionDao.findFirst("select * from " + AgentCommssion.table + " where agent_id=? ", item.getAgentId());
					if(agentCommssion == null){
						agentCommssion = new AgentCommssion();
						agentCommssion.setAgentId(item.getAgentId());
						agentCommssion.setCommssionValue(item.getCommissionValue());
						agentCommssion.setActive(true);
						agentCommssion.setCreated(new Date());
						agentCommssion.setUpdated(new Date());
						agentCommssion.save();
					}else{
						agentCommssion.setCommssionValue(agentCommssion.getCommssionValue().add(item.getCommissionValue()));
						agentCommssion.setUpdated(new Date());
						agentCommssion.update();
					}
				}
			}catch (Exception e) {
				log.error(e.getMessage());
			}finally {
				synchronized(itemSet) {
					itemSet.remove(item.getId());
	            }
			}
		}
		
	}

}
