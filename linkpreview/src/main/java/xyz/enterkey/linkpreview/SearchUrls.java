package xyz.enterkey.linkpreview;

import android.text.util.Linkify;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchUrls {

	public static final int ALL = 0;
	public static final int FIRST = 1;
	public static final Pattern PATTERN= PatternClass2.AUTOLINK_WEB_URL;
	/** It finds urls inside the text and return the matched ones */
	public static ArrayList<String> matches(String text) {
		return matches(text, ALL);
	}

	/** It finds urls inside the text and return the matched ones */
	public static ArrayList<String> matches(String text, int results) {

		ArrayList<String> urls = new ArrayList<String>();
		text=text.replace("\n"," ");
		String[] splitString = (text.split(" "));

		Linkify.MatchFilter matchFilter=Linkify.sUrlMatchFilter;

		for (String string : splitString) {
			Matcher matcher = PATTERN.matcher(string);
			while (matcher.find()) {
				int start = matcher.start();
				int end = matcher.end();
				String url=string.substring(start,end);

				if(url.contains("http://")|url.contains("https://")){url=url;}else{url="http://"+url;}
				urls.add(url);

			}
			if (results == FIRST && urls.size() > 0)
			break;
		}
		return urls;
	}

}
