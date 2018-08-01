package fi.metatavu.kuntaapi.server.integrations.kuntarekry;

import java.io.Serializable;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@SuppressWarnings ("squid:S3437")
public class KuntaRekryJob implements Serializable {

  private static final long serialVersionUID = -7272380834345432920L;

  @JacksonXmlProperty (localName = "jobtitle")
  private String jobTitle;

  @JacksonXmlProperty (localName = "employmenttype")
  private String employmentType;

  @JacksonXmlProperty (localName = "jobdescription")
  private String jobDescription;

  @JacksonXmlProperty (localName = "jobid")
  private Long jobId;

  @JacksonXmlProperty (localName = "location")
  private String location;

  @JacksonXmlProperty (localName = "organisation")
  private String organisation;

  @JacksonXmlProperty (localName = "organisationalunit")
  private String organisationalUnit;

  @JacksonXmlProperty (localName = "employmentduration")
  private String employmentDuration;

  @JacksonXmlProperty (localName = "taskarea")
  private String taskArea;

  @JacksonXmlProperty (localName = "publicationtimestart")
  private OffsetDateTime publicationTimeStart;

  @JacksonXmlProperty (localName = "publicationtimeend")
  private OffsetDateTime publicationTimeEnd;

  @JacksonXmlProperty (localName = "url")
  private String url;

  public String getJobTitle() {
    return jobTitle;
  }

  public void setJobTitle(String jobTitle) {
    this.jobTitle = jobTitle;
  }

  public String getEmploymentType() {
    return employmentType;
  }

  public void setEmploymentType(String employmentType) {
    this.employmentType = employmentType;
  }

  public String getJobDescription() {
    return jobDescription;
  }

  public void setJobDescription(String jobDescription) {
    this.jobDescription = jobDescription;
  }

  public Long getJobId() {
    return jobId;
  }

  public void setJobId(Long jobId) {
    this.jobId = jobId;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public String getOrganisation() {
    return organisation;
  }

  public void setOrganisation(String organisation) {
    this.organisation = organisation;
  }

  public String getOrganisationalUnit() {
    return organisationalUnit;
  }

  public void setOrganisationalUnit(String organisationalUnit) {
    this.organisationalUnit = organisationalUnit;
  }

  public String getEmploymentDuration() {
    return employmentDuration;
  }

  public void setEmploymentDuration(String employmentDuration) {
    this.employmentDuration = employmentDuration;
  }

  public String getTaskArea() {
    return taskArea;
  }

  public void setTaskArea(String taskArea) {
    this.taskArea = taskArea;
  }

  public OffsetDateTime getPublicationTimeStart() {
    return publicationTimeStart;
  }

  public void setPublicationTimeStart(OffsetDateTime publicationTimeStart) {
    this.publicationTimeStart = publicationTimeStart;
  }

  public OffsetDateTime getPublicationTimeEnd() {
    return publicationTimeEnd;
  }

  public void setPublicationTimeEnd(OffsetDateTime publicationTimeEnd) {
    this.publicationTimeEnd = publicationTimeEnd;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

}
