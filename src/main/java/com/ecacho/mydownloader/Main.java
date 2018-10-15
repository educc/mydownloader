package com.ecacho.mydownloader;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Main {

  static final String LINE_SEPARATOR = ";";
  static final String DATA_PATH = "data";

  public static void main(String[] args) throws IOException {
    if(!isValidArgs(args)){  return; }

    OkHttpClient client = new OkHttpClient().newBuilder()
            .readTimeout(3, TimeUnit.SECONDS)
            .writeTimeout(3, TimeUnit.SECONDS)
            .connectTimeout(3, TimeUnit.SECONDS)
            .build();

    final ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(2);


    Files.createDirectories(Paths.get(DATA_PATH));
    String filename = args[0];
    BufferedReader br = new BufferedReader(new FileReader(filename));
    Utils.fromBufferedReader(br)
            .map(line -> {
              String[] parts = line.split(LINE_SEPARATOR);
              if(parts.length >= 2 && parts[1].startsWith("http")){
                return new DownFile(parts[0], parts[1]);
              }else{
                System.out.println("This line doesn't have two columns: " + line);
              }
              return new DownFile(null, null);
            })
            .filter(it -> it.url != null)
            .map(it -> {
              return downData(client, it);
            })
            .blockingForEach(it -> {
              System.out.println(it);
            });

  }

  public static String downData(OkHttpClient client, DownFile dfile){
    System.out.println(dfile.toString());
    Request rq = new Request.Builder()
            .addHeader("cookie","B=1p65eoldbrhpd&b=3&s=mv; GUCS=ASoSZ6yP; GUC=AQEBAQFbOqlcEEIerQTB&s=AQAAACFGb6TP&g=Wzlk2g; PRF=t%3DTSLA%26fin-trd-cue%3D1")
            .url(dfile.url.trim())
            .build();

    try (Response response = client.newCall(rq).execute()) {
      if (!response.isSuccessful()){
        System.out.println("Unexpected response" + response);
      }

      Files.write(Paths.get(DATA_PATH, dfile.name), response.body().bytes());
    }catch (IOException ex){
      ex.printStackTrace();
    }
    try {
      Thread.sleep(1500);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return dfile.name;
  }

  public static boolean isValidArgs(String[] args){
    boolean isValid = false;

    if(args.length > 0){
      if(Files.exists(Paths.get(args[0]))){
        isValid = true;
      }else{
        System.out.println("File doesn't exists: " + args[0]);
      }
    }else{
      System.out.println("Need params");
      System.out.println("The file.csv should have two columns by " + LINE_SEPARATOR);
      System.out.println("mydownloader.jar file.csv");
    }

    return isValid;
  }



  static class DownFile {
    String name;
    String url;

    DownFile(String name, String url){
      this.name = name;
      this.url  = url;
    }

    public String toString(){
      return this.name + " " + this.url;
    }
  }
}
