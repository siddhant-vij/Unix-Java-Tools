package com.unixtools.command.networking;

import com.unixtools.core.Command;

import java.net.*;
import java.util.Enumeration;

public class IfconfigCommand implements Command {

  @Override
  public void execute(String[] args) {
    if (args.length > 0) {
      System.out.println("ifconfig: Unexpected flags provided. This command does not take flags.");
      return;
    }

    try {
      Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
      while (interfaces.hasMoreElements()) {
        NetworkInterface networkInterface = interfaces.nextElement();
        displayInterfaceInformation(networkInterface);
      }
    } catch (SocketException e) {
      System.out.println("Error retrieving network interface information: " + e.getMessage());
    }
  }

  private void displayInterfaceInformation(NetworkInterface networkInterface) throws SocketException {
    System.out.println("Interface: " + networkInterface.getDisplayName());
    System.out.println("Name: " + networkInterface.getName());

    byte[] mac = networkInterface.getHardwareAddress();
    if (mac != null) {
      StringBuilder macAddress = new StringBuilder();
      for (int i = 0; i < mac.length; i++) {
        macAddress.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
      }
      System.out.println("MAC Address: " + macAddress.toString());
    }

    System.out.println("MTU: " + networkInterface.getMTU());
    System.out.println("Up: " + networkInterface.isUp());
    System.out.println("Loopback: " + networkInterface.isLoopback());
    System.out.println("Virtual: " + networkInterface.isVirtual());

    Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
    while (inetAddresses.hasMoreElements()) {
      InetAddress inetAddress = inetAddresses.nextElement();
      System.out.println("IP Address: " + inetAddress.getHostAddress());
    }

    System.out.println();
  }
}
