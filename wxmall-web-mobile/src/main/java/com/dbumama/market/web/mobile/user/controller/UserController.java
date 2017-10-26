package com.dbumama.market.web.mobile.user.controller;

import java.util.List;

import com.dbumama.market.model.Agent;
import com.dbumama.market.model.Card;
import com.dbumama.market.model.MemberRank;
import com.dbumama.market.model.Order;
import com.dbumama.market.service.api.agent.AgentService;
import com.dbumama.market.service.api.card.CardActiveParamDto;
import com.dbumama.market.service.api.card.CardException;
import com.dbumama.market.service.api.card.CardService;
import com.dbumama.market.service.api.customer.MemberRankService;
import com.dbumama.market.web.core.annotation.RouteBind;
import com.dbumama.market.web.core.controller.BaseMobileController;
import com.dbumama.market.web.core.plugin.spring.Inject.BY_NAME;
import com.jfinal.plugin.activerecord.Db;

@RouteBind(path = "user")
public class UserController extends BaseMobileController{

	@BY_NAME
	private AgentService agentService;
	@BY_NAME
	private CardService cardService;
	@BY_NAME
	private MemberRankService memberRankService;
	
	public void index(){
		Agent agent = agentService.getBuyerAgent(getBuyerId());
		if(agent != null){
			//分销商级别
			Integer grade = agentService.getAgentGrade(agent);
			setAttr("agent", agent);
			setAttr("grade", grade);
		}
		//待付款
		Long unpayedCount = Db.queryLong("select count(*) from " + Order.table + " where buyer_id=? and order_type=0 and payment_status=0", getBuyerId());
		//已支付，未发货
		Long payedCount = Db.queryLong("select count(*) from " + Order.table + " where buyer_id=? and order_type=0 and payment_status=1 and shipping_status=0", getBuyerId());
		//已支付，未发货
		Long shippedCount = Db.queryLong("select count(*) from " + Order.table + " where buyer_id=? and order_type=0 and payment_status=1 and shipping_status=1", getBuyerId());
		//交易成功
		Long complateCount = Db.queryLong("select count(*) from " + Order.table + " where buyer_id=? and order_type=0 and order_status=2", getBuyerId());
		setAttr("unpayedCount", unpayedCount);
		setAttr("payedCount", payedCount);
		setAttr("shippedCount", shippedCount);
		setAttr("complateCount", complateCount);
		
		if(getBuyerUser().getMemberRankId() != null){
			MemberRank rank = memberRankService.findById(getBuyerUser().getMemberRankId());
			setAttr("rank", rank);
		}
		render("/user/index.html");
	}
	
	
	/**
	 * 会员卡充值
	 */
	public void recharge(){
		setAttr("cardId", getParaToLong("cardId"));
		Card card = cardService.findById(getParaToLong("cardId"));
		setAttr("card", card);
		if(card != null && card.getSupplyBuy() != null && "true".equals(card.getSupplyBuy())){
			List<MemberRank> ranks = memberRankService.getSellerRanks(getSellerId());
			setAttr("ranks", ranks);
			render("/user/rech_card_rank.html");
		}else{
			render("/user/rech_card.html");			
		}
	}
	
	/**
	 * 去到会员卡激活界面
	 */
	public void active_card(){
		Long cardId = getParaToLong("cardId");
		setAttr("cardId", cardId);
		render("/user/active_card.html");
	}
	
	//激活会员卡
	public void activecard(){
		//final String userName = getPara("userName");
		final String phone = getPara("phone");
		final String phoneCode = getPara("phoneCode");
		final String code = getPara("code");
		final String codeInSession = getSession().getAttribute("captcha") == null ? "" : getSession().getAttribute("captcha").toString();
		CardActiveParamDto cardActiveParam = new CardActiveParamDto(getBuyerId(), getSellerId(), getParaToLong("cardId"), phone, phoneCode, code, codeInSession);
		try {
			cardService.activeCard(cardActiveParam);
			rendSuccessJson();
		} catch (CardException e) {
			rendFailedJson(e.getMessage());
		}
	}
	
}
