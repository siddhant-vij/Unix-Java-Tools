package com.unixtools.command.filemanagement;

import com.unixtools.core.Command;
import java.io.File;

public class DfCommand implements Command {
  private static final String VALID_FLAGS = "h";

  @Override
  public void execute(String[] args) {
    boolean humanReadable = false;
    String specificFileSystem = null;

    for (String arg : args) {
      if (arg.equals("-h")) {
        humanReadable = true;
      } else if (!arg.startsWith("-")) {
        specificFileSystem = arg;
      } else {
        System.out.println("Invalid flags. Valid flags are: " + VALID_FLAGS);
        return;
      }
    }

    if (specificFileSystem != null) {
      displaySpecificFileSystemUsage(specificFileSystem, humanReadable);
    } else {
      displayAllFileSystemsUsage(humanReadable);
    }
  }

  private void displaySpecificFileSystemUsage(String fileSystem, boolean humanReadable) {
    File root = new File(fileSystem);
    if (root.exists()) {
      displayDriveUsage(root, humanReadable);
    } else {
      System.out.println("File system " + fileSystem + " does not exist.");
    }
  }

  private void displayAllFileSystemsUsage(boolean humanReadable) {
    File[] roots = File.listRoots();
    for (File root : roots) {
      displayDriveUsage(root, humanReadable);
    }
  }

  private void displayDriveUsage(File root, boolean humanReadable) {
    long total = root.getTotalSpace();
    long free = root.getFreeSpace();
    long used = total - free;

    String totalStr = humanReadable ? convertToHumanReadable(total) : String.valueOf(total);
    String usedStr = humanReadable ? convertToHumanReadable(used) : String.valueOf(used);
    String freeStr = humanReadable ? convertToHumanReadable(free) : String.valueOf(free);

    System.out.println("Drive: " + root + ", Total: " + totalStr + ", Used: " + usedStr + ", Free: " + freeStr);
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
