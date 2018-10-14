package org.fhcrc.www.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import com.day.cq.wcm.api.Page;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {

	public static int PAGE_TITLE_LEVEL = 0;
	public static int NAV_TITLE_LEVEL = 1;
	private final static Logger LOGGER = LoggerFactory.getLogger(Utils.class);
	
	/**
	 * Returns a valid URL for use in HTML attributes (e.g. an anchor's
	 * href attribute). If an AEM is passed in, it adds '.html' to
	 * the end of the path. If a non-aem path is passed in with no URL prefix,
	 * 'https://' is prepended to it. If a URL starting with one of the 
	 * allowedPrefixes is sent in, it is simply returned.
	 */
	public static String cleanLink(String path) {
		
		if (path == null) {
			LOGGER.error("cleanLink was passed a null path");
			return "";
		}
		
		String[] allowedPrefixes = {"http://", "https://", "mailto:", "tel:"};
		
		StringBuffer sb = new StringBuffer();
		
		/* If the path is a fully qualified URL, just return it */
		for (String prefix : allowedPrefixes) {
			
			if (path.startsWith(prefix)) {
				
				LOGGER.debug("cleanLink found allowed prefix: {} on path {}", prefix, path);
				return path;

			}
			
		}
		
		/* If it is an internal link, add '.html' to the end */
		if (path.startsWith(Constants.INTERNAL_LINK_PREFIX)) {
			
			LOGGER.debug("cleanLink found internal link: {}", path);
			sb.append(path);
			sb.append(".html");
			
		} else {

			LOGGER.debug("cleanLink found external link: {}. Prepending with scheme {}", path, Constants.SECURE_URL_PREFIX);
			/* Assuming this is a URL with no prefix, e.g. 'www.example.com' */
			sb.append(Constants.SECURE_URL_PREFIX);
			sb.append(path);
			
		}
		
		return sb.toString();
		
	}

	/**
	 * Returns the title of a page. It checks a page's properties in this 
	 * order: Navigation Title > Page Title > Title. The pageLevel 
	 * determines if the method should start at Navigation Title or Page
	 * Title in this check.
	 */
	public static String getTitle(Page p, int pageLevel) throws IllegalArgumentException {

		if (p == null) {

			throw new IllegalArgumentException();

		}

		String pageTitle = null;

		if (pageLevel == PAGE_TITLE_LEVEL) {

			LOGGER.debug("getTitle attempting to return Page Title");
			if (p.getPageTitle() != null && !p.getPageTitle().trim().equals("")) {
				pageTitle = p.getPageTitle();
				LOGGER.debug("Page Title found: {}", pageTitle);
			} else {
				pageTitle = p.getTitle();
				LOGGER.debug("No Page Title found. Using Title instead: {}", pageTitle);
			}
		} else if (pageLevel == NAV_TITLE_LEVEL) {

			LOGGER.debug("getTitle attempting to return Navigation Title");
			if (p.getNavigationTitle() != null && !p.getNavigationTitle().trim().equals("")) {
				pageTitle = p.getNavigationTitle();
				LOGGER.debug("Navigation Title found: {}", pageTitle);
			} else if (p.getPageTitle() != null && !p.getPageTitle().trim().equals("")) {
				pageTitle = p.getPageTitle();
				LOGGER.debug("No Navigation Title found. Using Page Title instead: {}", pageTitle);
			} else {
				pageTitle = p.getTitle();
				LOGGER.debug("No Navigation Title or Page Title found. Using Title instead: {}", pageTitle);
			}

		} else {

			LOGGER.warn("getTitle was passed a pageLevel that was not expected: {}", pageLevel);
			pageTitle = p.getTitle();

		}

		if (pageTitle == null) {

			LOGGER.error("getTitle function could not find a title for this page: {}", p.getPath());
			return "";

		}

		return pageTitle;

	}

	/**
	 * Returns a page's fully-qualified canonical URL
	 */
	public static String getCanonicalURL(ResourceResolver resourceResolver, SlingHttpServletRequest request, Page p) throws IllegalArgumentException {

		if (resourceResolver == null || request == null || p == null) {

			throw new IllegalArgumentException();

		}

		LOGGER.debug("Creating Canonical URL for page {}", p.getPath());
		StringBuffer sb = new StringBuffer();

		sb.append(Constants.SECURE_URL_PREFIX);
		sb.append(Constants.WWW_DOMAIN);
		sb.append(resourceResolver.map(request, p.getPath()));
		sb.append(".");
		sb.append(request.getRequestPathInfo().getExtension());

		return sb.toString();

	}

	/**
	 * Formats a phone number for display on a page. Only formats strings that
	 * contain either 10 or 11 digits currently. If the input cannot be
	 * correctly formatted, the original string is returned.
	 * @param phoneNumber
	 * @return String formatted to be xxx.xxx.xxxx or x.xxx.xxx.xxxx
	 */
	public static String formatPhoneNumber(String phoneNumber) {

		if (phoneNumber == null) {

			LOGGER.warn("formatPhoneNumber was passed a null value");
			return "";

		}

		final String TEL_DELIMITER = ".";

		String phoneNumberJustDigits = removeNonDigits(phoneNumber);
		StringBuffer sb = new StringBuffer();

		if (phoneNumberJustDigits.length() == 10) {

			sb.append(phoneNumberJustDigits.substring(0, 3));
			sb.append(TEL_DELIMITER);
			sb.append(phoneNumberJustDigits.substring(3, 6));
			sb.append(TEL_DELIMITER);
			sb.append(phoneNumberJustDigits.substring(6));
			LOGGER.debug("10-digit telephone number found: {}", sb.toString());


		} else if (phoneNumberJustDigits.length() == 11) {

			sb.append(phoneNumberJustDigits.substring(0, 1));
			sb.append(TEL_DELIMITER);
			sb.append(phoneNumberJustDigits.substring(1, 4));
			sb.append(TEL_DELIMITER);
			sb.append(phoneNumberJustDigits.substring(4, 7));
			sb.append(TEL_DELIMITER);
			sb.append(phoneNumberJustDigits.substring(7));
			LOGGER.debug("11-digit telephone number found: {}", sb.toString());

		} else {

			LOGGER.warn("Phone number did not have 10-11 digits: {}", phoneNumber);
			return phoneNumber;

		}

		return sb.toString();

	}

	/**
	 * Formats a phone number for use in the href attribute of an a tag. 
	 * Currently only formats US phone numbers.
	 * @param phoneNumber
	 * @return telephone number href attribute
	 */
	public static String formatPhoneLink(String phoneNumber) {

		if (phoneNumber == null) {

			LOGGER.warn("formatPhoneLink received null phone number");
			return "";

		}

		final String TEL_DELIMITER = "-";
		final String US_COUNTRY_CODE = "+1";
		phoneNumber = removeNonDigits(phoneNumber);
		StringBuffer sb = new StringBuffer();

		// If the number is longer than 10, cut it to the last 10 digits
		if (phoneNumber.length() > 10) {

			LOGGER.warn("formatPhoneLink received a number longer than 10 digits: {}", phoneNumber);
			phoneNumber = phoneNumber.substring(phoneNumber.length() - 10);

		}

		if (phoneNumber.length() == 10) {

			sb.append(Constants.TELEPHONE_URL_PREFIX);
			sb.append(US_COUNTRY_CODE);
			sb.append(TEL_DELIMITER);
			sb.append(phoneNumber.substring(0, 3));
			sb.append(TEL_DELIMITER);
			sb.append(phoneNumber.substring(3, 6));
			sb.append(TEL_DELIMITER);
			sb.append(phoneNumber.substring(6));
			LOGGER.debug("formatPhoneLink complete: {}", sb.toString());

		} else {

			// If we can't construct a 10-digit number, return empty string
			LOGGER.error("formatPhoneLink could not construct a 10-digit number from modified input {}", phoneNumber);
			return "";

		}

		return sb.toString();

	}

	/**
	 * Removes any characters from a string that are not digits [0-9]
	 * @param target String from which to remove all digits
	 * @return the target String with all non-digits removed
	 */
	private static String removeNonDigits(String target) {

		if (target == null) {

			return "";

		}

		String notDigits = "[^\\d]";
		target = target.replaceAll(notDigits, "");
		return target;

	}

	/**
	 * A method for iterating over all values stored in a composite multifield and returning an object suitable for
	 * use with data-sly-list or -repeat. Note that this only returns Strings as the values in the Maps.
	 * @param resource The resource the multifield values live under, usually a Page's content resource or a Style resource
	 * @param relPath The location of the multifield values relative to the resource (e.g. './mediaLinks')
	 * @param properties The properties of each multifield value that should be added to the map
	 * @return an ArrayList suitable to be iterated over using data-sly-list or -repeat
	 * @throws NullPointerException if there is no node at relPath underneath resource 
	 * @throws RepositoryException if the NodeIterator cannot be created using the Node at relPath
	 */
	public static ArrayList<Map<String,String>> getMultiFieldValues(Resource resource, String relPath, String[] properties)
		throws NullPointerException, RepositoryException, IllegalArgumentException {

		ArrayList<Map<String,String>> list = new ArrayList<Map<String,String>>();

		Resource multiFieldResource = resource.getChild(relPath);

		if (multiFieldResource == null) {

			throw new NullPointerException("There was no node located at " + relPath);

		} else if (properties == null) {
		
			throw new IllegalArgumentException("The array of properties passed was null");
			
		} else {

			Node multiFieldNode = multiFieldResource.adaptTo(Node.class);
			NodeIterator children = multiFieldNode.getNodes();

			while (children.hasNext()) {

				HashMap<String,String> map = new HashMap<String,String>();

				Node child = children.nextNode();
				LOGGER.debug("Searching in node {}", child.getPath());

				for (String property : properties) {

					LOGGER.debug("Looking for property {}", property);

					if (child.hasProperty(property)) {

						LOGGER.debug("Property {} found", property);
						map.put(property, child.getProperty(property).getString());

					} else {

						LOGGER.debug("No property {} found", property);

					}
	
				}

				if (!map.isEmpty()) {

					LOGGER.debug("Map successfully added to list");
					list.add(map);

				} else {

					LOGGER.debug("Map was empty and not added to list");

				}
				
			}
			
		}

		return list;

	}

}
