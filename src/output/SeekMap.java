/*******************************************************************************
 * This file is copied from part of DITL                                                  *
 *                                                                             *
 * Copyright (C) 2011-2012 John Whitbeck <john@whitbeck.fr>                    *
 *                                                                             *
 *******************************************************************************/
package output;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.TreeMap;

import app.ConstantConfig;

import core.Edge;
import tool.Log;

public final class SeekMap {

    private final TreeMap<ClusterIndex, Long> byteoffsets = new TreeMap<ClusterIndex, Long>(new ClusterIndexComparator());
    
    private SeekMap() {
    }

    public static SeekMap open(InputStream is,Edge edge) throws IOException {
        SeekMap sm = new SeekMap();
        CodedInputStream in = new CodedInputStream(new BufferedInputStream(is));
        while (!in.isAtEnd()) {
        	int fromaddr=in.readInt();
        	int toaddr=in.readInt();
        	long stime=in.readSLong();
        	long etime=in.readSLong();
        	long offset=in.readLong();
        	if (fromaddr==edge.getFromaddr() && toaddr==edge.getToaddr()){
        		ClusterIndex tpair=new ClusterIndex(stime,etime);
        		sm.byteoffsets.put(tpair, offset);
        	}
        }
        in.close();
        return sm;
    }
    
    public void printSeekMap(){
    	Log.writeln("size:"+byteoffsets.size(), 1000);
    	for(Map.Entry<ClusterIndex, Long> entry:byteoffsets.entrySet()){
    		ClusterIndex key=entry.getKey();
    		Long value=entry.getValue();
    		Log.writeln("stime:"+key.getStime()+" etime:"+key.getEtime()+" value:"+value, 1000);
    	}
    }
 
    public long getOffset(long time) {
    	ClusterIndex tpair=new ClusterIndex(time,time);
        Map.Entry<ClusterIndex, Long> e = byteoffsets.floorEntry(tpair);
        if (e == null)
            return Long.MIN_VALUE;
        return e.getValue();
    }
    
    public static final class Writer {
        private final OutputStream _os;
        private final CodedBuffer _buffer;
        private long indexnumber=0;
        

        public Writer(OutputStream out) {
            _os = new BufferedOutputStream(out);
            _buffer = new CodedBuffer(30);
        }

        public void append(Edge _edge,long stime,long etime, long byteoffset) throws IOException {
        	_buffer.writeInt(_edge.getFromaddr());
        	_buffer.writeInt(_edge.getToaddr());
            _buffer.writeSLong(stime);
            _buffer.writeSLong(etime);
            _buffer.writeLong(byteoffset);
            _buffer.flush(_os);
            indexnumber++;
            if (indexnumber % ConstantConfig.INDEX_FLUSH_SIZE==0) _os.flush();
        }

        public void close() throws IOException {
            _os.close();
        }

    }
    
    public TreeMap<ClusterIndex, Long> getByteOffsets() {
		return byteoffsets;
	}
}
