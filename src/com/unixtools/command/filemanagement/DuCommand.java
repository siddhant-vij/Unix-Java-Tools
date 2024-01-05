package com.unixtools.command.filemanagement;

import com.unixtools.core.Command;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DuCommand implements Command {
  private static final String VALID_FLAGS = "hs";

  @Override
  public void execute(String[] args) {
    List<String> flags = new ArrayList<>();
    String targetPath = parseArguments(args, flags);

    if (!validateFlags(flags)) {
      System.out.println("Invalid flags. Valid flags are: " + VALID_FLAGS);
      return;
    }

    boolean humanReadable = flags.contains("h");
    boolean summaryOnly = flags.contains("s");

    long size = calculateSize(Paths.get(targetPath), summaryOnly);
    printSize(size, humanReadable, Paths.get(targetPath));
  }

  private String parseArguments(String[] args, List<String> flags) {
    String path = ".";
    for (String arg : args) {
      if (arg.startsWith("-")) {
        for (char flag : arg.substring(1).toCharArray()) {
          flags.add(String.valueOf(flag));
        }
      } else {
        path = normalizePath(arg);
        break;
      }
    }
    return path;
  }

  private boolean validateFlags(List<String> flags) {
    for (String flag : flags) {
      if (!VALID_FLAGS.contains(flag)) {
        return false;
      }
    }
    return true;
  }

  private String normalizePath(String path) {
    String normalizedPath = path.replace("\\", "/");
    Path pathObj = Paths.get(normalizedPath).normalize();
    return pathObj.toString();
  }

  private long calculateSize(Path path, boolean summaryOnly) {
    File file = path.toFile();
    if (!file.exists()) {
      System.out.println("File or directory does not exist: " + path);
      return 0;
    }

    if (file.isFile()) {
      return file.length();
    }

    long size = 0;
    if (file.isDirectory() && !summaryOnly) {
      File[] files = file.listFiles();
      if (files != null) {
        for (File subFile : files) {
          size += calculateSize(subFile.toPath(), false);
        }
      }
    }
    return size + file.length();
  }

  private void printSize(long size, boolean humanReadable, Path path) {
    String sizeStr = humanReadable ? convertToHumanReadable(size) : String.valueOf(size);
    System.out.println(sizeStr + "\t" + path);
  }

  private String convertToHumanReadable(long size) {
    int unit = 1024;
    if (size < unit)
      return size + " B";
    int exp = (int) (Math.log(size) / Math.log(unit));
    String pre = "KMGTPE".charAt(exp - 1) + "";
    return String.format("%.1f %sB", size / Math.pow(unit, exp), pre);
  }
}
