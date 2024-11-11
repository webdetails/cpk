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


package pt.webdetails.cpk;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.pentaho.platform.engine.core.system.PentahoSystem;
import pt.webdetails.cpf.PentahoPluginEnvironment;
import pt.webdetails.cpf.plugins.Plugin;
import pt.webdetails.cpf.plugins.PluginsAnalyzer;
import pt.webdetails.cpf.session.ISessionUtils;
import pt.webdetails.cpf.session.PentahoSessionUtils;
import pt.webdetails.cpf.utils.IPluginUtils;
import pt.webdetails.cpk.security.AccessControl;
import pt.webdetails.cpk.security.IAccessControl;

public class CpkPentahoEnvironment extends PentahoPluginEnvironment implements ICpkEnvironment {

  private IPluginUtils pluginUtils;
  private HashSet<String> reservedWords;


  public CpkPentahoEnvironment() {
    this.pluginUtils = null;
    this.reservedWords = new HashSet<String>();
  }

  public CpkPentahoEnvironment( IPluginUtils pluginUtils, String[] reservedWords ) {
    this.pluginUtils = pluginUtils;
    this.reservedWords = new HashSet<String>( Arrays.asList( reservedWords ) );
  }

  @Override
  public IPluginUtils getPluginUtils() {
    return pluginUtils;
  }

  @Override
  public IAccessControl getAccessControl() {
    return new AccessControl( pluginUtils );
  }

  @Override
  public String getPluginName() {
    return pluginUtils.getPluginName();
  }

  @Override public Set<String> getReservedWords() {
    return Collections.unmodifiableSet( this.reservedWords );
  }

  @Override
  public ISessionUtils getSessionUtils() {
    return new PentahoSessionUtils();
  }

  @Override public String getWebAppDir() {
    return PentahoSystem.getApplicationContext().getApplicationPath( "" );
  }

  @Override
  public void reload() {
    PluginsAnalyzer pluginsAnalyzer = new PluginsAnalyzer();
    pluginsAnalyzer.refresh();
    List<Plugin> plugins = pluginsAnalyzer.getInstalledPlugins();
    String pluginName = pluginUtils.getPluginName();
    for ( Plugin plgn : plugins ) {
      if ( plgn.getName().equalsIgnoreCase( pluginName ) || plgn.getId().equalsIgnoreCase( pluginName ) ) {
        plgn.setName( pluginName );
        break;
      }
    }
  }
}
