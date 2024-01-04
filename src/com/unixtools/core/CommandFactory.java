package com.unixtools.core;

import com.unixtools.command.filemanagement.*;

public class CommandFactory {
  public static Command getCommand(String commandName) {
    switch (commandName) {
      case "ls":
        return new LsCommand();
      case "mkdir":
        return new MkdirCommand();
      case "rmdir":
        return new RmdirCommand();
      default:
        return null;
    }
  }
}
