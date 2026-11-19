package com.github.deepthi.dto;

import java.time.LocalDate;

public class DailyClickStatsResponse {
	 private final LocalDate date;
	    private final long clicks;

	    public DailyClickStatsResponse(LocalDate date, long clicks) {
	        this.date = date;
	        this.clicks = clicks;
	    }

	    public LocalDate getDate() {
	        return date;
	    }

	    public long getClicks() {
	        return clicks;
	    }
}
