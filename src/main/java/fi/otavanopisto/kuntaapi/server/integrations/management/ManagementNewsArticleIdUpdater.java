package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.AccessTimeout;
import javax.ejb.Asynchronous;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import fi.metatavu.management.client.ApiResponse;
import fi.metatavu.management.client.DefaultApi;
import fi.metatavu.management.client.model.Post;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.discover.IdUpdater;
import fi.otavanopisto.kuntaapi.server.discover.NewsArticleIdRemoveRequest;
import fi.otavanopisto.kuntaapi.server.discover.NewsArticleIdUpdateRequest;
import fi.otavanopisto.kuntaapi.server.discover.OrganizationIdUpdateRequest;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.NewsArticleId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class ManagementNewsArticleIdUpdater extends IdUpdater {

  private static final int WARMUP_TIME = 1000 * 10;
  private static final int TIMER_INTERVAL = 5000;
  private static final int PER_PAGE = 100;
  private static final int MAX_PAGES = 10;
  
  @Inject
  private Logger logger;
  
  @Inject
  private SystemSettingController systemSettingController;

  @Inject
  private ManagementApi managementApi;
  
  @Inject
  private OrganizationSettingController organizationSettingController; 

  @Inject
  private IdentifierController identifierController;
  
  @Inject
  private IdController idController;
  
  @Inject
  private Event<NewsArticleIdUpdateRequest> idUpdateRequest;

  @Inject
  private Event<NewsArticleIdRemoveRequest> idRemoveRequest;

  private boolean stopped;
  private List<OrganizationId> queue;
  
  @Resource
  private TimerService timerService;
  
  @PostConstruct
  public void init() {
    queue = Collections.synchronizedList(new ArrayList<>());
  }

  @Override
  public String getName() {
    return "management-news-article-ids";
  }
  
  @Override
  public void startTimer() {
    stopped = false;
    startTimer(WARMUP_TIME);
  }

  @Override
  public void stopTimer() {
    stopped = true;
  }
  
  private void startTimer(int duration) {
    TimerConfig timerConfig = new TimerConfig();
    timerConfig.setPersistent(false);
    timerService.createSingleActionTimer(duration, timerConfig);
  }
  
  @Asynchronous
  public void onOrganizationIdUpdateRequest(@Observes OrganizationIdUpdateRequest event) {
    if (!stopped) {
      OrganizationId organizationId = event.getId();
      
      if (organizationSettingController.getSettingValue(organizationId, ManagementConsts.ORGANIZATION_SETTING_BASEURL) == null) {
        return;
      }
      
      if (event.isPriority()) {
        queue.remove(organizationId);
        queue.add(0, organizationId);
      } else {
        if (!queue.contains(organizationId)) {
          queue.add(organizationId);
        }
      }
    }
  }

  @Timeout
  public void timeout(Timer timer) {
    if (!stopped) {
      if (systemSettingController.isNotTestingOrTestRunning() && !queue.isEmpty()) {
        updateManagementPosts(queue.remove(0));
      }

      startTimer(systemSettingController.inTestMode() ? 1000 : TIMER_INTERVAL);
    }
  }
  
  private void updateManagementPosts(OrganizationId organizationId) {
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
      idUpdateRequest.fire(new NewsArticleIdUpdateRequest(organizationId, newsArticleId, (long) i, false));
    }
  }
  
  private void checkRemovedManagementPosts(DefaultApi api, OrganizationId organizationId) {
    List<NewsArticleId> newsArticleIds = identifierController.listOrganizationNewsArticleIdsBySource(organizationId, ManagementConsts.IDENTIFIER_NAME);
    for (NewsArticleId newsArticleId : newsArticleIds) {
      NewsArticleId managementArticleId = idController.translateNewsArticleId(newsArticleId, ManagementConsts.IDENTIFIER_NAME);
      if (managementArticleId != null) {
        ApiResponse<Post> response = api.wpV2PostsIdGet(managementArticleId.getId(), null, null, null);
        if (response.getStatus() == 404) {
          idRemoveRequest.fire(new NewsArticleIdRemoveRequest(organizationId, managementArticleId));
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
