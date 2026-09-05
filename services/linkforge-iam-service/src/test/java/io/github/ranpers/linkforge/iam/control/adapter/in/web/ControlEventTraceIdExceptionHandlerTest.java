package io.github.ranpers.linkforge.iam.control.adapter.in.web;

import io.github.ranpers.linkforge.iam.control.domain.InvalidControlEventTraceIdException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ControlEventTraceIdExceptionHandlerTest {

    @Test
    void mapsInvalidTraceIdToBadRequest() {
        ProblemDetail detail = new ControlEventTraceIdExceptionHandler()
                .invalid(new InvalidControlEventTraceIdException("invalid traceId"));

        assertEquals(HttpStatus.BAD_REQUEST.value(), detail.getStatus());
        assertEquals("invalid traceId", detail.getDetail());
    }
}
