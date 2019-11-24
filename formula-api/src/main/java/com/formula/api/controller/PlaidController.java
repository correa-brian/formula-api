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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
import com.plaid.client.response.Account;
import com.plaid.client.response.AccountsBalanceGetResponse;
import com.plaid.client.response.AccountsGetResponse;
import com.plaid.client.response.AssetReportCreateResponse;
import com.plaid.client.response.AssetReportGetResponse;
import com.plaid.client.response.AssetReportGetResponse.AssetReport;
import com.plaid.client.response.AssetReportGetResponse.Warning;
import com.plaid.client.response.AuthGetResponse;
import com.plaid.client.response.AuthGetResponse.Numbers;
import com.plaid.client.response.CategoriesGetResponse;
import com.plaid.client.response.CategoriesGetResponse.Category;
import com.plaid.client.response.IdentityGetResponse;
import com.plaid.client.response.IdentityGetResponse.AccountWithOwners;
import com.plaid.client.response.IncomeGetResponse;
import com.plaid.client.response.IncomeGetResponse.Income;
import com.plaid.client.response.InvestmentsHoldingsGetResponse;
import com.plaid.client.response.InvestmentsHoldingsGetResponse.Holding;
import com.plaid.client.response.InvestmentsTransactionsGetResponse;
import com.plaid.client.response.InvestmentsTransactionsGetResponse.InvestmentTransaction;
import com.plaid.client.response.ItemGetResponse;
import com.plaid.client.response.ItemPublicTokenCreateResponse;
import com.plaid.client.response.ItemPublicTokenExchangeResponse;
import com.plaid.client.response.ItemRemoveResponse;
import com.plaid.client.response.ItemStatus;
import com.plaid.client.response.ItemStatusStatus;
import com.plaid.client.response.LiabilitiesGetResponse;
import com.plaid.client.response.LiabilitiesGetResponse.Liabilities;
import com.plaid.client.response.Security;
import com.plaid.client.response.TransactionsGetResponse;
import com.plaid.client.response.TransactionsGetResponse.Transaction;

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

	private PlaidClient plaidClient;
	private String accessToken;

	public PlaidController() {
	}

	// create plaid client
	@PostConstruct
	public void init() {
		this.plaidClient = PlaidClient.newBuilder().sandboxBaseUrl()
				.clientIdAndSecret(plaidClientId, plaidSecretKey).publicKey(plaidPublicKey)
				.logLevel(HttpLoggingInterceptor.Level.BODY).build();
	}

	// /item/public_token/exchange
	@RequestMapping(value = "/get_access_token", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<HashMap<String, Object>> getAccessToken(@Valid @RequestBody Map<String, Object> body)
			throws IOException {

		HashMap<String, Object> resp = new HashMap<String, Object>();
		String publicToken = (String) body.get("publicToken");

		// Ask (/item/public_token/exchange) for an access token using the public token
		Response<ItemPublicTokenExchangeResponse> response = plaidClient.service()
				.itemPublicTokenExchange(new ItemPublicTokenExchangeRequest(publicToken)).execute();

		// handle error
		if (response.errorBody() != null) {
			resp.put("error", response.errorBody());
			return new ResponseEntity<HashMap<String, Object>>(resp, HttpStatus.OK);
		}

		ItemPublicTokenExchangeResponse responseBody = response.body();
		if (response.isSuccessful()) {
			accessToken = responseBody.getAccessToken();

		}

		String requestId = responseBody.getRequestId();
		String itemId = responseBody.getItemId();
		resp.put("requestId", requestId);
		resp.put("accessToken", accessToken);
		resp.put("itemId", itemId);
		return new ResponseEntity<HashMap<String, Object>>(resp, HttpStatus.OK);
	}

	// Authenticate against an item (aka financial institution). /auth/
	@RequestMapping(value = "/auth", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<HashMap<String, Object>> getAuth() throws IOException {
		HashMap<String, Object> resp = new HashMap<String, Object>();

		Response<AuthGetResponse> response = plaidClient.service().authGet(new AuthGetRequest(accessToken)).execute();
		if (response.errorBody() != null) {
			resp.put("error", response.errorBody());
			return new ResponseEntity<HashMap<String, Object>>(resp, HttpStatus.OK);
		}

		AuthGetResponse body = response.body();
		List<Account> accounts = body.getAccounts();
		String requestId = body.getRequestId();
		ItemStatus item = body.getItem();
		Numbers numbers = body.getNumbers();

		resp.put("accounts", accounts);
		resp.put("requestId", requestId);
		resp.put("item", item);
		resp.put("numbers", numbers);
		return new ResponseEntity<HashMap<String, Object>>(resp, HttpStatus.OK);
	}

	// /transactions/
	// fetch transactions for the last 30 days
	@RequestMapping(value = "/transactions", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<HashMap<String, Object>> getTransactions() throws IOException {
		HashMap<String, Object> resp = new HashMap<String, Object>();

		// date setup
		Instant now = Instant.now();
		Instant before = now.minus(Duration.ofDays(30));
		Date todayMinus30 = Date.from(before);
		Date today = Date.from(now);

		Response<TransactionsGetResponse> response = plaidClient.service()
				.transactionsGet(new TransactionsGetRequest(accessToken, todayMinus30, today)).execute();

		if (response.errorBody() != null) {
			resp.put("error", response.errorBody());
			return new ResponseEntity<HashMap<String, Object>>(resp, HttpStatus.OK);
		}

		TransactionsGetResponse body = response.body();
		List<Account> accounts = body.getAccounts();
		List<Transaction> transactions = body.getTransactions();
		String requestId = body.getRequestId();
		ItemStatus item = body.getItem();

		resp.put("accounts", accounts);
		resp.put("transactions", transactions);
		resp.put("requestId", requestId);
		resp.put("item", item);
		return new ResponseEntity<HashMap<String, Object>>(resp, HttpStatus.OK);
	}

	// get the balance for a set of accounts
	// /balance/
	@RequestMapping(value = "/balance", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<HashMap<String, Object>> getBalance() throws IOException {

		HashMap<String, Object> resp = new HashMap<String, Object>();
		Response<AccountsBalanceGetResponse> response = plaidClient.service()
				.accountsBalanceGet(new AccountsBalanceGetRequest(accessToken)).execute();

		if (response.errorBody() != null) {
			resp.put("error", response.errorBody());
			return new ResponseEntity<HashMap<String, Object>>(resp, HttpStatus.OK);
		}

		AccountsBalanceGetResponse body = response.body();
		List<Account> accounts = body.getAccounts();
		String requestId = body.getRequestId();
		ItemStatus item = body.getItem();

		resp.put("accounts", accounts);
		resp.put("requestId", requestId);
		resp.put("item", item);
		return new ResponseEntity<HashMap<String, Object>>(resp, HttpStatus.OK);
	}

	// get the identity info for a set of accounts
	// /identity/
	@RequestMapping(value = "/identity", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<HashMap<String, Object>> getIdentity() throws IOException {
		HashMap<String, Object> resp = new HashMap<String, Object>();

		Response<IdentityGetResponse> response = plaidClient.service().identityGet(new IdentityGetRequest(accessToken))
				.execute();

		if (response.errorBody() != null) {
			resp.put("error", response.errorBody());
			return new ResponseEntity<HashMap<String, Object>>(resp, HttpStatus.OK);
		}

		IdentityGetResponse body = response.body();
		List<AccountWithOwners> accounts = body.getAccounts();
		String requestId = body.getRequestId();
		ItemStatus item = body.getItem();

		resp.put("accounts", accounts);
		resp.put("requestId", requestId);
		resp.put("item", item);

		return new ResponseEntity<HashMap<String, Object>>(resp, HttpStatus.OK);
	}

	// income info for a set of accounts
	// /income/
	@RequestMapping(value = "/income", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<HashMap<String, Object>> getIncome() throws IOException {
		HashMap<String, Object> resp = new HashMap<String, Object>();

		Response<IncomeGetResponse> response = plaidClient.service().incomeGet(new IncomeGetRequest(accessToken))
				.execute();

		if (response.errorBody() != null) {
			resp.put("error", response.errorBody());
			return new ResponseEntity<HashMap<String, Object>>(resp, HttpStatus.OK);
		}

		IncomeGetResponse body = response.body();
		Income income = body.getIncome();
		String requestId = body.getRequestId();
		ItemStatus item = body.getItem();

		resp.put("income", income);
		resp.put("requestId", requestId);
		resp.put("item", item);
		return new ResponseEntity<HashMap<String, Object>>(resp, HttpStatus.OK);
	}

	// create asset report
	@RequestMapping(value = "/create_asset_report", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<HashMap<String, Object>> createAssetReport() throws IOException {
		HashMap<String, Object> resp = new HashMap<String, Object>();

		List<String> accessTokens = new ArrayList<String>();
		accessTokens.add(accessToken);

		Response<AssetReportCreateResponse> response = plaidClient.service()
				.assetReportCreate(new AssetReportCreateRequest(accessTokens, 60)).execute();

		if (response.errorBody() != null) {
			resp.put("error", response.errorBody());
			return new ResponseEntity<HashMap<String, Object>>(resp, HttpStatus.OK);
		}

		AssetReportCreateResponse body = response.body();
		String assetReportId = body.getAssetReportId();
		String assetReportToken = body.getAssetReportToken();
		String requestId = body.getRequestId();

		resp.put("assetReportId", assetReportId);
		resp.put("assetReportToken", assetReportToken);
		resp.put("requestId", requestId);
		return new ResponseEntity<HashMap<String, Object>>(resp, HttpStatus.OK);
	}

	// get asset report
	@RequestMapping(value = "/get_asset_report", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<HashMap<String, Object>> getAssetReport(@Valid @RequestBody Map<String, Object> body)
			throws IOException {
		HashMap<String, Object> resp = new HashMap<String, Object>();

		String accessReportToken = (String) body.get("assetReportToken");
		Response<AssetReportGetResponse> response = plaidClient.service()
				.assetReportGet(new AssetReportGetRequest(accessReportToken)).execute();

		if (response.errorBody() != null) {
			resp.put("error", response.errorBody());
			return new ResponseEntity<HashMap<String, Object>>(resp, HttpStatus.OK);
		}

		AssetReportGetResponse responseBody = response.body();
		AssetReport assetReport = responseBody.getReport();
		List<Warning> warnings = responseBody.getWarnings();
		String requestId = responseBody.getRequestId();

		resp.put("assetReport", assetReport);
		resp.put("warnings", warnings);
		resp.put("requestId", requestId);
		return new ResponseEntity<HashMap<String, Object>>(resp, HttpStatus.OK);
	}

	// /investments/
	@RequestMapping(value = "/investments", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<HashMap<String, Object>> getInvestments() throws IOException {
		HashMap<String, Object> resp = new HashMap<String, Object>();

		Response<InvestmentsHoldingsGetResponse> response = plaidClient.service()
				.investmentsHoldingsGet(new InvestmentsHoldingsGetRequest(accessToken)).execute();

		if (response.errorBody() != null) {
			resp.put("error", response.errorBody());
			return new ResponseEntity<HashMap<String, Object>>(resp, HttpStatus.OK);
		}

		InvestmentsHoldingsGetResponse body = response.body();
		List<Holding> holdings = body.getHoldings();
		List<Security> securities = body.getSecurities();
		List<Account> accounts = body.getAccounts();
		ItemStatus item = body.getItem();
		String requestId = body.getRequestId();

		resp.put("hldings", holdings);
		resp.put("securities", securities);
		resp.put("accounts", accounts);
		resp.put("item", item);
		resp.put("requestId", requestId);
		return new ResponseEntity<HashMap<String, Object>>(resp, HttpStatus.OK);
	}

	// /investments/transactions/
	@RequestMapping(value = "/investments/transactions", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<HashMap<String, Object>> getInvestmentsTransactions() throws IOException {
		HashMap<String, Object> resp = new HashMap<String, Object>();

		// date setup
		Instant now = Instant.now();
		Instant before = now.minus(Duration.ofDays(30));
		Date todayMinus30 = Date.from(before);
		Date today = Date.from(now);

		Response<InvestmentsTransactionsGetResponse> response = plaidClient.service()
				.investmentsTransactionsGet(new InvestmentsTransactionsGetRequest(accessToken, todayMinus30, today))
				.execute();

		if (response.errorBody() != null) {
			resp.put("error", response.errorBody());
			return new ResponseEntity<HashMap<String, Object>>(resp, HttpStatus.OK);
		}

		InvestmentsTransactionsGetResponse body = response.body();
		List<InvestmentTransaction> investmentTransactions = body.getInvestmentTransactions();
		List<Security> securities = body.getSecurities();
		List<Account> accounts = body.getAccounts();
		ItemStatus item = body.getItem();
		String requestId = body.getRequestId();

		resp.put("investmentTransactions", investmentTransactions);
		resp.put("securities", securities);
		resp.put("accounts", accounts);
		resp.put("item", item);
		resp.put("requestId", requestId);
		return new ResponseEntity<HashMap<String, Object>>(resp, HttpStatus.OK);
	}

	// /liabilities/
	@RequestMapping(value = "/liabilities", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<HashMap<String, Object>> getLiabilities() throws IOException {
		HashMap<String, Object> resp = new HashMap<String, Object>();

		Response<LiabilitiesGetResponse> response = plaidClient.service()
				.liabilitiesGet(new LiabilitiesGetRequest(accessToken)).execute();

		if (response.errorBody() != null) {
			resp.put("error", response.errorBody());
			return new ResponseEntity<HashMap<String, Object>>(resp, HttpStatus.OK);
		}

		LiabilitiesGetResponse body = response.body();
		Liabilities liabilities = body.getLiabilities();
		List<Account> accounts = body.getAccounts();
		ItemStatus item = body.getItem();
		String requestId = body.getRequestId();

		resp.put("liabilities", liabilities);
		resp.put("accounts", accounts);
		resp.put("item", item);
		resp.put("requestId", requestId);
		return new ResponseEntity<HashMap<String, Object>>(resp, HttpStatus.OK);
	}

	// /accounts/
	@RequestMapping(value = "/accounts", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<HashMap<String, Object>> getAccounts() throws IOException {
		HashMap<String, Object> resp = new HashMap<String, Object>();

		Response<AccountsGetResponse> response = plaidClient.service().accountsGet(new AccountsGetRequest(accessToken))
				.execute();

		if (response.errorBody() != null) {
			resp.put("error", response.errorBody());
			return new ResponseEntity<HashMap<String, Object>>(resp, HttpStatus.OK);
		}

		AccountsGetResponse body = response.body();
		List<Account> accounts = body.getAccounts();
		ItemStatus item = body.getItem();
		String requestId = body.getRequestId();

		resp.put("accounts", accounts);
		resp.put("item", item);
		resp.put("requestId", requestId);
		return new ResponseEntity<HashMap<String, Object>>(resp, HttpStatus.OK);
	}

	// /item/
	// can also be used to get the item status
	@RequestMapping(value = "/item", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<HashMap<String, Object>> getItem() throws IOException {
		HashMap<String, Object> resp = new HashMap<String, Object>();

		Response<ItemGetResponse> response = plaidClient.service().itemGet(new ItemGetRequest(accessToken))
				.execute();

		if (response.errorBody() != null) {
			resp.put("error", response.errorBody());
			return new ResponseEntity<HashMap<String, Object>>(resp, HttpStatus.OK);
		}

		ItemGetResponse body = response.body();
		ItemStatusStatus status = body.getStatus();
		ItemStatus item = body.getItem();
		String requestId = body.getRequestId();

		resp.put("status", status);
		resp.put("item", item);
		resp.put("requestId", requestId);
		return new ResponseEntity<HashMap<String, Object>>(resp, HttpStatus.OK);
	}

	// /item/remove
	@RequestMapping(value = "/item/remove", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<HashMap<String, Object>> removeItem() throws IOException {
		HashMap<String, Object> resp = new HashMap<String, Object>();

		Response<ItemRemoveResponse> response = plaidClient.service().itemRemove(new ItemRemoveRequest(accessToken))
				.execute();

		if (response.errorBody() != null) {
			resp.put("error", response.errorBody());
			return new ResponseEntity<HashMap<String, Object>>(resp, HttpStatus.OK);
		}

		ItemRemoveResponse body = response.body();
		Boolean isRemoved = body.getRemoved();
		String requestId = body.getRequestId();

		resp.put("isRemoved", isRemoved);
		resp.put("requestId", requestId);
		return new ResponseEntity<HashMap<String, Object>>(resp, HttpStatus.OK);
	}

	// /categories
	@RequestMapping(value = "/categories", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<HashMap<String, Object>> getCategories() throws IOException {
		HashMap<String, Object> resp = new HashMap<String, Object>();
		Response<CategoriesGetResponse> response = plaidClient.service().categoriesGet(new CategoriesGetRequest())
				.execute();


		if (response.errorBody() != null) {
			resp.put("error", response.errorBody());
			return new ResponseEntity<HashMap<String, Object>>(resp, HttpStatus.OK);
		}

		CategoriesGetResponse body = response.body();
		List<Category> categories = body.getCategories();
		String requestId = body.getRequestId();

		resp.put("categories", categories);
		resp.put("requestId", requestId);
		return new ResponseEntity<HashMap<String, Object>>(resp, HttpStatus.OK);
	}

	// POST to /item/public_token/create with an access_token to generate a new
	// public_token
	@RequestMapping(value = "/get_public_token", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<HashMap<String, Object>> fetchPublicToken() throws IOException {

		Response<ItemPublicTokenCreateResponse> response = plaidClient.service()
				.itemPublicTokenCreate(new ItemPublicTokenCreateRequest(accessToken)).execute();

		String publicToken = "";
		if (response.isSuccessful()) {
			publicToken = response.body().getPublicToken();
		}

		ItemPublicTokenCreateResponse body = response.body();
		Date expiration = body.getExpiration();
		publicToken = body.getPublicToken();
		String requestId = body.getRequestId();

		HashMap<String, Object> resp = new HashMap<String, Object>();
		resp.put("requestID", requestId);
		resp.put("expiration", expiration);
		resp.put("publicToken", publicToken);
		return new ResponseEntity<HashMap<String, Object>>(resp, HttpStatus.OK);
	}

	// TODO: update item webhook
	// TODO: rotate access token
	// TODO: refresh asset report, filter request for asset reports, get asset
	// report pdf
	// TODO: write audit copy endpoint (provide docs to third party)

}
