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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
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
* implementation of matching questions
*/
public class matching extends question
{
	private static final Logger _logger = Logger.getLogger(matching.class);
	public static String	TYPE_IDENTIFIER			= "MA";
	
	static String	EDIT_PTS = "EDITmaPTS",
					EDIT_CHOICE = "EDITmaCHOICE",
					EDIT_DETRACTOR = "EDITmaDET",
					EDIT_MATCH = "EDITmaMATCH",
					EDIT_FEED_MATCH = "EDITmaFEEDmatch",
					EDIT_FEED_UNMATCH = "EDITmaFEEDunmatch",
					EDIT_BRANCH_SCORE = "EDITmaBRANCHscore",
					EDIT_BRANCH_GT = "EDITmaBRANCHgt",
					EDIT_BRANCH_LE = "EDITmaBRANCHle",
					EDIT_DEL = "EDITmaDelete",
					EDIT_DELDET = "EDITmaDELDET",
					EDIT_SUPP = "EDITmaSupress";
					
	static String	SUPPRESS_CHOICES		= "suppressChoices";
	

	public VectorAdapter	matches;		//v4.1.2a5 added detractors

	VectorAdapter detractors;

	int		branchScore = 0,
			branchGreater = -1,
			branchLessEqual = -1;

	boolean	supressChoices = false;
				
	
	/**
	* construct a new matching question
	*/
	public matching() {
		super();
		
		type= QUESTION_TYPE_matching;
		
		matches= new VectorAdapter();
		detractors= new VectorAdapter();
	}
	
	
	/**
		* construct a new multiple choice question from a vector of data as constructed by the question
	 * import procedure.
	 */
	public matching( VectorAdapter qInfo )
	{
		super();
		
		type= QUESTION_TYPE_matching;
		
		maxPoints= 0;
		matches= new VectorAdapter();
		detractors= new VectorAdapter();
		correctAnswerFeedback= new VectorAdapter();
		
		if (qInfo.size() >= 2) qtext= (String)qInfo.elementAt(2);
		
		for (int i=3 ; i< qInfo.size() ; i++) 
		{
			String thisChoice= (String)qInfo.elementAt(i);
			int theIndex= thisChoice.indexOf("::");
			if ((theIndex > 0) && (!thisChoice.endsWith("::")))
			{
				choices.addElement( thisChoice.substring(0,theIndex) );
				matches.addElement( thisChoice.substring(theIndex+2) );
				points.addElement("1");
				correctAnswerFeedback.addElement( (String)choices.elementAt(choices.size()-1) + " should be matched to " + (String)matches.elementAt(matches.size()-1) );
				feedback.addElement("");
				maxPoints++;
			}
			else
				detractors.addElement(tp_utils.substitute(thisChoice, "::", ""));
		}		
	}
	
	
	/**
	* duplicate a matching question.
	*/
	public matching( matching theOriginal )
	{
		super( theOriginal );
		
		type= QUESTION_TYPE_matching;
		
		// dup the ints
		branchScore= theOriginal.branchScore;
		branchGreater= theOriginal.branchGreater;
		branchLessEqual= theOriginal.branchLessEqual;
		
		supressChoices= theOriginal.supressChoices;

		// dup matches
		matches= new VectorAdapter();
		for (int i=0 ; i<theOriginal.matches.size() ; i++)
			matches.addElement( (String)theOriginal.matches.elementAt(i) );
			
		detractors= new VectorAdapter();
		for (int i=0 ; i<theOriginal.detractors.size() ; i++)
			detractors.addElement( (String)theOriginal.detractors.elementAt(i) );
		// dup additional subclasses here
	}
	
	
	/**
	* read a matching question from an InputStream.
	* 
	* @param theInput
	* the stream from which to read the object
	* 
	* @param format
	* the format of the data in the stream
	*/
	public matching( DataInputStream theInput, int format )
		throws testFormatException
	{
		super( theInput, format );
		
		type= QUESTION_TYPE_matching;
		
		matches= new VectorAdapter();
		detractors= new VectorAdapter();

		try {
			
			// read the ints
			if (format >= 5) {
				branchScore= theInput.readInt();
				branchGreater= theInput.readInt();
				branchLessEqual= theInput.readInt();
			}
			
			if (format >= 45) {
				supressChoices= theInput.readBoolean();
			}
			
			// read matches
			int matchesCount= theInput.readInt();
			for (int i=0 ; i<matchesCount ; i++)
				matches.addElement( theInput.readUTF() );

			if (format >= 407) {
				int detCount= theInput.readInt();
				for (int i=0 ; i<detCount ; i++)
					detractors.addElement( theInput.readUTF() );
			}
			
			// read additional subclasses here
		
		
			if (format < 20) {
				for (int i=0 ; i<choices.size() ; i++)
					feedback.setElementAt( fmReturn + (String)feedback.elementAt(i), i );
			}
			
			if (format < 404) {
				// v4.1.1a3
				maxPoints= 0;
				correctAnswerFeedback= new VectorAdapter();
				for (int i=0 ; i<choices.size() ;) {
					String theChoice= (String)choices.elementAt(i);
					String theMatch= (String)matches.elementAt(i);
					
					if ((theChoice.length() == 0) || (theMatch.length() == 0)) {		// remove empty choices
						choices.removeElementAt(i);
						matches.removeElementAt(i);
						feedback.removeElementAt(i);
						points.removeElementAt(i);
					}
					else {																// calculate maxPoints
						try {
							String pstr= (String)points.elementAt(i);
							int pts= Integer.parseInt( pstr );
							maxPoints += Math.max( 0, pts );
							if (pts > 0)
								correctAnswerFeedback.addElement( pstr + " points awarded for <I>" + theChoice + "::" + theMatch + "</I><BR>" );
						} catch (NumberFormatException e) {}
						i++;
					}
				}
			}
		
		} catch (IOException e) {
			
			throw (new testFormatException( "IOException reading matching question" ) );
			
		}
		
		supressChoices= questionProperties.getBoolean( SUPPRESS_CHOICES, supressChoices );
		questionProperties.setBoolean( SUPPRESS_CHOICES, supressChoices );
	}
	
	
	public matching( Element xmlQ, int format )
		throws testFormatException
	{
		super();
		
		if (xmlQ.getChildren().size() <= 0) throw new testFormatException();

		type= QUESTION_TYPE_matching;
		
		points= new VectorAdapter();
		matches= new VectorAdapter();
		detractors= new VectorAdapter();
		correctAnswerFeedback= new VectorAdapter();
		
		qtext= xmlQ.getChildText( test.XML_QUESTION_STEM );
		referenceTag= xmlQ.getChildText( test.XML_QUESTION_REFERENCE );
		if (referenceTag == null) referenceTag= "";
		pageTag= xmlQ.getChildText( test.XML_QUESTION_PAGE_REFERENCE );
		if (pageTag == null) pageTag= "";
		
		String theFeedback= xmlQ.getChildText( test.XML_QUESTION_FEEDBACK );
		if (theFeedback == null) theFeedback= "";
		
		java.util.List theList= xmlQ.getChildren( test.XML_QUESTION_MATCH );
		ListIterator iter= theList.listIterator();
		while (iter.hasNext())
		{
			maxPoints++;
			
			Element thisChoice= (Element)iter.next();
			String theText= thisChoice.getText();
			int theIndex= theText.indexOf("::");
			if (theIndex > 0) {
				choices.addElement( theText.substring(0, theIndex) );
				theIndex += 2;
				if (theIndex < theText.length())
					matches.addElement( theText.substring(theIndex) );
				else
					matches.addElement( "unmatched" );
			}
			else {
				choices.addElement( theText );
				matches.addElement( "unmatched" );
			}
			
			points.addElement("1");
			correctAnswerFeedback.addElement( (String)choices.elementAt(choices.size()-1) + " should be matched to " + (String)matches.elementAt(matches.size()-1) );
			feedback.addElement(theFeedback + fmReturn + theFeedback);
		}

		theList= xmlQ.getChildren( test.XML_QUESTION_DISTRACTOR );
		iter= theList.listIterator();
		while (iter.hasNext())
		{
			Element thisChoice= (Element)iter.next();
			detractors.addElement( thisChoice.getText() );
		}

	}
	

	public matching( tp_requestHandler theHandler, Element xmlQ, ConcurrentHashMap mediaConsumers )
		throws testFormatException
	{
		super();
		
		if (xmlQ.getChildren().size() <= 0) throw new testFormatException();

		type= QUESTION_TYPE_matching;
		bb6ImportTag= theHandler.theServlet.uniqueID();

		matches= new VectorAdapter();
		detractors= new VectorAdapter();
		correctAnswerFeedback= new VectorAdapter();
		
		ConcurrentHashMap theChoices= new ConcurrentHashMap();
		ConcurrentHashMap theMatches= new ConcurrentHashMap();
		
		boolean hasBody= false;
		
		java.util.List parts= xmlQ.getChildren();
		ListIterator iter= parts.listIterator();
		while (iter.hasNext()) 
		{
			Element thisChild= (Element)iter.next();
			String thisName= thisChild.getName();
			
			if (thisName.equals("BODY"))
			{
				qtext= tp_utils.fixBB6(thisChild.getChildText("TEXT"), mediaConsumers, bb6ImportTag);
				hasBody= true;
			}
			
			else if (thisName.equals("ANSWER"))
				theChoices.put(thisChild.getAttributeValue("id"), tp_utils.fixBB6(thisChild.getChildText("TEXT"), mediaConsumers, bb6ImportTag) );
			
			else if (thisName.equals("CHOICE"))
				theMatches.put(thisChild.getAttributeValue("id"), tp_utils.fixBB6(thisChild.getChildText("TEXT"), mediaConsumers, bb6ImportTag) );
			
			else if (thisName.equals("GRADABLE"))
			{
				ConcurrentHashMap dupChoices= new ConcurrentHashMap();
				ConcurrentHashMap dupMatches= new ConcurrentHashMap();
		
				java.util.List m2= thisChild.getChildren("CORRECTANSWER");
				ListIterator iter2= m2.listIterator();
				while (iter2.hasNext()) 
				{
					Element thisAnswer= (Element)iter2.next();
					
					String thisChoice= (String)theChoices.get(thisAnswer.getAttributeValue("answer_id"));
					if (dupChoices.get(thisChoice) != null)
					{
						_logger.info("Matching question with duplicate choices in Blackboard input - skipped");
						throw new testFormatException();
					}
					else dupChoices.put(thisChoice, thisChoice);
					
					String thisMatch= (String)theMatches.get(thisAnswer.getAttributeValue("choice_id"));
					if (dupMatches.get(thisMatch) != null)
					{
						_logger.info("Matching question with duplicate matches in Blackboard input - skipped");
						throw new testFormatException();
					}
					else dupMatches.put(thisMatch, thisMatch);
					
					if ((thisChoice != null) && (thisMatch != null))
					{
						choices.addElement(thisChoice);
						matches.addElement(thisMatch);
						points.addElement("1");
						correctAnswerFeedback.addElement( thisChoice + " should be matched to " + thisMatch );
						feedback.addElement("" + fmReturn + "");
					}
				}
			}
		}
		
		if (!hasBody) throw new testFormatException();
		
		//System.out.println("TEXT: " + qtext);
		
		maxPoints= choices.size();
	}


	public void write( DataOutputStream out ) 
		throws testFormatException
	{
		
		try {
			super.write( out );
				
			// write the ints
			out.writeInt( branchScore );
			out.writeInt( branchGreater );
			out.writeInt( branchLessEqual );
			
			out.writeBoolean( supressChoices );
			
			// write matches
			out.writeInt( matches.size() );
			for (int i=0 ; i<matches.size() ; i++)
				out.writeUTF( (String)matches.elementAt(i) );
				
			out.writeInt( detractors.size() );
			for (int i=0 ; i<detractors.size() ; i++)
				out.writeUTF( (String)detractors.elementAt(i) );
			
			// write additional subclasses here
		
		
		} catch (IOException e) {
			throw ( new testFormatException( "IOException writing matching question" ) );
		}
		
	}

	
	public String typeString() {
		return( "matching" );
	}
		
	
	public String formalTypeString() {
		return( "Matching&nbsp;Question" );
	}
	public String formalTypeString2( tp_requestHandler theHandler ) {
		return( "Matching" );
	}
		
	public String formalTypeString2() {
		return( "Matching" );
	}

	
	public String typeStringSmall() {
		return( "match" );
	}
		
		
	public void setDefaultAnswer( String theAnswer ) {}


	public void setMatches( String theParams ) throws testFormatException {
		
		StringTokenizer theTokens= new StringTokenizer( theParams, fmReturn );
		if (theTokens.countTokens() != MAX_CHOICES)
			throw (new testFormatException( "improper question matches for id#" + Integer.toString(id) ));
		
		while ( theTokens.hasMoreTokens() ) {
			String theMatch= theTokens.nextToken();
			if ( theMatch.equals( "NO ENTRY!!!" ))
				matches.addElement( "" );
			else
				matches.addElement( theMatch );
		}

	}
	
	
	public void setBranching( String theBranches ) throws testFormatException
	{
		/*		34. exportBranching
					for check all & matching
						score, fmReturn, id for <= score, fmReturn, id for > score
		*/
	
		StringTokenizer theTokens= new StringTokenizer( theBranches, test.fmReturn );
		if (theTokens.countTokens() != 3)
			throw (new testFormatException( "incorrect number of branching id's in checkAll question" ) );
	
		try {
		
			branchScore= Integer.parseInt( theTokens.nextToken() );
			branchLessEqual= Integer.parseInt( theTokens.nextToken() );
			branchGreater= Integer.parseInt( theTokens.nextToken() );
			
		} catch (NumberFormatException e) {
			throw (new testFormatException( "non-numeric branch id in checkAll question" ) );
		}
	}
	
	
	public void addEmptyVars( parameters theParams ) {
		for (int i=0 ; i<choices.size() ; i++)
			theParams.replaceParam( "Q_" + sqlID + "_" + Integer.toString(i), "-2-2");
	}


	/*
	public String buildHTML( test theTest, parameters theData ) {
					
		String theQID= "Q_" + sqlID;
		String resultHTML= "";

		if (choices.size() == 0) return("");
		
		int choiceCount= 0;
		VectorAdapter scrambledChoices= new VectorAdapter();
		VectorAdapter scrambledIDs= new VectorAdapter();
		
		boolean[] chosen= new boolean[ choices.size() ];
		for (int i=0 ; i<choices.size() ; i++)
			chosen[i]= false;
		boolean complete= false;
		

		if (supressChoices) {
			for (int i=0 ; i<choices.size() ; i++) {
							
				String theChoice= (String)choices.elementAt(i);				
				String theMatch= (String)matches.elementAt(i);
				
				// calculate the choice selection value
				double top= Math.rint( Math.random() * 100 ) * 10000;
				double bottom= Math.rint( Math.random() * 100);
				int theID= (int)(top + ((54-i)*100) + bottom);

				// add it to the vector
				scrambledChoices.addElement( theChoice );
				scrambledIDs.addElement( Integer.toString( theID ) );
				
			}
		}
		else {
			while ( ! complete ) {
			
				// select a choice at random
				double theIndex= Math.floor( Math.random() * choices.size() );
				int i= (int)theIndex;
				
				// if we have not already included the choice
				if (!chosen[i]) {
				
					chosen[i]= true;
				
					String theChoice= (String)choices.elementAt(i);
					String theMatch= (String)matches.elementAt(i);
					
					// calculate the choice selection value
					double top= Math.rint( Math.random() * 100 ) * 10000;
					double bottom= Math.rint( Math.random() * 100);
					int theID= (int)(top + ((54-i)*100) + bottom);

					// add it to the vector
					scrambledChoices.addElement( theChoice );
					scrambledIDs.addElement( Integer.toString( theID ) );
					
					// see if we've got them all
					complete= true;
					for (int j=0 ; j<choices.size() ; j++)
						complete = complete && chosen[j];
						
				}
				
			}
		}
		
		
		ConcurrentHashMap userChoices= new ConcurrentHashMap();
		if (theData != null) {
		
			for (int i=0 ; i<choices.size() ; i++) {
			
				String userEntry= theData.getParam( theQID + "_" + Integer.toString(i) );
				theData.removeParam( theQID + "_" + Integer.toString(i) );

				if (userEntry.length() > 0) {		// the question was offered
				
					int userChoice = -1;
					try {
						userChoice= Integer.parseInt( userEntry.trim() );
					} catch (NumberFormatException e) {}
					
					if (userChoice >= 0) {		// we answered the question
						
						userChoice %= 10000;
						userChoice /= 100;
						userChoice = 54 - userChoice;
						
						if ((userChoice >= 0) && (userChoice < choices.size()))
							userChoices.put( (String)matches.elementAt(i), (String)choices.elementAt(userChoice) );
					}
					
				}
				
			}
			
		}
		
		
		resultHTML += "<TABLE WIDTH=50%>";
		for (int i=0 ; i<choices.size() ; i++) {
			
			String theMatch= (String)matches.elementAt(i);

			if (supressChoices)
				resultHTML += "<TR><TD>&nbsp;</TD>";
			else {
				String thisChoice= (String)scrambledChoices.elementAt(i);
				try {		// see if we have media in the choice
					richMedia theMedia= new richMedia( thisChoice );
					theMedia.setChoice(true);
					thisChoice= theMedia.html("");
				} catch (testFormatException e) {}
				
				resultHTML += "<TR><TD VALIGN=BOTTOM>" + theTest.theGUI.responseFontStart() + Integer.toString( i + 1 ) + ". " + thisChoice + theTest.theGUI.responseFontEnd() + "</TD>";
			}
			
			resultHTML += "<TD VALIGN=BOTTOM ALIGN=RIGHT>" + theTest.theGUI.responseFontStart() + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + theMatch + theTest.theGUI.responseFontEnd() + "</TD>";
			
			resultHTML += "<TD VALIGN=BOTTOM>" + theTest.theGUI.responseFontStart() + "<SELECT NAME=\"" + theQID + "_" + Integer.toString(i) + "\">";

			if (theData == null) {
			
				resultHTML += "  <OPTION SELECTED VALUE=\"-2-2\">&nbsp;";
				
				for (int j=0 ; j<choices.size() ; j++)
					resultHTML += "  <OPTION VALUE=\"" + (String)scrambledIDs.elementAt(j) + "\">" + Integer.toString( j+1 );
				
			}
			else {
			
				String oldMatch= (String)userChoices.get( theMatch );
				
				if (oldMatch == null) {
					resultHTML += "  <OPTION SELECTED VALUE=\"-2-2\">&nbsp;";
					
					for (int j=0 ; j<choices.size() ; j++)
						resultHTML += "  <OPTION VALUE=\"" + (String)scrambledIDs.elementAt(j) + "\">" + Integer.toString( j+1 );
				}
				else {
					resultHTML += "  <OPTION VALUE=\"-2-2\">&nbsp;";
					
					for (int j=0 ; j<choices.size() ; j++) {
						if (oldMatch.equals((String)scrambledChoices.elementAt(j)))
							resultHTML += "  <OPTION SELECTED VALUE=\"" + (String)scrambledIDs.elementAt(j) + "\">" + Integer.toString( j+1 );
						else
							resultHTML += "  <OPTION VALUE=\"" + (String)scrambledIDs.elementAt(j) + "\">" + Integer.toString( j+1 );
							
					}
					
				}
			
			}

			resultHTML += "</SELECT></FONT></TD></TR>";
			
		}
		resultHTML += "</TABLE>";
		
		return(resultHTML);
						
	} */
	
	
	public String buildHTML( test theTest, parameters theData ) {
					
		if (choices.size() == 0) return("");

		String theQID= "Q_" + sqlID;
		String resultHTML= "";
		
		VectorAdapter scrambledChoices= new VectorAdapter();
		VectorAdapter scrambledIDs= new VectorAdapter();
		
		int totalChoices= choices.size() + detractors.size();
		boolean[] chosen= new boolean[ totalChoices ];
		for (int i=0 ; i<totalChoices ; i++)
			chosen[i]= false;
		boolean complete= false;

		while ( ! complete ) {
			// select a choice at random
			double theIndex= Math.floor( Math.random() * totalChoices );
			int i= (int)theIndex;
			
			if (!chosen[i]) {	// if we have not already included the choice
				chosen[i]= true;
			
				String theChoice= "unknown";
				if (i < choices.size())
					theChoice= (String)choices.elementAt(i);
				else
					theChoice= (String)detractors.elementAt(i-choices.size());
				
				theChoice= randomVariable.deReference(theChoice, theTest, sqlID, theData);
				theChoice= richMedia.deReference(theChoice, "");

				// calculate the choice selection value
				double top= Math.rint( Math.random() * 100 ) * 10000;
				double bottom= Math.rint( Math.random() * 100);
				int theID= (int)(top + ((54-i)*100) + bottom);

				// add it to the vector
				scrambledChoices.addElement( theChoice );
				scrambledIDs.addElement( Integer.toString( theID ) );
				
				// see if we've got them all
				complete= true;
				for (int j=0 ; j<totalChoices ; j++)
					complete = complete && chosen[j];
			}
		}
		
		
		ConcurrentHashMap userChoices= new ConcurrentHashMap();
		if (theData != null) {		// if the participant has responded
			for (int i=0 ; i<matches.size() ; i++) {
				String userEntry= theData.getParam( theQID + "_" + Integer.toString(i) );		// get each response
				theData.removeParam( theQID + "_" + Integer.toString(i) );

				if (userEntry.length() > 0) {		// the question was offered
					int userChoice = -1;
					try {
						userChoice= Integer.parseInt( userEntry.trim() );
					} catch (NumberFormatException e) {}
					
					if (userChoice >= 0) {		// we answered the question
						userChoice %= 10000;
						userChoice /= 100;
						userChoice = 54 - userChoice;
						
						if ((userChoice >= 0) && (userChoice < totalChoices)) {
							if (userChoice < choices.size())
								userChoices.put( (String)matches.elementAt(i), (String)choices.elementAt(userChoice) );
							else
								userChoices.put( (String)matches.elementAt(i), (String)detractors.elementAt(userChoice-choices.size()) );
						}
					}
				}
			}
		}
		
		
		resultHTML += "<TABLE WIDTH=50%>";
		for (int i=0 ; i<matches.size() ; i++) {
			
			String theMatch= (String)matches.elementAt(i);
			theMatch= randomVariable.deReference(theMatch, theTest, sqlID, theData);
			theMatch= richMedia.deReference(theMatch, "");

			String thisChoice= (String)scrambledChoices.elementAt(i);
			try {		// see if we have media in the choice
				richMedia theMedia= new richMedia( thisChoice );
				theMedia.setChoice(true);
				thisChoice= theMedia.html("");
			} catch (testFormatException e) {}

			resultHTML += "<TR><TD VALIGN=BOTTOM>" + theTest.theGUI.responseFontStart() + Integer.toString( i + 1 ) + ". " + thisChoice + theTest.theGUI.responseFontEnd() + "</TD>";
			
			resultHTML += "<TD VALIGN=BOTTOM ALIGN=RIGHT>" + theTest.theGUI.responseFontStart() + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + theMatch + theTest.theGUI.responseFontEnd() + "</TD>";
			
			resultHTML += "<TD VALIGN=BOTTOM>" + theTest.theGUI.responseFontStart() + "<SELECT NAME=\"" + theQID + "_" + Integer.toString(i) + "\">";

			if (theData == null) {
			
				resultHTML += "  <OPTION SELECTED VALUE=\"-2-2\">&nbsp;";
				
				for (int j=0 ; j<scrambledIDs.size() ; j++)
					resultHTML += "  <OPTION VALUE=\"" + (String)scrambledIDs.elementAt(j) + "\">" + Integer.toString( j+1 );
				
			}
			else {
			
				String oldMatch= (String)userChoices.get( theMatch );
				
				if (oldMatch == null) {
					resultHTML += "  <OPTION SELECTED VALUE=\"-2-2\">&nbsp;";
					
					for (int j=0 ; j<scrambledIDs.size() ; j++)
						resultHTML += "  <OPTION VALUE=\"" + (String)scrambledIDs.elementAt(j) + "\">" + Integer.toString( j+1 );
				}
				else {
					resultHTML += "  <OPTION VALUE=\"-2-2\">&nbsp;";
					
					for (int j=0 ; j<scrambledIDs.size() ; j++) {
						if (oldMatch.equals((String)scrambledChoices.elementAt(j)))
							resultHTML += "  <OPTION SELECTED VALUE=\"" + (String)scrambledIDs.elementAt(j) + "\">" + Integer.toString( j+1 );
						else
							resultHTML += "  <OPTION VALUE=\"" + (String)scrambledIDs.elementAt(j) + "\">" + Integer.toString( j+1 );
					}
					
				}
			
			}

			resultHTML += "</SELECT></FONT></TD></TR>";
			
		}
		for (int i=matches.size() ; i<scrambledChoices.size() ; i++) {
			
			String thisChoice= (String)scrambledChoices.elementAt(i);
			try {		// see if we have media in the choice
				richMedia theMedia= new richMedia( thisChoice );
				theMedia.setChoice(true);
				thisChoice= theMedia.html("");
			} catch (testFormatException e) {}
			
			resultHTML += "<TR><TD VALIGN=BOTTOM>" + theTest.theGUI.responseFontStart() + Integer.toString( i + 1 ) + ". " + thisChoice + theTest.theGUI.responseFontEnd() + "</TD>";
			resultHTML += "<TD VALIGN=BOTTOM>&nbsp;</TD><TD VALIGN=BOTTOM>&nbsp;</TD></TR>";
			
		}
		resultHTML += "</TABLE>";
		
		return(resultHTML);
						
	} // end buildHTML


	public int returnBranch( parameters theParams, test theTest ) {
	
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
		
		if (thisResponse != null) {
			if (thisResponse.points <= branchScore) 
				return( branchLessEqual );
			else
				return( branchGreater);
		}
	
		return( -1 );
		
	}


	public void evaluate( submission theSubmission, test theTest ) {
		
		String questionID= "Q_" + sqlID;

		response thisResponse= new response( sqlID, maxPoints, finalHTML( theTest, new parameters(theSubmission.formVariables) ) );

		for (int j=0 ; j<matches.size() ; j++) {
			
			String theChoice= (String)choices.elementAt(j);
			String theMatch= (String)matches.elementAt(j);
				
			int thePoints= 0;
			
			if (maxPoints > 0) {
				try {
					thePoints= Integer.parseInt( (String)points.elementAt(j) );
				} catch (NumberFormatException e) {}
			}
			
			int userChoice = 0;
			
			String userEntry= theSubmission.getParam( questionID + "_" + Integer.toString(j) );
			
			if (userEntry.length() > 0) {		// the question was offered
			
				try {
					userChoice= Integer.parseInt( userEntry.trim() );
				} catch (NumberFormatException e) {
					userChoice= -1;
				}
				
				String theFeedback= (String)feedback.elementAt(j);
				String feedbackIfMatched= "";
				String feedbackIfUnmatched= "";
				if (theFeedback.length() > 1) {
					int separatorIndex= theFeedback.indexOf( fmReturn );
					if (separatorIndex >= 0) {
						if (separatorIndex == 0)	// no checked feedback
							feedbackIfUnmatched= theFeedback.substring(1);
						else {
							feedbackIfMatched += theFeedback.substring(0,separatorIndex);
							if (++separatorIndex < theFeedback.length())
								feedbackIfUnmatched += theFeedback.substring(separatorIndex);
						}
					}
					else	// no separator, default to issue feedback on unchecked
						feedbackIfUnmatched= theFeedback;
				}

				if (userChoice >= 0) {		// we answered the question
					
					int count= 0;
						
					userChoice %= 10000;
					userChoice /= 100;
					userChoice = 54 - userChoice;
					
					// v4.1.3p42  fix screwy numbering
					if (userChoice < 0) userChoice= 0;
					
					String displayedMatch= "unknown";
					if (userChoice < choices.size())
						displayedMatch= theMatch + " :: " + (String)choices.elementAt(userChoice);
					else
						displayedMatch= theMatch + " :: " + (String)detractors.elementAt(userChoice-choices.size());
					
					thisResponse.recordValue( displayedMatch );
					
					if (maxPoints > 0) {
						
						if (userChoice == j) {
							thisResponse.incrementScore( thePoints );
							thisResponse.setFeedback( tp_utils.substitute( feedbackIfMatched, "%%", displayedMatch ).trim() );
						}
						else
							thisResponse.setFeedback( tp_utils.substitute( feedbackIfUnmatched, "%%", displayedMatch ).trim() );

					}
						
					thisResponse.setUserResponse( displayedMatch );
					
				}
				
				else if (maxPoints > 0) {	// unchecked but scored
					
					thisResponse.recordValue( "n/r" );
				
					thisResponse.setFeedback( tp_utils.substitute( feedbackIfUnmatched, "%%", " " ).trim() );

					thisResponse.setUserResponse( "<I>no match for: </I>" + theChoice);
					
				}
				
				else
					thisResponse.recordValue( "n/r" );
				
			}
			
			else {		// question not offered
			
				/* RECORD NOTHING */
				return;
				
			}
			
		}
		
		theSubmission.addResponse( thisResponse );
				
	}
	
	
	/*
	public void evaluate( submission theSubmission, test theTest ) {
		
		String questionID= "Q_" + sqlID;

		response thisResponse= new response( sqlID, maxPoints, finalHTML( theTest, new parameters(theSubmission.formVariables) ) );

		for (int j=0 ; j<choices.size() ; j++) {
			
			String theChoice= (String)choices.elementAt(j);
			String theMatch= (String)matches.elementAt(j);
				
			int thePoints= 0;
			
			if (maxPoints > 0) {
				try {
					thePoints= Integer.parseInt( (String)points.elementAt(j) );
				} catch (NumberFormatException e) {}
			}
			
			int userChoice = 0;
			
			String userEntry= theSubmission.getParam( questionID + "_" + Integer.toString(j) );
			
			if (userEntry.length() > 0) {		// the question was offered
			
				try {
					userChoice= Integer.parseInt( userEntry.trim() );
				} catch (NumberFormatException e) {
					userChoice= -1;
				}
				
				String theFeedback= (String)feedback.elementAt(j);
				String feedbackIfMatched= "";
				String feedbackIfUnmatched= "";
				if (theFeedback.length() > 1) {
					int separatorIndex= theFeedback.indexOf( fmReturn );
					if (separatorIndex >= 0) {
						if (separatorIndex == 0)	// no checked feedback
							feedbackIfUnmatched= theFeedback.substring(1);
						else {
							feedbackIfMatched += theFeedback.substring(0,separatorIndex);
							if (++separatorIndex < theFeedback.length())
								feedbackIfUnmatched += theFeedback.substring(separatorIndex);
						}
					}
					else	// no separator, default to issue feedback on unchecked
						feedbackIfUnmatched= theFeedback;
				}

				if (userChoice >= 0) {		// we answered the question
					
					int count= 0;
						
					userChoice %= 10000;
					userChoice /= 100;
					userChoice = 54 - userChoice;
					
					String displayedMatch= theMatch + " :: " + (String)choices.elementAt(userChoice);
					thisResponse.recordValue( displayedMatch );
					
					if (maxPoints > 0) {
						
						if (userChoice == j) {
							thisResponse.incrementScore( thePoints );
							thisResponse.setFeedback( tp_utils.substitute( feedbackIfMatched, "%%", displayedMatch ).trim() );
						}
						else
							thisResponse.setFeedback( tp_utils.substitute( feedbackIfUnmatched, "%%", displayedMatch ).trim() );

					}
						
					thisResponse.setUserResponse( theMatch + " :: " + (String)choices.elementAt(userChoice));
					
				}
				
				else if (maxPoints > 0) {	// unchecked but scored
					
					thisResponse.recordValue( "n/r" );
				
					thisResponse.setFeedback( tp_utils.substitute( feedbackIfUnmatched, "%%", " " ).trim() );

					thisResponse.setUserResponse( "<I>no match for: </I>" + theChoice);
					
				}
				
				else
					thisResponse.recordValue( "n/r" );
				
			}
			
			else {		// question not offered
			
				return;
				
			}
			
		}
		
		theSubmission.addResponse( thisResponse );
				
	}

	*/
	
