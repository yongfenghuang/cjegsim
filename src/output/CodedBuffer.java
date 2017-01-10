/*******************************************************************************
 * This file is copied from part of DITL                                                  *
 *                                                                             *
 * Copyright (C) 2011-2012 John Whitbeck <john@whitbeck.fr>                    *
 *                                                                             *
 *******************************************************************************/
package output;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

public class CodedBuffer {

    private final static int DEFAULT_BUFFER_SIZE = 4096;
    private final static int MAX_BUFFER_SIZE = (64 << 20); // 64MB

    private byte[] buffer;
    private int position = 0;

    public CodedBuffer() {
        buffer = new byte[DEFAULT_BUFFER_SIZE];
    }

    public CodedBuffer(int bufferSize) {
        buffer = new byte[bufferSize];
    }

    public int flush(OutputStream os) throws IOException {
        os.write(buffer, 0, position);
        int bytes_written = position;
        position = 0;
        return bytes_written;
    }

    public int bytesInBuffer() {
        return position;
    }

    public boolean isEmpty() {
        return position == 0;
    }

    public void writeByte(final int b) {
        if (position == buffer.length) { // buffer full!
            increaseBufferSize();
        }
        buffer[position++] = (byte) b;
    }

    public void writeInt(int value) {
        while (true) {
            if ((value & ~0x7F) == 0) {
                writeByte(value);
                return;
            } else {
                writeByte((value & 0x7F) | 0x80);
                value >>>= 7;
            }
        }
    }

    public void writeSInt(int value) {
        writeInt(encodeZigZag32(value));
    }

    public void writeLong(long value) {
        while (true) {
            // ~0x7FL = FFFF FFFF FFFF FF80
            // 0L = 0x0000000000000000
            // value = 0x00000000000000ab
            // a is lower or equal to 7 and b is any
            if ((value & ~0x7FL) == 0) { // ie. if value is not greater than 127
                                         // write directly to buffer
                writeByte((int) value);
                return;
            } else {
                writeByte(((int) value & 0x7F) | 0x80);// else write last 7 bit
                                                       // | 1000000 to buffer
                value >>>= 7; // unsigned right shift 7 positition
            }
        }
    }

    public void writeSLong(final long value) {
        writeLong(encodeZigZag64(value));
    }

    public void writeDouble(final double value) {
        writeRawLittleEndian64(Double.doubleToRawLongBits(value));
    }

    public void writeSIntSet(final Set<Integer> integers) {
        writeInt(integers.size());
        for (Integer i : integers)
            writeSInt(i);
    }

    private int encodeZigZag32(final int n) {
        return (n << 1) ^ (n >> 31);
    }

    private long encodeZigZag64(final long n) {
        return (n << 1) ^ (n >> 63);
    }

    private void writeRawLittleEndian64(final long value) {
        writeByte((int) (value) & 0xFF);
        writeByte((int) (value >> 8) & 0xFF);
        writeByte((int) (value >> 16) & 0xFF);
        writeByte((int) (value >> 24) & 0xFF);
        writeByte((int) (value >> 32) & 0xFF);
        writeByte((int) (value >> 40) & 0xFF);
        writeByte((int) (value >> 48) & 0xFF);
        writeByte((int) (value >> 56) & 0xFF);
    }

    private void increaseBufferSize() {
        int buffer_size = buffer.length << 1;
        if (buffer_size <= MAX_BUFFER_SIZE) {
            byte[] old_buffer = buffer;
            buffer = new byte[buffer_size];
            System.arraycopy(old_buffer, 0, buffer, 0, old_buffer.length);
        } else {
            throw new IllegalStateException("Exceeded max buffer size!");
        }
    }

}
