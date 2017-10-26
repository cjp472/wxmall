package com.dbumama.market.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings({"serial", "unchecked"})
public abstract class BaseCashbackRcd<M extends BaseCashbackRcd<M>> extends Model<M> implements IBean {

	public M setId(java.lang.Long id) {
		set("id", id);
		return (M)this;
	}

	public java.lang.Long getId() {
		return get("id");
	}

	public M setBuyerId(java.lang.Long buyerId) {
		set("buyer_id", buyerId);
		return (M)this;
	}

	public java.lang.Long getBuyerId() {
		return get("buyer_id");
	}

	public M setOrderId(java.lang.Long orderId) {
		set("order_id", orderId);
		return (M)this;
	}

	public java.lang.Long getOrderId() {
		return get("order_id");
	}

	public M setProductId(java.lang.Long productId) {
		set("product_id", productId);
		return (M)this;
	}

	public java.lang.Long getProductId() {
		return get("product_id");
	}

	public M setCashBackFee(java.math.BigDecimal cashBackFee) {
		set("cash_back_fee", cashBackFee);
		return (M)this;
	}

	public java.math.BigDecimal getCashBackFee() {
		return get("cash_back_fee");
	}

	public M setSendLog(java.lang.String sendLog) {
		set("send_log", sendLog);
		return (M)this;
	}

	public java.lang.String getSendLog() {
		return get("send_log");
	}

	public M setActive(java.lang.Boolean active) {
		set("active", active);
		return (M)this;
	}

	public java.lang.Boolean getActive() {
		return get("active");
	}

	public M setCreated(java.util.Date created) {
		set("created", created);
		return (M)this;
	}

	public java.util.Date getCreated() {
		return get("created");
	}

	public M setUpdated(java.util.Date updated) {
		set("updated", updated);
		return (M)this;
	}

	public java.util.Date getUpdated() {
		return get("updated");
	}

}