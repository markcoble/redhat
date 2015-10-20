package org.coble.core.camel.exception.handling;

import java.util.Map;

import org.apache.camel.ExchangeException;
import org.apache.camel.Headers;

/**
 * The start of an Exception Handling Service for Tru
 * 
 * @author mcoble
 * 
 */
public interface CamelExceptionHandler {

    /*
     * CONSTANTS
     */
    public static final String RESPONSE_CODE = "ResponseCode";
    public static final String ERROR_INFO = "ErrorInfo";
    public static final String ERROR_DESCRIPTION = "ErrorDescription";
    public static final String EXCEPTION_ACTION = "ExceptionAction";
    public static final String CLIENT_REPLY_TO = "clientReplyTo";

    public static final String ERROR_TYPE = "ErrorType";

    /**
     * Handle exception created by Camel context
     * 
     * @param headers Exchange headers set by Camel route to handle business exception
     * @param cause Runtime exception caught by Camel route
     * @throws DLQException for exceptions that need to be handled by DLQ
     */
    public void handleException(@Headers Map<String, Object> headers,
            @ExchangeException Throwable cause)
            throws DLQException;

}