package pt.webdetails.cpk;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.core.mt.Tenant;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.objfac.StandaloneSpringPentahoObjectFactory;
import org.pentaho.platform.plugin.services.security.userrole.PentahoCachingUserDetailsService;
import org.springframework.dao.DataAccessException;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;
import org.springframework.mock.web.MockHttpServletRequest;
import pt.webdetails.cpf.RestRequestHandler;
import pt.webdetails.cpf.http.CommonParameterProvider;
import pt.webdetails.cpf.http.ICommonParameterProvider;
import pt.webdetails.cpf.utils.IPluginUtils;
import pt.webdetails.cpf.utils.PluginUtils;
import pt.webdetails.cpk.testUtils.CpkApiForTesting;
import pt.webdetails.cpk.testUtils.HttpServletResponseForTesting;
import pt.webdetails.cpk.testUtils.PentahoSystemForTesting;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created with IntelliJ IDEA. User: joao Date: 11/1/13 Time: 10:52 AM To change this template use File | Settings |
 * File Templates.
 */
public class CpkApiTest {
  private static IPluginUtils pluginUtils;
  private static CpkApiForTesting cpkApi;
  private static OutputStream out;
  private static OutputStream outResponse;
  private static String userDir = System.getProperty( "user.dir" );


  @BeforeClass
  public static void setUp() throws Exception {

    StandaloneApplicationContext appContext =
      new StandaloneApplicationContext( userDir + "/" + "test-resources/repository", "" );

    StandaloneSpringPentahoObjectFactory factory = new StandaloneSpringPentahoObjectFactory();
    factory.init( "test-resources/repository/system/pentahoObjects.spring.xml", null );
    StandaloneSession session = new StandaloneSession( "admin" );


    PentahoCachingUserDetailsService p =
      new PentahoCachingUserDetailsService( getUserDetailsService(), getTenantPrincipleNameResolver() );
    PentahoSystemForTesting.registerObjectFactory( factory );
    PentahoSystemForTesting.init( appContext );


    pluginUtils = new PluginUtils();
    cpkApi = new CpkApiForTesting();

  }

  @Test
  public void testCreateContent() throws Exception {

    PentahoSystemForTesting.runAsSystem( new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        KettleEnvironment.init();
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
        Assert.assertTrue( evaluateResultRows );
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


  @Test
  public void testGetPluginName() throws Exception {

    PentahoSystemForTesting.runAsSystem( new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        String str = cpkApi.getPluginName();

        Assert.assertTrue( str.equals( "cpkSol" ) );//compare with a plugin I know
        return null;
      }
    } );
  }

  @Test
  public void testGetSitemapJson() throws Exception {

    PentahoSystemForTesting.runAsSystem( new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        boolean successful = true;
        boolean sublinksExist = false;
        out = new ByteArrayOutputStream();
        cpkApi
          .getSitemapJson( new HttpServletResponseForTesting( out ) );
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

        return null;
      }
    } );
  }

  private void sampleTrans() throws Exception {
    Map<String, Map<String, Object>> mainMap = new HashMap<String, Map<String, Object>>();
    Map<String, Object> path = new HashMap<String, Object>();
    Map<String, Object> request = new HashMap<String, Object>();

    path.put( "path", "/sampleTrans" );//kjb or ktr
    path.put( "httpresponse", new HttpServletResponseForTesting( outResponse ) );
    path.put( "httprequest", new MockHttpServletRequest() );
    request.put( "paramarg1", "value1" );
    request.put( "paramarg2", "value2" );
    request.put( "paramarg3", "value3" );
    request.put( "kettleOutput", "Json" );//not Infered kettle, so must pass Json Output
    mainMap.put( "path", path );
    mainMap.put( "request", request );

    cpkApi.createContent( mainMap );
  }

  private void evaluateResultRows() throws Exception {
    Map<String, Map<String, Object>> mainMap = new HashMap<String, Map<String, Object>>();
    Map<String, Object> path = new HashMap<String, Object>();
    Map<String, Object> request = new HashMap<String, Object>();
    path.put( "path", "/evaluate-result-rows" );//kjb or ktr
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

    path.put( "path", "/create-result-rows" );//kjb or ktr
    path.put( "httpresponse", new HttpServletResponseForTesting( outResponse ) );
    path.put( "httprequest", new MockHttpServletRequest() );
    request.put( "stepName", "copy rows to result" );
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

    path.put( "path", "/generate-rows" );//kjb or ktr
    path.put( "httpresponse", new HttpServletResponseForTesting( outResponse ) );
    path.put( "httprequest", new MockHttpServletRequest() );
    request.put( "stepName", "output" );
    mainMap.put( "path", path );
    mainMap.put( "request", request );
    cpkApi.createContent( mainMap );
  }

  private static UserDetailsService getUserDetailsService() {
    return new UserDetailsService() {
      @Override public UserDetails loadUserByUsername( String s )
        throws UsernameNotFoundException, DataAccessException {
        return new UserDetails() {
          @Override public GrantedAuthority[] getAuthorities() {
            return new GrantedAuthority[ 0 ];
          }

          @Override public String getPassword() {
            return "##debugPassword##";
          }

          @Override public String getUsername() {
            return "##debugUsername##";
          }

          @Override public boolean isAccountNonExpired() {
            return true;
          }

          @Override public boolean isAccountNonLocked() {
            return true;
          }

          @Override public boolean isCredentialsNonExpired() {
            return true;
          }

          @Override public boolean isEnabled() {
            return true;
          }
        };
      }
    };
  }

  private static ITenantedPrincipleNameResolver getTenantPrincipleNameResolver() {
    return new ITenantedPrincipleNameResolver() {
      @Override public ITenant getTenant( String s ) {
        return new Tenant();
      }

      @Override public String getPrincipleName( String s ) {
        return "admin";  //To change body of implemented methods use File | Settings | File Templates.
      }

      @Override public String getPrincipleId( ITenant iTenant, String s ) {
        return "admin";  //To change body of implemented methods use File | Settings | File Templates.
      }

      @Override public boolean isValid( String s ) {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
      }
    };
  }

  private static MockHttpServletRequest buildRequest( Map<String, String> params ) {
    MockHttpServletRequest request = new MockHttpServletRequest();

    Iterator<String> it = params.keySet().iterator();
    while ( it.hasNext() ) {
      String key = it.next();
      request.addParameter( key, params.get( key ) );
    }

    return request;
  }

}
