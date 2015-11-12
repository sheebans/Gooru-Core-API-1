/////////////////////////////////////////////////////////////
// ContentIdexDaoImpl.java
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
/**
 * 
 */
package org.ednovo.gooru.infrastructure.persistence.hibernate.index;

import java.util.List;

import org.ednovo.gooru.core.api.model.ContentProvider;
import org.ednovo.gooru.core.api.model.Resource;
import org.ednovo.gooru.infrastructure.persistence.hibernate.HibernateDaoSupport;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

@Repository
public class ContentIndexDaoImpl extends IndexDaoImpl implements ContentIndexDao {

        private final String GET_SCOLLECTION_INFO_FOR_RESOURCE = "SELECT distinct c.content_id, c.gooru_oid, col.title FROM collection_item ci INNER JOIN content c ON c.content_id = ci.collection_content_id INNER JOIN collection col ON col.content_id = c.content_id  WHERE  ci.resource_content_id = :contentId and c.sharing='public'";

        private static final String GET_QUIZ_BY_QUESTION = "select distinct c.gooru_oid from collection_item ci inner join content c on ci.collection_content_id=c.content_id where ci.resource_content_id=:contentId";

        private static final String CONTENT_ID = "contentId";

        private static final String GOORU_OID = "gooruOid";

        private static final String GET_ASSETS = "select a.name,aqaa.asset_key from assessment_question_asset_assoc aqaa inner join asset a on aqaa.asset_id = a.asset_id where aqaa.question_id=:contentId";

        private final String GET_SCOLLECTION_ITEM_IDS_BY_RESOURCE_ID = "SELECT collection_item_id FROM collection_item WHERE resource_content_id = :contentId";

        private static final String GET_COLLECTION_IDS_BY_USERID = "select c.gooru_oid,c.type_name from content c inner join collection cc on c.content_id=cc.content_id where cc.collection_type in('collection','assessment') and c.user_uid=:ownerUId limit 2000 ";

        private static final String GET_RESOURCE_IDS_BY_USERID = "select c.gooru_oid,c.type_name from content c inner join resource r on c.content_id=r.content_id where r.type_name not in('scollection', 'classpage', 'folder', 'gooru/classbook', 'gooru/classplan', 'shelf', 'assignment', 'quiz', 'assessment-quiz', 'gooru/notebook', 'gooru/studyshelf', 'assessment-exam') and c.user_uid=:ownerUId limit 2000";

        private static final String OWNER_ID = "ownerUId";

        private static final String TYPE_COLLECTION = "collection";

        private static final String TYPE_RESOURCE = "resource";

        private static final String GET_STANDARDS_TAXONOMY_META = "SELECT s.name AS subject, s.subject_id AS subjectId, cou.name AS course, cou.course_id AS courseId, cou.grades AS courseGrades, c.code AS standard, ccl.code_id AS codeId FROM content_classification ccl INNER JOIN code c ON c.code_id = ccl.code_id INNER JOIN subdomain_attribute_mapping sam ON sam.code_id = c.parent_id INNER JOIN subdomain sd ON sd.subdomain_id = sam.subdomain_id INNER JOIN course cou ON cou.course_id = sd.course_id INNER JOIN subject s ON s.subject_id = cou.subject_id WHERE content_id =:contentId UNION SELECT s.name AS subject, s.subject_id AS subjectId, cou.name AS course, cou.course_id AS courseId, cou.grades AS courseGrades, c.code AS standard, ccl.code_id AS codeId FROM content_classification ccl INNER JOIN code c ON c.code_id = ccl.code_id INNER JOIN subdomain_attribute_mapping sam ON sam.code_id = (SELECT parent_id FROM code WHERE code_id = (SELECT parent_id FROM code WHERE code_id = ccl.code_id)) INNER JOIN subdomain sd ON sd.subdomain_id = sam.subdomain_id INNER JOIN course cou ON cou.course_id = sd.course_id INNER JOIN subject s ON s.subject_id = cou.subject_id WHERE content_id =:contentId UNION SELECT s.name AS subject, s.subject_id AS subjectId, cou.name AS course, cou.course_id AS courseId, cou.grades AS courseGrades, c.code AS standard, ccl.code_id AS codeId FROM content_classification ccl INNER JOIN code c ON c.code_id = ccl.code_id INNER JOIN subdomain_attribute_mapping sam ON sam.code_id = c.code_id INNER JOIN subdomain sd ON sd.subdomain_id = sam.subdomain_id INNER JOIN course cou ON cou.course_id = sd.course_id INNER JOIN subject s ON s.subject_id = cou.subject_id WHERE content_id = :contentId UNION SELECT s.name AS subject, s.subject_id AS subjectId, cou.name AS course, cou.course_id AS courseId, cou.grades AS courseGrades, null AS standard, null AS codeId FROM content_subdomain_assoc csa INNER JOIN subdomain sd ON sd.subdomain_id = csa.subdomain_id INNER JOIN course cou ON cou.course_id = sd.course_id INNER JOIN subject s ON s.subject_id = cou.subject_id WHERE content_id =:contentId UNION SELECT s.name AS subject, s.subject_id AS subjectId, cou.name AS course, cou.course_id AS courseId, cou.grades AS courseGrades, null AS standard, null AS codeId FROM content_course_assoc csa INNER JOIN course cou ON cou.course_id = csa.course_id INNER JOIN subject s ON s.subject_id = cou.subject_id WHERE content_id =:contentId";

