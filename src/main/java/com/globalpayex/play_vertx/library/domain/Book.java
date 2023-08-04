package com.globalpayex.play_vertx.library.domain;

import java.util.concurrent.atomic.AtomicInteger;

public class Book {

  private static AtomicInteger atomicInteger = new AtomicInteger();

  private Integer id;

  private String title;

  private Integer pages;

  private Float price;

  public Book() {
    this.id = atomicInteger.getAndIncrement();
  }

  public Book(String title, Integer pages, Float price) {
    this();
    this.title = title;
    this.pages = pages;
    this.price = price;
  }

  public Integer getId() {
    return id;
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
}
