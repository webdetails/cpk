/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package pt.webdetails.cpk;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.springframework.mock.web.DelegatingServletOutputStream;

import pt.webdetails.cpf.plugins.Plugin;
import pt.webdetails.cpk.datasources.DataSource;
import pt.webdetails.cpk.datasources.DataSourceDefinition;
import pt.webdetails.cpk.datasources.DataSourceMetadata;
import pt.webdetails.cpk.elements.IDataSourceProvider;
import pt.webdetails.cpk.elements.IElement;
import pt.webdetails.cpk.elements.impl.DashboardElement;
import pt.webdetails.cpk.testUtils.CpkApiForTesting;
import pt.webdetails.cpk.testUtils.HttpHeadersForTesting;
import pt.webdetails.cpk.testUtils.HttpServletRequestForTesting;
import pt.webdetails.cpk.testUtils.HttpServletResponseForTesting;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.withSettings;
import static pt.webdetails.cpk.CpkApi.CPK_ENDPOINT_KEY;
import static pt.webdetails.cpk.CpkApi.CPK_ENDPOINT_SEPARATOR;

import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

public class CpkApiTest {

  private CpkApi cpkApi = null;
  private HttpServletRequest request = null;
  private HttpServletResponse response = null;
  private HttpHeaders headers = null;

  private static String userDir = System.getProperty( "user.dir" );
  private static final String TEST_RESULT = "Test Successful";
  private static final String TEST_RESULT_JSON = "{\"result\": \"Test Successful\"}";
  private static final String DEFAULT_ELEMENT_ID = "DefaultElementId";
  private static final String DEFAULT_ELEMENT_PLUGIN_ID = "DefaultElementPluginId";
  private static final String DATA_SOURCE_NAME = "DataSourceName";
  private static final String DATA_SOURCE_DATA_TYPE = "DataSourceDataType";
  private static final String DATA_SOURCE_GROUP_ID = "DataSourceGroupId";
  private static final String DATA_SOURCE_GROUP_DESCRIPTION = "DataSourceGroupDescription";

  @Before
  public void setUp() {
    cpkApi = new CpkApiForTesting();
    request = new HttpServletRequestForTesting();
    OutputStream out = new ByteArrayOutputStream();
    response = new HttpServletResponseForTesting( out );
    headers = new HttpHeadersForTesting();
  }

  @Test
  public void testGenericEndpoint() {
    Response responseResult = null;
    try {
      doAnswer( new Answer() {
        public Object answer( InvocationOnMock invocation ) {
          Object[] args = invocation.getArguments();
          HttpServletResponseForTesting response = getResponse( (Map) args[0] );
          try {
            if ( response != null && response.getOutputStream() != null ) {
              response.getOutputStream().write( TEST_RESULT.getBytes() );
              response.setStatus( HttpServletResponse.SC_OK );
            }
          } catch ( IOException ex ) {
            ex.printStackTrace();
          }
          return null;
        } } )
      .when( cpkApi.coreService ).createContent( anyMap() );

      responseResult = cpkApi.genericEndpointGet( "/", request, response, headers );
      assertEquals( Response.Status.OK.getStatusCode(), responseResult.getStatus() );
      assertEquals( HttpServletResponse.SC_OK, response.getStatus() );
      assertEquals( TEST_RESULT, getStringValue( response.getOutputStream() ) );
    } catch ( Exception ex ) {
      ex.printStackTrace();
    }
    assertNotNull( responseResult );
  }

  @Test
  public void testPing() {
    String actualResult = cpkApi.ping();
    assertEquals( cpkApi.PING_RESPONSE + cpkApi.getPluginName(), actualResult );
  }

  @Test
  public void testDefaultElement() {
    IElement defaultElement = Mockito.mock( IElement.class );
    when( defaultElement.getPluginId() ).thenReturn( DEFAULT_ELEMENT_PLUGIN_ID );
    when( defaultElement.getId() ).thenReturn( DEFAULT_ELEMENT_ID );

    when( cpkApi.coreService.getDefaultElement() ).thenReturn( defaultElement );

    String expectedUrl = DEFAULT_ELEMENT_PLUGIN_ID + cpkApi.API_ROOT + DEFAULT_ELEMENT_ID;

    Response responseResult = null;
    try {
      responseResult = cpkApi.defaultElement();
      assertEquals( Response.Status.TEMPORARY_REDIRECT.getStatusCode(), responseResult.getStatus() );
      assertEquals( expectedUrl, responseResult.getMetadata().get( "Location" ).get( 0 ).toString() );
    } catch ( IOException ex ) {
      ex.printStackTrace();
    }
    assertNotNull( responseResult );
  }

  @Test
  public void testDefaultElementNotDefined() {
    when( cpkApi.coreService.getDefaultElement() ).thenReturn( null );
    Response responseResult = null;
    try {
      responseResult = cpkApi.defaultElement();
      assertEquals( Response.Status.OK.getStatusCode(), responseResult.getStatus() );
      assertEquals( cpkApi.DEFAULT_NO_DASHBOARD_MESSAGE, new String( (byte[]) responseResult.getEntity() ) );
    } catch ( IOException ex ) {
      ex.printStackTrace();
    }
    assertNotNull( responseResult );
  }

