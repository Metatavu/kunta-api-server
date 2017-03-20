package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.ejb.TimerService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import fi.metatavu.management.client.ApiResponse;
import fi.metatavu.management.client.DefaultApi;
import fi.metatavu.management.client.model.Post;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.discover.IdUpdater;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.NewsArticleId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.management.tasks.OrganizationNewsArticlesTaskQueue;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask.Operation;
import fi.otavanopisto.kuntaapi.server.tasks.OrganizationEntityUpdateTask;
import fi.otavanopisto.kuntaapi.server.tasks.TaskRequest;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class ManagementNewsArticleIdUpdater extends IdUpdater {

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
  private Event<TaskRequest> taskRequest;
  
  @Inject
  private OrganizationNewsArticlesTaskQueue organizationNewsArticlesTaskQueue;

  @Resource
  private TimerService timerService;

  @Override
  public String getName() {
    return "management-news-article-ids";
  }
  
  @Override
  public void timeout() {
    OrganizationEntityUpdateTask task = organizationNewsArticlesTaskQueue.next();
    if (task != null) {
      updateManagementPosts(task.getOrganizationId());
    } else if (organizationNewsArticlesTaskQueue.isEmpty()) {
      organizationNewsArticlesTaskQueue.enqueueTasks(organizationSettingController.listOrganizationIdsWithSetting(ManagementConsts.ORGANIZATION_SETTING_BASEURL));
    }
  }
  
  @Override
  public TimerService getTimerService() {
    return timerService;
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
      taskRequest.fire(new TaskRequest(false, new IdTask<NewsArticleId>(Operation.UPDATE, newsArticleId, (long) i)));
    }
  }
  
  private void checkRemovedManagementPosts(DefaultApi api, OrganizationId organizationId) {
    List<NewsArticleId> newsArticleIds = identifierController.listOrganizationNewsArticleIdsBySource(organizationId, ManagementConsts.IDENTIFIER_NAME);
    for (NewsArticleId newsArticleId : newsArticleIds) {
      NewsArticleId managementArticleId = idController.translateNewsArticleId(newsArticleId, ManagementConsts.IDENTIFIER_NAME);
      if (managementArticleId != null) {
        ApiResponse<Post> response = api.wpV2PostsIdGet(managementArticleId.getId(), null, null, null);
        int status = response.getStatus();
        // If status is 404 the post has been removed and if its a 403 its either trashed or unpublished.
        // In both cases the post should not longer be available throught API
        if (status == 404 || status == 403) {
          taskRequest.fire(new TaskRequest(false, new IdTask<NewsArticleId>(Operation.REMOVE, managementArticleId)));
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
