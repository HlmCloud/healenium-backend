package com.epam.healenium.controller;

import com.epam.healenium.model.dto.RecordDto;
import com.epam.healenium.service.ReportService;
import com.epam.healenium.service.TenantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.nio.file.Paths;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/healenium/report")
@RequiredArgsConstructor
public class ReportController {

    @Value("${app.url.report}")
    private String reportUrl;

    private final ReportService reportService;
    private final TenantService tenantService;

    @GetMapping("/{uid}")
    public ModelAndView get(@PathVariable String uid, @RequestHeader Map<String, String> headers) {
        tenantService.setCurrentTenant(headers);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("report");
        modelAndView.addObject("dto", reportService.generate(uid));
        return modelAndView;
    }

    @GetMapping()
    public ModelAndView get(@RequestHeader Map<String, String> headers) {
        tenantService.setCurrentTenant(headers);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("report");
        modelAndView.addObject("dto", reportService.generate());
        return modelAndView;
    }

    @PostMapping("/init")
    public String init(@RequestHeader Map<String, String> headers) {
        tenantService.setCurrentTenant(headers);
        return reportService.initialize();
    }

    @PostMapping("/init/{uid}")
    public String initById(@PathVariable String uid, @RequestHeader Map<String, String> headers) {
        tenantService.setCurrentTenant(headers);
        String key = reportService.initialize(uid);
        return Paths.get(reportUrl, key).toString();
    }

    @PostMapping("/build")
    public String build(@RequestHeader("sessionKey") String key, @RequestHeader Map<String, String> headers) {
        tenantService.setCurrentTenant(headers);
        return Paths.get(reportUrl, key).toString();
    }

    @PostMapping("/data/{uid}")
    public RecordDto getRecord(@PathVariable String uid, @RequestHeader Map<String, String> headers) {
        tenantService.setCurrentTenant(headers);
        return reportService.generate(uid);
    }

    @PostMapping("/data")
    public RecordDto getRecords(@RequestHeader Map<String, String> headers) {
        tenantService.setCurrentTenant(headers);
        return reportService.generate();
    }
}