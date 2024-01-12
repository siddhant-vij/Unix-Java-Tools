package com.unixtools.core;

import com.unixtools.command.filemanagement.*;
import com.unixtools.command.filecontent.*;
import com.unixtools.command.networking.*;

public class CommandFactory {
  public static Command getCommand(String commandName) {
    switch (commandName) {
      /*
       * File and Directory Management
       */
      case "ls":
        return new LsCommand();
      case "mkdir":
        return new MkdirCommand();
      case "rmdir":
        return new RmdirCommand();
      case "cp":
        return new CpCommand();
      case "rm":
        return new RmCommand();
      case "mv":
        return new MvCommand();
      case "touch":
        return new TouchCommand();
      case "pwd":
        return new PwdCommand();
      case "df":
        return new DfCommand();
      case "du":
        return new DuCommand();
      case "find":
        return new FindCommand();
      /*
       * Networking Capabilities
       */
      case "ping":
        return new PingCommand();
      case "curl":
        return new CurlCommand();
      case "wget":
        return new WgetCommand();
      case "ifconfig":
        return new IfconfigCommand();
      case "traceroute":
        return new TracerouteCommand();
      /*
       * File Content Manipulation
       */
      case "echo":
        return new EchoCommand();
      case "cat":
        return new CatCommand();
      default:
        return null;
    }
  }
}
