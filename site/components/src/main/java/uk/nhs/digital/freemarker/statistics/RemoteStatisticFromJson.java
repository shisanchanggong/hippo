package uk.nhs.digital.freemarker.statistics;

import org.hippoecm.hst.site.HstServices;
import org.onehippo.cms7.crisp.api.broker.ResourceServiceBroker;
import org.onehippo.cms7.crisp.api.resource.Resource;
import org.onehippo.cms7.crisp.hst.module.CrispHstServices;
import uk.nhs.digital.freemarker.AbstractRemoteContent;

import java.net.URL;

public class RemoteStatisticFromJson extends AbstractRemoteContent {

    protected Object getContentObjectFrom(URL url) {
        ResourceServiceBroker broker =  CrispHstServices.getDefaultResourceServiceBroker(HstServices.getComponentManager());
        Resource r = broker.resolve("statisticsRestResourceResolver", url.toString());
        return broker.getResourceBeanMapper("statisticsRestResourceResolver").map(r, Statistic.class);
    }

}
