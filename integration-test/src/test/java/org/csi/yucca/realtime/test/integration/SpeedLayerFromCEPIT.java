package org.csi.yucca.realtime.test.integration;

import static io.restassured.RestAssured.given;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.csi.yucca.realtime.test.OdataUtils;
import org.csi.yucca.realtime.test.RestTest;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsEqual;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class SpeedLayerFromCEPIT extends RestTest {

	@BeforeClass
	public void setUpSecretObject() throws IOException {
		super.setUpSecretObject("/testSecret.json");
	}

	@DataProvider(name = "SpeedLayerFromCEPIT")
	public Iterator<Object[]> getFromJson() {

		return super.getFromJson("/SpeedLayerFromCEPIT.json");

	}

	@Test(dataProvider = "SpeedLayerFromCEPIT")
	public void sendHTTPEndCountTesting(JSONObject dato) throws InterruptedException {

		String message = dato.getString("rt.message");

		message = StringUtils.replace(message, "{{sysdate}}", new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ssZ").format(new Date()));

		
		
		RequestSpecification rsOdata = given();

		if (StringUtils.isNotEmpty(dato.optString("odata.username"))) {
			rsOdata = rsOdata.auth().basic(dato.getString("odata.username"),
					dato.getString("odata.password"));
		}

		String odataCount = rsOdata
				.when()
				.get(OdataUtils.makeUrl(dato, "json"))
				.then()
				.statusCode(HttpStatus.SC_OK)
				.extract().path("d.__count");

		
		RequestSpecification rsRealtime = given();
		
		
		if (StringUtils.isNotEmpty(dato.optString("rt.username"))) {
			rsRealtime = rsRealtime.auth().basic(dato.getString("rt.username"),
					dato.getString("rt.password"));
		}

		rsRealtime
			.body(message).contentType(ContentType.JSON)
			.when()
			.post(dato.get("rt.url") + "/" + dato.get("rt.tenant") + "/")
			.then().
			statusCode(dato.getInt("rt.httpStatusExcepted"));
		
		Thread.sleep(100);
		
		rsOdata
		.when()
		.get(OdataUtils.makeUrl(dato, "json"))
		.then()
		.statusCode(HttpStatus.SC_OK)
		.body("d.__count", Matchers.equalTo(Integer.toString(Integer.parseInt(odataCount)+dato.getInt("integration.added"))));
		}

}
