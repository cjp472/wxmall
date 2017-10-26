package com.dbumama.market.web.api.receiver.controller;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dbumama.market.model.Area;
import com.dbumama.market.model.BuyerReceiver;
import com.dbumama.market.service.api.area.AreaService;
import com.dbumama.market.service.api.receiver.BuyerReceiverSubmitParamDto;
import com.dbumama.market.service.api.receiver.ReceiverService;
import com.dbumama.market.web.core.annotation.RouteBind;
import com.dbumama.market.web.core.controller.BaseApiController;
import com.dbumama.market.web.core.plugin.spring.Inject.BY_NAME;
@RouteBind(path = "receiver")
public class ReceiverController extends BaseApiController{
	@BY_NAME
	private AreaService areaService;
	@BY_NAME
	private ReceiverService receiverService;
	
	public void list(){
		List<BuyerReceiver> receivers = receiverService.getBuyerReceiver(getBuyerId());
		rendSuccessJson(receivers);

	}
    public void edit() {
        BuyerReceiver receiver = receiverService.findById(getJSONParaToLong("id"));
        rendSuccessJson(receiver);
    }
	
	  // 收货地址添加
	public void save() {
        try {
        	final String address = getJSONPara("address");
        	final String name = getJSONPara("name");
        	final String phone = getJSONPara("phone");
        	final String area_id = getJSONPara("area_id");
        	final String province = getJSONPara("province");
        	final String city = getJSONPara("city");
        	final String district = getJSONPara("district");
        	final String is_default = getJSONPara("is_default");
        	Long receiverId = getJSONParaToLong("receiverId");
        	BuyerReceiverSubmitParamDto submitParam = new BuyerReceiverSubmitParamDto(getBuyerId(), address, name, phone, area_id, province, city, district);
        	submitParam.setIs_default(is_default);
        	submitParam.setReceiverId(receiverId);
            rendSuccessJson(receiverService.save(submitParam));
        } catch (Exception e) {
            log.error("get receiver error", e);
            rendFailedJson("系统500错误");
        }
    }
	
   /**
	 * 地区
	 */
	public void area() {
		Long parentId = getJSONParaToLong("parentId");
		List<Area> areas = new ArrayList<Area>();
		Area parent = areaService.findById(parentId);
		if (parent != null) {
			areas =areaService.getChildren(parent.getId());
		} else {
			areas = areaService.findRoots();
		}
		//Map<Long, String> options = new HashMap<Long, String>();
		JSONArray array=new JSONArray();
		for (Area area : areas) {
			//options.put(area.getId(), area.getName());
			JSONObject result = new JSONObject();
			result.put("id", area.getId());
			result.put("name", area.getName());
			array.add(result);
		}
		rendSuccessJson(array);
	}
}
