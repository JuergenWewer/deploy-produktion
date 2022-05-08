package io.vertx.starter;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.multipart.MultipartForm;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

class ExternalCondition {

  static public boolean local() {
    return true;
  }

}

@ExtendWith(VertxExtension.class)
public class ApiVerticleTest {

  static private String apiurl;
  static private int apiport;
  static private String yuuvisurl;
  static private int yuuvisport;
  private boolean ssl = false;
  private boolean selfSignCertificate = true;
  private CountDownLatch producerReadyLatch = new CountDownLatch(1);
  private static Vertx vertx = Vertx.vertx();
//  private String user= "prosozTest";
//  private String pwd = "sd43wGFds32(%1";
  private String user= "yuuvis";
  private String pwd = "optimalsystem";

  @Test
  @BeforeAll
  static public void setClusterIP() {
    System.out.println("set the local variables");
    yuuvisurl = "192.168.178.91";
    yuuvisport = 30080;

    if (ExternalCondition.local()) {
      apiurl = "localhost";
      apiport = 8080;
    } else {
      apiurl = "192.168.178.91";
      apiport = 30036;
    }
  }

  @BeforeEach
  void deploy_verticle(VertxTestContext testContext) throws Exception {
    Map<String,String> newEnv = new HashMap<>();
    newEnv.put("AUTHENTICATION_SERVICE_HOST", yuuvisurl);
    newEnv.put("AUTHENTICATION_SERVICE_PORT", Integer.toString(yuuvisport));
    newEnv.put("TENANT", "yuuvistest");
    newEnv.put("SERVERURL", apiurl + ":" + apiport);
    newEnv.put("USER", user);
    newEnv.put("PASSWORD", pwd);
    newEnv.put("YUUVISUSER", "root");
    newEnv.put("YUUVISPASSWORD", "optimalsystem");
    if (ssl) {
      newEnv.put("SSLENABLED", "true");
      if (selfSignCertificate) {
        newEnv.put("SELFSIGNEDCERTIFICATE", "true");
        newEnv.put("KEYSTORECN", "192.168.178.91");
        newEnv.put("KEYSTOREPASSWORD", "secret");
      } else {
        newEnv.put("SELFSIGNEDCERTIFICATE", "false");
        newEnv.put("KEYSTOREPATH", "./src/main/resources/yuuviskeystoremacpro.jks");
        newEnv.put("KEYSTOREPASSWORD", "optimalsystem");
      }
    } else {
      newEnv.put("SSLENABLED", "false");
    }
    setEnv(newEnv);
    vertx.deployVerticle(new ApiVerticle(), testContext.succeeding(id -> {
      System.out.println("server startet");
      producerReadyLatch.countDown();
      testContext.completeNow();
    }));
  }


  private void clientSendGet(VertxTestContext testContext, String endPoint, String user, String pwd,
                             int responseStatusCode, String responseStatusMessage, String responseBody) {

    WebClientOptions options = new WebClientOptions();
    options.setKeepAlive(false);
    options.setVerifyHost(false);
    options.setTrustAll(true);
    WebClient client = WebClient.create(this.vertx, options);

    if (ssl) {
      client
        .getAbs("https://" + apiurl + ":" + apiport + endPoint)
        .ssl(true)
        .basicAuthentication(user, pwd)
        .send()
        .onComplete(
          testContext.succeeding(buffer -> testContext.verify(() -> {
            assertThat(buffer.statusCode()).isEqualTo(responseStatusCode);
            if (!responseStatusMessage.isEmpty()) {
              assertThat(buffer.statusMessage()).isEqualTo(responseStatusMessage);
            }
            if (!responseBody.isEmpty()) {
              assertThat(buffer.bodyAsString()).isEqualTo(responseBody);
            }
            testContext.completeNow();
          })));
    } else {
      client
        .get(apiport, apiurl, endPoint)
        .basicAuthentication(user, pwd)
        .send()
        .onComplete(
          testContext.succeeding(buffer -> testContext.verify(() -> {
            assertThat(buffer.statusCode()).isEqualTo(responseStatusCode);
            if (!responseStatusMessage.isEmpty()) {
              assertThat(buffer.statusMessage()).isEqualTo(responseStatusMessage);
            }
            if (!responseBody.isEmpty()) {
              assertThat(buffer.bodyAsString()).isEqualTo(responseBody);
            }
            testContext.completeNow();
          })));
    }
  }

