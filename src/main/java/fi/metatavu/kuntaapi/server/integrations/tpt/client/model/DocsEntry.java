package fi.metatavu.kuntaapi.server.integrations.tpt.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.*;

@JsonIgnoreProperties (ignoreUnknown = true)
@SuppressWarnings ({"squid:S00116", "squid:S00100", "squid:S00117"})
public class DocsEntry implements Serializable {

  private static final long serialVersionUID = -4442994963182715417L;

  @JsonProperty("hakuTyosuhdetyyppikoodi")
  private String hakuTyosuhdetyyppikoodi;

  @JsonProperty("ammatti")
  private List<String> ammatti;

  @JsonProperty("tyoaikatekstiYhdistetty")
  private String tyoaikatekstiYhdistetty;

  @JsonProperty("hakuPaattyy")
  private String hakuPaattyy;

  @JsonProperty("maa")
  private String maa;

  @JsonProperty("asuntomahdollisuus")
  private Boolean asuntomahdollisuus;

  @JsonProperty("vuokratyopaikka")
  private Boolean vuokratyopaikka;

  @JsonProperty("etatyo")
  private Boolean etatyo;

  @JsonProperty("kieli")
  private String kieli;

  @JsonProperty("anonyymiTyonantaja")
  private Boolean anonyymiTyonantaja;

  @JsonProperty("tehtavanimi")
  private String tehtavanimi;

  @JsonProperty("ammattiLevel3")
  private String ammattiLevel3;

  @JsonProperty("tyonKestoKoodi")
  private String tyonKestoKoodi;

  @JsonProperty("kuvausteksti")
  private String kuvausteksti;

  @JsonProperty("englanti")
  private Boolean englanti;

  @JsonProperty("sijainti")
  private String sijainti;

  @JsonProperty("ilmoituspaivamaara")
  private OffsetDateTime ilmoituspaivamaara;

  @JsonProperty("tyonKesto")
  private String tyonKesto;

  @JsonProperty("tyonKestoTekstiYhdistetty")
  private String tyonKestoTekstiYhdistetty;

  @JsonProperty("id")
  private String id;

  @JsonProperty("hakuTyoaikakoodi")
  private String hakuTyoaikakoodi;

  @JsonProperty("kesatyopaikka")
  private Boolean kesatyopaikka;

  @JsonProperty("ilmoitettuTyomarkkinatLehdessa")
  private Boolean ilmoitettuTyomarkkinatLehdessa;

  @JsonProperty("timestamp")
  private Long timestamp;

  @JsonProperty("tyoaika")
  private String tyoaika;

  @JsonProperty("itsepalvelu")
  private Boolean itsepalvelu;

  @JsonProperty("vahtipostitus")
  private Boolean vahtipostitus;

  @JsonProperty("hakemusOsoitetaan")
  private String hakemusOsoitetaan;

  @JsonProperty("paikkalukumaara")
  private Integer paikkalukumaara;

  @JsonProperty("kunta")
  private String kunta;

  @JsonProperty("tyonantajanNimi")
  private String tyonantajanNimi;

  @JsonProperty("euresTyopaikka")
  private Boolean euresTyopaikka;

  @JsonProperty("ytunnus")
  private String ytunnus;

  @JsonProperty("mainAmmattikoodi")
  private String mainAmmattikoodi;

  @JsonProperty("oppisopimus")
  private Boolean oppisopimus;

  @JsonProperty("_version_")
  private Long _version_;

  @JsonProperty("ilmoitusnumero")
  private Integer ilmoitusnumero;

  @JsonProperty("maakunta")
  private String maakunta;

  @JsonProperty("otsikko")
  private String otsikko;

  @JsonProperty("hakuTyonKestoKoodi")
  private String hakuTyonKestoKoodi;

  public String getHakuTyosuhdetyyppikoodi() {
    return hakuTyosuhdetyyppikoodi;
  }

