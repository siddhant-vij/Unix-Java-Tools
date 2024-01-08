package com.unixtools.command.networking;

import com.unixtools.core.Command;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class PingCommand implements Command {
  private static final String VALID_FLAGS = "csinwt";

  @Override
  public void execute(String[] args) {
    List<String> flags = new ArrayList<>();
    List<String> targets = new ArrayList<>();
    parseArguments(args, flags, targets);

    if (!areFlagsValid(flags)) {
      System.out.println("Invalid flags. Valid flags are: " + VALID_FLAGS);
      return;
    }

    if (targets.isEmpty()) {
      System.out.println("Invalid input. Usage: ping [flags] [host_or_IP_address]");
      return;
    }

    String target = targets.get(0);
    executePingCommand(target, flags);
  }

  private void parseArguments(String[] args, List<String> flags, List<String> targets) {
    boolean isFlag = false;

    for (String arg : args) {
      if (arg.startsWith("-")) {
        isFlag = true;
        flags.add(arg);
      } else if (isFlag && isInteger(arg)) {
        flags.add(arg);
        isFlag = false;
      } else {
        targets.add(arg);
        isFlag = false;
      }
    }
  }

  private boolean isInteger(String s) {
    try {
      Integer.parseInt(s);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  private boolean areFlagsValid(List<String> flags) {
    for (int i = 0; i < flags.size(); i++) {
      String flag = flags.get(i);
      if (flag.startsWith("-")) {
        String flagPart = flag.substring(1);
        if (!VALID_FLAGS.contains(flagPart)) {
          return false;
        }
      }
    }
    return true;
  }

  private void executePingCommand(String target, List<String> flags) {
    try {
      String[] command = buildPingCommand(target, flags);
      Process process = Runtime.getRuntime().exec(command);
      try (BufferedReader reader = new BufferedReader(
          new InputStreamReader(process.getInputStream()))) {
        String line;
        while ((line = reader.readLine()) != null) {
          System.out.println(line);
        }
      }
    } catch (IOException e) {
      System.out.println("Error executing ping command: " + e.getMessage());
    }
  }

  private String[] buildPingCommand(String target, List<String> flags) {
    boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
    List<String> commandParts = new ArrayList<>();
    commandParts.add("ping");

    for (String flag : flags) {
      if (isWindows) {
        commandParts.add(translateFlagForWindows(flag));
      } else {
        commandParts.add(flag);
      }
    }

    commandParts.add(target);
    return commandParts.toArray(new String[0]);
  }

  private String translateFlagForWindows(String flag) {
    String[] parts = flag.split(" ");
    String flagPart = parts[0];
    String valuePart = parts.length > 1 ? parts[1] : "";

    switch (flagPart) {
      case "-c":
        return "-n " + valuePart;
      case "-i":
        return "-i " + valuePart;
      case "-s":
        return "-l " + valuePart;
      case "-w":
        return "-w " + valuePart;
      case "-n":
        return "-a " + valuePart;
      case "-t":
        return "-t " + valuePart;
      default:
        return flag;
    }
  }
}
