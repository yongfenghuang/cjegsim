package core;

import java.io.IOException;

import output.CodedBuffer;
import output.CodedInputStream;
import tool.Log;
import app.CjegApp;
import app.CjegException;

/**
 ** @author yfhuang created at 2014-3-8
 */

public class HopJourney extends Journey<Edge> {

	private static final long serialVersionUID = 1L;
	private long arrivetime;

	public void setArrivetime(long arrivetime) {
		this.arrivetime = arrivetime;
	}

	public long getDepartureTime() {
		return arrivetime - CjegApp.getAppconfig().getTau()
				* this.getHopCount();
	}

	public long getArriveTime() {
		return arrivetime;
	}

	@Override
	public long getDelay() {
		return CjegApp.getAppconfig().getTau() * this.getHopCount();
	}

	public JourneyType getJtype() {
		return JourneyType.DIRECT;
	}

	public void addHop(Edge edge) {
		this.add(edge);
	}

	@Override
	public HopJourney clone() {
		HopJourney clonejourney = new HopJourney();
		for (int j = 0; j < this.size(); j++) {
			Edge thisedge = this.get(j);
			Edge thatedge = thisedge.clone();
			clonejourney.add(thatedge);
		}
		return clonejourney;
	}

	//filter duplicate address if input a-b-c-d-c output a-b-c if filtered
	public int filterDuplicateAddress(int address){
		int index=-1;
		for (int i = 0; i < this.size(); i++) {
			Edge edge = this.get(i);
			if (edge.getFromaddr() == address || edge.getToaddr() == address)
			{
				index=i;
				break;
			}
		}
		
		if (index>=0){
			//Log.writeln(index+" "+toString(), 1000);
			int j=this.size()-1;
			while(j>=index){
				this.remove(j);
				j--;
			}
			//Log.writeln(index+" "+toString(), 1000);
		}
		
		return index;
	}

	// containAddress return the location else return -1
	public boolean containsAddress(int address) {
		for (int i = 0; i < this.size(); i++) {
			Edge edge = this.get(i);
			if (edge.getFromaddr() == address || edge.getToaddr() == address)
				return true;
		}
		return false;
	}

	public boolean equalsExceptLastHop(HopJourney comparejourney) {
		if (this.size() != comparejourney.size() - 1)
			return false;
		for (int i = 0; i < this.size(); i++) {
			Edge thisedge = this.get(i);
			Edge thatedge = comparejourney.get(i);
			if (!thisedge.equals(thatedge))
				return false;
		}
		return true;
	}

	// @Override
	public void write(CodedBuffer out) {

		// hop count
		out.writeInt(this.getHopCount());
		// left bracket
		out.writeByte(LBRACKET);
		// edge
		for (int i = 0; i < this.size(); i++) {
			Edge edge = this.get(i);
			edge.write(out);
		}
		// right bracket
		out.writeByte(RBRACKET);

	}

	public HopJourney() {
	}

	public HopJourney(CodedInputStream cis) throws IOException {
		int hopcount = cis.readInt();
		byte lbracket = cis.readByte();
		if (lbracket != LBRACKET)
			throw new CjegException(
					"should be lbracket in HopJourney read method");
		for (int i = 0; i < hopcount; i++) {
			Edge edge = new Edge(cis);
			this.addHop(edge);
		}
		byte rbracket = cis.readByte();
		if (rbracket != RBRACKET)
			throw new CjegException(
					"should be rbracket in HopJourney read method");
	}
}
