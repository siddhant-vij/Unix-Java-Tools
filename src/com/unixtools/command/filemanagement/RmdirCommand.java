package com.unixtools.command.filemanagement;

import com.unixtools.core.Command;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class RmdirCommand implements Command {
  private static final String VALID_FLAGS = "pv";

  @Override
  public void execute(String[] args) {
    List<String> flags = new ArrayList<>();
    List<String> directories = new ArrayList<>();

    parseArguments(args, flags, directories);
    if (!areFlagsValid(flags)) {
      System.out.println("Invalid flag(s). Valid flags are: " + VALID_FLAGS);
      return;
    }

    if (directories.isEmpty()) {
      System.out.println("rmdir: No Directory Name provided");
      return;
    }

    for (String dirName : directories) {
      removeDirectory(dirName, flags.contains("p"), flags.contains("v"));
    }
  }

  private void parseArguments(String[] args, List<String> flags, List<String> directories) {
    for (String arg : args) {
      if (arg.startsWith("-")) {
        for (char flag : arg.substring(1).toCharArray()) {
          flags.add(String.valueOf(flag));
        }
      } else {
        directories.add(arg);
      }
    }
  }

  private boolean areFlagsValid(List<String> flags) {
    return flags.stream().allMatch(flag -> VALID_FLAGS.contains(flag));
  }

  private void removeDirectory(String dirName, boolean parentDirectories, boolean verbose) {
    String normalizedDirName = normalizePath(dirName);
    File directory = new File(normalizedDirName);

    if (!directory.exists()) {
      System.out.println("rmdir: failed to remove '" + dirName + "': No such file or directory");
      return;
    }

    if (directory.isDirectory() && directory.list().length > 0) {
      System.out.println("rmdir: failed to remove '" + dirName + "': Directory not empty");
      return;
    }

    boolean success = directory.delete();
    if (success) {
      if (verbose) {
        System.out.println("rmdir: removed '" + dirName + "'");
      }
      if (parentDirectories) {
        removeParentDirectories(directory.getParentFile(), verbose);
      }
    } else {
      if (verbose) {
        System.out.println("rmdir: failed to remove '" + dirName + "'");
      }
    }
  }

  private void removeParentDirectories(File directory, boolean verbose) {
    if (directory != null && directory.list().length == 0) {
      if (directory.delete()) {
        if (verbose) {
          System.out.println("rmdir: removed '" + directory.getPath() + "'");
        }
        removeParentDirectories(directory.getParentFile(), verbose);
      }
    }
  }

  private String normalizePath(String path) {
    String normalizedPath = path.replace("\\", "/");
    Path pathObj = Paths.get(normalizedPath).normalize();
    return pathObj.toString();
  }
}