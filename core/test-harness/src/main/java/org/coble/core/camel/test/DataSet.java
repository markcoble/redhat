package org.coble.core.camel.test;

import java.io.IOException;

import org.apache.camel.Exchange;
import org.apache.camel.component.dataset.DataSetSupport;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataSet extends DataSetSupport {

    private static final Logger LOG = LoggerFactory.getLogger(DataSet.class);

    
    @Override
    protected Object createMessageBody(long arg0) {

        try {
            return IOUtils
                    .toString(this.getClass().getResourceAsStream("/data/order1.xml"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Allows derived classes to add some custom headers for a given message
     */
    protected void applyHeaders(Exchange exchange, long messageIndex) {
        exchange.getIn().setHeader("clientReplyTo", "activemq:dummy");
    }

}
