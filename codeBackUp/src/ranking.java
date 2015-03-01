package	com.mcgrawhill.ezto.test.questions.question_types;

import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletOutputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jdom.CDATA;
import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mcgrawhill.ezto.TestPilot4;
import com.mcgrawhill.ezto.tp_requestHandler;
import com.mcgrawhill.ezto.admin.licenseManager;
import com.mcgrawhill.ezto.api.caa.caaconstants.CaaConstants;
import com.mcgrawhill.ezto.api.caa.services.transferobject.PolicyTO;
import com.mcgrawhill.ezto.api.caa.services.transferobject.QuestionMetaDataTO;
import com.mcgrawhill.ezto.api.caa.services.transferobject.QuestionWiseResponseTO;
import com.mcgrawhill.ezto.api.caa.services.transferobject.ResponseTO;
import com.mcgrawhill.ezto.api.caa.services.transferobject.TestTO;
import com.mcgrawhill.ezto.api.caa.services.transferobject.UserResponseWithPolicyTO;
import com.mcgrawhill.ezto.api.license.services.LicenseService;
import com.mcgrawhill.ezto.integration.classware_hm;
import com.mcgrawhill.ezto.integration.hm_grading_r6b;
import com.mcgrawhill.ezto.integration.policies;
import com.mcgrawhill.ezto.media.embeddedMedia;
import com.mcgrawhill.ezto.media.richMedia;
import com.mcgrawhill.ezto.sql.tp_sql;
import com.mcgrawhill.ezto.test.test;
import com.mcgrawhill.ezto.test.testFormatException;
import com.mcgrawhill.ezto.test.v5test;
import com.mcgrawhill.ezto.test.v7test_student;
import com.mcgrawhill.ezto.test.exporters.exporter_vista;
import com.mcgrawhill.ezto.test.questions.question;
import com.mcgrawhill.ezto.test.questions.questionGroup;
import com.mcgrawhill.ezto.test.recording_reporting.response;
import com.mcgrawhill.ezto.test.recording_reporting.submission;
import com.mcgrawhill.ezto.test.recording_reporting.summaryItem;
import com.mcgrawhill.ezto.utilities.BeanExtractionUtil;
import com.mcgrawhill.ezto.utilities.Crypt;
import com.mcgrawhill.ezto.utilities.CustomMap;
import com.mcgrawhill.ezto.utilities.FeedBackTO;
import com.mcgrawhill.ezto.utilities.PartialTO;
import com.mcgrawhill.ezto.utilities.QuestionParameters;
import com.mcgrawhill.ezto.utilities.QuestionUtil;
import com.mcgrawhill.ezto.utilities.VectorAdapter;
import com.mcgrawhill.ezto.utilities.bbExporter;
import com.mcgrawhill.ezto.utilities.parameters;
import com.mcgrawhill.ezto.utilities.randomVariable;
import com.mcgrawhill.ezto.utilities.richProperties;
import com.mcgrawhill.ezto.utilities.rtf_pdf_output;
import com.mcgrawhill.ezto.utilities.tp_utils;

/**
* implementation of ranking questions
*/
public class ranking extends question
{
	private static final Logger _logger = Logger.getLogger(ranking.class);
	public static String		TYPE_IDENTIFIER			= "RA";
	
	public static String		EDIT_UNBRANCH = "EDIT_rUnanswered",
								EDIT_CORRECTBRANCH = "EDIT_rCorrect",
								EDIT_INCORRECTBRANCH = "EDIT_rIncorrect",
								EDIT_PTS = "EDITrPTS",
								EDIT_FEEDBACK = "EDITrFEEDBACK",
								EDIT_CHOICE = "EDITrCHOICE",
								EDIT_DEL = "EDITrDelete",
								EDIT_PARTIAL = "EDITrPartial";
						
	public static String		NO_PARTIAL_CREDIT		= "noPartialCredit";

	boolean		noPartialCredit = false;
				
	int			branchCorrect = -1,
				branchIncorrect = -1,
				branchUnanswered = -1;
	
	
	/**
	* construct a new ranking question
	*/
	public ranking() {
		super();

		type= QUESTION_TYPE_ranking;
		
		points.addElement("0");
		
		feedback.addElement("");
		feedback.addElement("");
	}
	

	/**
	* construct a new ranking question from a vector of data as constructed by the question
	* import procedure.
	*/
	public ranking( VectorAdapter qInfo ) {
		super();

		type= QUESTION_TYPE_ranking;

		if (qInfo.size() >= 2) qtext= (String)qInfo.elementAt(2);
		
		for (int i=3 ; i< qInfo.size() ; i++)
			choices.addElement( (String)qInfo.elementAt(i) );
		
		points.addElement("0");
		
		feedback.addElement("");
		feedback.addElement("");
	}
	

	/**
	* duplicate a ranking question.
	*/
	public ranking( ranking theOriginal )
	{
		super( theOriginal );

		type= QUESTION_TYPE_ranking;
		
		// dup the booleans
		noPartialCredit= theOriginal.noPartialCredit;			

		branchCorrect= theOriginal.branchCorrect;
		branchIncorrect= theOriginal.branchIncorrect;
		branchUnanswered= theOriginal.branchUnanswered;

		// dup additional subclasses here
	}
	
	
	/**
	* read a ranking question from an InputStream.
	* 
	* @param theInput
	* the stream from which to read the object
	* 
	* @param format
	* the format of the data in the stream
	*/
	public ranking( DataInputStream theInput, int format )
		throws testFormatException
	{
		super( theInput, format );

		type= QUESTION_TYPE_ranking;
		
		try {
			
			// read the booleans
			noPartialCredit= theInput.readBoolean();			
			
			// read the ints
			branchCorrect= theInput.readInt();
			branchIncorrect= theInput.readInt();
			branchUnanswered= theInput.readInt();

			// read additional subclasses here
		
		} catch (IOException e) {
			
			throw (new testFormatException( "IOException reading ranking question" ) );
			
		}
		
		noPartialCredit= questionProperties.getBoolean( NO_PARTIAL_CREDIT, noPartialCredit );
		questionProperties.setBoolean( NO_PARTIAL_CREDIT, noPartialCredit );
	}
	
	
	public ranking( Element xmlQ, int format )
		throws testFormatException
	{
		super();
		
		if (xmlQ.getChildren().size() <= 0) throw new testFormatException();

		type= QUESTION_TYPE_ranking;
		
		qtext= xmlQ.getChildText( test.XML_QUESTION_STEM );
		referenceTag= xmlQ.getChildText( test.XML_QUESTION_REFERENCE );
		if (referenceTag == null) referenceTag= "";
		pageTag= xmlQ.getChildText( test.XML_QUESTION_PAGE_REFERENCE );
		if (pageTag == null) pageTag= "";
		
		String theFeedback= xmlQ.getChildText( test.XML_QUESTION_FEEDBACK );
		if (theFeedback == null) theFeedback= "";
		
		java.util.List theList= xmlQ.getChildren( test.XML_QUESTION_ITEM );
		ListIterator iter= theList.listIterator();
		while (iter.hasNext())
		{
			maxPoints++;
			
			Element thisChoice= (Element)iter.next();
			String theText= thisChoice.getText();
			choices.addElement( theText );
		}

		points.addElement("1");
		
		feedback.addElement(theFeedback);
		feedback.addElement(theFeedback);
	}


	public void write( DataOutputStream out ) 
		throws testFormatException
	{
		
		try {
			super.write( out );
				
			// write the booleans
			out.writeBoolean( noPartialCredit );
			
			
			out.writeInt( branchCorrect );
			out.writeInt( branchIncorrect );
			out.writeInt( branchUnanswered );

			// write additional subclasses here
		
		} catch (IOException e) {
			throw ( new testFormatException( "IOException writing ranking question" ) );
		}
		
	}


	public String typeString() {
		return( "ranking" );
	}
		
	
	public String formalTypeString() {
		return( "Ranking&nbsp;Question" );
	}
	public String formalTypeString2( tp_requestHandler theHandler ) {
		return( "Ranking" );
	}
	public String formalTypeString2() {
		return( "Ranking" );
	}
	
	
	public String typeStringSmall() {
		return( "rank" );
	}
		
	
	public void addEmptyVars( parameters theParams ) {
	
		double obfusticator1= Math.rint(Math.random() * 1000);
		double obfusticator2= Math.rint(Math.random() * 1000);
		double obfusticator3= Math.rint(Math.random() * 100);
		String ob1Str= Integer.toString((int)obfusticator1);
		while (ob1Str.length() < 3) ob1Str= "0" + ob1Str;
		String ob2Str= Integer.toString((int)obfusticator2);
		while (ob2Str.length() < 3) ob2Str= "0" + ob2Str;
		String ob3Str= Integer.toString((int)obfusticator3);
		while (ob3Str.length() < 2) ob3Str= "0" + ob3Str;
		String theObfusticator= ob1Str + ob3Str + ob2Str;
		theParams.replaceParam("Q_" + sqlID, theObfusticator);

		for (int i=0 ; i<choices.size() ; i++)
			theParams.replaceParam("Q_" + sqlID + "_" + obfusticateID(i,theObfusticator), "0");
	}


	public String buildHTML( test theTest, parameters theData, String substitutedText ) {
		
		String theQID= "Q_" + sqlID;

		double obfusticator1= Math.rint(Math.random() * 1000);
		double obfusticator2= Math.rint(Math.random() * 1000);
		double obfusticator3= Math.rint(Math.random() * 100);
		String ob1Str= Integer.toString((int)obfusticator1);
		while (ob1Str.length() < 3) ob1Str= "0" + ob1Str;
		String ob2Str= Integer.toString((int)obfusticator2);
		while (ob2Str.length() < 3) ob2Str= "0" + ob2Str;
		String ob3Str= Integer.toString((int)obfusticator3);
		while (ob3Str.length() < 2) ob3Str= "0" + ob3Str;
		String theObfusticator= ob1Str + ob3Str + ob2Str;

		if (theData != null) {
			String tmp= theData.getParam( theQID );
			// v4.1.3p40 
			if (tmp.length() > 0) theObfusticator= tmp;
			theData.removeParam( theQID );
		}

		
		String resultHTML= theTest.getGUI().responseFontStart();
		
		// script to attempt to prevent duplications - don't assume it works
		resultHTML += "<script type=\"text/javascript\" >";
		resultHTML += "function " + theQID + "ck(nv,ti){";
		VectorAdapter scriptVector= new VectorAdapter();
		for (int i=0 ; i<choices.size() ; i++)
			scriptVector.addElement("	if (ti!=" + obfusticateID(i,theObfusticator) + ") if (document." + tp_requestHandler.FORMNAME + "." + theQID + "_" + obfusticateID(i,theObfusticator) + ".value==nv) document." + tp_requestHandler.FORMNAME + "." + theQID + "_" + obfusticateID(i,theObfusticator) + ".value=\"0\";");
			//resultHTML += "	if (ti!=" + Integer.toString(i) + ") if (document." + tp_requestHandler.FORMNAME + "." + theQID + "_" + Integer.toString(i) + ".value==nv) document." + tp_requestHandler.FORMNAME + "." + theQID + "_" + Integer.toString(i) + ".value=\"0\";";
		while (scriptVector.size() > 0) {
			if (scriptVector.size() == 1) {
				resultHTML += (String)scriptVector.elementAt(0);
				scriptVector.removeElementAt(0);
			}
			else {
				double calc= Math.random() * scriptVector.size();
				resultHTML += (String)scriptVector.elementAt((int)calc);
				scriptVector.removeElementAt((int)calc);
			}
		}
		resultHTML += "}";
		resultHTML += "</script>";


		VectorAdapter choiceVector= new VectorAdapter();
		for (int i=0 ; i<choices.size() ; i++) {
			String theChoice= (String)choices.elementAt(i);
			try {
				richMedia theMedia= new richMedia( theChoice );
				theMedia.setChoice(true);
				theChoice= theMedia.html("");
			} catch (testFormatException e) {}
			
			theChoice= randomVariable.deReference(theChoice, theTest, sqlID, theData);
			theChoice= richMedia.deReference(theChoice, "");

			int thisItemsDefault= 0;
			if (theData != null) {
				String thisDefault= theData.getParam( theQID + "_" + obfusticateID(i,theObfusticator) );
				theData.removeParam( theQID + "_" + obfusticateID(i,theObfusticator) );
				try {
					thisItemsDefault= Integer.parseInt(thisDefault);
				} catch (NumberFormatException n) {}
			}
			
			//String thisChoiceHTML= "<SELECT NAME=\"" + theQID + "_" + Integer.toString(i) + "\" onChange=\"" + theQID + "ck(this.value," + Integer.toString(i) + ");\">";
			String thisChoiceHTML= "<SELECT NAME=\"" + theQID + "_" + obfusticateID(i,theObfusticator) + "\" onChange=\"" + theQID + "ck(this.value," + obfusticateID(i,theObfusticator) + ");\">";
			thisChoiceHTML += "	<OPTION ";
			if (thisItemsDefault == 0) thisChoiceHTML += "SELECTED ";
			thisChoiceHTML += "VALUE=\"0\">&nbsp;";
			for (int j=0 ; j<choices.size() ; j++) {
				thisChoiceHTML += "<OPTION ";
				if (thisItemsDefault == (j+1)) thisChoiceHTML += "SELECTED ";
				thisChoiceHTML += "VALUE=\"" + Integer.toString(j+1) + "\">" + Integer.toString(j+1) + "";
			}
			thisChoiceHTML += "</SELECT>&nbsp;&nbsp;&nbsp;&nbsp;";
			thisChoiceHTML += theChoice + "<BR>";
			
			choiceVector.addElement(thisChoiceHTML);
		}
		
		
		// here is where we will randomize the order
		//for (int i=0; i<choiceVector.size() ; i++)
		//	resultHTML += (String)choiceVector.elementAt(i);
		
		while (choiceVector.size() > 0) {
			if (choiceVector.size() == 1) {
				resultHTML += (String)choiceVector.elementAt(0);
				choiceVector.removeElementAt(0);
			}
			else {
				double calc= Math.random() * choiceVector.size();
				resultHTML += (String)choiceVector.elementAt((int)calc);
				choiceVector.removeElementAt((int)calc);
			}
		}
		
		resultHTML += "</BLOCKQUOTE>";
		
		resultHTML += "<INPUT TYPE=\"HIDDEN\" NAME=\"" + theQID + "\" VALUE=\"" + theObfusticator + "\">";
		
		resultHTML += theTest.getGUI().responseFontEnd();
	
		return(resultHTML);

	} // end buildHTML
	
	
	private String obfusticateID(int id, String obStr) {
		String result= obStr.substring(0,3);
		if (id < 10) result += "0";
		result += Integer.toString(id);
		result += obStr.substring(5);
		return( result );
	}
	

	public void evaluate( submission theSubmission, test theTest ) {
	
		String questionID= "Q_" + sqlID;
		String theObfusticator= theSubmission.getParam( questionID );
		if (theObfusticator.length() == 0) return;		// question was not offered
		
		response thisResponse= new response( sqlID, maxPoints, finalHTML( theTest, new parameters(theSubmission.formVariables) ) );
		
		int offerCount= 0;
		int pointTotal= 0;
		
		int pointIncrement= 0;
		try {
			pointIncrement= Integer.parseInt((String)points.elementAt(0));
		} catch (NumberFormatException n) {}

		String userRanking= "";
		String userResponse= "";
		for (int j=0 ; j<choices.size() ; j++) {
			
			//String userEntry= theSubmission.getParam( questionID + "_" + Integer.toString(j) );
			String userEntry= theSubmission.getParam( questionID + "_" + obfusticateID(j,theObfusticator) );
			
			if (userEntry.length() == 0) {}
			else if (userEntry.equals( "0" )) { // unchecked
				offerCount++;
				if (userRanking.length()>0) userRanking += ",";
				userRanking += "0";
				userResponse += Integer.toString(j+1) + ".&nbsp;" + (String)choices.elementAt( j ) + " <FONT SIZE=1><I>NOT ranked</I></FONT><BR>";
			}
			else {
				offerCount++;
				if (userRanking.length()>0) userRanking += ",";
				userRanking += userEntry;
				userResponse += Integer.toString(j+1) + ".&nbsp;" + (String)choices.elementAt( j ) + " <FONT SIZE=1><I>ranked</I> #" + userEntry + "</FONT><BR>";
				try {
					int thisRank= Integer.parseInt(userEntry) - 1;
					if (thisRank == j) {
						thisResponse.incrementScore( pointIncrement );
						pointTotal += pointIncrement;
					}
				} catch (NumberFormatException n) {}
			}
				
		}
		
		thisResponse.recordValue(userRanking);
		thisResponse.setUserResponse(userResponse);
		
		if (noPartialCredit) {
			if (pointTotal < maxPoints) {
				thisResponse.points= 0;
				pointTotal = 0;
			}
		}
		
		if (pointTotal != 0)
			thisResponse.setFeedback((String)feedback.elementAt(0));
		else
			thisResponse.setFeedback((String)feedback.elementAt(1));
					
		if (offerCount > 0) theSubmission.addResponse( thisResponse );
				
	}


	public int returnBranch( parameters theParams, test theTest ) {
	
		String userEntry= theParams.getParam( "Q_" + sqlID ).trim();
		if (userEntry.length()==0) return( branchUnanswered );
			
		// make a temporary submission and have it evaluated
		submission tmpSubmission= new submission( theParams, theTest );
		evaluate( tmpSubmission, theTest );
		
		// find this question's response
		response thisResponse= null;
		for (int i=0 ; i<tmpSubmission.responses.size() ; i++) {
			thisResponse= (response)tmpSubmission.responses.elementAt(i);
			if (thisResponse.questionID.equals(sqlID)) break;
			thisResponse= null;
		}
		
		String userResponse= (String)thisResponse.userResponse.elementAt(0);
		if (userResponse.indexOf("NOT ranked") >= 0) return( branchUnanswered );
		
		if (thisResponse != null) {
			if (thisResponse.points > 0) 
				return( branchCorrect );
			else
				return( branchIncorrect );
		}
	
		return( -1 );
	}


	public String responseTableHeader() {
		return( "<TH><FONT FACE=\"Arial,Helvetica\" SIZE=1 COLOR=WHITE>Q" + Integer.toString(id) + "</FONT></TH>" );
	}
	
	public String emptyResponseTableEntry() {
		return( "<TD>-</TD>" );
	}

	public String responseSpreadsheetHeader() {
		return( "Q" + Integer.toString(id) + "\t" );
	}
	
	public String emptyResponseSpreadsheetEntry() {
		return( "\t" );		
	}


	public void dumpIt( ServletOutputStream output, test theTest ) {
		// dump the contents as HTML
		
		try {
			super.dumpIt( output, theTest );
			
			output.println( "<TABLE BORDER>" );
			output.println( "<TR><TH>Items</TH></TR>" );
			for (int i=0 ; i<choices.size() ; i++) {
				String theChoice= (String)choices.elementAt(i);
				output.println( "<TR><TD>" + Integer.toString( i+1 ) + ".&nbsp;" + tp_utils.substitute(theChoice, "<", "&lt;") + "</TD></TR>" );
			}
			output.println( "</TABLE><P>");

			output.println( "<TABLE BORDER>" );
			output.println( "<TR><TH>&nbsp;</TH><TH>Feedback</TH><TH>Branch</TH></TR>" );
			output.println( "<TR><TD>Correct</TD><TD>" + (String)feedback.elementAt(0) + "</TD><TD>" + Integer.toString( branchCorrect ) + "</TD></TR>" );
			output.println( "<TR><TD>Incorrect</TD><TD>" + (String)feedback.elementAt(1) + "</TD><TD>" + Integer.toString( branchIncorrect ) + "</TD></TR>" );
			output.println( "<TR><TD>Unanswered</TD><TD>&nbsp;</TD><TD>" + Integer.toString( branchUnanswered ) + "</TD></TR>" );
			output.println( "</TABLE><P>");

		} catch (IOException e) {
			_logger.error( "IOException in ranking.dumpIt()" );
		}
		
	}


	public void summarizeResponse( summaryItem theSummaryItem, response theResponse ) {
	
		theSummaryItem.setCount( choices.size() );

		String theValue= (String)theResponse.recordedValue.elementAt(0);
		boolean countedUnanswered= false;
		
		StringTokenizer theTokens= new StringTokenizer(theValue,",");
		for (int i=0 ; (theTokens.hasMoreTokens() && (i<choices.size())) ; i++) {
			try {
				int thisRank= Integer.parseInt(theTokens.nextToken());
				if (thisRank==0) {
					if (!countedUnanswered) {
						theSummaryItem.unansweredCount++;
						countedUnanswered= true;
					}
				}
				else if (thisRank == (i+1)) {
					int choiceCount= 0;
					String val= (String)theSummaryItem.frequencyCounts.get( Integer.toString(i) );
					try {
						if (val != null) choiceCount= Integer.parseInt( val );
					} catch (NumberFormatException n2) {}
					theSummaryItem.frequencyCounts.put( Integer.toString(i), Integer.toString(++choiceCount) );
				}
			} catch (NumberFormatException n) {
				if (!countedUnanswered) {
					theSummaryItem.unansweredCount++;
					countedUnanswered= true;
				}
			}
		}
		
	}
	
	
	public void updateCounts( response theResponse, ConcurrentHashMap frequencyCounts )
	{
	
		int count= choices.size();
		frequencyCounts.put( "itemCount", Integer.toString(count) );
		
		if (frequencyCounts.get("offeredCount") == null)
			count= 1;
		else {
			try { count= Integer.parseInt((String)frequencyCounts.get("offeredCount")) + 1; } catch (NumberFormatException n) { count= 1; };
		}
		frequencyCounts.put( "offeredCount", Integer.toString(count) );
		

		int unansweredCount= 0;
		if (frequencyCounts.get("unansweredCount") != null) {
			try { unansweredCount= Integer.parseInt((String)frequencyCounts.get("unansweredCount")); } catch (NumberFormatException n) { count= 1; };
		}


		String theValue= (String)theResponse.recordedValue.elementAt(0);
		boolean countedUnanswered= false;
		
		StringTokenizer theTokens= new StringTokenizer(theValue,",");
		for (int i=0 ; (theTokens.hasMoreTokens() && (i<choices.size())) ; i++) {
			try {
				int thisRank= Integer.parseInt(theTokens.nextToken());
				if (thisRank==0) {
					if (!countedUnanswered) {
						frequencyCounts.put( "unansweredCount", Integer.toString(++unansweredCount) );
						countedUnanswered= true;
					}
				}
				else if (thisRank == (i+1)) {
					int choiceCount= 0;
					String val= (String)frequencyCounts.get( Integer.toString(i) );
					try {
						if (val != null) choiceCount= Integer.parseInt( val );
					} catch (NumberFormatException n2) {}
					frequencyCounts.put( Integer.toString(i), Integer.toString(++choiceCount) );
				}
			} catch (NumberFormatException n) {
				if (!countedUnanswered) {
					frequencyCounts.put( "unansweredCount", Integer.toString(++unansweredCount) );
					countedUnanswered= true;
				}
			}
		}
	}
	
	
	/*
	public void frequencyAnalysis( ServletOutputStream output, test theTest, ConcurrentHashMap theCounts, int offeredCount ) {
	
		try {
		
			output.println( "<TABLE BORDER>" );
			output.println( "    <TR><TH>Frequency</TH><TH>Choice</TH><TH>Percent</TH></TR>" );
			
			for (int i= 0 ; i<choices.size() ; i++) {
				String theChoice= (String)choices.elementAt(i);
				
				int choiceCount= 0;
				try {
					String theValue= (String)theCounts.get( Integer.toString(i) );
					if (theValue != null) choiceCount= Integer.parseInt( theValue );
				} catch (NumberFormatException n) {};
				
				float pct= choiceCount * 100;
				if (offeredCount > 0) pct /= offeredCount;
				else pct= 0;
				int percent= Math.round(pct);
				
				output.println( "<TR><TD VALIGN=TOP ALIGN=CENTER>" + Integer.toString( choiceCount ) + "</TD><TD VALIGN=TOP>" + Integer.toString(i+1) + ". " + theChoice + "</TD><TD><TABLE BORDER=0 CELLPADDING=0 CELLSPACING=0><TR><TD BGCOLOR=#005000 WIDTH=" + Integer.toString(percent) + ">&nbsp;</TD><TD>&nbsp;" + Integer.toString(percent) + "%</TD></TR></TABLE></TD></TR>" );
			}

			output.println( "</TABLE>" );
			
		} catch (IOException e) {
			System.out.println( "IOException in ranking.frequencyAnalysis()" );
		}
	}
	*/
	
	public void frequencyAnalysis( ServletOutputStream output, test theTest, ConcurrentHashMap theCounts, int offeredCount ) {
	
		Connection con= null;
		PreparedStatement stmt = null;
		ResultSet rs= null;
		try {
		
			output.println( "<TABLE BORDER CELLPADDING=2>" );
			output.println( "<TR><TH>&nbsp;</TH>" );
			for (int i= 0 ; i<choices.size() ; i++)
				output.println( "<TH>#" + Integer.toString(i+1) + "</TH>" );
			output.println( "<TH>n/r</TH><TH>mean</TH></TR>" );
			
			int[][] countArray;
			countArray = new int[choices.size()][choices.size()+1];
			for (int i= 0 ; i<choices.size() ; i++) {
				for (int j= 0 ; j<choices.size()+1 ; j++) countArray[i][j]= 0;
			}
			
			con= theTest.sqlRoutine.getConnection();
			
			//Statement stmt= con.createStatement();
			//ResultSet rs= stmt.executeQuery("SELECT recordedValue FROM responses WHERE testID='" + theTest.sqlID + "' AND questionID='" + sqlID + "'");
			stmt= con.prepareStatement("SELECT recordedValue FROM responses WHERE testID=? AND questionID=?");
			stmt.setString(1, theTest.sqlID);
			stmt.setString(2, sqlID);
			rs= stmt.executeQuery();
			
			while (rs.next()) {
				Vector rspVector= theTest.sqlRoutine.vectorFromBlob(rs, "recordedValue");
				String userResponses= (String)rspVector.elementAt(0);

				StringTokenizer theTokens= new StringTokenizer(userResponses, ",");
				for (int ordinal=0 ; theTokens.hasMoreTokens() ; ordinal++) {
					try {
						int ranking= Integer.parseInt(theTokens.nextToken())-1;
						if (ranking == -1) ranking= choices.size();
						countArray[ordinal][ranking]++;
					} catch (NumberFormatException ignore) {}
				}
			}
			
			tp_sql.releaseResources(con, stmt, rs);
			
			DecimalFormat formatter= new DecimalFormat("0.00");
			for (int i= 0 ; i<choices.size() ; i++) {
				output.print("<TR><TD ALIGN=RIGHT><B>" + (String)choices.elementAt(i) + "</B></TD>");
				double mean= 0;
				for (int j= 0 ; j<choices.size()+1 ; j++) {
					output.print("<TD ALIGN=RIGHT>&nbsp;" + Integer.toString((countArray[i][j]*100)/offeredCount) + "%&nbsp;</TD>");
					if (j < choices.size())
						mean += countArray[i][j] * (j+1);
				}
				mean /= (double)offeredCount;
				output.print("<TD ALIGN=RIGHT>&nbsp;" + formatter.format(mean) + "&nbsp;</TD>");
				output.println("</TR>");
			}
						
			output.println( "</TABLE>" );
			
		} catch (SQLException s) {
			_logger.error( "SQLException in ranking.frequencyAnalysis()" );
		} catch (IOException e) {
			_logger.error( "IOException in ranking.frequencyAnalysis()" );
		}finally{
			tp_sql.releaseResources(con, stmt, rs);
		}
	}
	
