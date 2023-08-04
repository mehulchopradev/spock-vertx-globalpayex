package com.globalpayex.play_vertx

import com.globalpayex.play_vertx.library.domain.Book
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpClientRequest
import io.vertx.core.http.HttpClientResponse
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import spock.lang.Specification
import spock.util.concurrent.BlockingVariable
import spock.util.concurrent.BlockingVariables

class LibraryApiVerticleSpec extends Specification {

  Vertx vertx

  DeploymentOptions deploymentOptions

  static final Integer TEST_PORT = 8082
  static final String TEST_HOST = 'localhost'

  def setup() {
    vertx = Vertx.vertx()
    deploymentOptions = new DeploymentOptions()
    def jsonObject = new JsonObject()
      .put('http.port', TEST_PORT)
    deploymentOptions.setConfig jsonObject
  }

  def cleanup() {
    vertx.close()
  }

  def "testing the deployment and undeployment of the LibraryApiVerticle"() {
    given:
    def deploymentResult = new BlockingVariable<Boolean>()
    def undeploymentResult = new BlockingVariable<Boolean>()

    when:
    vertx.deployVerticle(new LibraryApiVerticle(), deploymentOptions) { deploymentAsyncResult ->
      deploymentResult.set(deploymentAsyncResult.succeeded())

      vertx.undeploy(deploymentAsyncResult.result()) {undeploymentAsyncResult ->
        undeploymentResult.set(undeploymentAsyncResult.succeeded())
      }
    }

    then:
    deploymentResult.get() == true
    undeploymentResult.get() == true
  }

  def "test the GET all books api endpoint" () {
    given:
    def deploymentResult = new BlockingVariable<Boolean>()
    def actualStatusCode = new BlockingVariable<Integer>()
    def actualBooksCount = new BlockingVariable<Integer>()
    HttpClient httpClient = vertx.createHttpClient()

    when:
    vertx.deployVerticle(new LibraryApiVerticle(), deploymentOptions) {deploymentAsyncResult ->
      deploymentResult.set(deploymentAsyncResult.succeeded())
      if (deploymentAsyncResult.succeeded()) {
        httpClient.request(HttpMethod.GET, TEST_PORT, TEST_HOST, '/api/v1/books') {httpClientRequestAsyncResult ->
          HttpClientRequest request = httpClientRequestAsyncResult.result()
          request.send { httpClientResponseAsyncResult ->
            HttpClientResponse response = httpClientResponseAsyncResult.result()
            actualStatusCode.set response.statusCode()

            response.bodyHandler { buffer ->
              List<Book> books = Json.decodeValue(buffer, List<Book>.class)
              actualBooksCount.set(books.size())
            }
          }
        }
      }
    }

    then:
    deploymentResult.get() == true
    actualStatusCode.get() == 200
    actualBooksCount.get() == 2
  }

  def "test the GET a single book api endpoint" () {
    given:
    def deploymentResult = new BlockingVariable<Boolean>()
    def actualStatusCode = new BlockingVariable<Integer>()
    def actualBookId = new BlockingVariable<Integer>()
    def actualBookTitle = new BlockingVariable<String>()

    HttpClient httpClient = vertx.createHttpClient()
    def bookId = 1

    when:
    vertx.deployVerticle(new LibraryApiVerticle(), deploymentOptions) {deploymentAsyncResult ->
      deploymentResult.set(deploymentAsyncResult.succeeded())
      if (deploymentAsyncResult.succeeded()) {
        httpClient.request(HttpMethod.GET, TEST_PORT, TEST_HOST, "/api/v1/books/$bookId") {httpClientRequestAsyncResult ->
          HttpClientRequest request = httpClientRequestAsyncResult.result()
          request.send { httpClientResponseAsyncResult ->
            HttpClientResponse response = httpClientResponseAsyncResult.result()
            actualStatusCode.set response.statusCode()

            response.bodyHandler { buffer ->
              println buffer.toString()
              Book book = Json.decodeValue(buffer, Book.class)
              actualBookId.set book.id
              actualBookTitle.set book.title
            }
          }
        }
      }
    }

    then:
    deploymentResult.get() == true
    actualStatusCode.get() == 200
    actualBookId.get() == bookId
    actualBookTitle.get() == 'Python programming'
  }
}
