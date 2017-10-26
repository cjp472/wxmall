package com.dbumama.market.service.api.ump;

import java.math.BigDecimal;
import java.util.List;

import com.dbumama.market.model.MultiGroup;
import com.dbumama.market.model.Product;
import com.dbumama.market.service.api.product.ProductException;
import com.dbumama.market.service.api.product.ProductParamDto;
import com.dbumama.market.service.api.product.ProductResultDto;
import com.jfinal.plugin.activerecord.Page;

public interface GrouponService {
	
	public void save(MultiGroup multiGroup, Long sellerId, String multiGroupSetItems)throws UmpException;
	
	/**
	 * 获取拼团活动详细信息
	 * @param grouponId
	 * @return
	 * @throws UmpException
	 */
	public GrouponResultDto getGroupInfo(Long grouponId) throws UmpException;
	
	/**
	 * 获取拼团“要选择”的商品信息
	 * @param productParamDto
	 * @return
	 * @throws ProductException
	 */
	public Page<ProductResultDto> getProducts4GrouponPage(ProductParamDto productParamDto) throws ProductException;
	
    /**
     * 获取拼团列表
     * @param grouponParamDto
     * @return
     * @throws UmpException
     */
	public Page<MultiGroup> list(GrouponParamDto grouponParamDto) throws UmpException; 
	
	/**
	 * 返回拼团“已选择”的商品信息：规格
	 * @param multiGroup
	 * @param productId
	 * @return
	 * @throws UmpException
	 */
	public ProductResultDto getProductResultDto(MultiGroup multiGroup, Long productId)throws UmpException;
	
	/**
	 * 微信端获取拼团信息
	 * @param product
	 * @return
	 * @throws ProductException
	 */
	public ProdGroupResultDto getProductGroup(Product product) throws UmpException;
	
	/**
	 * 获取拼团价
	 * @param productId
	 * @param groupId
	 * @param specvalue
	 * @return
	 * @throws ProductException
	 */
	public BigDecimal getCollagePrice(Product product, String specvalue) throws ProductException;
	
	/**
	 * 获取商品有效的拼团活动
	 * @param product
	 * @return
	 * @throws UmpException
	 */
	public MultiGroup getProductMultiGroup(Product product) throws UmpException;
	
	/**
	 * 获取拼团成员，拼团有效时限倒计时等信息
	 * @param productId			拼团商品
	 * @param groupUserId		开团者
	 * @param groupUserId		组团者
	 * @return
	 * @throws UmpException
	 */
	public GroupingResultDto getGroupUserInfos(Product product, Long gheaderId, Long buyerId) throws UmpException;
	
	/**
	 * 获取当前商品正在进行中的拼团列表
	 * @param product
	 * @return
	 * @throws UmpException
	 */
	public List<GroupingResultDto> getGroupsByProduct(Product product) throws UmpException;
	
}
