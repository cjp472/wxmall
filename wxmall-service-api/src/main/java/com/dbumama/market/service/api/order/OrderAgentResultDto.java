package com.dbumama.market.service.api.order;

import java.util.List;

import com.dbumama.market.service.api.agent.AgentCommssionResultDto;

/**
 * 分销订单Dto
 * @author yangzy
 *
 */
@SuppressWarnings("serial")
public class OrderAgentResultDto extends OrderResultDto {

	private List<AgentCommssionResultDto> commssions;	//该订单的分销明细数据

	public List<AgentCommssionResultDto> getCommssions() {
		return commssions;
	}

	public void setCommssions(List<AgentCommssionResultDto> commssions) {
		this.commssions = commssions;
	}
	
}
