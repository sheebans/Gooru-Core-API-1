package org.ednovo.gooru.domain.service.collection;

import org.ednovo.gooru.core.api.model.Collection;
import org.ednovo.gooru.core.api.model.User;

public interface CourseCopyService extends AbstractCollectionCopyService {
	
	Collection courseCopy(String courseId, User user);
	
}
