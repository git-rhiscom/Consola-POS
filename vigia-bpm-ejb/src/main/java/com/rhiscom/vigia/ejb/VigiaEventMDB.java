package com.rhiscom.vigia.ejb;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.WorkingMemoryEntryPoint;

import com.rhiscom.vigia.events.ProcessMessage;
import com.rhiscom.vigia.inferencias.KnowledgeBaseManager;
import com.rhiscom.vigia.workitem.SendEmailWorkItemHandler;

/**
 * Message-Driven Bean implementation class for: VigiaEventMDB
 * 
 */
@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/vigiaQueue") })
@TransactionManagement(TransactionManagementType.CONTAINER)
public class VigiaEventMDB implements MessageListener {

	@PersistenceContext(type = PersistenceContextType.TRANSACTION, unitName = "PersistenceUnit")
	protected EntityManager em;

	/**
	 * Default constructor.
	 */
	public VigiaEventMDB() {

	}

	/**
	 * @see MessageListener#onMessage(Message)
	 */
	public void onMessage(Message message) {
		ProcessMessage processMessage = new ProcessMessage();

		try {

			ObjectMessage msg = (ObjectMessage) message;
			if (msg.getObject() instanceof ProcessMessage) {
				processMessage = (ProcessMessage) msg.getObject();
			}

			StatefulKnowledgeSession ksession = KnowledgeBaseManager.getInstance().getSession();
			ksession.getWorkItemManager().registerWorkItemHandler("SendEmailWorkItemHandler",
					new SendEmailWorkItemHandler(ksession));
			WorkingMemoryEntryPoint vigiaEventsEP = ksession.getWorkingMemoryEntryPoint("vigia-events");
			vigiaEventsEP.insert(processMessage);

			ksession.fireAllRules();

			persistProcessMessage(processMessage);

		} catch (JMSException e) {

			e.printStackTrace();

		}
	}

	public void persistProcessMessage(ProcessMessage processMessage) {
		try {

			em.persist(processMessage);

		} catch (Exception ex) {
			ex.printStackTrace();

		}

	}

}
