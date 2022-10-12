package com.eBrother.util;


import java.io.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;


/**
 *  Ini File Handler Class. <br>
 *  It's same of the ebUtil.cpp
  * @author		CCMEDIA KOREA
 * @since		2000.04
 * @version	3.0
**/
public class eBrotherIni extends Object
{
	// protected String        m_path = "c:\\eBrother\\etc\\eBrother.ini";
	// protected String        m_path = "/home/eBrother/etc/eBrother.ini";
	// protected String        m_path = "/data1/ebrother/etc/eBrother.ini";
	protected	String			m_path = ""; 
	protected	String			m_sIniFile;
	protected	Hashtable		m_mapSections;

	/**
	 * Constructor <br>
	 */
	public eBrotherIni ()
	{
		m_sIniFile = "";
		m_mapSections = new Hashtable(20);
	}
	

	/**
	 *  Open Ini File <br>
	 * @param String	Ini File
	 * @return boolean If success, return TRUE, otherwise false
	 */
	
	public boolean open(String sIniFile)
	{
		String		sLine;

		Hashtable	mapEntrys = null;
		String		sSection;
		String		sEntry;
		String		sValue;
		int			nIndex;

		close();
		
		m_sIniFile  = sIniFile;

		try {
			BufferedReader	in = new BufferedReader(new FileReader(sIniFile));

			while ((sLine=in.readLine()) != null) {
				sLine = sLine.trim();

				if (sLine.length() == 0)
					continue;

				// Comment
				if (sLine.charAt(0) == '#' || sLine.charAt(0) == ';')
					continue;

				// Section
				if (sLine.charAt(0) == '[') {
					if ((nIndex=sLine.indexOf(']')) == -1)
						sSection = sLine.substring(1);
					else
						sSection = sLine.substring(1, nIndex);

					mapEntrys = new Hashtable(2);

					// System.out.println("[" + sSection.trim() + "]");
					m_mapSections.put(sSection.trim().toLowerCase(), mapEntrys);
				}
				// Entry
				else {
					if ((nIndex=sLine.indexOf('=')) == -1) {
						sEntry = sLine.substring(0);
						sValue = "";
					}
					else {
						if (nIndex == 0)
							continue;

						sEntry = sLine.substring(0, nIndex);
						if (nIndex < sLine.length()-1)
							sValue = sLine.substring(nIndex+1);
						else
							sValue = "";
					}

					if (mapEntrys != null) {
						// System.out.println(sEntry.trim() + "=" + sValue.trim());
						mapEntrys.put(sEntry.trim().toLowerCase(), sValue.trim());
					}
				}
			}
			in.close();
		}
		catch (Exception e) {
			
			System.out.println( "[eBrotherIni]  Ini Fail " + sIniFile + "( "  + e + " )\n" );
			return false;
		}

		return true;
	}

	/**
	 * Close Ini File Handle. <br>
	 * It clears the Hashtable
	 */
	public void close()
	{
		Enumeration	eSectionValues;

		m_sIniFile = "";

		eSectionValues = m_mapSections.elements();
		while (eSectionValues.hasMoreElements())
			((Hashtable)eSectionValues.nextElement()).clear();

		m_mapSections.clear();
	}

	/**
	 * If we update, the Ini Info, then it writes to File
	 */
	public void flush()
	{
		Enumeration	eSectionKeys;
		Enumeration	eEntryKeys;
		Hashtable	mapEntrys;

		String		sSection;
		String		sEntry;
		String		sValue;

		if (m_sIniFile.length() == 0)
			return;

		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(m_sIniFile));

