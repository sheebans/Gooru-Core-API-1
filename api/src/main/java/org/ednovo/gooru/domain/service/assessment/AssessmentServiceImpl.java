/////////////////////////////////////////////////////////////
// AssessmentServiceImpl.java
// gooru-api
// Created by Gooru on 2014
// Copyright (c) 2014 Gooru. All rights reserved.
// http://www.goorulearning.org/
// Permission is hereby granted, free of charge, to any person obtaining
// a copy of this software and associated documentation files (the
// "Software"), to deal in the Software without restriction, including
// without limitation the rights to use, copy, modify, merge, publish,
// distribute, sublicense, and/or sell copies of the Software, and to
// permit persons to whom the Software is furnished to do so, subject to
// the following conditions:
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
// LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
// OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
// WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
/////////////////////////////////////////////////////////////
package org.ednovo.gooru.domain.service.assessment;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.ednovo.gooru.application.util.AsyncExecutor;
import org.ednovo.gooru.application.util.CollectionUtil;
import org.ednovo.gooru.application.util.ResourceImageUtil;
import org.ednovo.gooru.core.api.model.ActionResponseDTO;
import org.ednovo.gooru.core.api.model.AssessmentAnswer;
import org.ednovo.gooru.core.api.model.AssessmentHint;
import org.ednovo.gooru.core.api.model.AssessmentQuestion;
import org.ednovo.gooru.core.api.model.AssessmentQuestionAssetAssoc;
import org.ednovo.gooru.core.api.model.Asset;
import org.ednovo.gooru.core.api.model.Code;
import org.ednovo.gooru.core.api.model.ContentMetaDTO;
import org.ednovo.gooru.core.api.model.ContentType;
import org.ednovo.gooru.core.api.model.License;
import org.ednovo.gooru.core.api.model.Resource;
import org.ednovo.gooru.core.api.model.ResourceSource;
import org.ednovo.gooru.core.api.model.ResourceType;
import org.ednovo.gooru.core.api.model.User;
import org.ednovo.gooru.core.application.util.ErrorMessage;
import org.ednovo.gooru.core.application.util.ServerValidationUtils;
import org.ednovo.gooru.core.constant.ConstantProperties;
import org.ednovo.gooru.core.constant.ParameterProperties;
import org.ednovo.gooru.core.exception.BadRequestException;
import org.ednovo.gooru.core.exception.NotFoundException;
import org.ednovo.gooru.domain.service.CollectionService;
import org.ednovo.gooru.domain.service.content.ContentService;
import org.ednovo.gooru.domain.service.resource.AssetManager;
import org.ednovo.gooru.domain.service.resource.ResourceManager;
import org.ednovo.gooru.domain.service.resource.ResourceService;
import org.ednovo.gooru.domain.service.storage.S3ResourceApiHandler;
import org.ednovo.gooru.domain.service.taxonomy.TaxonomyService;
import org.ednovo.gooru.domain.service.user.UserService;
import org.ednovo.gooru.infrastructure.messenger.IndexHandler;
import org.ednovo.gooru.infrastructure.messenger.IndexProcessor;
import org.ednovo.gooru.infrastructure.persistence.hibernate.BaseRepository;
import org.ednovo.gooru.infrastructure.persistence.hibernate.UserRepository;
import org.ednovo.gooru.infrastructure.persistence.hibernate.assessment.AssessmentRepository;
import org.ednovo.gooru.infrastructure.persistence.hibernate.content.ContentRepository;
import org.ednovo.gooru.infrastructure.persistence.hibernate.customTable.CustomTableRepository;
import org.ednovo.gooru.infrastructure.persistence.hibernate.resource.ResourceRepository;
import org.ednovo.gooru.infrastructure.persistence.hibernate.taxonomy.TaxonomyRespository;
import org.ednovo.gooru.infrastructure.persistence.hibernate.taxonomy.TaxonomyStoredProcedure;
import org.ednovo.gooru.mongodb.assessments.questions.services.MongoQuestionsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;

