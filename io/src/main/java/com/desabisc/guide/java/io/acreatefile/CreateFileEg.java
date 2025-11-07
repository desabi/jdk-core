package com.desabisc.guide.java.io.acreatefile;

import com.desabisc.guide.java.io.util.Constants;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>File and Path</p>
 * Both are used to represent the location of a file or directory within your computer's file system
 * (like C:\Users\MyDoc.txt or /home/user/report.pdf).
 * <ul>
 *   <li>File -> IO: input/output</li>
 *   <li>Path -> NIO.2: non-blocking I/O (recommend)</li>
 *   <li>The class File or Path can represent a file or directory.</li>
 *   <li>Both interfaces cannot read or write data within a file, although they are passed as
 *   reference o other classes</li>
 * </ul>
 *
 */
@Slf4j
public class CreateFileEg {

  public static void main(String[] args) {
    useFile();
    usePath();
    switchEg();
  }

  /**
   * The file should exist.
   * It does not create the file.
   * The File object points to a given location on disk.
   * Operations are on the File object itself.
   */
  static void useFile() {
    log.info("Using File...");
    File zooFile1 = new File(Constants.PATH_TO_FILE);
    log.info("File exist: {}", zooFile1.exists());
  }

  /**
   * The file should exist.
   * It does not create the file.
   * The Path object point to a given location on disk.
   * Represents just the path. Operations are done by the Files utility class.
   */
  static void usePath() {
    log.info("Using Path...");
    Path zooPath1 = Path.of(Constants.PATH_TO_FILE);
    log.info("File (with Path) exists: {}", Files.exists(zooPath1));
  }

  static void switchEg() {
    log.info("Switching between File and Path...");
    File file = new File("rabbit.txt");
    Path path = file.toPath();
    File backToFile = path.toFile();
  }
}
