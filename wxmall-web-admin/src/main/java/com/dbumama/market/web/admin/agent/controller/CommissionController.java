package com.dbumama.market.web.admin.agent.controller;

import com.dbumama.market.model.CommissionRate;
import com.dbumama.market.service.api.agent.CommissionService;
import com.dbumama.market.web.core.annotation.RouteBind;
import com.dbumama.market.web.core.controller.AdminBaseController;
import com.dbumama.market.web.core.plugin.spring.Inject.BY_NAME;

/**
 * 佣金
 * @author yangzy
 *
 */
@RouteBind(path="commission")
public class CommissionController extends AdminBaseController<CommissionRate>{

	@BY_NAME
	private CommissionService commissionService;
	
	/**
	 * 各分销商佣金
	 */
	public void index(){
		render("/agent/commission_index.html");
	}
	
	/**
	 * 各分销商佣金列表
	 */
	public void list (){
		rendSuccessJson(commissionService.list(getSellerId(), getPageNo(), getPageSize()));
	}
	
	/**
	 * 佣金占比设置
	 */
	public void rate(){
		CommissionRate rate = commissionService.getSellerUserRate(getSellerId());
		if(rate != null) setAttr("rate", rate);
		render("/agent/commission_rate_set.html");
	}
	
	/**
	 * 保存佣金比率设置
	 */
	public void saveRate(){
		try {
			CommissionRate rate = getModel();
			commissionService.saveRate(rate, getSellerId());
			rendSuccessJson();
		} catch (Exception e) {
			rendFailedJson(e.getMessage());
		}
	}
	
	@Override
	protected Class<CommissionRate> getModelClass() {
		return CommissionRate.class;
	}

}
