package org.albedu.oauth2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class RedirectResponseHandler implements HttpHandler {
	private static final Logger logger = Logger.getLogger(RedirectResponseHandler.class);
	private static RedirectResponseHandler mRedirectResponseHandler = null;
	private HttpServer server = null;
	private ExecutorService httpThreadPool = null;

	private final Properties appProperties;
	private final String urlParamQueryKeyCode;
	private final String webFileRootDir;
	private final int numberOfThreadsInHTTPThreadPool;

	private RedirectResponseHandler() throws IOException {
		this.appProperties = Util.loadProperties(Constant.APP_PROPERTIES_FILE_PATH);
		this.urlParamQueryKeyCode = this.appProperties.getProperty(Constant.URL_PARAM_QUERY_KEY_CODE, "code");
		this.webFileRootDir = this.appProperties.getProperty(Constant.WEB_FILE_ROOT_DIR, "wwwroot");
		this.numberOfThreadsInHTTPThreadPool = Integer.parseInt(this.appProperties.getProperty(Constant.NUMBER_OF_THREADS_IN_HTTP_THREAD_POOL, "1"));
	}

	public static RedirectResponseHandler getInstance() throws IOException {
		if (mRedirectResponseHandler == null) {
			mRedirectResponseHandler = new RedirectResponseHandler();
		}

		return mRedirectResponseHandler;
	}

	public void startServer(String url) throws IOException, URISyntaxException, Exception {
		if (server == null) {
			URI uri = new URI(url);

			String host = uri.getHost();
			int port = uri.getPort();

			logger.debug("host: " + host);
			logger.debug("port: " + port);

			if (host != null && (host.equals("127.0.0.1") || host.equals("::1") || host.equals("localhost"))) {
				this.server = HttpServer.create(new InetSocketAddress(port), 0);
			} else {
				this.server = HttpServer.create(new InetSocketAddress(host, port), 0);
			}

			this.server.createContext("/", this);
			this.httpThreadPool = Executors.newFixedThreadPool(this.numberOfThreadsInHTTPThreadPool);
			this.server.setExecutor(this.httpThreadPool);
			this.server.start();

			logger.info("RedirectResponseHandler has started server successfully. ");
		}
	}

	public void stopServer() {
		if (this.server != null) {
			this.server.stop(0);
		}

		if (this.server != null) {
			try {
				this.httpThreadPool.shutdown();
				this.httpThreadPool.awaitTermination(2, TimeUnit.HOURS);
			} catch (Exception e) {
				logger.error(Util.stackTraceToString(e));
			}
		}

		this.server = null;
		this.httpThreadPool = null;
	}

	@Override
	public void handle(HttpExchange Exchange) throws IOException {
		String requestedFilePath = Exchange.getRequestURI().getPath();

		logger.debug("requestedFilePath: " + requestedFilePath);

		File requestedFile = new File(this.webFileRootDir + requestedFilePath);

		logger.debug("requestedFile.exists(): " + requestedFile.exists());
		logger.debug("requestedFile.isDirectory(): " + requestedFile.isDirectory());

		String responseURLParameters = Exchange.getRequestURI().getQuery();

		logger.debug("responseURLParameters: " + responseURLParameters);
		
		if (!requestedFile.exists() || requestedFile.isDirectory()) {
			if (responseURLParameters != null && !responseURLParameters.isEmpty()) {
				Exchange.getResponseHeaders().set("Location", "/index.html?" + responseURLParameters);
			} else {
				Exchange.getResponseHeaders().set("Location", "/index.html");
			}
			Exchange.sendResponseHeaders(302, -1);
		}

		if (requestedFilePath != null) {
			if (requestedFilePath.endsWith(".html")) {
				Exchange.getResponseHeaders().put("Content-Type", Collections.singletonList("text/html"));
			} else if (requestedFilePath.endsWith(".css")) {
				Exchange.getResponseHeaders().put("Content-Type", Collections.singletonList("text/css"));
			} else if (requestedFilePath.endsWith(".js")) {
				Exchange.getResponseHeaders().put("Content-Type", Collections.singletonList("text/javascript"));
			} else if (requestedFilePath.endsWith(".png")) {
				Exchange.getResponseHeaders().put("Content-Type", Collections.singletonList("image/png"));
			} else if (requestedFilePath.endsWith(".gif")) {
				Exchange.getResponseHeaders().put("Content-Type", Collections.singletonList("image/gif"));
			}
		}

		Exchange.sendResponseHeaders(200, 0);

		OutputStream objOutputStream = Exchange.getResponseBody();
		FileInputStream objFileInputStream = new FileInputStream(requestedFile);
		int count = 0;

		if (requestedFilePath != null && requestedFilePath.equals("/index.html")) {
			String code = Util.queryToMap(responseURLParameters).get(this.urlParamQueryKeyCode);

			logger.debug("code: " + code);
			
			if (code == null) {
				code = "ERROR. Unable to retrieve verification code. ";
			}

			StringBuffer htmlFileContent = new StringBuffer("");

			byte[] buffer = new byte[1024];

			while ((count = objFileInputStream.read(buffer)) != -1) {
				htmlFileContent.append(new String(buffer, 0, count));
			}

			String finalHTMLFileContent = htmlFileContent.toString();

			finalHTMLFileContent = MessageFormat.format(finalHTMLFileContent, code);

			objOutputStream.write(finalHTMLFileContent.getBytes());
		} else {
			final byte[] buffer = new byte[0x10000];
			while ((count = objFileInputStream.read(buffer)) >= 0) {
				objOutputStream.write(buffer, 0, count);
			}
		}

		objOutputStream.flush();
		objOutputStream.close();
		objFileInputStream.close();
	}
}
