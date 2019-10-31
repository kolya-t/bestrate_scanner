package io.mywish.neocli4j;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mywish.neocli4j.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
public class NeoClientImpl implements NeoClient {
    private final HttpClient httpClient;
    private final URI rpc;
    private final ObjectMapper objectMapper;
    private final Charset UTF8;

    public NeoClientImpl(HttpClient httpClient, URI rpc, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.rpc = rpc;
        this.objectMapper = objectMapper;
        this.UTF8 = Charset.forName("UTF-8");
    }

    private <T extends JsonRpcResponse> T doRequest(final Class<T> clazz, final String method, final Object... params) throws java.io.IOException {
        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest("2.0", "mywishscanner", method, Arrays.asList(params));
        String json = objectMapper.writeValueAsString(jsonRpcRequest);
        HttpPost httpPost = new HttpPost(rpc);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setEntity(new StringEntity(json));
        HttpResponse httpResponse = httpClient.execute(httpPost);
        // TODO: process result codes
        HttpEntity entity = httpResponse.getEntity();
        // TODO: handle empty response
        String responseBody = EntityUtils.toString(entity, UTF8);
        // TODO: handle deserialize exceptions (if needed)
        T response = objectMapper.readValue(responseBody, clazz);
        if (response.getError() != null) {
            throw new IOException("Exception in method '" + method + "'"
                    + " with arguments "
                    + objectMapper.writeValueAsString(params)
                    + ". Error code: "
                    + response.getError().getCode()
                    + ". Error message: "
                    + response.getError().getMessage()
            );
        }
        return response;
    }

    private <T extends JsonRpcResponse> T doRequest(final Class<T> clazz, final String method) throws java.io.IOException {
        return doRequest(clazz, method, new Object[]{});
    }

    @Override
    public Integer getBlockCount() throws java.io.IOException {
        GetBlockCountResponse response = doRequest(GetBlockCountResponse.class, "getblockcount");
        return response.getResult();
    }

    @Override
    public Block getBlock(Long blockNumber) throws java.io.IOException {
        GetBlockResponse response = doRequest(GetBlockResponse.class, "getblock", blockNumber, "1");
        Block block = response.getResult();
        block.getTransactions().forEach(this::initTransaction);
        return block;
    }

    @Override
    public Block getBlock(String blockHash) throws java.io.IOException {
        GetBlockResponse response = doRequest(GetBlockResponse.class, "getblock", blockHash, "1");
        Block block = response.getResult();
        block.getTransactions().forEach(this::initTransaction);
        return block;
    }

    @Override
    public Transaction getTransaction(String txHash, boolean getInputs) throws java.io.IOException {
        GetTransactionResponse response = doRequest(GetTransactionResponse.class, "getrawtransaction", txHash, "1");
        Transaction tx = response.getResult();
        if (getInputs) {
            initTransaction(tx);
        }
        return tx;
    }

    @Override
    public List<Event> getEvents(String txHash) throws java.io.IOException {
        GetApplicationLogResponse response = doRequest(GetApplicationLogResponse.class, "getapplicationlog", txHash);
        if (response.getResult() == null || response.getResult().isEmpty()) {
            return Collections.emptyList();
        }
        return response.getResult();
    }

    @Override
    public BigInteger getBalance(String address) throws java.io.IOException {
        GetAccountStateResponse response = doRequest(GetAccountStateResponse.class, "getaccountstate", address);
        AccountState accountState = response.getResult();
        if (accountState == null
                || accountState.getBalances() == null
                || accountState.getBalances().isEmpty()) {
            return BigInteger.ZERO;
        }
        return accountState.getBalances()
                .stream()
                .filter(balance -> "0xc56f33fc6ecfcd0c225c4ab356fee59390af8560be0e930faebe74a6daff7c9b".equalsIgnoreCase(balance.getAsset()))
                .map(Balance::getValue)
                .findFirst()
                .orElse(BigInteger.ZERO);
    }

    private void initTransaction(Transaction transaction) {
        transaction.getInputs().forEach(input -> {
            try {
                Integer vout = input.getVout();
                Transaction prevTransaction = getTransaction(input.getTxid(), false);
                TransactionOutput txOutput = prevTransaction.getOutputs().get(vout);
                input.setConnectedOutput(txOutput);
            }
            catch (java.io.IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }
}
