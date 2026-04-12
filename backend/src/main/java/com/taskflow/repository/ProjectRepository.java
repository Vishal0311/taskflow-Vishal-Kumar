package com.taskflow.repository;

import com.taskflow.model.Project;
import com.taskflow.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    @Query("""
        SELECT DISTINCT p FROM Project p
        WHERE p.owner = :user
        OR EXISTS (
            SELECT t FROM Task t
            WHERE t.project = p AND t.assignee = :user
        )
    """)
    Page<Project> findAllAccessibleByUser(@Param("user") User user, Pageable pageable);

    boolean existsByIdAndOwner(UUID id, User owner);
}