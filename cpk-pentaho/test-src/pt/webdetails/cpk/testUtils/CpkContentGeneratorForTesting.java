/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cpk.testUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.pentaho.platform.api.engine.IParameterProvider;
import pt.webdetails.cpf.WrapperUtils;
import pt.webdetails.cpf.http.ICommonParameterProvider;
import pt.webdetails.cpk.CpkContentGenerator;
import pt.webdetails.cpk.CpkCoreService;
import pt.webdetails.cpk.CpkPentahoEnvironment;
import pt.webdetails.cpk.ICpkEnvironment;


public class CpkContentGeneratorForTesting extends CpkContentGenerator {

    /*public CpkContentGeneratorForTesting(ICpkEnvironment environment) {
        super(environment);
    }//*/

    public CpkContentGeneratorForTesting( ) {
        super();
        this.cpkEnv = new CpkPentahoEnvironment(pluginUtils, new PentahoRepositoryAccessForTesting());
        this.coreService = new CpkCoreService(cpkEnv);
    }
    
public void wrapParameters(){
        if (parameterProviders != null) {
            Iterator it = parameterProviders.entrySet().iterator();
            map = new HashMap<String, ICommonParameterProvider>();
            while (it.hasNext()) {
                Map.Entry<String, IParameterProvider> e = (Map.Entry<String, IParameterProvider>) it.next();
                map.put(e.getKey(), WrapperUtils.wrapParamProvider(e.getValue()));
            }
        }
        
    } 
}
