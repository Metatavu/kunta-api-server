<h2>Pöytäkirjat</h2>

<#if meetings??>
  <#list meetings>
    <ul class="subpage-list">
      <#items as meeting>
        <li class="subpage-list-item">
          <a href="${meeting.slug}">${meeting.title}</a>
        </li>
      </#items>
    </ul>
  </#list>
</#if>