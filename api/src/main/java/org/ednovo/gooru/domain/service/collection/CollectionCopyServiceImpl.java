package org.ednovo.gooru.domain.service.collection;

import org.ednovo.gooru.core.api.model.Collection;
import org.ednovo.gooru.core.api.model.CollectionType;
import org.ednovo.gooru.core.api.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CollectionCopyServiceImpl extends AbstractCollectionCopyServiceImpl implements CollectionCopyService {

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public Collection collectionCopy(String courseId, String unitId, String lessonId, String collectionId, User user, Collection newCollection) {
		Collection sourceCollection = this.getCollectionDao().getCollectionByType(collectionId, COLLECTION_TYPES);
		rejectIfNull(sourceCollection, GL0056, 404, _COLLECTION);
		final Collection lesson = this.getCollectionDao().getCollectionByType(lessonId, LESSON_TYPE);
		rejectIfNull(lesson, GL0056, 404, LESSON);
		Collection destCollection = collectionCopy(sourceCollection, lesson, user, newCollection);
		updateContentMetaDataSummary(lesson.getContentId(), destCollection.getCollectionType(), LESSON);
		return destCollection;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public Collection collectionCopy(String folderId, String collectionId, User user, Collection newCollection) {
		Collection sourceCollection = this.getCollectionDao().getCollectionByType(collectionId, COLLECTION_TYPES);
		rejectIfNull(sourceCollection, GL0056, 404, _COLLECTION);
		Collection targetCollection = null;
		if (folderId != null) {
			targetCollection = this.getCollectionDao().getCollectionByType(folderId, FOLDER_TYPE);
			rejectIfNull(targetCollection, GL0056, 404, FOLDER);
		} else {
			targetCollection = getCollectionDao().getCollection(user.getPartyUid(), CollectionType.SHElf.getCollectionType());
			if (targetCollection == null) {
				targetCollection = new Collection();
				targetCollection.setCollectionType(CollectionType.SHElf.getCollectionType());
				targetCollection.setTitle(CollectionType.SHElf.getCollectionType());
				super.createCollection(targetCollection, user);
			}
		}
		getAsyncExecutor().deleteFromCache(V2_ORGANIZE_DATA + targetCollection.getUser().getPartyUid() + "*");
		return collectionCopy(sourceCollection, targetCollection, user, newCollection);
	}
	@Override
	public Collection collectionCopy(Collection lesson, String collectionId, User user) {
		Collection sourceCollection = this.getCollectionDao().getCollectionByType(collectionId, COLLECTION_TYPES);
		rejectIfNull(sourceCollection, GL0056, 404, _COLLECTION);
		Collection destCollection = collectionCopy(sourceCollection, lesson, user, new Collection());
		updateContentMetaDataSummary(lesson.getContentId(), destCollection.getCollectionType(), LESSON);
		return destCollection;
	}
}
