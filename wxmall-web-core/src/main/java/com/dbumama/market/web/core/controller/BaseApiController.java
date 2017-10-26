package com.dbumama.market.web.core.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.dbumama.market.model.BuyerUser;
import com.dbumama.market.service.constants.Constants;
import com.dbumama.market.web.core.plugin.spring.IocInterceptor;
import com.dbumama.market.web.core.utils.ResultUtil;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.render.JsonRender;
import com.weixin.sdk.kit.WxSdkPropKit;

@Before({IocInterceptor.class})
public abstract class BaseApiController extends Controller{
	public Logger log = Logger.getLogger(getClass());
	
	protected JSONObject messageJson;
	
	public void setMessageJson(JSONObject messageJson){
		this.messageJson = messageJson;
	}
	
	public JSONObject getMessageJson(){
		return this.messageJson;
	}

	protected int getPageNo(){
		return this.getMessageJson().getInteger("page") == null
				|| this.getMessageJson().getInteger("page") <=0 ? 1 : this.getMessageJson().getInteger("page");
	}
	
	protected int getPageSize(){
		return this.getMessageJson().getInteger("rows")==null 
				|| this.getMessageJson().getInteger("rows") <=0 ? 20 : this.getMessageJson().getInteger("rows");
	}
	
	protected String getAppId(){
		return WxSdkPropKit.get("weapp_id");
	}
	
	protected BuyerUser getBuyerUser(){
		return (BuyerUser)getSession().getAttribute(Constants.BUYER_USER_IN_SESSION);
	}
	
	protected Long getBuyerId(){
		return getBuyerUser()==null ? null : getBuyerUser().getId();
	}
	
	protected Long getSellerId(){
		return getBuyerUser()==null ? null : getBuyerUser().getSellerId();
	}
	
	/**
	 * HTML视图
	 * @param view 视图文件名不含.html
	 */
	protected void renderHTML(String view) {
		if(view.endsWith(".html")){
			super.render(view);
		}else{
			super.render(view+".html");
		}
	}

	protected void rendSuccessJson(Object data){
		rendJson(ResultUtil.genSuccessResult(data));
	}
	
	protected void rendSuccessJson(){
		rendJson(ResultUtil.genSuccessResult());
	}
	
	public void rendFailedJson(final String msg){
		rendJson(ResultUtil.genFailedResult(msg));
	}
	
	public void rendFailedJson(final int code, final String msg){
		rendJson(ResultUtil.genFailedResult(code, msg));
	}
	
	public void rendFailedJson(final String code, final String msg){
		rendJson(ResultUtil.genFailedResult(code, msg));
	}
	
	protected void rendJson(Object json){
		String agent = getRequest().getHeader("User-Agent");
		if(agent.contains("MSIE"))
			this.render(new JsonRender(json).forIE());
		else{
			this.render(new JsonRender(json));
		}
	}
	
	protected synchronized String getUUIDStr(){
		return UUID.randomUUID().toString().replaceAll("-", "");
	}
	
	protected String inputStream2String(InputStream in) throws UnsupportedEncodingException, IOException{
		if(in == null)
			return "";
		StringBuffer out = new StringBuffer();
		byte[] b = new byte[4096];
		for (int n; (n = in.read(b)) != -1;) {
			out.append(new String(b, 0, n, "UTF-8"));
		}
		return out.toString();
	}
	
	public String getJSONPara(String paramName){
		return getMessageJson().getString(paramName);
	}
	
	public Long getJSONParaToLong(String paramName){
		return getMessageJson().getLong(paramName) ;
	}
	
	public Integer getJSONParaToInteger(String paramName){
		return getMessageJson().getInteger(paramName);
	}
	
	public String getImageDomain(){
		return WxSdkPropKit.get("img_domain");
	}
	
}
