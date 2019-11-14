package com.formula.api.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
import com.plaid.client.request.ItemPublicTokenExchangeRequest;
import com.plaid.client.response.ItemPublicTokenExchangeResponse;

import okhttp3.logging.HttpLoggingInterceptor;

@RestController
public class PlaidController {

	@Value("${plaid.public.key}")
	private String plaidPublicKey;

	@Value("${plaid.secret.key}")
	private String plaidSecretKey;

	@Value("${plaid.client.id}")
	private String plaidClientId;

//	@JsonProperty("public_token")
//	private String publicToken;

	@RequestMapping(value = "/get_access_token", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<HashMap<String, Object>> getAccessToken(@Valid @RequestBody Map<String, Object> body) throws IOException {
		HashMap<String, Object> resp = new HashMap<String, Object>();
		
		String publicToken = (String) body.get("publicToken");

		PlaidClient pc = PlaidClient.newBuilder().sandboxBaseUrl().clientIdAndSecret(plaidClientId, plaidSecretKey)
				.publicKey(plaidPublicKey).logLevel(HttpLoggingInterceptor.Level.BODY).build();

		// Synchronously exchange a Link public_token for an API access_token
		// Required request parameters are always Request object constructor arguments
		retrofit2.Response<ItemPublicTokenExchangeResponse> response = pc.service()
				.itemPublicTokenExchange(new ItemPublicTokenExchangeRequest(publicToken)).execute();

		String accessToken = "";
		if (response.isSuccessful()) {
			accessToken = response.body().getAccessToken();
		}

		resp.put("status", "success");
		resp.put("accessToken", accessToken);
		
		return new ResponseEntity<HashMap<String, Object>>(resp, HttpStatus.OK);
	}


//	public String createPlaidClient() throws IOException {


}
