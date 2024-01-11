package com.unixtools.command.networking;

import com.unixtools.core.Command;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class WgetCommand implements Command {
  private static final String VALID_FLAGS = "Oq";
  private String outputFileName = null;
  private boolean quietMode = false;

  @Override
  public void execute(String[] args) {
    List<String> flags = new ArrayList<>();
    List<String> urls = new ArrayList<>();
    parseArguments(args, flags, urls);

    if (!areFlagsValid(flags)) {
      System.out.println("Invalid flags. Valid flags are: " + VALID_FLAGS);
      return;
    }

    if (urls.isEmpty()) {
      System.out.println("wget: URL not specified.");
      return;
    }

    if (urls.size() > 1) {
      System.out.println("wget: Only one URL can be specified.");
      return;
    }

    String url = urls.get(0);
    downloadFile(url);
  }

  private void parseArguments(String[] args, List<String> flags, List<String> urls) {
    for (int i = 0; i < args.length; i++) {
      if (args[i].startsWith("-")) {
        for (char flag : args[i].substring(1).toCharArray()) {
          if (flag == 'O' && i + 1 < args.length) {
            outputFileName = args[++i];
          } else if (flag == 'q') {
            quietMode = true;
          } else {
            flags.add(String.valueOf(flag));
          }
        }
      } else {
        urls.add(args[i]);
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

  private void downloadFile(String urlString) {
    try {
      URL url = URI.create(urlString).toURL();
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
          OutputStream out = determineOutputStream()) {
        byte[] buffer = new byte[1024];
        int count;
        while ((count = in.read(buffer)) != -1) {
          out.write(buffer, 0, count);
        }
        if (!quietMode) {
          System.out.println("\nDownload completed: " + urlString);
        }
      }
    } catch (IOException e) {
      System.out.println("Error downloading file: " + e.getMessage());
    }
  }

  private OutputStream determineOutputStream() throws FileNotFoundException {
    if (outputFileName != null) {
      return new FileOutputStream(outputFileName);
    } else {
      return System.out;
    }
  }
}
