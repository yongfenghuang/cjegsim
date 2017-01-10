package app;

import input.CjEvent;
import input.CjEventListener;
import input.ConnectionEventListener;
import input.ConnectionEventQueue;
import input.DvcEventListener;
import input.DvcEventQueue;
import input.EventQueueBus;
import input.PtuEventListener;
import input.PtuEventQueue;
import input.ReceptionEventListener;
import input.ReceptionEventQueue;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import output.CjegReport;
import output.CjegTrace;
import core.CrJourneyEvolvingGraph;
import core.Edge;
import core.Node;
import core.VectorOnNode;
import tool.AppUtil;
import tool.IdMap;
import tool.Log;
import net.sf.json.JSONObject;

/**
 ** @author yfhuang created at 2014-2-28
 */
public class CjegApp {
	private EventQueueBus eventqueuebus;
	private ConnectionEventQueue coneventqueue;
	private ReceptionEventQueue receventqueue;
	private DvcEventQueue concrjceventqueue;
	private PtuEventQueue ptueventqueue;
	private static AppConfig appconfig;
	private static final CjegApp cjegapp = new CjegApp();

	// edge-time HashMap for ReceptionEvent
	private final HashMap<Edge, HashSet<Long>> inverse_reception_map = new HashMap<Edge, HashSet<Long>>();
	private final HashMap<Edge, HashSet<Long>> inverse_continue_map = new HashMap<Edge, HashSet<Long>>();
	private final HashMap<Edge, HashSet<Long>> inverse_ptu_map = new HashMap<Edge, HashSet<Long>>();
	private final TreeMap<Integer, Node> nodes_map = new TreeMap<Integer, Node>();

	// crjourneyeg_map
	private final HashMap<Edge, CrJourneyEvolvingGraph> crjourneyeg_map = new HashMap<Edge, CrJourneyEvolvingGraph>();
	private CjegTrace cjegtrace;

	// message counter of reception in system
	private static long receptioncounter = 0;
	// critical journey cluster counter arised by dvc
	private static long dvcjourneycounter = 0;

	long maxusedmemory = 0; // memory used in the process of excution
	long usedmemory;

	public static void incrementRecept() {
		receptioncounter++;
	}

	public static void incrementDvcjourney() {
		dvcjourneycounter++;
	}

	public CjegTrace getCjegtrace() {
		return cjegtrace;
	}

	public HashMap<Edge, CrJourneyEvolvingGraph> getCrjourneyeg_map() {
		return crjourneyeg_map;
	}

	public static AppConfig getAppconfig() {
		if (appconfig != null)
			return appconfig;
		else
			return AppConfig.getInstance();
	}

	public ConnectionEventQueue getConeventqueue() {
		return coneventqueue;
	}

	public ReceptionEventQueue getReceventqueue() {
		return receventqueue;
	}

	public DvcEventQueue getConcrjceventqueue() {
		return concrjceventqueue;
	}

	public PtuEventQueue getPtueventqueue() {
		return ptueventqueue;
	}

	private CjegApp() {

	}

	public TreeMap<Integer, Node> getNodesMap() {
		return nodes_map;
	}

	public HashMap<Edge, HashSet<Long>> getInverseReceptionMap() {
		return inverse_reception_map;
	}

	public HashMap<Edge, HashSet<Long>> getInversePtuMap() {
		return inverse_ptu_map;
	}

	public HashMap<Edge, HashSet<Long>> getInversecontinuemap() {
		return inverse_continue_map;
	}

	public void startApp() throws IOException {

		Date appbegintime = new Date();
		init();
		exec();
		report();
		Date appendtime = new Date();
		Log.writeln(
				"execute time(s):"
						+ (appendtime.getTime() - appbegintime.getTime())
						/ 2000, 2000);
		if (CjegApp.getAppconfig().isMonitorMemory())
			Log.writeln("max used memory:" + usedmemory / (1024 * 1024) + "MB", 2000);

		// print neighborsvec of every node
		// Log.writeln("print neighbors vectors of every node, which is the maximum memory overhead cjegsim use",2000);
		// for (Node anode : nodes_map.values()) {
		// Log.writeln("current node is:"+anode.getNodeStr(),2000);
		// for (VectorOnNode vectors: anode.getNeighborsvec().values()){
		// Log.writeln(vectors.toString(),2000);
		// }
		// }
	}

