package xyz.bnayagrawal.android.kat.util;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

import xyz.bnayagrawal.android.kat.data.Torrent;
import xyz.bnayagrawal.android.kat.data.TorrentDetails;
import xyz.bnayagrawal.android.kat.net.Url;

import static xyz.bnayagrawal.android.kat.net.Url.Builder.buildMagnetLink;

/**
 * Created by bnayagrawal on 14/3/18.
 */

public class KatDocumentUtil {
    private static final String TAG = KatDocumentUtil.class.getSimpleName();

    public static ArrayList<Torrent> getTorrentList(String rawHtml) {
        ArrayList<Torrent> torrents = new ArrayList<>();
        Document document = Jsoup.parse(rawHtml);
        try {
            Elements searchTable = document.select("table#mainSearchTable");
            Elements tableRows = searchTable.select("tr.odd");

            //Empty search result
            if(tableRows.html().length() == 0)
                return null;

            String name,age,size,detailsPageUrl,categoryImageLink,uploader,magnetLink,postedBy;
            int seeds, peers;

            int improperClosingTagIndex;
            String brokedHtml,repairedElements;
            Elements elements,nestedElements;

            for(Element row: tableRows) {
                elements = row.select("td > div");

                //Category image url
                nestedElements = elements.get(0).getElementsByTag("img");
                categoryImageLink = nestedElements.get(0).attr("abs:src");

                //Torrent name and details page url
                nestedElements = elements.get(0).select("a.cellMainLink");
                name = nestedElements.text();
                detailsPageUrl = nestedElements.attr("href");

                //magnet link
                nestedElements = elements.get(0).select("div.iaconbox");
                magnetLink = nestedElements.select("a[href^=magnet]").attr("href");

                //Posted by
                nestedElements = elements.select("span");
                postedBy = nestedElements.text();

                //Size, age and seeds
                nestedElements = row.getElementsByTag("td").select("td.center");
                size = nestedElements.get(0).text();
                age = nestedElements.get(1).text();
                seeds = Integer.parseInt(nestedElements.get(2).text());
                peers = Integer.parseInt(nestedElements.get(3).text());

                torrents.add(new Torrent(
                   name,postedBy,detailsPageUrl,age,size,seeds,peers
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
            torrents = null;
        }
        return torrents;
    }

    public static TorrentDetails getTorrentDetails(String rawHtml) {
        Document document = Jsoup.parse(rawHtml);
        try {
            String hash = document.select("div.infohash").select("span").text();
            String trackers = document.select("textarea#utorrent_textarea").text();
            String description = document.select("div#description").text();
            if(description.length() == 0) description = "No description provided!";
            String[] trackerList = trackers.split("\n");
            String magnetLink = buildMagnetLink(trackerList,hash);
            return new TorrentDetails(hash,trackers,description,magnetLink);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
