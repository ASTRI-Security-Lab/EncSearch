package org.astri.snds.encsearch;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.crypto.Mac;

public class MacInputStream extends FilterInputStream {
	
	private Mac mac;

	protected MacInputStream(InputStream in, Mac mac_) {
		super(in);
		mac = mac_;
	}
	
    public int read() throws IOException {
        int ch = in.read();
        if (ch != -1) {
            mac.update((byte)ch);
        }
        return ch;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        int result = in.read(b, off, len);
        if (result != -1) {
            mac.update(b, off, result);
        }
        return result;
    }
}
