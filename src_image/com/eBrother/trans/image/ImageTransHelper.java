package com.eBrother.trans.image;

import com.eBrother.trans.image.model.PlReq;
import com.eBrother.trans.image.model.PlResponse;
import com.eBrother.trans.image.model.TimeSyncResponse;
import com.eBrother.trans.image.util.CarBcdConv;
import com.eBrother.util.UtilExt;
import com.eBrother.util.eBrotherUtil;
import org.apache.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.Arrays;

/*
* Header Length : short --> int 로 바뀜에 따름.
*                 head length 는 +2 가 되고, data length 는 동일함
 */
public class ImageTransHelper implements ImageTransConst {

    protected static Logger _log = Logger.getLogger(ImageTransHelper.class.getName());

    static private CarBcdConv _carBcdHelper = CarBcdConv.getInstance();

    static ImageTransHelper _myself = null;

    public static ImageTransHelper getInstance() {

        if ( _myself == null ) {

            _myself = new ImageTransHelper();
        }

        return _myself;

    }

    ImageCacheJnaHelper _cacheHelper_ = ImageCacheJnaHelper.getInstance();

    static byte [] _dataFlagDefault = new byte [] { 0x00, 0x00};
    public ImageTransHelper() {

    }

    public byte [] run_core ( String ip, PlReq plreq ) {

        String run_step = "";

        if ( plreq.getCmdType() == _PL_MSG_REQ_ ) {



            // 차량번호 설정
            // BCD 형식으로 인천31바2476영 이런 차량번호가 입력됨
            // 이 경우, kkk[0] = 0x42; kkk[1] = 0x31; kkk[2] = 0x05; kkk[3] = 0x24; kkk[4] = 0x76; 에 해당함.
            // 실제 차량번호 master 파일은 "영" 을 제외하고 처리해야 함.
            // 결과적으로, bcd --> plain --> bcd 로 변환 처리함.
//            > Task :CarBcdConv.main()
//            org -> 인천31바2476영
//            org -> [ 0x42 0x31 0x05 0x24 0x76 ]
//            conv -> 인천31바2476
//            conv -> [ 0x02 0x31 0x05 0x24 0x76 ]
            try {
//            	System.out.println("plreq.getPayload() : " + plreq.getPayload());
                String revkkk = _carBcdHelper.convBcdFormatToCarNo(plreq.getPayload());
//                System.out.println("revkkk : " + revkkk);
                byte[] kkk2 = _carBcdHelper.convCarNoBcdFormat(revkkk);
//                System.out.println("kkk2 : " + kkk2);
                plreq.setCarNo(kkk2);
            }
            catch ( Exception e ) {
//            	System.out.println("e.printStackTrace() : " + e);
                byte [] carBcdNo = new byte [] { 0x00, 0x00, 0x00, 0x00, 0x00 };
                plreq.setCarNo(  carBcdNo );
            }
            /////////////////////////////////////////////////////////

            // response 를 만들어서 전달 ...
            PlResponse plresp = initPlResponse( plreq, _PL_MSG_RESP_, _PL_RESP_PAYLOAD_LEN_ );

            run_step = "step 08. resp ready";

            setPlRespDetail ( ip, plreq, plresp );

            run_step = "step 09. bytebuffer ready";

            ByteBuffer msg_resp = getPlRespOut ( plreq, plresp );

            run_step = "step 10. resp byte ready :" + msg_resp.capacity();

            byte [] msgout = new byte [ _PL_RESP_HEAD_LEN_V2 +  plresp.getLenPayload() ];

            msg_resp.flip();
            msg_resp.get( msgout, 0, msg_resp.capacity() );

            _log.info("resp " + _carBcdHelper.convBcdFormatToCarNo( plreq.getCarNo()) + " = " + UtilExt.print( msgout ));

            return msgout;

        }
        else if ( plreq.getCmdType() == _PL_MSG_MONITOR_ ) {

            // monitor data 저장하는 것 처리...


        }
        else if ( plreq.getCmdType() == _PL_MSG_BOSU_DATA_ ) {

            // bosu data 저장하는 것 처리...
        	 _log.info ("[BOSU_DATA_TEST] _PL_MSG_BOSU_DATA_== 02(setStx)==" + plreq.getStx());
	   		 _log.info ("[BOSU_DATA_TEST] _PL_MSG_BOSU_DATA_== 02(setSeq)==" + plreq.getSeq());
	   		 _log.info ("[BOSU_DATA_TEST] _PL_MSG_BOSU_DATA_== 02(setLenPayload)==" + plreq.getLenPayload());
	   		 _log.info ("[BOSU_DATA_TEST] _PL_MSG_BOSU_DATA_== 02(setIcCore)==" + plreq.getIcCore());
	   		 _log.info ("[BOSU_DATA_TEST] _PL_MSG_BOSU_DATA_== 02(setWorkNo)==" + plreq.getWorkNo());
	   		 _log.info ("[BOSU_DATA_TEST] _PL_MSG_BOSU_DATA_== 02(setWorkNo)==" + plreq.getWorkNo());
        	
        	


        }
        else if ( plreq.getCmdType() == _PL_MSG_IMG_INIT_REQ_ ) {

            // monitor data 저장하는 것 처리...

            byte [] carBcdNo = new byte [] { 0x00, 0x00, 0x00, 0x00, 0x00 };
            plreq.setCarNo(  carBcdNo );

            // response 를 만들어서 전달 ...
            PlResponse plresp = initPlResponse(plreq, _PL_MSG_IMG_INIT_RESP_, _PL_RESP_IMG_INIT_LEN_ );

            plresp.setImgHeight((short) 1080);
            plresp.setImgWidth ((short) 1920);

            run_step = "step 08. resp ready";
            ByteBuffer msg_resp = getImgInitRespOut ( plreq, plresp );

            run_step = "step 10. resp byte ready :" + msg_resp.capacity();

            byte [] msgout = new byte [ _PL_RESP_HEAD_LEN_V2 +  +  plresp.getLenPayload() ];

            msg_resp.flip();
            msg_resp.get( msgout, 0, msg_resp.capacity() );
            _log.info("resp = " + UtilExt.print( msgout ));

            return msgout;

        }
        else if ( plreq.getCmdType() == _PL_MSG_IMG_RECCHECK_REQ_ ) {

            // response 를 만들어서 전달 ...
            PlResponse plresp = initPlResponse(plreq, _PL_MSG_IMG_RECHECK_RESP_, _PL_RESP_IMG_RECHECK_LEN_ );

            run_step = "step 08. resp ready";
            // 이미지 재 인식을 통해 획득한 차량 bcdNo 를 대상으로 상세 정보 검색
            setPlRespDetail ( ip, plreq, plresp );

            run_step = "step 08. resp ready";
            ByteBuffer msg_resp = getImgReCheckRespOut ( plreq, plresp );

            run_step = "step 10. resp byte ready :" + msg_resp.capacity();

            byte [] msgout = new byte [ _PL_RESP_HEAD_LEN_V2 +  +  plresp.getLenPayload() ];

            msg_resp.flip();
            msg_resp.get( msgout, 0, msg_resp.capacity() );
            _log.info("resp = " + UtilExt.print( msgout ));

            return msgout;


        }
        return null;

    }

