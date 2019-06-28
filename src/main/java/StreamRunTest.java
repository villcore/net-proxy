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
        List<String> stringList = new ArrayList<>(Arrays.asList("1", "2", "3"));
        System.out.println(stringList.iterator().getClass());
        for (String str : stringList) {
            if ("2".equals(str)) {
                stringList.remove(str);
            }
            System.out.println(str);
        }
    }
}
