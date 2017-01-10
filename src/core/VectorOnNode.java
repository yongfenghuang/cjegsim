package core;

import java.util.HashMap;
import java.util.Map;

import tool.AppUtil;

/**
 * @author yfhuang created at 2014-3-6
 *
 */
public class VectorOnNode extends HashMap<Integer, SourceClock> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3413297059500572354L;

	public VectorOnNode clone(){
		
		VectorOnNode clonevclock=new VectorOnNode();
		for(Map.Entry<Integer, SourceClock> entry:this.entrySet()){
			Integer key=entry.getKey();
			SourceClock value=entry.getValue();
			Integer clonekey=new Integer(key);
			SourceClock clonesclock = value.clone();
			clonevclock.put(clonekey, clonesclock);
		}
		return clonevclock;
	}
	
	public String toString(){
		StringBuffer strb=new StringBuffer();
		for(Map.Entry<Integer, SourceClock> entry:this.entrySet()){
			Integer key=entry.getKey();
			strb.append("source node :"+AppUtil.getNodesFromAddress(key).getNodeStr()+" \n");
			SourceClock value=entry.getValue();
			strb.append(value.toString());
		}
		return strb.toString();
	}
	
	@Override
	public boolean equals(Object o){
		if (o==this){
			return true;
		}
		if (!(o instanceof VectorOnNode)) return false;
		return super.equals(o);
	}
	
}