  private void clientSendGetWithQueryParam(VertxTestContext testContext, String endPoint, String user, String pwd,
                             int responseStatusCode1, int responseStatusCode2,String responseStatusMessage, String responseBody,
                             String queryParam, String queryValue) {

    WebClientOptions options = new WebClientOptions();
    options.setKeepAlive(false);
    options.setVerifyHost(false);
    options.setTrustAll(true);
    WebClient client = WebClient.create(this.vertx, options);

    if (ssl) {
      client
        .getAbs("https://" + apiurl + ":" + apiport + endPoint)
        .ssl(true)
        .basicAuthentication(user, pwd)
        .addQueryParam(queryParam, queryValue)
        .send()
        .onComplete(
          testContext.succeeding(buffer -> testContext.verify(() -> {
            if (!responseStatusMessage.isEmpty()) {
              assertThat(buffer.statusMessage()).isEqualTo(responseStatusMessage);
            }
            if (!responseBody.isEmpty()) {
              assertThat(buffer.bodyAsString()).isEqualTo(responseBody);
            }
            if (buffer.statusCode() == responseStatusCode1 || buffer.statusCode() == responseStatusCode2) {
              testContext.completeNow();
            } else {
              fail("failure");
            }
          })));
    } else {
      client
        .get(apiport, apiurl, endPoint)
        .basicAuthentication(user, pwd)
        .addQueryParam(queryParam, queryValue)
        .send()
        .onComplete(
          testContext.succeeding(buffer -> testContext.verify(() -> {
            if (!responseStatusMessage.isEmpty()) {
              assertThat(buffer.statusMessage()).isEqualTo(responseStatusMessage);
            }
            if (!responseBody.isEmpty()) {
              assertThat(buffer.bodyAsString()).isEqualTo(responseBody);
            }
            if (buffer.statusCode() == responseStatusCode1 || buffer.statusCode() == responseStatusCode2) {
              testContext.completeNow();
            } else {
              fail("failure");
            }
          })));
    }
  }

  private void clientSendGetWithTwoQueryParams(VertxTestContext testContext, String endPoint, String user, String pwd,
                                           int responseStatusCode1, int responseStatusCode2,String responseStatusMessage, String responseBody,
                                           String queryParam1, String queryValue1, String queryParam2, String queryValue2) {

    WebClientOptions options = new WebClientOptions();
    options.setKeepAlive(false);
    options.setVerifyHost(false);
    options.setTrustAll(true);
    WebClient client = WebClient.create(this.vertx, options);

    if (ssl) {
      client
        .getAbs("https://" + apiurl + ":" + apiport + endPoint)
        .ssl(true)
        .basicAuthentication(user, pwd)
        .addQueryParam(queryParam1, queryValue1)
        .addQueryParam(queryParam2, queryValue2)
        .send()
        .onComplete(
          testContext.succeeding(buffer -> testContext.verify(() -> {
            if (!responseStatusMessage.isEmpty()) {
              assertThat(buffer.statusMessage()).isEqualTo(responseStatusMessage);
            }
            if (!responseBody.isEmpty()) {
              assertThat(buffer.bodyAsString()).isEqualTo(responseBody);
            }
            if (buffer.statusCode() == responseStatusCode1 || buffer.statusCode() == responseStatusCode2) {
              testContext.completeNow();
            } else {
              fail("failure");
            }
          })));
    } else {
      client
        .get(apiport, apiurl, endPoint)
        .basicAuthentication(user, pwd)
        .addQueryParam(queryParam1, queryValue1)
        .addQueryParam(queryParam2, queryValue2)
        .send()
        .onComplete(
          testContext.succeeding(buffer -> testContext.verify(() -> {
            if (!responseStatusMessage.isEmpty()) {
              assertThat(buffer.statusMessage()).isEqualTo(responseStatusMessage);
            }
            if (!responseBody.isEmpty()) {
              assertThat(buffer.bodyAsString()).isEqualTo(responseBody);
            }
            if (buffer.statusCode() == responseStatusCode1 || buffer.statusCode() == responseStatusCode2) {
              testContext.completeNow();
            } else {
              fail("failure");
            }
          })));
    }
  }

  private void clientSendPost(VertxTestContext testContext, JsonObject klientAkte,String endPoint, String user, String pwd,
                              int responseStatusCode, String responseStatusMessage, String responseBody) {
    WebClientOptions options = new WebClientOptions();
    options.setKeepAlive(false);
    options.setVerifyHost(false);
    options.setTrustAll(true);
    WebClient client = WebClient.create(this.vertx, options);

    if (ssl) {
      client
        .postAbs("https://" + apiurl + ":" + apiport + endPoint)
        .ssl(true)
        .basicAuthentication(user, pwd)
        .sendJsonObject(klientAkte)
        .onComplete(
          testContext.succeeding(buffer -> testContext.verify(() -> {
            if (!responseStatusMessage.isEmpty()) {
              assertThat(buffer.statusMessage()).isEqualTo(responseStatusMessage);
            }
            if (!responseBody.isEmpty()) {
              assertThat(buffer.bodyAsString()).isEqualTo(responseBody);
            }
            if (buffer.statusCode() == responseStatusCode || buffer.statusCode() == 409) {
              testContext.completeNow();
            } else {
              fail("failure");
            }
          })));
    } else {
      client
        .post(apiport, apiurl, endPoint)
        .basicAuthentication(user, pwd)
        .sendJsonObject(klientAkte)
        .onComplete(
          testContext.succeeding(buffer -> testContext.verify(() -> {
            System.out.println("responseStatusMessage: " + buffer.statusMessage());
            System.out.println("responseBody: " + buffer.bodyAsString());
            if (!responseStatusMessage.isEmpty()) {
              assertThat(buffer.statusMessage()).isEqualTo(responseStatusMessage);
            }
            if (!responseBody.isEmpty()) {
              assertThat(buffer.bodyAsString()).isEqualTo(responseBody);
            }
            if (buffer.statusCode() == responseStatusCode || buffer.statusCode() == 409) {
              testContext.completeNow();
            } else {
              fail("failure");
            }
          })));
    }

  }

