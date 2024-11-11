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

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.spi.container.ContainerRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pt.webdetails.cpf.plugins.IPluginFilter;
import pt.webdetails.cpf.plugins.Plugin;
import pt.webdetails.cpf.plugins.PluginsAnalyzer;
import pt.webdetails.cpf.utils.CharsetHelper;
import pt.webdetails.cpf.utils.MimeTypes;
import pt.webdetails.cpf.utils.PluginUtils;
import pt.webdetails.cpk.datasources.DataSource;
import pt.webdetails.cpk.elements.IDataSourceProvider;
import pt.webdetails.cpk.elements.IElement;
import pt.webdetails.cpk.sitemap.LinkGenerator;
import pt.webdetails.cpk.utils.CorsUtil;
import pt.webdetails.cpk.utils.CpkUtils;

@Path( "/{pluginId}/api" )
public class CpkApi {

  private static final Log logger = LogFactory.getLog( CpkApi.class );

  protected static final String API_ROOT = "/api/";
  private static final String CONTENT_TYPE_KEY = "contentType";
  private static final String PATH_SEPARATOR = "/";

  protected static final String DEFAULT_NO_DASHBOARD_MESSAGE = "This plugin does not contain a dashboard";

  protected static final String[] reservedWords = { "ping", "default", "reload", "refresh", "version", "status",
    "getSitemapJson", "elementsList", "listDataAccessTypes", "reloadPlugins" };

  protected static final String PING_RESPONSE = "Pong: I was called from ";
  protected static final String CPK_ENDPOINT_KEY = "CPKENDPOINT";
  protected static final String CPK_ENDPOINT_SEPARATOR = "_";

  protected CpkCoreService coreService;
  protected ICpkEnvironment cpkEnv;
  protected PluginsAnalyzer pluginsAnalyzer = null;


  public CpkApi() {
    init();
  }

  protected void init() {
    PluginUtils pluginUtils = new PluginUtils();

    this.cpkEnv = new CpkPentahoEnvironment( pluginUtils, reservedWords );
    this.coreService = new CpkCoreService( this.cpkEnv );
  }

  protected static String getEncoding() {
    return CharsetHelper.getEncoding();
  }

  @GET
  @Path( "/{resource: .+properties}" )
  public Response localizationResourceGet( @PathParam( "pluginId" ) String pluginId,
                                           @PathParam( "resource" ) String resource,
                                           @Context HttpServletRequest request ) {
    try {
      final String content = this.getLocalizationResource( pluginId, resource, request );

      return Response.ok( content ).build();
    } catch ( Exception ioe ) {
      return Response.status( Response.Status.NOT_FOUND ).build();
    }
  }

  @POST
  @Path( "/{resource: .+properties}" )
  public Response localizationResourcePost( @PathParam( "pluginId" ) String pluginId,
                                            @PathParam( "resource" ) String resource,
                                            @Context HttpServletRequest request ) {
    try {
      final String content = this.getLocalizationResource( pluginId, resource, request );

      return Response.ok( content ).build();
    } catch ( Exception e ) {
      return Response.status( Response.Status.NOT_FOUND ).build();
    }
  }

  private String getLocalizationResource( String pluginId, String resource,
                                          HttpServletRequest request ) throws Exception {
    final String elementId = getElementIdFromRequest( pluginId, request );

    return coreService.getLocalizationResourceContent( elementId, resource );
  }


  private String getElementIdFromRequest( String pluginId, HttpServletRequest request ) {
    final String referer = request.getHeader( "referer" );
    final String apiRoot = pluginId + API_ROOT;

    int apiRootIndex = referer.lastIndexOf( apiRoot ) + apiRoot.length();
    int queryStringIndex = referer.indexOf( '?' );

    if ( queryStringIndex < 0 ) {
      return referer.substring( apiRootIndex );
    }

    return referer.substring( apiRootIndex, queryStringIndex );
  }

  @GET
  @Path( "/{param}" )
  public Response genericEndpointGet( @PathParam( "param" ) String param,
                                  @Context HttpServletRequest request,
                                  @Context HttpServletResponse response,
                                  @Context HttpHeaders headers ) throws Exception {
    return getResponseFromCallingEndpoint( param, request, response, headers );
  }

  @POST
  @Path( "/{param}" )
  public Response genericEndpointPost( @PathParam( "param" ) String param,
                                   @Context HttpServletRequest request,
                                   @Context HttpServletResponse response,
                                   @Context HttpHeaders headers ) throws Exception {
    return getResponseFromCallingEndpoint( param, request, response, headers );
  }

  private Response getResponseFromCallingEndpoint( String endpoint, HttpServletRequest request, HttpServletResponse response,
                                                  HttpHeaders headers ) throws Exception {
    callEndpoint( endpoint, request, response, headers );
    return Response.status( CpkUtils.getEquivalentStatusFromHttpServletResponse( response ) ).build();
  }

