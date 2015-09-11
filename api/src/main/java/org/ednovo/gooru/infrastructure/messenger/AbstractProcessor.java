package org.ednovo.gooru.infrastructure.messenger;

import org.ednovo.gooru.core.constant.ParameterProperties;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class AbstractProcessor implements ParameterProperties {

	@javax.annotation.Resource(name = "sessionFactory")
	private SessionFactory sessionFactory;

	@javax.annotation.Resource(name = "transactionManager")
	private HibernateTransactionManager transactionManager;

	protected TransactionStatus initTransaction(String name, boolean isReadOnly) {

		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName(AUTHENTICATE_USER);
		if (isReadOnly) {
			def.setReadOnly(isReadOnly);
		} else {
			def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
		}

		return getTransactionManager().getTransaction(def);

	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public HibernateTransactionManager getTransactionManager() {
		return transactionManager;
	}

}
