package app;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;


/*************************************************************************************************
 * -----------------------------------------------------------------------------------------------
 *				INFORMATION
 * -----------------------------------------------------------------------------------------------
 *
 *  CronTrigger Format:
 *	
 *	seconds + " " + minutes + " " + hours + " ? * MON-FRI"
 *
 *************************************************************************************************/


public class User
{
	private boolean expectTime;
	private boolean expectInput;
	private boolean confirmDelete;
	
	private int hours;
	private int minutes;
	private int seconds;
	
	private long id;
	private String name;
	private ArrayList<String> inputs;
	
	private File saveDir;
	
	private AusbildungsnachweiseBot a_bot;
	private TelegramBot bot;
	private JobDetail job;
	private CronTrigger trigger;
	private Scheduler sc;
	
	
	/*******************************************************************************************
	 * 
	 * Constructor (id: long, name: String, bot: TelegramBot, a_bot: AusbildungsnachweiseBot)
	 * 
	 * This Constructor is called if a new User has registered.
	 * 
	 * initialize variables.
	 * create schedule and job and put context into the jobs JobDataMap.
	 * 
	 * *****************************************************************************************/
	
	public User (long id, String name, TelegramBot bot, AusbildungsnachweiseBot a_bot)
	{
		expectTime = false;
		expectInput = false;
		confirmDelete = false;
		
		inputs = new ArrayList<String>();
		
		this.id = id;
		this.name = name;
		this.bot = bot;
		this.a_bot = a_bot;
		
		saveDir = new File("\\\\cccloud.ausbildung.local\\IT-Ausbildung\\Ablage Azubi und Praktikanten\\AusbildungsnachweiseBot\\" + this.name);
		if (saveDir.mkdir())
		{
			System.out.println("[INFO]: Directory \\\\cccloud.ausbildung.local\\IT-Ausbildung\\Ablage Azubi und Praktikanten\\AusbildungsnachweiseBot\\" + this.name + " created.");
		}
		
		job = JobBuilder.newJob(AskJob.class)
				.withIdentity("AskJob_"+id, Scheduler.DEFAULT_GROUP)
				.build();
		
		job.getJobDataMap().put("bot", bot);
		job.getJobDataMap().put("user", this);
		
		try
		{
			sc = StdSchedulerFactory.getDefaultScheduler();
			sc.start();
		}
		catch (SchedulerException e)
		{
			e.printStackTrace();
		}
		
		System.out.println("[INFO]: New user with id " + this.id + " and name " + this.name + " created.");
	}
	
	
	/****************************************************************************************************************
	 * 
	 * Constructor (id: long, name: String, time: String, bot: TelegramBot, a_bot: AusbildungsnachweiseBot)
	 * 
	 * This Constructor is called if the Bot is restarted and the User was allready registered.
	 * 
	 * initialize variables.
	 * create schedule and job and put context into the jobs JobDataMap.
	 * create and set time for this User.
	 * 
	 * **************************************************************************************************************/
	
	public User (long id, String name, String time, TelegramBot bot, AusbildungsnachweiseBot a_bot)
	{
		expectTime = false;
		expectInput = false;
		confirmDelete = false;
		
		inputs = new ArrayList<String>();
		
		this.id = id;
		this.name = name;
		this.bot = bot;
		this.a_bot = a_bot;
		
		saveDir = new File("\\\\cccloud.ausbildung.local\\IT-Ausbildung\\Ablage Azubi und Praktikanten\\AusbildungsnachweiseBot\\" + this.name);
		if (saveDir.mkdir())
		{
			System.out.println("[INFO]: Directory \\\\cccloud.ausbildung.local\\IT-Ausbildung\\Ablage Azubi und Praktikanten\\AusbildungsnachweiseBot\\" + this.name + " created.");
		}
		
		job = JobBuilder.newJob(AskJob.class)
				.withIdentity("AskJob_"+id, Scheduler.DEFAULT_GROUP)
				.build();
		
		job.getJobDataMap().put("bot", bot);
		job.getJobDataMap().put("user", this);
		
		try
		{
			sc = StdSchedulerFactory.getDefaultScheduler();
			sc.start();
		}
		catch (SchedulerException e)
		{
			e.printStackTrace();
		}
		
		// read and set the time
		// if there was no time set in the Database just notify and reply
		if (time == null)
		{
			System.out.println("[WARNING]: User with id: " + id + " and name: " + name + " has no time set in Database.");
			bot.execute(new SendMessage(id, "Dieser Bot läuft wieder. Du hast noch keine Fragezeit festgelegt. Benutze /settime um eine Zeit festzulegen."));
		}
		// if there was a time set in the Database start the scheduler and reply in Telegram
		else
		{
			if (validateTimeString(time))
			{
				String[] timeparts = time.split(":");
				setTime(Integer.parseInt(timeparts[0]), Integer.parseInt(timeparts[1]), Integer.parseInt(timeparts[2]));
				
				bot.execute(new SendMessage(id, "Dieser Bot läuft wieder. Du wirst Montags bis Freitags um " + makeTimeString() + " nach deinen Tatigkeiten gefragt."));
			}
			else
			{
				System.out.println("[ERROR]: Wrong timestring format in Constructor User(long id, String name, String time, TelegramoBot bot, AusbildungsnachweiseBot a_bot)");
				bot.execute(new SendMessage(id, "Beim starten des Bots ist ein fehler aufgetregen. Versuche mit /settime eine neue Zeit zu setzten. Sollte dies nicht funktionieren wende dich an den Systemadministrator oder den Betreiber des Bots."));
			}
		}
		
		System.out.println("[INFO]: New user-object with id " + this.id + " and name " + this.name + " created.");
	}
	

