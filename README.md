> For when you need to find data real quick
# FindRQ

This is a simple wrapper around Lucene to index your searchable data and automatically tokenize the payload.

The data is only stored in-memory and this library is best used for more or less static data.

## Installation

### Gradle
```groovy
repositories {
    maven { url 'https://registry.provided.space' }
}

dependencies {
    implementation 'space.provided:rq:VERSION'
}
```

### Maven
```xml
<repositories>
    <repository>
        <id>provided</id>
        <name>provided.space</name>
        <url>https://registry.provided.space</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>space.provided</groupId>
        <artifactId>rq</artifactId>
        <version>VERSION</version>
    </dependency>
</dependencies>
```

## Example

First of all, define the `SearchAdapter` for your data, which will be used by the index when writing an object and when mapping an id back to the object.
After that, create your index via the IndexBuilder and start indexing your data for search later.

```java
public final class Post extends Entity {
    
    private String title;
    private String[] tags;
    
    // constructor, getters and setters
}

public final class PostSearch implements SearchAdapter<Post> {
    
    public Post fromId(String id) {
        return posts.get(id);
    }
    
    public IdentifiedPayload extract(Post object) {
        final Map<String, Object> payload = new HashMap<>();

        payload.put("title", object.getTitle());
        payload.put("tags", object.getTags());

        return new IdentifiedPayload(object.getId(), payload);
    }
}
```

```java
final IndexInterface<Post> postIndex = new IndexBuilder()
        .setAdapter(postSearch)
        .setNestingPunishment(2) // this will negatively impact the score depending on how deep nested the match was
        .setSynonyms(synonyms) // list of synonyms array which will resolve in many-to-many
        .build();

postIndex.index(firstPost);
postIndex.index(secondPost, thirdPost); // bulk indexing is also supported

final List<Post> posts = postIndex.search("Lucene wrapper", 10);
```