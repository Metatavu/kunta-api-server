package fi.otavanopisto.kuntaapi.server.integrations.tpt;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.YEAR;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.rest.model.Job;
import fi.otavanopisto.kuntaapi.server.id.JobId;
import fi.otavanopisto.kuntaapi.server.integrations.tpt.client.model.DocsEntry;

/**
 * Translator for te-palvelut.fi -integration
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class TptTranslator {
  
  @Inject
  private Logger logger;
  
  /**
   * Translates a te-palvelut job into Kunta API Job
   * 
   * @param kuntaApiJobId job id
   * @param baseUrl tpt API base url
   * @param tptJob te-palvelut job
   * @return Kunta API Job
   */
  public Job translateJob(JobId kuntaApiJobId, String baseUrl, DocsEntry tptJob) {
    if (tptJob == null) {
      return null;
    }
    
    String path = String.format(TptConsts.JOB_LINK_PATH, tptJob.getIlmoitusnumero());
    String link = String.format("%s%s", baseUrl, path);

    Job result = new Job();
    
    result.setId(kuntaApiJobId.getId());
    result.setTitle(tptJob.getOtsikko());
    result.setDescription(tptJob.getKuvausteksti());
    result.setEmploymentType(tptJob.getTyonKesto());
    result.setLocation(tptJob.getKunta());
    result.setOrganisationalUnit(tptJob.getTyonantajanNimi());
    result.setDuration(tptJob.getTyonKestoTekstiYhdistetty());
    result.setTaskArea(tptJob.getTehtavanimi());
    result.setPublicationEnd(translateHakuPaattyy(tptJob.getHakuPaattyy()));
    result.setPublicationStart(tptJob.getIlmoituspaivamaara());
    result.setLink(link);
    
    return result;
  }

  private OffsetDateTime translateHakuPaattyy(String hakuPaattyy) {
    if (hakuPaattyy == null) {
      return null;
    }
    
    DateTimeFormatter formatter = new DateTimeFormatterBuilder()
      .appendValue(DAY_OF_MONTH, 2)
      .appendLiteral('.')
      .appendValue(MONTH_OF_YEAR, 2)
      .appendLiteral('.')
      .appendValue(YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
      .optionalStart()
      .appendLiteral(" klo ")
      .appendValue(HOUR_OF_DAY, 2)
      .appendLiteral(":")
      .appendValue(MINUTE_OF_HOUR, 2)
      .toFormatter();
    
    TemporalAccessor temporalAccessor = formatter.parse(hakuPaattyy);
    if (temporalAccessor == null) {
      logger.log(Level.WARNING, () -> String.format("Failed to parse hakuPaattyy %s", hakuPaattyy));
      return null;
    }

    if (temporalAccessor.isSupported(ChronoField.HOUR_OF_DAY)) {
      LocalDateTime localDateTime = LocalDateTime.from(temporalAccessor);
      if (localDateTime == null) {
        logger.log(Level.WARNING, () -> String.format("Failed to parse hakuPaattyy %s", hakuPaattyy));
        return null;
      }
      
      return localDateTime.atZone(ZoneId.of(TptConsts.TIMEZONE)).toOffsetDateTime();
    }
    
    LocalDate localDate = LocalDate.from(temporalAccessor);
    if (localDate != null) {
      return localDate.atStartOfDay(ZoneId.of(TptConsts.TIMEZONE)).toOffsetDateTime();
    }
    
    logger.log(Level.WARNING, () -> String.format("Failed to convert hakuPaattyy %s into OffsetDateTime", hakuPaattyy));

    return null;    
  }
  
}
