package com.dbumama.market.service.provider;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dbumama.market.model.Agent;
import com.dbumama.market.model.AgentAduitLog;
import com.dbumama.market.model.AgentCashRcd;
import com.dbumama.market.model.AgentCommRcd;
import com.dbumama.market.model.AgentCommssion;
import com.dbumama.market.model.AgentRank;
import com.dbumama.market.model.Area;
import com.dbumama.market.model.AuthUserConfig;
import com.dbumama.market.model.BuyerUser;
import com.dbumama.market.model.CommissionRate;
import com.dbumama.market.model.Order;
import com.dbumama.market.model.UserCode;
import com.dbumama.market.service.api.agent.AgentAduitParamDto;
import com.dbumama.market.service.api.agent.AgentAduitResultDto;
import com.dbumama.market.service.api.agent.AgentApplyParamDto;
import com.dbumama.market.service.api.agent.AgentException;
import com.dbumama.market.service.api.agent.AgentParamDto;
import com.dbumama.market.service.api.agent.AgentRankParamDto;
import com.dbumama.market.service.api.agent.AgentRankResultDto;
import com.dbumama.market.service.api.agent.AgentResultDto;
import com.dbumama.market.service.api.agent.AgentService;
import com.dbumama.market.service.api.ump.UmpException;
import com.dbumama.market.service.enmu.AgentStatus;
import com.dbumama.market.service.utils.DateTimeUtil;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.weixin.sdk.pay.SendredpackApi;
import com.weixin.sdk.pay.SendredpackReqData;
import com.weixin.sdk.pay.SendredpackResData;

import cn.dreampie.encription.EncriptionKit;

@Service("agentService")
public class AgentServiceImpl implements AgentService{
	
	private static final Agent agentDao = new Agent().dao();
	private static final AgentAduitLog aduitAduitDao = new AgentAduitLog().dao();
	private static final AgentCommssion agentCommssionDao = new AgentCommssion().dao();
	private static final AgentRank agentRankDao = new AgentRank().dao();
	private static final Area areaDao = new Area().dao();
	private static final AuthUserConfig authConfigDao = new AuthUserConfig().dao();
	private static final BuyerUser buyerUserdao = new BuyerUser().dao();
	private static final CommissionRate cmsRatedao = new CommissionRate().dao();
	private static final UserCode usercodedao = new UserCode().dao();
	
	@Override
	public Page<AgentResultDto> list(AgentParamDto agentParam) throws AgentException {
		if(agentParam == null)
			throw new AgentException("调用分销商列表接口缺少必要参数");
		final StringBuffer where = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		if(StrKit.notBlank(agentParam.getAgentName())){
			where.append(" and a.agent_name like ? ");
			params.add("%"+agentParam.getAgentName()+"%");
		}
		
		if(StrKit.notBlank(agentParam.getAgentPhone())){
			where.append(" and a.agent_phone = ? ");
			params.add(agentParam.getAgentPhone());
		}
		
		if(agentParam.getParentId() != null){
			where.append(" and a.parent_id = ? ");
			params.add(agentParam.getParentId());
		}else {
			//默认查询出一级分销商
			where.append(" and a.parent_id is null ");
		}

		final String select = "select bu.headimgurl, bu.nickname, a.id, a.agent_name, a.status as a_status, a.agent_phone, a.created, a.audit_date, q.member_count, b.total_price ";
		String sqlExceptSelect = "from t_agent a "
		+ " LEFT JOIN t_buyer_user bu on a.buyer_id = bu.id "
		+ " LEFT JOIN (select count(parent_id) as member_count, parent_id as pid from t_agent GROUP BY parent_id) q on a.id = q.pid "
		+ " LEFT JOIN (select SUM(total_price) as total_price, buyer_id from t_order where payment_status=2) b on a.buyer_id = b.buyer_id "
		+ " where (a.status = 1 or a.status=3) and bu.active=1 "; 
		
		Page<Record> records = Db.paginate(agentParam.getPageNo(), agentParam.getPageSize(), select, sqlExceptSelect + where.toString(), params.toArray());
		List<AgentResultDto> agentResultDtos = new ArrayList<AgentResultDto>();
		for(Record record : records.getList()){
			AgentResultDto resultDto = new AgentResultDto();
			resultDto.setAgentId(record.getLong("id"));
			resultDto.setAgentName(record.getStr("agent_name"));
			resultDto.setAgentPhone(record.getStr("agent_phone"));
			resultDto.setWxNick(record.getStr("nickname"));
			resultDto.setWxHeadimg(record.getStr("headimgurl"));
			resultDto.setAduitDate(record.getDate("audit_date"));
			resultDto.setCreated(record.getDate("created"));
			resultDto.setStatus(record.getInt("a_status"));
			if(record.getLong("member_count") != null){
				resultDto.setMemberCount(record.getLong("member_count").toString());
			}
			if(record.getBigDecimal("total_price") != null){
				resultDto.setTotalMoney(record.getBigDecimal("total_price").toString());
			}
			agentResultDtos.add(resultDto);
		}
		return new Page<AgentResultDto>(agentResultDtos, records.getPageNumber(), records.getPageSize(), records.getTotalPage(), records.getTotalRow());
	}
	
