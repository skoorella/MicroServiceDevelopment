package com.koorella.moviecatalogservice.resources;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import com.koorella.moviecatalogservice.models.CatalogItem;
import com.koorella.moviecatalogservice.models.Movie;
import com.koorella.moviecatalogservice.models.UserRating;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

@RestController
@RequestMapping("/catalog")
public class MovieCatalogResource {

	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private WebClient.Builder builder;

	/**
	 * 
	 * @param userId
	 * @return
	 */
	@RequestMapping("/{userId}")
	@HystrixCommand(fallbackMethod = "getFallbackCatalog")
	public List<CatalogItem> getCatalog(@PathVariable("userId") String userId) {
		
		UserRating userRatings = restTemplate.getForObject("http://MOVIE-RATINGS-DATA-SERVICE/ratingsdata/users/"+userId, UserRating.class);

		return userRatings.getRatings().stream().map(rating -> {
			Movie movie = restTemplate.getForObject("http://MOVIE-INFO-SERVICE/movies/" + rating.getMovieId(), Movie.class);
			/*
			Movie movie = builder.build()
			.get()
			.uri("http://localhost:8081/movies/"+rating.getMovieId())
			.retrieve()
			.bodyToMono(Movie.class)
			.block();
			*/
			return new CatalogItem(movie.getName(), movie.getName(), rating.getRating());
		}).collect(Collectors.toList());
	}
	
	public List<CatalogItem> getFallbackCatalog(@PathVariable("userId") String userId) {
		return Arrays.asList(new CatalogItem("No Movie","No Movie",0));
	}
}
