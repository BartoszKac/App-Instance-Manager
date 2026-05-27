package com.example.DynamicCode.controler.compiler;


import com.example.DynamicCode.model.entity.code.SourceCode;
import com.example.DynamicCode.service.code.test.AplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/app")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class CompilerController {


    @Autowired
    private AplicationService aplicationService;

    @PostMapping("/")
    public String index(@RequestBody ArrayList<SourceCode> code) {
        if (code == null || code.isEmpty()) return "Błąd: Brak kodu w żądaniu.";
        aplicationService.SavaCode(code);
        return code.get(0).getName() + code.get(0).getLanguage().getExtension() + " saved successfully!";
    }

    @GetMapping("/info")
    public String GetInfo() {
        return aplicationService.GetInfo();
    }

    @GetMapping("/compile/{name}")
    public String Compile(@PathVariable String name, @RequestParam(defaultValue = ".java") String ext) {
        aplicationService.CompileandRun(name, ext);
        return "Kompilacja i uruchomienie rozpoczęte dla: " + name + ext;
    }

    @GetMapping("/delete/{name}")
    public String delete(@PathVariable String name, @RequestParam(defaultValue = ".java") String ext) {
        return aplicationService.Delete(name, ext);
    }
}
