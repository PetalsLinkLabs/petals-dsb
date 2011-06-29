/**
 * 
 */
package org.petalslink.dsb.kernel.servicepoller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.petalslink.dsb.servicepoller.api.ServicePollerInformation;
import org.w3c.dom.Document;

/**
 * @author chamerling
 * 
 */
public class ConfigurationLoader {

    public static final String CRON = "cron";

    public static final String TOPOLLENDPOINT = "pollendpoint";

    public static final String TOPOLLINTERFACE = "pollinterface";

    public static final String TOPOLLOPERATION = "polloperation";

    public static final String TOPOLLSERVICE = "pollservice";

    public static final String REPLYENDPOINT = "replyendpoint";

    public static final String REPLYINTERFACE = "replyinterface";

    public static final String REPLYOPERATION = "replyoperation";

    public static final String REPLYSERVICE = "replyservice";

    public static final List<Configuration> load(File configuration) {
        List<Configuration> result = new ArrayList<Configuration>();
        if (configuration == null || configuration.isDirectory()) {
            return result;
        }
        
        File folder = configuration.getParentFile();
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(configuration));
            // get all the keys
            Set<String> keys = getAllKeys(props);
            for (String string : keys) {
                Configuration config = load(props, string);
                if (config != null) {
                    // put the input message...
                    Document inputMessage = loadInputMessage(folder, string);
                    if (inputMessage != null) {
                        config.inputMessage = inputMessage;
                    }
                    result.add(config);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * @param props
     * @return
     */
    public static Set<String> getAllKeys(Properties props) {
        Set<String> result = new HashSet<String>();
        if (props == null) {
            return result;
        }
        for (Object o : props.keySet()) {
            String key = o.toString();
            if (key.indexOf('.') > 0 && key.indexOf('.') != key.length()) {
                result.add(key.substring(0, key.indexOf('.')));
            }
        }
        return result;
    }

    public static final Configuration load(Properties props, String key) {
        if (props == null || key == null || key.length() == 0) {
            return null;
        }

        Map<String, String> map = new HashMap<String, String>();
        for (Object o : props.keySet()) {
            String k = o.toString();
            if (k.startsWith(key + ".")) {
                map.put(k.substring(key.length() + 1), props.getProperty(k));
            }
        }

        if (map.size() == 0) {
            return null;
        }

        return load(map);
    }

    public static final Document loadInputMessage(File path, String key) {
        Document document = null;
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            document = builder.parse(new FileInputStream(new File(path, key + ".xml")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return document;
    }

    public static final Configuration load(Map<String, String> values) {
        Configuration result = null;
        if (values == null) {
            return result;
        }

        result = new Configuration();
        result.cronExpression = values.get(CRON);
        result.toPoll = new ServicePollerInformation();
        result.toPoll.setEndpointName(values.get(TOPOLLENDPOINT));

        QName itf = values.get(TOPOLLINTERFACE) != null ? QName
                .valueOf(values.get(TOPOLLINTERFACE)) : null;
        result.toPoll.setInterfaceName(itf);
        QName operation = values.get(TOPOLLOPERATION) != null ? QName.valueOf(values
                .get(TOPOLLOPERATION)) : null;
        result.toPoll.setOperation(operation);
        QName serviceName = values.get(TOPOLLSERVICE) != null ? QName.valueOf(values
                .get(TOPOLLSERVICE)) : null;
        result.toPoll.setServiceName(serviceName);

        result.replyTo = new ServicePollerInformation();
        result.replyTo.setEndpointName(values.get(REPLYENDPOINT));

        itf = values.get(REPLYINTERFACE) != null ? QName.valueOf(values.get(REPLYINTERFACE)) : null;
        result.replyTo.setInterfaceName(itf);
        operation = values.get(REPLYOPERATION) != null ? QName.valueOf(values.get(REPLYOPERATION))
                : null;
        result.replyTo.setOperation(operation);
        serviceName = values.get(REPLYSERVICE) != null ? QName.valueOf(values.get(REPLYSERVICE))
                : null;
        result.replyTo.setServiceName(serviceName);

        // load the document. Its name is key.xml

        return result;
    }

}
