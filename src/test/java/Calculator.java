import java.util.Scanner;

public class Calculator {

    private static final char ADD = '+';
    private static final char SUB = '-';

    private String express;

    public Calculator(String express) {
        this.express = express;
    }

    public int calculate() {
        String expression = express.replaceAll("\\s*", "");
        StringBuilder numberBuilder = new StringBuilder();

        int value = 0;
        int prefix = 1;

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (48 <= c && c <= 57) {
                numberBuilder.append(c);
            } else {
                if (numberBuilder.length() > 0) {
                    value = value + prefix * Integer.valueOf(numberBuilder.toString());
                    numberBuilder.setLength(0);
                }

                if (c == SUB) {
                    prefix = -1;
                } else if (c == ADD){
                    prefix = 1;
                }
            }
        }

        value = doAdd(numberBuilder, value, prefix);
        return value;
    }

    private int doAdd(StringBuilder numberBuilder, int value, int prefix) {
        if (numberBuilder.length() > 0) {
            value = value + prefix * Integer.valueOf(numberBuilder.toString());
            numberBuilder.setLength(0);
        }
        return value;
    }

    public static void main(String[] args) {
        System.out.println(Calculator.class.getPackage());

        boolean shutdown = false;
        while (!shutdown) {
            Scanner sc = new Scanner(System.in);
            String express = sc.nextLine();
            Calculator calculator = new Calculator(express);
            int result = calculator.calculate();
            System.out.println("Express: " + express + " result: " + result);
        }
    }
}
