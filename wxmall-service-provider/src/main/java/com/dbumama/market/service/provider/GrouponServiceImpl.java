package com.dbumama.market.service.provider;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dbumama.market.model.BuyerUser;
import com.dbumama.market.model.MultiGroup;
import com.dbumama.market.model.MultiGroupSet;
import com.dbumama.market.model.OrderGheader;
import com.dbumama.market.model.OrderGuser;
import com.dbumama.market.model.Product;
import com.dbumama.market.model.ProductSpecItem;
import com.dbumama.market.model.ProductSpecification;
import com.dbumama.market.model.ProductSpecificationValue;
import com.dbumama.market.model.Specification;
import com.dbumama.market.model.SpecificationValue;
import com.dbumama.market.service.api.product.ProductException;
import com.dbumama.market.service.api.product.ProductParamDto;
import com.dbumama.market.service.api.product.ProductResultDto;
import com.dbumama.market.service.api.ump.GroupingResultDto;
import com.dbumama.market.service.api.ump.GrouponParamDto;
import com.dbumama.market.service.api.ump.GrouponResultDto;
import com.dbumama.market.service.api.ump.GrouponService;
import com.dbumama.market.service.api.ump.GrouponUserResultDto;
import com.dbumama.market.service.api.ump.ProdGroupResultDto;
import com.dbumama.market.service.api.ump.UmpException;
import com.dbumama.market.service.base.AbstractServiceImpl;
import com.dbumama.market.service.sql.QueryHelper;
import com.dbumama.market.service.utils.DateTimeUtil;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
@Service("grouponService")
public class GrouponServiceImpl extends AbstractServiceImpl implements GrouponService{

	private static final BuyerUser buyerUserdao = new BuyerUser().dao();
	private static final MultiGroup multiGroupdao = new MultiGroup().dao();
	private static final MultiGroupSet multiGroupSetdao = new MultiGroupSet().dao();
	private static final OrderGheader orderGheaderdao = new OrderGheader().dao();
	private static final Product productDao = new Product().dao();
	private static final ProductSpecification prodSpecidao = new ProductSpecification().dao();
	private static final ProductSpecificationValue prodSpecValuedao = new ProductSpecificationValue().dao();
	private static final ProductSpecItem prodSpecItemdao = new ProductSpecItem().dao();
	private static final SpecificationValue specValueDao = new SpecificationValue().dao();
	private static final Specification specDao = new Specification().dao();
	
