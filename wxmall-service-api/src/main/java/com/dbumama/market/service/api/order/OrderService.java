package com.dbumama.market.service.api.order;

import java.util.List;
import java.util.Map;

import com.dbumama.market.service.api.pay.PayException;
import com.dbumama.market.service.api.ump.UmpException;
import com.jfinal.plugin.activerecord.Page;

public interface OrderService {

	/**
	 * 管理后台查询订单列表
	 * @param orderParamDto
	 * @return
	 * @throws OrderException
	 */
	public Page<OrderResultDto> list(OrderListParamDto orderParamDto) throws OrderException;
	
	/**
	 * 管理端获取分销商订单
	 * @param orderParamDto
	 * @return
	 * @throws OrderException
	 */
	public Page<OrderAgentResultDto> list4Agent(OrderListParamDto orderParamDto) throws OrderException;
	
	/**
	 * 管理端获取拼团订单
	 * @param orderParamDto
	 * @return
	 * @throws OrderException
	 */
	public Page<OrderTuanResultDto> list4Tuan(OrderListParamDto orderParamDto) throws OrderException;
	
	/**
	 * 管理端获取拼团订单 批量发货列表
	 * @param orderIds
	 * @return
	 * @throws OrderException
	 */
	public List<OrderTuanResultDto> groupSendsMore(String orderIds) throws OrderException;
	
	/**
	 * 微信端创建订单
	 * @param orderParamDto
	 * @throws OrderException
	 */
	public Long create(OrderCreateParamDto orderParamDto) throws OrderException;
	
	/**
	 * 微信端创建拼团订单
	 * @param orderParamDto
	 * @return
	 * @throws OrderException
	 */
	public Long gcreate(OrderCreateParamDto orderParamDto) throws OrderException;
	
	/**
	 * 微信端取消订单
	 * @param orderId
	 * @throws OrderException
	 */
	public void cancel(Long orderId) throws OrderException;
	
	/**
	 * 微信端取消订单，支持拼团
	 * @param orderId
	 * @param buyerId
	 * @throws OrderException
	 */
	public void cancel(Long orderId, Long buyerId) throws OrderException;
	
	/**
	 * 微信端订单确认收货
	 * @param orderId
	 * @throws OrderException
	 */
	public void confirm(Long orderId) throws OrderException;
	
	/**
	 * 微信端订单列表
	 * @return
	 * @throws OrderException
	 */
	public List<OrderMobileResultDto> list4Mobile(OrderListParamDto orderParamDto) throws OrderException;
	
	/**
	 * 微信端获取拼团订单列表
	 * @param orderParamDto
	 * @return
	 * @throws OrderException
	 */
	public List<OrderMobileResultDto> list4MobileGroup(OrderListParamDto orderParamDto) throws OrderException;
	
	/**
	 * 订单详情
	 * @param orderId
	 * @return
	 * @throws OrderException
	 */
	public OrderResultDto getOrder(Long orderId) throws OrderException;
	
	/**
	 * 获取订单支付信息
	 * @param orderId
	 * @param userId
	 * @return
	 * @throws OrderException
	 */
	public OrderPayResultDto getOrderPayInfo(Long orderId, Long userId) throws OrderException;
	
	/**
	 * 获取拼团订单详情
	 * @param orderId
	 * @param buyerId
	 * @return
	 * @throws OrderException
	 */
	public OrderResultDto getOrder(Long orderId, Long buyerId) throws OrderException;
	
	/**
	 * 批量获取订单详情
	 * @param orderIds
	 * @return
	 * @throws OrderException
	 */
	public List<OrderResultDto> getOrders(String orderIds) throws OrderException;

	/**
	 * 根据状态批量获取订单
	 * @param orderIds
	 * @param statusMap
	 * @return
	 * @throws OrderException
	 */
	public List<OrderResultDto> getOrders(String orderIds, Map<String, String> statusMap) throws OrderException;
	
	/**
	 * 批量发货
	 * @param items
	 * @throws OrderException
	 */
	public void batchShipping(String items) throws OrderException;
	
	/**
	 * 发货
	 * @param sendGoodParam
	 * @throws OrderException
	 */
	public String shipping(SendGoodParamDto sendGoodParam) throws OrderException;
	
	/**
	 * 订单原路退款
	 * @param orderId
	 * @throws OrderException
	 */
	public void refund(RefundParamDto refundParam) throws OrderException;
	
	/**
	 * 结算订单
	 * @param 	buyerId 买家
	 * @param 	receiverId 收货地址
	 * @param	items 商品项
	 * @return
	 * @throws PayException
	 */
	public OrderResultDto balance (Long buyerId, Long receiverId, String items) throws OrderException;
	
	/**
	 * 针对拼团订单做结算
	 * @param buyerId
	 * @param receiverId
	 * @param items
	 * @return
	 * @throws OrderException
	 */
	public OrderResultDto gbalance(Long buyerId, Long receiverId, String items) throws OrderException;
	
	/**
	 * 创建参团订单
	 * @param productId 	参团商品
	 * @param userId		团发起者
	 * @param joinUserId	参团者
	 * @param receiverId	收获地址
	 * @param items			商品数据
	 * @throws UmpException
	 */
	public Long joinGroup(OrderJoinParamDto orderJoinParam) throws OrderException;
}
