package input;

import core.Node;
import tool.AppUtil;
import tool.Log;

/**
 ** @author yfhuang created at 2014-2-26
 */
public class PtuEventListener implements CjEventListener {
	// private CjegApp app;

	public void onPtuReception(PtuEvent _ptuevent) {
		Node fromnode = AppUtil.getNodesFromAddress(_ptuevent.getFromaddr());
		Node tonode = AppUtil.getNodesFromAddress(_ptuevent.getToaddr());
		long time = _ptuevent.getTime();
		if (time == 5136 && tonode.getAddress() == 59) {
			Log.writeln(_ptuevent.getTime() + " " + tonode.getNodeStr()
					+ " receive a ptu message from " + fromnode.getNodeStr(),
					1000);
			Log.writeln(" receive ptucseqlist is:"
					+ _ptuevent.getPtuCseqList().toString(), 1000);
		}
		tonode.onPucReception(fromnode, _ptuevent.getTime(),
				_ptuevent.getPtuCseqList());
	}

	public void processEvent(CjEvent _cjevent) {
		PtuEvent ptuevent = (PtuEvent) _cjevent;
		onPtuReception(ptuevent);
	}

	public PtuEventListener() {
		// app = CjegApp.getInstance();

	}

}