	@Override
	@Transactional(rollbackFor = UmpException.class)
	public void save(MultiGroup multiGroup, Long sellerId, String multiGroupSetItems) throws UmpException {
		if(multiGroup == null || StrKit.isBlank(multiGroupSetItems) || sellerId == null)
			throw new UmpException("保存多人拼团缺少必要参数");
		if(multiGroup.getId() == null){
			multiGroup.setSellerId(sellerId);
			multiGroup.setActive(true);
			multiGroup.save();
		}else{
			multiGroup.update();
		}
		JSONArray jarr = null;
		try {
			jarr = JSONArray.parseArray(multiGroupSetItems);
		} catch (Exception e) {
			throw new UmpException(e.getMessage());
		}
		if(jarr == null || jarr.size() <=0) throw new UmpException("未设置活动拼团信息");
		
		JSONArray newArr = new JSONArray();
		for(Iterator<?> iterator = jarr.iterator(); iterator.hasNext();){
			JSONObject job = (JSONObject) iterator.next(); 
			 if(!"del".equals(job.getString("opt"))){
				newArr.add(job);
			 }
		}
		if(newArr.size()<=0) throw new UmpException("没有设置拼团商品");
		
		List<MultiGroupSet> multiGroupAddSet=new ArrayList<MultiGroupSet>();
		List<MultiGroupSet> multiGroupUpdateSet=new ArrayList<MultiGroupSet>();
		List<MultiGroupSet> multiGroupDelSet=new ArrayList<MultiGroupSet>();
		for(int i=0;i<jarr.size();i++){
			JSONObject jsonObj = jarr.getJSONObject(i);
			Long multiGroupSetId = jsonObj.getLong("msetId");
			MultiGroupSet multiGroupSet = null;
			if(multiGroupSetId != null){//修改拼团设置项
				multiGroupSet=multiGroupSetdao.findById(multiGroupSetId);
				if(multiGroupSet == null) continue;
				if("del".equals(jsonObj.getString("opt"))){
					multiGroupDelSet.add(multiGroupSet);
				}else{
					try {
						if(jsonObj == null 
								|| jsonObj.getLong("productId") == null 
								|| jsonObj.getBigDecimal("prime_cost") == null 
								|| jsonObj.getBigDecimal("collage_price") == null)
							throw new UmpException("保存拼团活动，拼团价设置项值有误，请检查拼团价格");				
					} catch (Exception e) {
						throw new UmpException("保存拼团活动失败，拼团价设置项值有误，未知异常");
					}
					if(jsonObj.getBigDecimal("collage_price").compareTo(jsonObj.getBigDecimal("prime_cost"))==1){
						throw new UmpException("拼团价格大于原价请检查拼团价格");
					}
					multiGroupSet.setPrimeCost(jsonObj.getBigDecimal("prime_cost"));
					multiGroupSet.setCollagePrice(jsonObj.getBigDecimal("collage_price"));
					multiGroupSet.setUpdated(new Date());
					multiGroupUpdateSet.add(multiGroupSet);
				}
			}else{
				if("del".equals(jsonObj.getString("opt"))) continue;//排除删除项
				try {
					if(jsonObj == null 
							|| jsonObj.getLong("productId") == null 
							|| jsonObj.getBigDecimal("prime_cost") == null 
							|| jsonObj.getBigDecimal("collage_price") == null)
						throw new UmpException("保存拼团活动，拼团价设置项值有误，请检查拼团价格");				
				} catch (Exception e) {
					throw new UmpException("保存拼团活动失败，拼团价设置项值有误，未知异常");
				}
				if(jsonObj.getBigDecimal("collage_price").compareTo(jsonObj.getBigDecimal("prime_cost"))==1){
					throw new UmpException("拼团价格大于原价请检查拼团价格");
				}
				//新增拼团设置项
				multiGroupSet=new MultiGroupSet();
				multiGroupSet.setProductId(jsonObj.getLong("productId"));
				multiGroupSet.setMultiGroupId(multiGroup.getId());
				multiGroupSet.setSpecificationValue(jsonObj.getString("specificationValue"));
				multiGroupSet.setPrimeCost(jsonObj.getBigDecimal("prime_cost"));
				multiGroupSet.setCollagePrice(jsonObj.getBigDecimal("collage_price"));
				multiGroupSet.setCreated(new Date());
				multiGroupSet.setActive(1);
				multiGroupSet.setUpdated(new Date());
				multiGroupAddSet.add(multiGroupSet);
			}
		}
		if(multiGroupAddSet.size()<=0 && multiGroupDelSet.size() <=0 && multiGroupUpdateSet.size()<=0)
			throw new UmpException("没有设置拼团项数据");
		try {
			if(multiGroupAddSet.size() > 0){
				Db.batchSave(multiGroupAddSet, multiGroupAddSet.size());	
			}
			if(multiGroupUpdateSet.size() > 0){
				Db.batchUpdate(multiGroupUpdateSet, multiGroupUpdateSet.size());	
			}
			if(multiGroupDelSet.size()>0){
				for(MultiGroupSet mg : multiGroupDelSet){
					if(mg != null) mg.delete();
				}
			}
		} catch (Exception e) {
			throw new UmpException(e.getMessage());
		}
	}
	
