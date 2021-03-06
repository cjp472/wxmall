package com.dbumama.market.web.mobile.agent.controller;

import java.math.BigDecimal;
import java.util.List;

import com.dbumama.market.model.Agent;
import com.dbumama.market.model.AgentAduitLog;
import com.dbumama.market.model.AgentCommssion;
import com.dbumama.market.service.api.agent.AgentApplyParamDto;
import com.dbumama.market.service.api.agent.AgentException;
import com.dbumama.market.service.api.agent.AgentService;
import com.dbumama.market.web.core.annotation.RouteBind;
import com.dbumama.market.web.core.controller.BaseMobileController;
import com.dbumama.market.web.core.interceptor.RepeatRequestInterceptor;
import com.dbumama.market.web.core.plugin.spring.Inject.BY_NAME;
import com.jfinal.aop.Before;

@RouteBind(path = "agent")
public class AgentController extends BaseMobileController{

	@BY_NAME
	private AgentService agentService;
	
	/**
	 * 申请分销商
	 */
	public void apply(){
		//check
		Agent agent = agentService.getBuyerAgent(getBuyerId());
		if(agent == null) {
			Long parentId = getParaToLong("parentId");
			if(parentId != null){
				Agent parentAgent = agentService.getAgentById(parentId);
				setAttr("parentAgent", parentAgent);
			}
			render("/agent/agent_apply_index.html");
		}else {
			List<AgentAduitLog> aduitLogs = agentService.getAgentAduitLogs(agent.getId());
			setAttr("aduitLogs", aduitLogs);
			setAttr("agent", agent);
			render("/agent/agent_info.html");	
		}
	}
	
	public void save (){
		final String agentName = getPara("agentName");
		final String agentPhone = getPara("agentPhone");
		final String phoneCode = getPara("phoneCode");
		final Long parentId = getParaToLong("parentId");
		final Long areaId = getParaToLong("areaId", -1L);
		final String code = getPara("code");
		final String addr = getPara("addr");
		final String codeInSession = getSession().getAttribute("captcha") == null ? "" : getSession().getAttribute("captcha").toString();
		
		AgentApplyParamDto applyParam = new AgentApplyParamDto(getBuyerId(), getSellerId(), agentPhone);
		applyParam.setParentId(parentId);   	//上级代理
		applyParam.setAddr(addr);
		applyParam.setAgentName(agentName);
		applyParam.setAreaId(areaId);
		applyParam.setCode(code);
		applyParam.setCodeInSession(codeInSession);
		applyParam.setPhoneCode(phoneCode);
		try {
			agentService.apply(applyParam);
			rendSuccessJson();
		} catch (Exception e) {
			rendFailedJson(e.getMessage());
		}
	}
	
	public void card(){
		Long agentId = getParaToLong("agentId");
		if(agentId != null){
			Agent agent = agentService.getAgentById(agentId);
			Integer grade = agentService.getAgentGrade(agent);
			if(grade !=null && grade<=3){
				setAttr("agent", agent);
				setAttr("agentGrade", grade);				
			}
		}
		render("/agent/agent_card.html");
	}
	
	/**
	 * 分销商佣金
	 */
	public void commission(){
		Agent agent = agentService.getBuyerAgent(getBuyerId());
		if(agent != null && agent.getStatus() == 1){
			//分销商可提现佣金
			AgentCommssion commssion = agentService.getAgentCommssion(agent.getId());
			BigDecimal totalCommission = commssion == null || commssion.getCommssionValue() == null ? new BigDecimal(0) : commssion.getCommssionValue();
			setAttr("totalCommission", totalCommission);
		}
		render("/agent/agent_commission.html");
	}
	
	/**
	 * 分销商提现
	 */
	@Before(RepeatRequestInterceptor.class)
	public void getCash(){
		try {
			agentService.getCash(getBuyerId());
			rendSuccessJson();
		} catch (AgentException e) {
			rendFailedJson(e.getMessage());
		}
	}
	
}
