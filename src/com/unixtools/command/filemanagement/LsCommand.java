package com.unixtools.command.filemanagement;

import com.unixtools.core.Command;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class LsCommand implements Command {
  private static final String VALID_FLAGS = "alR";

  @Override
  public void execute(String[] args) {
    List<String> flags = new ArrayList<>();
    String path = parseArguments(args, flags);

    if (!validateFlags(flags)) {
      System.out.println("Invalid flag(s). Valid flags are: " + VALID_FLAGS);
      return;
    }

    File directory = validatePath(path);
    if (directory == null)
      return;

    processDirectory(directory, flags, 0, "");
  }

  private String parseArguments(String[] args, List<String> flags) {
    String path = ".";
    for (String arg : args) {
      if (arg.startsWith("-")) {
        for (char flag : arg.substring(1).toCharArray()) {
          flags.add(String.valueOf(flag));
        }
      } else {
        path = normalizePath(arg);
        break;
      }
    }
    return path;
  }

  private String normalizePath(String path) {
    String normalizedPath = path.replace("\\", "/");
    Path pathObj = Paths.get(normalizedPath).normalize();
    return pathObj.toString();
  }

  private boolean validateFlags(List<String> flags) {
    for (String flag : flags) {
      if (!VALID_FLAGS.contains(flag)) {
        return false;
      }
    }
    return true;
  }

  private File validatePath(String path) {
    File dir = new File(path);
    if (!dir.exists()) {
      System.out.println("The specified directory does not exist.");
      return null;
    }
    if (!dir.isDirectory()) {
      System.out.println("The specified path is not a directory.");
      return null;
    }
    return dir;
  }

  private void processDirectory(File dir, List<String> flags, int level, String prefix) {
    boolean longListing = flags.contains("l");
    boolean recursive = flags.contains("R");

    File[] filesList = dir.listFiles(file -> flags.contains("a") || !file.isHidden());
    if (filesList == null || filesList.length == 0) {
      System.out.println(prefix + "Empty directory.");
      return;
    }

    for (int i = 0; i < filesList.length; i++) {
      File file = filesList[i];
      printTreeStructure(file, i, filesList.length, prefix);
      printFileInfo(file, longListing);

      if (recursive && file.isDirectory()) {
        String nextPrefix = prefix + (i < filesList.length - 1 ? "|   " : "    ");
        processDirectory(file, flags, level + 1, nextPrefix);
      }
    }
  }

  private void printTreeStructure(File file, int index, int total, String prefix) {
    String connector = (index < total - 1) ? "├── " : "└── ";
    System.out.print(prefix + connector);
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
