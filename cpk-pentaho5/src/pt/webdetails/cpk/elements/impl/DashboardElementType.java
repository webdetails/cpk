
/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cpk.elements.impl;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.utils.PluginUtils;
import pt.webdetails.cpk.InterPluginBroker;
import pt.webdetails.cpk.elements.AbstractElementType;
import pt.webdetails.cpk.elements.ElementInfo;
import pt.webdetails.cpk.elements.IElement;


import java.io.*;
import java.util.HashMap;

import pt.webdetails.cpf.utils.IPluginUtils;

/**
 * @author Pedro Alves<pedro.alves@webdetails.pt>
 */
public class DashboardElementType extends AbstractElementType {

  private IPluginUtils pluginUtils;

  public DashboardElementType( IPluginUtils plug ) {
    super( plug );
    pluginUtils = plug;
  }

  @Override
  public String getType() {
    return "Dashboard";
  }

  @Override
  public void processRequest( Map<String, Map<String, Object>> bloatedMap, IElement element ) {
    try {
      // element = (DashboardElement) element;
      callCDE( bloatedMap, element );
    } catch ( Exception ex ) {
      logger.error( "Error while calling CDE: " + Util.getExceptionDescription( ex ) );
    }
  }

  protected void callCDE( Map<String, Map<String, Object>> bloatedMap, IElement element )
    throws UnsupportedEncodingException, IOException {


    String path = pluginUtils.getPluginRelativeDirectory( element.getLocation(), true );

    ServletRequest wrapper = (HttpServletRequest) bloatedMap.get( "path" ).get( "httprequest" );
    HttpServletResponse response = (HttpServletResponse) bloatedMap.get( "path" ).get( "httpresponse" );
    OutputStream out = response.getOutputStream();

    //String root = wrapper.getScheme() + "://" + wrapper.getServerName() + ":" + wrapper.getServerPort();

    Map<String, Object> params = new HashMap<String, Object>();
    Map<String, Object> requestParams = bloatedMap.get( "request" );

    params.put( "solution", "system" );
    params.put( "path", path );
    if ( requestParams.containsKey( "mode" ) && requestParams.get( "mode" ).equals( "preview" ) ) {
      params.put( "file", element.getId() + "_tmp.cdfde" );
    } else {
      params.put( "file", element.getId() + ".wcdf" );
    }
    params.put( "absolute", "false" );
    params.put( "inferScheme", "false" );
    //params.put( "root", root );
    //PluginUtils.copyParametersFromProvider( params, requestParams );
    Iterator<String> it = requestParams.keySet().iterator();
    while ( it.hasNext() ) {
      String name = it.next();
      params.put( name, requestParams.get( name ) );
    }

    if ( requestParams.containsKey( "mode" ) && requestParams.get( "mode" ).equals( "edit" ) ) {
      redirectToCdeEditor( response, params );
      return;
    }
    try {
        InterPluginBroker.run( params, response, out );
    } catch ( Exception e ) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
  }

  private void redirectToCdeEditor( HttpServletResponse response,
                                    Map<String, Object> params ) throws IOException {

    StringBuilder urlBuilder = new StringBuilder();
    urlBuilder.append( "../pentaho-cdf-dd/edit" );
    if ( params.size() > 0 ) {
      urlBuilder.append( "?" );
    }

    List<String> paramArray = new ArrayList<String>();
    for ( String key : params.keySet() ) {
      Object value = params.get( key );
      if ( value instanceof String ) {
        paramArray.add( key + "=" + URLEncoder.encode( (String) value, "utf-8" ) );
      }
    }

    urlBuilder.append( StringUtils.join( paramArray, "&" ) );

    //XXX - copied from pluginUtils
    if (response == null) {
      logger.error("response not found");
      return;
    }
    try {
      response.sendRedirect(urlBuilder.toString());
    } catch (IOException e) {
      logger.error("could not redirect", e);
    }


  }


  @Override
  protected ElementInfo createElementInfo() {
    return new ElementInfo( "text/html" );
  }

  @Override
  public boolean isShowInSitemap() {
    return true;
  }


}
