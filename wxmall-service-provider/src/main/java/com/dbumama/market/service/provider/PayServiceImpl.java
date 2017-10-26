package com.dbumama.market.service.provider;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.dbumama.market.model.AuthUserConfig;
import com.dbumama.market.model.BuyerCard;
import com.dbumama.market.model.BuyerRecharge;
import com.dbumama.market.model.BuyerUser;
import com.dbumama.market.model.Card;
import com.dbumama.market.model.CashbackRcd;
import com.dbumama.market.model.MultiGroup;
import com.dbumama.market.model.Order;
import com.dbumama.market.model.OrderGheader;
import com.dbumama.market.model.OrderGuser;
import com.dbumama.market.model.OrderItem;
import com.dbumama.market.model.Product;
import com.dbumama.market.service.api.pay.PayException;
import com.dbumama.market.service.api.pay.PayService;
import com.dbumama.market.service.api.ump.CashbackService;
import com.dbumama.market.service.api.ump.GrouponService;
import com.dbumama.market.service.api.ump.ProdCashbackResultDto;
import com.dbumama.market.service.api.ump.UmpException;
import com.dbumama.market.service.base.AbstractServiceImpl;
import com.dbumama.market.service.enmu.GroupStatus;
import com.dbumama.market.service.enmu.OrderType;
import com.dbumama.market.service.enmu.PaymentStatus;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.StrKit;
import com.weixin.sdk.api.ApiResult;
import com.weixin.sdk.api.CardApi;
import com.weixin.sdk.api.TemplateMsgApi;
import com.weixin.sdk.kit.ParaMap;
import com.weixin.sdk.kit.WxSdkPropKit;
import com.weixin.sdk.pay.UnifiedOrderApi;
import com.weixin.sdk.pay.UnifiedOrderReqData;
import com.weixin.sdk.pay.UnifiedOrderResData;
import com.weixin.sdk.utils.SignKit;

@Service("payService")
public class PayServiceImpl extends AbstractServiceImpl implements PayService{
	
	@Autowired
	private CashbackService cashbackService;
	@Autowired
	private GrouponService grouponService;
	private static final BuyerCard buyerCardDao = new BuyerCard().dao();
	private static final BuyerRecharge rechargeDao = new BuyerRecharge().dao();
	private static final BuyerUser buyerUserdao = new BuyerUser().dao();
	private static final Card cardDao = new Card().dao();
	private static final CashbackRcd cbrDao = new CashbackRcd().dao();
	private static final Order orderdao = new Order().dao();
	private static final OrderGheader orderGheaderdao = new OrderGheader().dao();
	private static final OrderGuser orderGuserdao = new OrderGuser().dao();
	private static final OrderItem orderItemdao = new OrderItem().dao();
	private static final Product productDao = new Product().dao();
	private static final AuthUserConfig authConfigDao = new AuthUserConfig().dao();
	
	@Override
	@Transactional(rollbackFor = PayException.class)
	public void resultLotteryCallback(BuyerUser user, TreeMap<String, Object> params) throws PayException {
		//更新用户抽奖次数
		String totalFee = (String) params.get("total_fee");
		String tradeNo = (String) params.get("out_trade_no");
		//tradeNo = tradeNo.replaceAll("'", "");
		String transactionId = (String) params.get("transaction_id");
		if("200".equals(totalFee)){
			user.setScore(user.getScore() + 20);
		}else if("500".equals(totalFee)){
			user.setScore(user.getScore() + 60);
		}else if("1000".equals(totalFee)){
			user.setScore(user.getScore() + 120);
		}else{
			throw new PayException("支付金额错误");
		}
		
		user.setUpdated(new Date());
		if(!user.update()) throw new PayException("更新公众号["+WxSdkPropKit.get("wx_app_id")+"]用户["+user.getId()+"]信息失败");
		//记录用户充值记录
		BuyerRecharge recharge = new BuyerRecharge();
		recharge.setBuyerId(user.getId());
		recharge.setRecharge(new BigDecimal(totalFee));
		recharge.setOutTradeId(tradeNo);
		recharge.setTransactionId(transactionId);
		recharge.setActive(true);
		recharge.setCreated(new Date());
		recharge.setUpdated(new Date());
		try {
			recharge.save();			
		} catch (Exception e) {
			throw new PayException(e.getMessage());
		}
	}

