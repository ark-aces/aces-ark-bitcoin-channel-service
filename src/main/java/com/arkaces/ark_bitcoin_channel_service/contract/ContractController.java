package com.arkaces.ark_bitcoin_channel_service.contract;

import com.arkaces.ApiException;
import com.arkaces.aces_listener_api.AcesListenerApi;
import com.arkaces.aces_server.aces_service.contract.Contract;
import com.arkaces.aces_server.aces_service.contract.ContractStatus;
import com.arkaces.aces_server.aces_service.contract.CreateContractRequest;
import com.arkaces.aces_server.aces_service.error.ServiceErrorCodes;
import com.arkaces.aces_server.common.error.NotFoundException;
import com.arkaces.aces_server.common.identifer.IdentifierGenerator;
import io.ark.core.Crypto;
import io.swagger.client.model.Subscription;
import io.swagger.client.model.SubscriptionRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class ContractController {
    
    private final IdentifierGenerator identifierGenerator;
    private final ContractRepository contractRepository;
    private final ContractMapper contractMapper;
    private final AcesListenerApi arkListener;
    private final String arkEventCallbackUrl;

    @PostMapping("/contracts")
    public Contract<Results> postContract(@RequestBody CreateContractRequest<Arguments> createContractRequest) {
        ContractEntity contractEntity = new ContractEntity();
        contractEntity.setCorrelationId(createContractRequest.getCorrelationId());
        contractEntity.setRecipientBtcAddress(createContractRequest.getArguments().getRecipientBtcAddress());
        contractEntity.setCreatedAt(LocalDateTime.now());
        contractEntity.setId(identifierGenerator.generate());
        contractEntity.setStatus(ContractStatus.EXECUTED);
        
        // generate bitcoin wallet for deposits
        String depositArkAddressPassphrase = identifierGenerator.generate();

        String depositArkAddress = Crypto.getAddress(Crypto.getKeys(depositArkAddressPassphrase));
        contractEntity.setDepositArkAddress(depositArkAddress);
        contractEntity.setDepositArkAddressPassphrase(depositArkAddressPassphrase);

        // subscribe to bitcoin listener on deposit bitcoin address
        SubscriptionRequest subscriptionRequest = new SubscriptionRequest();
        subscriptionRequest.setCallbackUrl(arkEventCallbackUrl);
        subscriptionRequest.setMinConfirmations(1);
        subscriptionRequest.setRecipientAddress(depositArkAddress);
        Subscription subscription;
        try {
            subscription = arkListener.subscriptionsPost(subscriptionRequest);
            log.info("subscription: " + subscription.toString());
        } catch (ApiException e) {
            throw new RuntimeException("Ark Listener subscription failed to POST", e);
        }
        contractEntity.setSubscriptionId(subscription.getId());

        contractRepository.save(contractEntity);

        return contractMapper.map(contractEntity);
    }
    
    @GetMapping("/contracts/{contractId}")
    public Contract<Results> getContract(@PathVariable String contractId) {
        ContractEntity contractEntity = contractRepository.findOneById(contractId);
        if (contractEntity == null) {
            throw new NotFoundException(ServiceErrorCodes.CONTRACT_NOT_FOUND, "Contract not found with id = " + contractId);
        }
        
        return contractMapper.map(contractEntity);
    }
}
