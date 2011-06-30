package org.mortbay.jetty.test.remote;

import static org.hamcrest.Matchers.*;
import java.io.File;

import org.eclipse.jetty.toolchain.test.IO;
import org.eclipse.jetty.toolchain.test.MavenTestingUtils;
import org.junit.Assert;
import org.junit.Test;


public class RemoteTestSuiteResultsTest
{
    @Test
    public void testAllSuccess() throws Exception {
        File allSuccess = MavenTestingUtils.getTestResourceFile("all-success.json");
        String rawjson = IO.readToString(allSuccess);
        RemoteTestSuiteResults results = new RemoteTestSuiteResults(rawjson);
        Assert.assertThat("Results", results.getTestClassCount(), is(6));
    }
}
