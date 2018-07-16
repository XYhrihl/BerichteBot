package showdb;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ShowDB
{
	public static void main (String[] args)
	{
		Database db = new Database();
		
		ResultSet botUsers = db.getTable("BotUsers");
		ResultSet saves = db.getTable("Saves");
		ResultSet answers = db.getTable("Answers");
		
		try
		{
			System.out.println("\nTabelle BotUsers (id, name, time)");
			while(botUsers.next())
			{
				System.out.println("id: " + botUsers.getInt("id") + ",\tname: " + botUsers.getString("name") + ",\ttime: " + botUsers.getString("time"));
			}
			
			System.out.println("\nTabelle Saves (id, date, time, user_id)");
			while(saves.next())
			{
				System.out.println("id: " + saves.getInt("id") + ",\tdatum: " + saves.getString("datum") + ",\ttime: " + saves.getString("time") + ",\tuser_id: " + saves.getInt("user_id"));
			}
			
			System.out.println("\nTabelle Answers (id, wert, saves_id)");
			while(answers.next())
			{
				System.out.println("id: " + answers.getInt("id") + ",\twert: " + answers.getString("wert") + ",\tsave_id: " + answers.getInt("save_id"));
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
}
