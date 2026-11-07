package ru.home_pharmacy.home_pharmacy.util;

/**
 * Утилиты для работы с российскими номерами телефонов:
 * валидация по маске вида «8 900 123-45-67» / «+7 900 123-45-67» и приведение
 * к единому каноническому формату (11 цифр, начинается с 7).
 */
public final class PhoneUtils {

    public static final String PHONE_REGEX =
            "^\\+?7[\\s\\-]?\\(?\\d{3}\\)?[\\s\\-]?\\d{3}[\\s\\-]?\\d{2}[\\s\\-]?\\d{2}$"
            + "|^8[\\s\\-]?\\(?\\d{3}\\)?[\\s\\-]?\\d{3}[\\s\\-]?\\d{2}[\\s\\-]?\\d{2}$";

    private PhoneUtils() {
    }

    /**
     * Приводит номер к каноническому виду: 11 цифр, начинается с 7.
     * Пример: «8 900 123-45-67» и «+79001234567» -> «79001234567».
     */
    public static String normalize(String phone) {
        if (phone == null) {
            return null;
        }
        String digits = phone.replaceAll("\\D", "");
        if (digits.length() == 11 && digits.startsWith("8")) {
            return "7" + digits.substring(1);
        }
        if (digits.length() == 10) {
            return "7" + digits;
        }
        return digits;
    }
}