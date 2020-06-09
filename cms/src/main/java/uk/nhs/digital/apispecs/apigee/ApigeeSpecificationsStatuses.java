package uk.nhs.digital.apispecs.apigee;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import uk.nhs.digital.apispecs.model.OpenApiSpecificationStatus;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ApigeeSpecificationsStatuses {

    private List<OpenApiSpecificationStatus> contents;

    public List<OpenApiSpecificationStatus> getContents() {
        return contents;
    }
}
