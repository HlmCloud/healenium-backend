package com.epam.healenium.controller;

import com.epam.healenium.model.dto.*;
import com.epam.healenium.service.HealingService;
import com.epam.healenium.service.SelectorService;
import com.epam.healenium.service.TenantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.epam.healenium.constants.Constants.SESSION_KEY_V1;
import static com.epam.healenium.constants.Constants.SESSION_KEY_V2;

@Slf4j(topic = "healenium")
@RestController
@RequestMapping("/healenium")
@RequiredArgsConstructor
public class HealingController {

    private final HealingService healingService;
    private final SelectorService selectorService;
    private final TenantService tenantService;

    /**
     * Saving information about a successfully found item
     *
     * @param request
     */
    @PostMapping()
    public void save(@Valid @RequestBody SelectorRequestDto request, @RequestHeader Map<String, String> headers) {
        tenantService.setCurrentTenant(headers);
        log.info("[Save Elements] Request: {}({})", request.getType(), request.getLocator());
        selectorService.saveSelector(request);
    }

    /**
     * Getting last valid path for provided request
     *
     * @param dto
     * @return
     */
    @GetMapping()
    public ReferenceElementsDto getReferenceElements(RequestDto dto, @RequestHeader Map<String, String> headers) {
        tenantService.setCurrentTenant(headers);
        log.info("[Get Reference] Request: {})", dto);
        ReferenceElementsDto referenceElements = selectorService.getReferenceElements(dto);
        log.debug("[Get Reference] Response: {})", referenceElements);
        return referenceElements;
    }

    /**
     * Getting all saved selectors and config
     *
     * @return
     */
    @GetMapping("/elements")
    public ConfigSelectorDto getElements(@RequestHeader Map<String, String> headers) {
        tenantService.setCurrentTenant(headers);
        ConfigSelectorDto configSelectors = selectorService.getConfigSelectors();
        log.debug("[Get Elements] Response: {}", configSelectors);
        return configSelectors;
    }

    /**
     * Saving heal result for specific selector
     *
     * @param dto
     * @param headers
     * @return
     */
    @PostMapping("/healing")
    public void save(@Valid @RequestBody List<HealingRequestDto> dto,
                     @RequestHeader Map<String, String> headers) {
        tenantService.setCurrentTenant(headers);
        log.debug("[Save Healing] Request: {}. Headers: {}", dto, headers);
        if (StringUtils.isEmpty(headers.get(SESSION_KEY_V1)) && StringUtils.isEmpty(headers.get(SESSION_KEY_V2))) {
            log.warn("Session key is not present. Current issue would not be presented in any reports, but still available in replacement!");
        }
        dto.forEach(requestDto -> healingService.saveHealing(requestDto, headers));
    }

    /**
     * Restore session to parse dom for proxy type
     *
     * @param dto
     * @return
     */
    @PostMapping("/session")
    public void session(@Valid @RequestBody SessionDto dto, @RequestHeader Map<String, String> headers) {
        tenantService.setCurrentTenant(headers);
        log.debug("[Restore Session] Request: {}", dto);
    }

    /**
     * Getting healing with their results for provided request
     *
     * @param dto
     * @return
     */
    @GetMapping("/healing")
    public Set<HealingDto> getHealings(RequestDto dto, @RequestHeader Map<String, String> headers) {
        tenantService.setCurrentTenant(headers);
        log.debug("[Get Healing] Request: {}", dto);
        Set<HealingDto> healings = healingService.getHealings(dto);
        log.debug("[Get Healing] Response: {}", healings);
        return healings;
    }

    /**
     * Getting healing results for selector
     *
     * @param dto
     * @return
     */
    @GetMapping("/healing/results")
    public Set<HealingResultDto> getResults(RequestDto dto, @RequestHeader Map<String, String> headers) {
        tenantService.setCurrentTenant(headers);
        log.debug("[Get Healing Result] Request: {}", dto);
        Set<HealingResultDto> healingResults = healingService.getHealingResults(dto);
        log.debug("[Get Healing Result] Response: {}", healingResults);
        return healingResults;
    }

    /**
     * Setting status of healing
     *
     * @param dto
     * @return
     */
    @PostMapping("/healing/success")
    public void successHealing(@Valid @RequestBody RecordDto.ReportRecord dto, @RequestHeader Map<String, String> headers) {
        tenantService.setCurrentTenant(headers);
        log.debug("[Set Healing Status] Request: {}", dto);
        healingService.saveSuccessHealing(dto);
    }

    /**
     * Getting all selectors for selector.html
     *
     * @return
     */
    @GetMapping("/selectors")
    public ModelAndView get(@RequestHeader Map<String, String> headers) {
        tenantService.setCurrentTenant(headers);
        log.debug("[Get Selector Page]");
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("selector");
        modelAndView.addObject("dto", selectorService.getAllSelectors());
        return modelAndView;
    }

    /**
     * Setting status (enable/disable) to healing
     *
     * @param dto
     * @return
     */
    @PostMapping("/selector/status")
    public void setSelectorStatus(@Valid @RequestBody SelectorDto dto, @RequestHeader Map<String, String> headers) {
        tenantService.setCurrentTenant(headers);
        log.debug("[Set Selector Status] Request: {}", dto);
        selectorService.setSelectorStatus(dto);
    }

    @GetMapping("/migrate")
    public ModelAndView migrate(@RequestHeader Map<String, String> headers) {
        tenantService.setCurrentTenant(headers);
        log.debug("[Migrate Selectors]");
        selectorService.migrate();
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("index");
        modelAndView.addObject("message", "The migration of selectors was successful.");
        return modelAndView;
    }

    @PostMapping("/getId")
    public String updateSelectorId(@Valid @RequestParam String selector, @RequestHeader Map<String, String> headers) {
        tenantService.setCurrentTenant(headers);
        return healingService.updateSelectorId(selector);
    }

    @GetMapping("/tenant/{schema}")
    public void addTenant(@PathVariable String schema) {
        tenantService.addTenant(schema);
    }

    @DeleteMapping("/tenant/{schema}")
    public void deleteTenant(@PathVariable String schema) {
        tenantService.deleteTenant(schema);
    }
}