package com.example.DynamicCode.controler;

import com.example.DynamicCode.model.CodeRequest;
import com.example.DynamicCode.model.DeployConfig;
import com.example.DynamicCode.service.compiler.AplicationService;
import com.example.DynamicCode.service.deploy.DeployService;
import com.example.DynamicCode.service.deploymentconfig.DeployConfigService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/app")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class Aplication {

    @Autowired
    private AplicationService aplicationService;

    @Autowired
    private DeployService deployService;

    @Autowired
    private DeployConfigService deployConfigService;
    private final File configFile = new File("deploy-config.json");
    private final ObjectMapper objectMapper = new ObjectMapper();

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
        return code.get(0).getName() + code.get(0).getExtension() + " saved successfully!";
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

    @GetMapping("/config")
    public List<DeployConfig> getConfig() {
        return deployConfigService.getAllConfigs();
    }

    @PostMapping("/config")
    public String saveConfig(@RequestBody DeployConfig config) {
        return deployConfigService.saveNewConfig(config);
    }


    @GetMapping("/deploy/{name}")
    public String deploy(@PathVariable String name, @RequestParam(defaultValue = ".java") String ext) {
        DeployConfig config = deployConfigService.getConfigByName(name);

        if (config.getIp() == null || config.getIp().isEmpty()) {
            return "BŁĄD: Brak konfiguracji IP! Uzupełnij adres serwera w /app/config.";
        }

        System.out.println("Deploy: " + name + ext + " → " + config.getIp());

        // Przekazujemy pełną nazwę pliku (np. "Main.java") — serwis sam znajdzie powiązane pliki
        deployService.sendAndRunSsh(name + ext, config.getIp(), config.getUser(), config.getPass());

        return "Rozpoczęto deploy projektu '" + name + "' (" + ext + ") na serwer: " + config.getIp();
    }
}