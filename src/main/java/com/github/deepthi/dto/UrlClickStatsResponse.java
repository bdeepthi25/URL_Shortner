package com.github.deepthi.dto;

import java.util.List;

public class UrlClickStatsResponse {

	 private final String shortCode;
	    private final long totalClicks;
	    private final List<DailyClickStatsResponse> dailyClicks;

	    public UrlClickStatsResponse(String shortCode, long totalClicks,  List<DailyClickStatsResponse> dailyClicks) {
	        this.shortCode = shortCode;
	        this.totalClicks = totalClicks;
	        this.dailyClicks = dailyClicks;
	    }

	    public String getShortCode() {
	        return shortCode;
	    }

	    public long getTotalClicks() {
	        return totalClicks;
	    }

	    public List<DailyClickStatsResponse> getDailyClicks() {
	        return dailyClicks;
	    }
}
