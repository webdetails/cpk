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

import org.mockito.Mockito;
import pt.webdetails.cpf.plugins.PluginsAnalyzer;
import pt.webdetails.cpk.CpkApi;
import pt.webdetails.cpk.CpkCoreService;


public class CpkApiForTesting extends CpkApi {

  public CpkApiForTesting() {
  }

  @Override
  protected void init() {
    this.cpkEnv = new CpkPentahoEnvironmentForTesting( new PluginUtilsForTesting(), reservedWords );
    this.coreService = Mockito.mock( CpkCoreService.class );
    this.pluginsAnalyzer = Mockito.mock( PluginsAnalyzer.class );
  }
}
