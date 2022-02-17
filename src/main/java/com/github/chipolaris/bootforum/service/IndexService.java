package com.github.chipolaris.bootforum.service;

import java.io.File;
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
import org.apache.lucene.index.IndexableField;
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
import com.github.chipolaris.bootforum.domain.Tag;
import com.github.chipolaris.bootforum.util.ZipUtil;

import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.TextExtractor;

@Service
public class IndexService {

	private static final String DEFAULT_INDEX_DIRECTORY = System.getProperty("user.home") 
			+ File.separator + "BootForum" + File.separator + "index";
	
	private static final String DISCUSSION_DIR = "discussion";

	private static final String COMMENT_DIR = "comment";
	
	private static final String INDEX_BACKUP_DIR = "index-backup";

	private static final Logger logger = LoggerFactory.getLogger(IndexService.class);
	
	/*@Value("${Lucene.maxHitsPerPage}")
	private Integer maxHitsPerPage;*/
	
	@Value("${Lucene.indexDirectory:#{nul}}")
	private String indexDirectory;
	
	/* comment index */
	@Value("${Lucene.search.maxResults}")
	private Integer maxCommentSearchResults;
	
	private SearcherManager commentSearcherManager;
	
	private IndexWriter commentIndexWriter;
	
	private StandardAnalyzer commentAnalyzer = new StandardAnalyzer();
	
	/* discussion index */
	@Value("${Lucene.search.maxResults}")
	private Integer maxDiscussionSearchResults;
	
	private SearcherManager discussionSearcherManager;
	
	private IndexWriter discussionIndexWriter;
	
	private StandardAnalyzer discussionAnalyzer = new StandardAnalyzer();
	
	/* backup directory path */
	private Path indexBackupDirPath;
		
	@PostConstruct
	public void init() {
		
		logger.info("Initialize IndexService");
		
		if(indexDirectory == null) {
			indexDirectory = DEFAULT_INDEX_DIRECTORY;
		}
		
		try {
			commentIndexWriter = initCommentIndexWriter(OpenMode.CREATE_OR_APPEND);
			
			discussionIndexWriter = initDiscussionIndexWriter(OpenMode.CREATE_OR_APPEND);
			
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
			
			commentSearcherManager = new SearcherManager(commentIndexWriter, applyAllDeletes, writeAllDeletes, null);
			
			discussionSearcherManager = new SearcherManager(discussionIndexWriter, applyAllDeletes, writeAllDeletes, null);
		} 
		catch (IOException e) {
			logger.error("Unable to build indexWriter", e);
		}
		
		this.indexBackupDirPath = Paths.get(indexDirectory).resolve(INDEX_BACKUP_DIR);
		// create backup directory if it's not already exists
		indexBackupDirPath.toFile().mkdirs();
	}
	
	// starting a thread to periodically called SearcherManager.maybeRefresh() 
	// see: http://blog.mikemccandless.com/2011/09/lucenes-searchermanager-simplifies.html
	// 
	// Note: adapted to use Spring @Scheduled instead of raw Java Thread 
	@Scheduled(fixedDelayString = "#{ ${Lucene.search.refresh} * 1000 }")
	private void searchIndexRefresh() {
		try {
			commentSearcherManager.maybeRefresh();
			discussionSearcherManager.maybeRefresh();
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
	private IndexWriter initCommentIndexWriter(OpenMode openMode) throws IOException {
		
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(commentAnalyzer);
		indexWriterConfig.setOpenMode(openMode);
		Directory directory = new NIOFSDirectory(Paths.get(indexDirectory).resolve(COMMENT_DIR));
		
		return new IndexWriter(directory, indexWriterConfig);
	}
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	private IndexWriter initDiscussionIndexWriter(OpenMode openMode) throws IOException {
		
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(discussionAnalyzer);
		indexWriterConfig.setOpenMode(openMode);
		Directory directory = new NIOFSDirectory(Paths.get(indexDirectory).resolve(DISCUSSION_DIR));
		
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
				this.commentIndexWriter.close();
				logger.info("commentIndexWriter closed");
				this.commentSearcherManager.close();
				logger.info("commentSearcherManager closed");
							
				/* 2. backup: zip and save comment index directory */
				Path indexDirectoryPath = Paths.get(indexDirectory, COMMENT_DIR);
	
				// get parent directory of indexDirectory, then add timestamp and ".zip" extension
				Path zipFilePath = this.indexBackupDirPath
						.resolve(indexDirectoryPath.getFileName().toString()
						+ DateTimeFormatter.ofPattern("-yyyy.MM.dd.HH.mm.ss").format(LocalDateTime.now()) + ".zip");
				
				if(!ZipUtil.zipDirectory(indexDirectoryPath.toFile(), zipFilePath.toFile())) {
					
					okToClear = false;
					
					String errorMessage = "Unable to backup existing Comment index";
					response.addMessage(errorMessage);
					response.setAckCode(AckCodeType.FAILURE);
				}
			} 
			catch (IOException e) {
				
				okToClear = false;
				
				String errorMessage = "Unable to clear Comment index" + e.toString();
				logger.error(errorMessage);
				response.addMessage(errorMessage);
				response.setAckCode(AckCodeType.FAILURE);
			}
		}

		// finally, make sure to open the re-init index directory, whether the above process fail or not
		try {
			// OpenMode.CREATE will clear the index directory, OpenMode.APPEND will use existing directory
			commentIndexWriter = initCommentIndexWriter(okToClear ? OpenMode.CREATE : OpenMode.APPEND);

			boolean applyAllDeletes = true;
			boolean writeAllDeletes = true;
			commentSearcherManager = new SearcherManager(commentIndexWriter, applyAllDeletes, writeAllDeletes, null);
		} 
		catch (IOException e) {
			logger.error("Unable to build commentIndexWriter", e);
		}
		
		return response;
	}
	
