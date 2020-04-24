<#ftl output_format="HTML">

<#-- @ftlvariable name="section" type="uk.nhs.digital.website.beans.DynamicChartSection" -->

<#macro dynamicChartSection section size>
    <figure>
        <div id="chart-${section.uniqueId}" style="width:100%; height:${size}px;"></div>
    </figure>
    <script type="text/javascript">
        Highcharts.chart
        ('chart-${section.uniqueId}', { chart: {
                type: '${section.type?lower_case}',
                alignTicks: false,
            },
            <#if section.YTitle?has_content>
            yAxis: [{
                title: {
                    text: "${section.YTitle}"
                },
                gridLineDashStyle: 'shortdash',
                gridLineWidth: 1
            }],
            </#if>
            <#if section.XTitle?has_content>
            xAxis: [{
                title: {
                    text: "${section.XTitle}"
                },
                gridLineDashStyle: 'shortdash',
                gridLineWidth: 1
            }],
            </#if>
            title: {
                text: "${section.title}"
            },
            series: [<#list section.seriesItem as item>{
                type: '${item.type}',
                name: '${item.name}',
            }<#if item?is_last><#else>,</#if></#list>],
            data: {
                csvURL: '${section.url}',
                enablePolling: false,
                firstRowAsNames: true
            }
        });
    </script>
</#macro>
