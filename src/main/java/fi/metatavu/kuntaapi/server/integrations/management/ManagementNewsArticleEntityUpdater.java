package fi.metatavu.kuntaapi.server.integrations.management;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import fi.metatavu.kuntaapi.server.rest.model.NewsArticle;
import fi.metatavu.management.client.ApiResponse;
import fi.metatavu.management.client.DefaultApi;
import fi.metatavu.management.client.model.Attachment;
import fi.metatavu.management.client.model.Category;
import fi.metatavu.management.client.model.Post;
import fi.metatavu.management.client.model.Tag;
import fi.metatavu.kuntaapi.server.cache.ModificationHashCache;
import fi.metatavu.kuntaapi.server.controllers.IdentifierController;
import fi.metatavu.kuntaapi.server.controllers.IdentifierRelationController;
import fi.metatavu.kuntaapi.server.discover.EntityUpdater;
import fi.metatavu.kuntaapi.server.id.AttachmentId;
import fi.metatavu.kuntaapi.server.id.IdController;
import fi.metatavu.kuntaapi.server.id.NewsArticleId;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.images.ScaledImageStore;
import fi.metatavu.kuntaapi.server.index.IndexRemoveNewsArticle;
import fi.metatavu.kuntaapi.server.index.IndexRemoveRequest;
import fi.metatavu.kuntaapi.server.index.IndexRequest;
import fi.metatavu.kuntaapi.server.index.IndexableNewsArticle;
import fi.metatavu.kuntaapi.server.integrations.AttachmentData;
import fi.metatavu.kuntaapi.server.integrations.KuntaApiConsts;
import fi.metatavu.kuntaapi.server.integrations.management.client.ManagementApi;
import fi.metatavu.kuntaapi.server.integrations.management.resources.ManagementAttachmentDataResourceContainer;
import fi.metatavu.kuntaapi.server.integrations.management.resources.ManagementAttachmentResourceContainer;
import fi.metatavu.kuntaapi.server.integrations.management.tasks.NewsArticleIdTaskQueue;
import fi.metatavu.kuntaapi.server.persistence.model.Identifier;
import fi.metatavu.kuntaapi.server.resources.NewsArticleResourceContainer;
import fi.metatavu.kuntaapi.server.settings.OrganizationSettingController;
import fi.metatavu.kuntaapi.server.tasks.IdTask;
import fi.metatavu.kuntaapi.server.tasks.IdTask.Operation;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class ManagementNewsArticleEntityUpdater extends EntityUpdater<IdTask<NewsArticleId>> {

  @Inject
  private Logger logger;

  @Inject
  private OrganizationSettingController organizationSettingController; 
  
  @Inject
  private ManagementTranslator managementTranslator;
  
  @Inject
  private ManagementApi managementApi;
  
  @Inject
  private ManagementImageLoader managementImageLoader;
  
  @Inject
  private IdentifierController identifierController;
  
  @Inject
  private IdController idController;
  
  @Inject
  private IdentifierRelationController identifierRelationController;
  
  @Inject
  private NewsArticleResourceContainer newsArticleResourceContainer;

  @Inject
  private ManagementAttachmentDataResourceContainer managementAttachmentDataResourceContainer;

  @Inject
  private ManagementAttachmentResourceContainer managementAttachmentResourceContainer;
  
  @Inject
  private ModificationHashCache modificationHashCache;
  
  @Inject
  private NewsArticleIdTaskQueue newsArticleIdTaskQueue;
  
  @Inject
  private Event<IndexRequest> indexRequest;

  @Inject
  private Event<IndexRemoveRequest> indexRemoveRequest;

  @Inject
  private ScaledImageStore scaledImageStore;
  
  @Override
  public String getName() {
    return "management-news";
  }

  @Override
  public void timeout() {
    executeNextTask();
  }
  
  @Override
  public void execute(IdTask<NewsArticleId> task) {
    if (task.getOperation() == Operation.UPDATE) {
      updateManagementPost(task.getId(), task.getOrderIndex()); 
    } else if (task.getOperation() == Operation.REMOVE) {
      deleteManagementPost(task.getId());
    }
  }
  
  private void executeNextTask() {
    IdTask<NewsArticleId> task = newsArticleIdTaskQueue.next();
    if (task != null) {
      execute(task);
    }
  }
  
  private void updateManagementPost(NewsArticleId newsArticleId, Long orderIndex) {
    OrganizationId organizationId = newsArticleId.getOrganizationId();
    if (!organizationSettingController.hasSettingValue(organizationId, ManagementConsts.ORGANIZATION_SETTING_BASEURL)) {
      logger.log(Level.INFO, "Organization management baseUrl not set, skipping update"); 
      return;
    }
    
    DefaultApi api = managementApi.getApi(organizationId);
    
    ApiResponse<Post> response = api.wpV2PostsIdGet(newsArticleId.getId(), null, null, null);
    if (response.isOk()) {
      updateManagementPost(organizationId, api, response.getResponse(), orderIndex);
    } else {
      logger.warning(() -> String.format("Find organization %s post %s failed on [%d] %s", organizationId.getId(), newsArticleId.toString(), response.getStatus(), response.getMessage()));
    }
  }
  
  private void updateManagementPost(OrganizationId organizationId, DefaultApi api, Post managementPost, Long orderIndex) {
    List<String> managementCategoryIds = managementPost.getCategories();
    if (managementCategoryIds == null) {
      managementCategoryIds = Collections.emptyList();
    }
    
    List<String> managementTagIds = managementPost.getTags();
    if (managementTagIds == null) {
      managementTagIds = Collections.emptyList();
    }
    
    Integer postMenuOrder = managementApi.getPostMenuOrder(organizationId, managementPost.getId());
    List<String> tags = resolvePostTags(api, managementCategoryIds, managementTagIds);
    
    OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(organizationId, KuntaApiConsts.IDENTIFIER_NAME);
    NewsArticleId newsArticleId = new NewsArticleId(kuntaApiOrganizationId, ManagementConsts.IDENTIFIER_NAME, String.valueOf(managementPost.getId()));

    Identifier identifier = identifierController.acquireIdentifier(orderIndex, newsArticleId);
    identifierRelationController.setParentId(identifier, kuntaApiOrganizationId);
    
    NewsArticleId kuntaApiNewsArticleId = new NewsArticleId(kuntaApiOrganizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
    NewsArticle newsArticle = managementTranslator.translateNewsArticle(kuntaApiNewsArticleId, tags, managementPost);
    if (newsArticle == null) {
      logger.severe(() -> String.format("Failed to translate news article %d", managementPost.getId()));
      return;
    }
    
    newsArticleResourceContainer.put(kuntaApiNewsArticleId, newsArticle);
    modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(newsArticle));
    indexRequest.fire(new IndexRequest(createIndexableNewsArticle(kuntaApiOrganizationId, kuntaApiNewsArticleId, newsArticle.getSlug(),
        newsArticle.getTitle(), newsArticle.getAbstract(), newsArticle.getContents(), newsArticle.getTags(), 
        newsArticle.getPublished(), postMenuOrder, orderIndex)));
    
    List<AttachmentId> existingAttachmentIds = identifierRelationController.listAttachmentIdsBySourceAndParentId(ManagementConsts.IDENTIFIER_NAME, kuntaApiNewsArticleId);
    if (managementPost.getFeaturedMedia() != null && managementPost.getFeaturedMedia() > 0) {
      AttachmentId attachmentId = updateFeaturedMedia(organizationId, identifier, api, managementPost.getFeaturedMedia()); 
      if (attachmentId != null) {
        existingAttachmentIds.remove(attachmentId);
      }
    }
    
    for (AttachmentId existingAttachmentId : existingAttachmentIds) {
      identifierRelationController.removeChild(kuntaApiNewsArticleId, existingAttachmentId);
    }
  }
  
  private List<String> resolvePostTags(DefaultApi api, List<String> managementCategoryIds, List<String> managementTagIds) {
    List<String> tags = new ArrayList<>(managementCategoryIds.size() + managementTagIds.size());
    
    for (String managementCategoryId : managementCategoryIds) {
      String categoryName = resolveCategoryName(api, managementCategoryId);
      if (StringUtils.isNotBlank(categoryName)) {
        tags.add(categoryName);
      }
    }
    
    for (String managementTagId : managementTagIds) {
      String tagName = resolveTagName(api, managementTagId);
      if (StringUtils.isNotBlank(tagName)) {
        tags.add(tagName);
      }
    }
    
    return tags;
  }

  private String resolveCategoryName(DefaultApi api, String managementCategoryId) {
    ApiResponse<Category> managementCategoryResponse = api.wpV2CategoriesIdGet(managementCategoryId, null, null);
    if (managementCategoryResponse.isOk()) {
      return managementCategoryResponse.getResponse().getName();
    } else {
      logger.log(Level.WARNING, () -> String.format("Failed to retrieve category %s from management service", managementCategoryId));
    }
    
    return null;
  }
  
  private String resolveTagName(DefaultApi api, String managementTagId) {
    ApiResponse<Tag> managementTagResponse = api.wpV2TagsIdGet(managementTagId, null, null);
    if (managementTagResponse.isOk()) {
      return managementTagResponse.getResponse().getName();
    } else {
      logger.log(Level.WARNING, () -> String.format("Failed to retrieve tag %s from management service", managementTagId));
    }
    
    return null;
  }
  
  private AttachmentId updateFeaturedMedia(OrganizationId organizationId, Identifier newsArticleIdentifier, DefaultApi api, Integer featuredMedia) {
    ApiResponse<fi.metatavu.management.client.model.Attachment> response = api.wpV2MediaIdGet(String.valueOf(featuredMedia), null, null);
    if (!response.isOk()) {
      logger.severe(() -> String.format("Finding media failed on [%d] %s", response.getStatus(), response.getMessage()));
      return null;
    } else {
      Attachment managementAttachment = response.getResponse();
      AttachmentId managementAttachmentId = managementTranslator.createManagementAttachmentId(organizationId, managementAttachment.getId(), ManagementConsts.ATTACHMENT_TYPE_NEWS);
      Long orderIndex = 0l;
      
      Identifier identifier = identifierController.acquireIdentifier(orderIndex, managementAttachmentId);
      identifierRelationController.addChild(newsArticleIdentifier, identifier);

      AttachmentId kuntaApiAttachmentId = new AttachmentId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
      fi.metatavu.kuntaapi.server.rest.model.Attachment attachment = managementTranslator.translateAttachment(kuntaApiAttachmentId, managementAttachment, ManagementConsts.ATTACHMENT_TYPE_NEWS);
      if (attachment == null) {
        logger.severe(() -> String.format("Failed to translate news article attachment %d", featuredMedia));
        return null;
      }
      
      managementAttachmentResourceContainer.put(kuntaApiAttachmentId, attachment);
      
      AttachmentData imageData = managementImageLoader.getImageData(managementAttachment.getSourceUrl());
      if (imageData != null) {
        String dataHash = DigestUtils.md5Hex(imageData.getData());
        if (!dataHash.equals(modificationHashCache.get(identifier.getKuntaApiId()))) {
          modificationHashCache.put(identifier.getKuntaApiId(), dataHash);
          managementAttachmentDataResourceContainer.put(kuntaApiAttachmentId, imageData);
          scaledImageStore.purgeStoredImages(kuntaApiAttachmentId);
        }
      }
      
      return kuntaApiAttachmentId;
    }
  }

  private void deleteManagementPost(NewsArticleId managementNewsArticleId) {
    OrganizationId organizationId = managementNewsArticleId.getOrganizationId();
    
    Identifier newsArticleIdentifier = identifierController.findIdentifierById(managementNewsArticleId);
    if (newsArticleIdentifier != null) {
      NewsArticleId kuntaApiNewsArticleId = new NewsArticleId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, newsArticleIdentifier.getKuntaApiId());
      modificationHashCache.clear(newsArticleIdentifier.getKuntaApiId());
      newsArticleResourceContainer.clear(kuntaApiNewsArticleId);
      identifierController.deleteIdentifier(newsArticleIdentifier);
      
      IndexRemoveNewsArticle indexRemove = new IndexRemoveNewsArticle();
      indexRemove.setNewsArticleId(kuntaApiNewsArticleId.getId());
      
      indexRemoveRequest.fire(new IndexRemoveRequest(indexRemove));
    }
  }

  @SuppressWarnings ("squid:S00107")
  private IndexableNewsArticle createIndexableNewsArticle(OrganizationId kuntaApiOrganizationId, NewsArticleId kuntaApiNewsArticleId, String slug, String title, String newsAbstract, String contents, List<String> tags, OffsetDateTime published, Integer postMenuOrder, Long orderIndex) {
    
    IndexableNewsArticle indexableNewsArticle = new IndexableNewsArticle();
    indexableNewsArticle.setContents(contents);
    indexableNewsArticle.setNewsAbstract(newsAbstract);
    indexableNewsArticle.setNewsArticleId(kuntaApiNewsArticleId.getId());
    indexableNewsArticle.setOrderIndex(orderIndex);
    indexableNewsArticle.setOrganizationId(kuntaApiOrganizationId.getId());
    indexableNewsArticle.setPublished(published);
    indexableNewsArticle.setTags(tags);
    indexableNewsArticle.setTitle(title);
    indexableNewsArticle.setSlug(slug);
    indexableNewsArticle.setOrderNumber(postMenuOrder);
    
    return indexableNewsArticle;
  }

}