	/**
	 * 
	 * @param performBackup
	 * @return
	 */
	public ServiceResponse<Void> clearDiscussionIndex(boolean performBackup) {
		
		ServiceResponse<Void> response = new ServiceResponse<>();
		
		boolean okToClear = true;
		
		if(performBackup) {
			try {
				
				/* 1. close existing indexing resources */
				this.discussionIndexWriter.close();
				logger.info("discussionIndexWriter closed");
				this.discussionSearcherManager.close();
				logger.info("discussionSearcherManager closed");
							
				/* 2. backup: zip and save discussion index directory */
				Path indexDirectoryPath = Paths.get(indexDirectory, DISCUSSION_DIR);
	
				// get parent directory of indexDirectory, then add timestamp and ".zip" extension
				Path zipFilePath = this.indexBackupDirPath
						.resolve(indexDirectoryPath.getFileName().toString()
						+ DateTimeFormatter.ofPattern("-yyyy.MM.dd.HH.mm.ss").format(LocalDateTime.now()) + ".zip");
				
				if(!ZipUtil.zipDirectory(indexDirectoryPath.toFile(), zipFilePath.toFile())) {
					
					okToClear = false;
					
					String errorMessage = "Unable to backup existing Discussion index";
					response.addMessage(errorMessage);
					response.setAckCode(AckCodeType.FAILURE);
				}
			} 
			catch (IOException e) {
				
				okToClear = false;
				
				String errorMessage = "Unable to clear Discussion index" + e.toString();
				logger.error(errorMessage);
				response.addMessage(errorMessage);
				response.setAckCode(AckCodeType.FAILURE);
			}
		}
		
		// finally, make sure to open the re-init index directory, whether the above process fail or not
		try {
			// OpenMode.CREATE will clear the index directory, OpenMode.APPEND will use existing directory
			discussionIndexWriter = initDiscussionIndexWriter(okToClear ? OpenMode.CREATE : OpenMode.APPEND);

			boolean applyAllDeletes = true;
			boolean writeAllDeletes = true;
			discussionSearcherManager = new SearcherManager(discussionIndexWriter, applyAllDeletes, writeAllDeletes, null);
		} 
		catch (IOException e) {
			logger.error("Unable to build discussionIndexWriter", e);
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
					commentIndexWriter.addDocument(document);
				} 
				catch (IOException e) {
					logger.error("Unable to index comment with id: " + comment.getId(), e);
				}
			});
			
			try {
				commentIndexWriter.commit();
			} 
			catch (IOException e) {
			
				logger.error("Unable to commit comments after indexing: ", e);
			}
		}
				
		return response;
	}
	
	public ServiceResponse<Void> indexDiscussionStream(Stream<Discussion> discussionStream) {
		
		ServiceResponse<Void> response = new ServiceResponse<>();
		
		/*
		 * use try-with-resources to ensure stream is closed after done processing
		 */
		try(Stream<Discussion> workingStream = discussionStream) {
			
			workingStream.forEach(discussion -> {
				
				Document document = createDiscussionDocument(discussion);
				
				try {
					discussionIndexWriter.addDocument(document);
				} 
				catch (IOException e) {
					logger.error("Unable to index discussion with id: " + discussion.getId(), e);
				}
			});
			
			try {
				discussionIndexWriter.commit();
			} 
			catch (IOException e) {
			
				logger.error("Unable to commit discussions after indexing: ", e);
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
			commentIndexWriter.addDocument(document);
			commentIndexWriter.commit();
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
	public ServiceResponse<Void> updateComment(Comment comment) {
		
		ServiceResponse<Void> response = new ServiceResponse<Void>();
		
		Term term = new Term("id", comment.getId() + "");
		
		try {
			commentIndexWriter.updateDocument(term, createCommentDocument(comment));
			commentIndexWriter.commit();
		} 
		catch (IOException e) {
			logger.error("Unable to update Comment with id: " + comment.getId(), e);
			response.setAckCode(AckCodeType.FAILURE);
		}
		
		return response;
	}
	
	/**
	 * update
	 * @param discussion
	 * @return
	 */
	public ServiceResponse<Void> updateDiscussion(Discussion discussion) {
		
		ServiceResponse<Void> response = new ServiceResponse<Void>();
		
		Term term = new Term("id", discussion.getId() + "");
		
		try {
			discussionIndexWriter.updateDocument(term, createDiscussionDocument(discussion));
			discussionIndexWriter.commit();
		} 
		catch (IOException e) {
			logger.error("Unable to update Discussion with id: " + discussion.getId(), e);
			response.setAckCode(AckCodeType.FAILURE);
		}
		
		return response;
	}
	
	/**
	 * Delete Discussion doc with the given discussion.id
	 * It is expected that at most one is deleted since discussion.id is
	 * unique in the system
	 * 
	 * @param discussion
	 * @return
	 */
	public ServiceResponse<Void> deleteDiscussion(Discussion discussion) {
		
		ServiceResponse<Void> response = new ServiceResponse<Void>();
		
		Term term = new Term("id", discussion.getId() + "");
		
		try {
			discussionIndexWriter.deleteDocuments(term);
			discussionIndexWriter.commit();
		} 
		catch (IOException e) {
			logger.error("Unable to delete Discussion with id: " + discussion.getId(), e);
			response.setAckCode(AckCodeType.FAILURE);
		}
		
		return response;
	}
	
	/**
	 * Delete comment docs with the given discussionId
	 * @param discussionId
	 * @return
	 */
	public ServiceResponse<Void> deleteComments(Long discussionId) {
		
		ServiceResponse<Void> response = new ServiceResponse<Void>();
		
		Term term = new Term("discussionId", discussionId.toString());

		try {
			commentIndexWriter.deleteDocuments(term);
			commentIndexWriter.commit();
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
	
	/**
	 * utility method to create a Document based on Discussion entity
	 * @param discussion
	 * @return
	 */
	private Document createDiscussionDocument(Discussion discussion) {
		
		Document document = new Document();
		
		// store id as a String field
		document.add(new StringField("id", discussion.getId() + "", Store.YES));
		// also use id as a FeatureField to be factored in the scoring process during search
		document.add(new FeatureField("features", "MoreRecent", discussion.getId()));
		// use StoredField for attributes that do not get queried 
		document.add(new StoredField("createBy", discussion.getCreateBy()));
		document.add(new StoredField("createDate", discussion.getCreateDate().getTime()));
		
		// note: TextField vs. StringField: the former get tokenized while the later does not
		document.add(new TextField("title", discussion.getTitle(), Store.YES));
		document.add(new StringField("closed", String.valueOf(discussion.isClosed()), Store.YES));
		
		for(Tag tag : discussion.getTags()) {
			document.add(new StringField("tag", tag.getLabel(), Store.YES));
		}
		
		return document;
	}
	
	public ServiceResponse<SearchCommentResult> searchCommentByKeywords(String searchString, 
			int first, int pageSize) {
		
		ServiceResponse<SearchCommentResult> response = new ServiceResponse<>();
		
		IndexSearcher indexSearcher = null;	
		
		try {
			indexSearcher = commentSearcherManager.acquire();
			
			Query titleQuery = new QueryParser("title", commentAnalyzer).parse(searchString);
			Query contentQuery = new QueryParser("content", commentAnalyzer).parse(searchString);
			
			BoostQuery boostTitleQuery = new BoostQuery(titleQuery, 0.75f);
			BoostQuery boostContentQuery = new BoostQuery(contentQuery, 0.25f);
			
			/*
			 * Give a small boost (0.001f) to the "MoreRecent" feature based on "Id" attribute of the comment record.
			 * That is: greater id (more recent) has a small boost in scoring
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
			
			TopScoreDocCollector collector = TopScoreDocCollector.create(maxCommentSearchResults, maxCommentSearchResults);
			
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
				commentSearcherManager.release(indexSearcher);
				
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
	
	public ServiceResponse<SearchDiscussionResult> searchDiscussionByKeywords(String searchString, 
			int first, int pageSize) {
		
		ServiceResponse<SearchDiscussionResult> response = new ServiceResponse<>();
		
		IndexSearcher indexSearcher = null;	
		
		try {
			indexSearcher = discussionSearcherManager.acquire();
			
			Query titleQuery = new QueryParser("title", discussionAnalyzer).parse(searchString);
			Query isClosedQuery = new QueryParser("closed", discussionAnalyzer).parse(String.valueOf(Boolean.TRUE));
						
			// title match will get scoring boost of 0.65
			BoostQuery boostTitleQuery = new BoostQuery(titleQuery, 0.65f);
			
			// closed match will get scoring boost of 0.35
			BoostQuery boostClosedQuery = new BoostQuery(isClosedQuery, 0.35f);
			
			/*
			 * Give a small boost (0.001f) to the "MoreRecent" feature based on "Id" attribute of the comment record.
			 * That is: greater id (more recent) has a small boost in scoring
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
					.add(boostClosedQuery, Occur.SHOULD)
					.build();
			
			/*
			 * Combine the matchedQuery with the boostedQuery
			 */
			Query query = new BooleanQuery.Builder()
					.add(matchedQuery, Occur.MUST)
					.add(boostedQuery, Occur.MUST).build();
			
			TopScoreDocCollector collector = TopScoreDocCollector.create(maxDiscussionSearchResults, maxDiscussionSearchResults);
			
			indexSearcher.search(query, collector);
			
			TopDocs topDocs = collector.topDocs(first, pageSize);
			
			ScoreDoc[] scoreDocs = topDocs.scoreDocs;
			long totalHits = topDocs.totalHits.value;
			
			List<Discussion> results = new ArrayList<Discussion>();
			
			for(ScoreDoc scoreDoc : scoreDocs) {
				
				int docNumber = scoreDoc.doc;
				Document document = indexSearcher.doc(docNumber);
				results.add(createDiscussion(document));
			}
			
			SearchDiscussionResult searchResult = new SearchDiscussionResult();
			searchResult.setTotalHits(totalHits);
			searchResult.setDiscussions(results);
			
			response.setDataObject(searchResult);
		} 
		catch (IOException e) {
			logger.error("Unable to search Discussion with keyword " + searchString, e);
			response.setAckCode(AckCodeType.FAILURE);
		} 
		catch(ParseException e) {
			logger.error("Unable to search similar to Discussion with keyword " + searchString, e);
			response.setAckCode(AckCodeType.FAILURE);
		}
		finally {
			try {
				commentSearcherManager.release(indexSearcher);
				
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
	
	public ServiceResponse<SearchDiscussionResult> searchSimilarDiscussions(Discussion discussion, 
			int first, int pageSize) {
		
		ServiceResponse<SearchDiscussionResult> response = new ServiceResponse<>();
		
		IndexSearcher indexSearcher = null;	
		
		try {
			
			indexSearcher = discussionSearcherManager.acquire();

			BoostQuery boostTagQuery = null;
			
			String tagSearchString = "";
			for(Tag tag : discussion.getTags()) {
				tagSearchString += tag.getLabel() + " ";
			}
			
			if(!"".equals(tagSearchString)) {
				Query tagQuery = new QueryParser("tag", discussionAnalyzer).parse(tagSearchString);
			
				// tag match will get scoring boost of 0.35
				boostTagQuery = new BoostQuery(tagQuery, 0.35f);
			}
			
			Query titleQuery = new QueryParser("title", discussionAnalyzer).parse(discussion.getTitle());
			Query notClosedQuery = new QueryParser("closed", discussionAnalyzer).parse(String.valueOf(Boolean.FALSE));
						
			// title match will get scoring boost of 0.50 if tag is available, otherwise, it would get .85
			BoostQuery boostTitleQuery = new BoostQuery(titleQuery, boostTagQuery != null ? 0.50f : 0.85f);
			
			// not closed match will get scoring boost of 0.15
			BoostQuery boostNotClosedQuery = new BoostQuery(notClosedQuery, 0.15f);
			
			/*
			 * Give a small boost (0.001f) to the "MoreRecent" feature based on "Id" attribute of the comment record.
			 * That is: greater id (more recent) has a small boost in scoring
			 */
			BoostQuery boostedQuery = new BoostQuery(FeatureField.newSaturationQuery("features", "MoreRecent"), 0.001f);
			
			/*
			 * First, create a query that the searchString must match
			 * Note from Lucene Javadoc for {@link org.apache.lucene.search.BooleanClause Occur}
			 * Use this operator for clauses that should appear in the matching documents. 
			 * For a BooleanQuery with no MUST clauses one or more SHOULD clauses must match 
			 * a document for the BooleanQuery to match.
			 */
			BooleanQuery.Builder builder = new BooleanQuery.Builder();
			builder.add(boostTitleQuery, Occur.MUST).add(boostNotClosedQuery, Occur.SHOULD);
			
			if(boostTagQuery != null) {
				builder.add(boostTagQuery, Occur.SHOULD);
			}
			
			Query matchedQuery = builder.build();
			
			/*
			 * Combine the matchedQuery with the boostedQuery
			 */
			Query query = new BooleanQuery.Builder()
					.add(matchedQuery, Occur.MUST)
					.add(boostedQuery, Occur.MUST).build();
			
			TopScoreDocCollector collector = TopScoreDocCollector.create(maxDiscussionSearchResults, maxDiscussionSearchResults);
			
			indexSearcher.search(query, collector);
			
			TopDocs topDocs = collector.topDocs(first, pageSize);
			
			ScoreDoc[] scoreDocs = topDocs.scoreDocs;
			long totalHits = topDocs.totalHits.value;
			
			List<Discussion> results = new ArrayList<Discussion>();
			
			for(ScoreDoc scoreDoc : scoreDocs) {
				
				int docNumber = scoreDoc.doc;
				Document document = indexSearcher.doc(docNumber);
				results.add(createDiscussion(document));
			}
			
			SearchDiscussionResult searchResult = new SearchDiscussionResult();
			searchResult.setTotalHits(totalHits);
			searchResult.setDiscussions(results);
			
			response.setDataObject(searchResult);
		} 
		catch (IOException e) {
			logger.error("Unable to search similar to Discussion with id " + discussion.getId(), e);
			response.setAckCode(AckCodeType.FAILURE);
		} 
		catch(ParseException e) {
			logger.error("Unable to search similar to Discussion with id " + discussion.getId(), e);
			response.setAckCode(AckCodeType.FAILURE);
		}
		finally {
			try {
				commentSearcherManager.release(indexSearcher);
				
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
	
	private Discussion createDiscussion(Document doc) {
		
		Discussion discussion = new Discussion();
		
		discussion.setId(Long.valueOf(doc.getField("id").stringValue()));
		discussion.setCreateBy(doc.getField("createBy").stringValue());
		discussion.setCreateDate(new Date(doc.getField("createDate").numericValue().longValue()));
		discussion.setTitle(doc.getField("title").stringValue());
		discussion.setClosed(Boolean.valueOf(doc.getField("closed").stringValue()));
		
		/* construct tag list */
		List<Tag> tags = new ArrayList<>();
		discussion.setTags(tags);
		
		IndexableField[] tagFields = doc.getFields("tag");
		
		if(tagFields.length > 0) {
			for(IndexableField tagField : tagFields) {
				Tag tag = new Tag();
				tag.setLabel(tagField.stringValue());
				tags.add(tag);
			}
		}
		
		return discussion;
	}

	/**
	 * 
	 */
	@PreDestroy
	public void cleanup() {
		logger.info("Cleaning up IndexingService");
		
		try {
			this.commentIndexWriter.close();
			logger.info("commentIndexWriter closed");
			this.commentSearcherManager.close();
			logger.info("commentSearcherManager closed");
			
			this.discussionIndexWriter.close();
			logger.info("discussionIndexWriter closed");
			this.discussionSearcherManager.close();
			logger.info("discussionSearcherManager closed");
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
