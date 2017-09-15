//package com.villcore;
//
//import org.junit.Test;
//
//import java.util.stream.LongStream;
//
//public class StreamTest {
//    @Test
//    public void testStream() {
//        long[] result = new long[1];
//        for (int i = 0; i < 10; i++) {
//            result[0] = 0;
//            LongStream.range(0, 1000)
//                    .forEach(n -> result[0] = (result[0] + n) * n);
//            System.out.println("serial: " + result[0]);
//        }
//        for (int i = 0; i < 10; i++) {
//            result[0] = 0;
//            LongStream.range(0, 1000).parallel()
//                    .forEach(n -> result[0] = (result[0] + n) * n);
//            System.out.println("parallel: " + result[0]);
//        }
//
//        for (int i = 0; i < 10; i++) {
//            result[0] = 0;
//            LongStream.range(0, 1000).parallel().reduce()
//                    .forEachOrdered(n -> result[0] = (result[0] +
//                            n) * n);
//            System.out.println("parallel ordered: " + result[0]);
//        }
//    }
//}
