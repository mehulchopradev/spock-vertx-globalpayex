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
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.util.concurrent.AsyncConditions
import spock.util.concurrent.BlockingVariable
import spock.util.concurrent.BlockingVariables

@Stepwise
class LibraryApiVerticleSpec extends Specification {

  Vertx vertx

  DeploymentOptions deploymentOptions

  @Shared
  String newBookId

  static final Integer TEST_PORT = 8082
  static final String TEST_HOST = 'localhost'
  static final String TEST_DB_CONNECTION = 'mongodb+srv://admin:admin123@cluster0.bxo9m.mongodb.net/?retryWrites=true&w=majority'
  static final String TEST_DB = 'libmgmt_demo'

  def setup() {
    vertx = Vertx.vertx()
    deploymentOptions = new DeploymentOptions()
    def jsonObject = new JsonObject()
      .put('http.port', TEST_PORT)
      .put('db_name', TEST_DB)
      .put('connection_string', TEST_DB_CONNECTION)
    deploymentOptions.setConfig jsonObject

    def asyncConditions = new AsyncConditions();
    vertx.deployVerticle(new LibraryApiVerticle(), deploymentOptions) { asyncResult ->
      asyncConditions.evaluate {
        assert asyncResult.succeeded()
      }
    }

    asyncConditions.await(5)
  }

  def cleanup() {
    vertx.close()
  }

  def "test the creation of a new book"() {
    given:
    def client = vertx.createHttpClient()
    def endpoint = '/api/v1/books'
    def book = new Book("GlobalPayex story", 890, 1000f)
    def bookJsonString = Json.encodePrettily(book)
    def expectedStatusCode = new BlockingVariable<Integer>(5)
    def expectedNewBookId = new BlockingVariable<Boolean>(5)

    when:
    client.request(HttpMethod.POST, TEST_PORT, TEST_HOST, endpoint) { httpClientRequestAsyncResult ->
      def httpClientRequest = httpClientRequestAsyncResult.result()
      httpClientRequest
        .putHeader("content-type", "application/json")
        .putHeader("content-length", Integer.toString(bookJsonString.size()))
        .send(bookJsonString) { httpClientResponseAsyncResult ->
          def httpResponse = httpClientResponseAsyncResult.result()
          expectedStatusCode.set(httpResponse.statusCode())
          httpResponse.bodyHandler {buffer ->
            def newBook = Json.decodeValue(buffer.toString(), Book.class);
            this.newBookId = newBook.id
            expectedNewBookId.set(newBook.id != null)
          }
        }
    }

    then:
    expectedStatusCode.get() == 201
    expectedNewBookId.get() == true
  }

  def "test the deletion of a new book"() {
    given:
    def client = vertx.createHttpClient()
    def endpoint = "/api/v1/books/$newBookId"
    def expectedStatusCode = new BlockingVariable<Integer>(5)

    when:
    client.request(HttpMethod.DELETE, TEST_PORT, TEST_HOST, endpoint) { httpClientRequestAsyncResult ->
      def httpClientRequest = httpClientRequestAsyncResult.result()
      httpClientRequest
        .send { httpClientResponseAsyncResult ->
          def httpResponse = httpClientResponseAsyncResult.result()
          expectedStatusCode.set httpResponse.statusCode()
        }
    }

    then:
    expectedStatusCode.get() == 204
  }

  /* def "testing the deployment and undeployment of the LibraryApiVerticle"() {
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
  } */

  /* def "test the GET all books api endpoint" () {
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
  } */

  /* def "test the GET a single book api endpoint" () {
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
  } */
}
