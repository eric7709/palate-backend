package com.app.palate.payment;

public record MonnifyInvoiceResponse(
    String accountNumber,
    String bankName,
    double amount,
    Long orderId,
    String invoiceReference
) {}