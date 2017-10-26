package com.dbumama.market.service.api.order;

import com.dbumama.market.model.SellerAddr;

@SuppressWarnings("serial")
public class PrintExpItemResultDto extends OrderResultDto {

	private String contact_name;
	private String city;
	private String country;
	private String province;
	private String addr;
	private String memo;
	private String phone;
	private String seller_company;
	private String zip_code;
	
	public PrintExpItemResultDto(SellerAddr sendAddr, OrderResultDto orderDetailResultDto){
		
		//卖家发货信息
		this.sellerAddr = sendAddr;
		setContact_name(this.sellerAddr.getContactName());
		setCity(this.sellerAddr.getCity());
		setCountry(this.sellerAddr.getCountry());
		setProvince(this.sellerAddr.getProvince());
		setAddr(this.sellerAddr.getAddr());
		setMemo(this.sellerAddr.getMemo());
		setPhone(this.sellerAddr.getPhone());
		setSeller_company(this.sellerAddr.getSellerCompany());
		setZipCode(this.sellerAddr.getZipCode());
		
		//买家订单信息
		setOrderId(orderDetailResultDto.getOrderId());
		setReceiverName(orderDetailResultDto.getReceiverName());
		setReceiverPhone(orderDetailResultDto.getReceiverPhone());
		setReceiverProvince(orderDetailResultDto.getReceiverProvince());
		setReceiverCity(orderDetailResultDto.getReceiverCity());
		setReceiverCountry(orderDetailResultDto.getReceiverCountry());
		setReceiverAddr(orderDetailResultDto.getReceiverAddr());
		setZipCode(orderDetailResultDto.getZipCode());
		setBuyerNick(orderDetailResultDto.getBuyerNick());
		setBuyerMemo(orderDetailResultDto.getBuyerMemo());
		setTotalPrice(orderDetailResultDto.getTotalPrice());
		setPostFee(orderDetailResultDto.getPostFee());
		setPayFee(orderDetailResultDto.getPayFee());
		setOrderSn(orderDetailResultDto.getOrderSn());
		setOrderCreated(orderDetailResultDto.getOrderCreated());
		setOrderStatus(orderDetailResultDto.getOrderStatus());
		setOrderItems(orderDetailResultDto.getOrderItems());
		
	}
	
	public String getContact_name() {
		return contact_name;
	}

	public void setContact_name(String contact_name) {
		this.contact_name = contact_name;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getAddr() {
		return addr;
	}

	public void setAddr(String addr) {
		this.addr = addr;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getSeller_company() {
		return seller_company;
	}

	public void setSeller_company(String seller_company) {
		this.seller_company = seller_company;
	}

	public String getZip_code() {
		return zip_code;
	}

	public void setZip_code(String zip_code) {
		this.zip_code = zip_code;
	}

	private SellerAddr sellerAddr;

	public SellerAddr getSellerAddr() {
		return sellerAddr;
	}

	public void setSellerAddr(SellerAddr sellerAddr) {
		this.sellerAddr = sellerAddr;
	}
	
}
