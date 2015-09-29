package org.ednovo.gooru.domain.service.collection;

import java.util.List;
import java.util.Map;

public interface LibraryService {

	List<Map<String, Object>> getLibraries();
	
	Map<String, Object> getCourse(String userUid, int limit, int offset);
	
	List<Map<String, Object>> getSubjects(String userUid, int limit, int offset);
	
	List<Map<String, Object>> getCourses(String subjectId, int limit, int offset);
	
	List<Map<String, Object>> getUnits(String courseId, int limit, int offset);
	
	List<Map<String, Object>> getLessons(String unitId, int limit, int offset);
	
}
