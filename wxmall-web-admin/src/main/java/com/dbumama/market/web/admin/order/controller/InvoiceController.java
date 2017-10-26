package com.dbumama.market.web.admin.order.controller;

import java.util.List;
import java.util.Map;

import com.dbumama.market.model.InvoiceTemplate;
import com.dbumama.market.service.api.order.InvTemplateService;
import com.dbumama.market.service.api.order.InvTplSaveParamDto;
import com.dbumama.market.service.api.order.OrderException;
import com.dbumama.market.service.api.order.PalletElementResultDto;
import com.dbumama.market.web.core.annotation.RouteBind;
import com.dbumama.market.web.core.controller.BaseController;
import com.dbumama.market.web.core.plugin.spring.Inject.BY_NAME;
import com.jfinal.kit.StrKit;

@RouteBind(path = "invoice")
public class InvoiceController extends BaseController{
	
	@BY_NAME
	private InvTemplateService invTemplateService;
	
	public void setting(){
		Map<String, List<PalletElementResultDto>> elementCategoryMap = invTemplateService.initPalletElement(getSellerId());
		setAttr("elementCategoryMap", elementCategoryMap);
		
		Map<String, List<PalletElementResultDto>> columnCategoryMap = invTemplateService.initTableColumnElement(getSellerId());
		setAttr("columnCategoryMap", columnCategoryMap);
		
		render("/order/inv_tpl_setting.html");
	}
	
	public void getUserTpl(){
		try {
			InvoiceTemplate invoiceTemplate = invTemplateService.getUserTpl(getSellerId());
			rendSuccessJson(invoiceTemplate);
		} catch (OrderException e) {
			rendFailedJson(e.getMessage());
		}
	}
	
	public void save(){
		final String tplcontent = getPara("tplcontent");
		final String designhtml = getPara("designhtml");
		final String tablehtml = getPara("tablehtml");
		final String imghtml = getPara("imghtml");
		final int pagewidth = getParaToInt("pagewidth", 0);
		final int pageheight = getParaToInt("pageheight", 0);
		final int offsetx = getParaToInt("offsetx", 0);
		final int offsety = getParaToInt("offsety", 0);
		
		InvTplSaveParamDto tplParamDto = new InvTplSaveParamDto(tplcontent, designhtml, tablehtml, getSellerId());
		if(StrKit.notBlank(imghtml)) tplParamDto.setImghtml(imghtml);
		tplParamDto.setPagewidth(pagewidth);
		tplParamDto.setPageheight(pageheight);
		tplParamDto.setOffsetx(offsetx);
		tplParamDto.setOffsety(offsety);
		try {
			InvoiceTemplate invoicetpl = invTemplateService.save(tplParamDto);
			rendSuccessJson(invoicetpl);
		} catch (OrderException e) {
			rendFailedJson(e.getMessage());
		}
		
	}

}
