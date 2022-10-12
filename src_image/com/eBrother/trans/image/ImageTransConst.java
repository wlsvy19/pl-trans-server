package com.eBrother.trans.image;

public interface ImageTransConst {
	

	final static short _PL_HEAD_LEN_V2 = 13;

	static final short _PL_RESP_HEAD_LEN_V2 = 14;

	static final short _PL_RESP_PAYLOAD_LEN_ = 25;

	static final short _PL_RESP_TIMESYNC_LEN_ = 14;

	//  앞에서 7개를 먼저 읽음 - payload size 를 알기 위해서 ...

	// MSG_TOTAL = stx ( 1 ) + SEQ (2) + LEN ( 4) + IC (2) + Work(2) + MsgId ( 2 ) + payload ( X ) + etx ( 1 )
	// TAIL_LEN = ( LEN(4) 에 저장된 실제 payload 길이  + 7 ( = 6+1 )
	final static short _PL_TAIL_LEN_ = 1;
	
	final static short _PL_MSG_REQ_ = 1;
	
	final static short _PL_MSG_RESP_ = 2;
	
	final static short _PL_MSG_MONITOR_ = 3;

	final static short _PL_MSG_TIMESYNC_ = 4;

	// 재인식 초기정보 요청
	final static short _PL_MSG_IMG_INIT_REQ_ = 5;

	// 재인식 초기정보 응답
	final static short _PL_MSG_IMG_INIT_RESP_ = 6;
	final static short _PL_RESP_IMG_INIT_LEN_ = 40;
	final static short _PL_RESP_IMG_INIT_RESERV_ = 36;

	// 재인식 요청
	final static short _PL_MSG_IMG_RECCHECK_REQ_ = 7;

	// 제인식 응답
	final static short _PL_MSG_IMG_RECHECK_RESP_ = 8;
	final static short _PL_RESP_IMG_RECHECK_LEN_ = 29;
	final static short _PL_RESP_IMG_RECHECK_RESERV_ = 8;
	
	// 보수데이타
	final static short _PL_MSG_BOSU_DATA_ = 9;

}
