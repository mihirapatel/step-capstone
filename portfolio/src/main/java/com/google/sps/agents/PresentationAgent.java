package com.google.sps.agents;

// Imports the Google Cloud client library

/**
 * Presentation Agent
 *
 * <p>Handles a hardcoded presentation intent for the beginning of our presentation.
 */
public class PresentationAgent implements Agent {

  private String presentationLink =
      "https://docs.google.com/presentation/d/1oU2a_KR_YDwWnwg0EaKb2g_PrS-4KQ8joPwiiu6dXwo/edit?ts=5f2451ad#slide=id.g3da171a20a_0_36";

  @Override
  public String getOutput() {
    return "Okay, let's do it. Redirecting to presentation slides.";
  }

  @Override
  public String getDisplay() {
    return null;
  }

  @Override
  public String getRedirect() {
    return presentationLink;
  }
}
