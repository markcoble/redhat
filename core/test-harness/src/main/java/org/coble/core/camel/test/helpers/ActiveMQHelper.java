package org.coble.core.camel.test.helpers;


import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.jmx.QueueViewMBean;
import org.coble.core.camel.test.utils.PropertyLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

//import static   com.jayway.restassured.RestAssured.*;

/**
 * JMS/ActiveMQ helper
 * User: AKhettar
 * Date: 08/09/13
 * Time: 22:09
 * To change this template use File | Settings | File Templates.
 */
public final class ActiveMQHelper {

    private final Logger logger = LoggerFactory.getLogger(ActiveMQHelper.class);
    private Connection connection;
    private final static int DEFAULT_TIME_OUT = 20000;
    private final String jmxUrl;
    private final String brokerName = "batch-broker";

    /**
     * Default constructor
     */
    public ActiveMQHelper() {

        try {
            jmxUrl = PropertyLoader.getEnvironmentProperty("activemq.jmxurl");

            ConnectionFactory fac = new ActiveMQConnectionFactory(PropertyLoader.getEnvironmentProperty("activemq.brokerurl"));
            connection = fac.createConnection(PropertyLoader.getEnvironmentProperty("activemq.username")
                    , PropertyLoader.getEnvironmentProperty("activemq.password"));
            connection.start();
        } catch (JMSException e) {
            logger.error("Failed to create JMS connection", e);
            throw new RuntimeException("Failed to create JMS connection", e);

        }

    }

    /**
     * Overriding the broker url.
     *
     * @param brokerUrl
     */
    public ActiveMQHelper(final String brokerUrl)
    {
        try {
            jmxUrl = PropertyLoader.getEnvironmentProperty("activemq.jmxurl");

            ConnectionFactory fac = new ActiveMQConnectionFactory(PropertyLoader.getEnvironmentProperty(brokerUrl));
            connection = fac.createConnection(PropertyLoader.getEnvironmentProperty("activemq.username")
                    , PropertyLoader.getEnvironmentProperty("activemq.password"));
            connection.start();
        } catch (JMSException e) {
            logger.error("Failed to create JMS connection", e);
            throw new RuntimeException("Failed to create JMS connection", e);

        }
    }

    /**
     * Releases the JMS connection.
     */
    public void close() {
        try {
            logger.info("Closing the JMS connection");
            if (connection != null) {
                this.connection.close();
            }
        } catch (JMSException e) {
            logger.error("Failed to shutdown the connection properly {}", e);
        }
    }

    /**
     * Returns message from a given queue
     *
     * @param queue the given queue.
     * @return consumed message
     */
    public Message consumeMessage(final String queue, String messageSelector) {
        return consumeMessage(queue, DEFAULT_TIME_OUT, messageSelector);
    }



    /**
     * Listens for multiple messages
     *
     * @param queue
     * @param messageSelector
     * @param expectedMessages
     * @param timeout
     * @return
     * @throws Exception
     */
    public List<Message> consumeMessages(final String queue, final String messageSelector, int expectedMessages, int timeout) throws Exception
    {
       List<Message> messages = new ArrayList<Message>();
       int index=0;
       while (messages.size() < expectedMessages)
       {
          Message message = consumeMessage(queue, timeout, messageSelector);
          if (message != null)
          {
              messages.add(message);
          }
          index ++;
          if (index > 6)
          {
              break;
          }
          Thread.sleep(1000);
       }
       return messages;
    }

    /**
     * Returns message from a given queue  and timeout
     *
     * @param queue           the give queue
     * @param messageSelector the messageSelector.
     * @return the consumed message
     */
    public Message consumeMessage(final String queue, int timeout, String messageSelector) {

        Session session = null;
        MessageConsumer consumer = null;
        try {
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createQueue(queue);
            consumer = messageSelector != null ? session.createConsumer(destination, messageSelector) : session.createConsumer(destination);
            return consumer.receive(timeout);
        } catch (JMSException e) {
            logger.error("Failed to create session for given queue {}", queue, e);
            return null;
        } finally {
            if (session != null) {
                try {
                    session.close();
                    if (consumer != null) {
                        consumer.close();
                    }
                } catch (JMSException e) {
                    logger.warn("Failed to close the session for given queue {}", queue, e);
                }
            }
        }

    }

    /**
     * Publishes a message to a given queue or topic.
     *
     * @param queue   the given queue
     * @param message the gien message
     * @return true if message got successfully published.
     */
    public boolean publishMessage(final String queue, String message) {
        return publishMessage(queue, message, null, null);
    }

    public boolean publishMessage(final String queue, String message, Map<String, String> headers) {
        return publishMessage(queue, message, headers, null);
    }

