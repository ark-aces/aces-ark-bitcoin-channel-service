package com.arkaces.ark_bitcoin_channel_service.transfer;

import ark_java_client.ArkClient;
import com.arkaces.aces_server.common.identifer.IdentifierGenerator;
import com.arkaces.aces_server.aces_service.notification.NotificationService;
import com.arkaces.ark_bitcoin_channel_service.FeeSettings;
import com.arkaces.ark_bitcoin_channel_service.ark.ArkSatoshiService;
import com.arkaces.ark_bitcoin_channel_service.bitcoin_rpc.BitcoinService;
import com.arkaces.ark_bitcoin_channel_service.contract.ContractEntity;
import com.arkaces.ark_bitcoin_channel_service.contract.ContractRepository;
import com.arkaces.ark_bitcoin_channel_service.exchange_rate.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@RestController
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ArkTransactionEventHandler {

    private final ContractRepository contractRepository;
    private final TransferRepository transferRepository;
    private final IdentifierGenerator identifierGenerator;
    private final ExchangeRateService exchangeRateService;
    private final ArkClient arkClient;
    private final ArkSatoshiService arkSatoshiService;
    private final FeeSettings feeSettings;
    private final BitcoinService bitcoinService;
    private final BigDecimal lowCapacityThreshold;
    private final NotificationService notificationService;

    @PostMapping("/arkEvents")
    public ResponseEntity<Void> handleBitcoinEvent(@RequestBody ArkTransactionEventPayload eventPayload) {
        String arkTransactionId = eventPayload.getTransactionId();
        
        log.info("Received Ark event: " + arkTransactionId + " -> " + eventPayload.getData());
        
        String subscriptionId = eventPayload.getSubscriptionId();
        ContractEntity contractEntity = contractRepository.findOneBySubscriptionId(subscriptionId);
        if (contractEntity != null) {
            // todo: lock contract for update to prevent concurrent processing of a listener transaction.
            // Listeners send events serially, so that shouldn't be an issue, but we might want to lock
            // to be safe.

            log.info("Matched event for contract id " + contractEntity.getId() + " btc transaction id " + arkTransactionId);

            String transferId = identifierGenerator.generate();

            TransferEntity transferEntity = new TransferEntity();
            transferEntity.setId(transferId);
            transferEntity.setCreatedAt(LocalDateTime.now());
            transferEntity.setArkTransactionId(arkTransactionId);
            transferEntity.setContractEntity(contractEntity);

            // Get Ark amount from transaction
            ArkTransaction arkTransaction = eventPayload.getData();

            BigDecimal incomingArkAmount = arkSatoshiService.toArk(arkTransaction.getAmount());
            transferEntity.setArkAmount(incomingArkAmount);

            BigDecimal arkToBtcRate = exchangeRateService.getRate("ARK", "BTC"); // 1/2027.58, Ark 8, Btc 15000
            transferEntity.setArkToBtcRate(arkToBtcRate);

            // Deduct fees
            transferEntity.setArkFlatFee(feeSettings.getArkFlatFee());
            transferEntity.setArkPercentFee(feeSettings.getArkPercentFee());

            BigDecimal percentFee = feeSettings.getArkPercentFee()
                    .divide(new BigDecimal("100.00"), 8, BigDecimal.ROUND_HALF_UP);
            BigDecimal arkTotalFeeAmount = incomingArkAmount.multiply(percentFee).add(feeSettings.getArkFlatFee());
            transferEntity.setArkTotalFee(arkTotalFeeAmount);

            // Calculate send ark amount
            BigDecimal arkSendAmount = incomingArkAmount.subtract(arkTotalFeeAmount);
            BigDecimal btcSendAmount = arkSendAmount.multiply(arkToBtcRate).setScale(8, RoundingMode.HALF_DOWN);
            // todo: subtract bitcoin transaction fee
            BigDecimal minBtcAmount = BigDecimal.ZERO; // todo: this should be expected btc txn fee
            if (btcSendAmount.compareTo(minBtcAmount) <= 0) {
                btcSendAmount = BigDecimal.ZERO;
            }
            transferEntity.setBtcSendAmount(btcSendAmount);

            transferEntity.setStatus(TransferStatus.NEW);
            transferRepository.save(transferEntity);

            // Check that service has enough btc to send
            // todo: get btc balance? might not be possible per https://bitcoin.stackexchange.com/questions/10090/how-to-get-an-addresss-balance-with-the-bitcoin-client
            // todo: we may need to just depend on confirmations
            BigDecimal serviceAvailableBtc = btcSendAmount;

            // Send btc transaction
            if (btcSendAmount.compareTo(BigDecimal.ZERO) >= 0) {
                String btcTransactionId = bitcoinService.sendValue(contractEntity.getRecipientBtcAddress(), btcSendAmount);
                transferEntity.setBtcTransactionId(btcTransactionId);

                log.info("Sent " + btcSendAmount + " btc to " + contractEntity.getRecipientBtcAddress()
                        + ", btc transaction id " + btcTransactionId + ", ark transaction " + arkTransactionId);

                notificationService.notifySuccessfulTransfer(
                        transferEntity.getContractEntity().getId(),
                        transferEntity.getId()
                );

                // todo: asynchronously confirm transaction, if transaction fails to confirm we should return btc amount
                transferEntity.setNeedsBtcConfirmation(true);
            }

            transferEntity.setStatus(TransferStatus.COMPLETE);
            notificationService.notifySuccessfulTransfer(
                    transferEntity.getContractEntity().getId(),
                    transferEntity.getId()
            );

            transferRepository.save(transferEntity);
            
            log.info("Saved transfer id " + transferEntity.getId() + " to contract " + contractEntity.getId());
        }
        
        return ResponseEntity.ok().build();
    }
}
