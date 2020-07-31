package com.google.sps.recommendations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.Silent.class)
public class DatabaseTest {

  private static Logger log = LoggerFactory.getLogger(DatabaseTest.class);
  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
  private DatastoreService datastore;

  @Before
  public void setUp() {
    helper.setUp();
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  /** Test groceri database seeding. */
  @Test
  public void testDatabaseReset() throws Exception {
    testHelper("groceri");
  }

  /** Test Frac-groceri database seeding. */
  @Test
  public void testFracDatabaseReset() throws Exception {
    testHelper("Frac-groceri");
  }

  private void testHelper(String category) throws Exception {
    DatabaseUtils.resetDatabase(datastore);
    Query query = new Query(category);
    List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
    assertEquals(5, results.size());
  }
}
