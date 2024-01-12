package com.unixtools.command.filecontent;

import com.unixtools.core.Command;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class HeadCommand implements Command {
  private static final String VALID_FLAGS = "nc";
  private int numberOfLines = 10;
  private int numberOfBytes = -1;
  private boolean isBytesFlagUsed = false;

  @Override
  public void execute(String[] args) {
    List<String> flags = new ArrayList<>();
    List<String> filePaths = new ArrayList<>();
    parseArguments(args, flags, filePaths);

    if (!areFlagsValid(flags)) {
      System.out.println("Invalid flags. Valid flags are: " + VALID_FLAGS);
      return;
    }

    if (filePaths.isEmpty()) {
      System.out.println("head: No file specified.");
      return;
    }

    for (String filePath : filePaths) {
      if (filePaths.size() > 1) {
        System.out.println("==> " + filePath + " <==");
      }
      displayFileContents(normalizePath(filePath));
    }
  }

  private void parseArguments(String[] args, List<String> flags, List<String> paths) {
    for (int i = 0; i < args.length; i++) {
      if (args[i].startsWith("-")) {
        switch (args[i]) {
          case "-n":
            numberOfLines = Integer.parseInt(args[++i]);
            break;
          case "-c":
            isBytesFlagUsed = true;
            numberOfBytes = Integer.parseInt(args[++i]);
            break;
          default:
            flags.add(args[i]);
            break;
        }
      } else {
        paths.add(args[i]);
      }
    }
  }

  private boolean areFlagsValid(List<String> flags) {
    return flags.stream().allMatch(flag -> VALID_FLAGS.contains(flag.replace("-", "")));
  }

  private void displayFileContents(String filePath) {
    try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
      if (isBytesFlagUsed) {
        readBytes(reader, numberOfBytes);
      } else {
        readLines(reader, numberOfLines);
      }
    } catch (IOException e) {
      System.out.println("Error reading file: " + e.getMessage());
    }
  }

  private void readLines(BufferedReader reader, int numLines) throws IOException {
    for (int i = 0; i < numLines; i++) {
      String line = reader.readLine();
      if (line == null) {
        break;
      }
      System.out.println(line);
    }
  }

  private void readBytes(BufferedReader reader, int numBytes) throws IOException {
    int remainingBytes = numBytes;
    int ch;
    while (remainingBytes > 0 && (ch = reader.read()) != -1) {
      System.out.print((char) ch);
      remainingBytes--;
    }
    System.out.println();
  }

  private String normalizePath(String path) {
    String normalizedPath = path.replace("\\", "/");
    Path pathObj = Paths.get(normalizedPath).normalize();
    return pathObj.toString();
  }
}