	public String responseTableHeader() {
	
		String result= "";
		
		for (int i=0 ; i<choices.size() ; i++)
			result+= "<TH><FONT FACE=\"Arial,Helvetica\" SIZE=1 COLOR=WHITE>Q" + Integer.toString(id) + "_" + Integer.toString(i+1) + "</FONT></TH>";
			
		return( result );
		
	}
	
	public String emptyResponseTableEntry() {

		String result= "";
		
		for (int i=0 ; i<choices.size() ; i++)
			result+= "<TD>-</TD>";
			
		return( result );
		
	}

	public String responseSpreadsheetHeader() {
	
		String result= "";
		
		for (int i=0 ; i<choices.size() ; i++)
			result+= "Q" + Integer.toString(id) + "_" + Integer.toString(i+1) + "\t";
			
		return( result );
		
	}
	
	public String emptyResponseSpreadsheetEntry() {

		String result= "";
		
		for (int i=0 ; i<choices.size() ; i++)
			result+= "\t";
			
		return( result );
		
	}

	public void dumpIt( ServletOutputStream output, test theTest ) {
		// dump the contents as HTML
		
		try {
			
			super.dumpIt( output, theTest );
			
			output.println( "<TABLE BORDER>" );
			
			output.println( "<TR><TH>Match</TH><TH>Feedback</TH><TH>Points</TH></TR>" );
			for (int i=0 ; i<choices.size() ; i++) {
				String theEntry= (String)choices.elementAt(i);
				String theMatch= (String)matches.elementAt(i);
				String thePoints= (String)points.elementAt(i);
				String theFeedback= (String)feedback.elementAt(i);
				
				String matchMade= tp_utils.substitute(theEntry, "<", "&lt;") + " :: " + tp_utils.substitute(theMatch, "<", "&lt;");	
				output.println( "<TR><TD>" + matchMade + "</TD><TD>" + theFeedback + "</TD><TD>" + thePoints + "</TD></TR>" );
			}
			
			output.println( "</TABLE><P>");
			
			output.println( "<B>If score is less than or equal to </B>" + Integer.toString(branchScore) + "<B> then branch to question id# </B>" + Integer.toString(branchLessEqual) + ".<P>" );
			output.println( "<B>If score is greater than </B>" + Integer.toString(branchScore) + "<B> then branch to question id# </B>" + Integer.toString(branchGreater) + ".<P>" );
			
		} catch (IOException e) {
			_logger.error( "IOException in matching.dumpIt()" );
		}
		
	}
	
	
	public void summarizeResponse( summaryItem theSummaryItem, response theResponse ) {
	
		theSummaryItem.setCount( choices.size() );
		
		int answerCount= 0;
		
		for (int i=0 ; i<choices.size() ; i++) {
			String theChoice= (String)choices.elementAt(i);
			String theMatch= (String)matches.elementAt(i);
			
			String userMatch= "none";
			if (i < theResponse.recordedValue.size())
				userMatch= (String)theResponse.recordedValue.elementAt(i);
				
			String correctMatch= theMatch + " :: " + theChoice;
			
//System.out.println( "userMatch: " + userMatch );
//System.out.println( "correctMatch: " + correctMatch );
//System.out.println( "i: " + Integer.toString( i ) );
			
			int choiceCount= 0;
			try {
				String theValue= (String)theSummaryItem.frequencyCounts.get(Integer.toString(i));
				if (theValue != null) choiceCount= Integer.parseInt( theValue );
			} catch (NumberFormatException n) {};

			if (userMatch.equals(correctMatch))
				//theSummaryItem.frequencyCounts[i]++;
				theSummaryItem.frequencyCounts.put( Integer.toString(i), Integer.toString(++choiceCount) );
			
			if (! userMatch.equals( "n/r" )) answerCount++;
		}
		
		if (answerCount == 0) theSummaryItem.unansweredCount++;
		
	}
	
	
	//public void updateCounts( response theResponse, ConcurrentHashMap frequencyCounts, boolean initialize )
	public void updateCounts( response theResponse, ConcurrentHashMap frequencyCounts )
	{
		int count= choices.size();
		frequencyCounts.remove( "itemCount" );
		frequencyCounts.put( "itemCount", Integer.toString(count) );
		
		try { count= Integer.parseInt((String)frequencyCounts.get("offeredCount")) + 1; } catch (NumberFormatException n) { count= 1; };
		frequencyCounts.remove( "offeredCount" );
		frequencyCounts.put( "offeredCount", Integer.toString(count) );
		
		int answerCount= 0;
		
		for (int i=0 ; i<choices.size() ; i++) {
		
			String theChoice= (String)choices.elementAt(i);
			String theMatch= (String)matches.elementAt(i);
			
			String userMatch= "none";
			if (i < theResponse.recordedValue.size())
				userMatch= (String)theResponse.recordedValue.elementAt(i);
				
			String correctMatch= theMatch + " :: " + theChoice;
			
			if (userMatch.equals(correctMatch)) {
				if (frequencyCounts.get(Integer.toString(i)) == null)
					count= 1;
				else {
					try { count= Integer.parseInt((String)frequencyCounts.get(Integer.toString(i))) + 1; } catch (NumberFormatException n) { count= 1; };
				}
				frequencyCounts.remove( Integer.toString(i) );
				frequencyCounts.put( Integer.toString(i), Integer.toString(count) );
			}
			
			if (! userMatch.equals( "n/r" )) answerCount++;

		}
		
		if (answerCount == 0) {
			if (frequencyCounts.get("unansweredCount") == null)
				count= 1;
			else {
				try { count= Integer.parseInt((String)frequencyCounts.get("unansweredCount")) + 1; } catch (NumberFormatException n) { count= 1; };
			}
			frequencyCounts.remove( "unansweredCount" );
			frequencyCounts.put( "unansweredCount", Integer.toString(count) );
		}
		
	}
	

