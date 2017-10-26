package com.dbumama.market.service.provider;

import java.util.List;

import org.springframework.stereotype.Service;

import com.dbumama.market.model.Purchase;
import com.dbumama.market.model.PurchaseOrder;
import com.dbumama.market.service.api.purchase.PurchaseService;

@Service("purchaseService")
public class PurchaseServiceImpl implements PurchaseService{

	private static final Purchase dao = new Purchase().dao();
	private static final PurchaseOrder pOrderdao = new PurchaseOrder().dao();
	
	@Override
	public List<Purchase> find() {
		return dao.find("select * from " + Purchase.table+ " where status = 1");
	}

	@Override
	public Purchase findById(Long id) {
		return dao.findById(id);
	}

	@Override
	public PurchaseOrder getPurchaseOrder(String tradeNo) {
		return pOrderdao.findFirst(" select * from " + PurchaseOrder.table + " where trade_no =　?", tradeNo);
	}

}
