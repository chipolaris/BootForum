package com.github.chipolaris.bootforum.service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FeatureField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.SimilarityBase;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.github.chipolaris.bootforum.domain.Comment;
import com.github.chipolaris.bootforum.domain.Discussion;
import com.github.chipolaris.bootforum.util.ZipUtil;

import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.TextExtractor;

@Service
public class IndexService {

	private static final Logger logger = LoggerFactory.getLogger(IndexService.class);
	
	/*@Value("${Lucene.maxHitsPerPage}")
	private Integer maxHitsPerPage;*/
	
	@Value("${Lucene.indexDirectory}")
	private String indexDirectory;
	
	@Value("${Lucene.search.maxResults}")
	private Integer maxSearchResults;
	
	private SearcherManager searcherManager;
	
	private IndexWriter indexWriter;
	
	private StandardAnalyzer analyzer = new StandardAnalyzer();
	
	@PostConstruct
	public void init() {
		logger.info("Initialize IndexService");
		
		try {
			indexWriter = initIndexWriter(OpenMode.CREATE_OR_APPEND);
			
			/**
			 * from the Javadoc of SearcherManager constructor:
			 * 
			 * writer - the IndexWriter to open the IndexReader from.
			 * 
			 * applyAllDeletes - If true, all buffered deletes will be applied (made visible)
			 *  in the IndexSearcher / DirectoryReader. If false, the deletes may or may not be applied,
			 *  but remain buffered (in IndexWriter) so that they will be applied in the future. 
			 *  Applying deletes can be costly, so if your app can tolerate deleted documents 
			 *  being returned you might gain some performance by passing false. 
			 *  See DirectoryReader.openIfChanged(DirectoryReader, IndexWriter, boolean).
			 *  
			 * searcherFactory - An optional SearcherFactory. Pass null if you don't require 
			 * the searcher to be warmed before going live or other custom behavior.
			 */
			
			/**
			 * More notes (http://blog.mikemccandless.com/2011/11/near-real-time-readers-with-lucenes.html) 
			 */
			
			boolean applyAllDeletes = true;
			boolean writeAllDeletes = true;
			searcherManager = new SearcherManager(indexWriter, applyAllDeletes, writeAllDeletes, null);
		} 
		catch (IOException e) {
			logger.error("Unable to build indexWriter", e);
		}
	}
	
	// starting a thread to periodically called SearcherManager.maybeRefresh() 
	// see: http://blog.mikemccandless.com/2011/09/lucenes-searchermanager-simplifies.html
	// 
	// Note: adapted to use Spring @Scheduled instead of raw Java Thread 
	@Scheduled(fixedDelayString = "#{ ${Lucene.search.refresh} * 1000 }")
	private void searchIndexRefresh() {
		try {
			searcherManager.maybeRefresh();
		}
		catch(IOException e) {
			logger.error("Error calling SearcherManager.maybeRefresh()", e);
		}
		catch(AlreadyClosedException e) {
			logger.warn("Exception caught calling SearcherManager.maybeRefresh()", e);
		}
	}
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	private IndexWriter initIndexWriter(OpenMode openMode) throws IOException {
		
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
		indexWriterConfig.setOpenMode(openMode);
		Directory directory = new NIOFSDirectory(Paths.get(indexDirectory));
		
		return new IndexWriter(directory, indexWriterConfig);
	}
	
	/**
	 * 
	 * @param performBackup: whether to perform backup before clearing the index
	 * @return
	 */
	public ServiceResponse<Void> clearCommentIndex(boolean performBackup) {
		
		ServiceResponse<Void> response = new ServiceResponse<>();
		
		boolean okToClear = true;
		
		if(performBackup) {
			try {
				
				/* 1. close existing indexing resources */
				this.indexWriter.close();
				logger.info("indexWriter closed");
				this.searcherManager.close();
				logger.info("searcherManager closed");
							
				/* 2. backup: zip and save indexDirectory */
				Path indexDirectoryPath = Paths.get(indexDirectory);
	
				// get parent directory of indexDirectory, then add timestamp and ".zip" extension
				Path zipFilePath = indexDirectoryPath.getParent().resolve(indexDirectoryPath.getFileName().toString()
						+ DateTimeFormatter.ofPattern("-yyyy.MM.dd.HH.mm.ss").format(LocalDateTime.now()) + ".zip");
				
				if(!ZipUtil.zipDirectory(indexDirectoryPath.toFile(), zipFilePath.toFile())) {
					
					okToClear = false;
					
					String errorMessage = "Unable to backup existing index";
					response.addMessage(errorMessage);
					response.setAckCode(AckCodeType.FAILURE);
				}
			} 
			catch (IOException e) {
				
				okToClear = false;
				
				String errorMessage = "Unable to re-index comments" + e.toString();
				logger.error(errorMessage);
				response.addMessage(errorMessage);
				response.setAckCode(AckCodeType.FAILURE);
			}
		}
		
		if(okToClear) {
			try {
				// OpenMode.CREATE will clear the index directory
				indexWriter = initIndexWriter(OpenMode.CREATE);
	
				boolean applyAllDeletes = true;
				boolean writeAllDeletes = true;
				searcherManager = new SearcherManager(indexWriter, applyAllDeletes, writeAllDeletes, null);
			} 
			catch (IOException e) {
				logger.error("Unable to build indexWriter", e);
			}
		}
		
		return response;
	}
	
