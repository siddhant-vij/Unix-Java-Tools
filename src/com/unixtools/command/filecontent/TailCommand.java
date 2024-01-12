package com.unixtools.command.filecontent;

import com.unixtools.core.Command;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TailCommand implements Command {
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
      System.out.println("tail: No file specified.");
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
    try {
      if (isBytesFlagUsed) {
        readLastBytes(filePath, numberOfBytes);
      } else {
        readLastLines(filePath, numberOfLines);
      }
    } catch (IOException e) {
      System.out.println("Error reading file: " + e.getMessage());
    }
  }

  private void readLastLines(String filePath, int numLines) throws IOException {
    RandomAccessFile file = new RandomAccessFile(filePath, "r");
    long fileLength = file.length();
    List<String> lines = new ArrayList<>();
    StringBuilder sb = new StringBuilder();
    boolean lastCharWasCR = false;

    for (long filePointer = fileLength - 1; filePointer >= 0; filePointer--) {
      file.seek(filePointer);
      char ch = (char) file.readByte();

      if (ch == '\n') {
        if (lastCharWasCR) {
          sb.deleteCharAt(0);
        }
        lines.add(sb.reverse().toString());
        sb.setLength(0);
        if (lines.size() == numLines) {
          break;
        }
        lastCharWasCR = false;
      } else if (ch == '\r') {
        lastCharWasCR = true;
      } else {
        lastCharWasCR = false;
      }
      sb.append(ch);
    }

    for (int i = lines.size() - 1; i >= 0; i--) {
      System.out.print(lines.get(i));
    }
    
    System.out.println();
    file.close();
  }

  private void readLastBytes(String filePath, int numBytes) throws IOException {
    RandomAccessFile file = new RandomAccessFile(filePath, "r");
    long fileLength = file.length();
    file.seek(fileLength - numBytes);
    byte[] bytes = new byte[numBytes];
    file.readFully(bytes);
    System.out.println(new String(bytes));
    file.close();
  }

  private String normalizePath(String path) {
    String normalizedPath = path.replace("\\", "/");
    Path pathObj = Paths.get(normalizedPath).normalize();
    return pathObj.toString();
  }
}
