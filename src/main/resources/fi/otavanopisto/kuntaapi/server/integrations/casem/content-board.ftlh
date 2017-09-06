<h2>Pöytäkirjat</h2>

<#if meetings??>
  <ul class="subpage-list">
    <#list meetings>
      <#items as meeting>
        <li class="subpage-list-item">
          <a href="${meeting.slug}">${meeting.title}</a>
        </li>
      </#items>
    </#list>
  </ul>
</#if>

<div class="casem-meta" data-meta-name="casem-type" data-meta-value="board"></div>