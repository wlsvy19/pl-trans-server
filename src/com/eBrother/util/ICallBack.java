package com.eBrother.util;

import java.util.Hashtable;

public interface ICallBack {
	void run_Pre ();
	void run_CallBack(String szline);
	void run_Post ();
	// void run_CallBackFile ( String p1, String p2, Hashtable<String, Object> hparam);
	boolean run_CallBack(String szline, Hashtable<String, Object> hparam);
	boolean run_CallBackFile (String szfile, String szoutdir, Hashtable<String, Object> hparam);	
}
