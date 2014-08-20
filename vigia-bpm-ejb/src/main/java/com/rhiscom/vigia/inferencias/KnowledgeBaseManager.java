package com.rhiscom.vigia.inferencias;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;

public class KnowledgeBaseManager {

	private static KnowledgeBaseManager instance = null;
	private StatefulKnowledgeSession ksession;

	private KnowledgeBaseManager() {
		try {
			KnowledgeBase kbase = readKnowledgeBase();
			ksession = kbase.newStatefulKnowledgeSession();

		} catch (Exception ex) {
			ex.printStackTrace();

		}
	}

	public static KnowledgeBaseManager getInstance() {
		if (instance == null)
			instance = new KnowledgeBaseManager();
		return instance;
	}

	private KnowledgeBase readKnowledgeBase() throws Exception {
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
		kbuilder.add(ResourceFactory.newClassPathResource("events.drl"), ResourceType.DRL);
		kbuilder.add(ResourceFactory.newClassPathResource("NotificationProcess.bpmn"), ResourceType.BPMN2);

		KnowledgeBuilderErrors errors = kbuilder.getErrors();
		if (errors.size() > 0) {
			for (KnowledgeBuilderError error : errors) {
				System.err.println(error);
			}
			throw new IllegalArgumentException("Could not parse knowledge.");
		}
		KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
		kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
		return kbase;
	}

	public StatefulKnowledgeSession getSession() {
		return ksession;
	}
}
