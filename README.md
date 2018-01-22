# aces-service-btc-ark-channel
ACES ARK-BTC transfer channel service


## Using Service

Get service info:

```
curl http://localhost:9191/
```
```
{
  "name" : "Aces ARK-BTC Channel Service",
  "description" : "ACES ARK to BTC Channel service for value transfers.",
  "version" : "1.0.0",
  "websiteUrl" : "https://arkaces.com",
  "instructions" : "After this contract is executed, any ARK sent to depositArkAddress will be exchanged for BTC and  sent directly to the given recipientBtcAddress less service fees.\n",
  "flatFee" : "0",
  "percentFee" : "1.00%",
  "capacities": [{
    "value": "50.00",
    "unit": "BTC",
    "displayValue": "50 BTC"
  }],
  "inputSchema" : {
    "type" : "object",
    "properties" : {
      "recipientBtcAddress" : {
        "type" : "string"
      }
    },
    "required" : [ "recipientBtcAddress" ]
  },
  "outputSchema" : {
    "type" : "object",
    "properties" : {
      "depositArkAddress" : {
        "type" : "string"
      },
      "recipientBtcAddress" : {
        "type" : "string"
      },
      "transfers" : {
        "type" : "array",
        "properties" : {
          "arkAmount" : {
            "type" : "string"
          },
          "arkToBtcRate" : {
            "type" : "string"
          },
          "arkFlatFee" : {
            "type" : "string"
          },
          "arkPercentFee" : {
            "type" : "string"
          },
          "arkTotalFee" : {
            "type" : "string"
          },
          "btcSendAmount" : {
            "type" : "string"
          },
          "btcTransactionId" : {
            "type" : "string"
          },
          "createdAt" : {
            "type" : "string"
          }
        }
      }
    }
  }
}
```

Create a new Service Contract:

```
curl -X POST http://localhost:9191/contracts \
-H 'Content-type: application/json' \
-d '{
  "arguments": {
    "recipientBtcAddress": "muWWAMMKpKLb7toJrHscHXF91f87ZVkuNW"
  }
}' 
```

```
{
  "id": "abe05cd7-40c2-4fb0-a4a7-8d2f76e74978",
  "createdAt": "2017-07-04T21:59:38.129Z",
  "correlationId": "4aafe9-4a40-a7fb-6e788d2497f7",
  "status": "executed",
  "results": {
  
    "recipientBtcAddress": "muWWAMMKpKLb7toJrHscHXF91f87ZVkuNW",
    "depositArkAddress": "ARNJJruY6RcuYCXcwWsu4bx9kyZtntqeAx",
    "transfers": []
}
```

Get Contract information after sending ARK funds to `depositArkAddress`:

```
curl -X GET http://localhost:9191/contracts/abe05cd7-40c2-4fb0-a4a7-8d2f76e74978
```

```
{
  "id": "abe05cd7-40c2-4fb0-a4a7-8d2f76e74978",
  "createdAt": "2017-07-04T21:59:38.129Z",
  "correlationId": "4aafe9-4a40-a7fb-6e788d2497f7",
  "status": "executed",
  "results": {
    "recipientBtcAddress": "muWWAMMKpKLb7toJrHscHXF91f87ZVkuNW",
    "depositArkAddress": "ARNJJruY6RcuYCXcwWsu4bx9kyZtntqeAx",
    "transfers" : [ {
      "id" : "uDui0F8PIjldKyGm0rdd",
      "status" : "completed",
      "createdAt" : "2018-01-21T20:24:52.057Z",
      "arkTransactionId" : "78b6c99c40451d7e46f2eb41cdb831d087fecd759b01e00fd69e34959b5bee25",
      "arkAmount" : "109.100000",
      "arkToBtcRate" : "0.00050370",
      "arkFlatFee" : "0.00000000",
      "arkPercentFee" : "1.00000000",
      "arkTotalFee" : "0.00001000",
      "btcSendAmount" : "0.00100000"
    } ]
  }
}
```