	@Override
	public void apply(AgentApplyParamDto applyParam) throws AgentException {
		if(applyParam == null || applyParam.getBuyerId() == null 
				|| StrKit.isBlank(applyParam.getCode()) || StrKit.isBlank(applyParam.getCodeInSession()) || StrKit.isBlank(applyParam.getPhoneCode())
				|| applyParam.getSellerId() == null || StrKit.isBlank(applyParam.getAgentPhone()))
			throw new AgentException("调用申请分销商接口缺少必要参数");
		
		//检查图片验证码
		if(StrKit.notBlank(applyParam.getCode()) 
				&& StrKit.notBlank(applyParam.getCodeInSession())
				&& !EncriptionKit.encrypt(applyParam.getCode().toLowerCase()).equals(applyParam.getCodeInSession())) 
			throw new AgentException("验证码错误");
		
		//check 手机验证码
		UserCode userCode = usercodedao.findFirst("select * from " + UserCode.table + " where vcode_phone=? and vcode_code=? ", 
				applyParam.getAgentPhone(), applyParam.getPhoneCode());
		if(userCode == null) throw new AgentException("短信验证码错误");
		//检查验证码是否过期 30分钟后过期
		Integer expires_in = 1800;
		Long expiredTime = userCode.getUpdated().getTime() + ((expires_in -5) * 1000);
		if(expiredTime == null || expiredTime < System.currentTimeMillis())
			throw new AgentException("短信验证码已过期");
		
		Agent agent = agentDao.findFirst(" select * from " + Agent.table + " where agent_phone=? ", applyParam.getAgentPhone());
		if(agent != null) throw new AgentException("该手机号已存在");
		
		//查询地址
		Area area = areaDao.findById(applyParam.getAreaId());
		if(area == null) throw new AgentException("地址库不存在该地址");
		
		//插入到分销商表
		agent = new Agent();
		agent.setAgentName(applyParam.getAgentName());
		agent.setAgentPhone(applyParam.getAgentPhone());
		agent.setBuyerId(applyParam.getBuyerId());
		agent.setSellerId(applyParam.getSellerId());
		agent.setAgentAddr(applyParam.getAddr());
		agent.setAreaId(applyParam.getAreaId());
		agent.setAreaTreePath(area.getTreePath());
		agent.setParentId(applyParam.getParentId());
		if(agent.getParentId() == null)
			agent.setStatus(2); 	//2表示待审核，0表示审核不通过，1表示审核通过，成为分销商
		else
			agent.setStatus(1);  	//如果有上级代理的话，表示是主动邀请成为下级，无需审核
		agent.setActive(true);
		agent.setCreated(new Date());
		agent.setUpdated(new Date());
		try {
			agent.save();			
		} catch (Exception e) {
			throw new AgentException(e.getMessage());
		}
	}

