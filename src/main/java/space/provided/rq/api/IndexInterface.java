package space.provided.rq.api;

import java.io.IOException;
import java.util.List;

public interface IndexInterface<Type> {

    void index(Type... objects) throws IOException;

    List<Type> search(String query, int limit) throws IOException;
}
