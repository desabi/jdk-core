package com.desabisc.dates.bofmethod;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;

@Slf4j
public class OfMethodEg {
    public static void main(String[] args) {

        // LocalDate
        log.info("LocalDate.of(2020, Month.OCTOBER, 20) -> {}", LocalDate.of(2020, Month.OCTOBER, 20));
        log.info("LocalDate.of(2020, 10, 20) -> {}", LocalDate.of(2020, 10, 20));

        // LocalTime
        log.info("LocalTime.of(6, 15) -> {}", LocalTime.of(6, 15)); // hour and minute
        log.info("LocalTime.of(6, 15, 30) -> {}", LocalTime.of(6, 15, 30)); // + seconds
        log.info("LocalTime.of(6, 15, 30, 200) -> {}", LocalTime.of(6, 15, 30, 200)); // +

        // LocalDateTime
        var dt1 = LocalDateTime.of(2020, Month.OCTOBER, 20, 6, 15, 30);

        LocalDate date = LocalDate.of(2020, Month.OCTOBER,20);
        LocalTime time = LocalTime.of(6, 15);
        var dt2 = LocalDateTime.of(date, time);

        log.info("dt1: {}", dt1);
        log.info("dt2: {}", dt2);
    }
}