	/*
	public void editQuestionItem( tp_requestHandler theHandler, test theTest, ConcurrentHashMap messages )
	{
		int userLevel= 3;
		String mode= theHandler.requestParams.getParam( "MODE" );
		if (mode.equals("BEGINNER")) userLevel= 2;
		if (mode.equals("INTRO")) userLevel= 1;
		
		// show common fields, starting table and form
		super.editQuestionItem( theHandler, theTest, messages );
		
		boolean editable= theHandler.theServlet.authenticator.authenticateEditor(theTest.questions, theHandler);
		
		// show type-specific fields
		
		// do not allow partial credit
		theHandler.snd("<TR><TD class=\"tdright\"><B><A onMouseOver=\"(window.status='get help...'); return true;\" HREF=\"javascript:helpMessage('rankingPartial');\">Credit</A></B></TD><TD>");
		theHandler.snd("<INPUT TYPE=CHECKBOX NAME=\"" + EDIT_PARTIAL + "\" ");
		if (noPartialCredit) theHandler.snd("CHECKED ");
		theHandler.snd("><B>do not allow partial credit</B><BR>&nbsp;");
		theHandler.snd("</TD></TR>");
		
		// points per item
		theHandler.snd("<TR><TD class=\"tdright\"><B><A onMouseOver=\"(window.status='get help...'); return true;\" HREF=\"javascript:helpMessage('rankingPoints');\">Points</A></B></TD><TD>");
		theHandler.snd("<INPUT style=\"text-align: center\" TYPE=TEXT SIZE=4 MAXSIZE=3 NAME=\"" + EDIT_PTS + "\" VALUE=\"" + (String)points.elementAt(0) + "\"><B>per correctly ranked item</B><BR>&nbsp;");
		theHandler.snd("</TD></TR>");
		
		// items
		theHandler.snd("<TR><TD><B><A onMouseOver=\"(window.status='get help...'); return true;\" HREF=\"javascript:helpMessage('rankingItems');\">Response<BR>Handling</A></B></TD><TD>");
		theHandler.snd("<TABLE CELLSPACING=0 CELLPADDING=2><TR><TH>Del</TH><TH>Items&nbsp;in&nbsp;order</TH></TR>");
		for (int i=0 ; i<choices.size() ; i++) {
			String color= "tr2";
			if ((i%2)==0) color= "tr1";
			theHandler.snd("<TR class=\"" + color + "\"><TD><INPUT TYPE=CHECKBOX NAME=\"" + EDIT_DEL + Integer.toString(i) + "\" VALUE=\"delete\"></TD>");
			theHandler.snd("<TD><TEXTAREA ROWS=3 COLS=70 NAME=\"" + EDIT_CHOICE + Integer.toString(i) + "\">" + (String)choices.elementAt(i) + "</TEXTAREA></TD></TR>");
		}
		
		String bcolor= "tr2";
		if ((choices.size()%2)==0) bcolor= "tr1";
		theHandler.snd("<TR><TD COLSPAN=2><B>Enter next item below...</B></TD>");
		theHandler.snd("<TR class=\"" + bcolor + "\"><TD>&nbsp;</TD><TD><TEXTAREA ROWS=3 COLS=70 NAME=\"" + EDIT_CHOICE + "\"></TEXTAREA></TD></TR>");
		theHandler.snd("</TABLE></TD></TR>");

		// feedback and branching
		theHandler.snd("<TR><TD><BR><B><A onMouseOver=\"(window.status='get help...'); return true;\" HREF=\"javascript:helpMessage('rankingFeedback');\">Feedback</A></B></TD><TD><BR>");
		theHandler.snd("<TABLE CELLSPACING=0 CELLPADDING=2><TR><TH>&nbsp;</TH><TH>Feedback</TH>");
		if (theTest.getGUI().branchingEnabled) theHandler.snd("<TH>Branch</TH>");
		theHandler.snd("</TR>");
		
		theHandler.snd("<TR class=\"tr2\"><TD class=\"tdright\"><B>Correct</B></TD><TD><TEXTAREA NAME=\"" + EDIT_FEEDBACK + "0\" ROWS=3 COLS=40>" + (String)feedback.elementAt(0) + "</TEXTAREA></TD>");
		if (theTest.getGUI().branchingEnabled) theHandler.snd("<TD>" + tp_utils.questionPopup( EDIT_CORRECTBRANCH, theTest, branchCorrect ) + "</TD>");
		theHandler.snd("</TR>");
		theHandler.snd("<TR class=\"tr1\"><TD class=\"tdright\"><B>Incorrect</B></TD><TD><TEXTAREA NAME=\"" + EDIT_FEEDBACK + "1\" ROWS=3 COLS=40>" + (String)feedback.elementAt(1) + "</TEXTAREA></TD>");
		if (theTest.getGUI().branchingEnabled) theHandler.snd("<TD>" + tp_utils.questionPopup( EDIT_INCORRECTBRANCH, theTest, branchIncorrect ) + "</TD>");
		theHandler.snd("</TR>");
		if (theTest.getGUI().branchingEnabled)
			theHandler.snd("<TR class=\"tr2\"><TD class=\"tdright\"><B>Unanswered</B></TD><TD>&nbsp;</TD><TD>" + tp_utils.questionPopup( EDIT_UNBRANCH, theTest, branchUnanswered ) + "</TD></TR>");
		theHandler.snd("</TABLE></TD></TR>");

		
		// edit random variables
		theHandler.snd(randomVariable.editRandoms( localRandoms, theTest.globalRandoms ));
		
		//show a sample
		theHandler.snd("<TR><TD COLSPAN=2>&nbsp;</TD></TR>");
		theHandler.snd("<TR><TD COLSPAN=2 BGCOLOR=\"" + theTest.getGUI().testBackground + "\" ALIGN=LEFT>" + finalHTML( theTest, null ) + "</TD></TR>");


		// close table and end page
		theHandler.snd("</TABLE></FORM>");
		theHandler.snd("<HR WIDTH=80%><CENTER><SPAN class=\"footer\">");
		theHandler.snd("Copyright&nbsp;&copy;2008&nbsp;<A HREF=\"http://www.clearLearning.com/\">The McGraw Hill Companies</A>,&nbsp;All&nbsp;Rights&nbsp;Reserved.</SPAN></CENTER>");
		
		theHandler.snd("</BODY></HTML>");
	}
	
	
	public void update( tp_requestHandler theHandler, test theTest, ConcurrentHashMap messages )
	{
	
		messages= new ConcurrentHashMap();
		if (!theHandler.theServlet.authenticator.authenticateEditor(theTest.questions, theHandler)) {
			editQuestionItem( theHandler, theTest, messages );
			return;
		}
				
		super.update( theHandler, theTest, messages );

		String checkbox= theHandler.requestParams.getParam( EDIT_PARTIAL ).trim();
		noPartialCredit= (checkbox.length() != 0);
		
		int pts= 0;
		checkbox= theHandler.requestParams.getParam( EDIT_PTS ).trim();
		try {
			pts= Integer.parseInt(checkbox);
			if (pts < 0) pts= 0;
			points.setElementAt(Integer.toString(pts), 0);
		} catch (NumberFormatException n) {
			points.setElementAt("0", 0);
		}
		
		for (int i=0 ; i<choices.size() ; i++)
			choices.setElementAt( theHandler.requestParams.getParam( EDIT_CHOICE + Integer.toString(i) ).trim(), i );
		
		for (int i=choices.size()-1 ; i>=0 ; i--) {
			String delString= theHandler.requestParams.getParam( EDIT_DEL + Integer.toString(i) ).trim();
			if (delString.length()>0)
				choices.removeElementAt(i);
		}
		
		for (int i=choices.size()-1 ; i>=0 ; i--) {
			String theChoice= (String)choices.elementAt(i);
			if (theChoice.length()==0) choices.removeElementAt(i);
		}
		
		String newChoice= theHandler.requestParams.getParam( EDIT_CHOICE ).trim();
		if (newChoice.length() > 0)
			choices.addElement( newChoice );
			
		maxPoints= choices.size() * pts;
		
		feedback.setElementAt(theHandler.requestParams.getParam( EDIT_FEEDBACK + "0" ).trim(), 0);
		feedback.setElementAt(theHandler.requestParams.getParam( EDIT_FEEDBACK + "1" ).trim(), 1);

		String correctFeed= "The correct order should be:<BLOCKQUOTE>";
		for (int i=0 ; i<choices.size() ; i++)
			correctFeed += Integer.toString(i+1) + ".&nbsp;" + (String)choices.elementAt(i) + "<BR>";
		correctFeed += "</BLOCKQUOTE>";
		correctAnswerFeedback= new VectorAdapter();
		correctAnswerFeedback.addElement(correctFeed);

		if (theTest.getGUI().branchingEnabled) {
 			try {
 				branchCorrect= -1;
                if ( theHandler.requestParams.getParam( EDIT_CORRECTBRANCH + "_BRANCH" ).trim().equals("1") ) {
                	branchCorrect= Integer.parseInt( theHandler.requestParams.getParam( EDIT_CORRECTBRANCH ).trim() );
                    if ((branchCorrect != -1) && (theTest.questions.get(branchCorrect) == null)) branchCorrect= -1;
              }
			} catch (NumberFormatException e) {}
 			try {
 				branchIncorrect= -1;
                if ( theHandler.requestParams.getParam( EDIT_INCORRECTBRANCH + "_BRANCH" ).trim().equals("1") ) {
                	branchIncorrect= Integer.parseInt( theHandler.requestParams.getParam( EDIT_INCORRECTBRANCH ).trim() );
                    if ((branchIncorrect != -1) && (theTest.questions.get(branchIncorrect) == null)) branchIncorrect= -1;
              }
			} catch (NumberFormatException e) {}
 			try {
 				branchUnanswered= -1;
                if ( theHandler.requestParams.getParam( EDIT_UNBRANCH + "_BRANCH" ).trim().equals("1") ) {
                	branchUnanswered= Integer.parseInt( theHandler.requestParams.getParam( EDIT_UNBRANCH ).trim() );
                    if ((branchUnanswered != -1) && (theTest.questions.get(branchUnanswered) == null)) branchUnanswered= -1;
              }
			} catch (NumberFormatException e) {}
		}

		
		int localCount= localRandoms.size();
		localRandoms= new VectorAdapter();
		int globalCount= theTest.globalRandoms.size();
		theTest.globalRandoms= new VectorAdapter();
		randomVariable.updateRandoms( theHandler, localRandoms, localCount, theTest.globalRandoms, globalCount );
				
		editQuestionItem( theHandler, theTest, messages );
	}
	*/
	

	public void collectMedia( ConcurrentHashMap theMedia ) {
	
		super.collectMedia( theMedia );
		
		for (int i=0 ; i<choices.size() ; i++) {
			String theChoice= (String)choices.elementAt(i);
			if (theChoice.length() > 0) {
				try {
					richMedia theItem= new richMedia( theChoice );
					if (theItem.isURL) continue;
					theMedia.put( theItem.mediaString, sqlID );
				} catch (testFormatException e) {}
			}
		}

	}
	
	
	
	/*
	public String v5elxTitle() { return("Ranking Question"); }
	
	public String v5elxTemplate() { return("v5ra.xsl"); }
	
	public String v5elxContent( tp_requestHandler theHandler, test theTest ) 
	{
		String result= "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r";
		
		result += "<question xmlns=\"http://www.mhhe.com/EZTest/\">\r";

		result += "	<stem><p xmlns=\"http://www.w3.org/1999/xhtml\">\r";
		result += toXMLhtml(qtext, theHandler);
		result += "	</p></stem>\r";
	
		result += "	<reference>" + toXMLstring(referenceTag) + "</reference>\r";
		result += "	<page>" + toXMLstring(pageTag) + "</page>\r";

		if (longQuestion)
			result += "	<longquestion>true</longquestion>\r";
		else
			result += "	<longquestion>false</longquestion>\r";

		String theFeedback= "";
		if (feedback.size() > 0)
			theFeedback= (String)feedback.elementAt(0);
		result += "	<explanation><p xmlns=\"http://www.w3.org/1999/xhtml\">\r";
		result += toXMLhtml(theFeedback, theHandler);
		result += "	</p></explanation>\r";
	
		result += "	<raInfo>\r";
		
		VectorAdapter xmlChoices= new VectorAdapter();
		for (int i=0 ; i<choices.size() ; i++) 
		{
			String thisChoice = "			<item>\r";
			thisChoice += "				<itemhtml><p xmlns=\"http://www.w3.org/1999/xhtml\">\r";
			thisChoice += toXMLhtml((String)choices.elementAt(i), theHandler);
			thisChoice += "				</p></itemhtml>\r";
			thisChoice += "			</item>\r";
			
			xmlChoices.add(thisChoice);
		}
		
		if (xmlChoices.size() == 0)
		{
			String thisChoice = "			<item>\r";
			thisChoice += "				<itemhtml><p xmlns=\"http://www.w3.org/1999/xhtml\">\r";
			thisChoice += "				</p></itemhtml>\r";
			thisChoice += "			</item>\r";
			
			xmlChoices.add(thisChoice);
		}
		
		result += "		<items>\r";
		for (int i=0; i< xmlChoices.size(); i++)
			result += (String)xmlChoices.elementAt(i);
		result += " 	</items>\r";

		result += "	</raInfo>\r";
	
		result += "</question>\r";
				
		return(result);
	}
	*/
	

	public void v5update( tp_requestHandler theHandler, test theTest )
	{
		//String theXMLdata= theHandler.getParam("questionxml");
		String theXMLdata= theHandler.getParam("content");
		if (theXMLdata.length() == 0)
		{
			//v5edit( theHandler, theTest );
			_logger.error("update failure in ranking.v5update");
			return;
		}
		
		//System.out.println(theXMLdata);
		String theFeedback= "";
		String theAnswer= "";
		boolean correctSet= false;
		
		try 
		{
			SAXBuilder builder = new SAXBuilder();
			Document theXML = builder.build(new ByteArrayInputStream(theXMLdata.getBytes()));
			Element theQ= theXML.getRootElement();
			
			java.util.List theData= theQ.getChildren();
			ListIterator iter= theData.listIterator();
			
			//System.out.println( "XML data has " + Integer.toString(theData.size()) + " primary elements");
			
			while (iter.hasNext()) 
			{
				Element thisItem= (Element)iter.next();
				//System.out.println("  " + thisItem.getName());
				
				if (thisItem.getName().equals("stem"))
					qtext= embeddedMedia.processMediaIn( theHandler, theTest, getXMLhtml(thisItem, theHandler), sqlID );
				else if (thisItem.getName().equals("reference"))
					referenceTag= getXMLstring(thisItem);
				else if (thisItem.getName().equals("page"))
					pageTag= getXMLstring(thisItem);
				else if (thisItem.getName().equals("longquestion"))
					longQuestion= getXMLstring(thisItem).equals("true");
				else if (thisItem.getName().equals("explanation"))
				{
					theFeedback= embeddedMedia.processMediaIn( theHandler, theTest, getXMLhtml(thisItem, theHandler), sqlID );
					feedback= new VectorAdapter();
				}
				else if (thisItem.getName().equals("raInfo"))
				{
					java.util.List raData= thisItem.getChildren();
					ListIterator iter2= raData.listIterator();
					while (iter2.hasNext()) 
					{
						Element raItem= (Element)iter2.next();
						//System.out.println("    " + mcItem.getName());

						if (raItem.getName().equals("items"))
						{
							choices= new VectorAdapter();
							feedback= new VectorAdapter();
							points= new VectorAdapter();
							correctAnswerFeedback= new VectorAdapter();
							
							java.util.List choiceData= raItem.getChildren();
							ListIterator iter3= choiceData.listIterator();
							for (int i=0; iter3.hasNext(); i++) 
							{
								Element choiceItem= (Element)iter3.next();

								if (choiceItem.getName().equals("item"))
								{
									java.util.List thisChoiceData= choiceItem.getChildren();
									ListIterator iter4= thisChoiceData.listIterator();
									while (iter4.hasNext()) 
									{
										Element htmlItem= (Element)iter4.next();
										
										if (htmlItem.getName().equals("itemhtml"))
										{
											String thisChoice= embeddedMedia.processMediaIn( theHandler, theTest, getXMLhtml(htmlItem, theHandler), sqlID );
											//System.out.println("choice: " + thisChoice);
											choices.addElement( thisChoice );
											feedback.addElement( theFeedback );
											points.addElement("1");
											correctAnswerFeedback.addElement("should be ranked #" + Integer.toString(choices.size()));
										}
									}
								}
							}
						}
					}
				}
			}
		}
		catch (IOException io)
		{
			_logger.error("XML IO error in ranking.v5update()", io);
		}
		catch (JDOMException jd) 
		{
			_logger.error("XML parsing error in ranking.v5update()", jd);
		}
		
		updateMediaConsumers( theHandler, theTest );
		
		/*
		String theNextQ= theHandler.getParam("NEXTQ");
		if (theNextQ.length() > 0)
		{
			if (theNextQ.equals("NEW"))
			{
				theTest.v5newQ(theHandler);
			}
			else
			{
				theHandler.replaceParam(test.Q_ID ,theNextQ);
				theTest.v5editPane(theHandler);
			}
		}
		else
			v5edit( theHandler, theTest );
		*/
	}
	
	/*
	public void v5editv1( tp_requestHandler theHandler, test theTest )
	{
		super.v5editv1( theHandler, theTest );
		
		String pageID= theHandler.getParam(test.PAGE_ID);
		
		theHandler.snd( "<TR><TD COLSPAN=2 style=\"vertical-align: top; text-align: right\"><B><A onMouseOver=\"(window.status='get help...'); return true;\" HREF=\"javascript:helpMessage('v5/rachoices.htm');\">Answers</A></B></TD><TD>");
		theHandler.snd("<TABLE CELLSPACING=\"0\" CELPADDING=\"2\">");
		theHandler.snd("<TR><TH>#</TH><TH>Choice</TH><TH>Delete</TH></TR>");
		for (int i=0 ; i<choices.size() ; i++) {
			String color= "tr2";
			if ((i%2)==0) color="tr1";
			theHandler.snd("<TR class=\"" + color + "\"><TH>" + Integer.toString(i+1) + "</TH>");
			theHandler.snd("<TD>" + v5ui.htmlArea( EDIT_CHOICE + Integer.toString(i), 400, 75, (String)choices.elementAt(i), pageID) + "</TD>");
			theHandler.snd("<TD style=\"text-align: center; vertical-align: middle\">" + v5ui.checkbox( EDIT_DEL + Integer.toString(i), 20, 20, false, " ", pageID) + "</TD></TR>");
		}
		
		theHandler.snd("<TR><TH>Add<br>Another</TH><TD class=\"tr3\" COLSPAN=2>" + v5ui.htmlArea( EDIT_CHOICE, 400, 75, "", pageID) + "</TD></TR>");

		theHandler.snd("<TR><TH>&nbsp;</TH><TD class=\"tr3\" COLSPAN=2><SPAN class=\"info\">enter choices in correct ranking order</SPAN></TD></TR>");
		theHandler.snd("</TABLE>");
		theHandler.snd( "<BR>&nbsp;</TD></TR>");

		// feedback
		theHandler.snd( "<TR><TD COLSPAN=2 style=\"vertical-align: top; text-align: right\"><B><A onMouseOver=\"(window.status='get help...'); return true;\" HREF=\"javascript:helpMessage('v5/qfeedback.htm');\">Explanation</A></B></TD><TD style=\"text-align: left\">");
		String theFeedback= "";
		if (feedback.size() > 0)
			theFeedback= (String)feedback.elementAt(0);
		theHandler.snd( v5ui.htmlArea( EDIT_FEEDBACK, 450, 100, theFeedback, pageID) );
		theHandler.snd( "<BR>&nbsp;</TD></TR>");
		
		super.v5finishEdit( theHandler, theTest );
	}
	
	
	public void v5updatev1( tp_requestHandler theHandler, test theTest )
	{
		super.v5updatev1( theHandler, theTest );
		
		// default for print-based tests
		usePopup= false;
		storage= STORE_choices;
 		branchCorrect= -1;
        branchIncorrect= -1;
        branchUnanswered= -1;
		
		// feedback
		String theFeedback= theHandler.getHTMLParam( EDIT_FEEDBACK ).trim();
		feedback= new VectorAdapter();
		
		int count= choices.size();
		choices= new VectorAdapter();
		points= new VectorAdapter();
		correctAnswerFeedback= new VectorAdapter();
		
		// old choices
		int choiceCount= 0;
		for (int i=0 ; i<count ; i++) {
			String checkbox= theHandler.getParam( EDIT_DEL + Integer.toString(i) ).trim();
			if (checkbox.equals("0")) {
				String theChoice= theHandler.getHTMLParam( EDIT_CHOICE + Integer.toString(i) ).trim();
				choices.addElement(theChoice);
				points.addElement("1");
				correctAnswerFeedback.addElement(theChoice + " should be ranked #" + Integer.toString(++choiceCount) + "<BR>");
				feedback.addElement(theFeedback);
			}
		}
		
		// new choice
		String theChoice= theHandler.getHTMLParam( EDIT_CHOICE ).trim();
		if (theChoice.length() > 0) {
			choices.addElement(theChoice);
			points.addElement("1");
			correctAnswerFeedback.addElement(theChoice + " should be ranked #" + Integer.toString(++choiceCount) + "<BR>");
			feedback.addElement(theFeedback);
		}
		
		updateMediaConsumers( theHandler, theTest );
	}
	*/
	

	public void buildReferences( VectorAdapter references )
	{
		richMedia.buildReferences( references, qtext );
		buildHintReferences( references );
		buildTooltipReferences( references );
		richMedia.buildReferences( references, questionProperties.getString(ATTACHED_MEDIA, "") );
		richMedia.buildReferences( references, questionProperties.getString(COMMON_FEEDBACK, "") );
		richMedia.buildReferences( references, questionProperties.getString(PROBLEM_SOLUTION, "") );

		for (int i=0 ; i<choices.size() ; i++) {
			String theChoice= (String)choices.elementAt(i);
			richMedia.buildReferences(references, theChoice);
		}
		
		for (int i=0 ; i<feedback.size(); i++) {
			String theFeedback= (String)feedback.elementAt(i);
			richMedia.buildReferences(references, theFeedback);
		}
		
		// media in pooled random variables
		for (int i=0 ; i<localRandoms.size() ; i++) 
		{
			randomVariable theVar= (randomVariable)localRandoms.elementAt(i);
			richMedia.buildReferences(references, theVar.getPoolString());
		}
	}


	public void updateMedia( String oldName, String newName)
	{
		super.updateMedia(oldName, newName);

		for (int i=0 ; i<choices.size() ; i++) {
			String theChoice= (String)choices.elementAt(i);
			choices.setElementAt(tp_utils.substitute(theChoice, "%media:" + oldName + "%", "%media:" + newName + "%"), i);
		}
		
		for (int i=0 ; i<feedback.size(); i++) {
			String theFeedback= (String)feedback.elementAt(i);
			feedback.setElementAt(tp_utils.substitute(theFeedback, "%media:" + oldName + "%", "%media:" + newName + "%"), i);
		}
	}


	public String v5paperHTML( tp_requestHandler theHandler, test theTest, parameters theData, String substitutedText, VectorAdapter keyItem ) {
		
		String resultHTML= "";
		String keyHTML= "";
		
		VectorAdapter indexVector= new VectorAdapter();
		VectorAdapter choiceVector= new VectorAdapter();
		for (int i=0 ; i<choices.size() ; i++) {
			String theChoice= (String)choices.elementAt(i);
			try {
				richMedia theMedia= new richMedia( theChoice );
				theMedia.setChoice(true);
				theChoice= theMedia.html(theHandler, "", 0, 0);
			} catch (testFormatException e) {}
			
			theChoice= randomVariable.deReference(theChoice, theTest, sqlID, theData);
			theChoice= richMedia.deReference(theChoice, theHandler, sqlID, theData);

			choiceVector.addElement("____&nbsp;&nbsp;" + theChoice + "<BR>");
			indexVector.addElement(Integer.toString(i+1));
			
			if (theTest.getGUI().terseKey) {
				if (keyHTML.length() > 0)
					keyHTML += "<span class=\"rspHelpStyle\">&nbsp;&nbsp;-&nbsp;&nbsp;</span>";
				keyHTML += theChoice;
			}
		}
		
		// here is where we will randomize the order
		while (choiceVector.size() > 0) {
			if (choiceVector.size() == 1) {
				resultHTML += (String)choiceVector.elementAt(0);
				if (!theTest.getGUI().terseKey) keyHTML += "<B><U>" + (String)indexVector.elementAt(0) + "</U></B>" + ((String)choiceVector.elementAt(0)).substring(4);
				choiceVector.removeElementAt(0);
				indexVector.removeElementAt(0);
			}
			else {
				double calc= Math.random() * choiceVector.size();
				resultHTML += (String)choiceVector.elementAt((int)calc);
				if (!theTest.getGUI().terseKey) keyHTML += "<B><U>" + (String)indexVector.elementAt((int)calc) + "</U></B>" + ((String)choiceVector.elementAt((int)calc)).substring(4);
				choiceVector.removeElementAt((int)calc);
				indexVector.removeElementAt((int)calc);
			}
		}
		
		if (!theTest.getGUI().terseKey)
		{
			if (feedback.size() > 0)
			{
				String theFeedback= (String)feedback.elementAt(0);
				theFeedback= randomVariable.deReference(theFeedback, theTest, sqlID, theData);
				theFeedback= richMedia.deReference(theFeedback, theHandler, sqlID, theData);
				if (theFeedback.trim().length() > 0) keyHTML += "<p class=\"feedbackStyle\">" + theFeedback + "</p>";
			}
		}
		
		if (keyItem != null) keyItem.addElement(keyHTML);
			
		return(resultHTML);
	}
	
	
	public String v5roundrobin( tp_requestHandler theHandler, test theTest, ConcurrentHashMap references )
	{
		String result= v5roundrobinStart( theHandler, theTest, references );
		
		for (int i=0 ; i<choices.size() ; i++) 
		{
			String theChoice= (String)choices.elementAt(i);
			theChoice= richMedia.deReference(theChoice, theHandler);
			
			result += "<p>" + theChoice + "</p>\n";
		}
		
		if (feedback.size() > 0)
		{
			String theFeedback= (String)feedback.elementAt(0);
			theFeedback= richMedia.deReference(theFeedback, theHandler);
			if (theFeedback.trim().length() > 0) 
				result += "<p>Feedback: " + theFeedback + "</p>\n";
		}

		result += v5roundrobinEnd( theHandler, theTest, references );
		return(result);
	}


	public String v5rtf( tp_requestHandler theHandler, test theTest, parameters theData, String substitutedText, VectorAdapter keyItem ) 
	{
		String resultHTML= "";
		String keyHTML= "";
		
		VectorAdapter indexVector= new VectorAdapter();
		VectorAdapter choiceVector= new VectorAdapter();
		for (int i=0 ; i<choices.size() ; i++) 
		{
			String theChoice= (String)choices.elementAt(i);
			theChoice= randomVariable.deReference(theChoice, theTest, sqlID, theData);
			theChoice= richMedia.deReference(theChoice, theHandler, sqlID, theData);

			choiceVector.addElement("____&nbsp;&nbsp;" + theChoice);
			indexVector.addElement(Integer.toString(i+1));
			
			if (theTest.getGUI().terseKey) 
			{
				if (keyHTML.length() > 0)
					keyHTML += "<span class=\"rspHelpStyle\">&nbsp;&nbsp;-&nbsp;&nbsp;</span>";
				keyHTML += theChoice;
			}
		}
		
		resultHTML += "<span class=\"rspStyle\">";
		if (!theTest.getGUI().terseKey) keyHTML += "<span class=\"rspStyle\">";

		// here is where we will randomize the order
		for (int count=0; choiceVector.size() > 0; count++) 
		{
			if (count != 0)
			{
				resultHTML += "<br>";
				if (!theTest.getGUI().terseKey) keyHTML += "<br>";
			}
			
			if (choiceVector.size() == 1) 
			{
				resultHTML += (String)choiceVector.elementAt(0);
				if (!theTest.getGUI().terseKey) keyHTML += "<span style=\"font-weight: bold; text-decoration: underline\">" + (String)indexVector.elementAt(0) + "</span><span style=\"font-weight: plain; text-decoration: none\">" + ((String)choiceVector.elementAt(0)).substring(4) + "</span>";
				choiceVector.removeElementAt(0);
				indexVector.removeElementAt(0);
			}
			else 
			{
				double calc= Math.random() * choiceVector.size();
				resultHTML += (String)choiceVector.elementAt((int)calc);
				if (!theTest.getGUI().terseKey) keyHTML += "<span style=\"font-weight: bold; text-decoration: underline\">" + (String)indexVector.elementAt((int)calc) + "</span><span style=\"font-weight: plain; text-decoration: none\">" + ((String)choiceVector.elementAt((int)calc)).substring(4) + "</span>";
				choiceVector.removeElementAt((int)calc);
				indexVector.removeElementAt((int)calc);
			}
		}
		
		resultHTML += "</span>";
		if (!theTest.getGUI().terseKey) keyHTML += "</span>";

		if (!theTest.getGUI().terseKey)
		{
			String theFeedback= "";

			String commonFeedback= questionProperties.getString(COMMON_FEEDBACK, null);
			if (commonFeedback == null || ("").equals(commonFeedback)) {
				if (feedback.size() > 0)
					commonFeedback= ((String)feedback.elementAt(0)).trim();
				else
					commonFeedback= "";
			}
			
			theFeedback= commonFeedback;
			if(theFeedback.trim().length() > 0){
				theFeedback= tooltipDeReference(theFeedback);
				theFeedback= randomVariable.deReference(theFeedback, theTest, sqlID, theData);
				theFeedback= richMedia.deReference(theFeedback, theHandler, sqlID, theData);
				if (theFeedback.trim().length() > 0) keyHTML += "<p class=\"feedbackStyle\">" + theFeedback + "</p>";
			}
		}
		
		if (keyItem != null) keyItem.addElement(keyHTML);
			
		return(resultHTML);
	}
	
	

