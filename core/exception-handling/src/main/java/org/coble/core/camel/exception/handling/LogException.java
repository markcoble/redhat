package org.coble.core.camel.exception.handling;

import org.coble.core.camel.exception.handling.EnumerationTypes.ErrorType;
import org.coble.core.camel.exception.handling.EnumerationTypes.ExceptionAction;
import org.coble.core.camel.exception.handling.EnumerationTypes.ResponseCode;

public class LogException extends CamelException {

    private static final long serialVersionUID = 1L;

    /**
     * Public constructor for use from Camel Route where ENUM type cannot be passed.
     * 
     * @param errorType
     */
    public LogException(String errorType) {

        super(ErrorType.valueOf(errorType), ExceptionAction.LOG);
    }

    /**
     * Custom Tru exception for logging exception thrown from a Camel Context.
     * 
     * @param responseCode
     * @param errorType
     * @param errorInfo
     * @param errorDescription
     */
    public LogException(ResponseCode responseCode, ErrorType errorType,
            String errorInfo,
            String errorDescription) {
        super(ExceptionAction.LOG, responseCode, errorType, errorInfo,
                errorDescription);

    }
}
