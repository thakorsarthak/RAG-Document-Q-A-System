package rag_document.service;


import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import rag_document.dto.SearchResult;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**             what this service does:
 * Indexing: Store document chunks in Lucene's inverted index
 * Searching: Run BM25 keyword search
 * Deletion: Remove chunks when document is deleted  **/


@Service
@Slf4j
public class LuceneSearchService {

    @Value("${lucene.index-path}")
    private String indexPath;

    private Directory directory;
    private IndexWriter indexWriter;
    private StandardAnalyzer analyzer;

    @PostConstruct
    public void init() throws IOException {

        Path path = Paths.get(indexPath);
        directory = FSDirectory.open(path);
        analyzer = new StandardAnalyzer();

        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

        indexWriter = new IndexWriter(directory,config);
    }

    @PreDestroy
    public void cleanUp() throws IOException{
        if(indexWriter!=null){
            indexWriter.close();
        }
        if(directory!=null){
            directory.close();
        }
        log.info("Lucene resources closed");

    }


    public void indexChunk(Long documentId, String filename, int chunkIndex , String text) throws IOException{

        Document doc = new Document();

        //store metadata
        doc.add(new StringField("documentId" , documentId.toString() , Field.Store.YES));
        doc.add(new StringField("filename" , filename , Field.Store.YES));
        doc.add(new StringField("chunkIndex" , String.valueOf(chunkIndex) , Field.Store.YES));

        // Index text content (analyzed for searching)
        doc.add(new TextField("content", text, Field.Store.YES));

        indexWriter.addDocument(doc);
        indexWriter.commit();

        log.debug("Indexed chunk {} from document {}", chunkIndex, documentId);

    }

    public List<SearchResult> search(String queryText , int maxResults) throws Exception{
        IndexReader  reader  = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);

        //using BM25 similarity
        searcher.setSimilarity(new BM25Similarity());

        QueryParser parser = new QueryParser("content" , analyzer);
        Query query = parser.parse(queryText);

        //search
        TopDocs topDocs = searcher.search(query , maxResults);

        List<SearchResult> results = new ArrayList<>();
        for(ScoreDoc scoreDoc : topDocs.scoreDocs){
            Document doc = searcher.doc(scoreDoc.doc);

            SearchResult result = SearchResult.builder()
                    .documentId(Long.parseLong(doc.get("documentId")))
                    .filename(doc.get("filename"))
                    .chunkIndex(Integer.parseInt(doc.get("chunkIndex")))
                    .text(doc.get("content"))
                    .score((double) scoreDoc.score).build();

            results.add(result);
        }

        reader.close();
        log.info("BM25 search for '{}' returned {} results", queryText, results.size());

        return  results;
    }

    /**
     * Delete all chunks for a document
     */
    public void deleteDocument(Long documentId) throws IOException {
        Term term = new Term("documentId", documentId.toString());
        indexWriter.deleteDocuments(term);
        indexWriter.commit();
        log.info("Deleted all chunks for document {} from Lucene index", documentId);
    }
}
