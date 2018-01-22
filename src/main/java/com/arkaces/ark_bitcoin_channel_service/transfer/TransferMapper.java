package com.arkaces.ark_bitcoin_channel_service.transfer;

import org.springframework.stereotype.Service;

import java.time.ZoneOffset;

@Service
public class TransferMapper {
    
    public Transfer map(TransferEntity transferEntity) {
        Transfer transfer = new Transfer();
        transfer.setId(transferEntity.getId());
        transfer.setStatus(transferEntity.getStatus());
        transfer.setArkTransactionId(transferEntity.getArkTransactionId());
        transfer.setBtcSendAmount(transferEntity.getBtcSendAmount().toPlainString());
        transfer.setBtcTransactionId(transferEntity.getBtcTransactionId());
        transfer.setArkAmount(transferEntity.getArkAmount().toPlainString());
        transfer.setArkFlatFee(transferEntity.getArkFlatFee().toPlainString());
        transfer.setArkPercentFee(transferEntity.getArkPercentFee().toPlainString());
        transfer.setArkToBtcRate(transferEntity.getArkToBtcRate().toPlainString());
        transfer.setArkTotalFee(transferEntity.getArkTotalFee().toPlainString());
        transfer.setCreatedAt(transferEntity.getCreatedAt().atOffset(ZoneOffset.UTC).toString());
        
        return transfer;
    }
    
}
