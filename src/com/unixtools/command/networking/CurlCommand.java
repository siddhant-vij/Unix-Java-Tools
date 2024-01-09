package com.unixtools.command.networking;

import com.unixtools.core.Command;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CurlCommand implements Command {
  private static final String VALID_FLAGS = "oXH";
  private String outputFileName = null;
  private String requestMethod = "GET";
  private List<String> requestHeaders = new ArrayList<>();
  private String requestBody = "";

  @Override
  public void execute(String[] args) {
    List<String> flags = new ArrayList<>();
    List<String> urls = new ArrayList<>();
    List<String> unrecognizedFlags = new ArrayList<>();
    parseArguments(args, flags, urls, unrecognizedFlags);

    if (!unrecognizedFlags.isEmpty()) {
      System.out
          .println("Invalid flags: " + String.join(", ", unrecognizedFlags) + ". Valid flags are: " + VALID_FLAGS);
      return;
    }

    if (urls.isEmpty()) {
      System.out.println("curl: you must specify a URL.");
      return;
    }

    if (urls.size() > 1) {
      System.out.println("curl: you must specify only one URL.");
      return;
    }

    String url = urls.get(0);
    handleCurlRequest(url);
  }

  private void parseArguments(String[] args, List<String> flags, List<String> urls, List<String> unrecognizedFlags) {
    for (int i = 0; i < args.length; i++) {
      switch (args[i]) {
        case "-o":
          if (i + 1 < args.length)
            outputFileName = args[++i];
          break;
        case "-X":
          if (i + 1 < args.length)
            requestMethod = args[++i];
          break;
        case "-H":
          if (i + 1 < args.length)
            requestHeaders.add(args[++i]);
          break;
        case "-d":
          if (i + 1 < args.length)
            requestBody = args[++i];
          break;
        default:
          if (args[i].startsWith("-")) {
            unrecognizedFlags.add(args[i]);
          } else {
            urls.add(args[i]);
          }
          break;
      }
    }
  }

  private void handleCurlRequest(String urlString) {
    try {
      URL url = URI.create(urlString).toURL();
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();

      connection.setRequestMethod(requestMethod.toUpperCase());

      for (String header : requestHeaders) {
        String[] headerParts = header.split(": ");
        if (headerParts.length == 2) {
          connection.setRequestProperty(headerParts[0], headerParts[1]);
        }
      }

      printRequestHeaders(url, requestMethod, requestHeaders);

      if ("POST".equals(requestMethod.toUpperCase()) || "PUT".equals(requestMethod.toUpperCase())) {
        connection.setDoOutput(true);
        try (OutputStream os = connection.getOutputStream()) {
          byte[] input = requestBody.getBytes("utf-8");
          os.write(input, 0, input.length);
        }
      }

      printResponseHeaders(connection);

      try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
        String line;
        StringBuilder response = new StringBuilder();
        while ((line = reader.readLine()) != null) {
          response.append(line).append(System.lineSeparator());
        }

        if (outputFileName != null) {
          writeToFile(outputFileName, response.toString());
        } else {
          System.out.println(response.toString());
        }
      }
    } catch (IOException e) {
      System.out.println("Error executing curl command: " + e.getMessage());
    }
  }

  private void printRequestHeaders(URL url, String method, List<String> requestHeaders) {
    System.out.println("> " + method + " " + url.getPath() + " HTTP/1.1");
    System.out.println("> Host: " + url.getHost());
    for (String header : requestHeaders) {
      System.out.println("> " + header);
    }
    System.out.println(">");
  }

  private void printResponseHeaders(HttpURLConnection connection) {
    System.out.println("< " + connection.getHeaderField(null));
    connection.getHeaderFields().entrySet().stream()
        .filter(entry -> entry.getKey() != null)
        .forEach(entry -> {
          for (String value : entry.getValue()) {
            System.out.println("< " + entry.getKey() + ": " + value);
          }
        });
    System.out.println("<");
  }

  private void writeToFile(String fileName, String content) {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
      writer.write(content);
    } catch (IOException e) {
      System.out.println("Error writing to file: " + e.getMessage());
    }
  }
}
