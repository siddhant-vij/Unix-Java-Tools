package com.unixtools.command.filemanagement;

import com.unixtools.core.Command;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CpCommand implements Command {
  private static final String VALID_FLAGS = "riv";
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
    boolean recursive = flags.contains("r");
    boolean interactive = flags.contains("i");
    boolean verbose = flags.contains("v");

    try {
      copyPath(Paths.get(sourcePath), Paths.get(destinationPath), recursive, interactive, verbose);
    } catch (IOException e) {
      System.out.println("cp: error occurred - " + e.getMessage());
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

  private void copyPath(Path source, Path destination, boolean recursive, boolean interactive, boolean verbose)
      throws IOException {
    if (!Files.exists(source)) {
      System.out.println("cp: '" + source + "' does not exist");
      return;
    }

    if (Files.isDirectory(source) && Files.isRegularFile(destination)) {
      System.out.println("cp: cannot overwrite non-directory '" + destination + "' with directory '" + source + "'");
      return;
    }

    if (Files.isDirectory(source)) {
      if (!recursive) {
        System.out.println("cp: omitting directory '" + source + "' without -r flag");
        return;
      }
      copyDirectory(source, destination, recursive, interactive, verbose);
    } else {
      Path target = Files.isDirectory(destination) ? destination.resolve(source.getFileName()) : destination;
      copyFile(source, target, interactive, verbose);
    }
  }

  private void copyDirectory(Path sourceDir, Path destinationDir, boolean recursive, boolean interactive,
      boolean verbose) throws IOException {
    if (!Files.exists(destinationDir)) {
      Files.createDirectories(destinationDir);
    }
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(sourceDir)) {
      for (Path path : stream) {
        Path destPath = destinationDir.resolve(sourceDir.relativize(path));
        if (Files.isDirectory(path)) {
          copyDirectory(path, destPath, recursive, interactive, verbose);
        } else {
          copyFile(path, destPath, interactive, verbose);
        }
      }
    }
  }

  private void copyFile(Path sourceFile, Path destinationFile, boolean interactive, boolean verbose)
      throws IOException {
    if (Files.exists(destinationFile) && !confirmOverwrite(destinationFile)) {
      return;
    }

    Files.copy(sourceFile, destinationFile, StandardCopyOption.REPLACE_EXISTING);
    if (verbose) {
      System.out.println("cp: copied '" + sourceFile + "' to '" + destinationFile + "'");
    }
  }

  private boolean confirmOverwrite(Path file) {
    System.out.print("cp: overwrite '" + file + "'? (y/n) ");
    String response = scanner.nextLine();
    return response.equalsIgnoreCase("y");
  }

  private String normalizePath(String path) {
    String normalizedPath = path.replace("\\", "/");
    Path pathObj = Paths.get(normalizedPath).normalize();
    return pathObj.toString();
  }
}
