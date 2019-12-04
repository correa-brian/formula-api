package com.formula.api.controller;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
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
import com.formula.api.model.plaid.AccessReportRequest;
import com.formula.api.model.plaid.AccessTokenRequest;
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

// TODO: update item webhook
// TODO: rotate access token
// TODO: refresh asset report, filter request for asset reports, get asset
// report pdf
// TODO: write audit copy endpoint (provide docs to third party)

/**
 * Plaid API Wrapper
 * 
 * @author Brian
 *
 */
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
	 * Exchanges the Formula public token for an access token.
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

	/**
	 * Requests authentication info from an item (aka financial institution: routing
	 * number, account id, ACH, etc.)
	 * 
	 * @param requestBody
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/auth", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BaseResponse> getAuth(@Valid @RequestBody AccessTokenRequest requestBody)
			throws IOException {

		Response<AuthGetResponse> plaidResponse = plaidClient.service()
				.authGet(new AuthGetRequest(requestBody.accessToken))
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

	/**
	 * Fetches transactions for the last 30 days.
	 * 
	 * @param requestBody
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/transactions", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BaseResponse> getTransactions(@Valid @RequestBody AccessTokenRequest requestBody)
			throws IOException {
		// date setup
		Instant now = Instant.now();
		Instant before = now.minus(Duration.ofDays(30));
		Date todayMinus30 = Date.from(before);
		Date today = Date.from(now);

		Response<TransactionsGetResponse> plaidResponse = plaidClient.service()
				.transactionsGet(new TransactionsGetRequest(requestBody.accessToken, todayMinus30, today)).execute();

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

	/**
	 * Get the balance for a set of accounts.
	 * 
	 * @param requestBody
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/balance", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BaseResponse> getBalance(@Valid @RequestBody AccessTokenRequest requestBody)
			throws IOException {

		Response<AccountsBalanceGetResponse> plaidResponse = plaidClient.service()
				.accountsBalanceGet(new AccountsBalanceGetRequest(requestBody.accessToken)).execute();

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

	/**
	 * Fetches identity info for a set of accounts.
	 * 
	 * @param requestBody
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/identity", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BaseResponse> getIdentity(@Valid @RequestBody AccessTokenRequest requestBody)
			throws IOException {

		Response<IdentityGetResponse> plaidResponse = plaidClient.service()
				.identityGet(new IdentityGetRequest(requestBody.accessToken)).execute();

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

	/**
	 * Income info for a set of accounts.
	 * 
	 * @param requestBody
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/income", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BaseResponse> getIncome(@Valid @RequestBody AccessTokenRequest requestBody)
			throws IOException {

		Response<IncomeGetResponse> plaidResponse = plaidClient.service()
				.incomeGet(new IncomeGetRequest(requestBody.accessToken))
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

	/**
	 * Creates an assert report. TODO: add support for list of access tokens.
	 * 
	 * @param requestBody
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/create_asset_report", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BaseResponse> createAssetReport(@Valid @RequestBody AccessTokenRequest requestBody)
			throws IOException {

		List<String> accessTokens = new ArrayList<String>();
		accessTokens.add(requestBody.accessToken);

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

	/**
	 * Fetches an asset report.
	 * 
	 * @param requestBody
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/get_asset_report", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BaseResponse> getAssetReport(@Valid @RequestBody AccessReportRequest requestBody)
			throws IOException {

		Response<AssetReportGetResponse> plaidResponse = plaidClient.service()
				.assetReportGet(new AssetReportGetRequest(requestBody.assetReportToken)).execute();

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

	/**
	 * Fetches all investments associated with accessToken.
	 * 
	 * @param requestBody
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/investments", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BaseResponse> getInvestments(@Valid @RequestBody AccessTokenRequest requestBody)
			throws IOException {

		Response<InvestmentsHoldingsGetResponse> plaidResponse = plaidClient.service()
				.investmentsHoldingsGet(new InvestmentsHoldingsGetRequest(requestBody.accessToken)).execute();

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

	/**
	 * Retrieves all transactions for investments in last 30 days.
	 * 
	 * @param requestBody
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/investments/transactions", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BaseResponse> getInvestmentsTransactions(@Valid @RequestBody AccessTokenRequest requestBody)
			throws IOException {

		// date setup
		Instant now = Instant.now();
		Instant before = now.minus(Duration.ofDays(30));
		Date todayMinus30 = Date.from(before);
		Date today = Date.from(now);

		Response<InvestmentsTransactionsGetResponse> plaidResponse = plaidClient.service()
				.investmentsTransactionsGet(
						new InvestmentsTransactionsGetRequest(requestBody.accessToken, todayMinus30, today))
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

	/**
	 * Returns liabilities.
	 * 
	 * @param requestBody
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/liabilities", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BaseResponse> getLiabilities(@Valid @RequestBody AccessTokenRequest requestBody)
			throws IOException {

		Response<LiabilitiesGetResponse> plaidResponse = plaidClient.service()
				.liabilitiesGet(new LiabilitiesGetRequest(requestBody.accessToken)).execute();

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

	/**
	 * Fetches all accounts.
	 * 
	 * @param requestBody
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/accounts", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BaseResponse> getAccounts(@Valid @RequestBody AccessTokenRequest requestBody)
			throws IOException {

		Response<AccountsGetResponse> plaidResponse = plaidClient.service()
				.accountsGet(new AccountsGetRequest(requestBody.accessToken))
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

	/**
	 * Returns item information. TODO: support getting item status.
	 * 
	 * @param requestBody
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/item", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BaseResponse> getItem(@Valid @RequestBody AccessTokenRequest requestBody) throws IOException {

		Response<ItemGetResponse> plaidResponse = plaidClient.service()
				.itemGet(new ItemGetRequest(requestBody.accessToken))
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

	/**
	 * Removes an item from the accessToken.
	 * 
	 * @param requestBody
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/item/remove", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BaseResponse> removeItem(@Valid @RequestBody AccessTokenRequest requestBody)
			throws IOException {
		Response<ItemRemoveResponse> plaidResponse = plaidClient.service()
				.itemRemove(new ItemRemoveRequest(requestBody.accessToken))
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

	/**
	 * Returns a set of categories. Per Plaid docs, it doesn't seem to be used.
	 * 
	 * @return
	 * @throws IOException
	 */
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

	/**
	 * Exchange an accessToken for a publicToken.
	 * 
	 * @param requestBody
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/get_public_token", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BaseResponse> fetchPublicToken(@Valid @RequestBody AccessTokenRequest requestBody)
			throws IOException {

		Response<ItemPublicTokenCreateResponse> plaidResponse = plaidClient.service()
				.itemPublicTokenCreate(new ItemPublicTokenCreateRequest(requestBody.accessToken)).execute();

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

}
