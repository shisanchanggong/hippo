package uk.nhs.digital.jobs;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.spy;
import static org.powermock.api.mockito.PowerMockito.*;
import static uk.nhs.digital.test.util.FileUtils.readFileInClassPath;
import static uk.nhs.digital.test.util.JcrTestUtils.BloomReachJcrDocumentVariantType.DRAFT;
import static uk.nhs.digital.test.util.JcrTestUtils.*;
import static uk.nhs.digital.test.util.MockJcrRepoProvider.initJcrRepoFromYaml;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.collect.ImmutableMap;
import org.apache.sling.testing.mock.jcr.MockJcr;
import org.hippoecm.repository.api.Document;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.onehippo.cms7.crisp.api.resource.ResourceResolver;
import org.onehippo.cms7.crisp.core.resource.jackson.SimpleJacksonRestTemplateResourceResolver;
import org.onehippo.cms7.crisp.mock.broker.MockResourceServiceBroker;
import org.onehippo.cms7.crisp.mock.module.MockCrispHstServices;
import org.onehippo.forge.content.exim.core.DocumentManager;
import org.onehippo.repository.documentworkflow.DocumentVariant;
import org.onehippo.repository.scheduling.RepositoryJobExecutionContext;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;
import uk.nhs.digital.JcrDocumentUtils;
import uk.nhs.digital.apispecs.jobs.ApiSpecSyncFromApigeeJob;

import javax.jcr.Node;
import javax.jcr.Session;

@Ignore("WIP")
@RunWith(PowerMockRunner.class)
@PrepareForTest({JcrDocumentUtils.class, ApiSpecSyncFromApigeeJob.class})
@PowerMockIgnore({"javax.net.ssl.*", "javax.crypto.*", "javax.management.*"})
public class ApiSpecSyncFromApigeeJobIntegrationTest {

    private static final String PROPERTY_NAME_WEBSITE_HTML = "website:html";

    // Test data
    private static final String TEST_SPEC_ID = "269326";
    private static final String TEST_DATA_FILES_PATH = "/test-data/api-specifications/ApiSpecConversionJobIntegrationTest/";

    @Rule public ExpectedException expectedException = ExpectedException.none();

    @Rule public WireMockRule wireMock = new WireMockRule(wireMockConfig().dynamicPort());

    // JCR repo access
    private Session session;
    private Node repositoryRootNode;

    private RepositoryJobExecutionContext repositoryJobExecutionContext;

    // Apigee URLs
    private String apigeeAllSpecsUrlPath = "/dapi/api/organizations/test-org/specs/folder/home/specs";
    private String apigeeSingleSpecUrlPathTemplate = "/dapi/api/organizations/test-org/specs/doc/{specificationId}/content";
    private String oauthTokenUrlPath = "/oauth/token";

    private ApiSpecSyncFromApigeeJob apiSpecSyncFromApigeeJob;
    private ConfigurableEnvironment springApplicationContextEnvironment;

    @Before
    public void setUp() throws Exception {

        setUpCmsToApigeeAccessConfig();
        setUpCrispApi();

        session = spy(MockJcr.newSession());
        doNothing().when(session).logout(); // prevent the job from closing the session - it's needed in assertions

        repositoryJobExecutionContext = mock(RepositoryJobExecutionContext.class);
        given(repositoryJobExecutionContext.createSystemSession()).willReturn(session);

        apiSpecSyncFromApigeeJob = new ApiSpecSyncFromApigeeJob();
    }

    @Test
    public void downloadsSpecUpdateFromApigeeAndPublishesOnWebsite() throws Exception {

        // given
        apigeeReturnsAccessToken();
        apigeeReturnsListOfSpecsIncludingTheOneConfiguredInCms();
        apigeeReturnsDetailsOfTheSpecConfiguredInCms();
        cmsContainsSpecDocMatchingUpdatedSpecInApigee();

        // when
        apiSpecSyncFromApigeeJob.execute(repositoryJobExecutionContext);

        // then
        verifyHtmlGeneratedFromApigeeSpecWasSetOnApiSpecificationDocument();
        verifyApiSpecificationDocumentWasPublished();
        verifyLogsOff();
    }

