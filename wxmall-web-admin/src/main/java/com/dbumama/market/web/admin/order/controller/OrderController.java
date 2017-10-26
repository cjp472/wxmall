package com.dbumama.market.web.admin.order.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dbumama.market.model.ExpressTemplate;
import com.dbumama.market.model.Order;
import com.dbumama.market.service.api.order.ExpTemplateService;
import com.dbumama.market.service.api.order.OrderAgentResultDto;
import com.dbumama.market.service.api.order.OrderException;
import com.dbumama.market.service.api.order.OrderListParamDto;
import com.dbumama.market.service.api.order.OrderResultDto;
import com.dbumama.market.service.api.order.OrderService;
import com.dbumama.market.service.api.order.OrderTuanResultDto;
import com.dbumama.market.service.api.order.PrintExpResultDto;
import com.dbumama.market.service.api.order.PrintInvResultDto;
import com.dbumama.market.service.api.order.PrintParamDto;
import com.dbumama.market.service.api.order.PrintService;
import com.dbumama.market.service.api.order.RefundParamDto;
import com.dbumama.market.service.api.order.SendGoodParamDto;
import com.dbumama.market.web.core.annotation.RouteBind;
import com.dbumama.market.web.core.controller.AdminBaseController;
import com.dbumama.market.web.core.plugin.spring.Inject.BY_NAME;
import com.jfinal.plugin.activerecord.Page;

@RouteBind(path="order")
public class OrderController extends AdminBaseController<Order>{

	@BY_NAME
	private OrderService orderService;
	@BY_NAME
	private PrintService printService;
	@BY_NAME
	private ExpTemplateService expressTemplateService;
	public void index(){
		//查询用户已添加的快递模板
		List<ExpressTemplate> templates = expressTemplateService.getUserExpTemplate(getSellerId());
		setAttr("expressList", templates);
		render("/order/o_index.html");
	}
	
	public void agent(){
		//查询用户已添加的快递模板
		List<ExpressTemplate> templates = expressTemplateService.getUserExpTemplate(getSellerId());
		setAttr("expressList", templates);
		render("/order/o_agent_index.html");
	}
	
	public void tuan(){
		//查询用户已添加的快递模板
		List<ExpressTemplate> templates = expressTemplateService.getUserExpTemplate(getSellerId());
		setAttr("expressList", templates);
		render("/order/o_tuan_index.html");
	}
	
	public void list(){
		OrderListParamDto orderParamDto = new OrderListParamDto(getSellerId(), getPageNo());
		orderParamDto.setStartDate(getPara("startDate"));
		orderParamDto.setEndDate(getPara("endDate"));
		orderParamDto.setBuyerNickName(getPara("nickNmae"));
		orderParamDto.setReceiverPhone(getPara("receiverPhone"));
		orderParamDto.setReceiverName(getPara("receiverName"));
		orderParamDto.setOrderStatus(getPara("orderStatus"));
		orderParamDto.setPaymentStatus(getPara("paymentStatus"));
		orderParamDto.setShippingStatus(getPara("shippingStatus"));
		try {
			//合并订单  买家收货地址跟收货人做map key 对应的交易数据用List装
			Page<OrderResultDto> orders = orderService.list(orderParamDto);
			rendSuccessJson(orders);
		} catch (OrderException e) {
			rendFailedJson(e.getMessage());
		}
	}
	
	public void list4agent(){
		OrderListParamDto orderParamDto = new OrderListParamDto(getSellerId(), getPageNo());
		orderParamDto.setStartDate(getPara("startDate"));
		orderParamDto.setEndDate(getPara("endDate"));
		orderParamDto.setBuyerNickName(getPara("nickNmae"));
		orderParamDto.setReceiverPhone(getPara("receiverPhone"));
		orderParamDto.setReceiverName(getPara("receiverName"));
		orderParamDto.setOrderStatus(getPara("orderStatus"));
		orderParamDto.setPaymentStatus(getPara("paymentStatus"));
		orderParamDto.setShippingStatus(getPara("shippingStatus"));
		try {
			//合并订单  买家收货地址跟收货人做map key 对应的交易数据用List装
			Page<OrderAgentResultDto> orders = orderService.list4Agent(orderParamDto);
			rendSuccessJson(orders);
		} catch (OrderException e) {
			rendFailedJson(e.getMessage());
		}
	}
	
	public void list4tuan(){
		OrderListParamDto orderParamDto = new OrderListParamDto(getSellerId(), getPageNo());
		orderParamDto.setStartDate(getPara("startDate"));
		orderParamDto.setEndDate(getPara("endDate"));
		orderParamDto.setOrderStatus(getPara("orderStatus"));
		try {
			//合并订单  买家收货地址跟收货人做map key 对应的交易数据用List装
			Page<OrderTuanResultDto> orders = orderService.list4Tuan(orderParamDto);
			rendSuccessJson(orders);
		} catch (OrderException e) {
			rendFailedJson(e.getMessage());
		}
	}
	
