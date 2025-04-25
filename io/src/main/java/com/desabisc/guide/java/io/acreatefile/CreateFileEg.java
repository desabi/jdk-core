package com.desabisc.guide.java.io.acreatefile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;

/**
 * IO: input/output
 * NIO.2: non-blocking I/O (recommend)
 * The class File or Path can represent a file or directory.
 */
@Slf4j
public class CreateFileEg {
  public static void main(String[] args) {
    useFile();
    usePath();
    switchEg();
  }

  static void useFile() {
    log.info("Using File...");
    File zooFile1 = new File("C:\\Users\\desab\\dev\\stripes.txt");
    log.info("File exist: {}", zooFile1.exists());
  }

  static void usePath() {
    log.info("Using Path...");
    Path zooPath1 = Path.of("C:\\tests\\io\\stripes.txt");
    log.info("File (with Path) exists: {}", Files.exists(zooPath1));
  }

  static void switchEg() {
    log.info("Switching between File and Path...");
    File file = new File("rabbit.txt");
    Path path = file.toPath();
    File backToFile = path.toFile();
  }
}
