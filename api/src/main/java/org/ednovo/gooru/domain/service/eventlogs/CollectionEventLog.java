package org.ednovo.gooru.domain.service.eventlogs;

import java.util.List;

import net.sf.json.JSONArray;

import org.ednovo.gooru.core.api.model.Collection;
import org.ednovo.gooru.core.api.model.CollectionItem;
import org.ednovo.gooru.core.api.model.CollectionType;
import org.ednovo.gooru.core.api.model.ResourceType;
import org.ednovo.gooru.core.api.model.SessionContextSupport;
import org.ednovo.gooru.core.api.model.User;
import org.ednovo.gooru.infrastructure.persistence.hibernate.ClassRepository;
import org.ednovo.gooru.infrastructure.persistence.hibernate.CollectionDao;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CollectionEventLog extends EventLog {

	@Autowired
	private ClassRepository classRepository;
	
	@Autowired
	private CollectionDao collectionDao;

	public void collectionEventLog(String courseId, String unitId, String lessonId, CollectionItem collection, User user, Collection data, String action) {
		try {
			String collectionType = collection.getContent().getContentType().getName();
			JSONObject context = SessionContextSupport.getLog().get(CONTEXT) != null ? new JSONObject(SessionContextSupport.getLog().get(CONTEXT).toString()) : new JSONObject();
			context.put(CONTENT_GOORU_ID, collection.getContent().getGooruOid());
			context.put(PARENT_GOORU_ID, lessonId);
			SessionContextSupport.putLogParameter(CONTEXT, context.toString());
			JSONObject payLoadObject = SessionContextSupport.getLog().get(PAY_LOAD_OBJECT) != null ? new JSONObject(SessionContextSupport.getLog().get(PAY_LOAD_OBJECT).toString()) : new JSONObject();
			payLoadObject.put(COURSE_GOORU_ID, courseId);
			payLoadObject.put(UNIT_GOORU_ID, unitId);
			payLoadObject.put(LESSON_GOORU_ID, lessonId);
			List<String> classUids = this.getClassRepository().getClassUid(courseId);
			if (!classUids.isEmpty()) {
				JSONArray newArray = new JSONArray();
				newArray.addAll(classUids);
				payLoadObject.put(CLASS_GOORU_IDS, newArray);
				SessionContextSupport.putLogParameter(EVENT_NAME, action.equalsIgnoreCase(CREATE)? ITEM_CREATE:CLASS_ITEM_DELETE);
			} else {
				SessionContextSupport.putLogParameter(EVENT_NAME, action.equalsIgnoreCase(CREATE)? ITEM_CREATE:ITEM_DELETE);
			}
			
			if(action.equalsIgnoreCase(CREATE)){
				payLoadObject.put(MODE, CREATE);
				payLoadObject.put(DATA, data);
				payLoadObject.put(ITEM_SEQUENCE,collection.getItemSequence());
				payLoadObject.put(ITEM_ID,collection.getCollectionItemId());
			}
			else{
				payLoadObject.put(MODE, DELETE);
				payLoadObject.put(ITEM_SEQUENCE,collection.getItemSequence());
			}
			if (collectionType.equalsIgnoreCase(CollectionType.ASSESSMENT.getCollectionType())) {
				payLoadObject.put(TYPE, ASSESSMENT);
				payLoadObject.put(ITEM_TYPE, (!classUids.isEmpty())? CLASS_COURSE_ASSESSMENT:SHELF_COURSE_ASSESSMENT);
			} else if (collectionType.equalsIgnoreCase(CollectionType.COLLECTION.getCollectionType())) {
				payLoadObject.put(TYPE, COLLECTION);
				payLoadObject.put(ITEM_TYPE, (!classUids.isEmpty())? CLASS_COURSE_COLLECTION:SHELF_COURSE_COLLECTION);
			} else if(collectionType.equalsIgnoreCase(ResourceType.Type.ASSESSMENT_URL.getType())){
				payLoadObject.put(TYPE, ASSESSMENT_URL);
				payLoadObject.put(ITEM_TYPE, (!classUids.isEmpty())? CLASS_COURSE_ASSESSMENT_URL:SHELF_COURSE_ASSESSMENT_URL);
			}
			
			SessionContextSupport.putLogParameter(PAY_LOAD_OBJECT, payLoadObject.toString());
			JSONObject session = SessionContextSupport.getLog().get(SESSION) != null ? new JSONObject(SessionContextSupport.getLog().get(SESSION).toString()) : new JSONObject();
			session.put(ORGANIZATION_UID, user != null && user.getOrganization() != null ? user.getOrganization().getPartyUid() : null);
			SessionContextSupport.putLogParameter(SESSION, session.toString());
		} catch (Exception e) {
			LOGGER.error(_ERROR, e);
		}
	}

	public void collectionItemEventLog(String collectionId, CollectionItem resource, String userUid, String contentType, Object data, String action) {
		try {
			JSONObject context = SessionContextSupport.getLog().get(CONTEXT) != null ? new JSONObject(SessionContextSupport.getLog().get(CONTEXT).toString()) : new JSONObject();
			context.put(CONTENT_GOORU_ID, resource.getContent().getGooruOid());
			context.put(PARENT_GOORU_ID, collectionId);
			SessionContextSupport.putLogParameter(CONTEXT, context.toString());
			JSONObject payLoadObject = SessionContextSupport.getLog().get(PAY_LOAD_OBJECT) != null ? new JSONObject(SessionContextSupport.getLog().get(PAY_LOAD_OBJECT).toString()) : new JSONObject();
			if (contentType.equalsIgnoreCase(QUESTION)) {
				payLoadObject.put(TYPE, QUESTION);
				payLoadObject.put(ITEM_TYPE, SHELF_COURSE_QUESTION);
			} else {
				payLoadObject.put(TYPE, RESOURCE);
				payLoadObject.put(ITEM_TYPE, SHELF_COURSE_RESOURCE);
			}
			if(action.equalsIgnoreCase(CREATE)){
				payLoadObject.put(MODE, CREATE);
				payLoadObject.put(DATA, data);
				SessionContextSupport.putLogParameter(EVENT_NAME, ITEM_CREATE);
			} else if (action.equalsIgnoreCase(ADD)) {
				payLoadObject.put(MODE, ADD);
				payLoadObject.put(DATA, data);
				SessionContextSupport.putLogParameter(EVENT_NAME, ITEM_CREATE);
			}
			else{
				SessionContextSupport.putLogParameter(EVENT_NAME, ITEM_DELETE);
				payLoadObject.put(MODE, DELETE);
			}
			payLoadObject.put(PARENT_SHARING,resource.getCollection().getSharing());
			payLoadObject.put(ITEM_SEQUENCE,resource.getItemSequence());
			payLoadObject.put(ITEM_ID,resource.getCollectionItemId());
			SessionContextSupport.putLogParameter(PAY_LOAD_OBJECT, payLoadObject.toString());
			JSONObject session = SessionContextSupport.getLog().get(SESSION) != null ? new JSONObject(SessionContextSupport.getLog().get(SESSION).toString()) : new JSONObject();
			session.put(ORGANIZATION_UID, userUid != null);
			SessionContextSupport.putLogParameter(SESSION, session.toString());
		} catch (Exception e) {
			LOGGER.error(_ERROR, e);
		}
	}
	



	 public void getMoveEventLog(String courseId, String unitId, String lessonId, Collection collection, User user, String collectionType) {
	            try {
	            JSONObject context = SessionContextSupport.getLog().get(CONTEXT) != null ? new JSONObject(SessionContextSupport.getLog().get(CONTEXT).toString()) : new JSONObject();
	           context.put(CONTENT_GOORU_ID, collection.getGooruOid());
	           context.put(PARENT_GOORU_ID, lessonId);
	           SessionContextSupport.putLogParameter(CONTEXT, context.toString());
	           JSONObject payLoadObject = SessionContextSupport.getLog().get(PAY_LOAD_OBJECT) != null ? new JSONObject(SessionContextSupport.getLog().get(PAY_LOAD_OBJECT).toString()) : new JSONObject();
	           payLoadObject.put(MODE, MOVE);
	           JSONObject session = SessionContextSupport.getLog().get(SESSION) != null ? new JSONObject(SessionContextSupport.getLog().get(SESSION).toString()) : new JSONObject();
	           session.put(ORGANIZATION_UID, user != null && user.getOrganization() != null ? user.getOrganization().getPartyUid() : null);
	           SessionContextSupport.putLogParameter(SESSION, session.toString());
	           CollectionItem lesson = this.getCollectionDao().getParentCollection(collection.getContentId());
	           payLoadObject.put(LESSON_GOORU_ID, lesson.getCollection().getGooruOid());
	           CollectionItem unit = this.getCollectionDao().getParentCollection(lesson.getCollection().getContentId());
	           payLoadObject.put(UNIT_GOORU_ID, unit.getCollection().getGooruOid());
	           CollectionItem course = this.getCollectionDao().getParentCollection(unit.getCollection().getContentId());
	           payLoadObject.put(COURSE_GOORU_ID, course.getCollection().getGooruOid());
	           List<String> classUids = this.getClassRepository().getClassUid(courseId);
	                   JSONArray newArray = new JSONArray();
	                   newArray.addAll(classUids);
	                   if (!classUids.isEmpty()) {
	                           payLoadObject.put(CLASS_GOORU_IDS, newArray);
	                                SessionContextSupport.putLogParameter(EVENT_NAME, CLASS_ITEM_MOVE);
	                        } else {
	                                payLoadObject.remove(CLASS_GOORU_IDS);
	                                SessionContextSupport.putLogParameter(EVENT_NAME, ITEM_CREATE);
	                        }
	                   if (!classUids.isEmpty() && collectionType.equalsIgnoreCase(CollectionType.ASSESSMENT.getCollectionType())) {
	                                payLoadObject.put(ITEM_TYPE, CLASS_COURSE_ASSESSMENT);
	                                payLoadObject.put(TYPE, ASSESSMENT);
	                        } else if (collectionType.equalsIgnoreCase(CollectionType.ASSESSMENT.getCollectionType())) {
	                                payLoadObject.put(ITEM_TYPE, SHELF_COURSE_ASSESSMENT);
	                                payLoadObject.put(TYPE, ASSESSMENT);
	                        }
	                        if (!classUids.isEmpty() && collectionType.equalsIgnoreCase(CollectionType.COLLECTION.getCollectionType())) {
	                                payLoadObject.put(ITEM_TYPE, CLASS_COURSE_COLLECTION);
	                                payLoadObject.put(TYPE, COLLECTION);
	                        } else if (collectionType.equalsIgnoreCase(CollectionType.COLLECTION.getCollectionType())) {
	                                payLoadObject.put(ITEM_TYPE, SHELF_COURSE_COLLECTION);
	                                payLoadObject.put(TYPE, COLLECTION);
	                        }
	                        if (!classUids.isEmpty() && collectionType.equalsIgnoreCase(ResourceType.Type.ASSESSMENT_URL.getType())) {
	                                payLoadObject.put(ITEM_TYPE, CLASS_COURSE_ASSESSMENT_URL);
	                                payLoadObject.put(TYPE, ASSESSMENT_URL);
	                        } else if (collectionType.equalsIgnoreCase(ResourceType.Type.ASSESSMENT_URL.getType())) {
	                                payLoadObject.put(ITEM_TYPE, SHELF_COURSE_ASSESSMENT_URL);
	                                payLoadObject.put(TYPE, ASSESSMENT_URL);
	                        }
	           payLoadObject.put(TARGET_GOORU_ID, lessonId );
	           SessionContextSupport.putLogParameter(PAY_LOAD_OBJECT, payLoadObject.toString());
	           } catch (Exception e) {
	                   LOGGER.error(_ERROR, e);
	           }
	   }

	public CollectionDao getCollectionDao() {
		return collectionDao;
	}

	public ClassRepository getClassRepository() {
		return classRepository;
	}

}
