package org.ednovo.gooru.domain.service.collection;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.ednovo.gooru.core.api.model.Collection;
import org.ednovo.gooru.core.api.model.CollectionItem;
import org.ednovo.gooru.core.api.model.ContentMeta;
import org.ednovo.gooru.core.api.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LessonCopyServiceImpl extends AbstractCollectionCopyServiceImpl implements LessonCopyService {

	@Autowired
	private CollectionCopyService collectionCopyService;

	@Override
	public Collection lessonCopy(String courseId, String unitId, String lessonId, User user) {
		final Collection lesson = this.getCollectionDao().getCollectionByType(lessonId, LESSON_TYPE);
		rejectIfNull(lesson, GL0056, 404, LESSON);
		final Collection unit = this.getCollectionDao().getCollectionByType(unitId, UNIT_TYPE);
		rejectIfNull(unit, GL0056, 404, UNIT);
		Collection newLesson = copyLesson(lesson, unit, user);
		updateContentMetaDataSummary(unit.getContentId(), LESSON, ADD);
		return newLesson;
	}
	
	@Override
	public Collection lessonCopy(Collection unit, String lessonId, User user) {
		final Collection lesson = this.getCollectionDao().getCollectionByType(lessonId, LESSON_TYPE);
		rejectIfNull(lesson, GL0056, 404, LESSON);
		Collection newLesson = copyLesson(lesson, unit, user);
		updateContentMetaDataSummary(unit.getContentId(), LESSON, ADD);
		return newLesson;
	}

	private Collection copyLesson(Collection lesson, Collection unit, User user) {
		Collection newLesson = new Collection();
		newLesson.setTitle(lesson.getTitle());
		newLesson.setCopiedCollectionId(lesson.getGooruOid());
		newLesson.setCollectionType(lesson.getCollectionType());
		newLesson.setDescription(lesson.getDescription());
		newLesson.setNotes(lesson.getNotes());
		newLesson.setLanguage(lesson.getLanguage());
		newLesson.setGooruOid(UUID.randomUUID().toString());
		newLesson.setContentType(lesson.getContentType());
		newLesson.setLastModified(new Date(System.currentTimeMillis()));
		newLesson.setCreatedOn(new Date(System.currentTimeMillis()));
		newLesson.setIsRepresentative(0);
		newLesson.setSharing(lesson.getSharing());
		newLesson.setUser(user);
		newLesson.setOrganization(lesson.getOrganization());
		newLesson.setCreator(lesson.getCreator());
		newLesson.setUrl(lesson.getUrl());
		this.getCollectionDao().save(newLesson);
		if (lesson.getImagePath() != null && lesson.getImagePath().length() > 0) { 
			StringBuilder imagePath = new StringBuilder(newLesson.getFolder());
			imagePath.append(StringUtils.substringAfterLast(lesson.getImagePath(), lesson.getFolder()));
			newLesson.setImagePath(imagePath.toString());
		}
		this.getCollectionDao().save(newLesson);
		// copy lesson items to collection
		lessonCopyItems(lesson.getGooruOid(), newLesson, user);
		copyContentTaxonomyCourse(lesson.getContentId(), newLesson);
		copyContentMetaAssoc(lesson.getContentId(), newLesson);
		copyContentClassification(lesson.getContentId(), newLesson);
		copyCollectionRepoStorage(lesson, newLesson);
		// copy content meta details
		ContentMeta contentMeta = this.getContentRepository().getContentMeta(lesson.getContentId());
		ContentMeta newContentMeta = new ContentMeta();
		if (contentMeta != null) {
			newContentMeta.setContent(newLesson);
			newContentMeta.setMetaData(contentMeta.getMetaData());
			this.getContentRepository().save(newContentMeta);
		}
		// associating the copied collection to lesson
		CollectionItem newCollectionItem = new CollectionItem();
		newCollectionItem.setItemType(ADDED);
		createCollectionItem(newCollectionItem, unit, newLesson, user);
		return newLesson;
	}

	protected void lessonCopyItems(String lessonId, Collection newLesson, User user) {
		List<CollectionItem> collectionItems = this.getCollectionDao().getCollectionItems(lessonId, false);
		for (CollectionItem sourceLessonItem : collectionItems) {
			getCollectionCopyService().collectionCopy(newLesson, sourceLessonItem.getContent().getGooruOid(), user);
		}
	}

	public CollectionCopyService getCollectionCopyService() {
		return collectionCopyService;
	}

	

}
