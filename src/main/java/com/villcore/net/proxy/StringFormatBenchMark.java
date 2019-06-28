package com.villcore.net.proxy;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * created by WangTao on 2019-06-04
 */
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 3)
@Threads(6)
@Fork(2)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class StringFormatBenchMark {

    private static final int circleCount = 10 * 10000;

    private static final List<String> list = new ArrayList<>(180 * 10000);

    private static final Set<String> set = new HashSet<>(180 * 10000);

    @Setup(Level.Trial)
    public void setup() {
        for (int i = 0; i < 180 * 10000; i++) {
            list.add("ochvq0LLPnJ_rpuh6WXlaqAliPtw" + i);
            set.add("ochvq0LLPnJ_rpuh6WXlaqAliPtw" + i);
        }
        long start = System.currentTimeMillis();
        list.contains("a");
        System.out.println(System.currentTimeMillis() - start);
    }

    @TearDown(Level.Trial)
    public void teardown() {
        list.clear();
        set.clear();
    }
    //    @Benchmark
//    public void testStringFormat() {
//        for (int i = 0; i < circleCount; i++) {
//            String.format("test %s format, seq %d.", "string_format", i);
//        }
//    }
//
//    @Benchmark
//    public void testMessageFormat() {
//        for (int i = 0; i < circleCount; i++) {
//            MessageFormat.format("test {0} format, seq {1}.", "message_format", i);
//        }
//    }
//
//    @Benchmark
//    public void testSlf4jMessageFormat() {
//        for (int i = 0; i < circleCount; i++) {
//            MessageFormatter.format("test {} format, seq {}.", "slf4j_message_format", i).getMessage();
//        }
//    }

    @Benchmark
    public void testListContains() {
        boolean exist = list.contains("xx");
    }

    @Benchmark
    public void testSetContains() {
        boolean exist = set.contains("xx");
    }

    public static void main(String[] args) throws RunnerException {
        System.out.println(String.format("%dw", 100));
//        new StringFormatBenchMark().testStringFormat();
//        new StringFormatBenchMark().testMessageFormat();
//        new StringFormatBenchMark().testSlf4jMessageFormat();

//        Options options = new OptionsBuilder().include(StringFormatBenchMark.class.getSimpleName())
//                .output("/Users/wangtao/IdeaProjects/net-proxy/benchmark.log").build();
//        new Runner(options).run();

        List<Integer> habitOwnerLevels = Arrays.asList(1, 2,100, 3, 6, 8);
        habitOwnerLevels.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        });
        System.out.println(habitOwnerLevels.toString());
    }
}
