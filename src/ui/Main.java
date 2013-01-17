package ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.eclipse.swt.widgets.Display;
import javax.imageio.ImageIO;
import java.io.File;
import java.net.URL;

import database.Configure;
import database.Connect;
import server.Interface;

@SuppressWarnings("serial")
class Main extends JFrame {
	private static final Dimension modeButton = new Dimension(30, 30);
	private static final Dimension dataButton = new Dimension(25, 25);
	private static final Color chosen = new Color(0x2C5DCD);
	boolean modify;
	Connect database;
	Interface webServer;
	private boolean[] buffered = new boolean[1000];
	private BufferedImage[] bufferedImage = new BufferedImage[1000];
	BufferedImage nopic;
	private JLabel totalNumLabel = new JLabel("");
	private JPanel listPane;
	private JList list;
	private JTable table;
	private JToggleButton textmode, pictextmode, picmode;
	private String query;
	// mode 1 = text-only; mode 2 = picture + text; mode 3 = picture-only
	private int currentMode = 1;

	/**
	 * Buffer pictures from web-server.
	 * 
	 * @param index
	 * @return
	 */
	private BufferedImage paintPic(int index) {
		if (!buffered[index])
			try {
				URL picURL = new URL("http://"
						+ Configure.webserverAddress
						+ "/pic/"
						+ database.pic[index].substring(0,
								database.pic[index].length() - 5)
						+ "/"
						+ database.pic[index].substring(database.pic[index]
								.length() - 5) + ".jpg");
				BufferedImage input = ImageIO.read(picURL);
				Image scaledImage = input.getScaledInstance(150, 200,
						Image.SCALE_DEFAULT);
				bufferedImage[index] = new BufferedImage(150, 200,
						BufferedImage.TYPE_INT_RGB);
				bufferedImage[index].createGraphics().drawImage(scaledImage, 0,
						0, null);
				buffered[index] = true;
			} catch (Exception e) {
			}
		return bufferedImage[index];
	}

	/**
	 * Render picture + text list cells
	 */
	private class PictextList extends JPanel implements ListCellRenderer {
		private boolean isSelected;
		private int index;

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			this.isSelected = isSelected;
			this.index = index;
			return this;
		}

