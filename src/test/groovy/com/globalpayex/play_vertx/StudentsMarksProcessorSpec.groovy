package com.globalpayex.play_vertx

import com.globalpayex.play_vertx.library.domain.Student
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.kafka.client.producer.KafkaProducer
import io.vertx.kafka.client.serialization.JsonObjectSerializer
import org.apache.kafka.clients.producer.MockProducer
import org.apache.kafka.common.serialization.LongSerializer
import spock.lang.Specification
import spock.util.concurrent.BlockingVariable

class StudentsMarksProcessorSpec extends Specification {

  Vertx vertx

  MockProducer<Long, JsonObject> mockProducer

  def setup() {
    vertx = Vertx.vertx()
    mockProducer = new MockProducer<>(true, new LongSerializer(), new JsonObjectSerializer())
  }

  def cleanup() {
    vertx.close()
  }

  def "test the processMarks producer function"() {
    given:
    def producer = KafkaProducer.create(vertx, mockProducer)
    def student1 = new Student(10, "mehul", 'm' as Character, 78)
    def student2 = new Student(14, "jane", 'f' as Character, 45)
    def studentMarksProcessor = new StudentsMarksProcessor(producer)
    def producerHistory = new BlockingVariable<Integer>()
    def partition1 = new BlockingVariable<Integer>()
    def partition2 = new BlockingVariable<Integer>()

    when:
    studentMarksProcessor.processMarks(student1)
      .onSuccess {
        studentMarksProcessor.processMarks(student2)
          .onSuccess {
            producerHistory.set mockProducer.history().size()

            partition1.set mockProducer.history().get(0).partition()
            partition2.set mockProducer.history().get(1).partition()
          }
      }

    then:
    producerHistory.get() == 2
    partition1.get() == 1
    partition2.get() == 3
  }
}
