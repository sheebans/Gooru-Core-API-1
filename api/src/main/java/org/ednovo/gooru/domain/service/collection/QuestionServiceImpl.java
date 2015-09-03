package org.ednovo.gooru.domain.service.collection;

import java.io.File;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.ednovo.gooru.application.util.ConfigProperties;
import org.ednovo.gooru.core.api.model.AssessmentAnswer;
import org.ednovo.gooru.core.api.model.AssessmentHint;
import org.ednovo.gooru.core.api.model.AssessmentQuestion;
import org.ednovo.gooru.core.api.model.AssessmentQuestionAssetAssoc;
import org.ednovo.gooru.core.api.model.Asset;
import org.ednovo.gooru.core.api.model.Code;
import org.ednovo.gooru.core.api.model.ContentType;
import org.ednovo.gooru.core.api.model.License;
import org.ednovo.gooru.core.api.model.Resource;
import org.ednovo.gooru.core.api.model.ResourceType;
import org.ednovo.gooru.core.api.model.User;
import org.ednovo.gooru.core.application.util.ServerValidationUtils;
import org.ednovo.gooru.core.constant.ConstantProperties;
import org.ednovo.gooru.core.constant.Constants;
import org.ednovo.gooru.core.constant.ParameterProperties;
import org.ednovo.gooru.core.exception.BadRequestException;
import org.ednovo.gooru.infrastructure.messenger.IndexProcessor;
import org.ednovo.gooru.infrastructure.persistence.hibernate.QuestionRepository;
import org.ednovo.goorucore.application.serializer.JsonDeserializer;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;

@Service
public class QuestionServiceImpl extends AbstractResourceServiceImpl implements QuestionService, ParameterProperties, ConstantProperties {

	@Autowired
	private QuestionRepository questionRepository;

	
	private final static String QUESTION_IMAGE_DIMENSION = "160x120,80x60";

	@Override
	public AssessmentQuestion createQuestion(String data, User user) {
		AssessmentQuestion question = buildQuestion(data);
		ServerValidationUtils.rejectIfNull(question.getQuestionText(), GL0006, QUESTION_TEXT);
		question.setTitle(question.getQuestionText().substring(0, question.getQuestionText().length() > 1000 ? 999 : question.getQuestionText().length()));
		License license = (License) getBaseRepository().get(License.class, CREATIVE_COMMONS);
		question.setLicense(license);

		ContentType contentType = (ContentType) getBaseRepository().get(ContentType.class, ContentType.QUESTION);
		question.setContentType(contentType);
		question.setGooruOid(UUID.randomUUID().toString());
		question.setCreatedOn(new Date(System.currentTimeMillis()));
		question.setLastModified(new Date(System.currentTimeMillis()));
		question.setDistinguish((short) 0);
		question.setIsFeatured(0);
		if (question.getSharing() == null) {
			question.setSharing(PUBLIC);
		}
		if (question.getRecordSource() == null) {
			question.setRecordSource(Resource.RecordSource.COLLECTION.getRecordSource());
		}
		if (question.getTypeName().equalsIgnoreCase(AssessmentQuestion.TYPE.MATCH_THE_FOLLOWING.getName()) && question.getAnswers().size() > 0) {
			for (AssessmentAnswer assessmentAnswer : question.getAnswers()) {
				for (AssessmentAnswer matchingAnswer : question.getAnswers()) {
					if (assessmentAnswer.getMatchingSequence() != null && assessmentAnswer.getMatchingSequence().equals(matchingAnswer.getMatchingSequence()) && !assessmentAnswer.getSequence().equals(matchingAnswer.getSequence())) {
						matchingAnswer.setMatchingAnswer(assessmentAnswer);
					}
				}
			}
		}
		ResourceType resourceType = null;
		if (question.getSourceReference() != null && question.getSourceReference().equalsIgnoreCase(ASSESSMENT)) {
			resourceType = (ResourceType) getBaseRepository().get(ResourceType.class, ResourceType.Type.AM_ASSESSMENT_QUESTION.getType());
		} else {
			resourceType = (ResourceType) getBaseRepository().get(ResourceType.class, ResourceType.Type.ASSESSMENT_QUESTION.getType());
		}
		question.setResourceType(resourceType);
		question.setTypeName(question.getTypeName());
		question.setResourceFormat(this.getCustomTableRepository().getCustomTableValue(RESOURCE_CATEGORY_FORMAT, QUESTION));
		question.setOrganization(user.getOrganization());
		question.setCreator(user);
		question.setUser(user);
		question.setIsOer(1);
		this.getQuestionRepository().save(question);
		if (question.getMediaFilename() != null && !question.getMediaFilename().isEmpty()) {
			getGooruImageUtil().imageUpload(question.getMediaFilename(), question.getFolder(), QUESTION_IMAGE_DIMENSION);
			question.setThumbnail(question.getMediaFilename());
		}
		this.getQuestionRepository().save(question);
		getIndexHandler().setReIndexRequest(question.getGooruOid(), IndexProcessor.INDEX, RESOURCE, null, false, false);
		if (question.isQuestionNewGen()) {
			getMongoQuestionsService().createQuestion(question.getGooruOid(), data);
		}
		
		if (question.getMediaFiles() != null && question.getMediaFiles().size() > 0) {
			updateMediaFiles(question.getFolder(), question.getMediaFiles());
		}
		return question;
	}

