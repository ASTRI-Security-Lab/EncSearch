package org.astri.snds.encsearch.rest;

import javax.json.JsonObject;

public class ServiceJsonError extends Exception {
	private String errKind;
	
	public ServiceJsonError(JsonObject response) {
		assert response.get("result").equals("error");
		errKind = response.getString("err_kind");
	}
	
	public ServiceJsonError(String errKind_) {
		errKind = errKind_;
	}

	@Override
	public String toString() {
		return super.toString().concat("ServiceJsonError").concat(this.errKind);
	}
}
