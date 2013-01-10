package ui;

import java.awt.*;
import javax.swing.*;
import org.eclipse.swt.widgets.Display;
import java.io.File;
import javax.imageio.ImageIO;
import java.lang.reflect.Method;

import database.Configure;

public class Driver {
	public static void main(String[] args) {
		Display.setAppName("HyberCube");
		final Display display = Display.getDefault();

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			if (System.getProperty("os.name").startsWith("Mac OS")) {
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
			if (!new File(Configure.siteDirectory + "pic/").isDirectory())
				JOptionPane.showMessageDialog(null,
						"如果需要添加、删除照片，\n请在网页服务器上运行此程序。", "提示",
						JOptionPane.INFORMATION_MESSAGE);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "配置文件错误", "启动失败",
					JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					new Main(display);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, "初始化错误", "无法启动",
							JOptionPane.ERROR_MESSAGE);
					System.exit(-1);
				}
			}
		});

		while (true) {
			if (!display.readAndDispatch())
				display.sleep();
		}
	}
}
