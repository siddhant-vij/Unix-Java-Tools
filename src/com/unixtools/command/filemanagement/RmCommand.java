package com.unixtools.command.filemanagement;

import com.unixtools.core.Command;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class RmCommand implements Command {
  private static final String VALID_FLAGS = "rfi";
  private final Scanner scanner = new Scanner(System.in);

  @Override
  public void execute(String[] args) {
    List<String> flags = new ArrayList<>();
    List<String> paths = new ArrayList<>();

    parseArguments(args, flags, paths);
    if (!areFlagsValid(flags)) {
      System.out.println("Invalid usage. Valid flags are: " + VALID_FLAGS);
      return;
    }

    boolean recursive = flags.contains("r");
    boolean force = flags.contains("f");
    boolean interactive = flags.contains("i");

    for (String path : paths) {
      Path normalizedPath = Paths.get(normalizePath(path));
      try {
        removePath(normalizedPath, recursive, force, interactive);
      } catch (IOException e) {
        if (!force) {
          System.out.println("rm: error occurred - " + e.getMessage());
        }
      }
    }
  }

  private void parseArguments(String[] args, List<String> flags, List<String> paths) {
    for (String arg : args) {
      if (arg.startsWith("-")) {
        for (char flag : arg.substring(1).toCharArray()) {
          flags.add(String.valueOf(flag));
        }
      } else {
        paths.add(arg);
      }
    }
  }

  private boolean areFlagsValid(List<String> flags) {
    return flags.stream().allMatch(flag -> VALID_FLAGS.contains(flag.replace("-", "")));
  }

  private void removePath(Path path, boolean recursive, boolean force, boolean interactive) throws IOException {
    File file = path.toFile();
    if (!file.exists()) {
      if (!force) {
        System.out.println("rm: cannot remove '" + path + "': No such file or directory");
      }
      return;
    }

    if (file.isDirectory()) {
      if (!recursive && file.list().length > 0) {
        System.out.println("rm: cannot remove '" + path + "': Is a directory without -r flag");
        return;
      }
      if (interactive && !confirmRemoval(path)) {
        return;
      }
      removeDirectory(file, recursive, force, interactive);
    } else {
      if (interactive && !confirmRemoval(path)) {
        return;
      }
      Files.delete(path);
    }
  }

  private void removeDirectory(File directory, boolean recursive, boolean force, boolean interactive)
      throws IOException {
    if (recursive) {
      File[] files = directory.listFiles();
      if (files != null) {
        for (File file : files) {
          removePath(file.toPath(), true, force, interactive);
        }
      }
    }
    if (!interactive || confirmRemoval(directory.toPath())) {
      Files.delete(directory.toPath());
    }
  }

  private boolean confirmRemoval(Path path) {
    System.out.print("rm: remove '" + path + "'? (y/n) ");
    String response = scanner.nextLine();
    return response.equalsIgnoreCase("y");
  }

  private String normalizePath(String path) {
    String normalizedPath = path.replace("\\", "/");
    Path pathObj = Paths.get(normalizedPath).normalize();
    return pathObj.toString();
  }
}
