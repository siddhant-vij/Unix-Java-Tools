package com.unixtools;

import java.util.ArrayList;
import java.util.List;

import com.unixtools.core.CommandExecutor;

public class Main {
  public static void main(String[] args) {
    if (args.length == 0) {
      System.out.println("No command provided.");
      return;
    }

    String commandName = args[0];
    List<String> commandArgsList = new ArrayList<>();

    for (int i = 1; i < args.length; i++) {
      if (args[i].startsWith("-") && args[i].length() > 1) {
        for (int j = 1; j < args[i].length(); j++) {
          commandArgsList.add("-" + args[i].charAt(j));
        }
      } else {
        commandArgsList.add(args[i]);
      }
    }

    String[] commandArgs = commandArgsList.toArray(new String[0]);

    CommandExecutor executor = new CommandExecutor();
    executor.executeCommand(commandName, commandArgs);
  }
}
