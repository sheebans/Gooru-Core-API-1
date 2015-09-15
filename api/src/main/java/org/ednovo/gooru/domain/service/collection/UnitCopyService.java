package org.ednovo.gooru.domain.service.collection;

import org.ednovo.gooru.core.api.model.Collection;
import org.ednovo.gooru.core.api.model.User;

public interface UnitCopyService extends AbstractCollectionCopyService {
	
	Collection unitCopy(String courseId, String unitId, User user);
	
	Collection unitCopy(Collection course, String unitId, User user);
}