	@Override
	public Page<AgentAduitResultDto> getAduitList(AgentAduitParamDto aduitParam) throws AgentException {
		if(aduitParam == null || aduitParam.getStatus() == null)
			throw new AgentException("获取待审核分销商列表缺少参数");
		/*select bu.headimgurl, bu.nickname, a.id, a.agent_name, a.agent_phone, a.agent_addr, b.total_price, a.created, a.`status` from t_agent a 
		LEFT JOIN t_buyer_user bu on a.buyer_id = bu.id
		LEFT JOIN (select SUM(total_price) as total_price, buyer_id from t_order where payment_status=2) b on a.buyer_id = b.buyer_id 
		where a.seller_id = 1*/
		final StringBuffer where = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		params.add(aduitParam.getStatus());
		if(StrKit.notBlank(aduitParam.getAgentName())){
			where.append(" and a.agent_name like ? ");
			params.add("%"+aduitParam.getAgentName()+"%");
		}
		
		if(StrKit.notBlank(aduitParam.getAgentPhone())){
			where.append(" and a.agent_phone = ? ");
			params.add(aduitParam.getAgentPhone());
		}
		
		if(StrKit.notBlank(aduitParam.getWxNick())){
			where.append(" and bu.nickname like ? ");
			params.add("%"+aduitParam.getWxNick()+"%");
		}
		
		final String select = "select bu.headimgurl, bu.nickname, a.id, a.agent_name, a.agent_phone, a.agent_addr, b.total_price, a.created, a.`status` ";
		String sqlExceptSelect = "from t_agent a LEFT JOIN t_buyer_user bu on a.buyer_id = bu.id "
		+ "LEFT JOIN (select SUM(total_price) as total_price, buyer_id from t_order where payment_status=2) b on a.buyer_id = b.buyer_id " 
		+ "where a.status =? ";
		
		Page<Record> records = Db.paginate(aduitParam.getPageNo(), aduitParam.getPageSize(), select, sqlExceptSelect + where.toString(), params.toArray());
		List<AgentAduitResultDto> resultDtos = new ArrayList<AgentAduitResultDto>();

		for(Record record : records.getList()){
			AgentAduitResultDto resultDto = new AgentAduitResultDto();
			resultDtos.add(resultDto);
			resultDto.setWxNick(record.getStr("nickname"));
			resultDto.setWxHeadimg(record.getStr("headimgurl"));
			resultDto.setAgentId(record.getLong("id"));
			resultDto.setAgentName(record.getStr("agent_name"));
			resultDto.setAgentPhone(record.getStr("agent_phone"));
			resultDto.setAgentAddr(record.getStr("agent_addr"));
			if(record.getBigDecimal("total_price") != null){
				resultDto.setTotalMoney(record.getBigDecimal("total_price").toString());
			}
			resultDto.setApplyDate(record.getDate("created"));
			resultDto.setStatus(record.getInt("status"));
			if(record.getInt("status") == 0){
				resultDto.setStatusCn("不通过");
			}else if(record.getInt("status") == 2){
				resultDto.setStatusCn("待审核");
			}
			//获取审核日志
			List<AgentAduitLog> aduitLogs = aduitAduitDao.find(" select * from " + AgentAduitLog.table + " where agent_id=? ", resultDto.getAgentId());
			resultDto.setAduitLogs(aduitLogs);
		}
		return new Page<AgentAduitResultDto>(resultDtos, records.getPageNumber(), records.getPageSize(), records.getTotalPage(), records.getTotalRow());
	}

