package com.wks.caseengine.cpp.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.cpp.repository.CPPSRMappingRepository;
import com.wks.caseengine.cpp.dto.CPPSRMappingDTO;
import com.wks.caseengine.cpp.dto.CPPSRMappingImportDTO;

public interface CPPSRMappingService {

    CPPSRMapping saveMapping(CPPSRMapping entity);

    List<CPPSRMapping> getMappingsByFilters(
            String aopYear,
            UUID plantFkId
    );
    List<CPPSRMappingDTO> saveMappings(List<CPPSRMappingDTO> dtoList);

    void exportToExcel(OutputStream outputStream) throws Exception;

    List<CPPSRMappingImportDTO> importFromExcel(MultipartFile file) throws Exception;
}
