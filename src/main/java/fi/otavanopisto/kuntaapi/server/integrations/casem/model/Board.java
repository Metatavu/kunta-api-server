package fi.otavanopisto.kuntaapi.server.integrations.casem.model;

import java.util.List;

public class Board {
  
  private String boardTitle;
  private List<BoardMeeting> meetings;
  
  public String getBoardTitle() {
    return boardTitle;
  }
  
  public void setBoardTitle(String boardTitle) {
    this.boardTitle = boardTitle;
  }
  
  public List<BoardMeeting> getMeetings() {
    return meetings;
  }
  
  public void setMeetings(List<BoardMeeting> meetings) {
    this.meetings = meetings;
  }
  
}
