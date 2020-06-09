package uk.nhs.digital.apispecs.apigee;

import org.hippoecm.hst.site.HstServices;
import org.onehippo.cms7.crisp.api.broker.ResourceServiceBroker;
import org.onehippo.cms7.crisp.api.resource.Resource;
import org.onehippo.cms7.crisp.hst.module.CrispHstServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriComponentsBuilder;
import uk.nhs.digital.apispecs.OpenApiSpecificationRepository;
import uk.nhs.digital.apispecs.model.OpenApiSpecificationStatus;

import java.util.List;

public class ApigeeService implements OpenApiSpecificationRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApigeeService.class);

    private static final String RESOURCE_NAMESPACE_APIGEE_MANAGEMENT_API = "apigeeManagementApi";

    private ApigeeClientConfig config;

    public ApigeeService(ApigeeClientConfig config) {
        this.config = config;
    }

    @Override public List<OpenApiSpecificationStatus> getSpecsStatuses() throws ApigeeServiceException {

        LOGGER.debug("Retrieving list of available specifications.");

        final ResourceServiceBroker broker = CrispHstServices.getDefaultResourceServiceBroker(HstServices.getComponentManager());

        final Resource resource = broker.resolve(RESOURCE_NAMESPACE_APIGEE_MANAGEMENT_API, config.getAllSpecUrl());

        return broker
            .getResourceBeanMapper(RESOURCE_NAMESPACE_APIGEE_MANAGEMENT_API)
            .map(resource, ApigeeSpecificationsStatuses.class)
            .getContents();
    }

    @Override public String getSpecification(final String specificationId) {

        LOGGER.debug("Retrieving specification with id {}.", specificationId);

        final String singleSpecUrl = UriComponentsBuilder.fromHttpUrl(config.getSingleSpecUrl()).build(specificationId).toString();

        final ResourceServiceBroker broker = CrispHstServices.getDefaultResourceServiceBroker(HstServices.getComponentManager());

        final Resource resource = broker.resolve(RESOURCE_NAMESPACE_APIGEE_MANAGEMENT_API, singleSpecUrl);

        return resource.getNodeData().toString();

    }

}
