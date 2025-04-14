package com.wks.caseengine.repository;

import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.wks.caseengine.entity.UserScreenMapping;

@Repository
public interface UserScreenMappingRepository extends JpaRepository<UserScreenMapping, UUID>{
	


}
