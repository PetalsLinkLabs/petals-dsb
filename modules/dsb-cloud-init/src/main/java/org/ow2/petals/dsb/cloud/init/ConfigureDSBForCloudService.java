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
package org.ow2.petals.dsb.cloud.init;

import org.ow2.petals.cloud.init.CloudInit;
import org.ow2.petals.cloud.init.CloudInitException;
import org.ow2.petals.cloud.init.CloudInitImpl;
import org.ow2.petals.kernel.api.server.util.SystemUtil;
import org.petalslink.dsb.api.BootService;
import org.petalslink.dsb.api.DSBException;

/**
 * @author Christophe Hamerling - chamerling@linagora.com
 */
public class ConfigureDSBForCloudService implements BootService {

    public void execute() throws DSBException {
        System.out.println("Configuring the DSB for the Cloud...");
        System.out.println("Petals is installed under " + SystemUtil.getPetalsInstallDirectory());

        CloudInit init = new CloudInitImpl(SystemUtil.getPetalsInstallDirectory());
        try {
            init.configure();
        } catch (CloudInitException e) {
            throw new DSBException(e);
        }
        System.out.println("Node has been configured for the Cloud");
    }

    public boolean shouldAbortBoot(DSBException e) {
        // abort boot on any exception...
        return e != null;
    }
}
