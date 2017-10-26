package com.dbumama.market.service.provider;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.dbumama.market.model.CommissionRate;
import com.dbumama.market.service.api.agent.AgentCommissionResultDto;
import com.dbumama.market.service.api.agent.AgentException;
import com.dbumama.market.service.api.agent.CommissionService;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

@Service("commissionService")
public class CommissionServiceImpl implements CommissionService{

	private static final CommissionRate cmsRateDao = new CommissionRate().dao(); 
	
	@Override
	public Page<AgentCommissionResultDto> list(Long sellerId, Integer pageNo, Integer pageSize) {
		final String select = "select ac.total_commission, a.id, bu.nickname, bu.headimgurl, a.agent_name, a.agent_phone, tac.commssion_value, tacc.total_cash ";
		String sqlExceptSelect = "from "
		+ " (select SUM(commission_value) as total_commission, agent_id from t_agent_comm_rcd group BY agent_id) ac "
		+ " left join (select SUM(cash_value) as total_cash, agent_id from t_agent_cash_rcd group BY agent_id) tacc on ac.agent_id = tacc.agent_id "		
		+ " LEFT JOIN t_agent a on ac.agent_id = a.id "
		+ " LEFT JOIN t_buyer_user bu on a.buyer_id = bu.id " 
		+ " left join t_agent_commssion tac on tac.agent_id=ac.agent_id "
		+ " where a.seller_id = ? ";
		
		Page<Record> records = Db.paginate(pageNo, pageSize, select, sqlExceptSelect, sellerId);
		
		List<AgentCommissionResultDto> resultDtos = new ArrayList<AgentCommissionResultDto>();
		for(Record r : records.getList()){
			AgentCommissionResultDto resultDto = new AgentCommissionResultDto();
			resultDto.setAgentName(r.getStr("agent_name"));
			resultDto.setAgentPhone(r.getStr("agent_phone"));
			resultDto.setNickName(r.getStr("nickname"));
			resultDto.setNickHeader(r.getStr("headimgurl"));
			resultDto.setTotalCommission(r.getBigDecimal("total_commission"));
			if(r.getBigDecimal("commssion_value") != null){
				resultDto.setCommissionValue(r.getBigDecimal("commssion_value"));
			}
			if(r.getBigDecimal("total_cash") != null){
				resultDto.setCashValue(r.getBigDecimal("total_cash"));
			}
			resultDtos.add(resultDto);
		}
		return new Page<AgentCommissionResultDto>(resultDtos, records.getPageNumber(), records.getPageSize(), records.getTotalPage(), records.getTotalRow());
	}

	@Override
	public void saveRate(CommissionRate rate, Long sellerId) throws AgentException {
		if(rate == null || sellerId == null 
				|| rate.getSelfUpRate() == null || rate.getSecondUpRate() == null || rate.getThirdUpRate() == null)
			throw new AgentException("佣金设置缺少必要参数");
		
		BigDecimal total = rate.getSelfUpRate().add(rate.getSecondUpRate()).add(rate.getThirdUpRate()).setScale(2, BigDecimal.ROUND_HALF_UP);
		if(total.intValue() > 50){
			throw new AgentException("总比率不能超过订单金额的50%");
		}
		if(rate.getId() == null){
			rate.setSellerId(sellerId);
			rate.setActive(true);
			rate.save();
		}else{
			rate.update();
		}
	}

	@Override
	public CommissionRate getSellerUserRate(Long sellerId) {
		return cmsRateDao.findFirst("select * from " + CommissionRate.table + " where seller_id=?", sellerId);
	}

}
