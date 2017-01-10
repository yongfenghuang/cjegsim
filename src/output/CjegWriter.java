package output;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import core.CrJourneyCluster;
import core.CrJourneyEvolvingGraph;
import core.Edge;
import app.ConstantConfig;


/**
 * @author yfhuang created at 2014-3-18
 * 
 */
public class CjegWriter {

	private BufferedOutputStream out=null;
	private SeekMap.Writer smw=null;
	private int total_bytes_written = 0;
	private CjegTrace trace;
	private long max_time;
	private long min_time;
	private int n_clusters=0;
	private long clusternumber=0;
	
	public CjegWriter(CjegTrace _trace) throws IOException {
		trace = _trace;
		min_time = Long.MAX_VALUE;
		max_time = Long.MIN_VALUE;
		smw = new SeekMap.Writer(trace.getOutputStream(trace.indexFile()));
		out = new BufferedOutputStream(trace.getOutputStream(trace.traceFile()));
	}
	
	public void close() throws IOException {
		out.close();
		smw.close();
		writeInfo();
	}

	public void writeInfo() throws IOException {
		setRemainingInfo();
		OutputStreamWriter info_os = new OutputStreamWriter(
				trace.getOutputStream(trace.infoFile()));
		// toString(4) insert 4 blank space
		info_os.write(trace.getConfig().toString(4));
		info_os.close();
	}

	//must call append_open before this and call append_close after this 
	public void append(CrJourneyCluster crjc,boolean needmark,CrJourneyEvolvingGraph cjeg) throws IOException {
		//Log.writeln("append a new crjc to trace", 1000);
		updateTime(crjc.getStime(), crjc.getEtime(), crjc.getEdge(),needmark,cjeg);
		write(crjc);
		n_clusters++;
		clusternumber++;
	}

	void write(CrJourneyCluster crjc) throws IOException {
		CodedBuffer buffer = new CodedBuffer();
		crjc.write(buffer);
		total_bytes_written += buffer.flush(out);
		if (clusternumber%ConstantConfig.WRITER_FLUSH_SIZE==0) out.flush();
	}

	private void updateTime(long stime, long etime, Edge _edge,boolean needmark,CrJourneyEvolvingGraph cjeg) throws IOException {
		if (stime < min_time)
			min_time = stime;
		if (stime > max_time) {
			max_time = stime;
		}
		if (n_clusters >= ConstantConfig.N_CRJC_TRIGGER || needmark)
			markPosition(_edge, stime, etime,cjeg);
	}

	void setRemainingInfo() {
		trace.setIfUnset(CjegTrace.mintime, min_time);
		trace.setIfUnset(CjegTrace.maxtime, max_time);
		trace.setIfUnset(CjegTrace.clusternumber, clusternumber);
	}

	void markPosition(Edge _edge, long stime, long etime,CrJourneyEvolvingGraph cjeg) throws IOException {
		smw.append(_edge, stime, etime, total_bytes_written);
		cjeg.setLastmarkposition(clusternumber);
		n_clusters = 0;
	}
	
	public long getClusternumber() {
		return clusternumber;
	}

}