	/*************************************************************************************************************************
	 * 
	 * readInput(text: String, id: long) : void
	 * 
	 * this method is called to compute message wrote by the user in the Telegram chat and send the corresponding answer.
	 * 
	 * ***********************************************************************************************************************/
	
	public void readInput(String text)
	{		
		SendMessage send;
		
		// if the user typed /delete before he needs to confirm with "Ja, lösche meinen Account"
		if (confirmDelete)
		{
			confirmDelete = false;
			if(text.equals("Ja, lösche meinen Account"))
			{
				deleteAccount();
				System.out.println("[INFO]: Account from " + this.name + " with id " + this.id + " has been deleted.");
				send = new SendMessage(id, "Dein Account wurde erfolgreich gelöscht. Sende mir eine Nachricht um dich neu anzumelden.");
			}
			else
			{
				send = new SendMessage(id, "Löschen des Accounts wurde nicht bestätigt und somit abgebrochen.");
			}
		}
		// if next input is expected to be the time (after /start or /settime) this if statemend is executed
		else if (expectTime)
		{
			// check if this action has been canceled. Resets the boolean flag expectTime.
			if (text.equals("/cancel"))
			{
				expectTime = false;
				System.out.println("[INFO]: Settime canceled by user " + this.name + ".");
				send = new SendMessage(id, "Zeiteingabe abgebrochen.");
			}
			else
			{
				// if the input string is valid (in the correct format and with numbers) set the time and confirm in the telegram chat.
				if (validateTimeString(text))
				{
					expectTime = false;
					String[] timeparts = text.split(":");
					
					setTime(Integer.parseInt(timeparts[0]), Integer.parseInt(timeparts[1]), Integer.parseInt(timeparts[2]));
					
					System.out.println("[INFO]: User " + this.name + " has set his time to " + this.makeTimeString());
					send = new SendMessage(id, "Uhrzeit erfolgreich gesetzt: " + makeTimeString());
				}
				// else return a message in the telegram chat
				else
				{
					System.out.println("[INFO]: Wrong input from user " + this.name + " at the attempt to set time.");
					send = new SendMessage(id, "Falsche Eingabe. Bitte folgendes Format verwenden:\n\nhh:mm:ss\n\nhh: 00 - 23\nmm: 00 - 59\nss: 00 - 5\n\nOder mit /cancel abbrechen.");
				}
			}
		}
		// if the user has been asked to enter his/her activities
		else if (expectInput)
		{
			// save the input text in the inputs ArrayList<String> until the input is /beenden
			if (!text.equals("/beenden"))
			{
				inputs.add(text);
				send = new SendMessage(id, "weitere Antwort eingeben oder /beenden um deine Antworten zu speichern.");
			}
			else
			{
				// reset the boolean flag expectInput
				expectInput = false;
				
				// save the content of the inputs ArrayList<String> to a text file
				// name the textfile after the date and time when it was created
				SimpleDateFormat dateformat = new SimpleDateFormat("dd.MM.yyyy");
				SimpleDateFormat filenameformat = new SimpleDateFormat("yyyy-MM-dd__HH-mm-ss");
				Calendar now = new GregorianCalendar();
				String dateFilename = filenameformat.format(now.getTime());
				
				try
				{
					// database
					int saveid = a_bot.getDb().createSave(dateformat.format(now.getTime()), this);
					
					// textfile
					BufferedWriter writer = new BufferedWriter(new FileWriter("\\\\cccloud.ausbildung.local\\IT-Ausbildung\\Ablage Azubi und Praktikanten\\AusbildungsnachweiseBot\\" + this.name +"\\" + dateFilename + ".txt"));
					writer.write("Tätigkeiten vom " + dateFilename + " von " + this.name);
					
					for (String s : inputs)
					{
						// databse
						a_bot.getDb().createAnswer(saveid, s);
						
						// textfile
						writer.append("\r\n" + s);
					}
					writer.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				
				System.out.println("[INFO]: Answers of User " + this.name + " saved.");
				send = new SendMessage(id, "Antworten wurden gespeichert.");
			}
		}
		// if no specifig input is expected check the input for commands
		else 
		{
			/****************************************************************
			 * 		Command list:
			 * 		
			 * 		/help - show help
			 * 		/about - show information about this bot
			 * 		/settime - set asktime
			 * 		/gettime - returns the time which is set at the moment
			 * 		/cancel - cancel current action
			 *		/start - startet den trigger
			 *		/stop - stopt den trigger
			 *		/delete - delete your account (asks to confirm)
			 * 
			 ****************************************************************/
			
			switch (text)
			{
			case "/help":
				send = new SendMessage(id, "Verfügbare Befehle:\n/help\n/about\n/start\n/stop\n/settime\n/gettime\n/cancel");
				break;
				
			case "/about":
				send = new SendMessage(id, "Ich frage dich Täglich zu einer bestimmten Uhrzeit, die du mit /settime festlegen kannst, was du heute gemacht hast. Deine Antworten werden abgespeichert, damit du dann ohne zu überlegen deine Ausbildungsnachweise oder Berichte erstellen kannst.");
				break;
				
			case "/cancel":
				send = new SendMessage(id, "Ich kann nichts abbrechen wenn ich nichts mache...");
				break;
				
			case "/settime":
				send = new SendMessage(id, "Sende mir die gewünschte Zeit im folgenden Format:\n\nhh:mm:ss\n\nhh: 00 - 23\nmm: 00 - 59\nss: 00 - 59");
				expectTime = true;
				break;
				
			case "/gettime":
				send = getTime();
				System.out.println("[INFO]: User " + this.name + " asked for his time.");
				break;
				
			case "/start":
				send = start();
				System.out.println("[INFO]: User " + this.name + " started his Bot.");
				break;
				
			case "/stop":
				send = stop();
				System.out.println("[INFO]: User " + this.name + " stopt his Bot.");
				break;
				
			case "/delete":
				confirmDelete = true;
				send = new SendMessage(id, "Willst du deinen Account wirklich löschen? gebe 'Ja, lösche meinen Account' ein (ohne ') um deinen Account endgültig zu löschen.");
				break;
				
			default:
				send = new SendMessage(id, text);
				break;
			}
		}
		
		bot.execute(send);
	}
	

	/*********************************************************************
	 * 
	 * validateTimeString(time: String) : boolean
	 * 
	 * checks the timestring for correct format and numbers.
	 * returns true if timestring is valid. returns false if it isn't.
	 * 
	 * *******************************************************************/
	
	public boolean validateTimeString(String time)
	{
		String[] timeparts = time.split(":");
		
		if (timeparts.length != 3)
		{
			return false;
		}
		else
		{
			for (String s: timeparts)
			{
				if (s.length() != 2)
				{
					return false;
				}
				if (!Character.isDigit(s.charAt(0)) || !Character.isDigit(s.charAt(1)))
				{
					return false;
				}
			}
		}
		return true;
	}
	
	
	/******************************************************************
	 * 
	 * setTime(hours: int, minutes: int, seconds: int, id: long) : void
	 * 
	 * set the time for the Job and schedule it / reschedule it.
	 * 
	 * ****************************************************************/
	
	public void setTime(int hours, int minutes, int seconds)
	{
		this.hours = hours;
		this.minutes = minutes;
		this.seconds = seconds;
		
		a_bot.getDb().setTime(this);
		
		try
		{
			if (sc.getJobDetail(job.getKey()) != null)
			{
				sc.deleteJob(job.getKey());
			}
			
			if (trigger != null)
			{
				sc.unscheduleJob(trigger.getKey());
			}
			
			trigger = TriggerBuilder.newTrigger()
					.withIdentity("CronTrigger"+id)
					.withSchedule(CronScheduleBuilder.cronSchedule(seconds + " " + minutes + " " + hours + " ? * MON-FRI"))
					.build();
			
			sc.scheduleJob(job, trigger);
			
		}
		catch (SchedulerException e)
		{
			e.printStackTrace();
		}		
	}
	
	
	/****************************************************************************************************************
	 * 
	 * start(id: long) : SendMessage
	 * 
	 * schedules the Job. If a triggertime was allready set earlier it is used. Otherwise it asks to input a new time.
	 * 
	 * returns the message to send in the Telegram chat with the corresponding answer.
	 * 
	 * ***************************************************************************************************************/
	
	public SendMessage start()
	{
		SendMessage send = null;
				
		try
		{
			if (sc.getJobDetail(job.getKey()) != null)
			{
				send = new SendMessage(id, "Bot ist bereits gestartet.");
			}
			else
			{
				if (hours == 0 && minutes == 0 && seconds == 0)
				{
					expectTime = true;
					send = new SendMessage(id, "Bot wird gestartet. Sende mir die gewünschte Fragezeit im folgenden Format:\n\nhh:mm:ss\n\nhh: 00 - 23\nmm: 00 - 59\nss: 00 - 59");
				}
				else
				{
					setTime(hours, minutes, seconds);
					send = new SendMessage(id, "Bot wird gestartet. Ich frage dich von Montag bis Freitag um " + makeTimeString() + " Uhr was du heute gemacht hast.\nMit dem Befehl /settime kannst du die Fragezeit ändern.");
				}
			}
		}
		catch (SchedulerException e)
		{
			e.printStackTrace();
		}
		
		return send;
	}
	
	
	/************************************************************
	 * 
	 * stop(id: long) : SendMessage
	 * 
	 * unschedules the Job.
	 * 
	 * returns the Message to send in the Telegram chat.
	 * 
	 * **********************************************************/
	
	public SendMessage stop()
	{
		SendMessage send = null;
				
		try
		{
			if (sc.getJobDetail(job.getKey()) == null)
			{
				send = new SendMessage(id, "Bot ist bereits gestoppt.");
			}
			else
			{
				sc.unscheduleJob(trigger.getKey());
				send = new SendMessage(id, "Bot wurde angehalten. Benutze /start um ihn wieder zu starten.");
			}
		}
		catch (SchedulerException e)
		{
			e.printStackTrace();
		}
		
		return send;
	}
	
	
	/**************************************************************
	 * 
	 * getTime (id: long) : SendMessage
	 * 
	 * returns a massage containing the time when the Job triggers
	 * 
	 * ************************************************************/
	
	public SendMessage getTime()
	{
		SendMessage send = null;
				
		if (hours == 0 && minutes == 0 && seconds == 0)
		{
			send = new SendMessage(id, "Die Fragezeit wurde noch nicht festgelegt. Benutze den Befehl /settime um die Fragezeit festzulegen.");
		}
		else
		{
			send = new SendMessage(id, "Ich frage dich von Montag bis Freitag um " + makeTimeString() + " Uhr was du gemacht hast.");
		}
		
		return send;
	}
	

	/*************************************************************************
	 * 
	 * asked() : void
	 * 
	 * is called by the Job class to reset the inputs:ArrayList<String> 
	 * and set the expectInput boolean flag to true.
	 * This signals that the next inputs are answers to the Job Question.
	 * 
	 * ***********************************************************************/
	
	public void asked()
	{		
		expectInput = true;
		inputs.clear();
	}
	
	
	/*************************************************************
	 * 
	 * makeTimeString() : String
	 * 
	 * returns the timestring in the correct format hh:mm:ss.
	 * 
	 * ***********************************************************/
	
	public String makeTimeString()
	{
		String hstr, mstr, sstr;
		
		if (hours > 9)
		{
			hstr = "" + hours;
		}
		else
		{
			hstr = "0" + hours;
		}
		
		if (minutes > 9)
		{
			mstr = "" + minutes;
		}
		else
		{
			mstr = "0" + minutes;
		}
		
		if (seconds > 9)
		{
			sstr = "" + seconds;
		}
		else
		{
			sstr = "0" + seconds;
		}
		
		return hstr + ":" + mstr + ":" + sstr;
	}
	
	
	/************************************************************************************
	 * 
	 * deleteAccount() : void
	 * 
	 * deletes this userobject from the a_bot users list and from the Database.
	 * 
	 * **********************************************************************************/
	
	private void deleteAccount()
	{
		a_bot.deleteUser(this);
		a_bot.getDb().deleteUser(this);
	}
	
	
	/*******************************************
	 * 
	 * getName() : String
	 * 
	 * returns the name of this User object
	 * 
	 * *****************************************/
	
	public String getName()
	{
		return this.name;
	}
	
	
	/*******************************************
	 * 
	 * getId() : long
	 * 
	 * returns the id of this User object
	 * 
	 * *****************************************/
	
	public long getId()
	{
		return this.id;
	}
	
}
