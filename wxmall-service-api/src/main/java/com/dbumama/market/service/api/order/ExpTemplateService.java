package com.dbumama.market.service.api.order;

import java.util.List;
import java.util.Map;

import com.dbumama.market.model.ExpressComp;
import com.dbumama.market.model.ExpressImg;
import com.dbumama.market.model.ExpressTemplate;

/**
 * 快递单模板接口
 * @author yangzy
 *
 */
public interface ExpTemplateService {

	/**
	 * 获取用户支持的快递公司列表，排除用户已添加设置过的快递模板
	 * @param sellerId
	 * @return
	 * @throws OrderException
	 */
	List<ExpressComp> getUserExpComps (Long sellerId) throws OrderException;
	
	List<ExpressTemplate> getUserExpTemplate(Long sellerId);
	
	/**
	 * 根据快递公司，获取快递模块的背景图
	 * @param expKey
	 * @return
	 * @throws OrderException
	 */
	List<ExpressImg> getExpTemplateBackImage(String expKey) throws OrderException;
	
	/**
	 * 获取用户指定的快递模板
	 * @param key
	 * @param sellerId
	 * @return
	 * @throws OrderException
	 */
	ExpressTemplate getUserExpTemplateByKey(String key, Long sellerId) throws OrderException; 
	
	/**
	 * 保存用户添加的快递模板
	 * @param expTemplateDto
	 * @return
	 * @throws OrderException
	 */
	ExpressTemplate saveTemplate(ExpTplSaveParamDto expTemplateDto) throws OrderException;
	
	/**
	 * 删除用户的模板
	 * @param key
	 * @param sellerId
	 * @throws OrderException
	 */
	void delTemplate(String key, Long sellerId) throws OrderException;
	
	/**
	 * 初始化快递模板指标数据
	 * @return
	 */
	Map<String, List<PalletElementResultDto>> initPalletElement(Long sellerId);
	
}
