package output;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import app.CjegException;

import tool.AppUtil;
import core.CrJourneyCluster;
import core.CrJourneyEvolvingGraph;
import core.Edge;
import net.sf.json.JSONObject;

/**
 * @author yfhuang created at 2014-3-18
 * 
 */

public class CjegTrace {
	final public static String tau = "tau";
	final public static String clusternumber = "clusternumber";
	final public static String mintime = "min time";
	final public static String maxtime = "max time";
	final public static String description = "description";

	private String store;
	private String name;
	private JSONObject config;
	private String separator = "/";
	private String tracedir;
	private CjegReader reader;
	private boolean isreading=false;
	private CjegWriter writer;
	private boolean iswritting=false;
	private Edge edge;

	public Edge getEdge() {
		return edge;
	}

	/**
	 * @param _store
	 * @param _name
	 * @param wr_flag 1 to tracefile 0 to memory -1 read
	 * @throws IOException
	 */
	public CjegTrace(String _store, String _name, int wr_flag) throws IOException {
		store = _store;
		name = _name;
		tracedir = store + separator + name + separator;
		if (wr_flag==1) {
			config = new JSONObject();
		}else if(wr_flag==-1){
			config = JSONObject
					.fromObject(AppUtil.getFileAsString(tracedir+separator+infoFile()));
		}
	}

	public OutputStream getOutputStream(String _filename) throws IOException {
		final File parent = new File(tracedir, _filename).getParentFile();
		parent.mkdirs();
		return new FileOutputStream(new File(tracedir, _filename));
	}

	public InputStream getInputStream(String _filename) throws IOException {
		return new FileInputStream(new File(tracedir, _filename));
	}
	
	public void append(CrJourneyCluster acrjc,boolean needmark,CrJourneyEvolvingGraph cjeg) throws IOException{
		if (writer==null || !iswritting){
			throw new CjegException("writer hasn't initialized or opened");
		}else{
			writer.append(acrjc,needmark,cjeg);
		}
	}
	
	public CrJourneyCluster read() throws IOException{
		if (reader==null|| !isreading){
			throw new CjegException("reader hasn't initialized or opened");
		}
		return reader.read();
	}
	
	public CrJourneyCluster read(long time,Edge edge) throws IOException{
		if (reader==null|| !isreading){
			throw new CjegException("reader hasn't initialized or opened");
		}
		return reader.read(time,edge);
	}
	
	public CrJourneyCluster read(Edge edge) throws IOException{
		if (reader==null|| !isreading){
			throw new CjegException("reader hasn't initialized or opened");
		}
		return reader.read(edge);
	}
	
	public JSONObject getConfig() {
		return config;
	}

	String indexFile() {
		return "index";
	}

	String traceFile() {
		return "trace";
	}

	String infoFile() {
		return "info";
	}

	public int getTau() {
		return config.getInt(tau);
	}

	public CjegReader getReader() throws IOException {
		if (reader == null)
			reader = new CjegReader(this);
		return reader;
	}
	
	public CjegWriter getWriter() throws IOException {
		if (writer == null)
			writer = new CjegWriter(this);
		return writer;
	}
	
	public void reader_open() throws IOException{
		getReader();
		isreading=true;
	}
	
	public void reader_close() throws IOException{
		reader.close();
		isreading=false;
	}
	
	public void write_open() throws IOException{
		getWriter();
		iswritting=true;
	}
	
	public void writer_close() throws IOException{
		writer.close();
		iswritting=false;
	}

	public void set(String key, Object value) {
		config.put(key, value);
	}

	public void setIfUnset(String key, Object value) {
		if (!config.containsKey(key))
			config.put(key, value);
	}
	
	public long getClusterNumber(){
		if (writer == null)
			throw new CjegException("writer is null now.");
		return writer.getClusternumber();
	}
	
	public void PrintSeekMap(Edge edge) throws IOException{
		if (reader==null|| !isreading){
			throw new CjegException("reader hasn't initialized or opened");
		}
		reader.PrintSeekMap(edge);	
	}
}