	@Override
	@Transactional(rollbackFor = AgentException.class)
	public void pass(Long agentId, Long opterId) throws AgentException {
		if(agentId == null || opterId == null ) throw new AgentException("审核分销商缺少参数");
		Agent agent = agentDao.findById(agentId);
		if(agent == null || agent.getStatus() == 1) throw new AgentException("该分销商不存在或不能审核");
		
		agent.setStatus(1);//设置状态为审核通过
		agent.setAuditDate(new Date());
		agent.update();
		
		//插入审核日志
		AgentAduitLog aduitLog = new AgentAduitLog();
		aduitLog.setAgentId(agentId);
		aduitLog.setAduitOpter(opterId);
		aduitLog.setContent("");
		aduitLog.setStatus("通过");
		aduitLog.setActive(true);
		aduitLog.setCreated(new Date());
		aduitLog.setUpdated(new Date());
		
		try {
			aduitLog.save();	
		} catch (Exception e) {
			throw new AgentException(e.getMessage());
		}
		//发送审核通知
	}

	@Override
	@Transactional(rollbackFor = AgentException.class)
	public void nopass(Long agentId, Long opterId, String content) throws AgentException {
		if(agentId == null || opterId == null || StrKit.isBlank(content)) throw new AgentException("审核分销商缺少参数");
		
		Agent agent = agentDao.findById(agentId);
		if(agent == null || agent.getStatus() == 1) throw new AgentException("该分销商不存在或不能审核");
		
		agent.setStatus(0);//设置状态为审核不通过
		agent.setAuditDate(new Date());
		agent.update();
		
		//插入审核日志
		AgentAduitLog aduitLog = new AgentAduitLog();
		aduitLog.setAgentId(agentId);
		aduitLog.setAduitOpter(opterId);
		aduitLog.setContent(content);
		aduitLog.setStatus("不通过");
		aduitLog.setActive(true);
		aduitLog.setCreated(new Date());
		aduitLog.setUpdated(new Date());
		
		try {
			aduitLog.save();	
		} catch (Exception e) {
			throw new AgentException(e.getMessage());
		}
		//发送审核通知
	}
	
	@Override
	public void cancel(Long agentId) throws AgentException {
		if(agentId == null) throw new AgentException("取消分销商缺少必要参数");
		Agent agent = agentDao.findById(agentId);
		if(agent == null || agent.getStatus() != 1) throw new AgentException("该分销商不存在或无需取消");
		agent.setStatus(AgentStatus.cancel.ordinal());//设置状态为审核通过
		agent.setAuditDate(new Date());
		agent.update();
	}

	@Override
	public List<Agent> getSelfAndParents(Long agentId) {
		List<Agent> agents = getAgents(agentId, null);
		if(agents == null) return null;
		Collections.sort(agents, new Comparator<Agent>(){  
	        public int compare(Agent o1, Agent o2) {
	        	return o1.getId().intValue() - o2.getId().intValue();  
	        }  
	    });  
		return agents;
	}
	
	/**
	 * 获取所有父级分销商，包含自身
	 * @param agentId
	 * @param agents
	 * @return
	 */
	private List<Agent> getAgents (Long agentId, List<Agent> agents){
		Agent agent = agentDao.findById(agentId);
		if(agent == null ) return null;
		if(agents == null) agents = new ArrayList<Agent>();
		agents.add(agent);
		if(agent.getParentId() == null) return agents;
		return getAgents(agent.getParentId(), agents);
	}

