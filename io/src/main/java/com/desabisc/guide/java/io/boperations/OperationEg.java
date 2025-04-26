package com.desabisc.guide.java.io.boperations;

import com.desabisc.guide.java.io.util.Constants;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OperationEg {
  public static void main(String[] args) throws IOException {

    File file1 = new File(Constants.PATH_TO_FILE);
    File file2 = new File(Constants.PATH_TO_DIRECTORY);
    Path path1 = Path.of(Constants.PATH_TO_FILE);
    Path path2 = Path.of(Constants.PATH_TO_DIRECTORY);

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
   * Using NIO.2 (non-blocking I/O).
   *
   * @param path the path value.
   * @throws IOException
   */
  static void operationsWithPath(Path path) throws IOException {
    log.info("########## Using Path ##########");
    if (Files.exists(path)) {
      log.info("Absolute Path: {}", path.toAbsolutePath());
      log.info("Is Directory: {}", Files.isDirectory(path));
      log.info("Parent Path: {}", path.getParent());
      if (Files.isRegularFile(path)) {
        // If a NIO.2 method declares an IOOException, it usually requires the paths it operates on
        // to exist
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
