package com.unixtools.core;

public class CommandExecutor {
  public void executeCommand(String commandName, String[] args) {
    Command command = CommandFactory.getCommand(commandName);
    if (command != null) {
      command.execute(args);
    } else {
      System.out.println("Command not found.");
    }
  }
}
