package com.AustinPilz.ServerSync.IO;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import com.AustinPilz.ServerSync.ServerSync;


public class InputOutput 
{
    private static Connection connection;
    
	public InputOutput()
	{
		if (!ServerSync.instance.getDataFolder().exists()) 
		{
			try 
			{
				(ServerSync.instance.getDataFolder()).mkdir();
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
	}
   
    public static synchronized Connection getConnection() {
    	if (connection == null) connection = createConnection();
            try
            {
                if(connection.isClosed()) connection = createConnection();
            } 
            catch (SQLException ex) 
            {
                ex.printStackTrace();
            }
        
    	return connection;
    }
    
    private static Connection createConnection() 
    {
    	try
    	{
                Class.forName("org.sqlite.JDBC");
                Connection ret = DriverManager.getConnection("jdbc:sqlite:" +  new File(ServerSync.instance.getDataFolder().getPath(), "ServerSync.sqlite").getPath());
                ret.setAutoCommit(false);
                ServerSync.log.info(ServerSync.consolePrefix + "Using SQLite - Connection succeeded");
                return ret;
        } 
        catch (ClassNotFoundException e) 
        {
        	 ServerSync.log.severe(ServerSync.consolePrefix + "Connection to the MySQL database failed. Plugin startup terminated");
        	 e.printStackTrace();
        	 return null;
        } 
        catch (SQLException e) 
        {
        	 ServerSync.log.severe(ServerSync.consolePrefix + "Connection to the MySQL database failed. Plugin startup terminated");
        	e.printStackTrace();
        	return null;
        }
    }
    
    public static synchronized void freeConnection() 
    {
		Connection conn = getConnection();
        if(conn != null) {
            try 
            {
            	conn.close();
            	conn = null;
            } 
            catch (SQLException e) 
            {
                e.printStackTrace();
            }
        }
    }
    
    public void prepareDB()
    {
    	Connection conn = getConnection();
        Statement st = null;
        try 
        {
        		st = conn.createStatement();
            	st.executeUpdate("CREATE TABLE IF NOT EXISTS \"ServerSync_Encryption\" (\"ID\" VARCHAR PRIMARY KEY  NOT NULL , \"Key\" VARCHAR, \"Enabled\" VARCHAR)");
                conn.commit();
                st.close();

        } 
        catch (SQLException e) 
        {
        	 ServerSync.log.warning(ServerSync.consolePrefix + "Error while preparing database tables! - " + e.getMessage());
            e.printStackTrace();
        }
        catch (Exception e) 
		{
        	e.printStackTrace();
		}
    }
    
    public void updateDB()
    {
    	//Update("SELECT Arena  FROM sandfall_signs", "ALTER TABLE sandfall_signs ADD Arena VARCHAR;", "ALTER TABLE sandfall_signs ADD Arena varchar(250);" );
    }
    
    public void Update(String check, String sql)
    {
    	Update(check, sql, sql);
    }
    
    public void Update(String check, String sqlite, String mysql)
    {
    	try
    	{
    		Statement statement = getConnection().createStatement();
			statement.executeQuery(check);
			statement.close();
    	}
    	catch(SQLException ex)
    	{
    		 ServerSync.log.warning(ServerSync.consolePrefix + "Updating database...");
    		try 
    		{
    			String[] query;
    			
    			query = sqlite.split(";");
            	Connection conn = getConnection();
    			Statement st = conn.createStatement();
    			for (String q : query)	
    				st.executeUpdate(q);
    			conn.commit();
    			st.close();
    		} 
    		catch (SQLException e)
    		{
    			 ServerSync.log.warning(ServerSync.consolePrefix + "Error while updating tables to the new version - " + e.getMessage());
                e.printStackTrace();
    		}
    	}
        
	}
    
    /**
     * Performs encryption startup tasks
     */
    public void encryptionStartup()
    {
    	checkEncryptionRow(); //Verify database is ok
    	loadEncryptionSettings(); //Load encryption settings
    }
    
    /**
     * Checks to see if an encryption row exists in the database and creates one if not
     */
    private void checkEncryptionRow()
    {
    	//if the row doesn't exist, insert it
    	try
		{

	    	Connection conn;
			PreparedStatement ps = null;
			ResultSet result = null;
			conn = getConnection();
			ps = conn.prepareStatement("SELECT `ID` FROM `ServerSync_Encryption`");
			result = ps.executeQuery();
			
			int count = 0;
			while (result.next())
			{
				count++;
			}
			
			if (count == 0)
			{
				//The row does not exist, create it
				createEncryptionRow();
			}
			
			 conn.commit();
             ps.close();
		}
		catch (SQLException e)
		{
			ServerSync.log.warning(ServerSync.consolePrefix + "Encountered an error while checking encryption row: " + e.getMessage());
		}
    }
    
    private void createEncryptionRow()
    {
    	try 
    	{
	    	String sql;
			Connection conn = InputOutput.getConnection();
			
			sql = "INSERT INTO ServerSync_Encryption (`ID`, `Key`, `Enabled`) VALUES (?,?,?)";
			PreparedStatement preparedStatement = conn.prepareStatement(sql);
			
			
	        preparedStatement.setString(1, "1");
	        preparedStatement.setString(2, "");
	        preparedStatement.setString(3, "0");
	        preparedStatement.executeUpdate();
	        conn.commit();
	        
	        ServerSync.log.log(Level.INFO, ServerSync.consolePrefix + "Encryption row created");
    	}
    	catch (SQLException e) 
		{
    		ServerSync.log.log(Level.WARNING, ServerSync.consolePrefix + "Unexpected error while creating encryption row in database!");
			e.printStackTrace();
	    }
    }
    
    private void loadEncryptionSettings()
    {
    	try
		{

	    	Connection conn;
			PreparedStatement ps = null;
			ResultSet result = null;
			conn = getConnection();
			ps = conn.prepareStatement("SELECT `ID`, `Enabled`, `Key` FROM `ServerSync_Encryption`");
			result = ps.executeQuery();
			
			int count = 0;
			while (result.next())
			{
				//Key
				ServerSync.encryption.setEncryptionKey(result.getString("Key"));
				
				//Enabled
				if (result.getString("Enabled").equalsIgnoreCase("1"))
				{
					ServerSync.encryption.setEnabled(true);
				}
				else
				{
					ServerSync.encryption.setEnabled(false);
				}
				
				count++;
			}
			
			if (count == 0)
			{
				//The row does not exist, create it
				createEncryptionRow();
			}
			
			 conn.commit();
             ps.close();
		}
		catch (SQLException e)
		{
			ServerSync.log.warning(ServerSync.consolePrefix + "Encountered an error while checking encryption row: " + e.getMessage());
		}
    }
    
    public void updateEncryptionSettings(Boolean enabled, String key)
    {
    	try 
		{
    		String sql;
    		Connection conn = InputOutput.getConnection();
    		
    		String enabledString;
    		
    		
    		if (enabled)
    		{
    			enabledString = "1";
    		}
    		else
    		{
    			enabledString = "0";
    		}
    		
    		
    		sql = "UPDATE `ServerSync_Encryption` SET `Enabled` = ?, `Key` = ? WHERE `ID` = ?";
			//update
			PreparedStatement preparedStatement = conn.prepareStatement(sql);
	        preparedStatement.setString(1, enabledString);
	        preparedStatement.setString(2, key);
	        preparedStatement.setString(3, "1");
	        preparedStatement.executeUpdate();
	        connection.commit();
    		
    		conn.commit();
   		
    		
		} 
		catch (SQLException e) 
		{
			ServerSync.log.log(Level.WARNING, ServerSync.consolePrefix + "Error while attempting to update encryption row");
		}
    }
    
    
}

