package com.ratemaster.overseer.configuration;

import com.ratemaster.overseer.filter.RateLimitFilter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for excluding specific private API endpoints from rate limit
 * enforcement, allowing them to be accessed without restriction regardless of
 * the user's current rate limit plan.
 * 
 * When applied to a controller method, requests to that method will not be
 * subject to rate limiting by the {@link RateLimitFilter}.
 * 
 * @see RateLimitFilter
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BypassRateLimit {

}
