/* SchoolPlanner4Untis - Android app to manage your Untis timetable
    Copyright (C) 2011  Mathias Kub <mail@makubi.at>
						Gerald Schreiber <mail@gerald-schreiber.at>
						Philip Woelfel <philip@woelfel.at>
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package edu.htl3r.schoolplanner.backend.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.util.Log;

/**
 * 
 * Direkter Netzwerkzugriff ueber URL.
 * 
 */
public class Network implements NetworkAccess {
	
	private HttpClient client;

	// TODO: No duplicate url entries
	private String serverUrl;
	private String httpsServerUrl;
	
	private URI url;
	private URI httpsUrl;
	
	private String jsessionid;
		
	public Network() {
		HttpParams params = new BasicHttpParams();
		
		// TODO: Timeouts sind statisch
		HttpConnectionParams.setConnectionTimeout(params, 8000);
		HttpConnectionParams.setSoTimeout(params, 5000);
		
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
		HttpProtocolParams.setUseExpectContinue(params, true);
		
		SchemeRegistry registry = new SchemeRegistry();
		
		ClientConnectionManager connman = new ThreadSafeClientConnManager(params, registry);
		
		client = new DefaultHttpClient(connman, params);
	}
	
	@Override
	public String getResponse(String request) throws IOException {	
		String response;
		try {
			response = executeRequest(request);
		}
		catch (SocketTimeoutException e) {
			Log.w("Network", "Socket timed out during network access.",e);
			throw new SocketTimeoutException("Socket timed out during network access.");
		}
		catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(e.getMessage());
		}
		catch (SSLException e) {
			Log.d("Network","ERROR",e);
			// TODO: Import untrusted SSL certificates / add untrusted CAs
			Log.d("Network","No SSL available, switching to http");
			httpsUrl = url;
			response = getResponse(request);
		}
		catch (UnknownHostException e) {
			Log.d("Network","Unknown host exception occured, URL: "+url+", host: "+url.getHost());
			throw new UnknownHostException("Unable to resolve host: "+url.getHost());
		}
		
		return response;
	}
	
	private String executeRequest(String request) throws ClientProtocolException, UnknownHostException, IOException {
		HttpPost httpRequest = new HttpPost(httpsUrl);

		if (jsessionid != null) {
			httpRequest.addHeader("Cookie", "JSESSIONID=" + jsessionid);
			Log.d("Network",
					"Added header: "
							+ httpRequest.getHeaders("Cookie")[0].toString());
		}

		StringEntity entity = new StringEntity(request, "UTF-8");
		httpRequest.setEntity(entity);

		HttpResponse httpResponse  = null;
		String response;

		httpResponse = client.execute(httpRequest);
		Log.d("Network", "Sent: " + request);
		
		ByteArrayOutputStream body = new ByteArrayOutputStream();
		httpResponse.getEntity().writeTo(body);
		response = body.toString();
		
		Log.d("Network", "Got status: " + httpResponse.getStatusLine());
		Log.d("Network", "Got body: " + response);
		
		return response;
	}
	
	private void registerScheme() {
		client.getConnectionManager().getSchemeRegistry().register(new Scheme("http", PlainSocketFactory.getSocketFactory(), url != null && url.getPort() != -1 ? url.getPort() : 80));
		
		// trust all certs
		client.getConnectionManager().getSchemeRegistry().register(new Scheme("https", new EasySSLSocketFactory(), httpsUrl != null && httpsUrl.getPort() != -1 ? httpsUrl.getPort() : 443));
	}

	@Override
	public void setSchool(String school) {
		try {
			url = new URI(serverUrl + "?school=" + school);
			httpsUrl = new URI(httpsServerUrl + "?school=" + school);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void setJsessionid(String jsessionid) {
		this.jsessionid = jsessionid;
	}

	@Override
	public void setServerUrl(String serverUrl) {
		 this.serverUrl = "http://"+serverUrl+"/WebUntis/jsonrpc.do";
		 this.httpsServerUrl = "https://"+serverUrl+"/WebUntis/jsonrpc.do";
		 registerScheme();
	}
	
}
