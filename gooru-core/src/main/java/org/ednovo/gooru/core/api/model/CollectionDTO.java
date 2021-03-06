package org.ednovo.gooru.core.api.model;

import java.io.Serializable;

public class CollectionDTO implements Serializable{

	 /**
	 * 
	 */
	private static final long serialVersionUID = -2637760123025643274L;
	
	
	private String title;
	private String description;
	private String creator;
	private int subscriptionCount;
	private boolean isSubscribed;
	private String url;
	private String assetName;
	private int grade;
	private String subject;
	private String unit;
	private String topic;
	private String lesson;	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getCreator() {
		return creator;
	}
	public void setCreator(String creator) {
		this.creator = creator;
	}
	public int getGrade() {
		return grade;
	}
	public void setGrade(int grade) {
		this.grade = grade;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	public String getTopic() {
		return topic;
	}
	public void setTopic(String topic) {
		this.topic = topic;
	}
	public String getLesson() {
		return lesson;
	}
	public void setLesson(String lesson) {
		this.lesson = lesson;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getAssetName() {
		return assetName;
	}
	public void setAssetName(String assetName) {
		this.assetName = assetName;
	}
	public int getSubscriptionCount() {
		return subscriptionCount;
	}
	public void setSubscriptionCount(int subscriptionCount) {
		this.subscriptionCount = subscriptionCount;
	}
	public boolean isSubscribed() {
		return isSubscribed;
	}
	public void setSubscribed(boolean isSubscribed) {
		this.isSubscribed = isSubscribed;
	}
	public String getQuestionText() {
		return questionText;
	}
	public void setQuestionText(String questionText) {
		this.questionText = questionText;
	}
	public Long getQuestionId() {
		return questionId;
	}
	public void setQuestionId(Long questionId) {
		this.questionId = questionId;
	}
	public boolean isQuestionOfTheDay() {
		return questionOfTheDay;
	}
	public void setQuestionOfTheDay(boolean questionOfTheDay) {
		this.questionOfTheDay = questionOfTheDay;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	private String questionText;
	private Long questionId;
	private boolean questionOfTheDay;
	
}
