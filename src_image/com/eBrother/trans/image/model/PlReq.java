package com.eBrother.trans.image.model;

import java.nio.ByteBuffer;

public class PlReq {

	byte  stx;
	short seq;
	short lenHeader;
	short icCode;
	short workNo;
	short cmdType;
	byte [] payload;
	byte  etx;

	// 수정 : 기존 short --> int
	int lenPayload;

	short lenTail;

	byte[] carNo = null; // 5 byte.

	int imgSerial = 0;

	public void setImgSerial ( int imgSerial ) { this.imgSerial = imgSerial; }
	public int getImgSerial (  ) {  return this.imgSerial; }

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
	public short getLenHeader() {
		return lenHeader;
	}
	public void setLenHeader(short len) {
		this.lenHeader = len;
	}
	public short getIcCore() {
		return icCode;
	}
	public void setIcCore(short icCore) {
		this.icCode = icCore;
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

	public byte[] getPayload() {
		return payload;
	}
	public void setPayload(byte[] payload) {
		this.payload = payload.clone();
	}
	public int getLenPayload() {
		return lenPayload;
	}
	public void setLenPayload( int lenPayload) {
		this.lenPayload = lenPayload;
	}
	public short getLenTail() {
		return lenTail;
	}
	public void setLenTail(short lenTail) {
		this.lenTail = lenTail;
	}

	public byte [] getCarNo () {
		return this.carNo;
	}

	public void setCarNo ( byte [] carNo) {
		this.carNo = carNo.clone();
	}

}
