package de.johannes.cloud;

import de.johannes.cloud.server.Server;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerManager {

    private final PortAllocator portAllocator;
    private final Map<String, Server> servers;

    public ServerManager(PortAllocator portAllocator) {
        this.servers = new HashMap<>();
        this.portAllocator = portAllocator;
    }

    public String startServer(String template, int maxPlayers) {
        Server server = new Server(portAllocator.allocatePort(), template, maxPlayers);
        System.out.printf("Started Server %s on port: %d\n", server.id(), server.config().port());
        if(servers.containsKey(server.id())) {
            throw new IllegalStateException("Identical Server ID found!");
        }
        server.start();
        server.process().killed(() -> {
            if(!this.kill(server.id())) {
                throw new NullPointerException("Server "+server.id()+" could not be killed: Not found!");
            }else {
                Logger.getGlobal().log(Level.INFO, "Killed server: " + server.id());
            }
        });
        servers.put(server.id(), server);
        return server.id();
    }

    public boolean kill(String id) {
        Server server = this.servers.get(id);
        if(server == null) return false;
        server.kill();
        this.servers.remove(id);
        return true;
    }

    public void killAll() {
        servers.keySet().forEach(this::kill);
    }

    public boolean writeToServer(String id, String command) {
        return this.servers().get(id).process().message(command);
    }
    public void writeToAllServers(String command) {
        for(String id : this.servers.keySet()) {
            writeToServer(id, command);
        }
    }

    public Map<String, Server> servers() {
        return servers;
    }
    public Server get(String id) {
        return this.servers().getOrDefault(id, null);
    }
}
