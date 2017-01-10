package tool;

import app.AppConfig;

/**
 ** @author yfhuang created at 2014-2-26
 */
public class Log {
	private static int showlevel=AppConfig.getInstance().getLoglevel();
	
	public static void writeln(String logstr, int level) {

		/*
		 * showlevel 0 other information 
		 * 1 vectorclock information 
		 * 2 critical journey information 
		 * 3 continuecrjceventqueue
		 * 1000 release
		 * 2000 debug
		 */
		
		if (level == showlevel)
			System.out.println(logstr);
	}
	
	public static void write(String logstr, int level) {

		/*
		 * showlevel 0 other information 1 vectorclock information 2
		 * critical journey information
		 */

		
		if (level >= showlevel)
			System.out.print(logstr);
	}
}
