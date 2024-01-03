package com.unixtools.core;

import com.unixtools.command.filemanagement.LsCommand;

public class CommandFactory {
  public static Command getCommand(String commandName) {
    switch (commandName) {
      case "ls":
        return new LsCommand();
      default:
        return null;
    }
  }
}
