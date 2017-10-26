package com.dbumama.market.service.api.agent;

import java.math.BigDecimal;

import com.dbumama.market.service.api.common.AbstractResultDto;

@SuppressWarnings("serial")
public class AgentCommissionResultDto extends AbstractResultDto {

	private String nickName;
	private String nickHeader;
	private String agentName;
	private String agentPhone;
	private BigDecimal totalCommission;		//累计总佣金
	private BigDecimal commissionValue;		//可提现佣金
	private BigDecimal cashValue;			//已提现佣金
	
	public AgentCommissionResultDto(){
		setCommissionValue(new BigDecimal(0));
		setCashValue(new BigDecimal(0));
	}
	
	public String getNickName() {
		return nickName;
	}
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	public String getNickHeader() {
		return nickHeader;
	}
	public void setNickHeader(String nickHeader) {
		this.nickHeader = nickHeader;
	}
	public String getAgentName() {
		return agentName;
	}
	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}
	public String getAgentPhone() {
		return agentPhone;
	}
	public void setAgentPhone(String agentPhone) {
		this.agentPhone = agentPhone;
	}
	public BigDecimal getTotalCommission() {
		return totalCommission;
	}
	public void setTotalCommission(BigDecimal totalCommission) {
		this.totalCommission = totalCommission;
	}
	public BigDecimal getCommissionValue() {
		return commissionValue;
	}
	public void setCommissionValue(BigDecimal commissionValue) {
		this.commissionValue = commissionValue;
	}
	public BigDecimal getCashValue() {
		return cashValue;
	}
	public void setCashValue(BigDecimal cashValue) {
		this.cashValue = cashValue;
	}
	
}
