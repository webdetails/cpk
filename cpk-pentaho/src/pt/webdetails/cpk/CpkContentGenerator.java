/*!
* Copyright 2002 - 2017 Webdetails, a Hitachi Vantara company.  All rights reserved.
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

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.dom4j.DocumentException;
import org.pentaho.platform.api.engine.IParameterProvider;

import pt.webdetails.cpf.RestContentGenerator;
import pt.webdetails.cpf.RestRequestHandler;
import pt.webdetails.cpf.Router;
import pt.webdetails.cpf.WrapperUtils;
import pt.webdetails.cpf.annotations.AccessLevel;
import pt.webdetails.cpf.annotations.Exposed;
import pt.webdetails.cpf.http.ICommonParameterProvider;
import pt.webdetails.cpf.plugins.IPluginFilter;
import pt.webdetails.cpf.plugins.Plugin;
import pt.webdetails.cpf.plugins.PluginsAnalyzer;
import pt.webdetails.cpf.utils.PluginUtils;
import pt.webdetails.cpk.datasources.CpkDataSourceMetadata;
import pt.webdetails.cpk.datasources.DataSource;
import pt.webdetails.cpk.datasources.DataSourceDefinition;
import pt.webdetails.cpk.datasources.DataSourceMetadata;
import pt.webdetails.cpk.elements.IElement;
import pt.webdetails.cpk.elements.impl.KettleJobElement;
import pt.webdetails.cpk.elements.impl.KettleTransformationElement;
import pt.webdetails.cpk.sitemap.LinkGenerator;

import javax.servlet.http.HttpServletResponse;

public class CpkContentGenerator extends RestContentGenerator {

  private static final long serialVersionUID = 1L;
  protected CpkCoreService coreService;
  protected ICpkEnvironment cpkEnv;

  public static final String[] reservedWords = { "default", "refresh", "status", "reload", "getElementsList",
            "getSitemapJson", "version", "getPluginMetadata" };


  public CpkContentGenerator() {
    init();
  }

  protected void init() {
      this.pluginUtils = new PluginUtils();
      this.cpkEnv = new CpkPentahoEnvironment( this.pluginUtils, reservedWords );
      this.coreService = new CpkCoreService( this.cpkEnv );
  }

  @Override
  public void createContent() throws Exception {
    wrapParams();
    try {
      coreService.createContent( buildBloatedMap() );
    } catch ( Exception e ) {
      super.createContent();
    }
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC )
  public void reload( OutputStream out ) throws DocumentException, IOException {
    coreService.reload( out, buildBloatedMap() );
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC )
  public void refresh( OutputStream out ) throws DocumentException, IOException {
    coreService.refresh( out, buildBloatedMap() );
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC )
  public void version( OutputStream out ) {

    PluginsAnalyzer pluginsAnalyzer = new PluginsAnalyzer();
    pluginsAnalyzer.refresh();

    String version = null;

    IPluginFilter thisPlugin = new IPluginFilter() {
      @Override
      public boolean include( Plugin plugin ) {
        return plugin.getId().equalsIgnoreCase(pluginUtils.getPluginName());
      }
    };

    List<Plugin> plugins = pluginsAnalyzer.getPlugins( thisPlugin );


    version = plugins.get( 0 ).getVersion().toString();
    writeMessage( out, version );
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC )
  public void status( OutputStream out ) throws DocumentException, IOException {
    if ( map.get( "request" ).hasParameter( "json" ) ) {
      coreService.statusJson( out, getHttpResponse() );
    } else {
      coreService.status( out, buildBloatedMap() );
    }
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC )
  public void getPluginMetadata( OutputStream out ) {
    ObjectMapper mapper = new ObjectMapper();
    String json = null;
    IPluginFilter pluginFilter = new IPluginFilter() {

      @Override
      public boolean include( Plugin plugin ) {
        return plugin.getId().equals(pluginUtils.getPluginName());
      }
    };

    PluginsAnalyzer pluginsAnalyzer = new PluginsAnalyzer();
    pluginsAnalyzer.refresh();

    List<Plugin> plugins = pluginsAnalyzer.getPlugins( pluginFilter );

    Plugin plugin = null;

    if ( !plugins.isEmpty() ) {
      plugin = plugins.get( 0 );

      try {
        json = mapper.writeValueAsString( plugin );
      } catch ( IOException ex ) {
        Logger.getLogger( CpkContentGenerator.class.getName() ).log( Level.SEVERE, null, ex );
      }
    }

    if ( json == null ) {
      json = "{\"error\":\"There was a problem getting the plugin metadata into JSON. The result was 'null'\"}";
    }

    writeMessage( out, json );
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC )
  public void getSitemapJson( OutputStream out ) throws IOException {

    Map<String, IElement> elementsMap = pt.webdetails.cpk.CpkEngine.getInstance().getElementsMap();
    JsonNode sitemap = null;
    if ( elementsMap != null ) {
      LinkGenerator linkGen = new LinkGenerator( elementsMap, pluginUtils );
      sitemap = linkGen.getLinksJson();
    }
    ObjectMapper mapper = new ObjectMapper();
    mapper.writeValue( out, sitemap );
  }

  @Exposed( accessLevel = AccessLevel.PUBLIC )
  public void elementsList( OutputStream out ) {
    coreService.getElementsList( out, buildBloatedMap() );
  }

  @Override
  public String getPluginName() {

    return pluginUtils.getPluginName();
  }

  private void writeMessage( OutputStream out, String message ) {
    try {
      out.write( message.getBytes( ENCODING ) );
    } catch ( IOException ex ) {
      Logger.getLogger( CpkContentGenerator.class.getName() ).log( Level.SEVERE, null, ex );
    }
  }

  @Override
  public RestRequestHandler getRequestHandler() {
    return Router.getBaseRouter();
  }

  private void wrapParams() {
    if ( parameterProviders != null ) {
      Iterator it = parameterProviders.entrySet().iterator();
      map = new HashMap<String, ICommonParameterProvider>();
      while ( it.hasNext() ) {
        @SuppressWarnings( "unchecked" )
        Map.Entry<String, IParameterProvider> e = (Map.Entry<String, IParameterProvider>) it.next();
        map.put( e.getKey(), WrapperUtils.wrapParamProvider( e.getValue() ) );
      }
    }
  }


  /*
  // New Jackson API (version 2.x)
  static final com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();

  static {
    mapper.setSerializationInclusion( com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL );
    mapper.setSerializationInclusion( com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY );
  }
  */

  static final ObjectMapper mapper = new ObjectMapper();

  @Exposed( accessLevel = AccessLevel.PUBLIC, outputType = MimeType.JSON )
  public void listDataAccessTypes( final OutputStream out ) throws Exception {
    //boolean refreshCache = Boolean.parseBoolean(getRequestParameters().getStringParameter("refreshCache", "false"));

    Set<DataSource> dataSources = new LinkedHashSet<DataSource>();
    StringBuilder dsDeclarations = new StringBuilder( "{" );
    Collection<IElement> endpoints = coreService.getElements();

    if ( endpoints != null ) {
      for ( IElement endpoint : endpoints ) {

        // filter endpoints that aren't of kettle type
        if ( !( endpoint instanceof KettleJobElement || endpoint instanceof KettleTransformationElement ) ) {
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
    out.write( dsDeclarations.toString().getBytes() );
  }

  private Map<String, Map<String, Object>> buildBloatedMap() {

    Map<String, Map<String, Object>> mainMap = new HashMap<String, Map<String, Object>>();
    Map<String, Object> pathMap;

    pathMap = getPathMap();


    mainMap.put( "request", getRequestMap() );
    mainMap.put( "path", pathMap );

    return mainMap;
  }

  private Map<String, Object> getPathMap() {
    Map<String, Object> pathMap = map.get( "path" ).getParameters();

    try {
      pathMap.put( "httpresponse", getHttpResponse() );
      pathMap.put( "httprequest", map.get( "path" ).getParameter( "httprequest" ) );
    } catch ( NullPointerException e ) {
      return pathMap;
    }
    return pathMap;
  }

  private Map<String, Object> getRequestMap() {
    return map.get( "request" ).getParameters();
  }

  private HttpServletResponse getHttpResponse() {
    return (HttpServletResponse) map.get( "path" ).getParameters().get( "httpresponse" );
  }
}