	public ServiceResponse<Void> indexCommentStream(Stream<Comment> commentStream) {
		
		ServiceResponse<Void> response = new ServiceResponse<>();
		
		/*
		 * use try-with-resources to ensure stream is closed after done processing
		 */
		try(Stream<Comment> workingStream = commentStream) {
			
			workingStream.forEach(comment -> {
				
				Document document = createCommentDocument(comment);
				
				try {
					indexWriter.addDocument(document);
				} 
				catch (IOException e) {
					logger.error("Unable to index comment with id: " + comment.getId(), e);
				}
			});
			
			try {
				indexWriter.commit();
			} 
			catch (IOException e) {
			
				logger.error("Unable to commit after indexing: ", e);
			}
		}
				
		return response;
	}
	
	/**
	 * 
	 * @param comment - object which to index the description 
	 * @return
	 */
	public ServiceResponse<Void> addCommentIndex(Comment comment) {
		
		ServiceResponse<Void> response = new ServiceResponse<>();
		
		Document document = createCommentDocument(comment);
		
		try {
			indexWriter.addDocument(document);
			indexWriter.commit();
		} 
		catch (IOException e) {
			logger.error("Unable to index comment with id: " + comment.getId(), e);
			response.setAckCode(AckCodeType.FAILURE);
		}
		
		return response;
	}

	/**
	 * update
	 * @param comment
	 * @return
	 */
	public ServiceResponse<Void> updateCommentIndex(Comment comment) {
		
		ServiceResponse<Void> response = new ServiceResponse<Void>();
		
		Term term = new Term("id", comment.getId() + "");
		
		try {
			indexWriter.updateDocument(term, createCommentDocument(comment));
			indexWriter.commit();
		} 
		catch (IOException e) {
			logger.error("Unable to update comment with id: " + comment.getId(), e);
			response.setAckCode(AckCodeType.FAILURE);
		}
		
		return response;
	}
	
	/**
	 * delete Comment entries with the given comment.id
	 * It is expected that at most one is deleted since comment.id is
	 * unique in the system
	 * 
	 * @param comment
	 * @return
	 */
	public ServiceResponse<Void> deleteCommentIndex(Comment comment) {
		
		ServiceResponse<Void> response = new ServiceResponse<Void>();
		
		Term term = new Term("id", comment.getId() + "");
		
		try {
			indexWriter.deleteDocuments(term);
			indexWriter.commit();
		} 
		catch (IOException e) {
			logger.error("Unable to delete comment with id: " + comment.getId(), e);
			response.setAckCode(AckCodeType.FAILURE);
		}
		
		return response;
	}
	
	public ServiceResponse<Void> deleteCommentIndexes(List<Long> commentIds) {
		
		ServiceResponse<Void> response = new ServiceResponse<Void>();
		
		Term[] terms = new Term[commentIds.size()];
		
		for(int i = 0; i < commentIds.size(); i++) {

			terms[i] = new Term("id", commentIds.get(i).toString());
		}
		
		try {
			indexWriter.deleteDocuments(terms);
			indexWriter.commit();
		} 
		catch (IOException e) {
			logger.error("Unable to delete comments", e);
			response.setAckCode(AckCodeType.FAILURE);
		}
		
		return response;
	}
	
