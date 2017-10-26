package com.dbumama.market.service.fdfs;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageToolUtils {	
	

    //浏览器处理缩略图上限
    final public static int CONSTANT_MAX_SIZE = 128;
    //取图类型
    final public static String ATTACH_MASTER_NAME = "_master";//原图
    final public static String ATTACH_ZOOM_MIDDLE_NAME = "_middle";//中图
    final public static String ATTACH_ZOOM_SMALL_NAME = "_small";//小图
    //浏览器图像附件尺寸
    final public static String IMAGE_ZOOM_MIDDLE_SIZE = "128*128";//中图尺寸宽*高
    final public static String IMAGE_ZOOM_SMALL_SIZE = "128*128";//小图尺寸宽*高
    //头像三种尺寸
    final public static String FACE_ZOOM_MASTER_SIZE="108*108";
    final public static String FACE_ZOOM_MIDDLE_SIZE="54*54";
    final public static String FACE_ZOOM_SMALL_SIZE="32*32";
    
	public static byte[] getThumbnail(byte[] fileContent)   {
		return getThumbnail(fileContent, null ,true, null);
	} 


	/**
     * 获取缩略图
     * 如果选择等比缩放：尺寸上限为128
     * 如果高>宽，并且高>128，则按照高=128等比例缩小；
     * 如果高<宽，并且宽>128，则按照宽=128等比例缩小；
     * 如果高和宽都不超过128，则用原图。
     * 如果为非等比缩放，按传入尺寸处理  
     * @param fileContent  字节流
     * @param size              处理尺寸
     * @param proportion   是否等比缩放
     * @return
     */
	public static byte[] getThumbnail(byte[] fileContent, String size, boolean proportion, Rectangle rect)   {
		byte[] in2b = null;
	    int widthdist = 0;
	    int heightdist = 0;
	    
		try {    
			InputStream inStream = new ByteArrayInputStream(fileContent);
		    ImageInputStream iis = ImageIO.createImageInputStream(inStream);
		    BufferedImage src = null;
		    try{
		    	src = ImageIO.read(iis);
		    }catch(java.awt.color.CMMException ex){
		    }

		    //头像裁剪
		    if(rect != null){
	        	src = cutSubImage(src, rect);
	        }
	        if( proportion ){//是否等比缩放
		        int sw = src.getWidth();
		        int sh = src.getHeight();
		        double scale = (double)sw/sh;
		        if(sh >= sw && sh> CONSTANT_MAX_SIZE){
		        	heightdist = CONSTANT_MAX_SIZE;
		        	widthdist = (int) (heightdist * scale);
		        }else if(sh < sw && sw > CONSTANT_MAX_SIZE){
		        	widthdist = CONSTANT_MAX_SIZE;
		        	heightdist = (int) (widthdist/scale);
		        }else if(sh <= CONSTANT_MAX_SIZE && sw <= CONSTANT_MAX_SIZE){
		        	widthdist = sw;
		        	heightdist = sh;
		        }
	        }else{
	    	    String[] sizes = size.split("\\*");
	    	    widthdist = Integer.parseInt(sizes[0]);
	    	    heightdist = Integer.parseInt(sizes[1]);
	        }

	        BufferedImage tag= new BufferedImage(widthdist, heightdist,BufferedImage.TYPE_INT_RGB);
	        tag.getGraphics().drawImage(src.getScaledInstance(widthdist, heightdist, Image.SCALE_SMOOTH), 0, 0,  null);

            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
	        ImageIO.write(tag, "jpg", outStream);
//	        JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(outStream);
//	        JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(tag);
//	        param.setQuality(1f, true);
//
//	        encoder.encode(tag);
	        outStream.close();
	        in2b = outStream.toByteArray();
	    }catch (IOException ex) {
	        ex.printStackTrace();
	    }
        return in2b;
	}

	/**
	 * 	获取裁剪头像的缓存
	 * @param image
	 * @param subRect
	 * @return
	 * @throws java.io.IOException
	 */
	private static BufferedImage cutSubImage(BufferedImage image, Rectangle subRect)throws IOException{
		if(subRect == null){
			return image;
		}
	    BufferedImage subImage = new BufferedImage(subRect.width, subRect.height, 1);
	    Graphics g = subImage.getGraphics();
	    if ((subRect.width > image.getWidth()) || (subRect.height > image.getHeight())) {
	    	int left = subRect.x;
	    	int top = subRect.y;
	    	if (image.getWidth() < subRect.width){
	    		left = (subRect.width - image.getWidth()) / 2;
	    	}
	    	if (image.getHeight() < subRect.height){
	    		top = (subRect.height - image.getHeight()) / 2;
	    	}
	    	g.fillRect(0, 0, subRect.width, subRect.height);
	    	g.drawImage(image, left, top, null);
	    }else {
	    	g.drawImage(image.getSubimage(subRect.x, subRect.y, subRect.width, subRect.height), 0, 0, null);
	    }
	    g.dispose();
	    return subImage;
	}
	/**
	* @author shenhw
	* @version 1.0 2012-11-8
	* @param fileContent 二进行图片
	*        formatName  图片格式  "jpg"
	* @return byte[]
	* @功能描述 图片文件重绘
	* @修订历史：
	 */
	public static byte[] checkImage(byte[] fileContent,String formatName)  {
	    byte[] buf=new byte[0];
	    if(fileContent==null||fileContent.length<=0){
	    }
	    try{
    	    InputStream inStream = new ByteArrayInputStream(fileContent);
            ImageInputStream iis = ImageIO.createImageInputStream(inStream);
            BufferedImage src = null;
            try{
                src = ImageIO.read(iis);
            }catch(java.awt.color.CMMException ex){
            }
            ByteArrayOutputStream baos=new ByteArrayOutputStream(1024);
            ImageIO.write(src, formatName, baos);
            buf=baos.toByteArray();
            if(buf==null)
            return buf;
	    }catch (Exception ex) {   
        }
        return null;
	}
	
}
