package com.dbumama.market.service.provider;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.dbumama.market.model.BuyerUser;
import com.dbumama.market.service.api.customer.CustomerException;
import com.dbumama.market.service.api.customer.CustomerParamDto;
import com.dbumama.market.service.api.customer.CustomerService;
import com.dbumama.market.service.api.customer.MemberResultDto;
import com.dbumama.market.service.sql.QueryHelper;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

@Service("customerService")
public class CustomerServiceImpl implements CustomerService{

	private static final BuyerUser buyerUserdao = new BuyerUser().dao();
	
	@Override
	public Page<BuyerUser> list(CustomerParamDto customerParam) throws CustomerException {
		if(customerParam == null) 
			throw new CustomerException("获取客户列表缺少必要参数");
		
		QueryHelper helper = new QueryHelper("select * ", " from " + BuyerUser.table);
		helper.addWhereLike("nickname", customerParam.getNickName())
		.addWhere("active", customerParam.getActive())
		.addOrderBy("desc", "created").build();
		
		Page<BuyerUser> buyerUser = buyerUserdao.paginate(customerParam.getPageNo(), customerParam.getPageSize(), 
				helper.getSelect(), 
				helper.getSqlExceptSelect(),
				helper.getParams());
		
		return buyerUser;
	}

	@Override
	public Page<MemberResultDto> listMembers(CustomerParamDto customerParam) throws CustomerException {
		if(customerParam == null) throw new CustomerException("获取客户列表缺少必要参数");
		
		List<Record> records = Db.find("select buyer_id from t_buyer_card where `status` = 1 GROUP BY buyer_id");
		List<Long> ids = new ArrayList<Long>();
		for(Record r : records){
			ids.add(r.getLong("buyer_id"));
		}
		if(ids.size() <= 0)
			return new Page<MemberResultDto>(new ArrayList<MemberResultDto>(), customerParam.getPageNo(), customerParam.getPageSize(), 1, 0);
		
		QueryHelper helper = new QueryHelper("select * ", " from " + BuyerUser.table);
		helper.addWhereLike("nickname", customerParam.getNickName())
		.addWhere("active", customerParam.getActive())
		.addWhereIn("id", ids)
		.addOrderBy("desc", "created").build();
		
		Page<BuyerUser> buyerUsers = buyerUserdao.paginate(customerParam.getPageNo(), customerParam.getPageSize(), 
				helper.getSelect(), 
				helper.getSqlExceptSelect(),
				helper.getParams());
		
		List<MemberResultDto> members = new ArrayList<MemberResultDto>();
		for(BuyerUser by : buyerUsers.getList()){
			MemberResultDto mrd = new MemberResultDto();
			members.add(mrd);
			mrd.setNickName(by.getNickname());
			mrd.setHeadImg(by.getHeadimgurl());
			mrd.setSex(by.getSex());
			mrd.setLastLoginTime(by.getLastLoginTime());
			mrd.setActive(by.getActive());
			mrd.setProvince(by.getProvince());
		}
		return new Page<MemberResultDto>(members, buyerUsers.getPageNumber(), buyerUsers.getPageSize(), buyerUsers.getTotalPage(), buyerUsers.getTotalRow());
	}

}
