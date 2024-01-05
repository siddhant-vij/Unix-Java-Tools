package com.unixtools.command.filemanagement;

import com.unixtools.core.Command;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FindCommand implements Command {
  private static final String VALID_FLAGS = "name,type";
  private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

  @Override
  public void execute(String[] args) {
    Map<String, String> flags = parseArguments(args);
    if (!validateFlags(flags.keySet())) {
      System.out.println("Invalid flags. Valid flags are: -name, -type");
      return;
    }

    String searchPath = ".";
    if (flags.containsKey("path")) {
      searchPath = normalizePath(flags.get("path"));
    }

    File searchDirectory = new File(searchPath);
    if (!searchDirectory.exists()) {
      System.out.println("Specified path does not exist: " + searchPath);
      return;
    }

    String nameCriteria = flags.get("name");
    String typeCriteria = flags.get("type");

    CountDownLatch latch = new CountDownLatch(1);
    findFiles(Paths.get(searchPath), nameCriteria, typeCriteria, latch);
    try {
      latch.await(); // Wait for all tasks to complete
    } catch (InterruptedException e) {
      System.out.println("Search interrupted.");
    }
    executor.shutdown();
  }

  private Map<String, String> parseArguments(String[] args) {
    Map<String, String> flags = new HashMap<>();
    String lastFlag = null;

    for (String arg : args) {
      if (arg.startsWith("-")) {
        lastFlag = arg.substring(1);
        flags.put(lastFlag, null);
      } else {
        if (lastFlag != null && flags.containsKey(lastFlag)) {
          flags.put(lastFlag, arg);
          lastFlag = null;
        } else {
          flags.put("path", normalizePath(arg));
        }
      }
    }
    return flags;
  }

  private boolean validateFlags(Set<String> flagKeys) {
    Set<String> validFlags = new HashSet<>(Arrays.asList(VALID_FLAGS.split(",")));
    for (String flag : flagKeys) {
      if (!validFlags.contains(flag) && !flag.equals("path")) {
        return false;
      }
    }
    return true;
  }

  private void findFiles(Path path, String nameCriteria, String typeCriteria, CountDownLatch latch) {
    executor.execute(() -> {
      File[] files = path.toFile().listFiles();
      if (files == null) {
        latch.countDown();
        return;
      }

      CountDownLatch internalLatch = new CountDownLatch(files.length);
      for (File file : files) {
        if (file.isDirectory()) {
          if (typeCriteria == null || "d".equals(typeCriteria)) {
            checkAndPrintFile(file, nameCriteria, typeCriteria);
          }
          findFiles(file.toPath(), nameCriteria, typeCriteria, internalLatch);
        } else {
          if (typeCriteria == null || "f".equals(typeCriteria)) {
            checkAndPrintFile(file, nameCriteria, typeCriteria);
          }
          internalLatch.countDown();
        }
      }

      try {
        internalLatch.await();
      } catch (InterruptedException e) {
        System.out.println("Subdirectory search interrupted.");
      }
      latch.countDown();
    });
  }

  private void checkAndPrintFile(File file, String nameCriteria, String typeCriteria) {
    boolean nameMatch = (nameCriteria == null || file.getName().contains(nameCriteria));
    boolean typeMatch = true;

    if (typeCriteria != null) {
      switch (typeCriteria) {
        case "f":
          typeMatch = file.isFile();
          break;
        case "d":
          typeMatch = file.isDirectory();
          break;
      }
    }

    if (nameMatch && typeMatch) {
      System.out.println(file.getAbsolutePath());
    }
  }

  private String normalizePath(String path) {
    String normalizedPath = path.replace("\\", "/");
    if (normalizedPath.equals(".")) {
      normalizedPath = Paths.get("").toAbsolutePath().toString();
    }
    Path pathObj = Paths.get(normalizedPath).normalize();
    return pathObj.toString();
  }
}
