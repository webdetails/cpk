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

import java.text.SimpleDateFormat;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;


import com.sun.jersey.api.representation.Form;
import com.sun.jersey.spi.container.ContainerRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.dom4j.DocumentException;

import org.pentaho.platform.engine.core.system.PentahoSystem;
import pt.webdetails.cpf.plugins.IPluginFilter;
import pt.webdetails.cpf.plugins.Plugin;
import pt.webdetails.cpf.plugins.PluginsAnalyzer;
import pt.webdetails.cpf.utils.CharsetHelper;
import pt.webdetails.cpf.utils.MimeTypes;
import pt.webdetails.cpf.utils.PluginUtils;
import pt.webdetails.cpk.datasources.CpkDataSourceMetadata;
import pt.webdetails.cpk.datasources.DataSource;
import pt.webdetails.cpk.datasources.DataSourceDefinition;
import pt.webdetails.cpk.datasources.DataSourceMetadata;
import pt.webdetails.cpk.elements.IElement;
import pt.webdetails.cpk.elements.impl.KettleElementType;
import pt.webdetails.cpk.sitemap.LinkGenerator;
import org.apache.commons.io.IOUtils;


@Path( "/{pluginId}/api" )
public class CpkApi {

  private static final Log logger = LogFactory.getLog( CpkApi.class );
  private static final SimpleDateFormat format = new SimpleDateFormat( "yyyy-MM-dd HH:mm" );
  private static final String PARAM_WEBAPP_DIR = "paramcpk.webapp.dir";

  public static final String PLUGIN_NAME = "sparkl";

  protected CpkCoreService coreService;
  protected pt.webdetails.cpk.ICpkEnvironment cpkEnv;
  protected PluginUtils pluginUtils;


  public CpkApi() {
    this.pluginUtils = new PluginUtils();
    this.cpkEnv = new CpkPentahoEnvironment( pluginUtils, null );
    this.coreService = new pt.webdetails.cpk.CpkCoreService( cpkEnv );
    init();
  }


  protected void init() {
    pluginUtils.setPluginName( ( (CpkPentahoEnvironment) cpkEnv ).getPluginId() );
  }


  protected static String getEncoding() {
    return CharsetHelper.getEncoding();
  }

  @GET
  @Path( "/ping" )
  public String ping() {
    return "Pong: I was called from " + getPluginName();
  }

  @GET
  @Path( "/{param}" )
  public void genericEndpointGet( @PathParam( "param" ) String param, @Context HttpServletRequest request,
                                  @Context HttpServletResponse response, @Context HttpHeaders headers )
    throws Exception {
    callEndpoint( param, request, response, headers );
  }

  @POST
  @Path( "/{param}" )
  public void genericEndpointPost( @PathParam( "param" ) String param, @Context HttpServletRequest request,
                                   @Context HttpServletResponse response, @Context HttpHeaders headers )
    throws Exception {
    callEndpoint( param, request, response, headers );
  }


  @GET
  @Path( "/createContent/{param}" )
  @Produces( MimeTypes.JSON )
  public void createContentGet( @PathParam( "param" ) String param, @Context HttpServletRequest request,
                                @Context HttpServletResponse response,
                                @Context HttpHeaders headers ) throws Exception {
    callEndpoint( param, request, response, headers );
  }

  @GET
  @Path( "/reload" )
  public void reload( @Context HttpServletRequest request, @Context HttpServletResponse response,
                      @Context HttpHeaders headers )
    throws DocumentException, IOException {
    coreService.reload( response.getOutputStream(), buildBloatedMap( request, response, headers ) );
  }

  @GET
  @Path( "/refresh" )
  @Produces( MimeTypes.PLAIN_TEXT )
  public void refreshGet( @Context HttpServletRequest request, @Context HttpServletResponse response,
                          @Context HttpHeaders headers )
    throws DocumentException, IOException {
    refresh( request, response, headers );
  }

  @POST
  @Path( "/refresh" )
  @Produces( MimeTypes.PLAIN_TEXT )
  public void refreshPost( @Context HttpServletRequest request, @Context HttpServletResponse response,
                           @Context HttpHeaders headers )
    throws DocumentException, IOException {
    refresh( request, response, headers );
  }

