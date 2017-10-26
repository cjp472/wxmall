package com.dbumama.market.service.provider;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dbumama.market.model.AuthUserConfig;
import com.dbumama.market.model.ExpressTemplate;
import com.dbumama.market.model.InvoiceTemplate;
import com.dbumama.market.model.SellerAddr;
import com.dbumama.market.service.api.order.InvTemplateService;
import com.dbumama.market.service.api.order.OrderException;
import com.dbumama.market.service.api.order.OrderResultDto;
import com.dbumama.market.service.api.order.OrderService;
import com.dbumama.market.service.api.order.PrintExpItemResultDto;
import com.dbumama.market.service.api.order.PrintExpResultDto;
import com.dbumama.market.service.api.order.PrintInvItemResultDto;
import com.dbumama.market.service.api.order.PrintInvResultDto;
import com.dbumama.market.service.api.order.PrintParamDto;
import com.dbumama.market.service.api.order.PrintService;
import com.dbumama.market.service.api.order.SellerAddrService;
import com.dbumama.market.service.base.AbstractServiceImpl;
import com.jfinal.kit.StrKit;

@Service("printService")
public class PrintServiceImpl extends AbstractServiceImpl implements PrintService{

	@Autowired
	private OrderService orderService;
	@Autowired
	private SellerAddrService sendAddrService;
	@Autowired
	private InvTemplateService invTemplateService;
	
	public static final AuthUserConfig authConfigDao = new AuthUserConfig().dao();
	private static final ExpressTemplate expressTpldao = new ExpressTemplate().dao();
	
	@Override
	public PrintExpResultDto getExpressPrintData(PrintParamDto printParamDto) throws OrderException {
		if(printParamDto == null || StrKit.isBlank(printParamDto.getOrderIds()) 
				|| StrKit.isBlank(printParamDto.getExpressKey())
				|| printParamDto.getSellerId() == null)
			throw new OrderException("调用获取打印快递单数据接口参数错误");
		
		ExpressTemplate template = expressTpldao.findFirst("select * from " + ExpressTemplate.table + " where "
				+ " seller_id=? and exp_key = ? ", printParamDto.getSellerId(), printParamDto.getExpressKey());
		if(template == null) throw new OrderException("快递模板不存在");
		
		//查询发货设置信息
		SellerAddr sendAddr = sendAddrService.getSendAddr(printParamDto.getSellerId());
		if(sendAddr == null) throw new OrderException("发货信息为空，请到店铺模块中设置");
		
		//查询订单数据
		List<OrderResultDto> orderDetailList = orderService.getOrders(printParamDto.getOrderIds());
		
		PrintExpResultDto orderPrintResultDto = new PrintExpResultDto();
		orderPrintResultDto.setExpressTemplate(template);
		List<PrintExpItemResultDto> printItems = new ArrayList<PrintExpItemResultDto>();
		orderPrintResultDto.setPrintData(printItems);
		
		for(OrderResultDto orderDto : orderDetailList){
			PrintExpItemResultDto printItemDto = new PrintExpItemResultDto(sendAddr, orderDto);
			printItems.add(printItemDto);
		}
		
		return orderPrintResultDto;
	}

	@Override
	public PrintInvResultDto getInvoicePrintData(PrintParamDto printParamDto) throws OrderException {
		if(printParamDto == null || StrKit.isBlank(printParamDto.getOrderIds()) || printParamDto.getSellerId() == null)
			throw new OrderException("调用获取打印快递单数据接口参数错误");
		
		InvoiceTemplate invTpl = invTemplateService.getUserTpl(printParamDto.getSellerId());
		if(invTpl == null) throw new OrderException("发货模板不存在");
		
		//查询发货设置信息
		SellerAddr sendAddr = sendAddrService.getSendAddr(printParamDto.getSellerId());
		if(sendAddr == null) throw new OrderException("发货信息为空，请到店铺模块中设置");
		
		//获取正在使用的公众号信息
		/*AuthUser authUser = authUserDao.findFirst("select * from " + AuthUser.table + " where seller_id=? and is_used=1 and active=1", printParamDto.getSellerId());
		if(authUser == null) throw new OrderException("授权公众号不存在");
		String qrcodeBase64 = getImgBase64(authUser.getQrcodeUrl());
		authUser.setQrcodeUrl(qrcodeBase64);*/
		
		List<OrderResultDto> orderDetailList = orderService.getOrders(printParamDto.getOrderIds());
		
		PrintInvResultDto orderPrintInvResultDto = new PrintInvResultDto();
		orderPrintInvResultDto.setInvoiceTemplate(invTpl);
		List<PrintInvItemResultDto> printItems = new ArrayList<PrintInvItemResultDto>();
		orderPrintInvResultDto.setPrintData(printItems);
		
		for(OrderResultDto orderDto : orderDetailList){
			PrintInvItemResultDto printItemDto = new PrintInvItemResultDto(sendAddr, orderDto);
			printItems.add(printItemDto);
		}
		
		return orderPrintInvResultDto;
	}
	
	/*private String getImgBase64(final String url){
		String result = "";
		//获取公众号二维码的base64图片数据
		OkHttpClient okHttpClient = new OkHttpClient();
		Request request = new Request.Builder().
                url(url)
                .addHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116Safari/537.36")
                .build();
		try {
			Response okResponse = okHttpClient.newCall(request).execute();
			Base64 base64 = new Base64();
            result = "data:image/jpg;base64," + base64.encodeAsString(okResponse.body().bytes());
		} catch (IOException e) {
			e.printStackTrace();
			result = null;
		}
		return result;
	}*/

}
