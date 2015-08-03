package org.ednovo.gooru.domain.service.eventlogs;

import org.ednovo.gooru.core.constant.ConstantProperties;
import org.ednovo.gooru.core.constant.ParameterProperties;
import org.ednovo.gooru.infrastructure.persistence.hibernate.ClassRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class EventLog implements ConstantProperties, ParameterProperties{

	@Autowired
	private ClassRepository classRepository;
	
	public static final Logger LOGGER = LoggerFactory.getLogger(CourseEventLog.class);
	
	public final static String CLASS_ITEM_MOVE = "class.item.move";

	public static final String CLASS_ITEM_DELETE = "class.item.delete";
	
	public static final String CLASS_GOORU_IDS = "classGooruIds";
	
	//lesson
	public static final String SHELF_COURSE_LESSON = "shelf.course.unit.lesson";

	public static final String CLASS_COURSE_LESSON = "class.course.unit.lesson";
	
	//unit
	public static final String SHELF_COURSE_UNIT = "shelf.course.unit";

	public static final String CLASS_COURSE_UNIT = "class.course.unit";
	
	//course
	public static final String CLASS_COURSE = "class.course";

	public static final String SHELF_COURSE = "shelf.course";
	
	//collection
	public static final String SHELF_COURSE_COLLECTION = "shelf.course.unit.lesson.collection";

	public static final String CLASS_COURSE_COLLECTION = "class.course.unit.lesson.collection";

	public static final String SHELF_COURSE_ASSESSMENT = "shelf.course.unit.lesson.assessment";

	public static final String CLASS_COURSE_ASSESSMENT = "class.course.unit.lesson.assessment";

	public static final String SHELF_COURSE_ASSESSMENT_URL = "shelf.course.unit.lesson.assessment-url";

	public static final String CLASS_COURSE_ASSESSMENT_URL = "class.course.unit.lesson.assessment-url";

	public static final String SHELF_COURSE_RESOURCE = "shelf.course.unit.lesson.collection.resource";

	public static final String SHELF_COURSE_QUESTION = "shelf.course.unit.lesson.collection.question";

}
