package com.AustinPilz.ServerSync.Bungee;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.libs.jline.internal.Log;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.AustinPilz.ServerSync.ServerSync;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

public class BungeeIncoming implements PluginMessageListener 
{
	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] message) 
	{
		//Check to see if it's on the right channel first
	    if (!channel.equals(ServerSync.bungeeChannel) && !channel.equals(ServerSync.bungeePluginChannel)) 
	    {
	    	//It's not - ignore it
	    	return;
	    }
	    
	    if (channel.equals(ServerSync.bungeeChannel))
	    {
	    	ByteArrayDataInput in = ByteStreams.newDataInput(message);
		    String subchannel = in.readUTF();
		    
		    if (subchannel.equalsIgnoreCase("GetServer")) //Bungee is telling us our server name
		    {
		    	ServerSync.bungeeServerName = in.readUTF();

		    	Log.info(ServerSync.consolePrefix + "Bungee associated as " + ServerSync.bungeeServerName);

    			if (ServerSync.verbose)
    			{
    				Log.info(ServerSync.consolePrefix + "Bungee associated as " + ChatColor.BLUE + ServerSync.bungeeServerName);
    			}
		    }
		    
	    }
	    else if (channel.equals(ServerSync.bungeePluginChannel))
	    {
		    //Determine what plugin based on the sub-channel
	    	ByteArrayDataInput in = ByteStreams.newDataInput(message);
	 	    String subchannel = in.readUTF();
	 	    
		    if (subchannel.equalsIgnoreCase(ServerSync.vault.vaultSubchannel)) 
		    {
		    	//Reroute to Vault
		    	ServerSync.vault.vaultIO.incoming(channel, player, message);
		    }
		    else if (subchannel.equalsIgnoreCase(ServerSync.playerPoints.bungeePlayerPointsSubChannel)) 
		    {
		    	//Reroute to PlayerPoints
		    	ServerSync.playerPoints.playerPointsIO.incoming(subchannel, player, message);
		    }
		    else if (subchannel.equalsIgnoreCase(ServerSync.messageRelay.messageSubchannel))
		    {
		    	//Reroute to Message Relay
		    	ServerSync.messageRelay.incoming(player, message);
		    }
		    else
		    {
		    	//Unknown subchannel
		    	Log.info(ServerSync.consolePrefix + "Unknown message received on ServerSync bungee channel on unknown subchannel ("+subchannel+")");
		    }
	    }
	}
}