	@Override
	public AssessmentQuestion updateQuestion(String questionId, String data, User user) {
		AssessmentQuestion question = this.getQuestionRepository().getQuestion(questionId);
		rejectIfNull(question, GL0056, 404, QUESTION);
		AssessmentQuestion newQuestion = buildQuestion(data);
		if (newQuestion.getQuestionText() != null) {
			question.setQuestionText(newQuestion.getQuestionText());
			question.setTitle(newQuestion.getQuestionText().substring(0, newQuestion.getQuestionText().length() > 1000 ? 999 : newQuestion.getQuestionText().length()));
		}
		if (newQuestion.getDescription() != null) {
			question.setDescription(newQuestion.getDescription());
		}
		if (newQuestion.getExplanation() != null) {
			question.setExplanation(newQuestion.getExplanation());
		}
		if (newQuestion.getConcept() != null) {
			question.setConcept(newQuestion.getConcept());
		}
		if (newQuestion.getImportCode() != null) {
			question.setImportCode(newQuestion.getImportCode());
		}
		question.setDifficultyLevel(question.getDifficultyLevel());
		if (newQuestion.getTitle() != null) {
			question.setTitle(newQuestion.getTitle());
		}
		if (newQuestion.getTimeToCompleteInSecs() != null) {
			question.setTimeToCompleteInSecs(newQuestion.getTimeToCompleteInSecs());
		}
		if (newQuestion.getSharing() != null) {
			question.setSharing(question.getSharing());
		}
		if (newQuestion.getAnswers() != null) {
			updateAnswerList(newQuestion.getAnswers(), question.getAnswers());
		}
		if (newQuestion.getHints() != null) {
			updateHintList(newQuestion.getHints(), question.getHints());
		}
		if (newQuestion.getRecordSource() != null) {
			question.setRecordSource(newQuestion.getRecordSource());
		}
		
		if (newQuestion.getMediaFilename() != null && !newQuestion.getMediaFilename().isEmpty()) {
			getGooruImageUtil().imageUpload(newQuestion.getMediaFilename(), question.getFolder(), QUESTION_IMAGE_DIMENSION);
			question.setThumbnail(newQuestion.getMediaFilename());
		}

		question.setLastModified(new java.util.Date(System.currentTimeMillis()));
		getIndexHandler().setReIndexRequest(question.getGooruOid(), IndexProcessor.INDEX, RESOURCE, null, false, false);
		this.getQuestionRepository().save(question);
		newQuestion.setAnswers(question.getAnswers());
		newQuestion.setHints(question.getHints());
		newQuestion.setGooruOid(question.getGooruOid());
		// Update the question in mongo now that transaction is almost
		// done
		if (question.isQuestionNewGen()) {
			getMongoQuestionsService().updateQuestion(question.getGooruOid(), data);
		}
		if (newQuestion.getMediaFiles() != null && newQuestion.getMediaFiles().size() > 0) {
			updateMediaFiles(question.getFolder(), newQuestion.getMediaFiles());
		}
		if (newQuestion.getDeletedMediaFiles() != null && newQuestion.getDeletedMediaFiles().size() > 0) {
			deleteMediaFiles(question.getFolder(), newQuestion.getDeletedMediaFiles());
		}
		
		return newQuestion;
	}

	private void updateMediaFiles(String folderPath, List<String> mediaFiles) {
		for (String mediaFilename : mediaFiles) {
			StringBuilder sourceRepoPath = new StringBuilder(ConfigProperties.getNfsInternalPath());
			sourceRepoPath.append(Constants.UPLOADED_MEDIA_FOLDER).append(File.separator).append(mediaFilename);
			File srcFile = new File(sourceRepoPath.toString());
			StringBuilder targetRepoPath = new StringBuilder(ConfigProperties.getNfsInternalPath());
			targetRepoPath.append(folderPath).append(File.separator);
			File destFile = new File(targetRepoPath.toString());
			if (!destFile.exists()) {
				destFile.mkdirs();
			}
			if (srcFile.exists()) {
				srcFile.renameTo(new File(targetRepoPath.append(mediaFilename).toString()));
			}
		}
	}