@Service
public class AssessmentServiceImpl implements ConstantProperties, AssessmentService, ParameterProperties {

	@Autowired
	private AssessmentRepository assessmentRepository;

	@Autowired
	private BaseRepository baseRepository;

	@Autowired
	private ResourceRepository resourceRepository;

	@Autowired
	ResourceImageUtil resourceImageUtil;

	@Autowired
	private TaxonomyStoredProcedure procedureExecutor;

	@Autowired
	private TaxonomyRespository taxonomyRepository;

	@Autowired
	private TaxonomyService taxonomyService;

	@Autowired
	@javax.annotation.Resource(name = "assetManager")
	private AssetManager assetManager;

	@Autowired
	private S3ResourceApiHandler s3ResourceApiHandler;

	@Autowired
	private IndexProcessor indexProcessor;

	private static final Logger LOGGER = LoggerFactory.getLogger(AssessmentServiceImpl.class);

	@Autowired
	private ContentRepository contentRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	@javax.annotation.Resource(name = "resourceManager")
	private ResourceManager resourceManager;

	@Autowired
	private ContentService contentService;

	@Autowired
	private UserService userService;

	@Autowired
	private CustomTableRepository customTableRepository;

	@Autowired
	private AsyncExecutor asyncExecutor;

	@Autowired
	private CollectionService collectionService;

	@Autowired
	private ResourceService resourceService;

	@Autowired
	private IndexHandler indexHandler;

	@Autowired
	private MongoQuestionsService mongoQuestionsService;

	@Override
	public AssessmentQuestion getQuestion(String gooruOQuestionId) {
		return (AssessmentQuestion) assessmentRepository.getByGooruOId(AssessmentQuestion.class, gooruOQuestionId);
	}

	@Override
	public ActionResponseDTO<AssessmentQuestion> createQuestion(AssessmentQuestion question, boolean index) throws Exception {
		Set<Code> taxonomy = question.getTaxonomySet();
		question = initQuestion(question, null, true);
		question.setIsOer(1);
		question.setTaxonomySet(null);
		Errors errors = validateQuestion(question);
		if (!errors.hasErrors()) {
			// To Save Folder
			question.setOrganization(question.getCreator().getOrganization());
			assessmentRepository.save(question);
			resourceService.saveOrUpdateResourceTaxonomy(question, taxonomy);
			if (question.getResourceInfo() != null) {
				resourceRepository.save(question.getResourceInfo());
			}
			s3ResourceApiHandler.updateOrganization(question);

			if (index) {
				try {
					indexHandler.setReIndexRequest(question.getGooruOid(), IndexProcessor.INDEX, RESOURCE, null, false, false);
				} catch (Exception e) {
					LOGGER.info(e.getMessage());
				}
			}
		}
		return new ActionResponseDTO<AssessmentQuestion>(question, errors);
	}

