package com.wks.caseengine.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wks.caseengine.dto.AOPSummaryDTO;
import com.wks.caseengine.entity.AOPSummary;
import com.wks.caseengine.exception.RestInvalidArgumentException;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.AOPSummaryRepository;

@Service
public class AOPSummaryServiceImpl implements AOPSummaryService {

	@Autowired
	private AOPSummaryRepository aopSummaryRepository;

	@Override
	public AOPMessageVM saveAOPSummary(String plantId, String aopYear, AOPSummaryDTO aopSummaryDTO) {
		try {
			UUID plantUUID = UUID.fromString(plantId);

			// Search existing record by plantId and aopYear
			AOPSummary existingSummary = aopSummaryRepository.findByPlantFkIdAndAopYear(plantUUID, aopYear);

			if (existingSummary == null) {
				existingSummary = new AOPSummary();
				existingSummary.setPlantFkId(plantUUID);
				existingSummary.setAopYear(aopYear);
			}

			existingSummary.setSummary(aopSummaryDTO.getSummary());
			existingSummary.setUpdatedBy("system_user");
			existingSummary.setUpdatedDateTime(LocalDateTime.now());

			aopSummaryRepository.save(existingSummary);

			AOPMessageVM aopMessageVM = new AOPMessageVM();
			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Summary saved successfully.");

			Map<String, Object> result = new HashMap<>();
			result.put("id", existingSummary.getId());
			aopMessageVM.setData(result);

			return aopMessageVM;

		} catch (Exception ex) {
			throw new RestInvalidArgumentException("summary", ex);
		}
	}

	@Override
	public AOPMessageVM getAOPSummary(String plantId, String aopYear) {
		try {
			UUID plantUUID = UUID.fromString(plantId);
			AOPSummary existingSummary = aopSummaryRepository.findByPlantFkIdAndAopYear(plantUUID, aopYear);

			Map<String, Object> result = new HashMap<>();
			result.put("summary", existingSummary != null ? existingSummary.getSummary() : "");

			AOPMessageVM aopMessageVM = new AOPMessageVM();
			aopMessageVM.setCode(200);
			aopMessageVM.setMessage("Summary retrieved successfully.");
			aopMessageVM.setData(result);

			return aopMessageVM;

		} catch (Exception ex) {
			throw new RestInvalidArgumentException("summary", ex);
		}
	}

}
