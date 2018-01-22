package com.arkaces.ark_bitcoin_channel_service.transfer;

import lombok.Data;

@Data
public class ArkTransactionEventPayload {
    private String id;
    private String transactionId;
    private ArkTransaction data;
    private String createdAt;
    private String subscriptionId;
}

