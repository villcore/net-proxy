import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * created by WangTao on 2019-06-25
 */
public class StreamRunTest {

    private static void testForeach() {
        System.out.println("Current Thread : " + currentThreadName());
        List<String> list = Arrays.asList("a", "b", "c");
        list.forEach(s -> {
            System.out.println("Current Thread : " + currentThreadName());
            System.out.println(s);
        });
    }

    private static String currentThreadName() {
        return Thread.currentThread().getName();
    }

    public static void main(String[] args) {
        String str = "0123456";
        System.out.println(str.substring(str.length() - 6));
    }
}
