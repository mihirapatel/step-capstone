package com.google.sps.agents;
 
// Imports the Google Cloud client library
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import java.io.IOException;
import java.util.Map;
import com.google.sps.data.Output;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.QueryResult;
 
/**
 * DialogFlow API Detect Intent sample with audio files processes as an audio stream.
 */
public interface Agent {
    public void setParameters(Map<String, Value> parameters);
    public String getOutput();
    public String getDisplay();
    public String getRedirect();
}
    
