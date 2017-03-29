package com.vector.java;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import com.tokeniser.java.ITokeniser;

public class TFIDFMeasure {
	private String[] _docs;
	private String[][] _ngramDoc;
	private int _numDocs = 0;
	private int _numTerms = 0;
	private ArrayList _terms;
	private int[][] _termFreq;
	private float[][] _termWeight;

	private int[] _maxTermFreq;
	private int[] _docFreq;
	private ArrayList<String> uniques;
	// 装切分句子的容器
	private List<List<String>> docWords;

	ITokeniser _tokenizer = null;

	private Dictionary _wordsIndex = new Hashtable();

	public TFIDFMeasure(String[] documents, ITokeniser tokeniser, double low,
			double high,boolean isImproDF) throws IOException {
		_docs = documents;
		_numDocs = documents.length;
		_tokenizer = tokeniser;
		myInit(low, high,isImproDF);
	}

	public ArrayList<String> getArrayList() {
		return uniques;
	}

	/*
	 * private ArrayList GenerateTerms(String[] docs) throws IOException {
	 * ArrayList uniques = new ArrayList(); _ngramDoc = new String[_numDocs][];
	 * 
	 * for (int i = 0; i < docs.length; i++) { List<String> words =
	 * _tokenizer.partition(docs[i]); for (int j = 0; j < words.size(); j++) if
	 * (!uniques.contains(words.get(j))) uniques.add(words.get(j));
	 * 
	 * } return uniques; }
	 */
	private ArrayList GenerateTerms(String[] docs, double low, double high,
			boolean isImproDF) throws IOException {
		 uniques = new ArrayList<String>();
		_ngramDoc = new String[_numDocs][];

		docWords = new ArrayList<List<String>>();
		for (int i = 0; i < docs.length; i++) {
			List<String> words = _tokenizer.partition(docs[i]);
			docWords.add(words);// 将切分后的单词保存起来
			for (int j = 0; j < words.size(); j++)
				if (!uniques.contains(words.get(j)))
					uniques.add(words.get(j));
		}
		// 判断每个词语是否在DF所设置的范围内，不在的话就剔除
		Iterator it = uniques.iterator();
		while (it.hasNext()) {
			String word = (String) it.next();
			if (!isWithinDF(word, low, high, isImproDF)) {
				it.remove();
			}
		}
		for (int i = 0; i < uniques.size(); i++) {
			System.out.println(uniques.get(i));
		}
		return uniques;
	}

	// 是否在DF所设置的范围内
	private boolean isWithinDF(String word, double low, double high,
			boolean isImproDF) {
		int docNum = 0;
		int wordNum = 0;
		double d = 0;
		if (!isImproDF) {// DF
			for (List<String> list : docWords) {
				for (String s : list) {
					if (s.equals(word)) {
						docNum++;
						break;
					}
				}
			}
			d = (double) docNum / _numDocs;
		} else {// 改进的df
			for (List<String> list : docWords) {
				for (String s : list) {
					if (s.equals(word)) {
						docNum++;
						break;
					}
				}
			}
			double df = (double) docNum / _numDocs;
			for (List<String> list : docWords) {
				for (String s : list) {
					if (s.equals(word)) {
						wordNum++;
					}
				}
			}
			d = (double) (docNum * wordNum) / (_numDocs * docWords.size());
		}

		if (d >= low && d <= high) {
			return true;
		}
		return false;
	}

	private static Object AddElement(Dictionary collection, Object key,
			Object newValue) {
		Object element = collection.get(key);
		collection.put(key, newValue);
		return element;
	}

	private int GetTermIndex(String term) {
		Object index = _wordsIndex.get(term);
		if (index == null)
			return -1;
		return (Integer) index;
	}

	private void myInit(double low, double high,boolean isImproDF) throws IOException {
		_terms = GenerateTerms(_docs, low, high, isImproDF);// 生成总的词语
		_numTerms = _terms.size();

		_maxTermFreq = new int[_numDocs];
		_docFreq = new int[_numTerms];
		_termFreq = new int[_numTerms][];
		_termWeight = new float[_numTerms][];

		for (int i = 0; i < _terms.size(); i++) {
			_termWeight[i] = new float[_numDocs];
			_termFreq[i] = new int[_numDocs];

			AddElement(_wordsIndex, _terms.get(i), i);
		}

		GenerateTermFrequency();
		GenerateTermWeight();

	}

