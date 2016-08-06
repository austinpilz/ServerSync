package com.AustinPilz.ServerSync.IO;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.bukkit.craftbukkit.libs.jline.internal.Log;

import com.AustinPilz.ServerSync.ServerSync;

public class ServerSyncEncryption 
{
	private String encryptionKey;
	private boolean enabled;
	
	public ServerSyncEncryption()
	{
		encryptionKey = "";
		enabled = false;
	}
	
	/**
	 * Returns if transmission encryption is enabled
	 * @return
	 */
	public boolean isEnabled()
	{
		return enabled;
	}
	
	/**
	 * Updates the encryption key
	 * @param key
	 */
	public void setEncryptionKey(String key)
	{
		encryptionKey = key;
		ServerSync.IO.updateEncryptionSettings(enabled, encryptionKey);
	}
	
	/**
	 * Returns if key is stored
	 */
	public boolean keyValid()
	{
		if (encryptionKey.isEmpty())
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	/**
	 * Updates the encryption setting
	 * @param en
	 */
	public void setEnabled(boolean en)
	{
		enabled = en;
		ServerSync.IO.updateEncryptionSettings(enabled, encryptionKey);
	}

	/**
	 * Encrypts passed string
	 * @param toEncrypt
	 * @return
	 */
	public String encrypt(String toEncrypt)
	{
		String result = "";
		
		try 
		{
	         // Create key and cipher
	         Key aesKey = new SecretKeySpec(encryptionKey.getBytes(), "AES");
	         Cipher cipher = Cipher.getInstance("AES");

	         // encrypt the text
	         cipher.init(Cipher.ENCRYPT_MODE, aesKey);
	         byte[] encrypted = cipher.doFinal(toEncrypt.getBytes());
	         
	         StringBuilder sb = new StringBuilder();
	         for (byte b: encrypted) 
	         {
	        	 sb.append((char)b);
	         }
	            
	         result = sb.toString();
	         
	    }
		catch(Exception e) 
		{
			Log.error(ServerSync.consolePrefix + "Error while attempting to encrypt string");
	    }
		
		return result;
	}
	
	/**
	 * Decrypts passed string
	 * @param toDecrypt
	 * @return
	 */
	public String decrypt(String toDecrypt)
	{
		String result = "";
		
		try 
		{
	        // Create key and cipher
	        Key aesKey = new SecretKeySpec(encryptionKey.getBytes(), "AES");
	        Cipher cipher = Cipher.getInstance("AES");
	        
	        byte[] bb = new byte[toDecrypt.length()];
            for (int i=0; i<toDecrypt.length(); i++) {
                bb[i] = (byte) toDecrypt.charAt(i);
            }
	        
	        
	        // decrypt the text
	        cipher.init(Cipher.DECRYPT_MODE, aesKey);
	        result = new String(cipher.doFinal(bb));
      
	    }
		catch(Exception e) 
		{
			Log.error(ServerSync.consolePrefix + "Error while attempting to decrypt string" + encryptionKey);
	    }
		
		return result;
	}

}