  private void refresh( HttpServletRequest request, HttpServletResponse response, HttpHeaders headers )
    throws IOException, DocumentException {
    coreService.refresh( response.getOutputStream(), buildBloatedMap( request, response, headers ) );
    response.getOutputStream().flush();
  }

  @GET
  @Path( "/version" )
  @Produces(MimeTypes.PLAIN_TEXT)
  public void version( @PathParam( "pluginId" ) String pluginId, @Context HttpServletResponse response )
    throws IOException {
    PluginsAnalyzer pluginsAnalyzer = new PluginsAnalyzer();
    pluginsAnalyzer.refresh();

    String version = null;

    IPluginFilter thisPlugin = new IPluginFilter() {
      @Override
      public boolean include( Plugin plugin ) {
        return plugin.getId().equalsIgnoreCase( pluginUtils.getPluginName() );
      }
    };

    List<Plugin> plugins = pluginsAnalyzer.getPlugins( thisPlugin );


    version = plugins.get( 0 ).getVersion().toString();
    writeMessage( response.getOutputStream(), version );
  }

  @GET
  @Path( "/status" )
  public void status( @Context HttpServletRequest request, @Context HttpServletResponse response,
                      @Context HttpHeaders headers )
    throws DocumentException, IOException {
    if (request.getParameter( "json" ) != null) {
      coreService.statusJson( response.getOutputStream(), response );
    } else {
      coreService.status( response.getOutputStream(), buildBloatedMap( request, response, headers ) );
    }
  }

  @GET
  @Path( "/getSitemapJson" )
  public void getSitemapJson( @Context HttpServletResponse response )
    throws IOException {
    TreeMap<String, IElement> elementsMap = CpkEngine.getInstance().getElementsMap();
    JsonNode sitemap = null;
    if ( elementsMap != null ) {
      LinkGenerator linkGen = new LinkGenerator( elementsMap, pluginUtils );
      sitemap = linkGen.getLinksJson();
    }
    ObjectMapper mapper = new ObjectMapper();
    mapper.writeValue( response.getOutputStream(), sitemap );
  }

  @GET
  @Path( "/elementsList" )
  public void elementsList( @Context HttpServletRequest request, @Context HttpServletResponse response,
                            @Context HttpHeaders headers )
    throws IOException {
    coreService.getElementsList( response.getOutputStream(), buildBloatedMap( request, response, headers ) );
  }


  public String getPluginName() {

    return pluginUtils.getPluginName();
  }

  private void writeMessage( OutputStream out, String message ) {
    try {
      out.write( message.getBytes( getEncoding() ) );
      out.flush();
    } catch ( IOException ex ) {
      Logger.getLogger( CpkApi.class.getName() ).log( Level.SEVERE, null, ex );
    }
  }

  // New Jackson API (version 2.x)
  static final com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();

  static {
    mapper.setSerializationInclusion( com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL );
    mapper.setSerializationInclusion( com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY );
  }

