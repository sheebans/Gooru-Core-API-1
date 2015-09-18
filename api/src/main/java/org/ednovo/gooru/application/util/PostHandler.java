package org.ednovo.gooru.application.util;

import java.util.Map;

import org.ednovo.gooru.core.api.model.Content;
import org.ednovo.gooru.core.api.model.SessionContextSupport;
import org.ednovo.gooru.core.constant.ParameterProperties;
import org.ednovo.gooru.domain.component.CollectionDeleteProcessor;
import org.ednovo.gooru.domain.service.collection.CollectionBoService;
import org.ednovo.gooru.infrastructure.messenger.IndexProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PostHandler implements ParameterProperties {

	@Autowired
	private IndexProcessor indexProcessor;

	@Autowired
	private CollectionDeleteProcessor collectionDeleteProcessor;

	@Autowired
	private CollectionBoService collectionBoService;

	private static final Logger LOGGER = LoggerFactory.getLogger(PostHandler.class);

	public void initialize() {
		searchIndex();
		deleteCUL();
		moveCollectionToFolder();
	}

	private void searchIndex() {
		// Read re-index request from session context and sent re-index request
		// via Java HTTP client to index server
		try {
			indexProcessor.index(SessionContextSupport.getIndexMeta());
		} catch (Exception ex) {
			LOGGER.error("Re-index API trigger failed " + ex);
		}
	}

	private void deleteCUL() {
		try {
			Content content = SessionContextSupport.getDeleteContentMeta();
			if (content != null) {
				getCollectionDeleteProcessor().deleteContent(content.getGooruOid(), content.getContentType().getName());
			}
		} catch (Exception ex) {
			LOGGER.error("Bulk content(CULCA) sub items deletion failed ", ex);
		}
	}

	private void moveCollectionToFolder() {
		try {
			Map<String, Object> data = SessionContextSupport.getMoveContentMeta();
			if (data != null && data.size() > 0) {
				String folderSourceId = String.valueOf(data.get(SOURCE_ID));
				String gooruUid = String.valueOf(data.get(USER_UID));
				if (folderSourceId != null) {
					getCollectionBoService().updateFolderSharing(folderSourceId);
					getCollectionBoService().resetFolderVisibility(folderSourceId, gooruUid);
				}
				String folderTargetId = String.valueOf(data.get(TARGET_ID));
				if (folderTargetId != null) {
					getCollectionBoService().updateFolderSharing(folderTargetId);
					getCollectionBoService().resetFolderVisibility(folderTargetId, gooruUid);
				}
			}
		} catch (Exception ex) {
			LOGGER.error("Reset  sharing settings ", ex);
		}
	}

	public IndexProcessor getIndexProcessor() {
		return indexProcessor;
	}

	public CollectionDeleteProcessor getCollectionDeleteProcessor() {
		return collectionDeleteProcessor;
	}

	public CollectionBoService getCollectionBoService() {
		return collectionBoService;
	}

}
