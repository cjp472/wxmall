package com.dbumama.market.service.api.fdfs;

import java.awt.*;
import java.io.File;
import java.io.OutputStream;

/**
 * 
 * @author yangzy
 *
 */
public interface FileToolsService {

	public String uploadAttachFile(File file) throws Exception;
	
	public String uploadCompressFile(File file) throws Exception;
	
//	public String uploadAttachFile(MultipartFile uploadFile) throws Exception;
	
//	public String uploadAttachFile(File file, Rectangle rectangle) throws Exception;
	
	public String uploadFile(byte[] fileBuff, String fileName, long fileSize, Rectangle rectangle) throws Exception;
	
	public String uploadFile(String master_filename, String prefix_name, byte[] fileBuff, String fileName, long fileSize, Rectangle rectangle) throws Exception;
	
	public void downloadFile(String fastUrl, OutputStream outStream, String thumnailSize) throws Exception;
	
	public byte[] downloadFile(String fastUrl, String thumnailSize) throws Exception;
	
	public void deleteFile(String url) throws Exception;
	
}
