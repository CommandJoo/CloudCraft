package de.johannes;

import de.johannes.cloud.ServerManager;
import de.johannes.cloud.PortAllocator;
import de.johannes.rest.RestAPI;
import sun.misc.Signal;

import java.awt.*;
import java.util.Scanner;

public class Main {
    static void main() {
        ServerManager manager = new ServerManager(new PortAllocator(25566, 30000));
        RestAPI restAPI = new RestAPI(manager);
        manager.startServer("template", 1000);
        manager.startServer("template2", 1);

        System.out.print(color("#77FFAA")+"> "+reset()+color("#55FFDD"));
        Scanner scanner = new Scanner(System.in);
        String line;
        while ((line = scanner.nextLine()) != null) {
            System.out.print(color("#77FFAA")+"> "+reset()+color("#55FFDD"));
            if (line.equals("exit")) break;
            else {
                manager.writeToAllServers(line);
            }
        }
        manager.killAll();
        restAPI.kill();
        Signal.handle(new Signal("INT"), _ -> {
            manager.killAll();
            restAPI.kill();
        });
        Signal.handle(new Signal("TERM"), _ -> {
            manager.killAll();
            restAPI.kill();
        });
    }

    public static String color(String hex) {
        Color c = Color.decode(hex);
        return String.format("\u001b[38;2;%d;%d;%dm", c.getRed(), c.getGreen(), c.getBlue());
    }

    public static String color(float r, float g, float b) {
        return String.format("\u001b[38;2;%d;%d;%dm", r, g, b);
    }

    public static String reset() {
        return "\u001b[0m";
    }

}
