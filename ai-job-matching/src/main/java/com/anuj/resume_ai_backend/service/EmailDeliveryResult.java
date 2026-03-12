package com.anuj.resume_ai_backend.service;

public record EmailDeliveryResult(String status, String message) {

    public static EmailDeliveryResult sent(String message) {
        return new EmailDeliveryResult("sent", message);
    }

    public static EmailDeliveryResult skipped(String message) {
        return new EmailDeliveryResult("skipped", message);
    }

    public static EmailDeliveryResult failed(String message) {
        return new EmailDeliveryResult("failed", message);
    }
}
