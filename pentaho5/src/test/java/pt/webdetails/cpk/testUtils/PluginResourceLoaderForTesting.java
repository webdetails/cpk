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

import java.io.File;

import org.pentaho.platform.plugin.services.pluginmgr.PluginClassLoader;
import org.pentaho.platform.plugin.services.pluginmgr.PluginResourceLoader;

public class PluginResourceLoaderForTesting extends PluginResourceLoader {


  public PluginResourceLoaderForTesting() {
    super();
  }

  @Override
  protected PluginClassLoader getOverrideClassloader() {
    File path = new File( System.getProperty( "user.dir" )
      + "/test-resources/repository/system/cpkSol/" );//must add the "/" at the end because it is a dir
    return new PluginClassLoader( path, null );
  }

}
