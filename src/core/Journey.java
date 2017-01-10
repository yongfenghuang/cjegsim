package core;

import java.util.LinkedList;

import output.CodedBuffer;
import app.CjegApp;

/**
 * @author yfhuang created at 2014-3-5
 * 
 */
public abstract class Journey<E> extends LinkedList<E> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5850892300311071292L;

	// 40="("
	final static byte LBRACKET = 40;
	// 41=")"
	final static byte RBRACKET = 41;
	
	public Journey() {

	}

	public abstract long getDepartureTime();

	public abstract long getArriveTime();

	public abstract Journey<E> clone();

	public abstract long getDelay();

	public int getHopCount() {
		return this.size();
	}

	/**
	 * jtype 0 direct journey jtype 1 indirect journey
	 * 
	 * @return jtype
	 */
	public JourneyType getJtype() {
		if (getDelay() == getHopCount() * CjegApp.getAppconfig().getTau()) {
			return JourneyType.DIRECT;
		} else {
			return JourneyType.INDIRECT;
		}
	}

	@SuppressWarnings("unchecked")
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof Journey))
			return false;
		Journey<E> m = (Journey<E>) o;
		if (this.size() != m.size())
			return false;
		for (int j = 0; j < this.size(); j++) {
			E thisele = this.get(j);
			E thatele = m.get(j);
			if (!thisele.equals(thatele))
				return false;
		}
		return true;
	}

	public String toString() {
		StringBuffer strb = new StringBuffer();
		if (getJtype() == JourneyType.DIRECT)
			strb.append("DIRECT Journey:");
		else
			strb.append("INDIRECT Journey:");
		for (int index = 0; index < this.size(); index++) {
			strb.append(this.get(index).toString());
			strb.append(" ");
		}
		return strb.toString();
	}
	
	public String toDbString() {
		StringBuffer strb = new StringBuffer();
		for (int index = 0; index < this.size(); index++) {
			strb.append(((JourneyElement)this.get(index)).toDbString());
			strb.append(" ");
		}
		return strb.toString();
	}

	public abstract void write(CodedBuffer out);
}