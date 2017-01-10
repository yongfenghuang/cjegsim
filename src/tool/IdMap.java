package tool;

import java.util.HashMap;
import java.util.Map;
import net.sf.json.JSONObject;


/**
 ** @author yfhuang created at 2014-2-26
 */
public class IdMap {
	private final static IdMap idmap=new IdMap();
	private static final Map<Integer, String> iid_map = new HashMap<Integer, String>();
	private static final Map<String, Integer> eid_map = new HashMap<String, Integer>();

	private IdMap() {
	}

	public void init(JSONObject json) {
		for (Object key : json.keySet()) {
			String name = (String) key;
			Integer id = json.getInt(name);
			iid_map.put(id, name);
			eid_map.put(name, id);
		}
	}

	public static IdMap getInstance() {
		return idmap;
	}

	public String getExternalId(Integer internalId) {
		if (iid_map.size() == 0){
			Log.writeln("IdMap is not initialized",1);
			return "N"+internalId.toString();
		}
		final String eid = iid_map.get(internalId);
		if (eid == null)
			return "N"+internalId.toString();
		return eid;
	}

	public Integer getInternalId(String externalId) {
		if (eid_map.size() == 0)
			Log.writeln("IdMap is not initialized",1);
		final Integer Iid = eid_map.get(externalId);
		if (Iid == null)
			return Integer.parseInt(externalId);
		return Iid;
	}
}