  public void setHakuTyosuhdetyyppikoodi(String hakuTyosuhdetyyppikoodi) {
    this.hakuTyosuhdetyyppikoodi = hakuTyosuhdetyyppikoodi;
  }

  public List<String> getAmmatti() {
    return ammatti;
  }

  public void setAmmatti(List<String> ammatti) {
    this.ammatti = ammatti;
  }

  public String getTyoaikatekstiYhdistetty() {
    return tyoaikatekstiYhdistetty;
  }

  public void setTyoaikatekstiYhdistetty(String tyoaikatekstiYhdistetty) {
    this.tyoaikatekstiYhdistetty = tyoaikatekstiYhdistetty;
  }

  public String getHakuPaattyy() {
    return hakuPaattyy;
  }

  public void setHakuPaattyy(String hakuPaattyy) {
    this.hakuPaattyy = hakuPaattyy;
  }

  public String getMaa() {
    return maa;
  }

  public void setMaa(String maa) {
    this.maa = maa;
  }

  public Boolean getAsuntomahdollisuus() {
    return asuntomahdollisuus;
  }

  public void setAsuntomahdollisuus(Boolean asuntomahdollisuus) {
    this.asuntomahdollisuus = asuntomahdollisuus;
  }

  public Boolean getVuokratyopaikka() {
    return vuokratyopaikka;
  }

  public void setVuokratyopaikka(Boolean vuokratyopaikka) {
    this.vuokratyopaikka = vuokratyopaikka;
  }

  public Boolean getEtatyo() {
    return etatyo;
  }

  public void setEtatyo(Boolean etatyo) {
    this.etatyo = etatyo;
  }

  public String getKieli() {
    return kieli;
  }

  public void setKieli(String kieli) {
    this.kieli = kieli;
  }

  public Boolean getAnonyymiTyonantaja() {
    return anonyymiTyonantaja;
  }

  public void setAnonyymiTyonantaja(Boolean anonyymiTyonantaja) {
    this.anonyymiTyonantaja = anonyymiTyonantaja;
  }

  public String getTehtavanimi() {
    return tehtavanimi;
  }

  public void setTehtavanimi(String tehtavanimi) {
    this.tehtavanimi = tehtavanimi;
  }

  public String getAmmattiLevel3() {
    return ammattiLevel3;
  }

  public void setAmmattiLevel3(String ammattiLevel3) {
    this.ammattiLevel3 = ammattiLevel3;
  }

  public String getTyonKestoKoodi() {
    return tyonKestoKoodi;
  }

  public void setTyonKestoKoodi(String tyonKestoKoodi) {
    this.tyonKestoKoodi = tyonKestoKoodi;
  }

  public String getKuvausteksti() {
    return kuvausteksti;
  }

  public void setKuvausteksti(String kuvausteksti) {
    this.kuvausteksti = kuvausteksti;
  }

  public Boolean getEnglanti() {
    return englanti;
  }

  public void setEnglanti(Boolean englanti) {
    this.englanti = englanti;
  }

  public String getSijainti() {
    return sijainti;
  }

  public void setSijainti(String sijainti) {
    this.sijainti = sijainti;
  }

  public OffsetDateTime getIlmoituspaivamaara() {
    return ilmoituspaivamaara;
  }

  public void setIlmoituspaivamaara(OffsetDateTime ilmoituspaivamaara) {
    this.ilmoituspaivamaara = ilmoituspaivamaara;
  }

  public String getTyonKesto() {
    return tyonKesto;
  }

  public void setTyonKesto(String tyonKesto) {
    this.tyonKesto = tyonKesto;
  }

  public String getTyonKestoTekstiYhdistetty() {
    return tyonKestoTekstiYhdistetty;
  }

