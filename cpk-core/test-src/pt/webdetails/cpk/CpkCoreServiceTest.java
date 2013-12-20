/*!
* Copyright 2002 - 2013 Webdetails, a Pentaho company.  All rights reserved.
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

import org.dom4j.DocumentException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import pt.webdetails.cpf.RestRequestHandler;
import pt.webdetails.cpf.exceptions.InitializationException;
import pt.webdetails.cpf.repository.IRepositoryAccess;
import pt.webdetails.cpf.repository.vfs.VfsRepositoryAccess;
import pt.webdetails.cpf.utils.IPluginUtils;
import pt.webdetails.cpk.testUtils.CpkEnvironmentForTesting;
import pt.webdetails.cpk.testUtils.HttpServletResponseForTesting;
import pt.webdetails.cpk.testUtils.PluginUtilsForTesting;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class CpkCoreServiceTest {

  private static IPluginUtils pluginUtils;
  private static CpkCoreService cpkCore;
  private static Map<String, Map<String, Object>> bloatedMap;
  private static IRepositoryAccess repAccess;
  private static OutputStream out;
  private static OutputStream outResponse;
  private static String userDir = System.getProperty( "user.dir" );

  @BeforeClass
  public static void setUp() throws IOException, InitializationException, KettleException {

    repAccess = new VfsRepositoryAccess( userDir + "/test-resources/repository",
      userDir + "/test-resources/settings" );

    pluginUtils = new PluginUtilsForTesting();
    ICpkEnvironment environment = new CpkEnvironmentForTesting( pluginUtils, repAccess );

    KettleEnvironment.init();
    cpkCore = new CpkCoreService( environment );

    bloatedMap = buildBloatedMap( null, null );


  }

  @Test
  public void testCreateContent() throws Exception { //start a hypersonic to test
    outResponse = new ByteArrayOutputStream();


    cpkCore.createContent( sampleTrans() );
    String sampleTrans_result = outResponse.toString();
    outResponse.close();
    outResponse = new ByteArrayOutputStream();

    cpkCore.createContent( evaluateResultRows() );
    String evaluateResultRows_result = outResponse.toString();
    outResponse.close();
    outResponse = new ByteArrayOutputStream();

    cpkCore.createContent( createResultRows() );
    String createResultRows_result = outResponse.toString();
    outResponse.close();
    outResponse = new ByteArrayOutputStream();

    cpkCore.createContent( generateRows() );
    String generateRows_result = outResponse.toString();
    outResponse.close();


    boolean sampletrans, evaluateResultRows, createResultRows, generateRows;
    sampletrans = evaluateResultRows = createResultRows = generateRows = true;

    JSONObject sampletransJson = new JSONObject( sampleTrans_result );
    JSONObject evaluateResultRowsJson = new JSONObject( evaluateResultRows_result );
    JSONObject createResultRowsJson = new JSONObject( createResultRows_result );
    JSONObject generateRowsJson = new JSONObject( generateRows_result );

    if ( sampletransJson.getJSONObject( "queryInfo" ).length() < 1 ) {
      sampletrans = false;
    }
    if ( generateRowsJson.getJSONObject( "queryInfo" ).length() < 1 ) {
      generateRows = false;
    }
    if ( createResultRowsJson.getJSONObject( "queryInfo" ).length() < 1 ) {
      createResultRows = false;
    }
    if ( !evaluateResultRowsJson.getBoolean( "result" ) ) {
      evaluateResultRows = false;
    }

    Assert.assertTrue( sampletrans );
    Assert.assertTrue( evaluateResultRows );
    Assert.assertTrue( createResultRows );
    Assert.assertTrue( generateRows );

  }

  @Test
  public void testGetElementsList() throws IOException, JSONException {
    boolean successful = true;

    out = new ByteArrayOutputStream();
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

    Assert.assertTrue( successful );
    out.close();
  }

  @Test
  public void testReloadRefreshStatus() throws DocumentException, IOException, JSONException {
    out = new ByteArrayOutputStream();
    refreshBloatedMapStream( out );
    cpkCore.reload( out, bloatedMap );
    String str = out.toString();
    out.close();
    Assert.assertTrue( str.contains( "cpkSol Status" ) );
    Assert.assertTrue( !str.contains( "null" ) );
  }

  @Test
  public void testGetRequestHandler() {
    RestRequestHandler r = cpkCore.getRequestHandler();
    Assert.assertTrue( r != null );
  }

  private Map<String, Map<String, Object>> sampleTrans() {
    Map<String, Map<String, Object>> mainMap = new HashMap<String, Map<String, Object>>();
    Map<String, Object> requestMap = new HashMap<String, Object>();
    Map<String, Object> pathMap = new HashMap<String, Object>();

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
    Map<String, Map<String, Object>> mainMap = new HashMap<String, Map<String, Object>>();

    Map<String, Object> requestMap = new HashMap<String, Object>();
    Map<String, Object> pathMap = new HashMap<String, Object>();

    requestMap.put( "request", "unnecessary value?" );
    requestMap.put( "paramarg1", "value1" );
    requestMap.put( "paramarg2", "value2" );
    requestMap.put( "paramarg3", "value3" );

    pathMap.put( "path", "/evaluate-result-rows" );
    pathMap.put( "httpresponse", new HttpServletResponseForTesting( outResponse ) );

    mainMap.put( "request", requestMap );
    mainMap.put( "path", pathMap );
    return mainMap;
  }

  private Map<String, Map<String, Object>> createResultRows() {
    Map<String, Map<String, Object>> mainMap = new HashMap<String, Map<String, Object>>();

    Map<String, Object> requestMap = new HashMap<String, Object>();
    Map<String, Object> pathMap = new HashMap<String, Object>();

    requestMap.put( "request", "unnecessary value?" );
    requestMap.put( "paramarg1", "value1" );
    requestMap.put( "paramarg2", "value2" );
    requestMap.put( "paramarg3", "value3" );
    requestMap.put( "stepName", "copy rows to result" );

    pathMap.put( "path", "/create-result-rows" );
    pathMap.put( "httpresponse", new HttpServletResponseForTesting( outResponse ) );

    mainMap.put( "request", requestMap );
    mainMap.put( "path", pathMap );
    return mainMap;
  }

  private Map<String, Map<String, Object>> generateRows() {
    Map<String, Map<String, Object>> mainMap = new HashMap<String, Map<String, Object>>();

    Map<String, Object> requestMap = new HashMap<String, Object>();
    Map<String, Object> pathMap = new HashMap<String, Object>();

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


  private static Map<String, Map<String, Object>> buildBloatedMap( HttpServletRequest request,
                                                                   HttpServletResponse response ) {
    Map<String, Map<String, Object>> mainMap = new HashMap<String, Map<String, Object>>();

    Map<String, Object> requestMap = new HashMap<String, Object>();
    Map<String, Object> pathMap = new HashMap<String, Object>();

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
    Map<String, Object> temp = new HashMap<String, Object>();
    temp.put( "response", new HttpServletResponseForTesting( out ) );
    bloatedMap.put( "response", temp );
  }

}
