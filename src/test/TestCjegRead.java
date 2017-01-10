package test;

import app.AppConfig;
import output.CjegTrace;
import tool.AppUtil;
import tool.Log;
import core.CrJourneyCluster;
import core.Edge;

/**
 * @author yfhuang created at 2014-3-19
 * 
 */
public class TestCjegRead {
	public static void main(String[] args) {
		int sourcenode_id=14;
		int sinknode_id=29;
		AppConfig testconfig=AppConfig.getInstance();
		try {
			CrJourneyCluster crjc;
			Edge edge=new Edge(sourcenode_id,sinknode_id);
			// -1 denote we will read
			String tracedir=AppUtil.getTracedir(testconfig.getTracename(),testconfig.getTau());
			CjegTrace cjegtrace = new CjegTrace(testconfig.getStorename(),
					tracedir, -1);
			cjegtrace.reader_open();
			int readed = 0;
			while ((crjc = cjegtrace.read(edge)) != null) {
				Log.writeln(crjc.toDbString(), 2000);
				readed++;
			}
			cjegtrace.reader_close();
			Log.writeln("\nwe readed "+readed+" critical journey clusters", 2000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
