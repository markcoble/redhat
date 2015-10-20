package org.coble.core.odm.test;



import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;
import java.util.Dictionary;




import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.camel.Exchange;


import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.RouteDefinition;
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

import org.coble.core.camel.exception.handling.EnumerationTypes.ResponseCode;
import org.coble.core.camel.test.CamelBlueprintTestBase;
import org.coble.core.camel.test.helpers.JUnitHelper;
import org.coble.core.camel.test.helpers.ActiveMQHelper;

import javax.jms.*;



@RunWith(Parameterized.class)
public class GetBatchDecisionTest extends CamelBlueprintTestBase {

    private static final int TIMEOUT = 10000;

    private static final Logger LOG = LoggerFactory
            .getLogger(GetBatchDecisionTest.class);

    // Path to test scenarios.
    private static String CONTEXT_ID = "decisionService"; // overridden by child test
                                                                        // class
    private static String INPUT_PREFIX = "input"; // set default value
    private static String CONTROL_PREFIX = "control"; // set default value
    private static String ROUTE_ID = "dataset-to-jms-route"; // overridden by child test class

   // private static String ROOT_DIR = ROUTE_ID == null ? "/" + CONTEXT_ID + "/"
   //         : "/" + CONTEXT_ID + "/" + ROUTE_ID + "/";

    
   private static String ROOT_DIR="/";
   
    // url for test scenario folder
    private static URL urlInput = GetBatchDecisionTest.class
            .getResource(ROOT_DIR + INPUT_PREFIX);
    // url for test control folder
    private static URL urlControl = GetBatchDecisionTest.class
            .getResource(ROOT_DIR + CONTROL_PREFIX);
    
   

    /**
     * Parameterized Test Case constructor.
     * 
     * @param scenarioInput
     * @param scenarioControl
     */
    public GetBatchDecisionTest(File scenarioInput,
            File scenarioControl) {

        super(scenarioInput, scenarioControl);

    }

    @Override
    protected String getBlueprintDescriptor() {
        return "OSGI-INF/blueprint/config-properties.xml," +
                "OSGI-INF/blueprint/camel-context.xml";
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected String useOverridePropertiesWithConfigAdmin(Dictionary props) {
        // props.put("operator-information-service-in-queue",
        // "dataset:tru.esb.core.bean.DataSet?produceDelay=5");
        props.put("batch-decision-service-from", "direct:start");
        //props.put("cbr-operator-information-service-to", "mock:result");

        return "org.coble.core.odm.batch.decision.service";
    }

/*
    @Test
    public void testBatchDecisionServiceRequest() throws Exception {

       // String input = JUnitHelper.convertFileToString(super.scenarioInput);
        //String control = JUnitHelper.convertFileToString(super.scenarioControl);

        //LOG.debug("- testBatchDecisionServiceRequest() > Scenario Input: \n" + input);
        //LOG.debug("- testBatchDecisionServiceRequest() > Scenario Control: \n" + control);

        MockEndpoint mockEndpoint = getMockEndpoint("mock:result");
        mockEndpoint.expectedMessageCount(1);


        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put(
                "clientOperationName",
                scenarioInput.getName().substring(0,
                        scenarioInput.getName().indexOf("_")));
        headers.put("clientReplyTo", "mock:result");
        headers.put("clientName", "MNP");
        headers.put("JMSCorrelationID",
                GetBatchDecisionTest.class.getCanonicalName());

        try {
        	//startCamelContext();
        
        		context.startAllRoutes();
           // template.sendBodyAndHeaders("direct:interact-preprocess",
            //        input, headers);
            //headers.put("ResponseCode", ResponseCode.SUCCESS);
        } catch (Exception e) {
            LOG.error("Exception in JUnit Test: " + e.getMessage());
            headers.put("ResponseCode",
                    ResponseCode.UNABLE_TO_COMPLETE_OPERATION);
        } finally {

            // check count of return
            assertMockEndpointsSatisfied();

            // check response content vs. control content
            List<Exchange> exchanges = mockEndpoint.getExchanges();

            // Disabled full check for now due to issues with namespace variables changing when
            // mapped return is marshalled.
            // String actual = exchanges.get(0).getIn().getBody().toString();
            // assertEqualsXml(control, actual);

            assertEquals("Error mapping JMS to SOAP request",
                    ResponseCode.SUCCESS, headers.get("ResponseCode"));
        }
    }
*/
    
    @Test
    public void testBatchDecServiceIntegration() throws Exception {

        String input = JUnitHelper.convertFileToString(super.scenarioInput);
        String control = JUnitHelper.convertFileToString(super.scenarioControl);

 //       LOG.debug("- testBatchDecServiceIntegration() > Scenario Control: \n" + control);

        Map<String, String> headers = new HashMap<String, String>();


        headers.put("ClientReplyTo", "BATCH-DECISION-SERVICE-REPLY-TO");
        headers.put("clientName", "AFRC");
        headers.put("JMSCorrelationID", scenarioInput.getName());

        ActiveMQHelper amqHelper = new ActiveMQHelper();

        amqHelper.publishMessage("BATCH-DECISION-SERVICE-IN.QUEUE", input, headers);

        Message response = amqHelper.consumeMessage("OIS-CLIENT-REPLY-TO",
                "JMSCorrelationID = '" + scenarioInput.getName() + "'");

        if (response instanceof ActiveMQTextMessage) {

            String actual = ((ActiveMQTextMessage) response).getText();
            LOG.debug("- testCbrDev12() > Scenario Actual: \n" + actual);
  //          assertEqualsXml(control, actual);

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