	@Override
	public TreeMap<String, Object> prepareToPay(String openId, String tradeNo, BigDecimal payFee, String desc, String ip)
			throws PayException {
		AuthUserConfig authConfig = authConfigDao.findFirst("select * from " + AuthUserConfig.table);
		
		String payFee_ = String.valueOf(payFee.multiply(new BigDecimal(100)).intValue());
		
		UnifiedOrderResData unifiedOrderResData = null;
		UnifiedOrderReqData unifiedOrderReqData = new UnifiedOrderReqData(openId, desc, tradeNo, payFee_,  ip, "JSAPI");
		
		try {
			UnifiedOrderApi unifiedOrderApi = new UnifiedOrderApi();
			unifiedOrderResData = (UnifiedOrderResData) unifiedOrderApi.post(unifiedOrderReqData);
		} catch (Exception e) {
			e.printStackTrace();
			throw new PayException(e.getMessage());
		}
		
		if(unifiedOrderResData==null || !"OK".equals(unifiedOrderResData.getReturn_msg())){
			if(unifiedOrderResData != null){
				System.out.println(unifiedOrderResData.getReturn_msg() + "appId:" + authConfig.getAppId() + ",secretKey:" + authConfig.getPaySecretKey() + ",mchid:" + authConfig.getPayMchId());
			}
			throw new PayException("调用微信统一下单接口失败");
		}
		
		//准备调用支付js接口的参数
		TreeMap<String, Object> params = new TreeMap<String, Object>();
    	params.put("appId", authConfig.getAppId());
        params.put("timeStamp", Long.toString(new Date().getTime()));
        params.put("nonceStr", SignKit.genRandomString32());
        params.put("package", "prepay_id="+unifiedOrderResData.getPrepay_id());
        params.put("signType", "MD5");
        
        String paySign = SignKit.sign(params, authConfig.getPaySecretKey());
        
        params.put("paySign", paySign);
        params.put("packageValue", "prepay_id="+unifiedOrderResData.getPrepay_id());
        params.put("returnMsg", unifiedOrderResData.getReturn_msg());
        params.put("sendUrl", authConfig.getWxDomain() + "/pay/result/");
        params.put("tradeno", tradeNo);
		return params;
	}
	
	/**
	 * 微信小程序
	 */
	@Override
	public TreeMap<String, Object> wxAppPrepareToPay(String openId, String tradeNo, BigDecimal payFee,
			String desc, String ip) throws PayException {
		String payFee_ = String.valueOf(payFee.multiply(new BigDecimal(100)).intValue());
		
		UnifiedOrderResData unifiedOrderResData = null;
		UnifiedOrderReqData unifiedOrderReqData = null;
		
			String appId=WxSdkPropKit.get("weapp_id");
			String mchId=WxSdkPropKit.get("wx_mch_id");
			String payScretKey=WxSdkPropKit.get("wx_secret_key"); 
			String notify_url=WxSdkPropKit.get("wx_notify_url"); 
			unifiedOrderReqData = new UnifiedOrderReqData(appId,mchId,payScretKey,notify_url,
					openId, desc, tradeNo, payFee_,  ip, "JSAPI");
		
		
		try {
			UnifiedOrderApi unifiedOrderApi = new UnifiedOrderApi();
			unifiedOrderResData = (UnifiedOrderResData) unifiedOrderApi.post(unifiedOrderReqData);
		} catch (Exception e) {
			e.printStackTrace();
			throw new PayException(e.getMessage());
		}
		
		if(unifiedOrderResData==null || !"OK".equals(unifiedOrderResData.getReturn_msg())){
			if(unifiedOrderResData != null){
				System.out.println(unifiedOrderResData.getReturn_msg());
			}
			throw new PayException("调用微信统一下单接口失败");
		}
		
		//准备调用支付js接口的参数
		TreeMap<String, Object> params = new TreeMap<String, Object>();
    	params.put("appId",unifiedOrderReqData.getAppid() );
    	
    	String now=Long.toString(new Date().getTime());
    	if(now.length()>10){
			now=now.substring(0, 10);
		}
        params.put("timeStamp",now);
        
        params.put("nonceStr", SignKit.genRandomString32());
        params.put("package", "prepay_id="+unifiedOrderResData.getPrepay_id());
        params.put("signType", "MD5");
        
        String paySign = SignKit.sign(params,  payScretKey );
        
        params.put("paySign", paySign);
        params.put("packageValue", "prepay_id="+unifiedOrderResData.getPrepay_id());
        params.put("returnMsg", unifiedOrderResData.getReturn_msg());
      
        params.put("tradeno", tradeNo);
		return params;
	}