	/**
	 * utility method to create a Document based on Comment entity
	 * @param comment
	 * @return
	 */
	private Document createCommentDocument(Comment comment) {
		Document document = new Document();
		
		// store id as a String field
		document.add(new StringField("id", comment.getId() + "", Store.YES));
		// also use id as a FeatureField to be factored in the scoring process during search
		document.add(new FeatureField("features", "MoreRecent", comment.getId()));
		// use StoredField for attributes that do not get queried 
		document.add(new StoredField("createBy", comment.getCreateBy()));
		document.add(new StoredField("createDate", comment.getCreateDate().getTime()));
		// note: TextField vs. StringField: the former get tokenized while the later does not
		document.add(new TextField("title", comment.getTitle(), Store.YES));
		// comment.content contains HTML content so first extract the text content
		document.add(new TextField("content", 
				new TextExtractor(new Source(comment.getContent())).toString(), Store.YES));
		
		// discussion fields
		document.add(new StringField("discussionId", comment.getDiscussion().getId() + "", Store.YES));
		
		return document;
	}
	
	
	public ServiceResponse<SearchCommentResult> searchCommentByKeywords(String searchString, 
			boolean includeTitle, boolean includeContent, int first, int pageSize) {
		
		ServiceResponse<SearchCommentResult> response = new ServiceResponse<>();
		
		IndexSearcher indexSearcher = null;	
		
		try {
			indexSearcher = searcherManager.acquire();
			
			Query titleQuery = new QueryParser("title", analyzer).parse(searchString);
			Query contentQuery = new QueryParser("content", analyzer).parse(searchString);
			
			BoostQuery boostTitleQuery = new BoostQuery(titleQuery, 0.75f);
			BoostQuery boostContentQuery = new BoostQuery(contentQuery, 0.25f);
			
			/*
			 * Give a small boost (0.001f) to the "MoreRecent" feature (based on "Id" attribute of the comment record
			 */
			BoostQuery boostedQuery = new BoostQuery(FeatureField.newSaturationQuery("features", "MoreRecent"), 0.001f);
			
			/*
			 * First, create a query that the searchString must match
			 * Note from Lucene Javadoc for {@link org.apache.lucene.search.BooleanClause Occur}
			 * Use this operator for clauses that should appear in the matching documents. 
			 * For a BooleanQuery with no MUST clauses one or more SHOULD clauses must match 
			 * a document for the BooleanQuery to match.
			 */
			Query matchedQuery = new BooleanQuery.Builder()
					.add(boostTitleQuery, Occur.SHOULD)
					.add(boostContentQuery, Occur.SHOULD)
					.build();
			
			/*
			 * Combine the matchedQuery with the boostedQuery
			 */
			Query query = new BooleanQuery.Builder()
					.add(matchedQuery, Occur.MUST)
					.add(boostedQuery, Occur.MUST).build();
			
			TopScoreDocCollector collector = TopScoreDocCollector.create(maxSearchResults, maxSearchResults);
			
			indexSearcher.search(query, collector);
			
			TopDocs topDocs = collector.topDocs(first, pageSize);
			
			ScoreDoc[] scoreDocs = topDocs.scoreDocs;
			long totalHits = topDocs.totalHits.value;
			
			List<Comment> results = new ArrayList<Comment>();
			
			for(ScoreDoc scoreDoc : scoreDocs) {
				
				int docNumber = scoreDoc.doc;
				Document document = indexSearcher.doc(docNumber);
				results.add(createComment(document));
			}
			
			SearchCommentResult searchResult = new SearchCommentResult();
			searchResult.setTotalHits(totalHits);
			searchResult.setComments(results);
			
			response.setDataObject(searchResult);
		} 
		catch (IOException e) {
			logger.error("Unable to search Comments for keywords " + searchString, e);
			response.setAckCode(AckCodeType.FAILURE);
		} 
		catch(ParseException e) {
			logger.error("Unable to search Comments for keywords " + searchString, e);
			response.setAckCode(AckCodeType.FAILURE);
		}
		finally {
			try {
				searcherManager.release(indexSearcher);
				
				// make sure not to use this indexSearcher again:
				// 		ref: http://blog.mikemccandless.com/2011/09/lucenes-searchermanager-simplifies.html
				indexSearcher = null;
			} 
			catch (IOException e) {
				logger.error("Error trying to release indexSearcher", e);
			}
		}
		
		return response;
	}
	
	/**
	 * utility method to turn a Document (which must be guaranteed to be a Comment document)
	 * into a Comment object 
	 * @param doc
	 * @return
	 */
	private Comment createComment(Document doc) {
		
		Comment comment = new Comment();
		
		comment.setId(Long.valueOf(doc.getField("id").stringValue()));
		comment.setCreateBy(doc.getField("createBy").stringValue());
		comment.setCreateDate(new Date(doc.getField("createDate").numericValue().longValue()));
		comment.setTitle(doc.getField("title").stringValue());
		comment.setContent(doc.getField("content").stringValue());
		
		Discussion discussion = new Discussion();
		comment.setDiscussion(discussion);
		discussion.setId(Long.valueOf(doc.getField("discussionId").stringValue()));
		
		return comment;
	}

	/**
	 * 
	 */
	@PreDestroy
	public void cleanup() {
		logger.info("Cleaning up IndexingService");
		
		try {
			this.indexWriter.close();
			logger.info("indexWriter closed");
			this.searcherManager.close();
			logger.info("searcherManager closed");
		} 
		catch (IOException e) {
			logger.error("Unable to cleanup indexWriter and searcherManager", e);
		}
	}
	
	class CustomSimilarity extends SimilarityBase {

		@Override
		protected double score(BasicStats stats, double freq, double docLen) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public String toString() {
			
			return "CustomSimilarity";
		}		
	}
}
