package com.desabisc.guide.java.io.afilepath;

import com.desabisc.guide.java.io.util.Constants;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;

/**
 * <p><b>File and Path</b></p>
 * <p><b>Both are used to represent the location of a file or directory within your computer's file system
 * (like C:\Users\MyDoc.txt or /home/user/report.pdf).</b></p>
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
   * <ul>
   *   <li>The file should exist.</li>
   *   <li>It does not create the file.</li>
   *   <li>The File object points to a given location on disk.</li>
   *   <li>Operations are on the File object itself.</li>
   * </ul>
   */
  static void useFile() {
    log.info("Using File...");
    File zooFile1 = new File(Constants.PATH_TO_FILE);
    log.info("File exist: {}", zooFile1.exists());
  }

  /**
   * <ul>
   *   <li>The file should exist.</li>
   *   <li>It does not create the file.</li>
   *   <li>The Path object point to a given location on disk.</li>
   *   <li>Represents just the path. Operations are done by the Files utility class.</li>
   * </ul>
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
