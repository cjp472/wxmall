package com.dbumama.market.service.api.prize;

import com.dbumama.market.model.Prize;
import com.dbumama.market.service.api.common.AbstractResultDto;

/**
 * 33732992@qq.com
 * 2016年7月20日
 */
@SuppressWarnings("serial")
public class PrizeSendResultDto extends AbstractResultDto{

	private Prize prize;
	
	public Prize getPrize() {
		return prize;
	}
	public void setPrize(Prize prize) {
		this.prize = prize;
	}
	
}
