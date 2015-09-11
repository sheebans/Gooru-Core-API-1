package org.ednovo.gooru.infrastructure.messenger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.ednovo.gooru.application.util.AccountUtil;
import org.ednovo.gooru.core.api.model.User;
import org.ednovo.gooru.core.api.model.UserToken;
import org.ednovo.gooru.domain.service.CollectionDeleteHandler;
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
public class AccountProcessor extends AbstractProcessor {
	@Autowired
	private HibernateTransactionManager transactionManager;

	private TransactionTemplate transactionTemplate;

	@Autowired
	private CollectionDeleteHandler collectionDeleteHandler;

	@Autowired
	private AccountUtil accountUtil;

	private static final ExecutorService executorService = Executors.newFixedThreadPool(2);

	private static final Logger LOGGER = LoggerFactory.getLogger(AccountProcessor.class);

	@PostConstruct
	public void init() {
		setTransactionTemplate(new TransactionTemplate(transactionManager));
	}

	public void storeAccountLoginDetailsInRedis(final String key, final UserToken userToken, final User user) {
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
							getAccountUtil().storeAccountLoginDetailsInRedis(key, userToken, user);
						} catch (Exception ex) {
							LOGGER.error("Failed  to store account details in redis cache: ", ex);
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

	public static ExecutorService getExecutorservice() {
		return executorService;
	}

	public TransactionTemplate getTransactionTemplate() {
		return transactionTemplate;
	}

	public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
		this.transactionTemplate = transactionTemplate;
	}

	public AccountUtil getAccountUtil() {
		return accountUtil;
	}
}
