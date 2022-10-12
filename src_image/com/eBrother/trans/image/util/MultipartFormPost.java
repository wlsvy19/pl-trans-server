package com.eBrother.trans.image.util;

import com.eBrother.trans.image.ImageTransServer632;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class MultipartFormPost {


    protected static Logger _log = Logger.getLogger(MultipartFormPost.class.getName());

    public static String call ( String svr, int port, byte [] payLoad, String carNo, byte [] carBcdNo, String client_ip ) throws IOException {

        String respJson = "";

        CloseableHttpClient httpclient = HttpClients.createDefault();

        try {

            // call URL :
            HttpPost httppost = new HttpPost("http://" + svr + ":" + port + "/image?cr_num=" + carNo + "&client_ip=" + client_ip );

            // JSON ...
            httppost.addHeader("Content-type", "application/json");

            HttpEntity reqEntity = EntityBuilder.create()
                    .setBinary( payLoad )
                    .build();

            httppost.setEntity(reqEntity);

            _log.info ("executing request " + httppost.getRequestLine());

            CloseableHttpResponse response = httpclient.execute(httppost);

            try {

                _log.info ("-------" + response.getStatusLine() + " ---------------------------------");
//                System.out.println(response.headerIterator());

                HeaderIterator headerIterator = response.headerIterator( );
                HeaderElementIterator elementIterator = new BasicHeaderElementIterator(headerIterator);

//                while (elementIterator.hasNext()) {
//                    HeaderElement element = elementIterator.nextElement();
//                    String param = element.getName();
//                    String value = element.getValue();
//                    System.out.println("Header -> " + param + " = " + value );
//
//                }


                HttpEntity resEntity = response.getEntity();

                if (resEntity != null) {
                    _log.info  ("Response content length: " +    resEntity.getContentLength());
                }
                respJson = StringEscapeUtils.unescapeJava(EntityUtils.toString ( resEntity, "EUC-KR" ));

            } catch (IOException e) {
                _log.info ( e.getMessage());
            } finally {
                response.close();
            }
        } catch (ClientProtocolException e) {
            _log.info ( e.getMessage());
        } catch (IOException e) {
            _log.info ( e.getMessage());
        } finally {
            try {
                httpclient.close();
            }
            catch ( Exception eee ) {

            }
        }

        return respJson;

    }

    public static void  main(String[] args) throws IOException {

        try {

            File file = new File ( "/Users/demo860/app/dev/hipass/pl-trans-server/docu/temp.jpg");

            String respJson = call( "1.237.226.206", 5001, Files.readAllBytes( file.toPath()), "11루1960", null, "127.0.0.1");

            // respJson = StringEscapeUtils.unescapeJava(respJson);

            ObjectMapper mapper = new ObjectMapper();

            JsonNode root = mapper.readTree(respJson);

            JsonNode fields = root.get("platenum");


            System.out.println ( fields.getTextValue() + " = " + fields.get("platenum") + " ==> "  + respJson );
        }

        catch ( Exception e ) {

        }
    }

}
