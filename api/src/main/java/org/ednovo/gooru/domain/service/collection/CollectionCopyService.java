package org.ednovo.gooru.domain.service.collection;

import org.ednovo.gooru.core.api.model.Collection;
import org.ednovo.gooru.core.api.model.User;

public interface CollectionCopyService extends AbstractCollectionCopyService {

	Collection collectionCopy(String courseId, String unitId, String lessonId, String collectionId, User user, Collection newCollection);

	Collection collectionCopy(String folderId, String collectionId, User user, Collection newCollection);

	Collection collectionCopy(Collection lesson, String collectionId, User user);

}
