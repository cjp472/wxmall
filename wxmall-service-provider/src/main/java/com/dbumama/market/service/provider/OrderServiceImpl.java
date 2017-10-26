package com.dbumama.market.service.provider;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dbumama.market.model.Agent;
import com.dbumama.market.model.AgentCommRcd;
import com.dbumama.market.model.BuyerCard;
import com.dbumama.market.model.BuyerReceiver;
import com.dbumama.market.model.BuyerUser;
import com.dbumama.market.model.Card;
import com.dbumama.market.model.Cart;
import com.dbumama.market.model.DeliverySet;
import com.dbumama.market.model.DeliveryTemplate;
import com.dbumama.market.model.MemberRank;
import com.dbumama.market.model.MultiGroup;
import com.dbumama.market.model.Order;
import com.dbumama.market.model.OrderGheader;
import com.dbumama.market.model.OrderGuser;
import com.dbumama.market.model.OrderItem;
import com.dbumama.market.model.Product;
import com.dbumama.market.model.ProductSpecItem;
import com.dbumama.market.model.Refund;
import com.dbumama.market.model.Shipping;
import com.dbumama.market.model.SpecificationValue;
import com.dbumama.market.service.api.agent.AgentCommssionResultDto;
import com.dbumama.market.service.api.agent.AgentException;
import com.dbumama.market.service.api.agent.AgentService;
import com.dbumama.market.service.api.order.OrderAgentResultDto;
import com.dbumama.market.service.api.order.OrderCreateParamDto;
import com.dbumama.market.service.api.order.OrderException;
import com.dbumama.market.service.api.order.OrderItemResultDto;
import com.dbumama.market.service.api.order.OrderJoinParamDto;
import com.dbumama.market.service.api.order.OrderListParamDto;
import com.dbumama.market.service.api.order.OrderMobileResultDto;
import com.dbumama.market.service.api.order.OrderPayResultDto;
import com.dbumama.market.service.api.order.OrderResultDto;
import com.dbumama.market.service.api.order.OrderService;
import com.dbumama.market.service.api.order.OrderTuanGuserInfoDto;
import com.dbumama.market.service.api.order.OrderTuanResultDto;
import com.dbumama.market.service.api.order.RefundParamDto;
import com.dbumama.market.service.api.order.SendGoodParamDto;
import com.dbumama.market.service.api.serinum.SerinumService;
import com.dbumama.market.service.api.ump.FullCutService;
import com.dbumama.market.service.api.ump.GrouponService;
import com.dbumama.market.service.api.ump.ProdFullCutResultDto;
import com.dbumama.market.service.api.ump.PromotionService;
import com.dbumama.market.service.api.ump.UmpException;
import com.dbumama.market.service.base.AbstractServiceImpl;
import com.dbumama.market.service.enmu.GroupStatus;
import com.dbumama.market.service.enmu.OrderStatus;
import com.dbumama.market.service.enmu.OrderType;
import com.dbumama.market.service.enmu.PaymentStatus;
import com.dbumama.market.service.enmu.ShippingStatus;
import com.dbumama.market.service.sql.QueryHelper;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.ActiveRecordException;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.weixin.sdk.api.ApiResult;
import com.weixin.sdk.api.CardApi;
import com.weixin.sdk.api.TemplateData;
import com.weixin.sdk.api.TemplateMsgApi;
import com.weixin.sdk.kit.ParaMap;
import com.weixin.sdk.kit.WxSdkPropKit;

@Service("orderService")
public class OrderServiceImpl extends AbstractServiceImpl implements OrderService{

	@Autowired
	private PromotionService promotionService;	//限时打折
	@Autowired
	private SerinumService serinumService;		//序号
	@Autowired
	private FullCutService fullCutService;		//满减
	@Autowired
	private AgentService agentService;			//分销
	@Autowired
	private GrouponService grouponService;		//拼团

	private static final Agent agentDao = new Agent().dao();
	private static final BuyerCard buyerCardDao = new BuyerCard().dao();
	private static final BuyerReceiver receiverDao = new BuyerReceiver().dao();
	private static final BuyerUser buyerUserdao = new BuyerUser().dao();
	private static final Card cardDao = new Card().dao();
	private static final Cart cartDao = new Cart().dao();
	private static final DeliverySet deliveryDao = new DeliverySet().dao();
	private static final DeliveryTemplate deliveryTpldao = new DeliveryTemplate().dao();
	private static final MemberRank mbRankdao = new MemberRank().dao();
	private static final MultiGroup multiGroupdao = new MultiGroup().dao();
	private static final Order orderdao = new Order().dao();
	private static final OrderGheader orderGheaderdao = new OrderGheader().dao();
	private static final OrderGuser orderGuserdao = new OrderGuser().dao();
	private static final OrderItem orderItemdao = new OrderItem().dao();
	private static final Product productDao = new Product().dao();
	private static final ProductSpecItem prodSpecItemdao = new ProductSpecItem().dao();
	private static final SpecificationValue specValueDao = new SpecificationValue().dao();
	
	@Override
	public Page<OrderResultDto> list(OrderListParamDto orderParamDto) throws OrderException {
		if(orderParamDto == null || orderParamDto.getSellerId() == null)
			throw new OrderException("查询订单参数错误");
		
		String select = " SELECT  o.*, o.id as o_id, o.created as o_created, b.id as b_id, b.nickname, b.open_id, r.*, r.phone as r_phone, r.city as r_city, r.province as r_province, r.district as r_district ";
		String sqlExceptSelect = " FROM "+Order.table+" o "
				+ " left join " + BuyerUser.table + " b on o.buyer_id=b.id "
				+ " left join " + BuyerReceiver.table + " r on o.receiver_id=r.id "
				+ " where 1=1 and (o.order_type = 0 or o.order_type is null) ";
		
		StringBuffer where = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		setQuery(where, params, orderParamDto);
		sqlExceptSelect = sqlExceptSelect + " " + where.toString() + " order by o.created desc ";
		Page<Record> orders = Db.paginate(orderParamDto.getPageNo(), orderParamDto.getPageSize(), select, sqlExceptSelect, params.toArray());
		List<OrderResultDto> orderList = new ArrayList<OrderResultDto>();
		for(Record order : orders.getList()){
			OrderResultDto orderListResultDto = getOrderResult(order);
			orderList.add(orderListResultDto);
		}
		
		return new Page<OrderResultDto>(orderList, orderParamDto.getPageNo(), orderParamDto.getPageSize(), orders.getTotalPage(), orders.getTotalRow());
	}
	
	@Override
	public Page<OrderAgentResultDto> list4Agent(OrderListParamDto orderParamDto) throws OrderException {
		String select = " SELECT  o.*, o.id as o_id, o.created as o_created, b.id as b_id, b.nickname, b.open_id, r.*, r.phone as r_phone, r.city as r_city, r.province as r_province, r.district as r_district ";
		String sqlExceptSelect = " FROM "+Order.table+" o "
				+ " left join " + BuyerUser.table + " b on o.buyer_id=b.id "
				+ " left join " + BuyerReceiver.table + " r on o.receiver_id=r.id "
				+ " where 1=1 and o.order_type = 1 ";
		
		StringBuffer where = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		setQuery(where, params, orderParamDto);
		sqlExceptSelect = sqlExceptSelect + " " + where.toString() + " order by o.created desc ";
		Page<Record> orders = Db.paginate(orderParamDto.getPageNo(), orderParamDto.getPageSize(), select, sqlExceptSelect, params.toArray());
		List<OrderAgentResultDto> orderList = new ArrayList<OrderAgentResultDto>();
		for(Record order : orders.getList()){
			OrderAgentResultDto agentOrder = getOrderAgentResult(order);
			orderList.add(agentOrder);
		}
		return new Page<OrderAgentResultDto>(orderList, orderParamDto.getPageNo(), orderParamDto.getPageSize(), orders.getTotalPage(), orders.getTotalRow());
	}
	
	@Override
	public Page<OrderTuanResultDto> list4Tuan(OrderListParamDto orderParamDto) throws OrderException {
		String select = " SELECT  o.*, o.id as o_id, o.created as o_created, ogh.*, ogh.id as g_header_id ";
		String sqlExceptSelect = " FROM "+Order.table+" o "
				+ " inner join " + OrderGheader.table + " ogh on o.id=ogh.order_id"
				+ " where 1=1 and o.order_type = 2 and ogh.active=1 ";
		StringBuffer where = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		setQuery(where, params, orderParamDto);
		sqlExceptSelect = sqlExceptSelect + " " + where.toString() + " order by o.created desc ";
		Page<Record> orders = Db.paginate(orderParamDto.getPageNo(), orderParamDto.getPageSize(), select, sqlExceptSelect, params.toArray());
		List<OrderTuanResultDto> orderList = new ArrayList<OrderTuanResultDto>();
		for(Record order : orders.getList()){
			OrderTuanResultDto agentOrder = getOrderTuanResult(order);
			orderList.add(agentOrder);
		}
		return new Page<OrderTuanResultDto>(orderList, orderParamDto.getPageNo(), orderParamDto.getPageSize(), orders.getTotalPage(), orders.getTotalRow());	
	}
	
	//统一设置订单公共查询条件
	private void setQuery(StringBuffer where, List<Object> params, OrderListParamDto orderParamDto){
		where.append(" and o.seller_id=?");
		params.add(orderParamDto.getSellerId());
		
		if(StrKit.notBlank(orderParamDto.getStartDate()) && StrKit.notBlank(orderParamDto.getEndDate())){
			where.append(" and (o.created between ? and ?) ");
			params.add(orderParamDto.getStartDate());
			params.add(orderParamDto.getEndDate());
		}
		
		if(StrKit.notBlank(orderParamDto.getBuyerNickName())){
			where.append(" and b.nickname like ?");
			params.add("%"+orderParamDto.getBuyerNickName()+"%");
		}
		
		if(StrKit.notBlank(orderParamDto.getReceiverName())){
			where.append(" and r.receiver_name = ?");
			params.add(orderParamDto.getReceiverName());
		}
		
		if(StrKit.notBlank(orderParamDto.getReceiverPhone())){
			where.append(" and r.receiver_phone = ?");
			params.add(orderParamDto.getReceiverPhone());
		}
		
		if(StrKit.notBlank(orderParamDto.getOrderStatus())){
			final String osArr [] = orderParamDto.getOrderStatus().split("_");
			String type = "";
			String value = "";
			try {
				type = osArr[0];
				value = osArr[1];
			} catch (Exception e) {
				throw new OrderException("不支持的订单状态");
			}
			setOrderStatusCondition(type, value, where, params);
		}
	}

