package com.eBrother.trans.image;


import com.eBrother.trans.image.model.PlResponse;
import com.eBrother.trans.image.model.TransType;
import com.eBrother.trans.image.model.CacheJnaItem;
import com.eBrother.trans.image.util.CarBcdConv;
import com.eBrother.trans.image.util.JnaMemory;
import com.eBrother.util.eBrotherUtil;
import com.sun.jna.*;
import com.sun.jna.win32.StdCallLibrary;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Vector;

public class ImageCacheJnaHelper implements Runnable {

    protected static Logger _log = Logger.getLogger(ImageCacheJnaHelper.class.getName());

    // 최종 현재 type 에 해당한 것 처리 ..
    // 이것을 기준으로 master 를 찾고, 나머지 해당 일자등을 찾아서 정리 ...
    // 5분마다 처리하는 것을 별도로 해야 할 것 같은데 ...
    Hashtable<TransType, Vector<CacheJnaItem>> _hTypeMaster = new Hashtable<TransType, Vector<CacheJnaItem>> ();

    // clear 할 대상 임시 저장 ...
    Hashtable<TransType, Vector<CacheJnaItem>> _hTypeClear = new Hashtable<TransType, Vector<CacheJnaItem>> ();

    Hashtable<String, CacheJnaItem> _cache = new Hashtable<String, CacheJnaItem> ();

    boolean _isReady = false;

    static private CarBcdConv _carBcdHelper = CarBcdConv.getInstance();
    private String  _targetDir = "";
    private String  _backupDir = "";
    CacheJnaItem _cacheItem;
    private static final int KEY_LENGTH = 5;

    static ImageCacheJnaHelper _me_ = null;

    static public ImageCacheJnaHelper getInstance ( ) {

        if ( _me_ == null ) {

            _me_ = new ImageCacheJnaHelper ( "", "" );
        }
        return _me_;

    }

    public boolean isReady () {
        return _isReady;
    }

    /*
    * scan 할 directory 를 설정
     */
    public ImageCacheJnaHelper(String targetDir, String backupDir  ) {

        this._targetDir = targetDir;
        this._backupDir = backupDir;
        this._me_ = this;
    }

    /*
    * scan 할 direcrory 를 설정
     */
    public void setDir ( String targetDir, String backupDir ) {

        this._targetDir = targetDir;
        this._backupDir = backupDir;

    }

    /*
    * 파일이름이로 처리 type 확인
     */
    static public TransType getTransMainType ( String src) {

        if ( src.indexOf("MTB") >= 0 ) return TransType.BL;
        else if ( src.indexOf("SKP") >= 0 ) return TransType.PL;
        else if ( src.indexOf("STP") >= 0 ) return TransType.STP;
        else if ( src.indexOf("CSP") >= 0 ) return TransType.CS;

        else return null;
    }

    /*
    * sub type 확인 : STP ( 구 PL ) - 변경분없이 모두 master 라고
     */
    static public TransType getTransSubType ( String src) {

        if ( src.indexOf("MTBM") >= 0 ) return TransType.BL_MASTER;
        else if ( src.indexOf("MTBC") >= 0 ) return TransType.BL_UPDATE;
        else if ( src.indexOf("SKPM") >= 0 ) return TransType.PL_MASTER;
        else if ( src.indexOf("SKPC") >= 0 ) return TransType.PL_MASTER;
        else if ( src.indexOf("STPM") >= 0 ) return TransType.STP_MASTER;
        else if ( src.indexOf("STPC") >= 0 ) return TransType.STP_UPDATE;
        else if ( src.indexOf("CSPM") >= 0 ) return TransType.CS_MASTER;
        else if ( src.indexOf("CSPC") >= 0 ) return TransType.CS_UPDATE;
        else return null;
    }

    /*
    * 파일이름에서 전송일자 찾아냄
     */
    static public String getTransYmd ( String src ) {

        if ( src.length() < 5 ) return null;
        else return src.substring( 5);

    }

    public interface CLibrary extends Library {

        CLibrary INSTANCE = (CLibrary)Native.load((Platform.isWindows() ? "msvcrt" : "c"), CLibrary.class);

        void memcpy(Pointer des, byte[] src, int len);
        Pointer bsearch(byte[] key, Pointer base, int nitems, int size, User32.ebCache_Compare fn);
        void qsort ( Pointer base, int nitems, int size, User32.ebCache_Compare fn);

    }

