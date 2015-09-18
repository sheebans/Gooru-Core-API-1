package org.ednovo.gooru.domain.component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.ednovo.gooru.core.api.model.Collection;
import org.ednovo.gooru.core.api.model.Job;
import org.ednovo.gooru.core.api.model.JobType;
import org.ednovo.gooru.core.api.model.User;
import org.ednovo.gooru.domain.service.collection.CourseCopyService;
import org.ednovo.gooru.domain.service.collection.LessonCopyService;
import org.ednovo.gooru.domain.service.collection.UnitCopyService;
import org.ednovo.gooru.domain.service.job.JobService;
import org.ednovo.gooru.infrastructure.persistence.hibernate.JobRepository;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class CollectionCopyProcessor extends AbstractProcessor {

	@Autowired
	private HibernateTransactionManager transactionManager;

	private TransactionTemplate transactionTemplate;

	@Autowired
	private CourseCopyService courseCopyService;

	@Autowired
	private UnitCopyService unitCopyService;

	@Autowired
	private LessonCopyService lessonCopyService;

	@Autowired
	private JobRepository jobRepository;
	
	@Autowired
	private JobService jobService;

	private static final Logger LOGGER = LoggerFactory.getLogger(CollectionCopyProcessor.class);

	private static final ExecutorService executorService = Executors.newFixedThreadPool(2);

	@PostConstruct
	public void init() {
		transactionTemplate = new TransactionTemplate(transactionManager);
	}

	public Job copyCourse(final String courseId, final User user) {
		final Job job = createJob(user);
		getExecutorservice().execute(new Runnable() {
			public void run() {
				getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
					@Override
					protected void doInTransactionWithoutResult(TransactionStatus status) {
						TransactionStatus transactionStatus = null;
						Session session = null;
						try {
							transactionStatus = initTransaction(VALIDATE_RESOURCE, false);
							session = getSessionFactory().openSession();
							Collection course = getCourseCopyService().courseCopy(courseId, user);
							updateJob(job, course.getGooruOid(), Job.Status.COMPLETED.getStatus());
							getTransactionManager().commit(transactionStatus);
						} catch (Exception ex) {
							updateJob(job, null, Job.Status.FAILED.getStatus());
							LOGGER.error("Failed  to copy course : " + courseId, ex);
						} finally {
							if (session != null) {
								session.close();
							}
						}
					}
				});
			}
		});
		return job;
	}

	public Job copyUnit(final String courseId, final String unitId, final User user) {
		final Job job = createJob(user);
		getExecutorservice().execute(new Runnable() {
			public void run() {
				getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
					@Override
					protected void doInTransactionWithoutResult(TransactionStatus status) {
						TransactionStatus transactionStatus = null;
						Session session = null;
						try {
							transactionStatus = initTransaction(VALIDATE_RESOURCE, false);
							session = getSessionFactory().openSession();
							Collection unit = getUnitCopyService().unitCopy(courseId, unitId, user);
							updateJob(job, unit.getGooruOid(), Job.Status.COMPLETED.getStatus());
							getTransactionManager().commit(transactionStatus);
						} catch (Exception ex) {
							updateJob(job, null, Job.Status.FAILED.getStatus());
							LOGGER.error("Failed  to copy unit : " + unitId, ex);
						} finally {
							if (session != null) {
								session.close();
							}
						}
					}
				});
			}
		});
		return job;
	}

	public Job copyLesson(final String courseId, final String unitId, final String lessonId, final User user) {
		final Job job = createJob(user);
		getExecutorservice().execute(new Runnable() {
			public void run() {
				getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
					@Override
					protected void doInTransactionWithoutResult(TransactionStatus status) {
						TransactionStatus transactionStatus = null;
						Session session = null;
						try {
							transactionStatus = initTransaction(VALIDATE_RESOURCE, false);
							session = getSessionFactory().openSession();
							Collection lesson = getLessonCopyService().lessonCopy(courseId, unitId, lessonId, user);
							updateJob(job, lesson.getGooruOid(), Job.Status.COMPLETED.getStatus());
							getTransactionManager().commit(transactionStatus);
						} catch (Exception ex) {
							updateJob(job, null, Job.Status.FAILED.getStatus());
							LOGGER.error("Failed  to copy lesson : " + courseId, ex);
						} finally {
							if (session != null) {
								session.close();
							}
						}
					}
				});
			}
		});
		return job;
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	private Job createJob(final User user) {
		Job job = new Job();
		job.setUser(user);
		job.setStatus(Job.Status.INPROGRESS.getStatus());
		job.setFileSize(0L);
		job.setJobType((JobType) getJobRepository().get(JobType.class, JobType.Type.COPY.getType()));
		job.setOrganization(user.getOrganization());
		this.getJobRepository().save(job);
		return job;
	}
	
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	private void updateJob(final Job job, final String gooruOid, final String status) { 
		if (job != null) {
			job.setGooruOid(gooruOid);
			job.setStatus(status);
			this.getJobRepository().save(job);
		}
	}
	

	public static ExecutorService getExecutorservice() {
		return executorService;
	}

	public TransactionTemplate getTransactionTemplate() {
		return transactionTemplate;
	}

	public CourseCopyService getCourseCopyService() {
		return courseCopyService;
	}

	public UnitCopyService getUnitCopyService() {
		return unitCopyService;
	}

	public LessonCopyService getLessonCopyService() {
		return lessonCopyService;
	}

	public JobRepository getJobRepository() {
		return jobRepository;
	}

	public JobService getJobService() {
		return jobService;
	}
}
