/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
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

/**
 * @author joao
 */
public class CpkPentahoEnvironment extends PentahoPluginEnvironment implements pt.webdetails.cpk.ICpkEnvironment {

    private IPluginUtils pluginUtils;

    public CpkPentahoEnvironment(IPluginUtils pluginUtils) {
        this.pluginUtils = pluginUtils;
    }

    @Override
    public IPluginUtils getPluginUtils() {
        return pluginUtils;
    }

    @Override
    public IAccessControl getAccessControl() {
        return new AccessControl(pluginUtils);
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
        for (Plugin plgn : plugins) {
            if (plgn.getName().equalsIgnoreCase(pluginName) || plgn.getId().equalsIgnoreCase(pluginName)) {
                plgn.setName(pluginName);
                break;
            }
        }
    }
}
