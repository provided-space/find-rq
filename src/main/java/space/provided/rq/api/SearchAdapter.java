package space.provided.rq.api;

public interface SearchAdapter<Type> {

    Type fromId(String id);

    IdentifiedPayload extract(Type object);
}
