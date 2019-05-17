/*
 * Copyright (c) 2011-2019 Pivotal Software Inc, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package reactor.netty;

import org.junit.rules.ExternalResource;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.mock.action.ExpectationResponseCallback;
import org.mockserver.model.HttpClassCallback;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.HttpStatusCode;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.notFoundResponse;
import static org.mockserver.model.HttpResponse.response;

/**
 * An utility class that sets up a "echo-testing" server, a bit like https://httpbin.org.
 * The HTTP server replies to all requests on the `/anything` endpoint with a representation
 * of the request it received, and on the `/status/{xxx}` endpoint with a response of the
 * xxx status.
 *
 * @author Simon Baslé
 */
public class HttpEchoTestingUtils extends ExternalResource {

	private ClientAndServer mockServer;
	private int             port;

	@Override
	protected void before() throws Throwable {
		start();
	}

	@Override
	protected void after() {
		stop();
	}

	public int getPort() {
		return port;
	}

	public void start() {
		port = SocketUtils.findAvailableTcpPort();
		mockServer = ClientAndServer.startClientAndServer(port);

		mockServer
				.when(request())
				.respond(HttpClassCallback.callback("reactor.netty.HttpEchoTestingUtils$EchoStatusCallback"));
	}

	public void stop() {
		mockServer.stop();
	}

	public static final class EchoStatusCallback implements ExpectationResponseCallback {

		@Override
		public HttpResponse handle(HttpRequest httpRequest) {
			String path = httpRequest.getPath()
			                         .getValue();
			if (path.startsWith("/status/")) {
				String status = path.substring(path.lastIndexOf('/') + 1);
				int statusCode = Integer.parseInt(status);
				return response().withStatusCode(statusCode)
						.withBody(httpRequest.toString());
			}
			if (path.equals("/anything")) {
				return response()
						.withStatusCode(HttpStatusCode.OK_200.code())
						.withBody(httpRequest.toString());
			}
			if (path.equals("/headers")) {
				return response()
						.withStatusCode(HttpStatusCode.OK_200.code())
						.withBody(httpRequest.getHeaders().toString());
			}

			return notFoundResponse();
		}
	}

}
