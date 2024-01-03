package com.unixtools;

import com.unixtools.core.CommandExecutor;

public class Main {
  public static void main(String[] args) {
    if (args.length == 0) {
      System.out.println("No command provided.");
      return;
    }

    String commandName = args[0];
    String[] commandArgs = new String[args.length - 1];
    System.arraycopy(args, 1, commandArgs, 0, args.length - 1);

    CommandExecutor executor = new CommandExecutor();
    executor.executeCommand(commandName, commandArgs);
  }
}
