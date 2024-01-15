package com.unixtools.command.filecontent;

import com.unixtools.core.Command;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public class WcCommand implements Command {
  private static final long LARGE_FILE_THRESHOLD = 10 * 1024 * 1024;
  private static final String VALID_FLAGS = "lwcm";
  private boolean countLines = false;
  private boolean countWords = false;
  private boolean countBytes = false;
  private boolean countChars = false;

  private static class Counters {
    int lines = 0;
    int words = 0;
    int bytes = 0;
    int characters = 0;
  }

  @Override
  public void execute(String[] args) {
    List<String> flags = new ArrayList<>();
    List<String> filePaths = new ArrayList<>();
    parseArguments(args, flags, filePaths);

    if (!areFlagsValid(flags)) {
      System.out.println("Invalid flags. Valid flags are: " + VALID_FLAGS);
      return;
    }

    if (flags.isEmpty()) {
      countLines = countWords = countBytes = countChars = true;
    }

    String filePath = filePaths.isEmpty() ? null : filePaths.get(0);
    processFile(filePath);
  }

  private void parseArguments(String[] args, List<String> flags, List<String> paths) {
    for (String arg : args) {
      if (arg.startsWith("-")) {
        for (char flag : arg.substring(1).toCharArray()) {
          flags.add(String.valueOf(flag));
          switch (flag) {
            case 'l':
              countLines = true;
              break;
            case 'w':
              countWords = true;
              break;
            case 'c':
              countBytes = true;
              break;
            case 'm':
              countChars = true;
              break;
          }
        }
      } else {
        paths.add(arg);
      }
    }
  }

  private boolean areFlagsValid(List<String> flags) {
    return flags.stream().allMatch(flag -> VALID_FLAGS.contains(flag));
  }

  private void processFile(String filePath) {
    Counters counters = new Counters();

    if (filePath == null) {
      processStandardInput(counters);
      return;
    }

    Path path = Paths.get(filePath);
    if (!Files.exists(path) || !Files.isReadable(path)) {
      System.err.println("File does not exist or is not readable: " + filePath);
      return;
    }

    try {
      long fileSize = Files.size(path);
      if (fileSize >= LARGE_FILE_THRESHOLD) {
        processLargeFileConcurrently(path, fileSize, counters);
      } else {
        processSmallFile(path, counters);
      }
    } catch (IOException e) {
      System.err.println("Error processing file:" + e.getMessage());
    }
  }

  private void countFlagCounters(String line, Counters counters) {
    if (countLines) {
      counters.lines++;
    }
    if (countWords) {
      counters.words += new StringTokenizer(line).countTokens();
    }
    if (countChars) {
      counters.characters += line.length() + System.lineSeparator().length();
    }
    if (countBytes) {
      counters.bytes += line.getBytes(StandardCharsets.UTF_8).length
          + System.lineSeparator().getBytes(StandardCharsets.UTF_8).length;
    }
  }

  private void processStandardInput(Counters counters) {
    try (Scanner scanner = new Scanner(System.in)) {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        countFlagCounters(line, counters);
        printCounts(counters);
      }
    }
  }

  private void processSmallFile(Path path, Counters counters) throws IOException {
    try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
      String line;
      while ((line = reader.readLine()) != null) {
        countFlagCounters(line, counters);
      }
    }
    printCounts(counters);
  }

  private void processLargeFileConcurrently(Path path, long fileSize, Counters counters) throws IOException {
    ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    long segmentSize = fileSize / Runtime.getRuntime().availableProcessors();
    List<Future<int[]>> futures = new ArrayList<>();

    for (long offset = 0; offset < fileSize; offset += segmentSize) {
      final long startOffset = offset;
      final long endOffset = Math.min(offset + segmentSize, fileSize);
      futures.add(executor.submit(() -> processFileSegment(path, startOffset, endOffset, counters)));
    }

    for (Future<int[]> future : futures) {
      try {
        int[] counts = future.get();
        if (countLines) {
          counters.lines += counts[0];
        }
        if (countWords) {
          counters.words += counts[1];
        }
        if (countBytes) {
          counters.bytes += counts[2];
        }
        if (countChars) {
          counters.characters += counts[3];
        }
      } catch (InterruptedException | ExecutionException e) {
        System.err.println("Error processing file segment: " + e.getMessage());
      }
    }

    executor.shutdown();
    printCounts(counters);
  }

  private int[] processFileSegment(Path path, long startOffset, long endOffset, Counters counters) throws IOException {
    try (RandomAccessFile file = new RandomAccessFile(path.toFile(), "r")) {
      file.seek(startOffset);
      String line;
      while (file.getFilePointer() < endOffset && (line = file.readLine()) != null) {
        countFlagCounters(line, counters);
      }
    }

    return new int[] { counters.lines, counters.words, counters.bytes, counters.characters };
  }

  private void printCounts(Counters counters) {
    if (countLines) {
      System.out.print("Lines: " + counters.lines + " ");
    }
    if (countWords) {
      System.out.print("Words: " + counters.words + " ");
    }
    if (countBytes) {
      System.out.print("Bytes: " + counters.bytes + " ");
    }
    if (countChars) {
      System.out.print("Characters: " + counters.characters);
    }
    System.out.println();
  }
}
