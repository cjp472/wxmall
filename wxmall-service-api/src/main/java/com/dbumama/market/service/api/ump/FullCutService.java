package com.dbumama.market.service.api.ump;

import java.util.List;

import com.dbumama.market.model.FullCut;
import com.dbumama.market.model.FullCutSet;
import com.dbumama.market.model.Product;
import com.dbumama.market.service.api.product.ProductException;
import com.dbumama.market.service.api.product.ProductParamDto;
import com.dbumama.market.service.api.product.ProductResultDto;
import com.jfinal.plugin.activerecord.Page;

/**
 * 满减送
 * @author drs
 *
 */
public interface FullCutService {
	public FullCut save(FullCutParamDto paramDto)throws UmpException;
	
	public Page<ProductResultDto> getProducts4FullCutPage(ProductParamDto productParamDto) throws ProductException;
    
	public Page<FullCut> list(PromotionParamDto promotionParam) throws UmpException;
	
	public List<ProdFullCutResultDto> getProductFullCut(Product product) throws UmpException;
	
	FullCut findById(Long id);
	
	List<FullCutSet> getFullCutSetsByFullCut(Long fullCutId); 
}
