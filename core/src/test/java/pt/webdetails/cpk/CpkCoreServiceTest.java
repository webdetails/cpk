/*!
* Copyright 2002 - 2018 Webdetails, a Hitachi Vantara company.  All rights reserved.
*
* This software was developed by Webdetails and is provided under the terms
* of the Mozilla Public License, Version 2.0, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/
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
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CpkCoreServiceTest {

  private static CpkCoreService cpkCore;
  private static Map<String, Map<String, Object>> bloatedMap;
  private static OutputStream outResponse;

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

    cpkCore = new CpkCoreService( environment );

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

  @Test
  public void testGetElementsList() throws IOException, JSONException {
    boolean successful = true;

    OutputStream out = new ByteArrayOutputStream();
    cpkCore.getElementsList( out, bloatedMap );
    String str = out.toString();

    JSONArray elementsListJson = new JSONArray( str );

    for ( int i = 0; i < elementsListJson.length(); i++ ) {
      JSONObject obj = elementsListJson.getJSONObject( i );
      String id = obj.getString( "id" );
      if ( id.length() < 1 ) {
        successful = false;
      }
    }

    assertTrue( successful );
    out.close();
  }

  @Test
  public void testReloadRefreshStatus() throws IOException {
    OutputStream out = new ByteArrayOutputStream();
    refreshBloatedMapStream( out );

    cpkCore.reload( out, bloatedMap );

    String str = out.toString();
    out.close();

    assertTrue( str.contains( "cpkSol Status" ) );
    assertFalse( str.contains( "null" ) );
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