	private void deleteMediaFiles(String folderPath, List<String> mediaFiles) {
		for (String mediaFilename : mediaFiles) {
			StringBuilder targetRepoPath = new StringBuilder(ConfigProperties.getNfsInternalPath());
			targetRepoPath.append(folderPath).append(File.separator).append(mediaFilename);
			File srcFile = new File(targetRepoPath.toString());
			if (srcFile.exists()) {
				srcFile.delete();
			}
		}
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

	@Override
	public AssessmentQuestion copyQuestion(AssessmentQuestion question, User user) {
		AssessmentQuestion copyQuestion = new AssessmentQuestion();
		copyQuestion.setGooruOid(UUID.randomUUID().toString());
		copyQuestion.setDescription(question.getDescription());
		copyQuestion.setContentType(question.getContentType());
		copyQuestion.setConcept(question.getConcept());
		copyQuestion.setLicense(question.getLicense());
		copyQuestion.setCopiedResourceId(question.getGooruOid());
		copyQuestion.setLabel(question.getLabel());
		copyQuestion.setTitle(question.getTitle());
		copyQuestion.setResourceType(question.getResourceType());
		copyQuestion.setResourceFormat(question.getResourceFormat());
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
		copyQuestion.setCreatedOn(new Date(System.currentTimeMillis()));
		copyQuestion.setOrganization(question.getOrganization());
		copyQuestion.setThumbnail(question.getThumbnail());
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
		getQuestionRepository().save(copyQuestion);
		if (question.getAssets() != null && question.getAssets().size() > 0) {
			Set<AssessmentQuestionAssetAssoc> questionAssets = new HashSet<AssessmentQuestionAssetAssoc>();
			for (AssessmentQuestionAssetAssoc questionAssetAssoc : question.getAssets()) {
				AssessmentQuestionAssetAssoc copyQuestionAssetAssoc = new AssessmentQuestionAssetAssoc();
				Asset asset = new Asset();
				asset.setDescription(questionAssetAssoc.getAsset().getDescription());
				asset.setName(questionAssetAssoc.getAsset().getName());
				asset.setHasUniqueName(questionAssetAssoc.getAsset().getHasUniqueName());
				getQuestionRepository().save(asset);
				copyQuestionAssetAssoc.setAsset(asset);
				copyQuestionAssetAssoc.setAssetKey(questionAssetAssoc.getAssetKey());
				copyQuestion.setThumbnail(questionAssetAssoc.getAsset().getName());
				copyQuestionAssetAssoc.setQuestion(copyQuestion);
				questionAssets.add(copyQuestionAssetAssoc);
				getQuestionRepository().save(copyQuestionAssetAssoc);
			}
			getQuestionRepository().save(copyQuestion);
			copyQuestion.setAssets(questionAssets);
		}
		if (question.isQuestionNewGen()) {
			getMongoQuestionsService().copyQuestion(question.getGooruOid(), copyQuestion.getGooruOid());
		}
		StringBuilder sourceFilepath = new StringBuilder(question.getOrganization().getNfsStorageArea().getInternalPath());
		sourceFilepath.append(question.getFolder()).append(File.separator);
		StringBuilder targetFilepath = new StringBuilder(copyQuestion.getOrganization().getNfsStorageArea().getInternalPath());
		targetFilepath.append(copyQuestion.getFolder()).append(File.separator);
		this.getAsyncExecutor().copyResourceFolder(sourceFilepath.toString(), targetFilepath.toString());
		return copyQuestion;

	}

	@Override
	public AssessmentQuestion copyQuestion(String questionId, User user) {
		AssessmentQuestion question = this.getQuestion(questionId);
		rejectIfNull(question, GL0056, 404, QUESTION);
		return copyQuestion(question, user);
	}

	@Override
	public AssessmentQuestion getQuestion(String questionId) {
		return this.getQuestionRepository().getQuestion(questionId);
	}

	private static AssessmentQuestion buildQuestion(String data) {
		XStream xstream = new XStream(new JettisonMappedXmlDriver());
		xstream.alias(QUESTION, AssessmentQuestion.class);
		xstream.alias(ANSWER, AssessmentAnswer.class);
		xstream.alias(HINT, AssessmentHint.class);
		xstream.alias(TAXONOMY_CODE, Code.class);
		xstream.addImplicitCollection(AssessmentQuestion.class, "mediaFiles", String.class);
		xstream.addImplicitCollection(AssessmentQuestion.class, "deletedMediaFiles", String.class);
		xstream.addImplicitCollection(AssessmentQuestion.class, "skillIds", Integer.class);
		xstream.addImplicitCollection(AssessmentQuestion.class, "standardIds", Integer.class);
		xstream.addImplicitCollection(AssessmentQuestion.class, "depthOfKnowledgeIds", Integer.class);
		
		/*
		 * The change to make sure that if we add some other attributes
		 * tomorrow, or as we have added today, we don't have to make them parse
		 * in JAVA as they can directly be transferred to JSON store
		 */
		xstream.ignoreUnknownElements();
		AssessmentQuestion question = null;
		try {
			question = (AssessmentQuestion) xstream.fromXML(data);
			// deserialized again with flex json, to get as empty array object when it's passed as empty array string in json.
			// TO DO FIX ME, figure out one json deserialization which work  for all scenario.
			JSONObject JsonData = new  JSONObject(data);
			AssessmentQuestion assessmentQuestion = JsonDeserializer.deserialize(JsonData.getJSONObject(QUESTION).toString(), AssessmentQuestion.class);
			question.setDepthOfKnowledgeIds(assessmentQuestion.getDepthOfKnowledgeIds());
			question.setStandardIds(assessmentQuestion.getStandardIds());
			question.setSkillIds(assessmentQuestion.getSkillIds());
		} catch (Exception e) {
			throw new BadRequestException(e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
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

	public QuestionRepository getQuestionRepository() {
		return questionRepository;
	}
}
