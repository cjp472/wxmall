package com.weixin.sdk.pay;

import java.io.Serializable;
import java.util.TreeMap;

/**
 * 33732992@qq.com
 * 2015年12月11日
 */
public interface BaseReq extends Serializable{
	public TreeMap<String,Object> toMap();
}
