/**
 * 文件名:PayException.java
 * 版本信息:1.0
 * 日期:2015-11-2
 * 成都次时代信息科技版权所有
 */
package com.dbumama.market.service.api.pay;

/**
 * @author: 33732992@qq.com
 * @date:2015-11-2
 */
public class PayException extends RuntimeException{

	/**
	 * @param message
	 */
	public PayException(String message) {
		super(message);
	}

	/**
	 * @author: 33732992@qq.com
	 * @date: 2015-11-2
	 */
	private static final long serialVersionUID = 1L;

}
