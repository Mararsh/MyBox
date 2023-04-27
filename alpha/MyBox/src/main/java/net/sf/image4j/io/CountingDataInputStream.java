package net.sf.image4j.io;

import java.io.*;

public class CountingDataInputStream extends DataInputStream implements CountingDataInput {

	public CountingDataInputStream(InputStream in) {
		super(new CountingInputStream(in));
	}

	@Override
	public int getCount() {
		return ((CountingInputStream) in).getCount();
	}
	
	public int skip(int count, boolean strict) throws IOException {
		return IOUtils.skip(this, count, strict);
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" +in + ") ["+getCount()+"]";
	}
}
