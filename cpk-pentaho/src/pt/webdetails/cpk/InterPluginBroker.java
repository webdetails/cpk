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

import org.apache.commons.io.IOUtils;
import pt.webdetails.cpf.PentahoLegacyInterPluginCall;
import pt.webdetails.cpf.plugin.CorePlugin;

import javax.servlet.ServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public class InterPluginBroker {


  public static void run( Map<String, Object> params, ServletResponse response, OutputStream out ) throws IOException {
    PentahoLegacyInterPluginCall pluginCall = new PentahoLegacyInterPluginCall();
    pluginCall.init( CorePlugin.CDE, "Render", params );
    pluginCall.setResponse( response );
    pluginCall.setOutputStream( out );
    String returnVal = pluginCall.call();
    IOUtils.write( returnVal, out );
    out.flush();
  }

}
