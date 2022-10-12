package com.eBrother.trans.image.util;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.eBrother.util.UtilExt;

public class CarBcdConv {

    private static CarBcdConv g_instance = null;

    public static CarBcdConv getInstance() {
        if (null == g_instance) g_instance = new CarBcdConv();
        return g_instance;
    }

    private Map<String, String> carNoRegionMap = null; // 지역명, 코드
    private Map<String, String> carNo40RegionMap = null; // 지역명, 코드 (영업용)
    private Map<String, String> carNoCarClassMap = null; // Car_Class, 코드

    private CarBcdConv() {
        _initCarNoRegionMap();
        _initCarNo40RegionMap();
        _initCarNoCarClassMap();
    }

    private void _initCarNo40RegionMap() {
        this.carNo40RegionMap = new HashMap<String, String>();

        this.carNo40RegionMap.put("서울", "40");
        this.carNo40RegionMap.put("부산", "41");
        this.carNo40RegionMap.put("인천", "42");
        this.carNo40RegionMap.put("대구", "43");
        this.carNo40RegionMap.put("광주", "44");
        this.carNo40RegionMap.put("대전", "45");
        this.carNo40RegionMap.put("경기", "46");
        this.carNo40RegionMap.put("강원", "47");
        this.carNo40RegionMap.put("충북", "48");
        this.carNo40RegionMap.put("충남", "49");
        this.carNo40RegionMap.put("전북", "50");
        this.carNo40RegionMap.put("전남", "51");
        this.carNo40RegionMap.put("경북", "52");
        this.carNo40RegionMap.put("경남", "53");
        this.carNo40RegionMap.put("제주", "54");
        this.carNo40RegionMap.put("울산", "55");
        this.carNo40RegionMap.put("세종", "56");
        this.carNo40RegionMap.put("임", "97");
        this.carNo40RegionMap.put("외교", "98");
    }

    private void _initCarNoRegionMap() {
        this.carNoRegionMap = new HashMap<String, String>();

        this.carNoRegionMap.put("서울", "00");
        this.carNoRegionMap.put("부산", "01");
        this.carNoRegionMap.put("인천", "02");
        this.carNoRegionMap.put("대구", "03");
        this.carNoRegionMap.put("광주", "04");
        this.carNoRegionMap.put("대전", "05");
        this.carNoRegionMap.put("경기", "06");
        this.carNoRegionMap.put("강원", "07");
        this.carNoRegionMap.put("충북", "08");
        this.carNoRegionMap.put("충남", "09");
        this.carNoRegionMap.put("전북", "10");
        this.carNoRegionMap.put("전남", "11");
        this.carNoRegionMap.put("경북", "12");
        this.carNoRegionMap.put("경남", "13");
        this.carNoRegionMap.put("제주", "14");
        this.carNoRegionMap.put("울산", "15");
        this.carNoRegionMap.put("세종", "16");
        this.carNoRegionMap.put("임", "97");
        this.carNoRegionMap.put("외교", "98");
    }

    // 8-byte OBU제조번호, 5-byte 차량번호 : 차량번호는 코드표 참조
    public byte[] convCarNoBcdFormat( String carNo ) {

        byte[] data = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00 };

