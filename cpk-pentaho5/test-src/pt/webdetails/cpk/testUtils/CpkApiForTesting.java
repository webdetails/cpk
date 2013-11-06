package pt.webdetails.cpk.testUtils;

import org.dom4j.DocumentException;
import org.pentaho.platform.api.engine.IParameterProvider;
import pt.webdetails.cpf.WrapperUtils;
import pt.webdetails.cpf.http.ICommonParameterProvider;
import pt.webdetails.cpf.utils.PluginUtils;
import pt.webdetails.cpk.CpkApi;
import pt.webdetails.cpk.CpkCoreService;
import pt.webdetails.cpk.CpkPentahoEnvironment;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created with IntelliJ IDEA. User: joao Date: 11/1/13 Time: 10:54 AM To change this template use File | Settings |
 * File Templates.
 */
public class CpkApiForTesting extends CpkApi{


    /*public CpkContentGeneratorForTesting(ICpkEnvironment environment) {
        super(environment);
    }//*/

  public CpkApiForTesting() throws Exception {
    super();
    this.pluginUtils = new PluginUtilsForTesting();
    this.cpkEnv = new CpkPentahoEnvironment( pluginUtils, null );
    this.coreService = new CpkCoreService( cpkEnv );
  }

  class PluginUtilsForTesting extends PluginUtils {//XXX - confirm if this isnt actually bypassing a problem -

    @Override
    public void initialize() throws IOException, DocumentException {

      // We need to get the plugin name
        /*
         * Verify if the index 0 is actually the file we want!
         */
      String url = "file://" + System.getProperty( "user.dir" ) + "/test-resources/repository/system/cpkSol/plugin.xml";

      URL pluginUrl = new URL( url );
      //URL pluginUrl = new File("plugin.xml").toURI().toURL();

      // Parent file holds the name
      File pluginDir = new File( pluginUrl.getFile() ).getParentFile();
      setPluginName( pluginDir.getName() );
      setPluginDirectory( pluginDir );

      logger.debug( "Found resource? " + "?" );

    }
  }

}
