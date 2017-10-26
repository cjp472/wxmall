package com.dbumama.market.web.api.category;

import java.util.List;

import com.dbumama.market.model.ProductCategory;
import com.dbumama.market.service.api.product.ProductCategoryService;
import com.dbumama.market.web.core.annotation.RouteBind;
import com.dbumama.market.web.core.controller.BaseApiController;
import com.dbumama.market.web.core.plugin.spring.Inject.BY_NAME;

@RouteBind(path = "category")
public class CategoryController  extends BaseApiController{

	@BY_NAME
	ProductCategoryService productCategoryService;
	
	public void list(){
		List<ProductCategory> categories = productCategoryService.list();
		if(categories!=null){
			for(ProductCategory pv :categories){
				 pv.setImgPath(getImageDomain() + pv.getImgPath());
			}
		}
		rendSuccessJson(categories);
	}
}
