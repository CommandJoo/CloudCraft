package de.johannes;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Plugin(id = "server-cloud-registry", name = "Server Registry", version = "0.1.0-SNAPSHOT", description = "Velocity Plugin for automatically registering Servers provided by server cloud", authors = {"CommandJoo"})
public class VelocityCloud {

    private final ProxyServer server;
    private final Logger logger;

    @Inject
    public VelocityCloud(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Subscribe
    public void onInitialize(ProxyInitializeEvent e) {
        this.server.getScheduler().buildTask(this, this::synchServers).schedule();
        this.server.getScheduler().buildTask(this, this::synchServers).repeat(2, TimeUnit.SECONDS).schedule();
    }

    public void synchServers() {
        try {
            URLConnection connection = new URL("http://127.0.0.1:2200/servers").openConnection();
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            List<CloudCalls.ServerInfo> infos = CloudCalls.servers();
            for(RegisteredServer server : this.server.getAllServers()) {
                boolean exists = infos.stream()
                        .anyMatch(info -> info.id().equals(server.getServerInfo().getName()));
                if(!exists) {
                    try {
                        this.server.unregisterServer(server.getServerInfo());
                    } catch(Exception ex) {
                        logger.info("Server: "+server.getServerInfo().getName()+" can't be unregistered (doesn't exist).");
                    }
                }
            }
            for (CloudCalls.ServerInfo serverInfo : infos) {
                RegisteredServer rs = this.server.registerServer(new com.velocitypowered.api.proxy.server.ServerInfo(serverInfo.id(), new InetSocketAddress("127.0.0.1", serverInfo.port())));
            }
        } catch (Exception ex) {
            logger.warning("Unable to sync servers, couldn't connect to cloud!");
        }
    }

    @Subscribe
    public void onJoin(PlayerChooseInitialServerEvent event) {
        if(event.getInitialServer().isEmpty()) {
            findFallbackServer().ifPresent(event::setInitialServer);
        }
    }

    @Subscribe
    public void onKick(KickedFromServerEvent event) {
        Player player = event.getPlayer();
        Optional<RegisteredServer> fallback = this.server.getAllServers().stream()
                .filter(s -> !s.getServerInfo().getName().equals("standard"))
                .filter(s -> !s.equals(event.getServer())) // skip the server the player was kicked from
                .findFirst();

        if (fallback.isPresent()) {
            KickedFromServerEvent.ServerKickResult redirect =
                    KickedFromServerEvent.RedirectPlayer.create(fallback.get());
            event.setResult(redirect);
        } else {
            Component reason = Component.text("No lobby available, try again later");
            event.setResult(KickedFromServerEvent.DisconnectPlayer.create(reason));
        }
    }

    private Optional<RegisteredServer> findFallbackServer() {
        return this.server.getAllServers().stream()
                .filter((s) -> !s.getServerInfo().getName().equals("standard"))
                .findFirst();
    }
}