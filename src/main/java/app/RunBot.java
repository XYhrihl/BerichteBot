package app;

import java.io.IOException;
import java.util.List;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;

public class RunBot
{
	public static Config config;

	public static void main (String[] args) throws IOException
    {
	    config = new Config ("config.properties");

		final BerichteBot b_bot = new BerichteBot();

		final TelegramBot bot = b_bot.getTelegramBot();

		bot.setUpdatesListener(updates -> {
            for (Update u : updates)
            {
                b_bot.readInput(u.message().text(), u.message().chat().id());
            }

            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
	}
}
