package showdb;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Database
{
	private Connection conn;
	private String databasePath;
	private String databaseFileName;
	private File dbDir;
	
	
	/****************************************
	 * 
	 * Constructor
	 * 
	 * creates connection to the Database.
	 * 
	 * **************************************/
	
	public Database()
	{
		try
		{
			databasePath = "src/main/resources/database/"; // TODO: you can change this path if you want.
			databaseFileName = "database.db"; // TODO: you can change the name of your databasefile here if you want.
			
			dbDir = new File(databasePath);
			if (dbDir.mkdirs())
			{
				System.out.println("[INFO]: Database Directory " + databasePath + " was created.");
			}
			
			conn = DriverManager.getConnection("jdbc:sqlite:" + databasePath + databaseFileName);
			
			if (conn != null)
			{
				System.out.println("Database connection established.");
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	
	/******************************************************************
	 * 
	 * getTable(tablename: String) : ResultSet
	 * 
	 * returns the ResultSet of the table with the given tablename.
	 * returns null if there is en error, e.g. wrong tablename.
	 * 
	 * ****************************************************************/
	
	public ResultSet getTable(String tablename)
	{
		try
		{
			return conn.createStatement().executeQuery("SELECT * FROM " + tablename);
		}
		catch (SQLException e)
		{
			System.out.println("SQL execption. Maybe wrong tablename given to function getTable(tablename) in class showdb.Database.\n");
			e.printStackTrace();
		}
		
		return null;
	}
}
