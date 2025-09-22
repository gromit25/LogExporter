package com.redeye.logexporter.exporter.restapi;

@Data
public class TraceDTO {

  /** */
  private long start;

  /** */
  private long end;

  /** */
  private Map<String, JoinPointDTO> joinPointList = new ConcurrentHashMap<>();

  /** */
  private List<LinkDTO> linkList;

  
  public TraceDTO add(String message) throws Exception {
    return this;
  }
}