	@Override
	public BigDecimal getAgentCommission(Agent agent, Order order) throws AgentException {
		if(agent == null || agent.getStatus()!=1) throw new AgentException("获取佣金失败,分销商账号不能获取佣金");
		Integer grade = getAgentGrade(agent);
		if(grade == null) throw new AgentException("获取分销商佣金出错，分销商级别为空");

		BigDecimal commisson = null;
		//检查是否有分销商等级
		AgentRank agentRank = getAgentRank(agent);
		if(agentRank !=null){
			BigDecimal agentMonthCommossion = getCommossionByMonth(agent);
			//排序处理，先比较佣金值是否达标，再比较下级用户数是否达标，从小比到大，如果有大的符合，按大的等级计算
			if(agentMonthCommossion != null){
				if(agentMonthCommossion.compareTo(agentRank.getTotalCommission()) ==1){
					if(grade == 1){//说明是一级代理
						commisson = agentRank.getFirstRate().divide(new BigDecimal(100)).multiply(order.getPayFee());
					}else if(grade ==2){
						commisson = agentRank.getSecondRate().divide(new BigDecimal(100)).multiply(order.getPayFee());
					}else if(grade == 3){
						commisson = agentRank.getThirdRate().divide(new BigDecimal(100)).multiply(order.getPayFee());
					}else {
						throw new AgentException("获取分销商佣金出错，只有三级分销商才能获取佣金");
					}
				}
			}
			if(commisson == null){
				//按下级用户数
				Integer children = getChildrenByMonth(agent);
				if(children != null){
					if(children > agentRank.getChildrenCount()){
						if(grade == 1){//说明是一级代理
							commisson = agentRank.getFirstRate().divide(new BigDecimal(100)).multiply(order.getPayFee());
						}else if(grade ==2){
							commisson = agentRank.getSecondRate().divide(new BigDecimal(100)).multiply(order.getPayFee());
						}else if(grade == 3){
							commisson = agentRank.getThirdRate().divide(new BigDecimal(100)).multiply(order.getPayFee());
						}else {
							throw new AgentException("获取分销商佣金出错，只有三级分销商才能获取佣金");
						}
					}
				}
			}
		}
		
		CommissionRate rate = cmsRatedao.findFirst("select * from " + CommissionRate.table + " where seller_id=?", agent.getSellerId());
		if(rate == null || rate.getSelfUpRate() == null || rate.getSecondUpRate() == null || rate.getThirdUpRate() == null)
			throw new AgentException("获取分销商佣金出错，未设置佣金比率");
		
		if(rate.getSelfBuyRate() == null || !rate.getSelfBuyRate()){
			//未开启分销商内购的话，分销商本身是拿不到佣金的
			if(agent.getBuyerId() == order.getBuyerId()){
				//说明是分销商自己下单，不能拿佣金
				throw new AgentException("未开启分销商内购，分销商本身下单，无法获取佣金");
			}
		}
		
		if(commisson == null){
			if(grade == 1){//说明是一级代理
				commisson = rate.getThirdUpRate().divide(new BigDecimal(100)).multiply(order.getPayFee());
			}else if(grade ==2){
				commisson = rate.getSecondUpRate().divide(new BigDecimal(100)).multiply(order.getPayFee());
			}else if(grade == 3){
				commisson = rate.getSelfUpRate().divide(new BigDecimal(100)).multiply(order.getPayFee());
			}else {
				throw new AgentException("获取分销商佣金出错，只有三级分销商才能获取佣金");
			}
		}
		
		return commisson != null ? commisson.setScale(2, BigDecimal.ROUND_HALF_UP) : null;
	}
	

	@Override
	public void setAgentCommission(Agent agent, Order order) throws AgentException {
		BigDecimal commission = getAgentCommission(agent, order);
		if(commission == null || commission.compareTo(new BigDecimal(0)) !=1 ) throw new AgentException("佣金比0小");
		//记录分销商获得的佣金
		AgentCommRcd agentCommRcd = new AgentCommRcd();
		agentCommRcd.setAgentId(agent.getId());
		agentCommRcd.setOrderId(order.getId());
		agentCommRcd.setCommissionValue(commission);
		agentCommRcd.setActive(false);
		agentCommRcd.setCreated(new Date());
		agentCommRcd.setUpdated(new Date());
		agentCommRcd.save();	
	}
	
