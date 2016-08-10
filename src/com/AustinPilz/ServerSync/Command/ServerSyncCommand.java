package com.AustinPilz.ServerSync.Command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.AustinPilz.ServerSync.ServerSync;

public class ServerSyncCommand implements CommandExecutor
{
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		//Verify permissions
		if (sender.hasPermission("ServerSync.Admin"))
		{
			if (args.length < 1) 
			{
				//Just the base command, show information about the plugin
				sender.sendMessage(ServerSync.chatPrefix + ServerSync.pluginIdentifier);
			}
			else if (args.length >= 1)
			{
				if (args[0].equalsIgnoreCase("update"))
				{
					if (ServerSync.updateChecker.isUpdateNeeded())
					{
						sender.sendMessage(ServerSync.chatPrefix + ChatColor.WHITE + "You are running " + ServerSync.pluginIdentifier + " and the latest version available is v" + ServerSync.updateChecker.getLatestVersion() + ChatColor.RED + " Update recommended" + ChatColor.WHITE + "!! Please visit " + ChatColor.YELLOW + "http://serversyncbungeecord.austinpilz.com");
					}
					else
					{
						sender.sendMessage(ServerSync.chatPrefix + ChatColor.WHITE + ServerSync.pluginIdentifier + " is " + ChatColor.GREEN + "up to date" + ChatColor.WHITE + "!");
					}
				}
				else if (args[0].equalsIgnoreCase("Plugins"))
				{
					if (args.length == 1)
					{
						//Show supported plugins that are connected
						sender.sendMessage(ServerSync.chatPrefix + ChatColor.YELLOW + "----- " + ChatColor.WHITE + "Supported Plugins" + ChatColor.YELLOW + " -----");
						
						//Vault
						if (ServerSync.vault.isEnabled())
						{
							//Vault is enabled
							sender.sendMessage(ServerSync.chatPrefix + "Vault - " + ChatColor.GREEN + "ENABLED");
						}
						else
						{
							//Vault is disabled
							sender.sendMessage(ServerSync.chatPrefix + "Vault - " + ChatColor.RED + "Disabled");
						}
						
						//Player Points
						
						if (ServerSync.playerPoints.isEnabled())
						{
							//Vault is enabled
							sender.sendMessage(ServerSync.chatPrefix + "PlayerPoints - " + ChatColor.GREEN + "ENABLED");
						}
						else
						{
							//Vault is disabled
							sender.sendMessage(ServerSync.chatPrefix + "PlayerPoints - " + ChatColor.RED + "Disabled");
						}
						
					}
				}
				else if (args[0].equalsIgnoreCase("Bungee"))
				{
					if (!ServerSync.bungeeServerName.isEmpty())
					{
						//Bungee Associated
						sender.sendMessage(ServerSync.chatPrefix + "Bungee Status - " + ChatColor.GREEN + "CONNECTED" + ChatColor.WHITE + " as Bungee server name " + ChatColor.BLUE + ServerSync.bungeeServerName);
					}
					else
					{
						//Bungee not associated
						sender.sendMessage(ServerSync.chatPrefix + "Bungee Status - " + ChatColor.RED + "NOT CONNECTED");
					}
				}
				else if (args[0].equalsIgnoreCase("CommandRelay") || args[0].equalsIgnoreCase("cr"))
				{
					if (args.length > 1)
					{
						String command = "";
						
						for (int i = 1; i < args.length; i++)
						{
							command += args[i];
							command += " ";
						}
						
						//Send it
						ServerSync.messageRelay.sendCommandRelay((Player)sender, command);
						
					}
					else
					{
						sender.sendMessage(ServerSync.chatPrefix + ChatColor.RED + "Syntax error! You have to include the command you'd like to relay");
					}
				}
				else if (args[0].equalsIgnoreCase("Settings"))
				{
					if (args.length > 1)
					{
						if (args[1].equalsIgnoreCase("Verbose"))
						{
							if (ServerSync.verbose)
							{
								//Verbose Now Disabled
								ServerSync.verbose = false;
								sender.sendMessage(ServerSync.chatPrefix + "Verbose Logging now " + ChatColor.RED + "Disabled");
								
							}
							else
							{
								//Verbose Now Enabled
								ServerSync.verbose = true;
								sender.sendMessage(ServerSync.chatPrefix + "Verbose Logging now " + ChatColor.RED + "Enabled");
							}
						}
						else if (args[1].equalsIgnoreCase("Encryption"))
						{
							if (args.length == 2)
							{
								//Just encryption information
								if (ServerSync.encryption.isEnabled())
								{
									sender.sendMessage(ServerSync.chatPrefix + " ServerSync Encryption: " + ChatColor.GREEN + "Enabled");
								}
								else
								{
									sender.sendMessage(ServerSync.chatPrefix + " ServerSync Encryption: " + ChatColor.RED + "Disabled");
								}
							}
							else if (args.length == 3)
							{
								if (args[2].equalsIgnoreCase("enable"))
								{
									if (ServerSync.encryption.isEnabled())
									{
										//Already enabled
										sender.sendMessage(ServerSync.chatPrefix + ChatColor.RED + "Transmission encryption is already enabled");
									}
									else
									{
										if (ServerSync.encryption.keyValid())
										{
											ServerSync.encryption.setEnabled(true);
											sender.sendMessage(ServerSync.chatPrefix + "Transmission encryption has been " + ChatColor.GREEN + "enabled");
										}
										else
										{
											sender.sendMessage(ServerSync.chatPrefix + ChatColor.RED + "Transmission cannot be enabled as you have not yet set an encryption key");
										}
									}
								}
								else if (args[2].equalsIgnoreCase("disable"))
								{
									if (!ServerSync.encryption.isEnabled())
									{
										//Already enabled
										sender.sendMessage(ServerSync.chatPrefix + ChatColor.RED + "Transmission encryption is already disabled");
									}
									else
									{
										ServerSync.encryption.setEnabled(false);
										sender.sendMessage(ServerSync.chatPrefix + "Transmission encryption has been " + ChatColor.GREEN + "disabled");
									}
								}
								else
								{
									sender.sendMessage(ServerSync.chatPrefix + ChatColor.RED + "Unknown encryption setting! You can only enable or disable encryption");
								}
							}
							else if (args.length == 4)
							{
								//settings encryption key [myKey]
								if (args[2].equalsIgnoreCase("key"))
								{
									if (args[3].length() == 16)
									{
										ServerSync.encryption.setEncryptionKey(args[3]);
										sender.sendMessage(ServerSync.chatPrefix + "Transmission encryption key set " + ChatColor.GREEN + "successfully!");
									}
									else
									{
										sender.sendMessage(ServerSync.chatPrefix + ChatColor.RED + "Error! Encryption key must be 16 characters long");
									}
								}
								else
								{
									//Unknown
									sender.sendMessage(ServerSync.chatPrefix + ChatColor.RED + "Unknown encryption setting!");
								}
								
							}
							else
							{
								sender.sendMessage(ServerSync.chatPrefix + ChatColor.RED + "Unknown encryption command!");
							}
						}
						else
						{
							sender.sendMessage(ServerSync.chatPrefix + ChatColor.RED + "Unknown settings command!");
						}
					}
					else
					{
						sender.sendMessage(ServerSync.chatPrefix + ChatColor.RED + "Unknown settings command!");
					}
				}
				else
				{
					sender.sendMessage(ServerSync.chatPrefix + ChatColor.RED + "Unknown settings command!");
				}
			}
			else
			{
				sender.sendMessage(ServerSync.chatPrefix + ChatColor.RED + "Unknown command!");
			}
		}
		else
		{
			sender.sendMessage(ServerSync.chatPrefix + ChatColor.RED + " You don't have permission to access ServerSync");
		}
		
		
		
		return true;
	}

}