	public void v5vista( tp_requestHandler theHandler, test theTest, exporter_vista theExporter, int count ) 
	{
		String myQTIid= "QUE_" + theExporter.uniqueID();
		
		// add element to scores
		Element thisScore= new Element("score", theExporter.ns_webCT_np);
		thisScore.setAttribute("linkrefid", myQTIid + "_S");
		thisScore.addContent("1");
		theExporter.scores.addContent( thisScore );
	
		// add element to selection
		Element thisSelect= new Element("select", theExporter.ns_webCT_np);
		thisSelect.setAttribute("linkrefid", myQTIid + "_S");
		thisSelect.addContent("1");
		theExporter.selection.addContent( thisSelect );
	
		// add element to assessment
		Element thisSection= new Element("section", theExporter.ns_imsQuestion);
		thisSection.setAttribute("ident", myQTIid + "_S");
		theExporter.assessment.addContent( thisSection );

		Element sectionRef= new Element("itemref", theExporter.ns_imsQuestion);
		sectionRef.setAttribute("linkrefid", myQTIid);
		thisSection.addContent( sectionRef );


		// build db entry
		Element item= new Element("item", theExporter.ns_imsQuestion);
		item.setAttribute("title", "Question #" + Integer.toString(count));
		item.setAttribute("ident", myQTIid);
		item.addContent(new Comment("EZ Test - ranking question"));
		theExporter.section.addContent( item );
		
		Element metadata= new Element("itemmetadata", theExporter.ns_imsQuestion);
		item.addContent(metadata);
		Element subelement= new Element("qmd_itemtype", theExporter.ns_imsQuestion);
		subelement.addContent("Logical Identifier");
		metadata.addContent(subelement);
		
		Element qtimeta= new Element("qtimetadata", theExporter.ns_imsQuestion);
		metadata.addContent(qtimeta);
		subelement= new Element("vocabulary", theExporter.ns_imsQuestion);
		subelement.setAttribute("uri", "webct_imsqti_metadatav1p0.txt");
		subelement.setAttribute("vocab_type", "text/plain");
		qtimeta.addContent(subelement);
		
		Element qtifield= new Element("qtimetadatafield", theExporter.ns_imsQuestion);
		qtimeta.addContent(qtifield);
		
		subelement= new Element("fieldlabel", theExporter.ns_imsQuestion);
		subelement.addContent("wct_m_match_short_format");
		qtifield.addContent(subelement);
		subelement= new Element("fieldentry", theExporter.ns_imsQuestion);
		subelement.addContent("Yes");
		qtifield.addContent(subelement);
		
		qtifield= new Element("qtimetadatafield", theExporter.ns_imsQuestion);
		qtimeta.addContent(qtifield);
		
		subelement= new Element("fieldlabel", theExporter.ns_imsQuestion);
		subelement.addContent("wct_m_statement_short_format");
		qtifield.addContent(subelement);
		subelement= new Element("fieldentry", theExporter.ns_imsQuestion);
		subelement.addContent("Yes");
		qtifield.addContent(subelement);
		
		Element presentation= new Element("presentation", theExporter.ns_imsQuestion);
		item.addContent(presentation);
		
		Element material= new Element("material", theExporter.ns_imsQuestion);
		presentation.addContent(material);
		
		
		String baseURL= theHandler.getParam(v5test.USE_BASEURL);
		String stem= "";
		
		// handle references
		if (referenceTag.length() > 0)
		{
			question refQ= theTest.questions.getReference(referenceTag);
			if (refQ != null)
			{
				stem= randomVariable.deReference(refQ.qtext, theTest, refQ.sqlID, theHandler.requestParams);
				stem += "<br>&nbsp;<br>";
			}
		}
		
		// subsitute random variables into the text, process media references, do not number
		stem += randomVariable.deReference(qtext, theTest, sqlID, theHandler.requestParams);
		stem= richMedia.deReference(stem, baseURL, true);
		stem= tp_utils.substitute( stem, question.SUBTAG3, "&nbsp;");
		
		Element stemElement= new Element("mattext", theExporter.ns_imsQuestion);
		stemElement.setAttribute("texttype", "text/html");
		stemElement.addContent(stem);
		material.addContent(stemElement);
		
		
		Element left_group= new Element("material", theExporter.ns_imsQuestion);
		left_group.setAttribute("label", "left group");
		presentation.addContent(left_group);
		
		Element right_group= new Element("material", theExporter.ns_imsQuestion);
		right_group.setAttribute("label", "right group");
		presentation.addContent(right_group);
		

		Element resprocessing= new Element("resprocessing", theExporter.ns_imsQuestion);
		resprocessing.setAttribute("scoremodel", "SumofScores");
		item.addContent(resprocessing);
		
		subelement= new Element("qticomment", theExporter.ns_imsQuestion);
		subelement.addContent("Ranking Question will be scored as matching question with standard response processing.");
		resprocessing.addContent(subelement);
		
		Element outcomes= new Element("outcomes", theExporter.ns_imsQuestion);
		resprocessing.addContent(outcomes);
		
		int max= 100;
		int perMatch= 100;
		if (choices.size() > 0)
		{
			perMatch= max / choices.size();
			max= perMatch * choices.size();
		}
		
		subelement= new Element("decvar", theExporter.ns_imsQuestion);
		subelement.setAttribute("vartype", "Integer");
		subelement.setAttribute("defaultval", "0");
		subelement.setAttribute("varname", "que_score");
		subelement.setAttribute("maxvalue", Integer.toString(max));
		subelement.setAttribute("minvalue", "0");
		outcomes.addContent(subelement);
		
		subelement= new Element("decvar", theExporter.ns_imsQuestion);
		subelement.setAttribute("vartype", "Integer");
		subelement.setAttribute("defaultval", "0");
		subelement.setAttribute("varname", "WebCT_Correct");
		subelement.setAttribute("maxvalue", Integer.toString(choices.size()));
		subelement.setAttribute("minvalue", "0");
		outcomes.addContent(subelement);
		
		subelement= new Element("decvar", theExporter.ns_imsQuestion);
		subelement.setAttribute("vartype", "Integer");
		subelement.setAttribute("defaultval", "0");
		subelement.setAttribute("varname", "WebCT_Incorrect");
		subelement.setAttribute("minvalue", "0");
		outcomes.addContent(subelement);
		


		for (int i=0; i< choices.size(); i++)
		{
			String theChoice= Integer.toString(i+1);
			
			String theMatch= (String)choices.elementAt(i);
			theMatch= randomVariable.deReference(theMatch, theTest, sqlID, theHandler.requestParams);
			theMatch= richMedia.deReference(theMatch, baseURL, true);
				
			
			// add them to the groups
			subelement= new Element("mattext", theExporter.ns_imsQuestion);
			subelement.setAttribute("texttype", "text/html");
			subelement.addContent(theChoice);
			left_group.addContent(subelement);
				
			subelement= new Element("mattext", theExporter.ns_imsQuestion);
			subelement.setAttribute("texttype", "text/html");
			subelement.addContent(theMatch);
			right_group.addContent(subelement);


			String choiceID= myQTIid + "_RG" + Integer.toString(i+1);

			// qdb presentation response group entry
			Element response_grp= new Element("response_grp", theExporter.ns_imsQuestion);
			response_grp.setAttribute("ident", choiceID);
			response_grp.setAttribute("rcardinality", "Single");
			response_grp.setAttribute("rtiming", "No");
			presentation.addContent( response_grp );
			
			material= new Element("material", theExporter.ns_imsQuestion);
			response_grp.addContent(material);
			
			subelement= new Element("mattext", theExporter.ns_imsQuestion);
			subelement.setAttribute("texttype", "text/html");
			subelement.addContent(theChoice);
			material.addContent(subelement);
			
			Element render_choice= new Element("render_choice", theExporter.ns_imsQuestion);
			response_grp.addContent(render_choice);
			
			for (int j=0 ; j<choices.size() ; j++) 
			{
				theMatch= (String)choices.elementAt(j);
				theMatch= randomVariable.deReference(theMatch, theTest, sqlID, theHandler.requestParams);
				theMatch= richMedia.deReference(theMatch, baseURL, true);

				String matchID= myQTIid + "_A" + Integer.toString(j+1);

				Element response_label= new Element("response_label", theExporter.ns_imsQuestion);
				response_label.setAttribute("ident", matchID);
				render_choice.addContent(response_label);
				
				material= new Element("material", theExporter.ns_imsQuestion);
				response_label.addContent(material);
				
				subelement= new Element("mattext", theExporter.ns_imsQuestion);
				subelement.setAttribute("texttype", "text/html");
				subelement.addContent(theMatch);
				material.addContent(subelement);
			
				//  qdb resprocessing respcondition entry
				Element respcondition= new Element("respcondition", theExporter.ns_imsQuestion);
				respcondition.setAttribute("title", "if user matches " + choiceID + " to " + matchID);
				resprocessing.addContent(respcondition);
				
				Element conditionvar= new Element("conditionvar", theExporter.ns_imsQuestion);
				respcondition.addContent(conditionvar);
				
				Element varequal= new Element("varequal", theExporter.ns_imsQuestion);
				varequal.setAttribute("respident", choiceID);
				varequal.addContent(matchID);
				conditionvar.addContent(varequal);
				
				String varName= "WebCT_Incorrect";
				if (i==j)
					varName= "WebCT_Correct";

				Element setvar= new Element("setvar", theExporter.ns_imsQuestion);
				setvar.setAttribute("varname", varName);
				setvar.setAttribute("action", "Add");
				setvar.addContent("1");
				respcondition.addContent(setvar);
			}
		}


		//  qdb resprocessing respcondition scoring entry
		Element respcondition= new Element("respcondition", theExporter.ns_imsQuestion);
		respcondition.setAttribute("title", "score calculation");
		resprocessing.addContent(respcondition);
		
		Element conditionvar= new Element("conditionvar", theExporter.ns_imsQuestion);
		respcondition.addContent(conditionvar);
		
		Element logicalAnd= new Element("and", theExporter.ns_imsQuestion);
		conditionvar.addContent(logicalAnd);
		
		subelement= new Element("other", theExporter.ns_imsQuestion);
		logicalAnd.addContent(subelement);
		
		subelement= new Element("not", theExporter.ns_imsQuestion);
		logicalAnd.addContent(subelement);
		Element other= new Element("other", theExporter.ns_imsQuestion);
		subelement.addContent(other);
		
		Element setvar= new Element("setvar", theExporter.ns_imsQuestion);
		setvar.setAttribute("varname", "que_score");
		setvar.setAttribute("action", "Set");
		setvar.addContent("0");
		respcondition.addContent(setvar);
		
		setvar= new Element("setvar", theExporter.ns_imsQuestion);
		setvar.setAttribute("varname", "que_score");
		setvar.setAttribute("action", "Add");
		setvar.addContent("WebCT_Correct");
		respcondition.addContent(setvar);
		
		setvar= new Element("setvar", theExporter.ns_imsQuestion);
		setvar.setAttribute("varname", "que_score");
		setvar.setAttribute("action", "Multiply");
		setvar.addContent(Integer.toString(perMatch));
		respcondition.addContent(setvar);

		subelement= new Element("displayfeedback", theExporter.ns_imsQuestion);
		subelement.setAttribute("feedbacktype", "Response");
		subelement.setAttribute("linkrefid", myQTIid + "_ALL");
		respcondition.addContent(subelement);
		
		
		//  qdb resprocessing respcondition other entry
		respcondition= new Element("respcondition", theExporter.ns_imsQuestion);
		respcondition.setAttribute("title", "score calculation");
		resprocessing.addContent(respcondition);
		
		conditionvar= new Element("conditionvar", theExporter.ns_imsQuestion);
		respcondition.addContent(conditionvar);
		
		subelement= new Element("other", theExporter.ns_imsQuestion);
		conditionvar.addContent(subelement);
		
		subelement= new Element("displayfeedback", theExporter.ns_imsQuestion);
		subelement.setAttribute("feedbacktype", "Response");
		subelement.setAttribute("linkrefid", myQTIid + "_ALL");
		respcondition.addContent(subelement);


		String theFeedback= "";
		if (feedback.size() > 0) 
		{
			theFeedback= ((String)feedback.elementAt(0)).trim();
			theFeedback= randomVariable.deReference(theFeedback, theTest, sqlID, theHandler.requestParams);
			theFeedback= richMedia.deReference(theFeedback, baseURL, true).trim();
		}
		
		String suppInfo= "";
		if (theHandler.getParam(test.EXPORT_INFO).equals("true")) suppInfo= supplementaryKludge( theHandler );
		if (suppInfo.length() > 0)
		{
			if (theFeedback.length() > 0) theFeedback += "<br>&nbsp;<br>";
			theFeedback += suppInfo;
		}

		Element itemfeedback= new Element("itemfeedback", theExporter.ns_imsQuestion);
		itemfeedback.setAttribute("ident", myQTIid + "_ALL");
		itemfeedback.setAttribute("view", "ALL");
		item.addContent(itemfeedback);

		material= new Element("material", theExporter.ns_imsQuestion);
		itemfeedback.addContent(material);
		
		subelement= new Element("mattext", theExporter.ns_imsQuestion);
		subelement.setAttribute("texttype", "text/html");
		subelement.addContent( new CDATA(theFeedback) );
		material.addContent(subelement);
	}


	public String v5webct( tp_requestHandler theHandler, test theTest, parameters randomData )
	{
		String webCTcode= ":TYPE:P\n";

		webCTcode += ":TITLE:" + theHandler.getParam("webCTtitlePrefix") + " #" + theHandler.getParam("webCTtitleSuffix") + "\n";
		/*
		webCTcode += ":TITLE:";
		String tmpTitle= stemNoMedia(theTest, true);
		if (tmpTitle.length() > 42) tmpTitle= tmpTitle.substring(0,42);
		webCTcode += tmpTitle + "\n";
		*/
		
		
		String refHTML= "";
		if (referenceTag.length() > 0)
		{
			question refQ= theTest.questions.getReference(referenceTag);
			if (refQ != null)
			{
				refHTML= randomVariable.deReference(refQ.qtext, theTest, refQ.sqlID, randomData);
				
				// handle v5 media references
				refHTML= richMedia.deReference(refHTML, theHandler.getParam(v5test.USE_BASEURL) );
				
				// do not number
				refHTML= tp_utils.substitute( refHTML, question.SUBTAG3, "&nbsp;");
				
				refHTML += "<br>&nbsp;<br>";
			}
		}
		
		String stemHTML= "";
		
		// subsitute random variables into the text
		String substitutedText= randomVariable.deReference(qtext, theTest, sqlID, randomData);
		
		// handle v5 media references
		substitutedText= richMedia.deReference(substitutedText, theHandler.getParam(v5test.USE_BASEURL));
		stemHTML += substitutedText;

		// do not number
		stemHTML= tp_utils.substitute( stemHTML, question.SUBTAG3, "&nbsp;");
		
		webCTcode += ":QUESTION:H:60:5 \n" + refHTML.trim() + stemHTML.trim() + "\n";

		String answerData= "Items should be ranked in the following order:<ol>";
		for (int i=0 ; i<choices.size() ; i++) 
		{
			String theChoice= (String)choices.elementAt(i);
			answerData += "<li>" + theChoice + "</li>";
		}
		answerData += "</ol>";
		
		answerData= randomVariable.deReference(answerData, theTest, sqlID, randomData);
		answerData= richMedia.deReference(answerData, theHandler.getParam(v5test.USE_BASEURL) );
		webCTcode += ":ANSWER1:" + fixWebCTurls(answerData) + "::0:0\n";

		String theFeedback= ((String)feedback.elementAt(0)).trim();
		theFeedback= randomVariable.deReference(theFeedback, theTest, sqlID, randomData);
		theFeedback= richMedia.deReference(theFeedback, theHandler.getParam(v5test.USE_BASEURL) ).trim();
			
		String suppInfo= "";
		if (theHandler.getParam(test.EXPORT_INFO).equals("true")) suppInfo= supplementaryKludge( theHandler );
		if (suppInfo.length() > 0)
		{
			if (theFeedback.length() > 0) theFeedback += "<br>&nbsp;<br>";
			theFeedback += suppInfo;
		}

		if (theFeedback.length() > 0) 
			webCTcode += ":FEEDBACK\n" + theFeedback + "\n";
		
		String category= theHandler.getParam(v5test.WEBCT_CAT);
		if (category.length() == 0) category= "TP";
		webCTcode += ":CAT:" + category + "\n";
		
		webCTcode += "\n";
		
		return( webCTcode );
	}



	public Element bb9( tp_requestHandler theHandler, String mediaBaseURL )
	{
		Element item= new Element("item");
		item.setAttribute("maxattempts", "0");
		
		item.addContent( bbExporter.stdItemMetaData( theHandler, this, "Ordering" ) );
		
		Element presentation= new Element("presentation");
		item.addContent( presentation );

		Element resprocessing= new Element("resprocessing");
		item.addContent( resprocessing );
		resprocessing.setAttribute("scoremodel", "SumOfScores");
		
		Element flowOuter= new Element("flow");
		presentation.addContent( flowOuter );
		flowOuter.setAttribute("class", "Block");
		
		flowOuter.addContent( bbExporter.stdQuestionBlock( theHandler, this, mediaBaseURL ) );

		if (choices.size() > 0)
		{
			Element outcomes= new Element("outcomes");
			resprocessing.addContent(outcomes);
			
			outcomes.addContent( bbExporter.stdDecVar() );			
			
			Element correctCondition= new Element("respcondition");
			resprocessing.addContent(correctCondition);
			correctCondition.setAttribute("title", "correct");
			
			Element correctVar= new Element("conditionvar");
			correctCondition.addContent(correctVar);
			
			Element theAND= new Element("and");
			correctVar.addContent(theAND);
			
			correctCondition.addContent( bbExporter.stdScoreMax() );
			
			correctCondition.addContent( bbExporter.stdDisplayFeedback( "correct" ) );

			
			Element incorrectCondition= new Element("respcondition");
			resprocessing.addContent(incorrectCondition);
			incorrectCondition.setAttribute("title", "incorrect");
			
			Element incorrectVar= new Element("conditionvar");
			incorrectCondition.addContent(incorrectVar);
			
			Element incorrectCompare= new Element("other");
			incorrectVar.addContent( incorrectCompare );
			
			incorrectCondition.addContent(bbExporter.stdScoreZero());
			
			incorrectCondition.addContent( bbExporter.stdDisplayFeedback( "incorrect" ) );
			
			
		
			String theFeedback= (String)feedback.elementAt(0);
			theFeedback= randomVariable.deReference(theFeedback, theHandler.currentTest, sqlID, theHandler.requestParams);
			theFeedback= richMedia.deReference(theFeedback, mediaBaseURL ).trim();
			
			String suppInfo= "";
			if (theHandler.getParam(test.EXPORT_INFO).equals("true")) suppInfo= supplementaryKludge( theHandler );
			if (suppInfo.length() > 0) theFeedback += "<br>&nbsp;<br>" + suppInfo;
			
			Element correctitemfeedback= new Element("itemfeedback");
			item.addContent( correctitemfeedback );
			correctitemfeedback.setAttribute("ident", "correct");
			correctitemfeedback.setAttribute("view", "All");
			
			Element correctitemflow= new Element("flow_mat");
			correctitemfeedback.addContent( correctitemflow );
			correctitemflow.setAttribute("class", "Block");
			
			correctitemflow.addContent( bbExporter.stdFormattedTextBlock(new CDATA(theFeedback)) );
			
			Element incorrectitemfeedback= new Element("itemfeedback");
			item.addContent( incorrectitemfeedback );
			incorrectitemfeedback.setAttribute("ident", "incorrect");
			incorrectitemfeedback.setAttribute("view", "All");
			
			Element incorrectitemflow= new Element("flow_mat");
			incorrectitemfeedback.addContent( incorrectitemflow );
			incorrectitemflow.setAttribute("class", "Block");
			
			incorrectitemflow.addContent( bbExporter.stdFormattedTextBlock(new CDATA(theFeedback)) );		

				
			Element responseBlock= new Element("flow");
			flowOuter.addContent( responseBlock );
			responseBlock.setAttribute("class", "RESPONSE_BLOCK");
			
			Element response_lid= new Element("response_lid");
			responseBlock.addContent( response_lid );
			response_lid.setAttribute("ident", "response");
			response_lid.setAttribute("rcardinality", "Ordered");
			response_lid.setAttribute("rtiming", "No");
			
			Element render_choice= new Element("render_choice");
			response_lid.addContent( render_choice );
			render_choice.setAttribute("maxnumber", "0");
			render_choice.setAttribute("minnumber", "0");
			render_choice.setAttribute("shuffle", "No");
			

			for (int i=0 ; i<choices.size() ; i++) 
			{
				String cid= sqlID + "_" + Integer.toString(i);
				
				String theChoice= (String)choices.elementAt(i);
				try 
				{
					richMedia theMedia= new richMedia( theChoice );
					theMedia.setChoice(true);
					theChoice= theMedia.html(theHandler, "", 0, 0);
				} 
				catch (testFormatException e) {}
				
				theChoice= randomVariable.deReference(theChoice, theHandler.currentTest, sqlID, theHandler.requestParams);
				theChoice= richMedia.deReference(theChoice, mediaBaseURL);
				

				Element flow_label= new Element("flow_label");
				render_choice.addContent( flow_label );
				flow_label.setAttribute("class", "Block");
				
				Element response_label= new Element("response_label");
				flow_label.addContent( response_label );
				response_label.setAttribute("ident", cid);
				response_label.setAttribute("rarea", "Ellipse");
				response_label.setAttribute("rrange", "Exact");
				response_label.setAttribute("shuffle", "Yes");
				
				response_label.addContent( bbExporter.stdFormattedTextBlock(new CDATA(theChoice)) );
				//response_label.addContent( bbExporter.stdFileBlock() );
				//response_label.addContent( bbExporter.stdLinkBlock() );
				
				
				Element correctCompare= new Element("varequal");
				theAND.addContent( correctCompare );
				correctCompare.setAttribute("case", "No");
				correctCompare.setAttribute("respident", "response");
				correctCompare.setText(cid);
			}
		}
				
		return item;
	}



	public boolean v5bboard( tp_requestHandler theHandler, test theTest, parameters randomData, int questionCounter, Element thePool, Element theQList )
	{
		String elementName= "QUESTION_ESSAY";

		Element qListElement= new Element("QUESTION");
		qListElement.setAttribute("id", "q" + Integer.toString(questionCounter));
		qListElement.setAttribute("class", elementName);
		theQList.addContent(qListElement);
		
		Element thisQuestion= new Element(elementName);
		thisQuestion.setAttribute("id", "q" + Integer.toString(questionCounter));
		thePool.addContent(thisQuestion);
		
		Element body= new Element("BODY");
		thisQuestion.addContent(body);
		
		String refHTML= "";
		if (referenceTag.length() > 0)
		{
			question refQ= theTest.questions.getReference(referenceTag);
			if (refQ != null)
			{
				refHTML= randomVariable.deReference(refQ.qtext, theTest, refQ.sqlID, randomData);
				
				// handle v5 media references
				refHTML= richMedia.deReference(refHTML, theHandler.getParam(v5test.USE_BASEURL) );
				
				// do not number
				refHTML= tp_utils.substitute( refHTML, question.SUBTAG3, "&nbsp;");
				
				refHTML += "<br>&nbsp;<br>";
			}
		}
		

		String stemHTML= "";
		
		// subsitute random variables into the text
		String substitutedText= randomVariable.deReference(qtext, theTest, sqlID, randomData);
		
		// handle v5 media references
		substitutedText= richMedia.deReference(substitutedText, theHandler.getParam(v5test.USE_BASEURL));
		
		stemHTML += substitutedText;

		// do not number
		stemHTML= tp_utils.substitute( stemHTML, question.SUBTAG3, "&nbsp;");
		
		Element bodyText= new Element("TEXT");
		bodyText.addContent( new CDATA(refHTML + stemHTML) );
		body.addContent(bodyText);
		
		Element bodyFlags= new Element("FLAGS");
		body.addContent(bodyFlags);
		Element bodyFlagISHTML= new Element("ISHTML");
		bodyFlagISHTML.setAttribute("value", "true");
		bodyFlags.addContent(bodyFlagISHTML);
		Element bodyFlagISNEWLINELITERAL= new Element("ISNEWLINELITERAL");
		bodyFlagISNEWLINELITERAL.setAttribute("value", "false");
		bodyFlags.addContent(bodyFlagISNEWLINELITERAL);
		

		String aCorrectAnswer= "Items should be ranked in the following order:<ol>";
		for (int i=0 ; i<choices.size() ; i++) 
		{
			String theChoice= (String)choices.elementAt(i);
			aCorrectAnswer += "<li>" + theChoice + "</li>";
		}
		aCorrectAnswer += "</ol>";
		
		aCorrectAnswer= randomVariable.deReference(aCorrectAnswer, theTest, sqlID, randomData);
		aCorrectAnswer= richMedia.deReference(aCorrectAnswer, theHandler.getParam(v5test.USE_BASEURL));
		
		Element thisAnswer= new Element("ANSWER");
		thisAnswer.setAttribute("id", "q" + Integer.toString(questionCounter) + "_a1");
		thisQuestion.addContent(thisAnswer);

		Element thisAnswerText= new Element("TEXT");
		thisAnswerText.addContent(new CDATA(aCorrectAnswer));
		thisAnswer.addContent(thisAnswerText);

		Element gradable= new Element("GRADABLE");
		thisQuestion.addContent(gradable);
		Element feedbackWhenCorrect= new Element("FEEDBACK_WHEN_CORRECT");
		
		String theFeedback= (String)feedback.elementAt(0);
		theFeedback= randomVariable.deReference(theFeedback, theTest, sqlID, randomData);
		theFeedback= richMedia.deReference(theFeedback, theHandler.getParam(v5test.USE_BASEURL) ).trim();
		
		String suppInfo= "";
		if (theHandler.getParam(test.EXPORT_INFO).equals("true")) suppInfo= supplementaryKludge( theHandler );
		if (suppInfo.length() > 0) suppInfo= "<br>&nbsp;<br>" + suppInfo;
			
		if (theFeedback.length() > 0)
			feedbackWhenCorrect.addContent( new CDATA(theFeedback + suppInfo) );
		else
			feedbackWhenCorrect.addContent( new CDATA("correct" + suppInfo) );
			
		gradable.addContent(feedbackWhenCorrect);
		Element feedbackWhenIncorrect= new Element("FEEDBACK_WHEN_INCORRECT");
		if (theFeedback.length() > 0)
			feedbackWhenIncorrect.addContent( new CDATA(theFeedback + suppInfo) );
		else
			feedbackWhenIncorrect.addContent( new CDATA("incorrect" + suppInfo) );
		gradable.addContent(feedbackWhenIncorrect);
		
		return(true);
	}

