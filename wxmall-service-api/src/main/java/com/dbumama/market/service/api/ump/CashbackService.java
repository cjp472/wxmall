package com.dbumama.market.service.api.ump;

import java.math.BigDecimal;
import java.util.List;

import com.dbumama.market.model.BuyerUser;
import com.dbumama.market.model.Cashback;
import com.dbumama.market.model.CashbackProduct;
import com.dbumama.market.model.Order;
import com.dbumama.market.model.OrderItem;
import com.dbumama.market.model.Product;
import com.dbumama.market.service.api.product.ProductException;
import com.dbumama.market.service.api.product.ProductParamDto;
import com.dbumama.market.service.api.product.ProductResultDto;
import com.jfinal.plugin.activerecord.Page;

/**
 * 订单返现
 * @author
 *
 */
public interface CashbackService {
	
	public void save(Cashback cashback, String productIds, Long sellerId) throws UmpException;
	
	public Page<CashbackResultDto> list(PromotionParamDto promotionParam) throws UmpException;
	
	public Page<ProductResultDto> getProducts4CashbackPage(ProductParamDto productParamDto) throws ProductException;
	
	public Cashback getProductCashSet(ProductParamDto productParamDto) throws ProductException;
	
	public ProdCashbackResultDto getProductCashBack(Product product) throws ProductException;
	
	/**
	 * 给用户返现
	 * @param buyer
	 * @param order
	 * @param product
	 * @param cashback
	 * @throws UmpException
	 */
	public BigDecimal cash2Buyer(BuyerUser buyer, Order order, List<OrderItem> orderItems, Product product, ProdCashbackResultDto cashback) throws UmpException;
	
	Cashback findById(Long id);
	
	List<CashbackProduct> getCashbackProducts(Long cashbackId);
}
