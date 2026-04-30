package org.cts.adm.finguard.ComplianceReporting.ExceptionHandling;


public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}