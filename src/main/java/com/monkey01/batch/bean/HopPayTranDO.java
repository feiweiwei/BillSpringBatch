package com.monkey01.batch.bean;

/**
 * @author: feiweiwei
 * @description:
 * @created Date: 15:19 17/11/27.
 * @modify by:
 */
public class HopPayTranDO {
	private String tranId;
	private String channel;
	private String tranType;
	private String counterparty;
	private String goods;
	private String amount;
	private String isDebitCredit;
	private String state;
	private String tranDate;
	private String merId;

	public String getTranId() {
		return tranId;
	}

	public void setTranId(String tranId) {
		this.tranId = tranId;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getTranType() {
		return tranType;
	}

	public void setTranType(String tranType) {
		this.tranType = tranType;
	}

	public String getCounterparty() {
		return counterparty;
	}

	public void setCounterparty(String counterparty) {
		this.counterparty = counterparty;
	}

	public String getGoods() {
		return goods;
	}

	public void setGoods(String goods) {
		this.goods = goods;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getIsDebitCredit() {
		return isDebitCredit;
	}

	public void setIsDebitCredit(String isDebitCredit) {
		this.isDebitCredit = isDebitCredit;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getTranDate() {
		return tranDate;
	}

	public void setTranDate(String tranDate) {
		this.tranDate = tranDate;
	}

	public String getMerId() {
		return merId;
	}

	public void setMerId(String merId) {
		this.merId = merId;
	}

	@Override
	public String toString() {
		return "HopPayTranDO{" +
				"tranId='" + tranId + '\'' +
				", channel='" + channel + '\'' +
				", tranType='" + tranType + '\'' +
				", counterparty='" + counterparty + '\'' +
				", goods='" + goods + '\'' +
				", amount='" + amount + '\'' +
				", isDebitCredit='" + isDebitCredit + '\'' +
				", state='" + state + '\'' +
				", tranDate='" + tranDate + '\'' +
				", merId='" + merId + '\'' +
				'}';
	}
}
