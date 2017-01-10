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
		long starttime = 32*60;
		int source=1;
		int destination=32;
		
		
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
			Log.write("stime:" + crjc.getStime(), 1000);
			Log.write(" etime:" + crjc.getEtime(), 1000);
			Log.writeln(" journey is:"
					+ crjc.getRepresentative_crjourney().toDbString(), 1000);
		}
		cjegtrace.reader_close();
	}

}
