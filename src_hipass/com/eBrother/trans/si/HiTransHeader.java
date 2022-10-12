package com.eBrother.trans.si;

import java.net.Socket;

public class HiTransHeader {

	private String DATA_TYPE = "COLLECT";
	private String SYS_TYPE = "HIPASS";
	private String SEND_TIME = "20190102134317779";
	private String IC_CODE = "0001";
	private String LANE_NO = "01";
	private String BD_NAME = "MCU01";
	private String MSG_TYPE = "WORK_START";
	private String MAKER_NAME = "STRA";
	private String INTERFACE_VERSION = "1.0";
	private Socket socket = null;
	
	
	public String getDATA_TYPE() {
		return DATA_TYPE;
	}
	public void setDATA_TYPE(String dATA_TYPE) {
		DATA_TYPE = dATA_TYPE;
	}
	public String getSYS_TYPE() {
		return SYS_TYPE;
	}
	public void setSYS_TYPE(String sYS_TYPE) {
		SYS_TYPE = sYS_TYPE;
	}
	public String getSEND_TIME() {
		return SEND_TIME;
	}
	public void setSEND_TIME(String sEND_TIME) {
		SEND_TIME = sEND_TIME;
	}
	public String getIC_CODE() {
		return IC_CODE;
	}
	public void setIC_CODE(String iC_CODE) {
		IC_CODE = iC_CODE;
	}
	public String getLANE_NO() {
		return LANE_NO;
	}
	public void setLANE_NO(String lANE_NO) {
		LANE_NO = lANE_NO;
	}
	public String getBD_NAME() {
		return BD_NAME;
	}
	public void setBD_NAME(String bD_NAME) {
		BD_NAME = bD_NAME;
	}
	public String getMSG_TYPE() {
		return MSG_TYPE;
	}
	public void setMSG_TYPE(String mSG_TYPE) {
		MSG_TYPE = mSG_TYPE;
	}
	public String getMAKER_NAME() {
		return MAKER_NAME;
	}
	public void setMAKER_NAME(String mAKER_NAME) {
		MAKER_NAME = mAKER_NAME;
	}
	public String getINTERFACE_VERSION() {
		return INTERFACE_VERSION;
	}
	public void setINTERFACE_VERSION(String iNTERFACE_VERSION) {
		INTERFACE_VERSION = iNTERFACE_VERSION;
	}
	public Socket getSocket() {
		return socket;
	}
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	

}


