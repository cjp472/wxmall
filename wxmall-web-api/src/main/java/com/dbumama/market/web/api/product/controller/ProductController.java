package com.dbumama.market.web.api.product.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.dbumama.market.model.BuyerUser;
import com.dbumama.market.model.ProductReview;
import com.dbumama.market.service.api.authuser.AuthUserConfigService;
import com.dbumama.market.service.api.cart.CartService;
import com.dbumama.market.service.api.product.ProductDetailResultDto;
import com.dbumama.market.service.api.product.ProductMobileParamDto;
import com.dbumama.market.service.api.product.ProductMobileResultDto;
import com.dbumama.market.service.api.product.ProductParamDto;
import com.dbumama.market.service.api.product.ProductService;
import com.dbumama.market.service.api.product.ProductSpecPriceResultDto;
import com.dbumama.market.service.api.ump.GroupingResultDto;
import com.dbumama.market.service.api.ump.GrouponService;
import com.dbumama.market.web.core.annotation.RouteBind;
import com.dbumama.market.web.core.controller.BaseApiController;
import com.dbumama.market.web.core.plugin.spring.Inject.BY_NAME;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

@RouteBind(path = "product")
public class ProductController extends BaseApiController{

	@BY_NAME
	private ProductService productService;
	@BY_NAME
	private GrouponService grouponService;
	@BY_NAME
	CartService cartService;
	@BY_NAME
	private AuthUserConfigService authUserConfigService;
	
	public void list(){
		Long productCategoryId = getJSONParaToLong("categId");//categId
		BigDecimal startPrice = new BigDecimal(getPara("startPrice","0"));
		BigDecimal endPrice = new BigDecimal(getPara("endPrice","0"));
		ProductMobileParamDto mobileParamDto = new ProductMobileParamDto(getSellerId(), getPageNo());
		mobileParamDto.setCategId(productCategoryId);
		mobileParamDto.setStartPrice(startPrice);
		mobileParamDto.setEndPrice(endPrice);
		List<ProductMobileResultDto> productResultDtos = productService.findProducts4Mobile(mobileParamDto);
		rendSuccessJson(productResultDtos);
	}
	
	public void detail(){
		try {
			ProductParamDto productParamDto = new ProductParamDto(getSellerId());
			productParamDto.setProductId(getJSONParaToLong("id"));
			ProductDetailResultDto productDetail = productService.getMobieDetail(productParamDto);
			JSONObject result = new JSONObject();
			result.put("productDetail", productDetail);
			
	        List<Record> reviews = Db.find("select p.*, b.nickname, b.headimgurl from " + ProductReview.table + " p"
	        		+ " left join " + BuyerUser.table + " b on p.buyer_id=b.id"
	        		+ " where product_id=? order by p.created desc ", productParamDto.getProductId());
	        result.put("reviews", reviews);
	        
	        if(productDetail.getGroupInfo() != null){
	        	if(getParaToLong("gheaderUserId") != null){
	        		//读取拼团成员等信息
	            	GroupingResultDto groupingInfo = grouponService.getGroupUserInfos(productDetail.getProduct(), getParaToLong("gheaderUserId"), getBuyerId());
	            	result.put("groupingInfo", groupingInfo);	      		
	        	}
	        	//正在进行中的拼团
	        	List<GroupingResultDto> groupings = grouponService.getGroupsByProduct(productDetail.getProduct());
	        	result.put("groupings", groupings);
	        }
	        if(getBuyerUser()!=null){
        		result.put("cartCount", cartService.getCartItemCountByBuyer(getBuyerId()));	        		
        	}
	        
	        //规格
	        HashMap<String, ProductSpecPriceResultDto> priceMap=productService.getProductSpecPrice(productParamDto.getProductId());
	        result.put("specPriceMap", priceMap);
	        
	        rendSuccessJson(result);
		} catch (Exception e) {
			rendFailedJson(e.getMessage());
		}
	}
	
}
