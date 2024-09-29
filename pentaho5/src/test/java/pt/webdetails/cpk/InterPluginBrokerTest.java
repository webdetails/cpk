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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import pt.webdetails.cpf.PluginEnvironment;
import pt.webdetails.cpf.PluginSettings;
import pt.webdetails.cpf.plugincall.api.IPluginCall;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static pt.webdetails.cpk.InterPluginBroker.CDE_RENDER_API_BEAN_ID;
import static pt.webdetails.cpk.InterPluginBroker.CDE_RENDER_API_BEAN_ID_TAG;
import static pt.webdetails.cpk.InterPluginBroker.CDE_RENDER_API_LEGACY_BEAN_ID;
import static pt.webdetails.cpk.InterPluginBroker.CDE_RENDER_API_RENDER_METHOD;
import static pt.webdetails.cpk.InterPluginBroker.CDE_RENDER_API_RENDER_METHOD_TAG;

@RunWith( MockitoJUnitRunner.class )
public class InterPluginBrokerTest {
  MockedStatic<PluginEnvironment> mockedStatic;
  IPluginCall pluginCall;

  @Before
  public void setUp() {
    pluginCall = mock( IPluginCall.class );
    InterPluginBroker.clearCdeRenderApiCall();
  }

  @After
  public void clear() {
  }

  @Test
  public void testRunWithConfiguredValues() throws Exception {
    String beanID = "configured-bean-id";
    String beanMethod = "configured-bean-method";
    String pluginCallResult = "Test with configured values.";

    // mock PluginEnvironment
    PluginEnvironment environmentMock = mock( PluginEnvironment.class );
    PluginEnvironment.init( environmentMock );

    doReturn( mockPluginSettings( beanID, beanMethod ) ).when( environmentMock )
      .getPluginSettings();

    doReturn( mockPluginCall( true, pluginCallResult ) ).when( environmentMock )
      .getPluginCall( anyString(), eq( beanID ), eq( beanMethod ) );

    // Test output
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    InterPluginBroker.run( new HashMap<>(), out );

    assertEquals( pluginCallResult, out.toString() );
  }

  @Test
  public void testRunWithLatestBeanId() throws Exception {
    String pluginCallResult = "Test with the latest bean id.";

    // mock PluginEnvironment
    PluginEnvironment environmentMock = mock( PluginEnvironment.class );
    PluginEnvironment.init( environmentMock );
    doReturn( mockPluginSettings( null, null ) ).when( environmentMock ).getPluginSettings();

    doReturn( mockPluginCall( false, null ) ).when( environmentMock )
      .getPluginCall( anyString(), eq( "" ), eq( "" ) );

    Mockito.lenient().doReturn( mockPluginCall( true, pluginCallResult ) ).when( environmentMock )
      .getPluginCall( anyString(), eq( CDE_RENDER_API_BEAN_ID ), eq( CDE_RENDER_API_RENDER_METHOD ) );

    // Test output
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    InterPluginBroker.run( new HashMap<>(), out );

    assertEquals( pluginCallResult, out.toString() );
  }

  @Test
  public void testRunWithLegacyBeanId() throws Exception {
    String pluginCallResult = "Test with legacy bean id.";

    // mock PluginEnvironment
    PluginEnvironment environmentMock = mock( PluginEnvironment.class );
    PluginEnvironment.init( environmentMock );
    doReturn( mockPluginSettings( null, null ) ).when( environmentMock ).getPluginSettings();

    doReturn( mockPluginCall( false, null ) ).when( environmentMock )
      .getPluginCall( anyString(), eq( "" ), eq( "" ) );

    Mockito.lenient().doReturn( mockPluginCall( false, null ) ).when( environmentMock )
      .getPluginCall( anyString(), eq( CDE_RENDER_API_BEAN_ID ), eq( CDE_RENDER_API_RENDER_METHOD ) );

    Mockito.lenient().doReturn( mockPluginCall( true, pluginCallResult ) ).when( environmentMock )
      .getPluginCall( anyString(), eq( CDE_RENDER_API_LEGACY_BEAN_ID ), eq( CDE_RENDER_API_RENDER_METHOD ) );

    // Test output
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    InterPluginBroker.run( new HashMap<>(), out );

    assertEquals( pluginCallResult, out.toString() );
  }

  private IPluginCall mockPluginCall( boolean exists, String data ) throws Exception {
    //pluginCall = mock( IPluginCall.class );

    doReturn( exists ).when( pluginCall ).exists();
    doReturn( data ).when( pluginCall ).call( any() );

    return pluginCall;
  }

  private PluginSettings mockPluginSettings( String beanId, String beanMethod ) {
    List<String> beanIdList = new ArrayList<>();
    if ( beanId != null ) {
      beanIdList.add( beanId );
    }

    List<String> beanMethodList = new ArrayList<>();
    if ( beanMethod != null ) {
      beanMethodList.add( beanMethod );
    }

    PluginSettings settings = mock( PluginSettings.class );
    doReturn( beanIdList ).when( settings ).getTagValue( eq( CDE_RENDER_API_BEAN_ID_TAG ) );
    doReturn( beanMethodList ).when( settings ).getTagValue( eq( CDE_RENDER_API_RENDER_METHOD_TAG ) );

    return settings;
  }
}
