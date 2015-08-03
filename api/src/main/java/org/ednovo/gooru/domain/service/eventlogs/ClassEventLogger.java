package org.ednovo.gooru.domain.service.eventlogs;

import org.ednovo.gooru.core.api.model.SessionContextSupport;
import org.ednovo.gooru.core.constant.ConstantProperties;
import org.ednovo.gooru.core.constant.ParameterProperties;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassEventLogger {

	public static final Logger CLASS_EVENT_LOGGER = LoggerFactory.getLogger(ClassEventLogger.class);
	
	public static void memberRemoveLog(String organizationUid, String classUid, String studentUid) {
		
		try {
			SessionContextSupport.getLog().put(ConstantProperties.EVENT_NAME, ParameterProperties.CLASS_USER_REMOVE);
			JSONObject contextLog = SessionContextSupport.getLog().get(ParameterProperties.CONTEXT) != null ? new JSONObject(SessionContextSupport.getLog().get(ParameterProperties.CONTEXT).toString()) : new JSONObject();
			contextLog.put(ParameterProperties.CONTENT_GOORU_ID, classUid);
			SessionContextSupport.getLog().put(ParameterProperties.CONTEXT, contextLog.toString());
			if(organizationUid != null){
				JSONObject sessionLog = SessionContextSupport.getLog().get(ParameterProperties.SESSION) != null ? new JSONObject(SessionContextSupport.getLog().get(ParameterProperties.SESSION).toString()) : new JSONObject();
				sessionLog.put(ConstantProperties.ORGANIZATION_UID, organizationUid);
				SessionContextSupport.getLog().put(ParameterProperties.SESSION, sessionLog.toString());
			}
			JSONObject payLoadLog = SessionContextSupport.getLog().get(ConstantProperties.PAY_LOAD_OBJECT) != null ? new JSONObject(SessionContextSupport.getLog().get(ConstantProperties.PAY_LOAD_OBJECT).toString()) : new JSONObject();
			payLoadLog.put(ConstantProperties.REMOVE_GOORU_UID, studentUid);
			SessionContextSupport.getLog().put(ConstantProperties.PAY_LOAD_OBJECT, payLoadLog.toString());
		} catch (Exception e) {
			CLASS_EVENT_LOGGER.error(ParameterProperties._ERROR, e);
		}
	}
}
