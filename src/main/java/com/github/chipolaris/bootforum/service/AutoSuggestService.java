package com.github.chipolaris.bootforum.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.suggest.Lookup.LookupResult;
import org.apache.lucene.search.suggest.analyzing.AnalyzingInfixSuggester;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AutoSuggestService {

	private static final Logger logger = LoggerFactory.getLogger(AutoSuggestService.class);
	
	@Value("#{applicationProperties['Lucene.indexDirectory']}")
	private String indexDirectory;
	
	/*
	 * temp directory used by the MMapDirectory class
	 */
	private String mmapDirectory;
	
	private AnalyzingInfixSuggester analyzingInfixSuggester;
	
	@PostConstruct
	public void init() {
		logger.info("Initializing...");
		
		this.mmapDirectory = indexDirectory + File.separator + "mmap";
		
		// create mmap directory if not already exist
		Path path = Paths.get(mmapDirectory);
		
		if(!Files.isDirectory(path)) {
			try {
				Files.createDirectories(path);
				logger.info(String.format("Created Lucene Index MMapDirectory '%s'", mmapDirectory));
			} 
			catch (IOException e) {
				logger.error(String.format("Unable to create directory '%s'", mmapDirectory), e);
			}
		}
		else {
			buildSuggestingIndex();
		}
	}

	public void buildSuggestingIndex() {
		try {
			Path indexPath = Paths.get(indexDirectory);
			
			if(Files.exists(indexPath)) {
				analyzingInfixSuggester = new AnalyzingInfixSuggester(new MMapDirectory(Paths.get(mmapDirectory)), new StandardAnalyzer());
				analyzingInfixSuggester.build(new LuceneDictionary(DirectoryReader.open(new SimpleFSDirectory(Paths.get(indexDirectory))), "title"));
			}
		} 
		catch (IOException e) {
			logger.error(String.format("Unable to build suggesting index"), e);
		}
	}

	public List<String> lookup(String word) {
		
		List<String> results = new ArrayList<String>();
		
		try {
			List<LookupResult> lookupResults = analyzingInfixSuggester.lookup(
					word, 20, false, false);
			for (LookupResult lookupResult : lookupResults) {
				results.add(lookupResult.key.toString());
			}
		} 
		catch (Exception e) {
			logger.error(String.format("Unable to lookup %s", word), e);
		}
		
		return results;
	}
}