	public void frequencyAnalysis( ServletOutputStream output, test theTest, ConcurrentHashMap theCounts, int offeredCount ) {
	
		try {
		
			output.println( "<TABLE BORDER>" );
			output.println( "    <TR><TH>Frequency</TH><TH>Choice</TH><TH>Percent</TH></TR>" );
			
			for (int i= 0 ; i<choices.size() ; i++) {
				String theChoice= (String)choices.elementAt(i);
				String theMatch= (String)matches.elementAt(i);

				int choiceCount= 0;
				try {
					String theValue= (String)theCounts.get( Integer.toString(i) );
					if (theValue != null) choiceCount= Integer.parseInt( theValue );
				} catch (NumberFormatException n) {};

				float pct= choiceCount * 100;
				if (offeredCount > 0) pct /= offeredCount;
				else pct= 0;
				int percent= Math.round(pct);
				
				output.println( "    <TR><TD VALIGN=TOP ALIGN=CENTER>" + Integer.toString( choiceCount ) + "</TD><TD VALIGN=TOP>" + theChoice + "::" + theMatch + "</TD><TD><TABLE BORDER=0 CELLPADDING=0 CELLSPACING=0><TR><TD BGCOLOR=#005000 WIDTH=" + Integer.toString(percent) + ">&nbsp;</TD><TD>&nbsp;" + Integer.toString(percent) + "%</TD></TR></TABLE></TD></TR>" );
			}

			output.println( "</TABLE>" );
			
		} catch (IOException e) {
			_logger.error( "IOException in matching.frequencyAnalysis()" );
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
		
		theHandler.snd("<INPUT TYPE=HIDDEN NAME=\"" + EDIT_SUPP + "\" VALUE=\"");
		if (supressChoices) theHandler.snd("1");
		theHandler.snd("\">");
		
		// feedback, points, branching
		theHandler.snd("<TR><TD COLSPAN=2><BR><B><A onMouseOver=\"(window.status='get help...'); return true;\" HREF=\"javascript:helpMessage('responsehandling4');\">Response Handling</A></B></FONT><BR>");
		
		theHandler.snd("<TABLE CELLSPACING=0 CELLPADDING=2><TR><TH>Points</FONT></TH><TH>Choice</FONT></TH><TH>Match</FONT></TH><TH>Feedback</FONT></TH></TR>");

		for (int i=0 ; i<choices.size() ; i++) {
			String color= "tr2";
			if ((i%2)==0) color= "tr1";
			theHandler.snd("<TR class=\"" + color + "\"><TD><INPUT style=\"text-align: center\" TYPE=TEXT SIZE=3 MAXSIZE=3 NAME=\"" + EDIT_PTS + Integer.toString(i) + "\" VALUE=\"" + (String)points.elementAt(i) + "\">");
			theHandler.snd("<P>Delete<BR>&nbsp;&nbsp;<INPUT TYPE=CHECKBOX NAME=\"" + EDIT_DEL + Integer.toString(i) + "\" VALUE=\"delete\">");
			theHandler.snd("</TD>");
			theHandler.snd("<TD><TEXTAREA WRAP=\"VIRTUAL\" ROWS=5 COLS=20 NAME=\"" + EDIT_CHOICE + Integer.toString(i) + "\">" + (String)choices.elementAt(i) + "</TEXTAREA></TD>");
			theHandler.snd("<TD><TEXTAREA WRAP=\"VIRTUAL\" ROWS=5 COLS=20 NAME=\"" + EDIT_MATCH + Integer.toString(i) + "\">" + (String)matches.elementAt(i) + "</TEXTAREA></TD>");

			String theFeedback= (String)feedback.elementAt(i);
			String feedbackIfMatched= "";
			String feedbackIfUnmatched= "";
			if (theFeedback.length() > 1) {
				int separatorIndex= theFeedback.indexOf( fmReturn );
				if (separatorIndex >= 0) {
					if (separatorIndex == 0)	// no checked feedback
						feedbackIfUnmatched= theFeedback.substring(1);
					else {
						feedbackIfMatched += theFeedback.substring(0,separatorIndex);
						if (++separatorIndex < theFeedback.length())
							feedbackIfUnmatched += theFeedback.substring(separatorIndex);
					}
				}
				else	// no separator, default to issue feedback on unchecked
					feedbackIfUnmatched= theFeedback;
			}
			theHandler.snd("<TD class=\"tdright\">");
			theHandler.snd("match</FONT><TEXTAREA WRAP=\"VIRTUAL\" ROWS=3 COLS=20 NAME=\"" + EDIT_FEED_MATCH + Integer.toString(i) + "\">" + feedbackIfMatched + "</TEXTAREA><BR>");
			theHandler.snd("not</FONT><TEXTAREA WRAP=\"VIRTUAL\" ROWS=3 COLS=20 NAME=\"" + EDIT_FEED_UNMATCH + Integer.toString(i) + "\">" + feedbackIfUnmatched + "</TEXTAREA>");
			theHandler.snd("</TD>");

			theHandler.snd("</TR>");
		}
		
		int be= 0;
		if (theTest.getGUI().branchingEnabled) {
			be=1;
			String color= "tr2";
			if ((choices.size()%2)==0) color= "tr1";
			theHandler.snd("<TR class=\"" + color + "\"><TD COLSPAN=3><B>if score is GREATER THAN");
			theHandler.snd("<INPUT style=\"text-align: center\" TYPE=TEXT SIZE=3 MAXSIZE=4 NAME=\"" + EDIT_BRANCH_SCORE + "\" VALUE=\"" + Integer.toString(branchScore) + "\"> branch to ");
			theHandler.snd(tp_utils.questionPopup( EDIT_BRANCH_GT, theTest, branchGreater ));
			theHandler.snd("<BR>otherwise, branch to " + tp_utils.questionPopup( EDIT_BRANCH_LE, theTest, branchLessEqual ));
			theHandler.snd("</B></FONT></TD></TR>");
		}
		
		String bcolor= "tr2";
		if (((choices.size()+be)%2)==0) bcolor= "tr1";
		theHandler.snd("<TR><TD COLSPAN=4><B>Enter new choice below...</B></FONT></TD>");
		theHandler.snd("<TR class=\"" + bcolor + "\"><TD><INPUT style=\"text-align: center\" TYPE=TEXT SIZE=3 MAXSIZE=3 NAME=\"" + EDIT_PTS + "\" VALUE=\"0\"></TD>");
		theHandler.snd("<TD><TEXTAREA WRAP=\"VIRTUAL\" ROWS=5 COLS=20 NAME=\"" + EDIT_CHOICE + "\"></TEXTAREA></TD>");
		theHandler.snd("<TD><TEXTAREA WRAP=\"VIRTUAL\" ROWS=5 COLS=20 NAME=\"" + EDIT_MATCH + "\"></TEXTAREA></TD>");
		theHandler.snd("<TD class=\"tdright\">");
		theHandler.snd("match</FONT><TEXTAREA WRAP=\"VIRTUAL\" ROWS=3 COLS=20 NAME=\"" + EDIT_FEED_MATCH + "\"></TEXTAREA><BR>");
		theHandler.snd("not</FONT><TEXTAREA WRAP=\"VIRTUAL\" ROWS=3 COLS=20 NAME=\"" + EDIT_FEED_UNMATCH + "\"></TEXTAREA>");
		theHandler.snd("</TD>");
		theHandler.snd("</TR>");
		
		if (userLevel < 3) {
			theHandler.snd("<TR><TD><SPAN class=\"info\">must<BR>be<BR>&gt;=0</SPAN></TD>");
			theHandler.snd("<TD><SPAN class=\"info\">may be text or a media item.<BR>If media, enter a simple filename<BR>or a complete URL.</SPAN></TD>");
			theHandler.snd("<TD><SPAN class=\"info\">must be text</SPAN></TD>");
			theHandler.snd("<TD>&nbsp;</TD>");
			theHandler.snd("</TR>");
		}
		
		theHandler.snd("</TABLE></TD></TR>");
		
		
		// detractors
		theHandler.snd("<TR><TD><BR><B><A onMouseOver=\"(window.status='get help...'); return true;\" HREF=\"javascript:helpMessage('detractors');\">Detractors</A></B></FONT></TD><TD><BR>");
		theHandler.snd("<TABLE CELLSPACING=0 CELLPADDING=2><TR><TH>Del</FONT></TH><TH>Detractor</FONT></TH></TR>");
		for (int i=0 ; i<detractors.size() ; i++) {
			String color= "tr2";
			if ((i%2)==0) color= "tr1";
			theHandler.snd("<TR class=\"" + color + "\"><TH><INPUT TYPE=CHECKBOX NAME=\"" + EDIT_DELDET + Integer.toString(i) + "\" VALUE=\"delete\"></TH>");
			theHandler.snd("<TD><TEXTAREA WRAP=\"VIRTUAL\" ROWS=3 COLS=40 NAME=\"" + EDIT_DETRACTOR + Integer.toString(i) + "\">" + (String)detractors.elementAt(i) + "</TEXTAREA></TD></TR>");
		}
		bcolor= "tr2";
		if ((detractors.size()%2)==0) bcolor= "tr1";
		theHandler.snd("<TR><TD COLSPAN=2><B>Enter new detractor below...</B></FONT></TD>");
		theHandler.snd("<TR class=\"" + bcolor + "\"><TD>&nbsp;</TD><TD><TEXTAREA WRAP=\"VIRTUAL\" ROWS=3 COLS=40 NAME=\"" + EDIT_DETRACTOR + "\"></TEXTAREA></TD></TR>");
		
		theHandler.snd("</TABLE><SPAN class=\"info\"><BR>&nbsp;</SPAN></TD></TR>");


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

		String checkbox= theHandler.requestParams.getParam( EDIT_SUPP ).trim();
		supressChoices= (checkbox.length() != 0);

		for (int i=0 ; i<choices.size() ; i++) {
		
			choices.setElementAt( theHandler.requestParams.getParam( EDIT_CHOICE + Integer.toString(i) ).trim(), i );
			
			matches.setElementAt( theHandler.requestParams.getParam( EDIT_MATCH + Integer.toString(i) ).trim(), i );
			
			int pts= 0;
			try {
				pts= Integer.parseInt( theHandler.requestParams.getParam( EDIT_PTS + Integer.toString(i) ).trim() );
			} catch (NumberFormatException f) {
				pts= 0;
			}
			if (pts < 0) pts= 0;
			points.setElementAt( Integer.toString(pts), i );
							
			feedback.setElementAt( theHandler.requestParams.getParam( EDIT_FEED_MATCH + Integer.toString(i) ).trim() + fmReturn + theHandler.requestParams.getParam( EDIT_FEED_UNMATCH + Integer.toString(i) ).trim(), i );
			
		}
		
		for (int i=choices.size()-1 ; i>=0 ; i--) {
			String delString= theHandler.requestParams.getParam( EDIT_DEL + Integer.toString(i) ).trim();
			if (delString.length()>0) {
				choices.removeElementAt(i);
				matches.removeElementAt(i);
				feedback.removeElementAt(i);
				points.removeElementAt(i);
			}
		}
		
		String newChoice= theHandler.requestParams.getParam( EDIT_CHOICE ).trim();
		String newMatch= theHandler.requestParams.getParam( EDIT_MATCH ).trim();
		if ((newChoice.length() > 0) && (newMatch.length() > 0)) {
			choices.addElement( newChoice );
			matches.addElement( newMatch );
			feedback.addElement( theHandler.requestParams.getParam( EDIT_FEED_MATCH ).trim() + fmReturn + theHandler.requestParams.getParam( EDIT_FEED_UNMATCH ).trim() );
			points.addElement( theHandler.requestParams.getParam( EDIT_PTS ).trim() );
		}
		
		
		for (int i=0 ; i<detractors.size() ; i++)
			detractors.setElementAt( theHandler.requestParams.getParam( EDIT_DETRACTOR + Integer.toString(i) ).trim(), i );
		
		for (int i=detractors.size()-1 ; i>=0 ; i--) {
			String delString= theHandler.requestParams.getParam( EDIT_DELDET + Integer.toString(i) ).trim();
			if (delString.length()>0) detractors.removeElementAt(i);
		}
		
		String newDetractor= theHandler.requestParams.getParam( EDIT_DETRACTOR ).trim();
		if (newDetractor.length() > 0)
			detractors.addElement( newDetractor );
		
		
        if (theTest.getGUI().branchingEnabled) {
		
 			try {
                branchScore= -1;
				branchGreater= -1;
 				branchLessEqual= -1;
               
                if ( theHandler.requestParams.getParam( EDIT_BRANCH_SCORE + "_BRANCH" ).trim().equals("1") )
                	branchScore= Integer.parseInt( theHandler.requestParams.getParam( EDIT_BRANCH_SCORE ).trim() );
			
                if ( theHandler.requestParams.getParam( EDIT_BRANCH_GT + "_BRANCH" ).trim().equals("1") )
                	branchGreater= Integer.parseInt( theHandler.requestParams.getParam( EDIT_BRANCH_GT ).trim() );
			
                if ( theHandler.requestParams.getParam( EDIT_BRANCH_LE + "_BRANCH" ).trim().equals("1") )
                	branchLessEqual= Integer.parseInt( theHandler.requestParams.getParam( EDIT_BRANCH_LE ).trim() );
			
			} catch (NumberFormatException e) {}
            
            if ((branchScore != -1) && (theTest.questions.get(branchScore) == null)) branchScore= -1;
            if ((branchGreater != -1) && (theTest.questions.get(branchGreater) == null)) branchGreater= -1;
            if ((branchLessEqual != -1) && (theTest.questions.get(branchLessEqual) == null)) branchLessEqual= -1;
			
		}			

		// v4.0.6a11
		int localCount= localRandoms.size();
		localRandoms= new VectorAdapter();
		int globalCount= theTest.globalRandoms.size();
		theTest.globalRandoms= new VectorAdapter();
		randomVariable.updateRandoms( theHandler, localRandoms, localCount, theTest.globalRandoms, globalCount );
		

		// v4.1.1a3
		maxPoints= 0;
		correctAnswerFeedback= new VectorAdapter();
		for (int i=0 ; i<choices.size() ;) {
			String theChoice= (String)choices.elementAt(i);
			String theMatch= (String)matches.elementAt(i);
			
			if ((theChoice.length() == 0) || (theMatch.length() == 0)) {		// remove empty choices
				choices.removeElementAt(i);
				matches.removeElementAt(i);
				feedback.removeElementAt(i);
				points.removeElementAt(i);
			}
			else {																// calculate maxPoints
				try {
					String pstr= (String)points.elementAt(i);
					int pts= Integer.parseInt( pstr );
					maxPoints += Math.max( 0, pts );
					if (pts > 0)
						correctAnswerFeedback.addElement( pstr + " points awarded for <I>" + theChoice + "::" + theMatch + "</I><BR>" );
				} catch (NumberFormatException e) {}
				i++;
			}
		}
		

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
	public String v5elxTitle() { return("Matching Question"); }
	
	public String v5elxTemplate() { return("v5ma.xsl"); }
	
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
		if (feedback.size() > 0) {
			theFeedback= (String)feedback.elementAt(0);
			int separatorIndex= theFeedback.indexOf( fmReturn );
			if (separatorIndex >= 0) {
				if (separatorIndex == 0)	// no checked feedback
					theFeedback= theFeedback.substring(1);
				else
					theFeedback= theFeedback.substring(0,separatorIndex);
			}
		}
		result += "	<explanation><p xmlns=\"http://www.w3.org/1999/xhtml\">\r";
		result += toXMLhtml(theFeedback, theHandler);
		result += "	</p></explanation>\r";
	
		result += "	<maInfo>\r";
		
		VectorAdapter xmlChoices= new VectorAdapter();
		for (int i=0 ; i<choices.size() ; i++) 
		{
			String thisChoice = "			<pair>\r";
			thisChoice += "				<choice><p xmlns=\"http://www.w3.org/1999/xhtml\">\r";
			thisChoice += toXMLhtml((String)choices.elementAt(i), theHandler);
			thisChoice += "				</p></choice>\r";
			thisChoice += "				<match><p xmlns=\"http://www.w3.org/1999/xhtml\">\r";
			thisChoice += toXMLhtml((String)matches.elementAt(i), theHandler);
			thisChoice += "				</p></match>\r";
			thisChoice += "			</pair>\r";
			
			xmlChoices.add(thisChoice);
		}
		
		for (int i=0 ; i<detractors.size() ; i++) 
		{
			String thisChoice = "			<pair>\r";
			thisChoice += "				<choice><p xmlns=\"http://www.w3.org/1999/xhtml\">\r";
			thisChoice += toXMLhtml((String)detractors.elementAt(i), theHandler);
			thisChoice += "				</p></choice>\r";
			thisChoice += "				<match><p xmlns=\"http://www.w3.org/1999/xhtml\">\r";
			thisChoice += "				</p></match>\r";
			thisChoice += "			</pair>\r";
			
			xmlChoices.add(thisChoice);
		}
		
		if (xmlChoices.size() == 0)
		{
			String thisChoice = "			<pair>\r";
			thisChoice += "				<choice><p xmlns=\"http://www.w3.org/1999/xhtml\">\r";
			thisChoice += "				</p></choice>\r";
			thisChoice += "				<match><p xmlns=\"http://www.w3.org/1999/xhtml\">\r";
			thisChoice += "				</p></match>\r";
			thisChoice += "			</pair>\r";
			
			xmlChoices.add(thisChoice);
		}
		
		result += "		<matches>\r";
		for (int i=0; i< xmlChoices.size(); i++)
			result += (String)xmlChoices.elementAt(i);
		result += " 	</matches>\r";

		result += "	</maInfo>\r";
	
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
			_logger.error("update failure in fillInTheBlank.v5update");
			return;
		}
		
		//System.out.println(theXMLdata);
		String theFeedback= "";
		
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
				else if (thisItem.getName().equals("maInfo"))
				{
					java.util.List maData= thisItem.getChildren();
					ListIterator iter2= maData.listIterator();
					while (iter2.hasNext()) 
					{
						Element maItem= (Element)iter2.next();
						//System.out.println("    " + maItem.getName());

						if (maItem.getName().equals("matches"))
						{
							choices= new VectorAdapter();
							matches= new VectorAdapter();
							detractors= new VectorAdapter();
							feedback= new VectorAdapter();
							points= new VectorAdapter();
							correctAnswerFeedback= new VectorAdapter();
							
							java.util.List choiceData= maItem.getChildren();
							ListIterator iter3= choiceData.listIterator();
							for (int i=0; iter3.hasNext(); i++) 
							{
								Element choiceItem= (Element)iter3.next();
								//System.out.println("    " + choiceItem.getName());

								if (choiceItem.getName().equals("pair"))
								{
									String thisChoice= "";
									java.util.List thisChoiceData= choiceItem.getChildren();
									ListIterator iter4= thisChoiceData.listIterator();
									while (iter4.hasNext()) 
									{
										Element htmlItem= (Element)iter4.next();
										//System.out.println("    " + htmlItem.getName());
										
										if (htmlItem.getName().equals("choice"))
											thisChoice= embeddedMedia.processMediaIn( theHandler, theTest, getXMLhtml(htmlItem, theHandler), sqlID );
											
										else if (htmlItem.getName().equals("match"))
										{
											String thisMatch= embeddedMedia.processMediaIn( theHandler, theTest, getXMLhtml(htmlItem, theHandler), sqlID );
											
											String distractorTest= tp_utils.substitute( thisMatch, "&nbsp;", "" );
											distractorTest= tp_utils.substitute( distractorTest, "&#160;", "" );
											
											//System.out.println("match: (" + distractorTest.trim().length() + ") " + thisMatch);
											
											if (distractorTest.trim().length() == 0)
												detractors.addElement(thisChoice);
											else
											{
												//System.out.println("choice: " + thisChoice);
												choices.addElement( thisChoice );
												matches.addElement( thisMatch );
												feedback.addElement( theFeedback + fmReturn + theFeedback );
												points.addElement("1" + fmReturn + "0");
												correctAnswerFeedback.addElement("should be matched");
											}
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
//*/	

	/*
	public void v5editv1( tp_requestHandler theHandler, test theTest )
	{
		super.v5editv1( theHandler, theTest );
		
		String pageID= theHandler.getParam(test.PAGE_ID);
		
		theHandler.snd( "<TR><TD COLSPAN=2 style=\"vertical-align: top; text-align: right\"><B><A onMouseOver=\"(window.status='get help...'); return true;\" HREF=\"javascript:helpMessage('v5Choices_Matching.htm');\">Answers</A></B></TD><TD>");
		theHandler.snd("<TABLE CELLSPACING=\"0\" CELPADDING=\"2\">");
		theHandler.snd("<TR><TH>#</TH><TH>Choice</TH><TH>Match</TH><TH>Delete</TH></TR>");
		for (int i=0 ; i<choices.size() ; i++) {
			String color= "tr2";
			if ((i%2)==0) color="tr1";
			theHandler.snd("<TR class=\"" + color + "\"><TH>" + Integer.toString(i+1) + "</TH>");
			theHandler.snd("<TD>" + v5ui.htmlArea( EDIT_CHOICE + Integer.toString(i), 200, 75, (String)choices.elementAt(i), pageID) + "</TD>");
			theHandler.snd("<TD>" + v5ui.htmlArea( EDIT_MATCH + Integer.toString(i), 200, 75, (String)matches.elementAt(i), pageID) + "</TD>");
			theHandler.snd("<TD style=\"text-align: center; vertical-align: middle\">" + v5ui.checkbox( EDIT_DEL + Integer.toString(i), 20, 20, false, " ", pageID) + "</TD></TR>");
		}
		
		for (int i=0 ; i<detractors.size() ; i++) {
			String color= "tr2";
			if (((choices.size()+i)%2)==0) color="tr1";
			theHandler.snd("<TR class=\"" + color + "\"><TH>" + Integer.toString(choices.size()+i+1) + "</TH>");
			theHandler.snd("<TD>" + v5ui.htmlArea( EDIT_DETRACTOR + Integer.toString(i), 200, 75, (String)detractors.elementAt(i), pageID) + "</TD>");
			theHandler.snd("<TD style=\"text-align: center; vertical-align: middle\"><B>distractor</B></TD>");
			theHandler.snd("<TD style=\"text-align: center; vertical-align: middle\">" + v5ui.checkbox( EDIT_DEL + Integer.toString(i+1000), 20, 20, false, " ", pageID) + "</TD></TR>");
		}

		theHandler.snd("<TR><TH>Add<br>Another</TH><TD class=\"tr3\" style=\"vertical-align: top\">" + v5ui.htmlArea( EDIT_CHOICE, 200, 75, "", pageID) + "</TD><TD class=\"tr3\">" + v5ui.htmlArea( EDIT_MATCH, 200, 75, "", pageID) + "</TD><TD class=\"tr3\" style=\"vertical-align: middle\"><SPAN class=\"info\">leave<BR>match<BR>empty to<BR>define a<BR>distractor</SPAN></TD></TR>");
		theHandler.snd("</TABLE>");
		theHandler.snd( "<BR>&nbsp;</TD></TR>");

		// feedback
		String theFeedback= "";
		if (feedback.size() > 0) {
			theFeedback= (String)feedback.elementAt(0);
			int separatorIndex= theFeedback.indexOf( fmReturn );
			if (separatorIndex >= 0) {
				if (separatorIndex == 0)	// no checked feedback
					theFeedback= theFeedback.substring(1);
				else
					theFeedback= theFeedback.substring(0,separatorIndex);
			}
		}
		theHandler.snd( "<TR><TD COLSPAN=2 style=\"vertical-align: top; text-align: right\"><B><A onMouseOver=\"(window.status='get help...'); return true;\" HREF=\"javascript:helpMessage('v5/qfeedback.htm');\">Explanation</A></B></TD><TD style=\"text-align: left\">");
		theHandler.snd( v5ui.htmlArea( EDIT_FEEDBACK, 450, 100, theFeedback, pageID) );
		theHandler.snd( "<BR>&nbsp;</TD></TR>");

		super.v5finishEdit( theHandler, theTest );
	}
	

	static String	EDIT_FEEDBACK = "EDITfeedback";

	
	public void v5updatev1( tp_requestHandler theHandler, test theTest )
	{
		super.v5updatev1( theHandler, theTest );
		
		// default for print-based tests
		storage= STORE_choices;
        branchScore= -1;
        branchGreater= -1;
        branchLessEqual= -1;
		
		// feedback
		String theFeedback= theHandler.getHTMLParam( EDIT_FEEDBACK ).trim();
		feedback= new VectorAdapter();
		
		int count= choices.size();
		choices= new VectorAdapter();
		matches= new VectorAdapter();
		detractors= new VectorAdapter();
		correctAnswerFeedback= new VectorAdapter();
		points= new VectorAdapter();
		
		// old choices
		for (int i=0 ; i<count ; i++) {
			String checkbox= theHandler.getParam( EDIT_DEL + Integer.toString(i) ).trim();
			if (checkbox.equals("0")) {
				String theChoice= theHandler.getHTMLParam( EDIT_CHOICE + Integer.toString(i) ).trim();
				choices.addElement(theChoice);
				String theMatch= theHandler.getHTMLParam( EDIT_MATCH + Integer.toString(i) ).trim();
				matches.addElement(theMatch);
				points.addElement("1");
				feedback.addElement(theFeedback + fmReturn + theFeedback);
				correctAnswerFeedback.addElement(theChoice + " should be matched to " + theMatch + "<BR>");
			}
			
			checkbox= theHandler.getParam( EDIT_DEL + Integer.toString(i+1000) ).trim();
			if (checkbox.equals("0")) {
				checkbox= theHandler.getHTMLParam( EDIT_DETRACTOR + Integer.toString(i) ).trim();
				detractors.addElement(checkbox);
			}
		}
		
		// new choice
		String theChoice= theHandler.getHTMLParam( EDIT_CHOICE ).trim();
		if (theChoice.length() > 0) {
			String theMatch= theHandler.getHTMLParam( EDIT_MATCH ).trim();
			if (theMatch.length() == 0)
				detractors.addElement(theChoice);
			else {
				choices.addElement(theChoice);
				matches.addElement(theMatch);
				points.addElement("1");
				feedback.addElement(theFeedback + fmReturn + theFeedback);
				correctAnswerFeedback.addElement(theChoice + " should be matched to " + theMatch + "<BR>");
			}
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
		
		for (int i=0 ; i<matches.size() ; i++) {
			String theMatch= (String)matches.elementAt(i);
			richMedia.buildReferences(references, theMatch);
		}
		
		for (int i=0 ; i<detractors.size() ; i++) {
			String theDistractor= (String)detractors.elementAt(i);
			richMedia.buildReferences(references, theDistractor);
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
		
		for (int i=0 ; i<detractors.size() ; i++) {
			String theDet= (String)detractors.elementAt(i);
			detractors.setElementAt(tp_utils.substitute(theDet, "%media:" + oldName + "%", "%media:" + newName + "%"), i);
		}
		
		for (int i=0 ; i<matches.size() ; i++) {
			String theMatch= (String)matches.elementAt(i);
			matches.setElementAt(tp_utils.substitute(theMatch, "%media:" + oldName + "%", "%media:" + newName + "%"), i);
		}
		
		for (int i=0 ; i<feedback.size(); i++) {
			String theFeedback= (String)feedback.elementAt(i);
			feedback.setElementAt(tp_utils.substitute(theFeedback, "%media:" + oldName + "%", "%media:" + newName + "%"), i);
		}
	}


	public String v5paperHTML( tp_requestHandler theHandler, test theTest, parameters theData, String tmp, VectorAdapter keyItem ) {
					
		String resultHTML= "";
		String keyHTML= "";
		
		if (choices.size() > 0) {

			VectorAdapter scrambledChoices= new VectorAdapter();
			VectorAdapter scrambledIDs= new VectorAdapter();
			
			int totalChoices= choices.size() + detractors.size();
			boolean[] chosen= new boolean[ totalChoices ];
			for (int i=0 ; i<totalChoices ; i++)
				chosen[i]= false;
			boolean complete= false;
	
			while ( ! complete ) {
				// select a choice at random
				double theIndex= Math.floor( Math.random() * totalChoices );
				int i= (int)theIndex;
				
				if (!chosen[i]) {	// if we have not already included the choice
					chosen[i]= true;
				
					String theChoice= "unknown";
					if (i < choices.size())
						theChoice= (String)choices.elementAt(i);
					else
						theChoice= (String)detractors.elementAt(i-choices.size());
					
					// calculate the choice selection value
					double top= Math.rint( Math.random() * 100 ) * 10000;
					double bottom= Math.rint( Math.random() * 100);
					int theID= (int)(top + ((54-i)*100) + bottom);
	
					// add it to the vector
					scrambledChoices.addElement( theChoice );
					scrambledIDs.addElement( Integer.toString( theID ) );
					
					// see if we've got them all
					complete= true;
					for (int j=0 ; j<totalChoices ; j++)
						complete = complete && chosen[j];
				}
			}
			
			
			resultHTML += "<TABLE WIDTH=\"75%\">";
			if (!theTest.getGUI().terseKey) keyHTML += "<TABLE WIDTH=\"75%\">";
			
			resultHTML += "";
	
			for (int i=0 ; i<matches.size() ; i++) {
				
				String theMatch= (String)matches.elementAt(i);
				theMatch= randomVariable.deReference(theMatch, theTest, sqlID, theData);
				theMatch= richMedia.deReference(theMatch, theHandler, sqlID, theData);
	
				String thisChoice= (String)scrambledChoices.elementAt(i);
				try {		// see if we have media in the choice
					richMedia theMedia= new richMedia( thisChoice );
					theMedia.setChoice(true);
					thisChoice= theMedia.html(theHandler, "", 0, 0);
				} catch (testFormatException e) {}
	
				thisChoice= randomVariable.deReference(thisChoice, theTest, sqlID, theData);
				thisChoice= richMedia.deReference(thisChoice, theHandler, sqlID, theData);
	
				resultHTML += "<TR><TD class=\"rspStyle\" style=\"vertical-align: bottom\">" + theTest.getGUI().responseFontStart() + Integer.toString( i + 1 ) + ".&nbsp;" + thisChoice + "&nbsp;" + theTest.getGUI().responseFontEnd() + "</TD>";
				
				if (!theTest.getGUI().terseKey) keyHTML += "<TR><TD class=\"rspStyle\" style=\"vertical-align: bottom\">" + theTest.getGUI().responseFontStart() + Integer.toString( i + 1 ) + ".&nbsp;" + thisChoice + "&nbsp;" + theTest.getGUI().responseFontEnd() + "</TD>";
				
				resultHTML += "<TD class=\"rspStyle\" style=\"vertical-align: bottom; text-align: right\">" + theTest.getGUI().responseFontStart() + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + theMatch + "&nbsp;"  + theTest.getGUI().responseFontEnd() + "</TD>";
				
				if (!theTest.getGUI().terseKey) keyHTML += "<TD class=\"rspStyle\" style=\"vertical-align: bottom; text-align: right\">" + theTest.getGUI().responseFontStart() + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + theMatch + "&nbsp;"  + theTest.getGUI().responseFontEnd() + "</TD>";
				
				resultHTML += "<TD class=\"rspStyle\" style=\"vertical-align: bottom;\">" + theTest.getGUI().responseFontStart() + "&nbsp;&nbsp;____&nbsp;"  + theTest.getGUI().responseFontEnd() + "</TD></TR>";
				
				thisChoice= (String)choices.elementAt(i);
				int choiceIndex= 0;
				for( ; choiceIndex<scrambledChoices.size() ; choiceIndex++) {
					if (((String)scrambledChoices.elementAt(choiceIndex)).equals(thisChoice)) {
						if (!theTest.getGUI().terseKey) keyHTML += "<TD class=\"rspStyle\" style=\"vertical-align: bottom;\">" + theTest.getGUI().responseFontStart() + "&nbsp;&nbsp;<B><U>" + Integer.toString(choiceIndex+1) + "</U></B>&nbsp;"  + theTest.getGUI().responseFontEnd() + "</TD></TR>";
						break;
					}
				}
			}
			
			for (int i=matches.size() ; i<scrambledChoices.size() ; i++) {
				
				String thisChoice= (String)scrambledChoices.elementAt(i);
				try {		// see if we have media in the choice
					richMedia theMedia= new richMedia( thisChoice );
					theMedia.setChoice(true);
					thisChoice= theMedia.html(theHandler, "", 0, 0);
				} catch (testFormatException e) {}
	
				thisChoice= randomVariable.deReference(thisChoice, theTest, sqlID, theData);
				thisChoice= richMedia.deReference(thisChoice, theHandler, sqlID, theData);
				
				resultHTML += "<TR><TD class=\"rspStyle\" style=\"vertical-align: bottom\">" + theTest.getGUI().responseFontStart() + Integer.toString( i + 1 ) + ". " + thisChoice + "&nbsp;" + theTest.getGUI().responseFontEnd() + "</TD>";
				if (!theTest.getGUI().terseKey) keyHTML += "<TR><TD class=\"rspStyle\" style=\"vertical-align: bottom\">" + theTest.getGUI().responseFontStart() + Integer.toString( i + 1 ) + ". " + thisChoice + "&nbsp;" + theTest.getGUI().responseFontEnd() + "</TD>";

				resultHTML += "<TD>&nbsp;</TD><TD>&nbsp;</TD></TR>";
				if (!theTest.getGUI().terseKey) keyHTML += "<TD>&nbsp;</TD><TD>&nbsp;</TD></TR>";
				
			}
			
			resultHTML += "</TABLE>";
			
			if (!theTest.getGUI().terseKey) 
			{
				keyHTML += "</TABLE>";
				
				if (feedback.size() > 0)
				{
					String theFeedback= (String)feedback.elementAt(0);
					int separatorIndex= theFeedback.indexOf( fmReturn );
					if (separatorIndex >= 0) {
						if (separatorIndex == 0)	// no checked feedback
							theFeedback= theFeedback.substring(1);
						else
							theFeedback= theFeedback.substring(0,separatorIndex);
					}
					
					theFeedback= randomVariable.deReference(theFeedback, theTest, sqlID, theData);
					theFeedback= richMedia.deReference(theFeedback, theHandler, sqlID, theData);
					if (theFeedback.trim().length() > 0) keyHTML += "<p class=\"feedbackStyle\">" + theFeedback + "</p>";
				}
			}
			else
			{
				keyHTML= "";
				for (int i=0 ; i<matches.size() ; i++) {
					
					String theMatch= (String)matches.elementAt(i);
					theMatch= randomVariable.deReference(theMatch, theTest, sqlID, theData);
					theMatch= richMedia.deReference(theMatch, theHandler, sqlID, theData);
		
					String thisChoice= (String)choices.elementAt(i);
					try {		// see if we have media in the choice
						richMedia theMedia= new richMedia( thisChoice );
						theMedia.setChoice(true);
						thisChoice= theMedia.html(theHandler, "", 0, 0);
					} catch (testFormatException e) {}
		
					thisChoice= randomVariable.deReference(thisChoice, theTest, sqlID, theData);
					thisChoice= richMedia.deReference(thisChoice, theHandler, sqlID, theData);
		
					if (keyHTML.length() > 0) keyHTML += "<span class=\"rspHelpStyle\">&nbsp;&nbsp;&nbsp;and&nbsp;&nbsp;&nbsp;</span>";
					keyHTML += thisChoice + "&nbsp;::&nbsp;" + theMatch;
				}
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
			
			String theMatch= (String)matches.elementAt(i);
			theMatch= richMedia.deReference(theMatch, theHandler);
			
			result += "<p>" + theChoice + "::" + theMatch + "</p>\n";
		}
		
		for (int i=0 ; i<detractors.size() ; i++) 
		{
			String theChoice= (String)detractors.elementAt(i);
			theChoice= richMedia.deReference(theChoice, theHandler);
			
			result += "<p>" + "::" + theChoice + "</p>\n";
		}
		
		if (feedback.size() > 0)
		{
			String theFeedback= (String)feedback.elementAt(0);
			int separatorIndex= theFeedback.indexOf( fmReturn );
			if (separatorIndex >= 0) {
				if (separatorIndex == 0)	// no checked feedback
					theFeedback= theFeedback.substring(1);
				else
					theFeedback= theFeedback.substring(0,separatorIndex);
			}

			theFeedback= richMedia.deReference(theFeedback, theHandler);
			if (theFeedback.trim().length() > 0) 
				result += "<p>Feedback: " + theFeedback + "</p>\n";
		}

		result += v5roundrobinEnd( theHandler, theTest, references );
		return(result);
	}


	public String v5rtf( tp_requestHandler theHandler, test theTest, parameters theData, String tmp, VectorAdapter keyItem ) 
	{
		String resultHTML= "";
		String keyHTML= "";
		
		if (choices.size() > 0)
		{
			VectorAdapter scrambledChoices= new VectorAdapter();
			VectorAdapter scrambledIDs= new VectorAdapter();
			
			int totalChoices= choices.size() + detractors.size();
			boolean[] chosen= new boolean[ totalChoices ];
			for (int i=0 ; i<totalChoices ; i++)
				chosen[i]= false;
			boolean complete= false;
	
			while ( ! complete ) 
			{
				// select a choice at random
				double theIndex= Math.floor( Math.random() * totalChoices );
				int i= (int)theIndex;
				
				if (!chosen[i])		// if we have not already included the choice
				{
					chosen[i]= true;
				
					String theChoice= "unknown";
					if (i < choices.size())
						theChoice= (String)choices.elementAt(i);
					else
						theChoice= (String)detractors.elementAt(i-choices.size());
					
					// calculate the choice selection value
					double top= Math.rint( Math.random() * 100 ) * 10000;
					double bottom= Math.rint( Math.random() * 100);
					int theID= (int)(top + ((54-i)*100) + bottom);
	
					// add it to the vector
					scrambledChoices.addElement( theChoice );
					scrambledIDs.addElement( Integer.toString( theID ) );
					
					// see if we've got them all
					complete= true;
					for (int j=0 ; j<totalChoices ; j++)
						complete = complete && chosen[j];
				}
			}
			
			
			resultHTML += "<table width=\"75%\">";
			if (!theTest.getGUI().terseKey) keyHTML += "<table width=\"75%\">";
			
			for (int i=0 ; i<matches.size() ; i++)
			{
				
				String theMatch= (String)matches.elementAt(i);
				theMatch= randomVariable.deReference(theMatch, theTest, sqlID, theData);
				theMatch= richMedia.deReference(theMatch, theHandler);
	
				String thisChoice= (String)scrambledChoices.elementAt(i);
	
				thisChoice= randomVariable.deReference(thisChoice, theTest, sqlID, theData);
				thisChoice= richMedia.deReference(thisChoice, theHandler, sqlID, theData);
	
				resultHTML += "<TR><TD class=\"rspStyle\" style=\"vertical-align: bottom\">" + Integer.toString( i + 1 ) + ".&nbsp;" + thisChoice + "&nbsp;</TD>";
				
				if (!theTest.getGUI().terseKey) keyHTML += "<TR><TD class=\"rspStyle\" style=\"vertical-align: bottom\">" + Integer.toString( i + 1 ) + ".&nbsp;" + thisChoice + "&nbsp;</TD>";
				
				resultHTML += "<TD class=\"rspStyle\" style=\"vertical-align: bottom; text-align: right\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + theMatch + "&nbsp;</TD>";
				
				if (!theTest.getGUI().terseKey) keyHTML += "<TD class=\"rspStyle\" style=\"vertical-align: bottom; text-align: right\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + theMatch + "&nbsp;</TD>";
				
				resultHTML += "<TD class=\"rspStyle\" style=\"vertical-align: bottom;\">&nbsp;&nbsp;____&nbsp;</TD></TR>";
				
				thisChoice= (String)choices.elementAt(i);
				int choiceIndex= 0;
				for( ; choiceIndex<scrambledChoices.size() ; choiceIndex++) 
				{
					if (((String)scrambledChoices.elementAt(choiceIndex)).equals(thisChoice)) 
					{
						if (!theTest.getGUI().terseKey) keyHTML += "<TD class=\"rspStyle\" style=\"vertical-align: bottom;\">&nbsp;&nbsp;<span style=\"font-weight: bold; text-decoration: underline\">" + Integer.toString(choiceIndex+1) + "</span>&nbsp;</TD></TR>";
						break;
					}
				}
			}
			
			for (int i=matches.size() ; i<scrambledChoices.size() ; i++) 
			{
				String thisChoice= (String)scrambledChoices.elementAt(i);
				thisChoice= randomVariable.deReference(thisChoice, theTest, sqlID, theData);
				thisChoice= richMedia.deReference(thisChoice, theHandler, sqlID, theData);
				
				resultHTML += "<TR><TD class=\"rspStyle\" style=\"vertical-align: bottom\">" + Integer.toString( i + 1 ) + ". " + thisChoice + "&nbsp;</TD>";
				if (!theTest.getGUI().terseKey) keyHTML += "<TR><TD class=\"rspStyle\" style=\"vertical-align: bottom\">" + Integer.toString( i + 1 ) + ". " + thisChoice + "&nbsp;</TD>";

				resultHTML += "<TD>&nbsp;</TD><TD>&nbsp;</TD></TR>";
				if (!theTest.getGUI().terseKey) keyHTML += "<TD>&nbsp;</TD><TD>&nbsp;</TD></TR>";
				
			}
			
			resultHTML += "</table>";
			
			if (!theTest.getGUI().terseKey) 
			{
				keyHTML += "</table>";
				
				String defaultFeedback= questionProperties.getString(COMMON_FEEDBACK, "").trim();
				if (defaultFeedback == null || ("").equals(defaultFeedback))
				{
					defaultFeedback= (String)feedback.elementAt(0);
					int separatorIndex= defaultFeedback.indexOf( fmReturn );
					if (separatorIndex >= 0) 
					{
						if (separatorIndex == 0)
							defaultFeedback= defaultFeedback.substring(1);
						else
							defaultFeedback= defaultFeedback.substring(0,separatorIndex);
					}
				}
				
				String theFeedback= "";
				
				if ((defaultFeedback.length() > 0))
				{
					theFeedback= defaultFeedback;
					theFeedback= tooltipDeReference(theFeedback);
					theFeedback= randomVariable.deReference(theFeedback, theTest, sqlID, theData);
					theFeedback= richMedia.deReference(theFeedback, theHandler, sqlID, theData);
				}
				
				if (theFeedback.trim().length() > 0){
					keyHTML += "<p class=\"feedbackStyle\">" + theFeedback + "</p>";
				}
			}
			else
			{
				keyHTML= "";
				for (int i=0 ; i<matches.size() ; i++) 
				{
					String theMatch= (String)matches.elementAt(i);
					theMatch= randomVariable.deReference(theMatch, theTest, sqlID, theData);
					theMatch= richMedia.deReference(theMatch, theHandler, sqlID, theData);
		
					String thisChoice= (String)choices.elementAt(i);
					thisChoice= randomVariable.deReference(thisChoice, theTest, sqlID, theData);
					thisChoice= richMedia.deReference(thisChoice, theHandler, sqlID, theData);
		
					if (keyHTML.length() > 0) keyHTML += "<span class=\"rspHelpStyle\">&nbsp;&nbsp;&nbsp;and&nbsp;&nbsp;&nbsp;</span>";
					keyHTML += thisChoice + "&nbsp;::&nbsp;" + theMatch;
				}
			}
		}
		
		if (keyItem != null) keyItem.addElement(keyHTML);
		
		return(resultHTML);
	}

	/*
	public String v5pdfHTML( tp_requestHandler theHandler, test theTest, parameters theData, String tmp, VectorAdapter keyItem ) {
					
		String resultHTML= "";
		String keyHTML= "";
		
		if (choices.size() > 0) {

			VectorAdapter scrambledChoices= new VectorAdapter();
			VectorAdapter scrambledIDs= new VectorAdapter();
			
			int totalChoices= choices.size() + detractors.size();
			boolean[] chosen= new boolean[ totalChoices ];
			for (int i=0 ; i<totalChoices ; i++)
				chosen[i]= false;
			boolean complete= false;
	
			while ( ! complete ) {
				// select a choice at random
				double theIndex= Math.floor( Math.random() * totalChoices );
				int i= (int)theIndex;
				
				if (!chosen[i]) {	// if we have not already included the choice
					chosen[i]= true;
				
					String theChoice= "unknown";
					if (i < choices.size())
						theChoice= (String)choices.elementAt(i);
					else
						theChoice= (String)detractors.elementAt(i-choices.size());
						
					theChoice= pdfoutput.fixHTML(theChoice);
					
					// calculate the choice selection value
					double top= Math.rint( Math.random() * 100 ) * 10000;
					double bottom= Math.rint( Math.random() * 100);
					int theID= (int)(top + ((54-i)*100) + bottom);
	
					// add it to the vector
					scrambledChoices.addElement( theChoice );
					scrambledIDs.addElement( Integer.toString( theID ) );
					
					// see if we've got them all
					complete= true;
					for (int j=0 ; j<totalChoices ; j++)
						complete = complete && chosen[j];
				}
			}
			
			
			resultHTML += "<table width=\"85%\">";
			if (!theTest.getGUI().terseKey) keyHTML += "<table width=\"85%\">";
			
			resultHTML += "";
	
			for (int i=0 ; i<matches.size() ; i++) {
				
				String theMatch= pdfoutput.fixHTML( (String)matches.elementAt(i) );
				theMatch= randomVariable.deReference(theMatch, theTest, sqlID, theData);
				theMatch= richMedia.pdfDeReference(theMatch, theHandler, sqlID, theData);
	
				String thisChoice= (String)scrambledChoices.elementAt(i);
				try {		// see if we have media in the choice
					richMedia theMedia= new richMedia( thisChoice );
					theMedia.setChoice(true);
					thisChoice= theMedia.pdfhtml("");
				} catch (testFormatException e) {}
	
				thisChoice= randomVariable.deReference(thisChoice, theTest, sqlID, theData);
				thisChoice= richMedia.pdfDeReference(thisChoice, theHandler, sqlID, theData);
	
				resultHTML += "<tr><td class=\"matchLeft\">" + Integer.toString( i + 1 ) + ".&nbsp;" + thisChoice + "</td>";
				if (!theTest.getGUI().terseKey) keyHTML += "<tr><td class=\"matchLeft\">" + Integer.toString( i + 1 ) + ".&nbsp;" + thisChoice + "</td>";
				
				resultHTML += "<td class=\"matchRight\" style=\"vertical-align: bottom; align: right\" width=\"40%\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + theMatch + "</td>";
				if (!theTest.getGUI().terseKey) keyHTML += "<td class=\"matchRight\" style=\"vertical-align: bottom; align: right\" width=\"40%\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + theMatch + "</td>";
				
				resultHTML += "<td class=\"matchSpace\">&nbsp;&nbsp;____</td></tr>";
				
				thisChoice= (String)choices.elementAt(i);
				int choiceIndex= 0;
				for( ; choiceIndex<scrambledChoices.size() ; choiceIndex++) {
					if (((String)scrambledChoices.elementAt(choiceIndex)).equals(thisChoice)) {
						if (!theTest.getGUI().terseKey) keyHTML += "<td class=\"matchSpace\" style=\"vertical-align: bottom;\">&nbsp;&nbsp;<span class=\"correct\">" + Integer.toString(choiceIndex+1) + "</span></td></tr>";
						break;
					}
				}
			}
			for (int i=matches.size() ; i<scrambledChoices.size() ; i++) {
				
				String thisChoice= (String)scrambledChoices.elementAt(i);
				try {		// see if we have media in the choice
					richMedia theMedia= new richMedia( thisChoice );
					theMedia.setChoice(true);
					thisChoice= theMedia.pdfhtml("");
				} catch (testFormatException e) {}
	
				thisChoice= randomVariable.deReference(thisChoice, theTest, sqlID, theData);
				thisChoice= richMedia.pdfDeReference(thisChoice, theHandler, sqlID, theData);
				
				resultHTML += "<tr><td class=\"matchLeft\" colspan=\"3\">" + Integer.toString( i + 1 ) + ". " + thisChoice + "</td></tr>";
				if (!theTest.getGUI().terseKey) keyHTML += "<tr><td class=\"matchLeft\" colspan=\"3\">" + Integer.toString( i + 1 ) + ". " + thisChoice + "<span class=\"correct\">&nbsp;</span></td></tr>";

			}
			
			resultHTML += "</table>";

			if (!theTest.getGUI().terseKey)
			{
				keyHTML += "</table>";
				
				if (feedback.size() > 0)
				{
					String theFeedback= (String)feedback.elementAt(0);
					int separatorIndex= theFeedback.indexOf( fmReturn );
					if (separatorIndex >= 0) {
						if (separatorIndex == 0)	// no checked feedback
							theFeedback= theFeedback.substring(1);
						else
							theFeedback= theFeedback.substring(0,separatorIndex);
					}
					
					theFeedback= pdfoutput.fixHTML( theFeedback );
					theFeedback= randomVariable.deReference(theFeedback, theTest, sqlID, theData);
					theFeedback= richMedia.pdfDeReference(theFeedback, theHandler, sqlID, theData);
					if (theFeedback.trim().length() > 0) keyHTML += "<p>" + theFeedback + "</p>";
				}
			}
			
			else
			{
				keyHTML= "";
				for (int i=0 ; i<matches.size() ; i++) {
					
					String theMatch= (String)matches.elementAt(i);
					theMatch= randomVariable.deReference(theMatch, theTest, sqlID, theData);
					theMatch= richMedia.pdfDeReference(theMatch, theHandler, sqlID, theData);
		
					String thisChoice= (String)choices.elementAt(i);
					try {		// see if we have media in the choice
						richMedia theMedia= new richMedia( thisChoice );
						theMedia.setChoice(true);
						thisChoice= theMedia.html("");
					} catch (testFormatException e) {}
		
					thisChoice= randomVariable.deReference(thisChoice, theTest, sqlID, theData);
					thisChoice= richMedia.pdfDeReference(thisChoice, theHandler, sqlID, theData);
		
					if (keyHTML.length() > 0) keyHTML += "<span class=\"rspHelpStyle\">&nbsp;&nbsp;&nbsp;and&nbsp;&nbsp;&nbsp;</span>";
					keyHTML += thisChoice + "&nbsp;::&nbsp;" + theMatch;
				}
			}
		}
		
		if (keyItem != null) keyItem.addElement(keyHTML);
		
		return(resultHTML);
						
	}
	*/


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
		item.addContent(new Comment("EZ Test - matching question"));
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
		subelement.addContent("Matching Question will be scored by standard response processing.");
		resprocessing.addContent(subelement);
		
		Element outcomes= new Element("outcomes", theExporter.ns_imsQuestion);
		resprocessing.addContent(outcomes);
		
		int max= 100;
		int perMatch= 100;
		if (matches.size() > 0)
		{
			perMatch= max / matches.size();
			max= perMatch * matches.size();
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
		subelement.setAttribute("maxvalue", Integer.toString(matches.size()));
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
			String theChoice= theChoice= (String)choices.elementAt(i);
			theChoice= randomVariable.deReference(theChoice, theTest, sqlID, theHandler.requestParams);
			theChoice= richMedia.deReference(theChoice, baseURL, true);
			
			String theMatch=(String)matches.elementAt(i);
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
			
			for (int j=0 ; j<matches.size() ; j++) 
			{
				theMatch= (String)matches.elementAt(j);
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
			theFeedback= (String)feedback.elementAt(0);
			int separatorIndex= theFeedback.indexOf( fmReturn );
			if (separatorIndex >= 0) 
			{
				if (separatorIndex == 0)
					theFeedback= theFeedback.substring(1);
				else
					theFeedback= theFeedback.substring(0,separatorIndex);
			}

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


	/**
	* build a representation of this question appropriate for import into WebCT.
	* @param	theTest			the test being issued
	* @param	randomData		parameters object that include random variable values
	* @return	String containing WebCT-formatted question data.
	*/
	public String v5webct( tp_requestHandler theHandler, test theTest, parameters randomData )
	{
		String webCTcode= ":TYPE:M:long:short:E:0\n";
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
		
		// add v4 question media, if any
		if (questionMedia != null)
			stemHTML += questionMedia.html(substitutedText);
		else
			stemHTML += substitutedText;

		// do not number
		stemHTML= tp_utils.substitute( stemHTML, question.SUBTAG3, "&nbsp;");
		
		webCTcode += ":QUESTION:H " + refHTML.trim() + stemHTML.trim() + "\n";
		

		// add the responses html
		for (int i=0 ; i<choices.size() ; i++) {
			String theChoice= (String)choices.elementAt(i);
			String theMatch= (String)matches.elementAt(i);
			
			try {
				richMedia theMedia= new richMedia( theChoice );
				theMedia.setChoice(true);
				theChoice= theMedia.html(theHandler, "", 0, 0);
			} catch (testFormatException e) {}
			
			try {
				richMedia theMedia= new richMedia( theMatch );
				theMedia.setChoice(true);
				theMatch= theMedia.html(theHandler, "", 0, 0);
			} catch (testFormatException e) {}
			
			theChoice= randomVariable.deReference(theChoice, theTest, sqlID, randomData);
			theChoice= richMedia.deReference(theChoice, theHandler.getParam(v5test.USE_BASEURL));

			theMatch= randomVariable.deReference(theMatch, theTest, sqlID, randomData);
			theMatch= richMedia.deReference(theMatch, theHandler.getParam(v5test.USE_BASEURL));

			webCTcode += ":L" + Integer.toString(i+1) + "\n" + theChoice.trim() + "\n";
			webCTcode += ":R" + Integer.toString(i+1) + "\n" + theMatch.trim() + "\n";
		}

		webCTcode += ":FEEDBACK\n";
		for (int i=0 ; i<correctAnswerFeedback.size() ; i++) 
		{
			String theFeedback= "";
			if (feedback.size() > i)
				theFeedback= ((String)correctAnswerFeedback.elementAt(i)).trim();
			theFeedback= randomVariable.deReference(theFeedback, theTest, sqlID, randomData);
			theFeedback= richMedia.deReference(theFeedback, theHandler.getParam(v5test.USE_BASEURL) ).trim();
			
			String suppInfo= "";
			if (theHandler.getParam(test.EXPORT_INFO).equals("true")) suppInfo= supplementaryKludge( theHandler );
			if (suppInfo.length() > 0)
			{
				if (theFeedback.length() > 0) theFeedback += "<br>&nbsp;<br>";
				theFeedback += suppInfo;
			}

			if (theFeedback.trim().length() > 0)
				webCTcode += theFeedback + "\n";
		}
		
		String category= theHandler.getParam(v5test.WEBCT_CAT);
		if (category.length() == 0) category= "TP";
		webCTcode += ":CAT:" + category + "\n";
		
		webCTcode += "\n";
		
		return( webCTcode );
	}


	public static question webCTimport( tp_requestHandler theHandler, test theTest, VectorAdapter theData )
	{
		question theQ= new matching();
		theQ.qtext= "";
		
		// locate QUESTION
		boolean foundit= false;
		for (int i=0 ; i< theData.size() ; i++) {
			String qData= (String)theData.elementAt(i);
			if (qData.startsWith(":QUESTION")) foundit= true;
			else if (qData.startsWith(":")) foundit= false;
			else if (foundit) theQ.qtext += qData + " ";
		}
		
		// see if there's an IMAGE
		for (int i=0 ; i< theData.size() ; i++) {
			String qData= (String)theData.elementAt(i);
			if (qData.startsWith(":IMAGE:")) {
				theQ.qtext += "%media:" + qData.substring(7) + "%";
				break;
			}
		}
		
		// locate FEEDBACK
		foundit= false;
		String theFeedback= "";
		for (int i=0 ; i< theData.size() ; i++) {
			String qData= (String)theData.elementAt(i);
			if (qData.startsWith(":FEEDBACK")) foundit= true;
			else if (qData.startsWith(":")) foundit= false;
			else if (foundit) theFeedback += qData + " ";
		}


		// load choices and custom feedback
		VectorAdapter pairs= new VectorAdapter();
		for (int cNum= 1; cNum <=30 ; cNum++) {
		
			boolean foundChoice= false;
			boolean foundMatch= false;
			String theChoice= "";
			String theMatch= "";
			String cLabel= ":L" + Integer.toString(cNum);
			String mLabel= ":R" + Integer.toString(cNum);
			
			for (int j=0 ; j< theData.size() ; j++) {
				String qData= (String)theData.elementAt(j);

				if (qData.startsWith(cLabel)) foundChoice= true;
				else if (qData.startsWith(":")) foundChoice= false;
				else if (foundChoice) theChoice += qData + " ";	
							
				if (qData.startsWith(mLabel)) foundMatch= true;
				else if (qData.startsWith(":")) foundMatch= false;
				else if (foundMatch) theMatch += qData + " ";	
			}
			
			if ((theChoice.length() > 0) && (theMatch.length() > 0)) {
				theChoice= MH_media( theChoice, theQ.id, theTest );
				theMatch= MH_media( theMatch, theQ.id, theTest );
				theFeedback= MH_media( theFeedback, theQ.id, theTest );

				theQ.choices.addElement(theChoice);
				((matching)theQ).matches.addElement(theMatch);
				
				theQ.points.addElement("1");
				theQ.correctAnswerFeedback.addElement(theChoice + " should be matched to " + theMatch + "<BR>");
				theQ.feedback.addElement(theFeedback + fmReturn + theFeedback);
			}
			
		}
		
		
		// assign a new id
		theQ.id= theTest.questions.newID();
		
		// process any McGraw-Hill media library items
		theQ.qtext= MH_media( theQ.qtext, theQ.id, theTest );

		return( theQ );		
	}





	public Element bb9( tp_requestHandler theHandler, String mediaBaseURL )
	{
		Element item= new Element("item");
		item.setAttribute("maxattempts", "0");
		
		item.addContent( bbExporter.stdItemMetaData( theHandler, this, "Matching" ) );
		
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

			for (int i=0 ; i<choices.size() ; i++) 
			{
				String cid= sqlID + "_" + Integer.toString(i);
				String mid= sqlID + "_0" + Integer.toString(i);
				
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


				Element choiceBlock= new Element("flow");
				responseBlock.addContent( choiceBlock );
				choiceBlock.setAttribute("class", "Block");
				
				choiceBlock.addContent( bbExporter.stdFormattedTextBlock(new CDATA(theChoice)) );
				//choiceBlock.addContent( bbExporter.stdFileBlock() );
				//choiceBlock.addContent( bbExporter.stdLinkBlock() );

			
				Element response_lid= new Element("response_lid");
				choiceBlock.addContent( response_lid );
				response_lid.setAttribute("ident", cid);
				response_lid.setAttribute("rcardinality", "Single");
				response_lid.setAttribute("rtiming", "No");
				
				Element render_choice= new Element("render_choice");
				response_lid.addContent( render_choice );
				render_choice.setAttribute("maxnumber", "0");
				render_choice.setAttribute("minnumber", "0");
				render_choice.setAttribute("shuffle", "No");

				Element flow_label= new Element("flow_label");
				render_choice.addContent( flow_label );
				flow_label.setAttribute("class", "Block");
				
				for (int j=0; j< matches.size(); j++)
				{
					String theMID= sqlID + "_0" + Integer.toString(i);

					Element response_label= new Element("response_label");
					flow_label.addContent( response_label );
					response_label.setAttribute("ident", theMID);
					response_label.setAttribute("rarea", "Ellipse");
					response_label.setAttribute("rrange", "Exact");
					response_label.setAttribute("shuffle", "Yes");
				}
				

				Element respcondition= new Element("respcondition");
				resprocessing.addContent(respcondition);
				respcondition.setAttribute("title", "correct");
				
				Element conditionvar= new Element("conditionvar");
				respcondition.addContent(conditionvar);
				
				Element varequal= new Element("varequal");
				conditionvar.addContent( varequal );
				varequal.setAttribute("case", "No");
				varequal.setAttribute("respident", cid);
				varequal.setText(mid);
				
				respcondition.addContent( bbExporter.stdDisplayFeedback( "correct" ) );
			}
				

			Element incorrectCondition= new Element("respcondition");
			resprocessing.addContent(incorrectCondition);
			incorrectCondition.setAttribute("title", "incorrect");
			
			Element incorrectVar= new Element("conditionvar");
			incorrectCondition.addContent(incorrectVar);
			
			Element incorrectCompare= new Element("other");
			incorrectVar.addContent( incorrectCompare );
			
			incorrectCondition.addContent(bbExporter.stdScoreZero());
			
			incorrectCondition.addContent( bbExporter.stdDisplayFeedback( "incorrect" ) );


			
			Element rightMatchBlock= new Element("flow");
			flowOuter.addContent( rightMatchBlock );
			rightMatchBlock.setAttribute("class", "RIGHT_MATCH_BLOCK");
			
			for (int i=0 ; i<matches.size() ; i++) 
			{
				String mid= sqlID + "_0" + Integer.toString(i);
				
				String theMatch= (String)choices.elementAt(i);
				try 
				{
					richMedia theMedia= new richMedia( theMatch );
					theMedia.setChoice(true);
					theMatch= theMedia.html(theHandler, "", 0, 0);
				} 
				catch (testFormatException e) {}
				
				theMatch= randomVariable.deReference(theMatch, theHandler.currentTest, sqlID, theHandler.requestParams);
				theMatch= richMedia.deReference(theMatch, mediaBaseURL);


				Element matchBlock= new Element("flow");
				rightMatchBlock.addContent( matchBlock );
				matchBlock.setAttribute("class", "Block");
				
				matchBlock.addContent( bbExporter.stdFormattedTextBlock(new CDATA(theMatch)) );
				//matchBlock.addContent( bbExporter.stdFileBlock() );
				//matchBlock.addContent( bbExporter.stdLinkBlock() );
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
		

		String aCorrectAnswer= "";
		for (int i=0 ; i<choices.size() ; i++) 
		{
			String theChoice= (String)choices.elementAt(i);
			String theMatch= (String)matches.elementAt(i);
			aCorrectAnswer += "<p>" + theChoice + "<br>should be matched to<br>" + theMatch + "</p>";
		}
		
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
		
		String theFeedback= "";
		if (feedback.size() > 0)
		{
			theFeedback= (String)feedback.elementAt(0);
			int separatorIndex= theFeedback.indexOf( fmReturn );
			if (separatorIndex >= 0) 
			{
				if (separatorIndex == 0)
					theFeedback= theFeedback.substring(1);
				else
					theFeedback= theFeedback.substring(0,separatorIndex);
			}
		}
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
	
	

	public boolean consumesMedia( String theMedia )
	{
		if (qtext.indexOf(theMedia) >= 0) return(true);
		
		for (int i=0 ; i<choices.size(); i++) {
			String item= (String)choices.elementAt(i);
			if (item.indexOf(theMedia) >= 0) return(true);
		}
		
		for (int i=0 ; i<matches.size(); i++) {
			String item= (String)matches.elementAt(i);
			if (item.indexOf(theMedia) >= 0) return(true);
		}
		
		for (int i=0 ; i<detractors.size(); i++) {
			String item= (String)detractors.elementAt(i);
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
		
		theWriter.add("<span style=\"font-weight: bold; color: " + color + "\">" + Integer.toString(choices.size()) + "&nbsp;matched&nbsp;pairs</span><br />");
		theWriter.add(Integer.toString(detractors.size()) + "&nbsp;distractors&nbsp;with&nbsp;no&nbsp;match<br />");
		
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
				result += "<span style=\"color: red\">Error: </span>choice #" + Integer.toString(i+1) + " empty in " + qnumber + "<br />";

			String theMatch= (String)matches.elementAt(i);
			if (theMatch.length() == 0)
				result += "<span style=\"color: red\">Error: </span>match #" + Integer.toString(i+1) + " empty in " + qnumber + "<br />";
		}

		for (int i=0 ; i<detractors.size(); i++) {
			String theHerring= (String)detractors.elementAt(i);
			if (theHerring.length() == 0)
				result += "<span style=\"color: red\">Error: </span>distractor #" + Integer.toString(i+1) + " empty in " + qnumber + "<br />";
		}

		if (detractors.size() == 0) 
			result += "<span style=\"color: #cc3300\">Warning: </span>no red herring distractors #" + qnumber + "<br />";

		return(result);
	}
	
	
	public String v6show( tp_requestHandler theHandler, test theTest, submission theSubmission, parameters theData, String unusedText ) 
	{
		if (choices.size() == 0) return("");
		/**
		 * If the C15 Instructor UI feature is enabled, then v6show_C15 should be invoked for rendering question 
		 */
		Vector enabledFeatureList= tp_utils.listToVector( licenseManager.getFeatureInfo(theHandler, licenseManager.LICENSE_TYPE_HMFEATURE), "," );
		boolean c15InstructorUiEnabled = enabledFeatureList.contains(C15INSTRUCTORUI);
		if (c15InstructorUiEnabled && classware_hm.instructorRole(theHandler.getParam(classware_hm.ROLE))){	    
			return renderItem(theHandler, theTest, null, unusedText, theSubmission, theData);
		}
		//boolean answerSpecificFeedbackSupport= licenseManager.hasFeature(theHandler, licenseManager.FEATURE_SUPPORT_ASFEEDBACK);
		
		String theQID= "Q_" + sqlID;
		
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
		
		/* nevershow debug
		System.out.println("");
		System.out.println("isManager: " + (theHandler.isManager ? "true":"false"));
		System.out.println("afterTest: " + (afterTest ? "true":"false"));
		System.out.println("pregrading: " + (pregrading ? "true":"false"));
		System.out.println("previewing: " + (previewing ? "true":"false"));
		System.out.println("recallOK: " + (recallOK ? "true":"false"));
		System.out.println("indicators: " + (indicators ? "true":"false"));
		System.out.println("solution: " + (solution ? "true":"false"));
		System.out.println("fullFeedback: " + (fullFeedback ? "true":"false"));
		//*/

		String answerSpecificFeedback= "";
		String defaultFeedback= questionProperties.getString(COMMON_FEEDBACK, null);
		if (defaultFeedback == null)
		{
			defaultFeedback= (String)feedback.elementAt(0);
			int separatorIndex= defaultFeedback.indexOf( fmReturn );
			if (separatorIndex >= 0) 
			{
				if (separatorIndex == 0)
					defaultFeedback= defaultFeedback.substring(1);
				else
					defaultFeedback= defaultFeedback.substring(0,separatorIndex);
			}
		}

		VectorAdapter scrambledChoices= new VectorAdapter();
		VectorAdapter scrambledIDs= new VectorAdapter();
		
		VectorAdapter scrambledAnswers= new VectorAdapter();
		for (int i=0 ; i<matches.size() ; i++) 
			scrambledAnswers.addElement("");
		
		int totalChoices= choices.size() + detractors.size();
		boolean[] chosen= new boolean[ totalChoices ];
		for (int i=0 ; i<totalChoices ; i++)
			chosen[i]= false;
		boolean complete= false;
		
		while ( ! complete ) 
		{
			// select a choice at random
			double theIndex= Math.floor( Math.random() * totalChoices );
			int i= (int)theIndex;
			
			if (!chosen[i]) {	// if we have not already included the choice
				chosen[i]= true;
				
				String theChoice= "unknown";
				if (i < choices.size())
					theChoice= (String)choices.elementAt(i);
				else
					theChoice= (String)detractors.elementAt(i-choices.size());
				
				theChoice= tooltipDeReference(theChoice);
				theChoice= randomVariable.deReference(theChoice, theTest, sqlID, theData);
				theChoice= richMedia.deReference(theChoice, theHandler, sqlID, theData);
				
				// calculate the choice selection value
				double top= Math.rint( Math.random() * 100 ) * 10000;
				double bottom= Math.rint( Math.random() * 100);
				int theID= (int)(top + ((54-i)*100) + bottom);
				
				// add it to the vector
				scrambledChoices.addElement( theChoice );
				scrambledIDs.addElement( Integer.toString( theID ) );
				
				if (i < choices.size())
					scrambledAnswers.setElementAt( "&nbsp;&nbsp;<span style=\"color: #004000; font-size: 9pt\">#" + Integer.toString(scrambledChoices.size()) + "</span>", i );

				// see if we've got them all
				complete= true;
				for (int j=0 ; j<totalChoices ; j++)
					complete = complete && chosen[j];
			}
		}
		
		
		ConcurrentHashMap userCorrectness= new ConcurrentHashMap();
		ConcurrentHashMap userChoices= new ConcurrentHashMap();
		if (theData != null) 
		{		
			// if the participant has responded
			for (int i=0 ; i<matches.size() ; i++) 
			{
				String fb_ma= "";
				String fb_no= "";
				if (i < feedback.size())
				{
					StringTokenizer theTokens= new StringTokenizer( (String)feedback.elementAt(i), fmReturn );
					if (theTokens.hasMoreTokens()) fb_ma= theTokens.nextToken().trim();
					if (theTokens.hasMoreTokens()) fb_no= theTokens.nextToken().trim();
				}
				
				String theMatch= (String)matches.elementAt(i);
				theMatch= tooltipDeReference(theMatch);
				theMatch= randomVariable.deReference(theMatch, theTest, sqlID, theData);
				theMatch= richMedia.deReference(theMatch, theHandler, sqlID, theData);
				
				String userEntry= theData.getParam( theQID + "_" + Integer.toString(i) );		// get each response
				//theData.removeParam( theQID + "_" + Integer.toString(i) );
				
				if (userEntry.length() > 0) 
				{		
					// the question was offered
					int userChoice = -1;
					try 
					{
						userChoice= Integer.parseInt( userEntry.trim() );
					} 
					catch (NumberFormatException e) {}
					
					if (userChoice >= 0) 
					{
						//System.out.println("answered with " + userChoice);
						
						// we answered the question
						userChoice %= 10000;
						userChoice /= 100;
						userChoice = 54 - userChoice;
						
						//System.out.println("adjusted to " + userChoice);
						//System.out.println("totalChoices " + totalChoices);
						//System.out.println("size " + choices.size());
						//System.out.println("i " + i);
						//System.out.println("");

						if ((userChoice >= 0) && (userChoice < totalChoices)) 
						{
							if (userChoice < choices.size())
							{
								String uChoice= (String)choices.elementAt(userChoice);
								uChoice= tooltipDeReference(uChoice);
								uChoice= randomVariable.deReference(uChoice, theTest, sqlID, theData);
								uChoice= richMedia.deReference(uChoice, theHandler, sqlID, theData);
							
								userChoices.put( theMatch, uChoice );
								if (i==userChoice)
								{
									userCorrectness.put( theMatch, "true" );
									
									if (fb_ma.length() > 0)
										answerSpecificFeedback += "<p class=\"maFeedback\">" + fb_ma + "</p>";
								}
								else
								{
									userCorrectness.put( theMatch, "false" );
									
									if (fb_no.length() > 0)
										answerSpecificFeedback += "<p class=\"maFeedback\">" + fb_no + "</p>";
								}
							}
							else
							{
								String uChoice= (String)detractors.elementAt(userChoice-choices.size());
								uChoice= tooltipDeReference(uChoice);
								uChoice= randomVariable.deReference(uChoice, theTest, sqlID, theData);
								uChoice= richMedia.deReference(uChoice, theHandler, sqlID, theData);
							
								userChoices.put( theMatch, uChoice );
								userCorrectness.put( theMatch, "false" );
								
								if (fb_no.length() > 0)
									answerSpecificFeedback += "<p class=\"maFeedback\">" + fb_no + "</p>";
							}
						}
					}
					else
					{
						userCorrectness.put( theMatch, "unanswered" );
					}
				}
			}
		}
		
		//System.out.println("userCorrectness size " + userCorrectness.size());
		//System.out.println("userChoices size " + userChoices.size());
		
		
		String resultHTML= "<div class=\"rspStyle\">";
		
		resultHTML += "<table width=\"50%\" cellpadding=\"0\" cellspacing=\"0\">";
		
		for (int i=0 ; i<matches.size() ; i++) 
		{
			String theMatch= (String)matches.elementAt(i);
			theMatch= tooltipDeReference(theMatch);
			theMatch= randomVariable.deReference(theMatch, theTest, sqlID, theData);
			theMatch= richMedia.deReference(theMatch, theHandler, sqlID, theData);
			
			String thisChoice= (String)scrambledChoices.elementAt(i);
			
			resultHTML += "<tr><td height=\"25\" class=\"matchLeft\">" + Integer.toString( i + 1 ) + ". " + thisChoice + "</td>";
			
			resultHTML += "<td class=\"matchRight\">" + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + theMatch + "&nbsp;&nbsp;</td>";
			
			if(classware_hm.MANUAL_GRADE_R6B.equalsIgnoreCase(theHandler.getParam(classware_hm.CLASSWARE_REQUEST))
					&& hm_grading_r6b.BY_QUESTION.equalsIgnoreCase(theHandler.getParam(hm_grading_r6b.WORK_FLOW))){
			
				resultHTML += "<td class=\"matchLeft\"><select NAME=\"" + theQID + "_" + theSubmission.userID + "_" + Integer.toString(i) + "\">";
			} else {
				resultHTML += "<td class=\"matchLeft\"><select NAME=\"" + theQID + "_" + Integer.toString(i) + "\">";
			}
			
			if (theData == null) 
			{
				resultHTML += "  <OPTION SELECTED VALUE=\"-2-2\">&nbsp;";
				
				for (int j=0 ; j<scrambledIDs.size() ; j++)
					resultHTML += "  <OPTION VALUE=\"" + (String)scrambledIDs.elementAt(j) + "\">" + Integer.toString( j+1 );
			}
			else 
			{
				String oldMatch= (String)userChoices.get( theMatch );
				
				if (oldMatch == null) 
				{
					resultHTML += "  <OPTION SELECTED VALUE=\"-2-2\">&nbsp;";
					
					for (int j=0 ; j<scrambledIDs.size() ; j++)
						resultHTML += "  <OPTION VALUE=\"" + (String)scrambledIDs.elementAt(j) + "\">" + Integer.toString( j+1 );
				}
				else 
				{
					resultHTML += "  <OPTION VALUE=\"-2-2\">&nbsp;";
					
					for (int j=0 ; j<scrambledIDs.size() ; j++) 
					{
						if (oldMatch.equals((String)scrambledChoices.elementAt(j)))
							resultHTML += "  <OPTION SELECTED VALUE=\"" + (String)scrambledIDs.elementAt(j) + "\">" + Integer.toString( j+1 );
						else
							resultHTML += "  <OPTION VALUE=\"" + (String)scrambledIDs.elementAt(j) + "\">" + Integer.toString( j+1 );
					}
					
				}
				
			}
			
			resultHTML += "</select>";
			
				
			String correctness= (String)userCorrectness.get( theMatch );
			if (correctness == null) 
			{
				correctness= "unanswered";
				//System.out.println("null correctness");
			}
			//else System.out.println("correctness " + correctness);
			
			if (pregrading)
			{
				if (correctness.equals("true"))
					resultHTML += "<img class=\"checkMark\" src=\"" + theHandler.baseServer + "/EZTestOnline/Graphics/check.gif\" alt=\"correct\">";
				else if (!correctness.equals("unanswered"))
					resultHTML += "<img class=\"checkMark\" src=\"" + theHandler.baseServer + "/EZTestOnline/Graphics/x.gif\" alt=\"incorrect\">";
			}
			
			else if ((afterTest || indicators) && !previewing && !completeIncompleteGrading())
			{
				if (correctness.equals("true"))
				{
					if (indicators) resultHTML += "<img class=\"checkMark\" src=\"" + theHandler.baseServer + "/EZTestOnline/Graphics/check.gif\" alt=\"correct\">";
				}
				else
				{
					if (indicators) resultHTML += "<img class=\"checkMark\" src=\"" + theHandler.baseServer + "/EZTestOnline/Graphics/x.gif\" alt=\"incorrect\">";
					//Fix for QC#7556
					if (solution || fullFeedback) resultHTML += (String)scrambledAnswers.elementAt(i);
				}
			}
			
			else if (previewing)
				resultHTML += (String)scrambledAnswers.elementAt(i);
			
			resultHTML += "</td></tr>";
		}
		
		for (int i=matches.size() ; i<scrambledChoices.size() ; i++) 
		{
			String thisChoice= (String)scrambledChoices.elementAt(i);
			thisChoice= tooltipDeReference(thisChoice);
			thisChoice= randomVariable.deReference(thisChoice, theTest, sqlID, theData);
			thisChoice= richMedia.deReference(thisChoice, theHandler, sqlID, theData);

			resultHTML += "<tr><td height=\"25\" class=\"matchLeft\">" + Integer.toString( i + 1 ) + ". " + thisChoice + "</td>";
			resultHTML += "<td class=\"matchRight\">&nbsp;</td><td class=\"matchLeft\">&nbsp;</td></tr>";
		}
		
		resultHTML += "</table>";
		
		if (theSubmission != null)
		{
			//if (afterTest)
			boolean feedbackCheck= theTest.getDefaultAction().returnFeedback;
			if (theHandler.isHMrequest()) feedbackCheck= classware_hm.showExplanation(theHandler, this);

			if (theHandler.isManager || feedbackCheck || recallOK)
			{
				//if (answerSpecificFeedbackSupport){
					String theFeedback= "";
					
					if (!questionProperties.getBoolean(USE_COMMON_FEEDBACK, true))
					{
						theFeedback= answerSpecificFeedback + connectSolution(theHandler);
						theFeedback= tooltipDeReference(theFeedback);
						theFeedback= randomVariable.deReference(theFeedback, theTest, sqlID, theData);
						theFeedback= richMedia.deReference(theFeedback, theHandler, sqlID, theData);
					}
					
					if ((theFeedback.trim().length() == 0)  && (defaultFeedback.length() > 0))
					{
						theFeedback= "<p class=\"maFeedback\">" + defaultFeedback + "</p>" + connectSolution(theHandler);
						theFeedback= tooltipDeReference(theFeedback);
						theFeedback= randomVariable.deReference(theFeedback, theTest, sqlID, theData);
						theFeedback= richMedia.deReference(theFeedback, theHandler, sqlID, theData);
					}

					if (theFeedback.trim().length() > 0)
						resultHTML += "<div class=\"feedback_block\">" + theFeedback + "</div>";
				/*}
				
				else if (feedback.size() > 0)
				{
					String theFeedback= "<p class=\"maFeedback\">" + defaultFeedback + "</p>" + connectSolution(theHandler);
					theFeedback= tooltipDeReference(theFeedback);
					theFeedback= randomVariable.deReference(theFeedback, theTest, sqlID, theData);
					theFeedback= richMedia.deReference(theFeedback, theHandler, sqlID, theData);
					if (theFeedback.trim().length() > 0) resultHTML += "<div class=\"feedback_block\">" + theFeedback + "</div>";
				}*/
			}
			
			response theResponse= theSubmission.getResponse(sqlID);
			if ((theResponse != null) && !previewing) resultHTML += showComment( theHandler, theSubmission, theResponse );
		}
				
		resultHTML += supplementaryInfo( theHandler, theSubmission, theData, true );		
			
		resultHTML += "<br></div>";
		
		return(resultHTML);
	}
	
	/**
	 * This method is responsible for rendering a matching question in test/review mode for both student and instructor.
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
		if (choices.size() == 0) return("");
		/**
		 * If the C15 Instructor UI feature is enabled, then v6show_C15 should be invoked for rendering question 
		 */
		Vector enabledFeatureList= tp_utils.listToVector( licenseManager.getFeatureInfo(theHandler, licenseManager.LICENSE_TYPE_HMFEATURE), "," );
		boolean c15InstructorUiEnabled = enabledFeatureList.contains(C15INSTRUCTORUI);
		if (c15InstructorUiEnabled && classware_hm.instructorRole(theHandler.getParam(classware_hm.ROLE))){	    
			return renderItem(theHandler, theTest, partialTO, unusedText, null, null);
		}
		CustomMap<String, String> testParamMap = (CustomMap<String, String>)partialTO.getTestParameter();
		QuestionParameters questionParams = partialTO.getQuestionTO().getQuestionMap().get(sqlID);
		if(questionParams == null){
			questionParams = new QuestionParameters();
			partialTO.getQuestionTO().getQuestionMap().put(sqlID, questionParams);
		}
		
		
		//boolean answerSpecificFeedbackSupport= licenseManager.hasFeature(theHandler, licenseManager.FEATURE_SUPPORT_ASFEEDBACK);
		
		String theQID= "Q_" + sqlID;
		
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
		
		/* nevershow debug
		System.out.println("");
		System.out.println("isManager: " + (theHandler.isManager ? "true":"false"));
		System.out.println("afterTest: " + (afterTest ? "true":"false"));
		System.out.println("pregrading: " + (pregrading ? "true":"false"));
		System.out.println("previewing: " + (previewing ? "true":"false"));
		System.out.println("recallOK: " + (recallOK ? "true":"false"));
		System.out.println("indicators: " + (indicators ? "true":"false"));
		System.out.println("solution: " + (solution ? "true":"false"));
		System.out.println("fullFeedback: " + (fullFeedback ? "true":"false"));
		//*/

		String answerSpecificFeedback= "";
		String defaultFeedback= questionProperties.getString(COMMON_FEEDBACK, null);
		if (defaultFeedback == null)
		{
			defaultFeedback= (String)feedback.elementAt(0);
			int separatorIndex= defaultFeedback.indexOf( fmReturn );
			if (separatorIndex >= 0) 
			{
				if (separatorIndex == 0)
					defaultFeedback= defaultFeedback.substring(1);
				else
					defaultFeedback= defaultFeedback.substring(0,separatorIndex);
			}
		}

		VectorAdapter scrambledChoices= new VectorAdapter();
		VectorAdapter scrambledIDs= new VectorAdapter();
		
		VectorAdapter scrambledAnswers= new VectorAdapter();
		for (int i=0 ; i<matches.size() ; i++) 
			scrambledAnswers.addElement("");
		
		int totalChoices= choices.size() + detractors.size();
		boolean[] chosen= new boolean[ totalChoices ];
		for (int i=0 ; i<totalChoices ; i++)
			chosen[i]= false;
		boolean complete= false;
		
		while ( ! complete ) 
		{
			// select a choice at random
			double theIndex= Math.floor( Math.random() * totalChoices );
			int i= (int)theIndex;
			
			if (!chosen[i]) {	// if we have not already included the choice
				chosen[i]= true;
				
				String theChoice= "unknown";
				if (i < choices.size())
					theChoice= (String)choices.elementAt(i);
				else
					theChoice= (String)detractors.elementAt(i-choices.size());
				
				theChoice= tooltipDeReference(theChoice);
				theChoice= randomVariable.deReference(theChoice, theTest, sqlID, partialTO);
				theChoice= richMedia.deReferenceNew(theChoice, theHandler, sqlID, partialTO);
				
				// calculate the choice selection value
				double top= Math.rint( Math.random() * 100 ) * 10000;
				double bottom= Math.rint( Math.random() * 100);
				int theID= (int)(top + ((54-i)*100) + bottom);
				
				// add it to the vector
				scrambledChoices.addElement( theChoice );
				scrambledIDs.addElement( Integer.toString( theID ) );
				
				if (i < choices.size())
					scrambledAnswers.setElementAt( "&nbsp;&nbsp;<span style=\"color: #004000; font-size: 9pt\">#" + Integer.toString(scrambledChoices.size()) + "</span>", i );

				// see if we've got them all
				complete= true;
				for (int j=0 ; j<totalChoices ; j++)
					complete = complete && chosen[j];
			}
		}
		
		
		ConcurrentHashMap userCorrectness= new ConcurrentHashMap();
		ConcurrentHashMap userChoices= new ConcurrentHashMap();
		if (partialTO != null) 
		{		
			// if the participant has responded
			for (int i=0 ; i<matches.size() ; i++) 
			{
				String fb_ma= "";
				String fb_no= "";
				if (i < feedback.size())
				{
					StringTokenizer theTokens= new StringTokenizer( (String)feedback.elementAt(i), fmReturn );
					if (theTokens.hasMoreTokens()) fb_ma= theTokens.nextToken().trim();
					if (theTokens.hasMoreTokens()) fb_no= theTokens.nextToken().trim();
				}
				
				String theMatch= (String)matches.elementAt(i);
				theMatch= tooltipDeReference(theMatch);
				theMatch= randomVariable.deReference(theMatch, theTest, sqlID, partialTO);
				theMatch= richMedia.deReferenceNew(theMatch, theHandler, sqlID, partialTO);
				
				CustomMap<String, String> questionParamMap = (CustomMap<String, String>)questionParams.getQuestionParameters();
				String userEntry= questionParamMap.getParam( theQID + "_" + Integer.toString(i) );		// get each response
				//theData.removeParam( theQID + "_" + Integer.toString(i) );
				
				if (userEntry.length() > 0) 
				{		
					// the question was offered
					int userChoice = -1;
					try 
					{
						userChoice= Integer.parseInt( userEntry.trim() );
					} 
					catch (NumberFormatException e) {}
					
					if (userChoice >= 0) 
					{
						//System.out.println("answered with " + userChoice);
						
						// we answered the question
						userChoice %= 10000;
						userChoice /= 100;
						userChoice = 54 - userChoice;
						
						//System.out.println("adjusted to " + userChoice);
						//System.out.println("totalChoices " + totalChoices);
						//System.out.println("size " + choices.size());
						//System.out.println("i " + i);
						//System.out.println("");

						if ((userChoice >= 0) && (userChoice < totalChoices)) 
						{
							if (userChoice < choices.size())
							{
								String uChoice= (String)choices.elementAt(userChoice);
								uChoice= tooltipDeReference(uChoice);
								uChoice= randomVariable.deReference(uChoice, theTest, sqlID, partialTO);
								uChoice= richMedia.deReferenceNew(uChoice, theHandler, sqlID, partialTO);
							
								userChoices.put( theMatch, uChoice );
								if (i==userChoice)
								{
									userCorrectness.put( theMatch, "true" );
									
									if (fb_ma.length() > 0)
										answerSpecificFeedback += "<p class=\"maFeedback\">" + fb_ma + "</p>";
								}
								else
								{
									userCorrectness.put( theMatch, "false" );
									
									if (fb_no.length() > 0)
										answerSpecificFeedback += "<p class=\"maFeedback\">" + fb_no + "</p>";
								}
							}
							else
							{
								String uChoice= (String)detractors.elementAt(userChoice-choices.size());
								uChoice= tooltipDeReference(uChoice);
								uChoice= randomVariable.deReference(uChoice, theTest, sqlID, partialTO);
								uChoice= richMedia.deReferenceNew(uChoice, theHandler, sqlID, partialTO);
							
								userChoices.put( theMatch, uChoice );
								userCorrectness.put( theMatch, "false" );
								
								if (fb_no.length() > 0)
									answerSpecificFeedback += "<p class=\"maFeedback\">" + fb_no + "</p>";
							}
						}
					}
					else
					{
						userCorrectness.put( theMatch, "unanswered" );
					}
				}
			}
		}
		
		//System.out.println("userCorrectness size " + userCorrectness.size());
		//System.out.println("userChoices size " + userChoices.size());
		
		
		String resultHTML= "<div class=\"rspStyle\">";
		
		resultHTML += "<table width=\"50%\" cellpadding=\"0\" cellspacing=\"0\">";
		
		for (int i=0 ; i<matches.size() ; i++) 
		{
			String theMatch= (String)matches.elementAt(i);
			theMatch= tooltipDeReference(theMatch);
			theMatch= randomVariable.deReference(theMatch, theTest, sqlID, partialTO);
			theMatch= richMedia.deReferenceNew(theMatch, theHandler, sqlID, partialTO);
			
			String thisChoice= (String)scrambledChoices.elementAt(i);
			
			resultHTML += "<tr><td height=\"25\" class=\"matchLeft\">" + Integer.toString( i + 1 ) + ". " + thisChoice + "</td>";
			
			resultHTML += "<td class=\"matchRight\">" + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + theMatch + "&nbsp;&nbsp;</td>";
			
			if(classware_hm.MANUAL_GRADE_R6B.equalsIgnoreCase(theHandler.getParam(classware_hm.CLASSWARE_REQUEST))
					&& hm_grading_r6b.BY_QUESTION.equalsIgnoreCase(theHandler.getParam(hm_grading_r6b.WORK_FLOW))){
				
				resultHTML += "<td class=\"matchLeft\"><select NAME=\"" + theQID + "_" + partialTO.getStudentID() + "_" + Integer.toString(i) + "\">";
			} else {
				resultHTML += "<td class=\"matchLeft\"><select NAME=\"" + theQID + "_" + Integer.toString(i) + "\">";
			}
			
			if (partialTO == null) 
			{
				resultHTML += "  <OPTION SELECTED VALUE=\"-2-2\">&nbsp;</OPTION>";
				
				for (int j=0 ; j<scrambledIDs.size() ; j++)
					resultHTML += "  <OPTION VALUE=\"" + (String)scrambledIDs.elementAt(j) + "\">" + Integer.toString( j+1 ) + "</OPTION>";
			}
			else 
			{
				String oldMatch= (String)userChoices.get( theMatch );
				
				if (oldMatch == null) 
				{
					resultHTML += "  <OPTION SELECTED VALUE=\"-2-2\">&nbsp;</OPTION>";
					
					for (int j=0 ; j<scrambledIDs.size() ; j++)
						resultHTML += "  <OPTION VALUE=\"" + (String)scrambledIDs.elementAt(j) + "\">" + Integer.toString( j+1 ) + "</OPTION>";
				}
				else 
				{
					resultHTML += "  <OPTION VALUE=\"-2-2\">&nbsp;</OPTION>";
					
					for (int j=0 ; j<scrambledIDs.size() ; j++) 
					{
						if (oldMatch.equals((String)scrambledChoices.elementAt(j)))
							resultHTML += "  <OPTION SELECTED VALUE=\"" + (String)scrambledIDs.elementAt(j) + "\">" + Integer.toString( j+1 ) + "</OPTION>";
						else
							resultHTML += "  <OPTION VALUE=\"" + (String)scrambledIDs.elementAt(j) + "\">" + Integer.toString( j+1 ) + "</OPTION>";
					}
					
				}
				
			}
			
			resultHTML += "</select>";
			
				
			String correctness= (String)userCorrectness.get( theMatch );
			if (correctness == null) 
			{
				correctness= "unanswered";
				//System.out.println("null correctness");
			}
			//else System.out.println("correctness " + correctness);
			
			if (pregrading)
			{
				if (correctness.equals("true"))
					resultHTML += "<img class=\"checkMark\" src=\"" + theHandler.baseServer + "/EZTestOnline/Graphics/check.gif\" alt=\"correct\">";
				else if (!correctness.equals("unanswered"))
					resultHTML += "<img class=\"checkMark\" src=\"" + theHandler.baseServer + "/EZTestOnline/Graphics/x.gif\" alt=\"incorrect\">";
			}			
			
			else if ((afterTest || indicators) && !previewing && !completeIncompleteGrading())
			{
				if (correctness.equals("true"))
				{
					if (indicators) resultHTML += "<img class=\"checkMark\" src=\"" + theHandler.baseServer + "/EZTestOnline/Graphics/check.gif\" alt=\"correct\">";
				}
				else
				{
					if (indicators) resultHTML += "<img class=\"checkMark\" src=\"" + theHandler.baseServer + "/EZTestOnline/Graphics/x.gif\" alt=\"incorrect\">";
					//Fix for QC#7556
					if (solution || fullFeedback) resultHTML += (String)scrambledAnswers.elementAt(i);
				}
			}
			
			else if (previewing)
				resultHTML += (String)scrambledAnswers.elementAt(i);		
			
			resultHTML += "</td></tr>";
		}
		
		for (int i=matches.size() ; i<scrambledChoices.size() ; i++) 
		{
			String thisChoice= (String)scrambledChoices.elementAt(i);
			thisChoice= tooltipDeReference(thisChoice);
			thisChoice= randomVariable.deReference(thisChoice, theTest, sqlID, partialTO);
			thisChoice= richMedia.deReferenceNew(thisChoice, theHandler, sqlID, partialTO);

			resultHTML += "<tr><td height=\"25\" class=\"matchLeft\">" + Integer.toString( i + 1 ) + ". " + thisChoice + "</td>";
			resultHTML += "<td class=\"matchRight\">&nbsp;</td><td class=\"matchLeft\">&nbsp;</td></tr>";
		}
		
		resultHTML += "</table>";
		
		if (partialTO.isSubmission())
		{
			//if (afterTest)
			boolean feedbackCheck= theTest.getDefaultAction().returnFeedback;
			if (theHandler.isHMrequest()) feedbackCheck= classware_hm.showExplanation(theHandler, this);

			if (theHandler.isManager || feedbackCheck || recallOK)
			{
				//if (answerSpecificFeedbackSupport){
					String theFeedback= "";
					
					if (!questionProperties.getBoolean(USE_COMMON_FEEDBACK, true))
					{
						theFeedback= answerSpecificFeedback + connectSolution(theHandler);
						theFeedback= tooltipDeReference(theFeedback);
						theFeedback= randomVariable.deReference(theFeedback, theTest, sqlID, partialTO);
						theFeedback= richMedia.deReferenceNew(theFeedback, theHandler, sqlID, partialTO);
					}
					
					if ((theFeedback.trim().length() == 0)  && (defaultFeedback.length() > 0))
					{
						theFeedback= "<p class=\"maFeedback\">" + defaultFeedback + "</p>" + connectSolution(theHandler);
						theFeedback= tooltipDeReference(theFeedback);
						theFeedback= randomVariable.deReference(theFeedback, theTest, sqlID, partialTO);
						theFeedback= richMedia.deReferenceNew(theFeedback, theHandler, sqlID, partialTO);
					}

					if (theFeedback.trim().length() > 0)
						resultHTML += "<div class=\"feedback_block\">" + theFeedback + "</div>";
			//	}
				
				/*else if (feedback.size() > 0)
				{
					String theFeedback= "<p class=\"maFeedback\">" + defaultFeedback + "</p>" + connectSolution(theHandler);
					theFeedback= tooltipDeReference(theFeedback);
					theFeedback= randomVariable.deReference(theFeedback, theTest, sqlID, partialTO);
					theFeedback= richMedia.deReferenceNew(theFeedback, theHandler, sqlID, partialTO);
					if (theFeedback.trim().length() > 0) resultHTML += "<div class=\"feedback_block\">" + theFeedback + "</div>";
				}*/
			}
			
			/*response theResponse= theSubmission.getResponse(sqlID);
			if ((theResponse != null) && !previewing) resultHTML += showComment( theHandler, theSubmission, theResponse );*/
			if ((questionParams != null) && !previewing) resultHTML += showComment( theHandler, questionParams );
			
			
		}
				
		resultHTML += supplementaryInfo( theHandler, partialTO, true );		
			
		resultHTML += "<br></div>";
		
		return(resultHTML);
	}

	/**
	 * This method generates the default feedback for a matching question
	 * @return String feedback
	 */
	private String getDefaultFeedback(){
		String defaultFeedback= questionProperties.getString(COMMON_FEEDBACK, null);
		if (defaultFeedback == null)
		{
			defaultFeedback= (String)feedback.elementAt(0);
			int separatorIndex= defaultFeedback.indexOf( fmReturn );
			if (separatorIndex >= 0){
				if (separatorIndex == 0)
					defaultFeedback= defaultFeedback.substring(1);
				else
					defaultFeedback= defaultFeedback.substring(0,separatorIndex);
			}
		}
		return defaultFeedback;
	}
		
	/**
	 * This method generates the answer choices for a matching question 
	 * @param theHandler
	 * @param theTest
	 * @param partialTO student response
	 * @param theData student response
	 * @return
	 */
	public Map<String, Object> generateChoices(tp_requestHandler theHandler, test theTest, PartialTO partialTO, parameters theData){
		List<String> scrambledChoices= new ArrayList<String>();
		List<String> scrambledIDs= new ArrayList<String>();
		List<String> scrambledAnswers= new ArrayList<String>();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		for (int i=0 ; i<matches.size() ; i++){ 
			scrambledAnswers.add("");
		}
		
		int totalChoices= choices.size() + detractors.size();
		boolean[] chosen= new boolean[ totalChoices ];
		for (int i=0 ; i<totalChoices ; i++){
			chosen[i]= false;
		}
		boolean complete= false;
		
		while (!complete){
			// select a choice at random
			double theIndex= Math.floor( Math.random() * totalChoices );
			int i= (int)theIndex;
			
			//if we have not already included the choice
			if (!chosen[i]) { 
				chosen[i]= true;
				
				String theChoice= "unknown";
				if (i < choices.size()){
					theChoice= (String)choices.elementAt(i);
				}else{
					theChoice= (String)detractors.elementAt(i-choices.size());
				}
				theChoice= dereference(theChoice, theTest, theHandler, partialTO, theData);
				// calculate the choice selection value
				double top= Math.rint( Math.random() * 100 ) * 10000;
				double bottom= Math.rint( Math.random() * 100);
				int theID= (int)(top + ((54-i)*100) + bottom);
				
				// add it to the vector
				scrambledChoices.add( theChoice );
				scrambledIDs.add(Integer.toString(theID));

				if (i < choices.size()){
					scrambledAnswers.set(i, Integer.toString(scrambledChoices.size()));
				}
				// see if we've got them all
				complete= true;
				for (int j=0 ; j<totalChoices ; j++){
					complete = complete && chosen[j];
				}
			}
		}

		resultMap.put("scrambledChoices", scrambledChoices);
		resultMap.put("scrambledIDs", scrambledIDs);
		resultMap.put("scrambledAnswers", scrambledAnswers);
		return resultMap;
	}

	/**
	 * This method generates the following user responses details
	 * 1. user response
	 * 2. user response correctness
	 * 3. feedback based on user response correctness
	 * 
	 * @param theHandler
	 * @param theTest
	 * @param partialTO 	student response
	 * @param theData  		student response
	 * @return
	 */
	private Map<String, Object> getUserResponses(tp_requestHandler theHandler, test theTest, PartialTO partialTO, parameters theData){
		QuestionParameters questionParams = partialTO.getQuestionTO().getQuestionMap().get(sqlID);
		Map<String, String> userCorrectness= new HashMap<String, String>();
		Map<String, String> userChoices= new HashMap<String, String>();
		List<String> answerSpecificFeedbackList = new ArrayList<String>();
		List<String> matchesList = new ArrayList<String>();
		Map<String, Object> resultMap = new HashMap<String, Object>();

		String theQID= "Q_" + sqlID;
		int totalChoices = choices.size() + detractors.size();

		if ((theTest.isNewTest && partialTO != null) || (theData != null)){
			// if the participant has responded
			for (int i=0 ; i<matches.size() ; i++){
				String correctFeedback = "";
				String inCorrectFeedback = "";
				if (i < feedback.size()){
					StringTokenizer theTokens= new StringTokenizer( (String)feedback.elementAt(i), fmReturn );
					if (theTokens.hasMoreTokens()){
						correctFeedback= theTokens.nextToken().trim();
					}
					if (theTokens.hasMoreTokens()){
						inCorrectFeedback= theTokens.nextToken().trim();
					}
				}

				String theMatch= (String)matches.elementAt(i);
				theMatch= dereference(theMatch, theTest, theHandler, partialTO, theData);
				matchesList.add(theMatch);

				String userEntry = "";
				if (theTest.isNewTest && partialTO != null){
					CustomMap<String, String> questionParamMap = (CustomMap<String, String>)questionParams.getQuestionParameters();
					userEntry= questionParamMap.getParam( theQID + "_" + Integer.toString(i) );		// get each response
				}else if(theData != null){
					userEntry= theData.getParam( theQID + "_" + Integer.toString(i) );
				}

				if (StringUtils.isNotBlank(userEntry)) 
				{		
					// the question was offered
					int userChoice = -1;
					try 
					{
						userChoice= Integer.parseInt( userEntry.trim() );
					} 
					catch (NumberFormatException e) {}

					if (userChoice >= 0) 
					{
						// we answered the question
						userChoice %= 10000;
						userChoice /= 100;
						userChoice = 54 - userChoice;

						if ((userChoice >= 0) && (userChoice < totalChoices)) 
						{
							if (userChoice < choices.size())
							{
								String uChoice= (String)choices.elementAt(userChoice);
								uChoice= dereference(uChoice, theTest, theHandler, partialTO, theData);

								userChoices.put(theMatch, uChoice );
								if (i==userChoice)
								{
									userCorrectness.put(theMatch, "true" );

									if (correctFeedback.length() > 0){
										//answerSpecificFeedback += "<p class=\"maFeedback\">" + fb_ma + "</p>";
										correctFeedback = dereference(correctFeedback, theTest, theHandler, partialTO, theData);
										answerSpecificFeedbackList.add(correctFeedback);
									}
								}
								else
								{
									userCorrectness.put(theMatch, "false" );

									if (inCorrectFeedback.length() > 0){
										//answerSpecificFeedback += "<p class=\"maFeedback\">" + fb_no + "</p>";
										inCorrectFeedback = dereference(inCorrectFeedback, theTest, theHandler, partialTO, theData);
										answerSpecificFeedbackList.add(inCorrectFeedback);
									}
								}
							}
							else
							{
								String uChoice= (String)detractors.elementAt(userChoice-choices.size());
								uChoice= dereference(uChoice, theTest, theHandler, partialTO, theData);

								userChoices.put(theMatch, uChoice );
								userCorrectness.put(theMatch, "false" );

								if (inCorrectFeedback.length() > 0){
									//answerSpecificFeedback += "<p class=\"maFeedback\">" + fb_no + "</p>";
									inCorrectFeedback = dereference(inCorrectFeedback, theTest, theHandler, partialTO, theData);
									answerSpecificFeedbackList.add(inCorrectFeedback);
								}
							}
						}
					}
					else
					{
						userCorrectness.put(theMatch, "unanswered" );
					}
				}
			}
		}

		resultMap.put("userCorrectness", userCorrectness);
		resultMap.put("userChoices", userChoices);
		resultMap.put("feedbackList", answerSpecificFeedbackList);
		resultMap.put("matches", matchesList);

		return resultMap;
	}

	/**
	 * This method replaces any reference to tool-tip, random variable or media
	 * with the actual instances in the input string
	 * 
	 * @param input 		the input string where references will be replaced
	 * @param theTest
	 * @param theHandler
	 * @param partialTO 	student response
	 * @param theData  		student response
	 * @return
	 */
	private String dereference(String input, test theTest, tp_requestHandler theHandler, PartialTO partialTO, parameters theData){
		input = tooltipDeReference(input);
		if(theTest.isNewTest){
			input = randomVariable.deReference(input, theTest, sqlID, partialTO);
			input = richMedia.deReferenceNew(input, theHandler, sqlID, partialTO);
		}else{
			input = randomVariable.deReference(input, theTest, sqlID, theData);
			input = richMedia.deReference(input, theHandler, sqlID, theData);
		}

		return input;
	}

	/**
	 * This method populates all the relevant policies to render a matching question
	 *  
	 * @param theHandler
	 * @param theTest
	 * @param partialTO		student response
	 * @param theSubmission	student response
	 * @param theData
	 * @return
	 */
	private Map<String, Boolean> populatePolicies(tp_requestHandler theHandler, test theTest, PartialTO partialTO, submission theSubmission, parameters theData){
		boolean recallOK= false;
		boolean postSubmission= ((theTest.isNewTest && partialTO.isSubmission()) || (theSubmission != null));

		if (postSubmission && theTest.getDefaultAction().feedbackOnRecall && (theTest.getSecurity().recallTime > 0))
		{
			long now= (new java.util.Date()).getTime();
			recallOK= (now > theTest.getSecurity().recallTime);
		}

		CustomMap<String, String> testParamMap = null;
		if(theTest.isNewTest){
			testParamMap = (CustomMap<String, String>)partialTO.getTestParameter();
		}

		String mode = null;
		if(theTest.isNewTest){
			mode = testParamMap.getParam("mode");
		}else{
			mode = theData.getParam("mode");
		}
		boolean previewing= "preview".equals(mode);

		boolean afterTest= postSubmission && (theHandler.isManager || theTest.getDefaultAction().returnFeedback || recallOK);
		boolean pregrading= theHandler.getParam(v7test_student.PREGRADE).equals(sqlID) && (theTest.getGUI().allowPregrade || theHandler.isHMrequest());

		String pIndicator = "";
		String pSolution = "";
		if(theTest.isNewTest){
			pIndicator = policies.getValNew(classware_hm.POLICY_indicators, null, theHandler, partialTO, false);
			pSolution = policies.getValNew(classware_hm.POLICY_solution, null, theHandler, partialTO);
		}else{
			pIndicator = policies.getVal(classware_hm.POLICY_indicators, null, theHandler, theSubmission, false);
			pSolution = policies.getVal(classware_hm.POLICY_solution, null, theHandler, theSubmission);
		}
		boolean indicators= !completeIncompleteGrading() && !pregrading && postSubmission && policies.connectBoolean(pIndicator, afterTest);
		boolean solution= policies.connectBoolean(pSolution, !theHandler.isHMrequest());

		boolean fullFeedback= theHandler.getParam(classware_hm.POLICY_grading).equals(classware_hm.POLICY_grading_feedback);

		// QCQA#7482, if we're taking the test
		if (!theHandler.getParam(classware_hm.POST_SUBMISSION).equals(classware_hm.POST_SUBMISSION) && theHandler.isHMrequest())
		{
			// and in feedback between questions and shoing feedback, also show indicators
			if (theHandler.getParam(classware_hm.POLICY_feedback).equals("yes") && (theHandler.getParam(classware_hm.POLICY_feedback_state).equals(classware_hm.POLICY_feedback_SHOWING)))
				indicators= true;
		}

		if (pregrading){
			indicators= true;
		}

		Map<String, Boolean> policyMap = new HashMap<String, Boolean>();

		if(classware_hm.MANUAL_GRADE_R6B.equalsIgnoreCase(theHandler.getParam(classware_hm.CLASSWARE_REQUEST))
				&& hm_grading_r6b.BY_QUESTION.equalsIgnoreCase(theHandler.getParam(hm_grading_r6b.WORK_FLOW))){

			policyMap.put("mangradeByQuestion", true);
		}
		policyMap.put("pregrading", pregrading);
		policyMap.put("afterTest", afterTest);
		policyMap.put("indicators", indicators);
		policyMap.put("previewing", previewing);
		policyMap.put("completeIncompleteGrading", completeIncompleteGrading());
		policyMap.put("solution", solution);
		policyMap.put("fullFeedback", fullFeedback);
		policyMap.put("previewing", previewing);
		policyMap.put("recallOK", recallOK);
		policyMap.put("newTest", theTest.isNewTest);

		return policyMap;
	}

	/**
	 * This method is responsible for rendering a matching question in test/review mode for both student and instructor.
	 * 1. In test mode, only the question content is rendered.
	 * 2. In review mode, student response along with correct/incorrect indicator, score,feedback, solution are rendered based on the attempt policy
	 * 
	 * Following changes are done for Instructor UI changes
	 * 1. Restructuring java code to separate business and HTML generation logic
	 * 2. Change table structure to div structure
	 * 3. Externalize CSS
	 *   
	 * @param theHandler
	 * @param theTest - the test object
	 * @param partialTO representing student response
	 * @param unusedText 
	 */
	private String renderItem(tp_requestHandler theHandler, test theTest, PartialTO partialTO, String unusedText, submission theSubmission, parameters theData){
		if (choices.size() == 0){
			return("");
		}
		/**
		 * Added required js and css files
		 */
		TestPilot4.cssFile( theHandler, "/EZTestOnline/paamUI/css/matching-ranking.css");
		TestPilot4.jsFile( theHandler, "/EZTestOnline/paamUI/js/matching-ranking.js");

		QuestionParameters questionParams = null;
		if(theTest.isNewTest){
			questionParams = partialTO.getQuestionTO().getQuestionMap().get(sqlID);
			if(questionParams == null){
				questionParams = new QuestionParameters();
				partialTO.getQuestionTO().getQuestionMap().put(sqlID, questionParams);
			}
		}

		String defaultFeedback= getDefaultFeedback();

		Map<String, Object> choicesMap = generateChoices(theHandler, theTest, partialTO, theData);		
		Map<String, Boolean> policyMap = populatePolicies(theHandler, theTest, partialTO, theSubmission, theData);
		boolean recallOK = policyMap.get("recallOK");

		Map<String, Object> userResponsesMap = getUserResponses(theHandler, theTest, partialTO, theData);

		String problemSolution = connectSolution_C15(theHandler);
		problemSolution = dereference(problemSolution, theTest, theHandler, partialTO, theData);
		userResponsesMap.put("connectSolution", problemSolution);

		List<String> answerSpecificFeedbackList = (List<String>)userResponsesMap.get("feedbackList");
		if (partialTO.isSubmission()){
			boolean feedbackCheck= theTest.getDefaultAction().returnFeedback;
			if (theHandler.isHMrequest()){
				feedbackCheck= classware_hm.showExplanation(theHandler, this);
			}
			policyMap.put("feedbackCheck", feedbackCheck);

			if (theHandler.isManager || feedbackCheck || recallOK){
				if (!questionProperties.getBoolean(USE_COMMON_FEEDBACK, true)){
					answerSpecificFeedbackList = new ArrayList<String>();
				}

				if ((answerSpecificFeedbackList == null || answerSpecificFeedbackList.size() == 0)  && (StringUtils.isNotBlank(defaultFeedback))){
					if(answerSpecificFeedbackList == null){
						answerSpecificFeedbackList = new ArrayList<String>(); 
					}
					defaultFeedback = dereference(defaultFeedback, theTest, theHandler, partialTO, theData);
					answerSpecificFeedbackList.add(defaultFeedback);
				}
			}
		}
		userResponsesMap.put("feedbackList", answerSpecificFeedbackList);

		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.putAll(choicesMap);
		resultMap.putAll(userResponsesMap);
		resultMap.putAll(policyMap);
		resultMap.put("questionParams", questionParams);
		resultMap.put("studentId", partialTO.getStudentID());
		return renderHtml(theHandler, resultMap, partialTO, theSubmission, theData);
	}

	/**
	 * This method is responsible for showing html for question rendering for C15
	 * 
	 * @param theHandler {@link tp_requestHandler} that is processing the current request.
	 * @param valueMap A custom <code>Map</code> Object containing required parameters
	 * @param partialTO A Custom Object containing User response for new Test
	 * @param theSubmission A Custom Object containing User response for Old Test
	 * @param theData requst parameters
	 * @return A String Object representing question HTML
	 */
	public String renderHtml(tp_requestHandler theHandler, 
								 Map<String, Object> valueMap, 
								 PartialTO partialTO, 
								 submission theSubmission, 
								 parameters theData){
		StringBuilder resultHTML= new StringBuilder("");
		if(valueMap == null || valueMap.isEmpty()){
			return resultHTML.toString();
		}
		try{
			//Get all the required values from valueMap Object
			List<String> matchesList = (List<String>)valueMap.get("matches");
			List<String> scrambledAnswers = (List<String>)valueMap.get("scrambledAnswers");
			String studentId = (String)valueMap.get("studentId");
			List<String> scrambledIDs = (List<String>)valueMap.get("scrambledIDs");
			List<String> scrambledChoices = (List<String>)valueMap.get("scrambledChoices");
			Map<String, String> userChoices = (Map<String, String>)valueMap.get("userChoices");
			Map<String, String> userCorrectness = (Map<String, String>)valueMap.get("userCorrectness");
			List<String> feedbackList = (List<String>)valueMap.get("feedbackList");
			String connectSolution = (String)valueMap.get("connectSolution");
			QuestionParameters questionParams = null; 
			if(valueMap.get("questionParams") != null){
				questionParams = (QuestionParameters)valueMap.get("questionParams");
			}
			response theResponse= null;
			if(valueMap.get("theResponse") != null){
				theResponse = (response)valueMap.get("theResponse");
			}
			boolean isManGrade = getBooleanValue(valueMap, "mangradeByQuestion");
			boolean afterTest = getBooleanValue(valueMap, "afterTest");
			boolean previewing = getBooleanValue(valueMap, "previewing");
			boolean isNewTest = getBooleanValue(valueMap, "newTest");

			//Start constucting question HTML
			resultHTML.append("<div class=\"rspStyle\">");
			String theQID= "Q_" + sqlID;
			
			//This piece of code required for showing choices
			resultHTML.append(generateChoiceHtml(scrambledChoices));
			
			//It is for showing feedback of the question(Yet to be decided by UX) 
			resultHTML.append("<div class=\"matching-ranking__body__options-text\">").append("").append("</div>");
			
			/**
			 * This piece of code required to show matches and correct incorrect indicators and feedback
			 *  in case of previewing, post submission view
			 */
			if(matchesList != null && !matchesList.isEmpty()){
				for(int i=0; i< matchesList.size(); i++) {
					
					String selectName = null;
					String theMatch= matchesList.get(i);
					if(isManGrade){
						selectName = new StringBuilder(theQID).append("_").append(studentId).append("_").append(Integer.toString(i)).toString();
					}else{
						selectName = new StringBuilder(theQID).append("_").append(Integer.toString(i)).toString();
					}
					
					//This piece of code required for getting pre-selected answer choices (if exist)
					String selectValue = "";
					if(partialTO != null){
						String oldMatch = (String)userChoices.get(theMatch);
						if(StringUtils.isNotBlank(oldMatch)){
							for (int j=0 ; j<scrambledIDs.size() ; j++){
								if (oldMatch.equals((String)scrambledChoices.get(j))){
									selectValue = Integer.toString( j+1 );
								}
							}
						}
					}
					
					//Fetching the rendering mode for Instructor Preview & Add Question Mode
					String renderingMode = getRenderingMode(theHandler);
					
					//We are rendering different HTML views based on rendering mode
					// Instructor Preview Mode
					if(INSTRUCTOR_PREVIEW_MODE.equalsIgnoreCase(renderingMode)){
						resultHTML.append(renderPreview(theMatch, (String) scrambledAnswers.get(i), scrambledIDs));
					}
					// Add Question Mode
					else if (ADD_QUESTION_MODE.equalsIgnoreCase(renderingMode)) {
						resultHTML.append(renderAddQuestion(theMatch, (String) scrambledAnswers.get(i), scrambledIDs));
					}
					// Post Submission Mode
					else if (afterTest) {
						resultHTML.append(renderPostSubmission(theMatch, (String) scrambledAnswers.get(i), selectValue, scrambledIDs, i, userCorrectness));
					}
				}
				resultHTML.append("</div>");
				/**
				 * This piece of code responsible for showing feedback for questions in post submission, report view 
				 */
				if(isNewTest){
					if (partialTO.isSubmission()){
						String theFeedback = showFeedBackForQuestion(feedbackList, connectSolution);
						if(StringUtils.isNotBlank(theFeedback)){
							resultHTML.append("<div class=\"feedback_block\">" + theFeedback + "</div>");
						}
						if ((questionParams != null) && !previewing){
							resultHTML.append(showComment( theHandler, questionParams));
						}
					}
					resultHTML.append(supplementaryInfo( theHandler, partialTO, true ));
				}else{
					if(theSubmission != null){
						String theFeedback = showFeedBackForQuestion(feedbackList, connectSolution);
						if(StringUtils.isNotBlank(theFeedback)){
							resultHTML.append("<div class=\"feedback_block\">" + theFeedback + "</div>");
						}
						if ((questionParams != null) && !previewing){
							resultHTML.append(showComment( theHandler, theSubmission, theResponse));
						}
					}
					resultHTML.append(supplementaryInfo( theHandler, theSubmission, theData, true ));
				}
			}
		}catch(Exception ex){
			_logger.error("Problem in rendering HTML ",ex);
		}
		return resultHTML.toString();
	}

	/**
	 *  This method is for rendering Instructor Preview view
	 * and contains all the Html content
	 * @param theMatch
	 * @param scrambledAnswer
	 * @param scrambledIDs
	 * @return
	 */
	public StringBuilder renderPreview (String theMatch, String scrambledAnswer, List<String> scrambledIDs) {

		StringBuilder resultHTML = new StringBuilder("");

		resultHTML.append("<div class=\"matching-ranking__row question--matching matching-ranking__row--width-medium\">");
		resultHTML.append("<div class=\"matching-ranking__cell matching-ranking__cell--dropdown-right\">");
		resultHTML.append("<ul class=\"no-style-ul\"> ");
		resultHTML.append("<li class=\"matching-ranking__content--width\">");
		resultHTML.append("<a href=\"javascript:void(0);\" class=\"matching-ranking__cell--select-option is-checked matching-ranking__cell no-underline\" data-typeval=\"question--matching\"> ");
		resultHTML.append("<span class=\"matching-ranking__selected\"></span>");
		resultHTML.append("<span class=\"single-on-click icon-dropdown icon-dropdown-style show\"></span>");
		resultHTML.append("</a>");
		resultHTML.append("<div class=\"matching-ranking__selected-option\">");
		resultHTML.append(scrambledAnswer);
		resultHTML.append("</div>");
		resultHTML.append("<div class=\"matching-ranking__cell matching-ranking__correct-answer\">Correct: ");
		resultHTML.append(scrambledAnswer);
		resultHTML.append("</div>");
		resultHTML.append("<div class=\"matching-ranking__options-wrap\"> ");
		resultHTML.append("<div class=\"matching-ranking__options__triangle\"></div> ");
		resultHTML.append("<div class=\"matching-ranking__options\">");
		resultHTML.append("<ul class=\"no-style-ul\">");
		/**
		 * Here we are showing the drop down elements
		 */
		resultHTML.append("<li class=\"matching-ranking__option\" data-optionval=\"-2-2\">NO ANSWER</li>");
		for (int j = 0 ; j < scrambledIDs.size(); j++) {
			resultHTML.append("<li class=\"matching-ranking__option\")");
			resultHTML.append(" data-optionval=\"").append((String)scrambledIDs.get(j)).append("\">");
			resultHTML.append(Integer.toString(j+1));
			resultHTML.append("</li>");
		}

		resultHTML.append("</ul>");
		resultHTML.append("</div>");
		resultHTML.append("</div>");
		resultHTML.append("</li>");
		resultHTML.append("</ul>");
		resultHTML.append("</div>");
		resultHTML.append("<div class=\"matching-ranking__cell font-bold matching-ranking__cell--text-width\">");
		resultHTML.append(theMatch);
		resultHTML.append("</div>");
		resultHTML.append("</div>");

		return resultHTML;
	}
	
	/**
	 * This method is for rendering Post Submission view and corresponding HTML contents
	 * @param theMatch
	 * @param scrambledAnswer
	 * @param selectValue
	 * @param scrambledIDs
	 * @param currentIndex
	 * @param userCorrectness
	 * @return
	 */
	public String renderPostSubmission(String theMatch, String scrambledAnswer,	String selectValue, List<String> scrambledIDs, int currentIndex, Map<String, String> userCorrectness) {

		String correctness = (String) userCorrectness.get(theMatch);
		
		if (StringUtils.isBlank(correctness)){
			correctness = "unanswered";
		}
		
		boolean isCorrect = correctness.equals("true");
		boolean isUnChecked = correctness.equals("unanswered");
		StringBuilder resultHTML = new StringBuilder("");

		resultHTML.append("<div class=\"matching-ranking__row question--matching matching-ranking__row--width-medium\">");
		resultHTML.append("<div class=\"matching-ranking__cell matching-ranking__cell--dropdown-right\">");
		resultHTML.append("<ul class=\"no-style-ul\"> ");
		resultHTML.append("<li class=\"matching-ranking__content--width\">");
		resultHTML.append("<a href=\"javascript:void(0);\" class=\"matching-ranking__cell--select-option ");
		resultHTML.append(isUnChecked? "is-unchecked": "is-checked");
		resultHTML.append(" matching-ranking__cell no-underline");
		resultHTML.append(" matching-ranking__cell--select-option--").append(isCorrect? "correct": "wrong");
		resultHTML.append("\" data-typeval=\"question--matching\"> ");
		resultHTML.append("<span class=\"matching-ranking__selected\"></span>");
		resultHTML.append("<span class=\"single-on-click icon-dropdown icon-dropdown-style show\"></span>");
		resultHTML.append("</a>");

		if (isUnChecked) {
			resultHTML.append("<div class=\"matching-ranking__no-answer\"></div>");
		} 
		else {
			resultHTML.append("<div class=\"matching-ranking__selected-option\">");
			resultHTML.append(selectValue);
			resultHTML.append("</div>");
		}

		resultHTML.append("<div class=\"matching-ranking__cell matching-ranking__correct-answer\">Correct: ");
		resultHTML.append(scrambledAnswer);
		resultHTML.append("</div>");
		resultHTML.append("<div class=\"matching-ranking__options-wrap\"> ");
		resultHTML.append("<div class=\"matching-ranking__options__triangle\"></div> ");
		resultHTML.append("<div class=\"matching-ranking__options\">");
		resultHTML.append("<ul class=\"no-style-ul\">");
		/**
		 * Here we are showing the drop down elements
		 */
		resultHTML.append("<li class=\"matching-ranking__option\" data-optionval=\"-2-2\">NO ANSWER</li>");
		for (int j = 0 ; j < scrambledIDs.size(); j++) {
			resultHTML.append("<li class=\"matching-ranking__option\")");
			resultHTML.append(" data-optionval=\"").append((String)scrambledIDs.get(j)).append("\">");
			resultHTML.append(Integer.toString(j+1));
			resultHTML.append("</li>");
		}

		resultHTML.append("</ul>");
		resultHTML.append("</div>");
		resultHTML.append("</div>");
		resultHTML.append("</li>");
		resultHTML.append("</ul>");
		resultHTML.append("</div>");
		resultHTML.append("<div class=\"matching-ranking__cell font-bold matching-ranking__cell--text-width\">");
		resultHTML.append(theMatch);
		resultHTML.append("</div>");
		resultHTML.append("</div>");

		return resultHTML.toString();
	}
	
	/**
	 * This method is responsible for rendering Add Question View
	 * and contains the HTNL contents
	 * @param theMatch
	 * @param scrambledAnswer
	 * @param scrambledIDs
	 * @return
	 */
	public StringBuilder renderAddQuestion(String theMatch, String scrambledAnswer, List<String> scrambledIDs) {
		
		StringBuilder resultHTML = new StringBuilder("");

		resultHTML.append("<div class=\"matching-ranking__row question--matching matching-ranking__row--width-medium\">");
		resultHTML.append("<div class=\"matching-ranking__cell matching-ranking__cell--dropdown-right\">");
		resultHTML.append("<ul class=\"no-style-ul\"> ");
		resultHTML.append("<li class=\"matching-ranking__content--width\">");
		resultHTML.append("<a href=\"javascript:void(0);\" class=\"matching-ranking__cell--select-option is-unchecked matching-ranking__cell no-underline\" data-typeval=\"question--matching\"> ");
		resultHTML.append("<span class=\"matching-ranking__selected\"></span>");
		resultHTML.append("<span class=\"single-on-click icon-dropdown icon-dropdown-style show\"></span>");
		resultHTML.append("</a>");
		resultHTML.append("<div class=\"matching-ranking__no-answer\"></div>");
		resultHTML.append("<div class=\"matching-ranking__cell matching-ranking__correct-answer\">Correct: ");
		resultHTML.append(scrambledAnswer);
		resultHTML.append("</div>");
		resultHTML.append("<div class=\"matching-ranking__options-wrap\"> ");
		resultHTML.append("<div class=\"matching-ranking__options__triangle\"></div> ");
		resultHTML.append("<div class=\"matching-ranking__options\">");
		resultHTML.append("<ul class=\"no-style-ul\">");
		/**
		 * Here we are showing the drop down elements
		 */
		resultHTML.append("<li class=\"matching-ranking__option\" data-optionval=\"-2-2\">NO ANSWER</li>");
		for (int j = 0 ; j < scrambledIDs.size(); j++) {
			resultHTML.append("<li class=\"matching-ranking__option\")");
			resultHTML.append(" data-optionval=\"").append((String)scrambledIDs.get(j)).append("\">");
			resultHTML.append(Integer.toString(j+1));
			resultHTML.append("</li>");
		}

		resultHTML.append("</ul>");
		resultHTML.append("</div>");
		resultHTML.append("</div>");
		resultHTML.append("</li>");
		resultHTML.append("</ul>");
		resultHTML.append("</div>");
		resultHTML.append("<div class=\"matching-ranking__cell font-bold matching-ranking__cell--text-width\">");
		resultHTML.append(theMatch);
		resultHTML.append("</div>");
		resultHTML.append("</div>");

		return resultHTML;
	}

	/**
	 * This method is designed to showing feedback for Question
	 * @param feedbackList A <code>List</code> Object containing feedback list
	 * @param connectSolution A String Object reprsenting connect solution
	 * @return A String representing Question feedback
	 * @throws Exception
	 */
	public String showFeedBackForQuestion(List<String> feedbackList , String connectSolution) throws Exception{
		StringBuilder theFeedback= new StringBuilder();
		if(feedbackList != null && !feedbackList.isEmpty()){
			if(StringUtils.isNotBlank(connectSolution)){
				connectSolution = "<p class=\"problem_solution\">" + connectSolution + "</p>";
			}
			for(String chioceFeedBack : feedbackList){
				theFeedback.append("<p class=\"maFeedback\">").append(chioceFeedBack).append("</p>").append(connectSolution);
			}
		}
		return theFeedback.toString();
	}

	/** This method is required to generate Choices for question
	 * @param scrambledChoices A <code>List</code> Object containing choice list
	 * @return A String returning choice HTML
	 * @throws Exception
	 */
	public String generateChoiceHtml(List<String> scrambledChoices) throws Exception{
		StringBuilder resultHTML = new StringBuilder("");
		if(scrambledChoices != null && !scrambledChoices.isEmpty()){
			for (int i=0 ; i<scrambledChoices.size() ; i++){
				String thisChoice = (String)scrambledChoices.get(i);
				resultHTML.append("<div class=\"matching-ranking__row matching-ranking__row--width-medium font-bold\">")
				.append(Integer.toString( i + 1 )).append(". ").append(thisChoice)
				.append("</div>").toString();
			}
		}
		return resultHTML.toString();
	}

	/** This method is responsible to get the boolean value for the
	 * given parametername 
	 * @param valueMap A <code>Map</code> Object containing required values
	 * @param parameterName A String Object representing parameter boolean value
	 * @return A Boolean value representing true or false
	 */
	public boolean getBooleanValue(Map<String, Object> valueMap, String parameterName){
		boolean booleanValue = false;
		if(valueMap != null && !valueMap.isEmpty() && valueMap.get(parameterName) != null){
			booleanValue = (Boolean)valueMap.get(parameterName);
		}
		return booleanValue;
	}

	public String v6pdf( tp_requestHandler theHandler, test theTest, submission theSubmission, parameters theData, String unusedText ) 
	{
		if (choices.size() == 0) return("");
		
		String theQID= "Q_" + sqlID;
		
		VectorAdapter scrambledChoices= new VectorAdapter();
		VectorAdapter scrambledIDs= new VectorAdapter();
		
		VectorAdapter scrambledAnswers= new VectorAdapter();
		for (int i=0 ; i<matches.size() ; i++) 
			scrambledAnswers.addElement("");
		
		int totalChoices= choices.size() + detractors.size();
		boolean[] chosen= new boolean[ totalChoices ];
		for (int i=0 ; i<totalChoices ; i++)
			chosen[i]= false;
		boolean complete= false;

		while ( ! complete ) 
		{
			// select a choice at random
			double theIndex= Math.floor( Math.random() * totalChoices );
			int i= (int)theIndex;
			
			if (!chosen[i]) {	// if we have not already included the choice
				chosen[i]= true;

				String theChoice= "unknown";
				if (i < choices.size())
					theChoice= (String)choices.elementAt(i);
				else
					theChoice= (String)detractors.elementAt(i-choices.size());

				theChoice= randomVariable.deReference(theChoice, theTest, sqlID, theData);
				//theChoice= richMedia.pdfDeReference(theChoice, theHandler, sqlID, theData);
				theChoice= richMedia.deReference(theChoice, theHandler, sqlID, theData);

				// calculate the choice selection value
				double top= Math.rint( Math.random() * 100 ) * 10000;
				double bottom= Math.rint( Math.random() * 100);
				int theID= (int)(top + ((54-i)*100) + bottom);

				// add it to the vector
				scrambledChoices.addElement( theChoice );
				scrambledIDs.addElement( Integer.toString( theID ) );
				
				if (i < choices.size())
					scrambledAnswers.setElementAt( "&nbsp;&nbsp;<span style=\"color: #004000; font-size: 9pt\">#" + Integer.toString(scrambledChoices.size()) + "</span>", i );

				// see if we've got them all
				complete= true;
				for (int j=0 ; j<totalChoices ; j++)
					complete = complete && chosen[j];
			}
		}
		
		
		ConcurrentHashMap userChoices= new ConcurrentHashMap();
		if (theData != null) 
		{		// if the participant has responded
			for (int i=0 ; i<matches.size() ; i++) 
			{
				String userEntry= theData.getParam( theQID + "_" + Integer.toString(i) );		// get each response
				theData.removeParam( theQID + "_" + Integer.toString(i) );
				
				if (userEntry.length() > 0) 
				{		// the question was offered
					int userChoice = -1;
					try 
					{
						userChoice= Integer.parseInt( userEntry.trim() );
					} 
					catch (NumberFormatException e) {}
					
					if (userChoice >= 0) {		// we answered the question
						userChoice %= 10000;
						userChoice /= 100;
						userChoice = 54 - userChoice;
						
						if ((userChoice >= 0) && (userChoice < totalChoices)) 
						{
							if (userChoice < choices.size())
								userChoices.put( (String)matches.elementAt(i), (String)choices.elementAt(userChoice) );
							else
								userChoices.put( (String)matches.elementAt(i), (String)detractors.elementAt(userChoice-choices.size()) );
						}
					}
				}
			}
		}
		
		
		String resultHTML= "<p class=\"rspStyle\">";
		
		resultHTML += "<table cellpadding=\"0\" cellspacing=\"0\">";
		
		for (int i=0 ; i<matches.size() ; i++) 
		{
			String theMatch= (String)matches.elementAt(i);
			theMatch= randomVariable.deReference(theMatch, theTest, sqlID, theData);
			//theMatch= richMedia.pdfDeReference(theMatch, theHandler, sqlID, theData);
			theMatch= richMedia.deReference(theMatch, theHandler, sqlID, theData);
			
			String thisChoice= (String)scrambledChoices.elementAt(i);
			
			resultHTML += "<tr><td height=\"25\">" + Integer.toString( i + 1 ) + ". " + thisChoice + "</td>";
			
			resultHTML += "<td align=\"right\">" + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + theMatch + "&nbsp;&nbsp;</td>";
			
			resultHTML += "<td>";
			
			String oldMatch= (String)userChoices.get( theMatch );
			
			if (oldMatch == null) 
				resultHTML += "___&nbsp;";
			else 
			{
				for (int j=0; j<scrambledChoices.size(); j++)
				{
					String aChoice= (String)scrambledChoices.elementAt(j);
					if (oldMatch.equals(aChoice))
					{
						resultHTML += "_" + Integer.toString(j + 1) + "_";
						break;
					}
				}
			}
			
			resultHTML += "&nbsp;&nbsp;";
			
			resultHTML += (String)scrambledAnswers.elementAt(i);
			
			resultHTML += "</td></tr>";
		}
		
		for (int i=matches.size() ; i<scrambledChoices.size() ; i++) 
		{
			String thisChoice= (String)scrambledChoices.elementAt(i);
			thisChoice= randomVariable.deReference(thisChoice, theTest, sqlID, theData);
			//thisChoice= richMedia.pdfDeReference(thisChoice, theHandler, sqlID, theData);
			thisChoice= richMedia.deReference(thisChoice, theHandler, sqlID, theData);

			resultHTML += "<tr><td colspan=\"3\" height=\"25\">" + Integer.toString( i + 1 ) + ". " + thisChoice + "</td></tr>";
		}
		
		resultHTML += "</table>";
		
		String defaultFeedback= questionProperties.getString(COMMON_FEEDBACK, "").trim();
		if (defaultFeedback == null || ("").equals(defaultFeedback))
		{
			defaultFeedback= (String)feedback.elementAt(0);
			int separatorIndex= defaultFeedback.indexOf( fmReturn );
			if (separatorIndex >= 0) 
			{
				if (separatorIndex == 0)
					defaultFeedback= defaultFeedback.substring(1);
				else
					defaultFeedback= defaultFeedback.substring(0,separatorIndex);
			}
		}
		
		String theFeedback= "";
		
		if ((defaultFeedback.length() > 0))
		{
			theFeedback= "<p class=\"maFeedback\">" + defaultFeedback + "</p>";
			theFeedback= tooltipDeReference(theFeedback);
			theFeedback= randomVariable.deReference(theFeedback, theTest, sqlID, theData);
			theFeedback= richMedia.deReference(theFeedback, theHandler, sqlID, theData);
		}
		
		if (theFeedback.trim().length() > 0){
			resultHTML += "<div class=\"feedback_block\">" + theFeedback + "</div>";
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
		
		if ( completeIncompleteGrading() )
		{
			int matchCount= 0;
			for (int j=0 ; j<matches.size() ; j++) 
			{
				String userEntry= theSubmission.getParam( questionID + "_" + Integer.toString(j) );
				if (userEntry.length() == 0) continue;
				
				int userChoice = -1;
				try 
				{
					userChoice= Integer.parseInt( userEntry.trim() );
				} 
				catch (NumberFormatException e) {}
				
				if (userChoice < 0) continue;
				
				matchCount++;
			}
			
			if (matchCount == matches.size())
				thisResponse.points= maxPoints;
		}
		else
		{		
			int correctMatches= 0;
			String userMatches= "";
			
			for (int j=0 ; j<matches.size() ; j++) 
			{
				String theChoice= (String)choices.elementAt(j);
				String theMatch= (String)matches.elementAt(j);
				
				int userChoice = 0;
				
				String userEntry= theSubmission.getParam( questionID + "_" + Integer.toString(j) );
				
				if (userEntry.length() > 0) 		// the question was offered
				{
					try 
					{
						userChoice= Integer.parseInt( userEntry.trim() );
					} 
					catch (NumberFormatException e) 
					{
						userChoice= -1;
					}
					
					if (userChoice >= 0)			// they answered the question
					{
						theSubmission.replaceParam("Q_" + sqlID + "_answered", "true");

						userChoice %= 10000;
						userChoice /= 100;
						userChoice = 54 - userChoice;
						if (userChoice < 0) userChoice= 0;
						
						if (userChoice == j)
						{
							//System.out.println("correct!");
							correctMatches++;
						}
						//else System.out.println("INcorrect!");
						
						if (userMatches.length() > 0) userMatches += ", ";					
						userMatches += Integer.toString(j+1) + "::" + Integer.toString(userChoice+1);
					}
					
					else							// no match
						userMatches += Integer.toString(j+1) + "::n/r";
				}
				
				else
				{
					theSubmission.addResponse( thisResponse );
					return;
				}
			}
			
			thisResponse.recordValue( userMatches );
			if (matches.size() > 0)
			{
				if (correctMatches == matches.size()) thisResponse.points= maxPoints;
				/*
				float thePoints= (float)correctMatches;
				thePoints /= (float)matches.size();
				thePoints *= maxPoints;
				thisResponse.points= Math.round(thePoints);
				*/
				else if (matches.size() > 0)
					thisResponse.points= (correctMatches * maxPoints) / matches.size();
			}
		}
		
		theSubmission.addResponse( thisResponse );
	}


	public void classwareEvaluate( tp_requestHandler theHandler, test theTest, submission theSubmission ) 
	{
		String questionID= "Q_" + sqlID;
		response thisResponse= new response( sqlID, matches.size(), "" );
		
		if ( completeIncompleteGrading() )
		{
			int matchCount= 0;
			for (int j=0 ; j<matches.size() ; j++) 
			{
				String userEntry= theSubmission.getParam( questionID + "_" + Integer.toString(j) );
				if (userEntry.length() == 0) continue;
				
				int userChoice = -1;
				try 
				{
					userChoice= Integer.parseInt( userEntry.trim() );
				} 
				catch (NumberFormatException e) {}
				
				if (userChoice < 0) continue;
				
				matchCount++;
			}
			
			if (matchCount == matches.size())
				thisResponse.points= maxPoints;
		}
		else
		{		
			int correctMatches= 0;
			String userMatches= "";
			
			for (int j=0 ; j<matches.size() ; j++) 
			{
				String theChoice= (String)choices.elementAt(j);
				String theMatch= (String)matches.elementAt(j);
				
				int userChoice = 0;
				
				String userEntry= theSubmission.getParam( questionID + "_" + Integer.toString(j) );
				
				if (userEntry.length() > 0) 		// the question was offered
				{
					try {
						userChoice= Integer.parseInt( userEntry.trim() );
					} catch (NumberFormatException e) {
						userChoice= -1;
					}
					
					if (userChoice >= 0)			// they answered the question
					{
						theSubmission.replaceParam("Q_" + sqlID + "_answered", "true");
					
						userChoice %= 10000;
						userChoice /= 100;
						userChoice = 54 - userChoice;
						if (userChoice < 0) userChoice= 0;
						
						if (userChoice == j) correctMatches++;
						
						if (userMatches.length() > 0) userMatches += ", ";					
						userMatches += Integer.toString(j+1) + "::" + Integer.toString(userChoice+1);
					}
					
					else							// no match
						userMatches += Integer.toString(j+1) + "::n/r";
				}
				
				else
				{
					theSubmission.addResponse( thisResponse );
					return;
				}
			}
			
			thisResponse.recordValue( userMatches );
			thisResponse.points= correctMatches;
		}
		
		theSubmission.addResponse( thisResponse );
	}
	
	
	public void hm_evaluate( tp_requestHandler theHandler, test theTest, submission theSubmission ) 
	{
		String questionID= "Q_" + sqlID;
		//response thisResponse= new response( sqlID, matches.size(), "" );
		response thisResponse= new response( sqlID, maxPoints, "" );
		
		if ( completeIncompleteGrading() )
		{
			int matchCount= 0;
			for (int j=0 ; j<matches.size() ; j++) 
			{
				String userEntry= theSubmission.getParam( questionID + "_" + Integer.toString(j) );
				if (userEntry.length() == 0) continue;
				
				int userChoice = -1;
				try 
				{
					userChoice= Integer.parseInt( userEntry.trim() );
				} 
				catch (NumberFormatException e) {}
				
				if (userChoice < 0) continue;
				
				matchCount++;
			}
			
			if (matchCount == matches.size())
				thisResponse.points= maxPoints;
			
			if( "yes".equals(theHandler.getParam(classware_hm.POLICY_participation)) && matchCount > 0){
				thisResponse.points= maxPoints;
				theSubmission.replaceParam("Q_" + sqlID + "_answered", "true");
			}
		}
		else
		{		
			int correctMatches= 0;
			String userMatches= "";
			
			for (int j=0 ; j<matches.size() ; j++) 
			{
				String theChoice= (String)choices.elementAt(j);
				String theMatch= (String)matches.elementAt(j);
				
				int userChoice = 0;
				
				String userEntry= theSubmission.getParam( questionID + "_" + Integer.toString(j) );
				
				if (userEntry.length() > 0) 		// the question was offered
				{
					try {
						userChoice= Integer.parseInt( userEntry.trim() );
					} catch (NumberFormatException e) {
						userChoice= -1;
					}
					
					if (userChoice >= 0)			// they answered the question
					{
						theSubmission.replaceParam("Q_" + sqlID + "_answered", "true");
					
						userChoice %= 10000;
						userChoice /= 100;
						userChoice = 54 - userChoice;
						if (userChoice < 0) userChoice= 0;
						
						if (userChoice == j) correctMatches++;
						
						if (userMatches.length() > 0) userMatches += ", ";					
						userMatches += Integer.toString(j+1) + "::" + Integer.toString(userChoice+1);
					}
					
					else							// no match
						userMatches += Integer.toString(j+1) + "::n/r";
				}
				else
				{
					theSubmission.addResponse( thisResponse );
					return;
				}
			}
			
			thisResponse.recordValue( userMatches );

			thisResponse.points= 0;
			if (correctMatches == matches.size())
				thisResponse.points= classware_hm.DEFAULT_INTERNAL_POINTS;
			else if (matches.size() > 0)
				thisResponse.points= (classware_hm.DEFAULT_INTERNAL_POINTS * correctMatches) / matches.size();
			
			String answeredFlag = theSubmission.getParam("Q_" + sqlID + "_answered");
			if( "yes".equals(theHandler.getParam(classware_hm.POLICY_participation)) && "true".equalsIgnoreCase(answeredFlag)){
				thisResponse.points= maxPoints;
			}
		}
		theSubmission.addResponse( thisResponse );
	}
	
	public void hm_evaluate( tp_requestHandler theHandler, test theTest,  PartialTO partialTO) 
	{
		String questionID= "Q_" + sqlID;
		/*QuestionParameters questionParameters = null;
		QuestionParameters questionParameters1 = new QuestionParameters();
		questionParameters1.setQuestionID(sqlID);
		questionParameters1.setPointsMax(maxPoints);*/
		
		QuestionParameters questionParameters = partialTO.getQuestionTO().getQuestionMap().get(sqlID);
		if(questionParameters == null){
			questionParameters = new QuestionParameters();
			questionParameters.setQuestionID(sqlID);
			partialTO.getQuestionTO().getQuestionMap().put(sqlID, questionParameters);
		}
		questionParameters.setPointsMax(maxPoints);
		CustomMap<String, String> questionParamsMap = (CustomMap<String, String>)questionParameters.getQuestionParameters();
		
		
		if ( completeIncompleteGrading() )
		{
			int matchCount= 0;
			for (int j=0 ; j<matches.size() ; j++) 
			{
				
				String userEntry= questionParamsMap.getParam(questionID + "_" + Integer.toString(j) );
				
				if (userEntry.length() == 0) continue;
				
				int userChoice = -1;
				try 
				{
					userChoice= Integer.parseInt( userEntry.trim() );
				} 
				catch (NumberFormatException e) {}
				
				if (userChoice < 0) continue;
				
				matchCount++;
			}
			
			if (matchCount == matches.size())
				questionParameters.setPoints(maxPoints);
			
			if( "yes".equals(theHandler.getParam(classware_hm.POLICY_participation)) && matchCount > 0){//if p_participation is set to yes award full credit.
				 questionParameters.setPoints(maxPoints);
				 questionParamsMap.replaceParam("Q_" + sqlID + "_answered", "true");
			}
		}
		else
		{		
			int correctMatches= 0;
			String userMatches= "";
			
			for (int j=0 ; j<matches.size() ; j++) 
			{
				int userChoice = 0;
				
				String userEntry= questionParamsMap.getParam(questionID + "_" + Integer.toString(j) );
				
				if (userEntry.length() > 0) 		// the question was offered
				{
					try {
						userChoice= Integer.parseInt( userEntry.trim() );
					} catch (NumberFormatException e) {
						userChoice= -1;
					}
					
					if (userChoice >= 0)			// they answered the question
					{
						//theSubmission.replaceParam("Q_" + sqlID + "_answered", "true");
						questionParamsMap.replaceParam("Q_" + sqlID + "_answered", "true");
						userChoice %= 10000;
						userChoice /= 100;
						userChoice = 54 - userChoice;
						if (userChoice < 0) userChoice= 0;
						
						if (userChoice == j) correctMatches++;
						
						if (userMatches.length() > 0) userMatches += ", ";					
						userMatches += Integer.toString(j+1) + "::" + Integer.toString(userChoice+1);
					}
					
					else							// no match
						userMatches += Integer.toString(j+1) + "::n/r";
				}
				else
				{
					//partialTO.getQuestionTO().getQuestionMap().put(sqlID, questionParameters);
					return;
				}
			}
			
			//thisResponse.recordValue( userMatches );
			questionParameters.getRecordedValue().addElement(userMatches);

			questionParameters.setPoints(0);
			if (correctMatches == matches.size()){
			   questionParameters.setPoints(classware_hm.DEFAULT_INTERNAL_POINTS);
			}
			else if (matches.size() > 0){
			  questionParameters.setPoints((classware_hm.DEFAULT_INTERNAL_POINTS * correctMatches) / matches.size());
			}
			String answeredFlag = questionParamsMap.getParam("Q_" + sqlID + "_answered");
			if( "yes".equals(theHandler.getParam(classware_hm.POLICY_participation)) && "true".equalsIgnoreCase(answeredFlag)){
				 questionParameters.setPoints(maxPoints);
			}
		}
	}
	
	
	public String v6analysis( tp_requestHandler theHandler, test theTest )
	{
		int correctCount= 0;
		int offerCount= 0;
		
		int[][]	counts= new int[choices.size()+detractors.size()][matches.size()];
		
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
				if (values.size() == 0) continue; //unoffered

				offerCount++;
				if (rs.getInt("points") == rs.getInt("maxpoints")) correctCount++;
				
				String thisResponse= (String)values.elementAt(0);
				
				StringTokenizer theTokens= new StringTokenizer(thisResponse, ",");
				while (theTokens.hasMoreTokens())
				{
					String matchReport= theTokens.nextToken().trim();
					StringTokenizer matchTokens= new StringTokenizer(matchReport, ":");
					if (matchTokens.countTokens() == 2)
					{
						try
						{
							int thisMatch= Integer.parseInt(matchTokens.nextToken()) - 1;
							int thisChoice= Integer.parseInt(matchTokens.nextToken()) - 1;
							counts[thisChoice][thisMatch]++;
						}
						catch (NumberFormatException n) {}
					}
				}
			}
						
			tp_sql.releaseResources(con, stmt, rs);
		}
		catch (SQLException s)
		{
			theTest.sqlRoutine.reportException("matching.v6analysis()", s);
		}finally{
			tp_sql.releaseResources(con, stmt, rs);
		}
	
	
		String theAnalysis= "<p style=\"margin-left: 30; margin-top: 0; margin-bottom: 20\">never offered</p>";
		
		if (offerCount > 0)
		{		
			int thePct= (100 * correctCount) / offerCount;
			theAnalysis= "<p style=\"margin-left: 30; margin-top: 0; margin-bottom: 20\">Correctly answered " + Integer.toString(correctCount) + " out of " + Integer.toString(offerCount) + " (" + Integer.toString(thePct) + "%)<br>";
			
			theAnalysis += "<table border=\"1\" style=\"font-size: 9pt\" cellpadding=\"2\" cellspacing=\"2\"><tr><td>&nbsp;</td>";
			for (int i=0 ; i<matches.size() ; i++)
			{
				String theMatch= (String)matches.elementAt(i);
				theMatch= randomVariable.deReference(theMatch, theTest, sqlID, theHandler.requestParams);
				theMatch= richMedia.deReference(theMatch, theHandler, sqlID, theHandler.requestParams);
				theAnalysis += "<td>" + theMatch + "</td>";
			}
			theAnalysis += "</tr>";
			
			for (int i=0 ; i<(choices.size()+detractors.size()) ; i++) 
			{
				String thisChoice= "";
				if (i < choices.size()) thisChoice= (String)choices.elementAt(i);
				else thisChoice= (String)detractors.elementAt(i-choices.size());
				
				thisChoice= randomVariable.deReference(thisChoice, theTest, sqlID, theHandler.requestParams);
				thisChoice= richMedia.deReference(thisChoice, theHandler, sqlID, theHandler.requestParams);
				
				theAnalysis += "<tr><td style=\"text-align: right\">" + thisChoice + "</td>";

				for (int j=0 ; j<matches.size() ; j++)
				{
					String color= "";
					if (i==j) color="; font-weight: bold; color: #004000";
					
					thePct= (100 * counts[i][j]) / offerCount;
					theAnalysis += "<td style=\"font-size: 9pt; text-align: center" + color + "\">" + Integer.toString(thePct) + "%</td>";
				}
				theAnalysis += "</tr>";
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
		
		for (int i=0 ; i<matches.size() ; i++) 
		{
			String theMatch= (String)matches.elementAt(i);
			
			try 
			{
				richMedia theMedia= new richMedia( theMatch );
				matches.setElementAt(theMedia.upgradev4mediareferences(""), i);
				result= true;
			} 
			catch (testFormatException e) {}
		}
		
		return(result);
	}

	public String v5elxContent( tp_requestHandler theHandler, test theTest ) 
	{
		String result= "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r";
		
		result += "<maquestion xmlns=\"http://www.mhhe.com/EZTest/\">\r";

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
		if (feedback.size() > 0) {
			theFeedback= (String)feedback.elementAt(0);
			int separatorIndex= theFeedback.indexOf( fmReturn );
			if (separatorIndex >= 0) {
				if (separatorIndex == 0)	// no checked feedback
					theFeedback= theFeedback.substring(1);
				else
					theFeedback= theFeedback.substring(0,separatorIndex);
			}
		}
		result += "	<explanation><p xmlns=\"http://www.w3.org/1999/xhtml\">\r";
		result += toXMLhtml(theFeedback, theHandler);
		result += "	</p></explanation>\r";
	
		result += "	<maInfo>\r";
		
		VectorAdapter xmlChoices= new VectorAdapter();
		for (int i=0 ; i<choices.size() ; i++) 
		{
			String thisChoice = "			<pair>\r";
			thisChoice += "				<choice><p xmlns=\"http://www.w3.org/1999/xhtml\">\r";
			thisChoice += toXMLhtml((String)choices.elementAt(i), theHandler);
			thisChoice += "				</p></choice>\r";
			thisChoice += "				<match><p xmlns=\"http://www.w3.org/1999/xhtml\">\r";
			thisChoice += toXMLhtml((String)matches.elementAt(i), theHandler);
			thisChoice += "				</p></match>\r";
			thisChoice += "			</pair>\r";
			
			xmlChoices.add(thisChoice);
		}
		
		for (int i=0 ; i<detractors.size() ; i++) 
		{
			String thisChoice = "			<pair>\r";
			thisChoice += "				<choice><p xmlns=\"http://www.w3.org/1999/xhtml\">\r";
			thisChoice += toXMLhtml((String)detractors.elementAt(i), theHandler);
			thisChoice += "				</p></choice>\r";
			thisChoice += "				<match><p xmlns=\"http://www.w3.org/1999/xhtml\">\r";
			thisChoice += "				</p></match>\r";
			thisChoice += "			</pair>\r";
			
			xmlChoices.add(thisChoice);
		}
		
		if (xmlChoices.size() == 0)
		{
			String thisChoice = "			<pair>\r";
			thisChoice += "				<choice><p xmlns=\"http://www.w3.org/1999/xhtml\">\r";
			thisChoice += "				</p></choice>\r";
			thisChoice += "				<match><p xmlns=\"http://www.w3.org/1999/xhtml\">\r";
			thisChoice += "				</p></match>\r";
			thisChoice += "			</pair>\r";
			
			xmlChoices.add(thisChoice);
		}
		
		result += "		<matches>\r";
		for (int i=0; i< xmlChoices.size(); i++)
			result += (String)xmlChoices.elementAt(i);
		result += " 	</matches>\r";

		result += "	</maInfo>\r";
	
		result += "</maquestion>\r";
				
		return(result);
	}


	public String v7elxContent( tp_requestHandler theHandler, test theTest ) 
	{
		//boolean answerSpecificFeedbackSupport= licenseManager.hasFeature(theHandler, licenseManager.FEATURE_SUPPORT_ASFEEDBACK);

		String result= "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r";
		
		result += "<maquestion xmlns=\"http://www.mhhe.com/EZTest/\">\r";

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
		if (feedback.size() > 0) {
			theFeedback= (String)feedback.elementAt(0);
			int separatorIndex= theFeedback.indexOf( fmReturn );
			if (separatorIndex >= 0) {
				if (separatorIndex == 0)	// no checked feedback
					theFeedback= theFeedback.substring(1);
				else
					theFeedback= theFeedback.substring(0,separatorIndex);
			}
		}

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
		
		result += "	<maInfo>\r";
		
		result += "	 <manualScoring>" + getScoring() + "</manualScoring>\r";

		VectorAdapter xmlChoices= new VectorAdapter();
		for (int i=0 ; i<choices.size() ; i++) 
		{
			String thisChoice = "			<pair>\r";
			thisChoice += "				<choice><p xmlns=\"http://www.w3.org/1999/xhtml\">\r";
			thisChoice += toXMLhtml((String)choices.elementAt(i), theHandler);
			thisChoice += "				</p></choice>\r";
			thisChoice += "				<match><p xmlns=\"http://www.w3.org/1999/xhtml\">\r";
			thisChoice += toXMLhtml((String)matches.elementAt(i), theHandler);
			thisChoice += "				</p></match>\r";

			//if (answerSpecificFeedbackSupport){
				String fb_ma= theFeedback;
				String fb_no= theFeedback;
				if (i < feedback.size()) {
					StringTokenizer theTokens= new StringTokenizer( (String)feedback.elementAt(i), fmReturn );
					if (theTokens.hasMoreTokens()) fb_ma= theTokens.nextToken();
					if (theTokens.hasMoreTokens()) fb_no= theTokens.nextToken();
				}
				
				thisChoice += "				<feedbackmatched><p xmlns=\"http://www.w3.org/1999/xhtml\">\r";
				thisChoice += toXMLhtml(fb_ma, theHandler);
				thisChoice += "				</p></feedbackmatched>\r";

				thisChoice += "				<feedbackunmatched><p xmlns=\"http://www.w3.org/1999/xhtml\">\r";
				thisChoice += toXMLhtml(fb_no, theHandler);
				thisChoice += "				</p></feedbackunmatched>\r";
			//}

			thisChoice += "			</pair>\r";
			
			xmlChoices.add(thisChoice);
		}
		
		for (int i=0 ; i<detractors.size() ; i++) 
		{
			String thisChoice = "			<pair>\r";
			thisChoice += "				<choice><p xmlns=\"http://www.w3.org/1999/xhtml\">\r";
			thisChoice += toXMLhtml((String)detractors.elementAt(i), theHandler);
			thisChoice += "				</p></choice>\r";
			thisChoice += "				<match><p xmlns=\"http://www.w3.org/1999/xhtml\">\r";
			thisChoice += "				</p></match>\r";
			
			//if (answerSpecificFeedbackSupport){
				thisChoice += "				<feedbackmatched><p xmlns=\"http://www.w3.org/1999/xhtml\">\r";
				thisChoice += "				</p></feedbackmatched>\r";
				thisChoice += "				<feedbackunmatched><p xmlns=\"http://www.w3.org/1999/xhtml\">\r";
				thisChoice += "				</p></feedbackunmatched>\r";
			//}
			
			thisChoice += "			</pair>\r";
			
			xmlChoices.add(thisChoice);
		}
		
		if (xmlChoices.size() == 0)
		{
			String thisChoice = "			<pair>\r";
			thisChoice += "				<choice><p xmlns=\"http://www.w3.org/1999/xhtml\">\r";
			thisChoice += "				</p></choice>\r";
			thisChoice += "				<match><p xmlns=\"http://www.w3.org/1999/xhtml\">\r";
			thisChoice += "				</p></match>\r";
			
			//if (answerSpecificFeedbackSupport){
				thisChoice += "				<feedbackmatched><p xmlns=\"http://www.w3.org/1999/xhtml\">\r";
				thisChoice += "				</p></feedbackmatched>\r";
				thisChoice += "				<feedbackunmatched><p xmlns=\"http://www.w3.org/1999/xhtml\">\r";
				thisChoice += "				</p></feedbackunmatched>\r";
			//}
			
			thisChoice += "			</pair>\r";
			
			xmlChoices.add(thisChoice);
		}
		
		result += "		<matches>\r";
		for (int i=0; i< xmlChoices.size(); i++)
			result += (String)xmlChoices.elementAt(i);
		result += " 	</matches>\r";

		result += "	</maInfo>\r";
	
		result += hintXML( theHandler );
		result += contentLinkXML( theHandler );
		result += tooltipXML( theHandler );

		result += "</maquestion>\r";
				
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
			_logger.error("update failure in fillInTheBlank.v7update");
			return;
		}
		
		//System.out.println(theXMLdata);
		String theFeedback= "";
		
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
				
				/*else if (thisItem.getName().equals("explanation"))
				{
					theFeedback= embeddedMedia.processMediaIn( theHandler, theTest, getXMLhtml(thisItem, theHandler), sqlID );
					feedback= new VectorAdapter();
				}*/
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

				else if (thisItem.getName().equals("maInfo"))
				{
					java.util.List maData= thisItem.getChildren();
					ListIterator iter2= maData.listIterator();
					while (iter2.hasNext()) 
					{
						Element maItem= (Element)iter2.next();
						//System.out.println("    " + maItem.getName());

						if (maItem.getName().equals("manualScoring"))
							questionProperties.setString(question.CONNECT_FORCED_SCORING, getXMLstring(maItem));
						
						else if (maItem.getName().equals("matches"))
						{
							choices= new VectorAdapter();
							matches= new VectorAdapter();
							detractors= new VectorAdapter();
							feedback= new VectorAdapter();
							points= new VectorAdapter();
							correctAnswerFeedback= new VectorAdapter();
							
							java.util.List choiceData= maItem.getChildren();
							ListIterator iter3= choiceData.listIterator();
							for (int i=0; iter3.hasNext(); i++) 
							{
								Element choiceItem= (Element)iter3.next();
								//System.out.println("    " + choiceItem.getName());

								if (choiceItem.getName().equals("pair"))
								{
									String thisChoice= "";
									java.util.List thisChoiceData= choiceItem.getChildren();
									ListIterator iter4= thisChoiceData.listIterator();
									
									String fb_ma= "";
									String fb_no= "";
									boolean isDistractor= false;
									
									while (iter4.hasNext()) 
									{
										Element htmlItem= (Element)iter4.next();
										//System.out.println("    " + htmlItem.getName());
										
										if (htmlItem.getName().equals("choice"))
											thisChoice= embeddedMedia.processMediaIn( theHandler, theTest, getXMLhtml(htmlItem, theHandler), sqlID );
											
										else if (htmlItem.getName().equals("match"))
										{
											String thisMatch= embeddedMedia.processMediaIn( theHandler, theTest, getXMLhtml(htmlItem, theHandler), sqlID );
											
											String distractorTest= tp_utils.substitute( thisMatch, "&nbsp;", "" );
											distractorTest= tp_utils.substitute( distractorTest, "&#160;", "" );
											
											//System.out.println("match: (" + distractorTest.trim().length() + ") " + thisMatch);
											
											if (distractorTest.trim().length() == 0)
											{
												detractors.addElement(thisChoice);
												isDistractor= true;
											}
											else
											{
												//System.out.println("choice: " + thisChoice);
												choices.addElement( thisChoice );
												matches.addElement( thisMatch );
												points.addElement("1" + fmReturn + "0");
												correctAnswerFeedback.addElement("should be matched");
											}
										}

										else if (htmlItem.getName().equals("feedbackmatched"))
											fb_ma= embeddedMedia.processMediaIn( theHandler, theTest, getXMLhtml(htmlItem, theHandler), sqlID );
										
										else if (htmlItem.getName().equals("feedbackunmatched"))
											fb_no= embeddedMedia.processMediaIn( theHandler, theTest, getXMLhtml(htmlItem, theHandler), sqlID );
									}
									
									if (!isDistractor)
									{
										//if (answerSpecificFeedbackSupport)
											feedback.addElement( fb_ma + fmReturn + fb_no );
										//else
										//	feedback.addElement( theFeedback + fmReturn + theFeedback );
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
			_logger.error("XML IO error in matching.v7update()", io);
		}
		catch (JDOMException jd) 
		{
			_logger.error("XML parsing error in matching.v7update()", jd);
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
		boolean suppressProperty= questionProperties.getBoolean( SUPPRESS_CHOICES, supressChoices );
		questionProperties.setBoolean( SUPPRESS_CHOICES, suppressProperty );
		
		boolean commonFeedback= true;
		String theFeedback= "";
		String fb1= "";
		String fb2= "";
		for (int i=0; i<feedback.size(); i++)
		{
			String thisFeedback= (String)feedback.elementAt(i);
			if (i==0)
			{
				theFeedback= thisFeedback;
				
				StringTokenizer theTokens= new StringTokenizer( theFeedback, fmReturn );
				if (theTokens.hasMoreTokens()) fb1= theTokens.nextToken();
				if (theTokens.hasMoreTokens()) fb2= theTokens.nextToken();
			}
			else
			{
				if (!theFeedback.equals(thisFeedback))
				{
					commonFeedback= false;
					break;
				}
			}
		}
		if (!fb1.equals(fb2)) commonFeedback= false;
		String theCommonFeedback= questionProperties.getString(COMMON_FEEDBACK, null);		
		
		JSONObject result= jsonStub( theHandler );
			
		result.put(XML_MA_STEM, tp_utils.safeJSON(qtext));
		
		if (theCommonFeedback != null)
		{
			commonFeedback= false;	
			result.put(XML_MA_COMMONFEEDBACK, tp_utils.safeJSON(theCommonFeedback));
		}
		else if (commonFeedback)
			result.put(XML_MA_COMMONFEEDBACK, tp_utils.safeJSON(theFeedback));
		
		JSONArray jchoices= new JSONArray();
	
		for (int i=0; i<choices.size(); i++)
		{
			JSONObject choice= new JSONObject();
			
			String thisChoice= (String)choices.elementAt(i);
			choice.put( XML_MA_PAIR_PARTX, tp_utils.safeJSON(thisChoice) );

			String thisMatch= (String)matches.elementAt(i);
			choice.put( XML_MA_PAIR_PARTY, tp_utils.safeJSON(thisMatch) );
			
			if (!commonFeedback)
			{
				String fbMatched= "";
				String fbUnmatched= "";
				if (i < feedback.size())
				{
					StringTokenizer theTokens= new StringTokenizer( (String)feedback.elementAt(i), fmReturn );
					if (theTokens.hasMoreTokens()) fbMatched= theTokens.nextToken();
					if (theTokens.hasMoreTokens()) fbUnmatched= theTokens.nextToken();
				}
				
				choice.put( XML_MA_PAIR_FEEDBACK_MA, tp_utils.safeJSON(fbMatched) );
				choice.put( XML_MA_PAIR_FEEDBACK_NO, tp_utils.safeJSON(fbUnmatched) );
			}
			
			jchoices.put(choice);
		}
		
		for (int i=0; i<detractors.size(); i++)
		{
			JSONObject choice= new JSONObject();
			
			String thisChoice= (String)detractors.elementAt(i);
			choice.put( XML_MA_PAIR_PARTX, tp_utils.safeJSON(thisChoice) );

			String thisMatch= (String)matches.elementAt(i);
			choice.put( XML_MA_PAIR_PARTY, "" );
			
			if (!commonFeedback)
			{
				choice.put( XML_MA_PAIR_FEEDBACK_MA, "" );
				choice.put( XML_MA_PAIR_FEEDBACK_NO, "" );
			}
			
			jchoices.put(choice);
		}
		
		result.put("matches", jchoices);
		
		return result;
	}


	public JSONObject getItemQinfoJson()
	throws JSONException
	{
		richProperties questionProperties = new richProperties(this.questionProperties.toXML());
		boolean suppressProperty= questionProperties.getBoolean( SUPPRESS_CHOICES, supressChoices );
		questionProperties.setBoolean( SUPPRESS_CHOICES, suppressProperty );
		
		boolean commonFeedback= true;
		String theFeedback= "";
		String fb1= "";
		String fb2= "";
		for (int i=0; i<feedback.size(); i++)
		{
			String thisFeedback= (String)feedback.elementAt(i);
			if (i==0)
			{
				theFeedback= thisFeedback;
				
				StringTokenizer theTokens= new StringTokenizer( theFeedback, fmReturn );
				if (theTokens.hasMoreTokens()) fb1= theTokens.nextToken();
				if (theTokens.hasMoreTokens()) fb2= theTokens.nextToken();
			}
			else
			{
				if (!theFeedback.equals(thisFeedback))
				{
					commonFeedback= false;
					break;
				}
			}
		}
		if (!fb1.equals(fb2)) commonFeedback= false;
		String theCommonFeedback= questionProperties.getString(COMMON_FEEDBACK, null);		
		
		JSONObject result = getItemQinfoCommonJson();
		JSONArray props= questionProperties.exportJSON();
		if (props != null) result.put( "properties", props );
		
		result.put(XML_MA_STEM, tp_utils.safeJSON(qtext));
		
		if (theCommonFeedback != null)
		{
			commonFeedback= false;	
			result.put(XML_MA_COMMONFEEDBACK, tp_utils.safeJSON(theCommonFeedback));
		}
		else if (commonFeedback)
			result.put(XML_MA_COMMONFEEDBACK, tp_utils.safeJSON(theFeedback));
		
		JSONArray jchoices= new JSONArray();
	
		for (int i=0; i<choices.size(); i++)
		{
			JSONObject choice= new JSONObject();
			
			String thisChoice= (String)choices.elementAt(i);
			choice.put( XML_MA_PAIR_PARTX, tp_utils.safeJSON(thisChoice) );
	
			String thisMatch= (String)matches.elementAt(i);
			choice.put( XML_MA_PAIR_PARTY, tp_utils.safeJSON(thisMatch) );
			
			if (!commonFeedback)
			{
				String fbMatched= "";
				String fbUnmatched= "";
				if (i < feedback.size())
				{
					StringTokenizer theTokens= new StringTokenizer( (String)feedback.elementAt(i), fmReturn );
					if (theTokens.hasMoreTokens()) fbMatched= theTokens.nextToken();
					if (theTokens.hasMoreTokens()) fbUnmatched= theTokens.nextToken();
				}
				
				choice.put( XML_MA_PAIR_FEEDBACK_MA, tp_utils.safeJSON(fbMatched) );
				choice.put( XML_MA_PAIR_FEEDBACK_NO, tp_utils.safeJSON(fbUnmatched) );
			}
			
			jchoices.put(choice);
		}
		
		for (int i=0; i<detractors.size(); i++)
		{
			JSONObject choice= new JSONObject();
			
			String thisChoice= (String)detractors.elementAt(i);
			choice.put( XML_MA_PAIR_PARTX, tp_utils.safeJSON(thisChoice) );
	
			String thisMatch= (String)matches.elementAt(i);
			choice.put( XML_MA_PAIR_PARTY, "" );
			
			if (!commonFeedback)
			{
				choice.put( XML_MA_PAIR_FEEDBACK_MA, "" );
				choice.put( XML_MA_PAIR_FEEDBACK_NO, "" );
			}
			
			jchoices.put(choice);
		}
		
		result.put("matches", jchoices);

		return result;
	}
	
	public boolean importJSON( tp_requestHandler theHandler, JSONObject theJSON )
		throws JSONException
	{
		jsonStubImport(theHandler, theJSON);
		
		if (theJSON.has(XML_MA_STEM))
			qtext= tp_utils.jsonPostProcess( theHandler, theJSON.getString(XML_MA_STEM), this);
	
		JSONArray jchoices= null;
		if (theJSON.has("matches"))
			jchoices= theJSON.getJSONArray("matches");
		else
			throw new JSONException("missing matches");
		
		choices= new VectorAdapter();
		matches= new VectorAdapter();
		detractors= new VectorAdapter();
		feedback= new VectorAdapter();
		points= new VectorAdapter();
		
		boolean hasFeedback= false;
		
		for (int i=0; i<jchoices.length(); i++)
		{
			JSONObject thisChoice= jchoices.getJSONObject(i);
			
			String jdistractor= null;
			String jmatch= null;
			String jfeedback_match= null;
			String jfeedback_nomatch= null;
									
			if (thisChoice.has(XML_MA_PAIR_PARTX))
				jdistractor= tp_utils.jsonPostProcess( theHandler, thisChoice.getString(XML_MA_PAIR_PARTX), this);
			
			if (thisChoice.has(XML_MA_PAIR_PARTY))
			{
				jmatch= tp_utils.jsonPostProcess( theHandler, thisChoice.getString(XML_MA_PAIR_PARTY), this);
				if (jmatch.trim().length() == 0) jmatch= null;
			}
			
			if (thisChoice.has(XML_MA_PAIR_FEEDBACK_MA))
				jfeedback_match= tp_utils.jsonPostProcess( theHandler, thisChoice.getString(XML_MA_PAIR_FEEDBACK_MA), this);
			
			if (thisChoice.has(XML_MA_PAIR_FEEDBACK_NO))
				jfeedback_nomatch= tp_utils.jsonPostProcess( theHandler, thisChoice.getString(XML_MA_PAIR_FEEDBACK_NO), this);
			
			if ((jdistractor != null) && (jmatch != null))
			{
				choices.addElement(jdistractor);
				matches.addElement(jmatch);
				
				feedback.addElement(jfeedback_match + fmReturn + jfeedback_nomatch);
				
				points.addElement("1" + fmReturn + "0");
				
				if ((jfeedback_match.length() > 0) || (jfeedback_nomatch.length() > 0)) hasFeedback= true;
			}
			else if ((jdistractor != null) && (jmatch == null))
			{
				detractors.addElement(jdistractor);
			}

			else
				throw new JSONException("improperly formatted JSON match");
		}
		
		questionProperties.setBoolean(USE_COMMON_FEEDBACK, hasFeedback);
		
		supressChoices= questionProperties.getBoolean( SUPPRESS_CHOICES, false );
		
		return true;
	}
	
	

	static String		XML_MA						= "matching";
	
	static String		XML_MA_STEM					= "stem";
	static String		XML_MA_COMMONFEEDBACK		= "commonFeedback";
	static String		XML_MA_PAIR_SET				= "pairs";
	static String		XML_MA_DISTRACTOR_SET		= "distractors";

	static String		XML_MA_PAIR					= "pair";
	static String		XML_MA_PAIR_PARTX			= "distractor";
	static String		XML_MA_PAIR_PARTY			= "match";
	static String		XML_MA_PAIR_CREDIT_MA		= "creditMatched";
	static String		XML_MA_PAIR_CREDIT_NO		= "creditUnmatched";
	static String		XML_MA_PAIR_FEEDBACK_MA		= "feedbackMatched";
	static String		XML_MA_PAIR_FEEDBACK_NO		= "feedbackUnmatched";

	static String		XML_MA_DISTRACTOR			= "distractor";

	
	public Element buildExportXML( tp_requestHandler theHandler, test theTest )
	{
		// make sure this is in the questionProperties
		boolean suppressProperty= questionProperties.getBoolean( SUPPRESS_CHOICES, supressChoices );
		questionProperties.setBoolean( SUPPRESS_CHOICES, suppressProperty );
	
		Element result= xmlStub( theHandler, theTest );

		Element ma= new Element(XML_MA);
		result.addContent(ma);
		
		Element stem= new Element(XML_MA_STEM);
		ma.addContent(stem);
		stem.addContent( tp_utils.safeCDATA(qtext) );
				
		boolean commonFeedback= true;
		String theFeedback= "";
		String fb1= "";
		String fb2= "";
		for (int i=0; i<feedback.size(); i++)
		{
			String thisFeedback= (String)feedback.elementAt(i);
			if (i==0)
			{
				theFeedback= thisFeedback;
				
				StringTokenizer theTokens= new StringTokenizer( theFeedback, fmReturn );
				if (theTokens.hasMoreTokens()) fb1= theTokens.nextToken();
				if (theTokens.hasMoreTokens()) fb2= theTokens.nextToken();
			}
			else
			{
				if (!theFeedback.equals(thisFeedback))
				{
					commonFeedback= false;
					break;
				}
			}
		}
		if (!fb1.equals(fb2)) commonFeedback= false;

		// see if we have defined common feedback
		String theCommonFeedback= questionProperties.getString(COMMON_FEEDBACK, null);
		if (theCommonFeedback != null) {
			commonFeedback= false;
			
			Element common= new Element(XML_MA_COMMONFEEDBACK);
			ma.addContent(common);
			common.addContent( tp_utils.safeCDATA(theCommonFeedback) );
		}
		else if (commonFeedback)
		{
			Element common= new Element(XML_MA_COMMONFEEDBACK);
			ma.addContent(common);
			common.addContent( tp_utils.safeCDATA(fb1) );
		}
		

		Element choiceset= new Element(XML_MA_PAIR_SET);
		ma.addContent(choiceset);
		
		for (int i=0; i<choices.size(); i++)
		{
			String thisChoice= (String)choices.elementAt(i);
			String thisMatch= (String)matches.elementAt(i);
			
			String matchedPoints= "0";
			String unmatchedPoints= "0";
			StringTokenizer theTokens= new StringTokenizer( (String)points.elementAt(i), fmReturn );
			if (theTokens.hasMoreTokens()) matchedPoints= theTokens.nextToken();
			if (theTokens.hasMoreTokens()) unmatchedPoints= theTokens.nextToken();
			
			String matchedFeedback= "";
			String unmatchedFeedback= "";
			theTokens= new StringTokenizer( (String)feedback.elementAt(i), fmReturn );
			if (theTokens.hasMoreTokens()) matchedFeedback= theTokens.nextToken();
			if (theTokens.hasMoreTokens()) unmatchedFeedback= theTokens.nextToken();
			
			Element choice= new Element(XML_MA_PAIR);
			choiceset.addContent( choice );
			
			Element partx= new Element(XML_MA_PAIR_PARTX);
			choice.addContent(partx);
			partx.addContent( tp_utils.safeCDATA(thisChoice) );
			
			Element party= new Element(XML_MA_PAIR_PARTY);
			choice.addContent(party);
			party.addContent( tp_utils.safeCDATA(thisMatch) );
			
			Element checkPts= new Element(XML_MA_PAIR_CREDIT_MA);
			choice.addContent(checkPts);
			checkPts.setText( matchedPoints );
			
			Element uncheckPts= new Element(XML_MA_PAIR_CREDIT_NO);
			choice.addContent(uncheckPts);
			uncheckPts.setText( unmatchedPoints );
			
			if (!commonFeedback)
			{
				Element checkFB= new Element(XML_MA_PAIR_FEEDBACK_MA);
				choice.addContent(checkFB);
				checkFB.addContent( tp_utils.safeCDATA(matchedFeedback) );
				
				Element uncheckFB= new Element(XML_MA_PAIR_FEEDBACK_NO);
				choice.addContent(uncheckFB);
				uncheckFB.addContent( tp_utils.safeCDATA(unmatchedFeedback) );
			}
		}
		
		if (detractors.size() > 0)
		{
			Element distractorSet= new Element(XML_MA_DISTRACTOR_SET);
			ma.addContent(distractorSet);
			
			for (int i=0; i<detractors.size(); i++)
			{
				String thisDistractor= (String)detractors.elementAt(i);
				Element distractor= new Element(XML_MA_DISTRACTOR);
				distractorSet.addContent( distractor );
				distractor.addContent( tp_utils.safeCDATA(thisDistractor) );
			}
		}
		
		return result; 
	}
	
	
	public static question buildFromXML( tp_requestHandler theHandler, test theTest, Element qElement )
	{
		//boolean answerSpecificFeedbackSupport= licenseManager.hasFeature(theHandler, licenseManager.FEATURE_SUPPORT_ASFEEDBACK);

		matching theQ= new matching();
		theQ.stdXMLimport( theHandler, theTest, qElement );
		
		theQ.supressChoices= theQ.questionProperties.getBoolean( SUPPRESS_CHOICES, false );
				
		theQ.detractors= new VectorAdapter();
		theQ.choices= new VectorAdapter();
		theQ.matches= new VectorAdapter();
		theQ.points= new VectorAdapter();
		theQ.feedback= new VectorAdapter();
		
		try
		{
			Element mcElement= qElement.getChild(XML_MA);
			
			String commonFeedback= null;
			java.util.List mcInfo= mcElement.getChildren();
			ListIterator iter= mcInfo.listIterator();
			while (iter.hasNext()) 
			{
				Element thisChild= (Element)iter.next();
				String thisName= thisChild.getName();
			
				if (thisName.equals(XML_MA_STEM))
					theQ.qtext= thisChild.getText();
			
				//else if (thisName.equals(XML_MA_COMMONFEEDBACK))
				//	commonFeedback= thisChild.getText();
				else if (thisName.equals(XML_MA_COMMONFEEDBACK))
				{
					commonFeedback= thisChild.getText();
					//if (answerSpecificFeedbackSupport && (theQ.questionProperties.getString(COMMON_FEEDBACK, null) == null))
					if (theQ.questionProperties.getString(COMMON_FEEDBACK, null) == null)
						theQ.questionProperties.setString(COMMON_FEEDBACK, commonFeedback);
				}
			
			
				else if (thisName.equals(XML_MA_PAIR_SET))
				{
					java.util.List choiceInfo= thisChild.getChildren();
					ListIterator iter2= choiceInfo.listIterator();
					while (iter2.hasNext()) 
					{
						Element thisChoice= (Element)iter2.next();
						
						if (thisChoice.getName().equals(XML_MA_PAIR))
						{
							String choice= thisChoice.getChildText(XML_MA_PAIR_PARTX);
							if (choice == null) continue;
							
							String match= thisChoice.getChildText(XML_MA_PAIR_PARTY);
							if (match == null) continue;
							
							String award_ma= thisChoice.getChildText(XML_MA_PAIR_CREDIT_MA);
							if (award_ma == null) continue;
							
							String award_no= thisChoice.getChildText(XML_MA_PAIR_CREDIT_NO);
							if (award_no == null) continue;
							
							String fb_ma= thisChoice.getChildText(XML_MA_PAIR_FEEDBACK_MA);
							//if (fb_ma == null) fb_ma= commonFeedback;
							//if (!answerSpecificFeedbackSupport) fb_ma= commonFeedback;
							if (fb_ma == null) continue;
							
							String fb_no= thisChoice.getChildText(XML_MA_PAIR_FEEDBACK_NO);
							//if (fb_no == null) fb_no= commonFeedback;
							//if (!answerSpecificFeedbackSupport) fb_no= commonFeedback;
							if (fb_no == null) continue;
							
							theQ.choices.addElement(choice);
							theQ.matches.addElement(match);
							theQ.points.addElement(award_ma + fmReturn + award_no);
							theQ.feedback.addElement(fb_ma + fmReturn + fb_no);
						}
					}
				}
				
				else if (thisName.equals(XML_MA_DISTRACTOR_SET))
				{
					java.util.List choiceInfo= thisChild.getChildren();
					ListIterator iter2= choiceInfo.listIterator();
					while (iter2.hasNext()) 
					{
						Element thisChoice= (Element)iter2.next();
						
						if (thisChoice.getName().equals(XML_MA_DISTRACTOR))
							theQ.detractors.addElement( thisChoice.getText() );
					}
				}
			}
			theQ.importQFromXML( theHandler, theTest, qElement );
		}
		catch (Exception e)
		{
			_logger.error("Exception parsing XML in matching.buildFromXML()");
		}
		
		return theQ;	
	}


	public boolean compareQ( question otherQ )
	{
		if (otherQ.type != type) { _logger.info("type mismatch"); return false; }
		
		matching theQ= (matching)otherQ;
		
		boolean result= true;
		
		if (theQ.supressChoices != supressChoices) { _logger.info("choice suppression mismatch"); result= false; }
		
		if (theQ.choices.size() != choices.size()) { _logger.info("choices size mismatch"); result= false; }
		if (theQ.matches.size() != matches.size()) { _logger.info("matches size mismatch"); result= false; }
		if (theQ.detractors.size() != detractors.size()) { _logger.info("detractors size mismatch"); result= false; }
		if (theQ.points.size() != points.size()) { _logger.info("points size mismatch"); result= false; }
		if (theQ.feedback.size() != feedback.size()) { _logger.info("feedback size mismatch"); result= false; }
		
		if (result != false)
		{
			for (int i=0; i<choices.size(); i++)
			{
				if (!((String)choices.elementAt(i)).equals(((String)theQ.choices.elementAt(i)))) { _logger.info("choice mismatch"); result= false; break; }
			}
			
			for (int i=0; i<matches.size(); i++)
			{
				if (!((String)matches.elementAt(i)).equals(((String)theQ.matches.elementAt(i)))) { _logger.info("matches mismatch"); result= false; break; }
			}
			
			for (int i=0; i<detractors.size(); i++)
			{
				if (!((String)detractors.elementAt(i)).equals(((String)theQ.detractors.elementAt(i)))) { _logger.info("detractors mismatch"); result= false; break; }
			}
			
			for (int i=0; i<points.size(); i++)
			{
				if (!((String)points.elementAt(i)).equals(((String)theQ.points.elementAt(i)))) { _logger.info("points mismatch"); result= false; break; }
			}
			
			for (int i=0; i<feedback.size(); i++)
			{
				if (!((String)feedback.elementAt(i)).equals(((String)theQ.feedback.elementAt(i)))) { _logger.info("feedback mismatch"); result= false; break;	}
			}
		}
		
		return (result && compareStd( otherQ ));
	}


	public boolean feedbackToSolution( tp_requestHandler theHandler )
	{
		boolean commonFeedback= true;
		String theFeedback= "";
		String fb1= "";
		String fb2= "";
		for (int i=0; i<feedback.size(); i++)
		{
			String thisFeedback= (String)feedback.elementAt(i);
			if (i==0)
			{
				theFeedback= thisFeedback;
				
				StringTokenizer theTokens= new StringTokenizer( theFeedback, fmReturn );
				if (theTokens.hasMoreTokens()) fb1= theTokens.nextToken();
				if (theTokens.hasMoreTokens()) fb2= theTokens.nextToken();
			}
			else
			{
				if (!theFeedback.equals(thisFeedback))
				{
					commonFeedback= false;
					break;
				}
			}
		}
		if (!fb1.equals(fb2)) commonFeedback= false;
		
		// see if we have defined common feedback
		String theCommonFeedback= questionProperties.getString(COMMON_FEEDBACK, null);
		if (theCommonFeedback != null) 
		{
			questionProperties.setString(COMMON_FEEDBACK, "");
			
			setSolution(theCommonFeedback);
			return true;
		}
		else if (commonFeedback)
		{
			VectorAdapter newFeedback= new VectorAdapter();
			for (int i=0; i<feedback.size(); i++)
				newFeedback.addElement(fmReturn);
			feedback= newFeedback;
				
			setSolution(fb1);
			return true;
		}
		
		return false;
	}

	public JSONObject populateQuestionJSON() 
	throws Exception
	{
		JSONObject jsonObj = new JSONObject();
		jsonObj.append("type", this.typeIdentifier());
		String qText = this.qtext;
		qText = tp_utils.removeMediaRefSpecialCharsAndSpace(qText);
		jsonObj.append("stem", qText);
		
		VectorAdapter arrList = this.feedback;
		if(arrList != null && !arrList.isEmpty()){
			for(int i = 0; i < arrList.size(); i++){
				if(arrList.get(i) != null){
					String temp = (String)arrList.get(i);
					jsonObj.append("feedback", tp_utils.removeMediaRefSpecialCharsAndSpace(temp));
				}
			}
		}
		
		arrList = this.matches;
		if(arrList != null && !arrList.isEmpty()){
			for(int i = 0; i < arrList.size(); i++){
				if(arrList.get(i) != null){
					String temp = (String)arrList.get(i);
					jsonObj.append("matchpairs", tp_utils.removeMediaRefSpecialCharsAndSpace(temp));
				}
			}
		}
		arrList = this.detractors;
		if(arrList != null && !arrList.isEmpty()){
			for(int i = 0; i < arrList.size(); i++){
				if(arrList.get(i) != null){
					String temp = (String)arrList.get(i);
					jsonObj.append("distractors", tp_utils.removeMediaRefSpecialCharsAndSpace(temp));
				}
			}
		}
		return jsonObj;
	}
	
	
	/**
	 * This method returns item/question in json format after applying various 
	 * policies for matching question. This is termed as redacted item json.
	 * @param userResponseWithPolicyTO
	 * @param testId
	 * @param mode
	 */
	@Override
	public JSONObject getRedactedQinfoJson(UserResponseWithPolicyTO userResponseWithPolicyTO,String testId, String mode) throws Exception {
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
		//boolean isFeedbackSolnRequired = classware_hm.POLICY_grading_feedback.equals(postSubmissionMode);
		boolean showFeedBack = QuestionUtil.showExplanation(testParamMap, policyMap);
		boolean isTestMode =  TEST_MODE.equals(mode);
		boolean isReviewMode =  REVIEW_MODE.equals(mode);

		richProperties	questionProperties = null;
		if(isTestMode){
			questionProperties = richProperties.newInstance("TestModeRedactedProperties");			
		}else {
			questionProperties = new richProperties(this.questionProperties.toXML());
		}
		
		questionProperties.setBD( "questionPoint", this.questionProperties.getBD(classware_hm.HM_POINTS, "10") );
		boolean suppressProperty= questionProperties.getBoolean( SUPPRESS_CHOICES, supressChoices );
		questionProperties.setBoolean( SUPPRESS_CHOICES, suppressProperty );
		
		boolean answerSpecificFeedbackExist = false;
		String commonFeedback = questionProperties.getString(COMMON_FEEDBACK, null);
		
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
		
		if (props != null) result.put( "properties", props );
				
		QuestionWiseResponseTO questionWiseResponseTO = responseTO.getResponseMap().get(sqlID);
		CustomMap<String, String> questionResponseMap = questionWiseResponseTO.getQuestionParameters();
		String theQID = "Q_"+sqlID;
		
		String choiceString = questionResponseMap.getParam(theQID+SCRAMBLED_CHOICES);
		
		if(StringUtils.isBlank(choiceString)){
			choiceString = getChoiceString();
			questionResponseMap.replaceParam(theQID+SCRAMBLED_CHOICES, choiceString);
		}	
		//to generate the scrambled ids:start
		VectorAdapter scrambledChoices = null;
		VectorAdapter scrambledIDs = null;		
		Map<String,VectorAdapter> scrambledChoiceVectorMap = getScrambledChoiceVectorMap(choiceString);
		if(scrambledChoiceVectorMap != null){
			scrambledChoices = scrambledChoiceVectorMap.get("scrambledChoices");
			scrambledIDs = scrambledChoiceVectorMap.get("scrambledIDs");
		}
		//to generate the scrambled ids:end
		
		JSONArray jchoices= new JSONArray();
		String thisUserResponse = null;
		JSONObject choice = null;
		for(int i=0; i< matches.size(); i++){
			choice= new JSONObject();
			
			String thisChoice = (String)scrambledChoices.elementAt(i);
			choice.put( XML_MA_PAIR_PARTX, tp_utils.safeJSON(thisChoice) );
			choice.put(XML_MA_PAIR_PARTX+"_id", scrambledIDs.get(i) );
			
			String thisMatch= (String)matches.elementAt(i);
			choice.put( XML_MA_PAIR_PARTY, tp_utils.safeJSON(thisMatch) );
			/*if(isReviewMode && showFeedBack){
				String fbMatched= null;
				String fbUnmatched= null;
				if (feedback != null && i < feedback.size()) {
					StringTokenizer theTokens= new StringTokenizer( (String)feedback.elementAt(i), fmReturn );
					if (theTokens.hasMoreTokens()){
						fbMatched= theTokens.nextToken();
					}
					if (theTokens.hasMoreTokens()){
						fbUnmatched= theTokens.nextToken();
					}
				}
				thisUserResponse = questionResponseMap.getParam(theQID+"_"+i);
				int selectedIndex = -1;
				if(StringUtils.isNotBlank(thisUserResponse) && !DEFAULT_RESPONSE.equals(thisUserResponse) ){
					selectedIndex = decryptUserChoice(thisUserResponse);
				}
				if(selectedIndex != -1){
					if(selectedIndex == i && StringUtils.isNotBlank(fbMatched)){
						choice.put( XML_MA_PAIR_FEEDBACK_MA, tp_utils.safeJSON(fbMatched) );
						answerSpecificFeedbackExist = true;
					}else if(StringUtils.isNotBlank(fbUnmatched)){
						choice.put( XML_MA_PAIR_FEEDBACK_NO, tp_utils.safeJSON(fbUnmatched) );
						answerSpecificFeedbackExist = true;
					}
				}
			}*/
			
			jchoices.put(choice);
		}
		
		for (int i=matches.size() ; i<scrambledChoices.size() ; i++) {
			choice = new JSONObject();			
			String thisChoice= (String)scrambledChoices.elementAt(i);
			choice.put( XML_MA_PAIR_PARTX, tp_utils.safeJSON(thisChoice) );
			choice.put(XML_MA_PAIR_PARTX+"_id", scrambledIDs.get(i) );
	
			choice.put( XML_MA_PAIR_PARTY, "" );			
			jchoices.put(choice);
		}
		result.put("matches", jchoices);
		
		/*if(isReviewMode && showFeedBack){
			if (!answerSpecificFeedbackExist && StringUtils.isNotBlank(commonFeedback) ){
				result.put(XML_MA_COMMONFEEDBACK, tp_utils.safeJSON(commonFeedback));
			}
		}*/
		//for media dereference
		result = super.getMediaDereferencedJson(result, testId);
		
		return result;
	}
	
	/**
	 * This method decrypt the user choice for matching type question.
	 * If 645430 is the user entry then it returns 0 as the decrypted value.  
	 * @param userEntry
	 * @return int
	 * @throws Exception
	 */
	private int decryptUserChoice(String userEntry) throws Exception{
		int userChoice = -1;
		try {
			userChoice= Integer.parseInt( userEntry.trim() );
		} catch (NumberFormatException e) {}
		
		if (userChoice >= 0) {
			// we answered the question
			userChoice %= 10000;
			userChoice /= 100;
			userChoice = 54 - userChoice;
		}
		return userChoice;
	}
	
	
	/**
	 * This Method returns a json representation of the response which is to be
	 * used as rinfo in item json. This is matching question specific implementation.
	 * @param userResponseWithPolicyTO, userResponseWithPolicyTO a custom userResponse with policyTO.
	 * @param questionMetaDataTO
	 * @param mode
	 * @return JSONObject
	 * @throws Exception
	 */
	public JSONObject translateQuestionResponseToRinfo(UserResponseWithPolicyTO userResponseWithPolicyTO, QuestionMetaDataTO questionMetaDataTO, String mode,TestTO testTo) throws Exception{
		JSONObject rinfoJson = null;
		String theQID = "Q_" + sqlID;
		PolicyTO policyTO = null;
		ResponseTO responseTO = null;
		Map<String, String> policyMap = null;
		String correctness = "";
		try {
			rinfoJson = super.translateQuestionResponseToRinfo(userResponseWithPolicyTO,questionMetaDataTO,mode,testTo);
			responseTO = userResponseWithPolicyTO.getResponseTO();
			QuestionWiseResponseTO questionWiseResponseTO = responseTO.getResponseMap().get(sqlID);
			CustomMap<String, String> questionResponseMap = questionWiseResponseTO.getQuestionParameters();
			policyTO = userResponseWithPolicyTO.getPolicyTO();
			policyMap = policyTO.getPolicyMap();
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
			String choiceString = questionResponseMap.getParam(theQID+SCRAMBLED_CHOICES);
			if(StringUtils.isBlank(choiceString)){
				choiceString = getChoiceString();
				questionResponseMap.replaceParam(theQID+SCRAMBLED_CHOICES, choiceString);
			}
			Map<String, String> scrambledChoiceIdMap = null;
			//String scrambledChoice = "";
			Set<String> scrambledChoiceSet = null;
			if(StringUtils.isNotBlank(choiceString)){
				scrambledChoiceIdMap = QuestionUtil.getScrambledChoiceIdMap(choiceString);
				//scrambledChoice = QuestionUtil.getScrambledChoiceString(choiceString);
				scrambledChoiceSet = scrambledChoiceIdMap.keySet();
			}
			
			boolean solution = CaaConstants.YES.equals(policyMap.get(classware_hm.POLICY_solution) );
			boolean fullFeedback = classware_hm.POLICY_grading_feedback.equals(policyMap.get(classware_hm.POLICY_grading) );
			
			JSONArray responses = new JSONArray();
			String encryptedUserResponse = null;
			Map<Integer, String> userResponseMap = new HashMap<Integer, String>();
			for(int i=0;i<matches.size();i++){
				JSONObject thisResponse = new JSONObject();
				thisResponse.put(CaaConstants.ID, i);
				String userEntry = questionResponseMap.getParam(theQID+"_"+i);
				int selectedIndex = -1;
				
				if (StringUtils.isBlank(userEntry) || DEFAULT_RESPONSE.equals(userEntry)){
					userEntry = "";
				}

				selectedIndex = decryptUserChoice(userEntry);
				
				String tempScrambledID = null;
				int tempDecodedChoiceIndex = -1;
				for(String thisChoiceIndex : scrambledChoiceSet){
					tempScrambledID = scrambledChoiceIdMap.get(thisChoiceIndex);
					tempDecodedChoiceIndex = decryptUserChoice(tempScrambledID);
					if(selectedIndex == tempDecodedChoiceIndex){
						userEntry = tempScrambledID ;
						break;
					}
				}
				if(CaaConstants.REVIEW.equals(mode)){
					//selectedIndex = decryptUserChoice(userEntry);
					boolean isResponseCorrect = false;
					if(i == selectedIndex){
						isResponseCorrect = true;
					}
					if(solution || fullFeedback){
						String thisScrambledID = null;
						String thisScrambledIDEncrypted = null;
						int thisDecodedChoice = -1;
						for(String thisChoiceIndex : scrambledChoiceSet){
							thisScrambledID = (String)scrambledChoiceIdMap.get(thisChoiceIndex);

							thisDecodedChoice = decryptUserChoice(thisScrambledID);
							if(i == thisDecodedChoice){
								thisScrambledIDEncrypted = Crypt.encrypt(thisScrambledID);
								thisResponse.put(CaaConstants.CORRECT_ANSWER, thisScrambledIDEncrypted);
								break;
							}
						}
					}
					if(isIndicatorRequired){
						correctness = String.valueOf(isResponseCorrect);
					}
					thisResponse.put(CaaConstants.CORRECT, correctness);
					userResponseMap.put(i, correctness);
				}


				if(!"".equals(userEntry)){
					encryptedUserResponse = Crypt.encrypt(userEntry);
				}else{
					encryptedUserResponse = "";
				}
				thisResponse.put(CaaConstants.VALUE, encryptedUserResponse);
				responses.put(thisResponse);
			}
			
			rinfoJson.put(RESPONSE, responses);
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
			/*if(!REVIEW_MODE.equals(mode)){
				rinfoJson.put(SCRAMBLE, QuestionUtil.getIntArrayFromString(scrambledChoice) );
			}*/			
		} catch (Exception e) {
			throw e;
		}
		return rinfoJson;
	}
	
	
	/* (non-Javadoc)
	 * @see com.mcgrawhill.ezto.test.questions.question#translateRinfoToQuestionResponse()
	 */
	@Override
	public Map<String, String> translateRinfoToQuestionResponse(JSONObject jsonObject, UserResponseWithPolicyTO userResponseWithPolicyTO) throws Exception {
		Map<String, String> questionParamMap = new HashMap<String, String>();
		
		try {
			if (jsonObject.has(QUESTIONID)) {
				String questionId = jsonObject.getString(QUESTIONID);
				if (questionId != null && questionId.length() > 0) {
					String keyPrefix = "Q_" + questionId;

					if (jsonObject.has(RESPONSE)) {
						JSONArray responseArray = jsonObject.getJSONArray(RESPONSE);
						String encryptedResponse = null;
						String decryptedResponse = null;
						JSONObject singleResponse = null;
						String matchId = null;
						for(int i=0;i<responseArray.length(); i++){
							singleResponse = responseArray.getJSONObject(i);
							encryptedResponse = singleResponse.getString(CaaConstants.VALUE);
							decryptedResponse = Crypt.decrypt(encryptedResponse);
							matchId = singleResponse.getString(CaaConstants.ID);
							if (encryptedResponse != null) {
								if(encryptedResponse.length() > 0){
									questionParamMap.put(keyPrefix+"_"+matchId, decryptedResponse);
								}else{
									questionParamMap.put(keyPrefix+"_"+matchId, DEFAULT_RESPONSE);
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			_logger.error("Error parsing translateRinfoToQuestionResponse in matching",e);
			throw e;
		}
		
		return questionParamMap;
	}
	
	
	/**
	 * This method evaluates the response for a matching question
	 * @param questionWiseResponseTO
	 * @throws Exception
	 */
	@Override
	public void itemEvaluate(QuestionWiseResponseTO questionWiseResponseTO, PolicyTO policyTO) throws Exception {
		try{
			if(questionWiseResponseTO != null && policyTO != null){
				CustomMap<String, String> questionParameters = questionWiseResponseTO.getQuestionParameters();
				String questionIdPrefix = "Q_" + sqlID;
				questionWiseResponseTO.setQuestionID(this.sqlID);
				questionWiseResponseTO.setPointsMax(classware_hm.DEFAULT_INTERNAL_POINTS);
				Map<String, Object> evaluatedMap = evaluateResponse(questionWiseResponseTO, policyTO);
				if (completeIncompleteGrading() || (policyTO.getPolicyMap() != null && "yes".equals(policyTO.getPolicyMap().get(classware_hm.POLICY_participation)))) {
					if(evaluatedMap != null && !evaluatedMap.isEmpty() && "true".equals(evaluatedMap.get(ANSWERED))){
						questionParameters.replaceParam(questionIdPrefix + "_answered",evaluatedMap.get(ANSWERED));
						questionWiseResponseTO.setPoints(classware_hm.DEFAULT_INTERNAL_POINTS);
					}
				}else {
					if(evaluatedMap != null && !evaluatedMap.isEmpty()){
						if(CaaConstants.TRUE.equals(evaluatedMap.get(ANSWERED))){
							questionParameters.replaceParam(questionIdPrefix + "_answered",evaluatedMap.get(ANSWERED));
						}
						questionWiseResponseTO.getRecordedValue().add((String)evaluatedMap.get(RECORDED_VALUE));
						
						if(evaluatedMap.get("correctMatches") != null){
							int correctMatches = Integer.parseInt((String)evaluatedMap.get("correctMatches"));
							if(correctMatches == matches.size()){
								questionWiseResponseTO.setPoints(classware_hm.DEFAULT_INTERNAL_POINTS);
							}else{
								questionWiseResponseTO.setPoints((classware_hm.DEFAULT_INTERNAL_POINTS * correctMatches) / matches.size());
							}
						}
					}
				}
			}
		}catch (Exception e) {
			_logger.error("Exception while evaluating question of sqlID : "+ sqlID);
			throw e;
		}
	}
	
	/**(non-Javadoc)
	 * @see com.mcgrawhill.ezto.test.questions.question#evaluateResponse for details.
	 */	
	public Map<String, Object> evaluateResponse(QuestionWiseResponseTO questionWiseResponseTO, PolicyTO policyTO) throws Exception{
		Map<String, Object> evaluatedMap = new HashMap<String, Object>();
		String questionID= "Q_" + sqlID;
		int correctMatches= 0;
		String userMatches= "";
		try{
			if(questionWiseResponseTO != null && policyTO != null){				
				JSONArray evaluationJsonArray = new JSONArray();
				evaluatedMap.put(CORRECTNESS, evaluationJsonArray);
				for (int j=0 ; j<matches.size() ; j++) {
					JSONObject choice = new JSONObject();
					choice.put("matchid", j);
					choice.put(CaaConstants.CORRECT, false);
					int userChoice = 0;			
					String userEntry= questionWiseResponseTO.getQuestionParameters().getParam(questionID + "_" + Integer.toString(j) );
					
					if (StringUtils.isNotBlank(userEntry)) {
						try {
							userChoice= Integer.parseInt( userEntry.trim() );
						} catch (NumberFormatException e) {
							userChoice= -1;
						}
						
						if (userChoice >= 0){// they answered the question
							evaluatedMap.put(ANSWERED, "true");
							userChoice %= 10000;
							userChoice /= 100;
							userChoice = 54 - userChoice;
							if (userChoice < 0) userChoice= 0;
							
							if (userChoice == j){
								correctMatches++;
								choice.put(CaaConstants.CORRECT, true);
							}
							
							if (userMatches.length() > 0) userMatches += ", ";					
							userMatches += Integer.toString(j+1) + "::" + Integer.toString(userChoice+1);
						}else{ // no match
							if (userMatches.length() > 0) userMatches += ", ";
							userMatches += Integer.toString(j+1) + "::n/r";
						}
					}
					evaluationJsonArray.put(choice);	
				}
				evaluatedMap.put(RECORDED_VALUE, userMatches);
				evaluatedMap.put(CORRECTNESS, evaluationJsonArray);
				evaluatedMap.put("correctMatches", Integer.toString(correctMatches) );
			}
			
		}catch(Exception ex){
			_logger.error("Exception while evaluating question of sqlID : "+ sqlID);
			throw ex;
		}
		return evaluatedMap;
	}
	
	/**
	 * @see com.mcgrawhill.ezto.test.questions.question#generateScrambledChoice(ResponseTO)
	 */
	public void generateScrambledChoice(CustomMap<String, String> questionResponseMap, Map<String, String> requestMap) throws Exception{
		String snapShotParamName = "Q_"+sqlID+SCRAMBLED_CHOICES;
		String choicesSnapShot = questionResponseMap.getParam(snapShotParamName);
		//to generate the scrambled ids for choices if not already generated
		if(choicesSnapShot == null || choicesSnapShot.length() == 0){
			choicesSnapShot = getChoiceString();
			questionResponseMap.replaceParam(snapShotParamName, choicesSnapShot);
		}	
	}
	
	/**
	 * This method generates choice order and their dynamic option value for the 
	 * matching choice type question. This choiceString (separated by ,  
	 * and then again separated by : ) gets generated during 
	 * the start of the assignment.
	 * @return String separated by , and : Here ',' separates choices and 
	 * ':' separates choiceIndex and their ramdom value.  
	 * @throws Exception
	 */
	private String getChoiceString() throws Exception{
		String choicesSnapShot = "";
		
		int totalChoices= choices.size() + detractors.size();
		boolean[] chosen= new boolean[ totalChoices ];
		for (int i=0 ; i<totalChoices ; i++){
			chosen[i]= false;
		}
		boolean complete= false;
		
		while ( ! complete ) {
			// select a choice at random
			double theIndex= Math.floor( Math.random() * totalChoices );
			int i= (int)theIndex;
			if (!chosen[i]) {	// if we have not already included the choice
				chosen[i]= true;		
				// calculate the choice selection value
				double top= Math.rint( Math.random() * 100 ) * 10000;
				double bottom= Math.rint( Math.random() * 100);
				int theID= (int)(top + ((54-i)*100) + bottom);
								
				if(choicesSnapShot.length() == 0){
					choicesSnapShot = i+":"+theID;
				}else{
					choicesSnapShot = choicesSnapShot+","+i+":"+theID;
				}
				// see if we've got them all
				complete= true;
				for (int j=0 ; j<totalChoices ; j++){
					complete = complete && chosen[j];
				}
			}
		}
		return choicesSnapShot;
	}
	
	/**
	 * This method generates scrambledChoice and scrambledIDs vector from the choiceString.
	 * @param choiceString separated by , and : For example like 3:45199,2:345288,0:685406,1:545376
	 * @return Map<String, VectorAdapter> where keys are "scrambledChoices" and "scrambledIDs". Elements 
	 * 		   in the scrambledIDs are encrypted to prevent student cheating.
	 * @throws Exception
	 */
	public Map<String, VectorAdapter> getScrambledChoiceVectorMap(String choiceString) throws Exception{
		Map<String, VectorAdapter> scrambledChoiceVectorMap = new HashMap<String, VectorAdapter>();
		VectorAdapter scrambledChoices= new VectorAdapter();
		VectorAdapter scrambledIDs= new VectorAdapter();
		
		if(choiceString != null && choiceString.length() > 0){
			String[] choiceIndexAndScrambledIDsArray = choiceString.split(",");
			String choiceIndexAndScrambledID = null;
			for(int i=0;i<choiceIndexAndScrambledIDsArray.length;i++){
				choiceIndexAndScrambledID = choiceIndexAndScrambledIDsArray[i];
				String[] choiceIndexAndScrambledIDArray = choiceIndexAndScrambledID.split(":");
				int choiceIndex = -1;
				String scrambledID = null;
				String theChoice= "unknown";
				if(choiceIndexAndScrambledIDArray.length == 2){
					choiceIndex = Integer.parseInt(choiceIndexAndScrambledIDArray[0]);
					scrambledID = choiceIndexAndScrambledIDArray[1];
					scrambledID = Crypt.encrypt(scrambledID);
					//## choiceString=3:45199,2:345288,0:685406,1:545376
					//if choice index is greater or equal to matches size it must be a detractor
					if(choiceIndex >= matches.size()){
						theChoice = (String)detractors.elementAt(choiceIndex - matches.size());
					}else{
						theChoice = (String)choices.elementAt(choiceIndex);
					}
					scrambledChoices.addElement(theChoice);
					scrambledIDs.addElement( scrambledID );
				}
			}
			scrambledChoiceVectorMap.put("scrambledChoices", scrambledChoices);
			_logger.info("scrambledChoices="+scrambledChoices);
			scrambledChoiceVectorMap.put("scrambledIDs", scrambledIDs);
			_logger.info("scrambledIDs="+scrambledIDs);
		}
		return scrambledChoiceVectorMap;
	}
	
	@Override
	public void updateCompletionStatus(QuestionWiseResponseTO questionWiseResponseTO,StringBuilder compStatus, String questionid) throws Exception {
		int index = 0;
		for (int j=0 ; j<matches.size() ; j++) {
			StringBuilder responseKey = new StringBuilder("Q_").append(questionid).append("_").append(Integer.toString(j));	
			String userEntry= questionWiseResponseTO.getQuestionParameters().getParam(responseKey.toString());
			if (userEntry.length() > 0 && !DEFAULT_RESPONSE.equals(userEntry)) {
				if(compStatus != null && isNotBlank(compStatus.toString())){
					if(!(compStatus.toString()).contains(responseKey.toString())){
						compStatus.append("+").append(responseKey.toString());						
					}
				}else{
					//first time assign value to compStatus
					compStatus.append(responseKey.toString());
				}
				index++;
			}
			if(index == matches.size() && StringUtils.isBlank(referenceTag)){
				String plusQuestionId = new StringBuilder("+").append(questionid).toString();
				if(!(compStatus.toString()).contains(plusQuestionId)){
					compStatus.append(plusQuestionId);
				}
			}
		}
	}
	
	/**
	 * @see com.mcgrawhill.ezto.test.questions.question#getAnsweredPercentage(CustomMap, String, QuestionWiseResponseTO)
	 */
	public BigDecimal getAnsweredPercentage(CustomMap<String,String> questionParam, String responseKey, QuestionWiseResponseTO questionWiseResponseTO) throws Exception {
		BigDecimal percent = new BigDecimal(0.00).setScale(2, RoundingMode.HALF_UP);		
		//String answered = questionParam.getParam(responseKey+"_answered");
		int noOfMatchAnswered = 0;
		int noOfMatches = matches.size();
		for(int i=0;i<noOfMatches;i++){
			String userEntry = questionParam.getParam(responseKey+"_"+i);			
			if (StringUtils.isNotBlank(userEntry)){
				if(!DEFAULT_RESPONSE.equals(userEntry)){
					noOfMatchAnswered++;
				}
			}
		}
		if(noOfMatches > 0 && noOfMatchAnswered > 0){
			double percentInDouble = ((double)noOfMatchAnswered/noOfMatches)*100;
			percent = new BigDecimal(percentInDouble).setScale(2, RoundingMode.HALF_UP);
		}
		return percent;
	}

	@Override
	protected FeedBackTO getQuestionFeedback(UserResponseWithPolicyTO userResponseWithPolicyTO,Map<Integer, String> userResponseMap, String mode) throws Exception {
		/**
		 * Check wheather it is applicable to show feedback or not
		 * this super call validates policies
		 */
		FeedBackTO feedBackTO =super.getQuestionFeedback(userResponseWithPolicyTO, userResponseMap,	mode); 
		if(feedBackTO == null){
			return feedBackTO;
		}
		/**
		 * Get useCommonFeedBack value
		 */
		boolean useCommonFeedBack = questionProperties.getBoolean(USE_COMMON_FEEDBACK, true);
		/**
		 * set defaultFeedback value
		 */
		String defaultFeedback= questionProperties.getString(COMMON_FEEDBACK, null);
		if (StringUtils.isBlank(defaultFeedback)){
			defaultFeedback= (String)feedback.elementAt(0);
			if(StringUtils.isNotBlank(defaultFeedback)){
				int separatorIndex= defaultFeedback.indexOf( fmReturn );
				if (separatorIndex >= 0){
					if (separatorIndex == 0){
						defaultFeedback= defaultFeedback.substring(1);
					}else{
						defaultFeedback= defaultFeedback.substring(0,separatorIndex);
					}
				}
			}
		}
		/**
		 * if useCommonFeedBack value is false then get anser specific feedback
		 */
		if(!useCommonFeedBack){
			if(userResponseMap != null && !userResponseMap.isEmpty()){
				for (int i=0 ; i<matches.size() ; i++){
					String fb_ma= "";
					String fb_no= "";
					if (i < feedback.size()){
						StringTokenizer theTokens= new StringTokenizer( (String)feedback.elementAt(i), fmReturn );
						if (theTokens.hasMoreTokens()){
							fb_ma= theTokens.nextToken().trim();
						}
						if (theTokens.hasMoreTokens()){
							fb_no= theTokens.nextToken().trim();
						}
					}
					String userResponse = userResponseMap.get(i);
					if(CaaConstants.TRUE.equals(userResponse) && StringUtils.isNotBlank(fb_ma)){
						feedBackTO.getAnswerSpecificFeedBack().put(i, fb_ma);
					}else if(CaaConstants.FALSE.equals(userResponse) && StringUtils.isNotBlank(fb_no)){
						feedBackTO.getAnswerSpecificFeedBack().put(i, fb_no);
					}
				}
			}
		}
		if(feedBackTO.getAnswerSpecificFeedBack().isEmpty() && StringUtils.isNotBlank(defaultFeedback)){
			feedBackTO.setFeedBack(defaultFeedback);
		}
		return feedBackTO;
	}
	
	@Override
	protected JSONObject createFeebBackJson(FeedBackTO feedBackTO, UserResponseWithPolicyTO userResponseWithPolicyTO) throws Exception {
		JSONObject feedBackJson = null;
		String feedBackStr = null;
		if(feedBackTO != null){
			if(StringUtils.isNotBlank(feedBackTO.getFeedBack())){
				feedBackStr = feedBackTO.getFeedBack();
			}else if(!feedBackTO.getAnswerSpecificFeedBack().isEmpty()){
				for(int i=0; i < feedBackTO.getAnswerSpecificFeedBack().size(); i++){
					String anserSpecificFeedback = feedBackTO.getAnswerSpecificFeedBack().get(i);
					if(StringUtils.isBlank(feedBackStr)){
						feedBackStr = anserSpecificFeedback;
					}else{
						feedBackStr = new StringBuilder(feedBackStr).append(anserSpecificFeedback).toString();
					}
				}
			}
			if(StringUtils.isNotBlank(feedBackStr)){
				feedBackStr = new StringBuilder(feedBackStr).append(connectSolution(userResponseWithPolicyTO)).toString();
				feedBackJson = new JSONObject();
				feedBackJson.put(CaaConstants.FEED_BACK_STEM, feedBackStr);
			}
		}
		return feedBackJson;
	}
}