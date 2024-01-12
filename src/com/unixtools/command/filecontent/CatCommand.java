package com.unixtools.command.filecontent;

import com.unixtools.core.Command;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CatCommand implements Command {
  private static final String VALID_FLAGS = "nber";
  private boolean numberLines = false;
  private boolean numberNonBlankLines = false;
  private boolean showEndOfLine = false;
  private boolean replaceMultipleEmptyLines = false;

  @Override
  public void execute(String[] args) {
    List<String> flags = new ArrayList<>();
    List<String> filePaths = new ArrayList<>();
    parseArguments(args, flags, filePaths);

    if (!areFlagsValid(flags)) {
      System.out.println("Invalid flags. Valid flags are: " + VALID_FLAGS);
      return;
    }

    for (String filePath : filePaths) {
      readFileContents(normalizePath(filePath));
    }
  }

  private void parseArguments(String[] args, List<String> flags, List<String> paths) {
    for (String arg : args) {
      if (arg.startsWith("-")) {
        for (char flag : arg.substring(1).toCharArray()) {
          switch (flag) {
            case 'n':
              numberLines = true;
              break;
            case 'b':
              numberNonBlankLines = true;
              break;
            case 'e':
              showEndOfLine = true;
              break;
            case 'r':
              replaceMultipleEmptyLines = true;
              break;
            default:
              flags.add(String.valueOf(flag));
              break;
          }
        }
      } else {
        paths.add(arg);
      }
    }
  }

  private boolean areFlagsValid(List<String> flags) {
    for (String flag : flags) {
      if (!VALID_FLAGS.contains(flag)) {
        return false;
      }
    }
    return true;
  }

  private void readFileContents(String filePath) {
    try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
      String line;
      int lineNumber = 0;
      boolean lastLineEmpty = false;

      while ((line = reader.readLine()) != null) {
        boolean isLineEmpty = line.trim().isEmpty();

        if (replaceMultipleEmptyLines && isLineEmpty) {
          if (lastLineEmpty)
            continue;
          lastLineEmpty = true;
        } else {
          lastLineEmpty = false;
        }

        if (numberLines) {
          System.out.printf("%6d  ", ++lineNumber);
        } else if (numberNonBlankLines && !isLineEmpty) {
          System.out.printf("%6d  ", ++lineNumber);
        }

        System.out.print(line);
        if (showEndOfLine) {
          System.out.print("$");
        }
        System.out.println();
      }
    } catch (IOException e) {
      System.out.println("Error reading file: " + e.getMessage());
    }
  }

  private String normalizePath(String path) {
    String normalizedPath = path.replace("\\", "/");
    Path pathObj = Paths.get(normalizedPath).normalize();
    return pathObj.toString();
  }
}
