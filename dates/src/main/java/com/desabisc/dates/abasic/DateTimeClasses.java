package com.desabisc.dates.abasic;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;

@Slf4j
public class DateTimeClasses {
    public static void main(String[] args) {

        // Date with day, month, year. Example: birthdate.
        log.info("LocalDate.now(): {}", LocalDate.now()); // 2024-04-13

        // Time of the day. Example: Midnight
        log.info("LocalTime.now(): {}", LocalTime.now()); // 09:09:02.599667500

        // Day and time with no time zone: 10 a.m. next Monday
        log.info("LocalDateTime.now(): {}", LocalDateTime.now()); // 2024-04-13T09:09:02.599667500

        // Date and time with a specific time zone: 9 a.m. EST on 2/20/2021
        // 2024-04-13T09:09:02.600668700-05:00[America/Mexico_City]
        log.info("ZonedDateTime.now(): {}", ZonedDateTime.now());
    }
}