    /**
     * Publishes a message to a given queue or topic.
     *
     * @param queue   the given queue
     * @param message the message being published.
     * @return true if message got successfully published.
     */
    public boolean publishMessage(final String queue, String message, Map<String, String> headers, String replyTo) {
        Session session = null;
        MessageProducer producer = null;
        try {
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

	    logger.debug("-- publishMessage()> " + queue + " " + message);
	    
            Destination destination = session.createQueue(queue);
            producer = session.createProducer(destination);
            Message msg = session.createTextMessage(message);
            if(headers != null) {
                setHeaders(msg, headers);
            }

            if(replyTo != null) {
                Destination replyToQueue = session.createQueue(replyTo);
                msg.setJMSReplyTo(replyToQueue);
            }

            producer.send(msg);
            return true;
        } catch (JMSException e) {
            logger.error("Failed to create session for given queue {}", queue, e);
            return false;
        } finally {
            if (session != null) {
                try {
                    session.close();
                    if (producer != null) {
                        producer.close();
                    }
                } catch (JMSException e) {
                    logger.warn("Failed to close the session for given queue {}", queue, e);
                }
            }
        }
    }

    /**
     * Purge a queue. Useful before running a test.
     *
     * @param queue   the given queue
     * @return true if the queue was purged.
     */
  /*  public boolean purgeQueue(final String queue) {
        return purgeQueueViaJkia(queue);
    }
*/
    /**
     * Purge a queue. Useful before running a test.
     *
     * @param queue   the given queue
     * @return true if the queue was purged.
     */
   /* private boolean purgeQueueViaJkia(final String queue) {
        String user = PropertyLoader.getEnvironmentProperty("activemq.username");
        String pwd = PropertyLoader.getEnvironmentProperty("activemq.password");

        //Jolokia Url For Mbean Operation
        StringBuilder jkUrlfMbOp = new StringBuilder();
        jkUrlfMbOp.append(PropertyLoader.getEnvironmentProperty("jolokia.activemq.brokerurl"));
        jkUrlfMbOp.append("/exec/org.apache.activemq:type=Broker,brokerName=");
        jkUrlfMbOp.append(PropertyLoader.getEnvironmentProperty("jolokia.activemq.brokername"));
        jkUrlfMbOp.append(",destinationType=Queue,destinationName=");
        jkUrlfMbOp.append(queue);
        jkUrlfMbOp.append("/purge");

        int status=
                with()
                .auth().basic(user, pwd)
                .get(jkUrlfMbOp.toString())
                .statusCode();

        return status==200;
    }
    */

    /**
     * Purge a queue. Useful before running a test.
     *
     * @param queue   the given queue
     * @return true if the queue was purged.
     */
    @Deprecated
    private boolean purgeQueueViaJMX(final String queue) {
        Session session = null;
        MessageProducer producer = null;
        JMXConnector jmxc = null;
        try {
        	JMXServiceURL url = new JMXServiceURL(jmxUrl);
        	String[] creds = {PropertyLoader.getEnvironmentProperty("activemq.username"),
                    PropertyLoader.getEnvironmentProperty("activemq.password")};
        	Hashtable<String, String[]> env = new Hashtable<String, String[]>(); 
        	env.put(JMXConnector.CREDENTIALS, creds);
    		jmxc = JMXConnectorFactory.connect(url, env);
			MBeanServerConnection conn = jmxc.getMBeanServerConnection();
        	// The ActiveMQ JMX domain 
        	String amqDomain = "org.apache.activemq"; 
        	// The parameters for an ObjectName 
        	Hashtable<String, String> params = new Hashtable<String, String>(); 
        	params.put("type", "Broker"); 
        	params.put("brokerName", brokerName); 
        	params.put("destinationType", "Queue"); 
        	params.put("destinationName", queue);

        	// Create an ObjectName 
        	ObjectName queueObjectName = ObjectName.getInstance(amqDomain, params); 

        	// Create a proxy to the QueueViewMBean 
        	QueueViewMBean queueProxy = (QueueViewMBean) 
        	MBeanServerInvocationHandler.newProxyInstance(conn, queueObjectName, QueueViewMBean.class, true);

        	// Purge the queue 
        	queueProxy.purge(); 
        	
        	long queueSize = queueProxy.getQueueSize();
        	logger.info("Queue size is " + queueSize);
        	if (queueSize > 0) {
        		return false;
        	}
            return true;
        } catch (JMSException e) {
            logger.error("Failed to create session for given queue {}", queue, e);
            return false;
        } catch (javax.management.InstanceNotFoundException e) {
        	logger.error("The queue has not been created yet", queue, e);
            return true;
        }
        catch (Exception e) {
        	logger.error("Failed to create JMX connection to purge queue", queue, e);
            return false;
		}  finally {
            if (session != null) {
                try {
                    session.close();
                    if (producer != null) {
                        producer.close();
                    }
                } catch (JMSException e) {
                    logger.warn("Failed to close the session for given queue {}", queue, e);
                }
            }
            if (jmxc != null){
            	try {
					jmxc.close();
				} catch (IOException e) {
					logger.warn("Failed to close the JMX connection", queue, e);
				}
            }
        }
    }
    
    private void setHeaders(Message msg, Map<String, String> headers) {

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            try {
                msg.setStringProperty(entry.getKey(), entry.getValue());
            } catch (JMSException e) {
                logger.error("Failed to set message headers {}", headers.toString());
                throw new RuntimeException(e);
            }
        }
    }


}
