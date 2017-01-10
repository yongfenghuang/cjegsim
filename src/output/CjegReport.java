package output;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import tool.Log;
import core.CrJourneyCluster;
import core.CrJourneyEvolvingGraph;
import core.Edge;
import core.Node;
import app.CjegApp;

/**
 * @author yfhuang created at 2014-3-13
 * 
 */
public class CjegReport {
	private CjegApp app;
	private HashMap<Edge, CrJourneyEvolvingGraph> crjourneyeg_map;
	private static final CjegReport report = new CjegReport();

	private CjegReport() {
		app = CjegApp.getInstance();
		crjourneyeg_map = app.getCrjourneyeg_map();
	}

	public void reportCrjEgMap() {
		for (Map.Entry<Edge, CrJourneyEvolvingGraph> entry : crjourneyeg_map
				.entrySet()) {
			Edge edge = entry.getKey();
			CrJourneyEvolvingGraph crjourneyeg = entry.getValue();
			Log.writeln(
					"---From:" + edge.getFromStr() + " " + "To:"
							+ edge.getToStr() + "---", 2);
			if (crjourneyeg == null) {
				Log.writeln("there's no critical journey cluster", 2);
				Log.writeln("", 2);
				continue;
			}
			CrJourneyCluster crc;
			for (int i = 0; i < crjourneyeg.size(); i++) {
				crc = crjourneyeg.get(i);
				Log.writeln(
						"a critical journey cluster start at " + crc.getStime(),
						2);
				Log.writeln(
						"a critical journey cluster stop at " + crc.getEtime(),
						2);
				Log.writeln(crc.getRepresentative_crjourney().toString(), 2);
				Log.writeln("", 2);
			}
		}
	}

	public void reportSortedCrjEgMap() {
		TreeMap<Integer, Node> nodes_map = app.getNodesMap();
		ArrayList<Integer> nodeslist = new ArrayList<Integer>();
		for (Integer nodeaddress:nodes_map.keySet()) {
			nodeslist.add(nodeaddress.intValue());
		}
		for (int i = 0; i < nodeslist.size(); i++) {
			for (int j = 0; j < nodeslist.size(); j++) {
				if (i == j)
					continue;
				int fromaddr = nodeslist.get(i);
				int toaddr = nodeslist.get(j);
				Edge edge = new Edge(fromaddr, toaddr);
				reportCrjEgMap(edge);
			}
		}
	}

	public void reportCrjEgMap(Edge edge) {
		CrJourneyEvolvingGraph crjourneyeg = crjourneyeg_map.get(edge);
		Log.writeln(
		 "---From:" + edge.getFromStr() + " " + "To:" + edge.getToStr()
		 + "---", 2);
		if (crjourneyeg == null) {
			Log.writeln("there's no critical journey cluster", 2);
			Log.writeln("", 2);
			return;
		}
		CrJourneyCluster crc;
		for (int i = 0; i < crjourneyeg.size(); i++) {
			crc = crjourneyeg.get(i);
			Log.writeln(crc.toString(),2);
			Log.writeln("", 2);
		}
	}

	public static CjegReport getInstance() {
		return report;
	}

}