    private void verifyLogsOff() {
        then(session).should().logout();
    }

    private void setUpCmsToApigeeAccessConfig() {

        final UriComponentsBuilder baseWiremockUrl =
            UriComponentsBuilder.newInstance().scheme("http").host("localhost").port(wireMock.port());

        final String apigeeAllSpecsUrl = baseWiremockUrl.cloneBuilder().path(apigeeAllSpecsUrlPath).toUriString();
        final String apigeeSingleSpecUrlTemplate = baseWiremockUrl.cloneBuilder().toUriString() + apigeeSingleSpecUrlPathTemplate;
        final String oauthTokenUrl = baseWiremockUrl.cloneBuilder().path(oauthTokenUrlPath).toUriString();

        final String oauthUsername = "oauthUsername";
        final String oauthPassword = "oauthPassword";
        final String oauthBasicAuthToken = "ZWRnZWNsaTplZGdlY2xpc2VjcmV0";
        final String oauthOtpKey = "JBSWY3DPEHPK3PXP";

        mockStatic(System.class);

        // rktodo constants
        given(System.getProperty("devzone.apigee.resources.specs.all.url")).willReturn(apigeeAllSpecsUrl);
        given(System.getProperty("devzone.apigee.resources.specs.individual.url")).willReturn(apigeeSingleSpecUrlTemplate);
        given(System.getProperty("devzone.apigee.oauth.token.url")).willReturn(oauthTokenUrl);

        given(System.getenv("DEVZONE_APIGEE_OAUTH_USERNAME")).willReturn(oauthUsername);
        given(System.getenv("DEVZONE_APIGEE_OAUTH_PASSWORD")).willReturn(oauthPassword);
        given(System.getenv("DEVZONE_APIGEE_OAUTH_BASICAUTHTOKEN")).willReturn(oauthBasicAuthToken);
        given(System.getenv("DEVZONE_APIGEE_OAUTH_OTPKEY")).willReturn(oauthOtpKey);

        springApplicationContextEnvironment = new MockEnvironment()
            .withProperty("devzone.apigee.resources.specs.all.url", apigeeAllSpecsUrl)
            .withProperty("devzone.apigee.oauth.token.url", oauthTokenUrl)
            .withProperty("devzone.apigee.resources.specs.individual.url", apigeeSingleSpecUrlTemplate)
            .withProperty("DEVZONE_APIGEE_OAUTH_USERNAME", oauthUsername)
            .withProperty("DEVZONE_APIGEE_OAUTH_PASSWORD", oauthPassword)
            .withProperty("DEVZONE_APIGEE_OAUTH_BASICAUTHTOKEN", oauthBasicAuthToken)
            .withProperty("DEVZONE_APIGEE_OAUTH_OTPKEY", oauthOtpKey);
    }

    private void setUpCrispApi() {

        // See https://documentation.bloomreach.com/14/library/concepts/crisp-api/unit-testing.html

        // rktodo - parent bean (normally defined in hippo-addon-crisp-core/13.4.2/hippo-addon-crisp-core-13.4.2.jar!/META-INF/spring-assembly/addon/crisp/crisp-addon-resource-resolvers.xml

        final ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext();
        applicationContext.setEnvironment(springApplicationContextEnvironment);
        applicationContext.setConfigLocations(
                fromTestDataLocation("crisp-spring-context-properties-support.xml"),
                fromTestDataLocation("crisp-spring-context.xml") // rktodo - XML in XML file (or read from YAML?)
        );
        applicationContext.refresh();

        final String apigeeManagementApiCrispApiNamespace = "apigeeManagementApi";

        final SimpleJacksonRestTemplateResourceResolver simpleJacksonRestTemplateResourceResolver =
            applicationContext.getBean(apigeeManagementApiCrispApiNamespace, SimpleJacksonRestTemplateResourceResolver.class);

        final ImmutableMap<String, ResourceResolver> resourceResolverMap = ImmutableMap.<String, ResourceResolver>builder()
            .put(apigeeManagementApiCrispApiNamespace, simpleJacksonRestTemplateResourceResolver)
            .build();

        final MockResourceServiceBroker mockResourceServiceBroker = new MockResourceServiceBroker(
            resourceResolverMap
        );

        MockCrispHstServices.setDefaultResourceServiceBroker(mockResourceServiceBroker);
    }

