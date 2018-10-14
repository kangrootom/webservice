package cn.itcast.byecode;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service;

/**
 * This class was generated by Apache CXF 2.4.2
 * 2018-10-14T18:04:52.386+08:00
 * Generated source version: 2.4.2
 * 
 */
@WebServiceClient(name = "ByeInterService", 
                  wsdlLocation = "http://localhost:8080/cxf-web-server/services/bye?wsdl",
                  targetNamespace = "http://inter.server.itcast.cn/") 
public class ByeInterService extends Service {

    public final static URL WSDL_LOCATION;

    public final static QName SERVICE = new QName("http://inter.server.itcast.cn/", "ByeInterService");
    public final static QName ByeInterPort = new QName("http://inter.server.itcast.cn/", "ByeInterPort");
    static {
        URL url = null;
        try {
            url = new URL("http://localhost:8080/cxf-web-server/services/bye?wsdl");
        } catch (MalformedURLException e) {
            java.util.logging.Logger.getLogger(ByeInterService.class.getName())
                .log(java.util.logging.Level.INFO, 
                     "Can not initialize the default wsdl from {0}", "http://localhost:8080/cxf-web-server/services/bye?wsdl");
        }
        WSDL_LOCATION = url;
    }

    public ByeInterService(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public ByeInterService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public ByeInterService() {
        super(WSDL_LOCATION, SERVICE);
    }
    
    //This constructor requires JAX-WS API 2.2. You will need to endorse the 2.2
    //API jar or re-run wsdl2java with "-frontend jaxws21" to generate JAX-WS 2.1
    //compliant code instead.
    public ByeInterService(WebServiceFeature ... features) {
        super(WSDL_LOCATION, SERVICE, features);
    }

    //This constructor requires JAX-WS API 2.2. You will need to endorse the 2.2
    //API jar or re-run wsdl2java with "-frontend jaxws21" to generate JAX-WS 2.1
    //compliant code instead.
    public ByeInterService(URL wsdlLocation, WebServiceFeature ... features) {
        super(wsdlLocation, SERVICE, features);
    }

    //This constructor requires JAX-WS API 2.2. You will need to endorse the 2.2
    //API jar or re-run wsdl2java with "-frontend jaxws21" to generate JAX-WS 2.1
    //compliant code instead.
    public ByeInterService(URL wsdlLocation, QName serviceName, WebServiceFeature ... features) {
        super(wsdlLocation, serviceName, features);
    }

    /**
     *
     * @return
     *     returns ByeInter
     */
    @WebEndpoint(name = "ByeInterPort")
    public ByeInter getByeInterPort() {
        return super.getPort(ByeInterPort, ByeInter.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns ByeInter
     */
    @WebEndpoint(name = "ByeInterPort")
    public ByeInter getByeInterPort(WebServiceFeature... features) {
        return super.getPort(ByeInterPort, ByeInter.class, features);
    }

}