	@Override
	public void resultOrderCallback(BuyerUser user, TreeMap<String, Object> params) throws PayException {
		String tradeNo = (String) params.get("out_trade_no");
		String transactionId = (String) params.get("transaction_id");
		
		//检查是否拼团订单
		OrderGuser gOrder = orderGuserdao.findFirst("select * from " + OrderGuser.table + " where trade_no=? ", tradeNo);
		if(gOrder !=null){
			gOrder.setTransactionId(transactionId);
			dealGorder(gOrder);
		}else{
			List<Order> orders = orderdao.find("select * from " + Order.table +" where trade_no = ?", tradeNo);
			for(Order order : orders){
				order.setTransactionId(transactionId);
				dealOrder(order, user);
			}	
		}
	}
	
	/**
	 * 针对普通订单支付后回调处理
	 * @param order
	 * @param user
	 */
	private void dealOrder(Order order, BuyerUser user){
		//更新商品状态为已支付
		order.setPaymentStatus(PaymentStatus.paid.ordinal());
		order.setUpdated(new Date());
		order.update();
		
		//更新商品库存
		List<OrderItem> orderItems = orderItemdao.find("select * from " + OrderItem.table + " where order_id=? ", order.getId());
		//存储该笔订单中的商品，避免重复发sql查询多次
		List<Product> products = new ArrayList<Product>();
		for(OrderItem orderItem : orderItems){
			Product product = productDao.findById(orderItem.getProductId());
			products.add(product);
			product.setStock(product.getStock() - orderItem.getQuantity() > 0 ? product.getStock() - orderItem.getQuantity() : 0);
			product.setSales(product.getSales() + orderItem.getQuantity());
			product.update();
		}

		//检查商品是否有订单返现的活动
		for(Product product : products){
			ProdCashbackResultDto cashback = cashbackService.getProductCashBack(product);
			if(cashback != null){
				//订单中有多个商品有随机返现，取其中一个
				//检查订单数量
				List<CashbackRcd> cashbackRcds = cbrDao.find("select * from " + CashbackRcd.table + 
						" where buyer_id=? and product_id=? ", user.getId(), product.getId());
				if(cashbackRcds != null && cashbackRcds.size() <cashback.getLimit()){
					//给用户发送现金红包，发送成功后插入一条发送记录
					new SendRedpackThread(user, order, orderItems, product, cashback).start();
				}
				break;//跳出，只处理一个商品即可
			}
		}
		
		//推送消息，提示用户支付成功，仓库正在准备发货
		new SendTemplateMsgThread (user, order).start();
	}
	
	/**
	 * 针对拼团订单
	 * 支付后，回调对订单进行处理
	 * @param gOrder
	 */
	private void dealGorder(OrderGuser gOrder){
		gOrder.setPaymentStatus(PaymentStatus.paid.ordinal());
		gOrder.setUpdated(new Date());
		gOrder.update();
		
		//获取开团信息
		OrderGheader gheader = orderGheaderdao.findById(gOrder.getGheaderId());
		
		//更新商品库存
		List<OrderItem> orderItems = orderItemdao.find("select * from " + OrderItem.table + " where order_id=? ", gheader.getOrderId());
		if(orderItems !=null && orderItems.size()>0){
			OrderItem orderItem = orderItems.get(0);
			Product product = productDao.findById(orderItem.getProductId());
			product.setStock(product.getStock() - orderItem.getQuantity() > 0 ? product.getStock() - orderItem.getQuantity() : 0);
			product.setSales(product.getSales() + orderItem.getQuantity());
			product.update();
			
			//当前商品的拼团活动
			MultiGroup multiGroup = grouponService.getProductMultiGroup(product);
			List<OrderGuser> ogusers = orderGuserdao.find("select * from " + OrderGuser.table 
					+ " where gheader_id=? and payment_status=2 ", gheader.getId());//查询出已支付拼团订单
			if(ogusers.size() == multiGroup.getOfferNum()){//如果订单数跟拼团活动设置的数量相等的话，就说明拼团成功了
				//已参团人数跟拼团设置的人数相等的时候
				Order ggOrder = orderdao.findById(gheader.getOrderId());
				ggOrder.setGroupStatus(GroupStatus.success.ordinal());
				ggOrder.setPaymentStatus(PaymentStatus.paid.ordinal());
				ggOrder.setUpdated(new Date());
				ggOrder.update();
				//new SendTemplateMsgThread (user, ggOrder).start();
			}
		}
	}
	
