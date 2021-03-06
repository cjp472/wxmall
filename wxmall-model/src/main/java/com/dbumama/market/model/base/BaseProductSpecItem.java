package com.dbumama.market.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings({"serial", "unchecked"})
public abstract class BaseProductSpecItem<M extends BaseProductSpecItem<M>> extends Model<M> implements IBean {

	public M setId(java.lang.Long id) {
		set("id", id);
		return (M)this;
	}

	public java.lang.Long getId() {
		return get("id");
	}

	public M setProductId(java.lang.Long productId) {
		set("product_id", productId);
		return (M)this;
	}

	public java.lang.Long getProductId() {
		return get("product_id");
	}

	public M setSpecificationValue(java.lang.String specificationValue) {
		set("specification_value", specificationValue);
		return (M)this;
	}

	public java.lang.String getSpecificationValue() {
		return get("specification_value");
	}

	public M setPrice(java.math.BigDecimal price) {
		set("price", price);
		return (M)this;
	}

	public java.math.BigDecimal getPrice() {
		return get("price");
	}

	public M setStock(java.lang.Integer stock) {
		set("stock", stock);
		return (M)this;
	}

	public java.lang.Integer getStock() {
		return get("stock");
	}

	public M setWeight(java.math.BigDecimal weight) {
		set("weight", weight);
		return (M)this;
	}

	public java.math.BigDecimal getWeight() {
		return get("weight");
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

	public M setActive(java.lang.Integer active) {
		set("active", active);
		return (M)this;
	}

	public java.lang.Integer getActive() {
		return get("active");
	}

}
