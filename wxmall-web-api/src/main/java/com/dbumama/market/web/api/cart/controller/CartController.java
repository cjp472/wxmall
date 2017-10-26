package com.dbumama.market.web.api.cart.controller;


import java.util.List;
import com.dbumama.market.model.Cart;
import com.dbumama.market.service.api.cart.CartItemResultDto;
import com.dbumama.market.service.api.cart.CartService;
import com.dbumama.market.service.api.exception.MarketBaseException;
import com.dbumama.market.web.core.annotation.RouteBind;
import com.dbumama.market.web.core.controller.BaseApiController;
import com.dbumama.market.web.core.plugin.spring.Inject.BY_NAME;
import com.jfinal.kit.StrKit;

@RouteBind(path = "cart")
public class CartController extends BaseApiController{
	@BY_NAME
	CartService cartService;
	public void list() {
		try {
			List<CartItemResultDto> cartItems = cartService.getCartsByBuyer(getBuyerId());
			rendSuccessJson(cartItems);
		} catch (MarketBaseException e) {
			rendFailedJson(e.getMessage());
		}
    }
	

	public void addCart(){
		Long productId = getJSONParaToLong("productId");
        int quantity = getJSONParaToInteger("quantity");
        String speci = getJSONPara("speci");//规格值
        try {
			cartService.add(getBuyerId(), productId, quantity, speci);
			//取得购物车的品种数
			Long count=cartService.getCartItemCountByBuyer(getBuyerId());
			rendSuccessJson(count);
		} catch (MarketBaseException e) {
			rendFailedJson(e.getMessage());
		}
	}
	/**
	 * 取得购物车的品种数
	 */
	public void getCartCount(){
        try {
			Long count=cartService.getCartItemCountByBuyer(getBuyerId());
			rendSuccessJson(count);
		} catch (MarketBaseException e) {
			rendFailedJson(e.getMessage());
		}
	}
	
	public void delete() {
        String cartIds = getJSONPara("ids");
        if(StrKit.isBlank(cartIds)){
        	rendFailedJson("请选择要删除的项");
        	return;
        }
        for(String id : cartIds.split("#")){
        	Cart citem = cartService.findById(Long.valueOf(id));
        	if(citem != null){
        		citem.delete();
        	}
        }
        rendSuccessJson();
    }
}
