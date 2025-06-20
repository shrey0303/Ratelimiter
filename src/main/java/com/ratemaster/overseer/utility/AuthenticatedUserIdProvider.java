package com.ratemaster.overseer.utility;

import java.util.Optional;
import java.util.UUID;

import com.ratemaster.overseer.filter.JwtAuthenticationFilter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Utility class dedicated to provide authenticated user's ID as stored in the
 * DataSource in UUID format which uniquely identifies the user in the system.
 * This is fetched from the principal in security context, where it is stored in
 * by the {@link JwtAuthenticationFilter} during HTTP
 * request evaluation through the filter chain.
 * 
 * @see JwtAuthenticationFilter
 */
@Component
public class AuthenticatedUserIdProvider {
	
	/**
	 * Retrieves ID corresponding to the authenticated user from the security
	 * context.
	 * 
	 * @return Unique ID (UUID formatted) corresponding to the authenticated user.
	 * @throws IllegalStateException if the method is invoked when a request was
	 *                               destined to a public API endpoint and did not pass
	 *                               the JwtAuthenticationFilter
	 */
	public UUID getUserId() {
		return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
		        .map(Authentication::getPrincipal)
		        .filter(UUID.class::isInstance)
		        .map(UUID.class::cast)
		        .orElseThrow(IllegalStateException::new);
	}
	
	/**
	 * Checks whether the security context is populated with a valid authentication
	 * object.
	 *
	 * @return {@code true} if an authentication context is available, {@code false}
	 *         otherwise.
	 */
	public boolean isAvailable() {
		final var authentication = SecurityContextHolder.getContext().getAuthentication();
		return Optional.ofNullable(authentication).isPresent();
	}

}