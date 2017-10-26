package com.dbumama.market.web.admin.ump.controller;

import java.util.List;

import com.dbumama.market.model.Cashback;
import com.dbumama.market.model.CashbackProduct;
import com.dbumama.market.service.api.product.ProductException;
import com.dbumama.market.service.api.product.ProductParamDto;
import com.dbumama.market.service.api.product.ProductResultDto;
import com.dbumama.market.service.api.product.ProductService;
import com.dbumama.market.service.api.ump.CashbackResultDto;
import com.dbumama.market.service.api.ump.CashbackService;
import com.dbumama.market.service.api.ump.PromotionParamDto;
import com.dbumama.market.service.api.ump.UmpException;
import com.dbumama.market.web.core.annotation.RouteBind;
import com.dbumama.market.web.core.controller.AdminBaseController;
import com.dbumama.market.web.core.plugin.spring.Inject.BY_NAME;
import com.jfinal.plugin.activerecord.Page;

@RouteBind(path = "cashback")
public class CashBackController extends AdminBaseController<Cashback>{
	@BY_NAME
	private CashbackService cashbackService;
	@BY_NAME
	private ProductService productService;
	public void index(){
		render("/cashback/cashback_index.html");
	}
	
	public void set(){
		if(getParaToLong("pid") != null){
			setAttr("cashback", cashbackService.findById(getParaToLong("pid")));
			List<CashbackProduct> cashProducts = cashbackService.getCashbackProducts(getParaToLong("pid"));
			setAttr("cashProducts", cashProducts);
		}
		render("/cashback/cashback_set.html");
	}
	
	public void listProducts(){
		ProductParamDto productParamDto = new ProductParamDto(getSellerId(), getPageNo());
		try {
			Page<ProductResultDto> pages = cashbackService.getProducts4CashbackPage(productParamDto);
			rendSuccessJson(pages);
		} catch (ProductException e) {
			rendFailedJson(e.getMessage());
		}
	}
	
	public void listSelectProduct(){
		String productIds=getPara("productIds");
		try {
			List<ProductResultDto> prouctDto=productService.getProducts(productIds);
			rendSuccessJson(prouctDto);
		} catch (ProductException e) {
			rendFailedJson(e.getMessage());
		}
	}
	
	public void list(){
		PromotionParamDto promotionParam = new PromotionParamDto(getSellerId(), getPageNo());
		try{
		Page<CashbackResultDto> pages=cashbackService.list(promotionParam);
		rendSuccessJson(pages);
		} catch (UmpException e) {
			rendFailedJson(e.getMessage());
		}
	}
	
	public void save(){
		try {
			final String productIds = getPara("product_ids");
			Cashback cashback=getModel();
			cashbackService.save(cashback, productIds, getSellerId());
			rendSuccessJson();
		} catch (Exception e) {
			rendFailedJson(e.getMessage());
		}
		
	}	
	
	@Override
	protected Class<Cashback> getModelClass() {
		return Cashback.class;
	}

}
