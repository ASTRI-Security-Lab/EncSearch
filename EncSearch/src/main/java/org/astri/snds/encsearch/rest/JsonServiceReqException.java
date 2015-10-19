package org.astri.snds.encsearch.rest;

import javax.json.JsonObject;

public class JsonServiceReqException extends Exception {
	
	private static final long serialVersionUID = -7500122075905718136L;
	private String errKind;
	
	public JsonServiceReqException(JsonObject response) {
		assert response.get("result").equals("error");
		errKind = response.getString("err_kind");
	}
	
	public JsonServiceReqException(String errKind_) {
		errKind = errKind_;
	}

	@Override
	public String toString() {
		return super.toString().concat("ServiceJsonError").concat(this.errKind);
	}
}
