package mg.itu.prom16.mapping.utils;

import java.math.BigDecimal;

public class NumberUtils {
    public static int cardinalNumber (BigDecimal bigDecimal, Number number)throws Exception{
        if (bigDecimal.scale() <= 0) {
            return bigDecimal.toBigInteger().toString().length();
        }

        String numberStr = bigDecimal.toString();
        int decimalIndex = numberStr.indexOf('.');

        int integerDigits = decimalIndex;

        int fractionalDigits = numberStr.length() - decimalIndex - 1;

        return integerDigits + fractionalDigits;
    }

    public static Object BigDecimalToNumber(BigDecimal bigDecimal, Number number)throws Exception{
        if (number instanceof Integer) {
            return bigDecimal.intValue();
        } else if (number instanceof Long) {
            return bigDecimal.longValue();
        } else if (number instanceof Double) {
            return bigDecimal.doubleValue();
        } else if (number instanceof Float) {
            return bigDecimal.floatValue();
        } else {
            throw new Exception("Unsupported number type: " + number.getClass().getName());
        }
    }
}
