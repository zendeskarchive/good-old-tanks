package com.getbase.hackkrk.tanks.api;

import java.util.Optional;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.message.GZipEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import jersey.repackaged.com.google.common.base.Throwables;

public class TanksClient {
    private static final Logger log = LoggerFactory.getLogger(TanksClient.class);
    private String url;
    private String tournamentId;
    private String accessToken;
    private Client client;
    private ObjectMapper mapper;

    public TanksClient(String url, String tournamentId, String accessToken) {
        this.url = url;
        this.tournamentId = tournamentId;
        this.accessToken = accessToken;
        this.client = ClientBuilder.newClient();
        client.property(ClientProperties.CONNECT_TIMEOUT, 300000);
        client.property(ClientProperties.READ_TIMEOUT,    300000);
        client.register(GZipEncoder.class);
        this.mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public TurnResult submitMove(Command move) {
        log.info("Sending {}", move);
        return request("moves", TurnResult.class, Optional.of(move));
    }
    
    public GameSetup getMyGameSetup() {
        return request("games/my/setup", GameSetup.class, Optional.empty());
    }

    private <RESULT, POST> RESULT request(String path, Class<RESULT> responseType, Optional<POST> post) {
        Builder builder = client.target(url + "/tournaments/" + tournamentId + "/")
                .path(path)
                .request()
                .header("Authorization", accessToken)
                .header("Accept-Encoding", "gzip");

        Response response = sendRequest(post, builder);

        return readResponse(responseType, response);
        
    }

    private <RESULT> RESULT readResponse(Class<RESULT> responseType, Response response) {
        try {
            String responseText = response.readEntity(String.class);
            log.info(responseText);
            if (response.getStatus() != 200) {
                throw new CallFailedException(response.getStatus(), responseText);
            }
            return mapper.readValue(responseText, responseType);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        } finally {
            response.close();
        }
    }

    private <POST> Response sendRequest(Optional<POST> post, Builder builder) {
        if (post.isPresent()) {
            String requestJson = serialize(post.get());
            return builder.post(Entity.entity(requestJson, MediaType.APPLICATION_JSON));
        } else {
            return builder.get();
        }
    }

    private String serialize(Object move) {
        try {
            return mapper.writeValueAsString(move);
        } catch (JsonProcessingException e) {
            throw Throwables.propagate(e);
        }
    }

    @SuppressWarnings("serial")
    static class CallFailedException extends RuntimeException {
        public CallFailedException(int status, String responseText) {
            super("Call failed. " + responseText);
        }
    }
}