    public PlResponse initPlResponse ( PlReq plreq, short cmdRespType, int payLoadLen) {


        PlResponse plresp = new PlResponse ();

        plresp.setSeq(plreq.getSeq());
        plresp.setLenPayload( payLoadLen );
        plresp.setIcCode( plreq.getIcCore());
        plresp.setWorkNo(plreq.getWorkNo());
        plresp.setCmdType( plreq.getCmdType() );
        plresp.setCarBcdNo( plreq.getCarNo());

        plresp.setRespCmdType( cmdRespType );

        return plresp;

    }

    public boolean setPlRespDetail ( String ip, PlReq plreq, PlResponse plresp ) {

        boolean bret = false;

        // _log.info ( "cache : don't ready" );

        plresp.setDataFlag( _dataFlagDefault );
        plresp.setPlFlag( ( short ) 0x0000);
        plresp.setBlMoney( 0);
        plresp.setBlCnt( 0 );

        // cache 가 준비가 되지 않은 상태면 ...
        // 초기값 return 함.
        if ( ! _cacheHelper_.isReady()) return bret;

        // data 읽어 오고 ...
        _cacheHelper_.setImageTransInfo ( plresp );

        byte [] result = new byte []{ 0x00, 0x00 };

        StringBuffer msgBuf = new StringBuffer ();

        // CS 차량번호 정보 획득.
        // 기존에 PL 에서 차종을 획득했으나, 해당 정보를 CSPM - 차량제원정보로 대체 처리
        // CS spec   차량번호 | 차종 ( 1자리 - 1 ~ 6 ) | 구분코드 ( 00 : default ) | 처리구분
        // 처리구분(1)  1 : 입력  2 : 수정  3 : 삭제

        boolean isCsExist = false;
        if ( plresp.getCsData() != null && ! eBrotherUtil.getDelimitData( plresp.getCsData(), "|", 4 ).equals("3")) {

            isCsExist = true;
            String tmp = eBrotherUtil.getDelimitData(plresp.getCsData(), "|", 2);
            plresp.setPlFlag( ( short ) eBrotherUtil.getIntNumber( tmp ));

            msgBuf.append ( " master 차종 : " + tmp );
            result[1]= (byte) (result[1] | 0x02);
        }
        else msgBuf.append ( " master 차종 : 없음 "  );

        // PL 임시로 막아 놓음 : CS 로 대체 - 경차 여부에 대한 것 처리하는 부분이 없음
        // PL Spec : 차량번호(12)|유형(2)|처리구분(1)
        // 유형(2) 00 : 경차(PL)
        // 처리구분(1)  1 : 입력  2 : 수정  3 : 삭제
        if ( ! isCsExist && plresp.getPlData() != null && ! eBrotherUtil.getDelimitData( plresp.getPlData(), "|", 3 ).equals("3")) {

            String tmp = eBrotherUtil.getDelimitData(plresp.getPlData(), "|", 2);
            if ( tmp.equals("00")) plresp.setPlFlag( ( short ) 6);
            else plresp.setPlFlag( ( short ) eBrotherUtil.getIntNumber( eBrotherUtil.getDelimitData(plresp.getPlData(), "|", 2)));
            result[1]= (byte) (result[1] | 0x02);
        }

        // BL Spec : 차량번호(12)|미납금액(5)|미납건수(5)|유형(2)|처리구분(1)
        // 유형(2)  00 : 미납(BL)  01 : 제한차량(RL)  02 : 미납&제한차량   03 : 환불
        // 처리구분(1)   1 : 입력   2 : 수정    3 : 삭제
        if ( plresp.getBlData() != null && ! eBrotherUtil.getDelimitData( plresp.getBlData(), "|", 5 ).equals("3")) {

            if ( eBrotherUtil.getDelimitData( plresp.getBlData(), "|", 4 ).equals("00")) {
                // 미납 B/L
                result[1]= (byte) (result[1] | 0x01);

                msgBuf.append ( ", BL 미납 :  "  + "00" );
            }
            else if ( eBrotherUtil.getDelimitData( plresp.getBlData(), "|", 4 ).equals("01")) {
                // 제한차량
                result[1] = (byte) (result[1] | 0x04);
                msgBuf.append ( ", BL 제한차량 :  "  + "01" );
            }
            else if ( eBrotherUtil.getDelimitData( plresp.getBlData(), "|", 4 ).equals("02")) {
                // 미납 & 제한차량
                result[1] = (byte) (result[1] | 0x05);
                msgBuf.append ( ", BL 미납 & 제한차량 :  "  + "02" );
            }
            else if ( eBrotherUtil.getDelimitData( plresp.getBlData(), "|", 4 ).equals("03")) {
                // 미납 & 제한차량
                result[1] = (byte) (result[1] | 0x08);
                msgBuf.append ( ", BL 환불 :  "  + "03" );
            }

            plresp.setBlMoney(  eBrotherUtil.getIntNumber( eBrotherUtil.getDelimitData(plresp.getBlData(), "|", 2)));

            msgBuf.append ( ", BL 금액 :  "  + plresp.getBlMoney( ) );
            plresp.setBlCnt( eBrotherUtil.getIntNumber( eBrotherUtil.getDelimitData(plresp.getBlData(), "|", 3)));
            msgBuf.append ( ", BL 건수 :  "  + plresp.getBlCnt( ) );
        }
        else {
            msgBuf.append ( ", BL 없음 " );
        }

        // STPL Spec : 차량번호(12)|유형(2)|처리구분(1)
        // 유형(2) 00 : 00 : 소형화물(STPL)
        // 처리구분(1)  1 : 입력  2 : 수정  3 : 삭제
//        if ( plresp.getStplData() != null && ! eBrotherUtil.getDelimitData( plresp.getStplData(), "|", 3 ).equals("3")) {
//            result[1] = (byte) (result[1] | 0x010);
//        }
//        plresp.setDataFlag( result );

        // STPL Spec : 차량번호(12)|처리구분(1) -> master 는 어디에 있나 ? 이해가 가지 않는 spec 이다. 일관성이 없네 .
        // 처리구분(1)  00 : 등록, 01 : 삭제
        if ( plresp.getStplData() != null && eBrotherUtil.getDelimitData( plresp.getStplData(), "|", 2 ).equals("00")) {
            result[1] = (byte) (result[1] | 0x010);
            msgBuf.append ( ", STPL - 존재 " );

        }
        else {
            msgBuf.append ( ", STPL - 없음 " );
        }

        plresp.setDataFlag( result );

        _log.info ( "api result : " + ip + " - in carno :  " + byteArrayToHexString ( plreq.getCarNo(), 0, plreq.getCarNo().length)
                + "- " + _carBcdHelper.convBcdFormatToCarNo ( plreq.getCarNo())
                + ", " +  msgBuf.toString() + ", master = " + plresp.getCsData() + ", BL = " + plresp.getBlData() + "," + " "    // =" + plresp.getPlData()
                + ", STPL=" + plresp.getStplData());

        return true;

    }

