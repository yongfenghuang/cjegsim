package input;

import java.util.List;

/**
 ** @author yfhuang created at 2014-2-26
 */
public interface EventsReader {
	public List<ConnectionEvent> readEvents(int linenumber);
	public void close();
}
