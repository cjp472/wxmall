package com.dbumama.market.service.api.order;

import com.dbumama.market.service.api.common.AbstractParamDto;

@SuppressWarnings("serial")
public class ExpTplSaveParamDto extends AbstractParamDto{

	private String expkey;
	private String expname;
	private String expbgimg;
	private Long sellerId;
	private String tplcontent;
	private String designhtml;
	private Integer pagewidth;
	private Integer pageheight;
	private Integer offsetx;
	private Integer offsety;
	
	public ExpTplSaveParamDto(String expkey, String expname, String expbgimg, Long sellerId) {
		super();
		this.expkey = expkey;
		this.expname = expname;
		this.sellerId = sellerId;
		this.expbgimg = expbgimg;
	}
	
	public String getExpkey() {
		return expkey;
	}
	public void setExpkey(String expkey) {
		this.expkey = expkey;
	}
	public String getExpname() {
		return expname;
	}
	public void setExpname(String expname) {
		this.expname = expname;
	}
	public String getExpbgimg() {
		return expbgimg;
	}
	public void setExpbgimg(String expbgimg) {
		this.expbgimg = expbgimg;
	}
	public Long getSellerId() {
		return sellerId;
	}
	public void setSellerId(Long sellerId) {
		this.sellerId = sellerId;
	}
	public String getTplcontent() {
		return tplcontent;
	}
	public void setTplcontent(String tplcontent) {
		this.tplcontent = tplcontent;
	}
	public String getDesignhtml() {
		return designhtml;
	}
	public void setDesignhtml(String designhtml) {
		this.designhtml = designhtml;
	}
	public Integer getPagewidth() {
		return pagewidth;
	}
	public void setPagewidth(Integer pagewidth) {
		this.pagewidth = pagewidth;
	}
	public Integer getPageheight() {
		return pageheight;
	}
	public void setPageheight(Integer pageheight) {
		this.pageheight = pageheight;
	}
	public Integer getOffsetx() {
		return offsetx;
	}
	public void setOffsetx(Integer offsetx) {
		this.offsetx = offsetx;
	}
	public Integer getOffsety() {
		return offsety;
	}
	public void setOffsety(Integer offsety) {
		this.offsety = offsety;
	}
	
	
	
}
