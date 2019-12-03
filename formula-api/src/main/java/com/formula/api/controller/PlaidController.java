package com.formula.api.controller;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.formula.api.model.plaid.PlaidAPIError;
import com.plaid.client.PlaidClient;
import com.plaid.client.request.AccountsBalanceGetRequest;
import com.plaid.client.request.AccountsGetRequest;
import com.plaid.client.request.AssetReportCreateRequest;
import com.plaid.client.request.AssetReportGetRequest;
import com.plaid.client.request.AuthGetRequest;
import com.plaid.client.request.CategoriesGetRequest;
import com.plaid.client.request.IdentityGetRequest;
import com.plaid.client.request.IncomeGetRequest;
import com.plaid.client.request.InvestmentsHoldingsGetRequest;
import com.plaid.client.request.InvestmentsTransactionsGetRequest;
import com.plaid.client.request.ItemGetRequest;
import com.plaid.client.request.ItemPublicTokenCreateRequest;
import com.plaid.client.request.ItemPublicTokenExchangeRequest;
import com.plaid.client.request.ItemRemoveRequest;
import com.plaid.client.request.LiabilitiesGetRequest;
import com.plaid.client.request.TransactionsGetRequest;
import com.plaid.client.response.AccountsBalanceGetResponse;
import com.plaid.client.response.AccountsGetResponse;
import com.plaid.client.response.AssetReportCreateResponse;
import com.plaid.client.response.AssetReportGetResponse;
import com.plaid.client.response.AuthGetResponse;
import com.plaid.client.response.BaseResponse;
import com.plaid.client.response.CategoriesGetResponse;
import com.plaid.client.response.IdentityGetResponse;
import com.plaid.client.response.IncomeGetResponse;
import com.plaid.client.response.InvestmentsHoldingsGetResponse;
import com.plaid.client.response.InvestmentsTransactionsGetResponse;
import com.plaid.client.response.ItemGetResponse;
import com.plaid.client.response.ItemPublicTokenCreateResponse;
import com.plaid.client.response.ItemPublicTokenExchangeResponse;
import com.plaid.client.response.ItemRemoveResponse;
import com.plaid.client.response.LiabilitiesGetResponse;
import com.plaid.client.response.TransactionsGetResponse;

