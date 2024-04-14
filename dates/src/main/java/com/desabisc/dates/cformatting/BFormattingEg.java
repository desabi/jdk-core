package com.desabisc.dates.cformatting;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;

@Slf4j
public class BFormattingEg {
    public static void main(String[] args) {
        // Java provides a class called DateTimeFormatter to display standard formats
        LocalDate date = LocalDate.of(2020, Month.OCTOBER, 20);
        LocalTime time = LocalTime.of(11, 12, 34);
        LocalDateTime dateTime = LocalDateTime.of(date, time);

        String dateFormat = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String timeFormat = time.format(DateTimeFormatter.ISO_LOCAL_TIME);
        String dateTimeFormat = dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        log.info("date.format(DateTimeFormatter.ISO_LOCAL_DATE) -> {}", dateFormat);
        log.info("time.format(DateTimeFormatter.ISO_LOCAL_TIME) -> {}", timeFormat);
        log.info("dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) -> {}", dateTimeFormat);

        // the DateTimeFormatter will throw an exception if it encounters an incompatible type.
    }
}