	private float Log(float num) {
		return (float) Math.log(num);// log2
	}

	private void GenerateTermFrequency() throws IOException {
		for (int i = 0; i < _numDocs; i++) {
			String curDoc = _docs[i];
			Dictionary freq = GetWordFrequency(curDoc);//得到当前文档每个单词的出现频率
			Enumeration enums = freq.keys();
			_maxTermFreq[i] = Integer.MIN_VALUE;
			while (enums.hasMoreElements()) {
				String word = (String) enums.nextElement();
				int wordFreq = (Integer) freq.get(word);
				int termIndex = GetTermIndex(word);
				if (termIndex == -1)
					continue;
				_termFreq[termIndex][i] = wordFreq;
				_docFreq[termIndex]++;

				if (wordFreq > _maxTermFreq[i])
					_maxTermFreq[i] = wordFreq;
			}
		}
	}

	private void GenerateTermWeight() {
		for (int i = 0; i < _numTerms; i++) {
			for (int j = 0; j < _numDocs; j++)
				_termWeight[i][j] = ComputeTermWeight(i, j);
		}
	}

	private float GetTermFrequency(int term, int doc) {
		int freq = _termFreq[term][doc];
		int maxfreq = _maxTermFreq[doc];

		return ((float) freq / (float) maxfreq);
	}

	private float GetInverseDocumentFrequency(int term) {
		int df = _docFreq[term];
		return Log((float) (_numDocs) / (float) df);
	}

	private float ComputeTermWeight(int term, int doc) {
		float tf = GetTermFrequency(term, doc);
		float idf = GetInverseDocumentFrequency(term);
		return tf * idf;
	}

	private float[] GetTermVector(int doc) {
		float[] w = new float[_numTerms];
		for (int i = 0; i < _numTerms; i++)
			w[i] = _termWeight[i][doc];
		return w;
	}

	public double[] GetTermVector2(int doc) {
		double[] ret = new double[_numTerms];
		float[] w = GetTermVector(doc);
		for (int i = 0; i < ret.length; i++) {
			ret[i] = w[i];
		}
		return ret;
	}

	public double GetSimilarity(int doc_i, int doc_j) {
		double[] vector1 = GetTermVector2(doc_i);
		double[] vector2 = GetTermVector2(doc_j);

		return TermVector.ComputeCosineSimilarity(vector1, vector2);

	}

	private Dictionary GetWordFrequency(String input) throws IOException {
		String convertedInput = input.toLowerCase();

		List<String> temp = new ArrayList<String>(
				_tokenizer.partition(convertedInput));
		String[] words = new String[temp.size()];
		temp.toArray(words);

		Arrays.sort(words);

		String[] distinctWords = GetDistinctWords(words);

		Dictionary result = new Hashtable();
		for (int i = 0; i < distinctWords.length; i++) {
			Object tmp;
			tmp = CountWords(distinctWords[i], words);
			result.put(distinctWords[i], tmp);

		}

		return result;
	}

	private static String[] GetDistinctWords(String[] input) {
		if (input == null)
			return new String[0];
		else {
			List<String> list = new ArrayList<String>();

			for (int i = 0; i < input.length; i++)
				if (!list.contains(input[i])) // N-GRAM SIMILARITY?
					list.add(input[i]);
			String[] v = new String[list.size()];
			return (String[]) list.toArray(v);
		}
	}

	private int CountWords(String word, String[] words) {
		int itemIdx = Arrays.binarySearch(words, word);

		if (itemIdx > 0)
			while (itemIdx > 0 && words[itemIdx].equals(word))
				itemIdx--;

		int count = 0;
		while (itemIdx < words.length && itemIdx >= 0) {
			if (words[itemIdx].equals(word))
				count++;

			itemIdx++;
			if (itemIdx < words.length)
				if (!words[itemIdx].equals(word))
					break;

		}

		return count;
	}

	public int get_numTerms() {
		return _numTerms;
	}

	public void set_numTerms(int terms) {
		_numTerms = terms;
	}
}