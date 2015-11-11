package org.ednovo.gooru.controllers.v1.api;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.ednovo.gooru.application.spring.ClearCache;
import org.ednovo.gooru.application.spring.RedisCache;
import org.ednovo.gooru.controllers.BaseController;
import org.ednovo.gooru.core.api.model.ActionResponseDTO;
import org.ednovo.gooru.core.api.model.Collection;
import org.ednovo.gooru.core.api.model.CollectionItem;
import org.ednovo.gooru.core.api.model.RequestMappingUri;
import org.ednovo.gooru.core.api.model.User;
import org.ednovo.gooru.core.constant.ConstantProperties;
import org.ednovo.gooru.core.constant.Constants;
import org.ednovo.gooru.core.constant.GooruOperationConstants;
import org.ednovo.gooru.core.constant.ParameterProperties;
import org.ednovo.gooru.core.security.AuthorizeOperations;
import org.ednovo.gooru.domain.service.ClassService;
import org.ednovo.gooru.domain.service.collection.CollectionBoService;
import org.ednovo.gooru.domain.service.collection.CollectionCopyService;
import org.ednovo.gooru.infrastructure.messenger.IndexHandler;
import org.ednovo.gooru.infrastructure.messenger.IndexProcessor;
import org.ednovo.goorucore.application.serializer.JsonDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.type.TypeReference;

@Controller
public class CollectionRestController extends BaseController implements ConstantProperties, ParameterProperties {

	@Autowired
	private CollectionBoService collectionBoService;

	@Autowired
	private CollectionCopyService collectionCopyService;

	@Autowired
	private IndexHandler indexHandler;

	@Autowired
	private ClassService classService;

	private static final String COLLECTION_TYPES = "collection,assessment,assessment/url";

	private static final String INCLUDE_ITEMS = "includeItems";

	private static final String INCLUDE_LAST_MODIFIED_USER = "includeLastModifiedUser";

