package com.dbumama.market.web.api.index.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dbumama.market.model.Shop;
import com.dbumama.market.service.api.product.ProductMobileParamDto;
import com.dbumama.market.service.api.product.ProductMobileResultDto;
import com.dbumama.market.service.api.product.ProductService;
import com.dbumama.market.service.api.shop.ShopService;
import com.dbumama.market.web.core.annotation.RouteBind;
import com.dbumama.market.web.core.controller.BaseApiController;
import com.dbumama.market.web.core.plugin.spring.Inject.BY_NAME;
import com.jfinal.kit.StrKit;

@RouteBind(path = "/")
public class IndexController extends BaseApiController {

	@BY_NAME
	private ProductService productService;
	@BY_NAME
	private ShopService shopService;
	
	public void index(){
		Map<String, Object> result = new HashMap<String, Object>();
		Shop shop = shopService.findBySeller();
		result.put("shop", shop);
		
		if(shop!=null && StrKit.notBlank(shop.getShopSign())){
	     	shop.setShopSign(this.getImageDomain()+shop.getShopSign());
		}
		if(shop!=null && StrKit.notBlank(shop.getShopLogo())){
	     	shop.setShopLogo(this.getImageDomain()+shop.getShopLogo());
		}
		
		ProductMobileParamDto mobileParamDto = new ProductMobileParamDto(getSellerId(), getPageNo());
		//查询商品列表
		List<ProductMobileResultDto> indexProducts = productService.getIndexProduct(mobileParamDto);
		result.put("indexProducts", indexProducts);
		List<ProductMobileResultDto> hotProducts = productService.getHotProduct(mobileParamDto);
		result.put("hotProducts", hotProducts);
		List<ProductMobileResultDto> newProducts = productService.getNewProduct(mobileParamDto);
		result.put("newProducts", newProducts);
		List<ProductMobileResultDto> commondProducts = productService.getRecommendProduct(mobileParamDto);
		result.put("commondProducts", commondProducts);
		rendSuccessJson(result);
	}
	
}
