package com.CG;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;

/*
 * use this class to handle log
 */
public class CGLogger {
	private String logfile; // log file path
	
	CGLogger(String logfile) throws Exception {
		this.setLogfile(logfile);
		
		File f = new File(this.getLogfile());
        if (!f.exists()) {
        		CGHelper.mkdir(this.getLogfile());
            f.createNewFile();
        }
	}
	
	void setLogfile(String logfile) { this.logfile = logfile; }
	String getLogfile() { return this.logfile; }
	
	// add log a new record
	void addLogInfo(String ct) throws Exception {
		String nowstr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis());
		
		FileWriter fw = new FileWriter(new File(this.getLogfile()), true);
		PrintWriter pw = new PrintWriter(fw);
		pw.println(nowstr+" "+ct);
		fw.flush();
		pw.close();
		fw.close();
	}
}
