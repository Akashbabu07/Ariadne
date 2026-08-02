package com.Ariadne.shared.response;

/**
 * Every controller in this project returns responses wrapped in {@link ApiResponse}.
 * That class is deliberately construction-locked (private constructor, static
 * factories) for producers, which means Jackson can't deserialize INTO it on the
 * consumer side (no default constructor, no matching @JsonCreator).
 * <p>
 * Feign clients and RestClient callers that need to parse another service's
 * response should declare this record as the return/body type instead, then call
 * {@link #data()} to unwrap. Records get Jackson-compatible deserialization for free.
 */
public record ApiEnvelope<T>(boolean success, T data) {}
