package com.AustinPilz.ServerSync.Vault;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.AustinPilz.ServerSync.ServerSync;


public class VaultListener implements Listener
{
	@EventHandler(priority = EventPriority.NORMAL)
	public void onLogin(PlayerLoginEvent event) 
	{
		if (ServerSync.vault.isEnabled())
		{
			//
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		if (ServerSync.vault.isEnabled())
		{
			//
		}
	}

}