  private void clientSendPostWithMultiPartForm(VertxTestContext testContext, MultipartForm form, String endPoint, String user, String pwd,
                              int responseStatusCode, String responseStatusMessage, String responseBody) {
    WebClientOptions options = new WebClientOptions();
    options.setKeepAlive(false);
    options.setVerifyHost(false);
    options.setTrustAll(true);
    WebClient client = WebClient.create(this.vertx, options);

    if (ssl) {
      client
        .postAbs("https://" + apiurl + ":" + apiport + endPoint)
        .ssl(true)
        .basicAuthentication(user, pwd)
        .sendMultipartForm(form)
        .onComplete(
          testContext.succeeding(buffer -> testContext.verify(() -> {
            if (!responseStatusMessage.isEmpty()) {
              assertThat(buffer.statusMessage()).isEqualTo(responseStatusMessage);
            }
            if (!responseBody.isEmpty()) {
              assertThat(buffer.bodyAsString()).isEqualTo(responseBody);
            }
            if (buffer.statusCode() == responseStatusCode || buffer.statusCode() == 409) {
              testContext.completeNow();
            } else {
              fail("failure");
            }
          })));
    } else {
      client
        .post(apiport, apiurl, endPoint)
        .basicAuthentication(user, pwd)
        .sendMultipartForm(form)
        .onComplete(
          testContext.succeeding(buffer -> testContext.verify(() -> {
            if (!responseStatusMessage.isEmpty()) {
              assertThat(buffer.statusMessage()).isEqualTo(responseStatusMessage);
            }
            if (!responseBody.isEmpty()) {
              assertThat(buffer.bodyAsString()).isEqualTo(responseBody);
            }
            if (buffer.statusCode() == responseStatusCode || buffer.statusCode() == 409) {
              testContext.completeNow();
            } else {
              fail("failure");
            }
          })));
    }

  }

  private void clientSendPut(VertxTestContext testContext, JsonObject klientAkte,String endPoint, String user, String pwd,
                              int responseStatusCode, String responseStatusMessage, String responseBody) {
    WebClientOptions options = new WebClientOptions();
    options.setKeepAlive(false);
    options.setVerifyHost(false);
    options.setTrustAll(true);
    WebClient client = WebClient.create(this.vertx, options);

    if (ssl) {
      client
        .putAbs("https://" + apiurl + ":" + apiport + endPoint)
        .ssl(true)
        .basicAuthentication(user, pwd)
        .sendJsonObject(klientAkte)
        .onComplete(
          testContext.succeeding(buffer -> testContext.verify(() -> {
            if (!responseStatusMessage.isEmpty()) {
              assertThat(buffer.statusMessage()).isEqualTo(responseStatusMessage);
            }
            if (!responseBody.isEmpty()) {
              assertThat(buffer.bodyAsString()).isEqualTo(responseBody);
            }
            if (buffer.statusCode() == responseStatusCode || buffer.statusCode() == 409) {
              testContext.completeNow();
            } else {
              fail("failure");
            }
          })));
    } else {
      client
        .put(apiport, apiurl, endPoint)
        .basicAuthentication(user, pwd)
        .sendJsonObject(klientAkte)
        .onComplete(
          testContext.succeeding(buffer -> testContext.verify(() -> {
            if (!responseStatusMessage.isEmpty()) {
              assertThat(buffer.statusMessage()).isEqualTo(responseStatusMessage);
            }
            if (!responseBody.isEmpty()) {
              assertThat(buffer.bodyAsString()).isEqualTo(responseBody);
            }
            if (buffer.statusCode() == responseStatusCode || buffer.statusCode() == 409) {
              testContext.completeNow();
            } else {
              fail("failure");
            }
          })));
    }

  }

  @Test
  void testHealth(VertxTestContext testContext) throws Throwable {
    if (producerReadyLatch.await(60, TimeUnit.SECONDS)) {
      System.out.println("start the webclient: testHealth");
      String endPoint = "/Health";
      int responseStatusCode = 200;
      String responseStatusMessage = "OK";
      String responseBody = "Service health is OK";
      clientSendGet(testContext, endPoint, user, pwd, responseStatusCode, responseStatusMessage, responseBody);
    }
  }

///////////////////  Klientakte /////////////////////////
  @Test
  void testKlientakteGET(VertxTestContext testContext) throws Throwable {
    if (producerReadyLatch.await(60, TimeUnit.SECONDS)) {
      System.out.println("start the webclient: testKlientakteGET");
      String endPoint = "/api/Klientakte/Klientid";
      endPoint = "/api/Klientakte/9d0b0618-c3ed-4cb2-97b1-663c320814fa";
      int responseStatusCode = 200;
      String responseStatusMessage = "";
      String responseBody = "";
      clientSendGet(testContext, endPoint, user, pwd, responseStatusCode, responseStatusMessage, responseBody);
    }
  }

