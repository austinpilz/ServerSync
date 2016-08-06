package com.AustinPilz.ServerSync.Bungee;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.libs.jline.internal.Log;
import org.bukkit.entity.Player;

import com.AustinPilz.ServerSync.ServerSync;
import com.AustinPilz.ServerSync.Components.BungeeMessage;
import com.AustinPilz.ServerSync.Components.Queue;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class BungeeOutgoing 
{	
	private Queue<BungeeMessage> queue;
	
	public BungeeOutgoing()
	{
		queue = new Queue<BungeeMessage>();
	}
	
	/**
	 * Processes the outgoing message queue
	 */
	protected synchronized void processQueue()
	{
		if (queue.size() > 0 && Bukkit.getOnlinePlayers().size() > 0)
		{
			Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
			
			BungeeMessage tmp = queue.dequeue();
			
			player.sendPluginMessage(ServerSync.instance, tmp.getChannel(), tmp.getData().toByteArray());
		}
	}
	
	
	/**
	 * Sends outgoing Bungee message
	 */
	public void sendMessage(ByteArrayDataOutput out, String channel)
	{
		//Add to queue
		queue.enqueue(new BungeeMessage(out, channel));
	}
	
	/**
	 * Send request over Bungeecord channel to get server name
	 */
	public void sendServerNameRequest()
	{
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("GetServer");
		
		ServerSync.bungeeOutgoing.sendMessage(out, ServerSync.bungeeChannel);
		
		if (ServerSync.verbose)
		{
			Log.info(ServerSync.consolePrefix + "Sent server name request");
		}
	}
	
	public void sendServerCheckin()
	{
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF(ServerSync.bungeeCheckinSubChannel); //Bungee checkin sub-channel
		out.writeUTF("Checkin"); //Operation
		out.writeUTF(ServerSync.bungeeServerName); //Server Name
		out.writeUTF(ServerSync.pluginVersion); //Client Version
		
		ServerSync.bungeeOutgoing.sendMessage(out, ServerSync.bungeePluginChannel);
		
		if (ServerSync.verbose)
		{
			Log.info(ServerSync.consolePrefix + "Sent server check-in");
		}
	}

}
