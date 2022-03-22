package de.tobias.simpsocserv.utils;

import java.util.ArrayList;
import java.util.Arrays;

public class InternalPathMatcher {

    public static boolean isMatching(String uri, String pathMatcher) {
        return getURIWithoutRegex(uri, pathMatcher) != null;
    }

    public static String getURIWithoutRegex(String uri, String pathMatcher) {
        //System.out.println("Match: " + uri + " <--> " + pathMatcher);
        ArrayList<String> uriParts = new ArrayList<>(Arrays.asList(uri.split("/")));
        ArrayList<String> regexParts = new ArrayList<>(Arrays.asList(pathMatcher.split("/")));

        int lastMatching;
        int lastMatchingDiffs = 0;
        for(lastMatching = 0; lastMatching < regexParts.size(); lastMatching++) {
            String regexPart = regexParts.get(lastMatching);

            if(!regexPart.equalsIgnoreCase("*")) {
                if(lastMatching > uriParts.size()-1) {
                    //System.out.println("Match fail (index out): " + regexPart);
                    return null;
                }

                String uriPart = uriParts.get(lastMatching);
                if(!uriPart.equalsIgnoreCase(regexPart)) {
                    return null;
                }
            } else {
                lastMatchingDiffs = lastMatchingDiffs - 1;
            }
        }

        if (lastMatching == regexParts.size() && lastMatchingDiffs == 0) {
            return uri;
        }

        return String.join("/", uriParts.subList(lastMatching + lastMatchingDiffs, uriParts.size()));
    }
}
