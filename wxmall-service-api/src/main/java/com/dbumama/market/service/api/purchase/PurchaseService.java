package com.dbumama.market.service.api.purchase;

import java.util.List;

import com.dbumama.market.model.Purchase;
import com.dbumama.market.model.PurchaseOrder;

public interface PurchaseService {
	List<Purchase> find();
	Purchase findById(Long id);
	PurchaseOrder getPurchaseOrder(String tradeNo);
}