	public Page<AgentRankResultDto> getRanktList(AgentRankParamDto rankParam) throws AgentException {
		final StringBuffer where = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		if(StrKit.notBlank(rankParam.getRankName())){
			where.append(" and a.rank_name like ? ");
			params.add("%"+rankParam.getRankName()+"%");
		}
		
		final String select = "select a.*";
		String sqlExceptSelect = "from "+AgentRank.table+" a"
		+ "  where active=1";
		
		Page<Record> records = Db.paginate(rankParam.getPageNo(), rankParam.getPageSize(), select, sqlExceptSelect + where.toString(), params.toArray());
		List<AgentRankResultDto> resultDtos = new ArrayList<AgentRankResultDto>();
		for(Record record : records.getList()){
			AgentRankResultDto resultDto=new AgentRankResultDto();
			resultDto.setId(record.getLong("id"));
			resultDto.setRankName(record.getStr("rank_name"));
			resultDto.setRankWeight(record.getInt("rank_weight"));
			resultDto.setFirstRate(record.getBigDecimal("first_rate")+"%");
			resultDto.setSecondRate(record.getBigDecimal("second_rate")+"%");
			resultDto.setThirdRate(record.getBigDecimal("third_rate")+"%");
			resultDto.setRewardValue(record.getInt("reward_value"));
			resultDtos.add(resultDto);
		}
		return new Page<AgentRankResultDto>(resultDtos, records.getPageNumber(), records.getPageSize(), records.getTotalPage(), records.getTotalRow());
	}

	@Override
	public Integer getAgentGrade(Agent agent) throws AgentException {
		List<Agent> agents = getSelfAndParents(agent.getId());		
		return agents == null ? null : agents.size();
	}

	@Override
	@Transactional(rollbackFor = AgentException.class)
	public void getCash(Long buyerId) throws AgentException {
		if(buyerId == null) throw new AgentException("分销商提现缺少必要参数");
		BuyerUser buyer = buyerUserdao.findById(buyerId);
		if(buyer == null || buyer.getActive() != 1 || buyer.getSubscribe() != 1) throw new AgentException("请关注公众号再提现");
		
		Agent agent = agentDao.findFirst("select * from " + Agent.table + " where buyer_id=? ", buyerId);
		if(agent == null || agent.getStatus() != AgentStatus.pass.ordinal()) throw new AgentException("分销商账号存在异常，不可体现");
		
		AgentCommssion commssion = agentCommssionDao.findFirst("select * from "+AgentCommssion.table+" where agent_id=? ", agent.getId());
		if(commssion == null) throw new AgentException("没有佣金可以提现");
		
		AuthUserConfig authConfig = authConfigDao.findFirst("select * from " + AuthUserConfig.table);
		if(authConfig == null || authConfig.getPayCertFile() == null) throw new AgentException("公众号异常，支付未开通，提现失败");
		
		//此处锁住mysql行数据
		AgentCommssion agentCommssion = agentCommssionDao.findFirst(" select * from " + AgentCommssion.table + " where id = ? for update ", commssion.getId());
		if(agentCommssion == null || agentCommssion.getCommssionValue()==null) throw new AgentException("没有佣金可以提现");
		if(agentCommssion.getCommssionValue().compareTo(new BigDecimal(100)) !=1) throw new AgentException("佣金必须大于100元才可以提现");
		
		//开始减佣金 每次提100元
		agentCommssion.setCommssionValue(agentCommssion.getCommssionValue().subtract(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP));
		agentCommssion.update();
		
		//发红包
		SendredpackReqData sendredpackReqData = new SendredpackReqData(authConfig.getPayMchId(),
				authConfig.getPaySecretKey(),
				authConfig.getPayMchId() + DateTimeUtil.getDateTime8String() + String.valueOf(System.currentTimeMillis()).substring(3, 13), 
				"", authConfig.getAppId(), "", 
				authConfig.getAppId(), buyer.getOpenId(), String.valueOf(1 * 100), new BigDecimal(100).multiply(new BigDecimal(100)).setScale(1, BigDecimal.ROUND_HALF_UP).toString(),
				agent.getAgentName() + "提现", "127.0.0.1", agent.getAgentName()+"提现，恭喜发财", authConfig.getAppId(), "");
		
		try {
			SendredpackApi sendredpackApi = new SendredpackApi();
			SendredpackResData sendredpackResData = (SendredpackResData) sendredpackApi.post(sendredpackReqData, authConfig.getPayCertFile());
			if(!"SUCCESS".equals(sendredpackResData.getResult_code())){
				//记录红包发送失败的错误记录
				throw new UmpException(sendredpackResData.getErr_code_des());
			}
			
			//记录红包发放记录
			AgentCashRcd agentCashRcd = new AgentCashRcd();
			agentCashRcd.setAgentId(agent.getId());
			agentCashRcd.setCashValue(new BigDecimal(100));
			agentCashRcd.setActive(true);
			agentCashRcd.setCreated(new Date());
			agentCashRcd.setUpdated(new Date());
			agentCashRcd.save();
		} catch (Exception e) {
			e.printStackTrace();
			//记录红包发送失败的错误记录
			throw new UmpException(e.getMessage());
		}
		
	}

