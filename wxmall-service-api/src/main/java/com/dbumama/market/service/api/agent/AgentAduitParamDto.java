package com.dbumama.market.service.api.agent;

import com.dbumama.market.service.api.common.AbstractPageParamDto;

@SuppressWarnings("serial")
public class AgentAduitParamDto extends AbstractPageParamDto{

	/**
	 * @param pageNo
	 */
	public AgentAduitParamDto(Long sellerId, Integer pageNo) {
		super(sellerId, pageNo);
	}
	
	private String agentPhone;
	private String agentName;
	private String wxNick;
	private Integer status;
	
	public String getAgentPhone() {
		return agentPhone;
	}
	public void setAgentPhone(String agentPhone) {
		this.agentPhone = agentPhone;
	}
	public String getAgentName() {
		return agentName;
	}
	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}
	public String getWxNick() {
		return wxNick;
	}
	public void setWxNick(String wxNick) {
		this.wxNick = wxNick;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}

}
