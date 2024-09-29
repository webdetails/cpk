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


package pt.webdetails.cpk;

import junit.framework.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.parameters.DuplicateParamException;
import org.pentaho.di.core.parameters.NamedParams;
import org.pentaho.di.core.parameters.NamedParamsDefault;
import org.pentaho.di.core.parameters.UnknownParamException;
import pt.webdetails.cpf.repository.api.IBasicFile;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.session.ISessionUtils;
import pt.webdetails.cpf.session.IUserSession;
import pt.webdetails.cpk.elements.impl.KettleElementHelper;

import java.util.HashMap;
import java.util.Map;


public class KettleElementHelperTest {

  // region Tests

  /**
   * Tests that hasParameter function returns true for an existing parameter
   */
  @Test
  public void testHasExistingParameter () {
    // arrange
    String name = "paramName";
    NamedParams params = new NamedParamsDefault();
    try {
      params.addParameterDefinition( name, null, null );
    } catch ( DuplicateParamException e ) {
      Assert.fail( "Failed arranging test." );
    }

    // act
    boolean actualHasParameter = KettleElementHelper.hasParameter( params, name );

    // assert
    Assert.assertEquals( true, actualHasParameter );
  }

  /**
   * Tests that hasParameter function returns false for non existing parameter
   */
  @Test
  public void testDoesNotHaveNonExistingParameter () {
    // arrange
    String name = "paramName";
    NamedParams params = new NamedParamsDefault();

    // act
    boolean actualHasParameter = KettleElementHelper.hasParameter( params, name );

    // assert
    Assert.assertEquals( false, actualHasParameter );
  }

  @Test
  public void testSetParameterValue () {
    // arrange
    String name = "paramName";
    String expectedValue = "paramValue";
    NamedParams params = new NamedParamsDefault();
    try {
      params.addParameterDefinition( name, null, null );
    } catch ( DuplicateParamException e ) {
      Assert.fail( "Failed arranging test." );
    }

    // act
    KettleElementHelper.setParameterValue( params, name, expectedValue );

    // assert
    try {
      String actualValue = params.getParameterValue( name );
      Assert.assertEquals( expectedValue, actualValue );
    } catch ( UnknownParamException e ) {
      Assert.fail( );
    }
  }

  @Test
  public void testGetParameterValue () {
    // arrange
    String name = "paramName";
    String expectedValue = "paramValue";
    NamedParams params = new NamedParamsDefault();
    try {
      params.addParameterDefinition( name, null, null );
      params.setParameterValue( name, expectedValue );
    } catch ( Exception e ) {
      Assert.fail( "Failed arranging test." );
    }

    // act
    String actualValue = KettleElementHelper.getParameterValue( params, name );

    // assert
    Assert.assertEquals( expectedValue, actualValue );
  }

  @Test
  public void testGetParameterDefaultValue () {
    // arrange
    String name = "paramName";
    String expectedValue = "paramDefaultValue";
    NamedParams params = new NamedParamsDefault();
    try {
      params.addParameterDefinition( name, expectedValue, null );
    } catch ( Exception e ) {
      Assert.fail( "Failed arranging test." );
    }

    // act
    String actualValue = KettleElementHelper.getParameterDefaultValue( params, name );

    // assert
    Assert.assertEquals( expectedValue, actualValue );
  }

  /**
   * Tests that when setting multiple parameter values on existing parameters, the values are set.
   */
  @Test
  public void testSetParameterValues () {
    // arrange
    Map<String, String> setParameters = new HashMap<String, String>();
    String name1 = "paramName1";
    String expectedValue1 = "paramValue1";
    String name2 = "paramName2";
    String expectedValue2 = "paramValue2";
    setParameters.put( name1, expectedValue1 );
    setParameters.put( name2, expectedValue2 );

    NamedParams params = new NamedParamsDefault();
    try {
      params.addParameterDefinition( name1, null, null );
      params.addParameterDefinition( name2, null, null );
    } catch ( DuplicateParamException e ) {
      Assert.fail( "Failed arranging test." );
    }

    // act
    KettleElementHelper.setKettleParameterValues( params, setParameters );

    // assert
    try {
      String actualValue1 = params.getParameterValue( name1 );
      String actualValue2 = params.getParameterValue( name2 );
      Assert.assertEquals( expectedValue1, actualValue1 );
      Assert.assertEquals( expectedValue2, actualValue2 );
    } catch ( UnknownParamException e ) {
      Assert.fail();
    }
  }

