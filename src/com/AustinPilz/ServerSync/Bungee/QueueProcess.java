package com.AustinPilz.ServerSync.Bungee;

import org.bukkit.scheduler.BukkitRunnable;

import com.AustinPilz.ServerSync.ServerSync;

public class QueueProcess extends BukkitRunnable
{
	@Override
	public void run() 
	{
		ServerSync.bungeeOutgoing.processQueue();
	}
}