	@Override
	@Transactional(rollbackFor = OrderException.class)
	public Long create(OrderCreateParamDto orderParamDto) throws OrderException {
		if(orderParamDto == null || orderParamDto.getSellerId() == null 
				|| orderParamDto.getBuyerId() == null || orderParamDto.getReceiverId() == null
				|| StrKit.isBlank(orderParamDto.getItems())){
			throw new OrderException("创建订单失败，请检查参数");
		}
		
		//解析出提交的订单数据
		OrderResultDto orderDto = null;
		try {
			orderDto = balance(orderParamDto.getBuyerId(), orderParamDto.getReceiverId(), orderParamDto.getItems());
		} catch (OrderException e) {
			throw new OrderException(e.getMessage());
		}
		if(orderDto == null) throw new OrderException("创建订单失败，解析订单数据出错");
		
		Order order = new Order();
		order.setSellerId(orderParamDto.getSellerId());
		order.setBuyerId(orderParamDto.getBuyerId());
		order.setReceiverId(orderParamDto.getReceiverId());
		order.setOrderSn(serinumService.getOrderSn());
		order.setOrderType(orderDto.getOrderType());		//订单类型
		order.setOrderStatus(OrderStatus.unconfirmed.ordinal());
		order.setPaymentStatus(PaymentStatus.unpaid.ordinal());
		order.setShippingStatus(ShippingStatus.unshipped.ordinal());
		order.setMemo(orderParamDto.getMemo());
		order.setTotalPrice(orderDto.getTotalPrice());		//商品价格总和，不含邮费
		order.setPostFee(orderDto.getPostFee());			//订单邮费
		BigDecimal payFee = order.getTotalPrice().add(order.getPostFee());	//最终支付金额
		order.setPayFee(payFee);
		order.setTradeNo(getTradeNo());
		order.setActive(true);
		order.setCreated(new Date());
		order.setUpdated(new Date());
		//设置订单类型
		Agent agent = agentDao.findFirst("select * from " + Agent.table + " where buyer_id=? and active=1 and status=1 ", order.getBuyerId());
		//检查是否分销订单
		if(agent != null){
			order.setOrderType(OrderType.agent.ordinal());
		}
		
		try {
			order.save();
		} catch (ActiveRecordException e) {
			throw new OrderException(e.getMessage());
		}
		
		//保存订单项数据
		List<OrderItem> orderItems = new ArrayList<OrderItem>();
		for (OrderItemResultDto orderItemDto : orderDto.getOrderItems()) {
			Product product = productDao.findById(orderItemDto.getProductId());
			OrderItem orderItem = new OrderItem();
			orderItem.setSn(product.getSn());
			orderItem.setName(product.getName());
			orderItem.setProductImg(product.getImage());
			orderItem.setQuantity(orderItemDto.getQuantity());
			orderItem.setProductId(product.getId());
			orderItem.setOrderId(order.getId());
			if(orderItemDto.getSpecificationValues() != null){
				final StringBuffer sbff = new StringBuffer();
				for(SpecificationValue sfv : orderItemDto.getSpecificationValues()){
					sbff.append(sfv.getId()).append(",");
				}
				orderItem.setSpecificationValue(sbff.length()>0 ? sbff.deleteCharAt(sbff.length()-1).toString() : sbff.toString());
			}
			orderItem.setPrice(new BigDecimal(orderItemDto.getPrice()));
			orderItem.setActive(true);
			orderItem.setCreated(new Date());
			orderItem.setUpdated(new Date());
			orderItems.add(orderItem);
		}
		
		try {
			Db.batchSave(orderItems, orderItems.size());
		} catch (ActiveRecordException e) {
			throw new OrderException(e.getMessage());
		}
		
		//如果是分销订单，分配订单佣金
		if(order.getOrderType() == OrderType.agent.ordinal()){
			//1.检查该用户是否有上级分销商
			//2.根据分销规则给上级分销商分佣
			List<Agent> agents = agentService.getSelfAndParents(agent.getId());
			for(Agent ag : agents){
				try {
					agentService.setAgentCommission(ag, order);
				} catch (AgentException e) {
					logger.error(e.getMessage());//设置佣金失败的原因是否记录到数据库
				}
			}
		}
		
		//创建订单成功后检查是否有购物车数据，删除掉
		for(OrderItemResultDto tempOrderItem : orderDto.getOrderItems()){
			Cart c = cartDao.findFirst(" select * from " + Cart.table + " where buyer_id=? and product_id=? ", orderParamDto.getBuyerId(), tempOrderItem.getProductId());
			if(c!=null) c.delete();
		}
		
		//发送模板消息
		new SendTemplateMsgThread(order).start();
		
		return order.getId();
	}
	
	@Override
	@Transactional(rollbackFor = OrderException.class)
	public Long gcreate(OrderCreateParamDto orderParamDto) throws OrderException {
		if(orderParamDto == null || orderParamDto.getSellerId() == null 
				|| orderParamDto.getBuyerId() == null || orderParamDto.getReceiverId() == null
				|| StrKit.isBlank(orderParamDto.getItems())){
			throw new OrderException("创建拼团订单失败，请检查参数");
		}
		
		//解析出提交的订单数据
		OrderResultDto orderDto = null;
		try {
			orderDto = gbalance(orderParamDto.getBuyerId(), orderParamDto.getReceiverId(), orderParamDto.getItems());
		} catch (OrderException e) {
			throw new OrderException(e.getMessage());
		}
		if(orderDto == null) throw new OrderException("创建拼团订单失败，解析订单数据出错");
		
		if(orderDto.getOrderItems() == null || orderDto.getOrderItems().size()<=0) throw new OrderException("创建拼团订单失败，商品不存在");
		
		//理论上这里拼团只有一个商品
		OrderItemResultDto orderItemDto = orderDto.getOrderItems().get(0);
		Product product = productDao.findById(orderItemDto.getProductId());
		MultiGroup group = grouponService.getProductMultiGroup(product);
		if(group == null) throw new OrderException("创建拼团订单失败，商品拼团活动已失效");
		
		//同一个用户同一个商品同时只能开一个团，就是自己作为团长的话，只能有一个团存在
		OrderGheader gHeader = orderGheaderdao.findFirst("select * from " + OrderGheader.table + 
				" where buyer_id=? and product_id= ? and group_id=? and active=1", orderParamDto.getBuyerId(), product.getId(), group.getId());
		if(gHeader != null) throw new OrderException("您有该商品的拼团活动正在进行中");
		
		Order order = new Order();
		order.setSellerId(orderParamDto.getSellerId());
		order.setBuyerId(orderParamDto.getBuyerId());
		order.setReceiverId(orderParamDto.getReceiverId());
		order.setOrderSn(serinumService.getOrderSn());
		order.setOrderType(orderDto.getOrderType());		//订单类型  拼团订单
		order.setOrderStatus(OrderStatus.unconfirmed.ordinal());
		order.setPaymentStatus(PaymentStatus.unpaid.ordinal());
		order.setShippingStatus(ShippingStatus.unshipped.ordinal());
		order.setGroupStatus(GroupStatus.grouping.ordinal());//拼团中
		//order.setGroupId(group.getId()); 	//当前拼团订单属于哪一个拼团活动
		/*order.setMemo(orderParamDto.getMemo());
		order.setTotalPrice(orderDto.getTotalPrice());		//商品价格总和，不含邮费
		order.setPostFee(orderDto.getPostFee());			//订单邮费
		BigDecimal payFee = order.getTotalPrice().add(order.getPostFee());	//最终支付金额
		order.setPayFee(payFee);
		order.setTradeNo(getTradeNo());*/
		order.setActive(true);
		order.setCreated(new Date());
		order.setUpdated(new Date());
		
		try {
			order.save();
		} catch (Exception e) {
			throw new OrderException(e.getMessage());
		}
		
		//保存订单项数据
		OrderItem orderItem = new OrderItem();
		orderItem.setSn(product.getSn());
		orderItem.setName(product.getName());
		orderItem.setProductImg(product.getImage());
		orderItem.setQuantity(orderItemDto.getQuantity());
		orderItem.setProductId(product.getId());
		orderItem.setOrderId(order.getId());
		if(orderItemDto.getSpecificationValues() != null){
			final StringBuffer sbff = new StringBuffer();
			for(SpecificationValue sfv : orderItemDto.getSpecificationValues()){
				sbff.append(sfv.getId()).append(",");
			}
			orderItem.setSpecificationValue(sbff.length()>0 ? sbff.deleteCharAt(sbff.length()-1).toString() : sbff.toString());
		}
		orderItem.setPrice(new BigDecimal(orderItemDto.getPrice()));
		orderItem.setActive(true);
		orderItem.setCreated(new Date());
		orderItem.setUpdated(new Date());
		try {
			orderItem.save();
		} catch (Exception e) {
			throw new OrderException(e.getMessage());
		}
		
		//记录开团数据
		gHeader = new OrderGheader();
		gHeader.setBuyerId(order.getBuyerId());
		gHeader.setGroupId(group.getId());
		gHeader.setOrderId(order.getId());
		gHeader.setProductId(product.getId());
		gHeader.setActive(true);
		gHeader.setCreated(new Date());
		gHeader.setUpdated(new Date());
		try {
			gHeader.save();
		}catch(Exception e){
			throw new OrderException(e.getMessage()); 
		}
		
		//记录创建团的用户参团信息
		OrderGuser guser = new OrderGuser();
		guser.setBuyerId(order.getBuyerId());
		guser.setGheaderId(gHeader.getId());
		guser.setReceiverId(order.getReceiverId());
		guser.setPrice(orderItem.getPrice());
		guser.setQuantity(orderItem.getQuantity());
		guser.setSpecValue(orderItem.getSpecificationValue());
		guser.setPostFee(orderDto.getPostFee());
		BigDecimal payFee = orderDto.getTotalPrice().add(orderDto.getPostFee());	//最终支付金额
		guser.setPayFee(payFee);
		guser.setPaymentStatus(PaymentStatus.unpaid.ordinal());
		guser.setMemo(orderParamDto.getMemo());
		guser.setTradeNo(getTradeNo());
		guser.setActive(true);
		guser.setCreated(new Date());
		guser.setUpdated(new Date());
		try {
			guser.save();
		} catch (Exception e) {
			throw new OrderException(e.getMessage());
		}

		return order.getId();
	}

