/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cpk;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.dom4j.DocumentException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pt.webdetails.cpf.http.ICommonParameterProvider;
import pt.webdetails.cpf.repository.api.FileAccess;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IBasicFileFilter;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.repository.api.IRWAccess;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.repository.api.IUserContentAccess;
import pt.webdetails.cpf.repository.IRepositoryAccess;
import pt.webdetails.cpf.session.ISessionUtils;
import pt.webdetails.cpf.utils.IPluginUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;
import pt.webdetails.cpf.RestRequestHandler;
import pt.webdetails.cpf.http.CommonParameterProvider;
import pt.webdetails.cpf.impl.SimpleSessionUtils;
import pt.webdetails.cpf.impl.SimpleUserSession;
import pt.webdetails.cpk.elements.IElement;
import pt.webdetails.cpk.security.IAccessControl;
import pt.webdetails.cpk.testUtils.HttpServletResponseForTesting;
import pt.webdetails.cpk.testUtils.PluginUtilsForTesting;
import pt.webdetails.cpf.repository.vfs.VfsRepositoryAccess;
import pt.webdetails.cpf.session.IUserSession;
import org.pentaho.di.core.KettleEnvironment;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author joao
 */
public class CpkCoreServiceTest {

  private static IPluginUtils pluginUtils;
  private static CpkCoreServiceForTesting cpkCore;
  private static Map<String, Map<String, Object>> bloatedMap;
  private static IRepositoryAccess repAccess;
  private static OutputStream out;
  private static OutputStream outResponse;
  private static String userDir = System.getProperty( "user.dir" );

  @BeforeClass
  public static void setUp() throws IOException, InitializationException {

    repAccess = new VfsRepositoryAccess( userDir + "/test-resources/repository",
      userDir + "/test-resources/settings" );
    pluginUtils = new PluginUtilsForTesting();
    final IUserSession userSession = new SimpleUserSession( "userName", null, true, null );
    ICpkEnvironment environment = new ICpkEnvironment() {
      @Override
      public IPluginUtils getPluginUtils() {
        return pluginUtils;
      }

      @Override public IContentAccessFactory getContentAccessFactory() {
        return getContentFactory();
      }

      @Override
      public IAccessControl getAccessControl() {
        return new IAccessControl() {
          @Override
          public boolean isAllowed( IElement element ) {
            return true;
          }

          @Override
          public boolean isAdmin() {
            return true;
          }

          @Override
          public void throwAccessDenied( HttpServletResponse response ) {
            throw new UnsupportedOperationException( "Not supported yet." );
          }
        };
      }

      @Override
      public String getPluginName() {
        return pluginUtils.getPluginName();
      }

      @Override
      public ISessionUtils getSessionUtils() {
        return new SimpleSessionUtils( userSession, null, null );
      }

      @Override
      public void reload() {
      }
    };
    cpkCore = new CpkCoreServiceForTesting( environment );
    bloatedMap =  buildBloatedMap(null,null);


  }