	@Override
	public GrouponResultDto getGroupInfo(Long grouponId) throws UmpException {
		if(grouponId == null) throw new UmpException("获取拼团数据缺少必要参数");
		MultiGroup multiGroup=multiGroupdao.findById(grouponId);
		if(multiGroup == null) throw new UmpException("拼团数据不存在");
		
		List<MultiGroupSet> mgsets = multiGroupSetdao.find("select * from " + MultiGroupSet.table + " where multi_group_id=? ", grouponId); 
		Set<Long> productIds = new HashSet<Long>();
		for(MultiGroupSet mgs : mgsets){
			productIds.add(mgs.getProductId());
		}
		
		List<ProductResultDto> productDtos=new ArrayList<ProductResultDto>();
		for (Long productid : productIds) {
			ProductResultDto dto=getProductResultDto(multiGroup, productid);
			productDtos.add(dto);
		}
		GrouponResultDto resultDto=new GrouponResultDto();
		resultDto.setMultiGroup(multiGroup);
		resultDto.setProductDto(productDtos);
		return resultDto;
	}
	
	@Override
	public Page<ProductResultDto> getProducts4GrouponPage(ProductParamDto productParamDto) throws ProductException {
		List<ProductResultDto> resultDtos = new ArrayList<ProductResultDto>();
		Date currDate = new Date();
		final String select = "select * ";
		final String sqlExceptSelect = " from "+Product.table+" where seller_id=? and is_marketable=1 and active=1 and id not in"
				                        + " (select product_id from " + MultiGroupSet.table + " where multi_group_id "
				                        + "in (select id from "+MultiGroup.table+" where seller_id=? and active=1 and start_date<=? and end_date >=? ))";
		Page<Product> pages = productDao.paginate(productParamDto.getPageNo(), productParamDto.getPageSize(),
				select, sqlExceptSelect,productParamDto.getSellerId(),productParamDto.getSellerId(), currDate, currDate);
		for(Product product : pages.getList()){
			ProductResultDto resultDto = new ProductResultDto();
			resultDto.setId(product.getId());
			resultDto.setImg(getImageDomain() + product.getImage());
			resultDto.setName(product.getName().length()>10?product.getName().substring(0, 10).concat("..."):product.getName());
			resultDto.setPrice(product.getPrice());
			resultDto.setStock(product.getStock());
			
			 if(product.getIsUnifiedSpec() !=null && !product.getIsUnifiedSpec()){//是否多规格
				     List<String> arraySpec=new ArrayList<>();
				    String spec="";
					List<ProductSpecItem> specItems = prodSpecItemdao.find("select * FROM "+ProductSpecItem.table+" WHERE product_id = ? ", product.getId());
                       for (ProductSpecItem productSpecItem : specItems) {
                    	   spec=spec+productSpecItem.getPrice()+";"+productSpecItem.getStock()+",";
					}
                     arraySpec.add(spec);
				    List<String> arrayTitle=new ArrayList<>();
				    List<String> arrayInfor=new ArrayList<>();
					List<String> arrayInforId=new ArrayList<>();
				 List<ProductSpecification> prodSpeces = prodSpecidao.find("select * from " + ProductSpecification.table + " where product_id = ?", product.getId());
		            for(ProductSpecification ps : prodSpeces){
		            	Specification speci = specDao.findById(ps.getSpecificationId());
		            	arrayTitle.add(speci.getName());
		            	String names="";
		            	String ids="";
		            	List<ProductSpecificationValue> prodSpecValues = prodSpecValuedao.find(
		            			"select * from " + ProductSpecificationValue.table + " where product_id=? and specification_id = ? ", product.getId(), ps.getSpecificationId());
		            	for(ProductSpecificationValue productSpecValue : prodSpecValues){
		            		SpecificationValue specificationValue = specValueDao.findById(productSpecValue.getSpecificationValueId());
		            		names=names+specificationValue.getName()+";";
		            		ids=ids+specificationValue.getId().toString()+";";
		            	}
		            	arrayInfor.add(names);
		            	arrayInforId.add(ids);
		            }
		            
		            resultDto.setArrayTitle(arrayTitle);
		            resultDto.setArrayInfor(arrayInfor);
		            resultDto.setArrayInforId(arrayInforId);
		            resultDto.setArraySpec(arraySpec);
			 }
			resultDtos.add(resultDto);
		}
	
		return new Page<ProductResultDto>(resultDtos, pages.getPageNumber(), pages.getPageSize(), pages.getTotalPage(), pages.getTotalRow());

	}
	
