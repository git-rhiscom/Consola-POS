package com.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import javax.jms.*;
import javax.naming.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import com.rhiscom.vigia.events.*;

@WebServlet("/JMSClientServlet")
public class JMSClientServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final String PKG_INTERFACES = "org.jboss.ejb.client.naming";

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();

		Connection connection;

		try {
			final Properties env = new Properties();

			env.put(Context.PROVIDER_URL, "remote://localhost:4447");
			env.put(Context.SECURITY_PRINCIPAL, "guest");
			env.put(Context.SECURITY_CREDENTIALS, "guest");

			// env.put(Context.URL_PKG_PREFIXES, PKG_INTERFACES);
			Context remoteContext = new InitialContext(env);

			ConnectionFactory factory = (ConnectionFactory) remoteContext
					.lookup("RemoteConnectionFactory");
			Queue queue = (Queue) remoteContext.lookup("queue/vigiaQueue");
			connection = factory.createConnection();
			Session session = connection.createSession(false,
					QueueSession.AUTO_ACKNOWLEDGE);

			MessageProducer producer = session.createProducer(queue);

			// 1. Sending TextMessage to the Queue
			ObjectMessage objMsg = session.createObjectMessage();
			
			
			ProcessMessage processMessage = new ProcessMessage();
			
			processMessage.setOperatorMessageCode("009");
			processMessage.setProcessCode("PROC001");
			processMessage.setProcessState(ProcessMessage.PROCESS_STATE_ERROR);
			processMessage.setProcessText("PROCESO 1");
			
			

			objMsg.setObject(processMessage);
			producer.send(objMsg);

			

			out.println("Message sento to the JMS Provider");
			
			session.close();

		} catch (JMSException e) {
			e.printStackTrace();
		} catch (NamingException e) {
			e.printStackTrace();
		}

	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}