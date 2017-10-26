package com.dbumama.market.web.api.order.controller;

import java.util.Date;
import java.util.List;

import com.dbumama.market.model.ProductReview;
import com.dbumama.market.service.api.order.OrderCreateParamDto;
import com.dbumama.market.service.api.order.OrderException;
import com.dbumama.market.service.api.order.OrderListParamDto;
import com.dbumama.market.service.api.order.OrderMobileResultDto;
import com.dbumama.market.service.api.order.OrderResultDto;
import com.dbumama.market.service.api.order.OrderService;
import com.dbumama.market.web.core.annotation.RouteBind;
import com.dbumama.market.web.core.controller.BaseApiController;
import com.dbumama.market.web.core.plugin.spring.Inject.BY_NAME;

@RouteBind(path = "order")
public class OrderController extends BaseApiController{
	@BY_NAME
	private OrderService orderService;
	
	//创建订单
	public void create(){
		final String memo = getJSONPara("memo");
		final String items = getJSONPara("items");
		OrderCreateParamDto orderParamDto = new OrderCreateParamDto(getSellerId(), getBuyerId(), getJSONParaToLong("receiverId"), items);
		orderParamDto.setMemo(memo);	//买家留言
		try {
			rendSuccessJson(orderService.create(orderParamDto));
		} catch (OrderException e) {
			rendFailedJson(e.getMessage());
		}
	}
	
	public void list(){
		OrderListParamDto orderParamDto = new OrderListParamDto(getSellerId(), getBuyerId(), getPageNo());
		orderParamDto.setOrderStatus(getJSONPara("order_status"));
		orderParamDto.setPaymentStatus(getJSONPara("payment_status"));
		orderParamDto.setShippingStatus(getJSONPara("shipping_status"));
		try {
			List<OrderMobileResultDto> orderListResultDtos = orderService.list4Mobile(orderParamDto);
			rendSuccessJson(orderListResultDtos);
		} catch (OrderException e) {
			rendFailedJson(e.getMessage());
		}
	}
	
	/**
	 * 订单详情
	 */
	public void detail(){
		try{
		    OrderResultDto orderDetail = orderService.getOrder(getJSONParaToLong("orderId"), getBuyerId());
		    rendSuccessJson(orderDetail);
		}catch(Exception ex){
			rendFailedJson(ex.getMessage());
		}
	}
	
	public void cancel(){
		try {
			orderService.cancel(getJSONParaToLong("orderId"), getBuyerId());
			rendSuccessJson();
		} catch (OrderException e) {
			rendFailedJson(e.getMessage());
		}
	}
	
	/**
	 * 去评价
	 */
	public void getOrderInfo(){
		try{
			Long orderId = getJSONParaToLong("orderId");
			OrderResultDto dto=orderService.getOrder(orderId);
			rendSuccessJson(dto);
		}catch(Exception ex){
			rendFailedJson(ex.getMessage());
		}
		
	}
	
	/**
	 * 保存评价
	 */
	public void saveReview(){
		Long orderId = getJSONParaToLong("orderId");
		Long productId = getJSONParaToLong("productId");
		String content=getJSONPara("content");
		Integer score=getJSONParaToInteger("score");
		ProductReview review=new ProductReview();
		review.setOrderId(orderId);
		review.setProductId(productId);
		review.setContent(content);
		review.setScore(score);
		review.setBuyerId(getBuyerId());
		review.setActive(true);
		review.setCreated(new Date());
		review.setUpdated(new Date());
		review.setIsShow(true);
		try {
			review.save();
			rendSuccessJson("评论成功");
		} catch (Exception e) {
			rendFailedJson("评论失败，请重新评论");
		}
	}
	
}
