package com.unixtools.command.filecontent;

import com.unixtools.core.Command;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TrCommand implements Command {
  private static final String VALID_FLAGS = "dsc";
  private boolean deleteMode = false;
  private boolean squeezeMode = false;
  private boolean complementMode = false;
  private String set1;
  private String set2;

  @Override
  public void execute(String[] args) {
    List<String> flags = new ArrayList<>();
    List<String> sets = new ArrayList<>();
    parseArguments(args, flags, sets);

    if (!areFlagsValid(flags)) {
      System.out.println("Invalid flags. Valid flags are: " + VALID_FLAGS);
      return;
    }

    if (deleteMode && sets.size() != 1) {
      System.out.println("tr: Exactly one set is required with delete mode.");
      return;
    }
    if (squeezeMode && sets.size() != 1) {
      System.out.println("tr: Exactly one set is required with squeeze mode.");
      return;
    }
    if (!deleteMode && !squeezeMode && sets.size() != 2) {
      System.out.println(
          "tr: Two sets are required for simple replacement without delete or squeeze mode.");
      return;
    }

    set1 = expandSet(sets.get(0));
    set2 = sets.size() > 1 ? expandSet(sets.get(1)) : "";

    try {
      processInput();
    } catch (IOException e) {
      System.err.println("Error processing input: " + e.getMessage());
    }
  }

  private void parseArguments(String[] args, List<String> flags, List<String> sets) {
    for (String arg : args) {
      if (arg.startsWith("-")) {
        for (char flag : arg.substring(1).toCharArray()) {
          flags.add(String.valueOf(flag));
          switch (flag) {
            case 'd':
              deleteMode = true;
              break;
            case 's':
              squeezeMode = true;
              break;
            case 'c':
              complementMode = true;
              break;
          }
        }
      } else {
        sets.add(arg);
      }
    }
  }

  private boolean areFlagsValid(List<String> flags) {
    return flags.stream().allMatch(flag -> VALID_FLAGS.contains(flag));
  }

  private String expandSet(String set) {
    set = set.replace("[:alnum:]", "a-zA-zA-Z0-9")
        .replace("[:alpha:]", "a-zA-Z")
        .replace("[:digit:]", "0-9")
        .replace("[:lower:]", "a-z")
        .replace("[:upper:]", "A-Z")
        .replace("[:special:]", "!@#$%^&*()_+{}|:<>?[];',./`~\"");
    StringBuilder expandedSet = new StringBuilder();
    for (int i = 0; i < set.length(); i++) {
      if (i + 2 < set.length() && set.charAt(i + 1) == '-') {
        char start = set.charAt(i);
        char end = set.charAt(i + 2);
        for (char ch = start; ch <= end; ch++) {
          expandedSet.append(ch);
        }
        i += 2;
      } else {
        expandedSet.append(set.charAt(i));
      }
    }
    return expandedSet.toString();
  }

  private void processInput() throws IOException {
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
      String line;
      while ((line = reader.readLine()) != null) {
        String processedLine = processLine(line);
        System.out.println(processedLine);
      }
    }
  }

  private String processLine(String line) {
    StringBuilder result = new StringBuilder();
    char prevChar = '\0';

    for (char ch : line.toCharArray()) {
      boolean inSet1 = set1.indexOf(ch) != -1;

      if (deleteMode) {
        boolean shouldDelete = complementMode ? !inSet1 : inSet1;
        if (shouldDelete)
          continue;
      }

      if (squeezeMode) {
        if (ch == prevChar && inSet1) {
          continue;
        }
      } else if (!deleteMode) {
        int index = set1.indexOf(ch);
        if (complementMode) {
          if (index == -1) {
            ch = set2.isEmpty() ? ch : set2.charAt(0);
          }
        } else {
          if (index != -1) {
            ch = (index < set2.length()) ? set2.charAt(index) : set2.charAt(set2.length() - 1);
          }
        }
      }

      result.append(ch);
      prevChar = ch;
    }
    return result.toString();
  }

}