	public void init() throws IOException {
		// load app parameter from config file cjeg.txt
		appconfig = AppConfig.getInstance();

		// redirect output
		if (appconfig.getDebug() == 1) {
			try {
				FileOutputStream fos = new FileOutputStream(
						ConstantConfig.DEBUG_FILE);
				BufferedOutputStream bos = new BufferedOutputStream(fos, 1024);
				PrintStream ps = new PrintStream(bos, true);
				System.setOut(ps);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// init IdMap
		try {
			JSONObject idmap_json = JSONObject
					.fromObject(AppUtil.getFileAsString(AppUtil.getFileStr(
							appconfig.getStorename(), appconfig.getIdmap_file())));
			IdMap.getInstance().init(idmap_json);
		} catch (CjegException e) {
			Log.writeln("idmap load failed", 2);
		}

		cjegtrace = new CjegTrace(appconfig.getStorename(),
				AppUtil.getTracedir(appconfig.getTracename(),
						appconfig.getTau()), appconfig.getWr_flag());

		// init all queues this initialize must be before init listener
		coneventqueue = new ConnectionEventQueue(appconfig.getStorename(),
				appconfig.getCrawdadfile());
		receventqueue = new ReceptionEventQueue();
		concrjceventqueue = new DvcEventQueue();
		ptueventqueue = new PtuEventQueue();

		// init listener
		ConnectionEventListener coneventlistener = new ConnectionEventListener();
		ReceptionEventListener receventlistener = new ReceptionEventListener();
		DvcEventListener concrjceventlistener = new DvcEventListener();
		PtuEventListener ptueventlistener = new PtuEventListener();

		// set listener
		receventqueue.setEventlistener(receventlistener);
		coneventqueue.setEventlistener(coneventlistener);
		concrjceventqueue.setEventlistener(concrjceventlistener);
		ptueventqueue.setEventlistener(ptueventlistener);

		// init queuebus
		eventqueuebus = new EventQueueBus();
		// add queues to bus
		// the priority of the sequence is up to the adding sequence, if queue a
		// is added before queue b,the event in queue a is processed before the
		// one in
		// queue b if these two happened at the same time
		eventqueuebus.addQueue(ptueventqueue); // must be put first
		eventqueuebus.addQueue(receventqueue);
		eventqueuebus.addQueue(coneventqueue);
		eventqueuebus.addQueue(concrjceventqueue);
	}

	public void report() {
		CjegReport report = CjegReport.getInstance();
		// report.reportCrjEgMap();
		report.reportSortedCrjEgMap();
		Log.writeln("receptioncounter is:" + receptioncounter, 0);
		Log.writeln("dvcjourneycounter is:" + dvcjourneycounter, 0);
		Log.writeln("receptioncounter is:" + receptioncounter, 0);
		Log.writeln("dvcjourneycounter is:" + dvcjourneycounter, 0);
	}

	public void exec() throws IOException {
		// open write to trace
		if (CjegApp.getAppconfig().isTofile())
			cjegtrace.write_open();

		List<CjEvent> cjevents = eventqueuebus.nextEvents();
		long eventstime = eventqueuebus.getEventsTime();
		long nexteventstime;
		long previoustime = 0;
		CjEventListener cjeventlistener = eventqueuebus.getEventListener();
		Runtime runtime = Runtime.getRuntime();
		int count=0;
		while (eventstime != Long.MAX_VALUE) {
			Log.writeln("next time events:" + eventstime, 1);
			// Log.write("events size:"+cjevents.size());
			// Log.write("events listener:"+cjeventlistener.toString());
			// Log.write("next events list");
			for (CjEvent cjevent : cjevents) {
				Log.writeln("process event", 1);
				cjeventlistener.processEvent(cjevent);
			}
			eventstime = eventqueuebus.getEventsTime();
			nexteventstime = eventqueuebus.getNextEventsTime();
			Log.writeln("", 1);
			// send vectorclock at the end of time and set originvec
			if (nexteventstime != eventstime) {
				Log.writeln("now is:" + eventstime, 1000);
				Log.writeln("now is:" + eventstime, 2000);
				// Log.writeln("at time " + eventstime + " vector clock are:",
				// 1);
				for (Node anode : nodes_map.values()) {
					Log.writeln("now current node is:" + anode.getNodeStr(), 1);
					if (anode.needSend())
						anode.sendVecToNeighbors(eventstime);

					if (anode.isVectorChanged() || anode.isViewChanged())
						anode.onViewChanged(previoustime, eventstime);

					if (anode.needSend())
						anode.setOriginvec();
					if (anode.isViewChanged())
						anode.RestoreViewchanged();

					if (anode.needPtuSend()) {
						anode.sendPucCseqToNeighbors(eventstime);
						anode.restorPucChanged();
					}
				}
				previoustime = eventstime;
			}
			cjevents = eventqueuebus.nextEvents();
			eventstime = eventqueuebus.getEventsTime();
			cjeventlistener = eventqueuebus.getEventListener();
			// monitor memory use
			if (CjegApp.getAppconfig().isMonitorMemory()) {
				if (count == 0) {
					System.gc(); //collect object that could be moved from memory
				}
				count++; count=count % 100;
				usedmemory = runtime.totalMemory() - runtime.freeMemory();
				if (usedmemory > maxusedmemory)
					maxusedmemory = usedmemory;
			}
		}

		if (appconfig.isTofile()) {
			for (CrJourneyEvolvingGraph crjourneyeg : crjourneyeg_map.values()) {
				crjourneyeg.flushLeft();
			}
		}

		if (CjegApp.getAppconfig().isTofile())
			cjegtrace.writer_close();
		Log.writeln("done well finished", 1000);
		Log.writeln("done well finished", 2000);

	}

	public EventQueueBus getEventqueuebus() {
		return eventqueuebus;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			CjegApp cjegapp = CjegApp.getInstance();
			cjegapp.startApp();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static CjegApp getInstance() {
		return cjegapp;
	}

}
