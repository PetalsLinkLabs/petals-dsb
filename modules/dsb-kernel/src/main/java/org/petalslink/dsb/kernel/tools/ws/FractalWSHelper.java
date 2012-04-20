/**
 * 
 */
package org.petalslink.dsb.kernel.tools.ws;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.control.ContentController;
import org.objectweb.fractal.util.Fractal;
import org.ow2.petals.tools.ws.WebServiceHelper;
import org.petalslink.dsb.fractal.utils.FractalHelper;
import org.petalslink.dsb.kernel.api.tools.ws.WebServiceInformationBean;

/**
 * @author chamerling - PetalsLink
 * 
 */
public class FractalWSHelper {
    
    private static Logger logger = Logger.getLogger(FractalWSHelper.class.getName());

    public static final Set<WebServiceInformationBean> getAllBeans(
            final ContentController parentContentController) {
        Set<WebServiceInformationBean> result = new HashSet<WebServiceInformationBean>();
        Set<Component> components = FractalHelper.getAllComponents(parentContentController);
        for (Component component : components) {
            try {
                String name = Fractal.getNameController(component).getFcName();
                logger.fine("- Component : " + name);

                Object[] itfs = component.getFcInterfaces();
                for (Object object : itfs) {
                    // when we get all the interfaces, we get the component one
                    // + the fractal ones + the client ones...
                    // we need to introspect to just get the main interface...

                    org.objectweb.fractal.api.Interface i = (org.objectweb.fractal.api.Interface) object;
                    logger.fine(" - FCITF Name = " + i.getFcItfName());
                    logger.fine(" - FCITF Type = " + i.getFcItfType());
                    logger.fine(" - FCITF internal = " + i.isFcInternalItf());

                    // needs to check that the type is something like
                    // - FCITF Type =
                    // service/esstar.petalslink.com.service.management.admin._1_0.AdminManagement/server/mandatory/singleton
                    // which contains /server/ and not /client/
                    // and which is not a fractal interface ie does not contains
                    // org.objectweb.fractal
                    String stringName = i.getFcItfType().toString();

                    if (stringName.contains("/server/")
                            && !stringName.contains("org.objectweb.fractal")) {
                        
                        Class<?>[] cs = object.getClass().getInterfaces();
                        for (Class<?> class1 : cs) {
                            String className = class1.getName();
                            if (!className.contains("org.objectweb.fractal")) {
                                boolean isWs = WebServiceHelper.hasWebServiceAnnotation(class1);
                                if (isWs) {
                                    logger.fine("  - " + className + " is a web service");
                                    WebServiceInformationBean bean = new WebServiceInformationBean();
                                    bean.clazz = class1;
                                    bean.componentName = name;
                                    bean.implem = object;
                                    result.add(bean);
                                } else {
                                    logger.fine("  - " + className + " is NOT a web service");
                                }
                            }
                        }
                    } else {
                        // not the main component...
                        logger.fine("Bad component, not the main but probably an import...");
                    }
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return result;

    }
}
