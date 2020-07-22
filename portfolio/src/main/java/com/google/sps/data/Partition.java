package com.google.sps.data;

import java.util.ArrayList;

public class Partition<T> {

  public ArrayList<ArrayList<T>> ofSize(ArrayList<T> list, int chunkSize) {
    ArrayList<ArrayList<T>> listOfLists = new ArrayList<>();
    int startIndex = 0;
    int endIndex = chunkSize;

    for (int i = 0; i < list.size(); i++) {
      ArrayList<T> chunkedList = list.subList(startIndex, endIndex);
      listOfLists.add(chunkedList);
      startIndex += chunkSize;
      endIndex += chunkSize;
    }

    return listOfLists;
  }
}
