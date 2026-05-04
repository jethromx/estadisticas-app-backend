package com.lottery.api.infrastructure.adapter.persistence;

import com.lottery.api.infrastructure.adapter.persistence.entity.UserActivityLogEntity;
import com.lottery.api.infrastructure.adapter.persistence.projection.ActionCountProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserActivityLogJpaRepository extends JpaRepository<UserActivityLogEntity, Long> {

    @Query("SELECT e.action as action, COUNT(e) as count FROM UserActivityLogEntity e GROUP BY e.action")
    List<ActionCountProjection> countByAction();
}
