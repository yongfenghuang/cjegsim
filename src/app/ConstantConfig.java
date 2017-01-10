package app;

/**
 * @author yfhuang created at 2014-3-26 variant config in this app is in
 *         AppConfig
 */

public class ConstantConfig {
	public final static int N_CRJC_TRIGGER = 1000;
	public final static int CLUSTER_FLUSH_SIZE = 50;
	public final static int LEFT_SIZE = 2;
	public final static String DEBUG_FILE = "debug.txt";
	public final static int WRITER_FLUSH_SIZE = 5000;
	// if following is true, every first cluster in cjeg will be indexed.
	public final static boolean FIRST_NEED_INDEX = false;
	// when write crjc to trace if current clusternumber-lastmarkposition on
	// current edge ,mark it in index file which guarantee that those crjceg
	// which has only a few crjccluster can be index
	public final static int MARK_INTERVAL = 5000;
	public final static int INDEX_FLUSH_SIZE = 1000;

}
