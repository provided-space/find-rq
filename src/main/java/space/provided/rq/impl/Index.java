package space.provided.rq.impl;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.FlattenGraphFilter;
import org.apache.lucene.analysis.ngram.NGramTokenFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.synonym.SynonymGraphFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import space.provided.rq.api.IdentifiedPayload;
import space.provided.rq.api.IndexInterface;
import space.provided.rq.api.SearchAdapter;
import space.provided.rq.impl.util.MapFlattener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class Index<Type> implements IndexInterface<Type> {

    private final Directory index;
    private final IndexWriterConfig config;
    private final List<String> searchableFields;
    private final SearchAdapter<Type> adapter;
    private final float nestingPunishment;

    public Index(SearchAdapter<Type> adapter, float nestingPunishment, SynonymMap synonyms, int maxGram) {
        this.adapter = adapter;
        this.nestingPunishment = nestingPunishment;

        final Analyzer analyzer = new Analyzer() {
            @Override
            protected TokenStreamComponents createComponents(String s) {
                final StandardTokenizer tokenizer = new StandardTokenizer();
                TokenStream tokenStream = tokenizer;
                tokenStream = new LowerCaseFilter(tokenStream);
                tokenStream = new NGramTokenFilter(tokenStream, 3, maxGram, true);
                if (synonyms != null) {
                    tokenStream = new SynonymGraphFilter(tokenStream, synonyms, true);
                    tokenStream = new FlattenGraphFilter(tokenStream);
                }
                return new TokenStreamComponents(tokenizer, tokenStream);
            }
        };

        index = new ByteBuffersDirectory();
        config = new IndexWriterConfig(analyzer);
        searchableFields = new ArrayList<>();
    }

    public void index(Type... objects) throws IOException {
        final IndexWriter writer = new IndexWriter(index, config);
        final List<Document> documents = new ArrayList<>();

        for (Type object : objects) {
            final IdentifiedPayload data = adapter.extract(object);
            final Document document = new Document();
            for (Map.Entry<String, String> entry : MapFlattener.flatten(data.getPayload()).entrySet()) {
                final String fieldName = entry.getKey();
                document.add(new TextField(fieldName, entry.getValue(), Field.Store.YES));

                if (!fieldName.equals("id") && !searchableFields.contains(fieldName)) {
                    searchableFields.add(fieldName);
                }
            }
            document.add(new StringField("id", data.getIdentifier(), Field.Store.YES));
            documents.add(document);
        }

        writer.addDocuments(documents);
        writer.close();
    }

    public List<Type> search(String queryText, int limit) throws IOException {
        final IndexReader reader = DirectoryReader.open(index);
        final IndexSearcher searcher = new IndexSearcher(reader);

        final BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
        for (String field : searchableFields) {
            queryBuilder.add(createQuery(field, queryText), BooleanClause.Occur.SHOULD);
        }

        final TopDocs docs = searcher.search(queryBuilder.build(), limit);

        final List<Type> results = new ArrayList<>();
        for (ScoreDoc hit : docs.scoreDocs) {
            final Document document = searcher.doc(hit.doc);
            final Type object = adapter.fromId(document.get("id"));
            if (object != null) {
                results.add(object);
            }
        }

        reader.close();

        return results;
    }

    private Query createQuery(String field, String queryText) {
        final Term term = new Term(field, queryText);
        final Query query = new FuzzyQuery(term, 2);
        final float punishmentLevel = ((field.split("\\.").length - 1) * nestingPunishment) + 1;
        return new BoostQuery(query, 1.0f / punishmentLevel);
    }
}
