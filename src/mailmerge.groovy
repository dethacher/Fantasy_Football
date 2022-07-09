// Credit: https://www.docmosis.com/company/blog/7-february-2015.html

import java.io.File;
import java.util.Date;

import com.sun.star.beans.PropertyValue;
import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XStorable;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.text.XTextDocument;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.XReplaceDescriptor;
import com.sun.star.util.XReplaceable;

def merge(team = "", roster = "", money = "", ltc = "", broken="") {
	// Initialise
	XComponentContext xContext = Bootstrap.bootstrap();
	
	XMultiComponentFactory xMCF = xContext.getServiceManager();
	
	Object oDesktop = xMCF.createInstanceWithContext(
	     "com.sun.star.frame.Desktop", xContext);
	
	XDesktop xDesktop = (XDesktop) UnoRuntime.queryInterface(
	     XDesktop.class, oDesktop);

	// Load the Document
	String workingDir = new File(".").getAbsolutePath() + "/";
	String reportDir = workingDir + "Reports/";
	new File(reportDir).mkdirs();
	String myTemplate = workingDir + "letterTemplate.doc";
	
	if (!new File(myTemplate).canRead()) {
		throw new RuntimeException("Cannot load template:" + new File(myTemplate));
	}

	XComponentLoader xCompLoader = (XComponentLoader) UnoRuntime
		.queryInterface(com.sun.star.frame.XComponentLoader.class, xDesktop);

	String sUrl = "file:///" + myTemplate;
	
	PropertyValue[] propertyValues = new PropertyValue[0];
	
	propertyValues = new PropertyValue[1];
	propertyValues[0] = new PropertyValue();
	propertyValues[0].Name = "Hidden";
	propertyValues[0].Value = new Boolean(true);
	
	XComponent xComp = xCompLoader.loadComponentFromURL(
		sUrl, "_blank", 0, propertyValues);

	
	// Manipulate
	XReplaceDescriptor xReplaceDescr = null;
	XReplaceable xReplaceable = null;

	XTextDocument xTextDocument = (XTextDocument) UnoRuntime
			.queryInterface(XTextDocument.class, xComp);

	xReplaceable = (XReplaceable) UnoRuntime
			.queryInterface(XReplaceable.class,
					xTextDocument);

	xReplaceDescr = (XReplaceDescriptor) xReplaceable
			.createReplaceDescriptor();

	// mail merge the date
	xReplaceDescr.setSearchString("<date>");
	xReplaceDescr.setReplaceString(new Date().toString());
	xReplaceable.replaceAll(xReplaceDescr);
	
	// mail merge the team
	xReplaceDescr.setSearchString("<team>");
	xReplaceDescr.setReplaceString(team);
	xReplaceable.replaceAll(xReplaceDescr);
	
	// mail merge the roster
	xReplaceDescr.setSearchString("<roster>");
	xReplaceDescr.setReplaceString(roster);
	xReplaceable.replaceAll(xReplaceDescr);
	
	// mail merge the ltc
	xReplaceDescr.setSearchString("<ltc>");
	xReplaceDescr.setReplaceString(ltc);
	xReplaceable.replaceAll(xReplaceDescr);
	
	// mail merge the money
	xReplaceDescr.setSearchString("<money>");
	xReplaceDescr.setReplaceString(money);
	xReplaceable.replaceAll(xReplaceDescr);
	
	// mail merge the broken ltc
	xReplaceDescr.setSearchString("<broken>");
	xReplaceDescr.setReplaceString(broken);
	xReplaceable.replaceAll(xReplaceDescr);
	
	
	// save as a PDF 
	XStorable xStorable = (XStorable) UnoRuntime
			.queryInterface(XStorable.class, xComp);

	propertyValues = new PropertyValue[2];
	// Setting the flag for overwriting
	propertyValues[0] = new PropertyValue();
	propertyValues[0].Name = "Overwrite";
	propertyValues[0].Value = new Boolean(true);
	// Setting the filter name
	propertyValues[1] = new PropertyValue();
	propertyValues[1].Name = "FilterName";
	propertyValues[1].Value = "writer_pdf_Export";

	// Appending the favoured extension to the origin document name
	String myResult = reportDir + team + ".pdf";
	xStorable.storeToURL("file:///" + myResult, propertyValues);

	System.out.println("Saved " + myResult);

	// shutdown
	xDesktop.terminate();
}
