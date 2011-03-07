/*
 * 
 */

package net.webservicex;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;

/**
 * This class was generated by Apache CXF 2.1.3 Thu Jul 30 11:51:00 CEST 2009
 * Generated source version: 2.1.3
 * 
 */

@WebServiceClient(name = "StockQuote", wsdlLocation = "./src/test/resources/wsdl/stockquote.wsdl", targetNamespace = "http://www.webserviceX.NET/")
public class StockQuote extends Service {

	public final static URL WSDL_LOCATION;
	public final static QName SERVICE = new QName(
			"http://www.webserviceX.NET/", "StockQuote");
	public final static QName StockQuoteSoap1 = new QName(
			"http://www.webserviceX.NET/", "StockQuoteSoap1");
	public final static QName StockQuoteSoap2 = new QName(
			"http://www.webserviceX.NET/", "StockQuoteSoap2");
	public final static QName StockQuoteSoap3 = new QName(
			"http://www.webserviceX.NET/", "StockQuoteSoap3");
	static {
		URL url = null;
		try {
			url = new URL("./src/test/resources/wsdl/stockquote.wsdl");
		} catch (final MalformedURLException e) {
			System.err
					.println("Can not initialize the default wsdl from ./src/test/resources/wsdl/stockquote.wsdl");
			// e.printStackTrace();
		}
		WSDL_LOCATION = url;
	}

	public StockQuote() {
		super(StockQuote.WSDL_LOCATION, StockQuote.SERVICE);
	}

	public StockQuote(final URL wsdlLocation) {
		super(wsdlLocation, StockQuote.SERVICE);
	}

	public StockQuote(final URL wsdlLocation, final QName serviceName) {
		super(wsdlLocation, serviceName);
	}

	/**
	 * 
	 * @return returns StockQuoteSoap
	 */
	@WebEndpoint(name = "StockQuoteSoap1")
	public StockQuoteSoap getStockQuoteSoap1() {
		return super.getPort(StockQuote.StockQuoteSoap1, StockQuoteSoap.class);
	}

	/**
	 * 
	 * @param features
	 *            A list of {@link javax.xml.ws.WebServiceFeature} to configure
	 *            on the proxy. Supported features not in the
	 *            <code>features</code> parameter will have their default
	 *            values.
	 * @return returns StockQuoteSoap
	 */
	@WebEndpoint(name = "StockQuoteSoap1")
	public StockQuoteSoap getStockQuoteSoap1(
			final WebServiceFeature... features) {
		return super.getPort(StockQuote.StockQuoteSoap1, StockQuoteSoap.class,
				features);
	}

	/**
	 * 
	 * @return returns StockQuoteSoap
	 */
	@WebEndpoint(name = "StockQuoteSoap2")
	public StockQuoteSoap getStockQuoteSoap2() {
		return super.getPort(StockQuote.StockQuoteSoap2, StockQuoteSoap.class);
	}

	/**
	 * 
	 * @param features
	 *            A list of {@link javax.xml.ws.WebServiceFeature} to configure
	 *            on the proxy. Supported features not in the
	 *            <code>features</code> parameter will have their default
	 *            values.
	 * @return returns StockQuoteSoap
	 */
	@WebEndpoint(name = "StockQuoteSoap2")
	public StockQuoteSoap getStockQuoteSoap2(
			final WebServiceFeature... features) {
		return super.getPort(StockQuote.StockQuoteSoap2, StockQuoteSoap.class,
				features);
	}

	/**
	 * 
	 * @return returns StockQuoteSoap
	 */
	@WebEndpoint(name = "StockQuoteSoap3")
	public StockQuoteSoap getStockQuoteSoap3() {
		return super.getPort(StockQuote.StockQuoteSoap3, StockQuoteSoap.class);
	}

	/**
	 * 
	 * @param features
	 *            A list of {@link javax.xml.ws.WebServiceFeature} to configure
	 *            on the proxy. Supported features not in the
	 *            <code>features</code> parameter will have their default
	 *            values.
	 * @return returns StockQuoteSoap
	 */
	@WebEndpoint(name = "StockQuoteSoap3")
	public StockQuoteSoap getStockQuoteSoap3(
			final WebServiceFeature... features) {
		return super.getPort(StockQuote.StockQuoteSoap3, StockQuoteSoap.class,
				features);
	}

}
