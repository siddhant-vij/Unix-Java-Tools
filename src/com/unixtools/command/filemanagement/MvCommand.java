package com.unixtools.command.filemanagement;

import com.unixtools.core.Command;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MvCommand implements Command {
  private static final String VALID_FLAGS = "ivb";
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
    if (paths.size() != 2) {
      System.out.println("Invalid usage. Two paths required: source and destination.");
      return;
    }

    String sourcePath = normalizePath(paths.get(0));
    String destinationPath = normalizePath(paths.get(1));
    boolean interactive = flags.contains("i");
    boolean verbose = flags.contains("v");
    boolean backup = flags.contains("b");

    try {
      movePath(Paths.get(sourcePath), Paths.get(destinationPath), interactive, verbose, backup);
    } catch (IOException e) {
      System.out.println("mv: error occurred - " + e.getMessage());
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

  private void movePath(Path source, Path destination, boolean interactive, boolean verbose, boolean backup)
      throws IOException {
    if (!Files.exists(source)) {
      System.out.println("mv: '" + source + "' does not exist");
      return;
    }

    if (Files.isDirectory(source) && Files.isRegularFile(destination)) {
      System.out.println("mv: cannot overwrite non-directory '" + destination + "' with directory '" + source + "'");
      return;
    }

    if (Files.exists(destination) && !Files.isDirectory(destination)) {
      if (!interactive || !confirmOverwrite(destination)) {
        return;
      }
      if (backup) {
        createBackup(destination);
      }
    } else if (Files.isDirectory(destination)) {
      destination = destination.resolve(source.getFileName());
    }

    Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
    if (verbose) {
      System.out.println("mv: moved '" + source + "' to '" + destination + "'");
    }
  }

  private boolean confirmOverwrite(Path file) {
    System.out.print("mv: overwrite '" + file + "'? (y/n) ");
    String response = scanner.nextLine();
    return response.equalsIgnoreCase("y");
  }

  private void createBackup(Path file) throws IOException {
    Path backupPath = Paths.get(file.toString() + "~");
    Files.copy(file, backupPath, StandardCopyOption.REPLACE_EXISTING);
  }

  private String normalizePath(String path) {
    String normalizedPath = path.replace("\\", "/");
    Path pathObj = Paths.get(normalizedPath).normalize();
    return pathObj.toString();
  }
}