	@Override
	public List<OrderMobileResultDto> list4Mobile(OrderListParamDto orderParamDto) throws OrderException {
		if(orderParamDto == null || orderParamDto.getSellerId() == null || orderParamDto.getBuyerId() == null)
			throw new OrderException("调用手机端订单列表数据接口缺少参数");
		
		String select = " SELECT  * ";
		String sqlExceptSelect = " FROM "+Order.table;

		QueryHelper queryHelper = new QueryHelper(select, sqlExceptSelect);
		queryHelper.addWhere("seller_id", orderParamDto.getSellerId())
		.addWhereNOT_EQUAL("order_type", 2)//排除拼团订单
		.addWhere("buyer_id", orderParamDto.getBuyerId())
		.addWhere("payment_status", StrKit.notBlank(orderParamDto.getPaymentStatus()) ? PaymentStatus.valueOf(orderParamDto.getPaymentStatus()).ordinal() : null)
		.addWhere("shipping_status", StrKit.notBlank(orderParamDto.getShippingStatus()) ? ShippingStatus.valueOf(orderParamDto.getShippingStatus()).ordinal() : null)
		.addOrderBy("desc", "created");
		
		if(StrKit.notBlank(orderParamDto.getOrderStatus())){
			queryHelper.addWhere("order_status", OrderStatus.valueOf(orderParamDto.getOrderStatus()).ordinal());
		}else{
			queryHelper.addWhereNOT_EQUAL("order_status", OrderStatus.cancelled.ordinal());
		}
		
		queryHelper.build();
		
		Page<Order> orders = orderdao.paginate(orderParamDto.getPageNo(), orderParamDto.getPageSize(), queryHelper.getSelect(), queryHelper.getSqlExceptSelect(), queryHelper.getParams());

		List<OrderMobileResultDto> results = new ArrayList<OrderMobileResultDto>();
		for(Order order : orders.getList()){
			OrderMobileResultDto orderListResultDto = new OrderMobileResultDto();
			orderListResultDto.setCreated(order.getCreated());
			orderListResultDto.setOrderId(order.getId());
			orderListResultDto.setSn(order.getOrderSn());
			if(order.getInt("payment_status") == PaymentStatus.unpaid.ordinal()){
				orderListResultDto.setStatus("待支付");
			}else if(order.getInt("payment_status") == PaymentStatus.paid.ordinal()){
				orderListResultDto.setStatus("已支付");
			}
			if(order.getInt("shipping_status") == ShippingStatus.shipped.ordinal()){
				orderListResultDto.setStatus("已发货");
			}
			if(order.getInt("order_status") == OrderStatus.completed.ordinal()){
				orderListResultDto.setStatus("已完成");
			}else if(order.getInt("order_status") == OrderStatus.cancelled.ordinal()){
				orderListResultDto.setStatus("已取消");
			}
			orderListResultDto.setTotalPrice(order.getPayFee().toString());
			orderListResultDto.setOrderItems(getOrderItems(orderListResultDto.getOrderId(),orderParamDto.getBuyerId()));
			results.add(orderListResultDto);
		}
		
		return results;
	}
	
	@Override
	public List<OrderMobileResultDto> list4MobileGroup(OrderListParamDto orderParamDto) throws OrderException {
		if(orderParamDto == null || orderParamDto.getSellerId() == null || orderParamDto.getBuyerId() == null)
			throw new OrderException("获取手机端拼团订单列表数据缺少必要参数");
		String select =" select og.*, o.*, oi.*, og.created as og_created, og.pay_fee as og_pay_fee, "
				+ "og.price as og_price, ogh.product_id as og_product_id, og.quantity as og_quantity, "
				+ "o.id as o_id, o.buyer_id as o_buyer_id ";
		String sqlExceptSelect = " from " + OrderGuser.table + " og " 
				+ " left join " + OrderGheader.table + " ogh on og.gheader_id=ogh.id "
				+ " left join " + Order.table + " o on ogh.order_id=o.id "
				+ " left join " + OrderItem.table + " oi on ogh.order_id=oi.order_id";
		QueryHelper queryHelper = new QueryHelper(select, sqlExceptSelect);
		queryHelper
		.addWhere("og.buyer_id", orderParamDto.getBuyerId())
		.addWhere("o.group_status", StrKit.notBlank(orderParamDto.getOrderStatus())? GroupStatus.valueOf(orderParamDto.getOrderStatus()).ordinal() : null)
		.addWhere("o.order_type", 2)
		.addOrderBy("desc", "og.created").build();
		
		Page<Record> records = Db.paginate(orderParamDto.getPageNo(), orderParamDto.getPageSize(), queryHelper.getSelect(), queryHelper.getSqlExceptSelect(), queryHelper.getParams());
		List<OrderMobileResultDto> results = new ArrayList<OrderMobileResultDto>();
		for(Record record : records.getList()){
			OrderMobileResultDto orderResultDto = new OrderMobileResultDto();
			results.add(orderResultDto);
			orderResultDto.setCreated(record.getDate("og_created"));
			orderResultDto.setOrderId(record.getLong("o_id"));
			orderResultDto.setBuyerId(record.getLong("o_buyer_id"));//团长Id
			orderResultDto.setSn(record.getStr("order_sn"));
			orderResultDto.setTotalPrice(record.getBigDecimal("og_pay_fee").toString());
			Integer status = record.getInt("group_status");
			if(status == GroupStatus.grouping.ordinal()) orderResultDto.setStatus("组团中");
			else if(status == GroupStatus.success.ordinal()) orderResultDto.setStatus("组团成功");
			else if(status == GroupStatus.fail.ordinal()) orderResultDto.setStatus("组团失败");
			else orderResultDto.setStatus("未知状态");
			
			List<OrderItemResultDto> orderItemDtos = new ArrayList<OrderItemResultDto>();
			orderResultDto.setOrderItems(orderItemDtos);
			
			OrderItemResultDto itemDto = new OrderItemResultDto();
			orderItemDtos.add(itemDto);
			itemDto.setPrice(record.getBigDecimal("og_price").toString());
			itemDto.setProductId(record.getLong("og_product_id"));
			itemDto.setProductImg(getImageDomain() + record.getStr("product_img"));
			itemDto.setProductName(record.getStr("name").length()>20?record.getStr("name").substring(0, 20).concat("..."):record.getStr("name"));
			itemDto.setQuantity(record.getInt("og_quantity"));
			itemDto.setSn(record.getStr("sn"));
			if(isReviewed(orderParamDto.getBuyerId(),orderResultDto.getOrderId(),itemDto.getProductId())){
				itemDto.setIsReview(true);
			}else{
				itemDto.setIsReview(false);
			}
			
		}
		return results;
	}

	@Override
	public OrderResultDto getOrder(Long orderId) throws OrderException {
		if(orderId == null) throw new OrderException("调用订单详情接口出错，请传入订单id");
		Record record = Db.findFirst(" SELECT  o.*, o.id as o_id, o.created as o_created, r.*, r.id as r_id, r.phone as r_phone, r.city as r_city, r.province as r_province, r.district as r_district, bu.nickname FROM "+Order.table+" o "
				+ " left join " + BuyerReceiver.table + " r on o.receiver_id=r.id "
				+ " left join " + BuyerUser.table + " bu on o.buyer_id=bu.id "
				+ " where o.id=?", orderId);
		return getOrderResult(record);
	}
	
	@Override
	public OrderResultDto getOrder(Long orderId, Long buyerId) throws OrderException {
		if(orderId == null || buyerId == null) throw new OrderException("获取拼团订单详情缺少必要参数");
		
		Order _order = orderdao.findById(orderId);
		if(_order == null) throw new OrderException("订单不存在");
		
		if(_order.getOrderType() != OrderType.pintuan.ordinal()){
			return getOrder(orderId);
		}
		
		Record order = Db.findFirst("select og.*, og.created as og_created, og.pay_fee as og_pay_fee, og.post_fee as og_post_fee, "
				+ "og.price as og_price, ogh.product_id as og_product_id, og.memo as og_memo, "
				+ "og.quantity as og_quantity, og.payment_status as og_payment_status, og.transaction_id as og_transaction_id,"
				+ "r.*, r.id as r_id, r.phone as r_phone, r.city as r_city, r.province as r_province, r.district as r_district, "
				+ "o.*, oi.*, o.id as o_id from " 
				+ OrderGuser.table + " og "
				+ " left join " + OrderGheader.table + " ogh on og.gheader_id=ogh.id "
				+ " left join " + BuyerReceiver.table + " r on og.receiver_id=r.id "
				+ " left join " + Order.table + " o on ogh.order_id=o.id "
				+ " left join " + OrderItem.table + " oi on ogh.product_id=oi.product_id and ogh.order_id=oi.order_id "
				+ " where ogh.order_id=? and og.buyer_id=? ", orderId, buyerId);
		
		OrderResultDto orderDetailDto = new OrderResultDto();
		orderDetailDto.setOrderId(order.getLong("o_id"));
		orderDetailDto.setPayFee(order.getBigDecimal("og_pay_fee"));   			//需计算应付金额
		orderDetailDto.setBuyerNick(order.getStr("nickname"));
		if(StrKit.notBlank(order.getStr("og_memo"))){
			orderDetailDto.setBuyerMemo(order.getStr("og_memo"));
			orderDetailDto.setBuyerMemoSimple("留言:" + orderDetailDto.getBuyerMemo());
			if(orderDetailDto.getBuyerMemo().length()>5){
				orderDetailDto.setBuyerMemoSimple("留言:"+orderDetailDto.getBuyerMemo().substring(0, 5).concat("..."));
			}
		}
		orderDetailDto.setTotalPrice(order.getBigDecimal("og_pay_fee"));		//商品金额
		orderDetailDto.setPostFee(order.getBigDecimal("og_post_fee"));			//运费
		orderDetailDto.setOrderCreated(order.getDate("og_created"));
		orderDetailDto.setOrderSn(order.getStr("order_sn"));
		orderDetailDto.setReceiverProvince(order.getStr("r_province"));
		orderDetailDto.setReceiverCity(order.getStr("r_city"));
		orderDetailDto.setReceiverCountry(order.getStr("district"));
		orderDetailDto.setReceiverAddr(order.getStr("address"));
		orderDetailDto.setReceiverName(order.getStr("receiver_name"));
		orderDetailDto.setReceiverPhone(order.getStr("r_phone"));
		orderDetailDto.setZipCode(order.getStr("zip_code"));
		orderDetailDto.setTransactionId(order.getStr("og_transaction_id"));
		orderDetailDto.setOrderType(order.getInt("order_type"));
		
		if(order.getInt("group_status") == GroupStatus.grouping.ordinal())
			orderDetailDto.setGroupStatus("组团中");
		else if(order.getInt("group_status") == GroupStatus.success.ordinal())
			orderDetailDto.setGroupStatus("组团成功");
		else if(order.getInt("group_status") == GroupStatus.fail.ordinal()){
			orderDetailDto.setGroupStatus("组团失败");
		}
		
		if(order.getInt("og_payment_status") == PaymentStatus.unpaid.ordinal()){
			orderDetailDto.setOrderStatus("待支付");
		}else if(order.getInt("og_payment_status") == PaymentStatus.paid.ordinal()){
			orderDetailDto.setOrderStatus("已支付");
		}else if(order.getInt("og_payment_status") == PaymentStatus.refunded.ordinal()){
			orderDetailDto.setOrderStatus("已退款<br>"+orderDetailDto.getTransactionId());
		}
		
		if(order.getInt("shipping_status") == ShippingStatus.shipped.ordinal()){
			orderDetailDto.setOrderStatus("已发货");
		}
		
		if(order.getInt("order_status") == OrderStatus.completed.ordinal()){
			orderDetailDto.setOrderStatus("已完成");
		}else if(order.getInt("order_status") == OrderStatus.cancelled.ordinal()){
			orderDetailDto.setOrderStatus("已取消");
		}
		
		//查询订单项
		List<OrderItemResultDto> orderItemDtos = new ArrayList<OrderItemResultDto>();
		orderDetailDto.setOrderItems(orderItemDtos);
		
		OrderItemResultDto itemDto = new OrderItemResultDto();
		orderItemDtos.add(itemDto);
		itemDto.setPrice(order.getBigDecimal("og_price").toString());
		itemDto.setProductId(order.getLong("og_product_id"));
		itemDto.setProductImg(getImageDomain() + order.getStr("product_img"));
		itemDto.setProductName(order.getStr("name").length()>10?order.getStr("name").substring(0, 10).concat("..."):order.getStr("name"));
		itemDto.setQuantity(order.getInt("og_quantity"));
		itemDto.setSn(order.getStr("sn"));
		if(isReviewed(buyerId,orderDetailDto.getOrderId(),itemDto.getProductId())){
			itemDto.setIsReview(true);
		}else{
			itemDto.setIsReview(false);
		}
		
		String speciValueIds = order.getStr("spec_value");
		if(StrKit.notBlank(speciValueIds)){
			String [] orderIdArrs = speciValueIds.split(",");
			List<Long> ids = new ArrayList<Long>();
			StringBuffer condition = new StringBuffer(); 
			for(String id : orderIdArrs){
				ids.add(Long.valueOf(id));
				condition.append("?").append(",");
			}
			condition.deleteCharAt(condition.length() -1);
			List<SpecificationValue> specificationValues = specValueDao.find("select * from " + SpecificationValue.table + 
					" where id in("+condition+") ", ids.toArray());
			itemDto.setSpecificationValues(specificationValues);
			final StringBuffer valueNames = new StringBuffer();
			for(SpecificationValue specificationValue : itemDto.getSpecificationValues()){
				valueNames.append(specificationValue.getName()).append("/");
			}
			if(valueNames.length()>0) valueNames.deleteCharAt(valueNames.length()-1);
			itemDto.setSpecificationValueNames(valueNames.toString());
		}
		return orderDetailDto;
	}

