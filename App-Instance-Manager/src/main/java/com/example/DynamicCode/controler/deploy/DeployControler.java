package com.example.DynamicCode.controler.deploy;

import com.example.DynamicCode.model.entity.deploy.RemoteSerwerConfiguration;
import com.example.DynamicCode.service.deploy.test.DeployService;
import com.example.DynamicCode.service.deploymentconfig.DeployConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class DeployControler {

    @Autowired
    private DeployService deployService;

    @Autowired
    private DeployConfigService deployConfigService;

    @GetMapping("/deploy/{name}")
    public String deploy(
            @PathVariable String name,
            @RequestParam(defaultValue = ".java") String ext,
            @RequestParam(required = true) String configName,
            // DODANO: opcjonalny port forwarding — zdalny port widoczny lokalnie
            // np. localPort=8080&remotePort=8080 przekieruje ruch z localhost:8080 na zdalny:8080
            @RequestParam(required = false) Integer localPort,
            @RequestParam(required = false) Integer remotePort
    ) {
        System.out.println("Received deploy request: name=" + name + ext + ", configName=" + configName
                + (localPort != null && remotePort != null ? ", localPort=" + localPort + ", remotePort=" + remotePort : ""));
        RemoteSerwerConfiguration config = deployConfigService.getConfigByName(configName);

        if (config == null) {
            return "BŁĄD: Nie znaleziono konfiguracji o nazwie '" + configName + "'.";
        }
        if (config.getIp() == null || config.getIp().isBlank()) {
            return "BŁĄD: Konfiguracja '" + configName + "' nie ma ustawionego IP.";
        }

        System.out.println("Deploy: " + name + ext + " → " + config.getIp() + " [config: " + configName + "]");

        deployService.sendAndRunSsh(
                name + ext,
                config.getIp(),
                config.getUser(),
                config.getPass(),
                localPort,
                remotePort
        );

        String portInfo = (localPort != null && remotePort != null)
                ? " | Port forwarding: localhost:" + localPort + " → zdalny:" + remotePort
                : "";

        return "Rozpoczęto deploy '" + name + ext + "' na serwer: " + config.getIp() + portInfo;
    }
}
