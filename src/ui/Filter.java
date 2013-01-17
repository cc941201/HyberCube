package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

@SuppressWarnings("serial")
class Filter extends JFrame {
	private JPanel listPane = new JPanel();
	private JScrollPane scrollPane = new JScrollPane(listPane);
	private JPanel[] conditionPane = new JPanel[128];
	private JButton[] deleteButton = new JButton[128];
	private JComboBox[] box1 = new JComboBox[128], box2 = new JComboBox[128],
			box3 = new JComboBox[128];
	private JTextField[] field = new JTextField[128];
	private int conditionNum = 0;

	public Filter(Main frame) {
		super("筛选");
		setSize(500, 350);
		setResizable(false);
		setLocationRelativeTo(frame);
		((JComponent) getContentPane()).setBorder(BorderFactory
				.createEmptyBorder(10, 10, 10, 10));

		JButton addButton = new JButton("添加");
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (conditionNum < 128) {
					addCondition();
					conditionNum++;
				}
			}
		});

		JButton finishButton = new JButton("完成");

		listPane.setOpaque(false);
		listPane.setLayout(new WrapLayout());

		JPanel bottomPane = new JPanel();
		bottomPane.setLayout(new BorderLayout(0, 0));
		bottomPane.add(addButton, BorderLayout.WEST);
		bottomPane.add(finishButton, BorderLayout.EAST);
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		getContentPane().add(bottomPane, BorderLayout.SOUTH);

		setVisible(true);
	}

	private class DeleteListener implements ActionListener {
		private JPanel pane;

		DeleteListener(JPanel pane) {
			this.pane = pane;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			listPane.remove(pane);
			scrollPane.validate();
			scrollPane.updateUI();

			boolean flag = false;
			conditionNum--;
			for (int i = 0; i < conditionNum; i++) {
				if (conditionPane[i] == pane)
					flag = true;
				if (flag) {
					conditionPane[i] = conditionPane[i + 1];
					box1[i] = box1[i + 1];
					box2[i] = box2[i + 1];
					box3[i] = box3[i + 1];
					field[i] = field[i + 1];
				}
			}
		}
	}

	private void addCondition() {
		conditionPane[conditionNum] = new JPanel();
		conditionPane[conditionNum].setOpaque(false);
		conditionPane[conditionNum].setLayout(new FlowLayout());

		deleteButton[conditionNum] = new JButton("删除");
		conditionPane[conditionNum].add(deleteButton[conditionNum]);
		deleteButton[conditionNum].addActionListener(new DeleteListener(
				conditionPane[conditionNum]));

		box1[conditionNum] = new JComboBox();
		conditionPane[conditionNum].add(box1[conditionNum]);
		box1[conditionNum].setPreferredSize(new Dimension(120, 25));

		box2[conditionNum] = new JComboBox();
		conditionPane[conditionNum].add(box2[conditionNum]);
		box2[conditionNum].setPreferredSize(new Dimension(80, 25));

		field[conditionNum] = new JTextField();
		field[conditionNum].setColumns(10);
		conditionPane[conditionNum].add(field[conditionNum]);

		listPane.add(conditionPane[conditionNum]);
		scrollPane.validate();
	}
}
