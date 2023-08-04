package com.globalpayex.play_vertx

import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpClientRequest
import io.vertx.core.http.HttpClientResponse
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonObject
import spock.lang.Specification
import spock.util.concurrent.BlockingVariable

class MainVerticleSpec extends Specification {

  Vertx vertx

  DeploymentOptions deploymentOptions

  static final Integer TEST_PORT = 8082

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

  /* def "testing the deployment and undeployment of the MainVerticle"() {

    given:
    def asyncConditions = new AsyncConditions(2)

    when:
    vertx.deployVerticle(new MainVerticle()) { deployAsyncResult ->
      if (deployAsyncResult.succeeded()) {
        asyncConditions.evaluate {
          assert true
        }

        vertx.undeploy(deployAsyncResult.result()) { undeployAsyncResult ->
          asyncConditions.evaluate {
            assert undeployAsyncResult.succeeded()
          }
        }
      } else {
        asyncConditions.evaluate {
          assert false
        }

        asyncConditions.evaluate {
          assert false
        }
      }
    }

    then:
    asyncConditions.await(5)
  } */

  def "testing the deployment and undeployment of the MainVerticle"() {
    given:
    def deploymentResult = new BlockingVariable<Boolean>()
    def undeploymentResult = new BlockingVariable<Boolean>()

    when:
    vertx.deployVerticle(new MainVerticle(), deploymentOptions) { deploymentAsyncResult ->
      deploymentResult.set(deploymentAsyncResult.succeeded())

      vertx.undeploy(deploymentAsyncResult.result()) {undeploymentAsyncResult ->
        undeploymentResult.set(undeploymentAsyncResult.succeeded())
      }
    }

    then:
    deploymentResult.get() == true
    undeploymentResult.get() == true
  }

  def "test the GET call for the root route"() {
    given:
    def actualDeploymentResult = new BlockingVariable<Boolean>()
    def actualResponseCode = new BlockingVariable<Integer>()
    def actualResponseContent = new BlockingVariable<String>()
    def endpoint = '/'
    def host = 'localhost'
    def port = TEST_PORT

    when:
    vertx.deployVerticle(new MainVerticle(), deploymentOptions) {deploymentAsyncResult ->
      actualDeploymentResult.set(deploymentAsyncResult.succeeded())
      if (deploymentAsyncResult.succeeded()) {
        HttpClient client = vertx.createHttpClient()
        client.request(HttpMethod.GET, port, host, endpoint) {httpClientRequestAsyncResult ->
          if (httpClientRequestAsyncResult.succeeded()) {
            HttpClientRequest httpClientRequest = httpClientRequestAsyncResult.result()
            httpClientRequest.send {httpClientResponseAsyncResult ->
              HttpClientResponse response = httpClientResponseAsyncResult.result()
              actualResponseCode.set(response.statusCode())
              response.bodyHandler {buffer ->
                String content = buffer.toString()
                actualResponseContent.set(content)
              }
            }
          }
        }
      }
    }

    then:
    actualDeploymentResult.get() == true
    actualResponseCode.get() == 200
    actualResponseContent.get() == 'hello world from vertx'
  }
}
