package sd.action;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class EbayAPI {
    public String APP_NAME = "AntnioSi-sd2016-PRD-245f30466-99727ed0";
    public Map.Entry<String, String> getLowestPrice(String item_title) {
        String url_with_params = "http://svcs.ebay.com/services/search/FindingService/v1"
                +"?"+
                "OPERATION-NAME=findItemsByKeywords"+
                "&" +
                "SERVICE-VERSION=1.0.0" +
                "&" +
                "SECURITY-APPNAME=" + this.APP_NAME +
                "&" +
                "RESPONSE-DATA-FORMAT=JSON" +
                "&" +
                "REST-PAYLOAD" +
                "&" +
                "sortOrder=PricePlusShippingLowest" +
                "&" +
                "paginationInput.entriesPerPage=1" +
                "&" + "keywords=";

        ArrayList<String> words = new ArrayList<String>(Arrays.asList(item_title.split(" ")));
        int i=0;
        int n_words = words.size();
        for (String word : words){
            url_with_params += word;
            if (i<n_words-1){
                url_with_params +="%20";
            }
            i++;
        }

        try{
            URL eBayFindingAPI = new URL(url_with_params);
            HttpURLConnection conn = (HttpURLConnection) eBayFindingAPI.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String input;
            int responseCode=conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                //while ((inputLine = in.readLine()) != null)
                input = in.readLine();
                in.close();
                JSONParser parser = new JSONParser();
                JSONObject response = (JSONObject) parser.parse(input);
                System.out.println(response.toString());
                JSONArray findItemsByKeywordsResponse = (JSONArray)response.get("findItemsByKeywordsResponse");
                JSONObject results = (JSONObject)findItemsByKeywordsResponse.get(0);
                JSONArray searchResult = (JSONArray)results.get("searchResult");
                JSONObject first_searchResult = (JSONObject)searchResult.get(0);
                JSONArray items = (JSONArray)first_searchResult.get("item");
                JSONObject item = (JSONObject)items.get(0);
                JSONArray listingInfo = (JSONArray)item.get("listingInfo");
                JSONObject first_listingInfo = (JSONObject)listingInfo.get(0);
                JSONArray buyItNowAvailable = (JSONArray)first_listingInfo.get("buyItNowAvailable");
                String value;
                if((buyItNowAvailable.get(0)).equals("true")){
                    JSONArray buyItNowPrice = (JSONArray)first_listingInfo.get("buyItNowPrice");
                    JSONObject first_buyItNowPrice = (JSONObject)buyItNowPrice.get(0);
                    value = (String)first_buyItNowPrice.get("__value__");
                }else{
                    JSONArray sellingStatus = (JSONArray)item.get("sellingStatus");
                    JSONObject first_sellingStatus= (JSONObject)sellingStatus.get(0);
                    JSONArray currentPrice = (JSONArray)first_sellingStatus.get("currentPrice");
                    JSONObject first_currentPrice = (JSONObject)currentPrice.get(0);
                    value = (String)first_currentPrice.get("__value__");
                }
                System.out.println(value);

                JSONArray viewItemURL = (JSONArray)item.get("viewItemURL");
                String itemURL = (String)viewItemURL.get(0);
                System.out.println(itemURL);

                return new AbstractMap.SimpleEntry<>(value, itemURL);
            }
        } catch(Exception e){
            e.printStackTrace();
            System.out.println("Problem with eBay API!");
        }
        return null;
    }
}
