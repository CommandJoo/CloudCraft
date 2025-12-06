package de.johannes.cloud.server;

import de.johannes.Config;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerProcess {

    private final ServerConfig config;

    private Process process;
    private BufferedWriter writer;
    private final StringBuffer log;

    public ServerProcess(ServerConfig config) {
        this.config = config;
        this.log = new StringBuffer();
    }

    public boolean start() {
        ProcessBuilder builder = new ProcessBuilder().command(Config.JAVA_PATH, "-DserverId=" + (this.config.id()), "-Xmx"+this.config.maxRam()+"M", "-jar", "server.jar", "--port", (this.config.port() + ""), "--max-players", (this.config.maxPlayers() + ""), "nogui").directory(new File("runtime/" + this.config.id())).redirectOutput(ProcessBuilder.Redirect.PIPE);
        try {
            Process p = builder.start();
            this.process = p;
            this.writer = new BufferedWriter(new OutputStreamWriter(this.process.getOutputStream()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(this.process.getInputStream()));
            Thread logThread = new Thread(() -> {
                try {
                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        log.append(line + "\n");
                    }
                } catch (Exception _) {
                }
            });
            logThread.start();
            return true;
        } catch (Exception _) {
            return false;
        }
    }

    /**
     * Should only be used if the server process cant kill itself via the should_stop boolean received by the heartbeat request
     **/
    public void stop() {
        this.process.destroy();
    }

    public String log() {
        return log.toString();
    }

    public synchronized boolean message(String command) {
        try {
            writer.write(command);
            writer.newLine();
            writer.flush();
            return true;
        } catch (Exception ex) {
            Logger.getGlobal().log(Level.WARNING, "Error messaging server!");
            return false;
        }
    }

    public ProcessHandle handle() {
        Optional<ProcessHandle> handleOptional = ProcessHandle.of(this.process.pid());
        return handleOptional.orElse(null);
    }

    public void killed(Runnable c) {
        this.process.onExit().thenAccept(p -> {
            c.run();
        });
    }

}
