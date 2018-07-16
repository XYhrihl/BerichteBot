package app;

import java.util.List;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;

public class RunBot
{
	public static void main (String[] args)
	{
		final BerichteBot b_bot = new BerichteBot();
		
		final TelegramBot bot = b_bot.getTelegramBot();
		bot.setUpdatesListener(new UpdatesListener()
		{
			public int process(List<Update> updates)
			{
				for (Update u : updates)
				{
					b_bot.readInput(u.message().text(), u.message().chat().id());
				}
				
				return UpdatesListener.CONFIRMED_UPDATES_ALL;
			}	
		});
	}
}
