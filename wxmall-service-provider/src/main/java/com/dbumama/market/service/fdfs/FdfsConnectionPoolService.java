package com.dbumama.market.service.fdfs;

public interface FdfsConnectionPoolService {
	/**
	 * 获取与TrackerServer的链接
	 * @return
	 * @throws Exception
	 */
	public FdfsManagerConnection getFdfsConnection() throws Exception;
	/**
	 * 释放与TrackerServer的链接
	 * @param conn
	 */
	public void releaseFdfsConnection(FdfsManagerConnection conn);
}
