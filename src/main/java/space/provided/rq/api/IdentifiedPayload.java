package space.provided.rq.api;

import java.util.Map;

public final class IdentifiedPayload {

    private final String identifier;
    private final Map<String, Object> payload;

    public IdentifiedPayload(String identifier, Map<String, Object> payload) {
        this.identifier = identifier;
        this.payload = payload;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }
}