    public interface User32 extends StdCallLibrary {
        interface ebCache_Compare extends Callback   {
            int invoke(Pointer arg1, Pointer arg2);
        }
    }

    User32.ebCache_Compare fn = new User32.ebCache_Compare() {

        public int invoke(Pointer arg1, Pointer arg2) {

            byte[] d1 = arg1.getByteArray(0, 5);
            byte [] d2 = arg2.getByteArray(0, 5);

            int i1 = d1.length;
            int i2 = d2.length;
            int cc;
            int ret = 0;
            if ( i1 < i2 ) cc = i1;
            else cc = i2;
            for ( int i = 0; i < cc; i++ ) {
                if ( d1[i] < d2[i]) ret = -1;
                else if ( d1[i] > d2[i] ) ret = 1;
            }
            if ( ret == 0 && i1 < i2 ) ret = -1;
            else if ( ret == 0 && i1 > i2 ) ret = 1;
            else if ( ret == 0 ) ret = 0;
            return ret;
        }
    };

    private static void _showMemInfo() {
        long used = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        _log.info("used memory is " + (used/1024/1024) + "M bytes");
    }

    /*
    * 특정 target file 을 읽어서 memory 에 update
    * file 은 EUC-KR 임 : 나중에 변경되면, env 로 처리
     */
    void run_core_target ( String targetFile ) {

        JnaMemory bufPoint = null;
        BufferedReader br = null;

        try {

            File file = new File(targetFile);

            br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "euc-kr"));

            // memory 에 할당할 최대 size 를 구함 ...
            long offset = 0;
            int maxLenCol = 0;
            int maxRow = 0;

            while (true) {

                String line = br.readLine();
                if (null == line) break;
                // 마지막에 구분자 한번 더 넣자 ... 처리를 편하게 하기 위해서임.
                line = line + "|";
                byte [] bdata = line.getBytes( "EUC-KR");
                byte [] bcdCarNo = null;

                // 첫자리 : 항상 차량번호임.
                try {
                    bcdCarNo = _carBcdHelper.convCarNoBcdFormat(eBrotherUtil.getDelimitData(line, "|", 1));
                }
                catch ( Exception e ) {
                    // bcd format 에러는 cache 에서 제외함.
                    _log.error ( "BCD Convert Err : " + line );
                    continue;
                }

                if ( bcdCarNo == null || ( bcdCarNo [ 0] == 0x00 && bcdCarNo [ 1] == 0x00 && bcdCarNo [ 2] == 0x00 && bcdCarNo [ 3] == 0x00 && bcdCarNo [ 4] == 0x00  )) continue;
                if (maxLenCol < bdata.length) maxLenCol = bdata.length;
                maxRow++;

            }

            br.close();

            // 구분자 + 차량번호 Bcd 변환 5자리 제일 앞에 무조건 추가 !!!
            maxLenCol += 6;

            //////// MEMORY 할당
            // _log.info ( "파일 memory 적재 전: " + targetFile);
            // _showMemInfo ();
            bufPoint = new JnaMemory( maxLenCol *  maxRow);
            _log.info ( "파일 memory 적재 후: row - " + maxRow + " - " + targetFile );
            _showMemInfo ();

            // 00으로 채울 것들 미리 설정.
            byte [] zeroBytes = new byte [ maxLenCol ];
            for ( int i = 0; i < maxLenCol; i++ ) {
                if ( i == 5 ) zeroBytes [i] = (byte) 0x7c;
                else zeroBytes [i] = (byte) 0;

            }

