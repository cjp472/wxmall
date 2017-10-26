package com.dbumama.market.web.admin.card.controller;

import java.util.List;

import com.dbumama.market.model.Card;
import com.dbumama.market.model.MemberRank;
import com.dbumama.market.service.api.card.CardException;
import com.dbumama.market.service.api.card.CardListParamDto;
import com.dbumama.market.service.api.card.CardService;
import com.dbumama.market.service.api.customer.MemberRankService;
import com.dbumama.market.web.core.annotation.RouteBind;
import com.dbumama.market.web.core.controller.BaseController;
import com.dbumama.market.web.core.plugin.spring.Inject.BY_NAME;
/**
 * @author yangzy
 * 2017年7月23日
 */
@RouteBind(path="card", viewPath="card")
public class CardController extends BaseController{
	
	@BY_NAME
	private CardService cardService;
	@BY_NAME
	private MemberRankService memberRankService;
	
    public void index(){
    	render("/card/card_index.html");
    }
    
    public void list(){
    	CardListParamDto cardParam = new CardListParamDto(getSellerId(), getPageNo());
    	try {
    		rendSuccessJson(cardService.list(cardParam));
		} catch (CardException e) {
			rendFailedJson(e.getMessage());
		}
    }
    
    public void set(){
    	Card card = cardService.findById(getParaToLong("id"));
    	List<MemberRank> ranks = memberRankService.getSellerRanks(getSellerId());
    	setAttr("ranks", ranks);
    	setAttr("mcard", card);
    	render("/card/card_set.html");
    }
    
    //创建会员卡
   	public void save(){
   		try {
   			final String card = getPara("card");
   			final String supplyBuy = getPara("supply_buy");
   			cardService.save2Weixin(getSellerId(), card, supplyBuy);
   			rendSuccessJson();			
		} catch (CardException e) {
			rendFailedJson(e.getMessage());
		}
   	}
   	
   	/**
   	 * 更新会员卡
   	 */
   	public void update(){
   		try {
   			final String card = getPara("card");
   			final String supplyBuy = getPara("supply_buy");
   			cardService.update2Weixin(getSellerId(), card, supplyBuy);
   			rendSuccessJson();			
		} catch (CardException e) {
			rendFailedJson(e.getMessage());
		}
   	}
   	
   	/**
   	 * 跳到群发界面
   	 */
   	public void cput(){
   		String card_id=getPara("id");
   		setAttr("cardId", card_id);
   		render("/card/card_putin.html");
   	}
   	
   	/**
   	 * 投放
   	 */
   	public void putin(){
   		String card = getPara("card");
   		int type=getParaToInt("type");
   		try {
		String msgId=cardService.putIn(card,type);
		rendSuccessJson(msgId);
		} catch (CardException e) {
			rendFailedJson(e.getMessage());
		}
   	}
}