	private AssessmentQuestion initQuestion(AssessmentQuestion question, String gooruOQuestionId, boolean copyToOriginal) {
		if (copyToOriginal) {
			if (gooruOQuestionId == null) {
				License license = (License) baseRepository.get(License.class, CREATIVE_COMMONS);
				question.setLicense(license);
				ContentType contentType = (ContentType) baseRepository.get(ContentType.class, ContentType.QUESTION);
				question.setContentType(contentType);
				if (question.getGooruOid() == null) {
					question.setGooruOid(UUID.randomUUID().toString());
				}
				ServerValidationUtils.rejectIfNull(question.getQuestionText(), GL0006, QUESTION_TEXT);
				question.setTitle(question.getQuestionText().substring(0, question.getQuestionText().length() > 1000 ? 999 : question.getQuestionText().length()));
				// Explicitly set to null to reset any content id sent by
				// clients
				question.setContentId(null);
				question.setCreatedOn(new java.util.Date());
				question.setUrl("");
				if (question.getSharing() == null) {
					question.setSharing(PUBLIC);
				}
				if (question.getDistinguish() == null) {
					question.setDistinguish(Short.valueOf("0"));
				}
				if (question.getIsFeatured() == null) {
					question.setIsFeatured(0);
				}
				if (question.getTypeName() == null) {
					question.setTypeName(AssessmentQuestion.TYPE.MULTIPLE_CHOICE.getName());
				}
				if (question.getTypeName().equalsIgnoreCase(AssessmentQuestion.TYPE.MATCH_THE_FOLLOWING.getName()) && question.getAnswers().size() > 0) {
					for (AssessmentAnswer assessmentAnswer : question.getAnswers()) {
						for (AssessmentAnswer matchingAnswer : question.getAnswers()) {
							if (assessmentAnswer.getMatchingSequence() != null && assessmentAnswer.getMatchingSequence().equals(matchingAnswer.getMatchingSequence()) && !assessmentAnswer.getSequence().equals(matchingAnswer.getSequence())) {
								// assessmentAnswer.setMatchingAnswer(matchingAnswer);
								matchingAnswer.setMatchingAnswer(assessmentAnswer);
							}
						}
					}
				}
				ResourceType resourceType = null;
				if (question.getSourceReference() != null && question.getSourceReference().equalsIgnoreCase(ASSESSMENT)) {
					resourceType = (ResourceType) baseRepository.get(ResourceType.class, ResourceType.Type.AM_ASSESSMENT_QUESTION.getType());
				} else {
					resourceType = (ResourceType) baseRepository.get(ResourceType.class, ResourceType.Type.ASSESSMENT_QUESTION.getType());
				}
				question.setResourceType(resourceType);
				question.setTypeName(question.getTypeName());
				question.setCategory(QUESTION);
				question.setResourceFormat(this.getCustomTableRepository().getCustomTableValue(RESOURCE_CATEGORY_FORMAT, QUESTION));
			} else {
				AssessmentQuestion existingQuestion = getQuestion(gooruOQuestionId);
				if (existingQuestion == null) {
					throw new NotFoundException(ServerValidationUtils.generateErrorMessage(GL0056, RESOURCE), GL0056);
				}

				if (question.getQuestionText() != null) {
					existingQuestion.setQuestionText(question.getQuestionText());
					existingQuestion.setTitle(question.getQuestionText().substring(0, question.getQuestionText().length() > 1000 ? 999 : question.getQuestionText().length()));
				}
				if (question.getDescription() != null) {
					existingQuestion.setDescription(question.getDescription());
				}
				if (question.getExplanation() != null) {
					existingQuestion.setExplanation(question.getExplanation());
				}
				if (question.getConcept() != null) {
					existingQuestion.setConcept(question.getConcept());
				}
				if (question.getImportCode() != null) {
					existingQuestion.setImportCode(question.getImportCode());
				}
				if (question.getContentType() != null) {
					existingQuestion.setContentType(question.getContentType());
				}
				if (question.getTags() != null) {
					existingQuestion.setTags(question.getTags());
				}
				if (question.getResourceSource() != null && existingQuestion.getResourceSource() != null) {
					ResourceSource resourceSource = existingQuestion.getResourceSource();
					resourceSource.setAttribution(question.getResourceSource().getAttribution());
					existingQuestion.setResourceSource(resourceSource);
				}
				existingQuestion.setDifficultyLevel(question.getDifficultyLevel());

				if (question.getTitle() != null) {
					existingQuestion.setTitle(question.getTitle());
				}
				existingQuestion.setTimeToCompleteInSecs(question.getTimeToCompleteInSecs());
				if (question.getTypeName() != null) {
					existingQuestion.setTypeName(question.getTypeName());
				}
				if (question.getCategory() != null) {
					existingQuestion.setCategory(question.getCategory());
				}
				if (question.getSharing() != null) {
					existingQuestion.setSharing(question.getSharing());
				}
				if (question.getAnswers() != null) {
					updateAnswerList(question.getAnswers(), existingQuestion.getAnswers());
				}
				if (question.getHints() != null) {
					updateHintList(question.getHints(), existingQuestion.getHints());
				}
				resourceService.saveOrUpdateResourceTaxonomy(existingQuestion, question.getTaxonomySet());

				if (question.getRecordSource() != null) {
					existingQuestion.setRecordSource(question.getRecordSource());
				}

				question = existingQuestion;
			}
		}
		question.setLastModified(new java.util.Date());

		if (question.getConcept() == null) {
			question.setConcept("");
		}

		if (question.getRecordSource() == null) {
			question.setRecordSource(Resource.RecordSource.DEFAULT.getRecordSource());
		}

		if (question.getTimeToCompleteInSecs() == null) {
			question.setTimeToCompleteInSecs(0);
		}

		if (question.getExplanation() == null) {
			question.setExplanation("");
		}
		if (question.getDescription() == null) {
			question.setDescription("");
		}
		if (question.getSharing() == null) {
			question.setSharing(PUBLIC);
		}
		if (question.getLicense() == null) {
			question.setLicense(new License());
			question.getLicense().setName(CREATIVE_COMMONS);
		}
		if (question.getUrl() == null) {
			question.setUrl("");
		}

		if (question.getQuestionText() == null) {
			throw new BadRequestException("Question Text is mandatory");

		}

		return question;
	}

