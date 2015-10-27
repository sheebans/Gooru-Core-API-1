package org.ednovo.gooru.infrastructure.persistence.hibernate;

import java.util.List;
import java.util.Map;

import org.ednovo.gooru.core.api.model.UserClass;
import org.ednovo.gooru.core.constant.ConstantProperties;
import org.ednovo.gooru.core.constant.ParameterProperties;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.stereotype.Repository;

@Repository
public class ClassRepositoryHibernate extends BaseRepositoryHibernate implements ClassRepository, ConstantProperties, ParameterProperties {

	private static final String GET_CLASSES = "select class_id as classId, class_uid as classUid,name, user_group_code as classCode, minimum_score as minimumScore, visibility, username, gooru_uid as gooruUId, image_path as thumbnail,  member_count as memberCount, cc.gooru_oid as courseGooruOid, grades from class c inner join user_group ug  on ug.user_group_uid = c.class_uid inner join party p on p.party_uid = ug.user_group_uid inner join  user on  created_by_uid = gooru_uid left join content cc on cc.content_id = course_content_id where p.is_deleted=0 ";

	private static final String GET_STUDY_CLASSES = "select class_uid as classUid,name, user_group_code as classCode, minimum_score as minimumScore, visibility, username, u.gooru_uid as gooruUId, image_path as thumbnail, member_count as memberCount, grades, cc.gooru_oid as courseGooruOid from class c inner join user_group ug  on ug.user_group_uid = c.class_uid inner join party p on p.party_uid = ug.user_group_uid inner join  user u on  created_by_uid = gooru_uid left join content cc on cc.content_id = course_content_id inner join user_group_association uga on uga.user_group_uid = ug.user_group_uid where uga.gooru_uid =:gooruUId and p.is_deleted=0 order by uga.association_date desc";

	private static final String DELETE_USER_FROM_CLASS = "delete uga from class c inner join user_group ug on c.class_uid=ug.user_group_uid inner join user_group_association uga on ug.user_group_uid=uga.user_group_uid where uga.gooru_uid=:gooruUId and c.class_uid=:classUid";

	private static final String GET_MEMBERS = "select p.party_uid as gooruUId,u.username as username,i.external_id as emailId,uga.association_date as associationDate, u.firstname, u.lastname from class c inner join user_group ug on c.class_uid = ug.user_group_uid inner join user_group_association uga on uga.user_group_uid = ug.user_group_uid inner join party p on uga.gooru_uid = p.party_uid left join identity i on i.user_uid = p.party_uid inner join user u on u.gooru_uid = p.party_uid where c.class_uid=:classUid and p.is_deleted=0";

	private static final String GET_STUDY_CLASSES_COUNT = "select count(1) as count from class c inner join user_group ug  on ug.user_group_uid = c.class_uid inner join party p on p.party_uid = ug.user_group_uid inner join  user u on  created_by_uid = gooru_uid left join content cc on cc.content_id = course_content_id inner join user_group_association uga on uga.user_group_uid = ug.user_group_uid where uga.gooru_uid =:gooruUId and p.is_deleted=0 ";

	private static final String GET_CLASSES_COUNT = "select count(1) as count from class c inner join user_group ug  on ug.user_group_uid = c.class_uid inner join party p on p.party_uid = ug.user_group_uid inner join  user on  created_by_uid = gooru_uid left join content cc on cc.content_id = course_content_id where p.is_deleted =0 ";

	private static final String GET_MEMBERS_COUNT = "select count(1) as count from class c inner join user_group ug on c.class_uid = ug.user_group_uid inner join user_group_association uga on uga.user_group_uid = ug.user_group_uid inner join party p on uga.gooru_uid = p.party_uid left join identity i on i.user_uid = p.party_uid inner join user u on u.gooru_uid = p.party_uid where c.class_uid=:classUid and p.is_deleted=0";

	private static final String COLLECTION_ITEM = "select cc.gooru_oid as gooruOid, title, cc.content_id as contentId  from  collection c inner join collection_item ci on ci.resource_content_id = c.content_id  inner join content cc on cc.content_id = ci.resource_content_id inner join content cr on cr.content_id = ci.collection_content_id   where cr.gooru_oid =:gooruOid order by ci.item_sequence";

	private static final String COLLECTION_CLASS_SETTINGS = "select collection.collection_id as collectionId, title, gooru_oid as gooruOid, visibility, score_type_id as scoreTypeId   from (select cr.content_id as collection_id, cr.title, co.gooru_oid   from collection c inner join collection_item ci on ci.collection_content_id = c.content_id inner join collection cr on cr.content_id = ci.resource_content_id inner join content co on  co.content_id = cr.content_id   where collection_content_id =:lessonId) as collection left join (select c.class_id, c.class_uid, collection_id, cs.visibility, score_type_id  from class c  left join class_collection_settings cs  on c.class_id = cs.class_id where class_uid =:classUid) as class_setting on class_setting.collection_id = collection.collection_id";

