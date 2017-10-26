package com.dbumama.market.web.admin.ump.controller;

import com.dbumama.market.model.MultiGroup;
import com.dbumama.market.service.api.product.ProductException;
import com.dbumama.market.service.api.product.ProductParamDto;
import com.dbumama.market.service.api.product.ProductResultDto;
import com.dbumama.market.service.api.ump.GrouponParamDto;
import com.dbumama.market.service.api.ump.GrouponService;
import com.dbumama.market.service.api.ump.UmpException;
import com.dbumama.market.web.core.annotation.RouteBind;
import com.dbumama.market.web.core.controller.AdminBaseController;
import com.dbumama.market.web.core.plugin.spring.Inject.BY_NAME;
import com.jfinal.plugin.activerecord.Page;
@RouteBind(path = "groupon")
public class GrouponController extends AdminBaseController<MultiGroup>{
	@BY_NAME
	private GrouponService grouponService;
	
	public void index(){
	   render("/promotion/groupon_index.html");
	}
	
	public void set(){
		Long id=getParaToLong("id");
		if(id!=null){
			/*MultiGroup multiGroup=MultiGroup.dao.findById(id);
			List<ProductResultDto> productDto=new ArrayList<ProductResultDto>();
			String productIds=multiGroup.getProductIds();
			String[] productId=productIds.split(",");
			for (String productid : productId) {
				ProductResultDto dto=grouponService.getProductResultDto(multiGroup, new Long(productid));
				productDto.add(dto);
			}
			GrouponResultDto resultDto=new GrouponResultDto();
			resultDto.setMultiGroup(multiGroup);
			resultDto.setProductDto(productDto);*/
			setAttr("groupResult", grouponService.getGroupInfo(id));
		}
		render("/promotion/groupon_set.html");
	}
	
	public void list(){
		GrouponParamDto grouponParamDto=new GrouponParamDto(getSellerId(), getPageNo());
		try{
		Page<MultiGroup> pages=grouponService.list(grouponParamDto);
		rendSuccessJson(pages);
		} catch (UmpException e) {
			rendFailedJson(e.getMessage());
		}
	}
	
	public void save(){
		try {
			MultiGroup multiGroup=getModel();
			String multiGroupSetItems = getPara("setItems");
			grouponService.save(multiGroup, getSellerId(), multiGroupSetItems);
			rendSuccessJson();
		} catch (Exception e) {
			//e.printStackTrace();
			rendFailedJson(e.getMessage());
		}
	}
	public void listProducts(){
		ProductParamDto productParamDto = new ProductParamDto(getSellerId(), getPageNo());
		try {
			Page<ProductResultDto> pages = grouponService.getProducts4GrouponPage(productParamDto);
			rendSuccessJson(pages);
		} catch (ProductException e) {
			rendFailedJson(e.getMessage());
		}
	}

	@Override
	protected Class<MultiGroup> getModelClass() {
		return MultiGroup.class;
	}

}
