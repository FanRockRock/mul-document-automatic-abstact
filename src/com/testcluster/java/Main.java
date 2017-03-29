package com.testcluster.java;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;

import com.hankcs.hanlp.summary.TextRankSentence;
import com.tokeniser.java.Tokeniser;
import com.utils.Regex;
import com.vector.java.TFIDFMeasure;
import com.wangfan.io.FileIO;
import com.wangfan.io.SentenceParagraphPosition;

public class Main extends JFrame {
	private double[][] data;// 文档向量
	private String[] docs;
	private Map<String, SentenceParagraphPosition> map;
	private Canopy canopy;

	public static void main(String[] args) {
		Main main = new Main("论文实验");
		main.setVisible(true);
		main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		main.setSize(600, 400);
		main.setResizable(false);
		main.addGridBagPanes();
		main.pack();
	}

	public Main(String title) {
		super(title);
	}

	private void addGridBagPanes() {
		Font font = new Font("宋体", Font.PLAIN, 12);
		UIManager.put("Button.font", font);
		this.setLayout(new GridBagLayout()); // 設置frame的佈局管理器

		// 上侧的工具选择面板
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		// 添加組件
		JTextField searchField = new JTextField(40);
		JButton searchButton = new JButton("浏览目录");

		topPanel.add(searchField);
		topPanel.add(searchButton);

		this.add(topPanel, new GBC(0, 0, 2, 1).setFill(GBC.BOTH).setIpad(0, 0)
				.setWeight(200, 0));

		// 特征选择
		JPanel Panel1 = new JPanel();
		Panel1.setLayout(new BorderLayout());
		JRadioButton feature0 = new JRadioButton("过滤掉常用停用词", true);

		JRadioButton feature1 = new JRadioButton("DF大于");
		JTextField t1 = new JTextField(2);
		JLabel l1 = new JLabel("%小于");
		JTextField t2 = new JTextField(2);
		JLabel l2 = new JLabel("%");
		Box hbox1 = Box.createHorizontalBox();// 创建一个水平箱子
		hbox1.add(feature1); // 在水平箱子上添加一个标签组件，并且创建一个不可见的、20个单位的组件
		hbox1.add(t1);
		hbox1.add(l1);
		hbox1.add(t2);
		hbox1.add(l2);

		JRadioButton feature2 = new JRadioButton("改进的DF大于");
		ButtonGroup featureGroup = new ButtonGroup();
		featureGroup.add(feature0);
		featureGroup.add(feature1);
		featureGroup.add(feature2);
		JTextField t11 = new JTextField(2);
		JLabel l11 = new JLabel("%小于");
		JTextField t22 = new JTextField(2);
		JLabel l22 = new JLabel("%");
		Box hbox11 = Box.createHorizontalBox();// 创建一个水平箱子
		hbox11.add(feature2);
		hbox11.add(t11);
		hbox11.add(l11);
		hbox11.add(t22);
		hbox11.add(l22);

		Box vbox = Box.createVerticalBox();
		vbox.add(feature0);
		vbox.add(hbox1);
		vbox.add(hbox11);

		hbox1.setAlignmentX(LEFT_ALIGNMENT);
		hbox11.setAlignmentX(LEFT_ALIGNMENT);

		JLabel l3 = new JLabel("特征选择函数");
		Box hbox2 = Box.createHorizontalBox();
		hbox2.add(l3);
		hbox2.add(Box.createHorizontalStrut(100));
		hbox2.add(vbox);

		Panel1.add(hbox2, BorderLayout.WEST);

		JLabel jLabel1 = new JLabel(
				"----------------------------------------------------------------------------------------------------------------------------------------");
		Panel1.add(jLabel1, BorderLayout.SOUTH);
		this.add(Panel1, new GBC(0, 1, 2, 1).setFill(GBC.BOTH).setIpad(0, 0)
				.setWeight(0, 100));

		// 语义
		JPanel Panel2 = new JPanel();
		Panel2.setLayout(new BorderLayout());
		JRadioButton rb1 = new JRadioButton("是");
		JLabel l4 = new JLabel("相似度阀值");
		JTextField t3 = new JTextField(2);
		JLabel l5 = new JLabel("%");
		Box hbox3 = Box.createHorizontalBox();// 创建一个水平箱子
		hbox3.add(rb1);
		hbox3.add(Box.createHorizontalStrut(100));
		hbox3.add(l4);
		hbox3.add(t3);
		hbox3.add(l5);

		JRadioButton rb2 = new JRadioButton("否");
		Box vbox1 = Box.createVerticalBox();// 创建一个竖直的箱子
		vbox1.add(hbox3);
		vbox1.add(rb2);
		ButtonGroup group = new ButtonGroup();
		group.add(rb1);
		group.add(rb2);
		hbox3.setAlignmentX(LEFT_ALIGNMENT);
		rb2.setAlignmentX(LEFT_ALIGNMENT);

		Box hbox4 = Box.createHorizontalBox();// 创建一个水平箱子
		JLabel l6 = new JLabel("句子向量化引入语义");
		hbox4.add(l6);
		hbox4.add(Box.createHorizontalStrut(60));
		hbox4.add(vbox1);

		Panel2.add(hbox4, BorderLayout.WEST);

		JLabel jLabel2 = new JLabel(
				"----------------------------------------------------------------------------------------------------------------------------------------");
		Panel2.add(jLabel2, BorderLayout.SOUTH);
		this.add(Panel2, new GBC(0, 2, 2, 1).setFill(GBC.BOTH).setIpad(0, 0)
				.setWeight(0, 100));

		// 抽取句子个数
		JPanel Panel3 = new JPanel();
		Panel3.setLayout(new BorderLayout());
		JRadioButton rb3 = new JRadioButton("输入句子个数");
		JTextField t4 = new JTextField(10);
		Box hbox5 = Box.createHorizontalBox();// 创建一个水平箱子
		hbox5.add(rb3);
		hbox5.add(Box.createHorizontalStrut(70));
		hbox5.add(t4);

		JRadioButton rb4 = new JRadioButton("未知");
		JButton conculateKBut = new JButton("计算句子个数");
		JTextField t5 = new JTextField(2);
		Box hbox6 = Box.createHorizontalBox();// 创建一个水平箱子
		hbox6.add(rb4);
		hbox6.add(Box.createHorizontalStrut(40));
		hbox6.add(conculateKBut);
		hbox6.add(Box.createHorizontalStrut(10));
		hbox6.add(t5);

		Box vbox2 = Box.createVerticalBox();// 创建一个竖直的箱子
		vbox2.add(hbox5);
		vbox2.add(hbox6);
		ButtonGroup group1 = new ButtonGroup();
		group1.add(rb3);
		group1.add(rb4);
		hbox5.setAlignmentX(LEFT_ALIGNMENT);
		hbox6.setAlignmentX(LEFT_ALIGNMENT);

		Box hbox7 = Box.createHorizontalBox();// 创建一个水平箱子
		JLabel l7 = new JLabel("文摘句子个数");
		hbox7.add(l7);
		hbox7.add(Box.createHorizontalStrut(100));
		hbox7.add(vbox2);

		Panel3.add(hbox7, BorderLayout.WEST);

		JLabel jLabel3 = new JLabel(
				"----------------------------------------------------------------------------------------------------------------------------------------");
		Panel3.add(jLabel3, BorderLayout.SOUTH);
		this.add(Panel3, new GBC(0, 3, 2, 1).setFill(GBC.BOTH));

		// 确定、取消按钮
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 140, 5));
		JButton confirmJButton = new JButton("确定");
		JButton cancleJButton = new JButton("取消");
		buttonPanel.add(confirmJButton, BorderLayout.WEST);
		buttonPanel.add(cancleJButton, BorderLayout.EAST);
		this.add(
				buttonPanel,
				new GBC(0, 4, 2, 1).setFill(GBC.BOTH).setIpad(0, 0)
						.setWeight(100, 0));

		// 特征词
		JPanel wordPanel = new JPanel();
		wordPanel.setLayout(new BorderLayout());

		JLabel wordJLabel = new JLabel("文档特征词集合：");
		JTextArea wordArea = new JTextArea(10, 5);
		wordArea.setLineWrap(true);
		wordPanel.add(wordJLabel, BorderLayout.NORTH);
		wordPanel.add(new JScrollPane(wordArea));

		this.add(wordPanel,
				new GBC(0, 5, 2, 1).setFill(GBC.BOTH).setIpad(100, -100)
						.setWeight(100, 0));

		// 文摘效果
		JPanel resultPanel = new JPanel();
		resultPanel.setLayout(new BorderLayout());

		JLabel resultJLabel = new JLabel("自动文摘效果：");
		JTextArea area = new JTextArea(10, 5);
		area.setLineWrap(true);
		resultPanel.add(resultJLabel, BorderLayout.NORTH);
		resultPanel.add(new JScrollPane(area));

		this.add(resultPanel,
				new GBC(0, 6, 2, 1).setFill(GBC.BOTH).setIpad(0, -20)
						.setWeight(100, 0));

		// 事件
		// 浏览目录按钮
		searchButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				area.setText("");
				searchField.setText("");
				JFileChooser jFileChooser = new JFileChooser();
				jFileChooser
						.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				jFileChooser.showDialog(null, "选择");
				File file = jFileChooser.getSelectedFile();
				if (file.isDirectory()) {
					// System.out.println("文件夹:"+file.getAbsolutePath());
					searchField.setText(file.getAbsolutePath());
					try {
						wordArea.setText(getData(file.getAbsolutePath(), 0, 1,
								false));// 一选择目录就得到文档向量的值
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				} else if (file.isFile()) {
					// System.out.println("文件:"+file.getAbsolutePath());
				}
			}
		});
		feature0.addActionListener(new ActionListener() { // 默认过滤掉停用词
			public void actionPerformed(ActionEvent e) {
				t1.setText("");
				t2.setText("");
				t11.setText("");
				t22.setText("");
				try {
					wordArea.setText(getData(searchField.getText(), 0, 1, false));
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		feature1.addActionListener(new ActionListener() { // 捕获DF
			public void actionPerformed(ActionEvent e) {
				t11.setText("");
				t22.setText("");
				String s1 = t1.getText();
				String s2 = t2.getText();
				if (!s1.equals("") && !s2.equals("")) {
					double low = Double.parseDouble(s1);
					double high = Double.parseDouble(s2);
					try {
						wordArea.setText(getData(searchField.getText(),
								low / 100, high / 100, false));
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});
		feature2.addActionListener(new ActionListener() { // 捕获改进的DF
			public void actionPerformed(ActionEvent e) {
				t1.setText("");
				t2.setText("");
				String s1 = t11.getText();
				String s2 = t22.getText();
				if (!s1.equals("") && !s2.equals("")) {
					double low = Double.parseDouble(s1);
					double high = Double.parseDouble(s2);
					try {
						wordArea.setText(getData(searchField.getText(),
								low / 100, high / 100, true));
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});

		rb3.addActionListener(new ActionListener() { // 捕获输入句子个数单选按钮被选中的事件
			public void actionPerformed(ActionEvent e) {
				int length = docs.length;
				t4.setText("输入小于" + length + "的整数...");
				t5.setText("");
				area.setText("");
			}
		});
		// 给输入k框添加事件
		t4.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				JTextField field = (JTextField) e.getSource();
				field.setText("");
				area.setText("");
			}
		});
		rb4.addActionListener(new ActionListener() { // 捕获未知单选按钮被选中的事件
			public void actionPerformed(ActionEvent e) {
				t4.setText("");
				t5.setText("");
				area.setText("");
			}
		});

		// 计算句子个数
		conculateKBut.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (rb4.isSelected()) {
					List<Point> points = new ArrayList<Point>();
					for (int i = 0; i < docs.length; i++) {
						points.add(new Point(data[i]));
					}
					canopy = new Canopy(points);
					canopy.cluster();
					int k = canopy.getClusterNumber();
					t5.setText(String.valueOf(k));
				}
			}
		});
		// 确定按钮
		confirmJButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (rb3.isSelected()) {// 人为输入的k
					if (!t4.getText().equals("")) {
						int k = Integer.parseInt(t4.getText());
						try {
							area.setText(calculate(k));
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}
				if (rb4.isSelected()) {// 计算得到的k
					if (!t5.getText().equals("")) {
						try {
							area.setText(calculateNoK(canopy));
							// canopy=null;
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		});
		// 取消按钮
		cancleJButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				searchField.setText("");
				t1.setText("");
				t2.setText("");
				t11.setText("");
				t22.setText("");
				t3.setText("");
				t4.setText("");
				t5.setText("");
				wordArea.setText("");
				area.setText("");

				feature0.setSelected(true);

				group.clearSelection();

				group1.clearSelection();
			}
		});
	}

	/**
	 * 知道k的情况下计算得到文档文摘
	 * 
	 * @throws IOException
	 */
	private String calculate(int k) throws IOException {
		StringBuffer sb = new StringBuffer();
		double[][] dataTemp = new double[docs.length][];
		// 拷贝一份data数据副本
		for (int i = 0; i < docs.length; i++) {
			dataTemp[i] = data[i].clone();
		}
		WawaKMeans kmeans = new WawaKMeans(dataTemp, k);
		kmeans.Start();
		// 获取聚类结果
		WawaCluster[] clusters = kmeans.getClusters();

		// 获取聚类结果并输出
		for (WawaCluster cluster : clusters) {
			List<Integer> members = cluster.CurrentMembership;
			System.out.println("-----------------");
			for (int i : members) {
				System.out.println(docs[i]);
			}
		}

		System.out.println("\n\n对每个类抽取结果如下：");

		// 每个聚类进行TextRank提取
		int kk = 1;
		List<String> textRankList = new ArrayList<String>();
		for (WawaCluster cluster : clusters) {
			StringBuilder sbb = new StringBuilder();
			List<Integer> members = cluster.CurrentMembership;
			for (int i : members) {
				sbb.append(docs[i] + "。");
			}
			String document = sbb.toString();
			String extractStr = " ";
			List<String> topSentenceList = TextRankSentence.getTopSentenceList(
					document, 1);
			if (topSentenceList.size() > 0) {
				extractStr = topSentenceList.get(0);
			}

			System.out.println("第" + kk + "个类抽取的是:" + extractStr);
			kk++;
			textRankList.add(extractStr);
		}
		// 定义一个装最好结果的TreeMap
//		Map<SentenceParagraphPosition, String> treeMap = new TreeMap<>(
//				new Comparator<SentenceParagraphPosition>() {
//					public int compare(SentenceParagraphPosition o1,
//							SentenceParagraphPosition o2) {
//						if (o1 != null && o2 != null) {
//							if (o1.getParagraph() > o2.getParagraph())
//								return 1;
//							else {
//								if (o1.getParagraph() < o2.getParagraph())
//									return -1;
//								else {
//									if (o1.getSentence() > o2.getSentence())
//										return 1;
//									else {
//										return -1;
//									}
//								}
//							}
//						}
//						return 0;
//					}
//				});
		Map<SentenceParagraphPosition, String> treeMap = new TreeMap<>(
				new Comparator<SentenceParagraphPosition>() {
					public int compare(SentenceParagraphPosition o1,
							SentenceParagraphPosition o2) {
						if (o1 != null && o2 != null) {
							if (Regex.isDateTime(o1.getValue())) {
								return -1;
							} else {
								if (Regex.isDateTime(o2.getValue())) {
									return 1;
								} else {
									if (Regex.isDate(o1.getValue())) {
										return -1;
									} else {
										if (Regex.isDate(o2.getValue())) {
											return 1;
										} else {
											if (o1.getParagraph() > o2
													.getParagraph())
												return 1;
											else {
												if (o1.getParagraph() < o2
														.getParagraph())
													return -1;
												else {
													if (o1.getSentence() > o2
															.getSentence())
														return 1;
													else {
														return -1;
													}
												}
											}
										}

									}
								}

							}

						}
						return 0;
					}
				});
		for (String s : textRankList) {
			if (!s.equals(" ")) {
				SentenceParagraphPosition value = map.get(s);
				treeMap.put(value, s);
			}
		}
		Set<Entry<SentenceParagraphPosition, String>> set = treeMap.entrySet();
		for (Entry<SentenceParagraphPosition, String> entry : set) {
			String valueString = entry.getValue();
			if (!valueString.equals(" "))
				sb.append(entry.getValue() + "。");
		}

		return "        " + sb.toString();
	}

	/**
	 * 不知道k的情况下计算得到文档文摘
	 * 
	 * @throws IOException
	 */
	private String calculateNoK(Canopy canopy) throws IOException {
		StringBuffer sb = new StringBuffer();
		double[][] dataTemp = new double[docs.length][];
		// 拷贝一份data数据副本
		for (int i = 0; i < docs.length; i++) {
			dataTemp[i] = data[i].clone();
		}
		WawaKMeans kmeans = new WawaKMeans(dataTemp, canopy);

		kmeans.Start();
		// 获取聚类结果
		WawaCluster[] clusters = kmeans.getClusters();

		// 获取聚类结果并输出
		for (WawaCluster cluster : clusters) {
			List<Integer> members = cluster.CurrentMembership;
			System.out.println("-----------------");
			for (int i : members) {
				System.out.println(docs[i]);
			}
		}

		System.out.println("\n\n对每个类抽取结果如下：");

		// 每个聚类进行TextRank提取
		int kk = 1;
		List<String> textRankList = new ArrayList<String>();
		for (WawaCluster cluster : clusters) {
			StringBuilder sbb = new StringBuilder();
			List<Integer> members = cluster.CurrentMembership;
			for (int i : members) {
				sbb.append(docs[i] + "。");
			}
			String document = sbb.toString();
			String extractStr = " ";
			List<String> topSentenceList = TextRankSentence.getTopSentenceList(
					document, 1);
			if (topSentenceList.size() > 0) {
				extractStr = topSentenceList.get(0);
			}
			System.out.println("第" + kk + "个类抽取的是:" + extractStr);
			kk++;
			textRankList.add(extractStr);
		}

		// 定义一个装最好结果的TreeMap
		// Map<SentenceParagraphPosition, String> treeMap = new TreeMap<>(
		// new Comparator<SentenceParagraphPosition>() {
		// public int compare(SentenceParagraphPosition o1,
		// SentenceParagraphPosition o2) {
		// if (o1 != null && o2 != null) {
		// if (o1.getParagraph() > o2.getParagraph())
		// return 1;
		// else {
		// if (o1.getParagraph() < o2.getParagraph())
		// return -1;
		// else {
		// if (o1.getSentence() > o2.getSentence())
		// return 1;
		// else {
		// return -1;
		// }
		// }
		// }
		// }
		// return 0;
		// }
		// });

		Map<SentenceParagraphPosition, String> treeMap = new TreeMap<>(
				new Comparator<SentenceParagraphPosition>() {
					public int compare(SentenceParagraphPosition o1,
							SentenceParagraphPosition o2) {
						if (o1 != null && o2 != null) {
							if (Regex.isDateTime(o1.getValue())) {
								return -1;
							} else {
								if (Regex.isDateTime(o2.getValue())) {
									return 1;
								} else {
									if (Regex.isDate(o1.getValue())) {
										return -1;
									} else {
										if (Regex.isDate(o2.getValue())) {
											return 1;
										} else {
											if (o1.getParagraph() > o2
													.getParagraph())
												return 1;
											else {
												if (o1.getParagraph() < o2
														.getParagraph())
													return -1;
												else {
													if (o1.getSentence() > o2
															.getSentence())
														return 1;
													else {
														return -1;
													}
												}
											}
										}

									}
								}

							}

						}
						return 0;
					}
				});
		for (String s : textRankList) {
			if (!s.equals(" ")) {
				SentenceParagraphPosition value = map.get(s);
				treeMap.put(value, s);
			}
		}
		Set<Entry<SentenceParagraphPosition, String>> set = treeMap.entrySet();
		for (Entry<SentenceParagraphPosition, String> entry : set) {
			String valueString = entry.getValue();
			if (!valueString.equals(" "))
				sb.append(entry.getValue() + "。");
		}

		return "        " + sb.toString();
	}

	private String getData(String path, double low, double high,
			boolean isImproDF) throws IOException {
		// 拿到数据
		map = FileIO.getParaPosi(FileIO.readFile(path));
		List<String> list = new ArrayList<>();
		Set<Entry<String, SentenceParagraphPosition>> sets = map.entrySet();
		for (Entry<String, SentenceParagraphPosition> entry : sets) {
			list.add(entry.getKey());
		}
		docs = list.toArray(new String[list.size()]);

		if (docs.length < 1) {
			System.exit(0);
		}

		TFIDFMeasure tf = new TFIDFMeasure(docs, new Tokeniser(), low, high,
				isImproDF);

		data = new double[docs.length][];
		int docCount = docs.length;
		for (int i = 0; i < docCount; i++) {
			data[i] = tf.GetTermVector2(i);
		}
		System.out.println("初始化的data");
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++)
				System.out.print(data[i][j] + " ");
			System.out.println();
		}
		ArrayList<String> list2 = tf.getArrayList();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < list2.size() - 1; i++) {
			sb.append(list2.get(i) + "、");
		}
		sb.append(list2.get(list2.size() - 1));
		return sb.toString();
	}
}

