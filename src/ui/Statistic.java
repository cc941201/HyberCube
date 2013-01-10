package ui;

import javax.swing.*;
import java.awt.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

@SuppressWarnings("serial")
class Statistic extends JFrame {
	private Display display;

	private class SWTPane extends Panel {
		private Canvas canvas;

		public SWTPane() {
			canvas = new Canvas();
			setLayout(new BorderLayout());
			add(canvas, BorderLayout.CENTER);
		}

		public void addNotify() {
			super.addNotify();
			display.syncExec(new Runnable() {
				public void run() {
					Shell shell = SWT_AWT.new_Shell(display,
							canvas);
					shell.setLayout(new FillLayout());
					final Browser browser = new Browser(shell, SWT.NONE);
					browser.setLayoutData(BorderLayout.CENTER);
					browser.setUrl("http://blog.csdn.net/fafey");
				}
			});
		}
	}

	Statistic(Display display, String sql) {
		super("统计");
		this.display=display;

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		getContentPane().add(tabbedPane, BorderLayout.CENTER);

		tabbedPane.addTab("比例", null, new SWTPane(), null);

		setVisible(true);
	}
}
