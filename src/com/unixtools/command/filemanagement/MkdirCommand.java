package com.unixtools.command.filemanagement;

import com.unixtools.core.Command;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MkdirCommand implements Command {
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
      System.out.println("mkdir: No Directory Name provided");
      return;
    }

    for (String dirName : directories) {
      createDirectory(dirName, flags.contains("p"), flags.contains("v"));
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

  private void createDirectory(String dirName, boolean parentDirectories, boolean verbose) {
    String normalizedDirName = normalizePath(dirName);
    File directory = new File(normalizedDirName);

    if (!parentDirectories && directory.getParentFile() != null && !directory.getParentFile().exists()) {
      System.out.println("mkdir: cannot create directory \'" + dirName + "\': No such file or directory");
      return;
    }

    boolean exists = directory.exists();
    boolean created = exists ? false : (parentDirectories ? directory.mkdirs() : directory.mkdir());

    if (verbose) {
      printVerboseMessage(dirName, exists, created);
    } else if (!created && !parentDirectories) {
      System.out.println("mkdir: cannot create directory \'" + dirName + "\': Already exists");
    }
  }

  private String normalizePath(String path) {
    String normalizedPath = path.replace("\\", "/");
    Path pathObj = Paths.get(normalizedPath).normalize();
    return pathObj.toString();
  }

  private void printVerboseMessage(String dirName, boolean exists, boolean created) {
    if (created) {
      System.out.println("mkdir: created directory \'" + dirName + "\'");
    } else if (exists) {
      System.out.println("mkdir: directory \'" + dirName + "\' already exists");
    } else {
      System.out.println("mkdir: error creating directory \'" + dirName + "\'");
    }
  }
}
