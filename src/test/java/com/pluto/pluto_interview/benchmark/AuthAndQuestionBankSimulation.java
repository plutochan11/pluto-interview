package com.pluto.pluto_interview.benchmark;

import io.gatling.core.action.builder.FeedBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.http.HttpDsl.http;

public class AuthAndQuestionBankSimulation extends Simulation {

	private final String baseUrl = System.getProperty("baseUrl", "http://localhost:8080");

	HttpProtocolBuilder httpProtocol = http
		  .baseUrl(baseUrl)
		  .acceptHeader("application/json")
		  .contentTypeHeader("application/json");

}
