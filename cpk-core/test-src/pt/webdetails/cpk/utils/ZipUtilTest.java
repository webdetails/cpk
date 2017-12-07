package pt.webdetails.cpk.utils;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.steps.systemdata.SystemData;


/**
 * Created by chrisdeptula on 2/20/16.
 */
public class ZipUtilTest {

    private static ZipUtil zipUtil;

    @Before
    public void setUp() {
        zipUtil = new ZipUtil();
    }

    @Test
    public void testRemoveTopFilenamePathFromString() throws Exception {
        String topFileName = "file:///pentaho/biserver/pentaho-solutions/system/sparkl/test.txt";
        String fileName = "/pentaho/biserver/pentaho-solutions/system/sparkl/dashboards/admin/Main.cdfde";
        zipUtil.topFilename = KettleVFS.getFileObject(topFileName).getParent().getName();
        String removeTopFilename = zipUtil.removeTopFilenamePathFromString(fileName);
        Assert.assertEquals( "sparkl/dashboards/admin/Main.cdfde", removeTopFilename );
    }

}
