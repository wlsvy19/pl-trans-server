package com.eBrother.app.main;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilePattern {
	
	public String key = "";
	public String szpattern = "";
	public Pattern cpattern;
	public Matcher cma;
	public int idx = 0;
	public String sep = "";
	public String tail = "";
	public String iformat;
	public String oformat;
	public boolean istail = false;
	public boolean hasdate = false;
	public boolean isdelete = false;
	public SimpleDateFormat idateformat = null;
	public SimpleDateFormat odateformat = null;
	
	// new appended.
	public String sztimepattern = "";
	public Pattern ctimepattern;
	public String sztimestamp;
	public SimpleDateFormat dformattimetstamp = null;
	public SimpleDateFormat dateformatin = null;
	public Locale m_datelocale = Locale.ENGLISH;
	
	public boolean issendreal = false;
	public String sz2ndtoken = "";
	public int cutsize = 0;
	
}
