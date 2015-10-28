/////////////////////////////////////////////////////////////
// AssessmentRepositoryHibernate.java
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
package org.ednovo.gooru.infrastructure.persistence.hibernate.assessment;

import java.io.Serializable;
import java.util.List;

import org.ednovo.gooru.core.api.model.AssessmentQuestion;
import org.ednovo.gooru.infrastructure.persistence.hibernate.BaseRepositoryHibernate;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

@Repository
public class AssessmentRepositoryHibernate extends BaseRepositoryHibernate implements AssessmentRepository {
	@Override
	public Object getModel(Class<?> classModel, Serializable id) {
		return get(classModel, id);
	}
	
	@Override
	public <T extends Serializable> T getByGooruOId(Class<T> modelClass, String gooruOId) {
		String hql = "SELECT distinct model FROM " + modelClass.getSimpleName() + " model  WHERE model.gooruOid = '" + gooruOId + "' AND  " + generateAuthQueryWithDataNew("model.");
		return  getRecord(hql);
	}

	@Override
	public List<AssessmentQuestion> getAssessmentQuestions(String gooruOAssessmentId) {
		String hql = "SELECT aquestion FROM AssessmentQuestion aquestion, Assessment assessment  join assessment.segments as assessmentSegment inner join assessmentSegment.segmentQuestions as segmentQuestion WHERE assessment.gooruOid  = '" + gooruOAssessmentId
				+ "' AND aquestion = segmentQuestion.question AND  " + generateAuthQueryWithDataNew("assessment.") + " order by assessmentSegment.sequence , segmentQuestion.sequence";
		Query query = getSession().createQuery(hql);
		return list(query);
	}
	
	private <T> T getRecord(String hql) {
		Query query = getSession().createQuery(hql);
		List<T> list = list(query);
		return (list != null && list.size() > 0) ? list.get(0) : null;
	}
}
