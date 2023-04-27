package net.sf.image4j.io;

/**
 * Provides utility methods for endian conversions [big-endian to little-endian;
 * little-endian to big-endian].
 * 
 * @author Ian McDonagh
 */
public class EndianUtils {

	/**
	 * Reverses the byte order of the source <tt>short</tt> value
	 * 
	 * @param value
	 *            the source value
	 * @return the converted value
	 */
	public static short swapShort(short value) {
		return (short) (((value & 0xFF00) >> 8) | ((value & 0x00FF) << 8));
	}

	/**
	 * Reverses the byte order of the source <tt>int</tt> value
	 * 
	 * @param value
	 *            the source value
	 * @return the converted value
	 */
	public static int swapInteger(int value) {
		return ((value & 0xFF000000) >> 24) | ((value & 0x00FF0000) >> 8)
				| ((value & 0x0000FF00) << 8) | ((value & 0x000000FF) << 24);
	}

	/**
	 * Reverses the byte order of the source <tt>long</tt> value
	 * 
	 * @param value
	 *            the source value
	 * @return the converted value
	 */
	public static long swapLong(long value) {
		return ((value & 0xFF00000000000000L) >> 56)
				| ((value & 0x00FF000000000000L) >> 40)
				| ((value & 0x0000FF0000000000L) >> 24)
				| ((value & 0x000000FF00000000L) >> 8)
				| ((value & 0x00000000FF000000L) << 8)
				| ((value & 0x0000000000FF0000L) << 24)
				| ((value & 0x000000000000FF00L) << 40)
				| ((value & 0x00000000000000FFL) << 56);
	}

	/**
	 * Reverses the byte order of the source <tt>float</tt> value
	 * 
	 * @param value
	 *            the source value
	 * @return the converted value
	 */
	public static float swapFloat(float value) {
		int i = Float.floatToIntBits(value);
		i = swapInteger(i);
		return Float.intBitsToFloat(i);
	}

	/**
	 * Reverses the byte order of the source <tt>double</tt> value
	 * 
	 * @param value
	 *            the source value
	 * @return the converted value
	 */
	public static double swapDouble(double value) {
		long l = Double.doubleToLongBits(value);
		l = swapLong(l);
		return Double.longBitsToDouble(l);
	}

	public static String toHexString(int i, boolean littleEndian, int bytes) {
		if (littleEndian) {
			i = swapInteger(i);
		}
		StringBuilder sb = new StringBuilder();
		sb.append(Integer.toHexString(i));
		if (sb.length() % 2 != 0) {
			sb.insert(0, '0');
		}
		while (sb.length() < bytes * 2) {
			sb.insert(0, "00");
		}
		return sb.toString();
	}

	public static StringBuilder toCharString(StringBuilder sb, int i, int bytes, char def) {
		int shift = 24;
		for (int j = 0; j < bytes; j++) {
			int b = (i >> shift) & 0xFF;
			char c = b < 32 ? def : (char) b;
			sb.append(c);
			shift -= 8;
		}
		return sb;
	}

	public static String toInfoString(int info) {
		StringBuilder sb = new StringBuilder();
		sb.append("Decimal: ").append(info);
		sb.append(", Hex BE: ").append(toHexString(info, false, 4));
		sb.append(", Hex LE: ").append(toHexString(info, true, 4));
		sb.append(", String BE: [");
		sb = toCharString(sb, info, 4, '.');
		sb.append(']');
		sb.append(", String LE: [");
		sb = toCharString(sb, swapInteger(info), 4, '.');
		sb.append(']');
		return sb.toString();
	}
}