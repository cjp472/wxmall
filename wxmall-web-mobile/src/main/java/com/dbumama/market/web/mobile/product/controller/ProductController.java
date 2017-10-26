package com.dbumama.market.web.mobile.product.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import com.dbumama.market.model.BuyerUser;
import com.dbumama.market.model.Product;
import com.dbumama.market.model.ProductCategory;
import com.dbumama.market.model.ProductReview;
import com.dbumama.market.service.api.authuser.AuthUserConfigService;
import com.dbumama.market.service.api.cart.CartService;
import com.dbumama.market.service.api.product.ProductCategoryService;
import com.dbumama.market.service.api.product.ProductDetailResultDto;
import com.dbumama.market.service.api.product.ProductException;
import com.dbumama.market.service.api.product.ProductMobileParamDto;
import com.dbumama.market.service.api.product.ProductMobileResultDto;
import com.dbumama.market.service.api.product.ProductParamDto;
import com.dbumama.market.service.api.product.ProductService;
import com.dbumama.market.service.api.product.ProductSpecPriceResultDto;
import com.dbumama.market.service.api.product.ProductSpecificationService;
import com.dbumama.market.service.api.ump.GroupingResultDto;
import com.dbumama.market.service.api.ump.GrouponService;
import com.dbumama.market.service.api.ump.ProdGroupResultDto;
import com.dbumama.market.web.core.annotation.RouteBind;
import com.dbumama.market.web.core.controller.BaseMobileController;
import com.dbumama.market.web.core.plugin.spring.Inject.BY_NAME;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;


/**
 * Created by dextrys on 2016/1/8.
 */
@RouteBind(path="product")
public class ProductController extends BaseMobileController {
	
	@BY_NAME
	CartService cartService;
	@BY_NAME
	private ProductService productService;
	@BY_NAME
	private ProductCategoryService productCategoryService;
	@BY_NAME
	private ProductSpecificationService productSpecificationService;
	@BY_NAME
	private GrouponService grouponService;
	@BY_NAME
	private AuthUserConfigService authUserConfigService;
	
	public void index(){
		List<ProductCategory> productCategorys = productCategoryService.list();
		/*List<ProductCategory> validCategorys = new ArrayList<ProductCategory>();
		for(ProductCategory pcate : productCategorys){
			List<Product> products = Product.dao.find("select * from " + Product.table + " where product_category_id=? ", pcate.getId());
			if(products != null && products.size()>0){
				validCategorys.add(pcate);
			}
		}*/
		String keyword=getPara("keyword");
		setAttr("productCategory", productCategorys);
		Long productCategoryId = getParaToLong("categId");
		setAttr("categId", productCategoryId);
		setAttr("keyword", keyword);
		render("/product/index.html");
	}
		
	public void list(){
		Long productCategoryId = getParaToLong("categId");
		String keyword=getPara("keyword");
		BigDecimal startPrice = new BigDecimal(getPara("startPrice","0"));
		BigDecimal endPrice = new BigDecimal(getPara("endPrice","0"));
		ProductMobileParamDto mobileParamDto = new ProductMobileParamDto(getSellerId(), getPageNo());
		mobileParamDto.setCategId(productCategoryId);
		mobileParamDto.setStartPrice(startPrice);
		mobileParamDto.setEndPrice(endPrice);
		mobileParamDto.setKeyword(keyword);
		List<ProductMobileResultDto> productResultDtos = productService.findProducts4Mobile(mobileParamDto);
		rendSuccessJson(productResultDtos);
	}

    public void detail() {
    	try {
    		ProductParamDto productParamDto = new ProductParamDto(getSellerId());
    		productParamDto.setProductId(getParaToLong("id"));
    		ProductDetailResultDto productDetail = productService.getMobieDetail(productParamDto);
    		setAttr("productDetail", productDetail);
    		
            List<Record> reviews = Db.find("select p.*, b.nickname, b.headimgurl from " + ProductReview.table + " p"
            		+ " left join " + BuyerUser.table + " b on p.buyer_id=b.id"
            		+ " where product_id=? order by p.created desc ", productParamDto.getProductId());
            setAttr("reviews", reviews);
            
            setAttr("appUser", authUserConfigService.getAuthConfig());
            
            if(productDetail.getGroupInfo() != null){
            	if(getParaToLong("gheaderUserId") != null){
            		//读取拼团成员等信息
                	GroupingResultDto groupingInfo = grouponService.getGroupUserInfos(productDetail.getProduct(), getParaToLong("gheaderUserId"), getBuyerId());
                	setAttr("groupingInfo", groupingInfo);	      		
            	}
            	//正在进行中的拼团
            	List<GroupingResultDto> groupings = grouponService.getGroupsByProduct(productDetail.getProduct());
            	setAttr("groupings", groupings);
            	render("/product/g_detail.html");
            }else{
            	setAttr("cartCount", cartService.getCartItemCountByBuyer(getBuyerId()));
            	render("/product/detail.html");            	
            }
		} catch (Exception e) {
			setAttr("error", "获取商品详情出错:" + e.getMessage());
			render("/product/detail_error.html");
		}
    }
    
    /**
     * 参团购买
     */
    public void join(){
    	Product product = productService.findById(getParaToLong("productId"));
    	setAttr("product", product);
    	ProdGroupResultDto groupInfo=grouponService.getProductGroup(product);
    	setAttr("groupInfo", groupInfo);
    	GroupingResultDto groupingInfo = grouponService.getGroupUserInfos(product, getParaToLong("headerId"), getBuyerId());
    	setAttr("groupingInfo", groupingInfo);   
    	render("/product/g_join.html");
    }
    
    public void stocks(){
    	String productId=getPara("productId");
    	try {
    		HashMap<String, ProductSpecPriceResultDto> data = productService.getProductSpecPrice(Long.valueOf(productId));
    		rendSuccessJson(data);
		} catch (ProductException e) {
			rendFailedJson(e.getMessage());
		}
    }
    
    public void getPromotionProductList(){
    	ProductMobileParamDto mobileParamDto = new ProductMobileParamDto(getSellerId(), getPageNo());
    	try {
    		Page<ProductMobileResultDto> products = productService.getMobilePromotionProduct(mobileParamDto);
    		rendSuccessJson(products.getList());
		} catch (ProductException e) {
			rendFailedJson(e.getMessage());
		}
    }
    
    /**
     * 获取打折商品列表
     */
    public void getPromotionProduct(){
    	render("/product/promotion_index.html");
    }
    
    /**
     * 获取拼团商品列表
     */
    public void getGroupProduct(){
    	render("/groups/prod_group_index.html");
    }
    
    public void getGroupProductList(){
    	ProductMobileParamDto mobileParamDto = new ProductMobileParamDto(getSellerId(), getPageNo());
    	try {
    		Page<ProductMobileResultDto> products = productService.getMobileGroupProduct(mobileParamDto);
    		rendSuccessJson(products.getList());
		} catch (ProductException e) {
			rendFailedJson(e.getMessage());
		}
    }
    
}
