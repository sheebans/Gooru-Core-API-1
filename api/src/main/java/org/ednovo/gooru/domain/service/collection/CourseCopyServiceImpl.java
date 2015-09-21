package org.ednovo.gooru.domain.service.collection;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.ednovo.gooru.core.api.model.Collection;
import org.ednovo.gooru.core.api.model.CollectionItem;
import org.ednovo.gooru.core.api.model.CollectionType;
import org.ednovo.gooru.core.api.model.ContentMeta;
import org.ednovo.gooru.core.api.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CourseCopyServiceImpl extends AbstractCollectionCopyServiceImpl implements CourseCopyService {

	@Autowired
	private UnitCopyService unitCopyService;

	@Override
	public Collection courseCopy(String courseId, User user) {
		final Collection course = this.getCollectionDao().getCollectionByType(courseId, COURSE_TYPE);
		rejectIfNull(course, GL0056, 404, COURSE);
		Collection shelf = getCollectionDao().getCollection(user.getPartyUid(), CollectionType.SHElf.getCollectionType());
		if (shelf == null) {
			shelf = new Collection();
			shelf.setCollectionType(CollectionType.SHElf.getCollectionType());
			shelf.setTitle(CollectionType.SHElf.getCollectionType());
			shelf = super.createCollection(shelf, user);
		}
		Collection newCourse = copyCourse(course, shelf, user);
		return newCourse;
	}

	private Collection copyCourse(Collection course, Collection shelf, User user) {
		Collection newCourse = new Collection();
		newCourse.setTitle(course.getTitle());
		newCourse.setCopiedCollectionId(course.getGooruOid());
		newCourse.setCollectionType(course.getCollectionType());
		newCourse.setDescription(course.getDescription());
		newCourse.setNotes(course.getNotes());
		newCourse.setLanguage(course.getLanguage());
		newCourse.setImagePath(course.getImagePath());
		newCourse.setGooruOid(UUID.randomUUID().toString());
		newCourse.setContentType(course.getContentType());
		newCourse.setLastModified(new Date(System.currentTimeMillis()));
		newCourse.setCreatedOn(new Date(System.currentTimeMillis()));
		newCourse.setIsRepresentative(0);
		newCourse.setSharing(course.getSharing());
		newCourse.setUser(user);
		newCourse.setOrganization(course.getOrganization());
		newCourse.setCreator(course.getCreator());
		newCourse.setUrl(course.getUrl());
		this.getCollectionDao().save(newCourse);
		// copy lesson items to collection
		courseCopyItems(course.getGooruOid(), newCourse, user);
		copyContentTaxonomyCourse(course.getContentId(), newCourse);
		copyContentMetaAssoc(course.getContentId(), newCourse);
		copyContentClassification(course.getContentId(), newCourse);
		copyCollectionRepoStorage(course, newCourse);
		// copy content meta details
		ContentMeta contentMeta = this.getContentRepository().getContentMeta(course.getContentId());
		ContentMeta newContentMeta = new ContentMeta();
		if (contentMeta != null) {
			newContentMeta.setContent(newCourse);
			newContentMeta.setMetaData(contentMeta.getMetaData());
			this.getContentRepository().save(newContentMeta);
		}
		// associating the copied collection to lesson
		CollectionItem newCollectionItem = new CollectionItem();
		newCollectionItem.setItemType(ADDED);
		createCollectionItem(newCollectionItem, shelf, newCourse, user);
		return newCourse;
	}

	protected void courseCopyItems(String courseId, Collection newCourse, User user) {
		List<CollectionItem> collectionItems = this.getCollectionDao().getCollectionItems(courseId);
		for (CollectionItem sourceCourseItem : collectionItems) {
			getUnitCopyService().unitCopy(newCourse, sourceCourseItem.getContent().getGooruOid(), user);			
		}
	}
	

	public UnitCopyService getUnitCopyService() {
		return unitCopyService;
	}

}
