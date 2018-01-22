package com.arkaces.ark_bitcoin_channel_service;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "serviceBtcAccount")
public class ServiceArkAccountSettings {
    private String address;
}
