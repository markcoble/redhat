package org.coble.core.camel.exception.handling;

import org.coble.core.camel.exception.handling.EnumerationTypes.ErrorType;
import org.coble.core.camel.exception.handling.EnumerationTypes.ExceptionAction;
import org.coble.core.camel.exception.handling.EnumerationTypes.ResponseCode;

public class CRHException extends CamelException {

    private static final long serialVersionUID = 1L;

    /**
     * Public constructor for use from Camel Route where ENUM type cannot be passed.
     * 
     * @param errorType
     */
    public CRHException(String errorType) {

        super(ErrorType.valueOf(errorType),
                ExceptionAction.CLIENT_RESPONSE_HEADER);
    }
    
    /**
     * Custom Tru exception for Client Reply Header exception thrown from a Camel Context.
     * 
     * @param responseCode
     * @param errorType
     * @param errorInfo
     * @param errorDescription
     */
    public CRHException(ResponseCode responseCode, ErrorType errorType,
            String errorInfo,
            String errorDescription) {
        super(ExceptionAction.LOG, responseCode, errorType, errorInfo,
                errorDescription);

    }

}
