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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPluginLifecycleListener;
import org.pentaho.platform.api.engine.PluginLifecycleException;
import pt.webdetails.cpf.PentahoPluginEnvironment;
import pt.webdetails.cpf.PluginEnvironment;
import pt.webdetails.cpf.SimpleLifeCycleListener;

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
