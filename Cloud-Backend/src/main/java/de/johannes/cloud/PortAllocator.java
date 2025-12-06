package de.johannes.cloud;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class PortAllocator {

    private final int minPort;
    private final int maxPort;
    private final List<Integer> allocated = new ArrayList<>();

    public PortAllocator(int minPort, int maxPort) {
        this.minPort = minPort;
        this.maxPort = maxPort;
    }

    public synchronized int allocatePort() {
        for (int port = minPort; port <= maxPort; port++) {

            if (allocated.contains(port)) continue;
            if (!isPortAvailable(port)) continue;

            allocated.add(port);
            return port;
        }
        throw new IllegalStateException("No free ports available!");
    }

    public synchronized void freePort(int port) {
        allocated.remove(port);
    }

    private boolean isPortAvailable(int port) {
        try (ServerSocket socket = new ServerSocket()) {
            socket.setReuseAddress(false);
            socket.bind(new InetSocketAddress("0.0.0.0", port));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