	private void updateAnswerList(Set<AssessmentAnswer> sourceList, Set<AssessmentAnswer> existingList) {
		Set<AssessmentAnswer> addList = new TreeSet<AssessmentAnswer>();
		if (sourceList != null && sourceList.size() > 0) {
			if (existingList != null && existingList.size() > 0) {
				for (AssessmentAnswer srcObj : sourceList) {
					for (AssessmentAnswer chkObj : existingList) {
						if (srcObj.getAnswerId() != null) {
							if (srcObj.getAnswerId().equals(chkObj.getAnswerId())) {
								chkObj.setAnswerText(srcObj.getAnswerText());
								chkObj.setAnswerType(srcObj.getAnswerType());
								chkObj.setIsCorrect(srcObj.getIsCorrect());
								chkObj.setUnit(srcObj.getUnit());
								addList.add(chkObj);
								break;
							}
						} else if (!addList.contains(srcObj)) {
							addList.add(srcObj);
							if (srcObj.getIsCorrect() == null) {
								srcObj.setIsCorrect(false);
							}
						}
					}
				}
			} else {
				addList.addAll(sourceList);
			}
		}
		existingList.clear();
		existingList.addAll(addList);
	}

	private void updateHintList(Set<AssessmentHint> sourceList, Set<AssessmentHint> existingList) {
		Set<AssessmentHint> addList = new TreeSet<AssessmentHint>();
		if (sourceList != null && sourceList.size() > 0) {
			if (existingList != null && existingList.size() > 0) {
				for (AssessmentHint srcObj : sourceList) {
					for (AssessmentHint chkObj : existingList) {
						if (srcObj.getHintId() != null) {
							if (srcObj.getHintId().equals(chkObj.getHintId())) {
								chkObj.setHintText(srcObj.getHintText());
								addList.add(chkObj);
								break;
							}
						} else if (!addList.contains(srcObj)) {
							addList.add(srcObj);
						}
					}
				}
			} else {
				addList.addAll(sourceList);
			}
		}
		existingList.clear();
		existingList.addAll(addList);
	}

	private Errors validateQuestion(AssessmentQuestion question) throws Exception {
		final Errors errors = new BindException(question, QUESTION);
		if (question != null) {
			boolean imageExist = false;
			if (question.getAssets() != null) {
				for (AssessmentQuestionAssetAssoc assetAssoc : question.getAssets()) {
					if (assetAssoc.getAssetKey().equals(ASSET_QUESTION)) {
						imageExist = true;
					}
				}
			}
			if (!imageExist) {
				ServerValidationUtils.rejectIfNullOrEmpty(errors, question.getQuestionText(), QUESTION_TEXT, ErrorMessage.REQUIRED_FIELD);
			}
			if (question.getTypeName().equals(AssessmentQuestion.TYPE.SHORT_ANSWER.getName()) && question.getTypeName().equals(AssessmentQuestion.TYPE.OPEN_ENDED.getName())) {
				ServerValidationUtils.rejectIfNullOrEmpty(errors, question.getAnswers(), ANSWERS, ErrorMessage.REQUIRED_FIELD);
			}
		}
		return errors;
	}

