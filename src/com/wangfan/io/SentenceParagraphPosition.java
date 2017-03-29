package com.wangfan.io;

public class SentenceParagraphPosition {
	private String value;
	private double paragraph;
	private double sentence;
	public double getParagraph() {
		return paragraph;
	}
	public void setParagraph(double paragraph) {
		this.paragraph = paragraph;
	}
	public double getSentence() {
		return sentence;
	}
	public void setSentence(double sentence) {
		this.sentence = sentence;
	}
	public SentenceParagraphPosition(String value,double paragraph,double sentence){
		this.value=value;
		this.paragraph=paragraph;
		this.sentence=sentence;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
