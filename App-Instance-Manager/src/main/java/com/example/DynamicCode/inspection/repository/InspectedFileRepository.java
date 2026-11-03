package com.example.DynamicCode.inspection.repository;

import com.example.DynamicCode.inspection.entity.InspectedFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

public interface InspectedFileRepository extends JpaRepository<InspectedFile, Long> {

    @Query("select f from InspectedFile f where f.runId = :runId and f.parent = :parent "
            + "order by case when f.type = 'd' then 0 else 1 end, f.name")
    Page<InspectedFile> findChildren(@Param("runId") Long runId, @Param("parent") String parent, Pageable pageable);

    @Query("select f from InspectedFile f where f.runId = :runId and lower(f.path) like :pattern escape '\\' order by f.path")
    Page<InspectedFile> search(@Param("runId") Long runId, @Param("pattern") String pattern, Pageable pageable);

    long countByRunIdAndPathIn(Long runId, Collection<String> paths);
}
