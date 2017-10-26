package com.dbumama.market.service.api.order;

/**
 * 打印服务
 * @author yangzy
 *
 */
public interface PrintService {

	/**
	 * 获取快递单打印的相关数据
	 * @param printParamDto
	 * @return
	 * @throws OrderException
	 */
	public PrintExpResultDto getExpressPrintData (PrintParamDto printParamDto) throws OrderException;
	
	/**
	 * 获取发货单打印的相关数据
	 * @param printParamDto
	 * @return
	 * @throws OrderException
	 */
	public PrintInvResultDto getInvoicePrintData(PrintParamDto printParamDto) throws OrderException;
	
}
