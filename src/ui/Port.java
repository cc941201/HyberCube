package ui;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.sql.ResultSet;

import database.Configure;
import database.List;

@SuppressWarnings("serial")
class Port extends JFrame {
	private static final String[] NAME = { "导入", "导出" };
	private int mode;
	private JTextField csvField, picField;

	Port(final Main frame, final int mode, final String[] id) {
		super(NAME[mode] + "数据");
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent winEvt) {
				if (mode == 0)
					frame.refresh();
			}
		});
		setSize(370, 200);
		setLocationRelativeTo(frame);
		setResizable(false);
		this.mode = mode;
		getContentPane().setLayout(null);

		JLabel csvLabel = new JLabel("CSV文件：");
		csvLabel.setBounds(30, 35, 63, 16);
		getContentPane().add(csvLabel);

		JLabel picLabel = new JLabel("照片文件夹：");
		picLabel.setBounds(30, 75, 78, 16);
		getContentPane().add(picLabel);

		csvField = new JTextField();
		csvField.setBounds(120, 30, 134, 28);
		getContentPane().add(csvField);
		csvField.setColumns(10);
		csvLabel.setLabelFor(csvField);

		picField = new JTextField();
		picField.setBounds(120, 70, 134, 28);
		getContentPane().add(picField);
		picField.setColumns(10);
		picLabel.setLabelFor(picField);

		final JButton csvButton = new JButton("选取...");
		csvButton.setBounds(266, 30, 80, 29);
		getContentPane().add(csvButton);
		final JFileChooser csvChooser = new JFileChooser();
		csvChooser.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File file) {
				boolean flag = false;
				if (file.isDirectory() || file.toString().endsWith(".csv"))
					flag = true;
				return flag;
			}

			@Override
			public String getDescription() {
				return "CSV 逗号分割的文件";
			}
		});
		csvButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int result;
				if (mode == 0)
					result = csvChooser.showOpenDialog(Port.this);
				else
					result = csvChooser.showSaveDialog(Port.this);
				if (result == JFileChooser.APPROVE_OPTION)
					csvField.setText(csvChooser.getSelectedFile().toString());
			}
		});

		final JButton picButton = new JButton("选取...");
		picButton.setBounds(266, 70, 80, 29);
		getContentPane().add(picButton);
		final JFileChooser picChooser = new JFileChooser();
		picChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		picButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int result;
				if (mode == 0)
					result = picChooser.showOpenDialog(Port.this);
				else
					result = picChooser.showSaveDialog(Port.this);
				if (result == JFileChooser.APPROVE_OPTION)
					picField.setText(picChooser.getSelectedFile().toString());
			}
		});

		final JProgressBar progressBar = new JProgressBar();
		progressBar.setBounds(30, 130, 160, 20);
		getContentPane().add(progressBar);

		final JButton button = new JButton("开始");
		button.setBounds(230, 126, 117, 29);
		getContentPane().add(button);
		getRootPane().setDefaultButton(button);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				button.setEnabled(false);
				csvButton.setEnabled(false);
				picButton.setEnabled(false);
				csvField.setEnabled(false);
				picField.setEnabled(false);
				new Thread() {
					@Override
					public void run() {
						if (mode == 0) {
							progressBar.setValue(50);
							// TODO
						} else {
							try {
								for (int i = 0; i < id.length; i++) {
									ResultSet rs = frame.database.getOne(id[i]);
									rs.next();
									String[][] content = new String[7][7];
									for (int x = 0; x < 7; x++)
										for (int y = 0; y < 7; y++)
											content[x][y] = rs
													.getString(List.COLUMN_NAME[x][y]);
									String picAddress = rs.getString("pic");
									rs.close();
									ReadableByteChannel url = Channels
											.newChannel(new URL(
													"http://"
															+ Configure.webserverAddress
															+ "/pic/"
															+ picAddress
																	.substring(
																			0,
																			picAddress
																					.length() - 5)
															+ "/"
															+ picAddress
																	.substring(picAddress
																			.length() - 5)
															+ ".jpg")
													.openStream());
									FileOutputStream outStream = new FileOutputStream(
											"123.txt");
									FileChannel out = outStream.getChannel();
									ByteBuffer buffer = ByteBuffer
											.allocate(10000);
									while (url.read(buffer) != -1) {
										buffer.flip();
										out.write(buffer);
										buffer.clear();
									}
									out.close();
									outStream.close();
									url.close();
								}
							} catch (Exception e) {

							}
						}
						finish();
					}
				}.start();
			}
		});

		setVisible(true);
	}

	void finish() {
		JOptionPane.showMessageDialog(Port.this, NAME[mode] + "完成！", "成功",
				JOptionPane.INFORMATION_MESSAGE);
		dispose();
	}
}