  @Test
  void testKlientaktePOST(VertxTestContext testContext) throws Throwable {
    if (producerReadyLatch.await(60, TimeUnit.SECONDS)) {
      System.out.println("start the webclient: testKlientaktePOST");
      JsonObject klientAkte = getKlientakte();
      klientAkte = new JsonObject(" {\"klient\":{\"id\":\"9d0b0618-c3ed-4cb2-97b1-663c320814fa\",\"vorname\":\"Hans\",\"nachname\":\"Xtremepyrd\",\"geburtsdatum\":\"31.03.2021\",\"adresse\":\"\"},\"archivieren\":\"false\",\"archivierenDatum\":\"\",\"loeschen\":\"false\",\"loeschenDatum\":\"\",\"eaktenID\":\"\"}");
      String endPoint = "/api/Klientakte";
      int responseStatusCode = 200;
      String responseStatusMessage = "";
      String responseBody = "";
      clientSendPost(testContext, klientAkte, endPoint, user, pwd, responseStatusCode, responseStatusMessage, responseBody);
    }
  }

  @Test
  void testKlientaktePUT(VertxTestContext testContext) throws Throwable {
    if (producerReadyLatch.await(60, TimeUnit.SECONDS)) {
      System.out.println("start the webclient: testKlientaktePOST");
      JsonObject klientAkte = getKlientakte();
      klientAkte.put("vorname","JürgenTest");
      String endPoint = "/api/Klientakte";
      int responseStatusCode = 200;
      String responseStatusMessage = "";
      String responseBody = "";
      clientSendPut(testContext, klientAkte, endPoint, user, pwd, responseStatusCode, responseStatusMessage, responseBody);
    }
  }



  private JsonObject getKlientakte() {
    JsonObject klientAkte = new JsonObject();
    JsonObject klient = new JsonObject();
    klient.put("id",UUID.randomUUID().toString());
    klient.put("vorname","vorname");
    klient.put("nachname","nachname");
    klient.put("geburtsdatum","18.02.2003");
    klient.put("adresse","adresse");

    klientAkte.put("klient",klient);
    klientAkte.put("archivieren","true");
    klientAkte.put("archivdatum","18.02.2003");
    klientAkte.put("loeschen","false");
    klientAkte.put("loeschdatum","18.02.2003");
    klientAkte.put("eaktenID","");

    return klientAkte;
  }

  ///////////////////  Fallakte  /////////////////////////

  @Test
  void testFallakteGET(VertxTestContext testContext) throws Throwable {
    if (producerReadyLatch.await(60, TimeUnit.SECONDS)) {
      System.out.println("start the webclient: testGET");
      String endPoint = "/api/Fallakte/Akte2";
      int responseStatusCode = 200;
      String responseStatusMessage = "";
      String responseBody = "";
      clientSendGet(testContext, endPoint, user, pwd, responseStatusCode, responseStatusMessage, responseBody);
    }
  }

  @Test
  void testFallaktePOSTVorgangID(VertxTestContext testContext) throws Throwable {
    if (producerReadyLatch.await(60, TimeUnit.SECONDS)) {
      System.out.println("start the webclient: testFallaktePOST");
      JsonObject fallAkte = getFallakte();
      String endPoint = "/api/Fallakte";
      int responseStatusCode = 200;
      String responseStatusMessage = "";
      String responseBody = "";
      clientSendPost(testContext, fallAkte, endPoint, user, pwd, responseStatusCode, responseStatusMessage, responseBody);
    }
  }

  @Test
  void testFallaktePOSTVorgangIDRegister(VertxTestContext testContext) throws Throwable {
    if (producerReadyLatch.await(60, TimeUnit.SECONDS)) {
      System.out.println("start the webclient: testFallaktePOST");
      JsonObject fallAkte = getFallakteRegister();
      String endPoint = "/api/Fallakte";
      int responseStatusCode = 200;
      String responseStatusMessage = "";
      String responseBody = "";
      clientSendPost(testContext, fallAkte, endPoint, user, pwd, responseStatusCode, responseStatusMessage, responseBody);
    }
  }

  @Test
  void testFallaktePUTVorgangID(VertxTestContext testContext) throws Throwable {
    if (producerReadyLatch.await(60, TimeUnit.SECONDS)) {
      System.out.println("start the webclient: testFallaktePUT");
      JsonObject fallAkte = getFallakte();
      String endPoint = "/api/Fallakte";
      int responseStatusCode = 200;
      String responseStatusMessage = "";
      String responseBody = "";
      clientSendPut(testContext, fallAkte, endPoint, user, pwd, responseStatusCode, responseStatusMessage, responseBody);
    }
  }

