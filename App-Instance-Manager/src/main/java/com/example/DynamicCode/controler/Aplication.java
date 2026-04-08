package com.example.DynamicCode.controler;

import com.example.DynamicCode.model.CodeRequest;
import com.example.DynamicCode.service.AplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/app")
@CrossOrigin(origins = "http://localhost:5173")
public class Aplication {

    @Autowired
    AplicationService aplicationService;



    @PostMapping("/test")
    public String debugIndex(@RequestBody String rawJson) {
        System.out.println("--- OTRZYMANY JSON ---");
        System.out.println(rawJson);
        System.out.println("----------------------");

        // Na razie zwracamy info, żeby nie wywaliło frontu
        return "Odebrano: " + rawJson;
    }
    @PostMapping("/")
        public String index(@RequestBody ArrayList<CodeRequest> code) {
        System.out.println("Received code: " + code.toString());
        aplicationService.SavaCode(code);
        return code.get(0) + " saved successfully!";
    }
        @GetMapping("/info")
        public String GetInfo(){
       return aplicationService.GetInfo();
        }

        @GetMapping("/compile/{name}")
        public String Compile(@PathVariable String name){
             aplicationService.CompileandRun(name);
            return "Kompilacja i uruchomienie rozpoczęte dla: " + name;
        }
    @GetMapping("/delete/{name}")
    public String delete(@PathVariable String name){
        aplicationService.Delete(name);
        return "Kompilacja i uruchomienie rozpoczęte dla: " + name;
    }


}
