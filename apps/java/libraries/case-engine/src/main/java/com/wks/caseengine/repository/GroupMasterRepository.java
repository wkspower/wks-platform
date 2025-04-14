package com.wks.caseengine.repository;

import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.wks.caseengine.entity.GroupMaster;

@Repository
public interface GroupMasterRepository extends JpaRepository<GroupMaster, UUID>{
}
