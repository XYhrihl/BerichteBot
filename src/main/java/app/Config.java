package app;

import java.io.*;
import java.util.Properties;

public class Config
{
    private Properties properties;
    private String path;

    public Config (String path) throws IOException
    {
        this.path = path;
        properties = new Properties();
        try
        {
            properties.load(new FileInputStream(path));
        }
        catch (FileNotFoundException e)
        {
            properties.setProperty("APIToken", "APITokenMissing");
            properties.setProperty("DatabasePath", "save/database.db");
            properties.setProperty("FilePath", "save/");
            save();
        }
    }

    public void save () throws IOException
    {
        OutputStream output = new FileOutputStream(path);
        properties.store(output, null);
    }

    public String getAPIToken ()
    {
        return properties.getProperty("APIToken");
    }

    public String getDatabasePath ()
    {
        return properties.getProperty("DatabasePath");
    }

    public String getFilePath ()
    {
        return properties.getProperty("FilePath");
    }
}