	@Override
	public List<OrderResultDto> getOrders(String orderIds) throws OrderException {
		return getOrders(orderIds, null);
	} 
	
	@Override
	public List<OrderResultDto> getOrders(String orderIds, Map<String, String> statusMap) throws OrderException {
		if(StrKit.isBlank(orderIds)) throw new OrderException("调用批量获取订单详情接口参数错误");
		final String [] orderIdArrs = orderIds.split("-");
		List<Object> params = new ArrayList<Object>();
		final StringBuffer condition = new StringBuffer(); 
		for(String id : orderIdArrs){
			params.add(Long.valueOf(id));
			condition.append("?").append(",");
		}
		condition.deleteCharAt(condition.length() -1);
		
		final StringBuffer where = new StringBuffer(" where 1=1 ");
		where.append(" and o.id in ("+condition+")");
		
		if(statusMap != null){
			for(String key : statusMap.keySet()){
				setOrderStatusCondition(key, statusMap.get(key), where, params);
			}
		}
		
		List<Record> orders = Db.find(" SELECT  o.*, o.id as o_id, o.created as o_created, r.*, r.id as r_id, r.phone as r_phone, r.city as r_city, r.province as r_province, r.district as r_district, b.*, b.id as b_id FROM "+Order.table+" o "
				+ " left join " + BuyerUser.table + " b on o.buyer_id=b.id "
				+ " left join " + BuyerReceiver.table + " r on o.receiver_id=r.id "
				+ where.toString(), params.toArray());
		
		List<OrderResultDto> orderResultList = new ArrayList<OrderResultDto>();
		for(Record record : orders){
			OrderResultDto orderDetailDto = getOrderResult(record);
			orderResultList.add(orderDetailDto);
		}
		
		return orderResultList;
	}
	
	@Override
	public void batchShipping(String items) throws OrderException {
		if(StrKit.isBlank(items)) throw new OrderException("调用批量发货缺少必要参数");
		
		JSONArray jsonArray = null;
		try {
			jsonArray = JSONArray.parseArray(items);
		} catch (Exception e) {
			throw new OrderException(e.getMessage());
		}
				
		List<String> batchError = new ArrayList<String>();
		for(int i=0;i<jsonArray.size();i++){
			JSONObject json = jsonArray.getJSONObject(i);
			SendGoodParamDto sendGoodParam = new SendGoodParamDto();
			sendGoodParam.setOrderId(json.getLong("orderId"));
			sendGoodParam.setBuyerId(json.getLong("buyer_id"));
			sendGoodParam.setExpKey(json.getString("exp_key"));
			sendGoodParam.setBillNumber(json.getString("bill_number"));
			try {
				shipping(sendGoodParam);
			} catch (OrderException e) {
				batchError.add("订单[" + sendGoodParam.getOrderId() + "]发货失败，原因："+e.getMessage());
			}
		}
		if(batchError.size()>0){
			StringBuffer msg = new StringBuffer();
			for(String e : batchError){
				msg.append(e);
			}
			throw new OrderException(msg.toString());
		}
	}
	
	@Override
	@Transactional(rollbackFor = OrderException.class)
	public String shipping(SendGoodParamDto sendGoodParam) throws OrderException {
		if(sendGoodParam == null || StrKit.isBlank(sendGoodParam.getExpKey()) 
				|| StrKit.isBlank(sendGoodParam.getBillNumber()) || sendGoodParam.getOrderId() == null){
			throw new OrderException("调用订单发货接口缺少必要参数");
		}
		
		Order order = orderdao.findById(sendGoodParam.getOrderId());
		if(sendGoodParam.getBuyerId()!=null){
			if(order == null || order.getGroupStatus() != GroupStatus.success.ordinal())
				throw new OrderException("只有组团成功的订单才能发货");	
		}else{
		if(order == null || order.getPaymentStatus() != PaymentStatus.paid.ordinal())
			throw new OrderException("只有支付状态的订单才能发货");
		
		if(order.getShippingStatus() == ShippingStatus.shipped.ordinal())
			throw new OrderException("订单已发货，无需重复发货");
		}
		
		order.setShippingStatus(ShippingStatus.shipped.ordinal());
		order.update();
		
		Shipping ship=new Shipping();
		ship.setOrderId(sendGoodParam.getOrderId());
		ship.setBuyerId(sendGoodParam.getBuyerId());
		ship.setExpKey(sendGoodParam.getExpKey());
		ship.setBillNumber(sendGoodParam.getBillNumber());
		ship.setActive(1);
		ship.setCreated(new Date());
		ship.setUpdated(new Date());
		
		try {
			ship.save();
		} catch (Exception e) {
			throw new OrderException(e.getMessage());
		}
		
		return order.getOrderSn();
	}

	@Override
	@Transactional(rollbackFor = OrderException.class)
	public void refund(RefundParamDto refundParam) throws OrderException {
		if(refundParam == null || refundParam.getOrderId() == null || refundParam.getRefundFee() == null)
			throw new OrderException("订单退款缺少必要参数");
		
		Order order = orderdao.findById(refundParam.getOrderId());
		if(order == null) throw new OrderException("订单不存在");
		if(order.getPaymentStatus() != PaymentStatus.paid.ordinal()) throw new OrderException("未支付订单不允许退款");
		if(order.getPaymentStatus() == PaymentStatus.refunded.ordinal()) throw new OrderException("订单已退款，不可重复退款");
		if(order.getOrderStatus() == OrderStatus.completed.ordinal()) throw new OrderException("交易成功不可退款");
		if(order.getOrderStatus() == OrderStatus.cancelled.ordinal()) throw new OrderException("已取消订单不可退款");
		
		if(refundParam.getRefundFee().compareTo(new BigDecimal(0)) !=1) throw new OrderException("退款金额少于0");
		
		order.setPaymentStatus(PaymentStatus.refunded.ordinal());
		order.update();
		
		Refund refund=new Refund();
		refund.setOrderId(order.getId());
		refund.setTransactionId(order.getTransactionId());
		refund.setCash(refundParam.getRefundFee());
		refund.setCreated(new Date());
		refund.setUpdated(new Date());
		refund.setActive(true);
		try {
			refund.save();
		} catch (Exception e) {
			throw new OrderException(e.getMessage());
		}
		//需要调用接口线上退款？？？
	}
	
	private void setOrderStatusCondition(String type, String value, StringBuffer where, List<Object> params){
		if("o".equals(type)){
			//订单状态，完成还是取消
			where.append(" and o.order_status = ?");	
			params.add(OrderStatus.valueOf(value).ordinal());
		}else if("p".equals(type)){
			//支付状态
			if(value.equals("paid")){
				//查询出已支付，未发货订单
				where.append(" and o.payment_status = ? and o.shipping_status = ? ");
				params.add(PaymentStatus.valueOf(value).ordinal());
				params.add(ShippingStatus.unshipped.ordinal());
			}else{
				where.append(" and o.payment_status = ?");
				params.add(PaymentStatus.valueOf(value).ordinal());					
			}
		}else if("s".equals(type)){
			//发货状态
			where.append(" and o.shipping_status = ?");
			params.add(ShippingStatus.valueOf(value).ordinal());
		}else if("g".equals(type)){
			//拼团状态
			where.append(" and o.group_status = ?");
			params.add(GroupStatus.valueOf(value).ordinal());
		}else {
			throw new OrderException("不支持的订单状态");
		}
	}
	
