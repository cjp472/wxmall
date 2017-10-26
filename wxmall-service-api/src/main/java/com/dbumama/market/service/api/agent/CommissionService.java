package com.dbumama.market.service.api.agent;

import com.dbumama.market.model.CommissionRate;
import com.jfinal.plugin.activerecord.Page;

/**
 * 分销佣金
 * @author yangzy
 *
 */
public interface CommissionService {

	public Page<AgentCommissionResultDto> list (Long sellerId, Integer pageNo, Integer pageSize);
	
	public void saveRate(CommissionRate rate, Long sellerId) throws AgentException;
	
	CommissionRate getSellerUserRate(Long sellerId);
}
