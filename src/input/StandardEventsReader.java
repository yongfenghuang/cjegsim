package input;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import app.CjegException;

import tool.Log;

/**
 ** @author yfhuang created at 2014-2-26
 */

public class StandardEventsReader implements EventsReader {
	private Scanner scanner;
	// read last time but not add to List
	private String bufferline;
	private boolean isbuffer = false;

	// the time of current event is equal to the one of previous event
	boolean equaltoprevious = true;
	long previouseventtime = Long.MIN_VALUE;

	public StandardEventsReader(File eventsfile) {
		try {
			this.scanner = new Scanner(eventsfile);
		} catch (FileNotFoundException e) {
			Log.writeln("events file can't be found",1);
			e.printStackTrace();
		}
	}

	// return linenumber events but until the time of last event not equal to
	// previous one
	public List<ConnectionEvent> readEvents(int linenumber) {
		List<ConnectionEvent> celist = new ArrayList<ConnectionEvent>(
				linenumber);
		int eventsread = 0;

		// skip empty and comment lines

		Pattern skippattern = Pattern.compile("(#.*)|(^\\s*$)");

		if (isbuffer) {
			//Log.write("previousline:" + bufferline);
			ConnectionEvent conevent = lineToConEvent(bufferline);
			celist.add(conevent);
			isbuffer = false;
		}

		while ((eventsread <= linenumber || equaltoprevious)
				&& scanner.hasNextLine()) {
			String eventline = scanner.nextLine();
			//Log.write("eventline:" + eventline);
			if (skippattern.matcher(eventline).matches()) {
				// skip empty and comment lines
				Log.writeln("read Events skip one line",1);
				continue;
			}
			Scanner linescan = new Scanner(eventline);
			long etime = 0;
			int fromaddr;
			int toaddr;
			String conntype;
			boolean isup;
			ConnectionEvent conevent = null;

			etime = linescan.nextLong();
			if (etime == previouseventtime)
				equaltoprevious = true;
			else
				equaltoprevious = false;
			fromaddr = linescan.nextInt();
			toaddr = linescan.nextInt();
			conntype = linescan.next();
			if (conntype.equals("UP"))
				isup = true;
			else if (conntype.equals("DOWN"))
				isup = false;
			else
				throw new CjegException("read crawdadfile error:there is no such conntype");
			conevent = new ConnectionEvent(fromaddr, toaddr, isup, etime);

			previouseventtime = etime;
			eventsread++;

			if (eventsread > linenumber && !equaltoprevious) {
				bufferline = eventline;
				isbuffer = true;
			} else {
				celist.add(conevent);
			}
		}
		return celist;
	}

	private ConnectionEvent lineToConEvent(String eventline) {
		Scanner linescan = new Scanner(eventline);
		long etime;
		int fromaddr;
		int toaddr;
		String conntype;
		boolean isup;
		ConnectionEvent conevent = null;

		etime = linescan.nextLong();
		previouseventtime = etime;
		fromaddr = linescan.nextInt();
		toaddr = linescan.nextInt();
		conntype = linescan.next();
		if (conntype.equals("UP"))
			isup = true;
		else if (conntype.equals("DOWN"))
			isup = false;
		else
			throw new CjegException(
					"read crawdad file error:there is no such conntype");
		conevent = new ConnectionEvent(fromaddr, toaddr, isup, etime);

		return conevent;
	}

	public void close() {
		this.scanner.close();
	}
}
