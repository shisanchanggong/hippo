package uk.nhs.digital.apispecs.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenApiSpecificationStatus {

    private String id;
    private String modified;

    public String getId() {
        return id;
    }

    public Instant getModified() {
        return Instant.parse(modified);
    }

}
