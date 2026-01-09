package com.wks.caseengine.repository.tcs;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
//import java.util.Optional;
//import java.util.List;

import com.wks.caseengine.entity.tcs.TCSUnitCapacity;

@Repository
public interface TCSUnitCapacityRepository extends JpaRepository<TCSUnitCapacity, UUID> {

    // List<TCSUnitCapacity> findByParticulates(String particulates);

    // List<TCSUnitCapacity> findByUom(String uom);

    // Optional<TCSUnitCapacity> findByParticulatesAndUom(String particulates, String uom);
}

