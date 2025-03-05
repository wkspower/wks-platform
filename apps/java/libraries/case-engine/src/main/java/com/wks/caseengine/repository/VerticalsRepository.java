package com.wks.caseengine.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wks.caseengine.entity.Verticals;

public interface VerticalsRepository extends JpaRepository<Verticals, UUID>{

}
