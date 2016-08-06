package com.AustinPilz.ServerSync.Vault;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.libs.jline.internal.Log;
import org.bukkit.entity.Player;

import com.AustinPilz.ServerSync.ServerSync;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class VaultIO 
{
	/*
	 * Incoming message
	 */
	public void incoming(String channel, Player player, byte[] message) 
	{
		 //It's on the right channel - let's see if it's on our subchannel
	    ByteArrayDataInput in = ByteStreams.newDataInput(message);
	    String subchannel = in.readUTF();
	    
	    //Vault Communication
	    if (subchannel.equalsIgnoreCase(ServerSync.vault.vaultSubchannel)) 
	    {
	    	//Only read into message if this server has vault enabled
	    	if (ServerSync.vault.isEnabled())
	    	{
	    		String vaultComponent = in.readUTF(); //Vault component (Econ / Permission)
	    		String operation = in.readUTF(); //Sync operation
	    		
	    		if (vaultComponent.equals(ServerSync.vault.economySubchannel))
	    		{	
		    		if (operation.equals("BalanceRequest")) //Hub is requesting we send all of the online players balance information
		    		{
		    			ServerSync.vault.syncBalances();
		    			
		    			if (ServerSync.verbose)
		    			{
		    				Log.info(ServerSync.consolePrefix + "Vault - Balance update request received from hub");
		    			}
		    			
		    		}
		    		else if (operation.equals("Balance")) //Incoming Balance
		    		{
		    			//Receiving balance relay from the hub
		    			String playerName = in.readUTF();
		    			String stringBalance = in.readUTF();
		    			double playerBalance = -1;
		    			
		    			if (ServerSync.encryption.isEnabled())
		    			{
		    				playerBalance = Double.parseDouble(ServerSync.encryption.decrypt(stringBalance));
		    			}
		    			else
		    			{
		    				try
		    				{
		    					playerBalance = Double.parseDouble(stringBalance);
		    				}
		    				catch (Exception ex)
		    				{
		    					Log.info(ServerSync.consolePrefix + "Error while parsing Vault player balance from double -> ("+ stringBalance + ")");
		    				}
		    			}
				    	
				    	if (!playerName.isEmpty() && playerBalance >= 0)
				    	{
				    		//There is some data there, so relay to vault class
				    		ServerSync.vault.incomingEconomySync(playerName, playerBalance);	
				    		
				    		if (ServerSync.verbose)
			    			{
			    				Log.info(ServerSync.consolePrefix + "Vault - Balance update received for player " + ChatColor.YELLOW + playerName + ChatColor.WHITE + " for $" + ChatColor.GREEN + playerBalance);
			    			}
				    	}
				    	else
				    	{
				    		Log.error(ServerSync.consolePrefix + "Vault - Balance update received with empty player or amount");
				    	}
		    		}
		    		else if (operation.equals("VersionMismatch")) //Our client version does not match hub version
		    		{
		    			String hubVersion = in.readUTF();
		    			Log.error(ServerSync.consolePrefix + "Vault - Version Mismatch! Server Version " + hubVersion + " & client version " + ServerSync.pluginVersion);
		    		}
		    		else
		    		{
		    			Log.error(ServerSync.consolePrefix + "Vault - Unknown economy operation ("+operation+")");
			    	}
	    		}
	    		else if (vaultComponent.equals(ServerSync.vault.permissionSubchannel))
	    		{
	    			if (operation.equals("GroupsRequest")) 
		    		{
	    				//Hub requesting that we send all online player groups for sync
	    				ServerSync.vault.serversyncVaultPermission.syncPlayerGroups();
	    				
	    				if (ServerSync.verbose)
		    			{
		    				Log.info(ServerSync.consolePrefix + "Vault - Player group sync requested");
		    			}
		    		}
	    			else if (operation.equals("PlayerGroups"))
	    			{
	    				//Receiving player group information from sync
	    				String uuid = in.readUTF();
	    				String world = in.readUTF();
	    				String groups = in.readUTF();
	    				
	    				ServerSync.vault.serversyncVaultPermission.incomingPlayerSync(uuid, world, groups);
	    				
	    				if (ServerSync.verbose)
		    			{
		    				Log.info(ServerSync.consolePrefix + "Vault - Permission - Incoming group sync information received for " + Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName());
		    			}
	    			}
	    			else if (operation.equals("VersionMismatch")) //Our client version does not match hub version
		    		{
	    				String hubVersion = in.readUTF();
		    			Log.error(ServerSync.consolePrefix + "Vault - Version Mismatch! Server Version " + hubVersion + " & client version " + ServerSync.pluginVersion);
		    		}
	    			else
	    			{
	    				Log.error(ServerSync.consolePrefix + "Vault - Unknown permission operation ("+operation+")");
	    			}
	    		}
	    	}
	    }
	}

	public void sendVaultBalance(String playerName, double playerBalance)
	{
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF(ServerSync.vault.vaultSubchannel); //Vault Subchannel
		out.writeUTF(ServerSync.vault.economySubchannel); //Vault Economy Subchannel
		out.writeUTF("Balance"); //Operation
		out.writeUTF(ServerSync.bungeeServerName); //Server Name
		out.writeUTF(ServerSync.pluginVersion); //Client Version
		out.writeUTF(playerName); //Player name
		
		if (ServerSync.encryption.isEnabled())
		{
			out.writeUTF(ServerSync.encryption.encrypt(String.valueOf(playerBalance)));
		}
		else
		{
			out.writeUTF(Double.toString(playerBalance)+""); //Player balance
		}
		
		ServerSync.bungeeOutgoing.sendMessage(out, ServerSync.bungeePluginChannel);

		if (ServerSync.verbose)
		{
			Log.info(ServerSync.consolePrefix + "Vault - Balance update sent for " + ChatColor.YELLOW + playerName + " for $" + playerBalance);
		}
	}
	
	protected void sendPlayerGroups(OfflinePlayer player, String world, String groups)
	{
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF(ServerSync.vault.vaultSubchannel);
		out.writeUTF(ServerSync.vault.permissionSubchannel); //Vault Permission sub channel
		out.writeUTF("PlayerGroups"); //Operation
		out.writeUTF(ServerSync.bungeeServerName); //Server Name
		out.writeUTF(ServerSync.pluginVersion); //Client Version
		out.writeUTF(player.getUniqueId().toString()); //Player UUID
		out.writeUTF(world); //World
		out.writeUTF(groups); //Groups
		
		ServerSync.bungeeOutgoing.sendMessage(out, ServerSync.bungeePluginChannel);

		if (ServerSync.verbose)
		{
			Log.info(ServerSync.consolePrefix + "Vault - Permission group update sent for " + ChatColor.YELLOW + player.getName());
		}
	}
	
}