	/*
	public String v5pdfHTML( tp_requestHandler theHandler, test theTest, parameters theData, String substitutedText, VectorAdapter keyItem ) {
		
		String resultHTML= "";
		String keyHTML= "";
		
		VectorAdapter indexVector= new VectorAdapter();
		VectorAdapter choiceVector= new VectorAdapter();
		for (int i=0 ; i<choices.size() ; i++) {
			String theChoice= pdfoutput.fixHTML( (String)choices.elementAt(i) );
			try {
				richMedia theMedia= new richMedia( theChoice );
				theMedia.setChoice(true);
				theChoice= theMedia.pdfhtml("");
			} catch (testFormatException e) {}
			
			theChoice= randomVariable.deReference(theChoice, theTest, sqlID, theData);
			theChoice= richMedia.pdfDeReference(theChoice, theHandler, sqlID, theData);

			choiceVector.addElement("____&nbsp;&nbsp;" + theChoice + "<br />");
			indexVector.addElement(Integer.toString(i+1));
			
			if (theTest.getGUI().terseKey) {
				if (keyHTML.length() > 0)
					keyHTML += "<span class=\"rspHelpStyle\">&nbsp;&nbsp;-&nbsp;&nbsp;</span>";
				keyHTML += theChoice;
			}
		}
		
		// here is where we will randomize the order
		while (choiceVector.size() > 0) {
			if (choiceVector.size() == 1) {
				resultHTML += (String)choiceVector.elementAt(0);
				if (!theTest.getGUI().terseKey) keyHTML += "<span class=\"correct\">" + (String)indexVector.elementAt(0) + "</span>" + ((String)choiceVector.elementAt(0)).substring(4);
				choiceVector.removeElementAt(0);
				indexVector.removeElementAt(0);
			}
			else {
				double calc= Math.random() * choiceVector.size();
				resultHTML += (String)choiceVector.elementAt((int)calc);
				if (!theTest.getGUI().terseKey) keyHTML += "<span class=\"correct\">" + (String)indexVector.elementAt((int)calc) + "</span>" + ((String)choiceVector.elementAt((int)calc)).substring(4);
				choiceVector.removeElementAt((int)calc);
				indexVector.removeElementAt((int)calc);
			}
		}
				
		if (!theTest.getGUI().terseKey)
		{
			if (feedback.size() > 0)
			{
				String theFeedback= pdfoutput.fixHTML( (String)feedback.elementAt(0) );
				theFeedback= randomVariable.deReference(theFeedback, theTest, sqlID, theData);
				theFeedback= richMedia.pdfDeReference(theFeedback, theHandler, sqlID, theData);
				if (theFeedback.trim().length() > 0) keyHTML += "<p>" + theFeedback + "</p>";
			}
		}
		
		if (keyItem != null) keyItem.addElement(keyHTML);
			
		return(resultHTML);
	}
	*/
	
	public boolean consumesMedia( String theMedia )
	{
		if (qtext.indexOf(theMedia) >= 0) return(true);
		
		for (int i=0 ; i<choices.size(); i++) {
			String item= (String)choices.elementAt(i);
			if (item.indexOf(theMedia) >= 0) return(true);
		}
		
		return(false);
	}
	
	public void v5info( tp_requestHandler theHandler, test theTest, rtf_pdf_output theWriter )
	{
		theWriter.add(formalTypeString() + "<br />");
		
		String color= "black";
		if (choices.size() == 1) color= "#202080";
		else if (choices.size() == 2) color= "#208020";
		else if (choices.size() == 3) color= "#802020";
		else if (choices.size() == 4) color= "black";
		else if (choices.size() == 5) color= "#808020";
		else if (choices.size() == 6) color= "#802080";
		else color= "red";
		
		theWriter.add("<span style=\"font-weight: bold; color: " + color + "\">" + Integer.toString(choices.size()) + "&nbsp;items&nbsp;to&nbsp;be&nbsp;ranked</span><br />");
		
		theWriter.add( v5groupMembership(theTest, false) + "<br />");
	}

	
	public String v5diagnosticInfo( tp_requestHandler theHandler, test theTest, String qnumber )
	{
		String result= "";
		
		if (qtext.trim().length() == 0) 
			result += "<span style=\"color: red\">Error: </span>empty question text in " + qnumber + "<br />";
		
		if (theHandler.getParam("pageRefs").length() > 0)
		{
			if (pageTag.length() == 0) 
				result += "<span style=\"color: red\">Error: </span>missing page reference in " + qnumber + "<br />";
		}
		
		boolean inGroup= false;
		for (int gIndex=1; gIndex<theTest.getSelection().questionGroups.size(); gIndex++)
		{
			questionGroup thisGroup= (questionGroup)theTest.getSelection().questionGroups.elementAt(gIndex);
			
			for (int i=0; i<thisGroup.idlist.size(); i++)
			{
				if (((String)thisGroup.idlist.elementAt(i)).equals(Integer.toString(id)))
				{
					inGroup= true;
					break;
				}
			}
		}
		if ((theHandler.getParam("groupMembership").length() > 0) && !inGroup)
			result += "<span style=\"color: red\">Error: </span>no category membership for question #" + qnumber + "<br />";
		
		
		for (int i=0 ; i<choices.size(); i++) {
			String theChoice= (String)choices.elementAt(i);
			if (theChoice.length() == 0)
				result += "<span style=\"color: red\">Error: </span>ordered item #" + Integer.toString(i+1) + " empty in " + qnumber + "<br />";
		}

		return(result);
	}
	
	
	
	public String v6show( tp_requestHandler theHandler, test theTest, submission theSubmission, parameters theData, String unusedText ) 
	{
		/**
		 * If the C15 Instructor UI feature is enabled, then v6show_C15 should be invoked for rendering question 
		 */
		Vector enabledFeatureList= tp_utils.listToVector( licenseManager.getFeatureInfo(theHandler, licenseManager.LICENSE_TYPE_HMFEATURE), "," );
		boolean c15InstructorUiEnabled = enabledFeatureList.contains(C15INSTRUCTORUI);
		if (c15InstructorUiEnabled){	    
			return renderItem(theHandler, theTest, null, theSubmission, theData);
		}
		//boolean answerSpecificFeedbackSupport= licenseManager.hasFeature(theHandler, licenseManager.FEATURE_SUPPORT_ASFEEDBACK);
		
		String theQID= "Q_" + sqlID;
		
		String formName= "TESTPILOT";
		if (theHandler.isHMrequest()) formName= classware_hm.STUDENT_FORM_NAME;
		
		boolean recallOK= false;
		if ((theSubmission != null) && theTest.getDefaultAction().feedbackOnRecall && (theTest.getSecurity().recallTime > 0))
		{
			long now= (new java.util.Date()).getTime();
			recallOK= (now > theTest.getSecurity().recallTime);
		}

		boolean previewing= theData.getParam("mode").equals("preview");
		
		boolean postSubmission= (theSubmission != null);
		boolean afterTest= postSubmission && (theHandler.isManager || theTest.getDefaultAction().returnFeedback || recallOK);
		boolean pregrading= theHandler.getParam(v7test_student.PREGRADE).equals(sqlID) && (theTest.getGUI().allowPregrade || theHandler.isHMrequest());
		boolean indicators= !completeIncompleteGrading() && !pregrading && postSubmission && policies.connectBoolean( policies.getVal(classware_hm.POLICY_indicators, null, theHandler, theSubmission, false), afterTest );
		boolean solution= policies.connectBoolean( policies.getVal(classware_hm.POLICY_solution, null, theHandler, theSubmission), !theHandler.isHMrequest());
		boolean fullFeedback= theHandler.getParam(classware_hm.POLICY_grading).equals(classware_hm.POLICY_grading_feedback);
		
		// QCQA#7482, if we're taking the test
		if (!theHandler.getParam(classware_hm.POST_SUBMISSION).equals(classware_hm.POST_SUBMISSION) && theHandler.isHMrequest())
		{
			// and in feedback between questions and shoing feedback, also show indicators
			if (theHandler.getParam(classware_hm.POLICY_feedback).equals("yes") && (theHandler.getParam(classware_hm.POLICY_feedback_state).equals(classware_hm.POLICY_feedback_SHOWING)))
				indicators= true;
		}

		if (pregrading) indicators= true;
		
		double obfusticator1= Math.rint(Math.random() * 1000);
		double obfusticator2= Math.rint(Math.random() * 1000);
		double obfusticator3= Math.rint(Math.random() * 100);
		String ob1Str= Integer.toString((int)obfusticator1);
		while (ob1Str.length() < 3) ob1Str= "0" + ob1Str;
		String ob2Str= Integer.toString((int)obfusticator2);
		while (ob2Str.length() < 3) ob2Str= "0" + ob2Str;
		String ob3Str= Integer.toString((int)obfusticator3);
		while (ob3Str.length() < 2) ob3Str= "0" + ob3Str;
		String theObfusticator= ob1Str + ob3Str + ob2Str;
		
		if (theData != null) 
		{
			String tmp= theData.getParam( theQID );
			// v4.1.3p40 
			if (tmp.length() > 0) theObfusticator= tmp;
			theData.removeParam( theQID );
		}
		
		
		String resultHTML= "<div class=\"rspStyle\">";
		
		resultHTML += "<INPUT TYPE=\"HIDDEN\" NAME=\"" + theQID + "\" VALUE=\"" + theObfusticator + "\">";
		

		// script to attempt to prevent duplications - don't assume it works
		resultHTML += "\r<script type=\"text/javascript\" >\r";
		
		if(classware_hm.MANUAL_GRADE_R6B.equalsIgnoreCase(theHandler.getParam(classware_hm.CLASSWARE_REQUEST))
				&& hm_grading_r6b.BY_QUESTION.equalsIgnoreCase(theHandler.getParam(hm_grading_r6b.WORK_FLOW))){
			
			resultHTML += "function " + theQID + "_" + theSubmission.userID + "ck(nv,ti) {\r";
			//resultHTML += "  alert('nv=' + nv + ', ti=' + ti);\r";
			VectorAdapter scriptVector= new VectorAdapter();
			for (int i=0 ; i<choices.size() ; i++)
				scriptVector.addElement("	if (ti!=" + obfusticateID(i,theObfusticator) + ") if (document." + formName + "." + theQID + "_" + theSubmission.userID + "_" + obfusticateID(i,theObfusticator) + ".value==nv) document." + formName + "." + theQID + "_" + theSubmission.userID + "_" + obfusticateID(i,theObfusticator) + ".selectedIndex=\"0\";\r");
			while (scriptVector.size() > 0) 
			{
				if (scriptVector.size() == 1) 
				{
					resultHTML += (String)scriptVector.elementAt(0);
					scriptVector.removeElementAt(0);
				}
				else 
				{
					double calc= Math.random() * scriptVector.size();
					resultHTML += (String)scriptVector.elementAt((int)calc);
					scriptVector.removeElementAt((int)calc);
				}
			}
		} else {
			resultHTML += "function " + theQID + "ck(nv,ti) {\r";
			//resultHTML += "  alert('nv=' + nv + ', ti=' + ti);\r";
			VectorAdapter scriptVector= new VectorAdapter();
			for (int i=0 ; i<choices.size() ; i++)
				scriptVector.addElement("	if (ti!=" + obfusticateID(i,theObfusticator) + ") if (document." + formName + "." + theQID + "_" + obfusticateID(i,theObfusticator) + ".value==nv) document." + formName + "." + theQID + "_" + obfusticateID(i,theObfusticator) + ".selectedIndex=\"0\";\r");
			while (scriptVector.size() > 0) 
			{
				if (scriptVector.size() == 1) 
				{
					resultHTML += (String)scriptVector.elementAt(0);
					scriptVector.removeElementAt(0);
				}
				else 
				{
					double calc= Math.random() * scriptVector.size();
					resultHTML += (String)scriptVector.elementAt((int)calc);
					scriptVector.removeElementAt((int)calc);
				}
			}
		}
		resultHTML += "}\r";
		resultHTML += "</script>\r";
		
		boolean allCorrect= true;
		
		VectorAdapter choiceVector= new VectorAdapter();
		for (int i=0 ; i<choices.size() ; i++) 
		{
			String theChoice= (String)choices.elementAt(i);
			
			theChoice= tooltipDeReference(theChoice);
			theChoice= randomVariable.deReference(theChoice, theTest, sqlID, theData);
			theChoice= richMedia.deReference(theChoice, theHandler, sqlID, theData);
			
			int thisItemsDefault= 0;
			if (theData != null) 
			{
				String thisDefault= theData.getParam( theQID + "_" + obfusticateID(i,theObfusticator) );
				theData.removeParam( theQID + "_" + obfusticateID(i,theObfusticator) );
				try 
				{
					thisItemsDefault= Integer.parseInt(thisDefault);
				} 
				catch (NumberFormatException n) {}
			}
			
			boolean answered= (thisItemsDefault != 0);
			boolean correctlyAnswered= false;

			String thisChoiceHTML= "<tr><td class=\"mcCell\">";
			if(classware_hm.MANUAL_GRADE_R6B.equalsIgnoreCase(theHandler.getParam(classware_hm.CLASSWARE_REQUEST))
					&& hm_grading_r6b.BY_QUESTION.equalsIgnoreCase(theHandler.getParam(hm_grading_r6b.WORK_FLOW))){
				
				thisChoiceHTML += "<SELECT NAME=\"" + theQID + "_" + theSubmission.userID + "_" + obfusticateID(i,theObfusticator) + "\" onChange=\"" + theQID + "_" + theSubmission.userID + "ck(this.value," + obfusticateID(i,theObfusticator) + ");\">";
			} else {
				thisChoiceHTML += "<SELECT NAME=\"" + theQID + "_" + obfusticateID(i,theObfusticator) + "\" onChange=\"" + theQID + "ck(this.value," + obfusticateID(i,theObfusticator) + ");\">";
			}
			
			thisChoiceHTML += "	<OPTION ";
			if (thisItemsDefault == 0) thisChoiceHTML += "SELECTED ";
			thisChoiceHTML += "VALUE=\"0\">&nbsp;";
			for (int j=0 ; j<choices.size() ; j++) 
			{
				thisChoiceHTML += "<OPTION ";
				if (thisItemsDefault == (j+1)) 
				{
					thisChoiceHTML += "SELECTED ";
					if (i==j) correctlyAnswered= true;
				}
				thisChoiceHTML += "VALUE=\"" + Integer.toString(j+1) + "\">" + Integer.toString(j+1) + "";
			}
			thisChoiceHTML += "</SELECT>&nbsp;&nbsp;";
			
			allCorrect &= correctlyAnswered;
			
			if (pregrading)
			{
				if (answered)
				{
					if (correctlyAnswered)
						thisChoiceHTML += "<img class=\"checkMark\" src=\"" + theHandler.baseServer + "/EZTestOnline/Graphics/check.gif\" alt=\"correct\">&nbsp;&nbsp;";
					else
						thisChoiceHTML += "<img class=\"checkMark\" src=\"" + theHandler.baseServer + "/EZTestOnline/Graphics/x.gif\" alt=\"incorrect\">&nbsp;&nbsp;";
				}
				else
					thisChoiceHTML += "&nbsp;&nbsp;";
			}
			else if ((afterTest || indicators) && !completeIncompleteGrading())
			{
				if (answered)
				{
					if (correctlyAnswered)
					{
						if (indicators) thisChoiceHTML += "<img class=\"checkMark\" src=\"" + theHandler.baseServer + "/EZTestOnline/Graphics/check.gif\" alt=\"correct\">&nbsp;&nbsp;";
					}
					else
					{
						thisChoiceHTML += "<span style=\"color: #004000; font-size: 9pt\">";
						if (indicators) thisChoiceHTML += "<img class=\"checkMark\" src=\"" + theHandler.baseServer + "/EZTestOnline/Graphics/x.gif\" alt=\"incorrect\" title=\"#" + Integer.toString(i+1) + "\">";
						if (previewing || solution || fullFeedback) thisChoiceHTML += "#" + Integer.toString(i+1) + "</span>";
						thisChoiceHTML += "&nbsp;&nbsp;";
					}
				}
				else
				{
					thisChoiceHTML += "<span style=\"color: #004000; font-size: 9pt\">";
					if (!previewing && indicators) thisChoiceHTML += "<img class=\"checkMark\" src=\"" + theHandler.baseServer + "/EZTestOnline/Graphics/x.gif\" alt=\"incorrect\" title=\"#" + Integer.toString(i+1) + "\">";
					if (previewing || solution || fullFeedback) thisChoiceHTML += "#" + Integer.toString(i+1) + "</span>&nbsp;&nbsp;";
				}
			}
			
			thisChoiceHTML += "</td><td class=\"mcCell\">";
			thisChoiceHTML += theChoice + "</td></tr>";
			
			choiceVector.addElement(thisChoiceHTML);
		}
		
		
		resultHTML += "<table cellpadding=\"0\" cellspacing=\"0\">";
		
		// here is where we will randomize the order
		while (choiceVector.size() > 0) 
		{
			if (choiceVector.size() == 1) 
			{
				resultHTML += (String)choiceVector.elementAt(0);
				choiceVector.removeElementAt(0);
			}
			else 
			{
				double calc= Math.random() * choiceVector.size();
				resultHTML += (String)choiceVector.elementAt((int)calc);
				choiceVector.removeElementAt((int)calc);
			}
		}
		
		resultHTML += "</table>";

		if (theSubmission != null)
		{
			boolean feedbackCheck= theTest.getDefaultAction().returnFeedback;
			if (theHandler.isHMrequest()) feedbackCheck= classware_hm.showExplanation(theHandler, this);

			if (theHandler.isManager || feedbackCheck || recallOK)
			{
				String theFeedback= "";

				//if (answerSpecificFeedbackSupport){
					String commonFeedback= questionProperties.getString(COMMON_FEEDBACK, null);
					if (commonFeedback == null) {
						if (feedback.size() > 0)
							commonFeedback= ((String)feedback.elementAt(0)).trim();
						else
							commonFeedback= "";
					}
					
					if (questionProperties.getBoolean(USE_COMMON_FEEDBACK, true)) theFeedback= commonFeedback;
					else
					{
						if (allCorrect && (feedback.size() > 0)) theFeedback= ((String)feedback.elementAt(0)).trim();
						if (!allCorrect && (feedback.size() > 1)) theFeedback= ((String)feedback.elementAt(1)).trim();
						if (theFeedback.length() == 0) theFeedback= commonFeedback;
					}
				/*}
				else if (feedback.size() > 0)
					theFeedback= (String)feedback.elementAt(0);*/
			
				theFeedback += connectSolution(theHandler);
				
				theFeedback= tooltipDeReference(theFeedback);
				theFeedback= randomVariable.deReference(theFeedback, theTest, sqlID, theData);
				theFeedback= richMedia.deReference(theFeedback, theHandler, sqlID, theData);
				if (theFeedback.trim().length() > 0) resultHTML += "<div class=\"feedback_block\">" + theFeedback + "</div>";
			}
			
			response theResponse= theSubmission.getResponse(sqlID);
			if (theResponse != null) resultHTML += showComment( theHandler, theSubmission, theResponse );
		}
		
		resultHTML += supplementaryInfo( theHandler, theSubmission, theData, true );
		resultHTML += "<br></div>";
		
		return(resultHTML);
	}
	
	/**
	 * This method is to render ranking question with the new instructor UI.
	 * @param theHandler
	 * @param theTest
	 * @param partialTO for new test, for old test it should be null
	 * @param theSubmission for old test, for new test it should be null
	 * @param theData for old test, for new test it should be null
	 * @return String
	 */
	private String renderItem(tp_requestHandler theHandler, test theTest, PartialTO partialTO, submission theSubmission, parameters theData) {
		_logger.info("## rendering from renderItem");
		TestPilot4.cssFile(theHandler, "/EZTestOnline/paamUI/css/matching-ranking.css");
		TestPilot4.jsFile(theHandler, "/EZTestOnline/paamUI/js/matching-ranking.js");
		
		//STEP 1 : get all the parameter needed to render ranking question
		Map<String, Object> htmlElementsMap = getRenderingElements(theHandler, theTest, partialTO, theSubmission, theData);
		
		return rankingRender(htmlElementsMap);
	}
	
	/**
	 * This method is to rendering entire ranking questions along with instructor comment, feedback 
	 * supplementaryInfo etc. for the question.
	 * @param htmlElementsMap
	 * @return String
	 */

	public String rankingRender(Map<String, Object> htmlElementsMap) {
		StringBuilder resultHTML = new StringBuilder();
		try{
			if (htmlElementsMap == null || htmlElementsMap.isEmpty()) {
				return "";
			}
			String theQID = "Q_"+this.sqlID;
			//get the choice vector from the map
			VectorAdapter htmlChoices = (VectorAdapter) htmlElementsMap.get("choiceVector");
			//if no choice is generated
			if (htmlChoices == null || htmlChoices.isEmpty()) {
				_logger.error("No Choices are availabled for rendering");
				return "";
			}
			//get the obfucticator value
			String theObfusticator = (String)htmlElementsMap.get("obfucticator");
			
			//start generating HTML code for question rendering
			resultHTML.append("<div class=\"rspStyle\">");
			resultHTML.append("<INPUT TYPE=\"HIDDEN\" NAME=\"" + theQID + "\" VALUE=\"" + theObfusticator + "\">");
			//choice rendering goes here
			for(int i= 0; i < htmlChoices.size(); i++){
				resultHTML.append("<div class=\"matching-ranking__row question--ranking matching-ranking__row--width-medium\">");
				resultHTML.append((String)htmlChoices.elementAt(i));
				resultHTML.append("</div>");
			}
			//instructor comment goes here
			if (htmlElementsMap.get("instructorComment") != null) {
				resultHTML.append(htmlElementsMap.get("instructorComment"));
			}
			//feedback goes here
			if (htmlElementsMap.get("feedback") != null) {
				resultHTML.append((String)htmlElementsMap.get("feedback"));
			}
			//supplementary info goes here
			if (htmlElementsMap.get("supplementaryInfo") != null) {
				resultHTML.append((String)htmlElementsMap.get("supplementaryInfo"));
			}
			
			resultHTML.append("</div>");
		}catch(Exception ex){
			_logger.error("Error occurred during rendering ranking question: "+this.sqlID, ex);
		}
		
		return resultHTML.toString();
	}
	
	/**
	 * This method is to generate data for each choice of ranking question for new test
	 * Based on business logic we set various parameter value and put it into a map. The 
	 * Map holds following parameter :
	 * 		choice
	 * 		responseForChoice
	 * 		choiceInputBoxName
	 * 		answered
	 * 		correctlyAnswered
	 * 		correctAnswer
	 * @param theHandler
	 * @param theTest
	 * @param currentChoice
	 * @param theObfusticator
	 * @return Map<String,Object>
	 */
	public Map<String,Object> getItemChoiceOld(tp_requestHandler theHandler, test theTest, submission theSubmission, int currentChoice,parameters theData, String theObfusticator){
		_logger.info(" Start of getItemChoiceOld() : for Old test");
		String theQID = "Q_"+sqlID;
		Map<String, Object> itemChoiceMapOldTest= new HashMap<String, Object>();
		String choice= (String)choices.elementAt(currentChoice);
		
		choice= tooltipDeReference(choice);
		choice= randomVariable.deReference(choice, theTest, sqlID, theData);
		choice= richMedia.deReference(choice, theHandler, sqlID, theData);
		
		int thisItemsDefault = 0;
		if (theData != null) {
			String thisDefault= theData.getParam( theQID + "_" + obfusticateID(currentChoice,theObfusticator) );
			theData.removeParam( theQID + "_" + obfusticateID(currentChoice,theObfusticator) );
			try {
				thisItemsDefault= Integer.parseInt(thisDefault);
			} catch (NumberFormatException n) {}
		}
		itemChoiceMapOldTest.put("choice",choice);
		boolean answered= (thisItemsDefault != 0);
		boolean correctlyAnswered= false;
		
		//determining if the current choice is correctly answered or not
		for (int j=0 ; j<choices.size() ; j++) {			
			if (thisItemsDefault == (j+1)) {
				if (currentChoice == j) {
					correctlyAnswered= true;
				}
			}
		}
		/**
		 * if the rendering is for mangrade by question, then student id needs to passed.
		 * else no need to pass it.
		 */
		String inputBoxName = null;
		if(classware_hm.MANUAL_GRADE_R6B.equalsIgnoreCase(theHandler.getParam(classware_hm.CLASSWARE_REQUEST))
				&& hm_grading_r6b.BY_QUESTION.equalsIgnoreCase(theHandler.getParam(hm_grading_r6b.WORK_FLOW))){
			inputBoxName = theQID + "_" + theSubmission.userID + "_" + obfusticateID(currentChoice, theObfusticator);
		} else {
			inputBoxName = theQID + "_" + obfusticateID(currentChoice, theObfusticator);
		}
		itemChoiceMapOldTest.put("responseForChoice", thisItemsDefault);
		itemChoiceMapOldTest.put("choiceInputBoxName", inputBoxName);
		itemChoiceMapOldTest.put("answered", answered);
		itemChoiceMapOldTest.put("correctlyAnswered", correctlyAnswered);
		itemChoiceMapOldTest.put("correctAnswer", currentChoice+1);
		
		return itemChoiceMapOldTest;
	}
	
	/**
	 * This method is to generate data for each choice of ranking question for new test

	 * Based on business logic we set various parameter value and put it into a map. The 
	 * Map holds following parameter :
	 * 		choice
	 * 		responseForChoice
	 * 		choiceInputBoxName
	 * 		answered
	 * 		correctlyAnswered
	 * 		correctAnswer
	 * @param theHandler
	 * @param theTest
	 * @param partialTO
	 * @param currentChoice
	 * @param questionParams
	 * @param theObfusticator
	 * @return Map<String,Object>

	 */
	public Map<String, Object> getItemChoice(tp_requestHandler theHandler, test theTest, PartialTO partialTO, int currentChoice, QuestionParameters questionParams, String theObfusticator){
		String theQID = "Q_"+sqlID;
		Map<String, Object> itemChoiceMap= new HashMap<String, Object>();
		String choice= (String)choices.elementAt(currentChoice);
		
		choice= tooltipDeReference(choice);
		choice= randomVariable.deReference(choice, theTest, sqlID, partialTO);
		choice= richMedia.deReferenceNew(choice, theHandler, sqlID, partialTO);
		int thisItemsDefault= 0;
		if (questionParams != null) {
			CustomMap<String, String> questionParamMap = (CustomMap<String, String>)questionParams.getQuestionParameters();
			String thisDefault= questionParamMap.getParam( theQID + "_" + obfusticateID(currentChoice,theObfusticator) );
			questionParamMap.remove( theQID + "_" + obfusticateID(currentChoice,theObfusticator) );
			try{
				thisItemsDefault= Integer.parseInt(thisDefault);
			} catch (NumberFormatException n) {}
		}
		itemChoiceMap.put("choice",choice);
		boolean answered= (thisItemsDefault != 0);
		boolean correctlyAnswered= false;
		
		//determining if the current choice is correctly answered or not
		for (int j=0 ; j<choices.size() ; j++) {			
			if (thisItemsDefault == (j+1)) {
				if (currentChoice == j){
					correctlyAnswered= true;
				}
			}
		}
		/**
		 * if the rendering is for mangrade by question, then student id needs to passed.
		 * else no need to pass it.
		 */
		String inputBoxName = null;
		if(classware_hm.MANUAL_GRADE_R6B.equalsIgnoreCase(theHandler.getParam(classware_hm.CLASSWARE_REQUEST))
				&& hm_grading_r6b.BY_QUESTION.equalsIgnoreCase(theHandler.getParam(hm_grading_r6b.WORK_FLOW))){
			inputBoxName = theQID + "_" + partialTO.getStudentID() + "_" + obfusticateID(currentChoice, theObfusticator);
		} else {
			inputBoxName = theQID + "_" + obfusticateID(currentChoice, theObfusticator);
		}
		itemChoiceMap.put("responseForChoice", thisItemsDefault);
		itemChoiceMap.put("choiceInputBoxName", inputBoxName);
		itemChoiceMap.put("answered", answered);
		itemChoiceMap.put("correctlyAnswered", correctlyAnswered);
		itemChoiceMap.put("correctAnswer", currentChoice+1);
		
		return itemChoiceMap;
	}
	/**
	 * This method is responsible to generate the feedback for post submission view of ranking question

	 * @param theHandler
	 * @param allCorrect
	 * @param recallOk
	 * @param theTest
	 * @param partialTO
	 * @return String

	 */
	public String getFeedback (tp_requestHandler theHandler, boolean allCorrect, boolean recallOK, test theTest, PartialTO partialTO, submission theSubmission, parameters theData){
		String theFeedback= "";
		
		if (theSubmission != null || (partialTO != null && partialTO.isSubmission()) ) {
			boolean feedbackCheck = theTest.getDefaultAction().returnFeedback;
			if (theHandler.isHMrequest()) {
				feedbackCheck= classware_hm.showExplanation(theHandler, this);
			}
			if (theHandler.isManager || feedbackCheck || recallOK) {
				boolean answerSpecificFeedbackSupport = licenseManager.hasFeature(theHandler, licenseManager.FEATURE_SUPPORT_ASFEEDBACK);
				if (answerSpecificFeedbackSupport) {//if answerSpecific Feedback is supported
					String commonFeedback= questionProperties.getString(COMMON_FEEDBACK, null);
					if (commonFeedback == null) {
						if (feedback.size() > 0)
							commonFeedback= ((String)feedback.elementAt(0)).trim();
						else
							commonFeedback= "";
					}
					if (questionProperties.getBoolean(USE_COMMON_FEEDBACK, true)){
						theFeedback= commonFeedback;
					} else {
						//Correct - incorrect indicator
						if (allCorrect && (feedback.size() > 0)) {
							theFeedback= ((String)feedback.elementAt(0)).trim();
						}
						if (!allCorrect && (feedback.size() > 1)) {
							theFeedback= ((String)feedback.elementAt(1)).trim();
						}
						if (theFeedback.length() == 0) {
							theFeedback= commonFeedback;
						}
					}
				} else if (feedback.size() > 0){
					theFeedback= (String)feedback.elementAt(0);
				}
				//appending solution
				theFeedback += connectSolution(theHandler);
				
				theFeedback= tooltipDeReference(theFeedback);
				//for submission of new test
				if (partialTO != null) {
					theFeedback= randomVariable.deReference(theFeedback, theTest, sqlID, partialTO);
					theFeedback= richMedia.deReferenceNew(theFeedback, theHandler, sqlID, partialTO);
				}
				//for submission of old test
				if (theSubmission != null) {
					theFeedback= randomVariable.deReference(theFeedback, theTest, sqlID, theData);
					theFeedback= richMedia.deReference(theFeedback, theHandler, sqlID, theData);
				}
			}
		}
		return theFeedback;
	}
	
