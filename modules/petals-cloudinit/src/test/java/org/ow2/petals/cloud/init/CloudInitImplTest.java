/**
 *
 * Copyright (c) 2013, Linagora
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */
package org.ow2.petals.cloud.init;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.fail;
import static org.mockito.Mockito.*;

/**
 * @author Christophe Hamerling - chamerling@linagora.com
 */
@RunWith(JUnit4.class)
public class CloudInitImplTest {

    @Test
    public void testMustacheTemplateForServerProperties() throws IOException {
        Properties props = new Properties();
        props.put("local-ipv4", "10.10.10.1");
        File out = null;
        try {
            out = File.createTempFile("testMustacheTemplateForServerProperties", "test");
        } catch (IOException e) {
            fail();
        }
        out.deleteOnExit();
        CloudInitImpl client = spy(new CloudInitImpl());
        doReturn(props).when(client).getPlatformProperties();
        client.generateServerProps(out);
        String result = FileUtils.readFileToString(out, null);
        assertTrue(result.contains("petals.container.name=10.10.10.1"));
    }

}
