package com.eBrother.trans.image.model;

import java.util.Hashtable;

public class PlResponse {

	byte  stx = 0x02;
	short seq = 0;

	// 수정 : short --> int
	int lenPayload;


	short icCode;
	short workNo;
	short cmdType;

	short respCmdType;

	// data ...
	byte[] dataFlag = new byte [] {0x00, 0x00 };
	short plFlag;
	int blMoney;
	int blCnt;
	byte[] carBcdNo = null; // 5 byte.
	byte[] resv = new byte [] { 0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00 }; // 8 개 
	///////////////////////////////////////

	String blData = null;
	String plData = null;

	String curTime;

	short imgWidth = 0;
	short imgHeight = 0;

	public short getImgWidth () {
		return imgWidth;
	}

	public short getImgHeight () {
		return imgHeight;
	}

	public void setImgWidth ( short imgWidth ) {
		this.imgWidth = imgWidth;
	}

	public void setImgHeight ( short imgHeight) {
		this.imgHeight = imgHeight;
	}


	public String getCurTime () {
		return curTime;
	}

	public void setCurTime(String curTime) {
		this.curTime = curTime;
	}


	public String getBlData() {
		return blData;
	}

	public void setBlData(String blData) {
		this.blData = blData;
	}

	public String getPlData() {
		return plData;
	}

	public void setPlData(String plData) {
		this.plData = plData;
	}

	public String getStplData() {
		return stplData;
	}

	public void setStplData(String stplData) {
		this.stplData = stplData;
	}

	String stplData = null;

	String csData = null;

	public String getCsData() {
		return csData;
	}

	public void setCsData(String csData) {
		this.csData = csData;
	}
	////////////////////////////////////////////////////////////////////////////////

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

	public void setLenPayload( int lenPayload) {
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

	public short getRespCmdType() {
		return respCmdType;
	}

	public void setRespCmdType(short respCmdType) {
		this.respCmdType = respCmdType;
	}

	public byte [] getDataFlag() {
		return dataFlag;
	}

	public void setDataFlag( byte [] dataFlag) {
		this.dataFlag = dataFlag.clone();
	}

	public short getPlFlag() {
		return plFlag;
	}

	public void setPlFlag(short plFlag) {
		this.plFlag = plFlag;
	}

	public int getBlMoney() {
		return blMoney;
	}

	public void setBlMoney(int blMoney) {
		this.blMoney = blMoney;
	}

	public int getBlCnt() {
		return blCnt;
	}

	public void setBlCnt(int blCnt) {
		this.blCnt = blCnt;
	}

	public byte[] getResv() {
		return resv;
	}

	public void setResv(byte[] resv) {
		this.resv = resv.clone();
	}

	public byte getEtx() {
		return etx;
	}

	public void setEtx(byte etx) {
		this.etx = etx;
	}

	public byte [] getCarBcdNo () {
		return this.carBcdNo;
	}

	public void setCarBcdNo ( byte [] carBcdNo) {

		this.carBcdNo = carBcdNo.clone();

	}

}
