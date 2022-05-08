package io.vertx.starter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.handler.BasicAuthHandler;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.multipart.MultipartForm;
import io.vertx.ext.web.openapi.RouterBuilder;
import io.vertx.ext.web.validation.RequestParameters;
import sun.security.tools.keytool.CertAndKeyGen;
import sun.security.x509.X500Name;

import java.io.*;
import java.net.InetAddress;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

public class ApiVerticle extends AbstractVerticle {

  private HttpServer server;
  public static final String BASIC_USERNAME = "username";
  public static final String BASIC_PASSWORD = "password";

  public static String USER = "yuuvis";
  public static String PASSWORD = "optimalsystem";
  public static String YUUVISUSER = "root";
  public static String YUUVISPASSWORD = "optimalsystem";
  public static final String UPLOADDIR = "uploads";
  public static Boolean SSLENABLED = false;
  public static Boolean SELFSIGNEDCERTIFICATE;
  public static String KEYSTORECN = "yuuviskeystore.jks";
  public static String KEYSTOREPATH = "yuuviskeystoremacpro.jks";
  public static String KEYSTOREPASSWORD = "optimalsystem";

  public static String yuuvisuri = null;
  public static int yuuvisport = 0;
  public static String TENANT = null;
  public static String SERVERURL = null;
  public static String TABLENAME = null;