        String convedCarNo = _convCarNoParsingWithCode( "" , carNo );
        _convStringToBcd(convedCarNo, data, 0, 5);
        return data;
    }

    private static <K, V> K getKey(Map<K, V> map, V value) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (value.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    // 차량번호BCD코드 -> 차량번호 문자열 변환
    public String convBcdFormatToCarNo(byte[] carNoBCD) {
        if (carNoBCD.length != 5) {
            throw new RuntimeException(String.format("CAR_NO_REV_PARSE_ERR:INVALID_BCD_LENGTH:%d", carNoBCD.length));
        }

        String carNoBCDStr = byteArrayToHexString(carNoBCD, 0, carNoBCD.length);
        if (carNoBCDStr.length() != 10) {
            throw new RuntimeException(
                    String.format("CAR_NO_REV_PARSE_ERR:INVALID_BCDSTR_LENGTH:%d", carNoBCDStr.length()));
        }

        String[] bcdStrArr = new String[5];
        for (int i = 0; i < 5; i++) {
            bcdStrArr[i] = carNoBCDStr.substring(i * 2, (i * 2) + 2);
        }
        
        
        if ("FFFFFFFFFF".equals(Arrays.stream(bcdStrArr).collect(Collectors.joining()))) {
        	return "XXXXXXX";
        }
        
        //-----------------------------------------------------
        // BCD[0]: 지역코드
        //-----------------------------------------------------
        String region = null; // null이면 지역명 없는 번호
        char ch = bcdStrArr[0].charAt(0);
        if (ch == '8') {
            // 신규 3자리번호판
            region = bcdStrArr[0].substring(1, 2);
        } else if (ch == '4' || ch == '5') {
            // 특장차 번호판
            region = getKey(this.carNo40RegionMap, bcdStrArr[0]);
        } else {
            region = getKey(this.carNoRegionMap, bcdStrArr[0]);
        }
        if(region == null) region = "";

        //-----------------------------------------------------
        // BCD[1]: 차량번호 앞 숫자
        //-----------------------------------------------------
        String headerNum = bcdStrArr[1];
        ch = bcdStrArr[1].charAt(0);
        if ("FF".equalsIgnoreCase(headerNum)) {
        	headerNum = "";
        } else if(ch == 'F' || ch == 'f') {
            headerNum = bcdStrArr[1].substring(1,2);
        }

        //-----------------------------------------------------
        // BCD[2]: 가운데 한글
        //-----------------------------------------------------
        String carClass = getKey(this.carNoCarClassMap, bcdStrArr[2]);
        if(carClass == null) carClass = "X";
        if(carClass == "임") carClass = "";
        //this.carNoCarClassMap.put("-", "89");   //오타발견, _ 가 아니라 - 입니다.

        //-----------------------------------------------------
        // BCD[3]: 차량번호 4자리 중 앞 두개
        //-----------------------------------------------------
        String last4Num1 = bcdStrArr[3];
        last4Num1 = last4Num1.replace("D", "X");

        //-----------------------------------------------------
        // BCD[4]: 차량번호 4자리 중 뒤 두개
        //-----------------------------------------------------
        String last4Num2 = bcdStrArr[4];
        last4Num2 = last4Num2.replace("D", "X");

        // 영상통합쪽에는 특장차 번호판 끝에 "영"자 없이 데이터를 관리하기 때문에 무시한다.
        // 외교번호판도 실제 PL/BL테이블에 없을테니 외교123-456 형태로 별도 변환하지 않는다.
        return (region + headerNum + carClass + last4Num1 + last4Num2);
    }


    private void _convStringToBcd(String str, byte[] bytes, int sidx, int dataLen) {
        int strLen = str.length();
        if (strLen < (dataLen * 2)) {
            StringBuilder sb = new StringBuilder();
            if (str.startsWith("F") && str.endsWith("F")) {
            	for (int i=0; i < (dataLen * 2) - strLen; i++) sb.append("F");
            } else {
            	for (int i=0; i < (dataLen * 2) - strLen; i++) sb.append("0");
            }
            sb.append(str);
            str = sb.toString();
        }
        for (int i=0; i < dataLen; i++) {
            int idx = i * 2;
            int digit1 = Character.digit(str.charAt(idx), 16) << 4;
            int digit2 = Character.digit(str.charAt(idx+1), 16);

            bytes[i+sidx] = (byte)(digit1 + digit2);
        }
    }

    public byte[] convCountBcdFormat(String str, int len) {
        byte[] data = new byte[len];

        _convStringToBcd(str, data, 0, len);

        return data;
    }

    // 숫자 --> 4byte bcd format
    public byte[] convCountBcdFormat(int n) {
        byte[] data = new byte[4];

        _convStringToBcd(String.format("%d", n), data, 0, 4);

        return data;
    }

    // 자동차 번호 코드화
    private String _convCarNoParsingWithCode(String obuMakeNo, String carNo) {
        // 0: Region code, 1: Smal, Car_Class code, 3: Car_Number
        String[] retarr = new String[4];

        String updateChar = null;
        int tLen = carNo.length();

        if (carNo.startsWith("X") && carNo.endsWith("X")) {
        	String[] tmpNo = new String[carNo.length()];
        	
        	for(int i=0;i<carNo.length();i++) {
        		tmpNo[i] = "F";
        	}
        	
        	return String.format("%s", Arrays.stream(tmpNo).collect(Collectors.joining()));
        }
        
        // 영업용 차량
        String _carClass = null;
        if (carNo.endsWith("영")) {
            String region = carNo.substring(0, 2); // 번호판 앞2자리(지역명)
            String retval = this.carNo40RegionMap.get(region);
            if (null == retval) throw new RuntimeException(String.format("CAR_NO_PARSE_ERR:40_REGION_NOT_FOUND:%s/%s", obuMakeNo, carNo));

            retarr[0] = retval;
//			retarr[0] = "40";// TODO: 40~46
            retarr[1] = carNo.substring(2, 4); // Smal 번호
            _carClass = carNo.substring(4, tLen-5);
            retarr[3] = carNo.substring(tLen-5, tLen-1);
        } else if (carNo.startsWith("임")) {
        	retarr[0] = "97";
        	_carClass = "임";
        	if (carNo.substring(1).length() == 6) {
	        	retarr[1] = carNo.substring(1,3);
	        	retarr[3] = carNo.substring(3);
	        	updateChar = carNo.substring(1,3);
        	} else {
        		retarr[1] = "97";
	        	retarr[3] = carNo.substring(1);
	        	updateChar = "FF";
        	}
        	
        } else {
            // 일반차량
            if (Pattern.matches("^\\d\\d\\d.+$", carNo)) { // 신규 번호 체계 (앞3자리가 숫자)
                char ch = carNo.charAt(0);
                retarr[0] = "8" + ch;
                retarr[1] = carNo.substring(1, 3); // Smal 번호
                _carClass = carNo.substring(3, tLen-4).trim();

            } else if (Pattern.matches("^\\d\\d.+$", carNo)) { // 전국 번호 (지역명 없음)
                retarr[0] = "99";
                retarr[1] = carNo.substring(0, 2); // Smal 번호
                _carClass = carNo.substring(2, tLen-4).trim();
            } else { // 구번호 (지역명이 있는 경우)
                String region = carNo.substring(0, 2); // 번호판 앞2자리(지역명)
                String retval = this.carNoRegionMap.get(region);
                if (null == retval) throw new RuntimeException(String.format("CAR_NO_PARSE_ERR:REGION_NOT_FOUND:%s/%s", obuMakeNo, carNo));

                retarr[0] = retval;

                if ( carNo.length() == 8 ) {
                    // i.e. 아주 오래된 번호입니다. 충북8다3458
                    // 해당 번호는 image 에만 있고, hipass 는 적용 전입니다. 아마도 적용을 곧 한다고 합니다 ...
                    retarr[1] = carNo.substring(2, 3); // Smal 번호
                    _carClass =  carNo.substring(3, 4).trim();
                    updateChar = "F" + carNo.substring(2, 3);
                }
                else {
                    retarr[1] = carNo.substring(2, 4); // Smal 번호
                    _carClass = carNo.substring(4, tLen - 4).trim();
                }
            }
            retarr[3] = carNo.substring(tLen-4, tLen); // 뒤의 4자리
        }

        retarr[0] = retarr[0].replace("X", "D");
        retarr[3] = retarr[3].replace("X", "D");
        
        // Car_Class 확인
        retarr[2] = this.carNoCarClassMap.get(_carClass.trim());
        //if (null == retarr[2]) throw new RuntimeException(String.format("CAR_NO_PARSE_ERR:CAR_CLASS_NOT_FOUND:%s/%s", obuMakeNo, carNo));

        // Car_Number digit 확인
//        if (!Pattern.matches("\\d\\d", retarr[3]) 
//        		&& !Pattern.matches("\\d\\d\\d\\d", retarr[3]) 
//        		&& !Pattern.matches("\\d\\d\\d\\d\\d\\d", retarr[3])
//        		&& !Pattern.matches("FF", retarr[3])) throw new RuntimeException(String.format("CAR_NO_PARSE_ERR:CAR_NUM_NO_DIGIT:%s/%s", obuMakeNo, carNo));

        // Smal digit 확인
//        if ( (( updateChar == null && !Pattern.matches("\\d\\d", retarr[1]))
//                || ( updateChar != null && !Pattern.matches("\\d", retarr[1])))
//        	&& ( updateChar != null && !Pattern.matches("\\d\\d", retarr[1]))
//        ) throw new RuntimeException(String.format("CAR_NO_PARSE_ERR:SMAL_NO_DIGIT:%s/%s", obuMakeNo, carNo));

        if ( updateChar == null ) updateChar = retarr[1];

        return String.format("%s%s%s%s", retarr[0], updateChar, retarr[2], retarr[3]);
    }

    public String byteArrayToHexString(byte[] bytes, int sidx, int dataLen) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i < dataLen; i++) {
            byte b = bytes[i+sidx];
            sb.append(String.format("%02X", b&0xFF));
        }
        return sb.toString();
    }

    private void _initCarNoCarClassMap() {
        this.carNoCarClassMap = new HashMap<String, String>();

        this.carNoCarClassMap.put("가", "00");
        this.carNoCarClassMap.put("나", "01");
        this.carNoCarClassMap.put("다", "02");
        this.carNoCarClassMap.put("라", "03");
        this.carNoCarClassMap.put("마", "04");
        this.carNoCarClassMap.put("바", "05");
        this.carNoCarClassMap.put("사", "06");
        this.carNoCarClassMap.put("아", "07");
        this.carNoCarClassMap.put("자", "08");
        this.carNoCarClassMap.put("차", "09");
        this.carNoCarClassMap.put("카", "10");
        this.carNoCarClassMap.put("타", "11");
        this.carNoCarClassMap.put("파", "12");
        this.carNoCarClassMap.put("하", "13");

        this.carNoCarClassMap.put("거", "14");
        this.carNoCarClassMap.put("너", "15");
        this.carNoCarClassMap.put("더", "16");
        this.carNoCarClassMap.put("러", "17");
        this.carNoCarClassMap.put("머", "18");
        this.carNoCarClassMap.put("버", "19");
        this.carNoCarClassMap.put("서", "20");
        this.carNoCarClassMap.put("어", "21");
        this.carNoCarClassMap.put("저", "22");
        this.carNoCarClassMap.put("처", "23");
        this.carNoCarClassMap.put("커", "24");
        this.carNoCarClassMap.put("터", "25");
        this.carNoCarClassMap.put("퍼", "26");
        this.carNoCarClassMap.put("허", "27");

        this.carNoCarClassMap.put("고", "28");
        this.carNoCarClassMap.put("노", "29");
        this.carNoCarClassMap.put("도", "30");
        this.carNoCarClassMap.put("로", "31");
        this.carNoCarClassMap.put("모", "32");
        this.carNoCarClassMap.put("보", "33");
        this.carNoCarClassMap.put("소", "34");
        this.carNoCarClassMap.put("오", "35");
        this.carNoCarClassMap.put("조", "36");
        this.carNoCarClassMap.put("초", "37");
        this.carNoCarClassMap.put("코", "38");
        this.carNoCarClassMap.put("토", "39");
        this.carNoCarClassMap.put("포", "40");
        this.carNoCarClassMap.put("호", "41");

        this.carNoCarClassMap.put("구", "42");
        this.carNoCarClassMap.put("누", "43");
        this.carNoCarClassMap.put("두", "44");
        this.carNoCarClassMap.put("루", "45");
        this.carNoCarClassMap.put("무", "46");
        this.carNoCarClassMap.put("부", "47");
        this.carNoCarClassMap.put("수", "48");
        this.carNoCarClassMap.put("우", "49");
        this.carNoCarClassMap.put("주", "50");
        this.carNoCarClassMap.put("추", "51");
        this.carNoCarClassMap.put("쿠", "52");
        this.carNoCarClassMap.put("투", "53");
        this.carNoCarClassMap.put("푸", "54");
        this.carNoCarClassMap.put("후", "55");

        this.carNoCarClassMap.put("그", "56");
        this.carNoCarClassMap.put("느", "57");
        this.carNoCarClassMap.put("드", "58");
        this.carNoCarClassMap.put("르", "59");
        this.carNoCarClassMap.put("므", "60");
        this.carNoCarClassMap.put("브", "61");
        this.carNoCarClassMap.put("스", "62");
        this.carNoCarClassMap.put("으", "63");
        this.carNoCarClassMap.put("즈", "64");
        this.carNoCarClassMap.put("츠", "65");
        this.carNoCarClassMap.put("크", "66");
        this.carNoCarClassMap.put("트", "67");
        this.carNoCarClassMap.put("프", "68");
        this.carNoCarClassMap.put("흐", "69");

        this.carNoCarClassMap.put("기", "70");
        this.carNoCarClassMap.put("니", "71");
        this.carNoCarClassMap.put("디", "72");
        this.carNoCarClassMap.put("리", "73");
        this.carNoCarClassMap.put("미", "74");
        this.carNoCarClassMap.put("비", "75");
        this.carNoCarClassMap.put("시", "76");
        this.carNoCarClassMap.put("이", "77");
        this.carNoCarClassMap.put("지", "78");
        this.carNoCarClassMap.put("치", "79");
        this.carNoCarClassMap.put("키", "80");
        this.carNoCarClassMap.put("티", "81");
        this.carNoCarClassMap.put("피", "82");
        this.carNoCarClassMap.put("히", "83");

        this.carNoCarClassMap.put("육", "84");
        this.carNoCarClassMap.put("해", "85");
        this.carNoCarClassMap.put("공", "86");
        this.carNoCarClassMap.put("국", "87");
        this.carNoCarClassMap.put("합", "88");
        this.carNoCarClassMap.put("-", "89");
        this.carNoCarClassMap.put("배", "90");
        this.carNoCarClassMap.put("임", "97");
        this.carNoCarClassMap.put("외", "98");
        
        this.carNoCarClassMap.put("XX", "DD");
    }

    public static void  main(String[] args) {

        CarBcdConv conv = new CarBcdConv ();

        String target = "임7584";

//        target = "서울57XX1X44";
//        target = "부산14-8232";
//        target = "33호71X9";
//        target = "XXXXXXX";

        System.out.println(" org -> " + target);

        byte [] kkk = conv.convCarNoBcdFormat( target );

        System.out.println (" org2 -> " + UtilExt.print( kkk ));

        String revkkk = conv.convBcdFormatToCarNo(kkk);
        System.out.println ( " conv -> " + revkkk );

        // 02 31 05 24 76
        // kkk[0] = 0x42; kkk[1] = 0x31; kkk[2] = 0x05; kkk[3] = 0x24; kkk[4] = 0x76;

        byte [] kkk2 = conv.convCarNoBcdFormat( revkkk );
        System.out.println (" conv -> " + UtilExt.print( kkk2 ));

        if(revkkk.contains("X")) {
            // BCD변환이 제대로 안되면 BL/PL 조회하지 않고 바로 데이터 없음으로 리턴
        }
        else {
            //BLPL 조회
        }
    }

}
