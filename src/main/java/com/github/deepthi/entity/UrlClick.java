package com.github.deepthi.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "url_click")
public class UrlClick {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "clicked_at", nullable = false, updatable = false)
    private LocalDateTime clickedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "url_mapping_id", nullable = false)
    private UrlMapping urlMapping;

    protected UrlClick() {
    }

    public UrlClick(UrlMapping urlMapping) {
        this.urlMapping = urlMapping;
    }
    @PrePersist
    protected void onCreate() {
        if (clickedAt == null) {
            clickedAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getClickedAt() {
        return clickedAt;
    }
    public UrlMapping getUrlMapping() {
        return urlMapping;
    }
}
