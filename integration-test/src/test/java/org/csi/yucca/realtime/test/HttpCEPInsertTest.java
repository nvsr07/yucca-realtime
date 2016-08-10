package org.csi.yucca.realtime.test;

import static io.restassured.RestAssured.given;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matchers;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class HttpCEPInsertTest extends RestTest {

	@BeforeClass
	public void setUpSecretObject() throws IOException {
		super.setUpSecretObject("/testSecret.json");
	}
	
	@DataProvider(name = "ValidationCEPInsertTest")
	public Iterator<Object[]> getFromJson() {

		return super.getFromJson("/ValidationCEPInsertTest.json");
		
	}

	
	@Test(dataProvider = "ValidationCEPInsertTest")
	public void sendHTTPStatusErrorCodeTesting(JSONObject dato) {
		RequestSpecification rs = given().body(dato.get("rt.message")).contentType(ContentType.JSON);

		if (StringUtils.isNotEmpty(dato.optString("rt.username"))) {
			rs = rs.auth().basic(dato.getString("rt.username"), dato.getString("rt.password"));
		}

		Response rsp = rs.when().post(dato.get("rt.url") + "/" + dato.get("rt.tenant") + "/");

		rsp.then().statusCode(dato.getInt("rt.httpStatusExcepted"));

		if (StringUtils.isNotEmpty(dato.optString("rt.errorCode")))
			rsp.then().body("error_code", Matchers.equalTo(dato.optString("rt.errorCode")));
	}

}
