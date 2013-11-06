package pt.webdetails.cpk;

/**
 * Created with IntelliJ IDEA. User: joao Date: 10/22/13 Time: 2:08 PM To change this template use File | Settings |
 * File Templates.
 */

import pt.webdetails.cpf.PentahoLegacyInterPluginCall;
import pt.webdetails.cpf.plugin.CorePlugin;

import javax.servlet.ServletResponse;
import java.io.OutputStream;
import java.util.Map;


public class InterPluginBroker {


  public static void run( Map<String, Object> params, ServletResponse response, OutputStream out ) {
    PentahoLegacyInterPluginCall pluginCall = new PentahoLegacyInterPluginCall();
    pluginCall.init( CorePlugin.CDE, "Render", params );
    pluginCall.setResponse( response );
    pluginCall.setOutputStream( out );
    pluginCall.run();
  }

}