  private JsonObject getFallakte() {
    JsonObject fallAkte1 = new JsonObject();
    fallAkte1.put("eaktenID","");
    JsonObject vorgang = new JsonObject();
    vorgang.put("aktenzeichen","aktenzeichenValue");
    vorgang.put("archivieren","true");
    vorgang.put("archivierenDatum","31.03.2002");
    vorgang.put("bemerkung","bemerkungValue");
    vorgang.put("id","Akte2");
    vorgang.put("loeschen","false");
    vorgang.put("loeschenDatum","31.03.2002");
    vorgang.put("rechtsgebiet","rechtsgebietValue");
    vorgang.put("register","");
    vorgang.put("zustaendigerSachbearbeiter","zustaendigerSachbearbeiterValue");
    fallAkte1.put("vorgang", vorgang);
    JsonObject personBaseExtended1 = new JsonObject();
    personBaseExtended1.put("vorname","vornameantragssteller");
    personBaseExtended1.put("nachname","nachnameantragssteller");
    personBaseExtended1.put("id","idantragssteller");
    personBaseExtended1.put("geburtsdatum","31.03.2002");
    fallAkte1.put("antragssteller", personBaseExtended1);
    JsonObject personBaseExtended2 = new JsonObject();
    personBaseExtended2.put("vorname","vornameleistungsempfaenger");
    personBaseExtended2.put("nachname","nachnameleistungsempfaenger");
    personBaseExtended2.put("id","idleistungsempfaenger");
    personBaseExtended2.put("geburtsdatum","31.03.2002");
    fallAkte1.put("leistungsempfaenger", personBaseExtended2);
    JsonObject personBaseExtended3 = new JsonObject();
    personBaseExtended3.put("vorname","vornameunterhaltspflichtiger");
    personBaseExtended3.put("nachname","nachnameunterhaltspflichtiger");
    personBaseExtended3.put("id","idunterhaltspflichtiger");
    personBaseExtended3.put("geburtsdatum","31.03.2002");
    fallAkte1.put("unterhaltspflichtiger", personBaseExtended3);
    return fallAkte1;
  }
  private JsonObject getFallakteRegister() {
    JsonObject fallAkte1 = new JsonObject();
    fallAkte1.put("eAktenID","eAktenID1234");
    JsonObject vorgang = new JsonObject();
    vorgang.put("aktenzeichen","aktenzeichenValue");
    vorgang.put("archivieren","true");
    vorgang.put("archivierenDatum","31.03.2002");
    vorgang.put("bemerkung","bemerkungValue");
    vorgang.put("id","Akte1");
    vorgang.put("loeschen","false");
    vorgang.put("loeschenDatum","31.03.2002");
    vorgang.put("rechtsgebiet","rechtsgebietValue");
    vorgang.put("register","Register1");
    vorgang.put("zustaendigerSachbearbeiter","zustaendigerSachbearbeiterValue");
    fallAkte1.put("vorgang", vorgang);
    JsonObject personBaseExtended1 = new JsonObject();
    personBaseExtended1.put("vorname","vornameantragssteller");
    personBaseExtended1.put("nachname","nachnameantragssteller");
    personBaseExtended1.put("id","idantragssteller");
    personBaseExtended1.put("geburtsdatum","31.03.2002");
    fallAkte1.put("antragssteller", personBaseExtended1);
    JsonObject personBaseExtended2 = new JsonObject();
    personBaseExtended2.put("vorname","vornameleistungsempfaenger");
    personBaseExtended2.put("nachname","nachnameleistungsempfaenger");
    personBaseExtended2.put("id","idleistungsempfaenger");
    personBaseExtended2.put("geburtsdatum","31.03.2002");
    fallAkte1.put("leistungsempfaenger", personBaseExtended2);
    JsonObject personBaseExtended3 = new JsonObject();
    personBaseExtended3.put("vorname","Fritz");
    personBaseExtended3.put("nachname","Walter");
    personBaseExtended3.put("id","idunterhaltspflichtiger");
    personBaseExtended3.put("geburtsdatum","31.03.2002");
    fallAkte1.put("unterhaltspflichtiger", personBaseExtended3);
    return fallAkte1;
  }


///////////////    Get Dokumente Fallakte /////////////////

  @Test
  void testDokumentGETGetDokumeteeDokumentenIDFallakte(VertxTestContext testContext) throws Throwable {
    if (producerReadyLatch.await(60, TimeUnit.SECONDS)) {
      System.out.println("start the webclient: testGET");
      String endPoint = "/api/Dokument";
      int responseStatusCode1 = 200; //found
      int responseStatusCode2 = 204; // not found
      String responseStatusMessage = "";
      String responseBody = "";
      String queryParam = "eDokumentenID";
      String queryValue ="12345678901";
      clientSendGetWithQueryParam(testContext, endPoint, user, pwd, responseStatusCode1, responseStatusCode2, responseStatusMessage, responseBody,
        queryParam, queryValue);
    }
  }

///////////////    Get Dokumente Klientakte  /////////////

  @Test
  void testDokumentGETGetDokumeteeDokumentenIDKlientakte(VertxTestContext testContext) throws Throwable {
    if (producerReadyLatch.await(60, TimeUnit.SECONDS)) {
      System.out.println("start the webclient: testGET");
      String endPoint = "/api/Dokument";
      int responseStatusCode1 = 200; //found
      int responseStatusCode2 = 204; // not found
      String responseStatusMessage = "";
      String responseBody = "";
      String queryParam = "eDokumentenID";
      String queryValue ="12345678901";
      clientSendGetWithQueryParam(testContext, endPoint, user, pwd, responseStatusCode1, responseStatusCode2, responseStatusMessage, responseBody,
        queryParam, queryValue);
    }
  }

///////////////    POST Dokument Fallakte

