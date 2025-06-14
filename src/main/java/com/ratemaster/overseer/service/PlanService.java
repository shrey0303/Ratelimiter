package com.ratemaster.overseer.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ratemaster.overseer.dto.PlanResponseDto;
import com.ratemaster.overseer.dto.PlanUpdationRequestDto;
import com.ratemaster.overseer.entity.UserPlanMapping;
import com.ratemaster.overseer.exception.InvalidPlanException;
import com.ratemaster.overseer.repository.PlanRepository;
import com.ratemaster.overseer.repository.UserPlanMappingRepository;
import com.ratemaster.overseer.utility.AuthenticatedUserIdProvider;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlanService {

	private final PlanRepository planRepository;
	private final RateLimitingService rateLimitingService;
	private final UserPlanMappingRepository userPlanMappingRepository;
	private final AuthenticatedUserIdProvider authenticatedUserIdProvider;

	/**
	 * Updates the subscription plan for a user and deactivates their current plan
	 * in the system. The rate-limit corresponding to the previous plan is cleared
	 * on successful plan updation.
	 * 
	 * If the provided plan-id to update matches the user's current plan-id, then no
	 * changes in the datasource is performed and method execution is halted.
	 *
	 * @param planUpdationRequest containing user's new plan details.
	 * @throws IllegalArgumentException if provided argument is <code>null</code>.
	 * @throws InvalidPlanException if no plan exists with provided-id.
	 */
	public void update(@NonNull final PlanUpdationRequestDto planUpdationRequest) {
		final var planId = planUpdationRequest.getPlanId();
		final var isPlanIdValid = planRepository.existsById(planId);
		if (Boolean.FALSE.equals(isPlanIdValid)) {
			throw new InvalidPlanException("No plan exists in the system with provided-id");
		}

		final var userId = authenticatedUserIdProvider.getUserId();
		final var isExistingUserPlan = userPlanMappingRepository.isActivePlan(userId, planId);
		if (Boolean.TRUE.equals(isExistingUserPlan)) {
			return;
		}

		userPlanMappingRepository.deactivateCurrentPlan(userId);

		final var newPlan = new UserPlanMapping();
		newPlan.setUserId(userId);
		newPlan.setPlanId(planId);
		userPlanMappingRepository.save(newPlan);

		rateLimitingService.reset(userId);
	}

	/**
	 * Retrieves all available subscription plans.
	 *
	 * @return List of PlanResponseDto containing details of each available plan.
	 */
	public List<PlanResponseDto> retrieve() {
		return planRepository.findAll()
				.stream()
				.map(plan -> PlanResponseDto.builder()
						.id(plan.getId())
						.name(plan.getName())
						.limitPerHour(plan.getLimitPerHour())
						.build())
				.toList();
	}

}