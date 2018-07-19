package app;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Database
{
	private Connection conn;
	private String databasePath;
	private String databaseFileName;
	private File dbDir;
	
	
	/***********************************************************************
	 * 
	 * Constructor
	 * 
	 * creates connection to the Database and create tables if not exists.
	 * 
	 * *********************************************************************/
	
	public Database()
	{
		try
		{
			databasePath = "/your/database/path/"; // TODO: put your database path here.
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
				
				conn.createStatement().execute("CREATE TABLE IF NOT EXISTS BotUsers (\n" +
						"id integer NOT NULL, \n" +
						"name text NOT NULL, \n" + 
						"time text, \n" +
						"PRIMARY KEY (id));");
				
				conn.createStatement().execute("CREATE TABLE IF NOT EXISTS Saves (\n" +
						"id integer NOT NULL, \n" +
						"datum text NOT NULL, \n" +
						"time text NOT NULL, \n" +
						"user_id integer, \n" +
						"PRIMARY KEY (id), \n" +
						"FOREIGN KEY (user_id) REFERENCES BotUsers(id));");
				
				conn.createStatement().execute("CREATE TABLE IF NOT EXISTS Answers (\n" +
						"id integer NOT NULL, \n" +
						"wert text, \n" +
						"save_id integer, \n" +
						"PRIMARY KEY (id), \n" +
						"FOREIGN KEY (save_id) REFERENCES Saves(id));");
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	
	/*************************************************
	 * 
	 * createSave(time: String, user: User) : int
	 * 
	 * returns the id (primary key) of the created save.
	 * 
	 * creates an entry in the Saves Database.
	 * 
	 * ***********************************************/
	
	public int createSave(String date, User user)
	{
		Statement statement;
		try
		{
			statement = conn.createStatement();
			statement.execute("INSERT INTO Saves (datum, time, user_id) VALUES ('" + date + "' ,'" + user.makeTimeString() + "' ," + user.getId() + ");");
			
			// return the id of the created save.
			return statement.getGeneratedKeys().getInt(1);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		// return -1. If everything runs properly we don't get here.
		return -1;
	}
	
	
	/**************************************************************************************
	 * 
	 * createAnswer(saveid: int, text: String) : void
	 * 
	 * creates an entry in the Answers Database with the answer and the id of the Save.
	 * 
	 * ************************************************************************************/
	
	public void createAnswer(int saveid, String text)
	{
		try
		{
			conn.createStatement().execute("INSERT INTO Answers (wert, save_id) VALUES ('" + text + "' ," + saveid + ");");
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	
	/****************************************
	 * 
	 * craeteUser(username: String) : void
	 * 
	 * creates the user in the database
	 * 
	 * **************************************/
	
	public void createUser(long id, String username)
	{
		try
		{
			conn.createStatement().execute("INSERT INTO BotUsers (id, name) VALUES (" + id + ", '" + username + "');");
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	
	/************************************************
	 * 
	 * deleteUser(user: User) : void
	 * 
	 * removes the given user from the Database.
	 * 
	 * **********************************************/
	
	public void deleteUser(User user)
	{
		try
		{
			conn.createStatement().execute("DELETE FROM BotUsers WHERE id=" + user.getId() + ";");
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	
	/********************************************************************************************************
	 * 
	 * setTime(user: User) : void
	 * 
	 * Saves the time for this User the format hh:mm:ss as text to the Database.
	 * 
	 * ******************************************************************************************************/	
	
	public void setTime(User user)
	{
		try
		{
			conn.createStatement().execute("UPDATE BotUsers SET time = '" + user.makeTimeString() + "' WHERE id = " + user.getId() + ";");
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	
	/*****************************************************************
	 * 
	 * initialLoad(): ResultSet
	 * 
	 * returns the content of the BotUsers table from Database.
	 * 
	 * ***************************************************************/
	
	public ResultSet initailLoad()
	{
		try
		{
			return conn.createStatement().executeQuery("SELECT * FROM BotUsers;");
		} 
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return null;
	}
}
