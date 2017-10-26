package com.dbumama.market.service.api.lottery;

/**
 * 33732992@qq.com
 * 2016年6月27日
 */
@SuppressWarnings("serial")
public class LotteryServiceException extends RuntimeException{

	public LotteryServiceException (String msg){
		super(msg);
	}

	public LotteryServiceException (String code, String msg){
		super(code, new Throwable(msg));
	}
	
}