  @Test
  void testDokumentFallaktePOSTVorgangID(VertxTestContext testContext) throws Throwable {
    if (producerReadyLatch.await(60, TimeUnit.SECONDS)) {
      System.out.println("start the webclient: testDokumentPOSTVorgangID");

      JsonObject dokument = getFallakteDocument("Akte2","");
      MultipartForm form = MultipartForm.create()
        .attribute("data", dokument.encode())
        .binaryFileUpload(
        "File",
        "Lizenz",
        "src/test/resources/LicenseCertificate-R5292742.pdf",
        "application/pdf");
      String endPoint = "/api/Dokument";
      int responseStatusCode = 200;
      String responseStatusMessage = "";
      String responseBody = "";
      clientSendPostWithMultiPartForm(testContext, form, endPoint, user, pwd, responseStatusCode, responseStatusMessage, responseBody);
    }
  }

  @Test
  void testDokumentFallaktePOSTVorgangIDRegister(VertxTestContext testContext) throws Throwable {
    if (producerReadyLatch.await(60, TimeUnit.SECONDS)) {
      System.out.println("start the webclient: testDokumentPOST");
      JsonObject dokument = getFallakteDocument("Akte1","Register1");
      MultipartForm form = MultipartForm.create()
        .attribute("data", dokument.encode())
        .binaryFileUpload(
          "File",
          "Lizenz",
          "src/test/resources/LicenseCertificate-R5292742.pdf",
          "application/pdf");
      String endPoint = "/api/Dokument";
      int responseStatusCode = 200;
      String responseStatusMessage = "";
      String responseBody = "";
      clientSendPostWithMultiPartForm(testContext, form, endPoint, user, pwd, responseStatusCode, responseStatusMessage, responseBody);
    }
  }

  private JsonObject getFallakteDocument(String akte, String register) {
    JsonObject dokument = new JsonObject();
    dokument.put("Fallakte.EAktenID","Fallakte.EAktenID");
    dokument.put("Fallakte.Vorgang.Aktenzeichen","Fallakte.Vorgang.Aktenzeichen");
    dokument.put("Fallakte.Vorgang.Archivieren","Fallakte.Vorgang.Archivieren");
    dokument.put("Fallakte.Vorgang.ArchivierenDatum","Fallakte.Vorgang.ArchivierenDatum");
    dokument.put("Fallakte.Vorgang.Bemerkung","Fallakte.Vorgang.Bemerkung");
    dokument.put("Fallakte.Vorgang.ID",akte);
    dokument.put("Fallakte.Vorgang.Loeschen","Fallakte.Vorgang.Loeschen");
    dokument.put("Fallakte.Vorgang.LoeschenDatum","15.02.2003");
    dokument.put("Fallakte.Vorgang.Rechtsgebiet","Fallakte.Vorgang.Rechtsgebiet");
    dokument.put("Fallakte.Vorgang.Register",register);
    dokument.put("Fallakte.Vorgang.ZustaendigerSachbearbeiter","Fallakte.Vorgang.ZustaendigerSachbearbeiter");
    dokument.put("Fallakte.Antragssteller.ID","Fallakte.Antragssteller.ID");
    dokument.put("Fallakte.Antragssteller.Geburtsdatum","15.02.2003");
    dokument.put("Fallakte.Antragssteller.Vorname","Fallakte.Antragssteller.Vorname");
    dokument.put("Fallakte.Antragssteller.Nachname","Fallakte.Antragssteller.Nachname");
    dokument.put("Fallakte.Leistungsempfaenger.ID","Fallakte.Leistungsempfaenger.ID");
    dokument.put("Fallakte.Leistungsempfaenger.Geburtsdatum","15.02.2003");
    dokument.put("Fallakte.Leistungsempfaenger.Vorname","Fallakte.Leistungsempfaenger.Vorname");
    dokument.put("Fallakte.Leistungsempfaenger.Nachname","Fallakte.Leistungsempfaenger.Nachname");
    dokument.put("Fallakte.Unterhaltspflichtiger.ID","Fallakte.Unterhaltspflichtiger.ID");
    dokument.put("Fallakte.Unterhaltspflichtiger.Geburtsdatum","15.02.2003");
    dokument.put("Fallakte.Unterhaltspflichtiger.Vorname","Fallakte.Unterhaltspflichtiger.Vorname");
    dokument.put("Fallakte.Unterhaltspflichtiger.Nachname","Fallakte.Unterhaltspflichtiger.Nachname");
    dokument.put("Fallakte.OrdnerObjektTypName","Fallakte.OrdnerObjektTypName");
    dokument.put("Fallakte.RegisterObjektTypName","Fallakte.RegisterObjektTypName");
    dokument.put("Klientakte.EAktenID","");
    dokument.put("Klientakte.Klient.Adresse","");
    dokument.put("Klientakte.Klient.ID","");
    dokument.put("Klientakte.Klient.Geburtsdatum","15.02.2003");
    dokument.put("Klientakte.Klient.Vorname","");
    dokument.put("Klientakte.Klient.Nachname","Klientakte.Klient.Nachname");
    dokument.put("Klientakte.Archivieren","Klientakte.Archivieren");
    dokument.put("Klientakte.ArchivierenDatum","15.02.2003");
    dokument.put("Klientakte.Loeschen","Klientakte.Loeschen");
    dokument.put("Klientakte.LoeschenDatum","15.02.2003");
    dokument.put("Klientakte.OrdnerObjektTypName","Klientakte.OrdnerObjektTypName");
    dokument.put("Dokument.EDokumentenID","");
    dokument.put("Dokument.ErstellungZeitpunkt","2021-02-18");
    dokument.put("Dokument.Typ","Dokument.Typ");
    dokument.put("Dokument.Vorlage","Dokument.Vorlage");
    dokument.put("Dokument.Sachbearbeiter.Kennung","Dokument.Sachbearbeiter.Kennung");
    dokument.put("Dokument.Sachbearbeiter.Vorname","Dokument.Sachbearbeiter.Vorname");
    dokument.put("Dokument.Sachbearbeiter.Nachname","Dokument.Sachbearbeiter.Nachname");
    dokument.put("Dokument.Empfaenger.Adresse","Dokument.Empfaenger.Adresse");
    dokument.put("Dokument.Empfaenger.Vorname","Dokument.Empfaenger.Vorname");
    dokument.put("Dokument.Empfaenger.Nachname","Dokument.Empfaenger.Nachname");
    dokument.put("Dokument.ProsozDateiname","Dokument.ProsozDateiname");
    dokument.put("Dokument.DokumentObjektTypName","Dokument.DokumentObjektTypName");
    dokument.put("Dokument.ContentUrl","Dokument.ContentUrl");
    return dokument;
  }