  private void callEndpoint( String endpoint, HttpServletRequest request, HttpServletResponse response,
                             HttpHeaders headers ) throws Exception {
    setCorsHeaders( request, response );

    final Map<String, Map<String, Object>> bloatedMap = buildBloatedMap( request, response, headers );

    final String path = endpoint != null && !"null".equals( endpoint ) ? PATH_SEPARATOR + endpoint : null;
    bloatedMap.get( "path" ).put( "path", path );

    try {
      coreService.createContent( bloatedMap );
    } catch ( Exception e ) {
      response.setStatus( HttpServletResponse.SC_NOT_FOUND );
    }

    // make sure that everything written in the output stream is sent to the client
    response.getOutputStream().flush();
  }

  @GET
  @Path( "/ping" )
  public String ping() {
    return PING_RESPONSE + getPluginName();
  }

  @GET
  @Path( "/default" )
  public Response defaultElement() throws IOException {
    IElement defaultElement = coreService.getDefaultElement();
    if ( defaultElement != null ) {
      try {
        String url = defaultElement.getPluginId() + API_ROOT + defaultElement.getId();
        return CpkUtils.redirect( url );
      } catch ( URISyntaxException ex ) {
        logger.error( ex );
        return Response.serverError().build();
      }
    } else {
      return Response.ok( DEFAULT_NO_DASHBOARD_MESSAGE.getBytes( getEncoding() ) ).build();
    }
  }

  @GET
  @Path( "/reload" )
  @Produces( MimeTypes.PLAIN_TEXT )
  public Response reload( @Context HttpServletRequest request,
                      @Context HttpServletResponse response,
                      @Context HttpHeaders headers ) {
    String result = coreService.reload( buildBloatedMap( request, response, headers ) );
    return Response.status( CpkUtils.getEquivalentStatusFromHttpServletResponse( response ) )
      .entity( result )
      .build();
  }

  @GET
  @Path( "/refresh" )
  @Produces( MimeTypes.PLAIN_TEXT )
  public Response refreshGet( @Context HttpServletRequest request,
                          @Context HttpServletResponse response,
                          @Context HttpHeaders headers ) {
    return getRefreshResponse( request, response, headers );
  }

  @POST
  @Path( "/refresh" )
  @Produces( MimeTypes.PLAIN_TEXT )
  public Response refreshPost( @Context HttpServletRequest request,
                           @Context HttpServletResponse response,
                           @Context HttpHeaders headers ) {
    return getRefreshResponse( request, response, headers );
  }

  private Response getRefreshResponse( HttpServletRequest request, HttpServletResponse response,
                                       HttpHeaders headers ) {
    String result = refresh( request, response, headers );
    return Response.status( CpkUtils.getEquivalentStatusFromHttpServletResponse( response ) )
      .entity( result )
      .build();
  }

  private String refresh( HttpServletRequest request, HttpServletResponse response,
                        HttpHeaders headers ) {

    final Map<String, Map<String, Object>> bloatedMap = buildBloatedMap( request, response, headers );

    return coreService.refresh( bloatedMap );
  }

  private PluginsAnalyzer getPluginsAnalyser() {
    if ( this.pluginsAnalyzer == null ) {
      this.pluginsAnalyzer = new PluginsAnalyzer();
    }
    return this.pluginsAnalyzer;
  }

  @GET
  @Path( "/version" )
  @Produces( MimeTypes.PLAIN_TEXT )
  public Response version( @PathParam( "pluginId" ) String pluginId ) {
    PluginsAnalyzer localPluginsAnalyzer = getPluginsAnalyser();
    localPluginsAnalyzer.refresh();

    IPluginFilter thisPlugin = plugin -> plugin.getId().equalsIgnoreCase( getPluginName() );
    List<Plugin> plugins = localPluginsAnalyzer.getPlugins( thisPlugin );
    String version = plugins.get( 0 ).getVersion();

    return Response.ok( version ).build();
  }

  @GET
  @Path( "/status" )
  @Produces( MimeTypes.PLAIN_TEXT )
  public Response status( @Context HttpServletRequest request,
                      @Context HttpServletResponse response,
                      @Context HttpHeaders headers ) {
    String result;
    if ( request.getParameter( "json" ) != null ) {
      result = coreService.statusJson( response );
    } else {
      result = coreService.status( buildBloatedMap( request, response, headers ) );
    }
    return Response.status( CpkUtils.getEquivalentStatusFromHttpServletResponse( response ) )
      .entity( result )
      .build();
  }

  @GET
  @Path( "/getSitemapJson" )
  @Produces( MediaType.APPLICATION_JSON )
  public Response getSitemapJson() throws IOException {
    Map<String, IElement> elementsMap = coreService.getEngine().getElementsMap();

    JsonNode sitemap = null;

    if ( elementsMap != null ) {
      LinkGenerator linkGen = new LinkGenerator( elementsMap, cpkEnv.getPluginUtils() );
      sitemap = linkGen.getLinksJson();
    }

    ObjectMapper localMapper = new ObjectMapper();

    return Response.ok( localMapper.writeValueAsString(  sitemap ) ).build();
  }

  @GET
  @Path( "/elementsList" )
  @Produces( MediaType.TEXT_HTML )
  public Response elementsList() {
    String elements = coreService.getElementsList();
    return Response.ok( elements ).build();
  }

