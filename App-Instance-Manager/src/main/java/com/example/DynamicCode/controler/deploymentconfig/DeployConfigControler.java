package com.example.DynamicCode.controler.deploymentconfig;


import com.example.DynamicCode.model.entity.deploy.RemoteSerwerConfiguration;
import com.example.DynamicCode.service.deploymentconfig.DeployConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/app")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class DeployConfigControler {

    @Autowired
    private DeployConfigService deployConfigService;

    @GetMapping("/config")
    public List<RemoteSerwerConfiguration> getConfig() {
        return deployConfigService.getAllConfigs();
    }

    @PostMapping("/config")
    public String saveConfig(@RequestBody RemoteSerwerConfiguration config) {
        return deployConfigService.saveNewConfig(config);
    }
}
