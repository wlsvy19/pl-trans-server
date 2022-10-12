package com.eBrother.app.core;

import com.eBrother.app.main.FilePattern;

public interface IWorker extends Runnable {

	void run ();
	void init ( IWorkResult cmain, String [] args, String szrundate, String sztarget, FilePattern fpattern );
	void set_real ( boolean breal );
}
