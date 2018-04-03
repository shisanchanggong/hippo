<#ftl output_format="HTML">
<#include "../include/imports.ftl">
<#include "macro/searchTabsComponent.ftl">
<#include "macro/siteHeader.ftl">
<#include "macro/metaTags.ftl">

<#-- Add meta tags -->
<@metaTags></@metaTags>

<!DOCTYPE html>
<html lang="en" class="no-js">

<#include "app-layout-head.ftl">

<body class="debugs">
    <#-- Add site header with the search bar -->
    <@siteHeader false></@siteHeader>

    <#include "pubs_breadcrumb.ftl"/>

    <@hst.include ref="top"/>

    <div class="article article--search-results" aria-label="Search Results">
        <div class="grid-wrapper grid-wrapper--article">
            <div class="grid-row">
                <div class="column column--one-third page-block page-block--sidebar">
                    <div class="article-section-nav">
                        <!-- Facets -->
                        <@hst.include ref="left" />
                    </div>
                </div>

                <div class="column column--two-thirds page-block page-block--main">
                    <@searchTabsComponent contentNames=hstResponseChildContentNames></@searchTabsComponent>
                    <@hst.include ref="main"/>
                </div>
            </div>
        </div>
    </div>

    <#include "site-footer.ftl"/>

    <#include "cookie-banner.ftl"/>

    <#include "scripts/live-engage-chat.ftl"/>

    <@hst.headContributions categoryIncludes="htmlBodyEnd, scripts" xhtml=true/>
</body>

</html>