  @Test
  public void testReload() {
    when( cpkApi.coreService.reload( anyMap() ) ).thenReturn( TEST_RESULT );
    Response responseResult = cpkApi.reload( request, response, headers );
    assertEquals( Response.Status.OK.getStatusCode(), responseResult.getStatus() );
    assertEquals( TEST_RESULT, responseResult.getEntity() );
  }

  @Test
  public void testRefresh() {
    when( cpkApi.coreService.refresh( anyMap() ) ).thenReturn( TEST_RESULT );
    Response responseResult = cpkApi.refreshGet( request, response, headers );
    assertEquals( Response.Status.OK.getStatusCode(), responseResult.getStatus() );
    assertEquals( TEST_RESULT, responseResult.getEntity() );
  }

  @Test
  public void testVersion() {
    List<Plugin> plugins = new ArrayList<>();
    Plugin pluginMock = Mockito.mock( Plugin.class );
    when( pluginMock.getVersion() ).thenReturn( TEST_RESULT );
    plugins.add( pluginMock );

    doNothing().when( cpkApi.pluginsAnalyzer ).refresh();
    when( cpkApi.pluginsAnalyzer.getPlugins( any() ) ).thenReturn( plugins );

    Response responseResult = cpkApi.version( cpkApi.cpkEnv.getPluginName() );
    assertEquals( Response.Status.OK.getStatusCode(), responseResult.getStatus() );
    assertEquals( TEST_RESULT, responseResult.getEntity() );
  }

  @Test
  public void testStatus() {
    when( cpkApi.coreService.status( anyMap() ) ).thenReturn( TEST_RESULT );
    commonTestStatus( TEST_RESULT );
  }

  @Test
  public void testStatusJson() {
    request.setAttribute( "json", "" );
    when( cpkApi.coreService.statusJson( response ) ).thenReturn( TEST_RESULT_JSON );
    commonTestStatus( TEST_RESULT_JSON );
  }

  @Test
  public void testSitemapJson() {
    IElement elementMock = Mockito.mock( DashboardElement.class );
    when( elementMock.getLocation() ).thenReturn( userDir + "/target/test-classes/repository/system/cpkSol/dashboards/fileList.wcdf" );
    when( elementMock.getId() ).thenReturn( "fileList" );

    Map<String, IElement> elementsMap = new HashMap<>();
    elementsMap.put( "filelist", elementMock );

    CpkEngine engineMock = Mockito.mock( CpkEngine.class );
    when( engineMock.getElementsMap() ).thenReturn( elementsMap );

    when( cpkApi.coreService.getEngine() ).thenReturn( engineMock );

    JSONArray expectedResult = getExpectedSiteMapJsonString();

    Response responseResult = null;
    try {
      responseResult = cpkApi.getSitemapJson();
      assertEquals( Response.Status.OK.getStatusCode(), responseResult.getStatus() );
      JSONAssert.assertEquals( expectedResult, new JSONArray( (String) responseResult.getEntity() ),
        JSONCompareMode.LENIENT );
    } catch ( IOException | JSONException ex ) {
      ex.printStackTrace();
    }
    assertNotNull( responseResult );
  }

  @Test
  public void testElementsList() {
    when( cpkApi.coreService.getElementsList() ).thenReturn( TEST_RESULT );
    Response responseResult = cpkApi.elementsList();
    assertEquals( Response.Status.OK.getStatusCode(), responseResult.getStatus() );
    assertEquals( TEST_RESULT, responseResult.getEntity() );
  }

  @Test
  public void testListDataAccessTypesNoneExistent() {
    List<IElement> endpoints = new ArrayList<>();
    when( cpkApi.coreService.getElements() ).thenReturn( endpoints );

    String expectedResult = "{}";

    Response responseResult = null;
    try {
      responseResult = cpkApi.listDataAccessTypes();
      assertEquals( Response.Status.OK.getStatusCode(), responseResult.getStatus() );
      assertEquals( expectedResult, responseResult.getEntity() );
    } catch ( IOException ex ) {
      ex.printStackTrace();
    }
    assertNotNull( responseResult );
  }

