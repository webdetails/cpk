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

import org.dom4j.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.engine.core.system.objfac.StandaloneSpringPentahoObjectFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import pt.webdetails.cpk.testUtils.CpkApiForTesting;
import pt.webdetails.cpk.testUtils.HttpServletResponseForTesting;
import pt.webdetails.cpk.testUtils.PentahoSystemForTesting;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

public class CpkApiTest {
  private CpkApiForTesting cpkApi;
  private static OutputStream out;
  private static OutputStream outResponse;
  private static String userDir = System.getProperty( "user.dir" );


  @BeforeClass
  public static void setUp() throws Exception {

    StandaloneApplicationContext appContext =
      new StandaloneApplicationContext( userDir + "/" + "test-resources/repository", "" );

    StandaloneSpringPentahoObjectFactory factory = new StandaloneSpringPentahoObjectFactory();
    factory.init( "test-resources/repository/system/pentahoObjects.spring.xml", null );

    ISystemSettings settings = buildAMockSystemSettings();
    PentahoSystemForTesting.setSystemSettingsService( settings );

    PentahoSystemForTesting.registerObjectFactory( factory );
    PentahoSystemForTesting.init( appContext );

    KettleEnvironment.init();
  }


  @Before
  public void beforeEachTest() throws Exception {
    cpkApi = new CpkApiForTesting();
  }


