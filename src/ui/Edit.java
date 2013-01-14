package ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.net.URL;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import javax.imageio.ImageIO;

import database.Configure;
import database.List;

@SuppressWarnings("serial")
class Edit extends JFrame {
	private static final String[] NAME = { "添加", "编辑" };
	JTextField idField;
	private Main main;
	private int mode;
	private JComponent[][] field = new JComponent[7][7];
	private JLabel pic;
	private String id;
	private ResultSet rs;
	private String[][] content;
	private File picFile;

	/**
	 * mode 0 = add, mode 1 = edit
	 */
	public Edit(final Main frame, final int mode, final String id) {
		super(NAME[mode] + "资料");
		main = frame;
		this.mode = mode;
		this.id = id;
		setSize(500, 400);
		setLocationRelativeTo(frame);
		setResizable(false);

		// Get all info
		if (mode == 1) {
			try {
				rs = frame.database.getOne(id);
				rs.next();
				content = new String[7][7];
				for (int i = 0; i < 7; i++)
					for (int j = 0; j < 7; j++)
						content[i][j] = rs.getString(List.COLUMN_NAME[i][j]);
				String picAddress = rs.getString("pic");
				rs.close();
				URL picURL = new URL("http://" + Configure.webserverAddress
						+ "/pic/"
						+ picAddress.substring(0, picAddress.length() - 5)
						+ "/" + picAddress.substring(picAddress.length() - 5)
						+ ".jpg");
				pic = new JLabel(new ImageIcon(
						(ImageIO.read(picURL)).getScaledInstance(150, 200,
								java.awt.Image.SCALE_SMOOTH)));
				pic.setBounds(40, 10, 150, 200);
			} catch (Exception e) {
			}
		}

		JPanel buttonPane = new JPanel();
		FlowLayout flowLayout = (FlowLayout) buttonPane.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(buttonPane, BorderLayout.SOUTH);

		JButton button = new JButton("保存");
		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		buttonPane.add(button);
		getRootPane().setDefaultButton(button);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (mode == 0) {
						String id = idField.getText();
						if (id.equals("")) {
							JOptionPane.showMessageDialog(null, "学号必填", "请注意",
									JOptionPane.INFORMATION_MESSAGE);
							return;
						}
						if (frame.database.exist(id)) {
							JOptionPane.showMessageDialog(null, "学号重复", "请注意",
									JOptionPane.INFORMATION_MESSAGE);
							return;
						}
						frame.database.insert(getInput());
					} else {
						for (int i = 0; i < 7; i++)
							for (int j = 0; j < 7; j++) {
								String data = getOne(i, j);
								if (!data.equals(content[i][j]))
									frame.database.update(id,
											List.COLUMN_NAME[i][j], data);
							}
					}
					if (picFile != null)
						frame.database.update(id, "pic",
								"'" + frame.webServer.setPic(picFile) + "'");
					frame.refresh();
					Edit.this.dispose();
				} catch (Exception e1) {
				}
			}
		});
		if (!frame.modify)
			button.setEnabled(false);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
		tabbedPane.addTab("基本", basicInfo());
		tabbedPane.addTab("更多", contactInfo());
		tabbedPane.addTab("培养", teachInfo());
		tabbedPane.addTab("其他", studyInfo());

		setVisible(true);
		if (mode == 1)
			idField.setEnabled(false);
		else
			idField.requestFocus();
	}

	class NumberInputVerifier extends InputVerifier {
		@Override
		public boolean verify(JComponent field) {
			boolean flag = false;
			if (field instanceof JTextField) {
				try {
					Long.parseLong(((JTextField) field).getText());
					flag = true;
				} catch (Exception e) {
				}
			}
			return flag;
		}
	}

	class NullInputVerifier extends InputVerifier {
		@Override
		public boolean verify(JComponent field) {
			boolean flag = false;
			if (field instanceof JTextField) {
				if (((JTextField) field).getText().length() > 0)
					flag = true;
			}
			return flag;
		}
	}

	private JPanel basicInfo() {
		final JPanel basicInfo = new JPanel(true);
		basicInfo.setOpaque(false);
		basicInfo.setLayout(null);

		// picture
		try {
			basicInfo.add(pic);
		} catch (Exception e) {
		}
		final JLabel picback = new JLabel(new ImageIcon("res/addpic.png"));
		picback.setBounds(40, 10, 158, 208);
		basicInfo.add(picback);
		picback.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				JFileChooser picChooser = new JFileChooser();
				picChooser.setFileFilter(new FileFilter() {
					@Override
					public boolean accept(File file) {
						boolean flag = false;
						if (file.isDirectory()
								|| file.toString().endsWith(".jpg"))
							flag = true;
						return flag;
					}

					@Override
					public String getDescription() {
						return "JPG 图像文件";
					}
				});
				int result = picChooser.showOpenDialog(Edit.this);
				if (result == JFileChooser.APPROVE_OPTION) {
					picFile = picChooser.getSelectedFile();
					try {
						if (pic != null)
							basicInfo.remove(pic);
						basicInfo.remove(picback);
						pic = new JLabel(new ImageIcon((ImageIO.read(picFile))
								.getScaledInstance(150, 200,
										java.awt.Image.SCALE_SMOOTH)));
						pic.setBounds(40, 10, 150, 200);
						basicInfo.add(pic);
						basicInfo.add(picback);
					} catch (Exception e1) {
					}
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseReleased(MouseEvent e) {
			}
		});

		// id
		idField = new JTextField();
		idField.setInputVerifier(new NullInputVerifier());
		JLabel idLabel = new JLabel("学号*：");
		idField.setBounds(90, 230, 134, 28);
		idLabel.setBounds(20, 230, 134, 28);
		idLabel.setLabelFor(idField);
		basicInfo.add(idField);
		basicInfo.add(idLabel);
		if (mode == 1)
			(idField).setText(id);

		for (int i = 0; i < 7; i++)
			field[0][i] = field(basicInfo, 0, i);

		return basicInfo;
	}

	private JPanel contactInfo() {
		JPanel contactInfo = new JPanel(true);
		contactInfo.setOpaque(false);
		contactInfo.setLayout(null);

		for (int i = 1; i < 3; i++)
			for (int j = 0; j < 7; j++)
				field[i][j] = field(contactInfo, i, j);

		return contactInfo;
	}

	private JPanel teachInfo() {
		JPanel teachInfo = new JPanel(true);
		teachInfo.setOpaque(false);
		teachInfo.setLayout(null);

		for (int i = 3; i < 5; i++)
			for (int j = 0; j < 7; j++)
				field[i][j] = field(teachInfo, i, j);

		return teachInfo;
	}

	private JPanel studyInfo() {
		JPanel studyInfo = new JPanel(true);
		studyInfo.setOpaque(false);
		studyInfo.setLayout(null);

		for (int i = 5; i < 7; i++)
			for (int j = 0; j < 7; j++)
				field[i][j] = field(studyInfo, i, j);

		return studyInfo;
	}

	JComponent field(JPanel pane, int x, int y) {
		JComponent field = null;
		switch (List.COLUMN_TYPE[x][y]) {
		case 1:
			field = new JTextField();
			if (mode == 1) {
				((JTextField) field).setText(content[x][y]);
				content[x][y] = "'" + content[x][y] + "'";
			}
			break;
		case 2:
			field = new JTextField();
			field.setInputVerifier(new NumberInputVerifier());
			if (mode == 1)
				if (content[x][y] == null)
					content[x][y] = "null";
				else
					((JTextField) field).setText(content[x][y]);
			break;
		case 3:
			field = new JFormattedTextField(new SimpleDateFormat("yyyy-mm-dd"));
			if (mode == 1) {
				if (content[x][y] == null)
					content[x][y] = "0000-00-00";
				((JFormattedTextField) field).setText(content[x][y]);
				content[x][y] = "'" + content[x][y] + "'";
			}
			break;
		case 4:
			String[] list = main.database.getEnumList(x, y);
			field = new JComboBox(list);
			if (mode == 1)
				for (int i = 0; i < list.length; i++)
					if (content[x][y].equals(list[i])) {
						((JComboBox) field).setSelectedIndex(i);
						content[x][y] = String.valueOf(i);
						break;
					}
		}
		JLabel label = new JLabel(List.COLUMN[x][y] + "：");
		if (x % 2 == 0) {
			field.setBounds(320, y * 35 + 20, 134, 28);
			label.setBounds(250, y * 35 + 20, 134, 28);
		} else {
			field.setBounds(90, y * 35 + 20, 134, 28);
			label.setBounds(20, y * 35 + 20, 134, 28);
		}
		label.setLabelFor(field);
		pane.add(field);
		pane.add(label);
		return field;
	}

	private String getInput() {
		String data = "'" + idField.getText() + "'";
		for (int i = 0; i < 7; i++)
			for (int j = 0; j < 7; j++)
				data += "," + getOne(i, j);
		return data;
	}

	private String getOne(int x, int y) {
		String data;
		if (List.COLUMN_TYPE[x][y] == 4)
			data = String.valueOf(((JComboBox) field[x][y]).getSelectedIndex());
		else if (List.COLUMN_TYPE[x][y] == 2) {
			data = ((JTextField) field[x][y]).getText();
			if (data.equals(""))
				data = "null";
		} else
			data = "'" + ((JTextField) field[x][y]).getText() + "'";
		return data;
	}
}
