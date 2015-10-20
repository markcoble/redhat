package org.coble.core.odm.test;

import java.io.File;
import java.net.URL;

import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.camel.Exchange;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.coble.core.camel.test.helpers.JUnitHelper;
import org.coble.core.camel.test.helpers.ActiveMQHelper;

import javax.jms.*;

@RunWith(Parameterized.class)
public class GetBatchDecisionIntegrationTest extends JUnitHelper {

	private static final int TIMEOUT = 10000;
	private static int batchCount = 2;

	private static final Logger LOG = LoggerFactory
			.getLogger(GetBatchDecisionIntegrationTest.class);

	// Path to test scenarios.
	private static String INPUT_PREFIX = "input"; // set default value
	private static String CONTROL_PREFIX = "control"; // set default value

	private static String ROOT_DIR = "/";

	// url for test scenario folder
	private static URL urlInput = GetBatchDecisionIntegrationTest.class
			.getResource(ROOT_DIR + INPUT_PREFIX);
	// url for test control folder
	private static URL urlControl = GetBatchDecisionIntegrationTest.class
			.getResource(ROOT_DIR + CONTROL_PREFIX);

	/**
	 * Parameterized Test Case constructor.
	 * 
	 * @param scenarioInput
	 * @param scenarioControl
	 */
	public GetBatchDecisionIntegrationTest(File scenarioInput,
			File scenarioControl) {

		super(scenarioInput, scenarioControl);

	}

	@Test
	public void testBatchDecServiceIntegration() throws Exception {

		String input = JUnitHelper.convertFileToString(super.scenarioInput);
		String control = JUnitHelper.convertFileToString(super.scenarioControl);

		LOG.debug("- testBatchDecServiceIntegration() > Scenario Control: \n"
				+ control);

		Map<String, String> headers = new HashMap<String, String>();

		headers.put("ClientReplyTo", "BATCH-DECISION-SERVICE-REPLY-TO");
		headers.put("clientName", "AFRC");

		ActiveMQHelper amqHelper = new ActiveMQHelper();

		for (int i = 0; i < batchCount; i++) {
			headers.put("JMSCorrelationID", scenarioInput.getName() + "-" + i);
			amqHelper.publishMessage("BATCH-DECISION-SERVICE-IN.QUEUE", input,
					headers);

		}

		Thread.sleep(10000);

		for (int i = 0; i < batchCount; i++) {

			Message response = amqHelper.consumeMessage(
					"BATCH-DECISION-SERVICE-REPLY-TO", "JMSCorrelationID = '"
							+ scenarioInput.getName() + "-" + i + "'");


			if (response instanceof ActiveMQTextMessage) {

				String actual = ((ActiveMQTextMessage) response).getText();
				LOG.debug("- testBatchDecServiceIntegration() > Scenario Actual: \n"
						+ actual);
				assertEqualsXml(control, actual);

			}
		}
	}

	/**
	 * Load input and control scenarios for execution
	 * 
	 * @return
	 */
	@Parameters
	public static Collection<Object[]> getFiles() {

		// get all files in the input folder
		final File inputFolder = new File(urlInput.getPath());
		if (LOG.isDebugEnabled()) {
			LOG.debug("-- getFiles() > ");
			JUnitHelper.listFilesForFolder(inputFolder);
		}
		;

		final List<File> inputFileList = Arrays.asList(inputFolder.listFiles());
		// get all files in the control folder
		final File controlFolder = new File(urlControl.getPath());

		if (LOG.isDebugEnabled()) {
			LOG.debug("-- getFiles() > ");
			JUnitHelper.listFilesForFolder(controlFolder);
		}

		final List<File> controlFileList = Arrays.asList(controlFolder
				.listFiles());

		Collection<Object[]> params = new ArrayList<Object[]>();
		for (File scenarioInput : inputFileList) {
			File scenarioControl = controlFileList.get(inputFileList
					.indexOf(scenarioInput));
			Object[] arr = new Object[] { scenarioInput, scenarioControl };
			params.add(arr);
		}
		return params;
	}
}
