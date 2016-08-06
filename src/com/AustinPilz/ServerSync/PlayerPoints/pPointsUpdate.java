package com.AustinPilz.ServerSync.PlayerPoints;

import org.bukkit.scheduler.BukkitRunnable;

import com.AustinPilz.ServerSync.ServerSync;

public class pPointsUpdate extends BukkitRunnable
{
	@Override
	public void run() 
	{
		if (ServerSync.playerPoints.isEnabled())
		{
			ServerSync.playerPoints.updateInternalBalances(); //Update internal balances
			ServerSync.playerPoints.syncBalances();
		}
	}
}
