/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */
package pt.webdetails.cpk;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import org.dom4j.DocumentException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pt.webdetails.cpf.http.ICommonParameterProvider;
import pt.webdetails.cpf.session.ISessionUtils;
import pt.webdetails.cpf.utils.IPluginUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;
import pt.webdetails.cpf.RestRequestHandler;
import pt.webdetails.cpf.http.CommonParameterProvider;
import pt.webdetails.cpf.impl.SimpleSessionUtils;
import pt.webdetails.cpf.impl.SimpleUserSession;
import pt.webdetails.cpf.repository.IRepositoryAccess;
import pt.webdetails.cpk.elements.IElement;
import pt.webdetails.cpk.security.IAccessControl;
import pt.webdetails.cpk.testUtils.PluginUtilsForTesting;
import pt.webdetails.cpf.repository.vfs.VfsRepositoryAccess;
import pt.webdetails.cpf.session.IUserSession;
import org.pentaho.di.core.KettleEnvironment;

/**
 *
 * @author joao
 */
public class CpkCoreServiceTest {

    private static IPluginUtils pluginUtils;
    private static CpkCoreServiceForTesting cpkCore;
    private static Map<String, ICommonParameterProvider> map;
    private static IRepositoryAccess repAccess;
    private static OutputStream out;
    private static OutputStream outResponse;
    private static String userDir = System.getProperty("user.dir");

    @BeforeClass
    public static void setUp() throws IOException, InitializationException {

        repAccess = new VfsRepositoryAccess(userDir + "/test-resources/repository",
                userDir + "/test-resources/settings");
        pluginUtils = new PluginUtilsForTesting();
        final IUserSession userSession = new SimpleUserSession("userName", null, true, null);
        ICpkEnvironment environment = new ICpkEnvironment() {
            @Override
            public IPluginUtils getPluginUtils() {
                return pluginUtils;
            }

            @Override
            public IRepositoryAccess getRepositoryAccess() {
                return repAccess;
            }

            @Override
            public IAccessControl getAccessControl() {
                return new IAccessControl() {
                    @Override
                    public boolean isAllowed(IElement element) {
                        return true;
                    }

                    @Override
                    public boolean isAdmin() {
                        return true;
                    }

                    @Override
                    public void throwAccessDenied(Map<String, ICommonParameterProvider> parameterProviders) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                };
            }

            @Override
            public String getPluginName() {
                return pluginUtils.getPluginName();
            }

            @Override
            public ISessionUtils getSessionUtils() {
                return new SimpleSessionUtils(userSession, null, null);
            }

            @Override
            public void reload() {
            }
        };
        cpkCore = new CpkCoreServiceForTesting(environment);
        map = new HashMap<String, ICommonParameterProvider>();
        ICommonParameterProvider p = new CommonParameterProvider();
        ICommonParameterProvider p1 = new CommonParameterProvider();
        outResponse = new ByteArrayOutputStream();
        p.put("path", "/pass_arguments");//kjb or ktr
        p.put("outputstream", outResponse);
        p.put("httpresponse", null);
        p1.put("request", "unnecessary value?");
        p1.put("paramarg1", "value1");
        p1.put("paramarg2", "value2");
        p1.put("paramarg3", "value3");
        map.put("path", p);
        map.put("request", p1);

    }

    @Test
    public void testCreateContent() throws Exception {//start a hypersonic to test
        KettleEnvironment.init();
        outResponse = new ByteArrayOutputStream();


        cpkCore.createContent(sampleTrans());
        String sampleTrans_result = outResponse.toString();
        outResponse.close();
        outResponse = new ByteArrayOutputStream();

        cpkCore.createContent(evaluateResultRows());
        String evaluateResultRows_result = outResponse.toString();
        outResponse.close();
        outResponse = new ByteArrayOutputStream();

        cpkCore.createContent(createResultRows());
        String createResultRows_result = outResponse.toString();
        outResponse.close();
        outResponse = new ByteArrayOutputStream();

        cpkCore.createContent(generateRows());
        String generateRows_result = outResponse.toString();
        outResponse.close();


        boolean sampletrans, evaluateResultRows, createResultRows, generateRows;
        sampletrans = evaluateResultRows = createResultRows = generateRows = true;

        JSONObject sampletransJson = new JSONObject(sampleTrans_result);
        JSONObject evaluateResultRowsJson = new JSONObject(evaluateResultRows_result);
        JSONObject createResultRowsJson = new JSONObject(createResultRows_result);
        JSONObject generateRowsJson = new JSONObject(generateRows_result);

        if (sampletransJson.getJSONObject("queryInfo").length() < 1) {
            sampletrans = false;
        }
        if (generateRowsJson.getJSONObject("queryInfo").length() < 1) {
            generateRows = false;
        }
        if (createResultRowsJson.getJSONObject("queryInfo").length() < 1) {
            createResultRows = false;
        }
        if (!evaluateResultRowsJson.getBoolean("result")) {
            evaluateResultRows = false;
        }

        Assert.assertTrue(sampletrans);
        Assert.assertTrue(evaluateResultRows);
        Assert.assertTrue(createResultRows);
        Assert.assertTrue(generateRows);

    }

