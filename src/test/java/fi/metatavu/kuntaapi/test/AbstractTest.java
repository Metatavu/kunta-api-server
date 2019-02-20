package fi.metatavu.kuntaapi.test;

import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Abstract base class for all tests
 * 
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */
public abstract class AbstractTest {
    
  private static Logger logger = Logger.getLogger(AbstractTest.class.getName());
  
  @Rule
  public TestName testName = new TestName();
  
  private long started;
  
  @Before
  @SuppressWarnings ("squid:S106")
  public void beforeTestStart() {
    started = System.currentTimeMillis();
    System.out.println(String.format("> %s", testName.getMethodName()));
  }
  
  @After
  @SuppressWarnings ("squid:S106")
  public void afterTestEnd() {
    long duration = System.currentTimeMillis() - started;
    System.out.println(String.format("> %s done in %s", testName.getMethodName(), DurationFormatUtils.formatDurationWords(duration, true, true)));
  }
  
  protected ZonedDateTime getZonedDateTime(int year, int month, int dayOfMonth, int hour, int minute, int second, ZoneId zone) {
    return ZonedDateTime.of(year, month, dayOfMonth, hour, minute, second, 0, zone);
  }
  
  protected OffsetDateTime getOffsetDateTime(int year, int month, int dayOfMonth, int hour, int minute, ZoneId zone) {
    return getZonedDateTime(year, month, dayOfMonth, hour, minute, 0, zone).toOffsetDateTime();
  }
  protected OffsetDateTime getOffsetDateTime(int year, int month, int dayOfMonth, int hour, int minute, int second, ZoneId zone) {
    return getZonedDateTime(year, month, dayOfMonth, hour, minute, second, zone).toOffsetDateTime();
  }

  protected Instant getInstant(int year, int month, int dayOfMonth, int hour, int minute, int second, ZoneId zone) {
    return getOffsetDateTime(year, month, dayOfMonth, hour, minute, second, zone).toInstant();
  }
  
  protected Instant getInstant(int year, int month, int dayOfMonth, int hour, int minute, ZoneId zone) {
    return getOffsetDateTime(year, month, dayOfMonth, hour, minute, zone).toInstant();
  }
  
  protected String getIsoDateTime(int year, int month, int dayOfMonth, int hour, int minute, ZoneId zone) {
    return DateTimeFormatter.ISO_DATE_TIME.format(getOffsetDateTime(year, month, dayOfMonth, hour, minute, zone));
  }
  
  protected int getHttpPort() {
    return Integer.parseInt(System.getProperty("it.port.http"));
  }

  protected String getHost() {
    return System.getProperty("it.host");
  }
  
  protected int getWireMockPort() {
    return getHttpPort() + 1;
  }
  
  protected String getWireMockBasePath() {
    return String.format("http://%s:%d", getHost(), getWireMockPort());
  }

  protected String getApiBasePath() {
    return String.format("http://%s:%d/v1", getHost(), getHttpPort());
  }
  
  protected long insertOrganizationSetting(String organizationId, String key, String value) {
    return executeInsert("insert into OrganizationSetting (settingKey, organizationKuntaApiId, value) values (?, ?, ?)", key, organizationId, value);
  }
  
  protected void deleteOrganizationSetting(String organizationId, String key) {
    try {
      executeDelete("delete from OrganizationSetting where settingKey = ? and organizationKuntaApiId = ?", key, organizationId);
    } catch (SQLException e) {
      fail(e.getMessage());
    }
  }
  
  protected long insertSystemSetting(String key, String value) {
    return executeInsert("insert into SystemSetting (settingKey, value) values (?, ?)", key, value);
  }
  
  protected void deleteSystemSetting(String key) {
    try {
      executeDelete("delete from SystemSetting where settingKey = ?", key);
    } catch (SQLException e) {
      fail(e.getMessage());
    }
  }
  
  /**
   * Returns whether to purge organization identifiers
   * 
   * @return whether to purge organization identifiers
   */
  protected boolean getPurgeOrganizations() {
    return false;
  }

