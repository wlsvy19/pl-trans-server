package com.eBrother.trans.image.model;

public class TimeSyncResponse {

	byte  stx = 0x02;
	short seq;

	// short --> int 수정
	int lenPayload;


	short icCode;
	short workNo;
	short cmdType;
	String curTime;
	// data ...

	///////////////////////////////////////
	
	byte  etx = 0x03;

	public byte getStx() {
		return stx;
	}

	public void setStx(byte stx) {
		this.stx = stx;
	}

	public short getSeq() {
		return seq;
	}

	public void setSeq(short seq) {
		this.seq = seq;
	}

	public int getLenPayload() {
		return lenPayload;
	}

	public void setLenPayload(int lenPayload) {
		this.lenPayload = lenPayload;
	}

	public short getIcCode() {
		return icCode;
	}

	public void setIcCode(short icCode) {
		this.icCode = icCode;
	}

	public short getWorkNo() {
		return workNo;
	}

	public void setWorkNo(short workNo) {
		this.workNo = workNo;
	}

	public short getCmdType() {
		return cmdType;
	}

	public void setCmdType(short cmdType) {
		this.cmdType = cmdType;
	}

	public byte getEtx() {
		return etx;
	}

	public void setEtx(byte etx) {
		this.etx = etx;
	}

	public String getCurTime () {
		return curTime;
	}

	public void setCurTime(String curTime) {
		this.curTime = curTime;
	}

}
