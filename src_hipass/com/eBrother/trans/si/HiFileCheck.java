package com.eBrother.trans.si;

import com.eBrother.app.main.FileTest;
import com.eBrother.util.FileUtil;
import com.eBrother.util.Util;
import com.eBrother.util.UtilExt;
import com.eBrother.util.eBrotherUtil;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class HiFileCheck {

	private static String _eBroterLogTransClientVersion = "ASP LogTrans Client by CCMedia Technology Company, Version: 1.00.20070808.01.REAL";
	public static final String FILE_SEPARATOR = System.getProperty("file.separator");
	private static Logger _log = Logger.getLogger(HiFileCheck.class.getName());
	
	boolean _istest = false;
	public FileTest _ft = null;	
	
	
	String [] _args;
	public HiFileCheck( ) {

	}	
	
	void set_args (String[] args) {
		
		_args = args;
		
	}

	public void set_pattern ( String szpatternfile ) {
	
		
		_log.info("Version:" + _eBroterLogTransClientVersion);

		_ft = FileTest.getInstace();
		
		_ft.set_partterntype( 1 );
		
		_ft.setPattern( szpatternfile );
		
	}	

	private String GetPatternCheck ( String line, String key ) {
		
		String ret;
		String input = "\"" + key + "\"(.*?):(.*?)\"(.*?)\"";
		
		Pattern pattern = null;
		Matcher matcher = null;

		pattern = Pattern.compile(  input );
		
    	matcher = pattern.matcher( line );
    	
    	if ( matcher.matches()) {
    		 ret = 	(String)matcher.group(3);
    	}
    	else ret = null;
    	
    	return ret;
	}
	
	
	public void run_core ( ) {
	
		String indir;
		String outdir;
		String filter, szouterr;
		String runopt, extraopt;
		Vector<String>vFileL;

		vFileL = new Vector<String> ();
		
		try {	

			boolean bret;
			int ntargetfile;
			runopt = _args [1];
			filter = _args [2];
			indir = _args [3];
			outdir = _args [4];
			extraopt = _args [5];
			
			_log.info( "Parser Run option  : " + runopt + " - " + filter + " - " + extraopt );
			_log.info( "Parser Target Scan : " + indir );
			_log.info( "Parser Target Out  : " + outdir );

			
			File file = null;
			BufferedReader reader = null;
			BufferedWriter bw = null;
			String szfile, szoutfile;

			JSONParser _jsonparser = new JSONParser();

			bret = Util.getFileListCore ( vFileL, true, indir, "" );

			ntargetfile = vFileL.size();
			_log.info( "Parser Target Scan : " + ntargetfile + " - " + indir );

			szouterr = outdir + FILE_SEPARATOR + filter + ".err";

			for ( int i = 0; i < ntargetfile; i++ ) {
				
				szfile = vFileL.get(i);

				if ( filter.length() > 0 &&  szfile.indexOf( filter) < 0 ) {
					_log.trace( "Parser Target Skip : " + szfile + " - " + indir + ", " + filter);
					continue;
				}
				
				if ( szfile.indexOf(".run") >= 0 ) continue;
				
				szoutfile = outdir + FILE_SEPARATOR + FileUtil.getbasename( szfile);

				bw = UtilExt.getFileWriter(szoutfile);
				String line;
				int nreadline = 0;
				file = new File(szfile);
				String sztemp;
				int nrawdidx = 0;

				try {
					
					reader = new BufferedReader(new InputStreamReader(new FileInputStream(szfile), "EUC-KR"));

					boolean bquery = false;
					
					while(true) {

						try {

							line = reader.readLine();

							if(line == null) break;
							
							HiTransHeader hiheader = new HiTransHeader ();

							boolean jsoncheckresult = false;
							JSONObject jsonObject = null;
							try {
								jsonObject = (JSONObject) _jsonparser.parse(line);
								jsoncheckresult = true; 
							}
							catch ( Exception e ) {
								jsoncheckresult = false;
							}
							
							if ( jsoncheckresult ) {
								
								hiheader.setMSG_TYPE( (String ) jsonObject.get(HiHeaderConst.MSG_TYPE));
								hiheader.setDATA_TYPE( (String ) jsonObject.get(HiHeaderConst.DATA_TYPE));
								hiheader.setSYS_TYPE( (String ) jsonObject.get(HiHeaderConst.SYS_TYPE));
								hiheader.setSEND_TIME( (String ) jsonObject.get(HiHeaderConst.SEND_TIME));
								hiheader.setIC_CODE( (String ) jsonObject.get(HiHeaderConst.IC_CODE));
								hiheader.setLANE_NO( (String ) jsonObject.get(HiHeaderConst.LANE_NO));
								hiheader.setBD_NAME( (String ) jsonObject.get(HiHeaderConst.BD_NAME));
								hiheader.setMAKER_NAME( (String ) jsonObject.get(HiHeaderConst.MAKER_NAME));
								hiheader.setINTERFACE_VERSION( (String ) jsonObject.get(HiHeaderConst.INTERFACE_VERSION));
							} else {
								
								hiheader.setMSG_TYPE( GetPatternCheck( line, HiHeaderConst.MSG_TYPE));
								hiheader.setDATA_TYPE( GetPatternCheck( line, HiHeaderConst.DATA_TYPE));
								hiheader.setSYS_TYPE( GetPatternCheck( line, HiHeaderConst.SYS_TYPE));
								hiheader.setSEND_TIME( GetPatternCheck( line, HiHeaderConst.SEND_TIME));
								hiheader.setIC_CODE( GetPatternCheck( line, HiHeaderConst.IC_CODE));
								hiheader.setLANE_NO( GetPatternCheck( line, HiHeaderConst.LANE_NO));
								hiheader.setBD_NAME( GetPatternCheck( line, HiHeaderConst.BD_NAME));
								hiheader.setMAKER_NAME( GetPatternCheck( line, HiHeaderConst.MAKER_NAME));
								hiheader.setINTERFACE_VERSION( GetPatternCheck( line, HiHeaderConst.INTERFACE_VERSION));
							}
							
							StringBuffer buf = new StringBuffer();

							buf.append( hiheader.getIC_CODE());
							buf.append( ".");
							buf.append( hiheader.getLANE_NO());
							buf.append( ".");
							buf.append( hiheader.getBD_NAME());
							
							String hash_data = buf.toString();
						
							ObjectMapper mapper = new ObjectMapper();
							
							// FIL_yyyyymmddhhmiss_ 서버그룹코드_서버ID_인스턴스ID.log
							bw = UtilExt.getFileWriter(szoutfile);
							UtilExt.createFile( szoutfile + ".run");
							
							if ( line.indexOf("\"_raw\"") >= 0 ) {
								
								nrawdidx = 2;
								continue;
							}
							
							sztemp = eBrotherUtil.getDelimitData( line, "\"", 1 );
							
							if ( sztemp.length() > 0 ) continue;
							
							sztemp = eBrotherUtil.getDelimitData( line, "\"", nrawdidx );
							bw.write(sztemp);
							bw.write("\n");
							
						}
						catch (Exception e_file) {
							
						}
					}
				}
				catch ( Exception e_) {
					
					
				}
				
				FileUtil.deleteFile( szoutfile + ".run");
				if ( reader != null ) {
					try { reader.close(); }	catch ( Exception e_cl) { }
				}
				if ( bw != null ) {
					try { bw.close(); }	catch ( Exception e_cl) { }
				}

			}
			
		}
		catch ( Exception e ) {
			e.printStackTrace();
		}
		
	}

	
	
	public static void main (String[] args) {
		
		HiFileCheck ltc = new HiFileCheck ();
		
		String strCurHour = eBrotherUtil.getY2K_CurFullDate ();

		ltc.set_args( args );
		
	}
	
}