import okhttp3.ResponseBody;
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

	private final ObjectMapper mapper = new ObjectMapper();
	private PlaidClient plaidClient;
	private String accessToken;

	public PlaidController() {
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
	}

	/**
	 * Creates plaid client.
	 */
	@PostConstruct
	public void init() {
		this.plaidClient = PlaidClient.newBuilder().sandboxBaseUrl()
				.clientIdAndSecret(plaidClientId, plaidSecretKey).publicKey(plaidPublicKey)
				.logLevel(HttpLoggingInterceptor.Level.BODY).build();
	}


	/**
	 * Exchanges the public token for an access token.
	 * 
	 * @param requestBody
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/get_access_token", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BaseResponse> getAccessToken(
			@Valid @RequestBody Map<String, Object> requestBody)
			throws IOException {

		String publicToken = (String) requestBody.get("publicToken");

		// Exchange public token for access token via /item/public_token/exchange
		Response<ItemPublicTokenExchangeResponse> plaidResponse = plaidClient.service()
				.itemPublicTokenExchange(new ItemPublicTokenExchangeRequest(publicToken)).execute();

		// handle error
		if (plaidResponse.errorBody() != null) {
			ResponseBody msg = plaidResponse.errorBody();
			ObjectReader reader = mapper.readerFor(PlaidAPIError.class);
			PlaidAPIError error = reader.readValue(msg.bytes());

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			return new ResponseEntity<BaseResponse>(error, headers, HttpStatus.OK);
		}

		ItemPublicTokenExchangeResponse responseBody = plaidResponse.body();
		return new ResponseEntity<BaseResponse>(responseBody, HttpStatus.OK);
	}

	// Authenticate against an item (aka financial institution). /auth/
	@RequestMapping(value = "/auth", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BaseResponse> getAuth() throws IOException {
		Response<AuthGetResponse> plaidResponse = plaidClient.service().authGet(new AuthGetRequest(accessToken))
				.execute();
		// handle error
		if (plaidResponse.errorBody() != null) {
			ResponseBody msg = plaidResponse.errorBody();
			ObjectReader reader = mapper.readerFor(PlaidAPIError.class);
			PlaidAPIError error = reader.readValue(msg.bytes());

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			return new ResponseEntity<BaseResponse>(error, headers, HttpStatus.OK);
		}

		AuthGetResponse body = plaidResponse.body();
		return new ResponseEntity<BaseResponse>(body, HttpStatus.OK);
	}

	// /transactions/
	// fetch transactions for the last 30 days
	@RequestMapping(value = "/transactions", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BaseResponse> getTransactions() throws IOException {
		// date setup
		Instant now = Instant.now();
		Instant before = now.minus(Duration.ofDays(30));
		Date todayMinus30 = Date.from(before);
		Date today = Date.from(now);

		Response<TransactionsGetResponse> plaidResponse = plaidClient.service()
				.transactionsGet(new TransactionsGetRequest(accessToken, todayMinus30, today)).execute();

		// handle error
		if (plaidResponse.errorBody() != null) {
			ResponseBody msg = plaidResponse.errorBody();
			ObjectReader reader = mapper.readerFor(PlaidAPIError.class);
			PlaidAPIError error = reader.readValue(msg.bytes());

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			return new ResponseEntity<BaseResponse>(error, headers, HttpStatus.OK);
		}

		TransactionsGetResponse body = plaidResponse.body();
		return new ResponseEntity<BaseResponse>(body, HttpStatus.OK);
	}

	// get the balance for a set of accounts
	// /balance/
	@RequestMapping(value = "/balance", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BaseResponse> getBalance() throws IOException {
		Response<AccountsBalanceGetResponse> plaidResponse = plaidClient.service()
				.accountsBalanceGet(new AccountsBalanceGetRequest(accessToken)).execute();

		// handle error
		if (plaidResponse.errorBody() != null) {
			ResponseBody msg = plaidResponse.errorBody();
			ObjectReader reader = mapper.readerFor(PlaidAPIError.class);
			PlaidAPIError error = reader.readValue(msg.bytes());

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			return new ResponseEntity<BaseResponse>(error, headers, HttpStatus.OK);
		}

		AccountsBalanceGetResponse body = plaidResponse.body();
		return new ResponseEntity<BaseResponse>(body, HttpStatus.OK);
	}

	// get the identity info for a set of accounts
	// /identity/
	@RequestMapping(value = "/identity", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BaseResponse> getIdentity() throws IOException {
		Response<IdentityGetResponse> plaidResponse = plaidClient.service()
				.identityGet(new IdentityGetRequest(accessToken)).execute();

		// handle error
		if (plaidResponse.errorBody() != null) {
			ResponseBody msg = plaidResponse.errorBody();
			ObjectReader reader = mapper.readerFor(PlaidAPIError.class);
			PlaidAPIError error = reader.readValue(msg.bytes());

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			return new ResponseEntity<BaseResponse>(error, headers, HttpStatus.OK);
		}

		IdentityGetResponse body = plaidResponse.body();
		return new ResponseEntity<BaseResponse>(body, HttpStatus.OK);
	}

	// income info for a set of accounts
	// /income/
	@RequestMapping(value = "/income", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BaseResponse> getIncome() throws IOException {
		Response<IncomeGetResponse> plaidResponse = plaidClient.service().incomeGet(new IncomeGetRequest(accessToken))
				.execute();

		// handle error
		if (plaidResponse.errorBody() != null) {
			ResponseBody msg = plaidResponse.errorBody();
			ObjectReader reader = mapper.readerFor(PlaidAPIError.class);
			PlaidAPIError error = reader.readValue(msg.bytes());

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			return new ResponseEntity<BaseResponse>(error, headers, HttpStatus.OK);
		}

		IncomeGetResponse body = plaidResponse.body();
		return new ResponseEntity<BaseResponse>(body, HttpStatus.OK);
	}

	// create asset report
	@RequestMapping(value = "/create_asset_report", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BaseResponse> createAssetReport() throws IOException {

		List<String> accessTokens = new ArrayList<String>();
		accessTokens.add(accessToken);

		Response<AssetReportCreateResponse> plaidResponse = plaidClient.service()
				.assetReportCreate(new AssetReportCreateRequest(accessTokens, 60)).execute();

		// handle error
		if (plaidResponse.errorBody() != null) {
			ResponseBody msg = plaidResponse.errorBody();
			ObjectReader reader = mapper.readerFor(PlaidAPIError.class);
			PlaidAPIError error = reader.readValue(msg.bytes());

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			return new ResponseEntity<BaseResponse>(error, headers, HttpStatus.OK);
		}

		AssetReportCreateResponse body = plaidResponse.body();
		return new ResponseEntity<BaseResponse>(body, HttpStatus.OK);
	}

	// get asset report
	@RequestMapping(value = "/get_asset_report", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BaseResponse> getAssetReport(@Valid @RequestBody Map<String, Object> body)
			throws IOException {

		String accessReportToken = (String) body.get("assetReportToken");
		Response<AssetReportGetResponse> plaidResponse = plaidClient.service()
				.assetReportGet(new AssetReportGetRequest(accessReportToken)).execute();

		// handle error
		if (plaidResponse.errorBody() != null) {
			ResponseBody msg = plaidResponse.errorBody();
			ObjectReader reader = mapper.readerFor(PlaidAPIError.class);
			PlaidAPIError error = reader.readValue(msg.bytes());

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			return new ResponseEntity<BaseResponse>(error, headers, HttpStatus.OK);
		}

		AssetReportGetResponse responseBody = plaidResponse.body();
		return new ResponseEntity<BaseResponse>(responseBody, HttpStatus.OK);
	}

	// /investments/
	@RequestMapping(value = "/investments", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BaseResponse> getInvestments() throws IOException {

		Response<InvestmentsHoldingsGetResponse> plaidResponse = plaidClient.service()
				.investmentsHoldingsGet(new InvestmentsHoldingsGetRequest(accessToken)).execute();

		// handle error
		if (plaidResponse.errorBody() != null) {
			ResponseBody msg = plaidResponse.errorBody();
			ObjectReader reader = mapper.readerFor(PlaidAPIError.class);
			PlaidAPIError error = reader.readValue(msg.bytes());

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			return new ResponseEntity<BaseResponse>(error, headers, HttpStatus.OK);
		}

		InvestmentsHoldingsGetResponse body = plaidResponse.body();
		return new ResponseEntity<BaseResponse>(body, HttpStatus.OK);
	}

	// /investments/transactions/
	@RequestMapping(value = "/investments/transactions", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BaseResponse> getInvestmentsTransactions() throws IOException {

		// date setup
		Instant now = Instant.now();
		Instant before = now.minus(Duration.ofDays(30));
		Date todayMinus30 = Date.from(before);
		Date today = Date.from(now);

		Response<InvestmentsTransactionsGetResponse> plaidResponse = plaidClient.service()
				.investmentsTransactionsGet(new InvestmentsTransactionsGetRequest(accessToken, todayMinus30, today))
				.execute();

		// handle error
		if (plaidResponse.errorBody() != null) {
			ResponseBody msg = plaidResponse.errorBody();
			ObjectReader reader = mapper.readerFor(PlaidAPIError.class);
			PlaidAPIError error = reader.readValue(msg.bytes());

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			return new ResponseEntity<BaseResponse>(error, headers, HttpStatus.OK);
		}

		InvestmentsTransactionsGetResponse body = plaidResponse.body();
		return new ResponseEntity<BaseResponse>(body, HttpStatus.OK);
	}

	// /liabilities/
	@RequestMapping(value = "/liabilities", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BaseResponse> getLiabilities() throws IOException {

		Response<LiabilitiesGetResponse> plaidResponse = plaidClient.service()
				.liabilitiesGet(new LiabilitiesGetRequest(accessToken)).execute();

		// handle error
		if (plaidResponse.errorBody() != null) {
			ResponseBody msg = plaidResponse.errorBody();
			ObjectReader reader = mapper.readerFor(PlaidAPIError.class);
			PlaidAPIError error = reader.readValue(msg.bytes());

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			return new ResponseEntity<BaseResponse>(error, headers, HttpStatus.OK);
		}

		LiabilitiesGetResponse body = plaidResponse.body();
		return new ResponseEntity<BaseResponse>(body, HttpStatus.OK);
	}

	// /accounts/
	@RequestMapping(value = "/accounts", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BaseResponse> getAccounts() throws IOException {

		Response<AccountsGetResponse> plaidResponse = plaidClient.service()
				.accountsGet(new AccountsGetRequest(accessToken))
				.execute();

		// handle error
		if (plaidResponse.errorBody() != null) {
			ResponseBody msg = plaidResponse.errorBody();
			ObjectReader reader = mapper.readerFor(PlaidAPIError.class);
			PlaidAPIError error = reader.readValue(msg.bytes());

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			return new ResponseEntity<BaseResponse>(error, headers, HttpStatus.OK);
		}


		AccountsGetResponse body = plaidResponse.body();
		return new ResponseEntity<BaseResponse>(body, HttpStatus.OK);
	}

	// /item/
	// can also be used to get the item status
	@RequestMapping(value = "/item", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BaseResponse> getItem() throws IOException {

		Response<ItemGetResponse> plaidResponse = plaidClient.service().itemGet(new ItemGetRequest(accessToken))
				.execute();

		// handle error
		if (plaidResponse.errorBody() != null) {
			ResponseBody msg = plaidResponse.errorBody();
			ObjectReader reader = mapper.readerFor(PlaidAPIError.class);
			PlaidAPIError error = reader.readValue(msg.bytes());

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			return new ResponseEntity<BaseResponse>(error, headers, HttpStatus.OK);
		}

		ItemGetResponse body = plaidResponse.body();
		return new ResponseEntity<BaseResponse>(body, HttpStatus.OK);
	}

	// /item/remove
	@RequestMapping(value = "/item/remove", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BaseResponse> removeItem() throws IOException {
		HashMap<String, Object> resp = new HashMap<String, Object>();

		Response<ItemRemoveResponse> plaidResponse = plaidClient.service()
				.itemRemove(new ItemRemoveRequest(accessToken))
				.execute();

		// handle error
		if (plaidResponse.errorBody() != null) {
			ResponseBody msg = plaidResponse.errorBody();
			ObjectReader reader = mapper.readerFor(PlaidAPIError.class);
			PlaidAPIError error = reader.readValue(msg.bytes());

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			return new ResponseEntity<BaseResponse>(error, headers, HttpStatus.OK);
		}


		ItemRemoveResponse body = plaidResponse.body();
		return new ResponseEntity<BaseResponse>(body, HttpStatus.OK);
	}

	// /categories
	@RequestMapping(value = "/categories", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BaseResponse> getCategories() throws IOException {
		Response<CategoriesGetResponse> plaidResponse = plaidClient.service().categoriesGet(new CategoriesGetRequest())
				.execute();

		// handle error
		if (plaidResponse.errorBody() != null) {
			ResponseBody msg = plaidResponse.errorBody();
			ObjectReader reader = mapper.readerFor(PlaidAPIError.class);
			PlaidAPIError error = reader.readValue(msg.bytes());

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			return new ResponseEntity<BaseResponse>(error, headers, HttpStatus.OK);
		}


		CategoriesGetResponse body = plaidResponse.body();
		return new ResponseEntity<BaseResponse>(body, HttpStatus.OK);
	}

	// POST to /item/public_token/create with an access_token to generate a new
	// public_token
	@RequestMapping(value = "/get_public_token", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BaseResponse> fetchPublicToken() throws IOException {

		Response<ItemPublicTokenCreateResponse> plaidResponse = plaidClient.service()
				.itemPublicTokenCreate(new ItemPublicTokenCreateRequest(accessToken)).execute();

		// handle error
		if (plaidResponse.errorBody() != null) {
			ResponseBody msg = plaidResponse.errorBody();
			ObjectReader reader = mapper.readerFor(PlaidAPIError.class);
			PlaidAPIError error = reader.readValue(msg.bytes());

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			return new ResponseEntity<BaseResponse>(error, headers, HttpStatus.OK);
		}

		ItemPublicTokenCreateResponse body = plaidResponse.body();
		return new ResponseEntity<BaseResponse>(body, HttpStatus.OK);
	}

	// TODO: update item webhook
	// TODO: rotate access token
	// TODO: refresh asset report, filter request for asset reports, get asset
	// report pdf
	// TODO: write audit copy endpoint (provide docs to third party)

}
