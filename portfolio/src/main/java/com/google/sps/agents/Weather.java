package com.google.sps.agents;

// Imports the Google Cloud client library
import com.google.maps.errors.ApiException;
import com.google.protobuf.Value;
import com.google.sps.utils.LocationUtils;
import java.io.IOException;
import java.util.Map;

/** Weather Agent */
public class Weather implements Agent {
  private final String intentName;
  private String displayAddress;
  private String searchAddress;
  private String output = null;
  private String redirect = null;

  public Weather(String intentName, Map<String, Value> parameters)
      throws IllegalStateException, IOException, ApiException, InterruptedException,
          ArrayIndexOutOfBoundsException {
    this.intentName = intentName;
    setParameters(parameters);
  }

  @Override
  public void setParameters(Map<String, Value> parameters)
      throws IllegalStateException, IOException, ApiException, InterruptedException,
          ArrayIndexOutOfBoundsException {
    this.displayAddress = LocationUtils.getDisplayAddress("address", parameters);
    this.searchAddress = LocationUtils.getFormattedAddress("address", parameters);
    if (!displayAddress.isEmpty() && !searchAddress.isEmpty()) {
      String baseURL = "http://www.google.com/search?q=weather+in+";
      String[] individualWords = searchAddress.split(" ");
      String endURL = String.join("+", individualWords);
      this.redirect = baseURL + endURL;
      this.output = "Redirecting you to the current forecast in " + displayAddress + ".";
    }
  }

  @Override
  public String getOutput() {
    return this.output;
  }

  @Override
  public String getDisplay() {
    return null;
  }

  @Override
  public String getRedirect() {
    return this.redirect;
  }
}
