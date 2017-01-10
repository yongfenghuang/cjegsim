package input;

import app.CjegException;
import core.Node;
import tool.AppUtil;

/**
 ** @author yfhuang created at 2014-2-26
 *  ContinueCrJcEventListener = DvcEventListener = dumb view changed eventlistener
 */
public class DvcEventListener implements CjEventListener {
	public void onDvc(DvcEvent _dvcevent) {
		Node tonode=AppUtil.getNodesFromAddress(_dvcevent.getToaddr());
		if (tonode==null){
			throw new CjegException("no such node in ContinueCrJcEventListener");
		}
		tonode.onDvc(_dvcevent);
	}

	public void processEvent(CjEvent _cjevent) {
		DvcEvent concrjcevent = (DvcEvent) _cjevent;
		onDvc(concrjcevent);
	}
}
