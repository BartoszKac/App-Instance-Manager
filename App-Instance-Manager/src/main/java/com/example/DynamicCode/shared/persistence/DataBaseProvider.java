package com.example.DynamicCode.shared.persistence;


import java.util.List;

public interface DataBaseProvider<T>{
    List<T> getAllFilesFromMainClass(Long idMainClass);
}
