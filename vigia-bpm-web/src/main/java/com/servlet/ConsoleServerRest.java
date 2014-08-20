package com.servlet;

import java.net.URISyntaxException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.rhiscom.vigia.events.ProcessMessage;

@Path("/ConsoleServerRestService")
public class ConsoleServerRest {
	private static Logger logger = Logger.getLogger(ConsoleServerRest.class
			.getName());

	@POST
	@Path("/processmessage/send")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createUser(ProcessMessage processMessage)
			throws URISyntaxException {
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

			objMsg.setObject(processMessage);
			producer.send(objMsg);

			session.close();

		} catch (JMSException e) {
			e.printStackTrace();
		} catch (NamingException e) {
			e.printStackTrace();
		}

		return Response.ok().build();
	}

	@GET
	@Path("/getProcessMessage")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllTaskOpen() {
		ProcessMessage event = new ProcessMessage();
		event.setHost("host response");
		return Response.status(200).entity(event).build();
	}
}
