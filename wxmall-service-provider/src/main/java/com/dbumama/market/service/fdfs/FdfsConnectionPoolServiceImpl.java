package com.dbumama.market.service.fdfs;

import javax.annotation.PostConstruct;

import org.csource.fastdfs.ClientGlobal;
import org.springframework.stereotype.Service;

@Service("fdfsConnectionPoolService")
public class FdfsConnectionPoolServiceImpl implements FdfsConnectionPoolService{
	
	public FdfsConnectionPoolServiceImpl(){
		super();
	}
	//初始化参数
	@PostConstruct
	public void init() throws Exception {
		ClientGlobal.init(Thread.currentThread().getContextClassLoader().getResourceAsStream("fdfs_client.properties"));
	}
	//获取链接
	public FdfsManagerConnection getFdfsConnection() throws Exception{
		FdfsManagerConnectionManager manager = FdfsManagerConnectionManager.getInstance();
		FdfsManagerConnection conn = manager.getFdfsManagerConnection();
		return conn;
	}
	
	//置为空闲
	public void releaseFdfsConnection(FdfsManagerConnection conn){
		FdfsManagerConnectionManager manager = FdfsManagerConnectionManager.getInstance();
		manager.releaseManagerFdfsConnection(conn);
	}
}