            br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "euc-kr"));

            int nline = 0;
            while (true) {

                String line = br.readLine();
                if (null == line) break;

                // test 목적 ...
//                if ( line.indexOf ( "01가0147") >= 0 ) {
//                    _log.info ( line );
//                }

                byte [] readData = (line + "|" ).getBytes( "EUC-KR");

                byte [] bcdCarNo = null;

                try {
                    bcdCarNo = _carBcdHelper.convCarNoBcdFormat(eBrotherUtil.getDelimitData(line, "|", 1));
                }
                catch ( Exception e ) {
//                    _log.info ( "BCD Convert Err : " + line );
                    continue;
                }
                if ( bcdCarNo == null || ( bcdCarNo [ 0] == 0x00 && bcdCarNo [ 1] == 0x00 && bcdCarNo [ 2] == 0x00 && bcdCarNo [ 3] == 0x00 && bcdCarNo [ 4] == 0x00  )) continue;

                // memory 에 복사
                // memroy 적재할 때, 차량번호 BCD format 으로 데이터 읽어 내야 하기 때문에 5 byte 추가함.
                offset = nline * maxLenCol;
                nline++;

                CLibrary.INSTANCE.memcpy( bufPoint.share(offset), zeroBytes, zeroBytes.length);
                CLibrary.INSTANCE.memcpy( bufPoint.share(offset), bcdCarNo, 5 );
                CLibrary.INSTANCE.memcpy( bufPoint.share(offset + 6), readData, readData.length );

            }

            // 등록된 정보에 대해 sorting 수행. 생각보다 시간이 더 걸린다 ...

            CLibrary.INSTANCE.qsort( bufPoint, maxRow, maxLenCol, fn );

            CacheJnaItem cacheItem = new CacheJnaItem();

            // 그냥 test ...
            this._cacheItem = cacheItem;

            cacheItem.setBufPoint( bufPoint );
            cacheItem.setDataFile( targetFile);
            cacheItem.setNumRow( maxRow );
            cacheItem.setSizeCol( maxLenCol );
            cacheItem.setLastModified( file.lastModified() );
            cacheItem.setTransSubType( getTransSubType( file.getName()));
            cacheItem.setTransMainType( getTransMainType( file.getName()));
            cacheItem.setTransYmd( getTransYmd( file.getName() ));

            if ( cacheItem.getTransSubType() == TransType.PL_MASTER
                    || cacheItem.getTransSubType() == TransType.STP_MASTER
                    || cacheItem.getTransSubType() == TransType.BL_MASTER
                    || cacheItem.getTransSubType() == TransType.CS_MASTER ) {

                // master 이면 기존 데이터를 삭제하기 위해서, Vector를 신규로 생성함.
                Vector<CacheJnaItem> items = new Vector<CacheJnaItem> ();
                items.add ( cacheItem);

                // 기존에 master 가 존재하면, 삭제할 hash 에 정보를 저장함.
                // 아래 처리 완료 후 cache 를 삭제할 것임.
                if ( _hTypeMaster.containsKey( cacheItem.getTransMainType())) {
                    _hTypeClear.put ( cacheItem.getTransMainType(), _hTypeMaster.get( cacheItem.getTransMainType()));
                }
                _hTypeMaster.put ( cacheItem.getTransMainType(), items );

            }
            else {

                Vector<CacheJnaItem> items = _hTypeMaster.get ( cacheItem.getTransMainType());

                if ( items == null) {

                    _log.info ( "Error --> 해당 file 에 대한 master 파일을 data dir 로 옮겨 놔 주셔야 처리됩니다 - " + targetFile );

                    // 발생 가능성은 :
                    // 1) 특정일자 master 읽고 cache. 2) cache 된 file 을 backup 으로 이동. 3) engine restart
                    // 4) 신규 갱신 파일 확인 5) master 가 없음에 따라 .. error 발생.
                    // 어떻게 해야 하나 ?? -> 실행 전에 backup file 에서 최종일자 master 를 읽어서 등록 해야 함.
//                  items = new Vector<CacheJnaItem> ();
//                  _hTypeMaster.put ( cacheItem.getTransMainType(), items );
                }
                else items.add ( cacheItem);
            }

            _cache.put ( targetFile, cacheItem );

        }
        catch ( Exception e ) {
            _log.info ( "Error --> " + targetFile);
            e.printStackTrace();
        }

    }

    /*
    * 특정 차량번호에 대한 상세 정보 획
     */
    public byte [] get( CacheJnaItem item, String carNo ) {

        byte [] arrKey = _carBcdHelper.convCarNoBcdFormat ( carNo );
        return get_core ( item, arrKey );

    }

    /*
    * 실제 처리 부
     */
    public byte [] get_core ( CacheJnaItem item, byte [] arrKey ) {

        Pointer retval = CLibrary.INSTANCE.bsearch(arrKey, item.getBufPoint(), item.getNumRow(), item.getSizeCol(), fn);
        if (null == retval) return null;

        // 원래 처리 데이터 앞에 도공 BCD format 으로 차량번호를 5 byte 넣고 "," 를 넣음
        // 길이가 6 byte 됨에 따라, 획득한 자료에서 앞에 6 byte 를 제외해야 함.
        byte[] retdate = new byte[ item.getSizeCol() - 6 ];

        try {
            System.arraycopy(retval.getByteArray(6, item.getSizeCol() - 6), 0, retdate, 0, item.getSizeCol() -6 );
        }
        catch ( Exception e2 ) {
            e2.printStackTrace();
        }
        return retdate;

    }

    public void prtImageCacheInfo ( ) {

        _hTypeMaster.forEach(( transType, items) -> {

            for (int i = items.size() - 1; i >= 0; i--) {

                CacheJnaItem item = items.get( i);

                _log.info ( "메모리적재파일 : main type - " + item.getTransMainType()
                        + ", subtype - " + item.getTransSubType()
                        + ", file = " +  item.getDataFile());
            };
        });
    }

    public void setImageTransInfo ( PlResponse resp ) {

        // items : 특정 코드에 대해 vector 를 보유하고 있음.
        // 해당 vector 에 최근 것부터 찾아야 함.
        _hTypeMaster.forEach(( transType, items) -> {

            // master : 0, 1 차 변경분 : 1. 2차 변경분 : 2 ...
            for (int i = items.size() - 1; i >= 0; i--) {

                byte [] iteminfo = get_core (items.get(i), resp.getCarBcdNo());

                if ( iteminfo != null  ) {

                    try {

                        String k = new String(iteminfo, "euc-kr");

                        if (k != null) {

                            if ( transType == TransType.BL ) {
                                _log.info( k + " - bl search result");
                                resp.setBlData( k );
                            }
                            else if ( transType == TransType.PL ) {
                                _log.info( k + " - pl search result");
                                resp.setPlData( k );
                            }
                            else if ( transType == TransType.STP ) {
                                _log.info( k + " - stpl search result");
                                resp.setStplData( k );
                            }
                            else if ( transType == TransType.CS ) {
                                _log.info( k + " - master search result");
                                resp.setCsData( k );
                            }
                            break;
                        }
                    } catch (Exception e) {

                    }
                }
            }
        });


    }


    public void run_core( String targetDir ) {

        String s2;
        String s3;
        String sztemp;

        try {

            _showMemInfo ();

            // directory scan & call each one ...
            File file = new File( targetDir );
            File files[] = file.listFiles();

            if ( files == null ) return;

            // 일단 sorting 수행 .. 파일명 중, 시간부분으로 처리함.
            // SKPM202007010602 <-- 앞에 4 자리 처리코드, 뒷자리 : 생성시각
            Arrays.sort(files, new Comparator<File>(){

                public int compare(File f1, File f2) {

                    String s1, s2;
                    if ( f1.getName().length() >= 5 ) s1 = f1.getName().substring(5);
                    else s1 = f1.getName();

                    if ( f2.getName().length() >= 5 ) s2 = f2.getName().substring(5);
                    else s2 = f2.getName();
                    return s1.compareTo( s2 );

//                    return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());

                }
            } );

            String szYMD = eBrotherUtil.getY2KCurDate();

            // for global date setting ... we should not use this way. may has bug.
            // m_szRunDate = szYMD;

            int _ncnt_hashmap = 0;

            // 삭제 대상 적재 - 초기화
            _hTypeClear.clear();

            for( int j = 0; j < files.length; j++) {

                File fcur = files[j];
                s3= fcur.getAbsolutePath();

                if( s3.indexOf("lost+found") >= 0  || files[j].isDirectory() || s3.indexOf("tmp") >= 0 || s3.indexOf("TMP") >= 0 ) continue;

                CacheJnaItem cache = _cache.get ( fcur.getAbsolutePath());

                if ( cache != null && cache.getLastModified () >= fcur.lastModified() ) {
                    _log.debug ( "target file : 기존파일. 수정하지 않음");
                    continue;
                }

                _log.info ( "cache file start : " + fcur.getAbsolutePath() + " = " + fcur.lastModified() );

                TransType transMainType = getTransMainType( fcur.getName());
                TransType transSubType = getTransSubType( fcur.getName());

                // sub type 일 때 반듯이 master 가 먼저 있는지 확인해야 한다.
                // 한편, master 보다 파일시각이 작으면 skip 한다.
                // master 가 여러개 일수 있는 확률은 거의 없는데... 이런 경우는 시간차대로 처리한다. 올라 오는데 시간이 걸리겠지만 ..
                // 해당 처리는 사실 운영에서 신규로 올리는 분이 정리해 주어야 한다. 관리 point 로 남겨 둔다.
                if ( transSubType == TransType.BL_UPDATE || transSubType == TransType.PL_UPDATE || transSubType == TransType.STP_UPDATE || transSubType == TransType.CS_UPDATE ) {

                    Vector<CacheJnaItem> items = _hTypeMaster.get ( transMainType );
                    if ( items == null ) {
                        _log.error ( "해당 파일에 대한 master 파일이 있어야 합니다. 확인해 주셔요 :" + s3 );
                        continue;
                    }

                    // master ...
                    // MTBC202007011620
                    CacheJnaItem item = items.get(0);
                    if ( item.getDataFile().substring( item.getDataFile().length() - 12 ).compareTo(fcur.getName().substring( 4)) > 0 ) {
                        _log.error ( "master 파일보다 시점이 이름. skip 함 : 현재 master 파일 - " + item.getDataFile() + ", 대상 파일 - " + s3  );
                        continue;
                    }

                }

                run_core_target ( s3 );

                try {
                    // 파일을 backup 으로 옮김. 삭제를 해야 하는지 여부는 이후에 검토 !!!
                    boolean isMoved = fcur.renameTo(new File(this._backupDir + "/" + fcur.getName()));
                    if (!isMoved) {
                        _log.error("target file move err : " + this._backupDir + "/" + fcur.getName());
                    }
                }
                catch ( Exception e2 ) {

                }
                _log.info ( "cache file end : " + fcur.getAbsolutePath() + " = " + fcur.lastModified() );


            }

            ////////////////////////////////////////////////////////////////
            // 삭제 대상 : cache 에서 삭제 처리 ...
            _hTypeClear.forEach(( transType, items) -> {

                // master : 0, 1 차 변경분 : 1. 2차 변경분 : 2 ...
                for (int i = items.size() - 1; i >= 0; i--) {

                    CacheJnaItem item = items.get(i);

                    // cache 에서 삭제함 ...
                    try {
                        if (_cache.containsKey(item.getDataFile())) {
                            _cache.remove(item.getDataFile());
                        }
                    }
                    catch ( Exception e2) {
                        _log.error( "Clear Memory Error : " + item.getDataFile());
                        item = null;
                    }

                    // memory free 및 vector 삭제 ...
                    try {
                        _showMemInfo ();
                        _log.info ( "Clear Memory : " + item.getDataFile());
                        JnaMemory mm = item.getBufPoint();
                        mm.dispose();
                        _showMemInfo ();
                    }
                    catch ( Exception e2) {
                        _log.error( "Clear Memory Error : " + item.getDataFile());
                        item = null;
                    }
                }

                items.clear();

            });

            _hTypeClear.clear();
            //////////////////////////////////////////////////////////////////////

        }
        catch ( Exception e ) {
            e.printStackTrace();
        }

        _showMemInfo ();
        //////////////////////////////////////////////////////////////////////////
    }

    @Override
    public synchronized void run() {

        try {

            _isReady = false;

            while ( true ) {

                // 2. setup data ...
                run_core(this._targetDir);

                _isReady = true;

                // directory check 는 10초 단위로 함 ...
                Thread.sleep(10000);

            }

        }
        catch ( Exception e ) {

        }

    }

    public static void  main(String[] args) {

        String targetDir  = "/Users/demo860/app/dev/hipass/data/SKPM202007010602";

        targetDir = "/Users/demo860/app/dev/hipass/data/";

        String backupDir = "/Users/demo860/app/dev/hipass/backup/";


        ImageCacheJnaHelper helper = new ImageCacheJnaHelper( targetDir, backupDir );

        // run using thread ...
        try {
            Thread kk = new Thread ( helper );
            kk.start();
        }
        catch ( Exception  e ) {

        }

        try {

            while ( true  ) {

                if ( helper.isReady () ) {

                    byte[] arrKey = _carBcdHelper.convCarNoBcdFormat("01가0147");

                    PlResponse resp = new PlResponse();
                    resp.setCarBcdNo(arrKey);
                    helper.setImageTransInfo ( resp );

                    byte[] arrKey1 = _carBcdHelper.convCarNoBcdFormat("01가0348");

                    resp = new PlResponse();
                    resp.setCarBcdNo(arrKey1);
                    helper.setImageTransInfo ( resp );
                }

                Thread.sleep(10000);

            }

        }
        catch ( Exception e ) {

        }

        // helper.setImageTransInfo ( resp );
        // String data = new String ( ret );
    }

}
