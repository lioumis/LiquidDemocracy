package gr.upatras.ceid.ld.common.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateHelper {
    public static final String SIMPLE_DATE_FORMAT = "yyyy-MM-dd";

    protected DateHelper() {
        super();
    }

    public static String toString(LocalDateTime LocalDateTime) {
        if (LocalDateTime == null) {
            return "";
        }
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(SIMPLE_DATE_FORMAT);
        return LocalDateTime.format(formatter);
    }

    public static LocalDateTime startDateToLocalDateTime(String string) {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(SIMPLE_DATE_FORMAT);
        LocalDate localDate = LocalDate.parse(string, formatter);
        return localDate.atStartOfDay();
    }

    public static LocalDateTime endDateToLocalDateTime(String string) {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(SIMPLE_DATE_FORMAT);
        LocalDate localDate = LocalDate.parse(string, formatter);
        return localDate.atTime(23, 59, 59);
    }

}
