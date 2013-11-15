/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cpk;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.dom4j.DocumentException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pt.webdetails.cpf.http.ICommonParameterProvider;
import pt.webdetails.cpf.utils.IPluginUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.objfac.StandaloneSpringPentahoObjectFactory;
import org.pentaho.platform.engine.security.SecurityHelper;
import pt.webdetails.cpf.RestRequestHandler;
import pt.webdetails.cpf.http.CommonParameterProvider;
import pt.webdetails.cpf.repository.IRepositoryAccess;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import pt.webdetails.cpf.utils.PluginUtils;
import pt.webdetails.cpk.testUtils.CpkContentGeneratorForTesting;
import pt.webdetails.cpk.testUtils.PentahoRepositoryAccessForTesting;

/**
 * @author joao
 */
public class CpkContentGeneratorTest {

  private static IPluginUtils pluginUtils;
  //private static CpkContentGenerator cpkContentGenerator;
  private static CpkContentGeneratorForTesting cpkContentGenerator;
  //private static Map<String, ICommonParameterProvider> map;
  private static IRepositoryAccess repAccess;
  private static OutputStream out;
  private static OutputStream outResponse;
  private static String userDir = System.getProperty( "user.dir" );
  //private static PentahoSession session = new PentahoSession();
  private static StandaloneSession session = new StandaloneSession( "joe" );

  @BeforeClass
  public static void setUp() throws IOException, InitializationException, ObjectFactoryException {


    StandaloneApplicationContext appContext =
      new StandaloneApplicationContext( userDir + "/" + "test-resources/repository", "" );

    StandaloneSpringPentahoObjectFactory factory = new StandaloneSpringPentahoObjectFactory();
    factory.init( "test-resources/repository/system/pentahoObjects.spring.xml", null );


    GrantedAuthority[] roles = new GrantedAuthority[ 2 ];
    roles[ 0 ] = new GrantedAuthorityImpl( "Authenticated" ); //$NON-NLS-1$
    roles[ 1 ] = new GrantedAuthorityImpl( "Admin" ); //$NON-NLS-1$
    Authentication auth = new UsernamePasswordAuthenticationToken( "joe", "password", roles ); //$NON-NLS-1$
    session.setAttribute( SecurityHelper.SESSION_PRINCIPAL, auth );


    PentahoSessionHolder.setSession( session );
    PentahoSystem.setObjectFactory( factory );
    PentahoSystem.setSystemSettingsService( factory.get( ISystemSettings.class, "systemSettingsService", session ) );
    PentahoSystem.init( appContext );

    pluginUtils = new PluginUtils();
    //CorePlugin plugin = new Plugin(pluginUtils.getPluginDirectory().getPath());
    repAccess = new PentahoRepositoryAccessForTesting();
    //repAccess.setPlugin(plugin);
    ICpkEnvironment environment = new CpkPentahoEnvironment( pluginUtils, repAccess );
    //cpkContentGenerator = new CpkContentGeneratorForTesting(environment);
    cpkContentGenerator = new CpkContentGeneratorForTesting();


  }