	private static final String LESSON_COLLECTIONS = "select re.title, cr.gooru_oid as gooruOid, re.collection_type as collectionType, re.image_path as imagePath, ci.collection_item_id as collectionItemId, ci.item_sequence as itemSequence, re.description, re.url, cm.meta_data as metaData  from  collection c  inner join content cc on cc.content_id =  c.content_id inner join collection_item ci on ci.collection_content_id = c.content_id inner join collection re on re.content_id = ci.resource_content_id inner join content cr on  cr.content_id = re.content_id inner join class_collection_settings ccs on ccs.collection_id=cr.content_id  inner join organization o  on  o.organization_uid = cr.organization_uid  left join collection co on co.content_id = re.content_id left join content_meta cm  on  cm.content_id = re.content_id  where cc.gooru_oid =:gooruOid and ccs.visibility=1 and class_id=:classUid order by ci.item_sequence";

	private static final String COLLECTION_ITEMS = "select r.title, c.gooru_oid as gooruOid, r.type_name as resourceType, r.folder, r.thumbnail, ct.value, ct.display_name as displayName, ci.collection_item_id as collectionItemId, r.url, rsummary.rating_star_avg as average, rsummary.rating_star_count as count, ci.item_sequence as itemSequence, cm.meta_data as metaData from collection_item ci inner join resource r on r.content_id = ci.resource_content_id  left join custom_table_value ct on ct.custom_table_value_id = r.resource_format_id inner join content c on c.content_id = r.content_id inner join content rc on rc.content_id = ci.collection_content_id left join collection co on co.content_id = r.content_id left join content_meta cm on cm.content_id = c.content_id  left join resource_summary rsummary on   c.gooru_oid = rsummary.resource_gooru_oid  where rc.gooru_oid =:gooruOid  order by ci.item_sequence";

	private static final String GET_CLASS_UID = "select cs.class_uid from collection co join content c on c.content_id = co.content_id join class cs on cs.course_content_id = c.content_id where  c.gooru_oid =:courseId";

	private static final String UPDATE_MEMBER_COUNT = "update   user_group set member_count = (select count(1) from user_group_association where user_group_uid =:classUid)   where  user_group_uid =:classUid";

	private static final String COURSE_BY_CLASS = " select collection.collection_type collectionType, collection.collection_id as collectionId, title, gooru_oid as gooruOid, if(visibility=1,1,0) visibility  from (select cr.content_id as collection_id, cr.title, co.gooru_oid, cr.collection_type,item_sequence   from collection c inner join collection_item ci on ci.collection_content_id = c.content_id inner join collection cr on cr.content_id = ci.resource_content_id inner join content co on  co.content_id = cr.content_id inner join content cc on cc.content_id = c.content_id   where cc.gooru_oid =:gooruOid and cc.type_name =:collectionType and co.is_deleted=0 order by item_sequence) as collection left join (select c.class_id, c.class_uid, collection_id, cs.visibility, score_type_id  from class c  left join class_collection_settings cs  on c.class_id = cs.class_id inner join content co on co.content_id= collection_id where cs.class_id =:classUid and co.is_deleted=0) as class_setting on class_setting.collection_id = collection.collection_id";
	
	private static final String USERCLASS = "From UserClass u where u.partyUid=:partyUid and u.isDeleted=0";

	private static final String COURSE_COLLECTION_CLASSES = "select class_uid as classUid, class.class_id as classId, name, if(visibility = 1, 1, 0) as visibility  from (select class_uid, name, class_id from class c inner join user_group ug  on ug.user_group_uid = c.class_uid inner join party p on p.party_uid = ug.user_group_uid inner join  user on  created_by_uid = gooru_uid inner join content cc on cc.content_id = course_content_id  where p.is_deleted=0 and cc.gooru_oid =:courseId order by p.created_on desc) as class left join (select collection_id, visibility, class_id  from class_collection_settings  ccs inner join content cc on cc.content_id = ccs.collection_id   where cc.gooru_oid =:collectionId) as collection on collection.class_id = class.class_id";

	@Override
	public UserClass getClassById(String classUid) {
		Query query = getSession().createQuery(USERCLASS);
		query.setParameter(PARTY_UID, classUid);
		return (UserClass) ((query.list().size() > 0) ? query.list().get(0) : null);
	}

