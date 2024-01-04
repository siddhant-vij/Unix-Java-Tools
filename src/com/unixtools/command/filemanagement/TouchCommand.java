package com.unixtools.command.filemanagement;

import com.unixtools.core.Command;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.ArrayList;
import java.util.List;

public class TouchCommand implements Command {
  @Override
  public void execute(String[] args) {
    if (args.length == 0) {
      System.out.println("touch: no file name provided");
      return;
    }

    List<String> flags = new ArrayList<>();
    List<String> fileNames = new ArrayList<>();

    parseArguments(args, flags, fileNames);
    if (!areFlagsValid(flags)) {
      System.out.println("Invalid usage. No flags are supported.");
      return;
    }

    for (String fileName : fileNames) {
      Path filePath = Paths.get(normalizePath(fileName));
      try {
        touchFile(filePath);
      } catch (InvalidPathException e) {
        System.out.println("touch: invalid file name '" + fileName + "'");
      } catch (IOException e) {
        System.out.println("touch: error occurred - " + e.getMessage());
      }
    }
  }

  private void parseArguments(String[] args, List<String> flags, List<String> fileNames) {
    for (String arg : args) {
      if (arg.startsWith("-")) {
        for (char flag : arg.substring(1).toCharArray()) {
          flags.add(String.valueOf(flag));
        }
      } else {
        fileNames.add(arg);
      }
    }
  }

  private boolean areFlagsValid(List<String> flags) {
    return flags.isEmpty();
  }

  private void touchFile(Path filePath) throws IOException {
    if (Files.notExists(filePath.getParent())) {
      System.out.println("touch: cannot create file '" + filePath + "', the directory does not exist");
      return;
    }
    if (Files.notExists(filePath)) {
      Files.createFile(filePath);
    } else {
      Files.setLastModifiedTime(filePath, FileTime.fromMillis(System.currentTimeMillis()));
    }
  }

  private String normalizePath(String path) {
    String normalizedPath = path.replace("\\", "/");
    Path pathObj = Paths.get(normalizedPath).normalize();
    return pathObj.toString();
  }
}
