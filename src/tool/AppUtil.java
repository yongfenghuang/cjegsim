package tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;

import app.CjegApp;
import app.CjegException;


import core.HopJourney;
import core.Node;

/**
 ** @author yfhuang created at 2014-2-28
 */
public class AppUtil {
	private static TreeMap<Integer,Node> nodes_map=CjegApp.getInstance().getNodesMap();
	public AppUtil() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public static File getFile(String dir, String filename) {
		String separator = "/";
		return new File(dir + separator + filename);
	}
	
	public static String getFileStr(String dir, String filename) {
		String separator = "/";
		return new String(dir + separator + filename);
	}
	
	public static Node getNodesFromAddress(int address){
		return nodes_map.get(address);
	}
	
	public static void addtoNodesMap(int address,Node node){
		nodes_map.put(address, node);
	}
	
	public static String setsPrint(HashSet<Integer> nodesets){
		StringBuilder strb=new StringBuilder();
		strb.append("[ ");
		Iterator<Integer> setsite=nodesets.iterator();
		while (setsite.hasNext()){
			strb.append(AppUtil.getNodesFromAddress(setsite.next()).getNodeStr()+" ");
		}
		strb.append("]");
		return strb.toString();
	}
	
	public static int getLastHop(HopJourney _hopj){
		return _hopj.getLast().getFromaddr();
	}
	
	public static String getFileAsString(String pathname) {
		try {
			StringWriter sw = new StringWriter();
			BufferedReader bis = new BufferedReader(new InputStreamReader(
					getInputStream(pathname)));
			String line = null;
			while ((line = bis.readLine()) != null)
				sw.write(line);
			bis.close();
			return sw.toString();
		} catch (IOException e) {
			throw new CjegException("can't find file:"+pathname);
		}
	}
	
	public static InputStream getInputStream(String pathname) throws IOException {
		return new FileInputStream(pathname);
	}
	
	public static String getTracedir(String tracename,int tau){
		return tracename+"_"+tau;
	}

}