	private OrderResultDto getOrderResult(Record order){
		OrderResultDto orderDetailDto = new OrderResultDto();
		orderDetailDto.setOrderId(order.getLong("o_id"));
		orderDetailDto.setPayFee(order.getBigDecimal("pay_fee"));   			//需计算应付金额
		orderDetailDto.setBuyerNick(order.getStr("nickname"));
		if(StrKit.notBlank(order.getStr("memo"))){
			orderDetailDto.setBuyerMemo(order.getStr("memo"));
			orderDetailDto.setBuyerMemoSimple("留言:" + orderDetailDto.getBuyerMemo());
			if(orderDetailDto.getBuyerMemo().length()>5){
				orderDetailDto.setBuyerMemoSimple("留言:"+orderDetailDto.getBuyerMemo().substring(0, 5).concat("..."));
			}
		}
		orderDetailDto.setTotalPrice(order.getBigDecimal("total_price"));	//商品金额
		orderDetailDto.setPostFee(order.getBigDecimal("post_fee"));			//运费
		orderDetailDto.setOrderCreated(order.getDate("o_created"));
		orderDetailDto.setOrderSn(order.getStr("order_sn"));
		orderDetailDto.setReceiverProvince(order.getStr("r_province"));
		orderDetailDto.setReceiverCity(order.getStr("r_city"));
		if(StrKit.notBlank(order.getStr("r_district")))
			orderDetailDto.setReceiverCountry(order.getStr("r_district"));
		orderDetailDto.setReceiverAddr(order.getStr("address"));
		orderDetailDto.setReceiverName(order.getStr("receiver_name"));
		orderDetailDto.setReceiverPhone(order.getStr("r_phone"));
		orderDetailDto.setZipCode(order.getStr("zip_code"));
		orderDetailDto.setTransactionId(order.getStr("transaction_id"));
		orderDetailDto.setOrderType(order.getInt("order_type"));
		if(order.getInt("payment_status") == PaymentStatus.unpaid.ordinal()){
			orderDetailDto.setOrderStatus("待支付");
		}else if(order.getInt("payment_status") == PaymentStatus.paid.ordinal()){
			orderDetailDto.setOrderStatus("已支付");
		}else if(order.getInt("payment_status") == PaymentStatus.refunded.ordinal()){
			orderDetailDto.setOrderStatus("已退款<br>"+orderDetailDto.getTransactionId());
		}
		
		if(order.getInt("shipping_status") == ShippingStatus.shipped.ordinal()){
			orderDetailDto.setOrderStatus("已发货");
		}
		
		if(order.getInt("order_status") == OrderStatus.completed.ordinal()){
			orderDetailDto.setOrderStatus("已完成");
		}else if(order.getInt("order_status") == OrderStatus.cancelled.ordinal()){
			orderDetailDto.setOrderStatus("已取消");
		}
		
		//查询订单项
		List<OrderItemResultDto> orderItemDtos = getOrderItems(orderDetailDto.getOrderId(),null);
		orderDetailDto.setOrderItems(orderItemDtos);
		return orderDetailDto;
	}
	
	/**
	 * 获取分销订单
	 * @param record
	 * @return
	 */
	private OrderAgentResultDto getOrderAgentResult(Record record){
		OrderResultDto orderResult = getOrderResult(record);
		OrderAgentResultDto agentResult = new OrderAgentResultDto();
		BeanUtils.copyProperties(orderResult, agentResult);
		//查询订单佣金记录
		String sql = "select b.nickname, acr.commission_value from " + AgentCommRcd.table + " acr "
				+ " left join " + Agent.table + " a on acr.agent_id= a.id "
				+ " left join " + BuyerUser.table + " b on a.buyer_id = b.id where order_id=? ";
		
		List<Record> records = Db.find(sql, agentResult.getOrderId());
		List<AgentCommssionResultDto> commssions = new ArrayList<AgentCommssionResultDto>();
		for(Record r : records){
			AgentCommssionResultDto acr = new AgentCommssionResultDto();
			acr.setCommissionValue(r.getBigDecimal("commission_value"));
			acr.setNickName(r.getStr("nickname"));
			commssions.add(acr);
		}
		agentResult.setCommssions(commssions);
		return agentResult;
	}
	
	@Override
	public List<OrderTuanResultDto> groupSendsMore(String orderIds) throws OrderException {
		if(StrKit.isBlank(orderIds)) throw new OrderException("调用批量获取订单详情接口参数错误");
		List<OrderTuanResultDto>  tuanDtos=new ArrayList<OrderTuanResultDto>();
		final String [] orderIdArrs = orderIds.split("-");
		for(String id : orderIdArrs){
			Order order=orderdao.findById(id);
			OrderGheader gheader = orderGheaderdao.findFirst("select * from " + OrderGheader.table + " where order_id=? and active=1", order.getId());
			if(order.getGroupStatus()==GroupStatus.success.ordinal()){
				OrderTuanResultDto tuanDto=new OrderTuanResultDto();
				tuanDto.setOrderId(order.getId());
				tuanDto.setOrderCreated(order.getCreated());
				tuanDto.setOrderSn(order.getOrderSn());
				tuanDto.setGuserInfoItems(getGuserInfoResult(gheader.getId()));
				List<OrderItemResultDto> orderItemDtos = getOrderItems(tuanDto.getOrderId(),null);
				tuanDto.setOrderItems(orderItemDtos);
				tuanDtos.add(tuanDto);
			}
		}
		return tuanDtos;
	}
	
	/**
	 * 获取拼团订单
	 * @param record
	 * @return
	 */
	private OrderTuanResultDto getOrderTuanResult(Record record){
		OrderTuanResultDto tuanDto=new OrderTuanResultDto();
		tuanDto.setOrderId(record.getLong("o_id"));
		tuanDto.setOrderCreated(record.getDate("o_created"));
		tuanDto.setOrderSn(record.getStr("order_sn"));
		tuanDto.setGuserInfoItems(getGuserInfoResult(record.getLong("g_header_id")));
		MultiGroup multiGroup = multiGroupdao.findById(record.getLong("group_id"));
		tuanDto.setGroupInfo(multiGroup.getOfferNum()+"人成团");
		if(record.getInt("shipping_status")==ShippingStatus.shipped.ordinal()){
			tuanDto.setGroupStatus("卖家已发货");
		}else{
		if(record.getInt("group_status") == GroupStatus.grouping.ordinal()){
			tuanDto.setGroupStatus("组团中");
		}else if(record.getInt("group_status") == GroupStatus.success.ordinal()){
			tuanDto.setGroupStatus("组团成功");
		}else if(record.getInt("group_status") == GroupStatus.fail.ordinal()) {
			tuanDto.setGroupStatus("组团失败");
		}
		}
		//查询订单项
		List<OrderItemResultDto> orderItemDtos = getOrderItems(tuanDto.getOrderId(),null);
		tuanDto.setOrderItems(orderItemDtos);
		return tuanDto;
	}
	/**
	 * 获取拼团成员信息
	 * @param orderId
	 * @param groupId
	 * @return
	 */
	private List<OrderTuanGuserInfoDto> getGuserInfoResult(Long gHeaderId){
		List<OrderTuanGuserInfoDto> guserInfoItems= new ArrayList<OrderTuanGuserInfoDto>();
		List<Record> orderGusers = Db.find("select bu.id as buyer_id, bu.nickname, bu.headimgurl, og.*,og.id as og_id,r.*, r.phone as r_phone, r.city as r_city, r.province as r_province, r.district as r_district "
				+ " from " + OrderGuser.table + " og "
				+ " left join " + BuyerUser.table + " bu on og.buyer_id = bu.id "
				+ " left join " + BuyerReceiver.table + " r on og.receiver_id=r.id "
				+ " where og.gheader_id= ? order by og.created asc ",gHeaderId);
		for(Record order : orderGusers){
			OrderTuanGuserInfoDto orderDetailDto=new OrderTuanGuserInfoDto();
			orderDetailDto.setId(order.getLong("og_id"));
			orderDetailDto.setBuyer_id(order.getLong("buyer_id"));
			orderDetailDto.setPayFee(order.getBigDecimal("pay_fee"));   			//需计算应付金额
			orderDetailDto.setBuyerNick(order.getStr("nickname"));
			if(StrKit.notBlank(order.getStr("memo"))){
				orderDetailDto.setBuyerMemo(order.getStr("memo"));
				orderDetailDto.setBuyerMemoSimple("留言:" + orderDetailDto.getBuyerMemo());
				if(orderDetailDto.getBuyerMemo().length()>5){
					orderDetailDto.setBuyerMemoSimple("留言:"+orderDetailDto.getBuyerMemo().substring(0, 5).concat("..."));
				}
			}else{
				orderDetailDto.setBuyerMemo("");
				orderDetailDto.setBuyerMemoSimple("无留言");
			}
			orderDetailDto.setTotalPrice(order.getBigDecimal("total_price"));	//商品金额
			orderDetailDto.setPostFee(order.getBigDecimal("post_fee"));			//运费receiverCountry
			orderDetailDto.setReceiverProvince(order.getStr("r_province"));
			orderDetailDto.setReceiverCity(order.getStr("r_city"));
			if(StrKit.notBlank(order.getStr("r_district"))){
				orderDetailDto.setReceiverCountry(order.getStr("r_district"));
			}
			orderDetailDto.setReceiverAddr(order.getStr("address"));
			orderDetailDto.setReceiverName(order.getStr("receiver_name"));
			orderDetailDto.setReceiverPhone(order.getStr("r_phone"));
			orderDetailDto.setZipCode(order.getStr("zip_code"));
			orderDetailDto.setTransactionId(order.getStr("transaction_id"));
			orderDetailDto.setNum(order.getInt("quantity"));
			orderDetailDto.setHeadimg(order.getStr("headimgurl"));
			if(order.getInt("payment_status") == PaymentStatus.unpaid.ordinal()){
				orderDetailDto.setPaymentStatus("待支付");
			}else if(order.getInt("payment_status") == PaymentStatus.paid.ordinal()){
				orderDetailDto.setPaymentStatus("已支付");
			}else if(order.getInt("payment_status") == PaymentStatus.refunded.ordinal()){
				orderDetailDto.setPaymentStatus("已退款<br>"+orderDetailDto.getTransactionId());
			}
			
			guserInfoItems.add(orderDetailDto);
		}
		return guserInfoItems;
	}
	
