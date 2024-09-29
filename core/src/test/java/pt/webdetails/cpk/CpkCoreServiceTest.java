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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.Plugin;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;

import org.pentaho.di.trans.steps.jsonoutput.JsonOutputMeta;
import pt.webdetails.cpf.RestRequestHandler;
import pt.webdetails.cpf.repository.IRepositoryAccess;
import pt.webdetails.cpf.repository.vfs.VfsRepositoryAccess;
import pt.webdetails.cpf.utils.IPluginUtils;
import pt.webdetails.cpk.testUtils.CpkEnvironmentForTesting;
import pt.webdetails.cpk.testUtils.HttpServletResponseForTesting;
import pt.webdetails.cpk.testUtils.PluginUtilsForTesting;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class CpkCoreServiceTest {

  private static CpkCoreService cpkCore;
  private static Map<String, Map<String, Object>> bloatedMap;
  private static OutputStream outResponse;

  private static final String STATUS_RESULT = "cpkSol Status";

  @BeforeClass
  public static void setUp() throws KettleException {
    ICpkEnvironment environment = createCpkEnvironment();

    KettleEnvironment.init();

    PluginInterface plugin = createPluginInterface();

    PluginRegistry.getInstance().registerPlugin( StepPluginType.class, plugin );
    PluginRegistry.addPluginType( StepPluginType.getInstance() );
    PluginRegistry.init();

    if ( !Props.isInitialized() ) {
      Props.init( 0 );
    }

    cpkCore = spy( new CpkCoreService( environment ) );

    bloatedMap = buildBloatedMap();
  }

  @Before
  public void beforeEach() {
    outResponse = new ByteArrayOutputStream();
  }

  @Test
  public void testCreateContentSampleTrans() throws Exception { // start a hypersonic to test
    final String sampleTransResult = createContent( sampleTrans() );

    JSONObject queryInfo = new JSONObject( sampleTransResult ).getJSONObject( "queryInfo" );

    assertFalse( queryInfo.length() < 1 );
  }

  @Test
  public void testCreateContentEvaluateResultRows() throws Exception {
    final String evaluateResultRowsResult = createContent( evaluateResultRows() );

    JSONObject evaluateResultRowsJson = new JSONObject( evaluateResultRowsResult );

    assertTrue( evaluateResultRowsJson.getBoolean( "result" ) );
  }

  @Test
  public void testCreateContentCreateResultRows() throws Exception {
    final String createResultRowsResult = createContent( createResultRows() );

    JSONObject queryInfo = new JSONObject( createResultRowsResult ).getJSONObject( "queryInfo" );

    assertFalse( queryInfo.length() < 1 );
  }

  @Test
  public void testCreateContentGenerateRows() throws Exception {
    final String generateRowsResult = createContent( generateRows() );

    JSONObject queryInfo = new JSONObject( generateRowsResult ).getJSONObject( "queryInfo" );

    assertFalse( queryInfo.length() < 1 );
  }

  private String createContent( Map<String, Map<String, Object>> bloatedMap ) throws Exception {
    cpkCore.createContent( bloatedMap );

    String content = outResponse.toString();
    outResponse.close();

    return content;
  }

  @Test( expected = Exception.class )
  public void testGetLocalizationResourceElementNotFound() throws Exception {
    final String localizationResource = "messages.properties";

    final String elementId = "missingElement";
    doReturn( false ).when( cpkCore ).hasElement( eq( elementId ) );

    cpkCore.getLocalizationResource( elementId, localizationResource );
  }

  @Test( expected = FileNotFoundException.class )
  public void testGetLocalizationResourceNotFound() throws Exception {
    final String missingLocalizationResource = "messages_en.properties";

    final String elementId = "foo";
    doReturn( true ).when( cpkCore ).hasElement( eq( elementId ) );
    doReturn( "" ).when( cpkCore ).getElementRelativePath( eq( elementId ) );

    IPluginUtils pluginUtilsMock = mock( IPluginUtils.class );
    doReturn( Collections.emptySet() ).when( pluginUtilsMock )
        .getPluginResources( any(), any(), eq( missingLocalizationResource ) );

    doReturn( pluginUtilsMock ).when( cpkCore ).getPluginUtils();

    cpkCore.getLocalizationResource( elementId, missingLocalizationResource );
  }

  @Test
  public void testGetLocalizationResource() throws Exception {
    final String localizationResource = "messages_supported_languages.properties";

    final String relativeLocation = "path/to";
    final String elementId = "bar";

    doReturn( true ).when( cpkCore ).hasElement( eq( elementId ) );
    doReturn( relativeLocation ).when( cpkCore ).getElementRelativePath( eq( elementId ) );

    IPluginUtils pluginUtilsMock = mock( IPluginUtils.class );

    File localizationFile = mock( File.class );
    String expectedPath = relativeLocation + "/" + localizationResource;
    doReturn( expectedPath ).when( localizationFile ).getPath();

    doReturn( Collections.singleton( localizationFile ) ).when( pluginUtilsMock )
        .getPluginResources( eq( relativeLocation ), eq( true ), eq( localizationResource ) );
    doReturn( pluginUtilsMock ).when( cpkCore ).getPluginUtils();

    // ---

    final String actualPath = cpkCore.getLocalizationResource( elementId, localizationResource );

    assertEquals( expectedPath, actualPath );
  }

  @Test
  public void testGetElementsList() throws IOException, JSONException {
    OutputStream out = new ByteArrayOutputStream();
    cpkCore.getElementsList( out, bloatedMap );
    String str = out.toString();

    JSONArray elementsListJson = new JSONArray( str );
    assertTrue( checkIdLength( elementsListJson ) );
    out.close();
  }

  @Test
  public void testGetElementsListStringResult() throws JSONException {
    String actualResult = cpkCore.getElementsList();
    JSONArray elementsListJson = new JSONArray( actualResult );
    assertTrue( checkIdLength( elementsListJson ) );
  }

  private boolean checkIdLength( JSONArray elementsListJson ) throws JSONException {
    assertNotNull( elementsListJson );

    boolean successful = true;
    for ( int i = 0; i < elementsListJson.length(); i++ ) {
      JSONObject obj = elementsListJson.getJSONObject( i );
      String id = obj.getString( "id" );
      if ( id.length() < 1 ) {
        successful = false;
      }
    }
    return successful;
  }

  @Test
  public void testReloadRefreshStatus() throws IOException {
    OutputStream out = new ByteArrayOutputStream();
    refreshBloatedMapStream( out );

    cpkCore.reload( out, bloatedMap );

    String str = out.toString();
    out.close();

    assertTrue( str.contains( STATUS_RESULT ) );
    assertFalse( str.contains( "null" ) );
  }

  @Test
  public void testReloadRefreshStatusStringResult() throws IOException {
    OutputStream out = new ByteArrayOutputStream();
    refreshBloatedMapStream( out );

    String actualResult = cpkCore.reload( bloatedMap );

    assertTrue( actualResult.contains( STATUS_RESULT ) );
    assertFalse( actualResult.contains( "null" ) );
  }

  @Test
  public void testStatusJson() {
    String actualResult = null;
    try {
      actualResult = cpkCore.statusJson( null );
      JSONObject jsonObj = new JSONObject( actualResult );
      assertTrue( jsonObj.has( "pluginName" ) );
      assertEquals( "cpkSol", jsonObj.get( "pluginName" ) );
      assertTrue( jsonObj.has( "elements" ) );
      assertTrue( jsonObj.get( "elements" ) instanceof JSONObject );
      assertTrue( jsonObj.has( "elementsCount" ) );
      assertTrue( jsonObj.has( "defaultElement" ) );
    } catch ( JSONException ex ) {
      ex.printStackTrace();
    }
    assertNotNull( actualResult );
  }

  @Test
  public void testGetRequestHandler() {
    RestRequestHandler requestHandler = cpkCore.getRequestHandler();

    assertNotNull( requestHandler );
  }

  private Map<String, Map<String, Object>> sampleTrans() {
    Map<String, Map<String, Object>> mainMap = new HashMap<>();
    Map<String, Object> requestMap = new HashMap<>();
    Map<String, Object> pathMap = new HashMap<>();

    requestMap.put( "request", "unnecessary value?" );
    requestMap.put( "paramarg1", "value1" );
    requestMap.put( "paramarg2", "value2" );
    requestMap.put( "paramarg3", "value3" );
    requestMap.put( "kettleOutput", "Json" );

    pathMap.put( "path", "/sampleTrans" );
    pathMap.put( "httpresponse", new HttpServletResponseForTesting( outResponse ) );

    mainMap.put( "request", requestMap );
    mainMap.put( "path", pathMap );

    return mainMap;
  }

  private Map<String, Map<String, Object>> evaluateResultRows() {
    Map<String, Map<String, Object>> mainMap = new HashMap<>();

    Map<String, Object> requestMap = new HashMap<>();
    Map<String, Object> pathMap = new HashMap<>();

    requestMap.put( "request", "unnecessary value?" );
    requestMap.put( "paramarg1", "value1" );
    requestMap.put( "paramarg2", "value2" );
    requestMap.put( "paramarg3", "value3" );
    requestMap.put( "kettleOutput", "resultOnly" );

    pathMap.put( "path", "/evaluate-result-rows" );
    pathMap.put( "httpresponse", new HttpServletResponseForTesting( outResponse ) );

    mainMap.put( "request", requestMap );
    mainMap.put( "path", pathMap );

    return mainMap;
  }

  private Map<String, Map<String, Object>> createResultRows() {
    Map<String, Map<String, Object>> mainMap = new HashMap<>();

    Map<String, Object> requestMap = new HashMap<>();
    Map<String, Object> pathMap = new HashMap<>();

    requestMap.put( "request", "unnecessary value?" );
    requestMap.put( "paramarg1", "value1" );
    requestMap.put( "paramarg2", "value2" );
    requestMap.put( "paramarg3", "value3" );
    requestMap.put( "stepName", "OUTPUT Copy rows to result" );

    pathMap.put( "path", "/create-result-rows" );
    pathMap.put( "httpresponse", new HttpServletResponseForTesting( outResponse ) );

    mainMap.put( "request", requestMap );
    mainMap.put( "path", pathMap );

    return mainMap;
  }

  private Map<String, Map<String, Object>> generateRows() {
    Map<String, Map<String, Object>> mainMap = new HashMap<>();

    Map<String, Object> requestMap = new HashMap<>();
    Map<String, Object> pathMap = new HashMap<>();

    requestMap.put( "request", "unnecessary value?" );
    requestMap.put( "paramarg1", "value1" );
    requestMap.put( "paramarg2", "value2" );
    requestMap.put( "paramarg3", "value3" );
    requestMap.put( "stepName", "output" );

    pathMap.put( "path", "/generate-rows" );
    pathMap.put( "httpresponse", new HttpServletResponseForTesting( outResponse ) );

    mainMap.put( "request", requestMap );
    mainMap.put( "path", pathMap );

    return mainMap;
  }


  private static Map<String, Map<String, Object>> buildBloatedMap() {
    Map<String, Map<String, Object>> mainMap = new HashMap<>();

    Map<String, Object> requestMap = new HashMap<>();
    Map<String, Object> pathMap = new HashMap<>();

    requestMap.put( "request", "unnecessary value?" );
    requestMap.put( "paramarg1", "value1" );
    requestMap.put( "paramarg2", "value2" );
    requestMap.put( "paramarg3", "value3" );

    pathMap.put( "path", "/pass_arguments" );
    pathMap.put( "httpresponse", new HttpServletResponseForTesting( outResponse ) );

    mainMap.put( "request", requestMap );
    mainMap.put( "path", pathMap );
    return mainMap;
  }

  private static void refreshBloatedMapStream( OutputStream out ) {
    Map<String, Object> temp = new HashMap<>();
    temp.put( "response", new HttpServletResponseForTesting( out ) );
    bloatedMap.put( "response", temp );
  }

  private static ICpkEnvironment createCpkEnvironment() {
    final String userDir = System.getProperty( "user.dir" );

    final String repository = userDir + "/target/test-classes/repository";
    final String settings = userDir + "/target/test-classes/settings";

    IRepositoryAccess repAccess = new VfsRepositoryAccess( repository, settings );
    IPluginUtils pluginUtils = new PluginUtilsForTesting();

    return new CpkEnvironmentForTesting( pluginUtils, repAccess );
  }

  private static PluginInterface createPluginInterface() {
    Map<Class<?>, String> classMap = new HashMap<>();
    classMap.put( JsonOutputMeta.class, "org.pentaho.di.trans.steps.jsonoutput.JsonOutputMeta" );

    List<String> libraries = new ArrayList<>();

    return new Plugin( new String[] { "JsonOutput" }, StepPluginType.class, JsonOutputMeta.class,
        "Flow", "JsonOutputMeta", null, null, false,
        false, classMap, libraries, null, null );
  }
}
