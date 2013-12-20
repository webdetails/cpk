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

import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.repository.impl.FileBasedResourceAccess;
import pt.webdetails.cpf.repository.pentaho.SystemPluginResourceAccess;
import pt.webdetails.cpf.repository.pentaho.unified.UnifiedRepositoryAccess;
import pt.webdetails.cpf.utils.IPluginUtils;
import pt.webdetails.cpk.CpkPentahoEnvironment;

import java.io.File;
import java.util.Collection;

public class CpkPentahoEnvironmentForTesting extends CpkPentahoEnvironment {

  public CpkPentahoEnvironmentForTesting( IPluginUtils pluginUtils, String[] reservedWords ) {
    super( pluginUtils, reservedWords );
  }

  @Override
  public String getPluginId() {
    return "cpkSol";
  }

  @Override
  public IReadAccess getPluginSystemReader(String basePath) {
    return new FileBasedResourceAccessForTesting( System.getProperty( "user.dir" )
      + "/test-resources/repository/system/cpkSol/" );
  }
}