  @GET
  @Path( "/listDataAccessTypes" )
  @Produces( MediaType.APPLICATION_JSON )
  /**/
  public Response listDataAccessTypes() throws IOException  {
    Set<DataSource> dataSources = new LinkedHashSet<>();
    StringBuilder dsDeclarations = new StringBuilder( "{" );
    Collection<IElement> endpoints = coreService.getElements();

    String pluginId = getPluginName();

    // We need to make sure pluginId is safe - starts with a char and is only alphaNumeric
    String safePluginId = this.sanitizePluginId( pluginId );

    if ( endpoints != null ) {
      ObjectMapper mapper = new ObjectMapper();
      for ( IElement endpoint : endpoints ) {

        // filter endpoints that aren't data sources
        if ( !( endpoint instanceof IDataSourceProvider ) ) {
          continue;
        }

        logger.info( String.format( "DataSource Endpoint found: %s)", endpoint ) );
        IDataSourceProvider dataSourceProvider = (IDataSourceProvider) endpoint;

        String endpointName = endpoint.getName();

        DataSource dataSource = dataSourceProvider.getDataSource();
        dataSource.getMetadata().setPluginId( pluginId );

        dataSources.add( dataSource );

        dsDeclarations.append( String.format( "\"%s%s%s%s%s\": ", safePluginId, CPK_ENDPOINT_SEPARATOR,
          endpointName, CPK_ENDPOINT_SEPARATOR, CPK_ENDPOINT_KEY ) );
        dsDeclarations.append( mapper.writeValueAsString( dataSource ) );
        dsDeclarations.append( "," );
      }
    }

    int index = dsDeclarations.lastIndexOf( "," );
    if ( index > 0 ) {
      dsDeclarations.deleteCharAt( index );
    }

    dsDeclarations.append( "}" );

    return Response.ok( dsDeclarations.toString() ).build();
  }

  protected String getPluginName() {
    return cpkEnv.getPluginName();
  }

  private String sanitizePluginId( String pluginId ) {
    StringBuilder sb = new StringBuilder();
    for ( int i = 0; i < pluginId.length(); i++ ) {
      char c = pluginId.charAt( i );
      if ( ( Character.isJavaIdentifierStart( c ) && i == 0 )
        || ( Character.isJavaIdentifierPart( c ) && i > 0 ) ) {
        sb.append( c );
      }
    }
    return sb.toString();
  }

  @GET
  @Path( "/reloadPlugins" )
  public Response reloadPluginsGet() {
    return reloadPlugins();
  }

  @POST
  @Path( "/reloadPlugins" )
  public Response reloadPluginsPost() {
    return reloadPlugins();
  }

  private Response reloadPlugins() {
    // TODO: ????
    return Response.noContent().build();
  }

  @GET
  @Path( "/clearCache" )
  public Response clearKettleResultsCache() {
    this.coreService.clearKettleResultsCache();
    return Response.noContent().build();
  }

  private Map<String, Map<String, Object>> buildBloatedMap( HttpServletRequest request, HttpServletResponse response,
                                                            HttpHeaders headers ) {
    Map<String, Map<String, Object>> mainMap = new HashMap<>();

    mainMap.put( "request", buildRequestMap( request, headers ) );
    mainMap.put( "path", buildPathMap( request, response, headers ) );

    return mainMap;

  }

  private Map<String, Object> buildRequestMap( HttpServletRequest request, HttpHeaders headers ) {
    Map<String, Object> requestMap = new HashMap<>();
    if ( request == null ) {
      return requestMap;
    }

    Enumeration e = request.getParameterNames();
    if ( e != null ) {
      while ( e.hasMoreElements() ) {
        Object o = e.nextElement();
        requestMap.put( o.toString(), request.getParameter( o.toString() ) );
      }
    }

    if ( headers instanceof ContainerRequest ) {
      Form form = ((ContainerRequest) headers).getFormParameters();
      if ( form != null ) {
        for ( Map.Entry<String, List<String>> entry : form.entrySet() ) {
          requestMap.put( entry.getKey(), entry.getValue().get( 0 ) );
        }
      }
    }

    return requestMap;
  }

  private Map<String, Object> buildPathMap( HttpServletRequest request, HttpServletResponse response,
                                            HttpHeaders headers ) {
    Map<String, Object> pathMap = new HashMap<>();
    pathMap.put( "httprequest", request );
    pathMap.put( "httpresponse", response );

    if ( headers != null
      && headers.getRequestHeaders() != null
      && headers.getRequestHeaders().containsKey( CONTENT_TYPE_KEY ) ) {
      pathMap.put( CONTENT_TYPE_KEY, headers.getRequestHeader( CONTENT_TYPE_KEY ) );
    }

    return pathMap;
  }

  private void setCorsHeaders( HttpServletRequest request, HttpServletResponse response ) {
    CorsUtil.getInstance().setCorsHeaders( request, response );
  }

  public void createContent( Map<String, Map<String, Object>> bloatedMap ) throws Exception {
    coreService.createContent( bloatedMap );
  }
}
