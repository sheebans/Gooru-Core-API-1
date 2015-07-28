package org.ednovo.gooru.core.api.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;

import org.ednovo.gooru.core.api.model.OAuthClient;

@Entity(name="ltiService")
public class LtiService implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String ltiServiceId;
	
	private String serviceKey;
	
	private String resultSourceId;
	
	private Date createdAt;
	
	private Date updatedAt;
	
	private String serviceSha;

	private OAuthClient oauthClient;
	
	private Long oauthContentId;

	public String getServiceKey() {
		return serviceKey;
	}

	public void setServiceKey(String serviceKey) {
		this.serviceKey = serviceKey;
	}

	public String getResultSourceId() {
		return resultSourceId;
	}

	public void setResultSourceId(String resultSourceId) {
		this.resultSourceId = resultSourceId;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getServiceSha() {
		return serviceSha;
	}

	public void setServiceSha(String serviceSha) {
		this.serviceSha = serviceSha;
	}

	public OAuthClient getOAuthClient() {
		return oauthClient;
	}

	public void setOAuthClient(OAuthClient oauthClient) {
		this.oauthClient = oauthClient;
	}

	public String getLtiServiceId() {
		return ltiServiceId;
	}

	public void setLtiServiceId(String ltiServiceId) {
		this.ltiServiceId = ltiServiceId;
	}

	public Long getOauthContentId() {
		return oauthContentId;
	}

	public void setOauthContentId(Long oauthContentId) {
		this.oauthContentId = oauthContentId;
	}
}
