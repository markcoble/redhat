package org.coble.core.camel.exception.handling;

import org.coble.core.camel.exception.handling.EnumerationTypes.ErrorType;
import org.coble.core.camel.exception.handling.EnumerationTypes.ExceptionAction;
import org.coble.core.camel.exception.handling.EnumerationTypes.ResponseCode;

/**
 * Exception class to be thrown by all Tru Camel Contexts.
 * 
 * @author mcoble
 * 
 */
public abstract class CamelException extends Exception {

    private ErrorType errorType;

    private ExceptionAction exceptionAction;

    private ResponseCode responseCode;
    private String errorInfo;
    private String errorDescription;

    private static final long serialVersionUID = 1L;

    public CamelException(Throwable cause) {
        super(cause);
    }

    public CamelException(ErrorType errorType, ExceptionAction action) {
        super(errorType.toString());
        this.setErrorType(errorType);
        this.setExceptionAction(action);
    }

    public CamelException(ExceptionAction action, ResponseCode responseCode,
            ErrorType errorType, String errorInfo,
            String errorDescription) {
        super(errorType.toString());
        this.setExceptionAction(action);
        this.responseCode = responseCode;
        this.setErrorType(errorType);
        this.errorInfo = errorInfo;
        this.errorDescription = errorDescription;
    }

    public CamelException(ExceptionAction action) {
        super(ErrorType.BUSINESS.toString());
        this.setErrorType(ErrorType.BUSINESS);
        this.setExceptionAction(action);
    }

    /**
     * @return the responseCode
     */
    public ResponseCode getResponseCode() {
        return responseCode;
    }

    /**
     * @param responseCode the responseCode to set
     */
    public void setResponseCode(ResponseCode responseCode) {
        this.responseCode = responseCode;
    }

    /**
     * @return the errorInfo
     */
    public String getErrorInfo() {
        return errorInfo;
    }

    /**
     * @param errorInfo the errorInfo to set
     */
    public void setErrorInfo(String errorInfo) {
        this.errorInfo = errorInfo;
    }

    /**
     * @return the errorDescription
     */
    public String getErrorDescription() {
        return errorDescription;
    }

    /**
     * @param errorDescription the errorDescription to set
     */
    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    /**
     * @return the errorType
     */
    public ErrorType getErrorType() {
        return errorType;
    }

    /**
     * @param errorType the errorType to set
     */
    public void setErrorType(ErrorType errorType) {
        this.errorType = errorType;
    }

    /**
     * @return the exceptionAction
     */
    public ExceptionAction getExceptionAction() {
        return exceptionAction;
    }

    /**
     * @param exceptionAction the exceptionAction to set
     */
    public void setExceptionAction(ExceptionAction exceptionAction) {
        this.exceptionAction = exceptionAction;
    }

}
