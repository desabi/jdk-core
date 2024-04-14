package com.desabisc.dates.cformatting;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;

@Slf4j
public class CFormatting {
    public static void main(String[] args) {

        LocalDate date = LocalDate.of(2020, Month.OCTOBER, 20);
        LocalTime time = LocalTime.of(11, 12, 34);
        LocalDateTime dateTime = LocalDateTime.of(date, time);

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm");

        String dateTimeFormat = dateTime.format(dateTimeFormatter);
        log.info("dateTime.format(DateTimeFormatter.ofPattern(\"MMMM dd, yyyy 'at' hh:mm\")) -> {}", dateTimeFormat);
    }
}
