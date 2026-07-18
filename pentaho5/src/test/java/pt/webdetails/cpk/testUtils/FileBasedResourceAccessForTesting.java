/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package pt.webdetails.cpk.testUtils;

import pt.webdetails.cpf.repository.impl.FileBasedResourceAccess;

import java.io.File;

public class FileBasedResourceAccessForTesting extends FileBasedResourceAccess {

  private String baseDir;

  public FileBasedResourceAccessForTesting( final String baseDir ) {
    this.baseDir = baseDir;
  }

  @Override
  protected File getFile( String path ) {
    return new File( this.baseDir + path );
  }
}
