package showdb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Database
{
	Connection conn;
	
	
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
			conn = DriverManager.getConnection("jdbc:sqlite:src/main/resources/database/database.db");
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