  /**
   * Tests that setKettleParameterValues can handle source parameters which are not defined in the named parameter map.
   */
  @Test
  public void testSetParameterValuesIgnoresUndefinedSourceParameters () {
    // arrange
    Map<String, String> setParameters = new HashMap<>();

    String existingParamName = "paramName";
    String existingParamValue = "paramValue";

    String notExistingParamName = "notExistName";
    String notExistingParamValue = "notExistValue";

    setParameters.put( existingParamName, existingParamValue );
    setParameters.put( notExistingParamName, notExistingParamValue );

    NamedParams params = new NamedParamsDefault();
    try {
      params.addParameterDefinition( existingParamName, null, null );
    } catch ( DuplicateParamException e ) {
      Assert.fail( "Failed arranging test." );
    }

    // act
    KettleElementHelper.setKettleParameterValues( params, setParameters );

    // assert
    try {
      String actualExistingParamValue = params.getParameterValue( existingParamName );
      Assert.assertEquals( existingParamValue, actualExistingParamValue );
    } catch ( UnknownParamException e ) {
      Assert.fail();
    }

    try {
      String actualNotExistingParamValue = params.getParameterValue( notExistingParamName );

      // If we're here, it's because the `NamedParamsDefault` implementation does not throw an error, as it should...
      // Should be null, at least.
      Assert.assertNull( actualNotExistingParamValue );
    } catch( UnknownParamException e ) {
      // Expected.
    }

    Assert.assertEquals( 1, params.listParameters().length );
  }

  @Test
  public void testGetPluginIdParameter() {
    // arrange
    final String pluginIdParamName = "cpk.plugin.id";
    final String expectedPluginId = "myPluginId";

    NamedParams params = new NamedParamsDefault();
    try {
      params.addParameterDefinition( pluginIdParamName, null, null );
    } catch ( DuplicateParamException e ) {
      Assert.fail( "Failed arranging test." );
    }

    // we don't care about the path
    ICpkEnvironment mockEnvironment = this.getMockEnvironmentForPath( "" );
    Mockito.when( mockEnvironment.getPluginName() ).thenReturn( expectedPluginId );
    KettleElementHelper.setPluginEnvironment( mockEnvironment );

    // act
    String actualPluginId = KettleElementHelper.getInjectedParameters( params ).get( pluginIdParamName );

    // assert
    Assert.assertEquals( expectedPluginId, actualPluginId );
  }

  @Test
  public void testGetCpkSolutionSystemDirParameter() {
    // arrange
    final String solutionSystemDirParamName = "cpk.solution.system.dir";
    final String pluginDir = "/c:/program files/bi-server/pentaho-solutions/system/myPlugin";
    final String expectedSystemDir = "/c:/program files/bi-server/pentaho-solutions/system";

    NamedParams params = new NamedParamsDefault();
    try {
      params.addParameterDefinition( solutionSystemDirParamName, null, null );
    } catch ( DuplicateParamException e ) {
      Assert.fail( "Failed arranging test." );
    }

    ICpkEnvironment mockEnvironment = this.getMockEnvironmentForPath( pluginDir );
    KettleElementHelper.setPluginEnvironment( mockEnvironment );

    // act
    String actualSystemDir = KettleElementHelper.getInjectedParameters( params ).get( solutionSystemDirParamName );

    // assert
    Assert.assertEquals( expectedSystemDir, actualSystemDir );
  }

  @Test
  public void testGetCpkPluginDirParameter() {
    // arrange
    final String pluginDirParamName = "cpk.plugin.dir";
    final String expectedPluginDir = "/c:/program files/bi-server/pentaho-solutions/system/myPlugin";

    NamedParams params = new NamedParamsDefault();
    try {
      params.addParameterDefinition( pluginDirParamName, null, null );
    } catch ( DuplicateParamException e ) {
      Assert.fail( "Failed arranging test." );
    }

    ICpkEnvironment mockEnvironment = this.getMockEnvironmentForPath( expectedPluginDir );
    KettleElementHelper.setPluginEnvironment( mockEnvironment );

    // act
    String actualPluginDir = KettleElementHelper.getInjectedParameters( params ).get( pluginDirParamName );

    // assert
    Assert.assertEquals( expectedPluginDir, actualPluginDir );
  }

