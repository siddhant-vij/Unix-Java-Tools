package com.unixtools.command.filemanagement;

import com.unixtools.core.Command;
import java.nio.file.Paths;

public class PwdCommand implements Command {
  @Override
  public void execute(String[] args) {
    if (args.length > 0) {
      System.out.println("Invalid usage. No flags/args are supported for pwd.");
      return;
    }

    String currentDir = getCurrentDirectory();
    System.out.println(currentDir);
  }

  private String getCurrentDirectory() {
    return Paths.get("").toAbsolutePath().normalize().toString();
  }
}
