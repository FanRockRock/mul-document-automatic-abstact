package com.wangfan.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileIO {
	public static void main(String[] args) throws UnsupportedEncodingException,
			IOException {
		Map<String, SentenceParagraphPosition> map = getParaPosi(readFile("E:/JavaWorkspace/多文档语料库/航天专家海口被杀"));
		System.out.println(map);
	}

	/**
	 * 通过事件文件夹拿到事件下面的所有小文件名称
	 */
	public static List<String> getFileName(String rootPath) {
		List<String> list = new ArrayList<>();
		File f = new File(rootPath);
		if (!f.exists()) {
			System.out.println(rootPath + " not exists");
			return null;
		}
		File fa[] = f.listFiles();
		for (int i = 0; i < fa.length; i++) {
			File fs = fa[i];
			if (fs.isFile()) {
				list.add(fs.getName());
			}
		}
		return list;
	}

	/**
	 * 读取文件内容
	 * 
	 * @throws IOException
	 */
	public static List<List<String>> readFile(String rootPath)
			throws IOException {
		InputStreamReader read = null;
		BufferedReader reader = null;
		List<List<String>> list = new ArrayList<List<String>>();
		List<String> fileList = getFileName(rootPath);
		for (String fileName : fileList) {
			List<String> l = new ArrayList<String>();
			String pathName = rootPath + "/" + fileName;
			read = new InputStreamReader(
					new FileInputStream(new File(pathName)), "UTF-8");
			reader = new BufferedReader(read);
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.replace((char) 12288, ' ');
				line = line.trim();
				if (!line.isEmpty() && !line.equals("") && !line.equals(' ')
						&& !line.equals("	") && !line.equals("  ")
						&& !line.equals("   ") && !line.equals("    ")
						&& !line.equals("     ")) {
					l.add(line);
				}
			}
			list.add(l);
		}
		int wordNum=0;//字数
		int senNum=0;//句子个数
		//统计每句的平均字数
		for(List<String> sList:list){
			for(String s:sList){
				String[] split = s.split("。");
				for(int i=0;i<split.length;i++){
					++senNum;
					wordNum+=split[i].length();
				}
			}
		}
		System.out.println("文档数目:"+list.size()+"\n平均每条新闻字数:"+(double)wordNum/list.size()+"\n平均每句的字数为："+(double)wordNum/senNum);
		return list;
	}

	/**
	 * 处理句子信息，得到句子所在段落和位置
	 */
	public static Map<String, SentenceParagraphPosition> getParaPosi(
			List<List<String>> list) {
		Map<String, SentenceParagraphPosition> map = new HashMap<String, SentenceParagraphPosition>();
		for (int i = 0; i < list.size(); i++) {
			List<String> l = list.get(i);
			for (int j = 0; j < l.size(); j++) {
				String line = l.get(j);
				String[] sentences = line.split("。");// 以句号切分一行
				for (int k = 0; k < sentences.length; k++) {
					String sentence = sentences[k];
					SentenceParagraphPosition spp = new SentenceParagraphPosition(
							sentence, (double) (j + 1) / l.size(),
							(double) (k + 1) / sentences.length);
					map.put(sentence, spp);
				}
			}
		}
		return map;
	}

	/**
	 * 一行一行读取文件内容
	 * 
	 * @param 文件路径
	 * @return 返回String[]数组
	 */
	public static String[] readFromFile(String path) {
		List<String> list = new ArrayList<String>();
		FileReader reader = null;
		BufferedReader br = null;
		try {
			reader = new FileReader(path);
			br = new BufferedReader(reader);
			String str = null;
			while ((str = br.readLine()) != null) {
				list.add(str);
				System.out.println(str);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return list.toArray(new String[list.size()]);
	}
}
