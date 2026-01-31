package com.wks.caseengine.service;

import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.dto.PeopleInitiativeDTO;
import com.wks.caseengine.dto.PlantTeamDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;

public interface PeopleInitiativeService {
	
	public AOPMessageVM getPlantTeam(String plantId,String year);
	public AOPMessageVM deletePlantTeam(String id);
	public AOPMessageVM savePlantTeam( String year, String plantFKId, List<PlantTeamDTO> plantTeamDTOs);
	public AOPMessageVM getPeopleInitiative(String plantId,String year);
	public AOPMessageVM deletePeopleInitiative(String id);
	public AOPMessageVM savePeopleInitiative( String year, String plantFKId, List<PeopleInitiativeDTO> peopleInitiativeDTOs);
	public byte[] exportPeopleInitiative(String year, String plantFKId,boolean isAfterSave,List<PeopleInitiativeDTO> dtoList);
	public AOPMessageVM importPeopleInitiative(String year,UUID plantId,MultipartFile file);
	public byte[] exportPlantTeam(String year, String plantFKId,boolean isAfterSave,List<PlantTeamDTO> dtoList);
	public AOPMessageVM importPlantTeam(String year,UUID plantId,MultipartFile file);
}
