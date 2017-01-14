<h2>Pöytäkirjat</h2>

<#if meetings??>
  <#list meetings>
    <#items as meeting>
      <li>
        <a href="${meeting.slug}">${meeting.title}</a>
      </li>
    </#items>
  </#list>
</#if>