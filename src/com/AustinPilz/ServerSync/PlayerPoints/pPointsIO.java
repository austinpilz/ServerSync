package com.AustinPilz.ServerSync.PlayerPoints;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.libs.jline.internal.Log;
import org.bukkit.entity.Player;

import com.AustinPilz.ServerSync.ServerSync;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class pPointsIO 
{
	public void incoming(String channel, Player player, byte[] message) 
	{
		 //It's on the right channel - let's see if it's on our subchannel
	    ByteArrayDataInput in = ByteStreams.newDataInput(message);
	    String subchannel = in.readUTF();
	    
	    //Vault Communication
	    if (subchannel.equalsIgnoreCase(ServerSync.playerPoints.bungeePlayerPointsSubChannel)) 
	    {
	    	//Only read into message if this server has vault enabled
	    	if (ServerSync.playerPoints.isEnabled())
	    	{
	    		String operation = in.readUTF();
	    		
	    		if (operation.equals("BalanceRequest")) //Hub is requesting we send all of the online players balance information
	    		{
	    			ServerSync.playerPoints.syncBalances();
	    			
	    			if (ServerSync.verbose)
	    			{
	    				Log.info(ServerSync.consolePrefix + "Player Points - Balance update request received from hub");
	    			}
	    			
	    		}
	    		else if (operation.equals("Balance")) //Incoming Balance
	    		{
	    			//Receiving balance relay from the hub
	    			String UUID = in.readUTF(); //UUID
			    	int balance;
			    	
			    	if (ServerSync.encryption.isEnabled())
			    	{
			    		balance = Integer.parseInt(ServerSync.encryption.decrypt(in.readUTF()));
			    	}
			    	else
			    	{
			    		balance = in.readInt();
			    	}
			    	
			    	if (!UUID.isEmpty() && balance >= 0)
			    	{
			    		//There is some data there, relay
			    		ServerSync.playerPoints.incomingUpdate(UUID, balance);
			    		
			    		if (ServerSync.verbose)
		    			{
			    			Log.info(ServerSync.consolePrefix + "Player Points - Balance update received for player " + ChatColor.YELLOW + UUID + ChatColor.WHITE + " for $" + ChatColor.GREEN + balance);
		    			}
			    	}
	    		}
	    		else if (operation.equals("VersionMismatch")) //Our client version does not match hub version
	    		{
	    			String hubVersion = in.readUTF();
	    			Log.error(ServerSync.consolePrefix + "ServerSync version mismatch! Client version ("+ServerSync.pluginVersion+") does not match hub version ("+hubVersion+")");
	    		}
	    	}
	    }
	}
	
	/**
	 * Sends balance update for supplied player UUID
	 */
	protected void sendBalanceUpdate(String UUID, int balance)
	{
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF(ServerSync.playerPoints.bungeePlayerPointsSubChannel); //Bungee vault sub-channel
		out.writeUTF("Balance"); //Operation
		out.writeUTF(ServerSync.bungeeServerName); //Server Name
		out.writeUTF(ServerSync.pluginVersion); //Client Version
		out.writeUTF(UUID); //Player UUID
		
		if (ServerSync.encryption.isEnabled())
		{
			out.writeUTF(ServerSync.encryption.encrypt(Integer.toString(balance)));
		}
		else
		{
			//Send unencrypted
			out.writeUTF(Integer.toString(balance)); //Player balance
		}
		
		ServerSync.bungeeOutgoing.sendMessage(out, ServerSync.bungeePluginChannel); //Send it!

		if (ServerSync.verbose)
		{
			Log.info(ServerSync.chatPrefix + "Player points - Balance update sent for " + ChatColor.YELLOW + UUID);
		}
	}

}
