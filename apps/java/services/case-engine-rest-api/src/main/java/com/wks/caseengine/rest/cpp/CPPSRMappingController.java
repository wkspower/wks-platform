package com.wks.caseengine.rest.cpp;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.wks.caseengine.cpp.entity.CPPSRMapping;
import com.wks.caseengine.cpp.dto.CPPSRMappingDTO;
import com.wks.caseengine.cpp.service.CPPSRMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cpp")
public class CPPSRMappingController {

    private final CPPSRMappingService service;

    public CPPSRMappingController(CPPSRMappingService service) {
        this.service = service;
    }

    // POST KPI
    @PostMapping("/mapping")
    public ResponseEntity<List<CppSrMappingDTO>> saveMappings(
        @RequestBody List<CppSrMappingDTO> dtoList) {

    List<CppSrMappingDTO> response = service.saveMappings(dtoList);
    return ResponseEntity.ok(response);
}

    // GET KPI with filters
    @GetMapping("/sr-mapping")
    public ResponseEntity<List<CPPSRMapping>> getMappingsByFilters(
            @RequestParam String aopYear,
            @RequestParam UUID plantFkId
    ) {
        return ResponseEntity.ok(
                service.getMappingsByFilters(aopYear, plantFkId)
        );
    }

    // EXPORT
    @GetMapping("/mapping/export")
    public void exportToExcel(HttpServletResponse response) throws Exception {
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=cpp_sr_mapping.xlsx");

        service.exportToExcel(response.getOutputStream());
    }

    // IMPORT
    @PostMapping("/mapping/import")
    public ResponseEntity<List<CPPSRMappingExportDTO>> importFromExcel(
            @RequestParam("file") MultipartFile file) throws Exception {

        return ResponseEntity.ok(service.importFromExcel(file));
    }
}

