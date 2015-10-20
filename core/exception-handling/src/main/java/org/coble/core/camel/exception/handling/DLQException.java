package org.coble.core.camel.exception.handling;

import org.coble.core.camel.exception.handling.EnumerationTypes.ErrorType;
import org.coble.core.camel.exception.handling.EnumerationTypes.ExceptionAction;
import org.coble.core.camel.exception.handling.EnumerationTypes.ResponseCode;

public class DLQException extends CamelException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public DLQException(ErrorType errorType) {
        super(errorType, ExceptionAction.DLQ);
    }

    /**
     * Custom Tru exception for DLQ exception thrown from a Camel Context.
     * 
     * @param responseCode
     * @param errorType
     * @param errorInfo
     * @param errorDescription
     */
    public DLQException(ResponseCode responseCode, ErrorType errorType,
            String errorInfo,
            String errorDescription) {
        super(ExceptionAction.DLQ, responseCode, errorType, errorInfo,
                errorDescription);

    }

    public DLQException(Throwable cause) {
        super(cause);
    }

}
