package org.coble.core.camel.exception.handling;

public class EnumerationTypes {

    public enum ExceptionAction {
        LOG, EMAIL, DLQ, CLIENT_RESPONSE_HEADER
    }

    public enum ErrorType {
        RUNTIME, BUSINESS, UNSUPPORTED_OPERATION
    }

    public enum ResponseCode {
        BUSINESS_FAULT, SUCCESS, UNABLE_TO_COMPLETE_OPERATION
    }
}