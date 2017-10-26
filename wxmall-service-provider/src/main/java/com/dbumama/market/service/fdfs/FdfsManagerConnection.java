package com.dbumama.market.service.fdfs;

import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.StorageClient1;
import org.csource.fastdfs.TrackerServer;

import java.io.IOException;

public class FdfsManagerConnection {
	private TrackerServer trackerServer;
	private boolean isUse = false;

	public FdfsManagerConnection(){
		super();
	}
	
	public StorageClient getClient() {
		StorageClient client = new StorageClient1(this.trackerServer,null); 
		return  client;
	}
	
	public void closeConnection() throws IOException{
		this.trackerServer.close();
		this.trackerServer = null;
	}

	public TrackerServer getTrackerServer() {
		return trackerServer;
	}

	public void setTrackerServer(TrackerServer trackerServer) {
		this.trackerServer = trackerServer;
	}
	
	public synchronized boolean isUse() {
		return isUse;
	}

	public synchronized void setUse(boolean isUse) {
		this.isUse = isUse;
	}
	
}
