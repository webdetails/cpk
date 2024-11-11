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


package pt.webdetails.cpk.testUtils;

import org.dom4j.DocumentException;
import pt.webdetails.cpf.utils.PluginUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;


public class PluginUtilsForTesting extends PluginUtils {

  @Override
  public void initialize() throws IOException, DocumentException {

    // We need to get the plugin name
        /*
         * Verify if the index 0 is actually the file we want!
         */

    String url = "file://" + System.getProperty( "user.dir" ) + "/test-resources/repository/system/cpkSol/plugin.xml";
    //String url = "file://" + System.getProperty( "user.dir" ) + "/test-resources/cpkSol/plugin.xml";

    URL pluginUrl = new URL( url );
    //URL pluginUrl = new File("plugin.xml").toURI().toURL();

    // Parent file holds the name
    File pluginDir = new File( pluginUrl.getFile() ).getParentFile();
    setPluginName( pluginDir.getName() );
    setPluginDirectory( pluginDir );

    logger.debug( "Found resource? " + "?" );

  }
}

