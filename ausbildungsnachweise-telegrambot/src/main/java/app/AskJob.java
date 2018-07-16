package app;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;

public class AskJob implements Job
{

	public void execute(JobExecutionContext context) throws JobExecutionException
	{
		JobDataMap contextMap = context.getJobDetail().getJobDataMap();
		
		TelegramBot bot = (TelegramBot) contextMap.get("bot");
		User user = (User) contextMap.get("user");
				
		user.asked();
		SendMessage send = new SendMessage(user.getId(), "Was hast du heute gemacht? Gebe die Antworten ein und bestätige sie mit Enter. Wenn du fertig bist benutze /beenden um deine Antworten zu speichern.");
		
		bot.execute(send);
	}
	
}
