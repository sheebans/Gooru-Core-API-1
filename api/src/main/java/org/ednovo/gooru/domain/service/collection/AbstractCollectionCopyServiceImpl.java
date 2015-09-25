package org.ednovo.gooru.domain.service.collection;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.ednovo.gooru.application.util.GooruImageUtil;
import org.ednovo.gooru.core.api.model.AssessmentQuestion;
import org.ednovo.gooru.core.api.model.Collection;
import org.ednovo.gooru.core.api.model.CollectionItem;
import org.ednovo.gooru.core.api.model.ContentClassification;
import org.ednovo.gooru.core.api.model.ContentMeta;
import org.ednovo.gooru.core.api.model.ContentMetaAssociation;
import org.ednovo.gooru.core.api.model.ContentTaxonomyCourseAssoc;
import org.ednovo.gooru.core.api.model.Sharing;
import org.ednovo.gooru.core.api.model.TaxonomyCourse;
import org.ednovo.gooru.core.api.model.User;
import org.ednovo.gooru.core.constant.ConstantProperties;
import org.ednovo.gooru.core.constant.ParameterProperties;
import org.ednovo.gooru.infrastructure.messenger.IndexHandler;
import org.ednovo.gooru.infrastructure.persistence.hibernate.CollectionDao;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractCollectionCopyServiceImpl extends AbstractResourceServiceImpl implements AbstractCollectionCopyService, ParameterProperties, ConstantProperties {

	@Autowired
	private CollectionDao collectionDao;

	@Autowired
	private GooruImageUtil gooruImageUtil;

	@Autowired
	private QuestionService questionService;

	@Autowired
	private IndexHandler indexHandler;

	protected void copyCollectionItems(Collection lesson, Collection sourceCollection, Collection destCollection, User user) {
		List<CollectionItem> collectionItems = this.getCollectionDao().getCollectionItems(sourceCollection.getGooruOid(), false);
		for (CollectionItem sourceCollectionItem : collectionItems) {
			final CollectionItem destCollectionItem = new CollectionItem();
			if (sourceCollectionItem.getContent().getContentType().getName().equalsIgnoreCase(QUESTION)) {
				final AssessmentQuestion assessmentQuestion = this.getQuestionService().copyQuestion(sourceCollectionItem.getContent().getGooruOid(), user);
				destCollectionItem.setContent(assessmentQuestion);
			} else {
				destCollectionItem.setContent(sourceCollectionItem.getContent());
			}
			destCollectionItem.setItemType(sourceCollectionItem.getItemType());
			destCollectionItem.setItemSequence(sourceCollectionItem.getItemSequence());
			destCollectionItem.setNarration(sourceCollectionItem.getNarration());
			destCollectionItem.setNarrationType(sourceCollectionItem.getNarrationType());
			destCollectionItem.setStart(sourceCollectionItem.getStart());
			destCollectionItem.setAssociatedUser(user);
			destCollectionItem.setStop(sourceCollectionItem.getStop());
			destCollectionItem.setAssociationDate(new Date(System.currentTimeMillis()));
			destCollectionItem.setCollection(destCollection);
			this.getCollectionDao().save(destCollectionItem);
		}

	}

	protected void copyContentClassification(Long sourceContentId, Collection desCollection) {
		List<ContentClassification> contentClassifications = this.getContentClassificationRepository().getContentClassification(sourceContentId);
		for (ContentClassification contentClassification : contentClassifications) {
			ContentClassification newContentClassification = new ContentClassification();
			newContentClassification.setCode(contentClassification.getCode());
			newContentClassification.setContent(desCollection);
			newContentClassification.setTypeId(contentClassification.getTypeId());
			this.getContentRepository().save(newContentClassification);
		}
	}

	protected void copyContentMetaAssoc(Long sourceContentId, Collection destCollection) {
		List<ContentMetaAssociation> contentMetaAssocs = this.getContentRepository().getContentMetaAssoc(sourceContentId);
		for (ContentMetaAssociation contentMetaAssoc : contentMetaAssocs) {
			ContentMetaAssociation newContentMetaAssoc = new ContentMetaAssociation();
			newContentMetaAssoc.setContent(destCollection);
			newContentMetaAssoc.setCreatedOn(new Date(System.currentTimeMillis()));
			newContentMetaAssoc.setTypeId(contentMetaAssoc.getTypeId());
			newContentMetaAssoc.setUser(destCollection.getUser());
			this.getCollectionDao().save(newContentMetaAssoc);
		}
	}

	protected void copyCollectionRepoStorage(Collection sourceCollection, Collection destCollection) {
		StringBuilder sourceFilepath = new StringBuilder(sourceCollection.getOrganization().getNfsStorageArea().getInternalPath());
		sourceFilepath.append(sourceCollection.getImagePath()).append(File.separator);
		StringBuilder targetFilepath = new StringBuilder(destCollection.getOrganization().getNfsStorageArea().getInternalPath());
		targetFilepath.append(destCollection.getImagePath()).append(File.separator);
		getAsyncExecutor().copyResourceFolder(sourceFilepath.toString(), targetFilepath.toString());
	}

	protected Collection collectionCopy(Collection sourceCollection, Collection targetCollection, User user, Collection newCollection) {
		Collection destCollection = new Collection();
		if (newCollection.getTitle() != null) {
			destCollection.setTitle(newCollection.getTitle());
		} else {
			destCollection.setTitle(sourceCollection.getTitle());
		}
		destCollection.setCopiedCollectionId(sourceCollection.getGooruOid());
		destCollection.setCollectionType(sourceCollection.getCollectionType());
		destCollection.setDescription(sourceCollection.getDescription());
		destCollection.setNotes(sourceCollection.getNotes());
		destCollection.setLanguage(sourceCollection.getLanguage());
		destCollection.setImagePath(sourceCollection.getImagePath());
		destCollection.setGooruOid(UUID.randomUUID().toString());
		destCollection.setContentType(sourceCollection.getContentType());
		destCollection.setLastModified(new Date(System.currentTimeMillis()));
		destCollection.setCreatedOn(new Date(System.currentTimeMillis()));
		destCollection.setIsRepresentative(0);
		if (newCollection != null && newCollection.getSharing() != null) {
			destCollection.setSharing(newCollection.getSharing());
		} else {
			destCollection.setSharing(sourceCollection.getSharing().equalsIgnoreCase(Sharing.PUBLIC.getSharing()) ? Sharing.ANYONEWITHLINK.getSharing() : sourceCollection.getSharing());
		}
		destCollection.setUser(user);
		destCollection.setOrganization(sourceCollection.getOrganization());
		destCollection.setCreator(sourceCollection.getCreator());
		destCollection.setUrl(sourceCollection.getUrl());
		this.getCollectionDao().save(destCollection);
		// copy resource and question items to collection
		copyCollectionItems(targetCollection, sourceCollection, destCollection, user);
		copyContentTaxonomyCourse(sourceCollection.getContentId(), destCollection);
		copyContentMetaAssoc(sourceCollection.getContentId(), destCollection);
		copyContentClassification(sourceCollection.getContentId(), destCollection);
		copyCollectionRepoStorage(sourceCollection, destCollection);
		// copy content meta details
		ContentMeta contentMeta = this.getContentRepository().getContentMeta(sourceCollection.getContentId());
		ContentMeta newContentMeta = new ContentMeta();
		if (contentMeta != null) {
			newContentMeta.setContent(destCollection);
			newContentMeta.setMetaData(contentMeta.getMetaData());
			this.getContentRepository().save(newContentMeta);
		}
		// associating the copied collection to lesson
		CollectionItem newCollectionItem = new CollectionItem();
		newCollectionItem.setItemType(ADDED);
		createCollectionItem(newCollectionItem, targetCollection, destCollection, user);
		return destCollection;
	}

	protected void copyContentTaxonomyCourse(Long sourceContentId, Collection desCollection) {
		List<ContentTaxonomyCourseAssoc> taxonomyCourses = this.getTaxonomyCourseRepository().getContentTaxonomyCourseAssoc(desCollection.getContentId());
		if (taxonomyCourses != null && taxonomyCourses.size() > 0) {
			List<ContentTaxonomyCourseAssoc> contentTaxonomyCourseAssocs = new ArrayList<ContentTaxonomyCourseAssoc>();
			for (ContentTaxonomyCourseAssoc taxonomyCourse : taxonomyCourses) {
				ContentTaxonomyCourseAssoc contentTaxonomyCourseAssoc = new ContentTaxonomyCourseAssoc();
				contentTaxonomyCourseAssoc.setContent(desCollection);
				contentTaxonomyCourseAssoc.setTaxonomyCourse(taxonomyCourse.getTaxonomyCourse());
				contentTaxonomyCourseAssocs.add(contentTaxonomyCourseAssoc);
			}
			this.getTaxonomyCourseRepository().saveAll(contentTaxonomyCourseAssocs);
		}
	}

	public CollectionDao getCollectionDao() {
		return collectionDao;
	}

	public GooruImageUtil getGooruImageUtil() {
		return gooruImageUtil;
	}

	public QuestionService getQuestionService() {
		return questionService;
	}

	public IndexHandler getIndexHandler() {
		return indexHandler;
	}
}
