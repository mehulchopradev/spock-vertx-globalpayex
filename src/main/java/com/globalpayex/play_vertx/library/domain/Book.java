package com.globalpayex.play_vertx.library.domain;

import io.vertx.core.json.JsonObject;

import java.util.concurrent.atomic.AtomicInteger;

public class Book {

  // private static AtomicInteger atomicInteger = new AtomicInteger();

  private String id;

  private String title;

  private Integer pages;

  private Float price;

  public Book() {
    this.id = "";
  }

  public Book(String title, Integer pages, Float price) {
    this();
    this.title = title;
    this.pages = pages;
    this.price = price;
  }

  public Book(String id, String title, Integer pages, Float price) {
    this.id = id;
    this.title = title;
    this.pages = pages;
    this.price = price;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Integer getPages() {
    return pages;
  }

  public void setPages(Integer pages) {
    this.pages = pages;
  }

  public Float getPrice() {
    return price;
  }

  public void setPrice(Float price) {
    this.price = price;
  }

  @Override
  public String toString() {
    return "Book{" +
      "id=" + id +
      ", title='" + title + '\'' +
      ", pages=" + pages +
      ", price=" + price +
      '}';
  }

  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject()
      .put("title", this.title)
      .put("price", this.price)
      .put("pages", this.pages);

    if (this.id != null && !this.id.isEmpty()) {
      jsonObject.put("_id", this.id);
    }

    return jsonObject;
  }
}
