package io.smallrye.graphql.test;

import java.time.LocalDateTime;

import org.eclipse.microprofile.graphql.DateFormat;
import org.eclipse.microprofile.graphql.Name;

/**
 * Some other POJO
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class TestSource {

    @Name("value")
    @DateFormat("yyyy-MM-dd'T'HH:mm")
    private LocalDateTime timestamp;

    public TestSource() {
        this.timestamp = LocalDateTime.now();
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
