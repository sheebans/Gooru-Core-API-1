package org.ednovo.gooru.domain.component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class CollectionDeleteProcessor extends AbstractProcessor {

	@Autowired
	private HibernateTransactionManager transactionManager;

	private TransactionTemplate transactionTemplate;

	@Autowired
	private CollectionDeleteHandler collectionDeleteHandler;

	private static final Logger LOGGER = LoggerFactory.getLogger(CollectionDeleteProcessor.class);

	private static final ExecutorService executorService = Executors.newFixedThreadPool(2);

	@PostConstruct
	public void init() {
		transactionTemplate = new TransactionTemplate(transactionManager);
	}

	public void deleteContent(final String gooruOid, final String collectionType) {
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
							getCollectionDeleteHandler().deleteContent(gooruOid, collectionType);
							getTransactionManager().commit(transactionStatus);
						} catch (Exception ex) {
							LOGGER.error("Failed  to delete content : " + gooruOid, ex);
							getTransactionManager().rollback(transactionStatus);
						} finally {
							if (session != null) {
								session.close();
							}
						}
					}
				});
			}
		});
	}

	public CollectionDeleteHandler getCollectionDeleteHandler() {
		return collectionDeleteHandler;
	}

	public static ExecutorService getExecutorservice() {
		return executorService;
	}

	public TransactionTemplate getTransactionTemplate() {
		return transactionTemplate;
	}
}
