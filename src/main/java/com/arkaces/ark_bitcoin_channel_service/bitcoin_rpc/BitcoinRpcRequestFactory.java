package com.arkaces.ark_bitcoin_channel_service.bitcoin_rpc;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BitcoinRpcRequestFactory {

    public BitcoinRpcRequest create(String method, List<Object> params) {
        BitcoinRpcRequest request = new BitcoinRpcRequest();
        request.setMethod(method);
        request.setParams(params);
        return request;
    }
}
