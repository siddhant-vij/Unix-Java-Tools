package com.unixtools.command.filecontent;

import com.unixtools.core.Command;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GrepCommand implements Command {
  private static final long LARGE_FILE_THRESHOLD = 10 * 1024 * 1024;
  private static final int LARGE_DIR_THRESHOLD = 100;
  private static final String VALID_FLAGS = "cinfvwr";
  private boolean countLines = false;
  private boolean ignoreCase = false;
  private boolean lineNumber = false;
  private boolean invertMatch = false;
  private boolean matchWholeWord = false;
  private boolean recursive = false;
  private boolean ordered = false;
  private boolean unordered = false;
  private String patternFile = null;

  @Override
  public void execute(String[] args) {
    List<String> flags = new ArrayList<>();
    List<String> filePaths = new ArrayList<>();
    parseArguments(args, flags, filePaths);

    if (!areFlagsValid(flags)) {
      System.out.println("Invalid flags. Valid flags are: " + VALID_FLAGS);
      return;
    }

    if (filePaths.isEmpty() || (patternFile == null && filePaths.size() == 1)) {
      System.out.println("grep: No pattern or file specified.");
      return;
    }

    String pattern = patternFile == null ? filePaths.remove(0) : getPatternFromFile(patternFile);
    Pattern compiledPattern = Pattern.compile(pattern, ignoreCase ? Pattern.CASE_INSENSITIVE : 0);

    ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    List<Future<?>> futures = new ArrayList<>();

    for (String path : filePaths) {
      futures.add(executor.submit(() -> processFileOrDirectory(path, compiledPattern)));
    }

    futures.forEach(f -> {
      try {
        f.get();
      } catch (InterruptedException | ExecutionException e) {
        System.err.println("Error in processing: " + e.getMessage());
      }
    });

    executor.shutdown();
  }

  private void parseArguments(String[] args, List<String> flags, List<String> paths) {
    for (int i = 0; i < args.length; i++) {
      if (args[i].startsWith("-")) {
        for (char flag : args[i].substring(1).toCharArray()) {
          switch (flag) {
            case 'c':
              countLines = true;
              break;
            case 'i':
              ignoreCase = true;
              break;
            case 'n':
              lineNumber = true;
              break;
            case 'f':
              patternFile = args[++i];
              break;
            case 'v':
              invertMatch = true;
              break;
            case 'w':
              matchWholeWord = true;
              break;
            case 'r':
              recursive = true;
              break;
            case 'o':
              ordered = true;
              break;
            case 'u':
              unordered = true;
              break;
            default:
              flags.add(String.valueOf(flag));
              break;
          }
        }
      } else {
        paths.add(args[i]);
      }
    }
  }

  private boolean areFlagsValid(List<String> flags) {
    return flags.stream().allMatch(flag -> VALID_FLAGS.contains(flag.replace("-", "")));
  }

  private String normalizePath(String path) {
    String normalizedPath = path.replace("\\", "/");
    Path pathObj = Paths.get(normalizedPath).normalize();
    return pathObj.toString();
  }

  private String getPatternFromFile(String fileName) {
    StringBuilder patternBuilder = new StringBuilder();
    try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
      String line;
      while ((line = br.readLine()) != null) {
        patternBuilder.append(line).append("|");
      }
    } catch (IOException e) {
      System.out.println("Error reading pattern file: " + e.getMessage());
      return "";
    }
    return patternBuilder.toString().replaceAll("\\|$", "");
  }

  private void processFileOrDirectory(String path, Pattern pattern) {
    Path filePath = Paths.get(normalizePath(path));
    try {
      if (Files.isDirectory(filePath) && recursive) {
        processDirectory(filePath, pattern);
      } else if (Files.isRegularFile(filePath)) {
        processFile(filePath.toString(), pattern);
      }
    } catch (IOException e) {
      System.err.println("Error processing file/directory: " + e.getMessage());
    }
  }

  private void processFile(String filePath, Pattern pattern) throws IOException {
    if (Files.size(Paths.get(filePath)) > LARGE_FILE_THRESHOLD) {
      if (!ordered && !unordered) {
        throw new IllegalArgumentException(
            "At least one of -o (ordered) or -u (unordered) flags is required for concurrent processing.");
      }
      if (ordered && unordered) {
        System.out
            .println("Both ordered and unordered flags are set. Preferring unordered processing for performance.");
        unorderedProcessing(filePath, pattern);
      } else if (ordered) {
        orderedProcessing(filePath, pattern);
      } else {
        unorderedProcessing(filePath, pattern);
      }
    } else {
      grepFile(filePath, pattern);
    }
  }

  private void orderedProcessing(String filePath, Pattern pattern) throws IOException {
    processFileConcurrently(filePath, pattern, true);
  }

  private void unorderedProcessing(String filePath, Pattern pattern) throws IOException {
    processFileConcurrently(filePath, pattern, false);
  }

  private void grepFile(String filePath, Pattern pattern) {
    int matchingLineCount = 0;
    try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (matchesPattern(line, pattern)) {
          if (countLines) {
            matchingLineCount++;
            continue;
          }
          System.out.println((lineNumber ? matchingLineCount + ": " : "") + line);
        }
      }
      if (countLines) {
        System.out.println(matchingLineCount);
      }
    } catch (IOException e) {
      System.err.println("Error reading file: " + e.getMessage());
    }
  }

  private boolean matchesPattern(String line, Pattern pattern) {
    Matcher matcher = pattern.matcher(line);
    return matchWholeWord ? matcher.matches() : (invertMatch != matcher.find());
  }

  private void processFileConcurrently(String filePath, Pattern pattern, boolean ordered) throws IOException {
    RandomAccessFile file = new RandomAccessFile(filePath, "r");
    long fileSize = file.length();
    file.close();

    ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    long segmentSize = fileSize / LARGE_DIR_THRESHOLD;
    List<Future<List<String>>> futures = new ArrayList<>();

    for (long offset = 0; offset < fileSize; offset += segmentSize) {
      final long offsetFinal = offset;
      long endOffset = Math.min(offset + segmentSize, fileSize);
      futures.add(executor.submit(() -> processFileSegment(filePath, pattern, offsetFinal, endOffset)));
    }

    List<String> results = ordered ? combineAndOrderResults(futures) : combineResults(futures);
    results.forEach(System.out::println);

    executor.shutdown();
  }

  private List<String> combineResults(List<Future<List<String>>> futures) {
    List<String> combinedResults = new ArrayList<>();
    for (Future<List<String>> future : futures) {
      try {
        combinedResults.addAll(future.get());
      } catch (InterruptedException | ExecutionException e) {
        System.err.println("Error combining results: " + e.getMessage());
      }
    }
    return combinedResults;
  }

  private List<String> combineAndOrderResults(List<Future<List<String>>> futures) {
    List<String> combinedResults = combineResults(futures);
    combinedResults.sort(Comparator.naturalOrder());
    return combinedResults;
  }

  private List<String> processFileSegment(String filePath, Pattern pattern, long startOffset, long endOffset) {
    List<String> results = new ArrayList<>();
    try (RandomAccessFile file = new RandomAccessFile(filePath, "r")) {
      file.seek(startOffset);
      String line;
      while (file.getFilePointer() < endOffset && (line = file.readLine()) != null) {
        if (matchesPattern(line, pattern)) {
          results.add(startOffset + ": " + line);
        }
      }
    } catch (IOException e) {
      System.err.println("Error processing file segment: " + e.getMessage());
    }
    return results;
  }

  private void processDirectory(Path directory, Pattern pattern) throws IOException {
    try (Stream<Path> paths = Files.walk(directory)) {
      ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
      List<Future<?>> futures = paths.filter(Files::isRegularFile)
          .map(p -> executor.submit(() -> grepFile(p.toString(), pattern)))
          .collect(Collectors.toList());

      futures.forEach(f -> {
        try {
          f.get();
        } catch (InterruptedException | ExecutionException e) {
          System.err.println("Error processing directory file: " + e.getMessage());
        }
      });

      executor.shutdown();
    }
  }
}
