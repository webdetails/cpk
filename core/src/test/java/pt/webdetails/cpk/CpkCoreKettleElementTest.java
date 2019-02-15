/*!
* Copyright 2002 - 2019 Webdetails, a Hitachi Vantara company.  All rights reserved.
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

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import pt.webdetails.cpf.repository.IRepositoryAccess;
import pt.webdetails.cpf.repository.vfs.VfsRepositoryAccess;
import pt.webdetails.cpf.utils.IPluginUtils;
import pt.webdetails.cpk.testUtils.CpkEnvironmentForTesting;
import pt.webdetails.cpk.testUtils.HttpServletResponseForTesting;
import pt.webdetails.cpk.testUtils.PluginUtilsForTesting;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import pt.webdetails.cpk.elements.impl.KettleElementHelper;

public class CpkCoreKettleElementTest {

  private static String userDir = System.getProperty( "user.dir" );
  private static CpkCoreService cpkCore;
  private static ICpkEnvironment environment;

  @BeforeClass
  public static void setUp() throws KettleException {

    IRepositoryAccess repAccess = new VfsRepositoryAccess( userDir + "/target/test-classes/cpkSol",
      userDir + "/target/test-classes/settings" );
    IPluginUtils pluginUtils = new PluginUtilsForTesting();
    environment = new CpkEnvironmentForTesting( pluginUtils, repAccess );

    KettleEnvironment.init();
    cpkCore = new CpkCoreService( environment );
  }

  @Test
  public void testCpkParameters() throws Exception {
    OutputStream outResponse = new ByteArrayOutputStream();
    KettleElementHelper.setPluginEnvironment( environment );
    cpkCore.createContent( testParameters( outResponse ) );
    JSONObject json = new JSONObject( outResponse.toString() );
    outResponse.close();

    // assert there is a result
    boolean hasResults = json.getJSONObject( "queryInfo" ).length() > 0;
    Assert.assertTrue( hasResults );

    // define the expected result
    HashMap<String, String> parameters = new HashMap<String, String>();
    parameters.put( "pluginId", "cpkSol" );
    parameters.put( "solutionSystemDir", getFolderPathOSDependent( userDir + "/target/test-classes/" ) );
    parameters.put( "pluginDir", getFolderPathOSDependent( userDir + "/target/test-classes/cpkSol/" ) );
    parameters.put( "pluginSystemDir", getFolderPathOSDependent( userDir
      + "/target/test-classes/cpkSol/system/" ) );
    parameters.put( "webappDir", "" );
    parameters.put( "sessionUsername", "userName" );
    parameters.put( "sessionRoles", "{\"roles\":[\"administrator\",\"authenticated\"]}" );

    // assert that the result is the expected result
    int row = 0;
    int colLen = json.getJSONArray( "metadata" ).length();
    for ( int column = 0; column < colLen; column++ ) {
      final String paramName = json.getJSONArray( "metadata" ).getJSONObject( column ).getString( "colName" );
      final String paramValue = json.getJSONArray( "resultset" ).getJSONArray( row ).getString( column );
      if ( parameters.containsKey( paramName ) ) {
        Assert.assertEquals( paramValue, parameters.get( paramName ) );
      }
    }
  }

  private String getFolderPathOSDependent( String folderPath ) {
    return new File( folderPath ).toURI().getPath();
  }

  private Map<String, Map<String, Object>> testParameters( OutputStream outResponse ) {
    Map<String, Map<String, Object>> mainMap = new HashMap<String, Map<String, Object>>();
    Map<String, Object> pathMap = new HashMap<String, Object>();
    Map<String, Object> requestMap = new HashMap<String, Object>();

    pathMap.put( "path", "/testParameters" );
    pathMap.put( "httpresponse", new HttpServletResponseForTesting( outResponse ) );
    requestMap.put( "kettleOutput", "Json" );

    mainMap.put( "path", pathMap );
    mainMap.put( "request", requestMap );
    return mainMap;
  }
}
