package com.desabisc.guide.java.io.dretrieve;

import com.desabisc.guide.java.io.util.Constants;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;

/**
 * The Path interface contains numerous methods for retrieving particular elements of a Path,
 * returned as Path object themselves.
 */
@Slf4j
public class PathRetrieveElements {

  static void main() {
    printPathInformation(Path.of(Constants.PATH_TO_FILE));
    printPathInformation(Path.of(Constants.PATH_TO_DIRECTORY));
  }

  /**
   * <ul>
   *   <li>The getFileName() method returns the Path element of the current file or directory.</li>
   *   <li>The getRoot() method returns the root element of the file within the file system, or null
   *   if the path is a relative path.</li>
   *   <li>The getParent() method returns the full path of the containing directory, returns null
   *   if operated on the root path or ar the top of a relative path.</li>
   * </ul>
   *
   * @param path the given path.
   */
  private static void printPathInformation(Path path) {
    log.info("############# PATH INFORMATION ###########");
    log.info("Path: {}", path.toString());
    log.info("Filename is: {}", path.getFileName());
    log.info("    Root path is: {}", path.getRoot());
    Path currentParent = path;

    while ((currentParent = currentParent.getParent()) != null)
      log.info("Current Parent is: {}", currentParent);
  }
}
