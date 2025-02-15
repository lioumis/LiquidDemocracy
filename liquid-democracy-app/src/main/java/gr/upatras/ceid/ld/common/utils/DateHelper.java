package gr.upatras.ceid.ld.common.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateHelper {
    public static final String SIMPLE_DATE_FORMAT = "yyyy-MM-dd";

    protected DateHelper() {
        super();
    }

    public static String toString(LocalDate localDate) {
        if (localDate == null) {
            return "";
        }
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(SIMPLE_DATE_FORMAT);
        return localDate.format(formatter);
    }

    public static LocalDate toLocalDate(String string) {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(SIMPLE_DATE_FORMAT);
        return LocalDate.parse(string, formatter);
    }

}
