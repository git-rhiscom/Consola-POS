package com.rhiscom.vigia.workitem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.drools.runtime.KnowledgeRuntime;
import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemHandler;
import org.drools.runtime.process.WorkItemManager;

import com.sun.mail.smtp.SMTPTransport;

public class SendEmailWorkItemHandler implements WorkItemHandler {
	private final KnowledgeRuntime session;
	ResourceBundle bundle = ResourceBundle.getBundle("persistence");

	private static final Logger log = Logger.getLogger(SendEmailWorkItemHandler.class.getName());

	public SendEmailWorkItemHandler(KnowledgeRuntime session2) {
		this.session = session2;
	}

	@Override
	public void abortWorkItem(WorkItem arg0, WorkItemManager arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager workItemManager) {
		Map<String, Object> param = new HashMap<String, Object>();

		String from = (String) workItem.getParameter("from");
		String subject = (String) workItem.getParameter("subject");
		String message = (String) workItem.getParameter("message");
		String source = (String) workItem.getParameter("source");
		String type = (String) workItem.getParameter("type");

		param.put("from", from);
		param.put("subject", subject);
		param.put("message", message);
		param.put("source", source);
		param.put("type", type);

		SendMailThread mailThread = new SendMailThread(session, workItem, param);
		Thread hilo = new Thread(mailThread);
		hilo.start();

	}

	class SendMailThread implements Runnable {
		Map<String, Object> resultado;
		KnowledgeRuntime session;
		WorkItem workItem;
		Map<String, Object> context;

		public SendMailThread(KnowledgeRuntime session2, WorkItem workItem2, Map<String, Object> context2) {
			session = session2;
			workItem = workItem2;
			this.context = context2;
		}

		@Override
		public void run() {
			sendMail(context);
			session.getWorkItemManager().completeWorkItem(workItem.getId(), null);
		}
	}

	public void sendMail(Map<String, Object> context) {
		System.out.println(" :: SEND MAIL ::" + context);

		boolean smtpUseSSLFlag = true;

		String smtpUser = null;
		String smtpPassword = null;

		String from = (String) context.get("from");
		String subject = (String) context.get("subject");
		String smtpHost = null;// = (String) context.get("smtpHost");
		String smtpPort = null;// = (String) context.get("smtpPort");
		String message = (String) context.get("message");
		String event = (String) context.get("type");
		String source = (String) context.get("source");

		SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy");
		String content = "";

		content = message;

		Connection con = getConnection();
		String recipient = "";
		try {
			java.sql.Statement stmt = con.createStatement();

			ResultSet rs = stmt
					.executeQuery("select recipients from pa_recipients_by_event where event= 'STC_ATN_END'");

			while (rs.next()) {
				recipient = rs.getString("recipients");

			}
		} catch (SQLException ex) {

		}

		String to = recipient;

		java.sql.Statement stmt;
		try {
			stmt = con.createStatement();

			ResultSet rs = stmt.executeQuery("select emailPort,emailServer,password,  "
					+ " senderEmail from bpm_db.pa_email_configuration");

			while (rs.next()) {
				smtpHost = rs.getString("emailServer");
				smtpPort = rs.getString("emailPort");
				smtpUser = rs.getString("senderEmail");
				smtpPassword = rs.getString("password");
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		smtpUseSSLFlag = true;

		// smtpUser = "pablo.caceres@rhiscom.cl";
		// smtpPassword = "pablo2012";

		Properties props = System.getProperties();
		SMTPTransport t = null;

		if (smtpUseSSLFlag) {
			props.setProperty("mail.smtp.port", smtpPort);
			props.setProperty("mail.smtp.socketFactory.port", smtpPort);
			props.setProperty("mail.smtp.auth", "true");
			props.setProperty("mail.smtps.quitwait", "false");
			props.put("mail.debug", "false");
		} else {
			props.put("mail.smtp.host", smtpHost);
			props.put("mail.smtp.port", smtpPort);
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.user", smtpUser);
			props.put("mail.smtp.password", smtpPassword);
			props.put("mail.debug", "true");
		}

		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.socketFactory.fallback", "false");
		props.put("mail.smtp.starttls.enable", "true");

		javax.mail.Session session = javax.mail.Session.getInstance(props, null);

		SecurityManager security = System.getSecurityManager();

		System.out.println(" :: Security Manager ::" + security);

		if (security == null) {
			session = javax.mail.Session.getInstance(props);
		} else {
			session = javax.mail.Session.getDefaultInstance(props);
		}

		final MimeMessage msg = new MimeMessage(session);

		try {
			if (smtpUseSSLFlag) {
				t = (SMTPTransport) session.getTransport("smtp");
			} else {
				t = (SMTPTransport) session.getTransport("smtp");
			}
			String[] toArray = to.split(",");
			InternetAddress[] address = new InternetAddress[toArray.length];

			for (int i = 0; i <= toArray.length - 1; i++) {
				address[i] = new InternetAddress(toArray[i]);
			}

			msg.setFrom(new InternetAddress(from));
			msg.setRecipients(Message.RecipientType.TO, address);

			msg.setSubject(subject, "ISO-8859-1");
			msg.setContent(content, "text/html; charset=utf-8");
			msg.setSentDate(new java.util.Date());

			t.connect(smtpHost, smtpUser, smtpPassword);
			t.sendMessage(msg, msg.getAllRecipients());
			t.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private Connection getConnection() {
		try {
			// Cargamos el puente JDBC =&gt; Mysql
			log.info("::getConnection: Intentando cargar el conector...");
			Class.forName("com.mysql.jdbc.Driver");

			// Intentamos conectarnos a la base de Datos en este caso una base
			// llamada temp
			System.out.println("Conectando a la base...");

			String database = bundle.getString("database.host.bpm");
			String user = bundle.getString("database.user");
			String password = bundle.getString("database.password");

			Connection con = DriverManager.getConnection("jdbc:mysql://" + database, user, password);
			log.info("::getConnection:: Conexion a BD establecida");
			return con;
		} catch (SQLException ex) {
			log.info("Error de mysql :" + ex);
			return null;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (Exception e) {
			log.info("::getConnection:: Se produjo un error inesperado: " + e.getMessage());
			return null;
		}
	}
}