	@Override
	public List<Map<String, Object>> getClasses(int limit, int offset) {
		Query query = getSession().createSQLQuery(GET_CLASSES);
		query.setFirstResult(offset);
		query.setMaxResults(limit != 0 ? (limit > MAX_LIMIT ? MAX_LIMIT : limit) : LIMIT);
		query.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
		return list(query);
	}

	@Override
	public List<Map<String, Object>> getClasses(String gooruUid, boolean filterByEmptyCourse, int limit, int offset) {
		StringBuilder sql = new StringBuilder(GET_CLASSES);
		sql.append("and gooru_uid = :gooruUId ");
		if (filterByEmptyCourse) {
			sql.append(" and course_content_id is null ");
		}
		sql.append(" order by p.created_on desc");

		Query query = getSession().createSQLQuery(sql.toString());
		query.setParameter(GOORU_UID, gooruUid);
		query.setFirstResult(offset);
		query.setMaxResults(limit != 0 ? (limit > MAX_LIMIT ? MAX_LIMIT : limit) : LIMIT);
		query.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
		return list(query);
	}

	@Override
	public Map<String, Object> getClass(String classUid) {
		StringBuilder sql = new StringBuilder(GET_CLASSES);
		sql.append("and party_uid = :partyUid order by p.created_on desc");
		Query query = getSession().createSQLQuery(sql.toString());
		query.setParameter(PARTY_UID, classUid);
		query.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
		List<Map<String, Object>> results = list(query);
		return results.size() > 0 ? results.get(0) : null;
	}

	@Override
	public List<Map<String, Object>> getClassesByCourse(String courseGooruOid, int limit, int offset) {
		StringBuilder sql = new StringBuilder(GET_CLASSES);
		sql.append("and cc.gooru_oid = :gooruOid order by p.created_on desc");
		Query query = getSession().createSQLQuery(sql.toString());
		query.setParameter(GOORU_OID, courseGooruOid);
		query.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
		return list(query);
	}

	@Override
	public List<Map<String, Object>> getStudyClasses(String gooruUid, int limit, int offset) {
		Query query = getSession().createSQLQuery(GET_STUDY_CLASSES);
		query.setParameter(GOORU_UID, gooruUid);
		query.setFirstResult(offset);
		query.setMaxResults(limit != 0 ? (limit > MAX_LIMIT ? MAX_LIMIT : limit) : LIMIT);
		query.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
		return list(query);
	}

	@Override
	public List<Map<String, Object>> getMember(String classUid, int limit, int offset) {
		Query query = getSession().createSQLQuery(GET_MEMBERS);
		query.setParameter(CLASS_UID, classUid);
		query.setFirstResult(offset);
		query.setMaxResults(limit != 0 ? (limit > MAX_LIMIT ? MAX_LIMIT : limit) : LIMIT);
		query.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
		return list(query);
	}

	public void deleteUserFromClass(String classUid, String userUid) {
		Query query = getSession().createSQLQuery(DELETE_USER_FROM_CLASS);
		query.setParameter(GOORU_UID, userUid);
		query.setParameter(CLASS_UID, classUid);
		query.executeUpdate();
	}

	@Override
	public Map<String, Object> getClassByCode(String classCode) {
		StringBuilder sql = new StringBuilder(GET_CLASSES);
		sql.append("and user_group_code = :codeId order by p.created_on desc");
		Query query = getSession().createSQLQuery(sql.toString());
		query.setParameter(CODE_ID, classCode);
		query.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
		List<Map<String, Object>> results = list(query);
		return results.size() > 0 ? results.get(0) : null;
	}

	@Override
	public Integer getStudyClassesCount(String gooruUid) {
		Query query = getSession().createSQLQuery(GET_STUDY_CLASSES_COUNT).addScalar(COUNT, StandardBasicTypes.INTEGER);
		query.setParameter(GOORU_UID, gooruUid);
		List<Integer> result = list(query);
		return (result.size() > 0 ? result.get(0) : 0);
	}

	@Override
	public Integer getClassesCount(String gooruUid) {
		StringBuilder sql = new StringBuilder(GET_CLASSES_COUNT);
		if (gooruUid != null) {
			sql.append("and gooru_uid = :gooruUId ");
		}
		Query query = getSession().createSQLQuery(sql.toString()).addScalar(COUNT, StandardBasicTypes.INTEGER);
		if (gooruUid != null) {
			query.setParameter(GOORU_UID, gooruUid);
		}
		List<Integer> result = list(query);
		return (result.size() > 0 ? result.get(0) : 0);
	}

