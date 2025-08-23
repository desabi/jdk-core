package com.desabisc.guide.java.io.cpaths;

import com.desabisc.guide.java.io.util.Constants;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UsingPaths {
  public static void main(String[] args) {
    viewingThePath();
    subPathEg();
  }

  /**
   * The Path interface contains three method to retrieve basic information about the path representation.
   * The toString() method returns a String representation of the entire path.
   * The getNameAccount() and getName() are often used together to retrieve the number of elements
   * in the path and a reference to each element.
   * The last two methods do not include the root directory as the part of the path.
   */
  static void viewingThePath() {
    log.info("########## Viewing the Path ##########");
    Path path = Path.of(Constants.PATH_TO_FILE);
    log.info("The Path is: {}", path);
    for (int index = 0; index < path.getNameCount(); index++) {
      log.info("   Element {} is : {}", index, path.getName(index));
    }

    var rootPath = Path.of("C:\\");
    log.info("Root path getNameCount(): {}", rootPath.getNameCount());
    //log.info("Root Path getName(): {}", rootPath.getName(0)); // IllegalArgumentException
  }

  static void subPathEg() {
    log.info("########## Sub Path EG ##########");
    var path = Path.of(Constants.PATH_TO_FILE);
    log.info("The Path is: {}", path);
    for (int index = 0; index < path.getNameCount(); index++) {
      log.info("   Element {} is : {}", index, path.getName(index));
    }

    // subpath(inclusive beginIndex, exclusive endIndex)
    log.info("subpath(0, 3): {}", path.subpath(0, 3));
    log.info("subpath(1, 2): {}", path.subpath(1, 2));
    log.info("subpath(1, 3): {}", path.subpath(1, 3));
  }
}
