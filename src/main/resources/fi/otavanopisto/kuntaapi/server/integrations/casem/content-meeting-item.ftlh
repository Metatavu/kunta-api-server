<h2><#if article??>§ ${article}</#if> <#if name??>${name}</#if><#if additionalTopic> (jatkopykälä)</#if></h2>

<#if memoApproved>
  <p class="casem-memo-approved">Pöytäkirja on tarkastettu </p>
<#else>
  <p class="casem-memo-not-approved">Esityslista on tarkastamaton</p>
</#if>

<#if caseNativeId??>
<p>${caseNativeId}</p>
</#if>

<#if historyTopics??>
  <#list historyTopics>
    <div class="casem-history-topic">
      <h3>Aikaisempi käsittely</h3>
      <div class="casem-history-topic-contents">
        <#items as historyTopic>
          <h4><#if historyTopic.party??>${historyTopic.party}</#if><#if historyTopic.partySv??>, ${historyTopic.partySv}</#if><#if historyTopic.meetingDate??>${formatDateTime(historyTopic.meetingDate, "SHORT")}</#if></h4>
          <#if historyTopic.draftsmen??>
            <#list historyTopic.draftsmen>
              <p>Valmistelija(t)</p>
              <#items as draftsman>
                <p>${draftsman.name}<#if draftsman.title??>, ${draftsman.title}</#if><#if draftsman.email??>, ${draftsman.email}</#if></p>
              </#items>
            </#list>
          </#if>
          
          <#if historyTopic.description??>
            <h5>Kuvaus</h5>
            <p>${historyTopic.description}</p>
          </#if>
          
          <#if historyTopic.decisionProposal??>
            <h5>Päätösehdotus</h5>
            <p>${historyTopic.decisionProposal}</p>
          </#if>
          
          <#if historyTopic.decision??>
            <h5>Päätös</h5>
    
            <#if historyTopic.presenters??>
              <#list historyTopic.presenters>
                <p>Esittelijä(t)</p>
                <#items as presentter>
                  <p>${presentter.name}<#if presentter.title??>, ${presentter.title}</#if><#if presentter.email??>, ${presentter.email}</#if></p>
                </#items>
              </#list>
            </#if>
            
            <p>${historyTopic.decision}</p>
          </#if>
        </#items>
      </div>
    </div>
    
  </#list>
</#if>

<#if draftsmen??>
  <#list draftsmen>
    <p>Valmistelija(t)</p>
    <#items as draftsman>
      <p>${draftsman.name}<#if draftsman.title??>, ${draftsman.title}</#if><#if draftsman.email??>, ${draftsman.email}</#if></p>
    </#items>
  </#list>
</#if>

<#if description??>
  <h3>Kuvaus</h3>
  <p>${description}</p>
</#if>

<#if decisionProposal??>
  <h3>Päätösehdotus</h3>
  <p>${decisionProposal}</p>
</#if>

<#if presenters??>
  <#list presenters>
    <p>Esittelijä(t)</p>
    <#items as presentter>
      <p>${presentter.name}<#if presentter.title??>, ${presentter.title}</#if><#if presentter.email??>, ${presentter.email}</#if></p>
    </#items>
  </#list>
</#if>

<#if decision??>
  <h3>Päätös</h3>
   <p>${decision}</p>
</#if>

<#if inform??>
  <h3>Tiedoksi</h3>
  <p>${inform}</p>
</#if>

<#if attachments??>
  <#list attachments>
    <h3>Liitteet</h3>
    <#items as attachment>
      <p>${attachment}</p>
    </#items>
  </#list>
</#if>

<hr/>

<#if correctioninstructions??>
<h3>Muutoksenhaku</h3>
<p>${correctioninstructions}</p>
</#if>

<div class="casem-meta" data-meta-name="casem-type" data-meta-value="meeting-item"/>
<#if meetingTitle??><div class="casem-meta" data-meta-name="meeting-title" data-meta-value="${meetingTitle}"/></#if>
<#if name??><div class="casem-meta" data-meta-name="meeting-item-name" data-meta-value="${name}"/></#if>
<#if article??><div class="casem-meta" data-meta-name="meeting-item-article" data-meta-value="${article}"/></#if>
<#if additionalTopic??><div class="casem-meta" data-meta-name="meeting-item-additional-topic" data-meta-value="true"/></#if>