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
      case "cp":
        return new CpCommand();
      case "rm":
        return new RmCommand();
      case "mv":
        return new MvCommand();
      case "touch":
        return new TouchCommand();
      case "pwd":
        return new PwdCommand();
      default:
        return null;
    }
  }
}