	@Override
	public List<AssessmentQuestion> getAssessmentQuestions(String gooruOAssessmentId) {
		return assessmentRepository.getAssessmentQuestions(gooruOAssessmentId);
	}

	@Override
	@Deprecated
	public AssessmentQuestion copyAssessmentQuestion(User user, String gooruQuestionId) throws Exception {
		AssessmentQuestion question = getQuestion(gooruQuestionId);
		AssessmentQuestion copyQuestion = new AssessmentQuestion();
		if (question != null) {
			copyQuestion.setDescription(question.getDescription());
			copyQuestion.setContentType(question.getContentType());
			copyQuestion.setConcept(question.getConcept());
			copyQuestion.setLicense(question.getLicense());
			copyQuestion.setCopiedResourceId(question.getAssessmentGooruId());
			copyQuestion.setLabel(question.getLabel());
			copyQuestion.setTitle(question.getTitle());
			copyQuestion.setResourceType(question.getResourceType());
			copyQuestion.setSharing(question.getSharing());
			copyQuestion.setTimeToCompleteInSecs(question.getTimeToCompleteInSecs());
			copyQuestion.setDifficultyLevel(question.getDifficultyLevel());
			copyQuestion.setQuestionText(question.getQuestionText());
			copyQuestion.setExplanation(question.getExplanation());
			copyQuestion.setHelpContentLink(question.getHelpContentLink());
			copyQuestion.setInstruction(question.getInstruction());
			copyQuestion.setScorePoints(question.getScorePoints());
			copyQuestion.setUser(user);
			copyQuestion.setCreator(question.getCreator());
			copyQuestion.setType(question.getType());
			if (question.getHints() != null) {
				Set<AssessmentHint> copyHints = new TreeSet<AssessmentHint>();
				for (AssessmentHint hint : question.getHints()) {
					AssessmentHint copyHint = new AssessmentHint();
					copyHint.setHintText(hint.getHintText());
					copyHint.setSequence(hint.getSequence());
					copyHints.add(copyHint);
				}
				copyQuestion.setHints(copyHints);
			}
			if (question.getAnswers() != null) {
				Set<AssessmentAnswer> copyAnswers = new TreeSet<AssessmentAnswer>();
				for (AssessmentAnswer answer : question.getAnswers()) {
					AssessmentAnswer copyAnswer = new AssessmentAnswer();
					copyAnswer.setAnswerText(answer.getAnswerText());
					copyAnswer.setSequence(answer.getSequence());
					copyAnswer.setIsCorrect(answer.getIsCorrect());
					copyAnswer.setMatchingAnswer(answer.getMatchingAnswer());
					copyAnswer.setUnit(answer.getUnit());
					copyAnswers.add(copyAnswer);
				}
				copyQuestion.setAnswers(copyAnswers);
			}
			createQuestion(copyQuestion, true);
			if (question.getAssets() != null && question.getAssets().size() > 0) {
				Set<AssessmentQuestionAssetAssoc> questionAssets = new HashSet<AssessmentQuestionAssetAssoc>();
				for (AssessmentQuestionAssetAssoc questionAssetAssoc : question.getAssets()) {
					AssessmentQuestionAssetAssoc copyQuestionAssetAssoc = new AssessmentQuestionAssetAssoc();
					Asset asset = new Asset();
					asset.setDescription(questionAssetAssoc.getAsset().getDescription());
					asset.setName(questionAssetAssoc.getAsset().getName());
					asset.setHasUniqueName(questionAssetAssoc.getAsset().getHasUniqueName());
					assessmentRepository.save(asset);
					copyQuestionAssetAssoc.setAsset(asset);
					copyQuestionAssetAssoc.setAssetKey(questionAssetAssoc.getAssetKey());
					copyQuestion.setThumbnail(questionAssetAssoc.getAsset().getName());
					copyQuestionAssetAssoc.setQuestion(copyQuestion);
					questionAssets.add(copyQuestionAssetAssoc);
					assessmentRepository.save(copyQuestionAssetAssoc);
				}
				assessmentRepository.save(copyQuestion);
				if (copyQuestion.isQuestionNewGen()) {
					mongoQuestionsService.copyQuestion(gooruQuestionId, copyQuestion.getGooruOid());
				}
				copyQuestion.setAssets(questionAssets);
			}
		}
		return copyQuestion;
	}
	
	

