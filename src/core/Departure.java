package core;

import java.io.IOException;
import output.CodedBuffer;
import output.CodedInputStream;

/**
 * @author yfhuang created at 2014-3-5
 * 
 */
public class Departure implements JourneyElement{
	private Edge edge;
	private long time;

	public Edge getEdge() {
		return edge;
	}

	public long getTime() {
		return time;
	}

	public Departure clone() {
		Departure clonedeparture;
		Edge _edge = edge.clone();
		clonedeparture = new Departure(_edge, time);
		return clonedeparture;
	}

	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof Departure))
			return false;
		Departure m = (Departure) o;
		return this.getEdge().equals(m.getEdge())
				&& this.getTime() == m.getTime();
	}

	public void write(CodedBuffer out) {
		// write edge
		edge.write(out);
		// write departure time
		out.writeSLong(time);
	}
	
	public Departure(Edge _dedge, long _time) {
		this.edge = _dedge;
		this.time = _time;
	}

	public Departure(CodedInputStream cis) throws IOException {
		edge=new Edge(cis);
		time=cis.readSLong();
	}

	public String toString() {
		StringBuffer strb = new StringBuffer();
		strb.append(edge.toString());
		strb.append(",");
		strb.append(time + " ");
		return strb.toString();
	}
	
	public String toDbString() {
		StringBuffer strb = new StringBuffer();
		strb.append(edge.toDbString());
		strb.append(",");
		strb.append(time + " ");
		return strb.toString();
	}
}
