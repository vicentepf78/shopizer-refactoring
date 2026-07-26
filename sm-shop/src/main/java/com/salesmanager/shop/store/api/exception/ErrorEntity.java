package com.salesmanager.shop.store.api.exception;

public class ErrorEntity {
    private String errorCode;
    private String message;
    private String correlationId;
    public String getErrorCode() {
      return errorCode;
    }
    public void setErrorCode(String errorCode) {
      this.errorCode = errorCode;
    }
    public String getMessage() {
      return message;
    }
    public void setMessage(String message) {
      this.message = message;
    }
    public String getCorrelationId() {
      return correlationId;
    }
    public void setCorrelationId(String correlationId) {
      this.correlationId = correlationId;
    }
}
