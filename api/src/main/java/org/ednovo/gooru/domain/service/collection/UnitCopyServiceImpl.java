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
public class UnitCopyServiceImpl extends AbstractCollectionCopyServiceImpl  implements UnitCopyService {

	@Autowired
	private LessonCopyService lessonCopyService;

	@Override
	public Collection unitCopy(String courseId, String unitId, User user) {
		final Collection unit = this.getCollectionDao().getCollectionByType(unitId, UNIT_TYPE);
		rejectIfNull(unit, GL0056, 404, UNIT);
		final Collection course = this.getCollectionDao().getCollectionByType(courseId, COURSE_TYPE);
		rejectIfNull(course, GL0056, 404, COURSE);
		Collection newUnit = copyUnit(unit, course, user);
		updateContentMetaDataSummary(course.getContentId(), UNIT, ADD);
		return newUnit;
	}

	@Override
	public Collection unitCopy(Collection course, String unitId, User user) {
		final Collection unit = this.getCollectionDao().getCollectionByType(unitId, UNIT_TYPE);
		rejectIfNull(unit, GL0056, 404, UNIT);
		Collection newUnit = copyUnit(unit, course, user);
		updateContentMetaDataSummary(course.getContentId(), UNIT, ADD);
		return newUnit;
	}
	
	private Collection copyUnit(Collection unit, Collection course, User user) {
		Collection newUnit = new Collection();
		newUnit.setIdeas(unit.getIdeas());
		newUnit.setQuestions(unit.getQuestions());
		newUnit.setTitle(unit.getTitle());
		newUnit.setCopiedCollectionId(unit.getGooruOid());
		newUnit.setCollectionType(unit.getCollectionType());
		newUnit.setDescription(unit.getDescription());
		newUnit.setNotes(unit.getNotes());
		newUnit.setLanguage(unit.getLanguage());
		newUnit.setGooruOid(UUID.randomUUID().toString());
		newUnit.setContentType(unit.getContentType());
		newUnit.setLastModified(new Date(System.currentTimeMillis()));
		newUnit.setCreatedOn(new Date(System.currentTimeMillis()));
		newUnit.setIsRepresentative(0);
		newUnit.setSharing(unit.getSharing());
		newUnit.setUser(user);
		newUnit.setOrganization(unit.getOrganization());
		newUnit.setCreator(unit.getCreator());
		newUnit.setUrl(unit.getUrl());
		this.getCollectionDao().save(newUnit);
		if (unit.getImagePath() != null && unit.getImagePath().length() > 0) { 
			StringBuilder imagePath = new StringBuilder(newUnit.getFolder());
			imagePath.append(StringUtils.substringAfterLast(unit.getImagePath(), unit.getFolder()));
			newUnit.setImagePath(imagePath.toString());
		}
		this.getCollectionDao().save(newUnit);
		// copy lesson items to collection
		unitCopyItems(unit.getGooruOid(), newUnit, user);
		copyContentTaxonomyCourse(unit.getContentId(), newUnit);
		copyContentMetaAssoc(unit.getContentId(), newUnit);
		copyContentClassification(unit.getContentId(), newUnit);
		copyCollectionRepoStorage(unit, newUnit);
		// copy content meta details
		ContentMeta contentMeta = this.getContentRepository().getContentMeta(unit.getContentId());
		ContentMeta newContentMeta = new ContentMeta();
		if (contentMeta != null) {
			newContentMeta.setContent(newUnit);
			newContentMeta.setMetaData(contentMeta.getMetaData());
			this.getContentRepository().save(newContentMeta);
		}
		// associating the copied collection to lesson
		CollectionItem newCollectionItem = new CollectionItem();
		newCollectionItem.setItemType(ADDED);
		createCollectionItem(newCollectionItem, course, newUnit, user);
		return newUnit;
	}

	protected void unitCopyItems(String unitId, Collection newUnit, User user) {
		List<CollectionItem> collectionItems = this.getCollectionDao().getCollectionItems(unitId, false);
		for (CollectionItem sourceUnitItem : collectionItems) {
			getLessonCopyService().lessonCopy(newUnit, sourceUnitItem.getContent().getGooruOid(), user);
		}
	}
	
	public LessonCopyService getLessonCopyService() {
		return lessonCopyService;
	}

}
