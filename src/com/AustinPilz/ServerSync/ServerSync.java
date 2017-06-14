package com.AustinPilz.ServerSync;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.AustinPilz.ServerSync.IO.SpigotUpdateChecker;
import com.AustinPilz.ServerSync.Listener.PlayerListener;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.AustinPilz.ServerSync.Bungee.BungeeAssociation;
import com.AustinPilz.ServerSync.Bungee.BungeeIncoming;
import com.AustinPilz.ServerSync.Bungee.BungeeOutgoing;
import com.AustinPilz.ServerSync.Bungee.QueueProcess;
import com.AustinPilz.ServerSync.Command.ServerSyncCommand;
import com.AustinPilz.ServerSync.IO.InputOutput;
import com.AustinPilz.ServerSync.IO.MetricsLite;
import com.AustinPilz.ServerSync.IO.ServerSyncEncryption;
import com.AustinPilz.ServerSync.MessageRelay.MessageRelay;
import com.AustinPilz.ServerSync.PlayerPoints.pPoints;
import com.AustinPilz.ServerSync.PlayerPoints.pPointsUpdate;
import com.AustinPilz.ServerSync.Vault.Vault;
import com.AustinPilz.ServerSync.Vault.VaultListener;
import com.AustinPilz.ServerSync.Vault.VaultUpdate;




public class ServerSync extends JavaPlugin implements Listener 
{
	//Plugin Strings
	public static final String pluginVersion = "1.6.3";
	public static final String pluginName = "Server Sync";
	public static final String pluginIdentifier = pluginName + " v" + pluginVersion;
	public static final String consolePrefix = "[ServerSync] ";
	public static final String chatPrefix = ChatColor.GREEN + "[ServerSync] " + ChatColor.WHITE;
	
	//Bungee Communication Settings
	public static final String bungeeChannel = "BungeeCord";
	public static final String bungeePluginChannel = "ServerSync";
	public static final String bungeeCheckinSubChannel = "Checkin";
	public static BungeeOutgoing bungeeOutgoing;
	public static String bungeeServerName = "";
	
	//Schedules
	public static BukkitTask bungeeAssociation; 
	
	//Other Plugins
	public static Vault vault;
	public static pPoints playerPoints;
	public static MessageRelay messageRelay;
	
	//Instance
	public static ServerSync instance;
	public static ServerSyncEncryption encryption;
	public static InputOutput IO;
	public static SpigotUpdateChecker updateChecker;
	
	//Etc
	public static boolean verbose;
	
	
	public static final Logger log = Logger.getLogger("Minecraft");
	
	@Override
	public void onLoad() 
	{
		//
	}
	
	@Override
	public void onEnable()
	{
		//Instance
		instance = this;
		verbose = false;
		
		//Plugin bootup
		long startMili = System.currentTimeMillis() % 1000;
		
		//Encryption
		encryption = new ServerSyncEncryption();
		
		//IO
		IO = new InputOutput();
		IO.prepareDB();
		IO.encryptionStartup();
		
		//Setup other plugin hooks
		vault = new Vault();
		playerPoints = new pPoints();
		messageRelay = new MessageRelay();
		
		//Commands
		getCommand("serversync").setExecutor(new ServerSyncCommand());
		
		//Register Bungee Communicatios
		bungeeOutgoing = new BungeeOutgoing();
		this.getServer().getMessenger().registerOutgoingPluginChannel(this, bungeeChannel);
		this.getServer().getMessenger().registerIncomingPluginChannel(this, bungeeChannel, new BungeeIncoming());
		this.getServer().getMessenger().registerOutgoingPluginChannel(this, bungeePluginChannel);
		this.getServer().getMessenger().registerIncomingPluginChannel(this, bungeePluginChannel, new BungeeIncoming());
		
		//Register Runnables
		bungeeAssociation = Bukkit.getScheduler().runTaskTimer(this, new BungeeAssociation(), 20, 20); //Server association
		BukkitTask vaultUpdate = Bukkit.getScheduler().runTaskTimer(this, new VaultUpdate(), 20, 20); //Vault Balance Update
		BukkitTask playerPointsUpdate = Bukkit.getScheduler().runTaskTimer(this, new pPointsUpdate(), 40, 40); //Player Points Balance Update
		BukkitTask bungeeQueue = Bukkit.getScheduler().runTaskTimer(this, new QueueProcess(), 20, 20);
		
		//Register Listeners
		getServer().getPluginManager().registerEvents(new VaultListener(), this); //Vault
		getServer().getPluginManager().registerEvents(new PlayerListener(), this);

		//Update Checker
		this.updateChecker = new SpigotUpdateChecker();

		//Check for Update
		this.updateChecker = new SpigotUpdateChecker();
		try {
			this.updateChecker.checkUpdate(this.pluginVersion);
			log.log(Level.INFO, this.consolePrefix + "Checked for update! Running v" + this.pluginVersion + " and most recent is v" + this.updateChecker.getLatestVersion());
		} catch (Exception e) {
			log.log(Level.INFO, this.consolePrefix + "Update checked failed :(");
			e.printStackTrace();
		}
		
		try 
		{
	        MetricsLite metrics = new MetricsLite(this);
	        metrics.start();
	        log.log(Level.INFO, consolePrefix + "Metrics submitted!");
	    } 
		catch (IOException e) 
		{
	       log.log(Level.WARNING, consolePrefix + "Error while submitting metrics!");
	    }
		
		//Startup Successful
		log.log(Level.INFO, consolePrefix + "Startup took " + (System.currentTimeMillis() % 1000 - startMili) + " ms");
	}
	
	@Override
	public void onDisable()
	{
		log.log(Level.INFO, consolePrefix + pluginIdentifier + " disabled successfully");
	}

}
