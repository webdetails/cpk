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
