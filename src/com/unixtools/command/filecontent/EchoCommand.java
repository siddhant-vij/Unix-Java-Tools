package com.unixtools.command.filecontent;

import com.unixtools.core.Command;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class EchoCommand implements Command {
  private static final String VALID_FLAGS = "neo";

  @Override
  public void execute(String[] args) {
    List<String> flags = new ArrayList<>();
    List<String> texts = new ArrayList<>();
    parseArguments(args, flags, texts);

    if (!areFlagsValid(flags)) {
      System.out.println("Invalid flag(s). Valid flags are: " + VALID_FLAGS);
      return;
    }

    boolean noNewLine = flags.contains("n");
    boolean interpretEscapes = flags.contains("e");
    String outputFilePath = flags.contains("o") && !texts.isEmpty()
        ? normalizePath(texts.remove(texts.size() - 1))
        : null;

    String textToPrint = String.join(" ", texts);
    if (interpretEscapes) {
      textToPrint = textToPrint
          .replace("\\n", "\n")
          .replace("\\t", "\t");
    }

    try {
      printOrWrite(textToPrint, noNewLine, outputFilePath);
    } catch (IOException e) {
      System.out.println("Error writing to file: " + e.getMessage());
    }
  }

  private void parseArguments(String[] args, List<String> flags, List<String> texts) {
    for (String arg : args) {
      if (arg.startsWith("-")) {
        for (char flag : arg.substring(1).toCharArray()) {
          flags.add(String.valueOf(flag));
        }
      } else {
        texts.add(arg);
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

  private void printOrWrite(String text, boolean noNewLine, String outputFilePath) throws IOException {
    if (outputFilePath != null) {
      try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
        writer.write(text);
        if (!noNewLine) {
          writer.newLine();
        }
      }
    } else {
      System.out.print(text);
      if (!noNewLine) {
        System.out.println();
      }
    }
  }
}
