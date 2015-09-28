package org.ednovo.gooru.domain.service.eventlogs;

import java.util.List;

import org.ednovo.gooru.core.api.model.ClassCollectionSettings;
import org.ednovo.gooru.core.api.model.SessionContextSupport;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import flexjson.JSONSerializer;

@Component
public class ClassEventLogger extends EventLog{

	public static final Logger LOGGER = LoggerFactory.getLogger(ClassEventLogger.class);
	
	public void memberRemoveLog(String organizationUid, String classUid, String userUid) {
		
		try {
			SessionContextSupport.getLog().put(EVENT_NAME, CLASS_USER_REMOVE);
			putValue(CONTEXT, CONTENT_GOORU_ID, classUid);
			putValue(PAY_LOAD_OBJECT, REMOVE_GOORU_UID, userUid);
		} catch (Exception e) {
			LOGGER.error(_ERROR, e);
		}
	}
	
	public void memberJoinLog(String classUid) {
		
		try {
			SessionContextSupport.getLog().put(EVENT_NAME, CLASS_USER_ADD);
			putValue(CONTEXT, CONTENT_GOORU_ID, classUid);
		} catch (Exception e) {
			LOGGER.error(_ERROR, e);
		}
	}
	
	
	/**
	 * Class Delete Event Log
	 * @param classUid deleted classUId
	 */
	public void deleteClass(String classUid) {
		
		try {
			putValue(EVENT_NAME, ITEM_DELETE);
			putValue(CONTEXT, CONTENT_GOORU_ID, classUid);
			JSONObject payLoadObject = getLogParameter(PAY_LOAD_OBJECT);
			putValue(payLoadObject, MODE, DELETE);
			putValue(payLoadObject, TYPE, CLASS);
			putValue(payLoadObject, ITEM_TYPE, CLASS);
			putEntity(PAY_LOAD_OBJECT, payLoadObject);
		} catch (Exception e) {
			LOGGER.error(_ERROR, e);
		}
	}
	
	/**
	 * Class content settings Logs
	 * @param classUid
	 */
	public void classContentVisibilty(String classUid, List<ClassCollectionSettings> classCollectionSetting) {
		
		try {
			putValue(EVENT_NAME, ITEM_EDIT);
			putValue(CONTEXT, CONTENT_GOORU_ID, classUid);
			JSONObject payLoadObject = getLogParameter(PAY_LOAD_OBJECT);
			putValue(payLoadObject, MODE, VISIBILITY);
			putValue(payLoadObject, TYPE, CLASS);
			putValue(payLoadObject, ITEM_TYPE, CLASS);
			putValue(payLoadObject, CONTENT, new JSONSerializer().exclude(EXCLUDE_CLASS).deepSerialize(classCollectionSetting));
			putEntity(PAY_LOAD_OBJECT, payLoadObject);
		} catch (Exception e) {
			LOGGER.error(_ERROR, e);
		}
	}
}
