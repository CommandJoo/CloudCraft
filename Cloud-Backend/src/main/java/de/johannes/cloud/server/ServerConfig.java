package de.johannes.cloud.server;

import de.johannes.cloud.FileManager;

import java.io.File;
import java.util.Random;

public class ServerConfig {

    private final int maxRam;

    private final int port;
    private final String template;

    private final String id;
    private final File directory;

    private final int maxPlayers;

    public ServerConfig(int maxRam, int port, String template, int maxPlayers) {
        if(maxRam < 512) throw new IllegalArgumentException("The minimum amount of ram per server is 512M");
        this.maxRam = maxRam;
        this.port = port;
        this.template = template;
        //TODO implement better id generation -> server type etc
        this.id = "server-"+(new Random(port).nextInt(1000000, 9999999));
        this.directory = FileManager.runtimeDirectory(this);
        this.maxPlayers = maxPlayers;
    }

    public int maxRam() {
        return maxRam;
    }

    public int port() {
        return port;
    }

    public String template() {
        return template;
    }

    public String id() {
        return id;
    }

    public File directory() {
        return directory;
    }

    public int maxPlayers() {
        return maxPlayers;
    }

}
