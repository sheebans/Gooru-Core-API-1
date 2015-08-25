package org.ednovo.gooru.domain.service;

import java.util.List;

import org.ednovo.gooru.core.api.model.Collection;
import org.ednovo.gooru.core.api.model.CollectionItem;
import org.ednovo.gooru.core.api.model.Content;
import org.ednovo.gooru.core.api.model.Sharing;
import org.ednovo.gooru.infrastructure.persistence.hibernate.CollectionDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CollectionDeleteHandler {

	@Autowired
	private CollectionDao collectionDao;

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void deleteCourse(final String courseId) {
		Collection course = getCollectionDao().getCollection(courseId);
		if (course != null) {
			List<CollectionItem> courseItems = getCollectionDao().getCollectionItems(courseId);
			if (courseItems != null) {
				for (CollectionItem collectionItem : courseItems) {
					deleteUnit(collectionItem.getContent().getGooruOid());
				}
			}
		}
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void deleteUnit(final String unitId) {
		Collection unit = getCollectionDao().getCollection(unitId);
		if (unit != null) {
			List<CollectionItem> unitItems = getCollectionDao().getCollectionItems(unitId);
			if (unitItems != null) {
				for (CollectionItem collectionItem : unitItems) {
					deleteLesson(collectionItem.getContent().getGooruOid());
				}
			}
			getCollectionDao().remove(unit);
		}
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void deleteLesson(final String lessonId) {
		Collection lesson = getCollectionDao().getCollection(lessonId);
		if (lesson != null) {
			List<CollectionItem> lessonItems = getCollectionDao().getCollectionItems(lessonId);
			if (lessonItems != null) {
				for (CollectionItem collectionItem : lessonItems) {
					deleteCollection(collectionItem.getContent().getGooruOid());
				}
			}
			getCollectionDao().remove(lesson);
		}
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void deleteCollection(final String collectionId) {
		Collection collection = getCollectionDao().getCollection(collectionId);
		if (collection != null) {
			List<CollectionItem> collectionItems = getCollectionDao().getCollectionItems(collectionId);
			if (collectionItems != null) {
				for (CollectionItem collectionItem : collectionItems) {
					if (collectionItem.getContent().getSharing().equalsIgnoreCase(Sharing.PUBLIC.getSharing())) {
						getCollectionDao().remove(collectionItem);
					} else {
						Content content = collectionItem.getContent();
						getCollectionDao().remove(content);
					}
				}
			}
			getCollectionDao().remove(collection);
		}
	}

	public CollectionDao getCollectionDao() {
		return collectionDao;
	}
}