    private void verifyHtmlGeneratedFromApigeeSpecWasSetOnApiSpecificationDocument() {

        final Node specificationHandleNode = getExistingSpecHandleNode();

        final Node documentVariantNodeDraft = getDocumentVariantNode(specificationHandleNode, DRAFT);

        final String actualSpecHtml =
            getStringProperty(documentVariantNodeDraft, PROPERTY_NAME_WEBSITE_HTML);

        final String expectedSpecHtml = codeGenGeneratedSpecificationHtml();

        assertThat(
            "CodeGen-generated specification HTML has been set on the document",
            actualSpecHtml,
            is(expectedSpecHtml)
        );
    }

    private void verifyApiSpecificationDocumentWasPublished() {

        final Node specificationHandleNode = getExistingSpecHandleNode();

        verifyStatic(JcrDocumentUtils.class);
        JcrDocumentUtils.publish(specificationHandleNode);
    }

    private Node getExistingSpecHandleNode() {
        return getRelativeNode(
            repositoryRootNode, "/content/documents/corporate-website/api-specifications-location-a/api-spec-a");
    }

    private void apigeeReturnsAccessToken() {

        wireMock.givenThat(
            post(urlEqualTo(oauthTokenUrlPath))
                .willReturn(
                    ok()
                        .withHeader("Content-Type", "application/json;charset=UTF-8")
                        .withBody(apigeeAccessTokenResponse())
                )
        );
    }

    private void apigeeReturnsListOfSpecsIncludingTheOneConfiguredInCms() {
        wireMock.givenThat(
            get(urlMatching(apigeeAllSpecsUrlPath))
                .willReturn(
                    ok()
                        .withHeader("Content-Type", "application/json;charset=UTF-8")
                        .withBody(apigeeApiSpecificationsJson())
                )
        );
    }

    private void apigeeReturnsDetailsOfTheSpecConfiguredInCms() {

        final String expectedApigeeSingleSpecUrl =
            new UriTemplate(apigeeSingleSpecUrlPathTemplate).expand(TEST_SPEC_ID).toASCIIString();

        wireMock.givenThat(
            get(urlMatching(expectedApigeeSingleSpecUrl))
                .willReturn(
                    ok()
                        .withHeader("Content-Type", "application/json;charset=UTF-8")
                        .withBody(apigeeApiSpecificationJson())
                )
        );
    }

    private void cmsContainsSpecDocMatchingUpdatedSpecInApigee() throws Exception {

        initJcrRepoFromYaml(session, fromTestDataLocation("specification-documents-in-cms.yml"));

        repositoryRootNode = getRootNode(session);

        final Node specDocHandleNode = getExistingSpecHandleNode();

        MockJcr.setQueryResult(session, asList(
            specDocHandleNode
        ));

        // Low-level JCR-related components mocked where it was prohibitively hard to avoid:

        final DocumentManager documentManager = mock(DocumentManager.class);

        mockStatic(JcrDocumentUtils.class);
        given(JcrDocumentUtils.documentManagerFor(session)).willReturn(documentManager);

        final Node documentVariantNodeDraft = getDocumentVariantNode(specDocHandleNode, DRAFT);

        final Document documentVariantDraft = new DocumentVariant(documentVariantNodeDraft);
        given(documentManager.obtainEditableDocument(specDocHandleNode)).willReturn(documentVariantDraft);
    }

    private String apigeeAccessTokenResponse() {
        return readFileInClassPath(fromTestDataLocation("auth-access-token-response.json"));
    }

    private String apigeeApiSpecificationsJson() {
        return testDataFromFile("specifications-in-apigee.json");
    }

    private String apigeeApiSpecificationJson() {
        return testDataFromFile("openapi-specification.json");
    }

    private String codeGenGeneratedSpecificationHtml() {
        return testDataFromFile("codegen-generated-spec.html");
    }

    private String testDataFromFile(final String testDataFileName) {
        return readFileInClassPath(fromTestDataLocation(testDataFileName));
    }

    private String fromTestDataLocation(final String fileName) {
        return TEST_DATA_FILES_PATH + fileName;
    }
}