	/**
	 * This method is responsible for rendering the choice of ranking question

	 * 1. Check for post submission view

	 * 2. Generate for html element

	 * 3. Populate data for each choice

	 * 4. In test mode, only the question content is rendered.

	 * @param theHandler
	 * @param theTest
	 * @param partialTO
	 * @param unusedText
	 * @param theSubmission
	 * @param theData
	 * @return Map<String, Object>

	 */
	public Map<String, Object> getRenderingElements(tp_requestHandler theHandler, test theTest, PartialTO partialTO, submission theSubmission, parameters theData) {
		Map<String, Object> htmlCollection = new HashMap<String, Object>();
		
		boolean postSubmission = false;

		boolean indicators = false;

		boolean solution = false;

		boolean previewing = false;
		//set recallOK flag :start
		boolean recallOK= false;
		if (theSubmission != null) {
			recallOK = getRecallOK(theSubmission != null, theTest);
		} else {
			recallOK = getRecallOK(partialTO.isSubmission(), theTest);
		}
		//set recallOK flag :end
		

		CustomMap<String, String> testParamMap = null;

		QuestionParameters questionParams = null;
		boolean pregrading = theHandler.getParam(v7test_student.PREGRADE).equals(sqlID) && (theTest.getGUI().allowPregrade || theHandler.isHMrequest());
		boolean afterTest = false;
		if (theSubmission != null) {

			postSubmission = true;
			afterTest = postSubmission && (theHandler.isManager || theTest.getDefaultAction().returnFeedback || recallOK);

			indicators= !completeIncompleteGrading() && !pregrading && postSubmission && policies.connectBoolean( policies.getVal(classware_hm.POLICY_indicators, null, theHandler, theSubmission, false), afterTest );

			solution= policies.connectBoolean( policies.getVal(classware_hm.POLICY_solution, null, theHandler, theSubmission), !theHandler.isHMrequest());

			previewing= theData.getParam("mode").equals("preview");

		} else {
			testParamMap = (CustomMap<String, String>)partialTO.getTestParameter();
			questionParams = partialTO.getQuestionTO().getQuestionMap().get(sqlID);
			//populating questionParams

			if (questionParams == null) {
				questionParams = new QuestionParameters();
				partialTO.getQuestionTO().getQuestionMap().put(sqlID, questionParams);
			}

			postSubmission = (partialTO.isSubmission());
			afterTest = postSubmission && (theHandler.isManager || theTest.getDefaultAction().returnFeedback || recallOK);

			indicators = !completeIncompleteGrading() && !pregrading && postSubmission && policies.connectBoolean( policies.getValNew(classware_hm.POLICY_indicators, null, theHandler, partialTO, false), afterTest );

			solution = policies.connectBoolean( policies.getValNew(classware_hm.POLICY_solution, null, theHandler, partialTO), !theHandler.isHMrequest());

			previewing = testParamMap.getParam("mode").equals("preview");

		}
		

		boolean fullFeedback = theHandler.getParam(classware_hm.POLICY_grading).equals(classware_hm.POLICY_grading_feedback);
		boolean allCorrect = true;
		String theQID = "Q_"+sqlID;
		//get obfusticator for the ranking question
		String theObfusticator = getObfusticator();
		
		// QCQA#7482, if we're taking the test
		if (!theHandler.getParam(classware_hm.POST_SUBMISSION).equals(classware_hm.POST_SUBMISSION) && theHandler.isHMrequest()) {
			// and in feedback between questions and showing feedback, also show indicators
			if (theHandler.getParam(classware_hm.POLICY_feedback).equals("yes") && (theHandler.getParam(classware_hm.POLICY_feedback_state).equals(classware_hm.POLICY_feedback_SHOWING))){
				indicators = true;
			}
		}
		//overriding the indicators flag

		if (pregrading) {
			indicators = true;
		}
			
		//override the obfucticator value with already saved one : start
		if (theData != null) {//for old test

			String tmp = theData.getParam( theQID );

			if (tmp.length() > 0) {
				theObfusticator = tmp;
			}

			theData.removeParam( theQID );

		} else {
			if (questionParams != null) {
				CustomMap<String, String> questionParamMap = (CustomMap<String, String>)questionParams.getQuestionParameters();
				String tmp = questionParamMap.getParam(theQID);
				if (tmp.length() > 0) {
					theObfusticator = tmp;
				}
				questionParamMap.remove(theQID);
			}
		}

		htmlCollection.put("obfucticator", theObfusticator);
		//override the obfucticator value with already saved one : end
		
		//here we put the necessary flags in a map
		Map<String, Object> flagsMap = new HashMap<String, Object>();
		flagsMap.put("afterTest", afterTest);
		flagsMap.put("pregrading", pregrading);
		flagsMap.put("indicators", indicators);
		flagsMap.put("fullFeedback", fullFeedback);
		flagsMap.put("previewing", previewing);
		flagsMap.put("solution", solution);
		
		//generate the choice vector : start
		String thisChoiceHtml = "";
		String previewMode = getRenderingMode(theHandler);
		VectorAdapter choiceVector= new VectorAdapter();
		for (int i=0 ; i<choices.size() ; i++) {
			Map<String, Object> itemChoiceMap = new HashMap<String, Object>();
			if (theSubmission != null) {
				itemChoiceMap = getItemChoiceOld(theHandler,theTest, theSubmission, i, theData, theObfusticator);
			} else {
				itemChoiceMap = getItemChoice(theHandler,theTest, partialTO, i, questionParams, theObfusticator);
			}
			itemChoiceMap.putAll(flagsMap);
			allCorrect &=  (Boolean) itemChoiceMap.get("correctlyAnswered");
			
			if (INSTRUCTOR_PREVIEW_MODE.equals(previewMode) || ADD_QUESTION_MODE.equals(previewMode)) {
				itemChoiceMap.put("previewMode", previewMode);
				thisChoiceHtml = renderChoicePreview(itemChoiceMap);//call the preview mode
			}else{
				thisChoiceHtml = renderChoice(itemChoiceMap);//call the other mode
			}
			choiceVector.addElement(thisChoiceHtml);
		}
		//generate the choice vector : end
		
		//randomize the choice order : start
		VectorAdapter choiceVectorRandomized = new VectorAdapter();
		while (choiceVector.size() > 0) {   
			if (choiceVector.size() == 1) {
				choiceVectorRandomized.addElement((String)choiceVector.elementAt(0));
				choiceVector.removeElementAt(0);
			} else {
				double calc= Math.random() * choiceVector.size();
				choiceVectorRandomized.addElement((String)choiceVector.elementAt((int)calc));
				choiceVector.removeElementAt((int)calc);
			}
		}
		htmlCollection.put("choiceVector", choiceVectorRandomized);
		//randomize the choice order : end
		
		//generate feedback : start
		String feedback = getFeedback(theHandler, allCorrect, recallOK, theTest, partialTO, theSubmission, theData);
		htmlCollection.put("feedback", feedback);
		//generate feedback : end
		
		//generate instructor comment : start
		if ((questionParams != null) && !previewing) {
			String instructorComment = showComment( theHandler, questionParams );
			if (theSubmission != null) {
				response theResponse= theSubmission.getResponse(sqlID);
				instructorComment = showComment(theHandler, theSubmission, theResponse);
			}else{
				instructorComment = showComment( theHandler, questionParams );
			}
			htmlCollection.put("instructorComment", instructorComment);
		}
		//generate instructor comment : end
		
		//generate supplementary Info HTML (ebook link, reference link, hint links) : start
		String supplmentaryInfo = null;
		if (theSubmission != null) {
			supplmentaryInfo = supplementaryInfo( theHandler, theSubmission, theData, true );
		}else{
			supplmentaryInfo = supplementaryInfo( theHandler, partialTO, true );
		}
		htmlCollection.put("supplementaryInfo", supplmentaryInfo);
		//generate supplementary Info HTML (ebook link, reference link, hint links) : end
		
		return htmlCollection;
	}
		
	/**
	 * This method returns recallOK flag based on the feedbackOnRecall and recallTime of the test.
	 * @param submission
	 * @param theTest
	 * @return boolean
	 */
	public boolean getRecallOK(boolean submission, test theTest) {
		boolean recallOK = false;
		if (submission && theTest != null) {
			if(theTest.getDefaultAction().feedbackOnRecall && (theTest.getSecurity().recallTime > 0)) {
				long now = (new Date()).getTime();
				recallOK = (now > theTest.getSecurity().recallTime);
			}
		}
		return recallOK;
	}
	
	
	/**
	 * This method generates HTML for a single choice row for ranking type question for preview mode. 
	 * This includes options for the choice, correct answer. 
	 * @param valuesMap
	 * @return String which represent HTML element to display a single choice and it's related information
	 */
	public String renderChoicePreview(Map<String, Object> valuesMap) {
		StringBuilder htmlChoiceElement = new StringBuilder();
		htmlChoiceElement.append("<div class=\"matching-ranking__cell matching-ranking__cell--dropdown-right\">");
		htmlChoiceElement.append("	<ul class=\"no-style-ul\">");
		htmlChoiceElement.append("		<li class=\"matching-ranking__content--width\">");
		htmlChoiceElement.append("		<a href=\"javascript:void(0);\" class=\"matching-ranking__cell--select-option no-underline ");
		String previewMode = (String)valuesMap.get("previewMode");
		if (INSTRUCTOR_PREVIEW_MODE.equals(previewMode)) {//correct answer should be selected
			htmlChoiceElement.append("is-checked matching-ranking__cell\" data-typeval=\"question--ranking\">");
		} else {//correct answer should not be displayed for preview from add question tab
			htmlChoiceElement.append("is-unchecked matching-ranking__cell\" data-typeval=\"question--ranking\">");
		}
		htmlChoiceElement.append("<span class=\"matching-ranking__selected\"> </span>");
		htmlChoiceElement.append("<span class=\"single-on-click icon-dropdown icon-dropdown-style\"> </span>");
		htmlChoiceElement.append("</a>");

		//generates HTML code for showing right answer
		if (INSTRUCTOR_PREVIEW_MODE.equals(previewMode)){//for instructor preview from preview tab
			htmlChoiceElement.append("<div class=\"matching-ranking__selected-option\">").append(valuesMap.get("correctAnswer")).append("</div>");
		} else {//for instructor preview from add question tab
			htmlChoiceElement.append("<div class=\"matching-ranking__no-answer\"> </div>");
		}
		//correct answer HTML added here :start
		htmlChoiceElement.append("<div class=\"matching-ranking__cell matching-ranking__correct-answer\">Correct: ");
		htmlChoiceElement.append(valuesMap.get("correctAnswer"));
		htmlChoiceElement.append("</div>");
		//correct answer HTML added here :end
		
		htmlChoiceElement.append("<div class=\"matching-ranking__options-wrap\">");
		htmlChoiceElement.append("<div class=\"matching-ranking__options__triangle\"></div>");
		htmlChoiceElement.append("	<div class=\"matching-ranking__options\">");
		htmlChoiceElement.append("		<ul class=\"no-style-ul\">");
		//default choice value
		htmlChoiceElement.append("			<li class=\"matching-ranking__option\" data-optionval=\"0\">NO ANSWER</li>");
		//generates other choice option value
		for (int j=0 ; j<choices.size() ; j++) {
			htmlChoiceElement.append("		<li class=\"matching-ranking__option\" data-optionval=\"" + Integer.toString(j+1) + "\">" + Integer.toString(j+1) + "</li>");
		}
		htmlChoiceElement.append("		</ul>");
		htmlChoiceElement.append("	</div>");
		htmlChoiceElement.append("</div>");
		htmlChoiceElement.append("</li>");
		htmlChoiceElement.append("</ul>");
		htmlChoiceElement.append("</div>");
		
		//HTML for choice goes here
		htmlChoiceElement.append("<div class=\"matching-ranking__cell font-bold matching-ranking__cell--text-width\">" + valuesMap.get("choice") + "</div>");
		
		return htmlChoiceElement.toString();
	}
	
	/**
	 * This method generates HTML for a single choice row for ranking type question for report view. 
	 * This includes options for the choice, right answer and indicators HTML. 
	 * @param valuesMap
	 * @return String which represent HTML element to display a single choice and it's related information
	 */
	public String renderChoice(Map<String, Object> valuesMap) {
		StringBuilder htmlChoiceElement = new StringBuilder();
		htmlChoiceElement.append("<div class=\"matching-ranking__cell matching-ranking__cell--dropdown-right\">");
		htmlChoiceElement.append("	<ul class=\"no-style-ul\">");
		htmlChoiceElement.append("		<li class=\"matching-ranking__content--width\">");
		htmlChoiceElement.append("		<a href=\"javascript:void(0);\" class=\"matching-ranking__cell--select-option no-underline ");
		
		int responseForChoice = 0;
		if (valuesMap.get("responseForChoice") != null) {
			responseForChoice = (Integer)valuesMap.get("responseForChoice");
		}
		//generates HTML code for showing right answer and indicator
		Map<String, String> indicatorRightAnswerHtmlMap = getIndicatorRightAnswerHtml(valuesMap);
		String indicatorClass = indicatorRightAnswerHtmlMap.get("indicatorClassHtml");
		if (responseForChoice == 0) {//if no response is selected
			htmlChoiceElement.append(indicatorClass);
			htmlChoiceElement.append("is-unchecked matching-ranking__cell\" data-typeval=\"question--ranking\">");
		} else {//if no response is given, show it
			htmlChoiceElement.append(indicatorClass);
			htmlChoiceElement.append("is-checked matching-ranking__cell\" data-typeval=\"question--ranking\">");
		}
		//input box to hold the user selected response
		htmlChoiceElement.append("<input name=\"" + valuesMap.get("choiceInputBoxName") +"\" value= \""+responseForChoice+"\" type=\"hidden\"/>");
		htmlChoiceElement.append("<span class=\"single-on-click icon-dropdown icon-dropdown-style\"> </span>");
		htmlChoiceElement.append("</a>");
		
		//selected answer HTML added here :start
		if (responseForChoice == 0) {//if nothing is selected
			htmlChoiceElement.append("<div class=\"matching-ranking__no-answer\"></div>");
		}else {//if some option is selected
			htmlChoiceElement.append("<div class=\"matching-ranking__selected-option\">"+responseForChoice+"</div>");
		}
		//selected answer HTML added here :end
		
		//correct answer HTML added here :start
		String correctAnswerHtml = indicatorRightAnswerHtmlMap.get("correctAnswerHtml");
		htmlChoiceElement.append(correctAnswerHtml);
		//correct answer HTML added here :end
		
		htmlChoiceElement.append("<div class=\"matching-ranking__options-wrap\">");
		htmlChoiceElement.append("<div class=\"matching-ranking__options__triangle\"></div>");
		htmlChoiceElement.append("	<div class=\"matching-ranking__options\">");
		htmlChoiceElement.append("		<ul class=\"no-style-ul\">");
		//default choice value
		htmlChoiceElement.append("			<li class=\"matching-ranking__option\" data-optionval=\"0\">NO ANSWER</li>");
		//generates other choice option value
		for (int j=0 ; j<choices.size() ; j++) {
			htmlChoiceElement.append("		<li class=\"matching-ranking__option\" data-optionval=\"" + Integer.toString(j+1) + "\">" + Integer.toString(j+1) + "</li>");
		}
		htmlChoiceElement.append("		</ul>");
		htmlChoiceElement.append("	</div>");
		htmlChoiceElement.append("</div>");
		htmlChoiceElement.append("</li>");
		htmlChoiceElement.append("</ul>");
		htmlChoiceElement.append("</div>");
		
		//HTML for choice goes here
		htmlChoiceElement.append("<div class=\"matching-ranking__cell matching-ranking__cell--text-width font-bold\">" + valuesMap.get("choice") + "</div>");
		
		return htmlChoiceElement.toString();
	}
	/**
	 * This method generates HTML element to show right answer and indicators based on various 
	 * conditions.
	 * @param valuesMap
	 * @return Map of indicator HTML and right answer HTML
	 */
	public Map<String, String> getIndicatorRightAnswerHtml(Map<String, Object> valuesMap) {
		
		boolean pregrading = (Boolean)valuesMap.get("pregrading");
		boolean answered = (Boolean)valuesMap.get("answered");
		boolean correctlyAnswered = (Boolean)valuesMap.get("correctlyAnswered");
		boolean afterTest = (Boolean)valuesMap.get("afterTest");
		boolean indicators = (Boolean)valuesMap.get("indicators");
		boolean previewing = (Boolean)valuesMap.get("previewing");
		boolean solution = (Boolean)valuesMap.get("solution");
		boolean fullFeedback = (Boolean)valuesMap.get("fullFeedback");
		
		Map<String, String> indicatorRightAnswerHtmlMap = new HashMap<String, String>();
		StringBuilder correctAnswerHtml = new StringBuilder();
		StringBuilder indicatorClassHtml = new StringBuilder();
		
		if(pregrading) { //if pregrading i.e check my work
			if (answered) { //if answered in pregrading mode
				if (correctlyAnswered) {//if answered correctly show correct indicator
					indicatorClassHtml.append(" matching-ranking__cell--select-option--correct ");
				} else {//if answered correctly show incorrect indicator
					indicatorClassHtml.append(" matching-ranking__cell--select-option--wrong ");
				}
			}
		} else if ((afterTest || indicators ) && !completeIncompleteGrading()) {
			if (answered) {
				if (correctlyAnswered) {
					if (indicators) {//Added for showing correct indicator
						indicatorClassHtml.append(" matching-ranking__cell--select-option--correct ");
					}
				} else {
					if (indicators) {//Added for showing incorrect indicator
						indicatorClassHtml.append(" matching-ranking__cell--select-option--wrong ");
					}
				}
				if (previewing || solution || fullFeedback ) {//Added for showing correct answer
					correctAnswerHtml.append("<div class=\"matching-ranking__cell matching-ranking__correct-answer\">" +"Correct: "+ valuesMap.get("correctAnswer") + "</div>");
				}
			} else {
				if (!previewing && indicators ) {
					indicatorClassHtml.append(" matching-ranking__cell--select-option--wrong ");
				}
				if (previewing || solution || fullFeedback) {//Added for showing correct answer
					correctAnswerHtml.append("<div class=\"matching-ranking__cell matching-ranking__correct-answer\">" +"Correct: "+ valuesMap.get("correctAnswer") + "</div>");
				}
			}
		}
		indicatorRightAnswerHtmlMap.put("indicatorClassHtml", indicatorClassHtml.toString());//putting indicator class
		indicatorRightAnswerHtmlMap.put("correctAnswerHtml", correctAnswerHtml.toString());//putting correct answer HTML
		
		return indicatorRightAnswerHtmlMap;
	}

