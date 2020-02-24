package xyz.enterkey.linkpreview;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class LinkPreview extends LinearLayout{

	private int urlColor;
	private int descriptionColor;
	private int titleColor;
	private int backgroundColor;
	private int behaviors;
	private Boolean withImage;
	private static final int BEHAVIOR_NONE = 2;
	String openUrl="http://www.secondeelam.com";
	private TextView layoutTitle;
	private TextView layoutUrl;
	private TextView layoutDescription;
	private ImageView layoutImage;
	private LinearLayout layoutContent;
	private ProgressBar layoutProgress;
	private CardView layoutCard;

	public static final int ALL = -1;
	public static final int NONE = -2;

	private final String HTTP_PROTOCOL = "http://";
	private final String HTTPS_PROTOCOL = "https://";


	private AsyncTask getCodeTask;

	public LinkPreview(Context context) {
		this(context, null);
	}

	public LinkPreview(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public LinkPreview(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs, defStyleAttr, 0);
	}
	private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		populateAttributes(context, attrs, defStyleAttr, defStyleRes);
		initializeViews();
	}

	private void populateAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		TypedArray ta = context.getTheme()
				.obtainStyledAttributes(attrs, R.styleable.LinkPreview, defStyleAttr, defStyleRes);
		try {
			behaviors = ta.getInteger(R.styleable.LinkPreview_lp_style, BEHAVIOR_NONE);
			descriptionColor =  ta.getColor(R.styleable.LinkPreview_lp_description_color, getResources().getColor(R.color.lp_description_color));
			backgroundColor = ta.getColor(R.styleable.LinkPreview_lp_background_color,getResources().getColor(R.color.lp_background_color));
			titleColor =  ta.getColor(R.styleable.LinkPreview_lp_title_color,getResources().getColor(R.color.lp_title_color));
			urlColor = ta.getColor(R.styleable.LinkPreview_lp_url_color,getResources().getColor(R.color.lp_url_color));
			withImage = ta.getBoolean(R.styleable.LinkPreview_lp_with_image, false);
		} finally {
			ta.recycle();
		}
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
	}

	private void initializeViews() {
		int width =  FrameLayout.LayoutParams.MATCH_PARENT;
		int height = FrameLayout.LayoutParams.WRAP_CONTENT;
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);

		setLayoutParams(params);

		View rootView = inflate(getContext(), behaviors==1 ? R.layout.linkpreview_material :
						behaviors==2 ? R.layout.linkpreview_classic : R.layout.linkpreview_potrait,this);
		rootView.setLayoutParams(params);

		layoutTitle = rootView.findViewById(R.id.title);
		layoutDescription = rootView.findViewById(R.id.description);
		layoutUrl = rootView.findViewById(R.id.url);
		layoutImage = rootView.findViewById(R.id.image_view);
		layoutContent = rootView.findViewById(R.id.layout);
		layoutProgress = rootView.findViewById(R.id.progressbar);
		layoutCard = rootView.findViewById(R.id.background);

		if(withImage){
			layoutImage.setVisibility(View.VISIBLE);
		}else{
			layoutImage.setVisibility(View.GONE);
		}
		layoutCard.setCardBackgroundColor(backgroundColor);
		layoutUrl.setTextColor(urlColor);
		layoutTitle.setTextColor(titleColor);
		layoutDescription.setTextColor(descriptionColor);

		layoutContent.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(openUrl));
				getContext().startActivity(i);
			}
		});

	}

	public void makePreview(String url) {
		makePreview( url, 1);
	}

	public void makePreview( String url, int imageQuantity) {
		cancel();
		ArrayList<String> tempUrl =SearchUrls.matches(url);
		if (tempUrl.size() > 0){
			String trimUrl= extendedTrim(tempUrl.get(0));
			getCodeTask = createPreviewGenerator(imageQuantity).execute(trimUrl);
		}

	}

	protected GetCode createPreviewGenerator(int imageQuantity) {
		return new GetCode(imageQuantity);
	}


	public void cancel(){
		if(getCodeTask != null){
			getCodeTask.cancel(true);
		}
	}

	private class GetCode extends AsyncTask<String, Void, Void> {

		private SourceContent sourceContent = new SourceContent();
		private int imageQuantity;
		private String execUrls;

		public GetCode(int imageQuantity) {
			this.imageQuantity = imageQuantity;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			layoutProgress.setVisibility(View.VISIBLE);
			layoutContent.setVisibility(View.GONE);

		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			String Surl=sourceContent.getCannonicalUrl();
			String Stitle=sourceContent.getTitle();
			String Sdescription=sourceContent.getDescription();
			if(!Surl.equals("")&&!Stitle.equals("")&&!Sdescription.equals("")){
				layoutUrl.setText(" "+sourceContent.getCannonicalUrl());
				layoutTitle.setText(sourceContent.getTitle());
				layoutDescription.setText(sourceContent.getDescription());

				if(withImage){
					if(sourceContent.getImages().size()>0){
						Glide.with(getContext()).load(sourceContent
								.getImages().get(0)).error(R.drawable.ic_android_black_24dp).into(layoutImage);
					}
				}
				layoutProgress.setVisibility(View.GONE);
				layoutContent.setVisibility(View.VISIBLE);
			}else{
				layoutProgress.setVisibility(View.GONE);
				layoutContent.setVisibility(View.GONE);
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected Void doInBackground(String... params) {
			execUrls = params[0];
			String finalUrl=unshortenUrl(execUrls);

			if(finalUrl != null){
				openUrl=finalUrl;
				sourceContent.setFinalUrl(finalUrl);
				if (isImage(sourceContent.getFinalUrl())
						&& !sourceContent.getFinalUrl().contains("dropbox")) {
					sourceContent.setSuccess(true);
					sourceContent.getImages().add(sourceContent.getFinalUrl());
					sourceContent.setTitle("");
					sourceContent.setDescription("");
				} else {
					try {
						Document doc = getDocument();
						sourceContent.setHtmlCode(extendedTrim(doc.toString()));
						HashMap<String, String> metaTags = getMetaTags(sourceContent
								.getHtmlCode());
						sourceContent.setMetaTags(metaTags);
						sourceContent.setTitle(metaTags.get("title"));
						sourceContent.setDescription(metaTags
								.get("description"));

						if (sourceContent.getTitle().equals("")) {
							String matchTitle = Regex.pregMatch(
									sourceContent.getHtmlCode(),
									Regex.TITLE_PATTERN, 2);

							if (!matchTitle.equals(""))
								sourceContent.setTitle(htmlDecode(matchTitle));
						}

						if (sourceContent.getDescription().equals(""))
							sourceContent
									.setDescription(crawlCode(sourceContent
											.getHtmlCode()));

						sourceContent.setDescription(sourceContent
								.getDescription().replaceAll(
										Regex.SCRIPT_PATTERN, ""));

						if (imageQuantity != NONE) {
							if (!metaTags.get("image").equals(""))
								sourceContent.getImages().add(
										metaTags.get("image"));
							else {
								sourceContent.setImages(getImages(doc,
										imageQuantity));
							}
						}

						sourceContent.setSuccess(true);
					} catch (Throwable t) {
						sourceContent.setSuccess(false);
					}
				}
				String[] finalLinkSet = sourceContent.getFinalUrl().split("&");
				sourceContent.setUrl(finalLinkSet[0]);
				sourceContent.setCannonicalUrl(cannonicalPage(sourceContent
						.getFinalUrl()));
				sourceContent.setDescription(stripTags(sourceContent
						.getDescription()));
			}

			return null;
		}

		private String unshortenUrl(String shortURL) {
			if (!shortURL.startsWith(HTTP_PROTOCOL)
					&& !shortURL.startsWith(HTTPS_PROTOCOL))
				return null;
			URLConnection urlConn = connectURL(shortURL);
			if(urlConn!=null){
				urlConn.getHeaderFields();
				return urlConn.getURL().toString();
			}else{
				return null;
			}
		}

		private String stripTags(String content) {
			return Jsoup.parse(content).text();
		}

		private String htmlDecode(String content) {
			return Jsoup.parse(content).text();
		}

		private String cannonicalPage(String url) {

			String cannonical = "";
			if (url.startsWith(HTTP_PROTOCOL)) {
				url = url.substring(HTTP_PROTOCOL.length());
			} else if (url.startsWith(HTTPS_PROTOCOL)) {
				url = url.substring(HTTPS_PROTOCOL.length());
			}

			int urlLength = url.length();
			for (int i = 0; i < urlLength; i++) {
				if(getCodeTask.isCancelled()){
					break;
				}
				if (url.charAt(i) != '/')
					cannonical += url.charAt(i);
				else
					break;
			}

			return cannonical;

		}


		private String crawlCode(String content) {
			String result = "";
			String resultSpan = "";
			String resultParagraph = "";
			String resultDiv = "";

			resultSpan = getTagContent("span", content);
			resultParagraph = getTagContent("p", content);
			resultDiv = getTagContent("div", content);

			result = resultSpan;

			if (resultParagraph.length() > resultSpan.length()
					&& resultParagraph.length() >= resultDiv.length())
				result = resultParagraph;
			else if (resultParagraph.length() > resultSpan.length()
					&& resultParagraph.length() < resultDiv.length())
				result = resultDiv;
			else
				result = resultParagraph;

			return htmlDecode(result);
		}

		private boolean isImage(String url) {
			return url.matches(Regex.IMAGE_PATTERN);
		}

		protected Document getDocument() throws IOException {
			return Jsoup.connect(sourceContent.getFinalUrl()).userAgent("Mozilla").get();
		}

		private List<String> getImages(Document document, int imageQuantity) {
			List<String> matches = new ArrayList<String>();

			Elements media = document.select("[src]");

			for (Element srcElement : media) {
				if(getCodeTask.isCancelled()){
					break;
				}
				if (srcElement.tagName().equals("img")) {
					matches.add(srcElement.attr("abs:src"));
				}
			}

			if (imageQuantity != ALL)
				matches = matches.subList(0, imageQuantity);

			return matches;
		}


		private HashMap<String, String> getMetaTags(String content) {
			HashMap<String, String> metaTags = new HashMap<String, String>();
			metaTags.put("url", "");
			metaTags.put("title", "");
			metaTags.put("description", "");
			metaTags.put("image", "");

			List<String> matches = Regex.pregMatchAll(content,
					Regex.METATAG_PATTERN, 1);

			for (String match : matches) {
				if(getCodeTask.isCancelled()){
					break;
				}
				final String lowerCase = match.toLowerCase();
				if (lowerCase.contains("property=\"og:url\"")
						|| lowerCase.contains("property='og:url'")
						|| lowerCase.contains("name=\"url\"")
						|| lowerCase.contains("name='url'"))
					updateMetaTag(metaTags, "url", separeMetaTagsContent(match));
				else if (lowerCase.contains("property=\"og:title\"")
						|| lowerCase.contains("property='og:title'")
						|| lowerCase.contains("name=\"title\"")
						|| lowerCase.contains("name='title'"))
					updateMetaTag(metaTags, "title", separeMetaTagsContent(match));
				else if (lowerCase
						.contains("property=\"og:description\"")
						|| lowerCase
						.contains("property='og:description'")
						|| lowerCase.contains("name=\"description\"")
						|| lowerCase.contains("name='description'"))
					updateMetaTag(metaTags, "description", separeMetaTagsContent(match));
				else if (lowerCase.contains("property=\"og:image\"")
						|| lowerCase.contains("property='og:image'")
						|| lowerCase.contains("name=\"image\"")
						|| lowerCase.contains("name='image'"))
					updateMetaTag(metaTags, "image", separeMetaTagsContent(match));
			}

			return metaTags;
		}

		/** Verifies if the content could not be retrieved */
		public boolean isNull() {
			return !sourceContent.isSuccess() &&
					extendedTrim(sourceContent.getHtmlCode()).equals("") &&
					!isImage(sourceContent.getFinalUrl());
		}

	}

	private String getTagContent(String tag, String content) {

		String pattern = "<" + tag + "(.*?)>(.*?)</" + tag + ">";
		String result = "", currentMatch = "";

		List<String> matches = Regex.pregMatchAll(content, pattern, 2);

		int matchesSize = matches.size();
		for (int i = 0; i < matchesSize; i++) {
			if(getCodeTask.isCancelled()){
				break;
			}
			currentMatch = stripTags(matches.get(i));
			if (currentMatch.length() >= 120) {
				result = extendedTrim(currentMatch);
				break;
			}
		}

		if (result.equals("")) {
			String matchFinal = Regex.pregMatch(content, pattern, 2);
			result = extendedTrim(matchFinal);
		}

		result = result.replaceAll("&nbsp;", "");

		return htmlDecode(result);
	}

	private List<String> getImages(Document document, int imageQuantity) {
		List<String> matches = new ArrayList<String>();

		Elements media = document.select("[src]");

		for (Element srcElement : media) {
			if(getCodeTask.isCancelled()){
				break;
			}
			if (srcElement.tagName().equals("img")) {
				matches.add(srcElement.attr("abs:src"));
			}
		}

		if (imageQuantity != ALL)
			matches = matches.subList(0, imageQuantity);

		return matches;
	}


	private HashMap<String, String> getMetaTags(String content) {
		HashMap<String, String> metaTags = new HashMap<String, String>();
		metaTags.put("url", "");
		metaTags.put("title", "");
		metaTags.put("description", "");
		metaTags.put("image", "");

		List<String> matches = Regex.pregMatchAll(content,
				Regex.METATAG_PATTERN, 1);

		for (String match : matches) {
			if(getCodeTask.isCancelled()){
				break;
			}
			final String lowerCase = match.toLowerCase();
			if (lowerCase.contains("property=\"og:url\"")
					|| lowerCase.contains("property='og:url'")
					|| lowerCase.contains("name=\"url\"")
					|| lowerCase.contains("name='url'"))
				updateMetaTag(metaTags, "url", separeMetaTagsContent(match));
			else if (lowerCase.contains("property=\"og:title\"")
					|| lowerCase.contains("property='og:title'")
					|| lowerCase.contains("name=\"title\"")
					|| lowerCase.contains("name='title'"))
				updateMetaTag(metaTags, "title", separeMetaTagsContent(match));
			else if (lowerCase
					.contains("property=\"og:description\"")
					|| lowerCase
					.contains("property='og:description'")
					|| lowerCase.contains("name=\"description\"")
					|| lowerCase.contains("name='description'"))
				updateMetaTag(metaTags, "description", separeMetaTagsContent(match));
			else if (lowerCase.contains("property=\"og:image\"")
					|| lowerCase.contains("property='og:image'")
					|| lowerCase.contains("name=\"image\"")
					|| lowerCase.contains("name='image'"))
				updateMetaTag(metaTags, "image", separeMetaTagsContent(match));
		}

		return metaTags;
	}
	private void updateMetaTag(HashMap<String, String> metaTags, String url, String value) {
		if (value != null && (value.length() > 0)) {
			metaTags.put(url, value);
		}
	}
	private String separeMetaTagsContent(String content) {
		String result = Regex.pregMatch(content, Regex.METATAG_CONTENT_PATTERN,
				1);
		return htmlDecode(result);
	}


	private String stripTags(String content) {
		return Jsoup.parse(content).text();
	}

	private String htmlDecode(String content) {
		return Jsoup.parse(content).text();
	}

	private URLConnection connectURL(String strURL) {
		URLConnection conn = null;
		try {
			URL inputURL = new URL(strURL);
			conn = inputURL.openConnection();
		} catch (Exception ioe) {
//			Log.e("exception",ioe.toString());
		}
		return conn;
	}

	public static String extendedTrim(String content) {
		return content.replaceAll("\\s+", " ").replace("\n", " ")
				.replace("\r", " ").trim();
	}

	public void setWithImage(Boolean tempImage) {
		withImage = tempImage;
		if(withImage){
			layoutImage.setVisibility(View.VISIBLE);
		}else{
			layoutImage.setVisibility(View.GONE);
		}
	}
	public void setUrlColor(@ColorInt int color) {
		urlColor = color;
		layoutUrl.setTextColor(urlColor);
	}
	public void setTitleColor(@ColorInt int color) {
		titleColor = color;
		layoutTitle.setTextColor(titleColor);
	}
	public void setDescriptionColor(@ColorInt int color) {
		descriptionColor = color;
		layoutDescription.setTextColor(descriptionColor);
	}
	public void setPreviewBackgroundColor(@ColorInt int color) {
		backgroundColor = color;
		layoutCard.setCardBackgroundColor(backgroundColor);
	}
}
