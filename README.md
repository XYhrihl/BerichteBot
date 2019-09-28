# BerichteBot
This is a Telegram Bot that asks you every day for your activities and saves them in a database and textfile.

You need to create your Telegram Bot using the BotFather in Telegram. You get your APIToken from the BotFather when your Bot was created.
For more information on Telegram Bots visit https://core.telegram.org/bots.

You need to set the commands for your Bot via the BotFather. Use the command /setcommands then select your Bot and enter the following commandlist:

``` text
help - Hilfe
about - Informationen zu diesem Bot anzeigen
cancel - Aktuellen Befehl abbrechen
settime - Fragezeit setzen (und falls ausgeschaltet Bot starten)
gettime - Gesetzte Fragezeit anzeigen
delete - Löscht den eigenen Account
start - startet den Bot
stop - stopt den Bot
```

These commands are in german language. If you want to change them you need to make the change in the readInput() function in the User class.

When your Telegram Bot is created and set up you need to clone this repository and build it with maven. Then run the berichte-telegrambot-0.1.jar file that was created in the target folder.
This creates the config.properties file in the directory where the berichte-telegrambot-0.1.jar file is. 
Terminate the berichte-telegrambot-0.1.jar file, open the config.properties file and enter your APIToken. You can change the DatabasePath and FilePath if you want.

This is an example properties file:

``` properties
# Absolute or relative path to your database file.
DatabasePath=save/database.db

# Absolute or relative path to your text file directory.    
FilePath=save/

# Token of your Telegram Bot
APIToken=012345678:AbCd1Ef23Gh4IjKl56MnOpQrSt_UvWx78Yz
```

Now your Bot is set up. Run the berichte-telegrambot-0.1.jar file again and use your bot with Telegram. 