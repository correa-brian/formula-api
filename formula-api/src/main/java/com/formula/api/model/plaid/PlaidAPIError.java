package com.formula.api.model.plaid;

import com.plaid.client.response.BaseResponse;

/**
 * Base class for all possible Plaid API errors.
 * 
 * @author Brian
 *
 */
public class PlaidAPIError extends BaseResponse {
	public String display_message;
	public String error_code;
	public String error_message;
	public String error_type;
	public String request_id;
	public String suggested_action;

	public PlaidAPIError() {
	}

}
