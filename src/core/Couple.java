package core;

/**
 * @author yfhuang created at 2014-3-5
 * Couple here has direction represent nodes pair relation(two nodes contact directly or indirectly)
 * for example CrJourneyCluster evolving graph appear in all couples of system. Such nodes may haven't contact directly. 
 */
public class Couple {
	private int fromaddr;
	private int toaddr;

	public Couple(int _fromaddr, int _toaddr) {
		this.fromaddr = _fromaddr;
		this.toaddr = _toaddr;
	}

	// reference to effective java second edition page 32
	public int hashCode() {
		int result = 17;
		result = 37 * result + fromaddr;
		result = 37 * result + toaddr;
		return result;
	}

	// reference to effective java second page 32
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof Edge))
			return false;
		Couple couple = (Couple) o;
		return couple.fromaddr == this.fromaddr && couple.toaddr == this.toaddr;
	}
}
