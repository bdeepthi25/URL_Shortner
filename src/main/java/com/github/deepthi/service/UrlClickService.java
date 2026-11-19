package com.github.deepthi.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.github.deepthi.dto.DailyClickStatsResponse;
import com.github.deepthi.entity.UrlClick;
import com.github.deepthi.entity.UrlMapping;
import com.github.deepthi.repository.UrlClickRepository;

@Service
public class UrlClickService {

	 private final UrlClickRepository urlClickRepository;

	    public UrlClickService(UrlClickRepository urlClickRepository) {
	        this.urlClickRepository = urlClickRepository;
	    }

	    public UrlClick recordClick(UrlMapping urlMapping) {
	        UrlClick click = new UrlClick(urlMapping);
	        return urlClickRepository.save(click);
	    }


		public long getTotalClicks(String shortCode) {
		    return urlClickRepository.countByUrlMapping_ShortCode(shortCode);
		}

		   public List<DailyClickStatsResponse> getDailyClicks(String shortCode) {
		        return urlClickRepository.findDailyClicksByShortCode(shortCode)
		                .stream()
		                .map(dailyClick -> new DailyClickStatsResponse(
		                        dailyClick.getClickDate(),
		                        dailyClick.getClicks()))
		                .toList();
		    }

}