	/**
	 * 订单项
	 * @param orderId
	 * @param buyerId
	 * @return
	 */
	private List<OrderItemResultDto> getOrderItems (Long orderId, Long buyerId){
		List<OrderItemResultDto> orderItemDtos = new ArrayList<OrderItemResultDto>();
		List<OrderItem> orderItems = orderItemdao.find("select * from " + OrderItem.table + " where order_id=? ", orderId);
		for(OrderItem orderItem : orderItems){
			OrderItemResultDto itemDto = new OrderItemResultDto();
			itemDto.setPrice(orderItem.getPrice().toString());
			itemDto.setProductId(orderItem.getProductId());
			itemDto.setProductImg(getImageDomain() + orderItem.getProductImg());
			itemDto.setProductName(orderItem.getName().length()>10?orderItem.getName().substring(0, 10).concat("..."):orderItem.getName());
			itemDto.setQuantity(orderItem.getQuantity());
			itemDto.setSn(orderItem.getSn());
			if(buyerId !=null){
				if(isReviewed(buyerId,orderId,orderItem.getProductId())){
					itemDto.setIsReview(true);
				}else{
					itemDto.setIsReview(false);
				}
			}
			
			String speciValueIds = orderItem.getSpecificationValue();
			if(StrKit.notBlank(speciValueIds)){
				String [] orderIdArrs = speciValueIds.split(",");
				List<Long> ids = new ArrayList<Long>();
				StringBuffer condition = new StringBuffer(); 
				for(String id : orderIdArrs){
					ids.add(Long.valueOf(id));
					condition.append("?").append(",");
				}
				condition.deleteCharAt(condition.length() -1);
				List<SpecificationValue> specificationValues = specValueDao.find("select * from " + SpecificationValue.table + 
						" where id in("+condition+") ", ids.toArray());
				itemDto.setSpecificationValues(specificationValues);
				final StringBuffer valueNames = new StringBuffer();
				for(SpecificationValue specificationValue : itemDto.getSpecificationValues()){
					valueNames.append(specificationValue.getName()).append("/");
				}
				if(valueNames.length()>0) valueNames.deleteCharAt(valueNames.length()-1);
				itemDto.setSpecificationValueNames(valueNames.toString());
				
				ProductSpecItem stock=prodSpecItemdao.findFirst(
        				"select * FROM "+ProductSpecItem.table+" WHERE product_id = ? and specification_value = ?", itemDto.getProductId(), speciValueIds);
        		if(stock != null){
	        		itemDto.setPrice(stock.getPrice().toString());
        		}
			}
			
			itemDto.setTotalPrice(new BigDecimal(itemDto.getPrice()).multiply(new BigDecimal(itemDto.getQuantity())));
			orderItemDtos.add(itemDto);
		}
		return orderItemDtos;
	}
	
	@Override
	public OrderResultDto balance(Long buyerId, Long receiverId, String items) throws OrderException {
		if(StrKit.isBlank(items)) throw new OrderException("调用结算接口缺少必要参数");

		JSONArray jsonArray = null;
		try {
			 jsonArray = JSONArray.parseArray(items);
		} catch (Exception e) {
			throw new OrderException(e.getMessage());
		}
		
		if(jsonArray==null || jsonArray.size()<=0) throw new OrderException("请选择要结算的项");
		
		BuyerReceiver receiver = receiverDao.findById(receiverId);
		if(receiverId == null){
			receiver = receiverDao.findFirst(" select * from " + BuyerReceiver.table + " where buyer_id=? and is_default=1", buyerId);
		}
		
		OrderResultDto orderDto = new OrderResultDto();
		List<OrderItemResultDto> orderItemDtos = new ArrayList<OrderItemResultDto>();
		orderDto.setOrderItems(orderItemDtos);
		for(int i=0; i<jsonArray.size(); i++){
			JSONObject jsonObj = jsonArray.getJSONObject(i);
			final String prodId = jsonObj.getString("productId");
			final String specivalues = jsonObj.getString("speci");
			Product product = productDao.findById(prodId);
			if(product.getIsMarketable() == true 
					&& product.getStock()!=null && product.getStock()>0){
				OrderItemResultDto orderItem = new OrderItemResultDto();
				orderItem.setProductName(product.getName());
				orderItem.setQuantity(
						product.getStock()!=null && jsonObj.getInteger("pcount")>product.getStock()
						? product.getStock()
						: jsonObj.getInteger("pcount"));
				orderItem.setPrice(product.getPrice());
				orderItem.setProductId(product.getId());
				orderItem.setSn(product.getSn());
				orderItem.setProductImg(product.getImage());
				if(StrKit.isBlank(specivalues)){
					//没有传规格值，视统一规格
					String promoPrice = promotionService.getProductPromotionPrice(product);
	    			if(StrKit.notBlank(promoPrice)) orderItem.setPrice(promoPrice);
				}else{
					JSONArray jsonArr = JSON.parseArray(specivalues);
					if(jsonArr ==null || jsonArr.size() <=0){
						//没有传规格值，视统一规格
						String promoPrice = promotionService.getProductPromotionPrice(product);
		    			if(StrKit.notBlank(promoPrice)) orderItem.setPrice(promoPrice);
					}else{
						//多规格
						final StringBuffer sfvalueBuff = new StringBuffer();
						List<SpecificationValue> specificationValues = new ArrayList<SpecificationValue>();
		        		for(int k=0;k<jsonArr.size();k++){
		        			JSONObject json = jsonArr.getJSONObject(k);
		        			Long spvid = json.getLong("spvId");
		        			sfvalueBuff.append(spvid).append(",");
		        			specificationValues.add(specValueDao.findById(spvid));
		        		}
		        		orderItem.setSpecificationValues(specificationValues);
		        		ProductSpecItem stock=prodSpecItemdao.findFirst(
		        				"select * FROM "+ProductSpecItem.table+" WHERE product_id = ? and specification_value = ?", 
		        				product.getId(), sfvalueBuff.deleteCharAt(sfvalueBuff.length()-1).toString());
		        		if(stock == null || stock.getPrice() == null){
		        			throw new OrderException("请选择完整的规格值");
		        		}
		        		orderItem.setPrice(stock.getPrice().toString());
		        		//限时折扣价
		        		String promoPrice = promotionService.getProductPromotionPrice(product, stock);
		        		if(StrKit.notBlank(promoPrice)) orderItem.setPrice(promoPrice);
					}
				}
				//订单项商品小计金额
				orderItem.setTotalPrice(new BigDecimal(orderItem.getQuantity())
						.multiply(new BigDecimal(orderItem.getPrice())).setScale(2, BigDecimal.ROUND_HALF_UP));
				orderItemDtos.add(orderItem);
				
				//计算商品满减数据
				List<ProdFullCutResultDto> fullCuts = fullCutService.getProductFullCut(product);
				if(fullCuts != null && fullCuts.size()>0){
					orderItem.setFullCutDtos(fullCuts);
				}
			}
		}
		
		//商品总数量
		Integer num = 0;
		for(OrderItemResultDto orderItemDto : orderDto.getOrderItems()){
			num += orderItemDto.getQuantity();
		}
		orderDto.setNum(num);
		//订单总金额，不包含邮费
		BigDecimal totalPrice = new BigDecimal(0);
		for(OrderItemResultDto orderItemDto : orderDto.getOrderItems()){
			totalPrice = totalPrice.add(orderItemDto.getTotalPrice());
		}
		orderDto.setTotalPrice(totalPrice);
		
		//计算邮费
		BigDecimal orderPostFee = new BigDecimal(0);
		if(receiver != null){
			for(OrderItemResultDto orderItemDto : orderDto.getOrderItems()){
				Product product = productDao.findById(orderItemDto.getProductId());
				BigDecimal postFees = getDeliveryFees(product, orderDto, receiver, orderDto.getNum());
				orderPostFee = orderPostFee.add(postFees);	
			}
		}
		orderDto.setPostFee(orderPostFee);
		//计算满减
		setFullCut(orderDto);
		//计算会员价
		if(buyerId !=null){
			BuyerUser buyer = buyerUserdao.findById(buyerId);
			if(buyer !=null){
				setMemberDiscount(buyer, orderDto);
			}
		}
		//最终得出订单应支付金额
		orderDto.setPayFee(orderDto.getTotalPrice().add(orderDto.getPostFee()).setScale(2, BigDecimal.ROUND_HALF_UP));
		return orderDto;
	}
	
	@Override
	public OrderResultDto gbalance(Long buyerId, Long receiverId, String items) throws OrderException {
		if(StrKit.isBlank(items) || buyerId == null) throw new OrderException("调用结算接口缺少必要参数");

		JSONArray jsonArray = null;
		try {
			 jsonArray = JSONArray.parseArray(items);
		} catch (Exception e) {
			throw new OrderException(e.getMessage());
		}
		
		if(jsonArray==null || jsonArray.size()<=0) throw new OrderException("请选择要结算的项");
		
		BuyerReceiver receiver = null;
		if(receiverId == null){
			receiver = receiverDao.findFirst(" select * from " + BuyerReceiver.table + " where buyer_id=? and is_default=1", buyerId);
		}else{
			receiver = receiverDao.findById(receiverId);
		}
		
		OrderResultDto orderDto = new OrderResultDto();
		orderDto.setOrderType(OrderType.pintuan.ordinal());		//拼团订单
		List<OrderItemResultDto> orderItemDtos = new ArrayList<OrderItemResultDto>();
		orderDto.setOrderItems(orderItemDtos);
		for(int i=0; i<jsonArray.size(); i++){
			JSONObject jsonObj = jsonArray.getJSONObject(i);
			final String prodId = jsonObj.getString("productId");
			final String specivalues = jsonObj.getString("speci");
			Product product = productDao.findById(prodId);
			if(product == null || product.getIsMarketable() == false 
					|| product.getStock() == null || product.getStock()<=0){
				throw new OrderException("该商品目前不可拼团，可能是库存不足，或已下架，请见谅");
			}
			MultiGroup group = grouponService.getProductMultiGroup(product);
			if(group == null) throw new OrderException("商品拼团活动已失效");
			OrderItemResultDto orderItem = new OrderItemResultDto();
			orderItem.setProductName(product.getName());
			orderItem.setQuantity(
					product.getStock()!=null && jsonObj.getInteger("pcount")>product.getStock()
					? product.getStock()
					: jsonObj.getInteger("pcount"));
			orderItem.setPrice(product.getPrice());
			orderItem.setProductId(product.getId());
			orderItem.setSn(product.getSn());
			orderItem.setProductImg(product.getImage());
			if(StrKit.isBlank(specivalues)){
				//没有传规格值，视统一规格
				BigDecimal groupPrice = grouponService.getCollagePrice(product, null);
    			if(groupPrice != null) orderItem.setPrice(groupPrice.toString());
			}else{
				//多规格
				final StringBuffer sfvalueBuff = new StringBuffer();
				List<SpecificationValue> specificationValues = new ArrayList<SpecificationValue>();
        		JSONArray jsonArr = JSON.parseArray(specivalues);
        		for(int k=0;k<jsonArr.size();k++){
        			JSONObject json = jsonArr.getJSONObject(k);
        			Long spvid = json.getLong("spvId");
        			sfvalueBuff.append(spvid).append(",");
        			specificationValues.add(specValueDao.findById(spvid));
        		}
        		orderItem.setSpecificationValues(specificationValues);
        		BigDecimal groupPrice = grouponService.getCollagePrice(product, sfvalueBuff.deleteCharAt(sfvalueBuff.length() - 1).toString());
        		if(groupPrice != null){
        			orderItem.setPrice(groupPrice.toString());
        		}
			}
			//订单项商品小计金额
			orderItem.setTotalPrice(new BigDecimal(orderItem.getQuantity())
					.multiply(new BigDecimal(orderItem.getPrice())).setScale(2, BigDecimal.ROUND_HALF_UP));
			orderItemDtos.add(orderItem);
		}
		
		//商品总数量
		Integer num = 0;
		for(OrderItemResultDto orderItemDto : orderDto.getOrderItems()){
			num += orderItemDto.getQuantity();
		}
		orderDto.setNum(num);
		//订单总金额，不包含邮费
		BigDecimal totalPrice = new BigDecimal(0);
		for(OrderItemResultDto orderItemDto : orderDto.getOrderItems()){
			totalPrice = totalPrice.add(orderItemDto.getTotalPrice());
		}
		orderDto.setTotalPrice(totalPrice);
		
		//计算邮费
		BigDecimal orderPostFee = new BigDecimal(0);
		if(receiver != null){
			for(OrderItemResultDto orderItemDto : orderDto.getOrderItems()){
				Product product = productDao.findById(orderItemDto.getProductId());
				BigDecimal postFees = getDeliveryFees(product, orderDto, receiver, orderDto.getNum());
				orderPostFee = orderPostFee.add(postFees);	
			}
		}
		orderDto.setPostFee(orderPostFee);
		
		//最终得出订单应支付金额
		orderDto.setPayFee(orderDto.getTotalPrice().add(orderDto.getPostFee()).setScale(2, BigDecimal.ROUND_HALF_UP));
		return orderDto;
	}
	
