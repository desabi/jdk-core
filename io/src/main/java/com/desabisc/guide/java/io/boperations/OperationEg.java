package com.desabisc.guide.java.io.boperations;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OperationEg {
  public static void main(String[] args) throws IOException {
    String filePath = "C:\\tests\\java\\io\\zoo.txt";
    String directoryPath = "C:\\tests\\java\\io";

    File file1 = new File(filePath);
    File file2 = new File(directoryPath);
    Path path1 = Path.of(filePath);
    Path path2 = Path.of(directoryPath);

    operationsWithFile(file1);
    operationsWithFile(file2);
    operationsWithPath(path1);
    operationsWithPath(path2);
  }

  /**
   * Using IO (input/output).
   * @param file the file
   */
  static void operationsWithFile(File file) {
    log.info("########## Using File ##########");
    log.info("File exists: {}", file.exists());
    if (file.exists()) {
      log.info("Path to file: {}", file.getAbsolutePath());
      log.info("Is Directory: {}", file.isDirectory());
      log.info("Parent Path: {}", file.getParent());
      log.info("Is file: {}", file.isFile());
      if (file.isFile()) {
        log.info("File size: {}", file.length());
        log.info("Last Modified: {}", file.lastModified());
      } else {
        log.info("List files: ");
        for (File subfile: file.listFiles()) {
          log.info("Sub File Name: {}", subfile.getName());
        }
      }
    }
  }

  /**
   * Using NIO.2 (non-blocking I/O)
   * @param path the path
   */
  static void operationsWithPath(Path path) throws IOException {
    log.info("########## Using Path ##########");
    if (Files.exists(path)) {
      log.info("Absolute Path: {}", path.toAbsolutePath());
      log.info("Is Directory: {}", Files.isDirectory(path));
      log.info("Parent Path: {}", path.getParent());
      if (Files.isRegularFile(path)) {
        log.info("Size: {}", Files.size(path)); // IOException
        log.info("Last modified: {}", Files.getLastModifiedTime(path));
      } else {
        try (Stream<Path> stream = Files.list(path)) {
          stream.forEach(subFile -> log.info("Sub File name: {}", subFile.getFileName()));
        }
      }
    }
  }
}
