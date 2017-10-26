package com.dbumama.market.service.fdfs;

import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;

import java.io.IOException;
import java.util.Vector;

public class FdfsManagerConnectionManager {

	private Vector<FdfsManagerConnection> fdfsManagerConnections = new Vector<FdfsManagerConnection>(10);
	public int maxCons = 10;
	
	private static FdfsManagerConnectionManager manager = null;

	private FdfsManagerConnectionManager() {
		//
	}

	public synchronized static FdfsManagerConnectionManager getInstance() {
		if(manager == null){
			manager = 	new FdfsManagerConnectionManager();
		}
		return manager;
	}

	public FdfsManagerConnection establishConnection() throws Exception {
		FdfsManagerConnection fm = new FdfsManagerConnection();
		fm.setUse(true);
		TrackerServer ts = new TrackerClient().getConnection();
		fm.setTrackerServer(ts);
		fdfsManagerConnections.add(fm);
		return fm;
	}

	
	public synchronized FdfsManagerConnection getFdfsManagerConnection()
			throws Exception {
		while (true) {
			for (int i = 0; i < fdfsManagerConnections.size(); i++) {
				FdfsManagerConnection fm = (FdfsManagerConnection) fdfsManagerConnections.elementAt(i);
				if (!fm.isUse()) {
					synchronized (this) {
						fm.setUse(true);
						return fm;
					}
				}
			}
			if (fdfsManagerConnections.size() < this.maxCons) {
				return establishConnection();
			}
		}
	}
	//置为空闲
	public void releaseManagerFdfsConnection(FdfsManagerConnection fmConnection){
		fmConnection.setUse(false);
	}
	// 删除坏链接
	public synchronized void removeFdfsManagerConnection(FdfsManagerConnection fmConnection) throws IOException {
		fmConnection.closeConnection();
		fdfsManagerConnections.removeElement(fmConnection);
	}

	public int getMaxCons() {
		return maxCons;
	}

	public void setMaxCons(int maxCons) {
		this.maxCons = maxCons;
	}
	
	
}
