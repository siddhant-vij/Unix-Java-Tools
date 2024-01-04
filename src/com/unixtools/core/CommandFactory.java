package com.unixtools.core;

import com.unixtools.command.filemanagement.LsCommand;
import com.unixtools.command.filemanagement.MkdirCommand;

public class CommandFactory {
  public static Command getCommand(String commandName) {
    switch (commandName) {
      case "ls":
        return new LsCommand();
      case "mkdir":
        return new MkdirCommand();
      default:
        return null;
    }
  }
}
