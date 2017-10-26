package com.dbumama.market.service.api.card;

import com.dbumama.market.service.api.common.AbstractPageParamDto;

@SuppressWarnings("serial")
public class CardListParamDto extends AbstractPageParamDto{

	/**
	 * @param pageNo
	 */
	public CardListParamDto(Long sellerId, Integer pageNo) {
		super(pageNo);
		this.sellerId = sellerId;
	}

}
