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

import org.pentaho.platform.api.engine.IParameterProvider;
import pt.webdetails.cpf.WrapperUtils;
import pt.webdetails.cpf.http.ICommonParameterProvider;
import pt.webdetails.cpk.CpkContentGenerator;
import pt.webdetails.cpk.CpkCoreService;
import pt.webdetails.cpk.CpkPentahoEnvironment;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class CpkContentGeneratorForTesting extends CpkContentGenerator {

    /*public CpkContentGeneratorForTesting(ICpkEnvironment environment) {
        super(environment);
    }//*/

  public CpkContentGeneratorForTesting() {
    super();
    this.cpkEnv = new CpkPentahoEnvironment( pluginUtils );
    this.coreService = new CpkCoreService( cpkEnv );
  }

  public void wrapParameters() {
    if ( parameterProviders != null ) {
      Iterator it = parameterProviders.entrySet().iterator();
      map = new HashMap<String, ICommonParameterProvider>();
      while ( it.hasNext() ) {
        Map.Entry<String, IParameterProvider> e = (Map.Entry<String, IParameterProvider>) it.next();
        map.put( e.getKey(), WrapperUtils.wrapParamProvider( e.getValue() ) );
      }
    }

  }
}
