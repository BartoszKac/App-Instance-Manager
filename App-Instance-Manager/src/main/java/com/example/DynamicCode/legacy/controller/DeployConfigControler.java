package com.example.DynamicCode.legacy.controller;


import com.example.DynamicCode.deploy.entity.RemoteSerwerConfiguration;
import com.example.DynamicCode.legacy.service.DeployConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/app")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Profile("dev")
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
