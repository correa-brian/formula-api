package com.formula.api.model.plaid;

import com.plaid.client.response.BaseResponse;

// TOOD: extend this class for all errors.
/**
 * Should be the base class for all possible Plaid API errors.
 * 
 * @author Brian
 *
 */
public class PlaidAPIError extends BaseResponse {
	private String display_message;
	private String error_code;
	private String error_message;
	private String error_type;
	private String request_id;
	private String suggested_action;

	public PlaidAPIError() {
	}

	public String getDisplay_message() {
		return display_message;
	}

	public void setDisplay_message(String display_message) {
		this.display_message = display_message;
	}

	public String getError_code() {
		return error_code;
	}

	public void setError_code(String error_code) {
		this.error_code = error_code;
	}

	public String getError_message() {
		return error_message;
	}

	public void setError_message(String error_message) {
		this.error_message = error_message;
	}

	public String getError_type() {
		return error_type;
	}

	public void setError_type(String error_type) {
		this.error_type = error_type;
	}

	public String getRequest_id() {
		return request_id;
	}

	public void setRequest_id(String request_id) {
		this.request_id = request_id;
	}

	public String getSuggested_action() {
		return suggested_action;
	}

	public void setSuggested_action(String suggested_action) {
		this.suggested_action = suggested_action;
	}

}
