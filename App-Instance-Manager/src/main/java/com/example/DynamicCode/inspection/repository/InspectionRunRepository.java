package com.example.DynamicCode.inspection.repository;

import com.example.DynamicCode.inspection.entity.InspectionRun;
import com.example.DynamicCode.inspection.enums.InspectionState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface InspectionRunRepository extends JpaRepository<InspectionRun, Long> {

    Optional<InspectionRun> findFirstByServerIdOrderByIdDesc(Long serverId);

    Optional<InspectionRun> findFirstByServerIdAndStateOrderByIdDesc(Long serverId, InspectionState state);

    Optional<InspectionRun> findFirstByServerIdAndStateIn(Long serverId, Collection<InspectionState> states);

    List<InspectionRun> findByStateIn(Collection<InspectionState> states);

    List<InspectionRun> findByServerId(Long serverId);

    long countByStateIn(Collection<InspectionState> states);

    @Query("select r from InspectionRun r where r.id in (select max(x.id) from InspectionRun x group by x.serverId)")
    List<InspectionRun> findLatestPerServer();

    @Query("select r from InspectionRun r where r.id in "
            + "(select max(x.id) from InspectionRun x where x.state = com.example.DynamicCode.inspection.enums.InspectionState.DONE group by x.serverId)")
    List<InspectionRun> findLatestDonePerServer();
}