	@Override
	public void resultMemberCardCallback(BuyerUser user, TreeMap<String, Object> params) throws PayException {
		String tradeNo = (String) params.get("out_trade_no");
		String transactionId = (String) params.get("transaction_id");
		
		BuyerRecharge br = rechargeDao.findFirst("select * from " + BuyerRecharge.table + " where buyer_id=? and out_trade_id=? and active=0", user.getId(), tradeNo);
		if(br == null) throw new PayException("会员卡充值回调出现异常：充值明细记录不存在");
		
		//获取当前充值会员卡信息，需要激活
		BuyerCard buyerCard = buyerCardDao.findFirst("select * from " + BuyerCard.table + " where buyer_id=? and card_id=? and status=1 ", user.getId(), br.getCardId());
		if(buyerCard == null) throw new PayException("会员卡充值回调出现异常:会员卡激活信息记录不存在");
		
		Card mcard = cardDao.findFirst("select * from " + Card.table + " where card_id=? ", br.getCardId());
		if(mcard == null) throw new PayException("会员卡充值回调出现异常：会员卡不存在");
		if(!"true".equals(mcard.getSupplyBalance())) throw new PayException("会员卡充值回调出现异常：会员卡不支持充值");
			
		//获取该会员卡旧的积分以及余额信息
		Map<String, String> paramsCardInfoMap = ParaMap.create().put("card_id", buyerCard.getCardId()).put("code", buyerCard.getUserCardCode()).getData();
		ApiResult cardInfoResult = CardApi.memberCardInfo(JsonKit.toJson(paramsCardInfoMap));
		if(!cardInfoResult.isSucceed()) throw new PayException(cardInfoResult.getErrorMsg());
		String user_card_status = cardInfoResult.getStr("user_card_status");
		if(!"NORMAL".equals(user_card_status)) throw new PayException("会员卡充值回调出现异常:会员卡状态不可用,当前状态值：" + user_card_status);
		/*Integer bonus = cardInfoResult.getInt("bonus");	//原来的积分
		BigDecimal newBonus = new BigDecimal(bonus);*/
		
		Integer balance = cardInfoResult.getInt("balance");	//原来的余额
		BigDecimal newBalance = new BigDecimal(balance).add(new BigDecimal(params.get("total_fee").toString()));
		//调用接口//更新会员卡信息
		Map<String, String> paramsMap = ParaMap.create().put("code", buyerCard.getUserCardCode())
				.put("card_id", buyerCard.getCardId()).getData();
		if(StrKit.notBlank(mcard.getSupplyBonus()) && "true".equals(mcard.getSupplyBonus())){
			paramsMap.put("bonus", cardInfoResult.getInt("bonus") == null ? "0" : cardInfoResult.getInt("bonus").toString());
			paramsMap.put("add_bonus", "0");
			paramsMap.put("record_bonus", "消费送积分");
		}
		if(StrKit.notBlank(mcard.getSupplyBalance()) && "true".equals(mcard.getSupplyBalance())){
			paramsMap.put("balance", newBalance.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
			paramsMap.put("add_balance",  params.get("total_fee").toString());
			paramsMap.put("record_balance", "会员卡充值");
		}
		
		ApiResult cardResult = CardApi.updateUser(JsonKit.toJson(paramsMap));
		if(!cardResult.isSucceed()){
			throw new PayException(cardResult.getErrorMsg());
		}
		
		br.setUpdated(new Date());
		br.setTransactionId(transactionId);//用户微信接口退款标志
		br.setActive(true);//改条记录已充值成功 
		br.update();
		
		//判断是否用户是购买会员卡等级
		if(br.getMemberRankId() != null){
			user.setMemberRankId(br.getMemberRankId());
			user.update();
		}
	}

	@Override
	public void payByCard(Long orderId, Long userId) throws PayException {
		if(orderId == null) throw new PayException("使用会员卡支付缺少订单号");
		Order order = orderdao.findById(orderId);
		
		if(order == null || order.getPaymentStatus() != PaymentStatus.unpaid.ordinal())
			throw new PayException("当前订单不可支付");
		
		BuyerUser user = buyerUserdao.findById(order.getBuyerId());
		if(order.getOrderType() == OrderType.pintuan.ordinal()){
			//拼团订单
			user = buyerUserdao.findById("userId");
		}
		if(user == null) throw new PayException("使用会员卡支付失败，缺少会员数据");
		
		BigDecimal payFee = order.getPayFee();
		
		if(order.getOrderType() == OrderType.pintuan.ordinal()){
			//拼团订单
			OrderGuser gOrder = orderGuserdao.findFirst("select * from " + OrderGuser.table + " where order_id=? and buyer_id=?", order.getId(), user.getId());
			payFee = gOrder.getPayFee();
		}
		
		if(payFee == null || payFee.compareTo(new BigDecimal(0)) != 1)
			throw new PayException("使用会员卡支付失败，订单金额少于0");
		
		BuyerCard buyerCard = buyerCardDao.findFirst("select * from " + BuyerCard.table + " where buyer_id=? and status=1 ", user.getId());
		if(buyerCard == null) throw new PayException("使用会员卡支付失败，您还不是会员");
		
		Card card = cardDao.findFirst("select * from " + Card.table + " where card_id=? ", buyerCard.getCardId());
		if(card == null) throw new PayException("使用会员卡支付失败，会员卡不存在");
		
		//检查会员卡时效
		//获取会员卡信息，检查是否够钱支付
		//获取该会员卡旧的积分以及余额信息
		Map<String, String> paramsCardInfoMap = ParaMap.create().put("card_id", buyerCard.getCardId()).put("code", buyerCard.getUserCardCode()).getData();
		ApiResult cardInfoResult = CardApi.memberCardInfo(JsonKit.toJson(paramsCardInfoMap));
		if(!cardInfoResult.isSucceed()) throw new PayException(cardInfoResult.getErrorMsg());
		String user_card_status = cardInfoResult.getStr("user_card_status");
		if(!"NORMAL".equals(user_card_status)) throw new PayException("使用会员卡支付失败,会员卡状态不可用," + user_card_status);
		Integer balance = cardInfoResult.getInt("balance");	//原来的余额
		if(new BigDecimal(balance).compareTo(payFee) !=1) throw new PayException("使用会员卡支付失败，余额不足");
		//根据订单金额调整会员卡余额
		BigDecimal newBalance = new BigDecimal(balance).subtract(payFee);
		//调用接口//更新会员卡信息
		Map<String, String> paramsMap = ParaMap.create().put("code", buyerCard.getUserCardCode())
				.put("card_id", buyerCard.getCardId()).getData();
		if(StrKit.notBlank(card.getSupplyBonus()) && "true".equals(card.getSupplyBonus())){
			paramsMap.put("bonus", cardInfoResult.getInt("bonus") == null ? "0" : cardInfoResult.getInt("bonus").toString());
			paramsMap.put("add_bonus", "0");
			paramsMap.put("record_bonus", "消费送积分");
		}
		if(StrKit.notBlank(card.getSupplyBalance()) && "true".equals(card.getSupplyBalance())){
			paramsMap.put("balance", newBalance.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
			paramsMap.put("add_balance", "-"+payFee.toString());
			paramsMap.put("record_balance", "支付订单：" + order.getOrderSn());
		}
		ApiResult cardResult = CardApi.updateUser(JsonKit.toJson(paramsMap));
		if(!cardResult.isSucceed()){
			throw new PayException(cardResult.getErrorMsg());
		}
		if(order.getOrderType() == OrderType.pintuan.ordinal()){
			//拼团订单
			OrderGuser gOrder = orderGuserdao.findFirst("select * from " + OrderGuser.table + " where order_id=? and buyer_id=?", order.getId(), user.getId());
			gOrder.setTransactionId(buyerCard.getCardId());//退款标识使用会员卡号
			dealGorder(gOrder);
		}else{
			order.setTransactionId(buyerCard.getCardId());
			dealOrder(order, user);
		}
	}
	
	//订单返现，发送红包给买家
	class SendRedpackThread extends Thread {
		private BuyerUser buyerUser;   //买家
		private Order order;
		private List<OrderItem> orderItems;
		private Product product;
		private ProdCashbackResultDto cashback;
		
		public SendRedpackThread(BuyerUser buyerUser, Order order, List<OrderItem> orderItems, Product product, ProdCashbackResultDto cashback){
			this.buyerUser = buyerUser;
			this.order = order;
			this.orderItems = orderItems;
			this.product = product;
			this.cashback = cashback;
		}
		
		@Override
		public void run() {
			super.run();
			BigDecimal cashbackFee = null;//给用户返现的金额
			try {
				cashbackFee = cashbackService.cash2Buyer(buyerUser, order, orderItems, product, cashback);	
			} catch (UmpException e) {
				e.printStackTrace();
				//记录返现错误信息
				CashbackRcd cashbackRcd = new CashbackRcd();
				cashbackRcd.setOrderId(order.getId());
				cashbackRcd.setProductId(product.getId());
				cashbackRcd.setBuyerId(buyerUser.getId());
				cashbackRcd.setCashBackFee(cashbackFee);
				cashbackRcd.setSendLog(e.getMessage());//发送成功
				cashbackRcd.setActive(true);
				cashbackRcd.setCreated(new Date());
				cashbackRcd.setUpdated(new Date());
				cashbackRcd.save();
			}
		}
	}
	
	class SendTemplateMsgThread extends Thread{
		private BuyerUser buyerUser;   //买家
		private Order order;
		public SendTemplateMsgThread(BuyerUser buyerUser, Order order){
			this.buyerUser = buyerUser;
			this.order = order;
		}
		
		@Override
		public void run() {
			super.run();
			if (buyerUser == null) return;
			//推送一条消息给公众号消息接收者，提醒有人下单了
			JSONObject json = new JSONObject();
			json.put("template_id_short", "TM00015");
			String templateId = TemplateMsgApi.getTemplateId("TM00015", json.toString());
			if(StrKit.isBlank(templateId)) return;
			
			StringBuffer sbuffer = new StringBuffer();
			List<OrderItem> orderItems = orderItemdao.find("select * from " + OrderItem.table + " where order_id=? ", order.getId());
			for(OrderItem orderItem : orderItems){
				sbuffer.append(orderItem.getName() + "X" + orderItem.getQuantity()).append("<br>");
			}
			//1查找消息接收者
			/*List<BuyerUser> receivers = BuyerUser.dao.find(
					"select * from " + BuyerUser.table + " where seller_id=? and auth_app_id=? and subscribe=1 and is_receiver=1", 
					buyerUser.getSellerId(), buyerUser.getAuthAppId());
			//给公众号管理者发送消息，有人支付订单
			for(BuyerUser receiver : receivers){
				TemplateData templateData = TemplateData.New().setTemplate_id(templateId)
						.setTouser(receiver.getOpenId())
						.setUrl("http://"+buyerUser.getAuthAppId()+".ybwqy.com/order/detail/"+order.getId())
						.add("first",  "客户[" + buyerUser.getNickname() + "]已成功支付货款，请准备发货", "#173177")
						.add("orderProductName", sbuffer.toString(), "#173177")
						.add("orderMoneySum", order.getTotalPrice() + "，邮费：" + order.getPostFee(), "#173177")
						.add("backupFieldName", "", "#173177")
						.add("backupFieldData", "", "#173177")
						.add("remark", "订单编号："+order.getOrderSn()+", 如对订单有疑问，请联系公众号客服！", "#173177");
					
					ApiResult apiResult = CompTemplateMsgApi.send(accessToken, templateData.build());
					
					if(!apiResult.isSucceed()){
						logger.error("error_code:" + apiResult.getErrorCode() + ",error_msg" + apiResult.getErrorMsg());
					}
			}*/
			
		}
		
	}
	
}
