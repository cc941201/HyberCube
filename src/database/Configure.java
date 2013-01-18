package database;

import java.io.FileReader;
import java.util.Scanner;

public class Configure {
	public static String line, databaseAddress, webserverAddress,
			siteDirectory, picDirectory, database, table, user, password;

	public static void read() throws Exception {
		FileReader in = new FileReader("database.conf");
		Scanner conf = new Scanner(in);
		while (conf.hasNextLine()) {
			String line = conf.nextLine();
			if (line.equals("[database_address]"))
				databaseAddress = conf.next();
			if (line.equals("[webserver_address]"))
				webserverAddress = conf.next();
			if (line.equals("[site_directory]")) {
				siteDirectory = conf.next();
				if (!siteDirectory.endsWith("/"))
					siteDirectory += "/";
			}
			if (line.equals("[pic_directory]")) {
				picDirectory = conf.next();
				if (!picDirectory.endsWith("/"))
					picDirectory += "/";
			}
			if (line.equals("[database]"))
				database = conf.next();
			if (line.equals("[table]"))
				table = conf.next();
			if (line.equals("[user]"))
				user = conf.next();
			if (line.equals("[password]"))
				password = conf.next();
		}
		conf.close();
		in.close();
	}
}
