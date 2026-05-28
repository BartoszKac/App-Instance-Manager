package com.example.DynamicCode.databaseservice;


import java.util.List;

public interface DataBaseProvider<T>{
    List<T> getAllFilesFromMainClass(Long idMainClass);
}
