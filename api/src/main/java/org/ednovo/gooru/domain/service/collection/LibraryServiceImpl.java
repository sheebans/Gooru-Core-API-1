package org.ednovo.gooru.domain.service.collection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ednovo.gooru.application.util.GooruImageUtil;
import org.ednovo.gooru.application.util.TaxonomyUtil;
import org.ednovo.gooru.core.application.util.BaseUtil;
import org.ednovo.gooru.core.constant.ConfigConstants;
import org.ednovo.gooru.core.constant.ConstantProperties;
import org.ednovo.gooru.core.constant.Constants;
import org.ednovo.gooru.core.constant.ParameterProperties;
import org.ednovo.gooru.domain.service.setting.SettingService;
import org.ednovo.gooru.infrastructure.persistence.hibernate.LibraryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LibraryServiceImpl implements LibraryService, ParameterProperties, ConstantProperties {

	@Autowired
	private LibraryRepository libraryRepository;

	@Autowired
	private SettingService settingService;

	@Override
	public Map<String, Object> getCourse(String userUid, int limit, int offset) {
		if (!BaseUtil.isUuid(userUid)) {
			userUid = getLibraryRepository().getUserIdByUsername(userUid);
		}
		List<Map<String, Object>> results = getLibraryRepository().getCollections(userUid, COURSE, limit, offset);
		return results != null && results.size() > 0 ? mergeMetaData(results.get(0)) : null;
	}

	@Override
	public List<Map<String, Object>> getSubjects(String userUid, int limit, int offset) {
		if (!BaseUtil.isUuid(userUid)) {
			userUid = getLibraryRepository().getUserIdByUsername(userUid);
		}
		List<Map<String, Object>> results = getLibraryRepository().getCollections(userUid, SUBJECT, limit, offset);
		List<Map<String, Object>> subjects = new ArrayList<Map<String, Object>>();
		for (Map<String, Object> subject : results) {
			subjects.add(mergeMetaData(subject));
		}
		return subjects;
	}

	@Override
	public List<Map<String, Object>> getCourses(String subjectId, int limit, int offset) {
		List<Map<String, Object>> results = getLibraryRepository().getCollectionItems(subjectId, COURSE, limit, offset);
		List<Map<String, Object>> courses = new ArrayList<Map<String, Object>>();
		for (Map<String, Object> course : results) {
			courses.add(mergeMetaData(course));
		}
		return courses;
	}

	@Override
	public List<Map<String, Object>> getUnits(String courseId, int limit, int offset) {
		List<Map<String, Object>> results = getLibraryRepository().getCollectionItems(courseId, UNIT, limit, offset);
		List<Map<String, Object>> units = new ArrayList<Map<String, Object>>();
		for (Map<String, Object> unit : results) {
			units.add(mergeMetaData(unit));
		}
		return units;
	}

	@Override
	public List<Map<String, Object>> getLessons(String unitId, int limit, int offset) {
		List<Map<String, Object>> results = getLibraryRepository().getCollectionItems(unitId, LESSON, limit, offset);
		List<Map<String, Object>> lessons = new ArrayList<Map<String, Object>>();
		for (Map<String, Object> lesson : results) {
			List<Map<String, Object>> collectionResults = getLibraryRepository().getCollectionItems(String.valueOf(lesson.get(GOORU_OID)), COLLECTION_TYPES, 30, 0);
			List<Map<String, Object>> collections = new ArrayList<Map<String, Object>>();
			int count = 0;
			for (Map<String, Object> collection : collectionResults) {
				if (count == 1) {
					collection.put(COLLECTION_ITEMS, getLibraryRepository().getCollectionResourceItems(String.valueOf(collection.get(GOORU_OID)), 4, 0));
				}
				collections.add(mergeMetaData(collection));
			}
			lesson.put(COLLECTION_ITEMS, collections);
			lessons.add(mergeMetaData(lesson));
		}
		return lessons;
	}

	@Override
	public List<Map<String, Object>> getLibraries() {
		List<Map<String, Object>> libraries = new ArrayList<Map<String, Object>>();
		for (Map.Entry<Integer, String> data : Constants.LIBRARIES.entrySet()) {
			Map<String, Object> library = new HashMap<String, Object>();
			library.put(NAME, data.getValue());
			List<Map<String, Object>> results = getLibraryRepository().getLibraries(data.getKey());
			List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
			for (Map<String, Object> item : results) {
				items.add(mergeMetaData(item));
			}
			library.put(ITEMS, items);
			libraries.add(library);
		}
		return libraries;
	}

	protected Map<String, Object> mergeMetaData(Map<String, Object> content) {
		Object thumbnail = content.get(IMAGE_PATH);
		if (thumbnail != null) {
			content.put(THUMBNAILS, GooruImageUtil.getThumbnails(thumbnail));
		}
		if (content.get(GOORU_UID) != null) {
			content.put(USER, setUser(content.get(GOORU_UID), content.get(USER_NAME), content.get(FIRST_NAME), content.get(LAST_NAME)));
			content.remove(GOORU_UID);
			content.remove(USER_NAME);
		}
		content.remove(IMAGE_PATH);
		return content;
	}

	protected Map<String, Object> setUser(Object userUid, Object username, Object firstname, Object lastname) {
		Map<String, Object> user = new HashMap<String, Object>();
		user.put(GOORU_UID, userUid);
		user.put(USER_NAME, username);
		user.put(FIRST_NAME, firstname);
		user.put(LAST_NAME, lastname);
		user.put(PROFILE_IMG_URL, BaseUtil.changeHttpsProtocolByHeader(getSettingService().getConfigSetting(ConfigConstants.PROFILE_IMAGE_URL, TaxonomyUtil.GOORU_ORG_UID)) + "/" + String.valueOf(user.get(GOORU_UID)) + ".png");
		return user;
	}

	public LibraryRepository getLibraryRepository() {
		return libraryRepository;
	}

	public SettingService getSettingService() {
		return settingService;
	}

}