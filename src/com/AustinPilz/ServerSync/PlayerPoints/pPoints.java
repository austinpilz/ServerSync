package com.AustinPilz.ServerSync.PlayerPoints;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.black_ixx.playerpoints.PlayerPoints;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.libs.jline.internal.Log;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.AustinPilz.ServerSync.ServerSync;

public class pPoints 
{
	private boolean enabled;
	private PlayerPoints playerPoints;
	public static final String bungeePlayerPointsSubChannel = "PlayerPoints";
	
	//IO
	public pPointsIO playerPointsIO;
	
	//HashMaps
	private HashMap<String, Integer> playerBalance; //Storage of player point balances (UUID)
	private HashMap<String, Long> lastUpdated; //Storage of last player point balance update (UUID)
	private HashSet<String> updateNeeded; //Players in need of balance updates to be sent
	
	/**
	 * Constructor
	 */
	public pPoints()
	{
		enabled = hookPlayerPoints();
		playerPointsIO = new pPointsIO();
		playerBalance = new HashMap<String, Integer>();
		updateNeeded = new HashSet<String>();
		lastUpdated = new HashMap<String, Long>();
	}
	
	/**
	 * Validate that we have access to PlayerPoints
	 *
	 * @return True if we have PlayerPoints, else false.
	 */
	private boolean hookPlayerPoints() 
	{
		if (Bukkit.getServer().getPluginManager().getPlugin("PlayerPoints") == null) 
		{
			return false;
		}
		else
		{
			final Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("PlayerPoints");
		    playerPoints = PlayerPoints.class.cast(plugin);
		    return playerPoints != null; 
		}
	}
	
	/**
	 * Accessor for other parts of your plugin to retrieve PlayerPoints.
	 *
	 * @return PlayerPoints plugin instance
	 */
	public PlayerPoints getPlayerPoints() 
	{
	    return playerPoints;
	}

	/**
	 * Requests that all player points balances be sent to Bungeecord hub for relay
	 */
	protected synchronized void syncBalances()
	{
		@SuppressWarnings("rawtypes")
		Iterator iterator = updateNeeded.iterator(); 
		while (iterator.hasNext())
		{
			UUID playerUUID = UUID.fromString((String)iterator.next());
			
			outgoingUpdate(playerUUID.toString(), getPlayerPoints().getAPI().look(playerUUID)); //Send update message
		}
		
		updateNeeded.clear(); //Clear update needed queue
	}
	
	/**
	 * Updates ServerSync balances for tracking purposes
	 */
	protected synchronized void updateInternalBalances()
	{
		//PlayerPoints are not effected by time, so we can get away with syncing only online players
		
		for(Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) 
		{
			if (playerBalance.containsKey(onlinePlayer.getUniqueId().toString()))
			{
				//Compare balances
				if (playerBalance.get(onlinePlayer.getUniqueId().toString()) != getPlayerPoints().getAPI().look(onlinePlayer.getUniqueId()))
				{
					//Balances differ - update balance and last updated
					playerBalance.put(onlinePlayer.getUniqueId().toString(), getPlayerPoints().getAPI().look(onlinePlayer.getUniqueId()));
					lastUpdated.put(onlinePlayer.getUniqueId().toString(), new Long(System.currentTimeMillis() % 1000));
					updateNeeded.add(onlinePlayer.getUniqueId().toString()); //Add them to update needed
				
					if (ServerSync.verbose)
					{
						Log.info(ServerSync.consolePrefix + "PlayerPoints - Updated internal balance for " + onlinePlayer.getName());
					}
				}
			}
			else
			{
				//They're not in our storage
				
				//Add Balance
				playerBalance.put(onlinePlayer.getUniqueId().toString(), new Integer(getPlayerPoints().getAPI().look(onlinePlayer.getUniqueId())));
				
				if (ServerSync.verbose)
				{
					Log.info(ServerSync.consolePrefix + "PlayerPoints - Added internal balance for " + onlinePlayer.getName());
				}
			}
		}
		
		if (ServerSync.verbose)
		{
			//Log.info(ServerSync.consolePrefix + "PlayerPoints - Updated internal balances");
		}
	}

	/**
	 * Processes received update for player point balance
	 */
	protected synchronized void incomingUpdate(String receivedUUID, int balance)
	{
		//Update
		UUID uuid = UUID.fromString(receivedUUID);
		
		
		getPlayerPoints().getAPI().set(uuid, balance); //Update via API
		playerBalance.put(uuid.toString(), balance); //Update our tracking balance
		lastUpdated.put(uuid.toString(), System.currentTimeMillis() % 1000); //Update last updated
		
		if (ServerSync.verbose)
		{
			Log.info(ServerSync.consolePrefix + "PlayerPoints - Received update for player ("+uuid+") for ("+balance+") points");
		}
	}
	
	/**
	 * Sends outoing update for player point balance
	 */
	protected synchronized void outgoingUpdate(String UUID, int balance)
	{
		//Check to see if player has tracking data
		if (playerBalance.containsKey(UUID))
		{
			long stamp = System.currentTimeMillis() % 1000; //Tmp timestamp
				
			playerBalance.put(UUID, balance); //Update tracking balance
			lastUpdated.put(UUID, stamp); //Update out last updated
			playerPointsIO.sendBalanceUpdate(UUID, balance);
				
			if (ServerSync.verbose)
			{
				Log.info(ServerSync.consolePrefix + "PlayerPoints - sent update for player ("+UUID+") for ("+balance+") points");
			}
		}
	}
	
	/**
	 * Returns boolean if Player Points syndication is enabled
	 */
	public boolean isEnabled()
	{
		return enabled;
	}
}
