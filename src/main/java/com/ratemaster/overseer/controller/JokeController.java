package com.ratemaster.overseer.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ratemaster.overseer.dto.ExceptionResponseDto;
import com.ratemaster.overseer.dto.JokeResponseDto;
import com.ratemaster.overseer.utility.JokeGenerator;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "Joke Generator", description = "Endpoint for generating random unfunny joke")
public class JokeController {

	private final JokeGenerator jokeGenerator;

	@GetMapping(value = "/joke", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Generates a random unfunny joke")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "Successfully generated random unfunny joke",
					headers = @Header(name = "X-Rate-Limit-Remaining", description = "The number of remaining API invocations available with the user after processing the request.", required = true, 
							schema = @Schema(type = "integer"))),
			@ApiResponse(responseCode = "429", description = "API rate limit exhausted",
					headers = @Header(name = "X-Rate-Limit-Retry-After-Seconds", description = "Wait period in seconds before the user can invoke the API endpoint", required = true, 
							schema = @Schema(type = "integer")),
					content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class))) })
	public ResponseEntity<JokeResponseDto> generate() {
		final var response = jokeGenerator.generate();
		return ResponseEntity.ok(response);
	}

}