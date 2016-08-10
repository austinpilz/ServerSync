package com.AustinPilz.ServerSync.Listener;

import com.AustinPilz.ServerSync.ServerSync;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Created by austinpilz on 8/5/16.
 */
public class PlayerListener implements Listener
{
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent event)
    {
        if (event.getPlayer().hasPermission("ServerSync.Admin") && ServerSync.updateChecker.isUpdateNeeded())
        {
            event.getPlayer().sendMessage(ServerSync.chatPrefix + "There is an update for ServerSync! Currently running v" + ChatColor.RED + ServerSync.pluginVersion + ChatColor.WHITE + " and most recent is v" + ChatColor.GREEN + ServerSync.updateChecker.getLatestVersion() + ChatColor.WHITE + " Please visit " + ChatColor.YELLOW + "http://serversyncbungeecord.austinpilz.com" + ChatColor.WHITE + " to update");
        }
    }
}
