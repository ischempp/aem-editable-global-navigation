# aem-editable-global-navigation
A solution to creating a global navigation for a website that can be curated by an AEM author rather than generated automatically via content architecture

## Problem statement
The problem at hand was that the product owner of the website wanted a way to curate the global navigation of the site by hand,
without necessarily relying on content structure. The navigation generated needed to be used not only in the header of the webpage,
but also in the off-canvas navigation, which had slightly different HTML stucture due to restrictions put in place by the CSS
framework being used (Zurb Foundation).

The navigation needed to differentiate between three different kinds of items within the navigation:
a summary item to be placed at the top of the navigation, high-level topic items, and items that could be placed within these topics.

In addition, there needed to be the ability to separate these navigations into up to three columns within the dropdown.

## Proposed solution
The proposed solution was to create a component that represented one section of the website's global navigation.

The dialog of this component would allow an AEM author to determine the text to appear as the title of the section, the text of
the summary item, and the location that the summary text should link to. The component dialog would also have three additional tabs,
one for each potential column in the dropdown. These tabs would contain a nested multifield, allowing the author to create multiple 
high-level topic items and within those high-level topics, items to be placed within those topics.

The back-end of the component would read in the data entered by the author and create an ArrayList representing each of the
columns to be output in the dropdown. Each column would be represented by another ArrayList, this time containing a custom class
(Topic). This class held a __Map<String, String>__ representing the high-level topic page and an __ArrayList<Map<String, String>>__
representing the items that had been placed within this topic.

The HTL for this component would be split into two scripts: one to render the global navigation in the header of the page, and
another to render the global navigation in the off-canvas navigation of the page. The second script would be called using Sling
selectors within a data-sly-resource call.

Each HTL script would iterate through the columns as returned by the back-end. It would then have to iterate again through the topics
contained within each column, and potentially iterate once more through the items placed within the topics.

Once the components were created and populated with data, the template editor could set which of these global navigation section
components would actually be used on each template (and the order in which they appeared) by setting them in the policies of the 
topbar and off-canvas navigation structural components.
