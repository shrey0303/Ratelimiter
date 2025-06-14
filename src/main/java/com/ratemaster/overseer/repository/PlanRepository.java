package com.ratemaster.overseer.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ratemaster.overseer.entity.Plan;

@Repository
public interface PlanRepository extends JpaRepository<Plan, UUID> {

}