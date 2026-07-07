package com.nex.nexmart.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

	public static final String TRACE_ID = "traceId";
	public static final String TRACE_HEADER = "X-Trace-Id";

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request,
	                                @NonNull HttpServletResponse response,
	                                @NonNull FilterChain filterChain) throws ServletException, IOException {
		long start = System.currentTimeMillis();
		String traceId = resolveTraceId(request);
		MDC.put(TRACE_ID, traceId);
		response.setHeader(TRACE_HEADER, traceId);
		try {
			filterChain.doFilter(request, response);
		} finally {
			long cost = System.currentTimeMillis() - start;
			log.info("operation method={} uri={} status={} costMs={}",
					request.getMethod(), request.getRequestURI(), response.getStatus(), cost);
			MDC.remove(TRACE_ID);
		}
	}

	private String resolveTraceId(HttpServletRequest request) {
		String traceId = request.getHeader(TRACE_HEADER);
		return StringUtils.hasText(traceId) ? traceId : UUID.randomUUID().toString().replace("-", "");
	}
}
