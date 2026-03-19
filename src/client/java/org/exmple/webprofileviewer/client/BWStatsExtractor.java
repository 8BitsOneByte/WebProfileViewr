package org.exmple.webprofileviewer.client;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class BWStatsExtractor {
    public BWStats extractBWStats(String playername) throws Exception {
        String url = "https://hypixel.net/player/" + playername;
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .get();

        String finalKD4v4 = GlobalConstants.NOT_FOUND;
        String finalKD2v2 = GlobalConstants.NOT_FOUND;
        String totalWins = GlobalConstants.NOT_FOUND;
        String finalKD = GlobalConstants.NOT_FOUND;

        for (Element tdName : doc.select("#stats-content-bedwars td.statName")) {
            String nameText = tdName.text().trim();
            Element tr = tdName.parent();
            if (tr == null) continue;
            Element tdValue = tr.selectFirst("td.statValue");
            if (tdValue == null) continue;

            String value = tdValue.text();

            if (BWStatsConstants.BWCONST_4V4V4V4_FINAL_KD.equalsIgnoreCase(nameText) && GlobalConstants.NOT_FOUND.equals(finalKD4v4)) {
                finalKD4v4 = value;
            } else if (BWStatsConstants.BWCONST_DOUBLES_FINAL_KD.equalsIgnoreCase(nameText) && GlobalConstants.NOT_FOUND.equals(finalKD2v2)) {
                finalKD2v2 = value;
            } else if (BWStatsConstants.BWCONST_TOTALWINS.equalsIgnoreCase(nameText) && GlobalConstants.NOT_FOUND.equals(totalWins)) {
                totalWins = value;
            } else if (BWStatsConstants.BWCONST_FINAL_KD.equalsIgnoreCase(nameText) && GlobalConstants.NOT_FOUND.equals(finalKD)) {
                finalKD = value;
            }

            if (!GlobalConstants.NOT_FOUND.equals(finalKD4v4) && !GlobalConstants.NOT_FOUND.equals(finalKD2v2) && !GlobalConstants.NOT_FOUND.equals(totalWins) && !GlobalConstants.NOT_FOUND.equals(finalKD)) {
                break;
            }
        }

        return new BWStats(finalKD, finalKD2v2, finalKD4v4, totalWins);
    }

    public static class BWStats {
        private String finalKD;
        private String finalKD2v2;
        private String finalKD4v4;
        private String totalWins;

        public BWStats(String finalKD, String finalKD2v2, String finalKD4v4, String totalWins) {
            this.finalKD = finalKD;
            this.finalKD2v2 = finalKD2v2;
            this.finalKD4v4 = finalKD4v4;
            this.totalWins = totalWins;
        }

        public String getFinalKD() {
            return finalKD;
        }

        public String getFinalKD2v2() {
            return finalKD2v2;
        }

        public String getFinalKD4v4() {
            return finalKD4v4;
        }

        public String getTotalWins() {
            return totalWins;
        }
    }
}
