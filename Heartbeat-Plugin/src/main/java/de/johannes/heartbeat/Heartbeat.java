package de.johannes.heartbeat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;

public class Heartbeat extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getScheduler().runTaskTimer(this, this::heartbeat, 0, 100);
    }

    @Override
    public void onDisable() {
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        heartbeat();
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        heartbeat();
    }

    public void onKicked(PlayerKickEvent e) {
        heartbeat();
    }

    public void heartbeat() {
        if (CloudCalls.heartbeat(new CloudCalls.ServerHeartbeat(System.getProperty("serverId"), Bukkit.getOnlinePlayers().stream().map(Player::getName).toList())))
        {
            Bukkit.shutdown();
        }
    }

}