	@Override
	public Integer getMemeberCount(String classUid) {
		Query query = getSession().createSQLQuery(GET_MEMBERS_COUNT).addScalar(COUNT, StandardBasicTypes.INTEGER);
		query.setParameter(CLASS_UID, classUid);
		List<Integer> result = list(query);
		return (result.size() > 0 ? result.get(0) : 0);
	}

	@Override
	public List<Map<String, Object>> getCollectionItem(String gooruOid, int limit, int offset) {
		Query query = getSession().createSQLQuery(COLLECTION_ITEM);
		query.setParameter(GOORU_OID, gooruOid);
		query.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
		query.setFirstResult(offset);
		query.setMaxResults(limit != 0 ? (limit > MAX_LIMIT ? MAX_LIMIT : limit) : LIMIT);
		return list(query);
	}

	@Override
	public List<Map<String, Object>> getClassCollectionSettings(Long lessonId, String classUid) {
		Query query = getSession().createSQLQuery(COLLECTION_CLASS_SETTINGS);
		query.setParameter(LESSON_ID, lessonId);
		query.setParameter(CLASS_UID, classUid);
		query.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
		query.setFirstResult(0);
		query.setMaxResults(MAX_LIMIT);
		return list(query);
	}

	@Override
	public List<Map<String, Object>> getCollections(Long classUid, String gooruOid, int limit, int offset) {
		Query query = getSession().createSQLQuery(LESSON_COLLECTIONS);
		query.setParameter(GOORU_OID, gooruOid);
		query.setParameter(CLASS_UID, classUid);
		query.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
		query.setFirstResult(offset);
		query.setMaxResults(limit != 0 ? (limit > MAX_LIMIT ? MAX_LIMIT : limit) : LIMIT);
		return list(query);
	}

	@Override
	public List<Map<String, Object>> getCollectionItems(String gooruOid) {
		Query query = getSession().createSQLQuery(COLLECTION_ITEMS);
		query.setParameter(GOORU_OID, gooruOid);
		query.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
		query.setFirstResult(0);
		query.setMaxResults(MAX_LIMIT);
		return list(query);
	}

	@Override
	public List<String> getClassUid(String courseId) {
		Query query = getSession().createSQLQuery(GET_CLASS_UID);
		query.setParameter(COURSE_ID, courseId);
		return list(query);
	}

	@Override
	public void updateMemberCount(String classUid) {
		Query query = getSession().createSQLQuery(UPDATE_MEMBER_COUNT);
		query.setParameter(CLASS_UID, classUid);
		query.executeUpdate();
	}

	@Override
	public List<Map<String, Object>> getCourseData(Long classUid, String gooruOid, String collectionType) {
		Query query = getSession().createSQLQuery(COURSE_BY_CLASS).addScalar(VISIBILITY, StandardBasicTypes.BOOLEAN).addScalar(COLLECTION_ID).addScalar(GOORU_OID).addScalar(TITLE).addScalar(COLLECTION_TYPE);
		query.setParameter(COLLECTION_TYPE, collectionType);
		query.setParameter(CLASS_UID, classUid);
		query.setParameter(GOORU_OID, gooruOid);
		query.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
		return list(query);
	}

	@Override
	public List<Map<String, Object>> getClasses(String courseId, String collectionId,  int limit, int offset) {
		Query query = getSession().createSQLQuery(COURSE_COLLECTION_CLASSES).addScalar(VISIBILITY, StandardBasicTypes.BOOLEAN).addScalar(CLASS_UID).addScalar(CLASS_ID).addScalar(NAME);
		query.setParameter(COURSE_ID, courseId);
		query.setParameter(COLLECTION_ID, collectionId);
		query.setFirstResult(offset);
		query.setMaxResults(limit != 0 ? (limit > MAX_LIMIT ? MAX_LIMIT : limit) : LIMIT);
		query.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
		return list(query);
	}
	
	
	@Override
	public int getVisibilitySettings(Long collectionId, Long classIds){
		Query query = getSession().createSQLQuery("select if(count(*)>0,1,0) count FROM class_collection_settings cs join class c on cs.class_id=c.class_id where course_content_id=:courseId and collection_id =:collectionId and cs.visibility =1").addScalar(COUNT, StandardBasicTypes.INTEGER);
		query.setParameter(COLLECTION_ID, collectionId);
		query.setParameter(COURSE_ID, classIds);
		return (int) list(query).get(0);
	}
	
	@Override
	public void updateCollectionVisibility(Long collectionId){
		Query query = getSession().createSQLQuery("update class_collection_settings set visibility=0 where collection_id=:collectionId");
		query.setParameter(COLLECTION_ID, collectionId);
		query.executeUpdate();
	}

}
