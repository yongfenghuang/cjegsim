package app;

import net.sf.json.JSONObject;
import tool.AppUtil;

/**
 ** @author yfhuang created at 2014-3-4
 *  constant config in this app is in ConstantConfig
 */
public class AppConfig {
	
	private static final AppConfig appconfig = new AppConfig();
	// private config
	private String configfile = "cjeg.txt";
	private String storename;
	private String crawdadfile;
	private String idmap_file;
	private int tau;
	private int eta;
	// the parent of trace index info
	private String tracename;
	// 1 into filesystem 0 into memory
	private int wr_flag;
	private int debug;
	
	private int loglevel;
	private int monitormemory;

	public int getLoglevel() {
		return loglevel;
	}

	public void setLoglevel(int loglevel) {
		this.loglevel = loglevel;
	}

	// config key
	private final String storenamekey = "storename";
	private final String crawdadfilekey = "crawdadfile";
	private final String idmapkey = "idmap";
	private final String taukey = "tau";
	private final String etakey = "eta";
	private final String tracenamekey = "tracename";
	// 1 write to tracefile 0 write to memory
	private String wrflagkey = "wr_flag";
	// 1 write output info to debug file 0 out to console
	private String debugkey = "debug";
	private String loglevelkey="loglevel";
	private String monitormemorykey="monitormemory";

	public static AppConfig getInstance(){
		return appconfig;
	}
	
	private AppConfig() {
		init();
	}
	
	public void init() {
		final JSONObject config = JSONObject.fromObject(AppUtil
				.getFileAsString(configfile));
		storename = config.getString(storenamekey);
		crawdadfile = config.getString(crawdadfilekey);
		idmap_file = config.getString(idmapkey);
		tau = config.getInt(taukey);
		eta = config.getInt(etakey);
		tracename = config.getString(tracenamekey);
		wr_flag = config.getInt(wrflagkey);
		debug = config.getInt(debugkey);
		loglevel = config.getInt(loglevelkey);
		monitormemory=config.getInt(monitormemorykey);
	}

	public String getTracename() {
		return tracename;
	}

	public boolean isTofile() {
		return (wr_flag == 1);
	}

	public String getStorename() {
		return storename;
	}

	public String getCrawdadfile() {
		return crawdadfile;
	}

	public String getIdmap_file() {
		return idmap_file;
	}

	public int getTau() {
		return tau;
	}

	public int getDebug() {
		return debug;
	}

	public int getEta() {
		return eta;
	}

	public int getWr_flag() {
		return wr_flag;
	}
	
	public boolean isMonitorMemory() {
		return (monitormemory == 1);
	}
}
