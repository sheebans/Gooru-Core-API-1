package org.ednovo.gooru.domain.component;

import java.util.List;

import org.ednovo.gooru.core.api.model.Collection;
import org.ednovo.gooru.core.api.model.CollectionItem;
import org.ednovo.gooru.core.api.model.CollectionType;
import org.ednovo.gooru.core.api.model.Content;
import org.ednovo.gooru.core.api.model.Sharing;
import org.ednovo.gooru.core.constant.ParameterProperties;
import org.ednovo.gooru.infrastructure.messenger.IndexProcessor;
import org.ednovo.gooru.infrastructure.persistence.hibernate.CollectionDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CollectionDeleteHandler  {

	@Autowired
	private CollectionDao collectionDao;

	@Autowired
	protected IndexProcessor indexProcessor;

	private static final Logger LOGGER = LoggerFactory.getLogger(CollectionDeleteHandler.class);


	public void deleteCourse(final String courseId) {
		Collection course = getCollectionDao().getCollectionWithoutDeleteCheck(courseId);
		if (course != null) {
			List<CollectionItem> courseItems = getCollectionDao().getCollectionItems(courseId);
			if (courseItems != null) {
				for (CollectionItem collectionItem : courseItems) {
					deleteUnit(collectionItem.getContent().getGooruOid());
				}
			}
			getCollectionDao().remove(course);
		}
	}

	public void deleteUnit(final String unitId) {
		Collection unit = getCollectionDao().getCollectionWithoutDeleteCheck(unitId);
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

	public void deleteLesson(final String lessonId) {
		Collection lesson = getCollectionDao().getCollectionWithoutDeleteCheck(lessonId);
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

	public void deleteCollection(final String collectionId) {
		Collection collection = getCollectionDao().getCollectionWithoutDeleteCheck(collectionId);
		if (collection != null && !collection.getSharing().equalsIgnoreCase(Sharing.PUBLIC.getSharing())) {
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
			try {
				getIndexProcessor().indexByKafkaQueue(collection.getGooruOid(), IndexProcessor.DELETE, ParameterProperties.SCOLLECTION, false, false);
			} catch(Exception e) { 
				LOGGER.debug("Failed to push the  deleted content details to kafka queue.");
			}
			getCollectionDao().remove(collection);
		}
	}

	public void deleteContent(String gooruOid, String collectionType) {
			if (collectionType.equalsIgnoreCase(CollectionType.COURSE.getCollectionType())) {
				deleteCourse(gooruOid);
			} else if (collectionType.equalsIgnoreCase(CollectionType.UNIT.getCollectionType())) {
				deleteUnit(gooruOid);
			} else if (collectionType.equalsIgnoreCase(CollectionType.LESSON.getCollectionType())) {
				deleteLesson(gooruOid);
			} else if (collectionType.equalsIgnoreCase(CollectionType.COLLECTION.getCollectionType()) || collectionType.equalsIgnoreCase(CollectionType.ASSESSMENT.getCollectionType()) || collectionType.equalsIgnoreCase(CollectionType.ASSESSMENT_URL.getCollectionType())) {
				deleteCollection(gooruOid);
			}			
	}

	public CollectionDao getCollectionDao() {
		return collectionDao;
	}

	public IndexProcessor getIndexProcessor() {
		return indexProcessor;
	}
}
