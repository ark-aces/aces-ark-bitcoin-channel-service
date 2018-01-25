package com.arkaces.ark_bitcoin_channel_service;

import ark_java_client.*;
import com.arkaces.ApiClient;
import com.arkaces.aces_listener_api.AcesListenerApi;
import com.arkaces.aces_server.aces_service.config.AcesServiceConfig;
import com.arkaces.aces_server.ark_auth.ArkAuthConfig;
import com.arkaces.ark_bitcoin_channel_service.bitcoin_rpc.BitcoinRpcSettings;
import org.h2.tools.Server;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import java.sql.SQLException;

@Configuration
@EnableScheduling
@Import({AcesServiceConfig.class, ArkAuthConfig.class})
@EnableJpaRepositories
@EntityScan
public class ApplicationConfig {

    @Bean
    public ArkClient arkClient(Environment environment) {
        ArkNetworkFactory arkNetworkFactory = new ArkNetworkFactory();
        String arkNetworkConfigPath = environment.getProperty("arkNetworkConfigPath");
        ArkNetwork arkNetwork = arkNetworkFactory.createFromYml(arkNetworkConfigPath);

        HttpArkClientFactory httpArkClientFactory = new HttpArkClientFactory();
        return httpArkClientFactory.create(arkNetwork);
    }

    @Bean
    public AcesListenerApi arkListener(Environment environment) {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(environment.getProperty("arkListener.url"));
        if (environment.containsProperty("arkListener.apikey")) {
            apiClient.setUsername("token");
            apiClient.setPassword(environment.getProperty("arkListener.apikey"));
        }

        return new AcesListenerApi(apiClient);
    }

    @Bean
    public RestTemplate bitcoinRpcRestTemplate(BitcoinRpcSettings bitcoinRpcSettings) {
        return new RestTemplateBuilder()
                .rootUri(bitcoinRpcSettings.getUrl())
                .basicAuthorization(bitcoinRpcSettings.getUsername(), bitcoinRpcSettings.getPassword())
                .build();
    }

    @Bean
    public String arkEventCallbackUrl(Environment environment) {
        return environment.getProperty("arkEventCallbackUrl");
    }

    @Bean(initMethod="start", destroyMethod="stop")
    public Server h2WebConsonleServer () throws SQLException {
        return Server.createWebServer("-web","-webAllowOthers","-webDaemon","-webPort", "8082");
    }

}
