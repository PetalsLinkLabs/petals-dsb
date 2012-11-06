package org.ow2.petals.dsb.service.client.asynchttp;

import javax.xml.namespace.QName;

import org.petalslink.dsb.saaj.utils.SOAPMessageUtils;
import org.petalslink.dsb.service.client.ClientException;
import org.petalslink.dsb.service.client.Message;
import org.petalslink.dsb.service.client.MessageListener;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Response;

/**
 * @author chamerling
 * 
 */
public class Client implements org.petalslink.dsb.service.client.Client {

	private AsyncHttpClient client;

	/**
     * 
     */
	public Client() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.petalslink.dsb.service.client.Client#fireAndForget(org.petalslink
	 * .dsb.service.client.Message)
	 */
	public void fireAndForget(final Message message) throws ClientException {
		if (message == null) {
			throw new ClientException("Message can not be null...");
		}

		QName operation = message.getOperation();
		if (operation == null) {
			throw new ClientException("Operation can not be null...");
		}
		try {
			AsyncHttpClient client = getClient();
			client.preparePost(message.getEndpoint())
					.addHeader("SOAPAction", operation.getLocalPart())
					.setBody(
							SOAPMessageUtils.getSOAPMessageAsString(SOAPMessageUtils
									.createSOAPMessageFromBodyContent(message
											.getPayload())))
					.execute(new AsyncCompletionHandler<Response>() {

						@Override
						public Response onCompleted(Response response)
								throws Exception {
							return response;
						}

						@Override
						public void onThrowable(Throwable t) {
							System.out.println("Failto send message to "
									+ message.getEndpoint());
						}
					});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.petalslink.dsb.service.client.Client#sendReceive(org.petalslink.dsb
	 * .service.client.Message)
	 */
	public Message sendReceive(Message message) throws ClientException {
		Message result = null;
		if (message == null) {
			throw new ClientException("Message can not be null...");
		}

		QName operation = message.getOperation();
		if (operation == null) {
			throw new ClientException("Operation can not be null...");
		}
		try {
			AsyncHttpClient client = getClient();
			ListenableFuture<Response> future = client
					.preparePost(message.getEndpoint())
					.addHeader("SOAPAction", operation.getLocalPart())
					.setBody(
							SOAPMessageUtils.getSOAPMessageAsString(SOAPMessageUtils
									.createSOAPMessageFromBodyContent(message
											.getPayload()))).execute();
			String out = future.get().getResponseBody();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.petalslink.dsb.service.client.Client#sendAsync(org.petalslink.dsb
	 * .service.client.Message,
	 * org.petalslink.dsb.service.client.MessageListener)
	 */
	public void sendAsync(final Message message, final MessageListener listener)
			throws ClientException {
		if (message == null) {
			throw new ClientException("Message can not be null...");
		}

		QName operation = message.getOperation();
		if (operation == null) {
			throw new ClientException("Operation can not be null...");
		}
		try {
			AsyncHttpClient client = getClient();
			client.preparePost(message.getEndpoint())
					.addHeader("SOAPAction", operation.getLocalPart())
					.setBody(
							SOAPMessageUtils.getSOAPMessageAsString(SOAPMessageUtils
									.createSOAPMessageFromBodyContent(message
											.getPayload())))
					.execute(new AsyncCompletionHandler<Response>() {

						@Override
						public Response onCompleted(Response response)
								throws Exception {
							if (listener != null) {
								// TODO
								listener.onMessage(null);
							}
							return response;
						}

						@Override
						public void onThrowable(Throwable t) {
							System.out.println("Failto send message to "
									+ message.getEndpoint());
							if (listener != null) {
								listener.onError(t);
							}
						}
					});
		} catch (Exception e) {
			e.printStackTrace();
			if (listener != null) {
				listener.onError(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.petalslink.dsb.service.client.Client#getName()
	 */
	public String getName() {
		return "AsyncHTTPClient";
	}

	protected synchronized AsyncHttpClient getClient() {
		if (client == null) {
			client = new AsyncHttpClient(new AsyncHttpClientConfig.Builder()
					.setRequestTimeoutInMs(10000).build());
		}
		return client;
	}
}