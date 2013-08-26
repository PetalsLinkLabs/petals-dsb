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

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.Response;

import java.util.Properties;

/**
 * @author Christophe Hamerling - chamerling@linagora.com
 */
public class MetadataClient {

    private MetadataClient(){
    }

    /**
     * Get a property value
     *
     * @param property
     * @return
     */
    public static final String get(String property) {
        AsyncHttpClient client = new AsyncHttpClient(new AsyncHttpClientConfig.Builder().setRequestTimeoutInMs(10000).build());
        try {
            Response response = client.prepareGet(Constants.METADATA_ENDPOINT + property).execute().get();
            if (200 == response.getStatusCode()) {
                return response.getResponseBody();
            } else {
                // NOP
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Get the user data from the user-data endpoint
     * @return
     */
    public static final Properties getUserData() {
        Properties result = new Properties();
        AsyncHttpClient client = new AsyncHttpClient(new AsyncHttpClientConfig.Builder().setRequestTimeoutInMs(10000).build());
        try {
            Response response = client.prepareGet(Constants.USERDATA_ENDPOINT).execute().get();
            if (200 == response.getStatusCode()) {
                result.load(response.getResponseBodyAsStream());
            } else {
                // NOP
            }
        } catch (Exception e) {
            // NOP
        }
        return result;
    }
}
