package fi.metatavu.kuntaapi.server.integrations.management;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.controllers.IdentifierController;
import fi.metatavu.kuntaapi.server.discover.IdDiscoverJob;
import fi.metatavu.kuntaapi.server.id.IdController;
import fi.metatavu.kuntaapi.server.id.NewsArticleId;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.integrations.management.client.ManagementApi;
import fi.metatavu.kuntaapi.server.integrations.management.tasks.NewsArticleIdTaskQueue;
import fi.metatavu.kuntaapi.server.integrations.management.tasks.OrganizationNewsArticlesTaskQueue;
import fi.metatavu.kuntaapi.server.settings.OrganizationSettingController;
import fi.metatavu.kuntaapi.server.tasks.IdTask;
import fi.metatavu.kuntaapi.server.tasks.IdTask.Operation;
import fi.metatavu.kuntaapi.server.tasks.OrganizationEntityUpdateTask;
import fi.metatavu.management.client.ApiResponse;
import fi.metatavu.management.client.DefaultApi;
import fi.metatavu.management.client.model.Post;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class ManagementNewsArticleIdDiscoverJob extends IdDiscoverJob {

  private static final int PER_PAGE = 100;
  private static final int MAX_PAGES = 10;
  
  @Inject
  private Logger logger;
  
  @Inject
  private ManagementApi managementApi;
  
  @Inject
  private OrganizationSettingController organizationSettingController; 

  @Inject
  private IdentifierController identifierController;
  
  @Inject
  private IdController idController;
  
  @Inject
  private NewsArticleIdTaskQueue newsArticleIdTaskQueue;
  
  @Inject
  private OrganizationNewsArticlesTaskQueue organizationNewsArticlesTaskQueue;

  @Override
  public String getName() {
    return "management-news-article-ids";
  }
  
  @Override
  public void timeout() {
    OrganizationEntityUpdateTask task = organizationNewsArticlesTaskQueue.next();
    if (task != null) {
      updateManagementPosts(task.getOrganizationId());
    } else if (organizationNewsArticlesTaskQueue.isEmptyAndLocalNodeResponsible()) {
      organizationNewsArticlesTaskQueue.enqueueTasks(organizationSettingController.listOrganizationIdsWithSetting(ManagementConsts.ORGANIZATION_SETTING_BASEURL));
    }
  }
  
  private void updateManagementPosts(OrganizationId organizationId) {
    if (!organizationSettingController.hasSettingValue(organizationId, ManagementConsts.ORGANIZATION_SETTING_BASEURL)) {
      logger.log(Level.INFO, "Organization management baseUrl not set, skipping update"); 
      return;
    }
    
    DefaultApi api = managementApi.getApi(organizationId);    

    checkRemovedManagementPosts(api, organizationId);

    List<Post> managementPosts = new ArrayList<>();
    
    int page = 1;
    do {
      List<Post> pagePosts = listManagementPosts(api, organizationId, page);
      managementPosts.addAll(pagePosts);
      if (pagePosts.isEmpty() || pagePosts.size() < PER_PAGE) {
        break;
      } else {
        page++;
      }
    } while (page < MAX_PAGES);
    
    for (int i = 0, l = managementPosts.size(); i < l; i++) {
      Post managementPost = managementPosts.get(i);
      NewsArticleId newsArticleId = new NewsArticleId(organizationId, ManagementConsts.IDENTIFIER_NAME, String.valueOf(managementPost.getId()));
      newsArticleIdTaskQueue.enqueueTaskSync(new IdTask<NewsArticleId>(false, Operation.UPDATE, newsArticleId, (long) i));
    }
  }
  
  private void checkRemovedManagementPosts(DefaultApi api, OrganizationId organizationId) {
    List<NewsArticleId> newsArticleIds = identifierController.listOrganizationNewsArticleIdsBySource(organizationId, ManagementConsts.IDENTIFIER_NAME);
    for (NewsArticleId newsArticleId : newsArticleIds) {
      NewsArticleId managementArticleId = idController.translateNewsArticleId(newsArticleId, ManagementConsts.IDENTIFIER_NAME);
      if (managementArticleId != null) {
        ApiResponse<Object> response = api.wpV2PostsIdHead(managementArticleId.getId(), null, null, null);
        int status = response.getStatus();
        // If status is 404 the post has been removed and if its a 403 its either trashed or unpublished.
        // In both cases the post should not longer be available throught API
        if (status == 404 || status == 403) {
          newsArticleIdTaskQueue.enqueueTask(new IdTask<NewsArticleId>(false, Operation.REMOVE, newsArticleId));
        }
      }
    }
  }
  
  private List<Post> listManagementPosts(DefaultApi api, OrganizationId organizationId, Integer page) {
    ApiResponse<List<Post>> response = api.wpV2PostsGet(null, page, PER_PAGE, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    if (response.isOk()) {
      return response.getResponse();
    } else {
      logger.warning(String.format("Listing organization %s posts failed on [%d] %s", organizationId.getId(), response.getStatus(), response.getMessage()));
    }
    
    return Collections.emptyList();
  }

}
