package com.project.yogerOrder.global.util.trace;

import java.io.StringWriter;
import java.util.Properties;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapSetter;

public class TraceUtil {
	
	private static final TextMapSetter<Properties> PROPS_SETTER = (carrier, key, value) -> {
		if (carrier != null && key != null && value != null) {
			carrier.setProperty(key, value);
		}
	};
	
	/**
	 * 현재 Context 를 propagator로 뽑아서 Properties 직렬화 문자열로 반환.
	 * 이 문자열을 DB의 tracingspancontext 칼럼에 저장.
	 */
	public static String serializedTracingProperties() {
		Properties props = new Properties();
		GlobalOpenTelemetry.getPropagators()
			.getTextMapPropagator()
			.inject(Context.current(), props, PROPS_SETTER);
		
		// Properties -> String (Properties#store 사용: Debezium 쪽에서는 java.util.Properties.load 로 읽을 수 있음)
		try (StringWriter w = new StringWriter()) {
			props.store(w, null);
			return w.toString();
		} catch (Exception e) {
			throw new RuntimeException("failed to serialize tracing properties", e);
		}
	}
	
	public static String currentTraceparent() {
		var sc = Span.current().getSpanContext();
		if (!sc.isValid()) return null;
		
		return String.format(
			"00-%s-%s-%02x",
			sc.getTraceId(),
			sc.getSpanId(),
			sc.getTraceFlags().asByte()
		);
	}
}
