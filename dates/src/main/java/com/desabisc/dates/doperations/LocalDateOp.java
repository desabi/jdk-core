package com.desabisc.dates.doperations;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Slf4j
public class LocalDateOp {
    private static final String SALTO= "----------\n";

    public static void main(String[] args) {

        LocalDate date1 = LocalDate.of(2024, 4, 10);
        LocalDate date2 = LocalDate.of(2024, 4, 14);

        log.info("date1 --> {}", date1);
        log.info("date2 --> {}", date2);
        log.info(SALTO);

        operations(date1);
        calculations(date1, date2);
        comparations(date1, date2);
    }

    private static void operations(LocalDate date1) {
        LocalDate datePlusDay = date1.plusDays(1);
        LocalDate datePlusWeeks = date1.plusWeeks(3);
        LocalDate datePreviousMonth = date1.minus(1, ChronoUnit.MONTHS);
        //LocalDate datePreviousMonth = date1.minusMonths(1);

        log.info("date1 plus day --> {}", datePlusDay);
        log.info("date1 plus weeks --> {}", datePlusWeeks);
        log.info("date1 PreviousMonth --> {}", datePreviousMonth);
        log.info(SALTO);
    }

    private static void operationsb() {

    }

    public static void calculations(LocalDate date1, LocalDate date2) {
        long daysBetween = ChronoUnit.DAYS.between(date1, date2);
        long monthsBetween = ChronoUnit.MONTHS.between(date1, date2);
        long yearsBetween = ChronoUnit.YEARS.between(date1, date2);

        log.info("Days between date1 and date2 --> {}: ", daysBetween);
        log.info("Months between date1 and date2 --> {}: ", monthsBetween);
        log.info("Years between date1 and date2 --> {}: ", yearsBetween);

        log.info(SALTO);
    }
    private static void comparations(LocalDate date1, LocalDate date2) {
        boolean isBefore = date1.isBefore(date2);
        boolean isAfter = date1.isAfter(date2);
        boolean isEqual = date1.isEqual(date2);

        log.info("date1 is before date2? --> {}", isBefore);
        log.info("date1 is after date2? --> {}", isAfter);
        log.info("date1 is equal date2? --> {}", isEqual);
        log.info(SALTO);
    }

}