	@Override
	public Page<MultiGroup> list(GrouponParamDto grouponParamDto) throws UmpException {
		if(grouponParamDto == null)
			throw new UmpException("获取拼团活动列表数据参数错误");
		Page<MultiGroup> pages = multiGroupdao.paginate(grouponParamDto.getPageNo(), grouponParamDto.getPageSize(),
				"select * ", 
				" from " + MultiGroup.table + " where active=1 ");
		return pages;
	}
	
	@Override
	public ProductResultDto getProductResultDto(MultiGroup multiGroup, Long productId) throws UmpException {
		Product product = productDao.findById(productId);
		ProductResultDto resultDto = new ProductResultDto();
		resultDto.setId(product.getId());
		resultDto.setImg(getImageDomain() + product.getImage());
		resultDto.setName(product.getName().length()>10?product.getName().substring(0, 10).concat("..."):product.getName());
		resultDto.setPrice(product.getPrice());
		resultDto.setStock(product.getStock());
		
		 if(product.getIsUnifiedSpec() !=null && !product.getIsUnifiedSpec()){//是否多规格
			     List<String> arraySpec=new ArrayList<>();
			    String spec="";
				List<ProductSpecItem> specItems = prodSpecItemdao.find("select * FROM "+ProductSpecItem.table+" WHERE product_id = ? ", product.getId());
                   for (ProductSpecItem productSpecItem : specItems) {
                	   spec=spec+productSpecItem.getPrice()+";"+productSpecItem.getStock()+",";
				}
                 arraySpec.add(spec);
			    List<String> arrayTitle=new ArrayList<>();
			    List<String> arrayInfor=new ArrayList<>();
				List<String> arrayInforId=new ArrayList<>();
			 List<ProductSpecification> prodSpeces = prodSpecidao.find("select * from " + ProductSpecification.table + " where product_id = ?", product.getId());
	            for(ProductSpecification ps : prodSpeces){
	            	Specification speci = specDao.findById(ps.getSpecificationId());
	            	arrayTitle.add(speci.getName());
	            	String names="";
	            	String ids="";
	            	List<ProductSpecificationValue> prodSpecValues = prodSpecValuedao.find(
	            			"select * from " + ProductSpecificationValue.table + " where product_id=? and specification_id = ? ", product.getId(), ps.getSpecificationId());
	            	for(ProductSpecificationValue productSpecValue : prodSpecValues){
	            		SpecificationValue specificationValue = specValueDao.findById(productSpecValue.getSpecificationValueId());
	            		names=names+specificationValue.getName()+";";
	            		ids=ids+specificationValue.getId().toString()+";";
	            	}
	            	arrayInfor.add(names);
	            	arrayInforId.add(ids);
	            }
	            
	            resultDto.setArrayTitle(arrayTitle);
	            resultDto.setArrayInfor(arrayInfor);
	            resultDto.setArrayInforId(arrayInforId);
	            resultDto.setArraySpec(arraySpec);

		 }
			 List<String> arrayGroupSet=new ArrayList<>();
			 String groupSet="";
			 List<MultiGroupSet> items = multiGroupSetdao.find("select * FROM "+MultiGroupSet.table+" WHERE product_id = ? and multi_group_id=?", product.getId(),multiGroup.getId());
			 for (MultiGroupSet multiGroupSet : items) {
				 groupSet=groupSet+multiGroupSet.getId()+";"+multiGroupSet.getCollagePrice()+",";
			}
			 arrayGroupSet.add(groupSet);
			 resultDto.setArrayGroupSet(arrayGroupSet);
		 
		return resultDto;
	}
	
