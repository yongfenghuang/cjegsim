package core;

import java.io.IOException;

import output.CodedBuffer;
import output.CodedInputStream;
import tool.AppUtil;

/**
 ** @author yfhuang created at 2014-3-4
 *  Edge here has direction represent two nodes contact directly
 */
public class Edge implements JourneyElement{
	private int fromaddr;
	private int toaddr;
	
	public int getFromaddr() {
		return fromaddr;
	}
	
	public String getFromStr(){
		return AppUtil.getNodesFromAddress(fromaddr).getNodeStr();
	}
	
	public String getToStr(){
		return AppUtil.getNodesFromAddress(toaddr).getNodeStr();
	}

	public int getToaddr() {
		return toaddr;
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
		Edge edge = (Edge) o;
		return edge.fromaddr == this.fromaddr && edge.toaddr == this.toaddr;
	}
	
	public Edge clone(){
		Edge cloneedge = new Edge(this.fromaddr,this.toaddr);
		return cloneedge;
	}
	
	public void write(CodedBuffer out){
		out.writeInt(fromaddr);
		out.writeInt(toaddr);
	}
	
	public Edge(int _fromaddr, int _toaddr) {
		this.fromaddr = _fromaddr;
		this.toaddr = _toaddr;
	}
	
	public Edge(CodedInputStream cis) throws IOException{
		this.fromaddr=cis.readInt();
		this.toaddr=cis.readInt();
	}
	
	public String toString(){
		StringBuffer strb=new StringBuffer();
		strb.append("(");
		strb.append(AppUtil.getNodesFromAddress(this.getFromaddr()).getNodeStr()+" ");
		strb.append(AppUtil.getNodesFromAddress(this.getToaddr()).getNodeStr()+")");
		return strb.toString();
	}
	
	public String toDbString(){
		StringBuffer strb=new StringBuffer();
		//strb.append("(");
		strb.append(this.getFromaddr());
		//strb.append(this.getToaddr()+")");
		return strb.toString();
	}
}
