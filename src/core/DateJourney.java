package core;

import java.io.IOException;

import output.CodedBuffer;
import output.CodedInputStream;
import app.CjegApp;
import app.CjegException;

/**
 ** @author yfhuang created at 2014-3-8
 */
public class DateJourney extends Journey<Departure> {

	private static final long serialVersionUID = 1L;

	@Override
	public long getDepartureTime() {
		return this.getFirst().getTime();
	}

	public void addDeparture(Departure dep) {
		this.add(dep);
	}

	@Override
	public long getArriveTime() {
		return this.getLast().getTime() + CjegApp.getAppconfig().getTau();
	}

	@Override
	public long getDelay() {
		if (this.size() != 0)
			return this.getArriveTime() - this.getDepartureTime();
		else
			return Long.MAX_VALUE;
	}

	@Override
	public DateJourney clone() {
		DateJourney clonejourney = new DateJourney();
		for (int j = 0; j < this.size(); j++) {
			Departure dep = this.get(j);
			Departure clonedep = dep.clone();
			clonejourney.add(clonedep);
		}
		return clonejourney;
	}

	@Override
	public void write(CodedBuffer out) {

		// hop count
		out.writeInt(this.getHopCount());
		// left bracket
		out.writeByte(LBRACKET);
		// edge
		for (int i = 0; i < this.size(); i++) {
			Departure dep = this.get(i);
			dep.write(out);
		}
		// right bracket
		out.writeByte(RBRACKET);

	}

	public DateJourney(CodedInputStream cis) throws IOException{
		int hopcount = cis.readInt();
		byte lbracket = cis.readByte();
		if (lbracket != LBRACKET)
			throw new CjegException(
					"should be lbracket in DateJourney read method");
		for (int i = 0; i < hopcount; i++) {
			Departure dep = new Departure(cis);
			this.addDeparture(dep);
		}
		byte rbracket = cis.readByte();
		if (rbracket != RBRACKET)
			throw new CjegException(
					"should be Rbracket in DateJourney read method");
	}

	public DateJourney() {

	}

	public boolean containsAddress(int address) {
		for (int i = 0; i < this.size(); i++) {
			Edge edge = this.get(i).getEdge();
			if (edge.getFromaddr() == address || edge.getToaddr() == address)
				return true;
		}
		return false;
	}
}