  @Test
  public void testCreateContent() throws Exception {

    PentahoSystemForTesting.runAsSystem( new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        outResponse = new ByteArrayOutputStream();

        sampleTrans();
        String sampleTrans_result = outResponse.toString();
        outResponse.close();
        outResponse = new ByteArrayOutputStream();

        evaluateResultRows();
        String evaluateResultRows_result = outResponse.toString();
        outResponse.close();
        outResponse = new ByteArrayOutputStream();

        createResultRows();
        String createResultRows_result = outResponse.toString();
        outResponse.close();
        outResponse = new ByteArrayOutputStream();

        generateRows();
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
        //Assert.assertTrue( evaluateResultRows );
        Assert.assertTrue( createResultRows );
        Assert.assertTrue( generateRows );
        return null;
      }
    } );

  }

  @Test
  public void testGetElementsList() throws Exception {
    PentahoSystemForTesting.runAsSystem( new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        boolean successful = true;
        out = new ByteArrayOutputStream();
        cpkApi.elementsList( null, new HttpServletResponseForTesting( out ), null );
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
        return null;
      }
    } );

  }

  @Test
  public void testReloadRefreshStatus() throws Exception {

    PentahoSystemForTesting.runAsSystem( new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        out = new ByteArrayOutputStream();
        cpkApi.reload( null, new HttpServletResponseForTesting( out ), null );
        String str = out.toString();
        out.close();
        Assert.assertTrue( str.contains( "cpkSol Status" ) );
        Assert.assertTrue( !str.contains( "null" ) );
        return null;
      }
    } );


  }

  /*
  @Test
  public void testGetPluginName() throws Exception {

    PentahoSystemForTesting.runAsSystem( new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        String str = cpkApi.getPluginName();

        Assert.assertTrue( str.equals( "cpkSol" ) ); //compare with a plugin I know
        return null;
      }
    } );
  }
  */

  @Test
  public void testGetSitemapJson() throws Exception {

    PentahoSystemForTesting.runAsSystem( new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        boolean successful = true;
        boolean sublinksExist = false;
        out = new ByteArrayOutputStream();
        cpkApi.getSitemapJson( new HttpServletResponseForTesting( out ) );
        String str = out.toString();
        out.close();

        JSONArray json = new JSONArray( str );

        for ( int i = 0; i < json.length(); i++ ) {
          JSONObject obj = json.getJSONObject( i );
          String id = obj.getString( "id" );
          String link = obj.getString( "link" );
          JSONArray sublinks = obj.getJSONArray( "sublinks" );
          if ( !( id.contains( "wcdf" ) && link.contains( "cpkSol" ) ) ) {
            if ( sublinks.length() > 0 ) {
              sublinksExist = true;
            } else {
              successful = false;
              break;
            }
          }
        }
        Assert.assertTrue( successful && sublinksExist );

        return null;
      }
    } );
  }

  private void sampleTrans() throws Exception {
    Map<String, Map<String, Object>> mainMap = new HashMap<String, Map<String, Object>>();
    Map<String, Object> path = new HashMap<String, Object>();
    Map<String, Object> request = new HashMap<String, Object>();

    path.put( "path", "/sampleTrans" ); //kjb or ktr
    path.put( "httpresponse", new HttpServletResponseForTesting( outResponse ) );
    path.put( "httprequest", new MockHttpServletRequest() );
    request.put( "paramarg1", "value1" );
    request.put( "paramarg2", "value2" );
    request.put( "paramarg3", "value3" );
    request.put( "kettleOutput", "Json" ); //not Infered kettle, so must pass Json Output
    mainMap.put( "path", path );
    mainMap.put( "request", request );

    cpkApi.createContent( mainMap );
  }

  private void evaluateResultRows() throws Exception {
    Map<String, Map<String, Object>> mainMap = new HashMap<String, Map<String, Object>>();
    Map<String, Object> path = new HashMap<String, Object>();
    Map<String, Object> request = new HashMap<String, Object>();
    path.put( "path", "/evaluate-result-rows" ); //kjb or ktr
    path.put( "httpresponse", new HttpServletResponseForTesting( outResponse ) );
    path.put( "httprequest", new MockHttpServletRequest() );
    request.put( "paramarg1", "value1" );
    request.put( "paramarg2", "value2" );
    request.put( "paramarg3", "value3" );
    mainMap.put( "path", path );
    mainMap.put( "request", request );
    cpkApi.createContent( mainMap );
  }

  private void createResultRows() throws Exception {
    Map<String, Map<String, Object>> mainMap = new HashMap<String, Map<String, Object>>();
    Map<String, Object> path = new HashMap<String, Object>();
    Map<String, Object> request = new HashMap<String, Object>();

    path.put( "path", "/create-result-rows" ); //kjb or ktr
    path.put( "httpresponse", new HttpServletResponseForTesting( outResponse ) );
    path.put( "httprequest", new MockHttpServletRequest() );
    request.put( "stepName", "OUTPUT Copy rows to result" );
    request.put( "paramarg1", "value1" );
    request.put( "paramarg2", "value2" );
    request.put( "paramarg3", "value3" );
    mainMap.put( "path", path );
    mainMap.put( "request", request );
    cpkApi.createContent( mainMap );
  }

  private void generateRows() throws Exception {
    Map<String, Map<String, Object>> mainMap = new HashMap<String, Map<String, Object>>();
    Map<String, Object> path = new HashMap<String, Object>();
    Map<String, Object> request = new HashMap<String, Object>();

    path.put( "path", "/generate-rows" ); //kjb or ktr
    path.put( "httpresponse", new HttpServletResponseForTesting( outResponse ) );
    path.put( "httprequest", new MockHttpServletRequest() );
    request.put( "stepName", "output" );
    mainMap.put( "path", path );
    mainMap.put( "request", request );
    cpkApi.createContent( mainMap );
  }

  private static ISystemSettings buildAMockSystemSettings() {
    return new ISystemSettings() {
      @Override public String getSystemCfgSourceName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
      }

      @Override public String getSystemSetting( String path, String settingName, String defaultValue ) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
      }

      @Override public String getSystemSetting( String settingName, String defaultValue ) {
        return "Administrator";  //To change body of implemented methods use File | Settings | File Templates.
      }

      @Override public List getSystemSettings( String path, String settingSection ) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
      }

      @Override public List getSystemSettings( String settingSection ) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
      }

      @Override public void resetSettingsCache() {
        //To change body of implemented methods use File | Settings | File Templates.
      }

      @Override public Document getSystemSettingsDocument( String actionPath ) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
      }

      @Override public Properties getSystemSettingsProperties( String path ) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
      }
    };
  }


}
