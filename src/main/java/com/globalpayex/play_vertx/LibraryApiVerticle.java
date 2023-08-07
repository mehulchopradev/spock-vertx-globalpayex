package com.globalpayex.play_vertx;

import com.globalpayex.play_vertx.library.domain.Book;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class LibraryApiVerticle extends AbstractVerticle {

  private Map<Integer, Book> booksMap = new LinkedHashMap<>();

  private static final String ENDPOINT = "/api/v1/books";
  private MongoClient mongoClient;
  private static final String BOOKS_COLLECTION = "books";

  /* private void initializeDb() {
    Book b1 = new Book("prog in Java", 900, 1000f);
    booksMap.put(b1.getId(), b1);
    Book b2 = new Book("Python programming", 870, 1500f);
    booksMap.put(b2.getId(), b2);
  } */

  private void handleGetAllBooks(RoutingContext routingContext) {
    routingContext
      .response()
      .setStatusCode(200)
      .putHeader("content-type", "application/json")
      .end(Json.encodePrettily(booksMap.values()));
  }

  private void handleGetBook(RoutingContext routingContext) {
    String bookId = routingContext.request().getParam("id");
    System.out.println(booksMap);
    Book book = booksMap.get(Integer.valueOf(bookId));
    routingContext
      .response()
      .setStatusCode(200)
      .putHeader("content-type", "application/json")
      .end(Json.encodePrettily(book));
  }

  private void handleNewBook(RoutingContext routingContext) {
    String requestData = routingContext.body().asString();
    Book book = Json.decodeValue(requestData, Book.class);
    System.out.println("New book" + book);

    mongoClient.insert(
      BOOKS_COLLECTION,
      book.toJson(),
      asyncResult -> {
        if(asyncResult.succeeded()) {
          String newBookId = asyncResult.result();
          book.setId(newBookId);

          routingContext
            .response()
            .setStatusCode(201)
            .putHeader("content-type", "application/json")
            .end(Json.encodePrettily(book));
        } else {
          routingContext.response().setStatusCode(500).end("Something went wrong");
        }
      }
    );
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    // initializeDb();

    mongoClient = MongoClient.createShared(vertx, config());

    Router router = Router.router(vertx);
    router.route(ENDPOINT + "/*").handler(BodyHandler.create());
    router.get(ENDPOINT).handler(this::handleGetAllBooks);
    router.get(ENDPOINT + "/:id").handler(this::handleGetBook);
    router.post(ENDPOINT).handler(this::handleNewBook);
    router.delete(ENDPOINT + "/:id").handler(this::handleDeleteBook);

    vertx
      .createHttpServer()
      .requestHandler(router)
      .listen(config().getInteger("http.port"), httpServerResult -> {
        if (httpServerResult.succeeded()) {
          startPromise.complete();
          System.out.println("Library api server started on port " + config().getInteger("http.port"));
        } else {
          startPromise.fail(httpServerResult.cause());
        }
      });
  }

  private void handleDeleteBook(RoutingContext routingContext) {
    String bookId = routingContext.request().getParam("id");
    if (bookId == null) {
      routingContext.response().setStatusCode(400).end("Book id missing");
    } else {
      System.out.println(bookId);
      mongoClient
        .removeDocument(BOOKS_COLLECTION, new JsonObject().put("_id", bookId), asyncResult -> {
          if (asyncResult.succeeded() && asyncResult.result().getRemovedCount() != 0) {
            routingContext.response().setStatusCode(204).end();
          } else {
            System.out.println(asyncResult.cause());
            routingContext.response().setStatusCode(500).end("Something went wrong");
          }
        });
    }
  }
}
