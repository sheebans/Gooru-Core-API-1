package org.ednovo.gooru.core.api.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.context.ContextLoader;

public class RequestSupport {

	public RequestSupport() {
		log = new HashMap<String, Object>();
		searchIndexMeta = new ArrayList<SearchIndexMeta>();
		moveContentMeta = new HashMap<String, Object>();
	}

	public static RequestSupport getSessionContext() {
		return (RequestSupport) ContextLoader.getCurrentWebApplicationContext().getBean("requestSupport");
	}

	private Map<String, Object> log;

	private List<SearchIndexMeta> searchIndexMeta;

	private Map<String, Object> moveContentMeta;

	public Map<String, Object> getLog() {
		return log;
	}

	public void setLog(Map<String, Object> log) {
		this.log = log;
	}

	public void setSearchIndexMeta(List<SearchIndexMeta> searchIndexMeta) {
		this.searchIndexMeta = searchIndexMeta;
	}

	public List<SearchIndexMeta> getSearchIndexMeta() {
		return searchIndexMeta;
	}

	public Map<String, Object> getMoveContentMeta() {
		return moveContentMeta;
	}

	public void setMoveContentMeta(Map<String, Object> moveContentMeta) {
		this.moveContentMeta = moveContentMeta;
	}
}
