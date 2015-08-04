package org.ednovo.gooru.domain.service.eventlogs;

import org.ednovo.gooru.core.api.model.SessionContextSupport;
import org.ednovo.gooru.core.constant.ConstantProperties;
import org.ednovo.gooru.core.constant.ParameterProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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
}
