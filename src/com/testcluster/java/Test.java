package com.testcluster.java;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.hankcs.hanlp.summary.TextRankSentence;
import com.tokeniser.java.Tokeniser;
import com.vector.java.TFIDFMeasure;
import com.wangfan.io.FileIO;
import com.wangfan.io.SentenceParagraphPosition;

public class Test {
	public static void main(String[] args) throws IOException {
		// 拿到数据
		Map<String, SentenceParagraphPosition> map = FileIO.getParaPosi(FileIO
				.readFile("E:/JavaWorkspace/多文档语料库/朝鲜核问题"));
		List<String> list = new ArrayList<>();
		Set<Entry<String, SentenceParagraphPosition>> sets = map.entrySet();
		for (Entry<String, SentenceParagraphPosition> entry : sets) {
			list.add(entry.getKey());
		}
	    String[] docs=list.toArray(new String[list.size()]);
//		String[] docs = { "奥运 拳击 入场券 基本 分罄 邹市明 夺冠 对手奥运 浮出水面", "印花税 之 股民 四季",
//				"杭州 股民 放 鞭炮 庆祝 印花税 下调 ", "Asp.Net 页面 执行 流程 分析",
//				"残疾 女 青年 入围 奥运 游泳 比赛 创 奥运 历史 两 项 第一", "asp.net 控件 开发 显示 控件 内容",
//				"奥运 票务 网上 成功 订票 后 应 及时 到 银行 代售 网点 付款", "输 大钱 的 股民 给 我们 启迪",
//				"ASP.NET 自定义 控件 复杂 属性 声明 持久性 浅析" };
//		// String[] docs=FileIO.readFromFile("e:/cluster.txt");

		if (docs.length < 1) {
			System.out.println("没有文档输入");
			System.exit(0);
		}

		TFIDFMeasure tf = new TFIDFMeasure(docs, new Tokeniser(), 0, 1, false);

		double[][] data = new double[docs.length][];
		int docCount = docs.length;
		for (int i = 0; i < docCount; i++) {
			data[i] = tf.GetTermVector2(i);
			for (int j = 0; j < data[i].length; j++) {
				System.out.print(data[i][j] + " ");
			}
			System.out.println();
		}
		List<Point> points = new ArrayList<Point>();
		for (int i = 0; i < docs.length; i++) {
			points.add(new Point(data[i]));
		}
		Canopy canopy = new Canopy(points);
		canopy.cluster();

		// int []result=canopy.getClustersRepresent(points);

		WawaKMeans kmeans = new WawaKMeans(data, canopy);

		kmeans.Start();
		// 6、获取聚类结果并输出
		WawaCluster[] clusters = kmeans.getClusters();
		for (WawaCluster cluster : clusters) {
			List<Integer> members = cluster.CurrentMembership;
			System.out.println("-----------------");
			for (int i : members) {
				System.out.println(docs[i]);
			}
		}

		System.out.println("\n\n对每个类抽取结果如下：");
		// 每个聚类进行TextRank提取
		List<String> textRankList = new ArrayList<String>();
		int k = 1;
		for (WawaCluster cluster : clusters) {
			StringBuilder sb = new StringBuilder();
			List<Integer> members = cluster.CurrentMembership;
			for (int i : members) {
				sb.append(docs[i] + "。");
			}
			String document = sb.toString();
			String extractStr = (TextRankSentence.getTopSentenceList(document,
					1)).get(0);
			System.out.println("第" + k + "个类抽取的是:" + extractStr);
			k++;
			textRankList.add(extractStr);
		}

		System.out.println("\n\n对每个类抽取结果按照位置排序输入如下：");
		// 定义一个装最好结果的TreeMap
		Map<SentenceParagraphPosition, String> treeMap = new TreeMap<>(
				new Comparator<SentenceParagraphPosition>() {
					public int compare(SentenceParagraphPosition o1,
							SentenceParagraphPosition o2) {
						if (o1.getParagraph() > o2.getParagraph())
							return 1;
						else {
							if (o1.getParagraph() < o2.getParagraph())
								return -1;
							else {
								if (o1.getSentence() > o2.getSentence())
									return 1;
								else {
									return -1;
								}
							}
						}
					}
				});
		for (String s : textRankList) {
			SentenceParagraphPosition value = map.get(s);
			treeMap.put(value, s);
		}
		Set<Entry<SentenceParagraphPosition, String>> set = treeMap.entrySet();
		for (Entry<SentenceParagraphPosition, String> entry : set) {
			System.out.print(entry.getValue() + "。");
		}
	}
}
