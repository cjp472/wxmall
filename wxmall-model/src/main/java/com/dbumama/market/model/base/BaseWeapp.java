package com.dbumama.market.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings({"serial", "unchecked"})
public abstract class BaseWeapp<M extends BaseWeapp<M>> extends Model<M> implements IBean {

	public M setId(java.lang.Long id) {
		set("id", id);
		return (M)this;
	}

	public java.lang.Long getId() {
		return get("id");
	}

	public M setSellerId(java.lang.Long sellerId) {
		set("seller_id", sellerId);
		return (M)this;
	}

	public java.lang.Long getSellerId() {
		return get("seller_id");
	}

	public M setWeappId(java.lang.String weappId) {
		set("weapp_id", weappId);
		return (M)this;
	}

	public java.lang.String getWeappId() {
		return get("weapp_id");
	}

	public M setWeappSecret(java.lang.String weappSecret) {
		set("weapp_secret", weappSecret);
		return (M)this;
	}

	public java.lang.String getWeappSecret() {
		return get("weapp_secret");
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