package com.AustinPilz.ServerSync.Vault;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.craftbukkit.libs.jline.internal.Log;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.AustinPilz.ServerSync.ServerSync;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

public class Vault 
{
	private boolean enabled;
	private Economy vault;
	protected Permission vaultPermission;
	
	//ServerSync Vault Components
	VaultPermission serversyncVaultPermission;
	
	//Storage
	public HashMap<String, Long> lastUpdated; //Players and last system time balance was updated
	
	public HashMap<String, Double> playerBalances; //players balances to compare changes
	public HashMap<String, OfflinePlayer> updateNeeded; //players to be updated during next sync
	
	//IO
	public static VaultIO vaultIO;
	
	//Strings
	public static final String vaultSubchannel = "Vault";
	public static final String economySubchannel = "Economy";
	public static final String permissionSubchannel = "Permission";
	
	public Vault()
	{
		enabled = false;
		lastUpdated = new HashMap<String, Long>();
		playerBalances = new HashMap<String, Double>();
		updateNeeded = new HashMap<String, OfflinePlayer>();
		vaultIO = new VaultIO();
		setup();
		serversyncVaultPermission = new VaultPermission();	
	}

	/**
	 * Returns boolean if vault is enabled on the server
	 */
	public boolean isEnabled()
	{
		return enabled;
	}
	
	/**
	 * Checks for and sets up vault hook
	 */
	private void setup()
	{
		if (!setupEconomy() || !(setupPermissions()))
		{
			ServerSync.log.log(Level.SEVERE, ServerSync.consolePrefix + "Vault not found");
		}
		else
		{
			enabled = true;
			ServerSync.log.log(Level.INFO, ServerSync.consolePrefix + "Vault hooked!");
		}
	}
	
	/**
	 * Setup vault
	 */
	private boolean setupEconomy()
	{
		if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) 
        {
            return false;
        }
        
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) 
        {
            return false;
        }
        
        vault = rsp.getProvider();
        
        return vault != null;
	}
	
	/**
	 * Setup Vault Permissions
	 */
	private boolean setupPermissions()
    {
        RegisteredServiceProvider<Permission> permissionProvider = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) 
        {
            vaultPermission = permissionProvider.getProvider();
        }
        return (vaultPermission != null);
    }
	
	/**
	 * Return economy vault
	 */
	public Economy getVault()
	{
		return vault;
	}
	
	/**
	 * Incoming balance update for supplied player
	 * @param playerName Username of player
	 * @param playerBalane Balance to be updated to
	 */
	public void incomingEconomySync(String playerName, double playerBalance)
	{
		//Add them to our balances list
		OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
		playerBalances.put(player.getUniqueId().toString(), playerBalance);
		
		//Compare balances - we only update the value if it's different
		if (ServerSync.vault.getVault().getBalance(player) != playerBalance)
		{
			if (ServerSync.vault.getVault().getBalance(player) > playerBalance)
			{
				//The current balance is more than the received balance
				ServerSync.vault.getVault().withdrawPlayer(player, ServerSync.vault.getVault().getBalance(player) - playerBalance);
			}
			else
			{
				//The current balance is less than the received balance
				ServerSync.vault.getVault().depositPlayer(player, playerBalance - ServerSync.vault.getVault().getBalance(player));
			}
			
			//If log requested
			if (ServerSync.verbose)
			{
				Log.info(ServerSync.consolePrefix + "Balance received for " + playerName + " - balance updated");
			}
		}
		else
		{
			if (ServerSync.verbose)
			{
				Log.info(ServerSync.consolePrefix + "Balance received for " + playerName + " but balance was the same - update ignored");
			}
		}
		
	}
	
	/**
	 * Sends balance for supplied player and balance
	 */
	private void outgoingSync(String playerName, double playerBalance)
	{
		ServerSync.vault.vaultIO.sendVaultBalance(playerName, playerBalance);
		
		if (ServerSync.verbose)
		{
			Log.info(ServerSync.consolePrefix + "Balance update sent for " + playerName);
		}
	}
	
	/**
	 * Sync balances for all online players
	 */
	public synchronized void syncBalances()
	{
		//Only if there is at least one online player
		if (Bukkit.getOnlinePlayers().size() > 0)
		{
			//Determine which we should send to be sync
			Iterator it = playerBalances.entrySet().iterator();
			while (it.hasNext()) 
			{
				Map.Entry pair = (Map.Entry)it.next();
				
				//Create Offline Player
				UUID uuid = UUID.fromString((String)pair.getKey());
				OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
				
				//If their current balance doesn't equal what happened during the last sync
				if (ServerSync.vault.getVault().getBalance(player) != (double)pair.getValue())
				{
					updateNeeded.put(player.getUniqueId().toString(), player);
					
					if (ServerSync.verbose)
					{
						Log.info(ServerSync.consolePrefix + "Vault - Determined update needed for " + player.getName());
					}
				}
			}
			
			//Sync players that need updated
			Iterator iterator = updateNeeded.entrySet().iterator(); 
			while (iterator.hasNext())
			{
				Map.Entry pair = (Map.Entry)iterator.next();
				
				UUID uuid = UUID.fromString((String)pair.getKey());
				OfflinePlayer player = (OfflinePlayer)pair.getValue();
				
				//Sync
				outgoingSync(player.getName(), ServerSync.vault.getVault().getBalance(player));
				
				//Update internal balance
				playerBalances.put(player.getUniqueId().toString(), ServerSync.vault.getVault().getBalance(player));
				
				iterator.remove();
			}
		}
	}
	
}