  public void setTyonKestoTekstiYhdistetty(String tyonKestoTekstiYhdistetty) {
    this.tyonKestoTekstiYhdistetty = tyonKestoTekstiYhdistetty;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getHakuTyoaikakoodi() {
    return hakuTyoaikakoodi;
  }

  public void setHakuTyoaikakoodi(String hakuTyoaikakoodi) {
    this.hakuTyoaikakoodi = hakuTyoaikakoodi;
  }

  public Boolean getKesatyopaikka() {
    return kesatyopaikka;
  }

  public void setKesatyopaikka(Boolean kesatyopaikka) {
    this.kesatyopaikka = kesatyopaikka;
  }

  public Boolean getIlmoitettuTyomarkkinatLehdessa() {
    return ilmoitettuTyomarkkinatLehdessa;
  }

  public void setIlmoitettuTyomarkkinatLehdessa(Boolean ilmoitettuTyomarkkinatLehdessa) {
    this.ilmoitettuTyomarkkinatLehdessa = ilmoitettuTyomarkkinatLehdessa;
  }

  public Long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Long timestamp) {
    this.timestamp = timestamp;
  }

  public String getTyoaika() {
    return tyoaika;
  }

  public void setTyoaika(String tyoaika) {
    this.tyoaika = tyoaika;
  }

  public Boolean getItsepalvelu() {
    return itsepalvelu;
  }

  public void setItsepalvelu(Boolean itsepalvelu) {
    this.itsepalvelu = itsepalvelu;
  }

  public Boolean getVahtipostitus() {
    return vahtipostitus;
  }

  public void setVahtipostitus(Boolean vahtipostitus) {
    this.vahtipostitus = vahtipostitus;
  }

  public String getHakemusOsoitetaan() {
    return hakemusOsoitetaan;
  }

  public void setHakemusOsoitetaan(String hakemusOsoitetaan) {
    this.hakemusOsoitetaan = hakemusOsoitetaan;
  }

  public Integer getPaikkalukumaara() {
    return paikkalukumaara;
  }

  public void setPaikkalukumaara(Integer paikkalukumaara) {
    this.paikkalukumaara = paikkalukumaara;
  }

  public String getKunta() {
    return kunta;
  }

  public void setKunta(String kunta) {
    this.kunta = kunta;
  }

  public String getTyonantajanNimi() {
    return tyonantajanNimi;
  }

  public void setTyonantajanNimi(String tyonantajanNimi) {
    this.tyonantajanNimi = tyonantajanNimi;
  }

  public Boolean getEuresTyopaikka() {
    return euresTyopaikka;
  }

  public void setEuresTyopaikka(Boolean euresTyopaikka) {
    this.euresTyopaikka = euresTyopaikka;
  }

  public String getYtunnus() {
    return ytunnus;
  }

  public void setYtunnus(String ytunnus) {
    this.ytunnus = ytunnus;
  }

  public String getMainAmmattikoodi() {
    return mainAmmattikoodi;
  }

  public void setMainAmmattikoodi(String mainAmmattikoodi) {
    this.mainAmmattikoodi = mainAmmattikoodi;
  }

  public Boolean getOppisopimus() {
    return oppisopimus;
  }

  public void setOppisopimus(Boolean oppisopimus) {
    this.oppisopimus = oppisopimus;
  }

  public Long get_version_() {
    return _version_;
  }

  public void set_version_(Long _version_) {
    this._version_ = _version_;
  }

  public Integer getIlmoitusnumero() {
    return ilmoitusnumero;
  }

  public void setIlmoitusnumero(Integer ilmoitusnumero) {
    this.ilmoitusnumero = ilmoitusnumero;
  }

  public String getMaakunta() {
    return maakunta;
  }

  public void setMaakunta(String maakunta) {
    this.maakunta = maakunta;
  }

  public String getOtsikko() {
    return otsikko;
  }

  public void setOtsikko(String otsikko) {
    this.otsikko = otsikko;
  }

  public String getHakuTyonKestoKoodi() {
    return hakuTyonKestoKoodi;
  }

  public void setHakuTyonKestoKoodi(String hakuTyonKestoKoodi) {
    this.hakuTyonKestoKoodi = hakuTyonKestoKoodi;
  }

}