  @Override
  public void start(Promise<Void> startPromise) {

//    System.out.println(System.getenv());
    yuuvisuri = System.getenv("AUTHENTICATION_SERVICE_HOST");
    if (System.getenv("AUTHENTICATION_SERVICE_PORT") != null) {
      yuuvisport = Integer.valueOf(System.getenv("AUTHENTICATION_SERVICE_PORT"));
//      yuuvisport = 30080;
    }
    if (System.getenv("TENANT") != null) {
      TENANT = System.getenv("TENANT");
    } else {
      TENANT = "yuuvistest";
    }
    String cap = TENANT.substring(0, 1).toUpperCase() + TENANT.substring(1);
    TABLENAME = "ten" + cap;
    if (System.getenv("SERVERURL") != null) {
      SERVERURL = System.getenv("SERVERURL");
    } else {
      SERVERURL = "localhost:8080";
    }
    if (System.getenv("USER") != null) {
      USER = System.getenv("USER");
    } else {
      USER = "yuuvis";
    }
    if (System.getenv("PASSWORD") != null) {
      PASSWORD = System.getenv("PASSWORD");
    } else {
      PASSWORD = "optimalsystem";
    }
    if (System.getenv("YUUVISUSER") != null) {
      YUUVISUSER = System.getenv("YUUVISUSER");
    } else {
      YUUVISUSER = "root";
    }
    if (System.getenv("YUUVISPASSWORD") != null) {
      YUUVISPASSWORD = System.getenv("YUUVISPASSWORD");
    } else {
      YUUVISPASSWORD = "optimalsystem";
    }
    if (System.getenv("SSLENABLED") != null) {
      SSLENABLED = Boolean.valueOf(System.getenv("SSLENABLED"));
    } else {
      SSLENABLED = false;
    }
    if (System.getenv("SELFSIGNEDCERTIFICATE") != null) {
      SELFSIGNEDCERTIFICATE = Boolean.valueOf(System.getenv("SELFSIGNEDCERTIFICATE"));
    } else {
      SELFSIGNEDCERTIFICATE = false;
    }
    if (System.getenv("KEYSTORECN") != null) {
      KEYSTORECN = System.getenv("KEYSTORECN");
    } else {
      KEYSTORECN = "10.211.55.4";
    }
    if (System.getenv("KEYSTOREPATH") != null) {
      KEYSTOREPATH = System.getenv("KEYSTOREPATH");
    } else {
      KEYSTOREPATH = "yuuviskeystoremacpro.jks";
    }
    if (System.getenv("KEYSTOREPASSWORD") != null) {
      KEYSTOREPASSWORD = System.getenv("KEYSTOREPASSWORD");
    } else {
      KEYSTOREPASSWORD = "optimalsystem";
    }
    System.out.println("yuuvisuri: " + yuuvisuri);
    System.out.println("yuuvisport: " + yuuvisport);
    System.out.println("TENANT: " + TENANT);
    System.out.println("SERVERURL: " + SERVERURL);
    System.out.println("TABLENAME: " + TABLENAME);
    System.out.println("USER: " + USER);
    System.out.println("PASSWORD: " + PASSWORD.substring(0, 2) + "...");
    System.out.println("YUUVISUSER: " + YUUVISUSER);
    System.out.println("YUUVISPASSWORD: " + YUUVISPASSWORD.substring(0, 2) + "...");
    System.out.println("SSLENABLED: " + SSLENABLED);
    System.out.println("SELFSIGNEDCERTIFICATE: " + SELFSIGNEDCERTIFICATE);
    System.out.println("KEYSTORECN: " + KEYSTORECN);
    System.out.println("KEYSTOREPATH: " + KEYSTOREPATH);
    System.out.println("KEYSTOREPASSWORD: " + KEYSTOREPASSWORD.substring(0, 2) + "...");
    WebClientOptions options = new WebClientOptions()
      .setUserAgent("otto");
    options.setKeepAlive(false);
    System.out.println("start the webclient");
    WebClient client = WebClient.create(this.vertx, options);

    RouterBuilder.create(this.vertx, "dist/openapi3.yaml")
      .onSuccess(routerBuilder -> {
        // Add routes handlers
        System.out.println("routerbuilder succeed, build the routs");

        // tag::Home_Health[]
        routerBuilder.operation("Home_Health")
          .handler(routingContext -> {
            System.out.println("entered handler by id Home_Health");
            routingContext
              .response() // <1>
              .setStatusCode(200)
              .putHeader(HttpHeaders.CONTENT_TYPE, "application/octet-stream")
              .send("Service health is OK");
          });
        // end::Home_Health[]


        // tag::Dokument_Get[]
        routerBuilder.operation("Dokument_Get")
          .handler(BodyHandler.create().setUploadsDirectory(UPLOADDIR).setBodyLimit(-1L))
          .handler(routingContext -> {
          List<String> eDokumentenID = routingContext.queryParam("eDokumentenID");
          if (eDokumentenID.isEmpty()) {
            routingContext
              .response() // <1>
              .setStatusCode(204)
              .putHeader(HttpHeaders.CONTENT_TYPE, "text") // <2>
              .end("eDokumentenID is Empty"); // <3>
          }
          System.out.println("entered handler by id Dokument_Get eDokumentenID: " + eDokumentenID.get(0));
          JsonObject yuuvisQuery = new JsonObject();
          yuuvisQuery.put("statement", "SELECT * FROM " + TABLENAME  +  ":" + "fallakteneodokument WHERE " + TABLENAME  +  ":" + "edokumentenid = '" + eDokumentenID.get(0) + "'");
          yuuvisQuery.put("skipCount",0);
          yuuvisQuery.put("maxItems",50);
          JsonObject yuuvisQueryObject = new JsonObject();
          yuuvisQueryObject.put("query", yuuvisQuery);

          client
            .post(yuuvisport, yuuvisuri, "/api/dms/objects/search")
            .timeout(20000)
            .putHeader("X-ID-TENANT-NAME", TENANT)
            .basicAuthentication(YUUVISUSER, YUUVISPASSWORD)
            .sendJsonObject(yuuvisQueryObject)
            .onSuccess(sr -> {
              HttpResponse<Buffer> responseQuery = sr;
              System.out.println("Received yuuvis Query response with statuscode: " + responseQuery.statusCode() + " " + responseQuery.statusMessage());
              System.out.println("Received yuuvis Query response with body: " + responseQuery.bodyAsString());
              JsonObject yuuvisSearch = new JsonObject(responseQuery.bodyAsString());
              int results = yuuvisSearch.getInteger("numItems");
              if (responseQuery.statusCode() == 200 && results >= 1) {
                JsonArray yuuvisSearchObjects = yuuvisSearch.getJsonArray("objects");
                JsonObject yuuvisSearchObject = yuuvisSearchObjects.getJsonObject(0);
                JsonObject searchProperties = yuuvisSearchObject.getJsonObject("properties");
                String objectId = searchProperties.getJsonObject("system:objectId").getString("value");
                System.out.println("objectId: " + objectId);

                client
                  .get(yuuvisport, yuuvisuri, "/api/dms/objects/" + objectId + "/contents/file")
                  .timeout(20000)
                  .putHeader("X-ID-TENANT-NAME", TENANT)
                  .basicAuthentication(YUUVISUSER, YUUVISPASSWORD)
                  .send()
                  .onSuccess(ar -> {
                    HttpResponse<Buffer> responseQueryDokument = ar;
                    System.out.println("Received yuuvis Query response with status code: " + responseQueryDokument.statusCode() + " " + responseQueryDokument.statusMessage());
                    System.out.println("Received yuuvis Query response with body: " + responseQueryDokument.body());
                    routingContext
                      .response() // <1>
                      .setStatusCode(200)
                      .putHeader(HttpHeaders.CONTENT_TYPE, "application/octet-stream")
                      .send(responseQueryDokument.body());
                  })
                  .onFailure(err -> {
                    System.out.println("Something went wrong " + err.getMessage());
                    routingContext
                      .end(err.getMessage());

                  });
              } else {
//                check if klientakte
                JsonObject yuuvisQuery2 = new JsonObject();
                yuuvisQuery2.put("statement", "SELECT * FROM " + TABLENAME  +  ":" + "klientakteneodokument WHERE " + TABLENAME  +  ":" + "edokumentenid = '" + eDokumentenID.get(0) + "'");
                yuuvisQuery2.put("skipCount", 0);
                yuuvisQuery2.put("maxItems", 50);
                JsonObject yuuvisQueryObject2 = new JsonObject();
                yuuvisQueryObject2.put("query", yuuvisQuery2);
                client
                  .post(yuuvisport, yuuvisuri, "/api/dms/objects/search")
                  .timeout(20000)
                  .putHeader("X-ID-TENANT-NAME", TENANT)
                  .basicAuthentication(YUUVISUSER, YUUVISPASSWORD)
                  .sendJsonObject(yuuvisQueryObject2)
                  .onSuccess(sr2 -> {
                    HttpResponse<Buffer> responseQuery2 = sr2;
                    System.out.println("Received yuuvis Query response with status code: " + responseQuery2.statusCode() + " " + responseQuery.statusMessage());
                    System.out.println("Received yuuvis Query response with body: " + responseQuery2.bodyAsString());
                    JsonObject yuuvisSearch2 = new JsonObject(responseQuery2.bodyAsString());
                    int results2 = yuuvisSearch2.getInteger("numItems");
                    if (responseQuery2.statusCode() == 200 && results2 >= 1) {
                      JsonArray yuuvisSearchObjects2 = yuuvisSearch2.getJsonArray("objects");
                      JsonObject yuuvisSearchObject2 = yuuvisSearchObjects2.getJsonObject(0);
                      JsonObject searchProperties2 = yuuvisSearchObject2.getJsonObject("properties");
                      String objectId2 = searchProperties2.getJsonObject("system:objectId").getString("value");
                      System.out.println("objectId: " + objectId2);

                      client
                        .get(yuuvisport, yuuvisuri, "/api/dms/objects/" + objectId2 + "/contents/file")
                        .timeout(20000)
                        .putHeader("X-ID-TENANT-NAME", TENANT)
                        .basicAuthentication(YUUVISUSER, YUUVISPASSWORD)
                        .send()
                        .onSuccess(ar2 -> {
                          HttpResponse<Buffer> responseQueryDokument2 = ar2;
                          System.out.println("Received yuuvis Query response with status code: " + responseQueryDokument2.statusCode() + " " + responseQueryDokument2.statusMessage());
                          System.out.println("Received yuuvis Query response with body: " + responseQueryDokument2.body());
                          routingContext
                            .response() // <1>
                            .setStatusCode(200)
                            .putHeader(HttpHeaders.CONTENT_TYPE, "application/octet-stream")
                            .send(responseQueryDokument2.body());
                        })
                        .onFailure(err -> {
                          System.out.println("Something went wrong " + err.getMessage());
                          routingContext
                            .end(err.getMessage());

                        });
                    } else {
                      routingContext
                        .response() // <1>
                        .setStatusCode(204)
                        .putHeader(HttpHeaders.CONTENT_TYPE, "text") // <2>
                        .end("Dokument mit der eDokumentenID existiert nicht, oder es wurde noch keine Dokumentdatei angeheftet."); // <3>
                    }
                  })
                  .onFailure(err -> {
                    System.out.println("Something went wrong " + err.getMessage());
                    routingContext
                      .end(err.getMessage());
                  });
              }
            })
            .onFailure(err -> {
              System.out.println("Something went wrong " + err.getMessage());
              routingContext
                .end(err.getMessage());

            });

        });
        // end::Dokument_Get[]

        // tag::Dokument_Post[]
        routerBuilder.operation("Dokument_Post")
          .handler(BodyHandler.create().setUploadsDirectory(UPLOADDIR).setBodyLimit(-1L))
          .handler(routingContext -> {
          System.out.println("entered handler by id Dokument_Post");

          String nameValue="";
          String fileNameValue ="";
          String filePathValue ="";
          String fileTypeValue="";

//          routingContext.response().setChunked(true);
//          JsonObject requestBodyValue = null;
          for (FileUpload f : routingContext.fileUploads()) {
            System.out.println("Filename: " + f.fileName());
            System.out.println("Size: " + f.size());
            if (f.name().equals("File")) {
              nameValue = f.name();
              fileNameValue = f.fileName();
              filePathValue = f.uploadedFileName();
              fileTypeValue = f.contentType();
            } else {
              System.out.println("Unkonwn Upload Element: " + f.name());
              routingContext
                .response()
                .setStatusCode(204)
                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .end("Unkonwn Upload Element: " + f.name()); // <4>
            }
          }
          if (routingContext.fileUploads().size() != 1) {
            System.out.println("To much/less elements in upload, should be 2 but is: " + routingContext.fileUploads().size());
            routingContext
              .response()
              .setStatusCode(204)
              .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
              .end("To much/less elements in upload, should be 2 but is: " + routingContext.fileUploads().size()); // <4>
          }
          final String name=nameValue;
          final String fileName = fileNameValue;
          final String filePath = filePathValue;
          final String fileType=fileTypeValue;

          RequestParameters params = routingContext.get("parsedParameters"); // (1)
          JsonObject data = params.body().getJsonObject();
          JsonObject requestBody = new JsonObject(data.getString("data"));
          System.out.println("requestBody: " + requestBody);

          //check if document for fallakte or klientakte
          final boolean fallakte;
          if (!requestBody.getString("Klientakte.Klient.ID").isEmpty()) {
            fallakte = false;
          } else {
            fallakte = true;
          }
          JsonObject yuuvisQuery = new JsonObject();
          if (fallakte) {
            if (requestBody.getString("Fallakte.Vorgang.Register").isEmpty()) {
              yuuvisQuery.put("statement", "SELECT * FROM " + TABLENAME  +  ":" + "fallakteneo WHERE " + TABLENAME  +  ":" + "vorgangsid = '" + requestBody.getString("Fallakte.Vorgang.ID") + "'");
            } else {
              yuuvisQuery.put("statement", "SELECT * FROM " + TABLENAME  +  ":" + "fallakteneo WHERE " + TABLENAME  +  ":" + "vorgangsid = '" + requestBody.getString("Fallakte.Vorgang.ID") + "'" +
                " AND " + TABLENAME  +  ":" + "register = '" + requestBody.getString("Fallakte.Vorgang.Register") + "'");
            }
          } else {
            yuuvisQuery.put("statement", "SELECT * FROM " + TABLENAME  +  ":" + "klientakteneo WHERE " + TABLENAME  +  ":" + "id = '" + requestBody.getString("Klientakte.Klient.ID") + "'");
          }
          yuuvisQuery.put("skipCount",0);
          yuuvisQuery.put("maxItems",50);
          JsonObject yuuvisQueryObject = new JsonObject();
          yuuvisQueryObject.put("query", yuuvisQuery);

          client
            .post(yuuvisport, yuuvisuri, "/api/dms/objects/search")
            .timeout(20000)
            .putHeader("X-ID-TENANT-NAME", TENANT)
            .basicAuthentication(YUUVISUSER, YUUVISPASSWORD)
            .sendJsonObject(yuuvisQueryObject)
            .onSuccess(ar -> {
              HttpResponse<Buffer> responseQuery = ar;
              System.out.println("Received yuuvis Query response with status code: " + responseQuery.statusCode() +" "+ responseQuery.statusMessage());
              System.out.println("Received yuuvis Query response with body: " + responseQuery.bodyAsString());
              if (responseQuery.statusCode() == 200) {
                JsonObject yuuvisSearch = new JsonObject(responseQuery.bodyAsString());
                int results = yuuvisSearch.getInteger("numItems");
                int yuuvisSearchIndex = -1;
                if (results == 1) {
                  yuuvisSearchIndex = 0;
                } else if (results >= 1 && requestBody.getString("Fallakte.Vorgang.Register").isEmpty()) {
                  JsonArray yuuvisSearchObjects = yuuvisSearch.getJsonArray("objects");
                  for (int i = 0; i < results; i++) {
                    JsonObject yuuvisSearchObject = yuuvisSearchObjects.getJsonObject(i);
                    JsonObject searchProperties = yuuvisSearchObject.getJsonObject("properties");
                    if (searchProperties.getString("" + TABLENAME  +  ":" + "register") == null) {
                      yuuvisSearchIndex = i;
                    }
                  }
                }

                if (yuuvisSearchIndex >= 0) {
                  JsonObject yuuvisDocuments;
                  if (fallakte) {
                    yuuvisDocuments = getFallakteDokument(name, fileName, fileType, requestBody, yuuvisSearch, yuuvisSearchIndex);
                  } else {
                    yuuvisDocuments = getKlientakteDokument(name, fileName, fileType, requestBody, yuuvisSearch, yuuvisSearchIndex);
                  }


                  final String nameJson = "data";
                  final String fileNameJson = "document";
                  UUID uuid = UUID.randomUUID();
                  String uuidAsString = uuid.toString();

                  final String filePathJson = "file-" + UPLOADDIR + "/" + uuid.toString();
                  final String fileTypeJson = "application/json";

                  try (FileWriter file = new FileWriter(filePathJson)) {
                    file.write(yuuvisDocuments.toString());
                    file.flush();
                  } catch (IOException e) {
                    e.printStackTrace();
                  }

                  MultipartForm form = MultipartForm.create()
//                  .attribute("data", yuuvisDocuments.encode())
                    .textFileUpload(nameJson, fileNameJson, filePathJson, fileTypeJson)
                    .binaryFileUpload(name, fileName, filePath, fileType);
                  client
                    .post(yuuvisport, yuuvisuri, "/api/dms/objects")
                    .timeout(20000)
                    .putHeader("X-ID-TENANT-NAME", TENANT)
                    .putHeader("content-type", "multipart/form-data")
                    .basicAuthentication(YUUVISUSER, YUUVISPASSWORD)
                    .sendMultipartForm(form)
//                  .sendJsonObject(yuuvisDocuments)
                    .onSuccess(arDocument -> {
                      HttpResponse<Buffer> responseDocument = arDocument;
                      System.out.println("Received yuuvis response with status code: " + responseDocument.statusCode() + " " + responseDocument.statusMessage());
                      System.out.println("Received yuuvis response with body: " + responseDocument.bodyAsString());
                      //delete the uploaded file
                      for (FileUpload f : routingContext.fileUploads()) {
                        File myObj = new File(f.uploadedFileName());
                        if (myObj.delete()) {
                          System.out.println("Deleted the file: " + myObj.getName());
                        } else {
                          System.out.println("Failed to delete the file.");
                        }
                      }
                      File myObj = new File(filePathJson);
                      if (myObj.delete()) {
                        System.out.println("Deleted the file: " + myObj.getName());
                      } else {
                        System.out.println("Failed to delete the file.");
                      }
                      JsonObject answer = getDocumentPostAnswer(requestBody, responseDocument, fallakte);
                      if (answer.getString("eDokumentenID") != null &&
                        answer.getString("eDokumentenID").length() > 0) {
                        System.out.println("responseBody: " + requestBody);
                      routingContext
                        .response()
                        .setStatusCode(200)
                        .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .end(answer.encode()); // <4>
                      } else {
                        System.out.println("did not receive an objectId from yuuvi");
                        System.out.println("responseBody: " + requestBody);
                        routingContext
                          .response()
                          .setStatusCode(500)
                          .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                          .end(answer.encode());
                      }
                    })
                    .onFailure(err -> {
                      System.out.println("Something went wrong " + err.getMessage());
                      routingContext
                        .end(err.getMessage()); // <4>
                    });

                } else {
                  routingContext
                    .response()
                    .setStatusCode(204)
                    .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                    .end("Fallakte mit der Fallakte.Vorgang.ID existiert nicht"); // <4>
                }
              } else {
                routingContext
                  .response()
                  .setStatusCode(204)
                  .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                  .end("Yuuvisabfrage fehlgeschlagen: status code: " + responseQuery.statusCode() +" "+ responseQuery.statusMessage() ); // <4>
              }
            })
            .onFailure(err -> {
              System.out.println("Something went wrong during the query: " + err.getMessage());
              routingContext
                .end(err.getMessage()); // <4>
            });

//          RequestParameters params = routingContext.get(ValidationHandler.REQUEST_CONTEXT_KEY); // <1>
        });
        // end::Dokument_Post[]

        // tag::Dokument_GetDokumente[]
        routerBuilder.operation("Dokument_GetDokumente")
          .handler(routingContext -> {
            List<String> klientID = routingContext.queryParam("klientID");
            List<String> vorgangID = routingContext.queryParam("vorgangID");
            List<String> vorgangRegister = routingContext.queryParam("vorgangRegister");
            final boolean fallakte;
            if (klientID.isEmpty() && vorgangID.isEmpty()) {
              routingContext
                .response()
                .setStatusCode(200)
                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .end("missing parameter, but ok");
            }
            if (!klientID.isEmpty()) {
              fallakte = false;
              System.out.println("entered handler by id Dokument_GetDokumente" +
              " klientID: " +klientID.get(0));
            } else {
              fallakte = true;
              if (vorgangRegister.isEmpty()) {
                System.out.println("entered handler by id Dokument_GetDokumente" +
                  " vorgangID: " + vorgangID.get(0));
              } else {
                System.out.println("entered handler by id Dokument_GetDokumente" +
                  " vorgangID: " + vorgangID.get(0) + " vorgangRegister: " + vorgangRegister.get(0));
              }
            }

            JsonObject yuuvisQuery = new JsonObject();
            if (fallakte) {
              if (vorgangRegister.isEmpty()) {
                yuuvisQuery.put("statement", "SELECT * FROM " + TABLENAME  +  ":" + "fallakteneo WHERE " + TABLENAME  +  ":" + "vorgangsid = '" + vorgangID.get(0) + "'");
              } else {
                yuuvisQuery.put("statement", "SELECT * FROM " + TABLENAME  +  ":" + "fallakteneo WHERE " + TABLENAME  +  ":" + "vorgangsid = '" + vorgangID.get(0) + "'" +
                  " AND " + TABLENAME  +  ":" + "register = '" + vorgangRegister.get(0) + "'");
              }
            } else {
              yuuvisQuery.put("statement", "SELECT * FROM " + TABLENAME  +  ":" + "klientakteneo WHERE " + TABLENAME  +  ":" + "id = '" + klientID.get(0) + "'");
            }
            yuuvisQuery.put("skipCount",0);
            yuuvisQuery.put("maxItems",50);
            JsonObject yuuvisQueryObject = new JsonObject();
            yuuvisQueryObject.put("query", yuuvisQuery);

            client
              .post(yuuvisport, yuuvisuri, "/api/dms/objects/search")
              .timeout(20000)
              .putHeader("X-ID-TENANT-NAME", TENANT)
              .basicAuthentication(YUUVISUSER, YUUVISPASSWORD)
              .sendJsonObject(yuuvisQueryObject)
              .onSuccess(sr -> {
                HttpResponse<Buffer> responseQuery = sr;
                System.out.println("Received yuuvis Query response with status code: " + responseQuery.statusCode() + " " + responseQuery.statusMessage());
                System.out.println("Received yuuvis Query response with body: " + responseQuery.bodyAsString());
                JsonObject yuuvisSearch = new JsonObject(responseQuery.bodyAsString());
                int results = yuuvisSearch.getInteger("numItems");
                if (responseQuery.statusCode() == 200 && results >= 1) {
                  JsonArray yuuvisSearchObjects = yuuvisSearch.getJsonArray("objects");
                  JsonObject yuuvisSearchObject = yuuvisSearchObjects.getJsonObject(0);
                  JsonObject searchProperties = yuuvisSearchObject.getJsonObject("properties");
                  String objectId = searchProperties.getJsonObject("system:objectId").getString("value");

                  JsonObject yuuvisQueryDocs = new JsonObject();
                  if (fallakte) {
                    yuuvisQueryDocs.put("statement", "SELECT * FROM " + TABLENAME + ":" + "fallakteneodokument WHERE system:parentId = '" + objectId + "'");
                  } else {
                    yuuvisQueryDocs.put("statement", "SELECT * FROM " + TABLENAME + ":" + "klientakteneodokument WHERE system:parentId = '" + objectId + "'");
                  }
                  yuuvisQueryDocs.put("skipCount",0);
                  yuuvisQueryDocs.put("maxItems",50);
                  JsonObject yuuvisQueryDocsObject = new JsonObject();
                  yuuvisQueryDocsObject.put("query", yuuvisQueryDocs);
                  client
                    .post(yuuvisport, yuuvisuri, "/api/dms/objects/search")
                    .timeout(20000)
                    .putHeader("X-ID-TENANT-NAME", TENANT)
                    .basicAuthentication(YUUVISUSER, YUUVISPASSWORD)
                    .sendJsonObject(yuuvisQueryDocsObject)
                    .onSuccess(srd -> {
                      HttpResponse<Buffer> responseQueryDocs = srd;
                      System.out.println("Received yuuvis Query response with status code: " + responseQueryDocs.statusCode() + " " + responseQueryDocs.statusMessage());
                      System.out.println("Received yuuvis Query response with body: " + responseQueryDocs.bodyAsString());
                      JsonObject yuuvisSearchDocs = new JsonObject(responseQueryDocs.bodyAsString());
                      JsonArray searchResult = getDokument(yuuvisSearchDocs,fallakte,searchProperties);
                      routingContext
                        .response()
                        .setStatusCode(200)
                        .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .end(searchResult.encode());
                    })
                    .onFailure(err -> {
                      System.out.println("Something went wrong " + err.getMessage());
                      routingContext
                        .end(err.getMessage());

                    });
                } else {
                  routingContext
                    .response() // <1>
                    .setStatusCode(204)
                    .putHeader(HttpHeaders.CONTENT_TYPE, "text") // <2>
                    .end("Die KlientID/VorgangsID oder das Register/der Unterordner existiert im enaio® nicht."); // <3>
                }
              })
              .onFailure(err -> {
                System.out.println("Something went wrong " + err.getMessage());
                routingContext
                  .end(err.getMessage());

              });
          });
        // end::Dokument_GetDokumente[]

        // tag::Fallakte_Post[]
        routerBuilder.operation("Fallakte_Post")
          .handler(routingContext -> {
          System.out.println("entered handler by id Fallakte_Post");
          JsonObject requestBody = routingContext.getBodyAsJson();
          System.out.println("requestBody: " + requestBody);
          JsonObject yuuvisDocuments = new JsonObject();
          JsonArray yuuvisObjects = new JsonArray();
          JsonObject yuuvisObject = new JsonObject();
          JsonObject properties = new JsonObject();

          getFallAkteProperties(requestBody, properties);

          yuuvisObject.put("properties",properties);
          yuuvisObjects.add(yuuvisObject);
          yuuvisDocuments.put("objects",yuuvisObjects);

          JsonObject yuuvisQuery = new JsonObject();
          if (requestBody.getJsonObject("vorgang").getString("register").isEmpty()) {
            yuuvisQuery.put("statement", "SELECT * FROM " + TABLENAME  +  ":" + "fallakteneo WHERE " + TABLENAME  +  ":" + "vorgangsid = '" + requestBody.getJsonObject("vorgang").getString("id") + "'");
          } else {
            yuuvisQuery.put("statement", "SELECT * FROM " + TABLENAME  +  ":" + "fallakteneo WHERE " + TABLENAME  +  ":" + "vorgangsid = '" + requestBody.getJsonObject("vorgang").getString("id") + "'" +
              " AND " + TABLENAME  +  ":" + "register = '" + requestBody.getJsonObject("vorgang").getString("register") + "'");
          }
          yuuvisQuery.put("skipCount",0);
          yuuvisQuery.put("maxItems",50);
          JsonObject yuuvisQueryObject = new JsonObject();
          yuuvisQueryObject.put("query", yuuvisQuery);

          client
            .post(yuuvisport, yuuvisuri, "/api/dms/objects/search")
            .timeout(20000)
            .putHeader("X-ID-TENANT-NAME", TENANT)
            .basicAuthentication(YUUVISUSER, YUUVISPASSWORD)
            .sendJsonObject(yuuvisQueryObject)
            .onSuccess(sr -> {
              HttpResponse<Buffer> responseQuery = sr;
              System.out.println("Received yuuvis Query response with status code: " + responseQuery.statusCode() + " " + responseQuery.statusMessage());
              System.out.println("Received yuuvis Query response with body: " + responseQuery.bodyAsString());
              JsonObject yuuvisSearch = new JsonObject(responseQuery.bodyAsString());
              int results = yuuvisSearch.getInteger("numItems");
              if (responseQuery.statusCode() == 200 && results == 0) {
                client
                  .post(yuuvisport, yuuvisuri, "/api/dms/objects")
                  .timeout(10000)
                  .putHeader("X-ID-TENANT-NAME", TENANT)
                  .basicAuthentication(YUUVISUSER, YUUVISPASSWORD)
                  .sendJsonObject(yuuvisDocuments)
                  .onSuccess(ar -> {
                    HttpResponse<Buffer> response = ar;
                    System.out.println("Received yuuvis response with status code: " + response.statusCode() + " " + response.statusMessage());
                    System.out.println("Received yuuvis response with body: " + response.bodyAsString());
                    if (setEaktenID(requestBody, response)) {
                      System.out.println("responseBody: " + requestBody);
                      routingContext
                        .response()
                        .setStatusCode(200)
                        .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .end(requestBody.encode());
                    } else {
                      System.out.println("did not receive an objectId from yuuvi");
                      System.out.println("responseBody: " + requestBody);
                      routingContext
                        .response()
                        .setStatusCode(500)
                        .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .end(requestBody.encode());
                    }
                  })
                  .onFailure(err -> {
                    System.out.println("Something went wrong " + err.getMessage());
                    routingContext
                      .end(err.getMessage()); // <4>
                  });
              } else {
                String answer ="";
                if (requestBody.getJsonObject("vorgang").getString("register").isEmpty()) {
                  answer = "vorgangsid = '" + requestBody.getJsonObject("vorgang").getString("id") + "'";
                } else {
                  answer = "vorgangsid = '" + requestBody.getJsonObject("vorgang").getString("id") + "'" +
                    " AND register = '" + requestBody.getJsonObject("vorgang").getString("register") + "'";
                }

                routingContext
                  .response() // <1>
                  .setStatusCode(409)
                  .putHeader(HttpHeaders.CONTENT_TYPE, "text") // <2>
                  .end("fallakte allready exists: " + answer); // <3>
              }
            })
            .onFailure(err -> {
              System.out.println("Something went wrong " + err.getMessage());
              routingContext
                .end(err.getMessage()); // <4>
            });
        });
        // end::Fallakte_Post[]

        // tag::Fallakte_Put[]
        routerBuilder.operation("Fallakte_Put")
          .handler(routingContext -> {
            System.out.println("entered handler by id Fallakte_Put");
            JsonObject requestBody = routingContext.getBodyAsJson();
            System.out.println("requestBody: " + requestBody);
            JsonObject yuuvisDocuments = new JsonObject();
            JsonArray yuuvisObjects = new JsonArray();
            JsonObject yuuvisObject = new JsonObject();
            JsonObject properties = new JsonObject();

            JsonObject systemObject = new JsonObject();
            systemObject.put("value","" + TABLENAME  +  ":" + "fallakteneo");
            properties.put("system:objectTypeId",systemObject);

            getFallAkteProperties(requestBody, properties);


            yuuvisObject.put("properties",properties);
            yuuvisObjects.add(yuuvisObject);
            yuuvisDocuments.put("objects",yuuvisObjects);

            JsonObject yuuvisQuery = new JsonObject();
            if (requestBody.getJsonObject("vorgang").getString("register").isEmpty()) {
              yuuvisQuery.put("statement", "SELECT * FROM " + TABLENAME  +  ":" + "fallakteneo WHERE " + TABLENAME  +  ":" + "vorgangsid = '" + requestBody.getJsonObject("vorgang").getString("id") + "'");
            } else {
              yuuvisQuery.put("statement", "SELECT * FROM " + TABLENAME  +  ":" + "fallakteneo WHERE " + TABLENAME  +  ":" + "vorgangsid = '" + requestBody.getJsonObject("vorgang").getString("id") + "'" +
                " AND " + TABLENAME  +  ":" + "register = '" + requestBody.getJsonObject("vorgang").getString("register") + "'");
            }
            yuuvisQuery.put("skipCount",0);
            yuuvisQuery.put("maxItems",50);
            JsonObject yuuvisQueryObject = new JsonObject();
            yuuvisQueryObject.put("query", yuuvisQuery);

            client
              .post(yuuvisport, yuuvisuri, "/api/dms/objects/search")
              .timeout(20000)
              .putHeader("X-ID-TENANT-NAME", TENANT)
              .basicAuthentication(YUUVISUSER, YUUVISPASSWORD)
              .sendJsonObject(yuuvisQueryObject)
              .onSuccess(sr -> {
                HttpResponse<Buffer> responseQuery = sr;
                System.out.println("Received yuuvis Query response with status code: " + responseQuery.statusCode() + " " + responseQuery.statusMessage());
                System.out.println("Received yuuvis Query response with body: " + responseQuery.bodyAsString());
                JsonObject yuuvisSearch = new JsonObject(responseQuery.bodyAsString());
                int results = yuuvisSearch.getInteger("numItems");
                if (responseQuery.statusCode() == 200 && results >= 1) {
                  JsonArray yuuvisSearchObjects = yuuvisSearch.getJsonArray("objects");
                  JsonObject yuuvisSearchObject = yuuvisSearchObjects.getJsonObject(0);
                  JsonObject searchProperties = yuuvisSearchObject.getJsonObject("properties");
                  String objectId = searchProperties.getJsonObject("system:objectId").getString("value");
                  client
                    .patch(yuuvisport, yuuvisuri, "/api/dms/objects/" + objectId)
                    .timeout(10000)
                    .putHeader("X-ID-TENANT-NAME", TENANT)
                    .basicAuthentication(YUUVISUSER, YUUVISPASSWORD)
                    .sendJsonObject(yuuvisDocuments)
                    .onSuccess(ar -> {
                      HttpResponse<Buffer> response = ar;
                      System.out.println("Received yuuvis response with status code: " + response.statusCode() + " " + response.statusMessage());
                      System.out.println("Received yuuvis response with body: " + response.bodyAsString());
                      if (setEaktenID(requestBody, response)) {
                        System.out.println("responseBody: " + requestBody);
                        routingContext
                          .response()
                          .setStatusCode(200)
                          .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                          .end(requestBody.encode());
                      } else {
                        System.out.println("did not receive an objectId from yuuvi");
                        System.out.println("responseBody: " + requestBody);
                        routingContext
                          .response()
                          .setStatusCode(500)
                          .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                          .end(requestBody.encode());
                      }
                    })
                    .onFailure(err -> {
                      System.out.println("Something went wrong " + err.getMessage());
                      routingContext
                        .end(err.getMessage()); // <4>
                    });
                } else {
                  String answer ="";
                  if (requestBody.getJsonObject("vorgang").getString("register").isEmpty()) {
                    answer = "vorgangsid = '" + requestBody.getJsonObject("vorgang").getString("id") + "'";
                  } else {
                    answer = "vorgangsid = '" + requestBody.getJsonObject("vorgang").getString("id") + "'" +
                      " AND register = '" + requestBody.getJsonObject("vorgang").getString("register") + "'";
                  }
                  routingContext
                    .response() // <1>
                    .setStatusCode(204)
                    .putHeader(HttpHeaders.CONTENT_TYPE, "text") // <2>
                    .end("fallakte not found: " + answer); // <3>
                }
              })
              .onFailure(err -> {
                System.out.println("Something went wrong " + err.getMessage());
                routingContext
                  .end(err.getMessage()); // <4>
              });
          });
        // end::Fallakte_Put[]

        // tag::Fallakte_Get[]
        routerBuilder.operation("Fallakte_Get")
          .handler(routingContext -> {
          String param = routingContext.pathParam("vorgangsId");
          System.out.println("entered handler by id Fallakte_Get vorgangsId: " + param);
          JsonObject yuuvisQuery = new JsonObject();
          yuuvisQuery.put("statement", "SELECT * FROM " + TABLENAME  +  ":" + "fallakteneo WHERE " + TABLENAME  +  ":" + "vorgangsid = '" + param + "'");
          yuuvisQuery.put("skipCount",0);
          yuuvisQuery.put("maxItems",50);
          JsonObject yuuvisQueryObject = new JsonObject();
          yuuvisQueryObject.put("query", yuuvisQuery);

          client
            .post(yuuvisport, yuuvisuri, "/api/dms/objects/search")
            .timeout(20000)
            .putHeader("X-ID-TENANT-NAME", TENANT)
            .basicAuthentication(YUUVISUSER, YUUVISPASSWORD)
            .sendJsonObject(yuuvisQueryObject)
            .onSuccess(ar -> {
              HttpResponse<Buffer> responseQuery = ar;
              System.out.println("Received yuuvis Query response with status code: " + responseQuery.statusCode() +" "+ responseQuery.statusMessage());
              System.out.println("Received yuuvis Query response with body: " + responseQuery.bodyAsString());
              JsonObject yuuvisSearch = new JsonObject(responseQuery.bodyAsString());
              int results = yuuvisSearch.getInteger("numItems");
              if (responseQuery.statusCode() == 200 && results >= 1) {
                JsonArray yuuvisSearchObjects = yuuvisSearch.getJsonArray("objects");
                JsonObject yuuvisSearchObject = yuuvisSearchObjects.getJsonObject(0);
                JsonObject searchProperties = yuuvisSearchObject.getJsonObject("properties");

                JsonObject fallAkte = new JsonObject();
                fallAkte.put("eAktenID",getObjectId(responseQuery));
                JsonObject vorgang = new JsonObject();
                vorgang.put("aktenzeichen",searchProperties.getJsonObject("" + TABLENAME  +  ":" + "aktenzeichen").getString("value"));
                vorgang.put("archivieren",searchProperties.getJsonObject("" + TABLENAME  +  ":" + "archivieren").getString("value"));
                vorgang.put("archivierenDatum",searchProperties.getJsonObject("" + TABLENAME  +  ":" + "archivdatum").getString("value"));
                vorgang.put("bemerkung",searchProperties.getJsonObject("" + TABLENAME  +  ":" + "Bemerkung").getString("value"));
                vorgang.put("id",searchProperties.getJsonObject("" + TABLENAME  +  ":" + "vorgangsid").getString("value"));
                vorgang.put("loeschen",searchProperties.getJsonObject("" + TABLENAME  +  ":" + "loeschen").getString("value"));
                vorgang.put("loeschenDatum",searchProperties.getJsonObject("" + TABLENAME  +  ":" + "loeschdatum").getString("value"));
                vorgang.put("rechtsgebiet",searchProperties.getJsonObject("" + TABLENAME  +  ":" + "rechtsgebiet").getString("value"));
                if ((searchProperties.getJsonObject("" + TABLENAME + ":" + "register") != null) && !searchProperties.getJsonObject("" + TABLENAME + ":" + "register").isEmpty()) {
                  vorgang.put("register", searchProperties.getJsonObject("" + TABLENAME + ":" + "register").getString("value"));
                }
                vorgang.put("zustaendigerSachbearbeiter",searchProperties.getJsonObject("" + TABLENAME  +  ":" + "sachbearbeiter").getString("value"));
                fallAkte.put("vorgang", vorgang);
                JsonObject personBaseExtended1 = new JsonObject();
                personBaseExtended1.put("vorname",searchProperties.getJsonObject("" + TABLENAME  +  ":" + "vornameAntragsteller").getString("value"));
                personBaseExtended1.put("nachname",searchProperties.getJsonObject("" + TABLENAME  +  ":" + "nachnameAntragsteller").getString("value"));
                personBaseExtended1.put("id",searchProperties.getJsonObject("" + TABLENAME  +  ":" + "idAntragsteller").getString("value"));
                personBaseExtended1.put("geburtsdatum",searchProperties.getJsonObject("" + TABLENAME  +  ":" + "geburtsdatumAntragsteller").getString("value"));
                fallAkte.put("antragssteller", personBaseExtended1);
                JsonObject personBaseExtended2 = new JsonObject();
                personBaseExtended2.put("vorname",searchProperties.getJsonObject("" + TABLENAME  +  ":" + "vorname").getString("value"));
                personBaseExtended2.put("nachname",searchProperties.getJsonObject("" + TABLENAME  +  ":" + "nachname").getString("value"));
                personBaseExtended2.put("id",searchProperties.getJsonObject("" + TABLENAME  +  ":" + "id").getString("value"));
                personBaseExtended2.put("geburtsdatum",searchProperties.getJsonObject("" + TABLENAME  +  ":" + "geburtsdatum").getString("value"));
                fallAkte.put("leistungsempfaenger", personBaseExtended2);
                JsonObject personBaseExtended3 = new JsonObject();
                personBaseExtended3.put("vorname",searchProperties.getJsonObject("" + TABLENAME  +  ":" + "vornameUnterhaltspflichtiger").getString("value"));
                personBaseExtended3.put("nachname",searchProperties.getJsonObject("" + TABLENAME  +  ":" + "nachnameUnterhaltspflichtiger").getString("value"));
                personBaseExtended3.put("id",searchProperties.getJsonObject("" + TABLENAME  +  ":" + "idUnterhaltspflichtiger").getString("value"));
                personBaseExtended3.put("geburtsdatum",searchProperties.getJsonObject("" + TABLENAME  +  ":" + "geburtsdatumUnterhaltspflichtiger").getString("value"));
                fallAkte.put("unterhaltspflichtiger", personBaseExtended3);

                routingContext
                  .response() // <1>
                  .setStatusCode(200)
                  .putHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                  .end(fallAkte.toString()); // <3>
              } else {
                routingContext
                  .response() // <1>
                  .setStatusCode(204)
                  .putHeader(HttpHeaders.CONTENT_TYPE, "application/octet-stream") // <2>
                  .end("search result not unique found: " + results); // <3>
              }
            })
            .onFailure(err -> {
              System.out.println("Something went wrong during the query: " + err.getMessage());
              routingContext
                .end(err.getMessage()); // <4>
            });

        });
        // end::Fallakte_Get[]

        // tag::Klientakte_Post[]
        routerBuilder.operation("Klientakte_Post")
          .handler(routingContext -> {
            System.out.println("entered handler by id Klientakte_Post");
            JsonObject requestBody = routingContext.getBodyAsJson();
            System.out.println("requestBody: " + requestBody);
            JsonObject yuuvisDocuments = new JsonObject();
            JsonArray yuuvisObjects = new JsonArray();
            JsonObject yuuvisObject = new JsonObject();
            JsonObject properties = new JsonObject();

            getKlientAkteProperties(requestBody, properties);

            yuuvisObject.put("properties",properties);
            yuuvisObjects.add(yuuvisObject);
            yuuvisDocuments.put("objects",yuuvisObjects);

            JsonObject yuuvisQuery = new JsonObject();
            yuuvisQuery.put("statement", "SELECT * FROM " + TABLENAME  +  ":" + "klientakteneo WHERE " + TABLENAME  +  ":" + "id = '" + requestBody.getString("id") + "'");
            yuuvisQuery.put("skipCount",0);
            yuuvisQuery.put("maxItems",50);
            JsonObject yuuvisQueryObject = new JsonObject();
            yuuvisQueryObject.put("query", yuuvisQuery);

            client
              .post(yuuvisport, yuuvisuri, "/api/dms/objects/search")
              .timeout(20000)
              .putHeader("X-ID-TENANT-NAME", TENANT)
              .basicAuthentication(YUUVISUSER, YUUVISPASSWORD)
              .sendJsonObject(yuuvisQueryObject)
              .onSuccess(sr -> {
                HttpResponse<Buffer> responseQuery = sr;
                System.out.println("Received yuuvis Query response with status code: " + responseQuery.statusCode() + " " + responseQuery.statusMessage());
                System.out.println("Received yuuvis Query response with body: " + responseQuery.bodyAsString());
                JsonObject yuuvisSearch = new JsonObject(responseQuery.bodyAsString());
                int results = yuuvisSearch.getInteger("numItems");
                if (responseQuery.statusCode() == 200 && results == 0) {
                  client
                    .post(yuuvisport, yuuvisuri, "/api/dms/objects")
                    .timeout(10000)
                    .putHeader("X-ID-TENANT-NAME", TENANT)
                    .basicAuthentication(YUUVISUSER, YUUVISPASSWORD)
                    .sendJsonObject(yuuvisDocuments)
                    .onSuccess(ar -> {
                      HttpResponse<Buffer> response = ar;
                      System.out.println("Received yuuvis response with status code: " + response.statusCode() + " " + response.statusMessage());
                      System.out.println("Received yuuvis response with body: " + response.bodyAsString());

                      if (setEaktenID(requestBody, response)) {
                        System.out.println("responseBody: " + requestBody);
                        routingContext
                          .response()
                          .setStatusCode(200)
                          .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                          .end(requestBody.encode());
                      } else {
                        System.out.println("did not receive an objectId from yuuvi");
                        System.out.println("responseBody: " + requestBody);
                        routingContext
                          .response()
                          .setStatusCode(500)
                          .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                          .end(requestBody.encode());
                      }
                    })
                    .onFailure(err -> {
                      System.out.println("Something went wrong " + err.getMessage());
                      routingContext
                        .end(err.getMessage());
                    });
                } else {
                  String answer = " id = '" + requestBody.getString("id") + "'";
                  routingContext
                    .response() // <1>
                    .setStatusCode(409)
                    .putHeader(HttpHeaders.CONTENT_TYPE, "text") // <2>
                    .end("klientakte allready exists for: " + answer); // <3>
                }
              })
              .onFailure(err -> {
                System.out.println("Something went wrong " + err.getMessage());
                routingContext
                  .end(err.getMessage());

              });
          });
        // end::Klientakte_Post[]

        // tag::Klientakte_Put[]
        routerBuilder.operation("Klientakte_Put")
          .handler(routingContext -> {
            System.out.println("entered handler by id Klientakte_Put");
            JsonObject requestBody = routingContext.getBodyAsJson();
            System.out.println("requestBody: " + requestBody);
            JsonObject yuuvisDocuments = new JsonObject();
            JsonArray yuuvisObjects = new JsonArray();
            JsonObject yuuvisObject = new JsonObject();
            JsonObject properties = new JsonObject();

            getKlientAkteProperties(requestBody, properties);

            yuuvisObject.put("properties",properties);
            yuuvisObjects.add(yuuvisObject);
            yuuvisDocuments.put("objects",yuuvisObjects);

            String param = requestBody.getString("id");
            System.out.println("entered handler by id Dokument_Get eDokumentenID: " + param);
            JsonObject yuuvisQuery = new JsonObject();
            yuuvisQuery.put("statement", "SELECT * FROM " + TABLENAME  +  ":" + "klientakteneo WHERE " + TABLENAME  +  ":" + "id = '" + param + "'");
            yuuvisQuery.put("skipCount",0);
            yuuvisQuery.put("maxItems",50);
            JsonObject yuuvisQueryObject = new JsonObject();
            yuuvisQueryObject.put("query", yuuvisQuery);

            client
              .post(yuuvisport, yuuvisuri, "/api/dms/objects/search")
              .timeout(20000)
              .putHeader("X-ID-TENANT-NAME", TENANT)
              .basicAuthentication(YUUVISUSER, YUUVISPASSWORD)
              .sendJsonObject(yuuvisQueryObject)
              .onSuccess(sr -> {
                HttpResponse<Buffer> responseQuery = sr;
                System.out.println("Received yuuvis Query response with status code: " + responseQuery.statusCode() + " " + responseQuery.statusMessage());
                System.out.println("Received yuuvis Query response with body: " + responseQuery.bodyAsString());
                JsonObject yuuvisSearch = new JsonObject(responseQuery.bodyAsString());
                int results = yuuvisSearch.getInteger("numItems");
                if (responseQuery.statusCode() == 200 && results >= 1) {
                  JsonArray yuuvisSearchObjects = yuuvisSearch.getJsonArray("objects");
                  JsonObject yuuvisSearchObject = yuuvisSearchObjects.getJsonObject(0);
                  JsonObject searchProperties = yuuvisSearchObject.getJsonObject("properties");
                  String objectId = searchProperties.getJsonObject("system:objectId").getString("value");
                  client
                    .patch(yuuvisport, yuuvisuri, "/api/dms/objects/" + objectId)
                    .timeout(10000)
                    .putHeader("X-ID-TENANT-NAME", TENANT)
                    .basicAuthentication(YUUVISUSER, YUUVISPASSWORD)
                    .sendJsonObject(yuuvisDocuments)
                    .onSuccess(ar -> {
                      HttpResponse<Buffer> response = ar;
                      System.out.println("Received yuuvis response with status code: " + response.statusCode() + " " + response.statusMessage());
                      System.out.println("Received yuuvis response with body: " + response.bodyAsString());

                      if (setEaktenID(requestBody, response)) {
                        System.out.println("responseBody: " + requestBody);
                        routingContext
                          .response()
                          .setStatusCode(200)
                          .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                          .end(requestBody.encode());
                      } else {
                        System.out.println("did not receive an objectId from yuuvi");
                        System.out.println("responseBody: " + requestBody);
                        routingContext
                          .response()
                          .setStatusCode(500)
                          .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                          .end(requestBody.encode());
                      }
                    })
                    .onFailure(err -> {
                      System.out.println("Something went wrong " + err.getMessage());
                      routingContext
                        .end(err.getMessage()); // <4>
                    });
                } else {
                  routingContext
                    .response() // <1>
                    .setStatusCode(204)
                    .putHeader(HttpHeaders.CONTENT_TYPE, "text") // <2>
                    .end("klientakte not found: " + requestBody.getJsonObject("klient").getString("id")); // <3>

                }
              })
              .onFailure(err -> {
                System.out.println("Something went wrong during the query: " + err.getMessage());
                routingContext
                  .end(err.getMessage()); // <4>
              });
            });
        // end::Klientakte_Put[]

        // tag::Klientakte_Get[]
        routerBuilder.operation("Klientakte_Get").handler(routingContext -> {
          String param = routingContext.pathParam("klientId");
          System.out.println("entered handler by id Klientakte_Get klientId: " + param);
          JsonObject yuuvisQuery = new JsonObject();
//          if (fallakte) {
//            if (requestBody.getString("Fallakte.Vorgang.Register").isEmpty()) {
//              yuuvisQuery.put("statement", "SELECT * FROM " + TABLENAME  +  ":" + "fallakteneo WHERE " + TABLENAME  +  ":" + "vorgangsid = '" + requestBody.getString("Fallakte.Vorgang.ID") + "'");
//            } else {
//              yuuvisQuery.put("statement", "SELECT * FROM " + TABLENAME  +  ":" + "fallakteneo WHERE " + TABLENAME  +  ":" + "vorgangsid = '" + requestBody.getString("Fallakte.Vorgang.ID") + "'" +
//                " AND " + TABLENAME  +  ":" + "register = '" + requestBody.getString("Fallakte.Vorgang.Register") + "'");
//            }
//          } else {
          yuuvisQuery.put("statement", "SELECT * FROM " + TABLENAME  +  ":" + "klientakteneo WHERE " + TABLENAME  +  ":" + "id = '" + param + "'");
//          }
          yuuvisQuery.put("skipCount",0);
          yuuvisQuery.put("maxItems",50);
          JsonObject yuuvisQueryObject = new JsonObject();
          yuuvisQueryObject.put("query", yuuvisQuery);

          client
            .post(yuuvisport, yuuvisuri, "/api/dms/objects/search")
            .timeout(20000)
            .putHeader("X-ID-TENANT-NAME", TENANT)
            .basicAuthentication(YUUVISUSER, YUUVISPASSWORD)
            .sendJsonObject(yuuvisQueryObject)
            .onSuccess(ar -> {
              HttpResponse<Buffer> responseQuery = ar;
              System.out.println("Received yuuvis Query response with status code: " + responseQuery.statusCode() +" "+ responseQuery.statusMessage());
              System.out.println("Received yuuvis Query response with body: " + responseQuery.bodyAsString());
              JsonObject yuuvisSearch = new JsonObject(responseQuery.bodyAsString());
              int results = yuuvisSearch.getInteger("numItems");
              if (responseQuery.statusCode() == 200 && results >= 1) {
                JsonArray yuuvisSearchObjects = yuuvisSearch.getJsonArray("objects");
                JsonObject yuuvisSearchObject = yuuvisSearchObjects.getJsonObject(0);
                JsonObject searchProperties = yuuvisSearchObject.getJsonObject("properties");

                JsonObject klientAkte = new JsonObject();
                klientAkte.put("eAktenID",getObjectId(responseQuery));
                JsonObject klient = new JsonObject();
                klient.put("vorname",searchProperties.getJsonObject("" + TABLENAME  +  ":" + "vorname").getString("value"));
                klient.put("nachname",searchProperties.getJsonObject("" + TABLENAME  +  ":" + "nachname").getString("value"));
                klient.put("id",searchProperties.getJsonObject("" + TABLENAME  +  ":" + "id").getString("value"));
                klient.put("geburtsdatum",searchProperties.getJsonObject("" + TABLENAME  +  ":" + "geburtsdatum").getString("value"));
                klient.put("adresse",searchProperties.getJsonObject("" + TABLENAME  +  ":" + "adresse").getString("value"));
                klientAkte.put("klient", klient);
                klientAkte.put("archivieren",searchProperties.getJsonObject("" + TABLENAME  +  ":" + "archivieren").getString("value"));
                klientAkte.put("archivierenDatum",searchProperties.getJsonObject("" + TABLENAME  +  ":" + "archivdatum").getString("value"));
                klientAkte.put("loeschen",searchProperties.getJsonObject("" + TABLENAME  +  ":" + "loeschen").getString("value"));
                klientAkte.put("loeschenDatum",searchProperties.getJsonObject("" + TABLENAME  +  ":" + "loeschdatum").getString("value"));

                routingContext
                  .response() // <1>
                  .setStatusCode(200)
                  .putHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8")
                  .end(klientAkte.toString()); // <3>
              } else {
                routingContext
                  .response() // <1>
                  .setStatusCode(204)
                  .putHeader(HttpHeaders.CONTENT_TYPE, "application/octet-stream") // <2>
                  .end("search result not unique found: " + results); // <3>
              }
              })
            .onFailure(err -> {
                System.out.println("Something went wrong during the query: " + err.getMessage());
                routingContext
                  .end(err.getMessage()); // <4>
              });

        });
        // end::Klientakte_Get[]

        AuthProvider authProvider = createBasicAuthProvider();
//        routerBuilder.securityHandler("httpBasicAuth", BasicAuthHandler.create(authProvider));
        routerBuilder.securityHandler("Basic", BasicAuthHandler.create(authProvider));

        // Generate the router
        // tag::routerGen[]
        routerBuilder.bodyHandler(BodyHandler.create());
        Router router = routerBuilder.createRouter(); // <1>

        // REGISTER OPENAPI3 DEFINITION YAML FROM MOUNT AT SAME ROUTE AS WEBROOT
        router
          .route("/swagger/ui/*")
          .method(HttpMethod.GET)
          .handler(routingContext -> {
            System.out.println("GET " + routingContext.normalizedPath());
//            System.out.println("GET " + routingContext.normalizedPath().substring(12));
//            System.out.println("GET " + routingContext.normalizedPath().substring(routingContext.normalizedPath().indexOf(".")+1));
            String fileEnding = routingContext.normalizedPath().substring(routingContext.normalizedPath().indexOf(".")+1);
            String contentType = null;
            String fileName = null;
            if (routingContext.normalizedPath().length() <= 11) {
              fileName = "index.html";
            } else {
              fileName = routingContext.normalizedPath().substring(12);
            }
            switch (fileEnding) {
              case "html":
                contentType = "text/html";
                break;
              case "css":
                contentType = "text/css";
                break;
              case "js":
                contentType = "text/javascript";
                break;
              case "png":
                contentType = "image/png";
                break;
              case "jpeg":
                contentType = "image/jpeg";
                break;
              case "jpg":
                contentType = "image/jpeg";
                break;
              default:
                contentType = "text/html";
                break;
            }
//            StaticHandler.create()
//              .setAllowRootFileSystemAccess(true)
//              .setWebRoot("dist");
            if (fileName.isEmpty()) {
              fileName = "index.html";
            }
            FileReader fileReader = null;
            String fileContent = null;
            try {
              fileReader = new FileReader("src/main/resources/dist/"  + fileName);
              if (fileReader != null) {
                int data;
                StringBuilder content = new StringBuilder();
                while ((data = fileReader.read()) != -1) {
                  content.append((char) data);
                }
                fileContent = content.toString();
              }
            } catch (FileNotFoundException e) {
              e.printStackTrace();
            } catch (IOException e) {
              e.printStackTrace();
            }

            System.out.println("Deliver: " + fileName + " " + contentType);
              routingContext
                .response()
                .setChunked(true)
                .putHeader(HttpHeaders.CONTENT_TYPE, contentType)
                .putHeader("Access-Control-Allow-Origin", "*")
                .setStatusMessage("OK")
                .end(fileContent);

//                .sendFile("dist/"  + fileName );
          });


        // Enable multipart form data parsing
        router.route().handler(BodyHandler.create().setUploadsDirectory("uploads"));


//        router.route("/*").handler(BodyHandler.create());
        router.errorHandler(404, routingContext -> { // <2>
          JsonObject errorObject = new JsonObject() // <3>
            .put("code", 404)
            .put("message",
              (routingContext.failure() != null) ?
                routingContext.failure().getMessage() :
                "Not Found"
            );
          routingContext
            .response()
            .setStatusCode(404)
            .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .end(errorObject.encode()); // <4>
        });

        router.errorHandler(401, routingContext -> {
          JsonObject errorObject = new JsonObject()
            .put("code", 401)
            .put("message",
              (routingContext.failure() != null) ?
                routingContext.failure().getMessage() :
                "Unauthorized"
            );
          routingContext
            .response()
            .setStatusCode(401)
            .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .end(errorObject.encode());
        });

        router.errorHandler(400, routingContext -> {
          JsonObject errorObject = new JsonObject()
            .put("code", 400)
            .put("message",
              (routingContext.failure() != null) ?
                routingContext.failure().getMessage() :
                "Bad Request"
            );
          routingContext
            .response()
            .setStatusCode(400)
            .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .end(errorObject.encode());
        });

        final int serverPort;
        if (SSLENABLED) {
          if (!SELFSIGNEDCERTIFICATE) {
            File f = new File(KEYSTOREPATH);
            if (f.exists()) {
              System.out.println("file exists for KEYSTOREPATH: " + KEYSTOREPATH);
              server = vertx.createHttpServer(new HttpServerOptions().setPort(443).setMaxFormAttributeSize(10256).setSsl(true).setKeyStoreOptions(
                new JksOptions().
                  setPath(KEYSTOREPATH).
                  setPassword(KEYSTOREPASSWORD)
              ));
              System.out.println("Setup Server with SSLENABLED: " + SSLENABLED + " and with deliverd keystor");
              serverPort = 443;
            } else {
              System.out.println("file does not exists for KEYSTOREPATH: " + KEYSTOREPATH);
              server = vertx.createHttpServer(new HttpServerOptions().setPort(8080).setMaxFormAttributeSize(10256)); // <5>
              System.out.println("Setup Server with http as fall back ");
              serverPort = 8080;
            }
          } else {
            try {
              // Generate a self-signed key pair and certificate.
              KeyStore store = KeyStore.getInstance("JKS");
              store.load(null, null);
              CertAndKeyGen keypair = new CertAndKeyGen("RSA", "SHA256WithRSA", null);

              InetAddress localMachine = InetAddress.getLocalHost();
              System.out.println("Hostadress of local machine: " + KEYSTORECN);

              X500Name x500Name = new X500Name(KEYSTORECN, "IT", "yuuvis", "unknown", "unknown", "unknown");
              keypair.generate(4096);
              PrivateKey privKey = keypair.getPrivateKey();
              X509Certificate[] chain = new X509Certificate[1];
              chain[0] = keypair.getSelfCertificate(x500Name, new Date(), (long) 365 * 24 * 60 * 60);
              store.setKeyEntry("selfsigned", privKey, "changeit".toCharArray(), chain);
              store.store(new FileOutputStream("./file-uploads/.keystore"), "changeit".toCharArray());

              HttpServerOptions httpOpts = new HttpServerOptions();
              httpOpts.setKeyStoreOptions(new JksOptions().setPath("./file-uploads/.keystore").setPassword("changeit"));
              httpOpts.setSsl(true);
              httpOpts.setPort(443);
              httpOpts.setMaxFormAttributeSize(10256);
              server = vertx.createHttpServer(httpOpts);
            } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException | NoSuchProviderException | InvalidKeyException | SignatureException ex) {
              System.out.println("Failed to generate a self-signed cert and other SSL configuration methods failed. " + ex);
            }
            serverPort = 443;
          }
        } else {
          serverPort = 8080;
          server = vertx.createHttpServer(new HttpServerOptions().setPort(8080).setMaxFormAttributeSize(10256)); // <5>
        }
        server
          .requestHandler(router)
          .listen(
            (ar) -> {
              if (ar.succeeded()) {
                System.out.println("HTTP-OPENAPI3-SERVER STARTED ON PORT:" + serverPort);
                startPromise.complete();
              } else {
                System.out.println("ERROR starting HTTP-OPENAPI3-SERVER ON PORT:" + serverPort + " message: " + ar.cause().getMessage() +" couse: " +ar.cause().getCause());
                System.out.println("localisedmessage: " + ar.cause().getLocalizedMessage());
                startPromise.fail("start failed");
              }
            });
      })
      .onFailure(startPromise::fail);
  }

  private void getFallAkteProperties(JsonObject requestBody, JsonObject properties) {
    JsonObject systemObject = new JsonObject();
    systemObject.put("value","" + TABLENAME  +  ":" + "fallakteneo");
    properties.put("system:objectTypeId",systemObject);
    JsonObject clienttitle = new JsonObject();
    clienttitle.put("value", "ClientTitle");
    properties.put("appClient:clienttitle", clienttitle);
    JsonObject clientdescription = new JsonObject();
    clientdescription.put("value", "clientDescription");
    properties.put("appClient:clientdescription", clientdescription);
//          JsonObject clientSystem = new JsonObject();
//          clientSystem.put("value","" + TABLENAME  +  ":" + "fallakteneo");
//          properties.put("appClientsystem:leadingTypeId", clientSystem);

    JsonObject aktenzeichen = new JsonObject();
    aktenzeichen.put("value", requestBody.getJsonObject("vorgang").getString("aktenzeichen"));
    properties.put("aktenzeichen",aktenzeichen);
    JsonObject eaktenid = new JsonObject();
    eaktenid.put("value", requestBody.getString("eAktenID"));
    properties.put("eaktenid",eaktenid);
    JsonObject sachbearbeiter = new JsonObject();
    sachbearbeiter.put("value", requestBody.getJsonObject("vorgang").getString("zustaendigerSachbearbeiter"));
    properties.put("sachbearbeiter",sachbearbeiter);
    JsonObject vorgangsid = new JsonObject();
    vorgangsid.put("value", requestBody.getJsonObject("vorgang").getString("id"));
    properties.put("vorgangsid",vorgangsid);
    if (!requestBody.getJsonObject("vorgang").getString("register").isEmpty()) {
      JsonObject register = new JsonObject();
      register.put("value", requestBody.getJsonObject("vorgang").getString("register"));
      properties.put("register", register);
    }
    JsonObject rechtsgebiet = new JsonObject();
    rechtsgebiet.put("value", requestBody.getJsonObject("vorgang").getString("rechtsgebiet"));
    properties.put("rechtsgebiet",rechtsgebiet);
    JsonObject vorname = new JsonObject();
    vorname.put("value", requestBody.getJsonObject("leistungsempfaenger").getString("vorname"));
    properties.put("vorname",vorname);
    JsonObject nachname = new JsonObject();
    nachname.put("value", requestBody.getJsonObject("leistungsempfaenger").getString("nachname"));
    properties.put("nachname",nachname);
    JsonObject geburtsdatum = new JsonObject();

    String dateFormatted = getFormattedDate(requestBody.getJsonObject("leistungsempfaenger").getString("geburtsdatum"));

    geburtsdatum.put("value", dateFormatted);
    properties.put("geburtsdatum",geburtsdatum);
    JsonObject id = new JsonObject();
    id.put("value", requestBody.getJsonObject("leistungsempfaenger").getString("id"));
    properties.put("id",id);
    JsonObject vornameAntragsteller = new JsonObject();
    vornameAntragsteller.put("value", requestBody.getJsonObject("antragssteller").getString("vorname"));
    properties.put("vornameAntragsteller",vornameAntragsteller);
    JsonObject nachnameAntragsteller = new JsonObject();
    nachnameAntragsteller.put("value", requestBody.getJsonObject("antragssteller").getString("nachname"));
    properties.put("nachnameAntragsteller",nachnameAntragsteller);
    JsonObject geburtsdatumAntragsteller = new JsonObject();

    String dateFormatted2 = getFormattedDate(requestBody.getJsonObject("antragssteller").getString("geburtsdatum"));

    geburtsdatumAntragsteller.put("value", dateFormatted2);
    properties.put("geburtsdatumAntragsteller",geburtsdatumAntragsteller);
    JsonObject idAntragsteller = new JsonObject();
    idAntragsteller.put("value", requestBody.getJsonObject("antragssteller").getString("id"));
    properties.put("idAntragsteller",idAntragsteller);
    JsonObject vornameUnterhaltspflichtiger = new JsonObject();
    vornameUnterhaltspflichtiger.put("value", requestBody.getJsonObject("unterhaltspflichtiger").getString("vorname"));
    properties.put("vornameUnterhaltspflichtiger",vornameUnterhaltspflichtiger);
    JsonObject nachnameUnterhaltspflichtiger = new JsonObject();
    nachnameUnterhaltspflichtiger.put("value", requestBody.getJsonObject("unterhaltspflichtiger").getString("nachname"));
    properties.put("nachnameUnterhaltspflichtiger",nachnameUnterhaltspflichtiger);
    JsonObject geburtsdatumUnterhaltspflichtiger = new JsonObject();

    String dateFormatted3 = getFormattedDate(requestBody.getJsonObject("unterhaltspflichtiger").getString("geburtsdatum"));

    geburtsdatumUnterhaltspflichtiger.put("value", dateFormatted3);
    properties.put("geburtsdatumUnterhaltspflichtiger",geburtsdatumUnterhaltspflichtiger);
    JsonObject idUnterhaltspflichtiger = new JsonObject();
    idUnterhaltspflichtiger.put("value", requestBody.getJsonObject("unterhaltspflichtiger").getString("id"));
    properties.put("idUnterhaltspflichtiger",idUnterhaltspflichtiger);
    JsonObject archivdatum = new JsonObject();

    String dateFormatted4 = getFormattedDate(requestBody.getJsonObject("vorgang").getString("archivierenDatum"));

    archivdatum.put("value", dateFormatted4);
    properties.put("archivdatum",archivdatum);
    JsonObject loeschdatum = new JsonObject();

    String dateFormatted5 = getFormattedDate(requestBody.getJsonObject("vorgang").getString("loeschenDatum"));

    loeschdatum.put("value", dateFormatted5);
    properties.put("loeschdatum",loeschdatum);
    JsonObject archivieren = new JsonObject();
    archivieren.put("value", requestBody.getJsonObject("vorgang").getString("archivieren"));
    properties.put("archivieren",archivieren);
    JsonObject loeschen = new JsonObject();
    loeschen.put("value", requestBody.getJsonObject("vorgang").getString("loeschen"));
    properties.put("loeschen",loeschen);
    JsonObject Bemerkung = new JsonObject();
    Bemerkung.put("value", requestBody.getJsonObject("vorgang").getString("bemerkung"));
    properties.put("Bemerkung",Bemerkung);
  }

  private String getFormattedDate(String  dateInput) {
    String dateFormatted = "";
    if (dateInput != null) {
      String[] dateArray = dateInput.split("\\.");
      if (dateArray != null && dateArray.length == 3) {
        dateFormatted = dateArray[2] + "-" + dateArray[1] + "-" + dateArray[0];
      } else {
        dateFormatted = "1970-01-01";
      }
    } else {
      dateFormatted = "1970-01-01";
    }
    return dateFormatted;
  }

  private JsonObject getDocumentPostAnswer(JsonObject requestBody, HttpResponse<Buffer> response, boolean fallakte) {
    JsonObject oldRequest = requestBody;
    requestBody = new JsonObject();
    boolean result =  setFieldId("eDokumentenID", requestBody, response);
    requestBody.put("erstellungZeitpunkt",oldRequest.getString("Dokument.ErstellungZeitpunkt"));
    requestBody.put("typ",oldRequest.getString("Dokument.Typ"));
    requestBody.put("vorlage",oldRequest.getString("Dokument.Vorlage"));
    requestBody.put("prosozDateiname",oldRequest.getString("Dokument.ProsozDateiname"));
    requestBody.put("contentUrl",oldRequest.getString("Dokument.ContentUrl"));
    JsonObject sachbearbeiter = new JsonObject();
    sachbearbeiter.put("vorname",oldRequest.getString("Dokument.Sachbearbeiter.Vorname"));
    sachbearbeiter.put("nachname",oldRequest.getString("Dokument.Sachbearbeiter.Nachname"));
    sachbearbeiter.put("kennung",oldRequest.getString("Dokument.Sachbearbeiter.Kennung"));
    requestBody.put("sachbearbeiter",sachbearbeiter);
    JsonObject empfaenger = new JsonObject();
    empfaenger.put("vorname",oldRequest.getString("Dokument.Empfaenger.Vorname"));
    empfaenger.put("nachname",oldRequest.getString("Dokument.Empfaenger.Nachname"));
    empfaenger.put("adresse",oldRequest.getString("Dokument.Empfaenger.Adresse"));
    requestBody.put("empfaenger",empfaenger);
    JsonObject klient = new JsonObject();
    klient.put("vorname",oldRequest.getString("Klientakte.Klient.Vorname"));
    klient.put("nachname",oldRequest.getString("Klientakte.Klient.Nachname"));
    klient.put("id",oldRequest.getString("Klientakte.Klient.ID"));
    klient.put("geburtsdatum",oldRequest.getString("Klientakte.Klient.Geburtsdatum"));
    klient.put("adresse",oldRequest.getString("Klientakte.Klient.Adresse"));
    requestBody.put("klient",klient);
    return requestBody;
  }

  private boolean setEaktenID(JsonObject requestBody, HttpResponse<Buffer> response) {
    return setFieldId("eaktenID", requestBody, response);
  }

  private boolean setFieldId(String field ,JsonObject requestBody, HttpResponse<Buffer> response) {
    String objectID = getObjectId(response);
    requestBody.put(field, objectID);
    if (objectID != null ) {
      return true;
    } else {
      return false;
    }
  }

  private String getObjectId(HttpResponse<Buffer> response) {
    if (response != null &&
      response.bodyAsJsonObject() != null &&
      response.bodyAsJsonObject().getJsonArray("objects") != null &&
      response.bodyAsJsonObject().getJsonArray("objects").getJsonObject(0) != null &&
      response.bodyAsJsonObject().getJsonArray("objects").getJsonObject(0).getJsonObject("properties") != null &&
      response.bodyAsJsonObject().getJsonArray("objects").getJsonObject(0).getJsonObject("properties").getJsonObject("system:objectId") != null &&
      response.bodyAsJsonObject().getJsonArray("objects").getJsonObject(0).getJsonObject("properties").getJsonObject("system:objectId").getString("value") != null) {
      return response.bodyAsJsonObject().getJsonArray("objects").getJsonObject(0).getJsonObject("properties").getJsonObject("system:objectId").getString("value");
    } else {
      return "";
    }
  }

  private void getKlientAkteProperties(JsonObject requestBody, JsonObject properties) {
    JsonObject systemObject = new JsonObject();
    systemObject.put("value","" + TABLENAME  +  ":" + "klientakteneo");
    properties.put("system:objectTypeId",systemObject);
    JsonObject clienttitle = new JsonObject();
    clienttitle.put("value", "ClientTitle");
    properties.put("appClient:clienttitle", clienttitle);
    JsonObject clientdescription = new JsonObject();
    clientdescription.put("value", "clientDescription");
    properties.put("appClient:clientdescription", clientdescription);
    JsonObject vorname = new JsonObject();
    vorname.put("value",requestBody.getJsonObject("klient").getString("vorname"));
    properties.put("vorname",vorname);
    JsonObject nachname = new JsonObject();
    nachname.put("value",requestBody.getJsonObject("klient").getString("nachname"));
    properties.put("nachname",nachname);
    JsonObject id = new JsonObject();
    id.put("value",requestBody.getJsonObject("klient").getString("id"));
    properties.put("id",id);
    JsonObject geburtsdatum = new JsonObject();

    String dateFormatted = getFormattedDate(requestBody.getJsonObject("klient").getString("geburtsdatum"));

    geburtsdatum.put("value",dateFormatted);
    properties.put("geburtsdatum",geburtsdatum);
    JsonObject adresse = new JsonObject();
    adresse.put("value",requestBody.getJsonObject("klient").getString("adresse"));
    properties.put("adresse",adresse);

    JsonObject archivdatum = new JsonObject();
    String dateFormatted1 = getFormattedDate(requestBody.getString("archivdatum"));
    archivdatum.put("value",dateFormatted1);
    properties.put("archivdatum",archivdatum);
    JsonObject loeschdatum = new JsonObject();

    String dateFormatted2 = getFormattedDate(requestBody.getString("loeschdatum"));
    loeschdatum.put("value",dateFormatted2);
    properties.put("loeschdatum",loeschdatum);
    JsonObject archivieren = new JsonObject();
    archivieren.put("value",requestBody.getString("archivieren"));
    properties.put("archivieren",archivieren);
    JsonObject loeschen = new JsonObject();
    loeschen.put("value",requestBody.getString("loeschen"));
    properties.put("loeschen",loeschen);
  }

  private JsonArray getDokument(JsonObject yuuvisSearch, boolean fallakte, JsonObject klientFolder) {
//    JsonObject yuuvisDocuments = new JsonObject();
    JsonArray yuuvisObjects = new JsonArray();
    JsonObject yuuvisObject = new JsonObject();
    JsonObject properties = new JsonObject();

//    JsonObject klient = new JsonObject();
//    if (fallakte) {
//      klient.put("vorname","");
//      klient.put("nachname","");
//      klient.put("id","");
//      klient.put("geburtsdatum","");
//      klient.put("adresse","");
//    } else {
//      klient.put("vorname",klientFolder.getJsonObject("tenYuuvistest:vorname").getString("value"));
//      klient.put("nachname",klientFolder.getJsonObject("tenYuuvistest:nachname").getString("value"));
//      klient.put("id",klientFolder.getJsonObject("tenYuuvistest:id").getString("value"));
//      klient.put("geburtsdatum",klientFolder.getJsonObject("tenYuuvistest:geburtsdatum").getString("value"));
//      klient.put("adresse",klientFolder.getJsonObject("tenYuuvistest:adresse").getString("value"));
//    }

    JsonArray yuuvisSearchObjects = yuuvisSearch.getJsonArray("objects");
    for (int i = 0; i< yuuvisSearchObjects.size(); i++) {
      JsonObject yuuvisSearchObject = yuuvisSearchObjects.getJsonObject(i);
      JsonObject searchProperties = yuuvisSearchObject.getJsonObject("properties");

      yuuvisObject.put("eDokumentenID",searchProperties.getJsonObject("system:objectId").getString("value"));
      yuuvisObject.put("erstellungZeitpunkt",searchProperties.getJsonObject("tenYuuvistest:erstelldatum").getString("value"));
      yuuvisObject.put("typ",searchProperties.getJsonObject("tenYuuvistest:dokumenttyp").getString("value"));
      yuuvisObject.put("vorlage",searchProperties.getJsonObject("tenYuuvistest:vorlage").getString("value"));

      JsonObject sachbearbeiter = new JsonObject();
      JsonObject vornamesachbearbeiter = searchProperties.getJsonObject("tenYuuvistest:vornamesachbearbeiter");
      String vorname = vornamesachbearbeiter.getString("value");
      sachbearbeiter.put("vorname", vorname);
      JsonObject nachnamesachbearbeiter = searchProperties.getJsonObject("tenYuuvistest:nachnamesachbearbeiter");
      String nachname = nachnamesachbearbeiter.getString("value");
      sachbearbeiter.put("nachname", nachname);
      JsonObject kennungsachbearbeiter = searchProperties.getJsonObject("tenYuuvistest:kennungsachbearbeiter");
      String kennung = kennungsachbearbeiter.getString("value");
      sachbearbeiter.put("kennung", kennung);
      yuuvisObject.put("sachbearbeiter", sachbearbeiter);

      JsonObject empfaenger = new JsonObject();
      JsonObject vornameempfaenger = searchProperties.getJsonObject("tenYuuvistest:vornameempfaenger");
      vorname = vornameempfaenger.getString("value");
      empfaenger.put("vorname", vorname);
      JsonObject nachnameempfaenger = searchProperties.getJsonObject("tenYuuvistest:nachnameempfaenger");
      nachname = nachnameempfaenger.getString("value");
      empfaenger.put("nachname", nachname);
      JsonObject kennungempfaenger = searchProperties.getJsonObject("tenYuuvistest:kennungempfaenger");
      String adresse = kennungempfaenger.getString("value");
      empfaenger.put("adresse", adresse);
      yuuvisObject.put("empfaenger", empfaenger);

//      yuuvisObject.put("klient", klient);

      yuuvisObject.put("prosozDateiname",searchProperties.getJsonObject("tenYuuvistest:dateiname").getString("value"));
      yuuvisObject.put("contentUrl",SERVERURL + "/api/Dokument?eDokumentenID=" + searchProperties.getJsonObject("tenYuuvistest:edokumentenid").getString("value"));

      yuuvisObjects.add(yuuvisObject);
    }
//    yuuvisDocuments.put("documents", yuuvisObjects);
//    return yuuvisDocuments;
    return yuuvisObjects;
  }

  private JsonObject getFallakteDokument(String name, String fileName, String fileType, JsonObject requestBody, JsonObject yuuvisSearch, int yuuvisSearchIndex) {
    JsonArray yuuvisSearchObjects = yuuvisSearch.getJsonArray("objects");
    JsonObject yuuvisSearchObject = yuuvisSearchObjects.getJsonObject(yuuvisSearchIndex);
    JsonObject searchProperties = yuuvisSearchObject.getJsonObject("properties");
    JsonObject searchSystemobjectId = searchProperties.getJsonObject("system:objectId");
    String folderID = searchSystemobjectId.getString("value");

    JsonObject yuuvisDocuments = new JsonObject();
    JsonArray yuuvisObjects = new JsonArray();
    JsonObject yuuvisObject = new JsonObject();
    JsonObject properties = new JsonObject();

    JsonObject systemObject = new JsonObject();
    systemObject.put("value", "" + TABLENAME  +  ":" + "fallakteneodokument");
    properties.put("system:objectTypeId", systemObject);
//    JsonObject clientSystem = new JsonObject();
//    clientSystem.put("value","" + TABLENAME  +  ":" + "fallakteneodokument");
//    properties.put("appClientsystem:leadingTypeId", clientSystem);
    JsonObject systemparentId = new JsonObject();
    systemparentId.put("value", folderID);
    properties.put("system:parentId", systemparentId);
//    //vorname, nachname
    JsonObject clienttitle = new JsonObject();
    clienttitle.put("value", "clienttitle");
    properties.put("clienttitle", clienttitle);
    //prosozDateiname
    JsonObject clientdescription = new JsonObject();
    clientdescription.put("value", "clientdescription");
    properties.put("clientdescription", clientdescription);

    JsonObject edokumentenid = new JsonObject();
    edokumentenid.put("value",  requestBody.getString("Dokument.EDokumentenID"));
    properties.put("edokumentenid", edokumentenid);

//                registertyp ?
    JsonObject registertyp = new JsonObject();
    registertyp.put("value",  requestBody.getString("Fallakte.RegisterObjektTypName"));
    properties.put("registertyp", registertyp);
//                dokumententyp ?
    JsonObject dokumenttyp = new JsonObject();
    dokumenttyp.put("value",  requestBody.getString("Dokument.Typ"));
    properties.put("dokumenttyp", dokumenttyp);

    JsonObject vorlage = new JsonObject();
    vorlage.put("value", requestBody.getString("Dokument.Vorlage"));
    properties.put("vorlage", vorlage);
//                current system time - erstellungZeitpunkt+ ?
    JsonObject erstelldatum = new JsonObject();
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY);
//                String erstelldatumTime = sdf.format(System.currentTimeMillis());

    String dateFormatted = getFormattedDate(requestBody.getString("Dokument.ErstellungZeitpunkt"));

    erstelldatum.put("value", dateFormatted);
    properties.put("erstelldatum", erstelldatum);

    JsonObject vornamesachbearbeiter = new JsonObject();
    vornamesachbearbeiter.put("value", requestBody.getString("Dokument.Sachbearbeiter.Vorname"));
    properties.put("vornamesachbearbeiter", vornamesachbearbeiter);
    JsonObject nachnamesachbearbeiter = new JsonObject();
    nachnamesachbearbeiter.put("value", requestBody.getString("Dokument.Sachbearbeiter.Nachname"));
    properties.put("nachnamesachbearbeiter", nachnamesachbearbeiter);
    JsonObject kennungsachbearbeiter = new JsonObject();
    kennungsachbearbeiter.put("value", requestBody.getString("Dokument.Sachbearbeiter.Kennung"));
    properties.put("kennungsachbearbeiter", kennungsachbearbeiter);
    JsonObject vornameempfaenger = new JsonObject();
    vornameempfaenger.put("value", requestBody.getString("Dokument.Empfaenger.Vorname"));
    properties.put("vornameempfaenger", vornameempfaenger);
    JsonObject nachnameempfaenger = new JsonObject();
    nachnameempfaenger.put("value", requestBody.getString("Dokument.Empfaenger.Nachname"));
    properties.put("nachnameempfaenger", nachnameempfaenger);
    JsonObject kennungempfaenger = new JsonObject();
    kennungempfaenger.put("value", requestBody.getString("Dokument.Empfaenger.Adresse"));
    properties.put("kennungempfaenger", kennungempfaenger);


    JsonObject dateiname = new JsonObject();
    dateiname.put("value", requestBody.getString("Dokument.ProsozDateiname"));
    properties.put("dateiname", dateiname);

    yuuvisObject.put("properties", properties);
    JsonArray contentStreams = new JsonArray();
    JsonObject contentStreamsElement = new JsonObject();
    contentStreamsElement.put("mimeType", fileType);
    contentStreamsElement.put("fileName", fileName);
    contentStreamsElement.put("cid", name);
    contentStreams.add(contentStreamsElement);
    yuuvisObject.put("contentStreams", contentStreams);

    yuuvisObjects.add(yuuvisObject);
    yuuvisDocuments.put("objects", yuuvisObjects);
    return yuuvisDocuments;
  }

  private JsonObject getKlientakteDokument(String name, String fileName, String fileType, JsonObject requestBody, JsonObject yuuvisSearch, int yuuvisSearchIndex) {
    JsonArray yuuvisSearchObjects = yuuvisSearch.getJsonArray("objects");
    JsonObject yuuvisSearchObject = yuuvisSearchObjects.getJsonObject(yuuvisSearchIndex);
    JsonObject searchProperties = yuuvisSearchObject.getJsonObject("properties");
    JsonObject searchSystemobjectId = searchProperties.getJsonObject("system:objectId");
    String folderID = searchSystemobjectId.getString("value");

    JsonObject yuuvisDocuments = new JsonObject();
    JsonArray yuuvisObjects = new JsonArray();
    JsonObject yuuvisObject = new JsonObject();
    JsonObject properties = new JsonObject();

    JsonObject systemObject = new JsonObject();
    systemObject.put("value", "" + TABLENAME  +  ":" + "klientakteneodokument");
    properties.put("system:objectTypeId", systemObject);
//    JsonObject clientSystem = new JsonObject();
//    clientSystem.put("value","" + TABLENAME  +  ":" + "klientakteneodokument");
//    properties.put("appClientsystem:leadingTypeId", clientSystem);
    JsonObject systemparentId = new JsonObject();
    systemparentId.put("value", folderID);
    properties.put("system:parentId", systemparentId);
//    //vorname, nachname
    JsonObject clienttitle = new JsonObject();
    clienttitle.put("value", "clienttitle");
    properties.put("clienttitle", clienttitle);
    //prosozDateiname
    JsonObject clientdescription = new JsonObject();
    clientdescription.put("value", "clientdescription");
    properties.put("clientdescription", clientdescription);

    JsonObject edokumentenid = new JsonObject();
    edokumentenid.put("value",  requestBody.getString("Dokument.EDokumentenID"));
    properties.put("edokumentenid", edokumentenid);

//                registertyp ?
    JsonObject registertyp = new JsonObject();
    registertyp.put("value",  requestBody.getString("Klientakte.OrdnerObjektTypName"));
    properties.put("registertyp", registertyp);
//                dokumententyp ?
    JsonObject dokumenttyp = new JsonObject();
    dokumenttyp.put("value",  requestBody.getString("Dokument.Typ"));
    properties.put("dokumenttyp", dokumenttyp);

    JsonObject vorlage = new JsonObject();
    vorlage.put("value", requestBody.getString("Dokument.Vorlage"));
    properties.put("vorlage", vorlage);
//                current system time - erstellungZeitpunkt+ ?
    JsonObject erstelldatum = new JsonObject();
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY);
//                String erstelldatumTime = sdf.format(System.currentTimeMillis());

    String dateFormatted = getFormattedDate(requestBody.getString("Dokument.ErstellungZeitpunkt"));
    erstelldatum.put("value", dateFormatted);
    properties.put("erstelldatum", erstelldatum);

    JsonObject vornamesachbearbeiter = new JsonObject();
    vornamesachbearbeiter.put("value", requestBody.getString("Dokument.Sachbearbeiter.Vorname"));
    properties.put("vornamesachbearbeiter", vornamesachbearbeiter);
    JsonObject nachnamesachbearbeiter = new JsonObject();
    nachnamesachbearbeiter.put("value", requestBody.getString("Dokument.Sachbearbeiter.Nachname"));
    properties.put("nachnamesachbearbeiter", nachnamesachbearbeiter);
    JsonObject kennungsachbearbeiter = new JsonObject();
    kennungsachbearbeiter.put("value", requestBody.getString("Dokument.Sachbearbeiter.Kennung"));
    properties.put("kennungsachbearbeiter", kennungsachbearbeiter);
    JsonObject vornameempfaenger = new JsonObject();
    vornameempfaenger.put("value", requestBody.getString("Dokument.Empfaenger.Vorname"));
    properties.put("vornameempfaenger", vornameempfaenger);
    JsonObject nachnameempfaenger = new JsonObject();
    nachnameempfaenger.put("value", requestBody.getString("Dokument.Empfaenger.Nachname"));
    properties.put("nachnameempfaenger", nachnameempfaenger);
    JsonObject kennungempfaenger = new JsonObject();
    kennungempfaenger.put("value", requestBody.getString("Dokument.Empfaenger.Adresse"));
    properties.put("kennungempfaenger", kennungempfaenger);
