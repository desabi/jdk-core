package com.desabisc.dates.cformatting;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.Month;

@Slf4j
public class AGetData {
    public static void main(String[] args) {

        // The date and time classes supports many methods to get data out of them
        LocalDate date = LocalDate.of(2020, Month.OCTOBER, 20);

        log.info("date.getDayOfWeek() -> {}", date.getDayOfWeek()); //TUESDAY
        log.info("date.getMonth() -> {}", date.getMonth()); // OCTOBER
        log.info("date.getYear() -> {}", date.getYear()); // 2020
        log.info("date.getDayOfYear() -> {}", date.getDayOfYear()); // 294
    }
}
