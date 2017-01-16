/**
 * 
 */
package test;

import java.io.IOException;

import app.AppConfig;
import output.CjegTrace;
import tool.AppUtil;
import tool.Log;
import core.CrJourneyCluster;
import core.Edge;

/**
 ** @author yfhuang created at 2014-3-20
 */
public class TestCjegReadAtTime {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		// parameter set
		long starttime = 1920;
		int source=14;
		int destination=29;
		
		
		Edge e = new Edge(source, destination);
		AppConfig testconfig=AppConfig.getInstance();
		CrJourneyCluster crjc;
		String tracedir=AppUtil.getTracedir(testconfig.getTracename(),testconfig.getTau());
		CjegTrace cjegtrace = new CjegTrace(testconfig.getStorename(),
				tracedir, -1);
		cjegtrace.reader_open();
		crjc = cjegtrace.read(starttime, e);
		if (crjc == null) {
			Log.writeln("there's no journey at time:" + starttime, 2);
		} else {
			Log.write("stime:" + crjc.getStime(), 2000);
			Log.write(" etime:" + crjc.getEtime(), 2000);
			Log.writeln(" journey is:"
					+ crjc.getRepresentative_crjourney().toDbString(), 2000);
		}
		cjegtrace.reader_close();
	}

}
