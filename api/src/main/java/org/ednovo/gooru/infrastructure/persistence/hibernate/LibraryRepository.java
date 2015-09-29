package org.ednovo.gooru.infrastructure.persistence.hibernate;

import java.util.List;
import java.util.Map;

public interface LibraryRepository extends BaseRepository {
	
	List<Map<String, Object>> getLibraries(int categoryId);

	List<Map<String, Object>> getCollections(String userId, String collectionType, int limit, int offset);

	List<Map<String, Object>> getCollectionItems(String gooruOid, String[] collectionType, int limit, int offset);

	List<Map<String, Object>> getCollectionItems(String gooruOid, String collectionType, int limit, int offset);

	List<Map<String, Object>> getCollectionResourceItems(String gooruOid, int limit, int offset);

	String getUserIdByUsername(String username);
}
