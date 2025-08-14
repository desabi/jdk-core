package com.desabisc.dates.eepoch;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.GregorianCalendar;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@Slf4j
public class EpochApp {

  public static void main(String[] args) {
    Date now = new Date();
    long timestamp = now.getTime(); // returns milliseconds since epoch

    //log.info("timestamp = {}", timestamp);

    Date date = new Date(1630454400000L); // Milliseconds
    //log.info("date = {}", date); // Outputs a readable date

    obtainDate();
  }

  /**
   * In Java, there are several ways to obtain the current date or a specific date in epoch format
   * (i.e., as a long representing milliseconds since January 1, 1970 UTC). Here's a comprehensive
   * list of approaches:
   */
  static void obtainDate() {
    log.info("---- Obtain Date as Epoch -----");

    // Using System.currentTimeMillis()
    long currentTimeMillis = System.currentTimeMillis();

    // Using java.util.Date
    long dateTime = new Date().getTime();

    // You can also create a specific date:
    Date specificDate = new GregorianCalendar(2023, Calendar.JANUARY, 1).getTime();
    long specificDateTime = specificDate.getTime();

    // Using java.time.Instant (Java 8+)
    Instant now = Instant.now();
    long instantToEpoch = now.toEpochMilli();

    // Using java.time.Instant (Java 8+), for a specific date
    LocalDate localDate = LocalDate.of(2023, 10, 28);
    Instant instant = localDate.atStartOfDay(ZoneOffset.UTC).toInstant();
    long instantToEpochSpecific = instant.toEpochMilli();


    log.info("System.currentTimeMillis() = {}", currentTimeMillis);
    log.info("Date().getTime() = {}", dateTime);
    log.info("specificDate.getTime() = {}", specificDateTime);
    log.info("instantToEpoch = {}", instantToEpoch);
    log.info("instantToEpoch Specific = {}", instantToEpochSpecific);
  }
}