	/**
	 * 获取快递单打印数据
	 */
	public void getExpPrintData(){
		try {
			String exp = getPara("exp");
			PrintParamDto printParamDto = new PrintParamDto(getSellerId(), getPara("ids"), exp);
			PrintExpResultDto orderPrintResultDto = printService.getExpressPrintData(printParamDto);
			rendSuccessJson(orderPrintResultDto);
		} catch (OrderException e) {
			rendFailedJson(e.getMessage());
		}
	}
	
	/**
	 * 获取发货单打印数据
	 */
	public void getInvPrintData(){
		try {
			PrintParamDto printParamDto = new PrintParamDto(getSellerId(), getPara("ids"));
			PrintInvResultDto printInvResultDto = printService.getInvoicePrintData(printParamDto);
			rendSuccessJson(printInvResultDto);
		} catch (OrderException e) {
			rendFailedJson(e.getMessage());
		}
	}
	
	/**
	 * 发货详情
	 */
	public void sendgoodsList(){
		String orderIds=getPara("orderIds");
		List<OrderResultDto> orderDetailResultDto=orderService.getOrders(orderIds);
		setAttr("expresscomps", expressTemplateService.getUserExpComps(getSellerId()));
		setAttr("orderDetailResultDto", orderDetailResultDto);
		setAttr("orderIds", orderIds);
		render("/order/send_goods.html");
	}
	
	/**
	 * 批量发货列表
	 */
	public void sendMoreGoodsList(){
		setAttr("expresscomps", expressTemplateService.getUserExpComps(getSellerId()));
		setAttr("orderIds",getPara("ids"));
		setAttr("order_type",getPara("order_type"));
		render("/order/send_more_goods.html");
	}
	
	public void sendMoregoods(){
		String orderIds=getPara("orderIds");
		String order_type=getPara("order_type");
		try {
			//拼团订单
			if(order_type.equals("2")){
				List<OrderTuanResultDto> orderTuanResultDto=orderService.groupSendsMore(orderIds);
				rendSuccessJson(orderTuanResultDto);
			}else{
			//其它订单 
			Map<String, String> statusMap = new HashMap<String, String>();
			statusMap.put("p", "paid");//查询出已支付，未发货订单
			List<OrderResultDto> orderDetailResultDto=orderService.getOrders(orderIds, statusMap);
			rendSuccessJson(orderDetailResultDto);
			}
		} catch (OrderException e) {
			rendFailedJson(e.getMessage());
		}
	}
	/**
	 * 批量保存发货信息
	 */
	public void saveSendMoreGoods(){
		final String items = getPara("items");
		try {
			orderService.batchShipping(items);
			rendSuccessJson("发货成功");
		} catch (OrderException e) {
			rendFailedJson(e.getMessage());
		}
	}
	/**
	 * 保存发货信息
	 */
	public void saveSendGoods(){
		final Long orderIds = getParaToLong("orderIds");
		final String exp_key = getPara("exp_key");
		final String bill_number = getPara("bill_number");
		SendGoodParamDto sendGoodParam = new SendGoodParamDto();
		sendGoodParam.setOrderId(orderIds);
		sendGoodParam.setExpKey(exp_key);
		sendGoodParam.setBillNumber(bill_number);
		try {
			orderService.shipping(sendGoodParam);
			rendSuccessJson();
		} catch (Exception e) {
			rendFailedJson(e.getMessage());
		}
	}
	
	/**
	 * 退款
	 */
	public void refund(){
		String orderIds=getPara("orderIds");
		List<OrderResultDto> orderDetailResultDto=orderService.getOrders(orderIds);
		setAttr("orderDetailResultDto", orderDetailResultDto);
		setAttr("orderIds", orderIds);
		render("/order/refund.html");
	}
	
	/**
	 * 保存退款信息
	 */
	public void saveRefund(){
		final Long orderId = getParaToLong("orderIds");
		final  BigDecimal cash=new BigDecimal(getPara("cash"));
		RefundParamDto refundParam = new RefundParamDto();
		refundParam.setOrderId(orderId);
		refundParam.setRefundFee(cash);
		try {
			orderService.refund(refundParam);
			rendSuccessJson("退款成功");
		} catch (OrderException e) {
			rendFailedJson(e.getMessage());
		}
	}
	
	@Override
	protected Class<Order> getModelClass() {
		return Order.class;
	}

}