  @Test
  public void testGetCpkWebAppDirParameter() {
    // arrange
    final String webAppDirParamName = "cpk.webapp.dir";
    final String expectedWebAppDir = "/c:/program files/bi-server/pentaho-solutions/tomcat/webapps";

    NamedParams params = new NamedParamsDefault();
    try {
      params.addParameterDefinition( webAppDirParamName, null, null );
    } catch ( DuplicateParamException e ) {
      Assert.fail( "Failed arranging test." );
    }

    ICpkEnvironment mockEnvironment = this.getMockEnvironmentForPath( "" );
    Mockito.when( mockEnvironment.getWebAppDir() ).thenReturn( expectedWebAppDir );
    KettleElementHelper.setPluginEnvironment( mockEnvironment );

    // act
    String actualWebAppDir = KettleElementHelper.getInjectedParameters( params ).get( webAppDirParamName );

    // assert
    Assert.assertEquals( expectedWebAppDir, actualWebAppDir );
  }

  @Test
  public void testGetCpkSessionUsernameParameter() {
    // arrange
    final String usernameParamName = "cpk.session.username";
    String expectedUserName = "myUsername";

    ICpkEnvironment mockEnvironment = getMockEnvironmentForPath( "" );
    addSessionMock( mockEnvironment, expectedUserName, null );
    KettleElementHelper.setPluginEnvironment( mockEnvironment );

    NamedParams params = new NamedParamsDefault();
    try {
      params.addParameterDefinition( usernameParamName, null, null );
    } catch ( DuplicateParamException e ) {
      Assert.fail( "Failed arranging test." );
    }

    // act
    String actualUserName = KettleElementHelper.getInjectedParameters( params ).get( usernameParamName );

    // assert
    Assert.assertEquals( expectedUserName, actualUserName );
  }

  @Test
  public void testGetCpkSessionRolesParameter() {
    // arrange
    final String rolesParamName = "cpk.session.roles";
    String[] roles = { "admin", "something" };
    String expectedRoles = "{\"roles\":[\"admin\",\"something\"]}";

    ICpkEnvironment mockEnvironment = getMockEnvironmentForPath( "" );
    addSessionMock( mockEnvironment, null, roles );
    KettleElementHelper.setPluginEnvironment( mockEnvironment );

    NamedParams params = new NamedParamsDefault();
    try {
      params.addParameterDefinition( rolesParamName, null, null );
    } catch ( DuplicateParamException e ) {
      Assert.fail( "Failed arranging test." );
    }

    // act
    String actualRoles = KettleElementHelper.getInjectedParameters( params ).get( rolesParamName );

    // assert
    Assert.assertEquals( expectedRoles, actualRoles );
  }

  @Test
  public void testGetCpkSessionParameter() {
    // arrange
    final String sessionParamName = "aSessionParameter";
    final String cpkSessionParamName = "cpk.session." + sessionParamName;
    final String expectedValue = "myValue";

    ICpkEnvironment mockEnvironment = getMockEnvironmentForPath( "" );
    IUserSession mockSession = Mockito.mock( IUserSession.class );
    Mockito.when( mockSession.getStringParameter( sessionParamName ) ).thenReturn( expectedValue );
    ISessionUtils mockSessionUtils = Mockito.mock( ISessionUtils.class );
    Mockito.when( mockSessionUtils.getCurrentSession() ).thenReturn( mockSession );
    Mockito.when( mockEnvironment.getSessionUtils() ).thenReturn( mockSessionUtils );

    KettleElementHelper.setPluginEnvironment( mockEnvironment );

    NamedParams params = new NamedParamsDefault();
    try {
      params.addParameterDefinition( cpkSessionParamName, null, null );
    } catch ( DuplicateParamException e ) {
      Assert.fail( "Failed arranging test." );
    }

    // act
    String actualValue = KettleElementHelper.getInjectedParameters( params ).get( cpkSessionParamName );

    // assert
    Assert.assertEquals( expectedValue, actualValue );
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

  // endregion

  // region auxiliary methods

  /**
   * Adds username and roles information to cpk environment mock
   * @param mockEnvironment
   * @param username
   * @param roles
   */
  private void addSessionMock( ICpkEnvironment mockEnvironment, String username, String[] roles ) {
    IUserSession mockSession = Mockito.mock( IUserSession.class );
    Mockito.when( mockSession.getUserName() ).thenReturn( username );
    Mockito.when( mockSession.getAuthorities() ).thenReturn( roles );

    ISessionUtils mockSessionUtils = Mockito.mock( ISessionUtils.class );
    Mockito.when( mockSessionUtils.getCurrentSession() ).thenReturn( mockSession );

    Mockito.when( mockEnvironment.getSessionUtils() ).thenReturn( mockSessionUtils );
  }

  /**
   * Creates a mock ICpkEnvironment with a ContentAccessFactory for the given pluginDir
   * Auxiliary method for UriPathEncode and UriPathDecode tests.
   *
   * @param pluginDirPath
   * @return
   */
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

  // endregion

}
