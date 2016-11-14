package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.NewsArticleId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.AttachmentData;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.NewsProvider;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.rest.model.Attachment;
import fi.otavanopisto.kuntaapi.server.rest.model.NewsArticle;
import fi.otavanopisto.mwp.client.ApiResponse;
import fi.otavanopisto.mwp.client.model.Attachment.MediaTypeEnum;
import fi.otavanopisto.mwp.client.model.Post;

/**
 * News provider for management wordpress
 * 
 * @author Antti Leppä
 */
@RequestScoped
public class ManagementNewsProvider extends AbstractManagementProvider implements NewsProvider {
  
  @Inject
  private Logger logger;
  
  @Inject
  private ManagementApi managementApi;

  @Inject
  private ManagementImageLoader managementImageLoader;
  
  @Inject
  private IdController idController;
  
  @Inject
  private IdentifierController identifierController;
  
  @Override
  public List<NewsArticle> listOrganizationNews(OrganizationId organizationId, OffsetDateTime publishedBefore,
      OffsetDateTime publishedAfter, Integer firstResult, Integer maxResults) {
    
    String context = null;
    Integer page = null;
    Integer perPage = maxResults;
    String search = null;
    LocalDateTime after = toLocalDateTime(publishedAfter);
    List<String> author = null;
    List<String> authorExclude = null;
    LocalDateTime before = toLocalDateTime(publishedAfter); 
    List<String> exclude = null;
    List<String> include = null;
    Integer offset = firstResult;
    String order = null; 
    String orderby = null;
    String slug = null;
    String status = null;
    String filter = null;
    List<String> categories = null;
    List<String> tags = null;

    ApiResponse<List<Post>> postResponse = managementApi.getApi(organizationId).wpV2PostsGet(context, page, perPage, search, after, author, authorExclude, before, exclude, include,
        offset, order, orderby, slug, status, filter, categories, tags);
    if (!postResponse.isOk()) {
      logger.severe(String.format("Post listing failed on [%d] %s", postResponse.getStatus(), postResponse.getMessage()));
    } else {
      return translateNewsArticles(postResponse.getResponse());
    }
    
    return Collections.emptyList();
  }

  @Override
  public NewsArticle findOrganizationNewsArticle(OrganizationId organizationId, NewsArticleId newsArticleId) {
    Post post = findPostByArticleId(organizationId, newsArticleId);
    if (post != null) {
      return translateNewsArticle(post);
    }

    return null;
  }

  @Override
  public List<Attachment> listNewsArticleImages(OrganizationId organizationId, NewsArticleId newsArticleId) {
    Post post = findPostByArticleId(organizationId, newsArticleId);
    if (post != null) {
      Integer featuredMediaId = post.getFeaturedMedia();
      if (featuredMediaId != null) {
        fi.otavanopisto.mwp.client.model.Attachment featuredMedia = findMedia(organizationId, featuredMediaId);
        if ((featuredMedia != null) && (featuredMedia.getMediaType() == MediaTypeEnum.IMAGE)) {
          return Collections.singletonList(translateAttachment(featuredMedia));
        }
      }
    }

    return Collections.emptyList();
  }

  @Override
  public Attachment findNewsArticleImage(OrganizationId organizationId, NewsArticleId newsArticleId,
      AttachmentId attachmentId) {
    
    Post post = findPostByArticleId(organizationId, newsArticleId);
    if (post != null) {
      Integer featuredMediaId = post.getFeaturedMedia();
      if (featuredMediaId != null) {
        AttachmentId managementAttachmentId = getImageAttachmentId(featuredMediaId);
        if (!idController.idsEqual(attachmentId, managementAttachmentId)) {
          return null;
        }
        
        fi.otavanopisto.mwp.client.model.Attachment attachment = findMedia(organizationId, featuredMediaId);
        if (attachment != null) {
          return translateAttachment(attachment);
        }
      }
    }

    return null;
  }

  @Override
  public AttachmentData getNewsArticleImageData(OrganizationId organizationId, NewsArticleId newsArticleId,
      AttachmentId attachmentId, Integer size) {
    
    Integer mediaId = getMediaId(attachmentId);
    if (mediaId == null) {
      return null;
    }
    
    fi.otavanopisto.mwp.client.model.Attachment featuredMedia = findMedia(organizationId, mediaId);
    if (featuredMedia.getMediaType() == MediaTypeEnum.IMAGE) {
      AttachmentData imageData = managementImageLoader.getImageData(featuredMedia.getSourceUrl());
      
      if (size != null) {
        return scaleImage(imageData, size);
      } else {
        return imageData;
      }
      
    }
    
    return null;
  }
  
  private Post findPostByArticleId(OrganizationId organizationId, NewsArticleId newsArticleId) {
    NewsArticleId kuntaApiId = idController.translateNewsArticleId(newsArticleId, ManagementConsts.IDENTIFIER_NAME);
    if (kuntaApiId == null) {
      logger.severe(String.format("Failed to convert %s into MWP id", newsArticleId.toString()));
      return null;
    }
    
    ApiResponse<Post> response = managementApi.getApi(organizationId).wpV2PostsIdGet(kuntaApiId.getId(), null);
    if (!response.isOk()) {
      logger.severe(String.format("Finding post failed on [%d] %s", response.getStatus(), response.getMessage()));
    } else {
      return response.getResponse();
    }
    
    return null;
  }

  private List<NewsArticle> translateNewsArticles(List<Post> posts) {
    List<NewsArticle> result = new ArrayList<>();
    
    for (Post post : posts) {
      result.add(translateNewsArticle(post));
    }
    
    return result;
  }

  private NewsArticle translateNewsArticle(Post post) {
    NewsArticle newsArticle = new NewsArticle();
    
    NewsArticleId postId = new NewsArticleId(ManagementConsts.IDENTIFIER_NAME, String.valueOf(post.getId()));
    NewsArticleId kuntaApiId = idController.translateNewsArticleId(postId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiId == null) {
      logger.info(String.format("Found new news article %d", post.getId()));
      Identifier newIdentifier = identifierController.createIdentifier(postId);
      kuntaApiId = new NewsArticleId(KuntaApiConsts.IDENTIFIER_NAME, newIdentifier.getKuntaApiId());
    }
    
    newsArticle.setAbstract(post.getExcerpt().getRendered());
    newsArticle.setContents(post.getContent().getRendered());
    newsArticle.setId(kuntaApiId.getId());
    newsArticle.setPublished(toOffsetDateTime(post.getDate()));
    newsArticle.setTitle(post.getTitle().getRendered());
    
    return newsArticle;
  }
  
  private OffsetDateTime toOffsetDateTime(LocalDateTime date) {
    if (date == null) {
      return null;
    }
    
    return date.atZone(ZoneId.systemDefault()).toOffsetDateTime();
  }
  
  private LocalDateTime toLocalDateTime(OffsetDateTime dateTime) {
    if (dateTime == null) {
      return null;
    }
    
    return dateTime.toLocalDateTime();   
  }

}
