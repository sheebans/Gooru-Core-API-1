package org.ednovo.gooru.domain.service.collection;

import org.ednovo.gooru.core.api.model.Collection;
import org.ednovo.gooru.core.api.model.User;

public interface LessonCopyService extends AbstractCollectionCopyService {

	Collection lessonCopy(String courseId, String unitId, String lessonId, User user);

	Collection lessonCopy(Collection unit, String lessonId, User user);
}
