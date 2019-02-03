package com.benwixen.rnfilesystem;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

public class RNFileSystem extends ReactContextBaseJavaModule {

  public RNFileSystem(ReactApplicationContext reactContext) {
    super(reactContext);
  }

  public enum Storage {
    BACKED_UP,
    SUPPORT,
    IMPORTANT,
    AUXILIARY,
    TEMPORARY
  }

  @Override
  public Map<String, Object> getConstants() {
    final Map<String, Object> constants = new HashMap<>();
    constants.put(Storage.BACKED_UP.toString(), baseDirForStorage(Storage.BACKED_UP));
    constants.put(Storage.SUPPORT.toString(), baseDirForStorage(Storage.SUPPORT));
    constants.put(Storage.IMPORTANT.toString(), baseDirForStorage(Storage.IMPORTANT));
    constants.put(Storage.AUXILIARY.toString(), baseDirForStorage(Storage.AUXILIARY));
    constants.put(Storage.TEMPORARY.toString(), baseDirForStorage(Storage.TEMPORARY));
    return constants;
  }

  @Override
  public String getName() {
    return "RNFileSystem";
  }

  private String baseDirForStorage(Storage storage) {
    switch (storage) {
      case BACKED_UP:
        return getReactApplicationContext().getFilesDir().getAbsolutePath() + "/RNFS-BackedUp";
      case SUPPORT:
        return getReactApplicationContext().getFilesDir().getAbsolutePath() + "/RNFS-SUPPORT";
      case IMPORTANT:
        return getReactApplicationContext().getFilesDir().getAbsolutePath() + "/RNFS-Important";
      case AUXILIARY:
        return getReactApplicationContext().getFilesDir().getAbsolutePath() + "/RNFS-Auxiliary";
      case TEMPORARY:
        return getReactApplicationContext().getCacheDir().getAbsolutePath() + "/RNFS-Temporary";
      default:
        throw new RuntimeException("Unrecognized storage: " + storage.toString());
    }
  }

  private void createDirectories(File destination) {
    int lastSlash = destination.getAbsolutePath().lastIndexOf("/");
    String holdingFolderPath = destination.getAbsolutePath().substring(0, lastSlash);
    File holdingFolder = new File(holdingFolderPath);
    // noinspection ResultOfMethodCallIgnored
    holdingFolder.mkdirs();
  }

  public void writeToFile(String relativePath, String content, Storage storage) throws IOException {
    String baseDir = baseDirForStorage(storage);
    File destination = new File(baseDir + "/" + relativePath);
    createDirectories(destination);

    OutputStreamWriter output = null;
    try {
      output = new OutputStreamWriter(new FileOutputStream(destination));
      output.write(content);
    } finally {
      try {
        if (output != null) {
          output.close();
        }
      } catch (IOException ignored) {}
    }
  }


  public static String convertStreamToString(InputStream is) throws Exception {
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(is));
      StringBuilder sb = new StringBuilder();
      String line = null;
      while ((line = reader.readLine()) != null) {
        sb.append(line).append("\n");
      }
      reader.close();
      return sb.toString();
      } catch (IOException ignored) {
        return null;
      }
  }


  public String readFile(String relativePath, Storage storage) throws FileNotFoundException {
    String baseDir = baseDirForStorage(storage);
    File location = new File(baseDir + "/" + relativePath);

    if (!location.exists()) {
      throw new FileNotFoundException();
    }

    FileInputStream fin = new FileInputStream(location);
    try{
    String ret = convertStreamToString(fin);
    //Make sure you close all streams.
    fin.close();    
    return ret;
} catch (Exception e) {
  return null;
} 
  }

  public boolean fileExists(String relativePath, Storage storage) {
    String baseDir = baseDirForStorage(storage);
    File location = new File(baseDir + "/" + relativePath);
    return location.isFile();
  }

  public boolean directoryExists(String relativePath, Storage storage) {
    String baseDir = baseDirForStorage(storage);
    File location = new File(baseDir + "/" + relativePath);
    return location.isDirectory();
  }

  private boolean deleteRecursive(File path) {
    boolean ret = true;
    if (path.isDirectory()){
      for (File f : path.listFiles()){
        ret = ret && deleteRecursive(f);
      }
    }
    return ret && path.delete();
  }

  public boolean deleteFileOrDirectory(String relativePath, Storage storage) {
    String baseDir = baseDirForStorage(storage);
    File location = new File(baseDir + "/" + relativePath);
    if (!location.exists()) {
      return false;
    }
    deleteRecursive(location);
    return true;
  }

  @ReactMethod
  public void writeToFile(String relativePath, String content, String storage, Promise promise) {
    try {
      writeToFile(relativePath, content, Storage.valueOf(storage));
      promise.resolve(true);
    } catch (IOException e) {
      promise.reject("ERROR", e.getMessage());
    }
  }

  @ReactMethod
  public void readFile(String relativePath, String storage, Promise promise) {
    try {
      String content = readFile(relativePath, Storage.valueOf(storage));
      promise.resolve(content);
    } catch (FileNotFoundException e) {
      promise.reject("ERROR", "File was not found: " + relativePath);
    }
  }

  @ReactMethod
  public void fileExists(String relativePath, String storage, Promise promise) {
    promise.resolve(fileExists(relativePath, Storage.valueOf(storage)));
  }

  @ReactMethod
  public void directoryExists(String relativePath, String storage, Promise promise) {
    promise.resolve(directoryExists(relativePath, Storage.valueOf(storage)));
  }

  @ReactMethod
  public void delete(String relativePath, String storage, Promise promise) {
    boolean deleted = deleteFileOrDirectory(relativePath, Storage.valueOf(storage));
    if (!deleted) {
      promise.reject("ERROR", "Could not delete item at path: " + relativePath);
    } else {
      promise.resolve(true);
    }
  }
}
