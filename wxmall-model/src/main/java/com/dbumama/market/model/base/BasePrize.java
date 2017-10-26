package com.dbumama.market.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings({"serial", "unchecked"})
public abstract class BasePrize<M extends BasePrize<M>> extends Model<M> implements IBean {

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

	public M setPrizeTypeId(java.lang.Long prizeTypeId) {
		set("prize_type_id", prizeTypeId);
		return (M)this;
	}

	public java.lang.Long getPrizeTypeId() {
		return get("prize_type_id");
	}

	public M setPrizeName(java.lang.String prizeName) {
		set("prize_name", prizeName);
		return (M)this;
	}

	public java.lang.String getPrizeName() {
		return get("prize_name");
	}

	public M setPrizeImg(java.lang.String prizeImg) {
		set("prize_img", prizeImg);
		return (M)this;
	}

	public java.lang.String getPrizeImg() {
		return get("prize_img");
	}

	public M setPrizePrice(java.lang.String prizePrice) {
		set("prize_price", prizePrice);
		return (M)this;
	}

	public java.lang.String getPrizePrice() {
		return get("prize_price");
	}

	public M setPrizeMemo(java.lang.String prizeMemo) {
		set("prize_memo", prizeMemo);
		return (M)this;
	}

	public java.lang.String getPrizeMemo() {
		return get("prize_memo");
	}

	public M setPrizeSingleCash(java.lang.Integer prizeSingleCash) {
		set("prize_single_cash", prizeSingleCash);
		return (M)this;
	}

	public java.lang.Integer getPrizeSingleCash() {
		return get("prize_single_cash");
	}

	public M setPublishCount(java.lang.Integer publishCount) {
		set("publish_count", publishCount);
		return (M)this;
	}

	public java.lang.Integer getPublishCount() {
		return get("publish_count");
	}

	public M setHadOutCount(java.lang.Integer hadOutCount) {
		set("had_out_count", hadOutCount);
		return (M)this;
	}

	public java.lang.Integer getHadOutCount() {
		return get("had_out_count");
	}

	public M setStartDate(java.util.Date startDate) {
		set("start_date", startDate);
		return (M)this;
	}

	public java.util.Date getStartDate() {
		return get("start_date");
	}

	public M setEndDate(java.util.Date endDate) {
		set("end_date", endDate);
		return (M)this;
	}

	public java.util.Date getEndDate() {
		return get("end_date");
	}

	public M setOutId(java.lang.String outId) {
		set("out_id", outId);
		return (M)this;
	}

	public java.lang.String getOutId() {
		return get("out_id");
	}

	public M setActive(java.lang.Integer active) {
		set("active", active);
		return (M)this;
	}

	public java.lang.Integer getActive() {
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