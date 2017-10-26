package com.dbumama.market.web.api.user.controller;

import java.net.URLDecoder;

import javax.servlet.http.HttpSession;

import com.alibaba.fastjson.JSONObject;
import com.dbumama.market.model.Agent;
import com.dbumama.market.model.BuyerUser;
import com.dbumama.market.model.MemberRank;
import com.dbumama.market.model.Order;
import com.dbumama.market.service.api.agent.AgentService;
import com.dbumama.market.service.api.customer.MemberRankService;
import com.dbumama.market.service.api.user.UserException;
import com.dbumama.market.service.api.user.UserService;
import com.dbumama.market.service.api.user.WeappLoginResultDto;
import com.dbumama.market.service.api.user.WeappUserCheckParamDto;
import com.dbumama.market.service.constants.Constants;
import com.dbumama.market.web.core.annotation.RouteBind;
import com.dbumama.market.web.core.controller.BaseApiController;
import com.dbumama.market.web.core.plugin.spring.Inject.BY_NAME;
import com.jfinal.plugin.activerecord.Db;

@RouteBind(path="user")
public class UserController extends BaseApiController{

	@BY_NAME
	private UserService userService;
	@BY_NAME
	private MemberRankService memberRankService;
	@BY_NAME
	private AgentService agentService;
	
	public void index(){
		try{
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
			
			JSONObject result = new JSONObject();
	
			result.put("unpayedCount", unpayedCount);
			result.put("payedCount", payedCount);
			result.put("shippedCount", shippedCount);
			result.put("complateCount", complateCount);
			
			if(getBuyerUser()!=null &&getBuyerUser().getMemberRankId() != null){
				MemberRank rank = memberRankService.findById(getBuyerUser().getMemberRankId());
				result.put("rank", rank);
			}
			rendSuccessJson(result);
		} catch (Exception e) {
			e.printStackTrace();
			rendFailedJson(e.getMessage());
		}	
	}
	
	public void login(){
		final String code = getJSONPara("code");
		final String appId = getAppId();
		try {
			WeappLoginResultDto result = userService.loginWeapp(appId, code);
			HttpSession session = getSession(true);
			result.setSessionId(session.getId());
			//登陆后把sessionKey+openId的值存储到session
			session.setAttribute(result.getSessionId(), result);
			rendSuccessJson(result);
		} catch (UserException e) {
			rendFailedJson(e.getMessage());
		}
	}
	
	//检查小程序端用户数据合法性
	public void check(){
		final String sign = getJSONPara("signature");
		@SuppressWarnings("deprecation")
		final String rawData = URLDecoder.decode(getJSONPara("rawData"));
		final String encryptedData = getJSONPara("encryptedData");
		final String iv = getJSONPara("iv");
		WeappLoginResultDto weappLoginRes = (WeappLoginResultDto) getSession().getAttribute(getSession().getId());
		if(weappLoginRes == null) {
			rendFailedJson("session 失效");
			return;
		}
		
		WeappUserCheckParamDto userCheckParam = new WeappUserCheckParamDto(getAppId(), weappLoginRes.getSessionKey(), sign, rawData, encryptedData, iv);
		userCheckParam.setSellerId(getSellerId());
		try {
			BuyerUser buyer = userService.check(userCheckParam);
			getSession().setAttribute(Constants.BUYER_USER_IN_SESSION, buyer);
			rendSuccessJson();
		} catch (UserException e) {
			rendFailedJson(e.getMessage());
		}
	}
}
