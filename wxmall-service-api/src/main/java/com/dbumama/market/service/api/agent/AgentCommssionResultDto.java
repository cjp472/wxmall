package com.dbumama.market.service.api.agent;

import java.math.BigDecimal;

import com.dbumama.market.service.api.common.AbstractResultDto;

@SuppressWarnings("serial")
public class AgentCommssionResultDto extends AbstractResultDto{

	private String nickName;
	private BigDecimal commissionValue;
	public String getNickName() {
		return nickName;
	}
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	public BigDecimal getCommissionValue() {
		return commissionValue;
	}
	public void setCommissionValue(BigDecimal commissionValue) {
		this.commissionValue = commissionValue;
	}
	
}
