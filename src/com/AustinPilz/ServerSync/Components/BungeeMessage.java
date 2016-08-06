package com.AustinPilz.ServerSync.Components;

import com.google.common.io.ByteArrayDataOutput;

public class BungeeMessage 
{
	private ByteArrayDataOutput out;
	private String channel;
	
	public BungeeMessage (ByteArrayDataOutput o, String c)
	{
		out = o;
		channel = c;
	}
	
	public ByteArrayDataOutput getData()
	{
		return out;
	}
	
	public String getChannel()
	{
		return channel;
	}

}
