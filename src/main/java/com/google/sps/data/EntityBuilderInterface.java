package com.google.sps.data;

public interface EntityBuilderInterface {
  EntityBuilderInterface setID(long id);

  EntityBuilderInterface setName(String name);

  long getID();

  String getName();
}