  ////////////////   POST Dokument Klientakte

  @Test
  void testDokumentKlientaktePOSTVorgangID(VertxTestContext testContext) throws Throwable {
    if (producerReadyLatch.await(60, TimeUnit.SECONDS)) {
      System.out.println("start the webclient: testDokumentPOST");
      JsonObject dokument = getKlientakteDocument();
      MultipartForm form = MultipartForm.create()
        .attribute("data", dokument.encode())
        .binaryFileUpload(
          "File",
          "Lizenz",
          "src/test/resources/LicenseCertificate-R5292742.pdf",
          "application/octet-stream");
      String endPoint = "/api/Dokument";
      int responseStatusCode = 200;
      String responseStatusMessage = "";
      String responseBody = "";
      clientSendPostWithMultiPartForm(testContext, form, endPoint, user, pwd, responseStatusCode, responseStatusMessage, responseBody);
    }
  }

  private JsonObject getKlientakteDocument() {
    JsonObject dokument = new JsonObject();
    dokument.put("Fallakte.EAktenID","Fallakte.EAktenID");
    dokument.put("Fallakte.Vorgang.Aktenzeichen","Fallakte.Vorgang.Aktenzeichen");
    dokument.put("Fallakte.Vorgang.Archivieren","Fallakte.Vorgang.Archivieren");
    dokument.put("Fallakte.Vorgang.ArchivierenDatum","15.02.2003");
    dokument.put("Fallakte.Vorgang.Bemerkung","Fallakte.Vorgang.Bemerkung");
    dokument.put("Fallakte.Vorgang.ID","Akte2");
    dokument.put("Fallakte.Vorgang.Loeschen","Fallakte.Vorgang.Loeschen");
    dokument.put("Fallakte.Vorgang.LoeschenDatum","15.02.2003");
    dokument.put("Fallakte.Vorgang.Rechtsgebiet","Fallakte.Vorgang.Rechtsgebiet");
    dokument.put("Fallakte.Vorgang.Register","");
    dokument.put("Fallakte.Vorgang.ZustaendigerSachbearbeiter","Fallakte.Vorgang.ZustaendigerSachbearbeiter");
    dokument.put("Fallakte.Antragssteller.ID","Fallakte.Antragssteller.ID");
    dokument.put("Fallakte.Antragssteller.Geburtsdatum","15.02.2003");
    dokument.put("Fallakte.Antragssteller.Vorname","Fallakte.Antragssteller.Vorname");
    dokument.put("Fallakte.Antragssteller.Nachname","Fallakte.Antragssteller.Nachname");
    dokument.put("Fallakte.Leistungsempfaenger.ID","Fallakte.Leistungsempfaenger.ID");
    dokument.put("Fallakte.Leistungsempfaenger.Geburtsdatum","15.02.2003");
    dokument.put("Fallakte.Leistungsempfaenger.Vorname","Fallakte.Leistungsempfaenger.Vorname");
    dokument.put("Fallakte.Leistungsempfaenger.Nachname","Fallakte.Leistungsempfaenger.Nachname");
    dokument.put("Fallakte.Unterhaltspflichtiger.ID","Fallakte.Unterhaltspflichtiger.ID");
    dokument.put("Fallakte.Unterhaltspflichtiger.Geburtsdatum","15.02.2003");
    dokument.put("Fallakte.Unterhaltspflichtiger.Vorname","Fallakte.Unterhaltspflichtiger.Vorname");
    dokument.put("Fallakte.Unterhaltspflichtiger.Nachname","Fallakte.Unterhaltspflichtiger.Nachname");
    dokument.put("Fallakte.OrdnerObjektTypName","Fallakte.OrdnerObjektTypName");
    dokument.put("Fallakte.RegisterObjektTypName","Fallakte.RegisterObjektTypName");
    dokument.put("Klientakte.EAktenID","");
    dokument.put("Klientakte.Klient.Adresse","");
    dokument.put("Klientakte.Klient.ID","Klientid");
    dokument.put("Klientakte.Klient.Geburtsdatum","15.02.2003");
    dokument.put("Klientakte.Klient.Vorname","");
    dokument.put("Klientakte.Klient.Nachname","Klientakte.Klient.Nachname");
    dokument.put("Klientakte.Archivieren","Klientakte.Archivieren");
    dokument.put("Klientakte.ArchivierenDatum","15.02.2003");
    dokument.put("Klientakte.Loeschen","Klientakte.Loeschen");
    dokument.put("Klientakte.LoeschenDatum","15.02.2003");
    dokument.put("Klientakte.OrdnerObjektTypName","Klientakte.OrdnerObjektTypName");
    dokument.put("Dokument.EDokumentenID","");
    dokument.put("Dokument.ErstellungZeitpunkt","15.02.2003");
    dokument.put("Dokument.Typ","Dokument.Typ");
    dokument.put("Dokument.Vorlage","Dokument.Vorlage");
    dokument.put("Dokument.Sachbearbeiter.Kennung","Dokument.Sachbearbeiter.Kennung");
    dokument.put("Dokument.Sachbearbeiter.Vorname","Dokument.Sachbearbeiter.Vorname");
    dokument.put("Dokument.Sachbearbeiter.Nachname","Dokument.Sachbearbeiter.Nachname");
    dokument.put("Dokument.Empfaenger.Adresse","Dokument.Empfaenger.Adresse");
    dokument.put("Dokument.Empfaenger.Vorname","Dokument.Empfaenger.Vorname");
    dokument.put("Dokument.Empfaenger.Nachname","Dokument.Empfaenger.Nachname");
    dokument.put("Dokument.ProsozDateiname","Dokument.ProsozDateiname");
    dokument.put("Dokument.DokumentObjektTypName","Dokument.DokumentObjektTypName");
    dokument.put("Dokument.ContentUrl","Dokument.ContentUrl");
    return dokument;
  }

