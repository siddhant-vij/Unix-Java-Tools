package com.unixtools.command.networking;

import com.unixtools.core.Command;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TracerouteCommand implements Command {
  private static final String VALID_FLAGS = "mnw";
  private int maxHops = 30; // Default max hops
  private boolean numericOutput = false;
  private int timeout = 3000; // Default timeout in milliseconds

  @Override
  public void execute(String[] args) {
    List<String> flags = new ArrayList<>();
    List<String> hosts = new ArrayList<>();
    parseArguments(args, flags, hosts);

    if (!areFlagsValid(flags)) {
      System.out.println("Invalid flags. Valid flags are: " + VALID_FLAGS);
      return;
    }

    if (hosts.isEmpty()) {
      System.out.println("traceroute: Host address not specified.");
      return;
    }

    String hostAddress = hosts.get(0);
    executeNativeTracerouteCommand(hostAddress);
  }

  private void parseArguments(String[] args, List<String> flags, List<String> hosts) {
    for (int i = 0; i < args.length; i++) {
      switch (args[i]) {
        case "-m":
          if (i + 1 < args.length) {
            maxHops = Integer.parseInt(args[++i]);
          }
          break;
        case "-n":
          numericOutput = true;
          break;
        case "-w":
          if (i + 1 < args.length) {
            timeout = Integer.parseInt(args[++i]);
          }
          break;
        default:
          if (args[i].startsWith("-")) {
            flags.add(args[i]);
          } else {
            hosts.add(args[i]);
          }
          break;
      }
    }
  }

  private boolean areFlagsValid(List<String> flags) {
    for (String flag : flags) {
      if (!VALID_FLAGS.contains(flag)) {
        return false;
      }
    }
    return true;
  }

  private void executeNativeTracerouteCommand(String hostAddress) {
    String osName = System.getProperty("os.name").toLowerCase();
    List<String> command = new ArrayList<>();

    if (osName.startsWith("windows")) {
      command.add("tracert");
      if (numericOutput) {
        command.add("-d");
      }
      command.add("-w");
      command.add(String.valueOf(timeout));
      command.add("-h");
      command.add(String.valueOf(maxHops));
    } else {
      command.add("traceroute");
      if (numericOutput) {
        command.add("-n");
      }
      command.add("-w");
      command.add(String.valueOf(timeout / 1000));
      command.add("-m");
      command.add(String.valueOf(maxHops));
    }

    command.add(hostAddress);

    try {
      ProcessBuilder processBuilder = new ProcessBuilder(command);
      Process process = processBuilder.start();

      try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        String line;
        while ((line = reader.readLine()) != null) {
          System.out.println(line);
        }
      }
    } catch (Exception e) {
      System.out.println("Error executing traceroute command: " + e.getMessage());
    }
  }
}
