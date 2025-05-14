package com.wks.caseengine.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.wks.caseengine.entity.ConfigurationType;

@Repository
public interface ConfigurationTypeRepository extends JpaRepository<ConfigurationType, UUID> {

	List<ConfigurationType> findAllByOrderByDisplaySequenceAsc();
}