  @Test
  public void testCreateContent() throws Exception {//start a hypersonic to test
    KettleEnvironment.init();
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

  @Test
  public void testGetPluginName() {

    String str = cpkCore.getPluginName();
    Assert.assertTrue( str.equals( "cpkSol" ) );

  }

  private Map<String, Map<String, Object>> sampleTrans() {
    Map<String,Map<String,Object>> mainMap = new HashMap<String, Map<String, Object>>();
    Map<String,Object> requestMap = new HashMap<String, Object>();
    Map<String,Object> pathMap = new HashMap<String, Object>();

    requestMap.put( "request", "unnecessary value?" );
    requestMap.put( "paramarg1", "value1" );
    requestMap.put( "paramarg2", "value2" );
    requestMap.put( "paramarg3", "value3" );
    requestMap.put( "kettleOutput", "Json" );

    pathMap.put( "path", "/sampleTrans" );
    pathMap.put("httpresponse",new HttpServletResponseForTesting(outResponse));

    mainMap.put( "request", requestMap );
    mainMap.put( "path", pathMap );
    return mainMap;
  }

  private Map<String, Map<String, Object>> evaluateResultRows() {
    Map<String,Map<String,Object>> mainMap = new HashMap<String, Map<String, Object>>();

    Map<String,Object> requestMap = new HashMap<String, Object>();
    Map<String,Object> pathMap = new HashMap<String, Object>();

    requestMap.put( "request", "unnecessary value?" );
    requestMap.put( "paramarg1", "value1" );
    requestMap.put( "paramarg2", "value2" );
    requestMap.put( "paramarg3", "value3" );

    pathMap.put( "path", "/evaluate-result-rows" );
    pathMap.put("httpresponse",new HttpServletResponseForTesting(outResponse));

    mainMap.put( "request", requestMap );
    mainMap.put( "path", pathMap );
    return mainMap;
  }

  private Map<String, Map<String,Object>> createResultRows() {
    Map<String,Map<String,Object>> mainMap = new HashMap<String, Map<String, Object>>();

    Map<String,Object> requestMap = new HashMap<String, Object>();
    Map<String,Object> pathMap = new HashMap<String, Object>();

    requestMap.put( "request", "unnecessary value?" );
    requestMap.put( "paramarg1", "value1" );
    requestMap.put( "paramarg2", "value2" );
    requestMap.put( "paramarg3", "value3" );
    requestMap.put( "stepName", "copy rows to result" );

    pathMap.put( "path", "/create-result-rows" );
    pathMap.put("httpresponse",new HttpServletResponseForTesting(outResponse));

    mainMap.put( "request", requestMap );
    mainMap.put( "path", pathMap );
    return mainMap;
  }

  private Map<String, Map<String,Object>> generateRows() {
    Map<String,Map<String,Object>> mainMap = new HashMap<String, Map<String, Object>>();

    Map<String,Object> requestMap = new HashMap<String, Object>();
    Map<String,Object> pathMap = new HashMap<String, Object>();

    requestMap.put( "request", "unnecessary value?" );
    requestMap.put( "paramarg1", "value1" );
    requestMap.put( "paramarg2", "value2" );
    requestMap.put( "paramarg3", "value3" );
    requestMap.put( "stepName", "output" );

    pathMap.put( "path", "/generate-rows" );
    pathMap.put("httpresponse",new HttpServletResponseForTesting(outResponse));

    mainMap.put( "request", requestMap );
    mainMap.put( "path", pathMap );
    return mainMap;
  }

  private static IContentAccessFactory getContentFactory() {
    return new IContentAccessFactory() {
      @Override public IUserContentAccess getUserContentAccess( String s ) {
        return new IUserContentAccess() {
          @Override public boolean hasAccess( String s, FileAccess fileAccess ) {
            return true;  //To change body of implemented methods use File | Settings | File Templates.
          }

          @Override public boolean saveFile( String s, InputStream inputStream ) {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
          }

          @Override public boolean copyFile( String s, String s2 ) {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
          }

          @Override public boolean deleteFile( String s ) {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
          }

          @Override public boolean createFolder( String s ) {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
          }

          @Override public InputStream getFileInputStream( String s ) throws IOException {
            //return repAccess.getResourceInputStream(
            // "/home/joao/work/ctools/cpk/cpk-core/test-resources/settings/cpk.xml" );
            return new ByteArrayInputStream( repAccess.getSettingsResourceAsString( "cpk.xml" ).getBytes() );
          }

          @Override public boolean fileExists( String s ) {
            return true;
          }

          @Override public long getLastModified( String s ) {
            return 0;  //To change body of implemented methods use File | Settings | File Templates.
          }

          @Override public List<IBasicFile> listFiles( String s, IBasicFileFilter iBasicFileFilter, int i, boolean b ) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
          }

          @Override public List<IBasicFile> listFiles( String s, IBasicFileFilter iBasicFileFilter, int i ) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
          }

          @Override public List<IBasicFile> listFiles( String s, IBasicFileFilter iBasicFileFilter ) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
          }

          @Override public IBasicFile fetchFile( String s ) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
          }
        };  //To change body of implemented methods use File | Settings | File Templates.
      }

      @Override public IReadAccess getPluginRepositoryReader( String s ) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
      }

      @Override public IRWAccess getPluginRepositoryWriter( String s ) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
      }

      @Override public IReadAccess getPluginSystemReader( String s ) {
        return new IReadAccess() {
          @Override public InputStream getFileInputStream( String s ) throws IOException {
            return new ByteArrayInputStream( repAccess.getSettingsResourceAsString( "cpk.xml" ).getBytes() );
          }

          @Override public boolean fileExists( String s ) {
            return true;  //To change body of implemented methods use File | Settings | File Templates.
          }

          @Override public long getLastModified( String s ) {
            return 0;  //To change body of implemented methods use File | Settings | File Templates.
          }

          @Override public List<IBasicFile> listFiles( String s, IBasicFileFilter iBasicFileFilter, int i, boolean b ) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
          }

          @Override public List<IBasicFile> listFiles( String s, IBasicFileFilter iBasicFileFilter, int i ) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
          }

          @Override public List<IBasicFile> listFiles( String s, IBasicFileFilter iBasicFileFilter ) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
          }

          @Override public IBasicFile fetchFile( String s ) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
          }
        };  //To change body of implemented methods use File | Settings | File Templates.
      }

      @Override public IRWAccess getPluginSystemWriter( String s ) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
      }

      @Override public IReadAccess getOtherPluginSystemReader( String s, String s2 ) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
      }

      @Override public IRWAccess getOtherPluginSystemWriter( String s, String s2 ) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
      }
    };

  }


  private static Map<String,Map<String,Object>> buildBloatedMap( HttpServletRequest request, HttpServletResponse response ) {
    Map<String,Map<String,Object>> mainMap = new HashMap<String, Map<String, Object>>();

    Map<String,Object> requestMap = new HashMap<String, Object>();
    Map<String,Object> pathMap = new HashMap<String, Object>();

    requestMap.put( "request", "unnecessary value?" );
    requestMap.put( "paramarg1", "value1" );
    requestMap.put( "paramarg2", "value2" );
    requestMap.put( "paramarg3", "value3" );

    pathMap.put( "path", "/pass_arguments" );
    pathMap.put("httpresponse",new HttpServletResponseForTesting(outResponse));

    mainMap.put( "request", requestMap );
    mainMap.put( "path", pathMap );
    return mainMap;
  }

  private static void refreshBloatedMapStream(OutputStream out){
    Map<String,Object> temp = new HashMap<String, Object>();
    temp.put( "response",new HttpServletResponseForTesting( out ) );
    bloatedMap.put( "response", temp );
  }

}
