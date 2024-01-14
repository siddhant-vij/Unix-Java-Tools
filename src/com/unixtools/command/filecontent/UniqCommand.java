package com.unixtools.command.filecontent;

import com.unixtools.core.Command;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class UniqCommand implements Command {
  private static final String VALID_FLAGS = "cdui";
  private boolean countOccurrences = false;
  private boolean onlyDuplicates = false;
  private boolean onlyUniques = false;
  private boolean ignoreCase = false;
  private String inputFilePath = null;
  private String outputFilePath = null;

  @Override
  public void execute(String[] args) {
    List<String> flags = new ArrayList<>();
    parseArguments(args, flags);

    if (!areFlagsValid(flags)) {
      System.out.println("Invalid flags. Valid flags are: " + VALID_FLAGS);
      return;
    }

    try {
      List<String> lines = (inputFilePath == null || inputFilePath.equals("-"))
          ? new BufferedReader(new InputStreamReader(System.in)).lines().collect(Collectors.toList())
          : Files.readAllLines(Paths.get(normalizePath(inputFilePath)));

      List<String> uniqueLines = processLines(lines);
      outputProcessedLines(uniqueLines, outputFilePath);
    } catch (IOException e) {
      System.err.println("Error processing file: " + e.getMessage());
    }
  }

  private void parseArguments(String[] args, List<String> flags) {
    for (int i = 0; i < args.length; i++) {
      if (args[i].startsWith("-") && args[i].length() > 1) {
        for (char flag : args[i].substring(1).toCharArray()) {
          processFlag(flag, flags);
        }
      } else {
        if (inputFilePath == null) {
          inputFilePath = args[i];
        } else if (outputFilePath == null) {
          outputFilePath = args[i];
        }
      }
    }
  }

  private void processFlag(char flag, List<String> flags) {
    switch (flag) {
      case 'c':
        countOccurrences = true;
        break;
      case 'd':
        onlyDuplicates = true;
        break;
      case 'u':
        onlyUniques = true;
        break;
      case 'i':
        ignoreCase = true;
        break;
      default:
        flags.add(String.valueOf(flag));
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

  private List<String> processLines(List<String> lines) {
    Map<String, Integer> lineOccurrences = new LinkedHashMap<>();
    for (String line : lines) {
      String processedLine = ignoreCase ? line.toLowerCase() : line;
      lineOccurrences.put(processedLine, lineOccurrences.getOrDefault(processedLine, 0) + 1);
    }

    return lineOccurrences.entrySet().stream()
        .filter(entry -> filterCondition(entry))
        .map(entry -> formatLine(entry))
        .collect(Collectors.toList());
  }

  private boolean filterCondition(Map.Entry<String, Integer> entry) {
    if (onlyDuplicates)
      return entry.getValue() > 1;
    if (onlyUniques)
      return entry.getValue() == 1;
    return true;
  }

  private String formatLine(Map.Entry<String, Integer> entry) {
    return countOccurrences ? entry.getValue() + " " + entry.getKey() : entry.getKey();
  }

  private void outputProcessedLines(List<String> lines, String outputFile) throws IOException {
    if (outputFile != null && !outputFile.equals("-")) {
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
