package org.ednovo.gooru.controllers.v1.api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ednovo.gooru.controllers.BaseController;
import org.ednovo.gooru.core.api.model.RequestMappingUri;
import org.ednovo.gooru.core.constant.ConstantProperties;
import org.ednovo.gooru.core.constant.GooruOperationConstants;
import org.ednovo.gooru.core.security.AuthorizeOperations;
import org.ednovo.gooru.domain.service.collection.LibraryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@RequestMapping(value = { RequestMappingUri.V3_LIBRARY })
@Controller
public class LibraryRestV3Controller extends BaseController implements ConstantProperties {

	@Autowired
	private LibraryService libraryService;

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_SCOLLECTION_READ })
	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView getLibrary(final HttpServletRequest request, final HttpServletResponse response) {
		return toModelAndViewWithIoFilter(getLibraryService().getLibraries(), RESPONSE_FORMAT_JSON, EXCLUDE_ALL, true, "*");
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_SCOLLECTION_READ })
	@RequestMapping(value = RequestMappingUri.V3_LIBRARY_COURSE, method = RequestMethod.GET)
	public ModelAndView getCourses(@PathVariable(value = ID) final String userUId, @RequestParam(value = OFFSET_FIELD, required = false, defaultValue = "0") int offset, @RequestParam(value = LIMIT_FIELD, required = false, defaultValue = "10") int limit, final HttpServletRequest request,
			final HttpServletResponse response) {
		return toModelAndViewWithIoFilter(getLibraryService().getCourse(userUId, limit, offset), RESPONSE_FORMAT_JSON, EXCLUDE_ALL, true, "*");
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_SCOLLECTION_READ })
	@RequestMapping(value = RequestMappingUri.V3_LIBRARY_COURSE_UNIT, method = RequestMethod.GET)
	public ModelAndView getUnits(@PathVariable(value = ID) final String userUId, @PathVariable(value = COURSE_ID) final String courseId, @RequestParam(value = OFFSET_FIELD, required = false, defaultValue = "0") int offset,
			@RequestParam(value = LIMIT_FIELD, required = false, defaultValue = "20") int limit, final HttpServletRequest request, final HttpServletResponse response) {
		return toModelAndViewWithIoFilter(getLibraryService().getUnits(courseId, limit, offset), RESPONSE_FORMAT_JSON, EXCLUDE_ALL, true, "*");
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_SCOLLECTION_READ })
	@RequestMapping(value = RequestMappingUri.V3_LIBRARY_COURSE_LESSON, method = RequestMethod.GET)
	public ModelAndView getLessons(@PathVariable(value = ID) final String userUId, @PathVariable(value = COURSE_ID) final String courseId, @PathVariable(value = UNIT_ID) final String unitId, @RequestParam(value = OFFSET_FIELD, required = false, defaultValue = "0") int offset,
			@RequestParam(value = LIMIT_FIELD, required = false, defaultValue = "4") int limit, final HttpServletRequest request, final HttpServletResponse response) {
		return toModelAndViewWithIoFilter(getLibraryService().getLessons(unitId, limit, offset), RESPONSE_FORMAT_JSON, EXCLUDE_ALL, true, "*");
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_SCOLLECTION_READ })
	@RequestMapping(value = RequestMappingUri.V3_LIBRARY_COURSE_LESSON_COLLECTION, method = RequestMethod.GET)
	public ModelAndView getCollections(@PathVariable(value = ID) final String userUId, @PathVariable(value = COURSE_ID) final String courseId, @PathVariable(value = UNIT_ID) final String unitId, @PathVariable(value = LESSON_ID) final String lessonId,
			@RequestParam(value = OFFSET_FIELD, required = false, defaultValue = "0") int offset, @RequestParam(value = LIMIT_FIELD, required = false, defaultValue = "15") int limit, final HttpServletRequest request, final HttpServletResponse response) {
		return toModelAndViewWithIoFilter(getLibraryService().getCollections(lessonId, limit, offset), RESPONSE_FORMAT_JSON, EXCLUDE_ALL, true, COLLECTIONS);
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_SCOLLECTION_READ })
	@RequestMapping(value = RequestMappingUri.V3_LIBRARY_SUBJECT, method = RequestMethod.GET)
	public ModelAndView getSubjects(@PathVariable(value = ID) final String userUId, @RequestParam(value = OFFSET_FIELD, required = false, defaultValue = "0") int offset, @RequestParam(value = LIMIT_FIELD, required = false, defaultValue = "10") int limit, final HttpServletRequest request,
			final HttpServletResponse response) {
		return toModelAndViewWithIoFilter(getLibraryService().getSubjects(userUId, limit, offset), RESPONSE_FORMAT_JSON, EXCLUDE_ALL, true, "*");
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_SCOLLECTION_READ })
	@RequestMapping(value = RequestMappingUri.V3_LIBRARY_SUBJECT_COURSE, method = RequestMethod.GET)
	public ModelAndView getSubjectCourses(@PathVariable(value = ID) final String userUId, @PathVariable(value = SUBJECT_ID) final String subjectId, @RequestParam(value = OFFSET_FIELD, required = false, defaultValue = "0") int offset,
			@RequestParam(value = LIMIT_FIELD, required = false, defaultValue = "10") int limit, final HttpServletRequest request, final HttpServletResponse response) {
		return toModelAndViewWithIoFilter(getLibraryService().getCourses(subjectId, limit, offset), RESPONSE_FORMAT_JSON, EXCLUDE_ALL, true, "*");
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_SCOLLECTION_READ })
	@RequestMapping(value = RequestMappingUri.V3_LIBRARY_SUBJECT_COURSE_UNIT, method = RequestMethod.GET)
	public ModelAndView getSubjectUnits(@PathVariable(value = ID) final String userUId, @PathVariable(value = SUBJECT_ID) final String subjectId, @PathVariable(value = COURSE_ID) final String courseId, @RequestParam(value = OFFSET_FIELD, required = false, defaultValue = "0") int offset,
			@RequestParam(value = LIMIT_FIELD, required = false, defaultValue = "10") int limit, final HttpServletRequest request, final HttpServletResponse response) {
		return toModelAndViewWithIoFilter(getLibraryService().getUnits(courseId, limit, offset), RESPONSE_FORMAT_JSON, EXCLUDE_ALL, true, "*");
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_SCOLLECTION_READ })
	@RequestMapping(value = RequestMappingUri.V3_LIBRARY_SUBJECT_COURSE_UNIT_LESSON, method = RequestMethod.GET)
	public ModelAndView getSubjectLessons(@PathVariable(value = ID) final String userUId, @PathVariable(value = SUBJECT_ID) final String subjectId, @PathVariable(value = COURSE_ID) final String courseId, @PathVariable(value = UNIT_ID) final String unitId,
			@RequestParam(value = OFFSET_FIELD, required = false, defaultValue = "0") int offset, @RequestParam(value = LIMIT_FIELD, required = false, defaultValue = "4") int limit, final HttpServletRequest request, final HttpServletResponse response) {
		return toModelAndViewWithIoFilter(getLibraryService().getLessons(unitId, limit, offset), RESPONSE_FORMAT_JSON, EXCLUDE_ALL, true, "*");
	}

	@AuthorizeOperations(operations = { GooruOperationConstants.OPERATION_SCOLLECTION_READ })
	@RequestMapping(value = RequestMappingUri.V3_LIBRARY_SUBJECT_COURSE_UNIT_LESSON_COLLECTION, method = RequestMethod.GET)
	public ModelAndView getSubjectCollections(@PathVariable(value = ID) final String userUId, @PathVariable(value = SUBJECT_ID) final String subjectId, @PathVariable(value = COURSE_ID) final String courseId, @PathVariable(value = UNIT_ID) final String unitId,
			@PathVariable(value = LESSON_ID) final String lessonId, @RequestParam(value = OFFSET_FIELD, required = false, defaultValue = "0") int offset, @RequestParam(value = LIMIT_FIELD, required = false, defaultValue = "15") int limit, final HttpServletRequest request,
			final HttpServletResponse response) {
		return toModelAndViewWithIoFilter(getLibraryService().getCollections(lessonId, limit, offset), RESPONSE_FORMAT_JSON, EXCLUDE_ALL, true, COLLECTIONS);
	}

	public LibraryService getLibraryService() {
		return libraryService;
	}

}
