package com.desabisc.dates.cformatting;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;

@Slf4j
public class DFormattingEg {
    public static void main(String[] args) {

        LocalDateTime dateTime = LocalDateTime.of(2020, Month.OCTOBER, 20, 6, 15, 30);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss");

        log.info("dateTime.format(dateTimeFormatter) --> {}", dateTime.format(dateTimeFormatter));
        log.info("dateTimeFormatter.format(dateTime) --> {}", dateTimeFormatter.format(dateTime));
    }
}
