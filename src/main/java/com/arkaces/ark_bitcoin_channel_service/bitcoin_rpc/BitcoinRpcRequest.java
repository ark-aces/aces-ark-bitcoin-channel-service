package com.arkaces.ark_bitcoin_channel_service.bitcoin_rpc;

import lombok.Data;

import java.util.List;

@Data
public class BitcoinRpcRequest {
    private String jsonrpc = "1.0";
    private String id = "curltext";
    private String method;
    private List<Object> params;
}
