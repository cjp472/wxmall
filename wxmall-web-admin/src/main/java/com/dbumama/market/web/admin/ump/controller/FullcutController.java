package com.dbumama.market.web.admin.ump.controller;

import java.util.Date;
import java.util.List;

import com.dbumama.market.model.FullCut;
import com.dbumama.market.model.FullCutProduct;
import com.dbumama.market.model.FullCutSet;
import com.dbumama.market.service.api.product.ProductException;
import com.dbumama.market.service.api.product.ProductParamDto;
import com.dbumama.market.service.api.product.ProductResultDto;
import com.dbumama.market.service.api.ump.FullCutParamDto;
import com.dbumama.market.service.api.ump.FullCutResultDto;
import com.dbumama.market.service.api.ump.FullCutService;
import com.dbumama.market.service.api.ump.PromotionParamDto;
import com.dbumama.market.service.api.ump.UmpException;
import com.dbumama.market.web.core.annotation.RouteBind;
import com.dbumama.market.web.core.controller.AdminBaseController;
import com.dbumama.market.web.core.plugin.spring.Inject.BY_NAME;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;

@RouteBind(path = "fullcut")
public class FullcutController extends AdminBaseController<FullCut>{
	@BY_NAME
	private FullCutService fullCutService;
	public void index(){
		render("/promotion/full_cut_index.html");
	}
	
	public void set(){
		Long id=getParaToLong("pid");
		if(id != null){
			FullCutResultDto dto=new FullCutResultDto();
			FullCut fullCut=fullCutService.findById(id);
			dto.setId(fullCut.getId());
			dto.setName(fullCut.getFullCutName());
			dto.setStartDate(fullCut.getStartDate());
			dto.setEndDate(fullCut.getEndDate());
			dto.setProductIds(fullCut.getProductIds());
		    List<FullCutSet> set = fullCutService.getFullCutSetsByFullCut(fullCut.getId());
		    dto.setSets(set);
		    setAttr("fullCutDto", dto);
		}
		render("/promotion/full_cut_set.html");
	}
	
	public void list(){
		PromotionParamDto promotionParam = new PromotionParamDto(getSellerId(), getPageNo());
		try{
			Page<FullCut> pages=fullCutService.list(promotionParam);
			rendSuccessJson(pages);
			} catch (UmpException e) {
				rendFailedJson(e.getMessage());
			}
	}
	
	public void save(){
	  final Long id=getParaToLong("id");
      final String fullCutName=getPara("name");
      final Date startDate=getParaToDate("start_date");
      final Date endDate=getParaToDate("end_date");
      final String setItem=getPara("setItem");
      final String productIds=getPara("product_ids");
      final Long sellerId=getSellerId();
      FullCutParamDto paramDto=new FullCutParamDto(sellerId,productIds, endDate, endDate, productIds, productIds);
      paramDto.setFullCutName(fullCutName);
      paramDto.setStartDate(startDate);
      paramDto.setEndDate(endDate);
      paramDto.setSetItem(setItem);
      paramDto.setProductIds(productIds);
      paramDto.setSellerId(sellerId);
      paramDto.setId(id);
      
      try {
    	  FullCut fullCut=fullCutService.save(paramDto);
    	  rendSuccessJson(fullCut);
	} catch (UmpException e) {
		 rendFailedJson(e.getMessage());
	}
	}
	
	public void listProducts(){
		ProductParamDto productParamDto = new ProductParamDto(getSellerId(), getPageNo());
		try {
			Page<ProductResultDto> pages = fullCutService.getProducts4FullCutPage(productParamDto);
			rendSuccessJson(pages);
		} catch (ProductException e) {
			rendFailedJson(e.getMessage());
		}
	}
	
	public void delSet(){
		final Long id=getParaToLong("ids");
		try {
			FullCut set=fullCutService.findById(id);
			set.setActive(false);
			set.update();
			Db.deleteById(FullCutProduct.table,"full_cut_id",id);
			Db.deleteById(FullCutSet.table,"full_cut_id",id);
			rendSuccessJson(set);
		} catch (Exception e) {
			 rendFailedJson(e.getMessage());
		}
		
	}

	@Override
	protected Class<FullCut> getModelClass() {
		return FullCut.class;
	}

}
