/*
 * Copyright 2002 - 2014 Webdetails, a Pentaho company.  All rights reserved.
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

import junit.framework.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.parameters.NamedParams;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpk.elements.impl.KettleElementHelper;

import java.util.Map;


public class KettleElementHelperTest {

  //public static boolean hasParameter( NamedParams params, String paramName ) {

  //public static String getParameterDefaultValue( NamedParams params, String paramName ) {

  // public static boolean setParameterValue( NamedParams params, String paramName, String paramValue ) {

  //public static String getParameterValue( NamedParams params, String paramName ) {

  /**
   * Tests if getting a parameter value after setting it returns the proper value.
   */
  @Test
  @Ignore
  public void testSetGetParameterValue () {
    Assert.fail();
  }

  //   public static Collection<String> setKettleParameterValues( NamedParams params, Map<String, String> kettleParams ) {

  //  public static Map<String, String> getKettleParameters( Map<String, Object> requestParams ) {


  // public static void clearParameters( NamedParams params, Collection<String> paramNames ) {



  /**
   * Tests if Session parameters are returned properly.
   */
  @Test
  @Ignore
  public void testGetInjectedSessionParameters() {
    Assert.fail();
  }

  public ICpkEnvironment getMockEnvironmentForPath( String pluginDirPath ) {
    IBasicFile pluginDir = Mockito.mock( IBasicFile.class );
    Mockito.when( pluginDir.getFullPath() ).thenReturn( pluginDirPath );
    Mockito.when( pluginDir.getPath() ).thenReturn( pluginDirPath );

    IReadAccess readAccess = Mockito.mock( IReadAccess.class );
    Mockito.when( readAccess.fetchFile( null ) ).thenReturn( pluginDir );

    IContentAccessFactory accessFactory = Mockito.mock( IContentAccessFactory.class );
    Mockito.when( accessFactory.getPluginSystemReader( null ) ).thenReturn( readAccess );

    ICpkEnvironment mockEnvironment = Mockito.mock( ICpkEnvironment.class );
    Mockito.when( mockEnvironment.getContentAccessFactory() ).thenReturn( accessFactory );

    return mockEnvironment;
  }

  /**
   * Tests if an injected variable with uriPathEncode transformation is encoded properly
   */
  @Test
  public void testUriPathEncodedParameter() {
    // arrange
    String pluginDirPath = "/c:/Program Files/myPentaho";
    ICpkEnvironment mockEnvironment = this.getMockEnvironmentForPath( pluginDirPath );

    KettleElementHelper.setPluginEnvironment( mockEnvironment );

    String paramName = "cpk.plugin.dir" + KettleElementHelper.TRANSFORMATION_SEPARATOR + KettleElementHelper.TRANSFORMATION_URI_PATH_ENCODE;
    String[] parameters = { paramName };
    NamedParams namedParams = Mockito.mock( NamedParams.class );
    Mockito.when( namedParams.listParameters() ).thenReturn( parameters );

    // act
    Map<String, String> injectedParameters = KettleElementHelper.getInjectedParameters( namedParams );


    // assert
    String expectedEncodedPluginDir = "file:/c:/Program%20Files/myPentaho";
    String actualEncodedPluginDir = injectedParameters.get( paramName );
    Assert.assertEquals( expectedEncodedPluginDir, actualEncodedPluginDir );
  }

  /**
   * Tests if an injected variable with uriPathDecode transformation is decoded properly
   */
  @Test
  public void testUriPathDecodedParameter() {
    // arrange
    String pluginDirPath = "/c:/Program%20Files/myPentaho";
    ICpkEnvironment mockEnvironment = this.getMockEnvironmentForPath( pluginDirPath );

    KettleElementHelper.setPluginEnvironment( mockEnvironment );

    String paramName = "cpk.plugin.dir" + KettleElementHelper.TRANSFORMATION_SEPARATOR + KettleElementHelper.TRANSFORMATION_URI_PATH_DECODE;
    String[] parameters = { paramName };
    NamedParams namedParams = Mockito.mock( NamedParams.class );
    Mockito.when( namedParams.listParameters() ).thenReturn( parameters );

    // act
    Map<String, String> injectedParameters = KettleElementHelper.getInjectedParameters( namedParams );


    // assert
    String expectedDecodedPluginDir = "/c:/Program Files/myPentaho";
    String actualEncodedPluginDir = injectedParameters.get( paramName );
    Assert.assertEquals( expectedDecodedPluginDir, actualEncodedPluginDir );
  }
}