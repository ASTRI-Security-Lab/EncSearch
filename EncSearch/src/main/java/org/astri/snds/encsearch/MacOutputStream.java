package org.astri.snds.encsearch;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.crypto.Mac;

public class MacOutputStream extends FilterOutputStream {
	
	private Mac mac;

	public MacOutputStream(OutputStream out, Mac mac_) {
		super(out);
		mac = mac_;
	}

    @Override
    public void write(int b) throws IOException {
        out.write(b);
		mac.update((byte)b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
		mac.update(b, off, len);
    }
}
