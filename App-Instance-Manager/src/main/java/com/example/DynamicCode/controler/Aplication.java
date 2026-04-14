package com.example.DynamicCode.controler;

import com.example.DynamicCode.model.CodeRequest;
import com.example.DynamicCode.service.AplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/app")
@CrossOrigin(origins = "http://localhost:5173") // Dopasuj do swojego frontu
public class Aplication {

    @Autowired
    private AplicationService aplicationService;

    @PostMapping("/test")
    public String debugIndex(@RequestBody String rawJson) {
        System.out.println("--- OTRZYMANY JSON ---");
        System.out.println(rawJson);
        System.out.println("----------------------");
        return "Odebrano surowy JSON";
    }

    @PostMapping("/")
    public String index(@RequestBody ArrayList<CodeRequest> code) {
        if (code == null || code.isEmpty()) return "Błąd: Brak kodu w żądaniu.";

        aplicationService.SavaCode(code);
        // Zwracamy nazwę pierwszego pliku i informację o sukcesie
        return code.get(0).getName() + code.get(0).getExtension() + " saved successfully!";
    }

 @GetMapping("/info")
    public String GetInfo() {
        return aplicationService.GetInfo();
    }

    /**
     * Zaktualizowana metoda kompilacji.
     * Przyjmuje rozszerzenie jako parametr zapytania, np. /compile/HelloWorld?ext=.py
     */
    @GetMapping("/compile/{name}")
    public String Compile(@PathVariable String name, @RequestParam(defaultValue = ".java") String ext) {
        aplicationService.CompileandRun(name, ext);
        return "Kompilacja i uruchomienie rozpoczęte dla: " + name + ext;
    }

    /**
     * Zaktualizowana metoda usuwania.
     * Przyjmuje rozszerzenie jako parametr zapytania, np. /delete/HelloWorld?ext=.cpp
     */
    @GetMapping("/delete/{name}")
    public String delete(@PathVariable String name, @RequestParam(defaultValue = ".java") String ext) {
        return aplicationService.Delete(name, ext);
    }
}