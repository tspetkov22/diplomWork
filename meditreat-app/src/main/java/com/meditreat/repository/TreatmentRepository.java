package com.meditreat.repository;

import com.meditreat.model.Treatment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Set;
import java.util.Optional;

public interface TreatmentRepository extends JpaRepository<Treatment, Long> {

    @Query("""
            SELECT t FROM Treatment t
             JOIN t.type tt
             JOIN t.symptoms s
            WHERE LOWER(s.name) = LOWER(:symptomName)
              AND s.language = :language
              AND LOWER(tt.name) = LOWER(:typeName)
              AND tt.language = :language
              AND t.language = :language
            """)
    List<Treatment> findBySymptomNameAndTypeNameAndLanguage(
            @Param("symptomName") String symptomName,
            @Param("typeName") String typeName,
            @Param("language") String language);

    @Query("""
            SELECT t FROM Treatment t
            JOIN t.type tt
            WHERE t.language = :language
            AND tt.id = :typeId
            AND EXISTS (
                SELECT 1 FROM t.symptoms s
                WHERE s.id IN :symptomIds
            )
            """)
    List<Treatment> findBySymptomIdsAndTypeIdAndLanguage(
            @Param("symptomIds") Set<Long> symptomIds,
            @Param("typeId") Long typeId,
            @Param("language") String language);

    // Find treatments by language, a specific treatment type, and containing ALL
    // specified symptom IDs
    @Query("""
                SELECT t FROM Treatment t
                JOIN t.type tt
                WHERE t.language = :language
                AND tt.id = :typeId
                AND (SELECT COUNT(s.id) FROM t.symptoms s WHERE s.id IN :symptomIds AND s.language = :language) = :symptomCount
            """)
    List<Treatment> findByAllSymptomIdsAndTypeIdAndLanguage(
            @Param("symptomIds") Set<Long> symptomIds,
            @Param("typeId") Long typeId,
            @Param("language") String language,
            @Param("symptomCount") Long symptomCount);

    List<Treatment> findByLanguage(String language);

    Optional<Treatment> findByNameAndLanguage(String name, String language);
}