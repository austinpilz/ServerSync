package com.AustinPilz.ServerSync.Vault;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.libs.jline.internal.Log;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.AustinPilz.ServerSync.ServerSync;


public class VaultUpdate extends BukkitRunnable
{
	@Override
	public void run() 
	{
		//TODO
		if (Bukkit.getOnlinePlayers().size() > 0)
		{
			for (Player player: Bukkit.getOnlinePlayers()) 
			{
				 if (!ServerSync.vault.playerBalances.containsKey(player.getUniqueId().toString()))
				 {
					 ServerSync.vault.playerBalances.put(player.getUniqueId().toString(), ServerSync.vault.getVault().getBalance(player));
				 
					 if (ServerSync.verbose)
					 {
						 Log.info(ServerSync.consolePrefix + "Vault - added internal balance for " + player.getName());
					 }
				 }
			}
		}
		
		ServerSync.vault.syncBalances();
	}

}
