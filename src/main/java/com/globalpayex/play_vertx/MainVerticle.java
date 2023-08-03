package com.globalpayex.play_vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class MainVerticle extends AbstractVerticle {

  private void handleGetAtRootPath(RoutingContext routingContext) {
    HttpServerResponse response = routingContext.response();
    response
      .putHeader("content-type", "text/html")
      .end("hello world from vertx");
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    // Routes -> Router
    Router router = Router.router(vertx);
    router
      .route(HttpMethod.GET, "/")
      .handler(this::handleGetAtRootPath);


    vertx
      .createHttpServer()
      .requestHandler(router)
      .listen(8888, http -> {
        if (http.succeeded()) {
          startPromise.complete();
          System.out.println("HTTP server started on port 8888");
        } else {
          startPromise.fail(http.cause());
        }
    });
  }
}