	@Override
	public Integer getChildrenByMonth(Agent agent) throws AgentException {
		String sql = "select count(id) from " + Agent.table + " where parent_id=? "
				+ "  and date_format(created,'%Y-%m')=date_format(now(),'%Y-%m')";
		Long count = Db.queryLong(sql, agent.getId());
		return count == null ? 0 : count.intValue();
	}

	@Override
	public BigDecimal getCommossionByMonth(Agent agent) throws AgentException {
		String sql = "select sum(commission_value) from "+AgentCommRcd.table+" where agent_id = ?"
				+ " and date_format(created,'%Y-%m')=date_format(now(),'%Y-%m')";
		BigDecimal total = Db.queryBigDecimal(sql);
		return total == null ? new BigDecimal(0) : total;
	}

	@Override
	public AgentRank getAgentRank(Agent agent) throws AgentException {
		List<AgentRank> agentRanks = agentRankDao.find("select * from " + AgentRank.table + " where seller_id=? and active=1 ", agent.getSellerId());
		if(agentRanks == null || agentRanks.size()<=0) return null;
		AgentRank rank = null;
		BigDecimal agentMonthCommossion = getCommossionByMonth(agent);
		//排序处理，先比较佣金值是否达标，再比较下级用户数是否达标，从小比到大，如果有大的符合，按大的等级计算
		if(agentMonthCommossion != null){
			// 按等级佣金升序排序
			Collections.sort(agentRanks, new Comparator<AgentRank>(){
				@Override
				public int compare(AgentRank o1, AgentRank o2) {
					return o1.getTotalCommission().subtract(o2.getTotalCommission()).intValue();
				}
			});
			for(AgentRank agentRank : agentRanks){
				if(agentMonthCommossion.compareTo(agentRank.getTotalCommission()) ==1){
					rank = agentRank;
				}
			}
		}
		
		if(rank == null){
			//按下级用户数
			Integer children = getChildrenByMonth(agent);
			if(children != null){
				//按下级用户数升序排列
				Collections.sort(agentRanks, new Comparator<AgentRank>(){
					@Override
					public int compare(AgentRank o1, AgentRank o2) {
						return o1.getChildrenCount()-o2.getChildrenCount();
					}
				});
				for(AgentRank agentRank : agentRanks){
					if(children > agentRank.getChildrenCount()){
						rank = agentRank;
					}
				}
			}
		}
		return rank;
	}

	@Override
	public Agent getBuyerAgent(Long buyerId) {
		return agentDao.findFirst(" select * from " + Agent.table + " where buyer_id=? ", buyerId);
	}

	@Override
	public Agent getAgentById(Long id) {
		return agentDao.findById(id);
	}

	@Override
	public void deleteById(Long id) {
		agentDao.deleteById(id);
	}

	@Override
	public List<AgentAduitLog> getAgentAduitLogs(Long agentId) {
		return aduitAduitDao.find("select * from " + AgentAduitLog.table + " where agent_id=? order by created desc ", agentId);
	}

	@Override
	public AgentCommssion getAgentCommssion(Long agentId) {
		return agentCommssionDao.findFirst("select * from "+AgentCommssion.table+" where agent_id=? ", agentId);
	}

	@Override
	public AgentRank getAgentRankById(Long rankId) {
		return agentRankDao.findById(rankId);
	}

}
