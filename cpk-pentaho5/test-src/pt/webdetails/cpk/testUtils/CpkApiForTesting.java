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

package pt.webdetails.cpk.testUtils;

import org.dom4j.DocumentException;
import pt.webdetails.cpf.repository.pentaho.unified.UnifiedRepositoryAccess;
import pt.webdetails.cpf.utils.IPluginUtils;
import pt.webdetails.cpf.utils.PluginUtils;
import pt.webdetails.cpk.CpkApi;
import pt.webdetails.cpk.CpkCoreService;
import pt.webdetails.cpk.CpkPentahoEnvironment;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class CpkApiForTesting extends CpkApi {


    /*public CpkContentGeneratorForTesting(ICpkEnvironment environment) {
        super(environment);
    }//*/

  public CpkApiForTesting( boolean argh ) {

    this.pluginUtils = new PluginUtilsForTesting();
    this.cpkEnv = new CpkPentahoEnvironmentForTesting( pluginUtils, null );
    this.coreService = new CpkCoreService( cpkEnv );
  }

  class PluginUtilsForTesting extends PluginUtils {

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

  class CpkPentahoEnvironmentForTesting extends CpkPentahoEnvironment {

    public CpkPentahoEnvironmentForTesting( IPluginUtils pluginUtils, UnifiedRepositoryAccess repoAccess ) {
      super( pluginUtils, repoAccess );
    }

    @Override
    public String getPluginId() {
      return "cpkSol";
    }
  }

}
