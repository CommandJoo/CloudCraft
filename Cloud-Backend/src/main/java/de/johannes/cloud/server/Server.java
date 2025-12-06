package de.johannes.cloud.server;

import de.johannes.cloud.FileManager;
import de.johannes.rest.CloudCalls;

import java.util.List;

public class Server {

    private final ServerConfig config;
    private final ServerProcess server;

    private boolean shouldStop = false;

    private List<String> onlinePlayers;
    private CloudCalls.ServerStatus status = CloudCalls.ServerStatus.STARTING;

    public Server(int port, String template, int maxPlayers) {
        this.config = new ServerConfig(512, port, template, maxPlayers);
        this.server = new ServerProcess(this.config);
    }

    public void start() {
        FileManager.copyRuntime(this);
        this.server.start();
    }

    public void kill() {
        FileManager.deleteRuntime(this);
    }

    public String id() {
        return this.config.id();
    }
    public ServerConfig config() {
        return config;
    }
    public ServerProcess process() {
        return server;
    }

    public int maxPlayers() {
        return this.config.maxPlayers();
    }
    public List<String> onlinePlayers() {
        return onlinePlayers;
    }
    public void onlinePlayers(List<String> onlinePlayers) {
        this.onlinePlayers = onlinePlayers;
    }

    public void status(CloudCalls.ServerStatus status) {
        this.status = status;
    }
    public CloudCalls.ServerStatus status() {
        return this.status;
    }

    public boolean shouldStop() {
        return shouldStop;
    }
    public void shouldStop(boolean shouldStop) {
        this.shouldStop = shouldStop;
    }


    public CloudCalls.ServerInfo info() {
        return new CloudCalls.ServerInfo(this.config().id(), this.config().maxPlayers(), this.onlinePlayers(), this.config().port(), this.status());
    }
}
