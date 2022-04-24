package jcog.data.bit;

public enum FixedPoint {
	;

	public static final int Range8 = Byte.MAX_VALUE*2 + 1;
	public static final int Range16 = Short.MAX_VALUE*2 + 1;
	public static final int Range24 = (1<<24)*2 + 1;

	public static final double Epsilon8 = 1f / (Range8 - 1);
	public static final double Epsilon16 = 1f / (Range16 - 1);


//	public static float unitShortToFloat(long y) {
//		return y / Range16;
//	}

	public static float unitShortToFloat(/*int*/float y) {
		return y / Range16;
	}
	public static float unit24ToFloat(/*int*/float y) {
		return y / Range24;
	}
	public static float unitByteToFloat(/*int*/float y) {
		return y / Range8;
	}

	public static int unitShort(float x) {
		return (int) (x * Range16);
	}

	public static int unitShort(double x) {
		return (int) (x * Range16);
	}

	public static int unitByte(double x) {
		return (int) (x * Range8);
	}

}