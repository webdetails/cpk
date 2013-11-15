/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cpk;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPluginLifecycleListener;
import org.pentaho.platform.api.engine.PluginLifecycleException;
import pt.webdetails.cpf.PentahoPluginEnvironment;
import pt.webdetails.cpf.PluginEnvironment;
import pt.webdetails.cpf.SimpleLifeCycleListener;

/**
 * @author pedro
 */
public class CpkLifecycleListener extends SimpleLifeCycleListener implements IPluginLifecycleListener {

    static Log logger = LogFactory.getLog(CpkLifecycleListener.class);

    @Override
    public void init() throws PluginLifecycleException {
         //super.init();
        logger.info("Initializing CPK plugin");

    }

    //@Override
    public void loaded() throws PluginLifecycleException {
       //super.init();
        logger.info("CPK plugin loaded");

    }

    //@Override
    public void unLoaded() throws PluginLifecycleException {
        logger.info("CPK plugin unloaded");

    }

  @Override public PluginEnvironment getEnvironment() {
    return PentahoPluginEnvironment.getInstance();  //To change body of implemented methods use File | Settings | File Templates.
  }
}