	/**
	 * This method is responsible for rendering a ranking question in test/review mode for both student and instructor.
	 * 1. In test mode, only the question content is rendered.
	 * 2. In review mode, student response along with correct/incorrect indicator, score,feedback, solution are rendered based on the attempt policy
	 * 3. Changes done for replacing table with div and css externalization
	 * @param theHandler
	 * @param theTest - the test object
	 * @param partialTO representing student response
	 * @param unusedText  
	 */
	public String v6show( tp_requestHandler theHandler, test theTest, PartialTO partialTO, String unusedText )
	{
		/**
		 * If the C15 Instructor UI feature is enabled, then v6show_C15 should be invoked for rendering question 
		 */
		Vector enabledFeatureList= tp_utils.listToVector( licenseManager.getFeatureInfo(theHandler, licenseManager.LICENSE_TYPE_HMFEATURE), "," );
		boolean c15InstructorUiEnabled = enabledFeatureList.contains(C15INSTRUCTORUI);
		if (c15InstructorUiEnabled){	    
			return renderItem(theHandler, theTest, partialTO, null, null);
		}
		
		boolean answerSpecificFeedbackSupport= licenseManager.hasFeature(theHandler, licenseManager.FEATURE_SUPPORT_ASFEEDBACK);
		
		CustomMap<String, String> testParamMap = (CustomMap<String, String>)partialTO.getTestParameter();
		QuestionParameters questionParams = partialTO.getQuestionTO().getQuestionMap().get(sqlID);
		if(questionParams == null){
			questionParams = new QuestionParameters();
			partialTO.getQuestionTO().getQuestionMap().put(sqlID, questionParams);
		}

		String theQID= "Q_" + sqlID;
		
		String formName= "TESTPILOT";
		if (theHandler.isHMrequest()) formName= classware_hm.STUDENT_FORM_NAME;
		
		boolean recallOK= false;
		if ((partialTO.isSubmission()) && theTest.getDefaultAction().feedbackOnRecall && (theTest.getSecurity().recallTime > 0))
		{
			long now= (new java.util.Date()).getTime();
			recallOK= (now > theTest.getSecurity().recallTime);
		}

		boolean previewing= testParamMap.getParam("mode").equals("preview");
		
		boolean postSubmission= (partialTO.isSubmission());
		boolean afterTest= postSubmission && (theHandler.isManager || theTest.getDefaultAction().returnFeedback || recallOK);
		boolean pregrading= theHandler.getParam(v7test_student.PREGRADE).equals(sqlID) && (theTest.getGUI().allowPregrade || theHandler.isHMrequest());
		boolean indicators= !completeIncompleteGrading() && !pregrading && postSubmission && policies.connectBoolean( policies.getValNew(classware_hm.POLICY_indicators, null, theHandler, partialTO, false), afterTest );
		boolean solution= policies.connectBoolean( policies.getValNew(classware_hm.POLICY_solution, null, theHandler, partialTO), !theHandler.isHMrequest());
		boolean fullFeedback= theHandler.getParam(classware_hm.POLICY_grading).equals(classware_hm.POLICY_grading_feedback);
		
		// QCQA#7482, if we're taking the test
		if (!theHandler.getParam(classware_hm.POST_SUBMISSION).equals(classware_hm.POST_SUBMISSION) && theHandler.isHMrequest())
		{
			// and in feedback between questions and shoing feedback, also show indicators
			if (theHandler.getParam(classware_hm.POLICY_feedback).equals("yes") && (theHandler.getParam(classware_hm.POLICY_feedback_state).equals(classware_hm.POLICY_feedback_SHOWING)))
				indicators= true;
		}

		if (pregrading) indicators= true;
		
		double obfusticator1= Math.rint(Math.random() * 1000);
		double obfusticator2= Math.rint(Math.random() * 1000);
		double obfusticator3= Math.rint(Math.random() * 100);
		String ob1Str= Integer.toString((int)obfusticator1);
		while (ob1Str.length() < 3) ob1Str= "0" + ob1Str;
		String ob2Str= Integer.toString((int)obfusticator2);
		while (ob2Str.length() < 3) ob2Str= "0" + ob2Str;
		String ob3Str= Integer.toString((int)obfusticator3);
		while (ob3Str.length() < 2) ob3Str= "0" + ob3Str;
		String theObfusticator= ob1Str + ob3Str + ob2Str;
		
		if (questionParams != null) 
		{
			CustomMap<String, String> questionParamMap = (CustomMap<String, String>)questionParams.getQuestionParameters();
			String tmp= questionParamMap.getParam(theQID);
			// v4.1.3p40 
			if (tmp.length() > 0) theObfusticator= tmp;
			questionParamMap.remove(theQID);
		}
		
		
		String resultHTML= "<div class=\"rspStyle\">";
		
		//hidden inputbox to hold the obfusticator
		resultHTML += "<INPUT TYPE=\"HIDDEN\" NAME=\"" + theQID + "\" VALUE=\"" + theObfusticator + "\">";
		

		// script to attempt to prevent duplications - don't assume it works
		resultHTML += "\r<script type=\"text/javascript\" >\r";
		if(classware_hm.MANUAL_GRADE_R6B.equalsIgnoreCase(theHandler.getParam(classware_hm.CLASSWARE_REQUEST))
				&& hm_grading_r6b.BY_QUESTION.equalsIgnoreCase(theHandler.getParam(hm_grading_r6b.WORK_FLOW))){
			
			resultHTML += "function " + theQID + "_" + partialTO.getStudentID() + "ck(nv,ti) {\r";
			//resultHTML += "  alert('nv=' + nv + ', ti=' + ti);\r";
			VectorAdapter scriptVector= new VectorAdapter();
			for (int i=0 ; i<choices.size() ; i++)
				scriptVector.addElement("	if (ti!=" + obfusticateID(i,theObfusticator) + ") if (document." + formName + "." + theQID + "_" + partialTO.getStudentID() + "_" + obfusticateID(i,theObfusticator) + ".value==nv) document." + formName + "." + theQID + "_" + partialTO.getStudentID() + "_" + obfusticateID(i,theObfusticator) + ".selectedIndex=\"0\";\r");
			while (scriptVector.size() > 0) 
			{
				if (scriptVector.size() == 1) 
				{
					resultHTML += (String)scriptVector.elementAt(0);
					scriptVector.removeElementAt(0);
				}
				else 
				{
					double calc= Math.random() * scriptVector.size();
					resultHTML += (String)scriptVector.elementAt((int)calc);
					scriptVector.removeElementAt((int)calc);
				}
			}
		} else {
			resultHTML += "function " + theQID + "ck(nv,ti) {\r";
			//resultHTML += "  alert('nv=' + nv + ', ti=' + ti);\r";
			VectorAdapter scriptVector= new VectorAdapter();
			for (int i=0 ; i<choices.size() ; i++)
				scriptVector.addElement("	if (ti!=" + obfusticateID(i,theObfusticator) + ") if (document." + formName + "." + theQID + "_" + obfusticateID(i,theObfusticator) + ".value==nv) document." + formName + "." + theQID + "_" + obfusticateID(i,theObfusticator) + ".selectedIndex=\"0\";\r");
			while (scriptVector.size() > 0) 
			{
				if (scriptVector.size() == 1) 
				{
					resultHTML += (String)scriptVector.elementAt(0);
					scriptVector.removeElementAt(0);
				}
				else 
				{
					double calc= Math.random() * scriptVector.size();
					resultHTML += (String)scriptVector.elementAt((int)calc);
					scriptVector.removeElementAt((int)calc);
				}
			}
		}
		resultHTML += "}\r";
		resultHTML += "</script>\r";
		
		boolean allCorrect= true;
		
		VectorAdapter choiceVector= new VectorAdapter();
		//to populate the choiceVector
		for (int i=0 ; i<choices.size() ; i++) 
		{
			String theChoice= (String)choices.elementAt(i);
			
			theChoice= tooltipDeReference(theChoice);
			theChoice= randomVariable.deReference(theChoice, theTest, sqlID, partialTO);
			theChoice= richMedia.deReferenceNew(theChoice, theHandler, sqlID, partialTO);
			
			int thisItemsDefault= 0;
			if (questionParams != null) 
			{
				CustomMap<String, String> questionParamMap = (CustomMap<String, String>)questionParams.getQuestionParameters();
				String thisDefault= questionParamMap.getParam( theQID + "_" + obfusticateID(i,theObfusticator) );
				questionParamMap.remove( theQID + "_" + obfusticateID(i,theObfusticator) );
				try 
				{
					thisItemsDefault= Integer.parseInt(thisDefault);
				} 
				catch (NumberFormatException n) {}
			}
			
			boolean answered= (thisItemsDefault != 0);
			boolean correctlyAnswered= false;

			String thisChoiceHTML= "<tr><td class=\"mcCell\">";

			if(classware_hm.MANUAL_GRADE_R6B.equalsIgnoreCase(theHandler.getParam(classware_hm.CLASSWARE_REQUEST))
					&& hm_grading_r6b.BY_QUESTION.equalsIgnoreCase(theHandler.getParam(hm_grading_r6b.WORK_FLOW))){
				
				thisChoiceHTML += "<SELECT NAME=\"" + theQID + "_" + partialTO.getStudentID() + "_" + obfusticateID(i,theObfusticator) + "\" onChange=\"" + theQID + "_" + partialTO.getStudentID() + "ck(this.value," + obfusticateID(i,theObfusticator) + ");\">";
			} else {
				thisChoiceHTML += "<SELECT NAME=\"" + theQID + "_" + obfusticateID(i,theObfusticator) + "\" onChange=\"" + theQID + "ck(this.value," + obfusticateID(i,theObfusticator) + ");\">";
			}
			
			thisChoiceHTML += "	<OPTION ";
			if (thisItemsDefault == 0) thisChoiceHTML += "SELECTED ";
			thisChoiceHTML += "VALUE=\"0\">&nbsp;";

			for (int j=0 ; j<choices.size() ; j++) 
			{
				thisChoiceHTML += "<OPTION ";
				if (thisItemsDefault == (j+1)) 
				{
					thisChoiceHTML += "SELECTED ";
					if (i==j) correctlyAnswered= true;
				}
				thisChoiceHTML += "VALUE=\"" + Integer.toString(j+1) + "\">" + Integer.toString(j+1) + "";
			}
			thisChoiceHTML += "</SELECT>&nbsp;&nbsp;";

			
			allCorrect &= correctlyAnswered;
			if (pregrading)
			{
				if (answered)
				{
					if (correctlyAnswered)

						thisChoiceHTML += "<img class=\"checkMark\" src=\"" + theHandler.baseServer + "/EZTestOnline/Graphics/check.gif\" alt=\"correct\">&nbsp;&nbsp;";

					else

						thisChoiceHTML += "<img class=\"checkMark\" src=\"" + theHandler.baseServer + "/EZTestOnline/Graphics/x.gif\" alt=\"incorrect\">&nbsp;&nbsp;";

				}
				else

					thisChoiceHTML += "&nbsp;&nbsp;";

			}
			else if ((afterTest || indicators) && !completeIncompleteGrading())
			{
				if (answered)
				{
					if (correctlyAnswered)
					{
						if (indicators) thisChoiceHTML += "<img class=\"checkMark\" src=\"" + theHandler.baseServer + "/EZTestOnline/Graphics/check.gif\" alt=\"correct\">&nbsp;&nbsp;";

					}
					else
					{
						thisChoiceHTML += "<span style=\"color: #004000; font-size: 9pt\">";

						if (indicators) thisChoiceHTML += "<img class=\"checkMark\" src=\"" + theHandler.baseServer + "/EZTestOnline/Graphics/x.gif\" alt=\"incorrect\" title=\"#" + Integer.toString(i+1) + "\">";

						if (previewing || solution || fullFeedback) thisChoiceHTML += "#" + Integer.toString(i+1) + "</span>";

						thisChoiceHTML += "&nbsp;&nbsp;";

					}
				}
				else
				{
					thisChoiceHTML += "<span style=\"color: #004000; font-size: 9pt\">";

					if (!previewing && indicators) thisChoiceHTML += "<img class=\"checkMark\" src=\"" + theHandler.baseServer + "/EZTestOnline/Graphics/x.gif\" alt=\"incorrect\" title=\"#" + Integer.toString(i+1) + "\">";

					if (previewing || solution || fullFeedback) thisChoiceHTML += "#" + Integer.toString(i+1) + "</span>&nbsp;&nbsp;";

				}
			}
			
			thisChoiceHTML += "</td><td class=\"mcCell\">";

			thisChoiceHTML += theChoice + "</td></tr>";

			
			choiceVector.addElement(thisChoiceHTML);
		}
		
		

		resultHTML += "<table cellpadding=\"0\" cellspacing=\"0\">";

		

		// here is where we will randomize the order
		while (choiceVector.size() > 0) 
		{   
			if (choiceVector.size() == 1) 
			{
				resultHTML += (String)choiceVector.elementAt(0);
				choiceVector.removeElementAt(0);
			}
			else 
			{
				double calc= Math.random() * choiceVector.size();
				resultHTML += (String)choiceVector.elementAt((int)calc);
				choiceVector.removeElementAt((int)calc);
			}
		}
		

		resultHTML += "</table>";



		if (partialTO.isSubmission())
		{
			boolean feedbackCheck= theTest.getDefaultAction().returnFeedback;
			if (theHandler.isHMrequest()) feedbackCheck= classware_hm.showExplanation(theHandler, this);

			if (theHandler.isManager || feedbackCheck || recallOK)
			{
				String theFeedback= "";

				if (answerSpecificFeedbackSupport)
				{
					String commonFeedback= questionProperties.getString(COMMON_FEEDBACK, null);
					if (commonFeedback == null) {
						if (feedback.size() > 0)
							commonFeedback= ((String)feedback.elementAt(0)).trim();
						else
							commonFeedback= "";
					}
					
					if (questionProperties.getBoolean(USE_COMMON_FEEDBACK, true)) theFeedback= commonFeedback;
					else
					{
						if (allCorrect && (feedback.size() > 0)) theFeedback= ((String)feedback.elementAt(0)).trim();
						if (!allCorrect && (feedback.size() > 1)) theFeedback= ((String)feedback.elementAt(1)).trim();
						if (theFeedback.length() == 0) theFeedback= commonFeedback;
					}
				}
				else if (feedback.size() > 0)
					theFeedback= (String)feedback.elementAt(0);
			
				theFeedback += connectSolution(theHandler);
				
				theFeedback= tooltipDeReference(theFeedback);
				theFeedback= randomVariable.deReference(theFeedback, theTest, sqlID, partialTO);
				theFeedback= richMedia.deReferenceNew(theFeedback, theHandler, sqlID, partialTO);
				if (theFeedback.trim().length() > 0) resultHTML += "<div class=\"feedback_block\">" + theFeedback + "</div>";
			}
			
			if ((questionParams != null) && !previewing) resultHTML += showComment( theHandler, questionParams );
		}
		
		resultHTML += supplementaryInfo( theHandler, partialTO, true );
		resultHTML += "<br></div>";
		
		return(resultHTML);
	
	}
	
	public String v6pdf( tp_requestHandler theHandler, test theTest, submission theSubmission, parameters theData, String unusedText ) 
	{
		String theQID= "Q_" + sqlID;
		
		double obfusticator1= Math.rint(Math.random() * 1000);
		double obfusticator2= Math.rint(Math.random() * 1000);
		double obfusticator3= Math.rint(Math.random() * 100);
		String ob1Str= Integer.toString((int)obfusticator1);
		while (ob1Str.length() < 3) ob1Str= "0" + ob1Str;
		String ob2Str= Integer.toString((int)obfusticator2);
		while (ob2Str.length() < 3) ob2Str= "0" + ob2Str;
		String ob3Str= Integer.toString((int)obfusticator3);
		while (ob3Str.length() < 2) ob3Str= "0" + ob3Str;
		String theObfusticator= ob1Str + ob3Str + ob2Str;
		
		if (theData != null) {
			String tmp= theData.getParam( theQID );
			// v4.1.3p40 
			if (tmp.length() > 0) theObfusticator= tmp;
			theData.removeParam( theQID );
		}
		
		
		String resultHTML= "<p class=\"rspStyle\">";
				
		VectorAdapter choiceVector= new VectorAdapter();
		for (int i=0 ; i<choices.size() ; i++) 
		{
			String theChoice= (String)choices.elementAt(i);
			
			theChoice= randomVariable.deReference(theChoice, theTest, sqlID, theData);
			//theChoice= richMedia.pdfDeReference(theChoice, theHandler, sqlID, theData);
			theChoice= richMedia.deReference(theChoice, theHandler, sqlID, theData);
			
			int thisItemsDefault= 0;
			if (theData != null) 
			{
				String thisDefault= theData.getParam( theQID + "_" + obfusticateID(i,theObfusticator) );
				theData.removeParam( theQID + "_" + obfusticateID(i,theObfusticator) );
				try 
				{
					thisItemsDefault= Integer.parseInt(thisDefault);
				} 
				catch (NumberFormatException n) {}
			}
			
			String thisChoiceHTML= "<tr><td class=\"mcCell\">";
			if (thisItemsDefault == 0) 
				thisChoiceHTML += "___&nbsp;";
			else
				thisChoiceHTML += "_" + Integer.toString(thisItemsDefault) + "_&nbsp;";
			thisChoiceHTML += "&nbsp;";
			
			thisChoiceHTML += "<span style=\"color: #004000; font-size: 9pt\">#" + Integer.toString(i+1) + "</span>&nbsp;&nbsp;";
			
			thisChoiceHTML += "</td><td class=\"mcCell\">";
			thisChoiceHTML += theChoice + "</td></tr>";
			
			choiceVector.addElement(thisChoiceHTML);
		}
		
		
		resultHTML += "<table cellpadding=\"0\" cellspacing=\"0\">";
		
		// here is where we will randomize the order
		while (choiceVector.size() > 0) 
		{
			if (choiceVector.size() == 1) 
			{
				resultHTML += (String)choiceVector.elementAt(0);
				choiceVector.removeElementAt(0);
			}
			else 
			{
				double calc= Math.random() * choiceVector.size();
				resultHTML += (String)choiceVector.elementAt((int)calc);
				choiceVector.removeElementAt((int)calc);
			}
		}
		
		resultHTML += "</table>";
		
		String commonFeedback= questionProperties.getString(COMMON_FEEDBACK, null);
		if (commonFeedback == null || ("").equals(commonFeedback)) {
			if (feedback.size() > 0)
				commonFeedback= ((String)feedback.elementAt(0)).trim();
			else
				commonFeedback= "";
		}
		
		String theFeedback= commonFeedback;
		if(theFeedback.trim().length() > 0){
			theFeedback= tooltipDeReference(theFeedback);
			theFeedback= randomVariable.deReference(theFeedback, theTest, sqlID, theData);
			theFeedback= richMedia.deReference(theFeedback, theHandler, sqlID, theData);
			if (theFeedback.trim().length() > 0) resultHTML += "<p class=\"feedbackStyle\">" + theFeedback + "</p>";
		}
			
		response theResponse= theSubmission.getResponse(sqlID);
		if (theResponse != null)
		{
			if (theResponse.comment.length() > 0)
				resultHTML += "<p class=\"instructorComment\">" + tp_utils.substitute(theResponse.comment, "\n", "<br />") + "</p>";
		}
		
		resultHTML += "</p>";
		
		return(resultHTML);
	}

	
	public void v6evaluate( tp_requestHandler theHandler, test theTest, submission theSubmission ) 
	{
		String questionID= "Q_" + sqlID;
		response thisResponse= new response( sqlID, maxPoints, "" );

		String theObfusticator= theSubmission.getParam( questionID );
		if (theObfusticator.length() == 0) 
		{
			theSubmission.addResponse( thisResponse );
			return;
		}
		
		if ( completeIncompleteGrading() )
		{
			int entryCount= 0;
			for (int j=0 ; j<choices.size() ; j++) 
			{
				String userEntry= theSubmission.getParam( questionID + "_" + obfusticateID(j,theObfusticator) );
				
				if (userEntry.length() == 0) continue;
				if (userEntry.equals("0")) continue;
							
				entryCount++;
			}

			if (entryCount == choices.size())
				thisResponse.points= maxPoints;
		}
		else
		{		
			String userRanking= "";
			int correctlyRanked= 0;
			for (int j=0 ; j<choices.size() ; j++) 
			{
				String userEntry= theSubmission.getParam( questionID + "_" + obfusticateID(j,theObfusticator) );
				
				if (userEntry.length() == 0)
				{
					theSubmission.addResponse( thisResponse );
					return;
				}
				
				else if (userEntry.equals("0"))		// unranked
				{ 
					if (userRanking.length()>0) userRanking += ",";
					userRanking += "0";
				}
				
				else								// ranked
				{
					theSubmission.replaceParam("Q_" + sqlID + "_answered", "true");

					if (userRanking.length()>0) userRanking += ",";
					userRanking += userEntry;
					try {
						if ((Integer.parseInt(userEntry) - 1) == j) correctlyRanked++;
					} catch (NumberFormatException n) {}
				}
				
			}
			
			thisResponse.recordValue(userRanking);
			if (choices.size() > 0)
			{
				if (correctlyRanked == choices.size()) thisResponse.points= maxPoints;
				/*
				float thePoints= (float)correctlyRanked;
				thePoints /= (float)choices.size();
				thePoints *= maxPoints;
				thisResponse.points= Math.round(thePoints);
				*/
				//thisResponse.points= (correctlyRanked * maxScore) / choices.size();
			}
		}
		
		theSubmission.addResponse( thisResponse );
	}	


	public void classwareEvaluate( tp_requestHandler theHandler, test theTest, submission theSubmission ) 
	{
		String questionID= "Q_" + sqlID;
		response thisResponse= new response( sqlID, choices.size(), "" );

		String theObfusticator= theSubmission.getParam( questionID );
		if (theObfusticator.length() == 0) 
		{
			theSubmission.addResponse( thisResponse );
			return;
		}
		
		if ( completeIncompleteGrading() )
		{
			int entryCount= 0;
			for (int j=0 ; j<choices.size() ; j++) 
			{
				String userEntry= theSubmission.getParam( questionID + "_" + obfusticateID(j,theObfusticator) );
				
				if (userEntry.length() == 0) continue;
				if (userEntry.equals("0")) continue;
							
				entryCount++;
			}

			if (entryCount == choices.size())
				thisResponse.points= maxPoints;
		}
		else
		{		
			String userRanking= "";
			int correctlyRanked= 0;
			for (int j=0 ; j<choices.size() ; j++) 
			{
				String userEntry= theSubmission.getParam( questionID + "_" + obfusticateID(j,theObfusticator) );
				
				if (userEntry.length() == 0)
				{
					theSubmission.addResponse( thisResponse );
					return;
				}
				
				else if (userEntry.equals("0"))		// unranked
				{ 
					if (userRanking.length()>0) userRanking += ",";
					userRanking += "0";
				}
				
				else								// ranked
				{
					theSubmission.replaceParam("Q_" + sqlID + "_answered", "true");

					if (userRanking.length()>0) userRanking += ",";
					userRanking += userEntry;
					try {
						if ((Integer.parseInt(userEntry) - 1) == j) correctlyRanked++;
					} catch (NumberFormatException n) {}
				}
				
			}
			
			thisResponse.recordValue(userRanking);
			thisResponse.points= correctlyRanked;
		}
		
		theSubmission.addResponse( thisResponse );
	}	
		
	
	public void hm_evaluate( tp_requestHandler theHandler, test theTest, submission theSubmission ) 
	{
		String questionID= "Q_" + sqlID;
		//response thisResponse= new response( sqlID, choices.size(), "" );
		response thisResponse= new response( sqlID, maxPoints, "" );

		String theObfusticator= theSubmission.getParam( questionID );
		if (theObfusticator.length() == 0) 
		{
			theSubmission.addResponse( thisResponse );
			return;
		}
		
		if ( completeIncompleteGrading() )
		{
			int entryCount= 0;
			for (int j=0 ; j<choices.size() ; j++) 
			{
				String userEntry= theSubmission.getParam( questionID + "_" + obfusticateID(j,theObfusticator) );
				
				if (userEntry.length() == 0) continue;
				if (userEntry.equals("0")) continue;
							
				entryCount++;
			}

			if (entryCount == choices.size())
				thisResponse.points= maxPoints;
			
			if( "yes".equals(theHandler.getParam(classware_hm.POLICY_participation)) && entryCount > 0){
				thisResponse.points= maxPoints;
				theSubmission.replaceParam("Q_" + sqlID + "_answered", "true");
			}
		}
		else
		{		
			String userRanking= "";
			int correctlyRanked= 0;
			for (int j=0 ; j<choices.size() ; j++) 
			{
				String userEntry= theSubmission.getParam( questionID + "_" + obfusticateID(j,theObfusticator) );
				
				if (userEntry.length() == 0)
				{
					theSubmission.addResponse( thisResponse );
					return;
				}
				
				else if (userEntry.equals("0"))		// unranked
				{ 
					if (userRanking.length()>0) userRanking += ",";
					userRanking += "0";
				}
				
				else								// ranked
				{
					theSubmission.replaceParam("Q_" + sqlID + "_answered", "true");

					if (userRanking.length()>0) userRanking += ",";
					userRanking += userEntry;
					try {
						if ((Integer.parseInt(userEntry) - 1) == j) correctlyRanked++;
					} catch (NumberFormatException n) {}
				}
				
			}
			
			thisResponse.recordValue(userRanking);
			
			thisResponse.points= 0;
			if (correctlyRanked == choices.size())
				thisResponse.points= classware_hm.DEFAULT_INTERNAL_POINTS;
			else if (choices.size() > 0)
				thisResponse.points= (classware_hm.DEFAULT_INTERNAL_POINTS * correctlyRanked) / choices.size();
			
			String answeredFlag = theSubmission.getParam("Q_" + sqlID + "_answered");
			if( "yes".equals(theHandler.getParam(classware_hm.POLICY_participation)) && "true".equalsIgnoreCase(answeredFlag) ){
				thisResponse.points= maxPoints;
			}
		}
		theSubmission.addResponse( thisResponse );
	}	
	
	public void hm_evaluate( tp_requestHandler theHandler, test theTest,PartialTO partialTO ) 
	{
		String questionID= "Q_" + sqlID;


		QuestionParameters questionParameters = partialTO.getQuestionTO().getQuestionMap().get(sqlID);
		if(questionParameters == null){
			questionParameters = new QuestionParameters();
			questionParameters.setQuestionID(sqlID);
			partialTO.getQuestionTO().getQuestionMap().put(sqlID, questionParameters);
		}
		questionParameters.setPointsMax(maxPoints);
		CustomMap<String, String> questionParamsMap = (CustomMap<String, String>)questionParameters.getQuestionParameters();

		String theObfusticator= questionParamsMap.getParam( questionID );
		if (theObfusticator.length() == 0) 
		{
			partialTO.getQuestionTO().getQuestionMap().put(sqlID, questionParameters);
			return;
		}

		if ( completeIncompleteGrading() )
		{
			int entryCount= 0;
			for (int j=0 ; j<choices.size() ; j++) 
			{
				String userEntry= questionParamsMap.getParam( questionID + "_" + obfusticateID(j,theObfusticator) );

				if (userEntry.length() == 0) continue;
				if (userEntry.equals("0")) continue;

				entryCount++;
			}

			if (entryCount == choices.size()){
				questionParameters.setPoints(maxPoints);
			}
			if( "yes".equals(theHandler.getParam(classware_hm.POLICY_participation)) && entryCount > 0){
				questionParameters.setPoints(maxPoints);
				questionParamsMap.replaceParam("Q_" + sqlID + "_answered", "true");
			}
		}
		else
		{		
			String userRanking= "";
			int correctlyRanked= 0;
			for (int j=0 ; j<choices.size() ; j++) 
			{
				String userEntry= questionParamsMap.getParam( questionID + "_" + obfusticateID(j,theObfusticator) );

				if (userEntry.length() == 0)
				{
					partialTO.getQuestionTO().getQuestionMap().put(sqlID, questionParameters);
					return;
				}

				else if (userEntry.equals("0"))		// unranked
				{ 
					if (userRanking.length()>0) userRanking += ",";
					userRanking += "0";
				}

				else								// ranked
				{
					questionParamsMap.replaceParam("Q_" + sqlID + "_answered", "true");

					if (userRanking.length()>0) userRanking += ",";
					userRanking += userEntry;
					try {
						if ((Integer.parseInt(userEntry) - 1) == j) correctlyRanked++;
					} catch (NumberFormatException n) {}
				}

			}


			questionParameters.getRecordedValue().addElement(userRanking);

			questionParameters.setPoints(0);
			if (correctlyRanked == choices.size()){
				questionParameters.setPoints(classware_hm.DEFAULT_INTERNAL_POINTS);
			}else if (choices.size() > 0){
				questionParameters.setPoints((classware_hm.DEFAULT_INTERNAL_POINTS * correctlyRanked) / choices.size());
			}
			String answeredFlag = questionParamsMap.getParam("Q_" + sqlID + "_answered");
			if( "yes".equals(theHandler.getParam(classware_hm.POLICY_participation)) && "true".equalsIgnoreCase(answeredFlag)){
				questionParameters.setPoints(maxPoints);
			}
		}
		//partialTO.getQuestionTO().getQuestionMap().put(sqlID, questionParameters);
	}	
	
	@Override
	public void updateCompletionStatus(QuestionWiseResponseTO questionWiseResponseTO,StringBuilder compStatus, String questionid) throws Exception {
		String questionID = "Q_" + questionid;
		String theObfusticator = "";
		String scrambledChoice = "";
		if(questionWiseResponseTO != null){
			theObfusticator = questionWiseResponseTO.getQuestionParameters().getParam(questionID);
			scrambledChoice = questionWiseResponseTO.getQuestionParameters().getParam(questionID+SCRAMBLED_CHOICES);
			if(StringUtils.isNotBlank(theObfusticator) && StringUtils.isNotBlank(scrambledChoice)){
				String[] scrambledChoiceArray = scrambledChoice.split(",");
				String obfusticator = null;	
				int index = 0;
				for (int j=0 ; j<scrambledChoiceArray.length ; j++)	{
					int thisChoiceIndex = Integer.parseInt(scrambledChoiceArray[j]);
					obfusticator = obfusticateID(thisChoiceIndex,theObfusticator);
					String userEntry = questionWiseResponseTO.getQuestionParameters().getParam(questionID+ "_" + obfusticator);
					if (userEntry.length() > 0 && !DEFAULT_RESPONSE.equals(userEntry) && !"0".equals(userEntry)) {
						String frustetedQuestionId = new StringBuilder(questionID).append("_").append(obfusticator).toString();
						if(compStatus != null && isNotBlank(compStatus.toString())){
							if(!(compStatus.toString()).contains(frustetedQuestionId)){
								compStatus.append("+").append(frustetedQuestionId);							
							}
						}else{
							//first time assign value to compStatus
							compStatus.append(frustetedQuestionId);
						}
						index++;
					}				    
				}	
				if(index == scrambledChoiceArray.length && !isNotBlank(referenceTag)){
					String plusQuestionId = new StringBuilder("+").append(questionid).toString();
					if(!(compStatus.toString()).contains(plusQuestionId)){
						compStatus.append(plusQuestionId);
					}
				}
			}			
		}
	}
	
	public Map<String, Object> evaluateResponse(QuestionWiseResponseTO questionWiseResponseTO,PolicyTO policyTO) throws Exception{
		Map<String, Object> evaluatedMap = new HashMap<String, Object>();
		String questionID = "Q_" + this.sqlID;
		String userRanking= "";
		//int correctlyRanked= 0;
		String theObfusticator = "";
		String scrambledChoice = "";
		try {
			if(questionWiseResponseTO != null){
				CustomMap<String,String> questionParameters = questionWiseResponseTO.getQuestionParameters();
				theObfusticator = questionParameters.getParam(questionID);
				if(StringUtils.isBlank(theObfusticator)){
					theObfusticator = getObfusticator();
					questionParameters.replaceParam(questionID, theObfusticator);
				}
				scrambledChoice = questionWiseResponseTO.getQuestionParameters().getParam(questionID+SCRAMBLED_CHOICES);
				if(StringUtils.isBlank(scrambledChoice)){
					scrambledChoice = QuestionUtil.getChoiceString(choices);
					questionParameters.replaceParam(questionID+SCRAMBLED_CHOICES, scrambledChoice);
				}
				evaluatedMap.put(RECORDED_VALUE, "");
				JSONArray responseWithCorrectnessJsonArray = new JSONArray();
				if(StringUtils.isNotBlank(theObfusticator) && StringUtils.isNotBlank(scrambledChoice)){
					String[] scrambledChoiceArray = scrambledChoice.split(",");
					String encryptedObfusticator = null;
					String beforeEncryptedObfusticator = null;
					
					for (int j=0 ; j<scrambledChoiceArray.length ; j++)	{
						JSONObject choice = new JSONObject();
						int thisChoiceIndex = Integer.parseInt(scrambledChoiceArray[j]);
						beforeEncryptedObfusticator = obfusticateID(thisChoiceIndex,theObfusticator);
						encryptedObfusticator = Crypt.encrypt(beforeEncryptedObfusticator);
						choice.put(CaaConstants.ID, encryptedObfusticator);
						choice.put(CaaConstants.CORRECT, false);
						String userEntry = questionParameters.getParam(questionID+ "_" + beforeEncryptedObfusticator);
					    if (userEntry.equals("0")) { 
							if (userRanking.length()>0){
								userRanking += ",";
							}
							userRanking += "0";
						} else {
							evaluatedMap.put(ANSWERED, "true");
							if (userRanking.length()>0){
								userRanking += ",";
							}
							userRanking += userEntry;
							try {
								if ((Integer.parseInt(userEntry) - 1) == thisChoiceIndex){
									//correctlyRanked++;
									choice.put(CaaConstants.CORRECT, true);
								}else{
									choice.put("correctChoice", j+1);
								}
							} catch (NumberFormatException n) {}
						}
					    responseWithCorrectnessJsonArray.put(choice);
					}					
					evaluatedMap.put(RECORDED_VALUE, userRanking);								
				}
				evaluatedMap.put(CORRECTNESS, responseWithCorrectnessJsonArray);		
			}
		}catch (Exception ex) {
			_logger.error("Exception while evaluating question of sqlID : "+ sqlID);
			throw ex;
		}
		return evaluatedMap;
	}
	
