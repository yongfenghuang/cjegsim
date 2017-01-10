package core;

import java.io.IOException;

import output.CodedBuffer;
import output.CodedInputStream;
import app.CjegException;

/**
 * @author yfhuang created at 2014-3-5
 * 
 */
public class CrJourneyCluster {
	private JourneyClusterType ctype=null;
	private Edge edge;
	//private TimeType sflag;
	private long stime;
	private long etime;
	// private TimeType eflag;
	private Journey<?> representative_crjourney; //representative journey

	public Journey<?> getRepresentative_crjourney() {
		return representative_crjourney;
	}

	public Edge getEdge() {
		return edge;
	}
	
	public void setRepresentative_crjourney(Journey<?> _representative_crjourney) {
		this.representative_crjourney = _representative_crjourney;
	}

	public long getStime() {
		return stime;
	}

	public void setStime(long stime) {
		this.stime = stime;
	}
	
	public JourneyClusterType getCtype() {
		if (ctype != null)
			return ctype;
		else if (stime == etime)
			ctype = JourneyClusterType.DISCRETE;
		else if (stime < etime)
			ctype = JourneyClusterType.CONTINUOUS;
		else if (etime == Long.MAX_VALUE)
			ctype = JourneyClusterType.UNSURE;
		else
			throw new CjegException("Critical Journey Cluster Error");
		return ctype;
	}

	public long getEtime() {
		return etime;
	}

	public void setEtime(long etime) {
		this.etime = etime;
	}

	public void write(CodedBuffer out) {
		byte cflag;
		JourneyClusterType ctype = getCtype();
		switch (ctype) {
		case DISCRETE:
			cflag = 0;
			break;
		case CONTINUOUS:
			cflag = 1;
			break;
		default:
			cflag = 2;
			throw new CjegException(
					"incorrect cluster type in write method of crjourneycluster");
		}
		// write fromaddr
		out.writeInt(edge.getFromaddr());
		// write toaddr
		out.writeInt(edge.getToaddr());
		// write ctype
		out.writeByte(cflag);
		// write start time of cluster
		out.writeSLong(this.getStime());
		// write end time of cluster if continuous
		if (ctype == JourneyClusterType.CONTINUOUS)
			out.writeSLong(this.getEtime());
		// write jtype
		byte ishopj;
		if (representative_crjourney instanceof DateJourney) {
			ishopj = 0;
		} else if (representative_crjourney instanceof HopJourney) {
			ishopj = 1;
		} else {
			throw new CjegException(
					"journey type error in write method of crjourneycluster");
		}
		// write journey
		out.writeByte(ishopj);
		representative_crjourney.write(out);
	}

	public CrJourneyCluster(CodedInputStream cis) throws IOException {
			int fromaddr=cis.readInt();
			int toaddr=cis.readInt();
			edge=new Edge(fromaddr,toaddr);
			byte cflag = cis.readByte();
			stime = cis.readSLong();
			if (cflag == 0) {
				ctype=JourneyClusterType.DISCRETE;
				etime = stime;
			} else if (cflag == 1) {
				ctype=JourneyClusterType.CONTINUOUS;
				etime = cis.readSLong();
			} else {
				throw new CjegException("no such flag exception in CjegReader");
			}
			byte ishopj = cis.readByte();
			if (ishopj == 0) {
				representative_crjourney = new DateJourney(cis);
			} else if (ishopj == 1) {
				representative_crjourney = new HopJourney(cis);
			} else {
				throw new CjegException("no such flag exception in CjegReader");
			}
	}

	public CrJourneyCluster(Edge _edge,long _stime, long _etime,
			Journey<?> _earliest_crjourney) {
		edge=_edge;
		stime = _stime;
		etime = _etime;
		representative_crjourney = _earliest_crjourney;
	}

	public String toString() {
		StringBuilder strb = new StringBuilder();
		strb.append(getCtype().getDesp());
		strb.append(" critical cluster ");
		strb.append("start at " + getStime());
		strb.append(" stop at " + getEtime());
		strb.append("\n");
		strb.append(getRepresentative_crjourney().toString());
		return strb.toString();
	}

	public String toDbString() {
		StringBuilder strb = new StringBuilder();
		strb.append(getCtype().getDesp());
		strb.append(" critical cluster ");
		strb.append("start at " + getStime());
		strb.append(" stop at " + getEtime());
		strb.append(" between "+edge.getFromaddr()+" and "+edge.getToaddr());
		strb.append("\n");
		strb.append(getRepresentative_crjourney().toDbString());
		return strb.toString();
	}
}
