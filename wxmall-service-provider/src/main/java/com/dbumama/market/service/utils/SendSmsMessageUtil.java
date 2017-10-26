package com.dbumama.market.service.utils;


import com.alibaba.fastjson.JSONObject;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.AlibabaAliqinFcSmsNumSendRequest;
import com.taobao.api.response.AlibabaAliqinFcSmsNumSendResponse;


public class SendSmsMessageUtil {
   private static TaobaoClient client;
   private  static String  SmsTemplateCode="SMS_16265584";
   private SendSmsMessageUtil(){  
   }
   static {
	   String url="http://gw.api.taobao.com/router/rest";
	   String appkey="23562380";
	   String secret="68bbfd5d25c7796474b6b41e43c78d85";
	    client = new DefaultTaobaoClient(url, appkey, secret);
   }
   public static boolean  sendCheckCodeSMS(String phone,String checkcode){
	    AlibabaAliqinFcSmsNumSendRequest req = new AlibabaAliqinFcSmsNumSendRequest();
		req.setExtend("123456");
		req.setSmsType("normal");
		req.setSmsFreeSignName("次时代微助手");
		req.setSmsParamString("{\"code\":\""+ checkcode +"\",\"product\":\"次时代微助手\"}");
		req.setRecNum(phone);
		req.setSmsTemplateCode(SmsTemplateCode);
		AlibabaAliqinFcSmsNumSendResponse rsp;
		try {
			rsp = client.execute(req);
			//{"alibaba_aliqin_fc_sms_num_send_response":{"result":{"err_code":"0","model":"102389088078^1103063595109","success":true},"request_id":"3b4btfbz4362"}}
			// System.out.println(rsp.getBody());
			String jsonstr=rsp.getBody();
			JSONObject js=JSONObject.parseObject(jsonstr);
			JSONObject response= js.getJSONObject("alibaba_aliqin_fc_sms_num_send_response");
			if(response != null){
				JSONObject result= response.getJSONObject("result");
				if(result!=null && result.getBoolean("success")){
					return true;
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		  
		  return false;
   }
   
   
}