  /**
   * Deletes identifiers
   * 
   * @param purgeOrganizations whether to purge organization identifiers
   */
  protected void deleteIdentifiers(boolean purgeOrganizations) {
    try {
      if (!purgeOrganizations) {
        executeDelete("DELETE FROM StoredBinaryResource WHERE identifier_id NOT IN (SELECT id from Identifier WHERE type = 'ORGANIZATION')");
        executeDelete("DELETE FROM StoredResource WHERE identifier_id NOT IN (SELECT id from Identifier WHERE type = 'ORGANIZATION')");
        executeDelete("DELETE FROM IdentifierRelation WHERE parent_id NOT IN (SELECT id from Identifier WHERE type = 'ORGANIZATION') OR child_id NOT IN (SELECT id from Identifier WHERE type = 'ORGANIZATION')");
        executeDelete("DELETE FROM Identifier WHERE type != 'ORGANIZATION'");
        executeDelete("DELETE FROM ArchivedIdentifier");
      } else {
        executeDelete("DELETE FROM StoredBinaryResource");
        executeDelete("DELETE FROM StoredResource");
        executeDelete("DELETE FROM IdentifierRelation");
        executeDelete("DELETE FROM Identifier");
        executeDelete("DELETE FROM ArchivedIdentifier");
      }
    } catch (SQLException e) {
      fail(e.getMessage());
    }
  }
  
  protected long executeInsert(String sql, Object... params) {
    try (Connection connection = getConnection()) {
      connection.setAutoCommit(true);
      PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
      try {
        applyStatementParams(statement, params);
        statement.execute();
        
        try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
          return getGeneratedKey(generatedKeys);
        }
      } finally {
        statement.close();
      }
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Failed to execute insert", e);
      fail(e.getMessage());
    }
    
    return -1;
  }

  protected ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    
    return objectMapper;
  }
  
  private void applyStatementParams(PreparedStatement statement, Object... params)
      throws SQLException {
    for (int i = 0, l = params.length; i < l; i++) {
      Object param = params[i];
      if (param instanceof List) {
        statement.setObject(i + 1, ((List<?>) param).toArray());
      } else {
        statement.setObject(i + 1, params[i]);
      }
    }
  }
  
  private long getGeneratedKey(ResultSet generatedKeys) throws SQLException {
    if (generatedKeys.next()) {
      return generatedKeys.getLong(1);
    }
    
    return -1;
  }
  
  protected void executeDelete(String sql, Object... params) throws SQLException {
    try (Connection connection = getConnection()) {
      connection.setAutoCommit(true);
      PreparedStatement statement = connection.prepareStatement(sql);
      try {
        applyStatementParams(statement, params);
        statement.execute();
      } finally {
        statement.close();
      }
    }
  }

  protected Connection getConnection() {
    String username = System.getProperty("it.jdbc.username");
    String password = System.getProperty("it.jdbc.password");
    String url = System.getProperty("it.jdbc.url");
    try {
      Class.forName(System.getProperty("it.jdbc.driver")).newInstance();
    } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
      logger.log(Level.SEVERE, "Failed to load JDBC driver", e);
      fail(e.getMessage());
    }

    try {
      return DriverManager.getConnection(url, username, password);
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Failed to get connection", e);
      fail(e.getMessage());
    }
    
    return null;
  }

  protected static int getSecondsFromMidnight(String timeString) {
    int totalSeconds = 0;
    String[] timeParts = timeString.split(":");
    if(timeParts[0] != null && StringUtils.isNumeric(timeParts[0])) {
      int hours = Integer.parseInt(timeParts[0]);
      totalSeconds += hours * 3600;
    }
    if(timeParts[1] != null && StringUtils.isNumeric(timeParts[1])) {
      int minutes = Integer.parseInt(timeParts[1]);
      totalSeconds += minutes * 60;
    }
    if(timeParts[2] != null && StringUtils.isNumeric(timeParts[2])) {
      int seconds = Integer.parseInt(timeParts[2]);
      totalSeconds += seconds;
    }
    
    return totalSeconds;
  }
  
  @SuppressWarnings ({"squid:S1188", "squid:MethodCyclomaticComplexity"})
  protected static Matcher<Instant> sameInstant(final Instant instant) {
    
    return new BaseMatcher<Instant>(){

      @Override
      public void describeTo(Description description) {
        description.appendText("same instant: ").appendValue(instant.toString());
      }

      @Override
      public boolean matches(Object item) {
        if (item == null && instant == null) {
          return true;
        }
        
        if (item == null || instant == null) {
          return false;
        }
        
        Instant itemInstant = toInstant(item);
        if (itemInstant == null) {
          return false;
        }
        
        return itemInstant.getEpochSecond() == instant.getEpochSecond();
      }
      
      private Instant toInstant(Object item) {
        if (item instanceof String) {
          return Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse((String) item));
        } else if (item instanceof Instant) {
          return (Instant) item;
        }
        
        return null;
      }
      
    };
    
  }
}