	/**
	 * 设置会员优惠
	 * @param buyer
	 * @param orderDot
	 */
	private void setMemberDiscount(BuyerUser user, OrderResultDto orderDto){
		BuyerCard buyerCard = buyerCardDao.findFirst("select * from " + BuyerCard.table + " where buyer_id=? and status=1 and active=1", user.getId());
		if(buyerCard != null){
			Map<String, String> paramsCardInfoMap = ParaMap.create().put("card_id", buyerCard.getCardId()).put("code", buyerCard.getUserCardCode()).getData();
			ApiResult cardInfoResult = CardApi.memberCardInfo(JsonKit.toJson(paramsCardInfoMap));
			if(cardInfoResult.isSucceed() && "NORMAL".equals(cardInfoResult.getStr("user_card_status"))){
				//检查会员卡状态跟时效
				if(user.getMemberRankId() != null){
					MemberRank rank = mbRankdao.findById(user.getMemberRankId());
					//按会员等级进行优惠
					if(rank != null){
						//先打折，再满减
						orderDto.setOldPrice(orderDto.getTotalPrice());
						orderDto.setTotalPrice(orderDto.getTotalPrice().multiply(rank.getRankDiscount().divide(new BigDecimal(100))));
						if(rank.getRankCashFull() != null && rank.getRankCashRward()!=null){//会员等级满减规则
							if(orderDto.getTotalPrice().compareTo(rank.getRankCashFull()) == 1){
								orderDto.setOldPrice(orderDto.getTotalPrice());
								orderDto.setTotalPrice(orderDto.getTotalPrice().subtract(rank.getRankCashRward()));
							}
						}
					}
				}else{
					//按普通会员进行优惠，即当前会员卡进行优惠
					Card card = cardDao.findFirst("select * from " + Card.table + " where card_id=? ", buyerCard.getCardId());
					if(card != null && card.getDiscount()!=null){
						orderDto.setOldPrice(orderDto.getTotalPrice());
						orderDto.setTotalPrice(orderDto.getTotalPrice().multiply(new BigDecimal(100).subtract(new BigDecimal(card.getDiscount()).divide(new BigDecimal(1000)))));
					}
				}
			}
		}
	}
	
	/**
	 * 计算订单是否包含满减商品
	 * @param orderDto
	 */
	private void setFullCut(OrderResultDto orderDto){
		//该订单的满减数据，算法是：把订单中所有商品的满减设置数据找出来，
		//然后从小到大进行排序，最后把订单总金额跟集合中的满减数据一一比较，从小比到大，直到找到符合条件的满减数据
		List<ProdFullCutResultDto> orderFullCutDtos = new ArrayList<ProdFullCutResultDto>();
		for(OrderItemResultDto orderItemDto : orderDto.getOrderItems()){
			if(orderItemDto.getFullCutDtos() != null && orderItemDto.getFullCutDtos().size()>0){
				orderFullCutDtos.addAll(orderItemDto.getFullCutDtos());
			}
		}
		/**
		 * 按满减的金额进行升序排列
		 */
		Collections.sort(orderFullCutDtos, new Comparator<ProdFullCutResultDto>(){
			@Override
			public int compare(ProdFullCutResultDto o1, ProdFullCutResultDto o2) {
				return o1.getMeet().subtract(o2.getMeet()).intValue();
			}
		});
		BigDecimal fullCutTotalPrice = new BigDecimal(0);
		for(ProdFullCutResultDto fullCut : orderFullCutDtos){
			if(orderDto.getTotalPrice().compareTo(fullCut.getMeet()) != -1){//一个个比，从小比到大
				if(fullCut.getCash() != null){
					fullCutTotalPrice = orderDto.getTotalPrice().subtract(fullCut.getCash()).setScale(2, BigDecimal.ROUND_HALF_UP);
				}
				if(fullCut.getPostage() == 1){
					//说明包邮
					if(orderDto.getPostFee().compareTo(new BigDecimal(0)) ==1){
						orderDto.setOldPostFee(orderDto.getPostFee());
						orderDto.setPostFee(new BigDecimal(0));
					}
				}
			}
		}
		
		//说明有满减价格
		if(fullCutTotalPrice.compareTo(new BigDecimal(0)) ==1){
			orderDto.setOldPrice(orderDto.getTotalPrice());
			orderDto.setTotalPrice(fullCutTotalPrice);
		}
	}
    
	/**
	 * 算商品邮费
	 * @param product
	 * @param orderDto
	 * @param buyerReceiver
	 * @param pnum （订单中商品的总数量）按件算邮费使用
	 * @return
	 */
	private BigDecimal getDeliveryFees(Product product, OrderResultDto orderDto, BuyerReceiver buyerReceiver, final int pnum) throws OrderException{
		if(product.getDeliveryType() == null || product.getDeliveryType()==0)
			//统一邮费
			return product.getDeliveryFees() == null ? new BigDecimal(0) : product.getDeliveryFees();
			
		//根据邮费模板算邮费
		BigDecimal deliveryFees = new BigDecimal(0);
		DeliveryTemplate dt = deliveryTpldao.findById(product.getDeliveryTemplateId());
		if(dt == null || dt.getActive() !=1) throw new OrderException("计算邮费出错，运费模板不存在或被删除");
		
		//找出买家收货地址id
		final String areaId=buyerReceiver.getAreaTreePath()+buyerReceiver.getAreaId();
		//找出运费模板的配置项
		List<DeliverySet> ds = deliveryDao.find("SELECT * FROM "+DeliverySet.table+"  WHERE template_id = ? and active =1", product.getDeliveryTemplateId());
		
		if(ds == null || ds.size()<=0)
			return new BigDecimal(0);
			
    	if(dt.getValuationType()==1){
    		//按商品件数计算邮费
    		for (DeliverySet deliverySet : ds) {
				String setAreaIds = deliverySet.getAreaId();
				if(contains(setAreaIds, areaId)){
					if(pnum<=deliverySet.getAddStandards()){
						//如果商品数量少于或等于模板中设置的起始值，按起始邮费算
						return deliverySet.getStartFees();
					}else{
						//如果商品数量大于起始值，计算超过的商品数量
						final int overNum = pnum - deliverySet.getStartStandards();
						//看看商品到底超过多少件
						final int count = overNum % deliverySet.getAddStandards() == 0 ? overNum/deliverySet.getAddStandards() : overNum/deliverySet.getAddStandards() + 1;
						return deliverySet.getStartFees().add(new BigDecimal(count).multiply(deliverySet.getAddFees()));
					}
				}
			}	
    	}else if(dt.getValuationType()==2){
    		//按商品物流重量算邮费
    		//1.获取规格
    		List<SpecificationValue> specificationValues = null;
    		List<OrderItemResultDto> orderItems = orderDto.getOrderItems();
    		for(OrderItemResultDto orderItem : orderItems){
    			if(orderItem.getProductId() == product.getId()){
    				specificationValues = orderItem.getSpecificationValues();
    				break;
    			}
    		}
    		
    		if(specificationValues == null) throw new OrderException("计算邮费出错，找不到对应的规格值");
    		
    		StringBuffer specifValues = new StringBuffer();
    		for(SpecificationValue sv : specificationValues){
    			specifValues.append(sv).append(",");
    		}
    		
    		ProductSpecItem productStock = prodSpecItemdao.findFirst(
    				"select * from " + ProductSpecItem.table + " where product_id=? and specification_value=? ",
    				product.getId(), specifValues.deleteCharAt(specifValues.length()-1).toString());
    		
    		if(productStock == null) throw new OrderException("计算邮费出错，找不到对应规格设置的物流重量值");
    		
    		final int weight = productStock.getWeight().intValue();//物流重量
    		for (DeliverySet deliverySet : ds) {
    			String setAreaIds = deliverySet.getAreaId();
    			if(contains(setAreaIds, areaId)){
    				if(weight<=deliverySet.getAddStandards()){
						//如果商品数量少于或等于模板中设置的起始值，按起始邮费算
						return deliverySet.getStartFees();
					}else{
						//如果商品数量大于起始值，计算超过的重量
						final int overNum = weight - deliverySet.getStartStandards();
						//看看商品到底超过多少重量
						final int count = overNum % deliverySet.getAddStandards() == 0 ? overNum/deliverySet.getAddStandards() : overNum/deliverySet.getAddStandards() + 1;
						return deliverySet.getStartFees().add(new BigDecimal(count).multiply(deliverySet.getAddFees()));
					}
    			}
			}
    	}
		return deliveryFees;
	}
	
	/**
	 * 判断邮件模板中设置的地址是否包含买家的收货地址
	 * @param areaIdsets
	 * @param areaIds
	 * @return
	 */
	private boolean contains(String areaIdsets, String areaIds){
		for(String areaIdSet : areaIdsets.split(",")){
			for(String areaId : areaIds.split(",")){
				if(areaIdSet.equals(areaId)) return true;
			}
		}
		return false;
	}

	public Boolean isReviewed(Long buyerId, Long orderId, Long productId) throws OrderException {
		String sql = "SELECT count(*) FROM `t_product_review` review WHERE review.buyer_id = ? AND review.product_id = ? AND review.order_id= ?";
		Long count =Db.queryLong(sql,buyerId,productId,orderId);
		return count > 0;
	}

