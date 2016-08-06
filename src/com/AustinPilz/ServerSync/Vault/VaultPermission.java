package com.AustinPilz.ServerSync.Vault;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.AustinPilz.ServerSync.ServerSync;

public class VaultPermission 
{
	/**
	 * Sync player groups
	 */
	protected synchronized void syncPlayerGroups()
	{
		if (ServerSync.vault.vaultPermission.isEnabled() && ServerSync.vault.vaultPermission.hasGroupSupport())
		{
				
			//2 -> get list of all worlds (via Bukkit.GetWorlds())
			List<World> worlds = Bukkit.getWorlds();
					
			//3-> For each online player
			for (Player player : Bukkit.getOnlinePlayers())
			{
				//4 ->For each world
				for (World world : worlds) 
				{
					StringBuilder worldGroups = new StringBuilder();
					
					//5 -> For each player group
					for (String group : ServerSync.vault.vaultPermission.getPlayerGroups(world.getName(), player))
					{
						worldGroups.append(group);
						worldGroups.append(",");
					}
					
					//Send groups for player world
					if (worldGroups.length() > 0)
					{
						//Only send if there are groups
						ServerSync.vault.vaultIO.sendPlayerGroups(player, world.toString(), worldGroups.substring(0, worldGroups.length() - 1));
					}
				}
				
				//General Groups
				StringBuilder generalGroups = new StringBuilder();
				for (String group : ServerSync.vault.vaultPermission.getPlayerGroups(player))
				{
					generalGroups.append(group);
					generalGroups.append(",");
				}
				
				if (generalGroups.length() > 0)
				{
					ServerSync.vault.vaultIO.sendPlayerGroups(player, "", generalGroups.substring(0, generalGroups.length() - 1));
				}
			}
		}
		else
		{
			if (ServerSync.verbose)
			{
				ServerSync.log.log(Level.INFO, ServerSync.consolePrefix + "Vault permissions is not enabled!");
			}
		}
	}
	
	/**
	 * Receives incoming player group update
	 * @param uuid
	 * @param world
	 * @param groups
	 */
	protected void incomingPlayerSync(String uuid, String world, String groups)
	{
		OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(uuid)); //Convert to offline
		
	
		List<String> incomingGroups = new LinkedList<String>(Arrays.asList(groups.split(",")));
		List<String> currentGroups = Arrays.asList(ServerSync.vault.vaultPermission.getPlayerGroups(world, player));
		
		List<String> toRemove = new LinkedList<String>(Arrays.asList(ServerSync.vault.vaultPermission.getPlayerGroups(world, player)));
		List<String> toAdd = new LinkedList<String>(Arrays.asList(groups.split(",")));
		
		toRemove.removeAll(incomingGroups); //needs to be removed
		toAdd.removeAll(currentGroups); //needs to be added		
	
		
		//Remove groups
		for (String group : toRemove)
		{
			if (!world.isEmpty())
			{
				//Group is world based
				ServerSync.vault.vaultPermission.playerRemoveGroup(world, player, group);
			}
			else
			{
				//Group is general
				ServerSync.vault.vaultPermission.playerRemoveGroup((Player) player, group);
			}
		}
		
		//Add Groups
		for (String group : toAdd)
		{
			if (!world.isEmpty())
			{
				//Group is world based
				ServerSync.vault.vaultPermission.playerAddGroup(world, player, group);
			}
			else
			{
				//Group is general
				ServerSync.vault.vaultPermission.playerAddGroup((Player)player, group);
			}
		}
	}
}
