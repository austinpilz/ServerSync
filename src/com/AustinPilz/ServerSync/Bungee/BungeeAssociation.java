package com.AustinPilz.ServerSync.Bungee;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import com.AustinPilz.ServerSync.ServerSync;

public class BungeeAssociation extends BukkitRunnable
{
	@Override
	public void run() 
	{
		if (ServerSync.bungeeServerName.isEmpty())
		{
			if (Bukkit.getOnlinePlayers().size() > 0)
			{
				ServerSync.bungeeOutgoing.sendServerNameRequest();
			}
		}
		else
		{
			if (Bukkit.getOnlinePlayers().size() > 0)
			{
				ServerSync.bungeeOutgoing.sendServerCheckin(); //Send server check-in
			}
		}
	}
}
