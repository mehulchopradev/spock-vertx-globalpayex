package com.globalpayex.play_vertx;

import com.globalpayex.play_vertx.library.domain.Book;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class LibraryApiVerticle extends AbstractVerticle {

  private Map<Integer, Book> booksMap = new LinkedHashMap<>();

  private static final String ENDPOINT = "/api/v1/books";

  private void initializeDb() {
    Book b1 = new Book("prog in Java", 900, 1000f);
    booksMap.put(b1.getId(), b1);
    Book b2 = new Book("Python programming", 870, 1500f);
    booksMap.put(b2.getId(), b2);
  }

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

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    initializeDb();

    Router router = Router.router(vertx);
    router.get(ENDPOINT).handler(this::handleGetAllBooks);
    router.get(ENDPOINT + "/:id").handler(this::handleGetBook);

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
}