	@ClearCache(key = {CONTENT}, id = LESSON_ID)	
	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_SCOLLECTION_ADD })
	@RequestMapping(value = { RequestMappingUri.LESSON_COLLECTION }, method = RequestMethod.POST)
	public ModelAndView createCollection(@PathVariable(value = COURSE_ID) final String courseUId, @PathVariable(value = UNIT_ID) final String unitUId, @PathVariable(value = LESSON_ID) final String lessonUId, @RequestBody final String data, final HttpServletRequest request,
			final HttpServletResponse response) {
		final User user = (User) request.getAttribute(Constants.USER);
		final ActionResponseDTO<Collection> responseDTO = this.getCollectionBoService().createCollection(courseUId, unitUId, lessonUId, user, buildCollection(data));
		if (responseDTO.getErrors().getErrorCount() > 0) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		} else {
			response.setStatus(HttpServletResponse.SC_CREATED);
			responseDTO.getModel().setUri(generateUri(request.getRequestURI(), responseDTO.getModel().getGooruOid()));
		}
		String includes[] = (String[]) ArrayUtils.addAll(CREATE_INCLUDES, ERROR_INCLUDE);
		return toModelAndViewWithIoFilter(responseDTO.getModelData(), RESPONSE_FORMAT_JSON, EXCLUDE_ALL, true, includes);
	}

	@ClearCache(key = {CONTENT}, id = LESSON_ID)
	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_SCOLLECTION_UPDATE })
	@RequestMapping(value = { RequestMappingUri.LESSON_COLLECTION_ID }, method = RequestMethod.PUT)
	public void updateCollection(@PathVariable(value = COURSE_ID) final String courseId, @PathVariable(value = UNIT_ID) final String unitId, @PathVariable(value = LESSON_ID) final String lessonId, @PathVariable(value = ID) final String collectionId, @RequestBody final String data,
			final HttpServletRequest request, final HttpServletResponse response) {
		final User user = (User) request.getAttribute(Constants.USER);
		this.getCollectionBoService().updateCollection(lessonId, collectionId, buildCollection(data), user);
	}

	@ClearCache(key = {CONTENT}, id = LESSON_ID)
	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_SCOLLECTION_UPDATE })
	@RequestMapping(value = { RequestMappingUri.LESSON_COLLECTION_ITEM_ID }, method = RequestMethod.PUT)
	public void updateCollectionItem(@PathVariable(value = COURSE_ID) final String courseUId, @PathVariable(value = UNIT_ID) final String unitUId, @PathVariable(value = LESSON_ID) final String lessonUId, @PathVariable(value = COLLECTION_ID) final String collectionId,
			@PathVariable(value = ID) final String collectionItemId, @RequestBody final String data, final HttpServletRequest request, final HttpServletResponse response) {
		final User user = (User) request.getAttribute(Constants.USER);
		this.getCollectionBoService().updateCollectionItem(collectionId, collectionItemId, buildCollectionItem(data), user);
	}

	@ClearCache(key = {CONTENT}, id = LESSON_ID)
	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_SCOLLECTION_ADD })
	@RequestMapping(value = { RequestMappingUri.LESSON_COLLECTION_RESOURCE }, method = RequestMethod.POST)
	public ModelAndView createResource(@PathVariable(value = COURSE_ID) final String courseUId, @PathVariable(value = UNIT_ID) final String unitUId, @PathVariable(value = LESSON_ID) final String lessonUId, @PathVariable(value = ID) final String collectionId, @RequestBody final String data,
			final HttpServletRequest request, final HttpServletResponse response) {
		final User user = (User) request.getAttribute(Constants.USER);
		final ActionResponseDTO<CollectionItem> responseDTO = this.getCollectionBoService().createResource(collectionId, buildCollectionItem(data), user);
		if (responseDTO.getErrors().getErrorCount() > 0) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		} else {
			response.setStatus(HttpServletResponse.SC_CREATED);
			responseDTO.getModel().setUri(generateUri(request.getRequestURI(), responseDTO.getModel().getCollectionItemId()));
			responseDTO.getModel().setTitle(responseDTO.getModel().getResource().getTitle());
		}
		String includes[] = (String[]) ArrayUtils.addAll(CREATE_INCLUDES, ERROR_INCLUDE);
		return toModelAndViewWithIoFilter(responseDTO.getModelData(), RESPONSE_FORMAT_JSON, EXCLUDE_ALL, true, includes);
	}

	@ClearCache(key = {CONTENT}, id = LESSON_ID)
	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_SCOLLECTION_UPDATE })
	@RequestMapping(value = { RequestMappingUri.LESSON_COLLECTION_RESOURCE_ID }, method = RequestMethod.PUT)
	public void updateResource(@PathVariable(value = COURSE_ID) final String courseUId, @PathVariable(value = UNIT_ID) final String unitUId, @PathVariable(value = LESSON_ID) final String lessonUId, @PathVariable(value = COLLECTION_ID) final String collectionId,
			@PathVariable(value = ID) final String collectionItemId, @RequestBody final String data, final HttpServletRequest request, final HttpServletResponse response) {
		final User user = (User) request.getAttribute(Constants.USER);
		this.getCollectionBoService().updateResource(collectionId, collectionItemId, buildCollectionItem(data), user);
	}

	@ClearCache(key = {CONTENT}, id = LESSON_ID)
	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_SCOLLECTION_UPDATE })
	@RequestMapping(value = { RequestMappingUri.LESSON_COLLECTION_RESOURCE_ID }, method = RequestMethod.POST)
	public ModelAndView addResource(@PathVariable(value = COURSE_ID) final String courseUId, @PathVariable(value = UNIT_ID) final String unitUId, @PathVariable(value = LESSON_ID) final String lessonUId, @PathVariable(value = COLLECTION_ID) final String collectionId,
			@PathVariable(value = ID) final String resourceId, @RequestParam(value = COLLECTION_ITEM_ID, required = false) final String collectionItemId, final HttpServletRequest request, final HttpServletResponse response) {
		final User user = (User) request.getAttribute(Constants.USER);
		CollectionItem collectionItem = this.getCollectionBoService().addResource(collectionId, resourceId, collectionItemId, user);
		collectionItem.setUri(generateUri(StringUtils.substringBeforeLast(request.getRequestURI(), RequestMappingUri.SEPARATOR), collectionItem.getCollectionItemId()));
		String includes[] = (String[]) ArrayUtils.addAll(CREATE_INCLUDES, ERROR_INCLUDE);
		return toModelAndViewWithIoFilter(collectionItem, RESPONSE_FORMAT_JSON, EXCLUDE_ALL, true, includes);
	}

	@ClearCache(key = {CONTENT}, id = LESSON_ID)
	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_SCOLLECTION_ADD })
	@RequestMapping(value = { RequestMappingUri.LESSON_COLLECTION_QUESTION }, method = RequestMethod.POST)
	public ModelAndView createQuestion(@PathVariable(value = COURSE_ID) final String courseUId, @PathVariable(value = UNIT_ID) final String unitUId, @PathVariable(value = LESSON_ID) final String lessonUId, @PathVariable(value = ID) final String collectionId, @RequestBody final String data,
			final HttpServletRequest request, final HttpServletResponse response) {
		final User user = (User) request.getAttribute(Constants.USER);
		final CollectionItem collectionItem = this.getCollectionBoService().createQuestion(collectionId, data, user);
		response.setStatus(HttpServletResponse.SC_CREATED);
		collectionItem.setUri(generateUri(request.getRequestURI(), collectionItem.getCollectionItemId()));
		String includes[] = (String[]) ArrayUtils.addAll(CREATE_INCLUDES, ERROR_INCLUDE);
		return toModelAndViewWithIoFilter(collectionItem, RESPONSE_FORMAT_JSON, EXCLUDE_ALL, true, includes);
	}

	@ClearCache(key = {CONTENT}, id = LESSON_ID)
	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_SCOLLECTION_UPDATE })
	@RequestMapping(value = { RequestMappingUri.LESSON_COLLECTION_QUESTION_ID }, method = RequestMethod.PUT)
	public void updateQuestion(@PathVariable(value = COURSE_ID) final String courseUId, @PathVariable(value = UNIT_ID) final String unitUId, @PathVariable(value = LESSON_ID) final String lessonUId, @PathVariable(value = COLLECTION_ID) final String collectionId,
			@PathVariable(value = ID) final String collectionItemId, @RequestBody final String data, final HttpServletRequest request, final HttpServletResponse response) {
		final User user = (User) request.getAttribute(Constants.USER);
		this.getCollectionBoService().updateQuestion(collectionId, collectionItemId, data, user);
	}

	@ClearCache(key = {CONTENT}, id = LESSON_ID)
	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_SCOLLECTION_ADD })
	@RequestMapping(value = { RequestMappingUri.LESSON_COLLECTION_QUESTION_ID }, method = RequestMethod.POST)
	public ModelAndView addQuestion(@PathVariable(value = COURSE_ID) final String courseUId, @PathVariable(value = UNIT_ID) final String unitUId, @PathVariable(value = LESSON_ID) final String lessonUId, @PathVariable(value = COLLECTION_ID) final String collectionId,
			@PathVariable(value = ID) final String questionId, @RequestParam(value = COLLECTION_ITEM_ID, required = false) final String collectionItemId, final HttpServletRequest request, final HttpServletResponse response) {
		final User user = (User) request.getAttribute(Constants.USER);
		CollectionItem collectionItem = this.getCollectionBoService().addQuestion(collectionId, questionId, collectionItemId, user);
		collectionItem.setUri(generateUri(StringUtils.substringBeforeLast(request.getRequestURI(), RequestMappingUri.SEPARATOR), collectionItem.getCollectionItemId()));
		String includes[] = (String[]) ArrayUtils.addAll(CREATE_INCLUDES, ERROR_INCLUDE);
		return toModelAndViewWithIoFilter(collectionItem, RESPONSE_FORMAT_JSON, EXCLUDE_ALL, true, includes);
	}

	@ClearCache(key = {CONTENT}, id = LESSON_ID)
	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_SCOLLECTION_DELETE })
	@RequestMapping(value = { RequestMappingUri.LESSON_COLLECTION_ID }, method = RequestMethod.DELETE)
	public void deleteCollection(@PathVariable(value = COURSE_ID) final String courseUId, @PathVariable(value = UNIT_ID) final String unitUId, @PathVariable(value = LESSON_ID) final String lessonUId, @PathVariable(value = ID) final String collectionId, final HttpServletRequest request,
			final HttpServletResponse response) {
		final User user = (User) request.getAttribute(Constants.USER);
		this.getCollectionBoService().deleteCollection(courseUId, unitUId, lessonUId, collectionId, user);
	}

	@ClearCache(key = {CONTENT}, id = LESSON_ID)
	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_SCOLLECTION_COPY })
	@RequestMapping(value = { RequestMappingUri.TARGET_LESSON }, method = RequestMethod.POST)
	public ModelAndView collectionCopy(@PathVariable(value = COURSE_ID) final String courseId, @PathVariable(value = UNIT_ID) final String unitId, @PathVariable(value = LESSON_ID) final String lessonId, @PathVariable(value = ID) final String collectionId, @RequestBody final String data,
			final HttpServletRequest request, final HttpServletResponse response) throws Exception {
		final User user = (User) request.getAttribute(Constants.USER);
		final Collection collection = this.getCollectionCopyService().collectionCopy(courseId, unitId, lessonId, collectionId, user, buildCollection(data));
		getIndexHandler().setReIndexRequest(collection.getGooruOid(), IndexProcessor.INDEX, SCOLLECTION, null, false, false);
		collection.setUri(buildUri(RequestMappingUri.V3_COLLECTION, collection.getGooruOid()));
		String includes[] = (String[]) ArrayUtils.addAll(CREATE_INCLUDES, ERROR_INCLUDE);
		return toModelAndViewWithIoFilter(collection, RESPONSE_FORMAT_JSON, EXCLUDE_ALL, true, includes);
	}

	@ClearCache(key = {CONTENT}, id = LESSON_ID)
	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_SCOLLECTION_MOVE })
	@RequestMapping(value = { RequestMappingUri.TARGET_LESSON }, method = RequestMethod.PUT)
	public void moveCollection(@RequestBody final String data, @PathVariable(value = COURSE_ID) final String courseId, @PathVariable(value = UNIT_ID) final String unitId, @PathVariable(value = LESSON_ID) final String lessonId, @PathVariable(value = ID) final String collectionId,
			final HttpServletRequest request, final HttpServletResponse response) {
		final User user = (User) request.getAttribute(Constants.USER);
		this.getCollectionBoService().moveCollection(build(data), courseId, unitId, lessonId, collectionId, user);
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_SCOLLECTION_READ })
	@RedisCache(key = {CONTENT})
	@RequestMapping(value = { RequestMappingUri.LESSON_COLLECTION_ID }, method = RequestMethod.GET)
	public ModelAndView getCollection(@PathVariable(value = COURSE_ID) final String courseId, @PathVariable(value = UNIT_ID) final String unitId, @PathVariable(value = LESSON_ID) final String lessonId, @PathVariable(value = ID) final String collectionId,
			@RequestParam(value = INCLUDE_ITEMS, required = false, defaultValue = FALSE) final boolean includeItems, @RequestParam(value = INCLUDE_LAST_MODIFIED_USER, required = false, defaultValue = FALSE) final boolean includeLastModifiedUser, final HttpServletRequest request,
			final HttpServletResponse response) {
		final User user = (User) request.getAttribute(Constants.USER);
		return toModelAndViewWithIoFilter(this.getCollectionBoService().getCollection(collectionId, COLLECTION, user, includeItems, includeLastModifiedUser), RESPONSE_FORMAT_JSON, EXCLUDE, true, INCLUDE_COLLECTION_ITEMS);
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_SCOLLECTION_READ })
	@RedisCache(key = {CONTENT, COLLECTION})
	@RequestMapping(value = { RequestMappingUri.LESSON_COLLECTION }, method = RequestMethod.GET)
	public ModelAndView getCollections(@PathVariable(value = COURSE_ID) final String courseId, @PathVariable(value = UNIT_ID) final String unitId, @PathVariable(value = LESSON_ID) final String lessonUId, @RequestParam(value = OFFSET_FIELD, required = false, defaultValue = "0") int offset,
			@RequestParam(value = LIMIT_FIELD, required = false, defaultValue = "10") int limit, @RequestParam(value = COLLECTION_TYPE, required = false, defaultValue = COLLECTION_TYPES) String collectionTypes, final HttpServletRequest request, final HttpServletResponse response) {
		return toModelAndViewWithIoFilter(this.getCollectionBoService().getCollections(lessonUId, collectionTypes, limit, offset), RESPONSE_FORMAT_JSON, EXCLUDE_COLLECTION_ITEMS, true, INCLUDE_COLLECTION_ITEMS);
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_SCOLLECTION_READ })
	@RedisCache(key = {CONTENT, ITEM})
	@RequestMapping(value = { RequestMappingUri.LESSON_COLLECTION_ITEM }, method = RequestMethod.GET)
	public ModelAndView getCollectionItems(@PathVariable(value = COURSE_ID) final String courseUId, @PathVariable(value = UNIT_ID) final String unitUId, @PathVariable(value = LESSON_ID) final String lessonUId, @PathVariable(value = ID) final String collectionId,
			@RequestParam(value = OFFSET_FIELD, required = false, defaultValue = "0") int offset, @RequestParam(value = LIMIT_FIELD, required = false, defaultValue = "10") int limit, final HttpServletRequest request, final HttpServletResponse response) {
		return toModelAndViewWithIoFilter(this.getCollectionBoService().getCollectionItems(collectionId, limit, offset), RESPONSE_FORMAT_JSON, EXCLUDE_COLLECTION_ITEMS, true, INCLUDE_COLLECTION_ITEMS);
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_SCOLLECTION_READ })
	@RedisCache(key = {CONTENT})
	@RequestMapping(value = { RequestMappingUri.LESSON_COLLECTION_RESOURCE_ID, RequestMappingUri.LESSON_COLLECTION_QUESTION_ID }, method = RequestMethod.GET)
	public ModelAndView getResource(@PathVariable(value = COURSE_ID) final String courseUId, @PathVariable(value = UNIT_ID) final String unitUId, @PathVariable(value = LESSON_ID) final String lessonUId, @PathVariable(value = COLLECTION_ID) final String collectionId,
			@PathVariable(value = ID) final String collectionItemId, final HttpServletRequest request, final HttpServletResponse response) {
		return toModelAndViewWithIoFilter(this.getCollectionBoService().getCollectionItem(collectionId, collectionItemId), RESPONSE_FORMAT_JSON, EXCLUDE_COLLECTION_ITEMS, true, INCLUDE_COLLECTION_ITEMS);
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_SCOLLECTION_READ })
	@RedisCache(key = {CONTENT, CLASS})
	@RequestMapping(value = { RequestMappingUri.COURSE_COLLECTION_CLASSES }, method = RequestMethod.GET)
	public ModelAndView getClasses(@PathVariable(value = COURSE_ID) final String courseUId, @PathVariable(value = UNIT_ID) final String unitUId, @PathVariable(value = LESSON_ID) final String lessonUId, @PathVariable(value = ID) final String collectionId,
			@RequestParam(value = OFFSET_FIELD, required = false, defaultValue = "0") int offset, @RequestParam(value = LIMIT_FIELD, required = false, defaultValue = "10") int limit, final HttpServletRequest request, final HttpServletResponse response) {
		return toModelAndViewWithIoFilter(this.getClassService().getClassesByCourse(courseUId, collectionId, limit, offset), RESPONSE_FORMAT_JSON, EXCLUDE, true, "*");
	}

	public CollectionBoService getCollectionBoService() {
		return collectionBoService;
	}

	public CollectionCopyService getCollectionCopyService() {
		return collectionCopyService;
	}

	private Collection buildCollection(final String data) {
		return JsonDeserializer.deserialize(data, Collection.class);
	}

	private CollectionItem buildCollectionItem(final String data) {
		return JsonDeserializer.deserialize(data, CollectionItem.class);
	}

	private Map<String, String> build(final String data) {
		return JsonDeserializer.deserialize(data, new TypeReference<Map<String, String>>() {
		});
	}

	public IndexHandler getIndexHandler() {
		return indexHandler;
	}

	public ClassService getClassService() {
		return classService;
	}

}
