package com.google.sps.data;

import com.google.maps.model.LatLng;
import java.util.TimeZone;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** */
@RunWith(JUnit4.class)
public final class LocationTest {
  Location location = new Location("Los Angeles");
  Location blankLocation = new Location(" ");

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
  public void checkTimeZone() {
    TimeZone actual = location.getTimeZone();
    TimeZone expected = TimeZone.getTimeZone("America/Los_Angeles");
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void checkBlankAddressField() {
    String actual = blankLocation.getAddress();
    String expected = " ";
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void checkBlankFormattedAddressField() {
    String actual = blankLocation.getAddressFormatted();
    String expected = null;
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void checkBlankLatitude() {
    Double actual = blankLocation.getLat();
    Double expected = 0.0;
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void checkBlankLongitude() {
    Double actual = blankLocation.getLng();
    Double expected = 0.0;
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void checkBlankCoords() {
    LatLng actual = blankLocation.getCoords();
    LatLng expected = null;
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void checkBlankTimeZoneID() {
    String actual = blankLocation.getTimeZoneID();
    String expected = null;
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void checkBlankTimeZone() {
    TimeZone actual = blankLocation.getTimeZone();
    TimeZone expected = null;
    Assert.assertEquals(expected, actual);
  }
}
