package org.example.pro.tools;

public class ValidationUtils {


    public static boolean isEmailFormat(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        String e = email.replace(" ", "");
        String numeric = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return e.matches(numeric);
    }


    public static boolean hasTwoUppercaseLetters(String input) {
        int uppercaseCount = 0;

        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            if (Character.isUpperCase(ch)) {
                uppercaseCount++;
                if (uppercaseCount > 2) {
                    return false;
                }
            }
        }

        return uppercaseCount == 2;
    }
}
