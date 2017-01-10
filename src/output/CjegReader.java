package output;

import java.io.BufferedInputStream;
import java.io.IOException;
import core.CrJourneyCluster;
import core.Edge;

/**
 * @author yfhuang created at 2014-3-18
 * 
 */
public class CjegReader {

	private CodedInputStream cis;
	SeekMap seek_map;
	private final CjegTrace trace;

	public CjegReader(CjegTrace _trace) throws IOException {
		trace = _trace;
		init();
	}

	private void init() throws IOException {
		cis = new CodedInputStream(new BufferedInputStream(
				trace.getInputStream(trace.traceFile())));
	}

	public void seek(long time, Edge edge) throws IOException {
		seek_map = SeekMap.open(trace.getInputStream(trace.indexFile()), edge);
		long absolutePosition = seek_map.getOffset(time);
		if (cis.canFastForwardTo(absolutePosition)) {
			cis.fastForwardTo(absolutePosition);
		} else {
			reset();
			if (cis.canFastForwardTo(absolutePosition)) {
				cis.fastForwardTo(absolutePosition);
			}
		}
	}

	public CrJourneyCluster read() throws IOException {
		CrJourneyCluster crjc;
		if (!cis.isAtEnd()) {
			crjc = new CrJourneyCluster(cis);
			return crjc;
		}
		return null;
	}

	public CrJourneyCluster read(Edge edge) throws IOException {
		CrJourneyCluster crjc;
		while (!cis.isAtEnd()) {
			crjc = new CrJourneyCluster(cis);
			if (crjc.getEdge().equals(edge))
				return crjc;
		}
		return null;
	}

	public CrJourneyCluster read(long time, Edge edge) throws IOException{
		CrJourneyCluster crjc = null;
		seek(time, edge);
		boolean find = false;
		while (!find) {
			crjc = read(edge);
			if (crjc.getEtime() >= time)
				find = true;
		}
		return crjc;
	}

	// just for test
	public void PrintSeekMap(Edge edge) throws IOException {
		seek_map = SeekMap.open(trace.getInputStream(trace.indexFile()), edge);
		seek_map.printSeekMap();
	}

	private void reset() throws IOException {
		close();
		init();
	}

	public void close() throws IOException {
		cis.close();
	}

}