// idempfaenger - redundant, da kennungempfaenger
//    die folgenden felder ebenfalls redundant, da sie im folder bereits sind
//  <propertyReference>vornameKlient</propertyReference>
//  <propertyReference>nachnameKlient</propertyReference>
//  <propertyReference>geburtsdatumKlient</propertyReference>
//  <propertyReference>idKlient</propertyReference>
//  <propertyReference>dateiname</propertyReference>
    JsonObject dateiname = new JsonObject();
    dateiname.put("value", requestBody.getString("Dokument.ProsozDateiname"));
    properties.put("dateiname", dateiname);
//  <propertyReference>adresseKlient</propertyReference>


    yuuvisObject.put("properties", properties);
    JsonArray contentStreams = new JsonArray();
    JsonObject contentStreamsElement = new JsonObject();
    contentStreamsElement.put("mimeType", fileType);
    contentStreamsElement.put("fileName", fileName);
    contentStreamsElement.put("cid", name);
    contentStreams.add(contentStreamsElement);
    yuuvisObject.put("contentStreams", contentStreams);

    yuuvisObjects.add(yuuvisObject);
    yuuvisDocuments.put("objects", yuuvisObjects);
    return yuuvisDocuments;
  }

  @Override
  public void stop(){
    this.server.close();
  }

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new ApiVerticle());
  }

  private AuthProvider createBasicAuthProvider() {
    AuthProvider authProvider =
      (authInfo, resultHandler) -> {
        if (authInfo.getString(BASIC_USERNAME).equals(USER)
          && authInfo.getString(BASIC_PASSWORD).equals(PASSWORD)) {
          resultHandler.handle(Future.succeededFuture());
        } else {
          resultHandler.handle(Future.failedFuture("INVALID HTTPBASICAUTH CREDENTIALS"));
        }
      };
    System.out.println("auth provider");
    return authProvider;
  }

}
