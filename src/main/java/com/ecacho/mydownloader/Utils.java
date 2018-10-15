package com.ecacho.mydownloader;

import io.reactivex.Observable;

import java.io.BufferedReader;

public class Utils {

  public static Observable<String> fromBufferedReader(BufferedReader reader) {
    return Observable.create(e -> {
      String line;
      while (!e.isDisposed() && (line = reader.readLine()) != null) {
        e.onNext(line);
      }
      e.onComplete();
    });
  }
}