			// Section
			eSectionKeys = m_mapSections.keys();
			while (eSectionKeys.hasMoreElements()) {
				sSection = (String)eSectionKeys.nextElement();
				out.write("[" + sSection + "]"); out.newLine();
				// System.out.println("[" + sSection + "]");
				// Entry
				mapEntrys = (Hashtable)m_mapSections.get(sSection);
				eEntryKeys = mapEntrys.keys();
				while (eEntryKeys.hasMoreElements()) {
					sEntry = (String)eEntryKeys.nextElement();
					sValue = (String)mapEntrys.get(sEntry);
					out.write(sEntry + "=" + sValue); out.newLine();
					// System.out.println(sEntry + "=" + sValue);
				}
				out.newLine();
				// System.out.println("");
			}
			out.close();
		}
		catch (Exception e) {
			return;
		}

		return;
	}

	/**
	 * Get Section Names enumeration
	 * @return Enumeration	enumeration of the Section Key
	*/
	public Enumeration getSectionNames()
	{
		return m_mapSections.keys();
	}

	/**
	 * Get Entry Names enumeration of the Section
	 * @param String	section name
	 * @return Enumeration	enumeration of the Entry Names
	*/
	public Enumeration getEntryNames(String sSection)
	{
		Hashtable	mapEntrys;

		if ((mapEntrys=(Hashtable)m_mapSections.get(sSection.toLowerCase())) == null)
			return null;

		return mapEntrys.keys();
	}

	/**
	 * Get the Entry Value with default value
	 * @param String	section name
	 * @param String	entry name
	 * @param String	default value
	 * @return String return value
	 * @see #getInteger
	 * 
	 */
	public String getString(String sSection, String sEntry, String sDefault)
	{
		Hashtable	mapEntrys;
		String		sValue;

		if ((mapEntrys=(Hashtable)m_mapSections.get(sSection.toLowerCase())) == null)
			return sDefault;

		if ((sValue=(String)mapEntrys.get(sEntry.toLowerCase())) == null)
			return sDefault;

		return sValue;
	}

	/**
	 * Get the Entry Value with default value
	 * @param String	section name
	 * @param String	entry name
	 * @param int	default value
	 * @return int return value
	 * @see #getString
	 */
	public int getInteger(String sSection, String sEntry, int nDefault)
	{
		Hashtable	mapEntrys;
		String		sValue;
		int			nReturn;

		if ((mapEntrys=(Hashtable)m_mapSections.get(sSection.toLowerCase())) == null)
			return nDefault;

		if ((sValue=(String)mapEntrys.get(sEntry.toLowerCase())) == null)
			return nDefault;
		
		try{ nReturn = Integer.parseInt(sValue); } catch(Exception e){ nReturn = 0; }
		return nReturn;
	}

	/**
	 * Set the Entry Value
	 * @param String	section name
	 * @param String	entry name
	 * @param String	entry value
	 * @return boolean if success, it return true, otherwise return false.
	 * @see #setInteger
	 */
	public boolean setString(String sSection, String sEntry, String sValue)
	{
		Hashtable	mapEntrys;

		try {
			if ((mapEntrys=(Hashtable)m_mapSections.get(sSection.toLowerCase())) == null) {
				mapEntrys = new Hashtable(2);
				m_mapSections.put(sSection.toLowerCase(), mapEntrys);
			}
			mapEntrys.put(sEntry.toLowerCase(), sValue.trim());
		}
		catch (Exception e) {
			return false;
		}

		return true;
	}

	/**
	 * Set the Entry Value
	 * @param String	section name
	 * @param String	entry name
	 * @param int	entry value
	 * @return boolean if success, it return true, otherwise return false.
	 * @see #setInteger
	 */
	public boolean setInteger(String sSection, String sEntry, int nValue)
	{
		Hashtable	mapEntrys;

		try {
			if ((mapEntrys=(Hashtable)m_mapSections.get(sSection.toLowerCase())) == null) {
				mapEntrys = new Hashtable(2);
				m_mapSections.put(sSection.toLowerCase(), mapEntrys);
			}
			mapEntrys.put(sEntry.toLowerCase(), new Integer(nValue).toString());
		}
		catch (Exception e) {
			return false;
		}

		return true;
	}

	
  /**
	* Remove the Entry
	* @param String section name
	* @param String entry name
	*/
	public boolean removeEntry(String pSection, String pEntry)
	{
		Enumeration	eSectionKeys;
		Enumeration	eEntryKeys;
		Hashtable	mapEntrys;

		String		sSection;
		String		sEntry;
		String		sValue;
		try{
			eSectionKeys = m_mapSections.keys();
			while (eSectionKeys.hasMoreElements()) {
				sSection = (String)eSectionKeys.nextElement();
				if( sSection.equalsIgnoreCase(pSection) ){
					mapEntrys = (Hashtable)m_mapSections.get(sSection);
					eEntryKeys = mapEntrys.keys();
					while (eEntryKeys.hasMoreElements()) {
						sEntry = (String)eEntryKeys.nextElement();
						if( sEntry.equalsIgnoreCase(pEntry) ){
							mapEntrys.remove(sEntry);	
						}
					}
				}
			}
		}catch(Exception e){ return false; }
		return true;
	}
	
	public String extractSubString(String sFullString, int nIndex, int nDelimiter)
	{
		int			nBegin, nEnd;

		if (nIndex < 0)
			return "";

		try {
			nBegin = 0;
			nEnd = -1;
			while (nIndex >= 0) {
				nBegin = nEnd + 1;
				if ((nEnd=sFullString.indexOf(nDelimiter, nBegin)) == -1) {
					if (nIndex == 0)
						return sFullString.substring(nBegin);
					else
						return "";
				}

				nIndex--;
			}
			return sFullString.substring(nBegin, nEnd);
		}
		catch (Exception e) {
				return "";
		}
	}

	/**
	 * get the eBrother Ini File full Path name
	 * @return String	full path name with filename
	 */
    public String getPath()
    {

		try {
			if ( m_path == null || m_path.length () == 0 || m_path.equals ( "")   ) {
				m_path = getEbrotherIniFile ();
			}
		}
		catch ( Exception e ) { 
			System.out.println ( "[INI] Error " + e );
			m_path = "";
		}
       return m_path;
    }

	/** 
	 * set the ebrother ini file full path name
	 * @param String	ful path name with file name
	 */
    public void setPath(String path)
    {
        m_path = path;
    }
	
	/**
	 * get eBrotherIni Full path name with file name <br>
	 * It use CLASSPATH variable. <br>
	 * But, the WEBLOGIC does not use CLASSPATH. <br>
	 * So, We Must update it for useing /etc/ebrotherhome or registry value for WIN 2K
	 * @return String	full path name with file name
	 */
	public String getEbrotherIniFile ( )
	{
		String l_strPath = "", l_strValue= "";
		int	 l_intLib;
		boolean	bIsExist = false;
		try {
			
			// /etc/ebrotherhome(C:\\WINNT\\ebrotherhome) �� �������� �˻�
			if( !bIsExist ){
				String strHomeFile = "/etc/ebrotherhome";
				if (System.getProperty("os.name").toUpperCase().indexOf("WINDOWS") >= 0) strHomeFile = "C:\\WINNT\\ebrotherhome";
				if( IsFileExist(strHomeFile) ){
					try{
					    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(strHomeFile)));
						l_strValue = br.readLine();
						if( l_strValue != null ){ 
							l_strValue = l_strValue.trim();
							if( l_strValue.lastIndexOf(System.getProperty("file.separator")) != l_strValue.length()-1 )
								l_strValue += System.getProperty("file.separator");
							l_strValue += "etc" + System.getProperty("file.separator") + "eBrother.ini";
							bIsExist = IsFileExist ( l_strValue );
						}
					}catch(Exception e){
					    System.out.println("File Read Error : " + e);
					}
				}
			}
			
			//�ý��������� ã�ƺ��� 
			if( !bIsExist ){
				l_strValue = System.getProperty("ebrother.ini","");
				if( l_strValue == null ) l_strValue = "";
				if( !l_strValue.equals("") ){ bIsExist = IsFileExist( l_strValue ); }
			}
			
			//������ java class path�� ���� �˻�
			if( !bIsExist ){
				String   l_classPath = System.getProperty("java.class.path",""); 
				l_classPath += System.getProperty("weblogic.class.path","."); 
				l_classPath += System.getProperty("jvm.classpath","");
				StringTokenizer l_strPathSt = new StringTokenizer(l_classPath,  System.getProperty("path.separator") );
				while(l_strPathSt.hasMoreElements() ) {
					l_strPath = (String)l_strPathSt.nextElement();
					l_intLib = l_strPath.indexOf("lib");
					if ( l_intLib < 0 ) continue;
					l_strValue = l_strPath.substring(0,l_intLib);
					l_strValue += "etc" + System.getProperty("file.separator") + "eBrother.ini";
					bIsExist = IsFileExist ( l_strValue );
					if ( bIsExist ) break;
				}  //while                
			}
			// UPDATE 03/08/18
            // For GoodMorning Shinhan
            // There is no classpath. so we can't find it.
            if ( bIsExist == false ) {
                l_strValue = "/GTS/web/ebrother/etc/eBrother.ini";
                bIsExist = IsFileExist ( l_strValue );
            }
		}
		catch ( Exception e ) {
			l_strValue = "/export/home/ebrother/etc/eBrother.ini";		
		}
		
		return l_strValue;
		
	}

	/**
	 * Check Ini File Exist ? <br>
	 * It is at the eBrotherUtil. but we do not eBrtotherUtil for IsFileExist. <br>
	 * So, If there is need to update IsFileExist, please update eBrotherUtil too.
	 * @param String	FileName
	 * @return boolean
	 */
	public boolean IsFileExist ( String strFileName ) {

		File		objFileTemp;
		boolean bRet = false;
		
		// System.out.println ( "[eBrotherIni] File Check -> " + strFileName );
		try {
			objFileTemp = new File(  strFileName  );
		
			if ( objFileTemp.exists() ) bRet = true;
		}
		catch ( Exception e ) {
		
			System.out.println ( "[eBrotherIni] IsFileExist Error : " + e );
		}

		objFileTemp = null;

		return bRet;

	}
	
	/////////////////////////////////////////////////////////////////
	/************
	public static void main(String[] args)
	{
		eBrotherIni		ini = new eBrotherIni();

		Enumeration	eEntryNames;
		String			sEntryName;
		String			sEntryValue;
		System.out.println ( ini.getPath ());
		ini.open( ini.getPath () );

		// Get

		if ((eEntryNames=ini.getEntryNames("Cookie")) == null)	System.out.println("No entrys.\n");

		while (eEntryNames.hasMoreElements()) {
			sEntryName = (String)eEntryNames.nextElement();
			sEntryValue = ini.getString("Cookie", sEntryName, "");

			System.out.println(sEntryName);
			System.out.println("0 = " + ini.extractSubString(sEntryValue, 0, '|'));
			System.out.println("1 = " + ini.extractSubString(sEntryValue, 1, '|'));
			System.out.println("2 = " + ini.extractSubString(sEntryValue, 2, '|'));
			System.out.println("3 = " + ini.extractSubString(sEntryValue, 3, '|'));
			System.out.println("4 = " + ini.extractSubString(sEntryValue, 4, '|'));
		}

		ini.close();
	}
	********/
	
}
