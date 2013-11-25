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

package pt.webdetails.cpk;

import java.util.List;

import pt.webdetails.cpf.PentahoPluginEnvironment;
import pt.webdetails.cpf.plugins.Plugin;
import pt.webdetails.cpf.plugins.PluginsAnalyzer;
import pt.webdetails.cpf.session.ISessionUtils;
import pt.webdetails.cpf.session.PentahoSessionUtils;
import pt.webdetails.cpf.utils.IPluginUtils;
import pt.webdetails.cpk.security.AccessControl;
import pt.webdetails.cpk.security.IAccessControl;

public class CpkPentahoEnvironment extends PentahoPluginEnvironment implements pt.webdetails.cpk.ICpkEnvironment {

  private IPluginUtils pluginUtils;

  public CpkPentahoEnvironment( IPluginUtils pluginUtils ) {
    this.pluginUtils = pluginUtils;
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

  @Override
  public ISessionUtils getSessionUtils() {
    return new PentahoSessionUtils();
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
