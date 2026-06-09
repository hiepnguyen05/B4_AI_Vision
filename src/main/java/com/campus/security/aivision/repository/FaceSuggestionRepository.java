package com.campus.security.aivision.repository;

import com.campus.security.aivision.entity.FaceSuggestion;
import com.campus.security.aivision.entity.FaceSuggestion.SuggestionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FaceSuggestionRepository extends JpaRepository<FaceSuggestion, Long> {

    List<FaceSuggestion> findByFaceMatchId(Long faceMatchId);

    List<FaceSuggestion> findByStatus(SuggestionStatus status);

    List<FaceSuggestion> findBySuggestedPersonId(String personId);

    @Query("SELECT COUNT(s) FROM FaceSuggestion s WHERE s.status = :status")
    Long countByStatus(@Param("status") SuggestionStatus status);

    @Query("SELECT s FROM FaceSuggestion s WHERE s.status = 'PENDING' ORDER BY s.suggestionConfidence DESC")
    List<FaceSuggestion> findPendingSuggestions();

    @Query("SELECT AVG(s.suggestionConfidence) FROM FaceSuggestion s WHERE s.status = 'ACCEPTED'")
    Double averageAcceptedConfidence();
}