	@Override
	public ProdGroupResultDto getProductGroup(Product product) throws UmpException {
		if(product == null) throw new ProductException("没有商品");
		
		MultiGroup multiGroup = getProductMultiGroup(product);
		if(multiGroup == null) return null;
		
		ProdGroupResultDto dto=new ProdGroupResultDto();
		if(multiGroup.getStartDate().after(new Date())){
			dto.setGroupTime("还差" + DateTimeUtil.compareDay(multiGroup.getStartDate(), new Date()) + "天开始");        				
		}else {
			dto.setGroupTime("剩余" + DateTimeUtil.compareDay(multiGroup.getEndDate(), new Date()) + "天结束");
		}
		dto.setQuota(multiGroup.getQuota());//当此拼团没人限购件数
		dto.setGroupId(multiGroup.getId());
		dto.setGroupNum(multiGroup.getOfferNum()+"人成团");
		dto.setCollagePrice(getCollageSectionPrice(product, multiGroup));
		return dto;
	}

	/**
	 * 获取商品拼团活动区间价
	 * @param productId
	 * @return
	 */
	private String getCollageSectionPrice(Product product, MultiGroup multiGroup){
		String sql="select * from "+MultiGroupSet.table+" where multi_group_id=? and product_id=?";
		List<MultiGroupSet> set=multiGroupSetdao.find(sql, multiGroup.getId(),product.getId());
		BigDecimal min,max;
		min=max=set.get(0).getCollagePrice();
		
		for (MultiGroupSet multiGroupSet : set) {
			BigDecimal bprice =multiGroupSet.getCollagePrice();
			if(bprice.compareTo(max)==1){
				max= bprice;
			}
			if(bprice.compareTo(min)==-1){
				min= bprice;
			}
		}
		if(max.compareTo(min)==0){
			return max.toString();
		}else{  
		    return min+" ~ "+max;
		}
		
	}
	
	@Override
	public BigDecimal getCollagePrice(Product product, String specvalue) throws ProductException {
		MultiGroup group = getProductMultiGroup(product);
		if(group == null) return null;
		QueryHelper queryHelper = new QueryHelper();
		queryHelper.addWhere("multi_group_id", group.getId()).addWhere("product_id", product.getId()).addWhere("specification_value", specvalue).build();
		MultiGroupSet multiGroupSet=multiGroupSetdao.findFirst("select * from " + MultiGroupSet.table + queryHelper.getWhere(), queryHelper.getParams());
		return multiGroupSet == null ? null : multiGroupSet.getCollagePrice();
	}

	@Override
	public MultiGroup getProductMultiGroup(Product product) throws UmpException {
		List<MultiGroup> groups = multiGroupdao.find("select * from " + MultiGroup.table + 
				" where seller_id=? and active=1 and start_date<=? and end_date>=?", product.getSellerId(), new Date(), new Date());
		
		for(MultiGroup group : groups){
			List<MultiGroupSet> groupSets = multiGroupSetdao.find(" select * from " + MultiGroupSet.table + " where multi_group_id=? ", group.getId());
			for(MultiGroupSet groupSet : groupSets){
				if(groupSet.getProductId() == product.getId())
					return group;
			}
		}
		
		return null;
	}

