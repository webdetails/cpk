/*!
* Copyright 2002 - 2017 Webdetails, a Hitachi Vantara company.  All rights reserved.
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

import java.util.logging.Logger;

import pt.webdetails.cpf.repository.IRepositoryAccess;

public class CpkCoreServiceForTesting extends CpkCoreService {

  private static final long serialVersionUID = 1L;
  private static final String ENCODING = "UTF-8";
  private final String PLUGIN_UTILS = "PluginUtils";
  private IRepositoryAccess repAccess;
  private static final Logger logger = Logger.getLogger( CpkCoreServiceForTesting.class.getName() );

  public CpkCoreServiceForTesting( ICpkEnvironment environment ) {

    // this.pluginUtils=pluginUtils;
    // this.repAccess=repAccess;
    super( environment );
    //cpkEngine = CpkEngine.getInstanceWithEnv( environment );


  }
}
