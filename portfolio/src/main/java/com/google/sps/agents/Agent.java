package com.google.sps.agents;

// Imports the Google Cloud client library
import com.google.protobuf.Value;
import java.util.Map;

/** Agent Interface */
public interface Agent {
  public void setParameters(Map<String, Value> parameters) throws Exception;

  public String getOutput();

  public String getDisplay();

  public String getRedirect();
}
