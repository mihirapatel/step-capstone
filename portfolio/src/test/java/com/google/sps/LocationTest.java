package com.google.sps.data;

import com.google.maps.errors.ApiException;
import com.google.maps.model.LatLng;
import com.google.sps.utils.LocationUtils;
import java.io.IOException;
import java.util.TimeZone;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for creating valid Location objects */
@RunWith(JUnit4.class)
public final class LocationTest {
  Location location = null;
  Location invalidLocation = null;

  @Before
  public void init() {
    try {
      location = LocationUtils.getLocationObject("Los Angeles");
    } catch (IllegalStateException
        | IOException
        | ApiException
        | InterruptedException
        | ArrayIndexOutOfBoundsException e) {
      System.out.println("Error in creating valid location object.");
    }
  }

  @Test
  public void checkAddressField() {
    String actual = location.getAddress();
    String expected = "Los Angeles";
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void checkFormattedAddressField() {
    String actual = location.getAddressFormatted();
    String expected = "Los Angeles, CA, USA";
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void checkLatitude() {
    Double actual = location.getLat();
    Double expected = 34.05223420;
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void checkLongitude() {
    Double actual = location.getLng();
    Double expected = -118.24368490;
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void checkCoords() {
    LatLng actual = location.getCoords();
    LatLng expected = new LatLng(34.05223420, -118.24368490);
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void checkTimeZoneID() {
    String actual = location.getTimeZoneID();
    String expected = "America/Los_Angeles";
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void checkTimeZoneName() {
    String actual = location.getTimeZoneName();
    String expected = "Pacific Standard Time";
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void checkTimeZone() {
    TimeZone actual = location.getTimeZone();
    TimeZone expected = TimeZone.getTimeZone("America/Los_Angeles");
    Assert.assertEquals(expected, actual);
  }

  /** Invalid location input should not create a location object */
  @Test
  public void checkInvalidLocation() {
    try {
      invalidLocation = LocationUtils.getLocationObject("gibberish location input");
    } catch (IllegalStateException
        | IOException
        | ApiException
        | InterruptedException
        | ArrayIndexOutOfBoundsException e) {
      System.out.println("Correct error in creating invalid location object.");
    }
    Location actual = invalidLocation;
    Location expected = null;
    Assert.assertEquals(expected, actual);
  }
}
