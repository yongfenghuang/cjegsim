package input;

import core.Node;
import tool.AppUtil;
import tool.Log;

/**
 ** @author yfhuang created at 2014-2-26
 */
public class ReceptionEventListener implements CjEventListener {
//	private CjegApp app;
	
	public void onReception(ReceptionEvent _recevent) {
		Node fromnode = AppUtil.getNodesFromAddress(_recevent.getFromaddr());
		Node tonode = AppUtil.getNodesFromAddress(_recevent.getToaddr());
		Log.writeln(_recevent.getTime() + " " + tonode.getNodeStr()
				+ " receive a message from " + fromnode.getNodeStr(),1);
		tonode.onReception(fromnode, _recevent.getTime(),_recevent.getFromvec());
	}

	public void processEvent(CjEvent _cjevent) {
		ReceptionEvent recevent = (ReceptionEvent) _cjevent;
		onReception(recevent);
	}
	
	public ReceptionEventListener(){
//		app = CjegApp.getInstance();
		
	}

}
