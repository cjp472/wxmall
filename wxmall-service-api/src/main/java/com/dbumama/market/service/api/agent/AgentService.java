package com.dbumama.market.service.api.agent;

import java.math.BigDecimal;
import java.util.List;

import com.dbumama.market.model.Agent;
import com.dbumama.market.model.AgentAduitLog;
import com.dbumama.market.model.AgentCommssion;
import com.dbumama.market.model.AgentRank;
import com.dbumama.market.model.Order;
import com.jfinal.plugin.activerecord.Page;

/**
 * 分销代理服务
 * @author yangzy
 *
 */
public interface AgentService {

	/**
	 * 获取代理的自身以及所有父对象
	 * @param agentId
	 * @return
	 */
	public List<Agent> getSelfAndParents(Long agentId);
	
	/**
	 * 查询审核通过的分销商列表
	 * @param agentParam
	 * @return
	 * @throws AgentException
	 */
	Page<AgentResultDto> list(AgentParamDto agentParam) throws AgentException;

	/**
	 * 申请成为分销商代理
	 * @param applyParam
	 * @throws AgentException
	 */
	public void apply(AgentApplyParamDto applyParam) throws AgentException;
	
	/**
	 * 获取待审核分销商列表
	 * @param aduitParam
	 * @return
	 * @throws AgentException
	 */
	public Page<AgentAduitResultDto> getAduitList(AgentAduitParamDto aduitParam) throws AgentException;
	
	/**
	 * 通过分销商审核
	 * @param agentId
	 * @param opterId 操作人
	 * @throws AgentException
	 */
	public void pass(Long agentId, Long opterId) throws AgentException;
	
	/**
	 * 不通过审核
	 * @param agentId
	 * @param opterId 操作人
	 * @param content 不通过审核的理由
	 * @throws AgentException
	 */
	public void nopass(Long agentId, Long opterId, String content) throws AgentException;
	
	/**
	 * 取消代理商资格，取消后不能再获取佣金
	 * @param agentId
	 * @throws AgentException
	 */
	public void cancel(Long agentId) throws AgentException;
	
	/**
	 * 获取代理商佣金
	 * @param agent
	 * @return
	 * @throws AgentException
	 */
	public BigDecimal getAgentCommission(Agent agent, Order order) throws AgentException;
	
	/**
	 * 订单支付后，设置分销商佣金
	 * @param agent
	 * @param order
	 * @throws AgentException
	 */
	public void setAgentCommission(Agent agent, Order order) throws AgentException;
	
	/**
	 * 分销商等级列表
	 * @param rankParam
	 * @return
	 * @throws AgentException
	 */
	public Page<AgentRankResultDto> getRanktList(AgentRankParamDto rankParam) throws AgentException;
	
	/**
	 * 获取代理等级，不是指分销商等级
	 * @param agent
	 * @return 1 一级代理，2 二级代理，3 三级代理
	 * @throws AgentException
	 */
	public Integer getAgentGrade(Agent agent) throws AgentException;
	
	/**
	 * 分销商微信端提现
	 * @param agent
	 * @throws AgentException
	 */
	public void getCash(Long buyerId) throws AgentException;
	
	/**
	 * 获取分销商当月推广人数，直属下级用户数
	 * @param agent
	 * @return
	 * @throws AgentException
	 */
	public Integer getChildrenByMonth(Agent agent) throws AgentException;
	
	/**
	 * 获取分销商当月获取的佣金
	 * @param agent
	 * @return
	 * @throws AgentException
	 */
	public BigDecimal getCommossionByMonth(Agent agent) throws AgentException;
	
	/**
	 * 根据分销商当月分销情况，实时获取分销商等级
	 * @param agent
	 * @return
	 * @throws AgentException
	 */
	public AgentRank getAgentRank(Agent agent) throws AgentException;
	
	/**
	 * 获取买家的代理
	 * @param buyerId
	 * @return
	 */
	public Agent getBuyerAgent(Long buyerId);
	
	public Agent getAgentById(Long id);
	
	public void deleteById(Long id);
	
	public List<AgentAduitLog> getAgentAduitLogs(Long agentId);
	
	public AgentCommssion getAgentCommssion(Long agentId);
	
	public AgentRank getAgentRankById(Long rankId);
	
}
