package input;

import app.CjegException;
import core.Node;
import tool.AppUtil;
import tool.Log;

/**
 ** @author yfhuang created at 2014-2-26
 */
public class ConnectionEventListener implements CjEventListener {
//	private CjegApp app;
//	private HashMap<Edge, Long> inverse_reception_map;
//	private ReceptionEventQueue receventqueue;

	public void onConnectionUp(ConnectionEvent conevent) {
		String action = "UP";
		Node fromnode = AppUtil.getNodesFromAddress(conevent.getFromaddr());
		if (fromnode == null) {
			fromnode = new Node(conevent.getFromaddr());
			AppUtil.addtoNodesMap(fromnode.getAddress(), fromnode);
		}
		Node tonode = AppUtil.getNodesFromAddress(conevent.getToaddr());
		if (tonode == null) {
			tonode = new Node(conevent.getToaddr());
			AppUtil.addtoNodesMap(tonode.getAddress(), tonode);
		}
		Log.writeln(conevent.getTime() + "  " + fromnode.getNodeStr() + "  "
				+ tonode.getNodeStr() + "  " + action,1);

		fromnode.onConnectionUp(tonode,conevent.getTime());
		tonode.onConnectionUp(fromnode,conevent.getTime());
	}

	public void onConnectionDown(ConnectionEvent conevent) {
		String action = "DOWN";
		Node fromnode = AppUtil.getNodesFromAddress(conevent.getFromaddr());
		if (fromnode == null) {
			throw new CjegException(
					"there is no such fromnode when connectiondown");
		}
		Node tonode = AppUtil.getNodesFromAddress(conevent.getToaddr());
		if (tonode == null) {
			throw new CjegException(
					"there is no such tonode when connectiondown");
		}

		Log.writeln(conevent.getTime() + "  " + fromnode.getNodeStr() + "  "
				+ tonode.getNodeStr() + "  " + action,1);
		
		fromnode.onConnectionDown(tonode, conevent.getTime());
		tonode.onConnectionDown(fromnode, conevent.getTime());
	}

	public void processEvent(CjEvent cjevent) {
		ConnectionEvent conevent = (ConnectionEvent) cjevent;
		if (conevent.isUp()) {
			onConnectionUp(conevent);
		} else {
			onConnectionDown(conevent);
		}
	}

	public ConnectionEventListener() {
//		app = CjegApp.getInstance();
//		receventqueue = app.getReceventqueue();
//		inverse_reception_map = app.getInverseReceptionMap();
	}
}
