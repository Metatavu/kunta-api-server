package fi.otavanopisto.kuntaapi.server.integrations.gtfs;

import fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks.GtfsAgencyEntityTask;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks.GtfsAgencyTaskQueue;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks.GtfsRouteEntityTask;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks.GtfsRouteTaskQueue;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks.GtfsScheduleEntityTask;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks.GtfsScheduleTaskQueue;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks.GtfsStopEntityTask;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks.GtfsStopTaskQueue;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks.GtfsStopTimeEntityTask;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks.GtfsStopTimeTaskQueue;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks.GtfsTripEntityTask;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks.GtfsTripTaskQueue;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;
import javax.transaction.Transactional;


@ApplicationScoped
public class GtfsIdTaskPersistor {
  
  @Inject
  private GtfsAgencyTaskQueue agencyTaskQueue;
  
  @Inject
  private GtfsScheduleTaskQueue scheduleTaskQueue;
  
  @Inject
  private GtfsRouteTaskQueue routeTaskQueue;
  
  @Inject
  private GtfsStopTaskQueue stopTaskQueue;
  
  @Inject
  private GtfsStopTimeTaskQueue stopTimeTaskQueue;
  
  @Inject
  private GtfsTripTaskQueue tripTaskQueue;
  
  @Transactional (Transactional.TxType.REQUIRES_NEW)
  public void onGtfsAgencyUpdateRequest(@Observes (during = TransactionPhase.AFTER_COMPLETION) GtfsAgencyEntityTask event) {
    agencyTaskQueue.enqueueTask(false, event);
  }
  
  @Transactional (Transactional.TxType.REQUIRES_NEW)
  public void onGtfsStopUpdateRequest(@Observes (during = TransactionPhase.AFTER_COMPLETION) GtfsStopEntityTask event) {
    stopTaskQueue.enqueueTask(false, event);
  }
  
  @Transactional (Transactional.TxType.REQUIRES_NEW)  
  public void onGtfsStopTimeUpdateRequest(@Observes (during = TransactionPhase.AFTER_COMPLETION) GtfsStopTimeEntityTask event) {
    stopTimeTaskQueue.enqueueTask(false, event);
  }
  
  @Transactional (Transactional.TxType.REQUIRES_NEW)
  public void onGtfsTripUpdateRequest(@Observes (during = TransactionPhase.AFTER_COMPLETION) GtfsTripEntityTask event) {
    tripTaskQueue.enqueueTask(false, event);
  }
        
  @Transactional (Transactional.TxType.REQUIRES_NEW)
  public void onGtfsScheduleUpdateRequest(@Observes (during = TransactionPhase.AFTER_COMPLETION) GtfsScheduleEntityTask event) {
    scheduleTaskQueue.enqueueTask(false, event);
  }
  
  @Transactional (Transactional.TxType.REQUIRES_NEW)
  public void onGtfsRouteUpdateRequest(@Observes (during = TransactionPhase.AFTER_COMPLETION) GtfsRouteEntityTask event) {
    routeTaskQueue.enqueueTask(false, event);
  }
  
}
