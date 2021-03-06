package org.ohdsi.webapi.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.DataAccessConfig;
import org.ohdsi.webapi.evidence.EvidenceInfo;
import org.ohdsi.webapi.evidence.RdfInfo;
import org.ohdsi.webapi.helper.ResourceHelper;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;


@Path("sparql/")
@Component
public class SparqlService {
	
	@Autowired
	  private Environment env;
	
	@GET
	  @Path("source")
	  @Produces(MediaType.APPLICATION_JSON)
	  public Collection<RdfInfo> getInfo() throws JSONException {
		
		String query = ResourceHelper.GetResourceAsString("/resources/evidence/sparql/info.sparql");
		String uriQuery = null;
		String sparqlEndpoint = this.env.getRequiredProperty("sparql.endpoint");
		query = sparqlEndpoint + query;
		try {
			uriQuery = URIUtil.encodeQuery(query);
		} catch (URIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		uriQuery = uriQuery + "&format=application%2Fsparql-results%2Bjson";
	    List<RdfInfo> infoOnSources = new ArrayList<RdfInfo>();
	    infoOnSources = readJSONFeed(uriQuery);
	    
	    return infoOnSources;
	  }
	
	
	
	//readJSON function
	private static List<RdfInfo> readJSONFeed(String URL) throws JSONException {
	    StringBuilder stringBuilder = new StringBuilder();
	    HttpClient httpClient = new DefaultHttpClient();
	    HttpGet httpGet = new HttpGet(URL);
	    List<RdfInfo> infoOnSources = new ArrayList<RdfInfo>();

	    try {

	        HttpResponse response = httpClient.execute(httpGet);
	        StatusLine statusLine = response.getStatusLine();
	        int statusCode = statusLine.getStatusCode();

	        if (statusCode == 200) {

	            HttpEntity entity = response.getEntity();
	            InputStream inputStream = entity.getContent();
	            BufferedReader reader = new BufferedReader(
	                    new InputStreamReader(inputStream));
	            String line;
	            while ((line = reader.readLine()) != null) {
	                stringBuilder.append(line);
	            }
	            inputStream.close();

	        }
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	    JSONObject jsonObj = new JSONObject(stringBuilder.toString());
	    JSONArray lineItems = jsonObj.getJSONObject("results").getJSONArray("bindings");
	    for (int i = 0; i < lineItems.length(); ++i) {
	        JSONObject tempItem = lineItems.getJSONObject(i);
	        JSONObject tempSource = tempItem.getJSONObject("sourceDocument");
	        String source = tempSource.getString("value");
	        RdfInfo info = new RdfInfo();
	        info.sourceDocument = source;
	        infoOnSources.add(info);
	    }
	    return infoOnSources;
	}
}