  @Test
  public void testListDataAccessTypes() {
    DataSourceMetadata dsMetadata = new DataSourceMetadata();
    dsMetadata.setDataType( DATA_SOURCE_DATA_TYPE );
    dsMetadata.setGroup( DATA_SOURCE_GROUP_ID );
    dsMetadata.setGroupDescription( DATA_SOURCE_GROUP_DESCRIPTION );
    dsMetadata.setName( DATA_SOURCE_NAME );
    dsMetadata.setPluginId( cpkApi.cpkEnv.getPluginName() );

    DataSourceDefinition dsDefinition = new DataSourceDefinition();

    DataSource dataSource = new DataSource();
    dataSource.setMetadata( dsMetadata );
    dataSource.setDefinition( dsDefinition );

    IElement elementDataSourceMock = Mockito.mock( IElement.class,
      withSettings().extraInterfaces( IDataSourceProvider.class ) );
    when( elementDataSourceMock.getName() ).thenReturn( DATA_SOURCE_NAME );
    when( ( (IDataSourceProvider) elementDataSourceMock ).getDataSource() ).thenReturn( dataSource );

    List<IElement> endpoints = new ArrayList<>();
    endpoints.add( elementDataSourceMock );

    when( cpkApi.coreService.getElements() ).thenReturn( endpoints );

    Response responseResult = null;
    try {
      responseResult = cpkApi.listDataAccessTypes();
      assertEquals( Response.Status.OK.getStatusCode(), responseResult.getStatus() );

      JSONObject jsonObjectResult = new JSONObject( (String) responseResult.getEntity() );
      String dataAccessName = String.format( "%s%s%s%s%s", cpkApi.cpkEnv.getPluginName(),
        CPK_ENDPOINT_SEPARATOR, DATA_SOURCE_NAME, CPK_ENDPOINT_SEPARATOR, CPK_ENDPOINT_KEY );
      JSONObject jsonObjectExpected = getExpectedDataAccessTypes( dataAccessName, dataSource );

      JSONAssert.assertEquals( jsonObjectExpected, jsonObjectResult, JSONCompareMode.LENIENT );
    } catch ( IOException | JSONException ex ) {
      ex.printStackTrace();
    }
    assertNotNull( responseResult );
  }

  @Test
  public void testReloadPluginsGet() {
    Response responseResult = cpkApi.reloadPluginsGet();
    assertEquals( Response.Status.NO_CONTENT.getStatusCode(), responseResult.getStatus() );
  }

  @Test
  public void testClearCache() {
    doNothing().when( cpkApi.coreService ).clearKettleResultsCache();
    Response responseResult = cpkApi.clearKettleResultsCache();
    assertEquals( Response.Status.NO_CONTENT.getStatusCode(), responseResult.getStatus() );
  }

  @After
  public void tearDown() {
    cpkApi = null;
    request = null;
    response = null;
    headers = null;
  }

  private HttpServletResponseForTesting getResponse( Map map ) {
    if ( map != null ) {
      Map pathMap = (HashMap) map.get( "path" );
      if ( pathMap != null ) {
        return (HttpServletResponseForTesting) pathMap.get( "httpresponse" );
      }
    }
    return null;
  }

  private String getStringValue( Object outputStream ) {
    if ( outputStream != null ) {
      if ( outputStream instanceof DelegatingServletOutputStream ) {
        DelegatingServletOutputStream delegatingServletOutputStream = (DelegatingServletOutputStream) outputStream;
        if ( delegatingServletOutputStream.getTargetStream() != null ) {
          return delegatingServletOutputStream.getTargetStream().toString();
        }
      }
    }
    return null;
  }

  private void commonTestStatus( String expectedStringResult ) {
    Response responseResult = cpkApi.status( request, response, headers );
    assertEquals( Response.Status.OK.getStatusCode(), responseResult.getStatus() );
    assertEquals( expectedStringResult, responseResult.getEntity() );
    assertNotNull( responseResult );
  }

  private JSONArray getExpectedSiteMapJsonString() {
    String jsonStr = "[{\"name\":\"dashboardsAnotherSublink\","
      + "\"id\":\"\","
      + "\"sublinks\":["
        + "{\"name\":\"folder\","
        + "\"id\":\"\","
        + "\"sublinks\":[{\"name\":\"anotherFolder\",\"id\":\"\",\"sublinks\":[],\"link\":\"\"}],"
        + "\"link\":\"\"}],"
      + "\"link\":\"\"},"
      + "{\"name\":\"dashboardsSublink\","
      + "\"id\":\"\","
      + "\"sublinks\":["
        + "{\"name\":\"folder\","
        + "\"id\":\"\","
        + "\"sublinks\":[{\"name\":\"anotherFolder\",\"id\":\"\",\"sublinks\":[],\"link\":\"\"}],"
        + "\"link\":\"\"}],"
      + "\"link\":\"\"},"
      + "{\"name\":\"File List Dashboard\","
      + "\"id\":\"fileList.wcdf\","
      + "\"sublinks\":[],"
      + "\"link\":\"/pentaho/plugin/cpkSol/api/filelist\"}]";
    try {
      return new JSONArray( jsonStr );
    } catch ( JSONException ex ) {
      ex.printStackTrace();
    }
    return null;
  }

  private JSONObject getExpectedDataAccessTypes( String dataAccessName, DataSource dataSource ) {
    JSONObject jsonObject = new JSONObject();
    try {
      ObjectMapper mapper = new ObjectMapper();
      JSONObject joDefinitions = new JSONObject( mapper.writeValueAsString( dataSource.getDefinition() ) );
      JSONObject joMetadata = new JSONObject( mapper.writeValueAsString( dataSource.getMetadata() ) );

      JSONObject joDataSource = new JSONObject();
      joDataSource.put( "definition", joDefinitions );
      joDataSource.put( "metadata", joMetadata );

      jsonObject.put( dataAccessName, joDataSource );

    } catch ( IOException | JSONException ex ) {
      ex.printStackTrace();
    }
    return jsonObject;
  }
}
