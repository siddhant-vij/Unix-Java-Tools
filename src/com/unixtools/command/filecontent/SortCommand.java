package com.unixtools.command.filecontent;

import com.unixtools.core.Command;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class SortCommand implements Command {
  private static final String VALID_FLAGS = "dufnroc";
  private boolean dictionaryOrder = false;
  private boolean unique = false;
  private boolean caseInsensitive = false;
  private boolean numericSort = false;
  private boolean reverse = false;
  private boolean columnSort = false;
  private int columnIndex = -1;
  private String outputFile = null;

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
      System.out.println("No file specified.");
      return;
    }

    try {
      List<String> lines = Files.readAllLines(Paths.get(normalizePath(filePaths.get(0))));
      sortLines(lines);
      outputSortedLines(lines, outputFile);
    } catch (IOException e) {
      System.err.println("Error processing file: " + e.getMessage());
    }
  }

  private void parseArguments(String[] args, List<String> flags, List<String> paths) {
    for (int i = 0; i < args.length; i++) {
      if (args[i].startsWith("-")) {
        for (char flag : args[i].substring(1).toCharArray()) {
          switch (flag) {
            case 'd':
              dictionaryOrder = true;
              break;
            case 'u':
              unique = true;
              break;
            case 'f':
              caseInsensitive = true;
              break;
            case 'n':
              numericSort = true;
              break;
            case 'r':
              reverse = true;
              break;
            case 'o':
              if (i + 1 < args.length)
                outputFile = args[++i];
              break;
            case 'c':
              columnSort = true;
              if (i + 1 < args.length)
                columnIndex = Integer.parseInt(args[++i]);
              break;
            default:
              flags.add(String.valueOf(flag));
          }
        }
      } else {
        paths.add(args[i]);
      }
    }
  }

  private boolean areFlagsValid(List<String> flags) {
    return flags.stream().allMatch(flag -> VALID_FLAGS.contains(flag));
  }

  private String normalizePath(String path) {
    String normalizedPath = path.replace("\\", "/");
    Path pathObj = Paths.get(normalizedPath).normalize();
    return pathObj.toString();
  }

  private void sortLines(List<String> lines) {
    Comparator<String> comparator = getComparator();

    lines.sort(comparator);

    if (unique) {
      Set<String> uniqueLines = new LinkedHashSet<>(lines);
      lines.clear();
      lines.addAll(uniqueLines);
    }
  }

  private Comparator<String> getComparator() {
    Comparator<String> comparator = Comparator.naturalOrder();

    if (numericSort) {
      comparator = this::compareNumeric;
    } else if (dictionaryOrder) {
      comparator = String::compareToIgnoreCase;
    } else if (caseInsensitive) {
      comparator = String.CASE_INSENSITIVE_ORDER;
    }

    if (reverse) {
      comparator = comparator.reversed();
    }

    if (columnSort && columnIndex >= 0) {
      comparator = Comparator.comparing(
          line -> getColumnValue(line, columnIndex),
          comparator);
    }

    return comparator;
  }

  private String getColumnValue(String line, int columnIndex) {
    String[] parts = line.split(",");
    return columnIndex < parts.length ? parts[columnIndex].trim() : "";
  }

  private int compareNumeric(String o1, String o2) {
    try {
      double num1 = Double.parseDouble(o1);
      double num2 = Double.parseDouble(o2);
      return Double.compare(num1, num2);
    } catch (NumberFormatException e) {
      return o1.compareTo(o2);
    }
  }

  private void outputSortedLines(List<String> lines, String outputFile) throws IOException {
    if (outputFile != null) {
      try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputFile))) {
        for (String line : lines) {
          writer.write(line);
          writer.newLine();
        }
      }
    } else {
      lines.forEach(System.out::println);
    }
  }
}