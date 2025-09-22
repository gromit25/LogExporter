package com.redeye.logexporter.exporter.restapi;

public class TraceDTO {
  private long start;
  private long end;
  private List<JoinPointDTO> joinPointList;
  private List<LinkDTO> linkList;
}
