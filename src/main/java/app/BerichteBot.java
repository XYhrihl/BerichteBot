package app;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;


public class BerichteBot 
{	
	private int expectUserName;
	private ArrayList<Long> expectIdList;
	
	private ArrayList<User> users;
	private TelegramBot bot;
	private Database db;

	
	/**********************************************************************
	 * 
	 * Constructor
	 * 
	 * Initialize Telegram Bot and users ArrayList<User>
	 * 
	 * ********************************************************************/
	
	public BerichteBot()
	{
		expectUserName = 0;
		expectIdList = new ArrayList<>();
		
		users = new ArrayList<>();
		bot = new TelegramBot(RunBot.config.getAPIToken());
		db = new Database();
		
		loadFromDatabase();
	}
	
	
	/**********************************************************************************************
	 * 
	 * loadFromDatabase() : void
	 * 
	 * this function is called at startup and loads all userinfromation from the Database
	 * 
	 * ********************************************************************************************/
	
	public void loadFromDatabase()
	{
		ResultSet rs = db.initailLoad();
		
		try
		{
			while(rs.next())
			{
				users.add(new User(rs.getLong("id"), rs.getString("name"), rs.getString("time"), bot, this));
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	
	/*************************************************************************************************************************
	 * 
	 * readInput(text: String, id: long) : void
	 * 
	 * this method is called to compute the message wrote by the user in the Telegram chat and send the corresponding answer.
	 * 
	 * ***********************************************************************************************************************/
	
	public void readInput(String text, long id)
	{
		SendMessage send;
				
		// if the id is in the expectIdList and the expectUserName flag is set, this text contains the name of the user.
		if (expectUserName > 0 && expectIdList.contains(id))
		{
			if (getUserByName(text) == null)
			{
				expectUserName--;
				expectIdList.remove(id);
				createUser (id, text);
				send = new SendMessage(id, "Hallo " + text + ". Dein Account wurde angelegt. Benutze den Befehl /about für weiter Informationen zu diesem Bot oder sende /help um einen Überblick über die Befehle zu sehen.");
			}
			else
			{
				send = new SendMessage(id, "Der Name " + text + " wird bereits benutzt.\nBitte verwende einen anderen Namen.");
			}
			
			bot.execute(send);
		}
		// first massage from the chat with this id. Tell him to Enter his name and set the flags to signal that the next input is the name.
		else if (getUserByChatId(id) == null)
		{
			expectUserName++;
			expectIdList.add(id);
			send = new SendMessage(id, "Willkommen beim AusbildungsnachweiseBot.\nBitte gebe deinen Namen ein um dich zu registrieren.");
			
			bot.execute(send);
		}
		// else the user is allready registered and we just pass the text to the corresponding User-object
		else
		{
			getUserByChatId(id).readInput(text);
		}
	}
	
	
	/****************************************************************
	 * 
	 * createUser (id: long, name String) : void
	 * 
	 * creates the User and adds it to the users ArrayList<User>
	 * 
	 * **************************************************************/
	
	public void createUser (long id, String name)
	{
		users.add(new User(id, name, bot, this));
		db.createUser(id, name);
	}
	
	
	/**********************************************************
	 * 
	 * deleteUser (user: User) : void
	 * 
	 * removes the given user from the users ArrayList<User>
	 * 
	 * ********************************************************/
	
	public void deleteUser (User user)
	{
		users.remove(user);
	}
	
	
	/************************************************************************************************
	 * 
	 * getUserByName (name: String) : User
	 * 
	 * Returns the userobject with the given name. Returns null if there is no user with this name.
	 * 
	 * **********************************************************************************************/
	
	public User getUserByName (String name)
	{
		for (User u : users)
		{
			if (u.getName().equals(name))
			{
				return u;
			}
		}
		return null;
	}
	
	
	/************************************************************************************************
	 * 
	 * getUserByChatId (id: long) : User
	 * 
	 * Returns the userobject with the given id. Returns null if there is no user with this id.
	 * 
	 * **********************************************************************************************/
	
	public User getUserByChatId (long id)
	{
		for (User u : users)
		{
			if (u.getId() == id)
			{
				return u;
			}
		}
		return null;
	}
	
	
	/***************************************************************************************************
	 * 
	 * getTelegramBot() : TelegramBot
	 * 
	 * returns the TelegramBot object stored in the attribute bot. This is the connection to Telegram
	 * 
	 * *************************************************************************************************/
	
	public TelegramBot getTelegramBot()
	{
		return this.bot;
	}
	
	
	/***********************************
	 * 
	 * getDb() : Database
	 * 
	 * returns the Database object.
	 * 
	 * *********************************/
	
	public Database getDb()
	{
		return this.db;
	}
}