	@Override
	public GroupingResultDto getGroupUserInfos(Product product, Long gheaderUserId, Long buyerId) throws UmpException {
		if(product == null || gheaderUserId == null) throw new UmpException("获取拼团成员信息缺少必要参数");
		
		BuyerUser buyer = buyerUserdao.findById(gheaderUserId);
		if(buyer == null) throw new UmpException("获取拼团成员信息失败，团长不存在");
		
		//当前商品进行中的拼团活动
		MultiGroup multiGroup = getProductMultiGroup(product);
		if(multiGroup == null) throw new UmpException("拼团活动已过期");
		
		//获取当前拼团订单
		//检查发起者正在组团的订单
		OrderGheader gHeader = orderGheaderdao.findFirst("select * from " + OrderGheader.table + 
				" where buyer_id=? and product_id= ? and group_id=? and active=1 ", gheaderUserId, product.getId(), multiGroup.getId());
		if(gHeader == null) return null;
		
		GroupingResultDto groupingDto = new GroupingResultDto();
		groupingDto.setGroupHeader(buyer.getNickname().length()>4?buyer.getNickname().substring(0, 4).concat("..."):buyer.getNickname());
		groupingDto.setGroupHeaderId(gheaderUserId);
		//检查拼团活动的时效
		if(multiGroup.getValidTime() != null){
			Integer expiresIn = multiGroup.getValidTime() * 3600; //转换成秒
			Long expiredTime = gHeader.getCreated().getTime() + ((expiresIn -5) * 1000);
			if(expiredTime < System.currentTimeMillis())
				groupingDto.setIsExpires(true);
			Calendar ca=Calendar.getInstance();
			ca.setTime(gHeader.getCreated());
			ca.add(Calendar.HOUR_OF_DAY, multiGroup.getValidTime());
			groupingDto.setExpiresIn((ca.getTime().getTime() - new Date().getTime())/1000);
		}
		
		List<Record> orderGusers = Db.find("select bu.id, bu.nickname, bu.headimgurl, og.quantity from " 
				+ OrderGuser.table + " og "
				+ " left join " + BuyerUser.table + " bu on og.buyer_id = bu.id "
				+ " where og.gheader_id= ? order by og.created asc ", gHeader.getId());
		
		List<GrouponUserResultDto> groupUserDtos = new ArrayList<GrouponUserResultDto>();
		for(Record record : orderGusers){
			GrouponUserResultDto groupUser = new GrouponUserResultDto();
			groupUser.setId(record.getLong("id"));
			groupUser.setNickName(record.getStr("nickname").length()>4?record.getStr("nickname").substring(0, 4).concat("..."):record.getStr("nickname"));
			groupUser.setHeadimg(record.getStr("headimgurl"));
			groupUserDtos.add(groupUser);
			if(groupUser.getId() == buyerId)
				groupingDto.setIsGrouped(true);
		}
		groupingDto.setGroupUsers(groupUserDtos);
		groupingDto.setDiffUserCount(multiGroup.getOfferNum() - groupUserDtos.size());
		return groupingDto;
	}

	@Override
	public List<GroupingResultDto> getGroupsByProduct(Product product) throws UmpException {
		if(product == null) throw new UmpException("获取商品拼团列表缺少必要参数");
		//当前商品进行中的拼团活动
		MultiGroup multiGroup = getProductMultiGroup(product);
		if(multiGroup == null) return null;

		List<Record> gHeaders = Db.find("select ogh.*, bu.*, ogh.id as ogh_id, ogh.created as ogh_created from "
				+ OrderGheader.table + " ogh " 
				+ " left join " + BuyerUser.table + " bu on ogh.buyer_id=bu.id " 
				+ " where ogh.group_id=? and ogh.product_id=? and ogh.active=1 ", multiGroup.getId(), product.getId());
		if(gHeaders == null) return null;

		List<GroupingResultDto> groupings = new ArrayList<GroupingResultDto>();
		
		for(Record rcd : gHeaders){
			GroupingResultDto groupingDto = new GroupingResultDto();
			groupings.add(groupingDto);
			groupingDto.setGroupHeader(rcd.getStr("nickname").length()>4?rcd.getStr("nickname").substring(0, 4).concat("..."):rcd.getStr("nickname"));
			groupingDto.setGroupHeaderImg(rcd.getStr("headimgurl"));
			groupingDto.setGroupHeaderId(rcd.getLong("buyer_id"));
			//检查拼团活动的时效
			if(multiGroup.getValidTime() != null){
				Integer expiresIn = multiGroup.getValidTime() * 3600; //转换成秒
				Long expiredTime = rcd.getDate("ogh_created").getTime() + ((expiresIn -5) * 1000);
				if(expiredTime < System.currentTimeMillis())
					groupingDto.setIsExpires(true);
				
				Calendar ca=Calendar.getInstance();
				ca.setTime(rcd.getDate("ogh_created"));
				ca.add(Calendar.HOUR_OF_DAY, multiGroup.getValidTime());
				groupingDto.setExpiresIn((ca.getTime().getTime() - new Date().getTime())/1000);
			}
			Long groupUserCont = Db.queryLong("select count(id) from " + OrderGuser.table + " where gheader_id=? ", rcd.getLong("ogh_id"));
			groupingDto.setDiffUserCount(multiGroup.getOfferNum() - groupUserCont.intValue());
		}
		
		return groupings;
	}

}
