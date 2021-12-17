package jcog.nn.ntm.control;


import jcog.Util;

public enum UnitFactory {
    ;

    @Deprecated
    public static Unit[] vector(int vectorSize) {
        return Util.arrayOf(i -> new Unit(), new Unit[vectorSize]);
    }

    @Deprecated
    public static Unit[][] tensor2(int x, int y) {
        return Util.arrayOf(i -> vector(y), new Unit[x][]);
    }

    @Deprecated
    public static Unit[][][] tensor3(int x, int y, int z) {
        return Util.arrayOf(i -> tensor2(y, z), new Unit[x][][]);
    }

}