		@Override
		public void paintComponent(Graphics g) {
			if (isSelected) {
				g.setColor(chosen);
				g.fillRect(0, 0, 200, 100);
				g.setColor(Color.white);
			}
			g.drawImage(nopic, 0, 0, 75, 100, this);
			if (database.pic[index].length() == 32)
				g.drawImage(paintPic(index), 0, 0, 75, 100, this);
			g.drawString(database.name[index], 80, 20);
			g.drawString(database.id[index], 80, 40);
			g.drawString(database.faculty[index], 80, 60);
			g.drawString("山东大学", 80, 80);
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(200, 100);
		}
	}

	/**
	 * Render picture list cells
	 */
	private class PicList extends JPanel implements ListCellRenderer {
		private boolean isSelected;
		private int index;

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			this.isSelected = isSelected;
			this.index = index;
			return this;
		}

		@Override
		public void paintComponent(Graphics g) {
			if (isSelected) {
				g.setColor(chosen);
				g.fillRect(0, 0, 75, 120);
				g.setColor(Color.white);
			}
			g.drawImage(nopic, 0, 0, 75, 100, this);
			if (database.pic[index].length() == 32)
				g.drawImage(paintPic(index), 0, 0, 75, 100, this);
			g.drawString(database.name[index], 0, 115);
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(75, 120);
		}
	}

	/**
	 * Wrap list or table in a scroll panel
	 * 
	 * @param mode
	 * @return
	 */
	private JScrollPane mainList(int mode) {
		JScrollPane listScroller;
		if (mode == 1) {
			String[] columnName = { "姓名", "学号", "院系", "身份证号" };
			String[][] cellData = new String[database.id.length][4];
			for (int i = 0; i < database.id.length; i++) {
				cellData[i][0] = database.name[i];
				cellData[i][1] = database.id[i];
				cellData[i][2] = database.faculty[i];
				cellData[i][3] = database.idNum[i];
			}
			DefaultTableModel model = new DefaultTableModel(cellData,
					columnName) {
				public boolean isCellEditable(int row, int column) {
					return false;
				}
			};
			table = new JTable(model);
			table.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 2)
						new Edit(Main.this, 1, database.id[table
								.getSelectedRow()]);
				}
			});
			listScroller = new JScrollPane(table);
		} else {
			list = new JList(database.id);
			if (mode == 2)
				list.setCellRenderer(new PictextList());
			else
				list.setCellRenderer(new PicList());
			list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
			list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
			list.setVisibleRowCount(-1);
			list.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 2)
						new Edit(Main.this, 1, (String) list.getSelectedValue());
				}
			});
			listScroller = new JScrollPane(list);
		}
		return listScroller;
	}

	/**
	 * Update the TotalNumLabel
	 */
	private void updateLabel() {
		if (database.totalNum > 1000)
			totalNumLabel.setText(database.totalNum + "个中的前1000个");
		else
			totalNumLabel.setText("共" + database.totalNum + "个");
	}

	/**
	 * Refresh list or table
	 */
	void refresh() {
		try {
			database.getData(query);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "数据库错误", "运行时错误",
					JOptionPane.ERROR_MESSAGE);
		}
		updateLabel();
		buffered = new boolean[1000];
		listPane.removeAll();
		listPane.add(mainList(currentMode));
		listPane.validate();
	}

	/**
	 * Create the frame.
	 */
	Main(final Display display, final Interface webServer, boolean modify) {
		// Window
		super("数据库管理");
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent winEvt) {
				database.close();
				System.exit(0);
			}
		});
		setSize(650, 400);
		setLocationRelativeTo(null);
		setMinimumSize(new Dimension(450, 300));
		try {
			setIconImage(ImageIO.read(new File("res/icon.png")));
			nopic = ImageIO.read(new File("res/nopic.png"));
		} catch (Exception e) {
		}
		this.webServer = webServer;
		this.modify = modify;
		if (!modify)
			JOptionPane.showMessageDialog(this, "如果需要添加、删除信息，\n请启动服务器端。", "提示",
					JOptionPane.INFORMATION_MESSAGE);

		// Menu
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu dataMenu = new JMenu("数据");
		menuBar.add(dataMenu);

		JMenuItem importData = new JMenuItem("导入数据库...");
		dataMenu.add(importData);
		importData.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				new Port(Main.this, 0, null);
			}
		});

		JMenuItem exportCurrentData = new JMenuItem("导出当前所有条目...");
		dataMenu.add(exportCurrentData);
		exportCurrentData.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					new Port(Main.this, 1, database.getID(query));
				} catch (Exception e) {
					JOptionPane.showMessageDialog(Main.this, "数据库读取错误",
							"运行时错误", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		JMenuItem exportSelectedData = new JMenuItem("导出选中条目...");
		dataMenu.add(exportSelectedData);
		exportSelectedData.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int[] selected;
				if (currentMode == 1)
					selected = table.getSelectedRows();
				else
					selected = list.getSelectedIndices();
				if (selected.length == 0)
					JOptionPane.showMessageDialog(Main.this, "请选择条目", "提示",
							JOptionPane.INFORMATION_MESSAGE);
				else {
					String[] id = new String[selected.length];
					for (int i = 0; i < selected.length; i++)
						id[i] = database.id[selected[i]];
					new Port(Main.this, 1, id);
				}
			}
		});

		// Buttons
		JButton statisticButton = new JButton("统计");
		statisticButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new Statistic(display, query);
			}
		});

		final JButton filterButton = new JButton("筛选");
		filterButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new Filter(Main.this);
				//query = (String) JOptionPane.showInputDialog(Main.this,
						//"请输入条件：", "筛选", JOptionPane.PLAIN_MESSAGE, null, null,
						//query);
				//refresh();
			}
		});
		getRootPane().setDefaultButton(filterButton);

		JButton addButton = new JButton(new ImageIcon("res/addicon.png"));
		addButton.setPreferredSize(dataButton);
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Edit(Main.this, 0, null);
			}
		});

		JButton deleteButton = new JButton(new ImageIcon("res/deleteicon.png"));
		deleteButton.setPreferredSize(dataButton);
		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] selected;
				if (currentMode == 1)
					selected = table.getSelectedRows();
				else
					selected = list.getSelectedIndices();
				if (selected.length == 0)
					JOptionPane.showMessageDialog(Main.this, "请选择条目", "提示",
							JOptionPane.INFORMATION_MESSAGE);
				else if (JOptionPane.showConfirmDialog(Main.this, "是否确认删除？",
						"确认", JOptionPane.YES_NO_OPTION) == 0)
					try {
						for (int i = 0; i < selected.length; i++) {
							if (database.pic[selected[i]].length() == 32)
								webServer.deletePic(database.pic[selected[i]]);
							database.delete(database.id[selected[i]]);
						}
						refresh();
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(Main.this, "删除失败", "错误",
								JOptionPane.ERROR_MESSAGE);
						e1.printStackTrace();
					}
			}
		});

		textmode = new JToggleButton(new ImageIcon("res/texticon.png"), true);
		textmode.setSelectedIcon(new ImageIcon("res/texticonselected.png"));
		textmode.setPreferredSize(modeButton);

		pictextmode = new JToggleButton(new ImageIcon("res/pictexticon.png"));
		pictextmode
				.setSelectedIcon(new ImageIcon("res/pictexticonselected.png"));
		pictextmode.setPreferredSize(modeButton);

		picmode = new JToggleButton(new ImageIcon("res/picicon.png"));
		picmode.setSelectedIcon(new ImageIcon("res/piciconselected.png"));
		picmode.setPreferredSize(modeButton);

		ButtonGroup mode = new ButtonGroup();
		mode.add(textmode);
		mode.add(pictextmode);
		mode.add(picmode);

		class modeListener implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				if (textmode.isSelected()) {
					if (currentMode != 1) {
						listPane.removeAll();
						listPane.add(mainList(1));
						listPane.validate();
						currentMode = 1;
					}
				} else if (pictextmode.isSelected()) {
					if (currentMode != 2) {
						listPane.removeAll();
						listPane.add(mainList(2));
						listPane.validate();
						currentMode = 2;
					}
				} else if (picmode.isSelected()) {
					if (currentMode != 3) {
						listPane.removeAll();
						listPane.add(mainList(3));
						listPane.validate();
						currentMode = 3;
					}
				}
			}
		}
		textmode.addActionListener(new modeListener());
		pictextmode.addActionListener(new modeListener());
		picmode.addActionListener(new modeListener());

		// Get all students' data
		try {
			database = new Connect();
			database.getData(query);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "数据库错误", "启动失败",
					JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}
		updateLabel();

		// Set modifiable
		if (!modify) {
			addButton.setEnabled(false);
			deleteButton.setEnabled(false);
			importData.setEnabled(false);
		}

		// Lay out
		listPane = new JPanel();
		listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));
		listPane.add(mainList(1));
		listPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
		totalNumLabel.setLabelFor(listPane);

		JPanel topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.LINE_AXIS));
		topPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
		topPane.add(totalNumLabel);
		topPane.add(Box.createHorizontalGlue());
		topPane.add(textmode);
		topPane.add(pictextmode);
		topPane.add(picmode);

		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonPane.add(addButton);
		buttonPane.add(deleteButton);
		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(statisticButton);
		buttonPane.add(filterButton);

		Container contentPane = getContentPane();
		contentPane.add(topPane, BorderLayout.NORTH);
		contentPane.add(listPane, BorderLayout.CENTER);
		contentPane.add(buttonPane, BorderLayout.PAGE_END);

		setVisible(true);
	}
}
