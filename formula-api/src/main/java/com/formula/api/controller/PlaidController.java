package com.formula.api.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.plaid.client.PlaidClient;

@RestController
public class PlaidController {

	@Value("${plaid.public.key}")
	private String plaidPublicKey;

	@Value("${plaid.secret.key}")
	private String plaidSecretKey;

	@Value("${plaid.client.id}")
	private String plaidClientId;

	@GetMapping("/plaid")
	public void run() {

		PlaidClient pc = createPlaidClient();
	}

	public PlaidClient createPlaidClient() {
		System.out.println("createPC!!!");
		PlaidClient pc = PlaidClient.newBuilder().sandboxBaseUrl().clientIdAndSecret(plaidClientId, plaidSecretKey)
				.publicKey(plaidPublicKey).build();

//		// Synchronously exchange a Link public_token for an API access_token
//		// Required request parameters are always Request object constructor arguments
//		Response<ItemPublicTokenExchangeResponse> response = pc.service()
//				.itemPublicTokenExchange(new ItemPublicTokenExchangeRequest("the_link_public_token")).execute();
//
//		if (response.isSuccessful()) {
//			accessToken = response.body().getAccessToken();
//		}
//
//		// Asynchronously do the same thing. Useful for potentially long-lived calls.
//		pc.service().itemPublicTokenExchange(new ItemPublicTokenExchangeRequest(publicToken))
//				.enqueue(new Callback<ItemPublicTokenExchangeResponse>() {
//					@Override
//					public void onResponse(Call<ItemPublicTokenExchangeResponse> call,
//							Response<ItemPublicTokenExchangeResponse> response) {
//						if (response.isSuccessful()) {
//							accessToken = response.body.getAccessToken();
//						}
//					}
//
//					@Override
//					public void onFailure(Call<ItemPublicTokenExchangeResponse> call, Throwable t) {
//						// handle the failure as needed
//					}
//				});
//
//		// Decoding an unsuccessful response
//		try {
//			ErrorResponse errorResponse = pc.parseError(response);
//		} catch (Exception e) {
//			// deal with it. you didn't even receive a well-formed JSON error response.
//		}
//		
//		
//		// Generate a public_token for a given institution ID
//		// and set of initial products
//		Response<SandboxPublicTokenCreateResponse> createResponse =
//		  client().service().sandboxPublicTokenCreate(
//		    new SandboxPublicTokenCreateRequest(INSTITUTION_ID, 
//		INITIAL_PRODUCTS)
//		  ).execute();
//		// The generated public_token can now be
//		// exchanged for an access_token
//		Response<ItemPublicTokenExchangeResponse> exchangeResponse =
//		  client().service().itemPublicTokenExchange(
//		    new 
//		ItemPublicTokenExchangeRequest(createResponse.body().getPublicToken())
//		  ).execute();

		return null;
	}

}
