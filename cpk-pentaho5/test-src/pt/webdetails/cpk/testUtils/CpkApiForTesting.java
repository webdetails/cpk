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

import pt.webdetails.cpk.CpkApi;
import pt.webdetails.cpk.CpkCoreService;

public class CpkApiForTesting extends CpkApi {

  private static final String[] reserverdWords = { "default", "refresh", "status", "reload", "getElementsList",
    "getSitemapJson", "version", "getPluginMetadata" };

  public CpkApiForTesting() {
  }

  @Override
  protected void init() {
    this.cpkEnv = new CpkPentahoEnvironmentForTesting( new PluginUtilsForTesting(), reserverdWords );
    this.coreService = new CpkCoreService( cpkEnv );
  }

}
