package com.dbumama.market.service.api.card;

import com.dbumama.market.service.api.exception.MarketBaseException;

@SuppressWarnings("serial")
public class CardException extends MarketBaseException{

	public CardException(String message) {
		super(message);
	}

}
