package org.coble.core.camel.exception.handling.impl;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangeException;
import org.apache.camel.Headers;
import org.coble.core.camel.exception.handling.*;
import org.coble.core.camel.exception.handling.EnumerationTypes.ErrorType;
import org.coble.core.camel.exception.handling.EnumerationTypes.ExceptionAction;
import org.coble.core.camel.exception.handling.EnumerationTypes.ResponseCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The start of an Exception Handling Service for Tru
 * 
 * @author mcoble
 * 
 */
public class CamelExceptionHandlerImpl implements CamelExceptionHandler {

    private final Logger LOG = LoggerFactory
            .getLogger(CamelExceptionHandlerImpl.class);

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.truphone.esb.core.exception.handling.TruEsbExceptionHandler#handleException(java.util
     * .Map, java.lang.Throwable)
     */
    public void handleException(@Headers Map<String, Object> headers, @ExchangeException Throwable cause)
            throws DLQException {

        if (LOG.isTraceEnabled()) {
            LOG.trace(">> handleException()");
        }

        if (cause == null) {
            ExceptionAction exceptionAction = ExceptionAction
                    .valueOf((String) headers.get(EXCEPTION_ACTION));

            switch (exceptionAction) {
                case LOG:
                    LOG.info(formatErrorDetails(headers));
                    break;
                case CLIENT_RESPONSE_HEADER:
                    LOG.info(formatErrorDetails(headers));
                    break;
                case DLQ:
                    LOG.info(formatErrorDetails(headers));
                    throw new DLQException(
                            ResponseCode.valueOf((String) headers
                                    .get(RESPONSE_CODE)),
                            ErrorType.valueOf((String) headers.get(ERROR_TYPE)),
                            (String) headers.get(ERROR_INFO),
                            (String) headers.get(ERROR_DESCRIPTION));
                case EMAIL:
                    LOG.info(formatErrorDetails(headers));
                    break;
                default:
                    break;
            }
        } else {
            if (cause instanceof LogException) {
                LOG.info(formatErrorDetails((CamelException) cause));
            } else if (cause instanceof DLQException) {
                LOG.info(formatErrorDetails((CamelException) cause));
                throw new DLQException(cause);
            } else if (cause instanceof CRHException) {
                LOG.info(formatErrorDetails((CamelException) cause));
                headers.put("ResponseCode", ((CRHException) cause).getResponseCode());
                headers.put("ErrorType", ((CRHException) cause).getErrorType());
                headers.put("ErrorDescription", ((CRHException) cause).getErrorDescription());
                headers.put("ErrorInfo", ((CRHException) cause).getErrorInfo());

            } else {
                // if cause is not a CamelExcpetion then it is assumed the exception has been
                // caught in onException of a Camel route and headers must be set.

                String errorDescription = (String) headers.get(ERROR_DESCRIPTION) == null ? "System error occurred."
                        : (String) headers.get(ERROR_DESCRIPTION);
                String errorType = (String) headers.get(ERROR_TYPE) == null ? ErrorType.RUNTIME.toString()
                        : (String) headers.get(ERROR_TYPE);
                String exceptionAction = (String) headers.get(EXCEPTION_ACTION) == null ? ExceptionAction.LOG
                        .toString() : (String) headers.get(EXCEPTION_ACTION);
                String responseCode = (String) headers.get(RESPONSE_CODE) == null ?
                        ResponseCode.UNABLE_TO_COMPLETE_OPERATION.toString()
                        : (String) headers.get(RESPONSE_CODE);

                headers.put(ERROR_TYPE, errorType);
                headers.put(ERROR_DESCRIPTION, errorDescription);
                headers.put(ERROR_INFO, cause.getMessage());
                headers.put(EXCEPTION_ACTION, exceptionAction);
                headers.put(RESPONSE_CODE, responseCode);
                
                LOG.info("-- handleException() : " + headers.get(RESPONSE_CODE));
                LOG.info(formatErrorDetails(headers));
            }
        }

        if (LOG.isTraceEnabled()) {
            LOG.trace("<< handleException()");
        }
    }

    /*
     * Format log message for error details contained in exchange headers.
     */
    private String formatErrorDetails(Map<String, Object> headers) {

        String errorDescription = (String) headers.get(ERROR_DESCRIPTION);
        String errorInfo = (String) headers.get(ERROR_INFO);
        String errorType = (String) headers.get(ERROR_TYPE);
        String exceptionAction = (String) headers.get(EXCEPTION_ACTION);
        String responseCode = (String) headers.get(RESPONSE_CODE);

        StringBuffer sb = new StringBuffer();
        sb.append("\n---------------------------------------------- \n");
        sb.append("------------- EXCEPTION DETAILS -------------- \n");
        sb.append("---------------------------------------------- \n");
        sb.append("RESPONSE CODE: " + responseCode);
        sb.append("\n");
        sb.append("ERROR DESCRIPTION: " + errorDescription);
        sb.append("\n");
        sb.append("ERROR INFO: " + errorInfo);
        sb.append("\n");
        sb.append("ERROR TYPE: " + errorType);
        sb.append("\n");
        sb.append("EXCEPTION ACTION: " + exceptionAction);
        sb.append("\n");
        if (exceptionAction == ExceptionAction.CLIENT_RESPONSE_HEADER
                .toString())
            sb.append("CLIENT REPLY_TO: " + headers.get(CLIENT_REPLY_TO));
        sb.append("\n");
        sb.append("---------------------------------------------- \n");
        return sb.toString();
    }

    /*
     * Format log message for exception.
     */
    private String formatErrorDetails(CamelException cause) {

        StringBuffer sb = new StringBuffer();
        sb.append("\n---------------------------------------------- \n");
        sb.append("------------- EXCEPTION DETAILS -------------- \n");
        sb.append("---------------------------------------------- \n");
        sb.append("RESPONSE CODE: " + cause.getResponseCode());
        sb.append("\n");
        sb.append("ERROR DESCRIPTION: " + cause.getErrorDescription());
        sb.append("\n");
        sb.append("ERROR INFO: " + cause.getErrorInfo());
        sb.append("\n");
        sb.append("ERROR TYPE: " + cause.getErrorType().toString());
        sb.append("\n");
        sb.append("EXCEPTION ACTION: " + cause.getExceptionAction().toString());
        sb.append("\n");
        sb.append("---------------------------------------------- \n");
        return sb.toString();
    }

}