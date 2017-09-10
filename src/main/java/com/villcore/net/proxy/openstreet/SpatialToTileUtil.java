package com.villcore.net.proxy.openstreet;

import static java.lang.Math.pow;

public class SpatialToTileUtil {
    private static final String TILE_SERVER = "http://c.tile.openstreetmap.org/";

    public static void main(String[] args) {
        int[] offsets = new int[]{
                1, //9
                2, //10
                3, //11
                7, //12
                15, //13
                30, //14
                60, //15
                120, //16
                240, //17
                480, //18
                960 //19
        };
        //亦庄地区19级别
        //116.4477, 39.8423, 116.6333, 39.7121
        double minlat=38.7627000, minlon=114.4281000, maxlat=40.6556000, maxlon=118.7402000;
        int level = 9;

        //http://a.tile.openstreetmap.org/10/843/390.png

        for(; level <= 19; level++) {
            System.out.println("====================================");
            int offsetX = level - 8;
            offsetX = 0;

            int[] tiles = getTileIndex(minlon, minlat, level);
            System.out.printf("%d, %d\n", tiles[0] - offsets[level - 9], tiles[1]);

            int[] tiles2 = getTileIndex(maxlon, maxlat, level);
            System.out.printf("%d, %d\n", tiles2[0] + offsets[level - 9], tiles2[1]);

            //6694, 3137
            //6802, 3081
            int minTileX = tiles[0];
            int maxTileX = tiles2[0];

            int minTileY = tiles2[1];
            int maxTileY = tiles[1];

//            for(int i = minTileX; i < maxTileX; i++) {
//                for(int j = minTileY; j < maxTileY; j++) {
//                    String href = TILE_SERVER + level + "/" + i + "/" + j + ".png";
//                    System.out.println(href);
//                }
//            }
            //http://c.tile.openstreetmap.org/10/844/388.png
        }
    }

    public static int[] getTileIndex(double lng, double lat, double level) {
        int tileX = long2tilex(lng, (int) level);
        int tileY = lat2tiley(lat, (int) level);
        return new int[]{tileX, tileY};
    }

    public static int long2tilex(double lon, int z) {
        return (int)(Math.floor((lon + 180.0) / 360.0 * pow(2.0, z)));
    }

    public static int lat2tiley(double lat, int z) {
        return (int)(Math.floor((1.0 - Math.log( Math.tan(lat * Math.PI/180.0) + 1.0 / Math.cos(lat * Math.PI/180.0)) / Math.PI) / 2.0 * pow(2.0, z)));
    }
}
