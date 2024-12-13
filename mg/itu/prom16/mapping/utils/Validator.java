package mg.itu.prom16.mapping.utils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

import mg.itu.prom16.annotation.checker.DateForm;
import mg.itu.prom16.annotation.checker.EMail;
import mg.itu.prom16.annotation.checker.Numeric;
import mg.itu.prom16.annotation.checker.Required;

public class Validator {

    public static Object validation (Field attribut, Object value)throws Exception{
        if(attribut.isAnnotationPresent(Required.class) && value == null){
            throw new Exception("The field '"+attribut.getName()+"' is required but null");
        }
        if(value == null){
            return null;
        }
        if(attribut.isAnnotationPresent(DateForm.class)){
            return validateDate(attribut, value);
        } 
        else if(attribut.isAnnotationPresent(EMail.class)){
            validateEmail(attribut, value);
        }
        else if(attribut.isAnnotationPresent(Numeric.class)){
            return validateNumeric(attribut, value);
        }
        return value;
    }

    public static Object validateDate(Field attribut, Object value)throws Exception{
        DateForm annotation = attribut.getAnnotation(DateForm.class);
        String format = annotation.value();
        if (value instanceof java.util.Date) {
            SimpleDateFormat sdf = new SimpleDateFormat(format);

            // System.out.println("Avant : "+(java.util.Date) value);

            String formattedDate = sdf.format((java.util.Date) value);
            // System.out.println("Apres : "+formattedDate);

            if (value instanceof java.sql.Date) {
                java.sql.Date sqlDate = new java.sql.Date(sdf.parse(formattedDate).getTime());
                return sqlDate;
            } else {
                java.util.Date utilDate = sdf.parse(formattedDate);
                return utilDate;
            }
        }
        return value;
    }

    public static void validateEmail(Field attribut, Object value) throws Exception{
        if (value instanceof String) {
            String email = (String) value;
            if (!isValidEmail(email)) {
                throw new Exception("The field '" + attribut.getName() + "' is not a valid email address: " + email);
            }
        } else {
            throw new Exception("The field '" + attribut.getName() + "' should be a String containing a valid email address");
        }
    }

    private static boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }

    public static Object validateNumeric(Field attribut, Object value)throws Exception{
        if (value instanceof Number) {
            Numeric annotation = attribut.getAnnotation(Numeric.class);
            BigDecimal bigDecimal = new BigDecimal(value.toString());

            int card = NumberUtils.cardinalNumber(bigDecimal, (Number) value);
            if(card > annotation.cardinal()){
                throw new Exception("The field '" + attribut.getName() + "' exceeds the maximum number of digits (" 
                + annotation.cardinal() + "). Current value: " + value.toString());
            }

            bigDecimal = bigDecimal.setScale(annotation.scale(), RoundingMode.HALF_UP);

            return NumberUtils.BigDecimalToNumber(bigDecimal, (Number) value);
        } else {
            throw new Exception("The field '" + attribut.getName() + "' must be a numerical value");
        }
    }

}
