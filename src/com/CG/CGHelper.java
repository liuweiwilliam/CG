package com.CG;

import java.io.File;

final public class CGHelper {
	/*
	 * first, this function will backword move headto and tailto to remove space and \n,
	 * second it will forward move tailfrom to remove space and \n. then, split from headfrom to headto(include headto), 
	 * and from tailfrom to tailto(include tailto). at last, insert insert string after headto position. 
	 * e.g
	 * str="a[b]cd[e]fg", if you want to remove string "[a]" and "[e]" from variable str, you need call this function as follow
	 * divideHeadTail(str, "", 1, 3, 6, 8)
	 */
	static String divideHeadTail(String str, String insert, int headfrom, int headto, int tailfrom, int tailto) {
		// adjust headfrom position to remove \n and space
		if (headfrom>=0 && headfrom<=headto && headfrom<str.length()) {
			for (int i=headfrom-1; i>=0; i--) {
				if ((str.charAt(i) == ' ') || (str.charAt(i) == '\t')) continue;
				if (str.charAt(i) == '\n') {
					headfrom = i+1;
					break;
				}
				headfrom = i+1;
				break;
			}
		}
		
		// adjust headto position to remove \n and space
		if (headto>=headfrom && headfrom<=tailfrom && tailfrom<str.length()) {
			for (int i=headto+1; i<=tailfrom; i++) {
				if ((str.charAt(i) == ' ') || (str.charAt(i) == '\t')) continue;
				if (str.charAt(i) == '\n') {
					headto = i+1;
					break;
				}
				headto = i;
				break;
			}
		}

		// adjust tailfrom position to remove \n and space
		if (tailfrom>=headto && tailfrom<=tailto && tailfrom<str.length()) {
			for (int i=tailfrom-1; i>=headto; i--) {
				if ((str.charAt(i) == ' ') || (str.charAt(i) == '\t')) continue;
				if (str.charAt(i) == '\n') {
					tailfrom = i+1;
					break;
				}
				tailfrom = i+1;
				break;
			}
		}
		
		// adjust tailto position to remove \n and pace
		if (tailto>=tailfrom && tailto<str.length()) {
			if (tailto<str.length()) {
				for (int i=tailto+1; i<str.length(); i++) {
					if ((str.charAt(i) == ' ') || (str.charAt(i) == '\t')) continue;
					if ((str.charAt(i) == '\n')) {
						tailto = i+1;
						break;
					}
					tailto = i;
					break;
				}
			}
		}

		String head = "";
		String median = str.substring(headto, tailfrom);
		String tail = "";
		
		if (headfrom>=0) {
			head = str.substring(0, headfrom)+insert;
		}
		if (tailfrom<str.length()) {
			tail = str.substring(tailto, str.length());
		}
		
		return head+median+tail;
	}

	// use logger object add a log record
	static void log(boolean condition, CGLogger logger, String info) throws Exception {
		if (condition) {
			System.out.println(info);
		} else {
			logger.addLogInfo(info);
		}
	}
	
	// use logger object add a log record
	static void log(boolean condition, CGLogger logger, String print, String log) throws Exception {
		if (condition) {
			System.out.println(print);
		} else {
			logger.addLogInfo(log);
		}
		
		if (print.indexOf("program abort !!!") >= 0) System.exit(1);
	}
	
	static void mkdir(String filepath) {
		/*
		int index = 1;
		String path;
		while ((index=filepath.indexOf(fileSeparator(), index)) > 0) {
			path = filepath.substring(0, index);
			File dir = new File(path);
            if (!dir.exists()) dir.mkdir();
            index += 1;
		}
		*/
		
		int loc = filepath.lastIndexOf(fileSeparator());
		if (filepath.substring(loc+1).contains(".")) {
			new File(filepath.substring(0, loc)).mkdirs();
		} else {
			new File(filepath).mkdirs();
		}
	}
	
	public static String homeDirectory() {
		return System.getProperty("user.dir");
	}
	
	public static String fileSeparator() {
		return System.getProperty("file.separator");
	}
}
