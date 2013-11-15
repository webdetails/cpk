package pt.webdetails.cpk;

/**
 * Created with IntelliJ IDEA. User: joao Date: 10/22/13 Time: 2:08 PM To change this template use File | Settings |
 * File Templates.
 */

import org.apache.commons.io.IOUtils;
import org.pentaho.platform.api.engine.IContentGenerator;
import pt.webdetails.cpf.PluginEnvironment;
import pt.webdetails.cpf.plugin.CorePlugin;
import pt.webdetails.cpf.plugincall.api.IPluginCall;
import pt.webdetails.cpf.plugincall.base.CallParameters;

import javax.servlet.ServletResponse;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;


public class InterPluginBroker {

  public static void run( Map<String, Object> params, OutputStream out ) throws Exception {
    CallParameters parameters = new CallParameters();
    Iterator<String> it = params.keySet().iterator();
    while ( it.hasNext() ) {
      String key = it.next();
      Object value = params.get( key );
      parameters.put(key, value.toString());
    }
    IPluginCall pluginCall = PluginEnvironment.env().getPluginCall( CorePlugin.CDE.getId(), "renderer", "render" );
    String returnVal = pluginCall.call( parameters.getParameters() );
    IOUtils.write( returnVal, out );
    out.flush();
  }

}
