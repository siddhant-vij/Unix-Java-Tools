package com.unixtools.command.filemanagement;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import com.unixtools.core.Command;

public class LsCommand implements Command {
  private static final String VALID_FLAGS = "-a, -l, -R";

  @Override
  public void execute(String[] args) {
    List<String> arguments = Arrays.asList(args);
    if (!arguments.stream().allMatch(arg -> arg.equals("-a") || arg.equals("-l") || arg.equals("-R"))) {
      System.out.println("Invalid flag(s). Valid flags are: " + VALID_FLAGS);
      return;
    }

    File currentDir = new File(".");
    processDirectory(currentDir, arguments, 0, "");
  }

  private void processDirectory(File dir, List<String> arguments, int level, String prefix) {
    boolean longListing = arguments.contains("-l");
    boolean recursive = arguments.contains("-R");

    File[] filesList = dir.listFiles(file -> arguments.contains("-a") || !file.isHidden());
    if (filesList == null || filesList.length == 0) {
      if (level == 0) {
        System.out.println("..");
      }
      return;
    }

    for (int i = 0; i < filesList.length; i++) {
      File file = filesList[i];
      String connector = (i < filesList.length - 1) ? "├── " : "└── ";
      String childPrefix = (i < filesList.length - 1) ? "|   " : "    ";

      System.out.print(prefix + connector);
      printFileInfo(file, longListing);

      if (recursive && file.isDirectory()) {
        processDirectory(file, arguments, level + 1, prefix + childPrefix);
      }
    }
  }

  private void printFileInfo(File file, boolean longListing) {
    if (longListing) {
      SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
      System.out.printf("%-20s %s%n", sdf.format(file.lastModified()), file.getName());
    } else {
      System.out.println(file.getName());
    }
  }
}