class GBC extends GridBagConstraints {
	// 初始化左上角位置
	public GBC(int gridx, int gridy) {
		this.gridx = gridx;
		this.gridy = gridy;
	}

	// 初始化左上角位置和所占行数和列数
	public GBC(int gridx, int gridy, int gridwidth, int gridheight) {
		this.gridx = gridx;
		this.gridy = gridy;
		this.gridwidth = gridwidth;
		this.gridheight = gridheight;
	}

	// 对齐方式
	public GBC setAnchor(int anchor) {
		this.anchor = anchor;
		return this;
	}

	// 是否拉伸及拉伸方向
	public GBC setFill(int fill) {
		this.fill = fill;
		return this;
	}

	// x和y方向上的增量
	public GBC setWeight(double weightx, double weighty) {
		this.weightx = weightx;
		this.weighty = weighty;
		return this;
	}

	// 外部填充
	public GBC setInsets(int distance) {
		this.insets = new Insets(distance, distance, distance, distance);
		return this;
	}

	// 外填充
	public GBC setInsets(int top, int left, int bottom, int right) {
		this.insets = new Insets(top, left, bottom, right);
		return this;
	}

	// 内填充
	public GBC setIpad(int ipadx, int ipady) {
		this.ipadx = ipadx;
		this.ipady = ipady;
		return this;
	}
}