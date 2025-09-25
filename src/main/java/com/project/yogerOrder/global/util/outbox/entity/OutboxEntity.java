package com.project.yogerOrder.global.util.outbox.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.CreatedDate;

import com.project.yogerOrder.global.util.trace.TraceUtil;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class OutboxEntity {

    @Id
    @NotBlank
    protected String eventId;

    @NotBlank
    protected String eventType;

    @NotBlank
    private String payload;

    @CreatedDate
    private LocalDateTime occurrenceTime;
    
    @Column(name="tracingspancontext")
    private String tracingSpanContext;


    protected OutboxEntity(String eventType, String payload) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.payload = payload;
        this.tracingSpanContext = TraceUtil.serializedTracingProperties();
    }
}
