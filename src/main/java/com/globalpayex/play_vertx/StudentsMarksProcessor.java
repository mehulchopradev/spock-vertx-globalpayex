package com.globalpayex.play_vertx;

import com.globalpayex.play_vertx.library.domain.Student;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import io.vertx.kafka.client.producer.RecordMetadata;

public class StudentsMarksProcessor {

  private KafkaProducer<Long, JsonObject> producer;

  public StudentsMarksProcessor(KafkaProducer producer) {
    this.producer = producer;
  }

  public Future<RecordMetadata> processMarks(Student student) {

    KafkaProducerRecord<Long, JsonObject> record = KafkaProducerRecord.create(
      "student-marks", student.getRoll(), student.toJson(), buildPartition(student));
    return this.producer.send(record);
  }

  private Integer buildPartition(Student student) {
    int partition;
    double marks = student.getMarks();
    if (marks > 100 || marks < 0) {
      partition = 0;
    } else if (marks >= 70) {
      partition = 1;
    } else if (marks >= 60) {
      partition = 2;
    } else if (marks >= 40) {
      partition = 3;
    } else {
      partition = 4;
    }

    return partition;
  }
}
