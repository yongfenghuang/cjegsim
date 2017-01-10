package core;

import java.io.IOException;
import java.util.LinkedList;
import app.CjegApp;
import app.ConstantConfig;
import output.CjegTrace;

/**
 * @author yfhuang created at 2014-3-19
 * 
 */
public class CrJourneyEvolvingGraph extends LinkedList<CrJourneyCluster> {

	private static final long serialVersionUID = 1L;
	private CjegTrace cjegtrace;
	private boolean isfirst = true;
	private long lastmarkposition = 0;

	public long getLastmarkposition() {
		return lastmarkposition;
	}

	public void setLastmarkposition(long lastmarkposition) {
		this.lastmarkposition = lastmarkposition;
	}

	public boolean append(CrJourneyCluster _crjc) {
		try {

			int cregsize = this.size();
			if (cregsize >= ConstantConfig.CLUSTER_FLUSH_SIZE
					+ ConstantConfig.LEFT_SIZE) {
				while (this.size() > ConstantConfig.LEFT_SIZE) {
					CrJourneyCluster remove_crjc = this.pollFirst();
					boolean needmark = false;
					if ((isfirst && ConstantConfig.FIRST_NEED_INDEX)
							|| (cjegtrace.getClusterNumber() - lastmarkposition) > ConstantConfig.MARK_INTERVAL)
						needmark = true;
					cjegtrace.append(remove_crjc, needmark, this);
					isfirst = false;
				}
			}
			return super.add(_crjc);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * @param _edge
	 * 
	 */
	public CrJourneyEvolvingGraph() {
		try {
			cjegtrace = CjegApp.getInstance().getCjegtrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public CrJourneyEvolvingGraph(CjegTrace _cjegtrace) {
		cjegtrace = _cjegtrace;
	}

	public boolean addCrjc(CrJourneyCluster _crjc) {
		if (!CjegApp.getAppconfig().isTofile())
			return super.add(_crjc);
		else
			return this.append(_crjc);
	}

	public void flushLeft() {
		try {
			while (this.size() > 0) {
				CrJourneyCluster remove_crjc = this.pollFirst();
				boolean needmark = false;
				if ((isfirst && ConstantConfig.FIRST_NEED_INDEX)
						|| (cjegtrace.getClusterNumber() - lastmarkposition) > ConstantConfig.MARK_INTERVAL)
					needmark = true;
				cjegtrace.append(remove_crjc, needmark, this);
				isfirst = false;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// just for test
	public void printIndex(Edge edge) throws IOException {
		cjegtrace.getReader().PrintSeekMap(edge);
	}

}
