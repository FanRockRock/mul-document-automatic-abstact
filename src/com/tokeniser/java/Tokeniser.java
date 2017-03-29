package com.tokeniser.java;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.wltea.analyzer.lucene.IKAnalyzer;

public class Tokeniser implements ITokeniser {

	public Tokeniser() {
	}

	public String[] IK(String input) throws IOException {

		Analyzer anal = new IKAnalyzer(true);
		StringReader reader = new StringReader(input);
		
		TokenStream ts = anal.tokenStream("", reader);

		CharTermAttribute term = ts.getAttribute(CharTermAttribute.class);
	
		List<String> list = new ArrayList<String>();
		while (ts.incrementToken()) {
			list.add(term.toString().trim().toLowerCase());
		}
		reader.close();
		anal.close();

		String[] v = new String[list.size()];
		return (String[]) list.toArray(v);
	}

	public List<String> partition(String input) throws IOException {
		String[] tokens = IK(input);

		List<String> filter = new ArrayList<String>();

		for (int i = 0; i < tokens.length; i++) {
			String token = tokens[i];
			if (token.trim().length() > 0) {
				filter.add(token);
			}
		}
		return filter;
	}
}
