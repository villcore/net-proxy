import com.sun.btrace.AnyType;
import com.sun.btrace.annotations.*;

import static com.sun.btrace.BTraceUtils.*;

@BTrace
public class BtraceUtils {
    @OnMethod(clazz = "/org\\.springframework\\.web\\.servlet\\.mvc\\.method\\.annotation\\.RequestResponseBodyMethodProcessor/", method = "/readWithMessageConverters/", location = @Location(Kind.RETURN))
    public static void func2(@ProbeClassName String probeClass, @ProbeMethodName String probeMethod, @Return AnyType result, @Duration long time, AnyType[] args) {
        if (time / 1000000 > 100) {
            println(Time.timestamp("yyyy-MM-dd HH:mm:ss") + "   " + probeClass + "#" + probeMethod + ":" + time / 1000000);
        }
    }
}