package org.ow2.petals.cloud.init;

/**
 * @author Christophe Hamerling - chamerling@linagora.com
 */
public interface Constants {

    public static String CLOUD_FILE = "petals-cloud.cfg";

    public static String CLOUD_CONTROLLER = "cloud-controller";

    public static String TOPOLOGY_URL = "topology-url";

    public static String METADATA_ENDPOINT = "http://169.254.169.254/latest/meta-data/";

    public static String USERDATA_ENDPOINT = "http://169.254.169.254/latest/user-data/";

    public static String LOCAL_IPV4 = "local-ipv4";

    public static String PUBLIC_IPV4 = "public-ipv4";

    public static String HOSTNAME = "hostname";

    public static String LOCAL_HOSTNAME = "local-hostname";

    public static String PUBLIC_HOSTNAME = "local-hostname";

}
