package app;

import java.util.List;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;

public class RunBot
{
	public static void main (String[] args)
	{
		final AusbildungsnachweiseBot a_bot = new AusbildungsnachweiseBot();
		
		final TelegramBot bot = a_bot.getTelegramBot();
		bot.setUpdatesListener(new UpdatesListener()
		{
			public int process(List<Update> updates)
			{
				for (Update u : updates)
				{
					a_bot.readInput(u.message().text(), u.message().chat().id());
				}
				
				return UpdatesListener.CONFIRMED_UPDATES_ALL;
			}	
		});
	}
}
