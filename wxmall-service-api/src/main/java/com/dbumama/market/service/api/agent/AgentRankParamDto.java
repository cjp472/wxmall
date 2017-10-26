package com.dbumama.market.service.api.agent;

import com.dbumama.market.service.api.common.AbstractPageParamDto;
@SuppressWarnings("serial")
public class AgentRankParamDto extends AbstractPageParamDto{
	/**
	 * @param pageNo
	 */
	public AgentRankParamDto(Long sellerId, Integer pageNo) {
		super(sellerId, pageNo);
	}

	private String rankName;

	public String getRankName() {
		return rankName;
	}

	public void setRankName(String rankName) {
		this.rankName = rankName;
	}
	
	
}