    @Test
    public void testGetElementsList() throws IOException, JSONException {
        boolean successful = true;

        out = new ByteArrayOutputStream();
        cpkCore.getElementsList(out, map);
        String str = out.toString();

        JSONArray elementsListJson = new JSONArray(str);

        for (int i = 0; i < elementsListJson.length(); i++) {
            JSONObject obj = elementsListJson.getJSONObject(i);
            String id = obj.getString("id");
            if (id.length() < 1) {
                successful = false;
            }
        }

        Assert.assertTrue(successful);
        out.close();
    }

    @Test
    public void testReloadRefreshStatus() throws DocumentException, IOException, JSONException {
        out = new ByteArrayOutputStream();
        cpkCore.reload(out, map);
        String str = out.toString();
        out.close();
        Assert.assertTrue(str.contains("cpkSol Status"));
        Assert.assertTrue(!str.contains("null"));
    }

    @Test
    public void testGetRequestHandler() {
        RestRequestHandler r = cpkCore.getRequestHandler();
        Assert.assertTrue(r != null);
    }

    @Test
    public void testGetPluginName() {

        String str = cpkCore.getPluginName();
        Assert.assertTrue(str.equals("cpkSol"));

    }

    private Map<String, ICommonParameterProvider> sampleTrans() {
        Map<String, ICommonParameterProvider> map = new HashMap<String, ICommonParameterProvider>();
        ICommonParameterProvider p = new CommonParameterProvider();
        ICommonParameterProvider p1 = new CommonParameterProvider();
        p.put("path", "/sampleTrans");//kjb or ktr
        p.put("outputstream", outResponse);
        p.put("httpresponse", null);
        p1.put("paramarg1", "value1");
        p1.put("paramarg2", "value2");
        p1.put("paramarg3", "value3");
        p1.put("kettleOutput", "Json");//not Infered kettle, so must pass Json Output
        map.put("path", p);
        map.put("request", p1);
        return map;
    }

    private Map<String, ICommonParameterProvider> evaluateResultRows() {
        Map<String, ICommonParameterProvider> map = new HashMap<String, ICommonParameterProvider>();
        ICommonParameterProvider p = new CommonParameterProvider();
        ICommonParameterProvider p1 = new CommonParameterProvider();
        p.put("path", "/evaluate-result-rows");//kjb or ktr
        p.put("outputstream", outResponse);
        p.put("httpresponse", null);
        p1.put("paramarg1", "value1");
        p1.put("paramarg2", "value2");
        p1.put("paramarg3", "value3");
        map.put("path", p);
        map.put("request", p1);
        return map;
    }

    private Map<String, ICommonParameterProvider> createResultRows() {
        Map<String, ICommonParameterProvider> map = new HashMap<String, ICommonParameterProvider>();
        ICommonParameterProvider p = new CommonParameterProvider();
        ICommonParameterProvider p1 = new CommonParameterProvider();
        p.put("path", "/create-result-rows");//kjb or ktr
        p.put("outputstream", outResponse);
        p.put("httpresponse", null);
        p1.put("stepName", "copy rows to result");
        p1.put("paramarg1", "value1");
        p1.put("paramarg2", "value2");
        p1.put("paramarg3", "value3");
        map.put("path", p);
        map.put("request", p1);
        return map;
    }

    private Map<String, ICommonParameterProvider> generateRows() {
        Map<String, ICommonParameterProvider> map = new HashMap<String, ICommonParameterProvider>();
        ICommonParameterProvider p = new CommonParameterProvider();
        ICommonParameterProvider p1 = new CommonParameterProvider();
        p.put("path", "/generate-rows");//kjb or ktr
        p.put("outputstream", outResponse);
        p.put("httpresponse", null);
        p1.put("stepName", "output");
        map.put("path", p);
        map.put("request", p1);
        return map;
    }
}
