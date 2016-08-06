package com.AustinPilz.ServerSync.MessageRelay;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.libs.jline.internal.Log;
import org.bukkit.entity.Player;

import com.AustinPilz.ServerSync.ServerSync;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class MessageRelay 
{
	//Class for relaying commands across servers
	public static final String messageSubchannel = "MessageRelay";
	
	/** 
	 * Incoming message relay
	 * @param player
	 * @param message
	 */
	public void incoming(OfflinePlayer player, byte[] message)
	{
		//Subchannel -> Operation -> Player UUID -> Command
		ByteArrayDataInput in = ByteStreams.newDataInput(message);
		String subchannel = in.readUTF();
	    
		//Vault Communication
		if (subchannel.equalsIgnoreCase(ServerSync.messageRelay.messageSubchannel)) 
		{
			String operation = in.readUTF(); //OPERATION
			if (operation.equals("CommandRelay")) //Hub is requesting we send all of the online players balance information
    		{
				OfflinePlayer commandPlayer = Bukkit.getOfflinePlayer(UUID.fromString(in.readUTF()));
				String command = in.readUTF();
				
				//De-crypt if necessary 
				if (ServerSync.encryption.isEnabled())
				{
					command = ServerSync.encryption.decrypt(command);
				}
				
				//Execute command
				Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), command);
				
				//Notify players
				for(Player p : Bukkit.getServer().getOnlinePlayers())
				{
					if (p.hasPermission("ServerSync.*") || p.hasPermission("ServerSync.admin"))
					{
						p.sendMessage(ServerSync.chatPrefix + "Executed command relay " + ChatColor.GREEN + command + ChatColor.WHITE + " from player " + ChatColor.AQUA + commandPlayer.getName());
					}
						
				}
				
				//Log it always
				ServerSync.log.info(ServerSync.consolePrefix + "Executed command from remote server send by " + commandPlayer.getName() + " - " + command);
    		}
			else
			{
				//Unknown operation
				if (ServerSync.verbose)
				{
					ServerSync.log.warning(ServerSync.consolePrefix + "Unknwn command in MessageRelay subchannel ("+operation+")");
				}
			}
		}
		else
		{
			//Unknown sub channel
			if (ServerSync.verbose)
			{
				ServerSync.log.warning(ServerSync.consolePrefix + "Unknown subchannel got routed to MessageRelay class");
			}
		}
	}

	public void sendCommandRelay(Player player, String command)
	{
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF(ServerSync.messageRelay.messageSubchannel);
		out.writeUTF("CommandRelay"); //Operation
		out.writeUTF(ServerSync.bungeeServerName); //Server Name
		out.writeUTF(ServerSync.pluginVersion); //Client Version
		out.writeUTF(player.getUniqueId().toString()); //Player UUID
		
		if (ServerSync.encryption.isEnabled())
		{
			out.writeUTF(ServerSync.encryption.encrypt(command)); //Command
		}
		else
		{
			out.writeUTF(command); //Command
		}
		
		ServerSync.bungeeOutgoing.sendMessage(out, ServerSync.bungeePluginChannel);

		player.sendMessage(ServerSync.chatPrefix + "Command relay sent " + ChatColor.GREEN + "successfully!");
		
		if (ServerSync.verbose)
		{
			Log.info(ServerSync.consolePrefix + "MessageRelay - Command relay from " + player.getName() + ChatColor.RED + command);
		}
	}
}
