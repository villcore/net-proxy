package com.villcore.net.proxy.bio.pkg;

public class PkgConf {
    public static int getEndryptPackageHeaderLen() {
        //(int)(body size + inteferenct bytes size) / (int)body size / (long) userFlag / iv bytes /
        return 4 + 4 + 8 + getIvBytesLen();
    }

    public static int getIvBytesLen() {
        //encrypt size + iv + normal header len
        return 16;
    }

    public static int getDefaultPackageHeaderLen() {
        return 4 + 4 + 8 + 4;
    }

    public static float getInterferenceFactor() {
        return 0.0f;
    }

    public static int getTransferPackageHeaderLen() {
        return 4;
    }
}