  ///////////////    Get GetDokumente  KlientID
  @Test
  void testDokumentGETGetDokumeteKlientID(VertxTestContext testContext) throws Throwable {
    if (producerReadyLatch.await(60, TimeUnit.SECONDS)) {
      System.out.println("start the webclient: testDokumentGETGetDokumeteKlientID");
      String endPoint = "/api/Dokument/GetDokumente";
      int responseStatusCode1 = 200; //found
      int responseStatusCode2 = 204; // not found
      String responseStatusMessage = "";
      String responseBody = "";
      String queryParam = "klientID";
      String queryValue ="Klientid";
      clientSendGetWithQueryParam(testContext, endPoint, user, pwd, responseStatusCode1, responseStatusCode2, responseStatusMessage, responseBody,
        queryParam, queryValue);
    }
  }

///////////////     Get GetDokumente Fallakte
  @Test
  void testDokumentGETGetDokumeteVorgangID(VertxTestContext testContext) throws Throwable {
    if (producerReadyLatch.await(60, TimeUnit.SECONDS)) {
      System.out.println("start the webclient: testDokumentGETGetDokumeteKlientID");
      String endPoint = "/api/Dokument/GetDokumente";
      int responseStatusCode1 = 200; //found
      int responseStatusCode2 = 204; // not found
      String responseStatusMessage = "";
      String responseBody = "";
      String queryParam = "vorgangID";
      String queryValue ="Akte2";
      clientSendGetWithQueryParam(testContext, endPoint, user, pwd, responseStatusCode1, responseStatusCode2, responseStatusMessage, responseBody,
        queryParam, queryValue);
    }
  }

  @Test
  void testDokumentGETGetDokumeteVorgangIDRegister(VertxTestContext testContext) throws Throwable {
    if (producerReadyLatch.await(60, TimeUnit.SECONDS)) {
      System.out.println("start the webclient: testDokumentGETGetDokumeteKlientID");
      String endPoint = "/api/Dokument/GetDokumente";
      int responseStatusCode1 = 200; //found
      int responseStatusCode2 = 204; // not found
      String responseStatusMessage = "";
      String responseBody = "";
      String queryParam1 = "vorgangID";
      String queryValue1 ="Akte1";
      String queryParam2 = "vorgangRegister";
      String queryValue2 ="Register1";
      clientSendGetWithTwoQueryParams(testContext, endPoint, user, pwd, responseStatusCode1, responseStatusCode2, responseStatusMessage, responseBody,
        queryParam1, queryValue1, queryParam2, queryValue2);
    }
  }



  protected static void setEnv(Map<String, String> newenv) throws Exception {
    try {
      Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
      Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
      theEnvironmentField.setAccessible(true);
      Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
      env.putAll(newenv);
      Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
      theCaseInsensitiveEnvironmentField.setAccessible(true);
      Map<String, String> cienv = (Map<String, String>)     theCaseInsensitiveEnvironmentField.get(null);
      cienv.putAll(newenv);
    } catch (NoSuchFieldException e) {
      Class[] classes = Collections.class.getDeclaredClasses();
      Map<String, String> env = System.getenv();
      for(Class cl : classes) {
        if("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
          Field field = cl.getDeclaredField("m");
          field.setAccessible(true);
          Object obj = field.get(env);
          Map<String, String> map = (Map<String, String>) obj;
          map.clear();
          map.putAll(newenv);
        }
      }
    }
  }

}
