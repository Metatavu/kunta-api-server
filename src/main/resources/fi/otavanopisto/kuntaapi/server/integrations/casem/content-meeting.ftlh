<#if meetingTitle??>
  <h1>${meetingTitle}</h1>
</#if>

<#if memoApproved>
  <p>Pöytäkirja on tarkastettu </p>
</#if>

<h2>Pöytäkirja</h2>

<#if attachments??>
  <#list attachments>
    <#items as attachment>
      <p>${attachment}</p>
    </#items>
  </#list>
</#if>

<#if agendaAttachments??>
  <#list agendaAttachments>
    <#items as agendaAttachment>
      <p>${agendaAttachment}</p>
    </#items>
  </#list>
</#if>

<#list itemLinks>
    <ul>
    <#items as itemLink>
      <li class="<#if itemLink.hasAttachments>casem-has-attachments</</#if>">
        <a href="${itemLink.slug}">
          <#if itemLink.article??> § ${itemLink.article} </#if>${itemLink.text}
        </a>
      </li>
    </#items>
   </ul>
</#list>

<#if councilmen??>
  <#if councilmen.members??>
    <#list councilmen.members>
      <h3>Paikalla olleet jäsenet</h3>
      <ul>
        <#items as member>
          <li>
            <#if member.name??>${member.name}</#if>
            <#if member.name??><#if member.title??> - </#if></#if>
            <#if member.title??>${member.title}</#if>
          </li>
        </#items>
      </ul>
    </#list>
  </#if>
  
  <#if councilmen.others??>
    <#list councilmen.others>
      <h3>Muut paikalla olleet</h3>
      <ul>
        <#items as member>
          <li>
            <#if member.name??>${member.name}</#if>
            <#if member.name??><#if member.title??> - </#if></#if>
            <#if member.title??>${member.title}</#if>
          </li>
        </#items>
      </ul>
    </#list>
  </#if>
  
  <#if councilmen.away??>
    <#list councilmen.away>
      <h3>Poissa</h3>
      <ul>
        <#items as member>
          <li>
            <#if member.name??>${member.name}</#if>
            <#if member.name??><#if member.title??> - </#if></#if>
            <#if member.title??>${member.title}</#if>
          </li>
        </#items>
      </ul>
    </#list>
  </#if>
</#if>