	public String v6analysis( tp_requestHandler theHandler, test theTest )
	{
		int correctCount= 0;
		int offerCount= 0;
		
		int counts[][] = new int[choices.size()][choices.size()+1];
		Connection con= null;
		PreparedStatement stmt = null;
		ResultSet rs= null;
		try
		{
			con= theHandler.getConnection();
			//Statement stmt= con.createStatement();
			
			//ResultSet rs= stmt.executeQuery("SELECT points, maxpoints, recordedValue FROM responses WHERE testID='" + theTest.sqlID + "' AND questionID='" + sqlID + "'");
			stmt= con.prepareStatement("SELECT points, maxpoints, recordedValue FROM responses WHERE testID=? AND questionID=?");
			stmt.setString(1, theTest.sqlID);
			stmt.setString(2, sqlID);
			rs= stmt.executeQuery();
			while (rs.next())
			{
				Vector values= tp_sql.vectorFromStream(rs.getBinaryStream("recordedValue"));
				if (values.size() == 0) continue;	//unoffered
				
				offerCount++;
				if (rs.getInt("points") == rs.getInt("maxpoints")) correctCount++;
				
				String thisResponse= (String)values.elementAt(0);
				
				StringTokenizer theTokens= new StringTokenizer(thisResponse, ",");
				for (int i=0; theTokens.hasMoreTokens(); i++)
				{
					try
					{
						int thisRanking= Integer.parseInt(theTokens.nextToken());
						if (thisRanking == 0) counts[i][choices.size()]++;
						else
							counts[i][thisRanking-1]++;						
					}
					catch (NumberFormatException ignore) {}
				}
			}
						
			tp_sql.releaseResources(con, stmt, rs);
		}
		catch (SQLException s)
		{
			theTest.sqlRoutine.reportException("multiplechoice.v6analysis()", s);
		}finally{
			tp_sql.releaseResources(con, stmt, rs);
		}
	
	
		String theAnalysis= "<p style=\"margin-left: 30; margin-top: 0; margin-bottom: 20\">never offered</p>";
		
		if (offerCount > 0)
		{		
			int thePct= (100 * correctCount) / offerCount;
			theAnalysis= "<p style=\"margin-left: 30; margin-top: 0; margin-bottom: 20\">Correctly answered " + Integer.toString(correctCount) + " out of " + Integer.toString(offerCount) + " (" + Integer.toString(thePct) + "%)<br>";
			
			theAnalysis += "<table style=\"font-size: 9pt\" border=\"1\" cellpadding=\"2\" cellspacing=\"2\"><tr><td>&nbsp;</td>";
			for (int i=0 ; i<choices.size() ; i++) 
				theAnalysis += "<td style=\"text-align: center\">#" + Integer.toString(i+1) + "</td>";
			theAnalysis += "<td>unranked</td></tr>";
			
			for (int i=0 ; i<choices.size() ; i++) 
			{
				String theChoice= (String)choices.elementAt(i);
				theChoice= randomVariable.deReference(theChoice, theTest, sqlID, theHandler.requestParams);
				theChoice= richMedia.deReference(theChoice, theHandler, sqlID, theHandler.requestParams);
			
				theAnalysis += "<tr><td style=\"text-align: right\">" + theChoice + "</td>";
				
				for (int j=0 ; j<choices.size() ; j++) 
				{
					String color= "";
					if (i==j) color= "; font-weight: bold; color: #004000";
					
					thePct= (100 * counts[i][j]) / offerCount;
					theAnalysis += "<td style=\"text-align: center" + color + "\">" + Integer.toString(thePct) + "%</td>";
				}

				thePct= (100 * counts[i][choices.size()]) / offerCount;
				theAnalysis += "<td style=\"text-align: center\">" + Integer.toString(thePct) + "%</td></tr>";
			}
			
			theAnalysis += "</table></p>";
		}
		
		return(theAnalysis);
	}


	public boolean upgradev4mediareferences( tp_requestHandler theHandler, test theTest )
	{
		boolean result= false;
		
		if (questionMedia != null)
		{		
			qtext= questionMedia.upgradev4mediareferences(qtext);
			questionMedia= null;
			result= true;
		}
		
		for (int i=0 ; i<choices.size() ; i++) 
		{
			String theChoice= (String)choices.elementAt(i);
			
			try 
			{
				richMedia theMedia= new richMedia( theChoice );
				choices.setElementAt(theMedia.upgradev4mediareferences(""), i);
				result= true;
			} 
			catch (testFormatException e) {}
		}
		
		return(result);
	}

	public String v5elxContent( tp_requestHandler theHandler, test theTest ) 
	{
		String result= "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r";
		
		result += "<raquestion xmlns=\"http://www.mhhe.com/EZTest/\">\r";

		result += "	<stem><p xmlns=\"http://www.w3.org/1999/xhtml\">\r";
		result += toXMLhtml(qtext, theHandler);
		result += "	</p></stem>\r";
	
		result += "	<reference>" + toXMLstring(referenceTag) + "</reference>\r";
		result += "	<page>" + toXMLstring(pageTag) + "</page>\r";

		if (longQuestion)
			result += "	<longquestion>true</longquestion>\r";
		else
			result += "	<longquestion>false</longquestion>\r";

		String theFeedback= "";
		if (feedback.size() > 0)
			theFeedback= (String)feedback.elementAt(0);
		result += "	<explanation><p xmlns=\"http://www.w3.org/1999/xhtml\">\r";
		result += toXMLhtml(theFeedback, theHandler);
		result += "	</p></explanation>\r";
	
		result += "	<raInfo>\r";
		
		VectorAdapter xmlChoices= new VectorAdapter();
		for (int i=0 ; i<choices.size() ; i++) 
		{
			String thisChoice = "			<item>\r";
			thisChoice += "				<itemhtml><p xmlns=\"http://www.w3.org/1999/xhtml\">\r";
			thisChoice += toXMLhtml((String)choices.elementAt(i), theHandler);
			thisChoice += "				</p></itemhtml>\r";
			thisChoice += "			</item>\r";
			
			xmlChoices.add(thisChoice);
		}
		
		if (xmlChoices.size() == 0)
		{
			String thisChoice = "			<item>\r";
			thisChoice += "				<itemhtml><p xmlns=\"http://www.w3.org/1999/xhtml\">\r";
			thisChoice += "				</p></itemhtml>\r";
			thisChoice += "			</item>\r";
			
			xmlChoices.add(thisChoice);
		}
		
		result += "		<items>\r";
		for (int i=0; i< xmlChoices.size(); i++)
			result += (String)xmlChoices.elementAt(i);
		result += " 	</items>\r";

		result += "	</raInfo>\r";
	
		result += "</raquestion>\r";
				
		return(result);
	}


	public String v7elxContent( tp_requestHandler theHandler, test theTest ) 
	{
		//boolean answerSpecificFeedbackSupport= licenseManager.hasFeature(theHandler, licenseManager.FEATURE_SUPPORT_ASFEEDBACK);

		String result= "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r";
		
		result += "<raquestion xmlns=\"http://www.mhhe.com/EZTest/\">\r";

		result += "	<stem><p xmlns=\"http://www.w3.org/1999/xhtml\">\r";
		result += toXMLhtml(qtext, theHandler);
		result += "	</p></stem>\r";
	
		result += "	<reference>" + toXMLstring(referenceTag) + "</reference>\r";
		result += "	<mpCustomTitle>" + toXMLstring( questionProperties.getString(MP_CUSTOM_TITLE, "") ) + "</mpCustomTitle>\r";

		result += "	<page>" + toXMLstring(pageTag) + "</page>\r";
		result += "	<selecttitle>" + toXMLstring(cleanSelectionTitle( selectionTitle )) + "</selecttitle>\r";
		result += "	<instructorinfo>" + toXMLstring( questionProperties.getString(INSTRUCTOR_INFO, "") ) + "</instructorinfo>\r";

		if (longQuestion)
			result += "	<longquestion>true</longquestion>\r";
		else
			result += "	<longquestion>false</longquestion>\r";

		result += "	 <" + COMPLETE_INCOMPLETE_GRADING + ">" + questionProperties.getString(COMPLETE_INCOMPLETE_GRADING, "false") + "</" + COMPLETE_INCOMPLETE_GRADING + ">\r";

		String theFeedback= "";
		if (feedback.size() > 0)
			theFeedback= (String)feedback.elementAt(0);
		
		String commonFeedback= questionProperties.getString(COMMON_FEEDBACK, null);
		if (commonFeedback != null) theFeedback= commonFeedback;

		result += "	<explanation><p xmlns=\"http://www.w3.org/1999/xhtml\">\r";
		result += toXMLhtml(theFeedback, theHandler);
		result += "	</p></explanation>\r";

		//if (answerSpecificFeedbackSupport){
			result += "	<solution><p xmlns=\"http://www.w3.org/1999/xhtml\">\r";
			result += toXMLhtml( questionProperties.getString(PROBLEM_SOLUTION, ""), theHandler);
			result += "	</p></solution>\r";

			result += "	 <useCommonFeedback>" + (questionProperties.getBoolean(USE_COMMON_FEEDBACK, true) ? "true" : "false") + "</useCommonFeedback>\r";
		//}
		
		result += "	<raInfo>\r";
		
		result += "	 <manualScoring>" + getScoring() + "</manualScoring>\r";

		//if (answerSpecificFeedbackSupport){
			String fbCorrect= "";
			if (feedback.size() > 0) fbCorrect= (String)feedback.elementAt(0);
			result += "  <correctfeedback><p xmlns=\"http://www.w3.org/1999/xhtml\">\r";
			result += toXMLhtml(fbCorrect, theHandler);
			result += "  </p></correctfeedback>\r";
			
			String fbIncorrect= "";
			if (feedback.size() > 1) fbIncorrect= (String)feedback.elementAt(1);
			result += "  <incorrectfeedback><p xmlns=\"http://www.w3.org/1999/xhtml\">\r";
			result += toXMLhtml(fbIncorrect, theHandler);
			result += "  </p></incorrectfeedback>\r";			
		//}

		VectorAdapter xmlChoices= new VectorAdapter();
		for (int i=0 ; i<choices.size() ; i++) 
		{
			String thisChoice = "			<item>\r";
			thisChoice += "				<itemhtml><p xmlns=\"http://www.w3.org/1999/xhtml\">\r";
			thisChoice += toXMLhtml((String)choices.elementAt(i), theHandler);
			thisChoice += "				</p></itemhtml>\r";
			thisChoice += "			</item>\r";
			
			xmlChoices.add(thisChoice);
		}
		
		if (xmlChoices.size() == 0)
		{
			String thisChoice = "			<item>\r";
			thisChoice += "				<itemhtml><p xmlns=\"http://www.w3.org/1999/xhtml\">\r";
			thisChoice += "				</p></itemhtml>\r";
			thisChoice += "			</item>\r";
			
			xmlChoices.add(thisChoice);
		}
		
		result += "		<items>\r";
		for (int i=0; i< xmlChoices.size(); i++)
			result += (String)xmlChoices.elementAt(i);
		result += " 	</items>\r";

		result += "	</raInfo>\r";
	
		result += hintXML( theHandler );
		result += contentLinkXML( theHandler );
		result += tooltipXML( theHandler );

		result += "</raquestion>\r";
				
		return(result);
	}
	

	public void v7update( tp_requestHandler theHandler, test theTest )
	{
		//boolean answerSpecificFeedbackSupport= licenseManager.hasFeature(theHandler, licenseManager.FEATURE_SUPPORT_ASFEEDBACK);

		clearPreviewCache( theHandler, theTest );
		
		String theXMLdata= theHandler.getParam("content");
		if (theXMLdata.length() == 0)
		{
			//v5edit( theHandler, theTest );
			_logger.error("update failure in ranking.v7update");
			return;
		}
		
		//System.out.println(theXMLdata);
		String theFeedback= "";
		String theAnswer= "";
		boolean correctSet= false;
		
		try 
		{
			SAXBuilder builder = new SAXBuilder();
			Document theXML = builder.build(new ByteArrayInputStream(theXMLdata.getBytes()));
			Element theQ= theXML.getRootElement();
			
			java.util.List theData= theQ.getChildren();
			ListIterator iter= theData.listIterator();
			
			//System.out.println( "XML data has " + Integer.toString(theData.size()) + " primary elements");
			
			while (iter.hasNext()) 
			{
				Element thisItem= (Element)iter.next();
				//System.out.println("  " + thisItem.getName());
				
				if (thisItem.getName().equals("stem"))
					qtext= embeddedMedia.processMediaIn( theHandler, theTest, getXMLhtml(thisItem, theHandler), sqlID );
				else if (thisItem.getName().equals("reference"))
					referenceTag= getXMLstring(thisItem);

				else if (thisItem.getName().equals("mpCustomTitle"))
					questionProperties.setString(MP_CUSTOM_TITLE, getXMLstring(thisItem));

				else if (thisItem.getName().equals(COMPLETE_INCOMPLETE_GRADING))
					setCompleteIncompleteGrading( getXMLstring(thisItem).equals("true") );

				else if (thisItem.getName().equals("page"))
					pageTag= getXMLstring(thisItem);
				else if (thisItem.getName().equals("selecttitle"))
					selectionTitle= cleanSelectionTitle( getXMLstring(thisItem) );
				else if (thisItem.getName().equals("instructorinfo"))
					questionProperties.setString(INSTRUCTOR_INFO, getXMLstring(thisItem));
				else if (thisItem.getName().equals("longquestion"))
					longQuestion= getXMLstring(thisItem).equals("true");

				else if (thisItem.getName().equals("solution"))
				{
					String theSolution= embeddedMedia.processMediaIn( theHandler, theTest, getXMLhtml(thisItem, theHandler), sqlID );
					questionProperties.setString(PROBLEM_SOLUTION, theSolution);
				}
				
				else if (thisItem.getName().equals("explanation"))
				{
					theFeedback= embeddedMedia.processMediaIn( theHandler, theTest, getXMLhtml(thisItem, theHandler), sqlID );
					questionProperties.setString(COMMON_FEEDBACK, theFeedback);
					//if (answerSpecificFeedbackSupport) 
						theFeedback= "";
					feedback= new VectorAdapter();
					//if (! answerSpecificFeedbackSupport) questionProperties.setBoolean(USE_COMMON_FEEDBACK, true);
				}
				else if (thisItem.getName().equals("useCommonFeedback"))
					questionProperties.setBoolean(USE_COMMON_FEEDBACK, getXMLstring(thisItem).equals("true"));
				
				else if (thisItem.getName().equals("contentLinkInfo")) parseContentLinks( thisItem, theHandler, theTest );

				else if (thisItem.getName().equals("hintInfo")) parseHints( thisItem, theHandler, theTest );

				else if (thisItem.getName().equals("tooltipInfo")) parseTooltips( thisItem, theHandler, theTest );

				else if (thisItem.getName().equals("raInfo"))
				{
					java.util.List raData= thisItem.getChildren();
					ListIterator iter2= raData.listIterator();
					while (iter2.hasNext()) 
					{
						Element raItem= (Element)iter2.next();
						//System.out.println("    " + mcItem.getName());

						if (raItem.getName().equals("manualScoring"))
							questionProperties.setString(question.CONNECT_FORCED_SCORING, getXMLstring(raItem));
						
						else if (raItem.getName().equals("correctfeedback"))
							feedback.addElement( embeddedMedia.processMediaIn( theHandler, theTest, getXMLhtml(raItem, theHandler), sqlID ) );

						else if (raItem.getName().equals("incorrectfeedback"))
							feedback.addElement( embeddedMedia.processMediaIn( theHandler, theTest, getXMLhtml(raItem, theHandler), sqlID ) );

						else if (raItem.getName().equals("items"))
						{
							choices= new VectorAdapter();
							points= new VectorAdapter();
							correctAnswerFeedback= new VectorAdapter();
							
							java.util.List choiceData= raItem.getChildren();
							ListIterator iter3= choiceData.listIterator();
							for (int i=0; iter3.hasNext(); i++) 
							{
								Element choiceItem= (Element)iter3.next();

								if (choiceItem.getName().equals("item"))
								{
									java.util.List thisChoiceData= choiceItem.getChildren();
									ListIterator iter4= thisChoiceData.listIterator();
									while (iter4.hasNext()) 
									{
										Element htmlItem= (Element)iter4.next();
										
										if (htmlItem.getName().equals("itemhtml"))
										{
											String thisChoice= embeddedMedia.processMediaIn( theHandler, theTest, getXMLhtml(htmlItem, theHandler), sqlID );
											//System.out.println("choice: " + thisChoice);
											choices.addElement( thisChoice );
											//if (!answerSpecificFeedbackSupport) feedback.addElement( theFeedback );
											points.addElement("1");
											correctAnswerFeedback.addElement("should be ranked #" + Integer.toString(choices.size()));
										}
									}
								}
							}
						}
					}
				}
			}
		}
		catch (IOException io)
		{
			_logger.error("XML IO error in ranking.v7update()", io);
		}
		catch (JDOMException jd) 
		{
			_logger.error("XML parsing error in ranking.v7update()", jd);
		}
		
		updateMediaConsumers( theHandler, theTest );
	}




	public void connect_collectPolicies( tp_requestHandler theHandler, ConcurrentHashMap<String, String> policyTable )
	{
		connect_defaultPolicies( theHandler, policyTable );
		collectContentLinkPolicies(  theHandler, policyTable );
		
		policyTable.put(classware_hm.POLICY_checkWork, classware_hm.POLICY_checkWork);
		policyTable.put(classware_hm.POLICY_printing, classware_hm.POLICY_printing);
	}
	
	
	
	public JSONObject getJSON( tp_requestHandler theHandler )
		throws JSONException
	{
		boolean partialCreditPolicy= questionProperties.getBoolean( NO_PARTIAL_CREDIT, noPartialCredit );
		questionProperties.setBoolean( NO_PARTIAL_CREDIT, partialCreditPolicy );
			
		JSONObject result= jsonStub( theHandler );
		
		result.put(XML_RA_STEM, tp_utils.safeJSON(qtext));
		
		boolean commonFeedback= true;
		String theFeedback= "";
		for (int i=0; i<feedback.size(); i++)
		{
			String thisFeedback= (String)feedback.elementAt(i);
			if (i==0) theFeedback= thisFeedback;
			else
			{
				if (!theFeedback.equals(thisFeedback))
				{
					commonFeedback= false;
					break;
				}
			}
		}
		
		String theCommonFeedback= questionProperties.getString(COMMON_FEEDBACK, null);
		if (theCommonFeedback == null)
			result.put(XML_RA_COMMONFEEDBACK, "");
		else
			result.put(XML_RA_COMMONFEEDBACK, tp_utils.safeJSON(theCommonFeedback));

		String fbCorrect= "";
		String fbIncorrect= "";
		if (feedback.size() > 0) fbCorrect= (String)feedback.elementAt(0);
		if (feedback.size() > 1) fbIncorrect= (String)feedback.elementAt(1);

		result.put(XML_RA_FBCORRECT, tp_utils.safeJSON(fbCorrect));
		result.put(XML_RA_FBINCORRECT, tp_utils.safeJSON(fbIncorrect));
			
		
		JSONArray jchoices= new JSONArray();
	
		for (int i=0; i<choices.size(); i++)
		{
			JSONObject choice= new JSONObject();
			
			String thisChoice= (String)choices.elementAt(i);
			choice.put( XML_RA_CHOICE_DISTRACTOR, tp_utils.safeJSON(thisChoice) );
	
			jchoices.put(choice);
		}
		
		result.put("items", jchoices);
		
		return result;
	}
	
	
	public JSONObject getItemQinfoJson()
	throws JSONException
	{
		richProperties questionProperties = new richProperties(this.questionProperties.toXML());
		boolean partialCreditPolicy= questionProperties.getBoolean( NO_PARTIAL_CREDIT, noPartialCredit );
		questionProperties.setBoolean( NO_PARTIAL_CREDIT, partialCreditPolicy );
				
		JSONObject result = getItemQinfoCommonJson();
		JSONArray props= questionProperties.exportJSON();
		if (props != null) result.put( "properties", props );
		result.put(XML_RA_STEM, tp_utils.safeJSON(qtext));
		
		boolean commonFeedback= true;
		String theFeedback= "";
		for (int i=0; i<feedback.size(); i++)
		{
			String thisFeedback= (String)feedback.elementAt(i);
			if (i==0) theFeedback= thisFeedback;
			else
			{
				if (!theFeedback.equals(thisFeedback))
				{
					commonFeedback= false;
					break;
				}
			}
		}
		
		String theCommonFeedback= questionProperties.getString(COMMON_FEEDBACK, null);
		if (theCommonFeedback == null)
			result.put(XML_RA_COMMONFEEDBACK, "");
		else
			result.put(XML_RA_COMMONFEEDBACK, tp_utils.safeJSON(theCommonFeedback));
	
		String fbCorrect= "";
		String fbIncorrect= "";
		if (feedback.size() > 0) fbCorrect= (String)feedback.elementAt(0);
		if (feedback.size() > 1) fbIncorrect= (String)feedback.elementAt(1);
	
		result.put(XML_RA_FBCORRECT, tp_utils.safeJSON(fbCorrect));
		result.put(XML_RA_FBINCORRECT, tp_utils.safeJSON(fbIncorrect));
			
		
		JSONArray jchoices= new JSONArray();
	
		for (int i=0; i<choices.size(); i++)
		{
			JSONObject choice= new JSONObject();
			
			String thisChoice= (String)choices.elementAt(i);
			choice.put( XML_RA_CHOICE_DISTRACTOR, tp_utils.safeJSON(thisChoice) );
	
			jchoices.put(choice);
		}
		
		result.put("items", jchoices);

		return result;
	}
	
	public boolean importJSON( tp_requestHandler theHandler, JSONObject theJSON )
		throws JSONException
	{
		jsonStubImport(theHandler, theJSON);
		
		if (theJSON.has(XML_RA_STEM))
			qtext= tp_utils.jsonPostProcess( theHandler, theJSON.getString(XML_RA_STEM), this);
	
		JSONArray jchoices= null;
		if (theJSON.has("items"))
			jchoices= theJSON.getJSONArray("items");
		else
			throw new JSONException("missing matches");
		
		choices= new VectorAdapter();
		feedback= new VectorAdapter();
		points= new VectorAdapter();
		
		boolean hasFeedback= false;
		
		for (int i=0; i<jchoices.length(); i++)
		{
			JSONObject thisChoice= jchoices.getJSONObject(i);
			
			String jdistractor= null;
									
			if (thisChoice.has(XML_RA_CHOICE_DISTRACTOR))
				jdistractor= tp_utils.jsonPostProcess( theHandler, thisChoice.getString(XML_RA_CHOICE_DISTRACTOR), this);
			
			if (jdistractor != null) 
			{
				choices.addElement(jdistractor);				
				points.addElement("1");
			}	
			else
				throw new JSONException("improperly formatted JSON item");
		}
		
		String theCommonFeedback= questionProperties.getString(COMMON_FEEDBACK, null);
		
		if (theJSON.has(XML_RA_FBCORRECT))
		{
			String theFeedback= tp_utils.jsonPostProcess( theHandler, theJSON.getString(XML_RA_FBCORRECT), this).trim();
			if (theFeedback.length() > 0)
			{
				hasFeedback= true;
				feedback.addElement(theFeedback);
			}
			else
				feedback.addElement(theCommonFeedback);
		}
		else
			feedback.addElement(theCommonFeedback);
		
		if (theJSON.has(XML_RA_FBINCORRECT))
		{
			String theFeedback= tp_utils.jsonPostProcess( theHandler, theJSON.getString(XML_RA_FBINCORRECT), this).trim();
			if (theFeedback.length() > 0)
			{
				hasFeedback= true;
				feedback.addElement(theFeedback);
			}
			else
				feedback.addElement(theCommonFeedback);
		}
		else
			feedback.addElement(theCommonFeedback);
		
		questionProperties.setBoolean(USE_COMMON_FEEDBACK, hasFeedback);
		
		noPartialCredit= questionProperties.getBoolean( NO_PARTIAL_CREDIT, false );
		
		return true;
	}

	
	
	
	static String		XML_RA						= "ranking";
	
	static String		XML_RA_STEM					= "stem";
	
	static String		XML_RA_CHOICE_SET			= "items";
	static String		XML_RA_COMMONFEEDBACK		= "commonFeedback";

	static String		XML_RA_CHOICE				= "item";
	static String		XML_RA_CHOICE_DISTRACTOR	= "distractor";
	static String		XML_RA_CHOICE_CREDIT		= "credit";
	
	static String		XML_RA_CHOICE_FEEDBACK		= "feedback";

	static String		XML_RA_FBCORRECT			= "correctfeedback";
	static String		XML_RA_FBINCORRECT			= "incorrectfeedback";

	
	public Element buildExportXML( tp_requestHandler theHandler, test theTest )
	{
		//boolean answerSpecificFeedbackSupport= licenseManager.hasFeature(theHandler, licenseManager.FEATURE_SUPPORT_ASFEEDBACK);

		// make sure this is in the questionProperties
		boolean partialCreditPolicy= questionProperties.getBoolean( NO_PARTIAL_CREDIT, noPartialCredit );
		questionProperties.setBoolean( NO_PARTIAL_CREDIT, partialCreditPolicy );
	
		Element result= xmlStub( theHandler, theTest );

		Element ra= new Element(XML_RA);
		result.addContent(ra);
		
		Element stem= new Element(XML_RA_STEM);
		ra.addContent(stem);
		stem.addContent( tp_utils.safeCDATA(qtext) );
		
		boolean commonFeedback= true;
		String theFeedback= "";
		for (int i=0; i<feedback.size(); i++)
		{
			String thisFeedback= (String)feedback.elementAt(i);
			if (i==0) theFeedback= thisFeedback;
			else
			{
				if (!theFeedback.equals(thisFeedback))
				{
					commonFeedback= false;
					break;
				}
			}
		}
		
		// see if we have defined common feedback
		String theCommonFeedback= questionProperties.getString(COMMON_FEEDBACK, null);
		if (theCommonFeedback != null) {
			commonFeedback= false;
			
			Element common= new Element(XML_RA_COMMONFEEDBACK);
			ra.addContent(common);
			common.addContent( tp_utils.safeCDATA(theCommonFeedback) );
		}
		else if (commonFeedback)
		{
			Element common= new Element(XML_RA_COMMONFEEDBACK);
			ra.addContent(common);
			common.addContent( tp_utils.safeCDATA(theFeedback) );
		}
		
		
		String fbCorrect= theFeedback;
		String fbIncorrect= theFeedback;
		if (feedback.size() > 0) fbCorrect= (String)feedback.elementAt(0);
		if (feedback.size() > 1) fbIncorrect= (String)feedback.elementAt(1);
		if (!commonFeedback)
		{
			Element cfb= new Element(XML_RA_FBCORRECT);
			ra.addContent(cfb);
			cfb.addContent( tp_utils.safeCDATA(fbCorrect) );			
	
			Element icfb= new Element(XML_RA_FBINCORRECT);
			ra.addContent(icfb);
			icfb.addContent( tp_utils.safeCDATA(fbIncorrect) );			
		}
		
		
		Element choiceset= new Element(XML_RA_CHOICE_SET);
		ra.addContent(choiceset);
		
		for (int i=0; i<choices.size(); i++)
		{
			String thisChoice= (String)choices.elementAt(i);
			String thisPoints= "";
			if (i < points.size()) thisPoints= (String)points.elementAt(i);
			String thisFeedback= "";
			if (i < feedback.size()) thisFeedback= (String)feedback.elementAt(i);
			
			Element choice= new Element(XML_RA_CHOICE);
			choiceset.addContent( choice );
			
			Element distractor= new Element(XML_RA_CHOICE_DISTRACTOR);
			choice.addContent(distractor);
			distractor.addContent( tp_utils.safeCDATA(thisChoice) );
			
			Element credit= new Element(XML_RA_CHOICE_CREDIT);
			choice.addContent(credit);
			credit.setText( thisPoints );
			
			if (!commonFeedback)
			{
				// this is incorrect but leaving for backwards compat
				Element fb= new Element(XML_RA_CHOICE_FEEDBACK);
				choice.addContent(fb);
				fb.addContent( tp_utils.safeCDATA(thisFeedback) );
			}
		}
		
		return result; 
	}


