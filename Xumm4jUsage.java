package examples;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.Socket;
import java.util.Properties;
import javax.net.ssl.SSLSocketFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fl.xrpl4j.model.jackson.ObjectMapperFactory;
import com.fl.xrpl4j.model.transactions.Address;
import com.fl.xrpl4j.model.transactions.Payment;
import com.fl.xrpl4j.model.transactions.XrpCurrencyAmount;
import com.fl.xumm4j.dao.GetPayloadDAO;
import com.fl.xumm4j.dao.PostPayloadDAO;
import com.fl.xumm4j.sdk.Deserialize;
import com.fl.xumm4j.sdk.XummClient;
import com.fl.xumm4j.sdk.builder.CredentialsBuilder;
import com.fl.xumm4j.sdk.builder.PayloadBuilder;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;

class Xumm4jUsage {
	public static void main(String[] args) throws JsonProcessingException {

		ObjectMapper objectMapper = ObjectMapperFactory.create();
		Deserialize deserialize = new Deserialize();
		String strApiKey = "";
		String strSecretKey = "";
		//
		FileInputStream fis = null;
		Properties prop = new Properties();
		try {
			fis = new FileInputStream("application.properties"); // file location for the credentials
			try {
				prop.load(fis);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			strApiKey = prop.getProperty("apikey");
			strSecretKey = prop.getProperty("secretkey");
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Don't worry about this key being exposed... I built it for testing,
		// demonstration purposes. keys not being used in production env.
		CredentialsBuilder myAccess = new CredentialsBuilder.builder().apiKey(strApiKey).secretKey(strSecretKey)
				.build();
		XummClient xummClient = new XummClient(myAccess);

		// Use modified version of xrpl4j model, It won't throw an error if Account and
		// sequence are missing.
		Payment payment = Payment.builder()
				// .account()
				// .sequence()
				.fee(XrpCurrencyAmount.ofDrops(12)).destination(Address.of("ra5nK24KXen9AHvsdFTKHSANinZseWnPcX")) 
				.amount(XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(8787))).build();
		String JSON = null;
		try {
			JSON = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(payment);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		String payload = new PayloadBuilder.builder().txjson(JSON).instruction("This is a payment transaction").build();

		System.out.println("Generated Payload: \n" + payload);
		String Payload = payload;
		System.out.println(Payload + "\n");
		// Post payload
		// System.out.println(xummClient.postPayload(Payload));
		String postPayloadResponse = xummClient.postPayload(Payload);
		PostPayloadDAO postPayloadResponseDAO = deserialize.Payload(postPayloadResponse); // prepare the response

		// start asynchronous web socket read the message return
		WebSocket ws = null;
		try {

			WebSocketFactory wsfactory = new WebSocketFactory()
					.setSSLSocketFactory((SSLSocketFactory) SSLSocketFactory.getDefault());
			ws = wsfactory.createSocket(postPayloadResponseDAO.getWebsocket_status(), 10000); // pass the web socket url

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			ws.connect();
			// add listener
			ws.addListener(new WebSocketAdapter() {
				@Override
				public void onTextMessage(WebSocket websocket, String message) throws Exception {
					// Received a text message for reference, it will display if there is a reply
					System.out.print(message + "\n");
				}
			});

		} catch (WebSocketException e) {
			e.printStackTrace();
		}
		// end asynchronous web socket read

		String strPingJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(xummClient.doPing());
		System.out.println(xummClient.getRates("USD"));
		System.out.println(xummClient.doPing());
		System.out.println(xummClient.getCuratedAssets());
		// Storage
		System.out.println(xummClient.setStorage(JSON));
		System.out.println(xummClient.getStorage());
		System.out.println(xummClient.deleteStorage());
		// KYC
		System.out.println(xummClient.getKycStatus("rDWLGshgAxSX2G4TEv3gA6QhtLgiXrWQXB"));
		// Get Transaction
		System.out
				.println(xummClient.getTransaction("DA66B07C9FE0876A3447DE4C57D565FC9C5324485912D10B48C0507F191A4021"));

		String jsonResponse = xummClient.getCuratedAssets();

		// Other DAO are available under com.fl.xumm4j.dao.*
		/*
		 * PayloadDAO result = deserialize.Payload(jsonResponse);
		 * result.forEachCurrencies(System.out::println);
		 * result.forEachDetails(System.out::println);
		 * result.forEachIssuer(System.out::println);
		 */

		String Response = xummClient.getPayload(postPayloadResponseDAO.getUuid());
		System.out.println(Response);
		GetPayloadDAO resultTwo = deserialize.getPayload(Response);
		System.out.println(resultTwo.getResponse());


	}
}