package com.dbumama.market.service.api.card;

import java.util.TreeMap;

import com.dbumama.market.model.BuyerCard;
import com.dbumama.market.model.Card;
import com.jfinal.plugin.activerecord.Page;

/**
 * 微信卡券
 * @author yangzy
 *
 */
public interface CardService {

	/**
	 * 获取卖家的卡券列表
	 * 根据seller_id 跟 auth_user_id
	 * @param cartParamDto
	 * @return
	 * @throws CardException
	 */
	public Page<CardResultDto> list(CardListParamDto cardParamDto) throws CardException;
	
	/**
	 * 保存卡券
	 * @param cardResultJson
	 * @throws CardException
	 */
	public Card save(Long sellerId, String cardId, String cardResultJson, String supplyBuy) throws CardException;
	
	/**
	 * 保存卡券到微信
	 * @param sellerId
	 * @param authUser
	 * @param accessToken
	 * @param cardData
	 * @throws CardException
	 */
	public String save2Weixin(Long sellerId, String cardData, String supplyBuy) throws CardException;
	
	/**
	 * 调用接口修改卡券
	 * @param sellerId
	 * @param authUser
	 * @param accessToken
	 * @param updateData
	 * @return
	 * @throws CardException
	 */
	public String update2Weixin(Long sellerId, String updateData, String supplyBuy) throws CardException;
	
	/**
	 * 激活会员卡
	 * @param cardActiveParam
	 * @return
	 * @throws CardException
	 */
	public String activeCard(CardActiveParamDto cardActiveParam) throws CardException;
	
	/**
	 * 会员卡充值
	 * @param buyerId
	 * @param cardId
	 * @param recharge
	 * @param tradeNo
	 * @return
	 * @throws CardException
	 */
	public TreeMap<String, Object> rechargeCard(Long buyerId, Long cardId, String recharge, String clientIp) throws CardException;
	
	/**
	 * 会员卡按会员等级充值
	 * 对应的充值金额对应不同的会员等级
	 * @param buyerId
	 * @param cardId
	 * @param rankId
	 * @param clientIp
	 * @return
	 * @throws CardException
	 */
	public TreeMap<String, Object> rechargeCardRank(Long buyerId, Long cardId, Long rankId, String clientIp) throws CardException;
	
	/**
	 * 投放会员卡
	 * @param paramDto
	 * @throws CardException
	 */
	public String putIn(String Data,Integer type) throws CardException;
	
	/**
	 * 获取用户会员卡
	 * @param buyerId
	 * @return
	 * @throws CardException
	 */
	public Card getCardByUser(Long buyerId);
	
	Card findById(Long id);
	
	/**
	 * 
	 * @param openId
	 * @param cardId
	 * @param userCode
	 * @return
	 */
	public BuyerCard getUserBuyerCard(String openId, String cardId, String userCode);
	
	/**
	 * 微信端领取会员卡后，微信通知调用改方法记录用户领取的会员卡
	 * @param openId
	 * @param cardId
	 * @param userCode
	 */
	public void getWechatCard(String openId, String cardId, String userCode);
	
}
