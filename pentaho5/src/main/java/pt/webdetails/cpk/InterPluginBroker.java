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

import org.pentaho.platform.engine.core.system.PentahoSystem;
import pt.webdetails.cpf.InterPluginCall;
import pt.webdetails.cpf.PluginEnvironment;
import pt.webdetails.cpf.plugin.CorePlugin;
import pt.webdetails.cpf.plugincall.api.IPluginCall;
import pt.webdetails.cpf.plugincall.base.CallParameters;
import pt.webdetails.cpf.utils.PluginIOUtils;

import java.io.OutputStream;
import java.util.Map;

public class InterPluginBroker {

  private static final String CDE_RENDER_API_BEAN_ID_TAG = "cde-render-api-bean-id";

  private static final String CDE_RENDER_API_BEAN_ID = "renderApi";
  private static final String CDE_RENDER_API_LEGACY_BEAN_ID = "renderer";

  private static final String CDE_RENDER_API_RENDER_METHOD_TAG = "cde-render-api-render-method";
  private static final String CDE_RENDER_API_RENDER_METHOD = "render";

  private static IPluginCall cdeRenderApiCall;

  public static void run( Map<String, Object> params, OutputStream out ) throws Exception {
    CallParameters parameters = new CallParameters();

    for ( String key : params.keySet() ) {
      String value = params.get( key ).toString();

      parameters.put( key, value );
    }

    IPluginCall pluginCall = getPluginCall();

    String response = pluginCall != null
      ? pluginCall.call( parameters.getParameters() )
      : "No valid InterPluginCall available";

    PluginIOUtils.writeOutAndFlush( out, response );
  }

  private static IPluginCall getPluginCall() {
    if ( cdeRenderApiCall != null ) {
      return cdeRenderApiCall;
    }

    IPluginCall cdePluginCall;
    final PluginEnvironment environment = PluginEnvironment.env();
    final String pluginID = CorePlugin.CDE.getId();

    // 1. try configured values
    String beanId = PentahoSystem.getSystemSetting( CDE_RENDER_API_BEAN_ID_TAG, CDE_RENDER_API_BEAN_ID );
    String method = PentahoSystem.getSystemSetting( CDE_RENDER_API_RENDER_METHOD_TAG, CDE_RENDER_API_RENDER_METHOD );

    cdePluginCall = environment.getPluginCall( pluginID, beanId, method );
    if ( beanExists( cdePluginCall ) ) {
      return ( cdeRenderApiCall = cdePluginCall );
    }

    // 2. fallback to latest cde render bean id
    cdePluginCall = environment.getPluginCall( pluginID, CDE_RENDER_API_BEAN_ID, CDE_RENDER_API_RENDER_METHOD );
    if ( beanExists( cdePluginCall ) ) {
      return ( cdeRenderApiCall = cdePluginCall );
    }

    // 3. fallback to legacy cde render bean id
    cdePluginCall = environment.getPluginCall( pluginID, CDE_RENDER_API_LEGACY_BEAN_ID, CDE_RENDER_API_RENDER_METHOD );
    if ( beanExists( cdePluginCall ) ) {
      return ( cdeRenderApiCall = cdePluginCall );
    }

    return null;
  }

  private static boolean beanExists( IPluginCall pluginCall ) {
    return ( (InterPluginCall) pluginCall).beanExists();
  }

}
