
# OVERVIEW
This README file serves as a guide for understanding and managing the SDK Help content that is deployed with the Dragonfly SDK Eclipse plugin.  

## Status 
---- 09.25 02:01:59 PM ----
* Main content load done.  Now need to test deploying and viewing content in Eclipse.  
* Updates for BUG 2.0 hardware and SDK not done yet
* Organized SDK Help navigation into 3 sections: Getting Started, Configure and Run Applications, and BUGnet Community.  
* Redundant toc files will be deleted after new toc xml format is validated

## References
* Eclipse Help HOWTO: http://www.ibm.com/developerworks/opensource/library/os-echelp/
* This is a markdown file.  http://en.wikipedia.org/wiki/Markdown


# Content Management Process
The pages in the SDK Help are first authored in the BUG Labs wiki (the new one:  wiki.buglabs.net) and migrated here.  In general, all SDK Help pages in the wiki are in the Tutorial namespace and belong to the category "SDK Help".  In this way, they are easily findable [##TODO##:link to SDK Help page] using the DynamicPageList extension in Mediawiki.  

By using the wiki to author content, we take advantage of the content editing and rendering capabilities of Mediawiki to do the hard work in updating the SDK Help docs.  This also ensures consistency between the two sets of information and should yield a richer set of SDK help pages. cool, right?


## Updating Pages
If you open any of the html pages in a browser, you will notice that each has a link back to the original page on the wiki.  Edit the page in the wiki until you are satisfied with the results.  When you are finished, you will need to do the following to migrate the content here.  
* Copy-paste the bodytext part of the page source from the wiki 
* Fix image paths that are messed up by mediawiki.
* Copy image files from the wiki to the directory ./html/images
* Remove or fix any links
* Preview the page and fix issues

### Copying the bodytext 
The term "bodytext" refers to the html comments that surround the relevant html source.  To get to the html source, you should click on the "Printable page" link in the left nav of the wiki.  We want the printable page, because it does not have the "Edit" links throughout the page.  From the Printable page, display the page source for the page and look for the range of code that begins with: <!-- bodytext --> and ends with <!-- /bodytext -->.  Copy the entire range of code (including the bodytext comments) and replace the similar range of bodytext code in the index.html file for the page you are editing.  These are clearly identified with more html comments.


### Fixing Images
If there are images in the page, you will notice that Mediawiki has its own way of organizing and rendering image content.  The following are a series of fixes that you need to do.  In each case, regex replace values are provided to make this easier.

1. Remove a tags surrounding images:  
Mediawiki has a funny need to link to the image page for an uploaded file. And we need to get rid of these.
FIND:    \<a [^\>]*\>(\<img [^\>]*\>)</a>
REPLACE: 

2. Fix image paths
You also need to replace these with the "../images/" path used throughout the html files here.  Example: Mediawiki image paths look like this src="/images/7/70/Bug.gif" and we need to fix them so they look like this src="../images/Bug.gif".
FIND:    src="/images/\w{1}/\w{2}
REPLACE: src="../images

3. Replacing thumbnail images
If the page has thumbnail images to reduce full-size images, you need to fix those as well.
FIND:    src="/images/thumb/\w{1}/\w{2}/([^/]+)/[^"]+
REPLACE: src="../images/$1


## Creating Pages
This works like the Updating Pages instructions, except you need to create and modify more files.  Briefly, the steps are:
* Copy the TEMPLATE index.html
* Create or find the desired page content in the wiki.
* Follow the Updating Pages process defined above.
* Preview the new page and fix issues
* Add a new entry in toc.xml for the new page

### Copy TEMPLATE html
Within the html subdirectory here, go to the TEMPLATE directory and find the index.html file there.  Create a subdirectory in the html directory, give it a unique descriptive name like the others, and create a copy of the TEMPLATE index.html file inside it. 

### Modify toc.xml
The toc.xml file has topic sections that look like this:

`
<topic label="Getting Started">
  <topic label="Create a BUG Project" href="html/createBUGProject/index.html"/>	
  <topic label="Creating a Basic Application" href="html/creatingABasicApplication/index.html"/>
  <topic label="Call a Web Service in an Application" href="html/callWebServicesApp/index.html"/>       
  <topic label="Debug an Application" href="html/debugAnApp/index.html"/>
  <topic label="Baby Monitor Tutorial" href="html/babyMonitor/index.html"/>
</topic>
`

Add a new topic row and input the title in the label attribute and the path the html file in the href attribute.

# Other Notes

## Page TOC 
Nice surprise.  We get the TOC generated by Mediawiki as a bonus.  

## CSS Styles
The basic css of the BugLabs wiki has been copied here to provide some uniformity in the look and feel.

## Related Links
Each/most of the html pages in the SDK Help has a related links section at the bottom.  This is conveniently outside of the bodytext copy-paste region.  You can manually edit these links in order to improve the user navigation of content.
 

## KNOWN ISSUES:
- pre tag generates leading spaces