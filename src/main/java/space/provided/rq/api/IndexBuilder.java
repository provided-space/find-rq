package space.provided.rq.api;

import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.util.CharsRef;
import space.provided.rq.impl.Index;

import java.io.IOException;
import java.util.List;

public final class IndexBuilder<Type> {

    private SearchAdapter<Type> adapter;
    private float nestingPunishment = 1.0f;
    private SynonymMap synonyms;

    public IndexInterface<Type> build() {
        return new Index<>(adapter, nestingPunishment, synonyms);
    }

    public IndexBuilder<Type> setAdapter(SearchAdapter<Type> adapter) {
        this.adapter = adapter;
        return this;
    }

    public IndexBuilder<Type> setNestingPunishment(float nestingPunishment) {
        this.nestingPunishment = nestingPunishment;
        return this;
    }

    public IndexBuilder<Type> setSynonyms(List<List<String>> synonyms) {
        if (synonyms == null) {
            return this;
        }

        try {
            this.synonyms = createSynonymMap(synonyms);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return this;
    }

    private SynonymMap createSynonymMap(List<List<String>> allSynonyms) throws IOException {
        final SynonymMap.Builder builder = new SynonymMap.Builder();

        for (List<String> synonyms : allSynonyms) {
            for (String synonym : synonyms) {
                for (String sibling : synonyms) {
                    if (!synonym.equals(sibling)) {
                        builder.add(new CharsRef(sibling), new CharsRef(synonym), true);
                    }
                }
            }
        }

        return builder.build();
    }
}
