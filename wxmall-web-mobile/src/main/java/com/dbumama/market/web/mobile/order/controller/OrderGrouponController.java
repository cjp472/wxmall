package com.dbumama.market.web.mobile.order.controller;

import java.util.List;

import com.dbumama.market.service.api.order.OrderException;
import com.dbumama.market.service.api.order.OrderListParamDto;
import com.dbumama.market.service.api.order.OrderMobileResultDto;
import com.dbumama.market.service.api.order.OrderService;
import com.dbumama.market.web.core.annotation.RouteBind;
import com.dbumama.market.web.core.controller.BaseMobileController;
import com.dbumama.market.web.core.plugin.spring.Inject.BY_NAME;

@RouteBind(path ="order/group", viewPath="order")
public class OrderGrouponController extends BaseMobileController{

	@BY_NAME
	private OrderService orderService;
	
	public void index(){
		//setAttr("order_status", "success");
		render("g_index.html");
	}
	
	public void success(){
		setAttr("group_status", "success");
		render("g_index.html");
	}
	
	public void fail(){
		setAttr("group_status", "fail");
		render("g_index.html");
	}
	
	public void grouping(){
		setAttr("group_status", "grouping");
		render("g_index.html");
	}
	
	public void list(){
		OrderListParamDto orderParamDto = new OrderListParamDto(getSellerId(), getBuyerId(), getPageNo());
		orderParamDto.setOrderStatus(getPara("order_status"));
		try {
			List<OrderMobileResultDto> orderListResultDtos = orderService.list4MobileGroup(orderParamDto);
			rendSuccessJson(orderListResultDtos);
		} catch (OrderException e) {
			rendFailedJson(e.getMessage());
		}
	}
	
}
