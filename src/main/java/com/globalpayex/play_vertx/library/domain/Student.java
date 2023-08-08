package com.globalpayex.play_vertx.library.domain;

import io.vertx.core.json.JsonObject;

public class Student {

  private Long roll;
  private String name;
  private Character gender;
  private Double marks;

  public Student(Long roll, String name, Character gender, Double marks) {
    this.roll = roll;
    this.name = name;
    this.gender = gender;
    this.marks = marks;
  }

  public Long getRoll() {
    return roll;
  }

  public void setRoll(Long roll) {
    this.roll = roll;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Character getGender() {
    return gender;
  }

  public void setGender(Character gender) {
    this.gender = gender;
  }

  public Double getMarks() {
    return marks;
  }

  public void setMarks(Double marks) {
    this.marks = marks;
  }

  @Override
  public String toString() {
    return "Student{" +
      "roll=" + roll +
      ", name='" + name + '\'' +
      ", gender=" + gender +
      ", marks=" + marks +
      '}';
  }

  public JsonObject toJson() {
    return new JsonObject()
      .put("roll", this.roll)
      .put("name", this.name)
      .put("gender", this.gender)
      .put("marks", this.marks);
  }
}