	public ContentRepository getContentRepository() {
		return contentRepository;
	}

	public void setContentRepository(ContentRepository contentRepository) {
		this.contentRepository = contentRepository;
	}

	public BaseRepository getBaseRepository() {
		return baseRepository;
	}

	public void setBaseRepository(BaseRepository baseRepository) {
		this.baseRepository = baseRepository;
	}

	public UserRepository getUserRepository() {
		return userRepository;
	}

	public void setUserRepository(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public ResourceManager getResourceManager() {
		return resourceManager;
	}

	public void setResourceManager(ResourceManager resourceManager) {
		this.resourceManager = resourceManager;
	}

	@Override
	public AssessmentQuestion buildQuestionFromInputParameters(String jsonData, User user, boolean addFlag) {

		XStream xstream = new XStream(new JettisonMappedXmlDriver());
		xstream.alias(QUESTION, AssessmentQuestion.class);
		xstream.alias(ANSWER, AssessmentAnswer.class);
		xstream.alias(HINT, AssessmentHint.class);
		xstream.alias(TAXONOMY_CODE, Code.class);
		xstream.alias(_DEPTH_OF_KNOWLEDGE, ContentMetaDTO.class);
		xstream.alias(_EDUCATIONAL_USE, ContentMetaDTO.class);
		xstream.addImplicitCollection(AssessmentQuestion.class, "deletedMediaFiles", String.class);
		xstream.addImplicitCollection(AssessmentQuestion.class, "mediaFiles", String.class);
		xstream.addImplicitCollection(AssessmentQuestion.class, "depthOfKnowledgeIds", Integer.class);
		xstream.addImplicitCollection(AssessmentQuestion.class, "standardIds", Integer.class);
		xstream.addImplicitCollection(AssessmentQuestion.class, "skillIds", Integer.class);

		/*
		 * The change to make sure that if we add some other attributes
		 * tomorrow, or as we have added today, we don't have to make them parse
		 * in JAVA as they can directly be transferred to JSON store
		 */
		xstream.ignoreUnknownElements();
		AssessmentQuestion question = null;
		try {
			question = (AssessmentQuestion) xstream.fromXML(jsonData);
		} catch (Exception e) {
			throw new BadRequestException(e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
		}
		if (addFlag) {
			question.setUser(user);
		}
		if (question.isQuestionNewGen()) {
			question.setAnswers(null);
			question.setHints(null);
		} else {
			if (question.getAnswers() != null) {
				question.setAnswers(new TreeSet<AssessmentAnswer>(question.getAnswers()));
			}
			if (question.getHints() != null) {
				question.setHints(new TreeSet<AssessmentHint>(question.getHints()));
			}
		}

		return question;
	}

	@Override
	public int deleteQuestion(String gooruOQuestionId, User caller) {
		AssessmentQuestion question = getQuestion(gooruOQuestionId);
		if (question != null) {
			assessmentRepository.remove(AssessmentQuestion.class, question.getContentId());
			indexHandler.setReIndexRequest(question.getGooruOid(), IndexProcessor.DELETE, RESOURCE, null, false, false);
			return 1;
		}
		return 0;
	}

	public ResourceImageUtil getResourceImageUtil() {
		return resourceImageUtil;
	}

	public AsyncExecutor getAsyncExecutor() {
		return asyncExecutor;
	}

	public CustomTableRepository getCustomTableRepository() {
		return customTableRepository;
	}

}
