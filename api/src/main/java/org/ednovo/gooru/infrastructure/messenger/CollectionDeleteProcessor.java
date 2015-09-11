package org.ednovo.gooru.infrastructure.messenger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.ednovo.gooru.domain.service.CollectionDeleteHandler;
import org.hibernate.Session;
import org.json.JSONObject;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
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

	private static String[] emails = { "qas_astrid1030@gmail.com", "qas_astrid1072@gmail.com", "qas_astrid1359@gmail.com", "qas_astrid1556@gmail.com", "qas_astrid1565@gmail.com", "qas_astrid161@gmail.com", "qas_astrid1658@gmail.com", "qas_astrid186@gmail.com", "qas_astrid1907@gmail.com",
			"qas_astrid1920@gmail.com", "qas_astrid2030@gmail.com", "qas_astrid215@gmail.com", "qas_astrid2258@gmail.com", "qas_astrid2474@gmail.com", "qas_astrid2476@gmail.com", "qas_astrid2565@gmail.com", "qas_astrid2574@gmail.com", "qas_astrid2817@gmail.com", "qas_astrid3167@gmail.com",
			"qas_astrid3605@gmail.com", "qas_astrid4100@gmail.com", "qas_astrid4553@gmail.com", "qas_astrid4910@gmail.com", "qas_astrid5137@gmail.com", "qas_astrid5388@gmail.com", "qas_astrid5495@gmail.com", "qas_astrid5898@gmail.com", "qas_astrid6511@gmail.com", "qas_astrid6590@gmail.com",
			"qas_astrid6658@gmail.com", "qas_astrid6787@gmail.com", "qas_astrid7020@gmail.com", "qas_astrid7489@gmail.com", "qas_astrid7800@gmail.com", "qas_astrid8268@gmail.com", "qas_astrid8754@gmail.com", "qas_astrid8975@gmail.com", "qas_astrid9206@gmail.com", "qas_astrid9325@gmail.com",
			"qas_astrid9423@gmail.com", "qas_astrid9828@gmail.com", "qas_astrid9883@gmail.com", "qas_barrette1234@gmail.com", "qas_barrette1397@gmail.com", "qas_barrette1583@gmail.com", "qas_barrette2291@gmail.com", "qas_barrette2575@gmail.com", "qas_barrette2689@gmail.com",
			"qas_barrette2722@gmail.com", "qas_barrette3145@gmail.com", "qas_barrette3163@gmail.com", "qas_barrette3377@gmail.com", "qas_barrette3708@gmail.com", "qas_barrette3941@gmail.com", "qas_barrette410@gmail.com", "qas_barrette4800@gmail.com", "qas_barrette4988@gmail.com",
			"qas_barrette5061@gmail.com", "qas_barrette518@gmail.com", "qas_barrette5388@gmail.com", "qas_barrette5497@gmail.com", "qas_barrette5603@gmail.com", "qas_barrette5761@gmail.com", "qas_barrette5929@gmail.com", "qas_barrette5949@gmail.com", "qas_barrette612@gmail.com",
			"qas_barrette6215@gmail.com", "qas_barrette6423@gmail.com", "qas_barrette6886@gmail.com", "qas_barrette7108@gmail.com", "qas_barrette7196@gmail.com", "qas_barrette7259@gmail.com", "qas_barrette7644@gmail.com", "qas_barrette8246@gmail.com", "qas_barrette841@gmail.com",
			"qas_barrette8422@gmail.com", "qas_barrette8873@gmail.com", "qas_barrette8879@gmail.com", "qas_barrette8974@gmail.com", "qas_barrette9006@gmail.com", "qas_barrette9459@gmail.com", "qas_barrette9514@gmail.com", "qas_barrette9569@gmail.com", "qas_barrette9633@gmail.com",
			"qas_barrette984@gmail.com", "qas_barrette9929@gmail.com", "qas_cate1009@gmail.com", "qas_cate1131@gmail.com", "qas_cate1342@gmail.com", "qas_cate135@gmail.com", "qas_cate1631@gmail.com", "qas_cate1829@gmail.com", "qas_cate1986@gmail.com", "qas_cate2096@gmail.com",
			"qas_cate2115@gmail.com", "qas_cate2401@gmail.com", "qas_cate2464@gmail.com", "qas_cate2480@gmail.com", "qas_cate2861@gmail.com", "qas_cate2956@gmail.com", "qas_cate3201@gmail.com", "qas_cate3408@gmail.com", "qas_cate3492@gmail.com", "qas_cate4050@gmail.com", "qas_cate4454@gmail.com",
			"qas_cate4537@gmail.com", "qas_cate4555@gmail.com", "qas_cate4729@gmail.com", "qas_cate4904@gmail.com", "qas_cate5161@gmail.com", "qas_cate5280@gmail.com", "qas_cate5752@gmail.com", "qas_cate5781@gmail.com", "qas_cate5976@gmail.com", "qas_cate6124@gmail.com", "qas_cate6429@gmail.com",
			"qas_cate6528@gmail.com", "qas_cate6965@gmail.com", "qas_cate7178@gmail.com", "qas_cate7613@gmail.com", "qas_cate7870@gmail.com", "qas_cate8174@gmail.com", "qas_cate8184@gmail.com", "qas_cate8255@gmail.com", "qas_cate8464@gmail.com", "qas_cate8664@gmail.com", "qas_cate8718@gmail.com",
			"qas_cate8747@gmail.com", "qas_cate8752@gmail.com", "qas_cate879@gmail.com", "qas_cate8943@gmail.com", "qas_cate9003@gmail.com", "qas_cate9372@gmail.com", "qas_cutshall1022@gmail.com", "qas_cutshall1314@gmail.com", "qas_cutshall1436@gmail.com", "qas_cutshall1465@gmail.com",
			"qas_cutshall1667@gmail.com", "qas_cutshall1915@gmail.com", "qas_cutshall2026@gmail.com", "qas_cutshall2133@gmail.com", "qas_cutshall2276@gmail.com", "qas_cutshall2427@gmail.com", "qas_cutshall2438@gmail.com", "qas_cutshall2547@gmail.com", "qas_cutshall2689@gmail.com",
			"qas_cutshall2968@gmail.com", "qas_cutshall3020@gmail.com", "qas_cutshall3049@gmail.com", "qas_cutshall3104@gmail.com", "qas_cutshall3518@gmail.com", "qas_cutshall3573@gmail.com", "qas_cutshall3912@gmail.com", "qas_cutshall4041@gmail.com", "qas_cutshall4365@gmail.com",
			"qas_cutshall4916@gmail.com", "qas_cutshall4965@gmail.com", "qas_cutshall5406@gmail.com", "qas_cutshall5717@gmail.com", "qas_cutshall6271@gmail.com", "qas_cutshall6430@gmail.com", "qas_cutshall6508@gmail.com", "qas_cutshall6840@gmail.com", "qas_cutshall6881@gmail.com",
			"qas_cutshall7130@gmail.com", "qas_cutshall7444@gmail.com", "qas_cutshall7810@gmail.com", "qas_cutshall7955@gmail.com", "qas_cutshall7999@gmail.com", "qas_cutshall8376@gmail.com", "qas_cutshall8535@gmail.com", "qas_cutshall8823@gmail.com", "qas_cutshall8835@gmail.com",
			"qas_cutshall8939@gmail.com", "qas_cutshall9037@gmail.com", "qas_cutshall9502@gmail.com", "qas_cutshall9734@gmail.com", "qas_cutshall9880@gmail.com", "qas_damaris121@gmail.com", "qas_damaris1283@gmail.com", "qas_damaris1335@gmail.com", "qas_damaris141@gmail.com",
			"qas_damaris1491@gmail.com", "qas_damaris170@gmail.com", "qas_damaris1748@gmail.com", "qas_damaris1839@gmail.com", "qas_damaris1991@gmail.com", "qas_damaris2201@gmail.com", "qas_damaris2328@gmail.com", "qas_damaris2625@gmail.com", "qas_damaris2818@gmail.com",
			"qas_damaris2883@gmail.com", "qas_damaris2932@gmail.com", "qas_damaris3030@gmail.com", "qas_damaris33@gmail.com", "qas_damaris3836@gmail.com", "qas_damaris3901@gmail.com", "qas_damaris399@gmail.com", "qas_damaris4196@gmail.com", "qas_damaris4343@gmail.com", "qas_damaris4404@gmail.com",
			"qas_damaris465@gmail.com", "qas_damaris4758@gmail.com", "qas_damaris4819@gmail.com", "qas_damaris5024@gmail.com", "qas_damaris5170@gmail.com", "qas_damaris5382@gmail.com", "qas_damaris5479@gmail.com", "qas_damaris5532@gmail.com", "qas_damaris5564@gmail.com", "qas_damaris566@gmail.com",
			"qas_damaris5949@gmail.com", "qas_damaris5966@gmail.com", "qas_damaris611@gmail.com", "qas_damaris6144@gmail.com", "qas_damaris624@gmail.com", "qas_damaris6605@gmail.com", "qas_damaris6747@gmail.com", "qas_damaris6783@gmail.com", "qas_damaris6789@gmail.com", "qas_damaris706@gmail.com",
			"qas_damaris7731@gmail.com", "qas_damaris7747@gmail.com", "qas_damaris7818@gmail.com", "qas_damaris8020@gmail.com", "qas_damaris8424@gmail.com", "qas_damaris8533@gmail.com", "qas_damaris8648@gmail.com", "qas_damaris901@gmail.com", "qas_damaris9058@gmail.com",
			"qas_damaris9267@gmail.com", "qas_damaris9510@gmail.com", "qas_damaris9609@gmail.com", "qas_damaris9649@gmail.com", "qas_damaris9833@gmail.com", "qas_damaris9985@gmail.com", "qas_greig1069@gmail.com", "qas_greig1194@gmail.com", "qas_greig138@gmail.com", "qas_greig1424@gmail.com",
			"qas_greig1555@gmail.com", "qas_greig1563@gmail.com", "qas_greig1646@gmail.com", "qas_greig1731@gmail.com", "qas_greig1821@gmail.com", "qas_greig2167@gmail.com", "qas_greig2313@gmail.com", "qas_greig2794@gmail.com", "qas_greig2812@gmail.com", "qas_greig2958@gmail.com" };

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

	public static void main(String a[]) {
		for (final String email : emails) {
			getExecutorservice().execute(new Runnable() {
				public void run() {

					JSONObject json = new JSONObject();
					try {
						json.put("username", email);
						json.put("password", "qa123");
						Representation clientResource = new ClientResource("http://54.176.159.228:8080/gooruapi/rest/v2/account/login?apiKey=ASERTYUIOMNHBGFDXSDWERT123RTGHYT").post(json.toString());
					} catch (Exception e) {
						System.out.println(email);
					}
				}

			});
		}

	}
}