  @Test
  public void testCreateContent() throws Exception {
    KettleEnvironment.init();
    outResponse = new ByteArrayOutputStream();


    cpkContentGenerator.setParameterProviders( unwrapParams( sampleTrans() ) );
    cpkContentGenerator.wrapParameters();
    cpkContentGenerator.createContent();
    String sampleTrans_result = outResponse.toString();
    outResponse.close();
    outResponse = new ByteArrayOutputStream();

    cpkContentGenerator.setParameterProviders( unwrapParams( evaluateResultRows() ) );
    cpkContentGenerator.wrapParameters();
    cpkContentGenerator.createContent();
    String evaluateResultRows_result = outResponse.toString();
    outResponse.close();
    outResponse = new ByteArrayOutputStream();

    cpkContentGenerator.setParameterProviders( unwrapParams( createResultRows() ) );
    cpkContentGenerator.wrapParameters();
    cpkContentGenerator.createContent();
    String createResultRows_result = outResponse.toString();
    outResponse.close();
    outResponse = new ByteArrayOutputStream();

    cpkContentGenerator.setParameterProviders( unwrapParams( generateRows() ) );
    cpkContentGenerator.wrapParameters();
    cpkContentGenerator.createContent();
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
    cpkContentGenerator.elementsList( out );
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
  public void testReloadRefreshStatus() throws DocumentException, IOException {

    out = new ByteArrayOutputStream();
    cpkContentGenerator.reload( out );
    String str = out.toString();
    out.close();
    Assert.assertTrue( str.contains( "cpkSol Status" ) );
    Assert.assertTrue( !str.contains( "null" ) );

  }

  @Test
  public void testGetRequestHandler() {
    RestRequestHandler r = cpkContentGenerator.getRequestHandler();
    Assert.assertTrue( r != null );
  }

  @Test
  public void testGetPluginName() {

    String str = cpkContentGenerator.getPluginName();

    Assert.assertTrue( str.equals( "cpkSol" ) );//compare with a plugin I know

  }

  @Test
  public void testGetSitemapJson() throws IOException, JSONException {

    boolean successful = true;
    boolean sublinksExist = false;
    out = new ByteArrayOutputStream();
    cpkContentGenerator.getSitemapJson( out );
    String str = out.toString();
    out.close();

    JSONArray json = new JSONArray( str );

    for ( int i = 0; i < json.length(); i++ ) {
      JSONObject obj = json.getJSONObject( i );
      String id = obj.getString( "id" );
      String link = obj.getString( "link" );
      String name = obj.getString( "name" );
      JSONArray sublinks = obj.getJSONArray( "sublinks" );
      if ( id.contains( "wcdf" ) && link.contains( "cpkSol" ) ) {
      } else { //probably a folder with sublinks
        if ( sublinks.length() > 0 ) {
          sublinksExist = true;
        } else {
          successful = false;
          break;
        }
      }
    }
    Assert.assertTrue( successful && sublinksExist );


  }

  private Map<String, ICommonParameterProvider> sampleTrans() {
    Map<String, ICommonParameterProvider> map = new HashMap<String, ICommonParameterProvider>();
    ICommonParameterProvider p = new CommonParameterProvider();
    ICommonParameterProvider p1 = new CommonParameterProvider();
    p.put( "path", "/sampleTrans" );//kjb or ktr
    p.put( "httpresponse", buildAResponse( outResponse ) );
    p1.put( "paramarg1", "value1" );
    p1.put( "paramarg2", "value2" );
    p1.put( "paramarg3", "value3" );
    p1.put( "kettleOutput", "Json" );//not Infered kettle, so must pass Json Output
    map.put( "path", p );
    map.put( "request", p1 );
    return map;
  }

  private Map<String, ICommonParameterProvider> evaluateResultRows() {
    Map<String, ICommonParameterProvider> map = new HashMap<String, ICommonParameterProvider>();
    ICommonParameterProvider p = new CommonParameterProvider();
    ICommonParameterProvider p1 = new CommonParameterProvider();
    p.put( "path", "/evaluate-result-rows" );//kjb or ktr
    p.put( "httpresponse", buildAResponse( outResponse ) );
    p1.put( "paramarg1", "value1" );
    p1.put( "paramarg2", "value2" );
    p1.put( "paramarg3", "value3" );
    map.put( "path", p );
    map.put( "request", p1 );
    return map;
  }

  private Map<String, ICommonParameterProvider> createResultRows() {
    Map<String, ICommonParameterProvider> map = new HashMap<String, ICommonParameterProvider>();
    ICommonParameterProvider p = new CommonParameterProvider();
    ICommonParameterProvider p1 = new CommonParameterProvider();
    p.put( "path", "/create-result-rows" );//kjb or ktr
    p.put( "httpresponse", buildAResponse( outResponse ) );
    p1.put( "stepName", "copy rows to result" );
    p1.put( "paramarg1", "value1" );
    p1.put( "paramarg2", "value2" );
    p1.put( "paramarg3", "value3" );
    map.put( "path", p );
    map.put( "request", p1 );
    return map;
  }

  private Map<String, ICommonParameterProvider> generateRows() {
    Map<String, ICommonParameterProvider> map = new HashMap<String, ICommonParameterProvider>();
    ICommonParameterProvider p = new CommonParameterProvider();
    ICommonParameterProvider p1 = new CommonParameterProvider();
    p.put( "path", "/generate-rows" );//kjb or ktr
    p.put( "httpresponse", buildAResponse( outResponse ) );
    p1.put( "stepName", "output" );
    map.put( "path", p );
    map.put( "request", p1 );
    return map;
  }

  private static Map<String, IParameterProvider> unwrapParams( Map<String, ICommonParameterProvider> params ) {

    Map<String, IParameterProvider> resultMap = new HashMap<String, IParameterProvider>();
    SimpleParameterProvider result = new SimpleParameterProvider();
    Iterator<Entry<String, ICommonParameterProvider>> it = params.entrySet().iterator();
    while ( it.hasNext() ) {
      Entry<String, ICommonParameterProvider> e = it.next();
      Iterator<String> names = e.getValue().getParameterNames();
      while ( names.hasNext() ) {
        String name = names.next();
        Object value = e.getValue().getParameter( name );
        result.setParameter( name, value );
      }
      resultMap.put( e.getKey(), result );
      result = new SimpleParameterProvider();

    }
    return resultMap;

  }

  private HttpServletResponse buildAResponse( final OutputStream output ) {
    return new HttpServletResponse() {
      @Override
      public ServletOutputStream getOutputStream() throws IOException {
        return new ServletOutputStream() {
          @Override
          public void write( int b ) throws IOException {
            output.write( b );
          }
        };
      }

      @Override
      public void setHeader( String string, String string1 ) {
        //this method will be called in the tests
      }

      @Override
      public void setIntHeader( String string, int i ) {
        throw new UnsupportedOperationException(
          "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
      }

      @Override
      public void addIntHeader( String string, int i ) {
        throw new UnsupportedOperationException(
          "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
      }

      @Override
      public void setStatus( int i ) {
        throw new UnsupportedOperationException(
          "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
      }

      @Override
      public void setStatus( int i, String string ) {
        throw new UnsupportedOperationException(
          "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
      }

      @Override
      public String getCharacterEncoding() {
        throw new UnsupportedOperationException(
          "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
      }

      @Override
      public String getContentType() {
        throw new UnsupportedOperationException(
          "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
      }

      @Override
      public void addCookie( Cookie cookie ) {
        throw new UnsupportedOperationException(
          "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
      }

      @Override
      public boolean containsHeader( String string ) {
        throw new UnsupportedOperationException(
          "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
      }

      @Override
      public String encodeURL( String string ) {
        throw new UnsupportedOperationException(
          "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
      }

      @Override
      public String encodeRedirectURL( String string ) {
        throw new UnsupportedOperationException(
          "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
      }

      @Override
      public String encodeUrl( String string ) {
        throw new UnsupportedOperationException(
          "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
      }

      @Override
      public String encodeRedirectUrl( String string ) {
        throw new UnsupportedOperationException(
          "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
      }

      @Override
      public void sendError( int i, String string ) throws IOException {
        throw new UnsupportedOperationException(
          "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
      }

      @Override
      public void sendError( int i ) throws IOException {
        throw new UnsupportedOperationException(
          "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
      }

      @Override
      public void sendRedirect( String string ) throws IOException {
        throw new UnsupportedOperationException(
          "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
      }

      @Override
      public void setDateHeader( String string, long l ) {
        throw new UnsupportedOperationException(
          "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
      }

      @Override
      public void addDateHeader( String string, long l ) {
        throw new UnsupportedOperationException(
          "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
      }

      @Override
      public PrintWriter getWriter() throws IOException {
        throw new UnsupportedOperationException(
          "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
      }

      @Override
      public void setCharacterEncoding( String string ) {
        throw new UnsupportedOperationException(
          "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
      }

      @Override
      public void setContentLength( int i ) {
        throw new UnsupportedOperationException(
          "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
      }

      @Override
      public void setContentType( String string ) {
        throw new UnsupportedOperationException(
          "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
      }

      @Override
      public void setBufferSize( int i ) {
        throw new UnsupportedOperationException(
          "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
      }

      @Override
      public int getBufferSize() {
        throw new UnsupportedOperationException(
          "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
      }

      @Override
      public void flushBuffer() throws IOException {
        throw new UnsupportedOperationException(
          "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
      }

      @Override
      public void resetBuffer() {
        throw new UnsupportedOperationException(
          "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
      }

      @Override
      public boolean isCommitted() {
        throw new UnsupportedOperationException(
          "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
      }

      @Override
      public void reset() {
        throw new UnsupportedOperationException(
          "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
      }

      @Override
      public void setLocale( Locale locale ) {
        throw new UnsupportedOperationException(
          "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
      }

      @Override
      public Locale getLocale() {
        throw new UnsupportedOperationException(
          "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
      }

      @Override
      public void addHeader( String string, String string1 ) {
        throw new UnsupportedOperationException(
          "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
      }
    };
  }
}