	public static question buildFromXML( tp_requestHandler theHandler, test theTest, Element qElement )
	{
		//boolean answerSpecificFeedbackSupport= licenseManager.hasFeature(theHandler, licenseManager.FEATURE_SUPPORT_ASFEEDBACK);

		ranking theQ= new ranking();
		theQ.stdXMLimport( theHandler, theTest, qElement );
		
		theQ.noPartialCredit= theQ.questionProperties.getBoolean( NO_PARTIAL_CREDIT, false );
		
		theQ.choices= new VectorAdapter();
		theQ.points= new VectorAdapter();
		theQ.feedback= new VectorAdapter();
		
		boolean updatedXML= false;
		
		try
		{
			Element mcElement= qElement.getChild(XML_RA);
			
			String commonFeedback= null;
			java.util.List mcInfo= mcElement.getChildren();
			ListIterator iter= mcInfo.listIterator();
			while (iter.hasNext()) 
			{
				Element thisChild= (Element)iter.next();
				String thisName= thisChild.getName();
			
				if (thisName.equals(XML_RA_STEM))
					theQ.qtext= thisChild.getText();
			
				else if (thisName.equals(XML_RA_COMMONFEEDBACK))
				{
					commonFeedback= thisChild.getText();
					//if (answerSpecificFeedbackSupport && (theQ.questionProperties.getString(COMMON_FEEDBACK, null) == null))
					if (theQ.questionProperties.getString(COMMON_FEEDBACK, null) == null)
						theQ.questionProperties.setString(COMMON_FEEDBACK, commonFeedback);
				}
			
				else if (thisName.equals(XML_RA_FBCORRECT))
				{
					updatedXML= true;
					theQ.feedback.addElement( thisChild.getText() );
				}
			
				else if (thisName.equals(XML_RA_FBINCORRECT))
				{
					updatedXML= true;
					theQ.feedback.addElement( thisChild.getText() );
				}
				
				else if (thisName.equals(XML_RA_CHOICE_SET))
				{
					java.util.List choiceInfo= thisChild.getChildren();
					ListIterator iter2= choiceInfo.listIterator();
					while (iter2.hasNext()) 
					{
						Element thisChoice= (Element)iter2.next();
						
						if (thisChoice.getName().equals(XML_RA_CHOICE))
						{
							String choice= thisChoice.getChildText(XML_RA_CHOICE_DISTRACTOR);
							if (choice == null) continue;
							
							String award= thisChoice.getChildText(XML_RA_CHOICE_CREDIT);
							if (award == null) continue;
							
							String fb= thisChoice.getChildText(XML_RA_CHOICE_FEEDBACK);
							if (fb == null) fb= commonFeedback;
							
							theQ.choices.addElement(choice);
							theQ.points.addElement(award);
							if (!updatedXML) theQ.feedback.addElement(fb);
						}
					}
				}
			}
			theQ.importQFromXML( theHandler, theTest, qElement );
		}
		catch (Exception e)
		{
			_logger.error("Exception parsing XML in ranking.buildFromXML()");
		}
		
		return theQ;	
	}


	public boolean compareQ( question otherQ )
	{
		if (otherQ.type != type) { _logger.info("type mismatch"); return false; }
		
		ranking theQ= (ranking)otherQ;
		
		boolean result= true;
		
		if (theQ.noPartialCredit != noPartialCredit) { _logger.info("noPartialCredit mismatch"); result= false; }
		
		if (theQ.choices.size() != choices.size()) { _logger.info("choices size mismatch"); result= false; }
		if (theQ.points.size() != points.size()) { _logger.info("points size mismatch"); result= false; }
		if (theQ.feedback.size() != feedback.size()) { _logger.info("feedback size mismatch"); result= false; }
		
		if (result != false)
		{
			for (int i=0; i<choices.size(); i++)
			{
				if (!((String)choices.elementAt(i)).equals(((String)theQ.choices.elementAt(i)))) { _logger.info("choice mismatch"); result= false; break; }
			}
			
			for (int i=0; i<points.size(); i++)
			{
				if (!((String)points.elementAt(i)).equals(((String)theQ.points.elementAt(i)))) { _logger.info("points mismatch"); result= false; break; }
			}
			
			for (int i=0; i<feedback.size(); i++)
			{
				if (!((String)feedback.elementAt(i)).equals(((String)theQ.feedback.elementAt(i)))) { _logger.info("feedback mismatch"); result= false; break; }
			}
		}
		
		return (result && compareStd( otherQ ));
	}
	public JSONObject populateQuestionJSON() 
	throws Exception
	{
		JSONObject jsonObj = new JSONObject();
		jsonObj.append("type", this.typeIdentifier());
		String qText = this.qtext;
		qText = tp_utils.removeMediaRefSpecialCharsAndSpace(qText);
		jsonObj.append("stem", qText);
		
		VectorAdapter arrList = this.choices;
		if(arrList != null && !arrList.isEmpty()){
			for(int i = 0; i < arrList.size(); i++){
				if(arrList.get(i) != null){
					String temp = (String)arrList.get(i);
					jsonObj.append("choices", tp_utils.removeMediaRefSpecialCharsAndSpace(temp));
				}
			}
		}

		arrList = this.feedback;
		if(arrList != null && !arrList.isEmpty()){
			for(int i = 0; i < arrList.size(); i++){
				if(arrList.get(i) != null){
					String temp = (String)arrList.get(i);
					jsonObj.append("feedback", tp_utils.removeMediaRefSpecialCharsAndSpace(temp));
				}
			}
		}
		return jsonObj;
	}
	/**
	 * @see question#getRedactedQinfoJson(UserResponseWithPolicyTO, String, String)
	 */
	@Override
	public JSONObject getRedactedQinfoJson(UserResponseWithPolicyTO userResponseWithPolicyTO,String testId,String mode) throws Exception {

		Map<String,String> policyMap = new HashMap<String,String>();
		ResponseTO responseTO = null;
		CustomMap<String, String> testParamMap = null;
		if(userResponseWithPolicyTO != null){
			if(userResponseWithPolicyTO.getPolicyTO() != null){
				policyMap = userResponseWithPolicyTO.getPolicyTO().getPolicyMap();
			}				
			responseTO = userResponseWithPolicyTO.getResponseTO();
			testParamMap = responseTO.getTestParameter();
		}
		String postSubmissionMode = policyMap.get(classware_hm.POLICY_grading);
		boolean showFeedBack = QuestionUtil.showExplanation(testParamMap, policyMap);
		boolean isTestMode =  TEST_MODE.equals(mode);
		boolean isReviewMode =  REVIEW_MODE.equals(mode);
		richProperties	questionProperties = null;
		if(isTestMode){
			questionProperties = richProperties.newInstance("TestModeRedactedProperties");			
		}else {
			questionProperties = new richProperties(this.questionProperties.toXML());
		}
		String theCommonFeedback= questionProperties.getString(COMMON_FEEDBACK, null);
		if(isTestMode){
			questionProperties.setBD( "questionPoint", this.questionProperties.getBD(classware_hm.HM_POINTS, "10") );
		} else {
			questionProperties.setBD( "questionPoint", this.questionProperties.getBD(classware_hm.HM_POINTS, "10") );
			boolean partialCreditPolicy= questionProperties.getBoolean( NO_PARTIAL_CREDIT, noPartialCredit );
			questionProperties.setBoolean( NO_PARTIAL_CREDIT, partialCreditPolicy );
		}
				
		JSONObject result = super.getRedactedQinfoJson(userResponseWithPolicyTO, testId, mode);
		if(isReviewMode && (classware_hm.POLICY_grading_none.equals(postSubmissionMode)||classware_hm.POLICY_grading_score.equals(postSubmissionMode))){
			return result;
		}

		questionProperties = getPolicyBasedQuestionProperties(questionProperties, userResponseWithPolicyTO);
		JSONArray props= questionProperties.exportJSON();
		if(isTestMode){
			
			for(int i=0 , length = props.length(); i  < length ; i++){
				if(((JSONObject)props.get(i)).get("name").equals(AGGREGATED_GRADING)){
					props.remove(i);
					length = props.length();
				}
				if(((JSONObject)props.get(i)).get("name").equals(COMPLETE_INCOMPLETE_GRADING)){
					props.remove(i);
					length = props.length();
				}
			}

		}
		if (props != null){
			result.put( "properties", props );
		}
	
		CustomMap<String, String> questionResponseMap = responseTO.getResponseMap().get(this.sqlID).getQuestionParameters();
		
		String theObfusticator = questionResponseMap.getParam("Q_"+this.sqlID);		
		if(StringUtils.isBlank(theObfusticator)){
			theObfusticator = getObfusticator();
		}
		
		JSONArray jchoices= new JSONArray();
		String thisChoice = null;
		String thisId = null;
		String thisIdEncrypted = null;
		boolean allCorrect = true;
		boolean answered = false;
		String theQID = "Q_"+this.sqlID;
		String userResponse = null;
		int userSelectedIndex = -1;
		for (int i=0; i<choices.size(); i++) {
			boolean correctlyAnswered = false;
			JSONObject choice= new JSONObject();			
			thisChoice= (String)choices.elementAt(i);
			thisId = obfusticateID(i,theObfusticator);
			thisIdEncrypted = Crypt.encrypt(thisId);
			choice.put(CaaConstants.ID, thisIdEncrypted);
			
			userResponse = questionResponseMap.getParam(theQID+"_"+thisId);
			if (StringUtils.isNotBlank(userResponse) && !"0".equals(userResponse)){
				answered = true;
				try {
					userSelectedIndex = Integer.parseInt(userResponse);
				}catch (NumberFormatException n) {}
				
				if((userSelectedIndex-1) == i){
					correctlyAnswered = true;
				}else{
					correctlyAnswered = false;
				}
			}
			allCorrect &= correctlyAnswered;
			choice.put( XML_RA_CHOICE_DISTRACTOR, tp_utils.safeJSON(thisChoice) );	
			jchoices.put(choice);
		}
		
		/*if(isReviewMode && showFeedBack){
			boolean answerSpecificFeedbackExist = false;
			String fbCorrect= null;
			String fbIncorrect= null;
			if (allCorrect && feedback.size() > 0 ){
				fbCorrect = ((String)feedback.elementAt(0)).trim();
				if(StringUtils.isNotBlank(fbCorrect)){
					result.put(XML_RA_FBCORRECT, tp_utils.safeJSON(fbCorrect));
					answerSpecificFeedbackExist = true;
				}
			}
			if (answered && !allCorrect && feedback.size() > 1 ){
				fbIncorrect = ((String)feedback.elementAt(1)).trim();
				if(StringUtils.isNotBlank(fbIncorrect)){
					result.put(XML_RA_FBINCORRECT, tp_utils.safeJSON(fbIncorrect));
					answerSpecificFeedbackExist = true;
				}
			}
			if(!answerSpecificFeedbackExist){
				if(StringUtils.isNotBlank(theCommonFeedback)){
					result.put(XML_RA_COMMONFEEDBACK, theCommonFeedback);
				}
			}
		}*/
		//result.put("items", jchoices);
		String scrambledChoice = questionResponseMap.get(theQID+SCRAMBLED_CHOICES);
		if(StringUtils.isBlank(scrambledChoice)){
			_logger.info("## scrambledChoice is null : going to generate one for the questionid:"+this.sqlID);
			scrambledChoice = QuestionUtil.getChoiceString(choices);
			questionResponseMap.replaceParam(theQID+SCRAMBLED_CHOICES, scrambledChoice);
		}
		String[] scrambledChoiceArray = scrambledChoice.split(",");
		
		JSONArray jchoicesScrambled = new JSONArray();
		for(int i=0; i< scrambledChoiceArray.length; i++){
			int thisChoiceIndex = Integer.parseInt(scrambledChoiceArray[i]);
			jchoicesScrambled.put(jchoices.get(thisChoiceIndex));
		}
		
		result.put("items", jchoicesScrambled);
		
		result = super.getMediaDereferencedJson(result, testId);
		return result;			
	}


	public Map<String, String> translateRinfoToQuestionResponse(JSONObject jsonObject, UserResponseWithPolicyTO userResponseWithPolicyTO) throws Exception {
		Map<String, String> questionParamMap = new HashMap<String, String>();
		
		try {
			if (jsonObject.has(QUESTIONID)) {
				String questionId = jsonObject.getString(QUESTIONID);
				if (questionId != null && questionId.length() > 0) {
					String keyPrefix = "Q_" + questionId;

					if (jsonObject.has(RESPONSE)) {
						JSONArray responseArray = jsonObject.getJSONArray(RESPONSE);
						String encryptedId = null;
						String decryptedId = null;
						for(int i=0 ; i < responseArray.length() ; i++){
							JSONObject responseObj = responseArray.getJSONObject(i);
							encryptedId = responseObj.getString(CaaConstants.ID);
							decryptedId = Crypt.decrypt(encryptedId);
							String value = responseObj.getString(CaaConstants.VALUE);
							if("".equals(value)){
								value = "0";
							}
							questionParamMap.put(keyPrefix + "_" + decryptedId , value);							
						}
					}
					/*if (jsonObject.has(SCRAMBLE)) {
						String scrambleOrder = jsonObject.getString(SCRAMBLE);
						if (scrambleOrder != null && scrambleOrder.length() > 0) {
							questionParamMap.put(keyPrefix + "_scramble",scrambleOrder); // added not exist now.
						}
					}*/
				}
			}

		} catch (JSONException e) {
			_logger.error("Error parsing translateRinfoToQuestionResponse in ranking for qid="+sqlID,e);
			throw new Exception(e);
		}
		
		return questionParamMap;
	}

	/**
	 * @see question#translateQuestionResponseToRinfo(UserResponseWithPolicyTO, QuestionMetaDataTO, String, TestTO)
	 */
	public JSONObject translateQuestionResponseToRinfo(UserResponseWithPolicyTO userResponseWithPolicyTO, QuestionMetaDataTO questionMetaDataTO, String mode,TestTO testTo) throws Exception{
		JSONObject rinfoJson = null;
		String theQID = "Q_" + sqlID;
		PolicyTO policyTO = null;
		ResponseTO responseTO = null;
		Map<String, String> policyMap = null;
		String correctness = "";
		try {
			rinfoJson = super.translateQuestionResponseToRinfo(userResponseWithPolicyTO,questionMetaDataTO, mode, testTo);
			QuestionWiseResponseTO questionWiseResponseTO = userResponseWithPolicyTO.getResponseTO().getResponseMap().get(sqlID);
			CustomMap<String, String> questionResponseMap = questionWiseResponseTO.getQuestionParameters();
			String theObfusticator = questionResponseMap.getParam(theQID);
			policyTO = userResponseWithPolicyTO.getPolicyTO();
			policyMap = policyTO.getPolicyMap();
			responseTO = userResponseWithPolicyTO.getResponseTO();
			String postSubmissionMode = policyMap.get(classware_hm.POLICY_grading);
			boolean isPostSubmission = (classware_hm.POLICY_grading_feedback.equals(postSubmissionMode)||classware_hm.POLICY_grading_scoreplus.equals(postSubmissionMode));
			boolean isIndicatorRequired = isPostSubmission &&
						 				  !completeIncompleteGrading() &&
						 				  responseTO.isSubmission() &&
						 				  QuestionUtil.getPolicyValue(classware_hm.POLICY_indicators, null, policyMap, responseTO.isSubmission()); 
			boolean isScoreEachQuestion = "yes".equals(policyMap.get(classware_hm.POLICY_feedback));
			boolean isShown = isQuestionResponseLockedFBQ(responseTO,this.sqlID);
			if(isScoreEachQuestion && isShown){
				isIndicatorRequired = true;
			}
			String scrambledChoice = questionResponseMap.get(theQID+SCRAMBLED_CHOICES);
			if(StringUtils.isNotBlank(theObfusticator) && StringUtils.isNotBlank(scrambledChoice)){
				JSONArray jchoices= new JSONArray();
				String userResponse = null;
				String[] scrambledChoiceArray = scrambledChoice.split(",");
				String beforeEncryptedId = null;
				String afterEncryptedId = null;
				int userSelectedIndex = -1;
				Map<Integer, String> userResponseMap = new HashMap<Integer, String>();
				for (int i=0; i<scrambledChoiceArray.length; i++){
					int thisChoiceIndex = Integer.parseInt(scrambledChoiceArray[i]);
					beforeEncryptedId = obfusticateID(thisChoiceIndex,theObfusticator);
					afterEncryptedId = Crypt.encrypt(beforeEncryptedId);
					JSONObject choice = new JSONObject();
					choice.put(CaaConstants.ID, afterEncryptedId);
					userResponse = questionResponseMap.getParam(theQID+"_"+beforeEncryptedId);
					if("0".equals(userResponse)){
						userResponse = "";
					}
					choice.put(CaaConstants.VALUE, userResponse);
					if(CaaConstants.REVIEW.equals(mode)){
						if(isIndicatorRequired){
							/*if (StringUtils.isNotBlank(userResponse) && (Integer.parseInt(userResponse) - 1) == thisChoiceIndex){
								correctness = CaaConstants.TRUE;
							}else{
								correctness = CaaConstants.FALSE;
							}
							choice.put(CaaConstants.CORRECT, correctness);*/
							if(StringUtils.isNotBlank(userResponse)){
								try {
									userSelectedIndex = Integer.parseInt(userResponse);
								}catch (NumberFormatException n) {}
								
								if((userSelectedIndex-1) == thisChoiceIndex){
									correctness = CaaConstants.TRUE;
								}else{
									correctness = CaaConstants.FALSE;
								}
							}else{
								correctness = CaaConstants.FALSE;
							}
							choice.put(CaaConstants.CORRECT, correctness);
							userResponseMap.put(i, correctness);
						}
						//choice.put(CaaConstants.CORRECT, correctness);
						boolean solution = CaaConstants.YES.equals(policyMap.get(classware_hm.POLICY_solution) );
						boolean fullFeedback = classware_hm.POLICY_grading_feedback.equals(policyMap.get(classware_hm.POLICY_grading) );
						if(solution || fullFeedback){
							choice.put(CaaConstants.CORRECT_ANSWER, thisChoiceIndex+1);
						}
					}
					
					jchoices.put(choice);
				}
				rinfoJson.put(RESPONSE, jchoices );
				/**
				 * Get feed back
				 */
				FeedBackTO feedBackTO = getQuestionFeedback(userResponseWithPolicyTO, userResponseMap, mode);
				/**
				 * Create feed back Json
				 */
				JSONObject feedBackJson = createFeebBackJson(feedBackTO, userResponseWithPolicyTO);
				if(feedBackJson != null && feedBackJson.length() > 0){
					/**
					 * Apply tooltip dereference and media dereference
					 */
					feedBackJson = getMediaDereferencedJson(feedBackJson, responseTO.getTestID());
					/**
					 * put the feedback in rinfo
					 */
					rinfoJson.put(CaaConstants.FEEDBACK, feedBackJson);
				}
			}
			/*if(!REVIEW_MODE.equals(mode)){
				String scrambledChoice = questionResponseMap.getParam(theQID+SCRAMBLED_CHOICES);			
				rinfoJson.put(SCRAMBLE, QuestionUtil.getIntArrayFromString(scrambledChoice) );
			}*/			
		} catch (Exception e) {
			throw e;
		}
		return rinfoJson;
	}
	
	/**
	 * @see question#itemEvaluate(QuestionWiseResponseTO, PolicyTO)
	 */
	public void itemEvaluate(QuestionWiseResponseTO questionWiseResponseTO,	PolicyTO policyTO) throws Exception {

		try {
			if (questionWiseResponseTO != null && policyTO != null) {
				CustomMap<String, String> questionParameters = questionWiseResponseTO.getQuestionParameters();
				String questionIdPrefix = "Q_" + sqlID;
				questionWiseResponseTO.setQuestionID(this.sqlID);
				questionWiseResponseTO.setPointsMax(classware_hm.DEFAULT_INTERNAL_POINTS);
				Map<String, Object> evaluatedMap = evaluateResponse(questionWiseResponseTO, policyTO);

				if (completeIncompleteGrading()|| (policyTO.getPolicyMap() != null && "yes".equals(policyTO.getPolicyMap().get(classware_hm.POLICY_participation)))) {
					if (evaluatedMap != null && !evaluatedMap.isEmpty()&& "true".equals(evaluatedMap.get(ANSWERED))) {
						questionParameters.replaceParam(questionIdPrefix + "_answered",evaluatedMap.get(ANSWERED));
						questionWiseResponseTO.setPoints(classware_hm.DEFAULT_INTERNAL_POINTS);
					}
				} else {
					if (evaluatedMap != null && !evaluatedMap.isEmpty()) {
						if(CaaConstants.TRUE.equals(evaluatedMap.get(ANSWERED))){
							questionParameters.replaceParam(questionIdPrefix + "_answered",evaluatedMap.get(ANSWERED));
						}
						questionWiseResponseTO.getRecordedValue().add((String)evaluatedMap.get(RECORDED_VALUE));
						JSONArray corrArray = (JSONArray)evaluatedMap.get(CORRECTNESS);
						if (corrArray != null) {
							int correctCount = 0;							
							for (int i = 0; i < corrArray.length(); i++) {
								JSONObject choice = corrArray.getJSONObject(i);
								if(choice.has(CaaConstants.CORRECT)){
									boolean isCorrect = choice.getBoolean(CaaConstants.CORRECT);
									if (isCorrect) {
										correctCount++;
									}
								}								
							}
							questionWiseResponseTO.setPoints(0);
							if (correctCount == corrArray.length()) {
								questionWiseResponseTO.setPoints(classware_hm.DEFAULT_INTERNAL_POINTS);
							} else if (corrArray.length() > 0) {
								questionWiseResponseTO.setPoints((classware_hm.DEFAULT_INTERNAL_POINTS * correctCount)/ corrArray.length());
							}
						}
					}
				}
			}
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * @see com.mcgrawhill.ezto.test.questions.question#generateScrambledChoice(Map, Map)
	 */
	public void generateScrambledChoice(CustomMap<String, String> questionResponseMap, Map<String, String> requestMap) throws Exception{
		String snapShotParamName = "Q_"+sqlID+SCRAMBLED_CHOICES;
		String theObfusticator = questionResponseMap.getParam("Q_"+sqlID);

		//to generate the Obfusticator if not already generated
		if(theObfusticator == null || theObfusticator.length() == 0){
			theObfusticator = getObfusticator();
			questionResponseMap.replaceParam("Q_"+sqlID, theObfusticator);
		}
		String choicesSnapShot = questionResponseMap.getParam(snapShotParamName);
		if(choicesSnapShot == null || choicesSnapShot.length() == 0){
			choicesSnapShot = QuestionUtil.getChoiceString(choices);
			questionResponseMap.replaceParam(snapShotParamName, choicesSnapShot);
		}
	}
	
	/**
	 * This method generates the master obfuscator for the ranking type question. This 
	 * obfuscator will be used to generate the obfuscator for all the choices of the question.   
	 * @return String
	 * @throws Exception
	 */
	private String getObfusticator() {
		String theObfusticator = "";
		try{
			double obfusticator1= Math.rint(Math.random() * 1000);
			double obfusticator2= Math.rint(Math.random() * 1000);
			double obfusticator3= Math.rint(Math.random() * 100);
			String ob1Str= Integer.toString((int)obfusticator1);
			while (ob1Str.length() < 3) ob1Str= "0" + ob1Str;
			String ob2Str= Integer.toString((int)obfusticator2);
			while (ob2Str.length() < 3) ob2Str= "0" + ob2Str;
			String ob3Str= Integer.toString((int)obfusticator3);
			while (ob3Str.length() < 2) ob3Str= "0" + ob3Str;
			theObfusticator = ob1Str + ob3Str + ob2Str;
		}catch(Exception ex){
			_logger.info("Exception occurred during getObfusticator for qid:"+this.sqlID);
		}
		return theObfusticator;
	}
	
	@Override
	public BigDecimal getAnsweredPercentage(CustomMap<String, String> questionParam, String responseKey, QuestionWiseResponseTO questionWiseResponseTO) throws Exception {
		BigDecimal percent = new BigDecimal(0.00).setScale(2, RoundingMode.HALF_UP);
		if(questionWiseResponseTO != null){
			CustomMap<String, String> questionResponseMap = questionWiseResponseTO.getQuestionParameters();
			if(questionResponseMap != null){
				String theObfusticator = questionResponseMap.getParam(responseKey);
				
				if(!StringUtils.isBlank(theObfusticator)){
					int answeredChoices = 0;
					int totalChoices = 0;
					totalChoices = choices.size();
					for (int j=0 ; j<totalChoices ; j++) {
						String userEntry = questionResponseMap.getParam(responseKey + "_" + obfusticateID(j,theObfusticator));
						if(!("0").equals(userEntry) && !"".equals(userEntry)){
							answeredChoices ++;
						}
					}
					BigDecimal answeredChoicesCount = new BigDecimal(Integer.toString(answeredChoices) + ".00");
					answeredChoicesCount = answeredChoicesCount.multiply(new BigDecimal("100.00"));
					percent = answeredChoicesCount.divide(new BigDecimal(Integer.toString(totalChoices) + ".00"), 2, RoundingMode.HALF_UP);
				}
			}
		}
		return percent;
	}
	
	@Override
	protected FeedBackTO getQuestionFeedback(UserResponseWithPolicyTO userResponseWithPolicyTO, Map<Integer, String> userResponseMap, String mode) throws Exception {
		LicenseService licenseService = null;
		boolean answerSpecificFeedbackSupport = false;
		boolean allCorrect = true;
		boolean correctlyAnswered= false;
		String theFeedback= "";
		/**
		 * Check wheather it is applicable to show feedback or not
		 * this super call validates policies
		 */
		FeedBackTO feedBackTO = super.getQuestionFeedback(userResponseWithPolicyTO, userResponseMap, mode);
		if(feedBackTO == null){
			return feedBackTO;
		}
		/**
		 * check user has correct all answers or not
		 */
		if(userResponseMap != null && !userResponseMap.isEmpty()){
			for(int i=0; i < userResponseMap.size(); i++){
				String userCorrectness = userResponseMap.get(i);
				if(CaaConstants.TRUE.equals(userCorrectness)){
					correctlyAnswered = true;
				}else{
					correctlyAnswered = false;
				}
				allCorrect &= correctlyAnswered;
			}
		}

		licenseService = (LicenseService)BeanExtractionUtil.getAppSpringBean("licenseService");
		if(licenseService != null){
			answerSpecificFeedbackSupport = licenseService.hasFeature(licenseManager.FEATURE_SUPPORT_ASFEEDBACK);
		}
		if(answerSpecificFeedbackSupport){
			/**
			 * Get the common feed back value
			 */
			String commonFeedback= questionProperties.getString(COMMON_FEEDBACK, null);
			if(StringUtils.isBlank(commonFeedback)){
				if(feedback.size() > 0 && StringUtils.isNotBlank((String)feedback.elementAt(0))){
					commonFeedback= ((String)feedback.elementAt(0)).trim();
				}else{
					commonFeedback = "";
				}
			}
			/**
			 * TODO: 
			 * 
			 * 1. as per classic connect implementation if any asnwer specific feedback exist, then also
			 * USE_COMMON_FEEDBACK is coming as true. so as per this, answer specific feedback never shown 
			 * for Ranking question. Need confirmation
			 * 
			 * 2. if question is not answered should we show feedback ?
			 * 
			 */
			/**
			 * if use common feed back is coming as true then show common feed back
			 * else show answer specific feedback
			 */
			if(questionProperties.getBoolean(USE_COMMON_FEEDBACK, true)){
				theFeedback= commonFeedback;
			}else{
				if (allCorrect && (feedback.size() > 0) && StringUtils.isNotBlank((String)feedback.elementAt(0))){
					String fbCorrect = ((String)feedback.elementAt(0)).trim();
					theFeedback= tp_utils.safeJSON(fbCorrect);
				}
				if (!allCorrect && (feedback.size() > 1) && StringUtils.isNotBlank((String)feedback.elementAt(1))){
					String fbInCorrect = ((String)feedback.elementAt(0)).trim();
					theFeedback= tp_utils.safeJSON(fbInCorrect);
				}
				if (StringUtils.isBlank(theFeedback)){
					theFeedback= commonFeedback;
				}
			}
		}else if(feedback.size() > 0 && StringUtils.isNotBlank((String)feedback.elementAt(0))){
			theFeedback= (String)feedback.elementAt(0);
		}
		if(StringUtils.isNotBlank(theFeedback)){
			feedBackTO.setFeedBack(theFeedback.trim());
		}
		return feedBackTO;
	}
	
}