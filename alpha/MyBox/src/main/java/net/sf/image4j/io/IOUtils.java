package net.sf.image4j.io;

import java.io.*;

public class IOUtils {

	public static int skip(InputStream in, int count, boolean strict) throws IOException {
		int skipped = 0;
		while (skipped < count) {
			int b = in.read();
			if (b == -1) {
				break;
			}
			skipped++;
		}
		if (skipped < count && strict) {
			throw new EOFException("Failed to skip " + count
					+ " bytes in input");
		}
		return skipped;
	}

}