  @GET
  @Path( "/listDataAccessTypes" )
  @Produces( MimeTypes.JSON )
  public void listDataAccessTypes( @Context HttpServletResponse response )
    throws Exception {
    //boolean refreshCache = Boolean.parseBoolean(getRequestParameters().getStringParameter("refreshCache", "false"));

    Set<DataSource> dataSources = new LinkedHashSet<DataSource>();
    StringBuilder dsDeclarations = new StringBuilder( "{" );
    IElement[] endpoints = coreService.getElements();

    if ( endpoints != null ) {
      for ( IElement endpoint : endpoints ) {

        // filter endpoints that aren't of kettle type
        if ( !( endpoint instanceof KettleElementType || endpoint.getElementType().equalsIgnoreCase( "kettle" ) ) ) {
          continue;
        }

        logger.info( String.format( "CPK Kettle Endpoint found: %s)", endpoint ) );


        String pluginId = pluginUtils.getPluginName();
        String endpointName = endpoint.getName();

        //We need to make sure pluginId is safe - starts with a char and is only alphaNumeric
        StringBuilder sb = new StringBuilder();
        for ( int i = 0; i < pluginId.length(); i++ ) {
          char c = pluginId.charAt( i );
          if ( ( Character.isJavaIdentifierStart( c ) && i == 0 ) ||
            ( Character.isJavaIdentifierPart( c ) && i > 0 ) ) {
            sb.append( c );
          }
        }
        String safePluginId = sb.toString();


        DataSourceMetadata metadata = new CpkDataSourceMetadata( pluginId, endpointName );
        DataSourceDefinition definition = new DataSourceDefinition();

        DataSource dataSource = new DataSource().setMetadata( metadata ).setDefinition( definition );
        dataSources.add( dataSource );

        dsDeclarations.append( String.format( "\"%s_%s_CPKENDPOINT\": ", safePluginId, endpointName ) );
        dsDeclarations.append( mapper.writeValueAsString( dataSource ) );
        dsDeclarations.append( "," );
      }
    }

    int index = dsDeclarations.lastIndexOf( "," );
    if ( index > 0 ) {
      dsDeclarations.deleteCharAt( index );
    }
    dsDeclarations.append( "}" );
    IOUtils.write( dsDeclarations.toString(), response.getOutputStream() );
    response.getOutputStream().flush();
  }

  @GET
  @Path( "/reloadPlugins" )
  public void reloadPluginsGet() throws Exception {
    reloadPlugins();
  }

  @POST
  @Path( "/reloadPlugins" )
  public void reloadPluginsPost() throws Exception {
    reloadPlugins();
  }

  public void reloadPlugins() throws Exception {
    //  IPluginManager pluginManager = PentahoSystem.get(IPluginManager.class, PentahoSessionHolder.getSession());
    //  pluginManager.loadNewPlugins();

  }


  private Map<String, Map<String, Object>> buildBloatedMap( HttpServletRequest request, HttpServletResponse response,
                                                            HttpHeaders headers ) {
    Map<String, Map<String, Object>> mainMap = new HashMap<String, Map<String, Object>>();

    mainMap.put( "request", buildRequestMap( request, headers ) );
    mainMap.put( "path" +
      "", buildPathMap( request, response, headers ) );

    return mainMap;

  }

  private Map<String, Object> buildRequestMap( HttpServletRequest request, HttpHeaders headers ) {
    Map<String, Object> requestMap = new HashMap<String, Object>();
    requestMap.put( PARAM_WEBAPP_DIR, PentahoSystem.getApplicationContext().getApplicationPath( "" ) );
    if ( request == null ) {
      return requestMap;
    }
      Enumeration e = request.getParameterNames();
      while ( e.hasMoreElements() ) {
        Object o = e.nextElement();
        requestMap.put( o.toString(), request.getParameter( o.toString() ) );
      }
      Form form =
        ( (ContainerRequest) headers ).getFormParameters();
      Iterator<String> it = form.keySet().iterator();
      while ( it.hasNext() ) {
        String next = it.next();
        requestMap.put( next, form.get( next ).get( 0 ) );
      }
    return requestMap;
  }

  private Map<String, Object> buildPathMap( HttpServletRequest request, HttpServletResponse response,
                                            HttpHeaders headers ) {

    Map<String, Object> pathMap = new HashMap<String, Object>();
    pathMap.put( "httprequest", request );
    pathMap.put( "httpresponse", response );
    if ( headers != null && headers.getRequestHeaders()
      .containsKey( "contentType" ) ) {
      pathMap.put( "contentType", headers.getRequestHeader( "contentType" ) );
    }
    return pathMap;
  }


  private void callEndpoint( String endpoint, HttpServletRequest request, HttpServletResponse response,
                             HttpHeaders headers )
    throws Exception {
    Map<String, Map<String, Object>> bloatedMap = buildBloatedMap( request, response, headers );
    bloatedMap.get( "path" ).put( "path", "/" + endpoint );
    coreService.createContent( bloatedMap );
  }

  public void createContent( Map<String, Map<String, Object>> bloatedMap ) throws Exception {
    coreService.createContent( bloatedMap );
  }

  private void setPluginName( String pluginId){
    pluginUtils.setPluginName( pluginId );
  }
}
