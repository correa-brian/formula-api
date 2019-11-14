package com.formula.api.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.plaid.client.PlaidClient;
import com.plaid.client.request.ItemPublicTokenCreateRequest;
import com.plaid.client.request.ItemPublicTokenExchangeRequest;
import com.plaid.client.response.ItemPublicTokenCreateResponse;
import com.plaid.client.response.ItemPublicTokenExchangeResponse;

import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Response;

@RestController
public class PlaidController {

	@Value("${plaid.public.key}")
	private String plaidPublicKey;

	@Value("${plaid.secret.key}")
	private String plaidSecretKey;

	@Value("${plaid.client.id}")
	private String plaidClientId;

	private String accessToken = "";
	private String publicToken = "";
	private PlaidClient plaidClient;

	public PlaidController() {
	}

	@PostConstruct
	public void init() {
		this.plaidClient = PlaidClient.newBuilder().sandboxBaseUrl()
				.clientIdAndSecret(plaidClientId, plaidSecretKey).publicKey(plaidPublicKey)
				.logLevel(HttpLoggingInterceptor.Level.BODY).build();
	}

	@RequestMapping(value = "/fetch_access_token", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<HashMap<String, Object>> fetchAccessToken(@Valid @RequestBody Map<String, String> body)
			throws IOException {
		HashMap<String, Object> resp = new HashMap<String, Object>();
		String publicToken = body.get("publicToken");

		// Ask item/public_token/exchange for an access token
		Response<ItemPublicTokenExchangeResponse> response = plaidClient.service()
				.itemPublicTokenExchange(new ItemPublicTokenExchangeRequest(publicToken)).execute();

		if (response.isSuccessful()) {
			accessToken = response.body().getAccessToken();
		}

		resp.put("status", "success");
		resp.put("accessToken", accessToken);
		return new ResponseEntity<HashMap<String, Object>>(resp, HttpStatus.OK);
	}

	// ask /item/public_token/create with an access_token to generate a new
	// public_token
	public ResponseEntity<HashMap<String, Object>> fetchPublicToken() throws IOException {
		Response<ItemPublicTokenCreateResponse> response = plaidClient.service()
				.itemPublicTokenCreate(new ItemPublicTokenCreateRequest(accessToken)).execute();

		if (response.isSuccessful()) {
			publicToken = response.body().getPublicToken();
		}

		HashMap<String, Object> resp = new HashMap<String, Object>();
		resp.put("status", "success");
		resp.put("publicToken", publicToken);

		return new ResponseEntity<HashMap<String, Object>>(resp, HttpStatus.OK);
	}

	public String getPublicToken() {
		return publicToken;
	}

	public String getAccessToken() {
		return accessToken;
	}


}
