package tool.tmg;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.Iterator;
import edu.stanford.math.primitivelib.autogen.pair.IntIntPair;

/**
 * this implemention is copied from javaplex project in GitHub
 * 
 * Implements the Erdos-Renyi G(n,p) model.
 * 
 * From Wikipedia: In the G(n, p) model, a graph is thought to be constructed by
 * connecting nodes randomly. Each edge is included in the graph with
 * probability p, with the presence or absence of any two distinct edges in the
 * graph being independent.
 * 
 * @author Tim Harrington
 * @date Dec 22, 2008
 * 
 */
public class RandomGraph extends GraphInstanceGenerator {

	private static final long serialVersionUID = 8667419683575090941L;
	protected int n;
	protected double p;
	protected double b;
	protected double d;
	private AbstractUndirectedGraph randomgraph;
	private int rgid;
	private long time;

	/**
	 * @param n
	 * @param k
	 */
	public RandomGraph(int n, double p, double b, double d, int _rgid,
			long _time) {
		this.n = n;
		this.p = p;
		this.b = b;
		this.d = d;
		this.rgid = _rgid;
		this.time = _time;
	}

	public void generate() {
		AbstractUndirectedGraph graph = this.initializeGraph(n);
		for (int i = 0; i < n; i++) {
			for (int j = i; j < n; j++) {
				// don't make loops
				if (i == j)
					continue;
				// add edge with probability p
				if (RandomUtility.nextBernoulli(p) == 1) {
					graph.addEdge(i, j);
					String eventtypes = "UP";
					TravelingMerchantGraph.insertRecord(time, rgid, i, j,
							eventtypes);
				}
			}
		}
		randomgraph = graph;
	}

	public void generateNext(long _time) {
		time = _time;
		for (int i = 0; i < n; i++) {
			for (int j = i; j < n; j++) {
				// don't make loops
				if (i == j)
					continue;
				if (randomgraph.containsEdge(i, j)) {
					// remove edge with probability d
					if (RandomUtility.nextBernoulli(d) == 1) {
						randomgraph.removeEdge(i, j);
						String eventtypes = "DOWN";
						TravelingMerchantGraph.insertRecord(time, rgid, i, j,
								eventtypes);
					}
				} else {
					// add edge with probability b
					if (RandomUtility.nextBernoulli(b) == 1) {
						randomgraph.addEdge(i, j);
						String eventtypes = "UP";
						TravelingMerchantGraph.insertRecord(time, rgid, i, j,
								eventtypes);
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String retstr = "";
		// DecimalFormat df = new DecimalFormat("#.###");
		// retstr = retstr + "ErdosRenyi(" + n + "," + df.format(p) + ")\n";
		// retstr = retstr + "Nodes from 0 to "
		// + (randomgraph.getNumVertices() - 1) + "\n";
		retstr = retstr + "this is village:" + rgid + "\n";
		int numedges = randomgraph.getNumEdges();
		retstr = retstr + "has " + numedges + " edges\n";
		if (numedges > 0) {
			Iterator<IntIntPair> edgeiterator = randomgraph.iterator();
			while (edgeiterator.hasNext()) {
				IntIntPair edge = edgeiterator.next();
				retstr = retstr + "(" + edge.getFirst() + ","
						+ edge.getSecond() + ") ";
			}
		}
		retstr = retstr + "\n";
		return retstr;
	}

}
