package com.desabisc.dates.doperations;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
public class LocalDateTimeOp {
    public static void main(String[] args) {

        LocalDateTime dateTime = LocalDateTime.now();
        LocalDateTime dateTimePlusMinutes = dateTime.plusMinutes(10);
        LocalDateTime dateTimePlusHour = dateTime.plusHours(1);

        log.info("dateTime --> {}", dateTime);
        log.info("dateTime plus minutes --> {}", dateTimePlusMinutes);
        log.info("dateTime plus hour --> {}", dateTimePlusHour);

    }
}