    public static String byteArrayToHexString(byte[] bytes, int sidx, int dataLen) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i < dataLen; i++) {
            byte b = bytes[i+sidx];
            sb.append(String.format("%02X", b&0xFF));
        }
        return sb.toString();
    }

    public static void setRespHeader ( ByteBuffer buf, PlResponse plresp ) {

        //////////////////////////////////////////////////////////////////////////
        buf.put( plresp.getStx());									// 0, 1
        buf.putShort( plresp.getSeq());								// 2, 3

        // short --> int 로 수정됨.
        buf.putInt ( plresp.getLenPayload());		// 4, 7
        buf.putShort( plresp.getIcCode());							// 2, 9
        buf.putShort( plresp.getWorkNo());							// 2, 11
        buf.putShort( plresp.getRespCmdType());				// 2, 13
        /////////////////////////////////////////////////////////////////////////////////////////

    }

    public ByteBuffer getPlRespOut (  PlReq plreq, PlResponse plresp ) {

        ByteBuffer buf = ByteBuffer.allocate( plresp.getLenPayload() +  _PL_RESP_HEAD_LEN_V2 );

        setRespHeader ( buf, plresp );

        buf.put( plresp.getDataFlag());						        // 2, 15
        buf.putShort( plresp.getPlFlag());							// 2, 17
        buf.putInt( plresp.getBlMoney());							// 4, 21
        buf.putInt( plresp.getBlCnt());								// 4, 25
        
        // 2021.12.08 권선태 대리 수정 요청
        //buf.putInt( plresp.getBlCnt());								// 4, 21
        //buf.putInt( plresp.getBlMoney());							// 4, 25
        
        buf.put( plresp.getCarBcdNo());								// 5, 30
        buf.put( plresp.getResv());									// 8, 38
        buf.put( plresp.getEtx());									// 1, 39

        return buf;
    }

    public ByteBuffer getTimeSyncOut (  PlResponse plresp ) {

        byte f = ( byte)255;
        short maxShort = -32768;

        ByteBuffer buf = ByteBuffer.allocate( plresp.getLenPayload() +  _PL_RESP_HEAD_LEN_V2 );
        buf.put( plresp.getStx());									// 0, 1
        buf.put( f);                                                // 1, 2
        buf.put( f);                                                // 1, 3

        buf.putInt( plresp.getLenPayload());		// 4, 7

        buf.put( f); buf.put( f);                                   // 2, 9
        buf.put( f); buf.put( f);                                   // 2, 11
        buf.putShort( plresp.getRespCmdType());			// 2, 13

        buf.put( eBrotherUtil.getY2K_CurFullDate ().getBytes());    // 14, 27
        buf.put( plresp.getEtx());									// 1, 28

        return buf;
    }

    public ByteBuffer getImgInitRespOut (  PlReq plreq, PlResponse plresp ) {

        byte[] resv = new byte [ _PL_RESP_IMG_INIT_RESERV_ ]; // 36 개

        Arrays.fill( resv,(byte)0);
        ByteBuffer buf = ByteBuffer.allocate( plresp.getLenPayload() +  _PL_RESP_HEAD_LEN_V2 );

        setRespHeader ( buf, plresp );

        buf.putShort( plresp.getImgWidth ());							// 2, 9
        buf.putShort( plresp.getImgHeight());							// 2, 11
        buf.put( resv );                                    // 32, 43
        buf.put( plresp.getEtx());							// 1, 44

        return buf;

    }

    public ByteBuffer getImgReCheckRespOut (  PlReq plreq, PlResponse plresp ) {

        byte[] resv = new byte [ _PL_RESP_IMG_RECHECK_RESERV_ ]; // 8 개

        Arrays.fill( resv,(byte)0);
        ByteBuffer buf = ByteBuffer.allocate( _PL_RESP_HEAD_LEN_V2 + plresp.getLenPayload());

        setRespHeader ( buf, plresp );

        buf.putInt( plreq.getImgSerial() );							// 2, 9
        buf.put( plresp.getCarBcdNo());

        buf.put( plresp.getDataFlag());						        // 2, 15
        buf.putShort( plresp.getPlFlag());							// 2, 17
        buf.putInt( plresp.getBlMoney());							// 4, 21
        buf.putInt( plresp.getBlCnt());								// 4, 25

        buf.put( resv );									// 8, 38
        buf.put( plresp.getEtx());									// 1, 39

        return buf;

    }

    public static void  main(String[] args) {

//        CarBcdConv conv = new CarBcdConv ();
//
//        String target = "임7584";
//
//        target = "서울57가1244";
//        target = "임923070";
//        target = "임7584";
//
//        System.out.println(" org -> " + target);
//
//        byte [] kkk = conv.convCarNoBcdFormat( target );
//
//        System.out.println (" org2 -> " + UtilExt.print( kkk ));
//
//        String revkkk = conv.convBcdFormatToCarNo(kkk);
//        System.out.println ( " conv -> " + revkkk );
//
//        // 02 31 05 24 76
//        // kkk[0] = 0x42; kkk[1] = 0x31; kkk[2] = 0x05; kkk[3] = 0x24; kkk[4] = 0x76;
//
//        byte [] kkk2 = conv.convCarNoBcdFormat( revkkk );
//        System.out.println (" conv -> " + UtilExt.print( kkk2 ));
//
//        if(revkkk.contains("X")) {
//            // BCD변환이 제대로 안되면 BL/PL 조회하지 않고 바로 데이터 없음으로 리턴
//        }
//        else {
//            //BLPL 조회
//        }
    	byte [] payload = new byte[5];
    	payload[0] = (byte) 0xFF;
    	payload[1] = (byte) 0xFF;
    	payload[2] = (byte) 0xFF;
    	payload[3] = (byte) 0xFF;
    	payload[4] = (byte) 0xFF;
    	
    	System.out.println("payload = " + payload);
        String revkkk = _carBcdHelper.convBcdFormatToCarNo(payload);
        System.out.println("revkkk = " + revkkk);
        byte[] kkk2 = _carBcdHelper.convCarNoBcdFormat(revkkk);
        System.out.println("kkk2 = " + kkk2);
    }
}