	@Override
	public void cancel(Long orderId) throws OrderException {
		if(orderId == null) throw new OrderException("取消订单缺少必要参数");
		Order order = orderdao.findById(orderId);
		if(order == null || order.getPaymentStatus() != PaymentStatus.unpaid.ordinal()) throw new OrderException("该订单不能取消，或已支付");
		order.setOrderStatus(OrderStatus.cancelled.ordinal());
		order.update();
	}
	
	@Override
	public void confirm(Long orderId) throws OrderException {
		if(orderId == null) throw new OrderException("取消订单缺少必要参数");
		Order order = orderdao.findById(orderId);
		if(order == null 
				|| order.getPaymentStatus() != PaymentStatus.paid.ordinal() 
				|| order.getShippingStatus() != ShippingStatus.shipped.ordinal()) 
			throw new OrderException("该订单不能确认收货");
		order.setOrderStatus(OrderStatus.completed.ordinal());//确认收货视为交易成功
		order.update();		
	}
	
	@Override
	@Transactional(rollbackFor = OrderException.class)
	public void cancel(Long orderId, Long buyerId) throws OrderException {
		if(orderId == null) throw new OrderException("取消拼团订单缺少必要参数");
		Order order = orderdao.findById(orderId);
		if(order == null) throw new OrderException("订单不存在，id：" + orderId);
		if(order.getOrderType() != OrderType.pintuan.ordinal()){
			cancel(orderId);
			return;
		}
		
		//取消拼团订单
		if(buyerId == null) throw new OrderException("取消拼团订单缺少必要buyerId");
		
		if(order.getGroupStatus() != GroupStatus.grouping.ordinal()) throw new OrderException("组团中的订单才可取消");
		
		//团数据
		OrderGheader ogh = orderGheaderdao.findFirst("select * from " + OrderGheader.table + " where order_id=? and active=1", orderId);
		if(ogh == null) throw new OrderException("取消拼团订单失败，拼团数据异常");
		
		OrderGuser oguser = orderGuserdao.findFirst("select * from " + OrderGuser.table 
				+ " where gheader_id=? and buyer_id=? ", ogh.getId(), buyerId);
		if(oguser == null || oguser.getPaymentStatus() != PaymentStatus.unpaid.ordinal()) {
			throw new OrderException("该订单不可取消，或已支付");
		}
		
		if(order.getBuyerId() == buyerId){
			//如果是团长取消订单的情况
			List<OrderGuser> ogusers = orderGuserdao.find("select * from " + OrderGuser.table + " where gheader_id=? ", ogh.getId());
			for(OrderGuser og : ogusers){
				if(og.getPaymentStatus() == PaymentStatus.paid.ordinal()) 
					throw new OrderException("自己的团，并且有成员已支付，不可取消"); 
			}
			
			for(OrderGuser og : ogusers){
				og.delete();
			}
			
			order.setOrderStatus(OrderStatus.cancelled.ordinal());
			try {
				ogh.delete();
				order.update();	
			} catch (Exception e) {
				throw new OrderException(e.getMessage());
			}
		}else{
			oguser.delete();//直接物理删除该数据，只删除团成员拼团数据
		}
	}

	@Override
	public Long joinGroup(OrderJoinParamDto orderJoinParam) throws OrderException {
		if(orderJoinParam == null || orderJoinParam.getGroupUserId() == null 
				|| orderJoinParam.getBuyerId() == null || orderJoinParam.getReceiverId() == null 
				|| StrKit.isBlank(orderJoinParam.getItems())) 
			throw new UmpException("拼团缺少必要参数");
		OrderResultDto orderResult  = null;
		try {
			orderResult = gbalance(orderJoinParam.getBuyerId(), orderJoinParam.getReceiverId(), orderJoinParam.getItems());
		} catch (OrderException e) {
			throw new OrderException(e.getMessage());
		}
		
		if(orderResult == null || orderResult.getOrderItems() == null || orderResult.getOrderItems().size()<=0) 
			throw new OrderException("拼团失败，解析订单数据出错");
		OrderItemResultDto orderItem = orderResult.getOrderItems().get(0);
		
		Product product = productDao.findById(orderItem.getProductId());
		if(product == null) throw new OrderException("拼团失败，商品异常");
		MultiGroup multiGroup = grouponService.getProductMultiGroup(product);
		if(multiGroup == null) throw new OrderException("拼团失败，可能拼团活动已过时失效");
		
		//检查发起者正在组团的订单
		//检查团长的活动是否存在
		OrderGheader gHeader = orderGheaderdao.findFirst("select * from " + OrderGheader.table + 
				" where buyer_id=? and product_id= ? and group_id=? and active =1", orderJoinParam.getGroupUserId(), product.getId(), multiGroup.getId());
		if(gHeader == null) throw new OrderException("不存在该拼团活动");
		
		//检查拼团活动的时效
		if(multiGroup.getValidTime() != null){
			Integer expiresIn = multiGroup.getValidTime() * 3600; //转换成秒
			Long expiredTime = gHeader.getCreated().getTime() + ((expiresIn -5) * 1000);
			if(expiredTime < System.currentTimeMillis()) throw new OrderException("拼团失败，本次拼团已过期");
		}
		
		//检查拼团活动是否满员
		List<OrderGuser> gusers = orderGuserdao.find("select * from " + OrderGuser.table + " where gheader_id=?", gHeader.getId());
		if(gusers!=null && gusers.size()>=multiGroup.getOfferNum()) throw new OrderException("拼团失败，该团已满员");
		
		//插入拼团者信息
		OrderGuser guser = orderGuserdao.findFirst("select * from " + OrderGuser.table + " where "
				+ " buyer_id=? and gheader_id=? ", 
				orderJoinParam.getBuyerId(), gHeader.getId());
		if(guser != null) throw new OrderException("您已参与本次拼团，不可重复拼团");
		
		guser = new OrderGuser();
		guser.setBuyerId(orderJoinParam.getBuyerId());
		guser.setGheaderId(gHeader.getId());
		guser.setReceiverId(orderJoinParam.getReceiverId());
		guser.setPrice(new BigDecimal(orderItem.getPrice()));
		guser.setQuantity(orderItem.getQuantity());
		if(orderItem.getSpecificationValues() != null){
			final StringBuffer sbff = new StringBuffer();
			for(SpecificationValue sfv : orderItem.getSpecificationValues()){
				sbff.append(sfv.getId()).append(",");
			}
			guser.setSpecValue(sbff.length()>0 ? sbff.deleteCharAt(sbff.length()-1).toString() : sbff.toString());
		}
		guser.setPostFee(orderResult.getPostFee());
		BigDecimal payFee = orderResult.getTotalPrice().add(orderResult.getPostFee());	//最终支付金额
		guser.setPayFee(payFee);
		guser.setPaymentStatus(PaymentStatus.unpaid.ordinal());
		guser.setMemo(orderJoinParam.getMemo());
		guser.setTradeNo(getTradeNo());
		guser.setActive(true);
		guser.setCreated(new Date());
		guser.setUpdated(new Date());
		try {
			guser.save();
		} catch (Exception e) {
			throw new OrderException(e.getMessage());
		}
		return gHeader.getOrderId();
	}
	
	class SendTemplateMsgThread extends Thread{
		
		private Order order;
		
		public SendTemplateMsgThread(Order order){
			this.order = order;
		}
		
		@Override
		public void run() {
			if(StrKit.isBlank(WxSdkPropKit.get("wx_domain"))) {
				logger.error("系统异常，公众账号不存在");
				return;	
			}
			
			//购买者
			BuyerUser buyerUser = buyerUserdao.findById(order.getBuyerId());
			if(buyerUser == null){
				logger.error("订单数据异常，找不到购买者");
				return;
			}
			
			//获取模板
			JSONObject json = new JSONObject();
			json.put("template_id_short", "TM00016");
			String templateId = TemplateMsgApi.getTemplateId("TM00016", json.toString());
			if(StrKit.isBlank(templateId)) return;
			
			TemplateData templateData = TemplateData.New().setTemplate_id(templateId)
					.setTouser(buyerUser.getOpenId())
					.setUrl(WxSdkPropKit.get("wx_domain") + "order/detail/?orderId="+order.getId())
					.add("first", "[" + buyerUser.getNickname() + "]您好，我们已为您创建订单，并分配商品库存，请及时支付", "#173177")
					.add("orderID", order.getOrderSn(), "#173177")
					.add("orderMoneySum", order.getTotalPrice() + "，邮费：" + order.getPostFee(), "#173177")
					/*.add("backupFieldName", "", "#173177")
					.add("backupFieldData", "", "#173177")*/
					.add("remark", "温馨提示：未支付订单系统将在24小时内取消", "#173177");
			ApiResult apiResult = TemplateMsgApi.send(templateData.build());
			if(!apiResult.isSucceed()){
				logger.error("error_code:" + apiResult.getErrorCode() + ",error_msg" + apiResult.getErrorMsg());
			}
		}
	}

	@Override
	public OrderPayResultDto getOrderPayInfo(Long orderId, Long userId) throws OrderException {
		if(orderId == null) throw new OrderException("获取订单缺少必要参数");
		
		Order order = orderdao.findById(orderId);
		if(order == null || order.getPaymentStatus() == PaymentStatus.paid.ordinal())
			throw new OrderException("订单不存在或已支付");
		
		OrderPayResultDto orderPayResultDto = new OrderPayResultDto();
		orderPayResultDto.setOrderSn(order.getOrderSn());
		orderPayResultDto.setPayFee(order.getPayFee());
		orderPayResultDto.setTradeNo(order.getTradeNo());
		
		if(order.getOrderType() == OrderType.pintuan.ordinal()){
			//一个订单只有一条开团数据
			OrderGheader gheader = orderGheaderdao.findFirst(" select * from " + OrderGheader.table + " where order_id=? and active=1 ", orderId);
			if(gheader == null) throw new OrderException("获取订单支付信息失败，没有找到开团数据");
			
			/*List<OrderItem> orderItems = OrderItem.dao.find("select * from " + OrderItem.table + " where order_id=? ", orderId);
			if(orderItems == null || orderItems.size() <=0) throw new OrderException("订单数据异常");*/
			
			OrderGuser guser = orderGuserdao.findFirst("select * from " + OrderGuser.table 
					+ " where gheader_id=? and buyer_id=? ", gheader.getId(), userId);
			if(guser == null) throw new OrderException("获取订单支付信息失败，用户参团数据不存在");
			
			orderPayResultDto.setPayFee(guser.getPayFee());
			orderPayResultDto.setTradeNo(guser.getTradeNo());
		} 
		
		return orderPayResultDto;
	}

}
