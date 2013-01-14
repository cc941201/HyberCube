package ui;

import java.awt.*;
import javax.swing.UIManager;
import org.eclipse.swt.widgets.Display;
import java.io.File;
import javax.imageio.ImageIO;
import java.lang.reflect.Method;
import java.rmi.Naming;

import database.Configure;
import server.Interface;

public class Driver {
	public static void main(String[] args) {
		Display.setAppName("HyberCube");
		final Display display = Display.getDefault();

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			if (System.getProperty("os.name").contains("OS X")) {
				// Menu bar
				System.setProperty("apple.laf.useScreenMenuBar", "true");
				// Dock Icon
				Class<?> app = Class.forName("com.apple.eawt.Application");
				Method getapp = app
						.getMethod("getApplication", new Class<?>[0]);
				Object app_obj = getapp.invoke(null, new Object[0]);
				Method seticon = app.getMethod("setDockIconImage",
						new Class[] { Image.class });
				seticon.invoke(app.cast(app_obj),
						new Object[] { ImageIO.read(new File("res/icon.png")) });
			}
		} catch (Exception e) {
		}

		try {
			Configure.read();
		} catch (Exception e) {
			System.out.println("配置文件读取错误！");
			System.exit(-1);
		}

		boolean modify = false;
		Interface webServer = null;
		try {
			webServer = (Interface) Naming.lookup("//"
					+ Configure.webserverAddress + "/hybercube");
			webServer.connect();
			modify = true;
		} catch (Exception e) {
		}

		final boolean modify1 = modify;
		final Interface webServer1 = webServer;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				new Main(display, webServer1, modify1);
			}
		});

		while (true) {
			if (!display.readAndDispatch())
				display.sleep();
		}
	}
}
