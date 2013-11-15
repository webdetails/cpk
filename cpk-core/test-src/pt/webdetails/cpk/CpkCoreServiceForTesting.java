/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cpk;

import java.util.logging.Logger;

import pt.webdetails.cpf.repository.IRepositoryAccess;

/**
 *
 * @author joao
 */
public class CpkCoreServiceForTesting extends CpkCoreService {

    private static final long serialVersionUID = 1L;
    private static final String ENCODING = "UTF-8";
    private final String PLUGIN_UTILS = "PluginUtils";
    private IRepositoryAccess repAccess;
    private static final Logger logger = Logger.getLogger(CpkCoreServiceForTesting.class.getName());

    public CpkCoreServiceForTesting(ICpkEnvironment environment) {

        // this.pluginUtils=pluginUtils;
        // this.repAccess=repAccess;
        super(environment);
        cpkEngine = CpkEngine.getInstanceWithEnv(environment);


    }
}
