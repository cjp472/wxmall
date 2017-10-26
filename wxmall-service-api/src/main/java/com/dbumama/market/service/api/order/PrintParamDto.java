package com.dbumama.market.service.api.order;

import com.dbumama.market.service.api.common.AbstractParamDto;

@SuppressWarnings("serial")
public class PrintParamDto extends AbstractParamDto{

	private String orderIds;		//需要打印的订单数据
	private String expressKey;		//使用的哪个快递模板进行打印
	
	public PrintParamDto(Long sellerId, String orderIds, String expressKey) {
		super();
		this.sellerId = sellerId;
		this.orderIds = orderIds;
		this.expressKey = expressKey;
	}
	
	public PrintParamDto(Long sellerId, String orderIds) {
		this(sellerId, orderIds, null);
	}

	public String getOrderIds() {
		return orderIds;
	}
	public void setOrderIds(String orderIds) {
		this.orderIds = orderIds;
	}
	public String getExpressKey() {
		return expressKey;
	}
	public void setExpressKey(String expressKey) {
		this.expressKey = expressKey;
	}
	
}
