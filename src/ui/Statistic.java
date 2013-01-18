package ui;

import javax.swing.*;
import java.awt.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import java.io.*;
import java.util.Scanner;

import database.Count;

@SuppressWarnings("serial")
class Statistic extends JFrame {
	private static final String[] COLOR = { "0DA068", "194E9C", "ED9C13",
			"ED5713", "057249", "5F91DC", "F88E5D" };
	private Display display;
	private Main frame;
	private JTabbedPane tabbedPane;

	private class SWTPane extends Panel {
		private Canvas canvas;
		private String name;

		public SWTPane(String name) {
			this.name = name;
			canvas = new Canvas();
			setLayout(new BorderLayout());
			add(canvas, BorderLayout.CENTER);
		}

		@Override
		public void addNotify() {
			super.addNotify();
			display.syncExec(new Runnable() {
				public void run() {
					Shell shell = SWT_AWT.new_Shell(display, canvas);
					shell.setLayout(new FillLayout());
					Browser browser = new Browser(shell, SWT.NONE);
					browser.setLayoutData(BorderLayout.CENTER);
					browser.setUrl("file://"
							+ new File("chart/" + name + ".html")
									.getAbsolutePath());
				}
			});
		}
	}

	void generatePage(String name, String colName) throws Exception {
		File temp = new File("chart/" + colName + ".html");
		temp.deleteOnExit();
		Scanner in = new Scanner(new File("chart/template.html"));
		PrintWriter out = new PrintWriter(temp);
		while (in.hasNextLine())
			out.println(in.nextLine());
		Count[] count = frame.database.getStatistic(colName, frame.query);
		out.println("<th>" + name + "</th><th>人数</th></tr>");
		for (int i = 0; i < count.length; i++)
			if (!count[i].data.equals(""))
				out.println("<tr style=\"color: #" + COLOR[i % COLOR.length]
						+ "\"><td>" + count[i].data + "</td><td>"
						+ count[i].num + "</td></tr>");
		out.println("</table></div></body></html>");
		out.close();
		in.close();
		tabbedPane.addTab(name, null, new SWTPane(colName), null);
	}

	Statistic(Main frame, Display display, String sql) {
		super("统计");
		setSize(1000, 680);
		setResizable(false);
		setLocationRelativeTo(frame);
		this.display = display;
		this.frame = frame;

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		getContentPane().add(tabbedPane, BorderLayout.CENTER);

		try {
			generatePage("性别", "sex");
			generatePage("年龄", "age");
			generatePage("民族", "nation");
			generatePage("院系", "faculty");
			generatePage("校区", "region");
			generatePage("政治面貌", "identity");
		} catch (Exception e) {
			e.printStackTrace();
		}

		setVisible(true);
	}
}