        private static final String GET_RESOURCE_METADATA = "SELECT r FROM Resource r  where r.gooruOid =:gooruOid and r.resourceType.name in ('animation/kmz','animation/swf','assessment-question','exam/pdf','handouts','image/png','ppt/pptx','resource/url','textbook/scribd','video/youtube','vimeo/video')";


        @Override
        public List<String> getQuestionQuiz(Long contentId) {
                return HibernateDaoSupport.list(createSQLQuery(GET_QUIZ_BY_QUESTION).setParameter(CONTENT_ID, contentId));
        }


        @Override
        public List<Object[]> getResourceSCollections(Long contentId) {
                return HibernateDaoSupport.list(createSQLQuery(GET_SCOLLECTION_INFO_FOR_RESOURCE).setLong(CONTENT_ID, contentId));
        }


        private Query createSQLQuery(String query) {
                return getSessionFactory().getCurrentSession().createSQLQuery(query);
        }


        @Override
        public List<Object[]> getAssets(long contentId) {
                List<Object[]> list = HibernateDaoSupport.list(createSQLQuery(GET_ASSETS).setLong(CONTENT_ID, contentId));
                return list;
        }


        @Override
        public Resource findResourceByContentGooruId(String gooruOid) {
                Query query = getSessionFactory().getCurrentSession().createQuery(GET_RESOURCE_METADATA);
                query.setParameter(GOORU_OID, gooruOid);
                List<Resource> resources = HibernateDaoSupport.list(query);
                return resources.size() == 0 ? null : resources.get(0);
        }

        @Override
        public List<String> getCollectionItemIdsByResourceId(Long contentId) {
                return HibernateDaoSupport.list(createSQLQuery(GET_SCOLLECTION_ITEM_IDS_BY_RESOURCE_ID).setLong(CONTENT_ID, contentId));
        }

        @Override
        public List<Object[]> getContentProviderAssoc(long contentId) {
                String sql = "SELECT cp.type, cp.name, cp.content_provider_uid from content_provider cp INNER JOIN content_provider_assoc cpa on cp.content_provider_uid=cpa.content_provider_uid WHERE content_id = :contentId";
                List<Object[]> list = HibernateDaoSupport.list(createSQLQuery(sql).setLong(CONTENT_ID, contentId));
                return list;
        }

        @Override
        public ContentProvider  getContentProviderlist(String contentProviderId) {
                String sql="SELECT cp FROM ContentProvider cp WHERE cp.contentProviderUid='"+contentProviderId+"'";
                    List<ContentProvider> contentProvider =HibernateDaoSupport.list(getSessionFactory().getCurrentSession().createQuery(sql));
                return contentProvider.size()== 0 ? null:contentProvider.get(0);
        }


        @Override
        public List<Object[]> getCollectionIdsByUserId(String gooruUId) {
                return getIds(gooruUId, TYPE_COLLECTION);
        }

        @SuppressWarnings("unchecked")
        private List<Object[]> getIds(String gooruUId, String type){
                String sqlQuery = null;

                if(type.equalsIgnoreCase(TYPE_COLLECTION)){
                        sqlQuery = GET_COLLECTION_IDS_BY_USERID;
                }
                else if(type.equalsIgnoreCase(TYPE_RESOURCE)){
                        sqlQuery = GET_RESOURCE_IDS_BY_USERID;
                }

                Session session = getSessionFactory().getCurrentSession();
                SQLQuery query = session.createSQLQuery(sqlQuery);
                query.setParameter(OWNER_ID, gooruUId);
                return query.list();
        }

        @Override
        public List<Object[]> getResourceIdsByUserId(String gooruUId) {
                return getIds(gooruUId, TYPE_RESOURCE);
        }

        @Override
        public List<Object[]> getStandardsTaxonomyMeta(Long contentId, boolean useSlave) {
                List<Object[]> list = HibernateDaoSupport.list(createSQLQuery(GET_STANDARDS_TAXONOMY_META, useSlave).setLong(CONTENT_ID, contentId));
                return list.size() > 0 ? list : null;
        }

        private Query createSQLQuery(String query, boolean useSlave) {
                if(useSlave) {
                        return getSessionFactoryReadOnly().getCurrentSession().createSQLQuery(query);
                } else {
                        return getSessionFactory().getCurrentSession().createSQLQuery(query);
                }
        }
}

