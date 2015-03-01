package	com.mcgrawhill.ezto.test.questions.question_types;

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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mcgrawhill.ezto.tp_requestHandler;
import com.mcgrawhill.ezto.admin.licenseManager;
import com.mcgrawhill.ezto.api.caa.caaconstants.CaaConstants;
import com.mcgrawhill.ezto.api.caa.dao.util.QuestionMetadataUtil;
import com.mcgrawhill.ezto.api.caa.services.transferobject.PolicyTO;
import com.mcgrawhill.ezto.api.caa.services.transferobject.QuestionMetaDataTO;
import com.mcgrawhill.ezto.api.caa.services.transferobject.QuestionWiseResponseTO;
import com.mcgrawhill.ezto.api.caa.services.transferobject.ResponseTO;
import com.mcgrawhill.ezto.api.caa.services.transferobject.TestTO;
import com.mcgrawhill.ezto.api.caa.services.transferobject.UserResponseWithPolicyTO;
import com.mcgrawhill.ezto.api.exception.BusinessException;
import com.mcgrawhill.ezto.api.license.services.LicenseService;
import com.mcgrawhill.ezto.integration.classware_edit;
import com.mcgrawhill.ezto.integration.classware_hm;
import com.mcgrawhill.ezto.media.embeddedMedia;
import com.mcgrawhill.ezto.media.richMedia;
import com.mcgrawhill.ezto.security.authentication.authentication;
import com.mcgrawhill.ezto.sql.tp_sql;
import com.mcgrawhill.ezto.styles.v5ui;
import com.mcgrawhill.ezto.test.test;
import com.mcgrawhill.ezto.test.testFormatException;
import com.mcgrawhill.ezto.test.v5edit;
import com.mcgrawhill.ezto.test.v5test;
import com.mcgrawhill.ezto.test.v7list;
import com.mcgrawhill.ezto.test.v7media;
import com.mcgrawhill.ezto.test.v7scoring;
import com.mcgrawhill.ezto.test.v7test_instructor;
import com.mcgrawhill.ezto.test.v7test_student;
import com.mcgrawhill.ezto.test.questions.question;
import com.mcgrawhill.ezto.test.questions.questionGroup;
import com.mcgrawhill.ezto.test.questions.question_types.worksheet_answers.worksheet_answer;
import com.mcgrawhill.ezto.test.questions.question_types.worksheet_answers.worksheet_answer_external;
import com.mcgrawhill.ezto.test.questions.question_types.worksheet_answers.worksheet_answer_flash;
import com.mcgrawhill.ezto.test.questions.question_types.worksheet_answers.worksheet_answer_multipleChoice;
import com.mcgrawhill.ezto.test.questions.question_types.worksheet_answers.worksheet_answer_number;
import com.mcgrawhill.ezto.test.recording_reporting.response;
import com.mcgrawhill.ezto.test.recording_reporting.submission;
import com.mcgrawhill.ezto.utilities.BeanExtractionUtil;
import com.mcgrawhill.ezto.utilities.CustomMap;
import com.mcgrawhill.ezto.utilities.FeedBackTO;
import com.mcgrawhill.ezto.utilities.PartialTO;
import com.mcgrawhill.ezto.utilities.QuestionParameters;
import com.mcgrawhill.ezto.utilities.QuestionUtil;
import com.mcgrawhill.ezto.utilities.QuestionXrefUtil;
import com.mcgrawhill.ezto.utilities.VectorAdapter;
import com.mcgrawhill.ezto.utilities.parameters;
import com.mcgrawhill.ezto.utilities.randomVariable;
import com.mcgrawhill.ezto.utilities.richProperties;
import com.mcgrawhill.ezto.utilities.rtf_pdf_output;
import com.mcgrawhill.ezto.utilities.tp_sortedList;
import com.mcgrawhill.ezto.utilities.tp_utils;


/**
* implementation of worksheet questions
*/
public class worksheet extends question
{
	private static final Logger _logger = Logger.getLogger(worksheet.class);
	public static String		TYPE_IDENTIFIER			= "WK";
	
	public static String		PDF			= "PDFoutput";
	public final static String ALL_ESSAY = "all_essay"; 
	public final static String NOT_ALL_ESSAY = "not_all_essay";
	
	public VectorAdapter		answers;
	
	public String		explanation = "";
	
	
	/**
	* construct a new worksheet question
	*/
	public worksheet()
	{
		super();

		type= QUESTION_TYPE_worksheet;
		answers= new VectorAdapter();
		
		maxPoints= 1;
		longQuestion= true;
	}
	
	
	/**
	* duplicate a worksheet question. Changed for wimba changes
	*/
	public worksheet( tp_requestHandler theHandler, worksheet theOriginal )
	{
		super( theOriginal );
		Vector debugTransactions = tp_utils.listToVector(licenseManager.getFeatureInfo(theHandler, licenseManager.LICENSE_TYPE_HMDEBUG), ",");
		boolean wimbaDebug= (debugTransactions.contains(classware_hm.WIMBA_DEBUG));
		
		type= QUESTION_TYPE_worksheet;
		answers= new VectorAdapter();
		
		explanation= theOriginal.explanation;
		
		// dup additional subclasses here
		for (int i=0; i< theOriginal.answers.size(); i++)
		{
			worksheet_answer theAnswer= (worksheet_answer)theOriginal.answers.elementAt(i);
			answers.addElement( theAnswer.duplicate());
		}
		if(wimbaDebug){
			this.wimbaEnabled = theOriginal.wimbaEnabled;
		}
	}
	
	
	// text import - none
	
	// Bundler import - none	

	// Blackboard 6 input - none
	
	// Examview 4 input - none
	

	
	/**
	* read a worksheet question from an InputStream.
	* @param	theInput	the stream from which to read the object
	* @param	format	the format of the data in the stream
	*/
	public worksheet( DataInputStream theInput, int format )
		throws testFormatException
	{
		super( theInput, format );
		
		type= QUESTION_TYPE_worksheet;
		answers= new VectorAdapter();

		try 
		{
			//explanation= theInput.readUTF();
			explanation= tp_sql.readString(theInput);
			
			int count= theInput.readInt();
			for (int i=0 ; i< count; i++)
			{
				worksheet_answer theAnswer= worksheet_answer.read(theInput, format);
				if (theAnswer != null) answers.addElement( theAnswer );
			}
			
			if (count != answers.size())
				_logger.info("count should be " + count + "; it actually is " + answers.size());
			
			// read additional subclasses here
		} 
		catch (IOException e) 
		{
			_logger.error("worksheet question reading error", e);
			throw (new testFormatException( "IOException reading worksheet question" ) );
		}
	}
	
	
	public void write( DataOutputStream out ) 
		throws testFormatException
	{
		try 
		{
			super.write( out );
			
			//out.writeUTF( explanation );
			tp_sql.writeString( explanation, out );
			
			out.writeInt( answers.size() );
			for (int i=0; i<answers.size(); i++)
			{
				worksheet_answer theAnswer= (worksheet_answer)answers.elementAt(i);
				theAnswer.write( out );
			}
				
			// write additional subclasses here
		} 
		catch (IOException e) 
		{
			throw ( new testFormatException( "IOException writing worksheet question" ) );
		}
	}


	public String typeString() 
	{
		return( "worksheet" );
	}
	
	public String formalTypeString() 
	{
		return( "Worksheet&nbsp;Question" );
	}
	
	
	public String formalTypeString2( tp_requestHandler theHandler )
	{
		String customType= questionProperties.getString(CUSTOM_TYPE, "Worksheet");
		if (customType.length() == 0) customType= "Worksheet";
		if (!customType.equals("Worksheet")) return customType;
		
		for (int i=0; i<answers.size(); i++)
		{
			worksheet_answer theAnswer= (worksheet_answer)answers.elementAt(i);
			if (theAnswer.type == worksheet_answer.ANSWER_TYPE_flash)
			{
				String theType= ((worksheet_answer_flash)theAnswer).getTypeMapping( theHandler );
				if (theType != null) return( theType );
			}
			else if (theAnswer.type == worksheet_answer.ANSWER_TYPE_external)
			{
				String theType= theAnswer.answerProperties.getString(worksheet_answer_external.CUSTOM_TYPE, null);
				if (theType != null) return( theType );
			}
		}
		
		return( "Worksheet" );
	}

	public String formalTypeString2()
	{
		String customType= questionProperties.getString(CUSTOM_TYPE, "Worksheet");
		if (customType.length() == 0) customType= "Worksheet";
		if (!customType.equals("Worksheet")) return customType;
		
		for (int i=0; i<answers.size(); i++)
		{
			worksheet_answer theAnswer= (worksheet_answer)answers.elementAt(i);
			if (theAnswer.type == worksheet_answer.ANSWER_TYPE_flash)
			{
				String theType= ((worksheet_answer_flash)theAnswer).getTypeMapping();
				if (theType != null) return( theType );
			}
			else if (theAnswer.type == worksheet_answer.ANSWER_TYPE_external)
			{
				String theType= theAnswer.answerProperties.getString(worksheet_answer_external.CUSTOM_TYPE, null);
				if (theType != null) return( theType );
			}
		}
		
		return( "Worksheet" );
	}
	
	public String typeStringSmall() 
	{
		return( "work" );
	}
	
	public boolean supportsFastGrading(tp_requestHandler theHandler)
	{
		if (!scoring(theHandler).equals("manual")) return false;
		
		if (worksheet_answer.supportsFastGrading(theHandler, this)) return true;
		
		return false;
	}
	
	public boolean rendersOwnStem()
	{
		return(true);
	}

	public boolean supportsSideBySide()
	{
		return true;
	}

	public void addEmptyVars( parameters theParams )
	{
		theParams.replaceParam( "Q_" + sqlID, "-2-2");
	}


	public void buildReferences( VectorAdapter references ) 
	{
		richMedia.buildReferences( references, qtext );
		buildHintReferences( references );
		buildTooltipReferences( references );
		richMedia.buildReferences( references, explanation );
		//richMedia.buildReferences( references, questionProperties.getString(ATTACHED_MEDIA, "") );
		richMedia.buildReferencesAttachMedia( references, questionProperties.getString(ATTACHED_MEDIA, "") );
		richMedia.buildReferences( references, questionProperties.getString(COMMON_FEEDBACK, "") );
		richMedia.buildReferences( references, questionProperties.getString(PROBLEM_SOLUTION, "") );
		// media in pooled random variables
		for (int i=0 ; i<localRandoms.size() ; i++) 
		{
			randomVariable theVar= (randomVariable)localRandoms.elementAt(i);
			richMedia.buildReferences(references, theVar.getPoolString());
		}

		for (int i=0; i<answers.size(); i++)
		{
			worksheet_answer theAnswer= (worksheet_answer)answers.elementAt(i);
			theAnswer.buildReferences( references );
		}
	}


	public boolean consumesMedia( String theMedia )
	{
		return((qtext.indexOf(theMedia) >= 0) || (explanation.indexOf(theMedia) >= 0));
	}
	
	
	public void updateMedia( String oldName, String newName)
	{
		super.updateMedia(oldName, newName);
		explanation= tp_utils.substitute(explanation, "%media:" + oldName + "%", "%media:" + newName + "%");
	}


	/*
	public String v5elxTitle() { return("Worksheet Question"); }
	
	public String v5elxTemplate() { return("v5wk.xsl"); }
	
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

		result += "	<explanation><p xmlns=\"http://www.w3.org/1999/xhtml\">\r";
		result += toXMLhtml(explanation, theHandler);
		result += "	</p></explanation>\r";
	
		result += "</question>\r";
				
		return(result);
	}
	*/

	public void v5editOthers( tp_requestHandler theHandler, test theTest )
	{
		theHandler.snd("<a style=\"font-weight: bold\" onMouseOver=\"(window.status='get help...'); return true;\" href=\"javascript:helpMessage('v5/qwkanswers.htm');\">Worksheet Answers</a><br>");

		String contentURL= theTest.getMyURL(theHandler);
		contentURL += "?" + test.REQUEST + "=" + v5test.v5_EDITOTHERS;
		contentURL += "&" + v5test.v5_EDITOTHERS + "=" + sqlID;
		
		theHandler.snd("<IFRAME NAME=\"answers" + sqlID + "\" WIDTH=\"90%\" HEIGHT=\"200\" MARGINWIDTH=\"0\" MARGINHEIGHT=\"0\" FRAMEBORDER=\"1\" BORDER=\"1\" SRC=\"" + contentURL + "\">Requires a modern browser - e.g. Safari 1, Netscape 6 or IE 5</IFRAME><br>&nbsp;");
	}
	
	
	public void v5editOthersFrame( tp_requestHandler theHandler, test theTest )
	{
		String answerDelete= theHandler.getParam("OTHERdelete");
		//System.out.println("Deleting " + answerDelete);
		if (answerDelete.length() > 0)
		{
			for (int i=0; i<answers.size(); i++)
			{
				worksheet_answer thisOne= (worksheet_answer)answers.elementAt(i);
				if (thisOne.name.equals(answerDelete))
				{
					answers.removeElementAt(i);
				
					maxPoints= answers.size();

					try {
						theTest.sqlRoutine.updateQuestion(this, theTest.questions);
						//TCS add start for QuestionXref Model
						QuestionXrefUtil.createXrefFromNewQuestion(this, theHandler, theTest.sqlID);
						//TCS add start for QuestionXref Model
					} catch (SQLException s) {
						theTest.sqlRoutine.reportException("worksheet.v5editOther", s);
					}
					break;
				}
			}
		}
		
		tp_sortedList varList= new tp_sortedList();

		int weightTotal= 0;
		for (int i=0 ; i<answers.size() ; i++) 
		{
			worksheet_answer theAnswer= (worksheet_answer)answers.elementAt(i);
			varList.add( theAnswer.name, theAnswer.listEntry() );
			weightTotal += theAnswer.points;
		}
		
		/*
		String editURL= theTest.getMyURL(theHandler);
		editURL += "?" + test.REQUEST + "=" + test.v5_EDITother;
		editURL += "&" + test.v5_EDITOTHERS + "=" + sqlID;
		*/
		String editURL= theHandler.testURL + "?" + v5edit.stdURLVars( v5edit.EDIT_ADDENDA ) + "&" + v5edit.QID + "=" + sqlID;
		
		String scripts= "";
		scripts += "var editingWindow= null;\r";
		
		scripts += "function newAnswer( theType )\r";
		scripts += "{\r";
		scripts += "  theURL= '" + editURL + "&OTHERtype=' + theType;\r";
		//scripts += "  alert('start new answer: ' + theURL);\r";
		scripts += "  editingWindow= window.open( theURL, 'edita', 'toolbar=no,location=no,directories=no,status=no,scrollbars=yes,resizable=yes,copyhistory=no,width=600,height=500,screenX=50,screenY=50,left=50,top=50' );\r";
		scripts += "  editingWindow.focus();\r";
		scripts += "}\r";

		scripts += "function editAnswer( varname )\r";
		scripts += "{\r";
		scripts += "  theURL= '" + editURL + "&OTHERname=' + varname;\r";
		//scripts += "  alert('editing answer ' + theURL);\r";
		scripts += "  editingWindow= window.open( theURL, 'edita', 'toolbar=no,location=no,directories=no,status=no,scrollbars=yes,resizable=yes,copyhistory=no,width=600,height=500,screenX=50,screenY=50,left=50,top=50' );\r";
		scripts += "  editingWindow.focus();\r";
		scripts += "}\r";

		scripts += "function deleteAnswer( varname )\r";
		scripts += "{\r";
		scripts += "  if (!confirm('Are you sure you wish to delete the answer named ' + varname)) return;\r";
		scripts += "  if (editingWindow != null) {\r";
		scripts += "    if (!editingWindow.closed) editingWindow.close();\r";
		scripts += "    editingWindow= null;\r";
		scripts += "  }\r";
		scripts += "  document.answerForm.OTHERdelete.value= varname;\r";
		scripts += "  document.answerForm.submit();\r";
		scripts += "}\r";

		scripts += "function closeChildren()\r";
		scripts += "{\r";
		scripts += "  if (editingWindow != null) {\r";
		scripts += "    if (!editingWindow.closed) editingWindow.close();\r";
		scripts += "    editingWindow= null;\r";
		scripts += "  }\r";
		scripts += "}\r";

		theHandler.theResponse.setHeader("Content-Type","text/html; charset=UTF-8");
		
		theHandler.snd("<html><head><title>Additional Info Editing</title>");
		theHandler.snd(authentication.NO_CACHE_HTML);
		v5ui.selectStyleSheet(theHandler, "WHITE");
		v5ui.script(theHandler, scripts);
		theHandler.snd("</head><body style=\"border: 1px solid #000000\" onbeginunload=\"closeChildren();\"><form name=\"answerForm\" action=\"" + theHandler.testURL + "\">");
		
		theHandler.snd("<table width=\"100%\" cellspacing=\"0\" cellpadding=\"2\">");
		theHandler.snd("<tr><th>Name</th><th>Type</th><th>Weight</th><th>Info</th>");
		theHandler.snd("<th style=\"text-align: right\">");
		theHandler.snd("<a title=\"get help on worksheet questions and answers\" href=\"javascript:helpMessage('v5/workanstype.htm');\" style=\"color: #d0d0ff; font-weight: bold\">Help</a>&nbsp;&nbsp;");
		
		theHandler.snd("<input title=\"create new answer\" type=\"button\" name=\"bogus1\" value=\"New\" onClick=\"newAnswer( document.answerForm.answerType.selectedIndex );\">");
		
		theHandler.snd("&nbsp;<select name=\"answerType\">" + worksheet_answer.v5typeOptions() + "</select>");
		theHandler.snd("</th></tr>");
		
		if (varList.size() == 0)
		{
			theHandler.snd("<tr><td colspan=\"4\">No answers defined</td></tr>");
		}
		else
		{
			Enumeration entries= varList.sortedValues();
			for (int i=0; entries.hasMoreElements(); i++)
			{
				if ((i % 2) == 0) 
					theHandler.snd("<tr class=\"tr2\">");
				else 
					theHandler.snd("<tr class=\"tr1\">");
				
				theHandler.snd((String)entries.nextElement() + "</tr>");
			}
		}
		
		if (varList.size() > 0)
			theHandler.snd("<tr><th>" + Integer.toString(varList.size()) + "&nbsp;answers</th><th>&nbsp;</th><th>" + Integer.toString(weightTotal) + "</th><th>&nbsp;</th><th>&nbsp;</th></tr>");
		
		theHandler.snd("</table>");
		
		theHandler.snd( v5edit.stdFormVars(v5edit.ADDENDA) );
		theHandler.snd("<input type=\"hidden\" name=\"" + v5edit.QID + "\" value=\"" + sqlID + "\">");
		theHandler.snd("<input type=\"hidden\" name=\"OTHERdelete\" value=\"\">");
		
		theHandler.snd("</form></body></html>");
	}


	public void v5editOther( tp_requestHandler theHandler, test theTest )
	{
		worksheet_answer theAnswer= null;
		
		String newParam= theHandler.getParam("OTHERtype");
		if (newParam.length() != 0)
		{
			theAnswer= worksheet_answer.v5newAnswer( newParam );
			
			if (theAnswer != null)
			{
				String newName= "new";
				int theTry= 1;
				boolean found= false;
				do
				{
					found= false;
					for (int i=0; i<answers.size(); i++)
					{
						worksheet_answer thisOne= (worksheet_answer)answers.elementAt(i);
						if (thisOne.name.equals(newName))
						{
							found= true;
							theTry++;
							newName= "new" + Integer.toString(theTry);
							break;
						}
					}
				} while (found);
				
				theAnswer.name= newName;
				answers.addElement( theAnswer );
				
				maxPoints= answers.size();
				
				try {
					theTest.sqlRoutine.updateQuestion(this, theTest.questions);
					//TCS add start for QuestionXref Model
					QuestionXrefUtil.createXrefFromNewQuestion(this, theHandler, theTest.sqlID);
					//TCS add start for QuestionXref Model
				} catch (SQLException s) {
					theTest.sqlRoutine.reportException("worksheet.v5editOther", s);
				}
			}
		}
		else
		{
			String searchName= theHandler.getParam("OTHERname");
			
			for (int i=0; i<answers.size(); i++)
			{
				worksheet_answer thisOne= (worksheet_answer)answers.elementAt(i);
				if (thisOne.name.equals(searchName))
				{
					theAnswer= thisOne;
					break;
				}
			}
		}
		
		if (theAnswer != null)
		{
			theAnswer.v5edit(theHandler, theTest, this);
		}
		else
		{
			theHandler.errorMessage("undefined answer for editing");
		}
	}


	public void v5updateOther( tp_requestHandler theHandler, test theTest )
	{
		//String whichAnswer= theHandler.getParam(test.v5_UPDATEother);
		String whichAnswer= theHandler.getParam("OTHERname");
		if (whichAnswer.length() > 0) 
		{
			for (int i=0; i<answers.size(); i++)
			{
				worksheet_answer thisAnswer= (worksheet_answer)answers.elementAt(i);
				if (thisAnswer.name.equals(whichAnswer))
				{
					thisAnswer.v5updateOther( theHandler, theTest, this );
					break;
				}
			}
		}		
		
		v5editOthersFrame( theHandler, theTest );
		return;
	}


	public void v5update( tp_requestHandler theHandler, test theTest )
	{
		//String theXMLdata= theHandler.getParam("questionxml");
		String theXMLdata= theHandler.getParam("content");
		if (theXMLdata.length() == 0)
		{
			//v5edit( theHandler, theTest );
			_logger.error("update failure in worksheet.v5update");
			return;
		}
		
		//System.out.println(theXMLdata);
		
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
					explanation= embeddedMedia.processMediaIn( theHandler, theTest, getXMLhtml(thisItem, theHandler), sqlID );
			}
		}
		catch (IOException io)
		{
			_logger.error("XML IO error in worksheet.v5update()", io);
		}
		catch (JDOMException jd) 
		{
			_logger.error("XML parsing error in worksheet.v5update()", jd);
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
	
	
	public String v5paperHTML( tp_requestHandler theHandler, test theTest, parameters theData, String substitutedText, VectorAdapter keyItem )
	{
		String qid= "Q_" + sqlID;
		
		String resultHTML= "";
		if (substitutedText == null) return(resultHTML);
		
		resultHTML += theTest.getGUI().questionFontStart();
		resultHTML += "<SPAN class=\"qStyle\">";
		resultHTML += worksheet_answer.v5makeSubstitutions(substitutedText, theData, answers, qid);
		resultHTML += "</SPAN>";
		resultHTML += theTest.getGUI().questionFontEnd();
		
		String keyHTML= theTest.getGUI().questionFontStart();
		keyHTML += "<SPAN class=\"qStyle\">";
		keyHTML += worksheet_answer.v5correctSubstitutions(substitutedText, theTest, theData, answers, this);
		keyHTML += "</SPAN>";
		keyHTML += theTest.getGUI().questionFontEnd();
		
		keyItem.addElement(keyHTML);

		return( resultHTML );
	}
	
	
	/* no need to implement v5rtf as the question superclass will simply call v5paperHTML */


	/* WebCT and Blackboard are incapable for offering this sort of question */


	public void v5info( tp_requestHandler theHandler, test theTest, rtf_pdf_output theWriter )
	{
		theWriter.add(formalTypeString() + "<br />");
		
		if (answers.size() == 0)
			theWriter.add("<span style=\"color: red\">Error: </span>no worksheet answers defined<br />");
		else
		{
			theWriter.add(Integer.toString(answers.size()) + " worksheet answers<br />");
			
			for (int i=0; i< answers.size(); i++)
			{
				worksheet_answer theAnswer= (worksheet_answer)answers.elementAt(i);
				theWriter.add(theAnswer.v5info(this));
			}
		}
		
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
		
		
		if (answers.size() == 0)
			result += "<span style=\"color: red\">Error: </span>no worksheet answers defined<br />";
		else
		{
			String entries= "";
			int errorCount= 0;
			for (int i=0; i< answers.size(); i++)
			{
				worksheet_answer theAnswer= (worksheet_answer)answers.elementAt(i);
				String thisError= theAnswer.v5diagnosticInfo(this);
				if (thisError.length() > 0)
				{
					errorCount++;
					entries += thisError;
				}
			}
			
			if (errorCount > 0)
				result += "<span style=\"color: red\">Error: </span>" + Integer.toString(errorCount) + " of " + Integer.toString(answers.size()) + " worksheet answers defined have problems<br />" + entries;
		}

		return(result);
	}

	
	public String v6show( tp_requestHandler theHandler, test theTest, submission theSubmission, parameters theData, String substitutedText ) 
	{
		String theQID= "Q_" + sqlID;
		
		boolean recallOK= false;
		if ((theSubmission != null) && theTest.getDefaultAction().feedbackOnRecall && (theTest.getSecurity().recallTime > 0))
		{
			long now= (new java.util.Date()).getTime();
			recallOK= (now > theTest.getSecurity().recallTime);
		}
			
		String resultHTML= "<div class=\"rspStyle\">";
		
		resultHTML += "<input type=\"hidden\" name=\"" + theQID + "\" value=\"-2-2\">";
		v7media.fixAttachedMediaForRender(theHandler, this);
		
		/**
		 * Added for HT 510
		 * This piece of code modified for MMS external media handling for worksheet questions.
		 * for Worksheet question, we are adding one extra parameter to showMedia URL to distinguish it from other
		 * showMedia URL created and handled by EZTEST.
		 * Use case:- Learning Object or Worksheet Question(NQI) can have flv media.
		 * In this case, even if the flv media is processed by MMS system, Eztest should pass original 
		 * flv content to the tools rather than the MP4 media
		 * */
		if (theData.getParam("sideBYside").length() > 0){
			if (!theData.getParam("mode").equals("preview")){
				//resultHTML += "<input type=\"hidden\" name=\"" + theQID + "_media\" id=\"" + theQID + "_media\" value=\"" + tp_utils.substitute(theTest.getMyMediaURL(theHandler), "SHOWmedia&amp;media", "SHOWmedia&media") + "," + myAttachedMedia() + "\">";
				resultHTML += "<input type=\"hidden\" name=\"" + theQID + "_media\" id=\"" + theQID + "_media\" value=\"" + tp_utils.substitute(theTest.getMyMediaWorksheetURL(theHandler), "SHOWmedia&amp;showActualMedia=true&amp;media", "SHOWmedia&showActualMedia=true&media") + "," + myAttachedMedia() + "\">";
			}
		}
		else{
			//resultHTML += "<input type=\"hidden\" name=\"" + theQID + "_media\" id=\"" + theQID + "_media\" value=\"" + tp_utils.substitute(theTest.getMyMediaURL(theHandler), "SHOWmedia&amp;media", "SHOWmedia&media") + "," + myAttachedMedia() + "\">";
			resultHTML += "<input type=\"hidden\" name=\"" + theQID + "_media\" id=\"" + theQID + "_media\" value=\"" + tp_utils.substitute(theTest.getMyMediaWorksheetURL(theHandler), "SHOWmedia&amp;showActualMedia=true&amp;media", "SHOWmedia&showActualMedia=true&media") + "," + myAttachedMedia() + "\">";
		}
		
		String started= theData.getParam(theQID + "_started");
		if (started.length() == 0) started= theHandler.getParam(theQID + "_started");
		if (started.length() == 0) started= "0";
		
		if (theHandler.getParam(v7test_instructor.v6TEST_MGMT).equals(v7scoring.v6ANALYSIS))
			resultHTML += substitutedText;
		else
			resultHTML += worksheet_answer.v6show( theHandler, theTest, theSubmission, theData, substitutedText, this);
			
		if (theSubmission != null)
		{
			boolean feedbackCheck= theTest.getDefaultAction().returnFeedback;
			if (theHandler.isHMrequest()) feedbackCheck= classware_hm.showExplanation(theHandler, this);

			if (theHandler.isManager || feedbackCheck || recallOK)
			{
				String theFeedback= explanation + connectSolution(theHandler);
				
				if (theFeedback.length() > 0)
				{
					theFeedback= tooltipDeReference(theFeedback);
					theFeedback= randomVariable.deReference(theFeedback, theTest, sqlID, theData);
					theFeedback= richMedia.deReference(theFeedback, theHandler, sqlID, theData);
					
					if (theFeedback.trim().length() > 0)
					{
						boolean showFeedback= true;
						if (theHandler.isHMrequest() && (theData.getParam("sideByside").length() == 0) && (theHandler.getParam(classware_hm.PREVIEW_MODE).length() == 0))
							showFeedback= false;
						if (theHandler.getParam(classware_hm.PREVIEW_QUESTION_MODE).equals(classware_hm.PREVIEW_QUESTION_MODE)) showFeedback= true;

						boolean pregrading= theHandler.getParam(v7test_student.PREGRADE).equals(sqlID);
						if (pregrading) showFeedback= false;

						resultHTML += "<br>&nbsp;<br>";
						if (showFeedback)
							resultHTML += "<span style=\"font-size: 9pt; font-style: italic\">Explanation:</span><div class=\"feedback_block\">" + theFeedback + "</div>";
					}
				}
			}
			
			response theResponse= theSubmission.getResponse(sqlID);
			if ((theResponse != null) && !theData.getParam("mode").equals("preview")) resultHTML += showComment( theHandler, theSubmission, theResponse );
		}

		/**
		 * Added for wimba changes
		 */
		if(this.wimbaEnabled || this.isZeusTool() || this.isFormTool()){
			resultHTML += supplementaryInfo( theHandler, theSubmission, theData, false );
		}else{
			resultHTML += supplementaryInfo( theHandler, theSubmission, theData, true );
		}	
		resultHTML += "<br>&nbsp;<br></div>";
		
		return(resultHTML);
	}
	public String v6show( tp_requestHandler theHandler, test theTest, PartialTO partialTO, String substitutedText ) 
	{
		String theQID= "Q_" + sqlID;
		CustomMap<String, String> testParamMap = (CustomMap<String, String>)partialTO.getTestParameter();
		QuestionParameters questionParams = partialTO.getQuestionTO().getQuestionMap().get(sqlID);
		if(questionParams == null){
			questionParams = new QuestionParameters();
			partialTO.getQuestionTO().getQuestionMap().put(sqlID, questionParams);
		}
		
		boolean recallOK= false;
		if ((partialTO.isSubmission()) && theTest.getDefaultAction().feedbackOnRecall && (theTest.getSecurity().recallTime > 0))
		{
			long now= (new java.util.Date()).getTime();
			recallOK= (now > theTest.getSecurity().recallTime);
		}
			
		String resultHTML= "<div class=\"rspStyle\">";
		
		resultHTML += "<input type=\"hidden\" name=\"" + theQID + "\" value=\"-2-2\">";
		v7media.fixAttachedMediaForRender(theHandler, this);
		/**
		 * Added for HT 510
		 * This piece of code modified for MMS external media handling for worksheet questions.
		 * for Worksheet question, we are adding one extra parameter to showMedia URL to distinguish it from other
		 * showMedia URL created and handled by EZTEST.
		 * Use case:- Learning Object or Worksheet Question(NQI) can have flv media.
		 * In this case, even if the flv media is processed by MMS system, Eztest should pass original 
		 * flv content to the tools rather than the MP4 media
		 * */
		if (testParamMap.getParam("sideBYside").length() > 0){
			if (!testParamMap.getParam("mode").equals("preview")){
				//resultHTML += "<input type=\"hidden\" name=\"" + theQID + "_media\" id=\"" + theQID + "_media\" value=\"" + tp_utils.substitute(theTest.getMyMediaURL(theHandler), "SHOWmedia&amp;media", "SHOWmedia&media") + "," + myAttachedMedia() + "\">";
				resultHTML += "<input type=\"hidden\" name=\"" + theQID + "_media\" id=\"" + theQID + "_media\" value=\"" + tp_utils.substitute(theTest.getMyMediaWorksheetURL(theHandler), "SHOWmedia&amp;showActualMedia=true&amp;media", "SHOWmedia&showActualMedia=true&media") + "," + myAttachedMedia() + "\">";
			}
		}
		else{
			//resultHTML += "<input type=\"hidden\" name=\"" + theQID + "_media\" id=\"" + theQID + "_media\" value=\"" + tp_utils.substitute(theTest.getMyMediaURL(theHandler), "SHOWmedia&amp;media", "SHOWmedia&media") + "," + myAttachedMedia() + "\">";
			resultHTML += "<input type=\"hidden\" name=\"" + theQID + "_media\" id=\"" + theQID + "_media\" value=\"" + tp_utils.substitute(theTest.getMyMediaWorksheetURL(theHandler), "SHOWmedia&amp;showActualMedia=true&amp;media", "SHOWmedia&showActualMedia=true&media") + "," + myAttachedMedia() + "\">";
		}
		
		CustomMap<String, String> questionParameters = (CustomMap<String, String>)questionParams.getQuestionParameters();
		String started= questionParameters.getParam(theQID + "_started");
		if (started.length() == 0) started= theHandler.getParam(theQID + "_started");
		if (started.length() == 0) started= "0";
		
		if (theHandler.getParam(v7test_instructor.v6TEST_MGMT).equals(v7scoring.v6ANALYSIS))
			resultHTML += substitutedText;
		else
			resultHTML += worksheet_answer.v6show( theHandler, theTest, partialTO, substitutedText, this);
			
		if (partialTO.isSubmission())
		{
			boolean feedbackCheck= theTest.getDefaultAction().returnFeedback;
			if (theHandler.isHMrequest()) feedbackCheck= classware_hm.showExplanation(theHandler, this);

			if (theHandler.isManager || feedbackCheck || recallOK)
			{
				String theFeedback= explanation + connectSolution(theHandler);
				
				if (theFeedback.length() > 0)
				{
					theFeedback= tooltipDeReference(theFeedback);
					theFeedback= randomVariable.deReference(theFeedback, theTest, sqlID, partialTO);
					theFeedback= richMedia.deReferenceNew(theFeedback, theHandler, sqlID, partialTO);
					
					if (theFeedback.trim().length() > 0)
					{
						boolean showFeedback= true;
						if (theHandler.isHMrequest() && (testParamMap.getParam("sideByside").length() == 0) && (theHandler.getParam(classware_hm.PREVIEW_MODE).length() == 0))
							showFeedback= false;
						if (theHandler.getParam(classware_hm.PREVIEW_QUESTION_MODE).equals(classware_hm.PREVIEW_QUESTION_MODE)) showFeedback= true;

						boolean pregrading= theHandler.getParam(v7test_student.PREGRADE).equals(sqlID);
						if (pregrading) showFeedback= false;

						resultHTML += "<br>&nbsp;<br>";
						if (showFeedback)
							resultHTML += "<span style=\"font-size: 9pt; font-style: italic\">Explanation:</span><div class=\"feedback_block\">" + theFeedback + "</div>";
					}
				}
			}
			if (questionParams != null && !testParamMap.getParam("mode").equals("preview")) {
				resultHTML += showComment( theHandler, questionParams );
			}
			
			//response theResponse= theSubmission.getResponse(sqlID);
			//if ((theResponse != null) && !theData.getParam("mode").equals("preview")) resultHTML += showComment( theHandler, theSubmission, theResponse );
		}

		/**
		 * Added for Wimba changes
		 */
		if(this.wimbaEnabled || this.isZeusTool() || this.isFormTool()){
			resultHTML += supplementaryInfo( theHandler, partialTO, false );
		}else{
			resultHTML += supplementaryInfo( theHandler, partialTO, true );
		}
			
		resultHTML += "<br>&nbsp;<br></div>";
		
		return(resultHTML);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.mcgrawhill.ezto.test.questions.question#itemScoringMode()
	 */
	@Override
	public boolean isZeusTool(){
		int answerSize = answers.size();
		for (int i=0; i<answerSize; i++){
			worksheet_answer thisAnswer= (worksheet_answer)answers.elementAt(i);
			if (!worksheet_answer.usedInQuestion(thisAnswer.name, this)){
				continue;
			}
			if(thisAnswer.type == worksheet_answer.ANSWER_TYPE_external){
				try{
					if(((worksheet_answer_external)thisAnswer).isZeusTool()){
						return true;
					}
				}catch (Exception e) {
					_logger.error("Error in checkZeusLearningObject ",e);
				}
			}
		}
		return false;
	}
	
	
	@Override
	public boolean isFormTool() {
		int answerSize = answers.size();
		for (int i=0; i<answerSize; i++){
			worksheet_answer thisAnswer= (worksheet_answer)answers.elementAt(i);
			if (!worksheet_answer.usedInQuestion(thisAnswer.name, this)){
				continue;
			}
			if(thisAnswer.type == worksheet_answer.ANSWER_TYPE_external){
				try{
					if(((worksheet_answer_external)thisAnswer).isFormTool()){
						return true;
					}
				}catch (Exception e) {
					_logger.error("Error in isFormTool ",e);
				}
			}
		}
		return false;
	
	}
	
	@Override
	public boolean isWimbaTool() {
		int answerSize = answers.size();
		for (int i=0; i<answerSize; i++){
			worksheet_answer thisAnswer= (worksheet_answer)answers.elementAt(i);
			if (!worksheet_answer.usedInQuestion(thisAnswer.name, this)){
				continue;
			}
			if(thisAnswer.type == worksheet_answer.ANSWER_TYPE_external){
				try{
					if(((worksheet_answer_external)thisAnswer).isWimbaQuestion()){
						return true;
					}
				}catch (Exception e) {
					_logger.error("Error in isWimbaTool ",e);
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean isOnlyWorksheetEssay(){
		boolean onlyWkEssay = false;
		int answerSize = answers.size();
		
		for (int i=0; i<answerSize; i++){
			worksheet_answer thisAnswer= (worksheet_answer)answers.elementAt(i);
			if (!worksheet_answer.usedInQuestion(thisAnswer.name, this)){
				continue;
			}
			if(thisAnswer.type == worksheet_answer.ANSWER_TYPE_essay){
				onlyWkEssay = true;
			}else{
				onlyWkEssay = false;
				break;
			}
		}
		return onlyWkEssay;
	}
	
	@Override
	public boolean isOnlyNonCMWAnswerType () throws Exception{
		boolean onlyNonCMWWk = false;
		int answerSize = answers.size();
		
		for (int i = 0; i<answerSize; i++){
			worksheet_answer thisAnswer= (worksheet_answer)answers.elementAt(i);
			if (!worksheet_answer.usedInQuestion(thisAnswer.name, this)){
				continue;
			}
			if(thisAnswer.type == worksheet_answer.ANSWER_TYPE_essay || thisAnswer.type == worksheet_answer.ANSWER_TYPE_trueFalse || thisAnswer.type == worksheet_answer.ANSWER_TYPE_checkAll){
				onlyNonCMWWk = true;
			}else{
				onlyNonCMWWk = false;
				break;
			}
		}
		return onlyNonCMWWk;
	}
	
	public boolean supportsAggregatedGrading(tp_requestHandler theHandler) {
		if(isAlgorithmic(theHandler)){
			return false;
		}
		
		if(questionProperties.getBoolean( AGGREGATED_GRADING, false )){
			return supportsAggregatedGradingWorksheetAnswers();
		}
		
		return false;		
	}

	private boolean supportsAggregatedGradingWorksheetAnswers(){
		for (int i=0; i<answers.size(); i++)
		{
			worksheet_answer thisAnswer= (worksheet_answer)answers.elementAt(i);
			
			if(thisAnswer.type == worksheet_answer.ANSWER_TYPE_essay && thisAnswer.points > 0 ){
				return true;
			}
			if(thisAnswer.type == worksheet_answer.ANSWER_TYPE_avRecording || thisAnswer.type == worksheet_answer.ANSWER_TYPE_videoVoiceover){
				if(thisAnswer.answerProperties != null){
					boolean completeIncompleteGrading = thisAnswer.answerProperties.getBoolean(question.COMPLETE_INCOMPLETE_GRADING, false);
					if( !completeIncompleteGrading ){
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	public void v6evaluate( tp_requestHandler theHandler, test theTest, submission theSubmission ) 
	{
		response thisResponse= new response( sqlID, maxPoints, "" );
		theSubmission.addResponse( thisResponse );
		
		worksheet_answer.v6evaluate( theHandler, theTest, theSubmission, thisResponse, this );
		
		String answeredFlag = theSubmission.getParam("Q_" + sqlID + "_answered");
		if( "yes".equals(theHandler.getParam(classware_hm.POLICY_participation)) && "true".equalsIgnoreCase(answeredFlag)){
			thisResponse.points = maxPoints;
		}
	}
	
	public void v6evaluate( tp_requestHandler theHandler, test theTest, PartialTO partialTO ) 
	{
		QuestionParameters questionParams = partialTO.getQuestionTO().getQuestionMap().get(sqlID);
		if(questionParams == null){
			questionParams = new QuestionParameters();
			partialTO.getQuestionTO().getQuestionMap().put(sqlID, questionParams);
		}
		questionParams.setQuestionID(sqlID);
		questionParams.setPointsMax(maxPoints);
		
		worksheet_answer.v6evaluate( theHandler, theTest, partialTO, this );
		
		CustomMap<String, String> questionParamsMap = (CustomMap<String, String>)questionParams.getQuestionParameters();		
		String answeredFlag = questionParamsMap.getParam("Q_" + sqlID + "_answered");
		if( "yes".equals(theHandler.getParam(classware_hm.POLICY_participation)) && "true".equalsIgnoreCase(answeredFlag) ){//if p_participation is set to yes award full credit.
			questionParams.setPoints(maxPoints);
		}
	}
	
	/**
	 * This method is responsible of evaluating the response of a WORKSHEET question based on all its
	 * answer types and policies
	 * @param  questionWiseResponseTO
	 * @param  policyTO
	 */
	@Override
	public void itemEvaluate(QuestionWiseResponseTO questionWiseResponseTO, PolicyTO policyTO) throws Exception {
		String answeredFlag = "";
		Map<String,String> policyMap = null;
		CustomMap<String, String> questionParameterMap = null;
		if(questionWiseResponseTO != null && policyTO != null){
			questionWiseResponseTO.setQuestionID(this.sqlID);
			questionWiseResponseTO.setPointsMax(classware_hm.DEFAULT_INTERNAL_POINTS);
			policyMap = policyTO.getPolicyMap();
		}
		worksheet_answer.itemWkAnswerEvaluate(questionWiseResponseTO, policyTO, this);
		questionParameterMap = (CustomMap<String, String>)questionWiseResponseTO.getQuestionParameters();
		if(questionParameterMap != null && policyMap != null){
			answeredFlag = questionParameterMap.getParam(new StringBuilder("Q_").append(this.sqlID).append(CaaConstants.ANSWERED).toString());
			if(CaaConstants.YES.equals(policyMap.get(classware_hm.POLICY_participation)) && CaaConstants.TRUE.equals(answeredFlag)){
				questionWiseResponseTO.setPoints(classware_hm.DEFAULT_INTERNAL_POINTS);
			}
		}
	}
	/**
	 * This method is wrapper method that gets all the responses of different answer types and converts 
	 * them into a map
	 * @param questionWiseResponseTO
	 * @param policyTO
	 */
	@Override
	public Map<String, Object> evaluateResponse(QuestionWiseResponseTO questionWiseResponseTO, PolicyTO policyTO) throws Exception {
		Map<String,Object> evaluateMap = new HashMap<String,Object>();
		JSONObject jsonObject = worksheet_answer.evaluateWkAnsResponse(questionWiseResponseTO, policyTO, this);
		evaluateMap.put(question.CORRECTNESS, jsonObject);
		return evaluateMap;
	}
	
	public void classwareEvaluate( tp_requestHandler theHandler, test theTest, submission theSubmission ) 
	{
		if (theHandler.debugTransactions.contains("worksheet"))
			_logger.info("worksheet.classwareEvaluate()");
		response thisResponse= new response( sqlID, maxPoints, "" );
		theSubmission.addResponse( thisResponse );
		
		worksheet_answer.classwareEvaluate( theHandler, theTest, theSubmission, thisResponse, this );

		String answeredFlag = theSubmission.getParam("Q_" + sqlID + "_answered");
		if( "yes".equals(theHandler.getParam(classware_hm.POLICY_participation)) && "true".equalsIgnoreCase(answeredFlag)){
			thisResponse.points = maxPoints;
		}
	}
	
	public void classwareEvaluate( tp_requestHandler theHandler, test theTest, PartialTO partialTO ) 
	{
		if (theHandler.debugTransactions.contains("worksheet"))
			_logger.info("worksheet.classwareEvaluate()");
		QuestionParameters questionParams = partialTO.getQuestionTO().getQuestionMap().get(sqlID);
		if(questionParams == null){
			questionParams = new QuestionParameters();
			partialTO.getQuestionTO().getQuestionMap().put(sqlID, questionParams);
		}
		questionParams.setQuestionID(sqlID);
		questionParams.setPointsMax(maxPoints);
		worksheet_answer.classwareEvaluate( theHandler, theTest, partialTO, this );
		
		CustomMap<String, String> questionParamsMap = (CustomMap<String, String>)questionParams.getQuestionParameters();		
		String answeredFlag = questionParamsMap.getParam("Q_" + sqlID + "_answered");
		if( "yes".equals(theHandler.getParam(classware_hm.POLICY_participation)) && "true".equalsIgnoreCase(answeredFlag) ){//if p_participation is set to yes award full credit.
			questionParams.setPoints(maxPoints);
		}
	}
	
	
	public boolean isMostlyCorrect( tp_requestHandler theHandler, submission theSubmission )
	{
		return worksheet_answer.isMostlyCorrect( theHandler, theSubmission, this );
	}


	public String v6pdf( tp_requestHandler theHandler, test theTest, submission theSubmission, parameters theData, String substitutedText ) 
	{
		String theQID= "Q_" + sqlID;
		
		boolean recallOK= false;
		if ((theSubmission != null) && theTest.getDefaultAction().feedbackOnRecall && (theTest.getSecurity().recallTime > 0))
		{
			long now= (new java.util.Date()).getTime();
			recallOK= (now > theTest.getSecurity().recallTime);
		}

		String resultHTML= "";
		
		theHandler.replaceParam(PDF, "true");
		
		resultHTML += worksheet_answer.v6show( theHandler, theTest, theSubmission, theData, substitutedText, this);
		
		if (theSubmission != null)
		{
			if (theHandler.isManager || theTest.getDefaultAction().returnFeedback || recallOK)
			{
				if (explanation.length() > 0)
				{
					String theFeedback= randomVariable.deReference(explanation, theTest, sqlID, theData);
					theFeedback= richMedia.deReference(theFeedback, theHandler, sqlID, theData);
					if (theFeedback.trim().length() > 0)
						resultHTML += "<br>&nbsp;<br><span style=\"font-size: 9pt; font-style: italic\">Explanation:</span><br>" + theFeedback + "<br>";
				}
			}
			
			response theResponse= theSubmission.getResponse(sqlID);
			if (theResponse != null)
			{
				if (theResponse.comment.length() > 0)
					resultHTML += "<p class=\"instructorComment\">" + theResponse.comment + "</p>";
			}
		}

		resultHTML += "<br>&nbsp;<br>";
		
		return(resultHTML);
	}
	
	
	public String v6analysis( tp_requestHandler theHandler, test theTest )
	{
		int offerCount= 0;
		int correctCount= 0;
		
		int [] ans_correct= new int[ answers.size() ];
		int [] ans_incorrect= new int[ answers.size() ];
		int [] ans_unanswered= new int[ answers.size() ];
		
		int meanTotal= 0;
		int meanTotalPct= 0;
		tp_sortedList medianList= new tp_sortedList();
		
		for (int i=0; i<answers.size(); i++)
		{
			ans_correct[i]= 0;
			ans_incorrect[i]= 0;
			ans_unanswered[i]= 0;
		}
		Connection con= null;
		PreparedStatement stmt = null;
		ResultSet rs= null;	
		try
		{
			con= theHandler.getConnection();
			//Statement stmt= con.createStatement();
			
			//ResultSet rs= stmt.executeQuery("SELECT points, maxpoints, response, recordedValue FROM responses WHERE testID='" + theTest.sqlID + "' AND questionID='" + sqlID + "'");
			stmt= con.prepareStatement("SELECT points, maxpoints, response, recordedValue FROM responses WHERE testID=? AND questionID=?");
			stmt.setString(1, theTest.sqlID);
			stmt.setString(2, sqlID);
			rs= stmt.executeQuery();
			while (rs.next())
			{
				Vector evaluations= tp_sql.vectorFromStream(rs.getBinaryStream("response"));
				Vector responses= tp_sql.vectorFromStream(rs.getBinaryStream("recordedValue"));
				if (responses.size() == 0) continue;	//unoffered
				
				offerCount++;
				
				int points= rs.getInt("points");
				int max= rs.getInt("maxpoints");
				if (points == max) correctCount++;
				
				meanTotal += points;
				meanTotalPct += (points * 100) / max;
				
				medianList.add( points, new Integer(points) );
				
				
				for (int i=0; i<answers.size(); i++)
				{
					if (i < evaluations.size())
					{
						String thisEval= (String)evaluations.elementAt(i);
						String thisResp= (String)responses.elementAt(i);
						
						if (thisEval.equals(worksheet_answer.EVALUATE_correct))
							ans_correct[i]++;
						else if (thisResp.equals("n/r") || (thisResp.length() == 0))
							ans_unanswered[i]++;
						else
							ans_incorrect[i]++;
					}
					else
						ans_unanswered[i]++;
				}
			}
						
			tp_sql.releaseResources(con, stmt, rs);
		}
		catch (SQLException s)
		{
			theTest.sqlRoutine.reportException("worksheet.v6analysis()", s);
		}finally{
			tp_sql.releaseResources(con, stmt, rs);
		}
	
		String theAnalysis= "<p style=\"margin-left: 30; margin-top: 0; margin-bottom: 20\">never offered</p>";
		
		if (offerCount > 0)
		{		
			int thePct= (100 * correctCount) / offerCount;
			theAnalysis= "<p style=\"margin-left: 30; margin-top: 0; margin-bottom: 20\">";
			theAnalysis += "Full credit awarded " + Integer.toString(correctCount) + " out of " + Integer.toString(offerCount) + " times (" + Integer.toString(thePct) + "%)<br>";
			theAnalysis += "Mean points: " + Integer.toString(meanTotal / offerCount) + "<br>";
			theAnalysis += "Mean percentage: " + Integer.toString(meanTotalPct / offerCount) + "%<br>";
			
			VectorAdapter theScores= medianList.sortedVector();
			theAnalysis += "Median points: " + ((Integer)theScores.elementAt(theScores.size()/2)).toString() + "<br>";
			
			theAnalysis += "<table width=\"500\" cellpadding=\"2\" cellspacing=\"0\">";
			theAnalysis += "<tr><th>Answer</th><th>Correct</th><th>Incorrect</th><th>Unanswered</th></tr>";
			
			for (int i=0; i<answers.size(); i++)
			{
				worksheet_answer theAnswer= (worksheet_answer)answers.elementAt(i);
				
				int total= ans_correct[i] + ans_incorrect[i] + ans_unanswered[i];
				
				String tableRow= "<tr style=\"font-size: 9pt\"\">";
				tableRow += "<td style=\"text-align: right\">" + theAnswer.name + "</td>";
				
				if (theAnswer.type == worksheet_answer.ANSWER_TYPE_labeledValues)
				{
					tableRow += "<td style=\"text-align: center\">" + Integer.toString(ans_correct[i]) + " (" + Integer.toString( (ans_correct[i] * 100) / total ) + "%)</td>";
					tableRow += "<td style=\"text-align: center\">" + Integer.toString(ans_incorrect[i]) + " (" + Integer.toString( (ans_incorrect[i] * 100) / total ) + "%)</td>";
					tableRow += "<td style=\"text-align: center\">n/a</td>";
				}
				else
				{
					tableRow += "<td style=\"text-align: center\">" + Integer.toString(ans_correct[i]) + " (" + Integer.toString( (ans_correct[i] * 100) / total ) + "%)</td>";
					tableRow += "<td style=\"text-align: center\">" + Integer.toString(ans_incorrect[i]) + " (" + Integer.toString( (ans_incorrect[i] * 100) / total ) + "%)</td>";
					tableRow += "<td style=\"text-align: center\">" + Integer.toString(ans_unanswered[i]) + " (" + Integer.toString( (ans_unanswered[i] * 100) / total ) + "%)</td>";
				}
				tableRow += "</tr>";
				
				theAnalysis += tableRow;
			}

			theAnalysis += "</table></p>";
		}
		
		return(theAnalysis);
	}
	
	
	/*
	public static void importXML( tp_requestHandler theHandler, test theTest, byte [] theXMLdata)
	{
		try 
		{
			SAXBuilder builder = new SAXBuilder();
			Document theXML = builder.build(new ByteArrayInputStream(theXMLdata));
			Element theQ= theXML.getRootElement();
			question theNewQ= importEDU( theQ );
			
			if (!theTest.questions.newQuestion( theNewQ, theHandler.getParam(test.USER_ID),  theHandler.getParam(test.USER_PW), theHandler )) 
			{
				theHandler.returnError( "Insufficient permission to add question" );
				return;
			}
			
			questionGroup defaultGroup= theTest.getDefaultGroup();
			defaultGroup.idlist.addElement(Integer.toString(theNewQ.id));
			defaultGroup.updateGroup( theHandler, theTest );
			
			theTest.sqlRoutine.saveTest( theTest );
			theHandler.theServlet.theLibrary.pruneTest(theTest.sqlID, null);
		}
		catch (IOException io)
		{
			System.out.println("XML IO error in worksheet.importXML()");
			io.printStackTrace();
		}
		catch (JDOMException jd) 
		{
			System.out.println("XML parsing error in worksheet.importXML()");
			jd.printStackTrace();
		}
	}
	*/
	
	// edu2xml input
	public static worksheet importEDU( Element theWorksheet )
	{
		worksheet result= new worksheet();
		
		String title= theWorksheet.getAttributeValue("title");
		if (title != null)
		{
			if (title.length() > 0) result.selectionTitle= title;
		}
		
		java.util.List theData= theWorksheet.getChildren();
		ListIterator iter= theData.listIterator();
		
		//System.out.println( "XML data has " + Integer.toString(theData.size()) + " primary elements");
		
		while (iter.hasNext()) 
		{
			Element thisItem= (Element)iter.next();
			//System.out.println("  " + thisItem.getName());
			
			if (thisItem.getName().equals("HTML"))
			{
				result.qtext= thisItem.getText();
			}
			
			else if (thisItem.getName().equals("VARIABLES"))
			{
				java.util.List varData= thisItem.getChildren();
				ListIterator iter2= varData.listIterator();
				
				//System.out.println( "  " + Integer.toString(varData.size()) + " variable declarations");
				
				while (iter2.hasNext()) 
				{
					Element thisVar= (Element)iter2.next();
					//System.out.println("    " + thisVar.getName());
					
					randomVariable newVar= null;
					
					if (thisVar.getName().equals("RANGE"))
					{
						String varName= thisVar.getAttributeValue("name");
						String varStart= thisVar.getAttributeValue("start");
						String varEnd= thisVar.getAttributeValue("end");
						String varIncrement= thisVar.getAttributeValue("increment");
						
						try
						{
							double startValue= (new Double(varStart)).doubleValue();
							double endValue= (new Double(varEnd)).doubleValue();
							double increment= (new Double(varIncrement)).doubleValue();
							newVar= new randomVariable( varName, startValue, endValue, increment );
						}
						catch (NumberFormatException ignore) {}
					}
					
					else if (thisVar.getName().equals("CONSTANT"))
					{
						String varName= thisVar.getAttributeValue("name");
						String varValue= thisVar.getAttributeValue("value");
						newVar= new randomVariable( varName, varValue, true );
					}
					
					else if (thisVar.getName().equals("CALCULATION"))
					{
						String varName= thisVar.getAttributeValue("name");
						String varExpression= thisVar.getAttributeValue("expression");
						newVar= new randomVariable( varName, varExpression, true );
					}
					
					if (newVar != null)
						result.localRandoms.addElement(newVar);
				}
			}

			// CONDITIONS - currently disable due to expression incompatability
			/*
			else if (thisItem.getName().equals("CONDITIONS"))
			{
				java.util.List varData= thisItem.getChildren();
				ListIterator iter2= varData.listIterator();
				
				//System.out.println( "  " + Integer.toString(varData.size()) + " condition declarations");
				
				randomVariable newVar= new randomVariable( "CONDITIONALS", 1, 10, 1 );
				
				while (iter2.hasNext()) 
				{
					Element thisVar= (Element)iter2.next();
					//System.out.println("    " + thisVar.getName());

					if (thisVar.getName().equals("CONDITION"))
						newVar.dependencies.addElement( thisVar.getText() );
				}
				
				if (newVar.dependencies.size() > 0)
					result.localRandoms.addElement(newVar);
			}
			*/

			else if (thisItem.getName().equals("ANSWERS"))
			{
				java.util.List ansData= thisItem.getChildren();
				ListIterator iter2= ansData.listIterator();
				
				//System.out.println( "  " + Integer.toString(ansData.size()) + " answer declarations");
				
				while (iter2.hasNext()) 
				{
					Element thisAns= (Element)iter2.next();
					//System.out.println("    " + thisAns.getName());
					
					worksheet_answer theAnswer= null;
					
					if (thisAns.getName().equals("NUMBER"))
					{
						worksheet_answer_number thisAnswer= new worksheet_answer_number();
						
						thisAnswer.name= thisAns.getAttributeValue("name");
						thisAnswer.correctAnswer= thisAns.getAttributeValue("expression");
						
						try
						{
							int weight= Integer.parseInt(thisAns.getAttributeValue("weight"));
							thisAnswer.points= weight;
						}
						catch (NumberFormatException ignore) {}
						
						thisAnswer.formatString= thisAns.getAttributeValue("format");
						
						theAnswer= thisAnswer;
					}
					
					else if (thisAns.getName().equals("MULTIPLE_CHOICE"))
					{
						worksheet_answer_multipleChoice thisAnswer= new worksheet_answer_multipleChoice();
						thisAnswer.choices= new VectorAdapter();
						
						thisAnswer.name= thisAns.getAttributeValue("name");

						try
						{
							int weight= Integer.parseInt(thisAns.getAttributeValue("weight"));
							thisAnswer.points= weight;
						}
						catch (NumberFormatException ignore) {}
						
						try
						{
							int correct= Integer.parseInt(thisAns.getAttributeValue("correct"));
							thisAnswer.correctChoice= correct;
						}
						catch (NumberFormatException ignore) {}
						
						java.util.List choiceData= thisAns.getChildren();
						ListIterator iter3= choiceData.listIterator();
						
						//System.out.println( "  " + Integer.toString(choiceData.size()) + " choice declarations");
						
						while (iter3.hasNext()) 
						{
							Element thisChoice= (Element)iter3.next();
							//System.out.println("    " + thisChoice.getName());
							
							if (thisChoice.getName().equals("CHOICE"))
								thisAnswer.choices.addElement(thisChoice.getText());
						}
						
						theAnswer= thisAnswer;
					}

					if (theAnswer != null)
						result.answers.addElement( theAnswer );
				}
			}
		}
	
		return(result);
	}

		
	public String v5elxContent( tp_requestHandler theHandler, test theTest ) 
	{
		String result= "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r";
		
		result += "<wkquestion xmlns=\"http://www.mhhe.com/EZTest/\">\r";

		result += "	<stem><p xmlns=\"http://www.w3.org/1999/xhtml\">\r";
		result += toXMLhtml(qtext, theHandler);
		result += "	</p></stem>\r";
	
		result += "	<reference>" + toXMLstring(referenceTag) + "</reference>\r";
		result += "	<page>" + toXMLstring(pageTag) + "</page>\r";

		if (longQuestion)
			result += "	<longquestion>true</longquestion>\r";
		else
			result += "	<longquestion>false</longquestion>\r";

		result += "	<explanation><p xmlns=\"http://www.w3.org/1999/xhtml\">\r";
		result += toXMLhtml(explanation, theHandler);
		result += "	</p></explanation>\r";
	
		result += "</wkquestion>\r";
				
		return(result);
	}


	public String v7elxContent( tp_requestHandler theHandler, test theTest ) 
	{
		//boolean answerSpecificFeedbackSupport= licenseManager.hasFeature(theHandler, licenseManager.FEATURE_SUPPORT_ASFEEDBACK);
		
		String result= "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r";
		
		result += "<wkquestion xmlns=\"http://www.mhhe.com/EZTest/\">\r";

		result += "	<stem><p xmlns=\"http://www.w3.org/1999/xhtml\">\r";
		
		/* nested table problem
		String stemHTML= toXMLhtml(qtext, theHandler);
		stemHTML= tp_utils.substitute(stemHTML, "<table ", "__%TABLE_START%__");
		stemHTML= tp_utils.substitute(stemHTML, "</table>", "__%TABLE_END%__");
		stemHTML= tp_utils.substitute(stemHTML, "__%TABLE_START%__", "</p><table xmlns=\"http://www.w3.org/1999/xhtml\" ");
		stemHTML= tp_utils.substitute(stemHTML, "__%TABLE_END%__", "</table><p xmlns=\"http://www.w3.org/1999/xhtml\">");
		result += stemHTML;
		//*/
		
		result += toXMLhtml(qtext, theHandler);
		
		result += "	</p></stem>\r";
	
		result += "	<customType>" + toXMLstring( questionProperties.getString(CUSTOM_TYPE, "Worksheet") ) + "</customType>\r";
		result += "	<reference>" + toXMLstring(referenceTag) + "</reference>\r";
		result += "	<mpCustomTitle>" + toXMLstring( questionProperties.getString(MP_CUSTOM_TITLE, "") ) + "</mpCustomTitle>\r";

		result += "	<page>" + toXMLstring(pageTag) + "</page>\r";
		result += "	<selecttitle>" + toXMLstring(cleanSelectionTitle( selectionTitle )) + "</selecttitle>\r";
		result += "	<instructorinfo>" + toXMLstring( questionProperties.getString(INSTRUCTOR_INFO, "") ) + "</instructorinfo>\r";

		String palette= questionProperties.getString(PALETTE_SUPPORT, "");
		if (palette.length() == 0) palette= "none";
		result += "	<paletteSupport>" + palette + "</paletteSupport>\r";

		if (longQuestion)
			result += "	<longquestion>true</longquestion>\r";
		else
			result += "	<longquestion>false</longquestion>\r";

		result += "	 <" + COMPLETE_INCOMPLETE_GRADING + ">" + questionProperties.getString(COMPLETE_INCOMPLETE_GRADING, "false") + "</" + COMPLETE_INCOMPLETE_GRADING + ">\r";

		result += "	 <manualScoring>" + getScoring() + "</manualScoring>\r";

		result += "	<explanation><p xmlns=\"http://www.w3.org/1999/xhtml\">\r";
		result += toXMLhtml(explanation, theHandler);
		result += "	</p></explanation>\r";
	
		//if (answerSpecificFeedbackSupport){
			result += "	<solution><p xmlns=\"http://www.w3.org/1999/xhtml\">\r";
			result += toXMLhtml( questionProperties.getString(PROBLEM_SOLUTION, ""), theHandler);
			result += "	</p></solution>\r";
		//}
		
		result += hintXML( theHandler );
		result += contentLinkXML(theHandler);
		result += tooltipXML( theHandler );

		result += "</wkquestion>\r";
				
		return(result);
	}


	public String v7addendaButton( tp_requestHandler theHandler ) 
	{
		return("<input title=\"edit worksheet answers\" type=\"button\" name=\"addendaButton\" value=\"Worksheet Answers\" onClick=\"doAddenda();\">&nbsp;"); 
	}

	public void v7editOthersFrame( tp_requestHandler theHandler, test theTest )
	{
		String answerDelete= theHandler.getParam("OTHERdelete");
		//System.out.println("Deleting " + answerDelete);
		if (answerDelete.length() > 0)
		{
			for (int i=0; i<answers.size(); i++)
			{
				worksheet_answer thisOne= (worksheet_answer)answers.elementAt(i);
				if (thisOne.name.equals(answerDelete))
				{
					answers.removeElementAt(i);
				
					maxPoints= answers.size();

					try 
					{
						theTest.sqlRoutine.updateQuestion(this, theTest.questions);
						theHandler.theServlet.sqlRoutine.timestampTest( theTest, theHandler );
						//TCS add start for QuestionXref Model
						QuestionXrefUtil.createXrefFromNewQuestion(this, theHandler, theTest.sqlID);
						//TCS add start for QuestionXref Model
					} 
					catch (SQLException s) 
					{
						theTest.sqlRoutine.reportException("worksheet.v7editOther", s);
					}
					break;
				}
			}
		}
		
		tp_sortedList varList= new tp_sortedList();

		int weightTotal= 0;
		for (int i=0 ; i<answers.size() ; i++) 
		{
			worksheet_answer theAnswer= (worksheet_answer)answers.elementAt(i);
			varList.add( theAnswer.name, theAnswer.v7listEntry() );
			weightTotal += theAnswer.points;
		}
		
		String editURL= theHandler.testURL + "?" + v7list.stdURLVars( v7list.EDIT_ADDENDA ) + "&qid=" + sqlID;
		
		String scripts= "";
		scripts += "var editingWindow= null;\r";
		
		scripts += "function newAnswer( theType )\r";
		scripts += "{\r";
		scripts += "  theURL= '" + editURL + "&OTHERtype=' + theType;\r";
		//scripts += "  alert('start new answer: ' + theURL);\r";
		scripts += "  editingWindow= window.open( theURL, 'edita', 'toolbar=no,location=no,directories=no,status=no,scrollbars=yes,resizable=yes,copyhistory=no,width=600,height=500,screenX=50,screenY=50,left=50,top=50' );\r";
		scripts += "  editingWindow.focus();\r";
		scripts += "}\r";

		scripts += "function editAnswer( varname )\r";
		scripts += "{\r";
		scripts += "  theURL= '" + editURL + "&OTHERname=' + varname;\r";
		//scripts += "  alert('editing answer ' + theURL);\r";
		scripts += "  editingWindow= window.open( theURL, 'edita', 'toolbar=no,location=no,directories=no,status=no,scrollbars=yes,resizable=yes,copyhistory=no,width=600,height=500,screenX=50,screenY=50,left=50,top=50' );\r";
		scripts += "  editingWindow.focus();\r";
		scripts += "}\r";

		scripts += "function editLargeAnswer( varname )\r";
		scripts += "{\r";
		scripts += "  theURL= '" + editURL + "&OTHERname=' + varname;\r";
		//scripts += "  alert('editing answer ' + theURL);\r";
		scripts += "  editingWindow= window.open( theURL, 'edita', 'toolbar=no,location=no,directories=no,status=no,scrollbars=yes,resizable=yes,copyhistory=no,width=800,height=600,screenX=50,screenY=50,left=50,top=50' );\r";
		scripts += "  editingWindow.focus();\r";
		scripts += "}\r";

		scripts += "function deleteAnswer( varname )\r";
		scripts += "{\r";
		scripts += "  if (!confirm('Are you sure you wish to delete the answer named ' + varname)) return;\r";
		scripts += "  if (editingWindow != null) {\r";
		scripts += "    if (!editingWindow.closed) editingWindow.close();\r";
		scripts += "    editingWindow= null;\r";
		scripts += "  }\r";
		scripts += "  document.answerForm.OTHERdelete.value= varname;\r";
		scripts += "  document.answerForm.submit();\r";
		scripts += "}\r";

		scripts += "function closeChildren()\r";
		scripts += "{\r";
		scripts += "  if (editingWindow != null) {\r";
		scripts += "    if (!editingWindow.closed) editingWindow.close();\r";
		scripts += "    editingWindow= null;\r";
		scripts += "  }\r";
		scripts += "}\r";

		theHandler.theResponse.setHeader("Content-Type","text/html; charset=UTF-8");
		theHandler.noCache();
		theHandler.snd("<html><head><title>Additional Info Editing</title>");
		theHandler.snd(authentication.NO_CACHE_HTML);
		v5ui.selectStyleSheet(theHandler, "WHITE");
		v7list.script(theHandler, scripts);
		theHandler.snd("</head><body style=\"border: 1px solid #000000\" onbeginunload=\"closeChildren();\"><form name=\"answerForm\" action=\"" + theHandler.testURL + "\">");
		
		theHandler.snd("<table width=\"100%\" cellspacing=\"0\" cellpadding=\"2\">");
		theHandler.snd("<tr><th>Name</th><th>Type</th><th>Weight</th><th>Info</th>");
		theHandler.snd("<th style=\"text-align: right\">");

		theHandler.snd("<a href=\"javascript:newAnswer( document.answerForm.answerType.selectedIndex );\" title=\"create a new answer\"><img align=\"top\"src=\"/EZTestOnline/Graphics/btn_new.gif\" border=\"0\"></a>");
		//theHandler.snd("<input type=\"button\" name=\"bogus1\" value=\"New\" onClick=\"newAnswer( document.answerForm.answerType.selectedIndex );\">");
		//theHandler.snd("&nbsp;<select name=\"answerType\"><option selected value=\"0\">True/False<option value=\"1\">Multiple Choice<option value=\"2\">Number<option value=\"3\">Word or Phrase</select>");
		theHandler.snd("&nbsp;<select name=\"answerType\">" + worksheet_answer.v7typeOptions( theHandler ) + "</select>");
		theHandler.snd("</th></tr>");
		
		if (varList.size() == 0)
		{
			theHandler.snd("<tr><td colspan=\"4\">No answers defined</td></tr>");
		}
		else
		{
			Enumeration entries= varList.sortedValues();
			for (int i=0; entries.hasMoreElements(); i++)
			{
				if ((i % 2) == 0) 
					theHandler.snd("<tr class=\"tr2\">");
				else 
					theHandler.snd("<tr class=\"tr1\">");
				
				theHandler.snd((String)entries.nextElement() + "</tr>");
			}
		}
		
		if (varList.size() > 0)
			theHandler.snd("<tr><th>" + Integer.toString(varList.size()) + "&nbsp;answers</th><th>&nbsp;</th><th>" + Integer.toString(weightTotal) + "</th><th>&nbsp;</th><th>&nbsp;</th></tr>");
		
		theHandler.snd("</table>");
		
		theHandler.snd( v7list.stdFormVars(v7list.ADDENDA) );
		theHandler.snd("<input type=\"hidden\" name=\"qid\" value=\"" + sqlID + "\">");
		theHandler.snd("<input type=\"hidden\" name=\"OTHERdelete\" value=\"\">");
		
		theHandler.snd("</form></body></html>");
	}


	public void v7editOther( tp_requestHandler theHandler, test theTest )
	{
		worksheet_answer theAnswer= null;
		
		String newParam= theHandler.getParam("OTHERtype");
		if (newParam.length() != 0)
		{
			theAnswer= worksheet_answer.v7newAnswer( newParam );
			
			if (theAnswer != null)
			{
				String newName= "new";
				int theTry= 1;
				boolean found= false;
				do
				{
					found= false;
					for (int i=0; i<answers.size(); i++)
					{
						worksheet_answer thisOne= (worksheet_answer)answers.elementAt(i);
						if (thisOne.name.equals(newName))
						{
							found= true;
							theTry++;
							newName= "new" + Integer.toString(theTry);
							break;
						}
					}
				} while (found);
				
				theAnswer.name= newName;
				answers.addElement( theAnswer );
				
				maxPoints= answers.size();
				
				try 
				{
					theTest.sqlRoutine.updateQuestion(this, theTest.questions);
					theTest.sqlRoutine.timestampTest(theTest, theHandler);
					//TCS add start for QuestionXref Model
					QuestionXrefUtil.createXrefFromNewQuestion(this, theHandler, theTest.sqlID);
					//TCS add start for QuestionXref Model
				} 
				catch (SQLException s) {
					theTest.sqlRoutine.reportException("worksheet.v7editOther", s);
				}
			}
		}
		else
		{
			String searchName= theHandler.getParam("OTHERname");
			
			for (int i=0; i<answers.size(); i++)
			{
				worksheet_answer thisOne= (worksheet_answer)answers.elementAt(i);
				if (thisOne.name.equals(searchName))
				{
					theAnswer= thisOne;
					break;
				}
			}
		}
		
		if (theAnswer != null)
		{
			theAnswer.v7edit(theHandler, theTest, this);
		}
		else
		{
			theHandler.errorMessage("undefined answer for editing");
		}
	}


	public void v7updateOther( tp_requestHandler theHandler, test theTest )
	{
		String whichAnswer= theHandler.getParam("OTHERname");
		if (whichAnswer.length() > 0) 
		{
			for (int i=0; i<answers.size(); i++)
			{
				worksheet_answer thisAnswer= (worksheet_answer)answers.elementAt(i);
				if (thisAnswer.name.equals(whichAnswer))
				{
					thisAnswer.v7updateOther( theHandler, theTest, this );
					break;
				}
			}
		}		
			
		if (theHandler.getParam(worksheet_answer.REFRESH_PARAM).length() == 0)
			v7editOthersFrame( theHandler, theTest );
			
		return;
	}


	public void v7update( tp_requestHandler theHandler, test theTest )
	{
		clearPreviewCache( theHandler, theTest );
		
		String theXMLdata= theHandler.getParam("content");
		if (theXMLdata.length() == 0)
		{
			//v5edit( theHandler, theTest );
			_logger.error("update failure in worksheet.v7update");
			return;
		}
		
		//System.out.println(theXMLdata);
		
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

				else if (thisItem.getName().equals("customType"))
					questionProperties.setString(CUSTOM_TYPE, getXMLstring(thisItem));
				
				else if (thisItem.getName().equals("page"))
					pageTag= getXMLstring(thisItem);
				
				else if (thisItem.getName().equals("selecttitle"))
					selectionTitle= cleanSelectionTitle( getXMLstring(thisItem) );
				
				else if (thisItem.getName().equals("instructorinfo"))
					questionProperties.setString(INSTRUCTOR_INFO, getXMLstring(thisItem));
				
				else if (thisItem.getName().equals("paletteSupport"))
				{
					String palette= getXMLstring(thisItem);
					if (palette.indexOf("none") >=0 ) palette= "";
					questionProperties.setString(question.PALETTE_SUPPORT, palette);
				}

				else if (thisItem.getName().equals("longquestion"))
					longQuestion= getXMLstring(thisItem).equals("true");
				
				else if (thisItem.getName().equals("explanation"))
					explanation= embeddedMedia.processMediaIn( theHandler, theTest, getXMLhtml(thisItem, theHandler), sqlID );
					
				else if (thisItem.getName().equals("solution"))
				{
					String theSolution= embeddedMedia.processMediaIn( theHandler, theTest, getXMLhtml(thisItem, theHandler), sqlID );
					questionProperties.setString(PROBLEM_SOLUTION, theSolution);
				}
				
				else if (thisItem.getName().equals("contentLinkInfo")) parseContentLinks( thisItem, theHandler, theTest );

				else if (thisItem.getName().equals("hintInfo")) parseHints( thisItem, theHandler, theTest );

				else if (thisItem.getName().equals("tooltipInfo")) parseTooltips( thisItem, theHandler, theTest );

				else if (thisItem.getName().equals("manualScoring"))
					questionProperties.setString(question.CONNECT_FORCED_SCORING, getXMLstring(thisItem));
			}
		}
		catch (IOException io)
		{
			_logger.error("XML IO error in worksheet.v7update()", io);
		}
		catch (JDOMException jd) 
		{
			_logger.error("XML parsing error in worksheet.v7update()", jd);
		}
		
		updateMediaConsumers( theHandler, theTest );
	}
	
	
	public void classware_editOthersFrame( tp_requestHandler theHandler, test theTest )
	{
		String answerDelete= theHandler.getParam("OTHERdelete");
		//System.out.println("Deleting " + answerDelete);
		if (answerDelete.length() > 0)
		{
			for (int i=0; i<answers.size(); i++)
			{
				worksheet_answer thisOne= (worksheet_answer)answers.elementAt(i);
				if (thisOne.name.equals(answerDelete))
				{
					answers.removeElementAt(i);
				
					// QCS#231 - don't do this line for Spark
					if (!theHandler.isHMrequest()) maxPoints= answers.size();

					try 
					{
						theTest.sqlRoutine.updateQuestion(this, theTest.questions);
						theHandler.theServlet.sqlRoutine.timestampTest( theTest, theHandler );
						//TCS add start for QuestionXref Model
						QuestionXrefUtil.createXrefFromNewQuestion(this, theHandler, theTest.sqlID);
						//TCS add start for QuestionXref Model
					} 
					catch (SQLException s) 
					{
						theTest.sqlRoutine.reportException("worksheet.classware_editOther", s);
					}
					break;
				}
			}
		}
		
		tp_sortedList varList= new tp_sortedList();

		int weightTotal= 0;
		for (int i=0 ; i<answers.size() ; i++) 
		{
			worksheet_answer theAnswer= (worksheet_answer)answers.elementAt(i);
			varList.add( theAnswer.name, theAnswer.v7listEntry() );
			weightTotal += theAnswer.points;
		}
		
		String editURL= theHandler.classwareURL + "&" + classware_edit.stdURLVars( classware_edit.EDIT_ADDENDA ) + "&" + classware_edit.QID + "=" + sqlID;
		
		String scripts= "";
		scripts += "var editingWindow= null;\r";
		
		scripts += "function newAnswer( theType )\r";
		scripts += "{\r";
		scripts += "  theURL= '" + editURL + "&OTHERtype=' + theType;\r";
		//scripts += "  alert('start new answer: ' + theURL);\r";
		scripts += "  editingWindow= window.open( theURL, 'edita', 'toolbar=no,location=no,directories=no,status=no,scrollbars=yes,resizable=yes,copyhistory=no,width=600,height=500,screenX=50,screenY=50,left=50,top=50' );\r";
		scripts += "  editingWindow.focus();\r";
		scripts += "}\r";

		scripts += "function editAnswer( varname )\r";
		scripts += "{\r";
		scripts += "  theURL= '" + editURL + "&OTHERname=' + varname;\r";
		//scripts += "  alert('editing answer ' + theURL);\r";
		scripts += "  editingWindow= window.open( theURL, 'edita', 'toolbar=no,location=no,directories=no,status=no,scrollbars=yes,resizable=yes,copyhistory=no,width=600,height=500,screenX=50,screenY=50,left=50,top=50' );\r";
		scripts += "  editingWindow.focus();\r";
		scripts += "}\r";

		scripts += "function editLargeAnswer( varname )\r";
		scripts += "{\r";
		scripts += "  theURL= '" + editURL + "&OTHERname=' + varname;\r";
		//scripts += "  alert('editing answer ' + theURL);\r";
		scripts += "  editingWindow= window.open( theURL, 'edita', 'toolbar=no,location=no,directories=no,status=no,scrollbars=yes,resizable=yes,copyhistory=no,width=800,height=600,screenX=50,screenY=50,left=50,top=50' );\r";
		scripts += "  editingWindow.focus();\r";
		scripts += "}\r";

		scripts += "function deleteAnswer( varname )\r";
		scripts += "{\r";
		scripts += "  if (!confirm('Are you sure you wish to delete the answer named ' + varname)) return;\r";
		scripts += "  if (editingWindow != null) {\r";
		scripts += "    if (!editingWindow.closed) editingWindow.close();\r";
		scripts += "    editingWindow= null;\r";
		scripts += "  }\r";
		scripts += "  document.answerForm.OTHERdelete.value= varname;\r";
		scripts += "  document.answerForm.submit();\r";
		scripts += "}\r";

		scripts += "function closeChildren()\r";
		scripts += "{\r";
		scripts += "  if (editingWindow != null) {\r";
		scripts += "    if (!editingWindow.closed) editingWindow.close();\r";
		scripts += "    editingWindow= null;\r";
		scripts += "  }\r";
		scripts += "}\r";

		theHandler.theResponse.setHeader("Content-Type","text/html; charset=UTF-8");
		theHandler.noCache();
		theHandler.snd("<html><head><title>Additional Info Editing</title>");
		theHandler.snd(authentication.NO_CACHE_HTML);
		v5ui.selectStyleSheet(theHandler, "WHITE");
		v5ui.script(theHandler, scripts);
		theHandler.snd("</head><body style=\"border: 1px solid #000000\" onbeginunload=\"closeChildren();\"><form name=\"answerForm\" action=\"" + theHandler.classwareURL + "\">");
		
		theHandler.snd("<table width=\"100%\" cellspacing=\"0\" cellpadding=\"2\">");
		theHandler.snd("<tr><th>Name</th><th>Type</th><th>Weight</th><th>Info</th>");
		theHandler.snd("<th style=\"text-align: right\">");

		theHandler.snd("<a href=\"javascript:newAnswer( document.answerForm.answerType.selectedIndex );\" title=\"create a new answer\"><img align=\"top\"src=\"/EZTestOnline/Graphics/btn_new.gif\" border=\"0\"></a>");
		
		theHandler.snd("&nbsp;<select name=\"answerType\">" + worksheet_answer.v7typeOptions( theHandler ) + "</select>");
		theHandler.snd("</th></tr>");
		
		if (varList.size() == 0)
		{
			theHandler.snd("<tr><td colspan=\"4\">No answers defined</td></tr>");
		}
		else
		{
			Enumeration entries= varList.sortedValues();
			for (int i=0; entries.hasMoreElements(); i++)
			{
				if ((i % 2) == 0) 
					theHandler.snd("<tr class=\"tr2\">");
				else 
					theHandler.snd("<tr class=\"tr1\">");
				
				theHandler.snd((String)entries.nextElement() + "</tr>");
			}
		}
		
		if (varList.size() > 0)
			theHandler.snd("<tr><th>" + Integer.toString(varList.size()) + "&nbsp;answers</th><th>&nbsp;</th><th>" + Integer.toString(weightTotal) + "</th><th>&nbsp;</th><th>&nbsp;</th></tr>");
		
		theHandler.snd("</table>");
		
		theHandler.snd( classware_edit.stdFormVars(classware_edit.ADDENDA) );
		theHandler.snd("<input type=\"hidden\" name=\"" + classware_edit.QID + "\" value=\"" + sqlID + "\">");
		theHandler.snd("<input type=\"hidden\" name=\"OTHERdelete\" value=\"\">");
		
		theHandler.snd("</form></body></html>");
	}


	public void classware_editOther( tp_requestHandler theHandler, test theTest )
	{
		worksheet_answer theAnswer= null;
		
		String searchName= theHandler.getParam("OTHERname");
		if (searchName.length() > 0)
		{
			for (int i=0; i<answers.size(); i++)
			{
				worksheet_answer thisOne= (worksheet_answer)answers.elementAt(i);
				if (thisOne.name.equals(searchName))
				{
					theAnswer= thisOne;
					break;
				}
			}
		}
		
		if (theAnswer == null)
		{
			String newParam= theHandler.getParam("OTHERtype");
			if (newParam.length() != 0)
			{
				theAnswer= worksheet_answer.v7newAnswer( newParam );
				
				if (theAnswer != null)
				{
					String newName= "new";
					int theTry= 1;
					boolean found= false;
					do
					{
						found= false;
						for (int i=0; i<answers.size(); i++)
						{
							worksheet_answer thisOne= (worksheet_answer)answers.elementAt(i);
							if (thisOne.name.equals(newName))
							{
								found= true;
								theTry++;
								newName= "new" + Integer.toString(theTry);
								break;
							}
						}
					} while (found);
					
					theAnswer.name= newName;
					answers.addElement( theAnswer );
					
					maxPoints= answers.size();
					
					try {
						theTest.sqlRoutine.updateQuestion(this, theTest.questions);
						//TCS add start for QuestionXref Model
						QuestionXrefUtil.createXrefFromNewQuestion(this, theHandler, theTest.sqlID);
						//TCS add start for QuestionXref Model
					} catch (SQLException s) {
						theTest.sqlRoutine.reportException("worksheet.classware_editOther", s);
					}
				}
			}
		}
		
		if (theAnswer != null)
			theAnswer.classware_edit(theHandler, theTest, this);
		else
			theHandler.errorMessage("undefined answer for editing");
	}


	public void classware_updateOther( tp_requestHandler theHandler, test theTest )
	{
		//String whichAnswer= theHandler.getParam(test.classware__UPDATEother);
		String whichAnswer= theHandler.getParam("OTHERname");
		if (whichAnswer.length() > 0) 
		{
			for (int i=0; i<answers.size(); i++)
			{
				worksheet_answer thisAnswer= (worksheet_answer)answers.elementAt(i);
				if (thisAnswer.name.equals(whichAnswer))
				{
					thisAnswer.classware_updateOther( theHandler, theTest, this );
					break;
				}
			}
		}		
		
		if (theHandler.getParam(worksheet_answer.REFRESH_PARAM).length() == 0)
			classware_editOthersFrame( theHandler, theTest );
		return;
	}


	public boolean isLSI()
	{
		int answersize = answers.size();
		for (int i=0; i<answersize; i++)
		{
			worksheet_answer thisAnswer= (worksheet_answer)answers.elementAt(i);
			if (thisAnswer.type == worksheet_answer.ANSWER_TYPE_lsi) return(true);
		}
		return(false);
	}
	
	public boolean isSparkPlug()
	{
		for (int i=0; i<answers.size(); i++)
		{
			worksheet_answer thisAnswer= (worksheet_answer)answers.elementAt(i);
			if (thisAnswer.type == worksheet_answer.ANSWER_TYPE_flash) return(true);
		}
		return(false);
	}
	
	
	public boolean isExternal()
	{
		for (int i=0; i<answers.size(); i++)
		{
			worksheet_answer thisAnswer= (worksheet_answer)answers.elementAt(i);
			if (thisAnswer.type == worksheet_answer.ANSWER_TYPE_external) return(true);
		}
		return(false);
	}
	
	
	public boolean isChat(tp_requestHandler theHandler)
	{
		boolean result= false;
		
		for (int i=0; i<answers.size(); i++)
		{
			worksheet_answer thisAnswer= (worksheet_answer)answers.elementAt(i);
			result |= thisAnswer.isChat(theHandler);
		}
		
		return(result);
	}
	
	public boolean isChat(boolean wimbaDebug)
	{
		boolean result= false;
		
		for (int i=0; i<answers.size(); i++)
		{
			worksheet_answer thisAnswer= (worksheet_answer)answers.elementAt(i);
			result |= thisAnswer.isChat(wimbaDebug);
		}
		
		return(result);
	}
	
	public boolean maySuppressRandoms()
	{
		if (isExternal()) return false;
		if (isSparkPlug()) return false;
		return true;
	}
	
	public boolean mustEncryptRandoms()
	{
		if (isExternal()) return true;
		return false;
	}




	public void removeExternalResponseInfo( tp_requestHandler theHandler, parameters thePartial )
	{
		for (int i=0; i<answers.size(); i++)
		{
			worksheet_answer thisAnswer= (worksheet_answer)answers.elementAt(i);
			thisAnswer.removeExternalResponseInfo( theHandler, this, thePartial );
		}
	}

	public void removeExternalResponseInfo( tp_requestHandler theHandler, PartialTO partialTO )
	{
		for (int i=0; i<answers.size(); i++)
		{
			worksheet_answer thisAnswer= (worksheet_answer)answers.elementAt(i);
			thisAnswer.removeExternalResponseInfo( theHandler, this, partialTO );
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.mcgrawhill.ezto.test.questions.question#removeExternalResponseInfo(com.mcgrawhill.ezto.api.caa.services.transferobject.QuestionWiseResponseTO, com.mcgrawhill.ezto.api.caa.services.transferobject.QuestionMetaDataTO)
	 */
	@Override
	public void removeExternalResponseInfo(QuestionWiseResponseTO questionWiseResponseTO, QuestionMetaDataTO questionMetaDataTO) throws Exception {
		List<Map<String, String>> worksheetAnswers = null;
		richProperties answerProperties = null;
		worksheet_answer worksheetAnswer = null;
		if(questionWiseResponseTO != null && questionMetaDataTO != null){
			worksheetAnswers = questionMetaDataTO.getAnswers();
			if(worksheetAnswers != null && worksheetAnswers.size() > 0) {
				for(Map<String,String> answersMap : worksheetAnswers){
					String ansType = answersMap.get(QuestionMetadataUtil.TYPE);
					if(!StringUtils.isBlank(ansType) && ansType.equals(worksheet_answer.shortTypeString(worksheet_answer.ANSWER_TYPE_external))){
						String ansName = answersMap.get(QuestionMetadataUtil.NAME);
						worksheetAnswer = worksheet_answer.new_answer(ansName, ansType);
						answerProperties = richProperties.newInstance(ansName);
						answerProperties.setString(worksheet_answer_external.IDENTIFIER,answersMap.get(worksheet_answer_external.IDENTIFIER));
						worksheetAnswer.answerProperties = answerProperties;
						worksheetAnswer.removeExternalResponseInfo(questionWiseResponseTO);
					}
				}
			}
		}
	}
	
	public String scoring( tp_requestHandler theHandler ){
		Vector debugTransactions = tp_utils.listToVector(licenseManager.getFeatureInfo(theHandler, licenseManager.LICENSE_TYPE_HMDEBUG), ",");
		boolean wimbaDebug= (debugTransactions.contains(classware_hm.WIMBA_DEBUG));
		return scoring(theHandler, wimbaDebug);
	}

	public String scoring(tp_requestHandler theHandler, boolean wimbaDebug)
	{
		if(isQuestionPointZero()){
			return("automatic");
		}
		
		if((wimbaDebug && this.wimbaEnabled) || manualScoring()){
			return "manual";
		}
		
		if (completeIncompleteGrading()){
			return "automatic";
		}
		
		for (int i=0; i<answers.size(); i++)
		{
			worksheet_answer thisAnswer= (worksheet_answer)answers.elementAt(i);
			
			//TCS added code for Mangrade changes
			if(thisAnswer.type == worksheet_answer.ANSWER_TYPE_essay && thisAnswer.points > 0 ){
				return "manual";
			}
			if(thisAnswer.type == worksheet_answer.ANSWER_TYPE_avRecording || thisAnswer.type == worksheet_answer.ANSWER_TYPE_videoVoiceover){
				if(thisAnswer.answerProperties != null){
					boolean completeIncompleteGrading = thisAnswer.answerProperties.getBoolean(question.COMPLETE_INCOMPLETE_GRADING, false);
					if( !completeIncompleteGrading ){
						return "manual";
					}
				}
			}
			//TCS added code for Mangrade changes
		}

		return("automatic");
	}
	
	public String getGradingGroup( tp_requestHandler theHandler ){
		Vector debugTransactions = tp_utils.listToVector(licenseManager.getFeatureInfo(theHandler, licenseManager.LICENSE_TYPE_HMDEBUG), ",");
		boolean wimbaDebug= (debugTransactions.contains(classware_hm.WIMBA_DEBUG));
		return getGradingGroup(theHandler, wimbaDebug);
	}
	
	public String getGradingGroup( tp_requestHandler theHandler,boolean wimbaDebug)
	{
		int answerCount = 0;
		
		if((wimbaDebug && this.wimbaEnabled) || manualScoring()){
			return GRADING_GROUP_MANUAL;
		}
		
		if (completeIncompleteGrading()){
			return GRADING_GROUP_COMP_INC;
		}
		
		for (int i=0; i<answers.size(); i++)
		{
			worksheet_answer thisAnswer= (worksheet_answer)answers.elementAt(i);
			
			//TCS added code for Mangrade changes
			if(thisAnswer.type == worksheet_answer.ANSWER_TYPE_essay && thisAnswer.points > 0 ){
				return GRADING_GROUP_MANUAL;
			}
			if(thisAnswer.type == worksheet_answer.ANSWER_TYPE_avRecording || thisAnswer.type == worksheet_answer.ANSWER_TYPE_videoVoiceover){
				if(thisAnswer.answerProperties != null){
					boolean completeIncompleteGrading = thisAnswer.answerProperties.getBoolean(question.COMPLETE_INCOMPLETE_GRADING, false);
					if( !completeIncompleteGrading ){
						return GRADING_GROUP_MANUAL;
					}
				}
			}
			
			/*
			 * associate this question to "Complete/ Incomplete Tag" 
			 * if ALL ANSWERS set to complete/incomplete or weight = 0
			 */
			int answerPoints = thisAnswer.points;
			boolean thisAnswerCompleteIncompleteGrading = thisAnswer.answerProperties.getBoolean(question.COMPLETE_INCOMPLETE_GRADING, false);
			if(answerPoints == 0 || thisAnswerCompleteIncompleteGrading){
				answerCount ++ ;
			}
		}
		
		if(answers.size() > 0 && (answers.size() == answerCount)){
			return GRADING_GROUP_COMP_INC;
		}
		
		return GRADING_GROUP_AUTOMATIC;
	}

	public void connect_collectPolicies( tp_requestHandler theHandler, ConcurrentHashMap<String, String> policyTable )
	{
		connect_defaultPolicies( theHandler, policyTable );
		collectContentLinkPolicies(  theHandler, policyTable );
		
		policyTable.put(classware_hm.POLICY_printing, classware_hm.POLICY_printing);

		for (int i=0; i<answers.size(); i++)
		{
			worksheet_answer thisAnswer= (worksheet_answer)answers.elementAt(i);
			thisAnswer.connect_collectPolicies( theHandler, policyTable );
		}
	}


	public int paletteSet( tp_requestHandler theHandler, String selected )
	{
		questionProperties.setString(PALETTE_SUPPORT, selected);

		for (int i=0; i<answers.size(); i++)
		{
			worksheet_answer thisAnswer= (worksheet_answer)answers.elementAt(i);
			if (thisAnswer == null) continue;
			thisAnswer.answerProperties.setString(PALETTE_SUPPORT, selected);
		}

		updateQuestion( theHandler );
		return 1;
	}
		
	public String paletteGet()
	{
		String defaultPalette= questionProperties.getString(PALETTE_SUPPORT, "");
		
		for (int i=0; i<answers.size(); i++)
		{
			worksheet_answer thisAnswer= (worksheet_answer)answers.elementAt(i);
			if (thisAnswer == null) continue;
			String oldPalette= thisAnswer.answerProperties.getString(PALETTE_SUPPORT, defaultPalette);
			if (oldPalette.length() > 0) return oldPalette;
		}
		
		return defaultPalette;
	}
		


	public JSONObject getJSON( tp_requestHandler theHandler )
	throws JSONException
	{
		JSONObject result= jsonStub( theHandler );
		
		result.put(XML_WS_STEM, tp_utils.safeJSON(qtext));
		
		result.put(XML_WS_ANSWER_SET, worksheet_answer.getJSONlist(theHandler, this));
	
		result.put(XML_WS_COMMONFEEDBACK, tp_utils.safeJSON(explanation));
		
		return result;
	}
	
	/**
	 * Description: This method returns item json for worksheet question
	 * @return JSONObject
	 */
	public JSONObject getItemQinfoJson() throws JSONException
	{
		JSONObject result = getItemQinfoCommonJson();
		
		result.put(XML_WS_STEM, tp_utils.safeJSON(qtext));
		result.put(XML_WS_ANSWER_SET, worksheet_answer.getJSONlist(this));	
		result.put(XML_WS_COMMONFEEDBACK, tp_utils.safeJSON(explanation));
		
		return result;
	}
	
	
	public boolean importJSON( tp_requestHandler theHandler, JSONObject theJSON )
	throws JSONException
	{
		jsonStubImport(theHandler, theJSON);
	
		if (theJSON.has(XML_WS_STEM))
			qtext= tp_utils.jsonPostProcess( theHandler, theJSON.getString(XML_WS_STEM), this);
	
		if (theJSON.has(XML_WS_ANSWER_SET))
			worksheet_answer.processJSONlist( theHandler, theJSON.getJSONArray(XML_WS_ANSWER_SET), this );
		
		if (theJSON.has(XML_WS_COMMONFEEDBACK))
			explanation= tp_utils.jsonPostProcess( theHandler, theJSON.getString(XML_WS_COMMONFEEDBACK), this);
	
		return true;
	}



	static String		XML_WS						= "worksheet";
	
	static String		XML_WS_STEM					= "stem";
	
	static String		XML_WS_COMMONFEEDBACK		= "commonFeedback";
	
	static String		XML_WS_ANSWER_SET			= "answers";

	
	public Element buildExportXML( tp_requestHandler theHandler, test theTest )
	{
		Element result= xmlStub( theHandler, theTest );

		Element wk= new Element(XML_WS);
		result.addContent(wk);
		
		Element stem= new Element(XML_WS_STEM);
		wk.addContent(stem);
		stem.addContent( tp_utils.safeCDATA(qtext) );
		
		Element common= new Element(XML_WS_COMMONFEEDBACK);
		wk.addContent(common);
		common.addContent( tp_utils.safeCDATA(explanation) );
		
		Element answerset= new Element(XML_WS_ANSWER_SET);
		wk.addContent(answerset);
		
		for (int i=0; i<answers.size(); i++)
		{
			worksheet_answer thisAnswer= (worksheet_answer)answers.elementAt(i);
			answerset.addContent( thisAnswer.buildExportXML( theHandler, theTest, this ) );
		}
		
		/*
		worksheet testQ= (worksheet)buildFromXML( theHandler, theTest, result );
		System.out.println( compareQ(testQ) ? "MATCH!" : "MISMATCH!!!!" );
		try
		{
			FileOutputStream fout= new FileOutputStream("/Users/wmd/Desktop/Xsource");
			DataOutputStream dout= new DataOutputStream( fout );
			dout.writeUTF(qtext);
			fout.close();
			
			fout= new FileOutputStream("/Users/wmd/Desktop/Xdest");
			dout= new DataOutputStream( fout );
			dout.writeUTF(testQ.qtext);
			fout.close();
		}
		catch (IOException ignore) {}
		*/
		
		return result; 
	}


	public static question buildFromXML( tp_requestHandler theHandler, test theTest, Element qElement )
	{
		worksheet theQ= new worksheet();
		theQ.stdXMLimport( theHandler, theTest, qElement );
		
		theQ.answers= new VectorAdapter();
		
		try
		{
			Element mcElement= qElement.getChild(XML_WS);
			
			String commonFeedback= null;
			java.util.List mcInfo= mcElement.getChildren();
			ListIterator iter= mcInfo.listIterator();
			while (iter.hasNext()) 
			{
				Element thisChild= (Element)iter.next();
				String thisName= thisChild.getName();
			
				if (thisName.equals(XML_WS_STEM))
					theQ.qtext= thisChild.getText();
			
				else if (thisName.equals(XML_WS_COMMONFEEDBACK))
					theQ.explanation= thisChild.getText();
			
				else if (thisName.equals(XML_WS_ANSWER_SET))
					worksheet_answer.buildFromXML(theHandler, theTest, theQ, thisChild);
			}
			theQ.importQFromXML( theHandler, theTest, qElement );
		}
		catch (Exception e)
		{
			_logger.error("Exception parsing XML in worksheet.buildFromXML()");
		}
		
		return theQ;	
	}


	public boolean compareQ( question otherQ )
	{
		if (otherQ.type != type) { _logger.info("type mismatch"); return false; }
		
		worksheet theQ= (worksheet)otherQ;
		
		boolean result= true;
		
		result= worksheet_answer.compareAnswers( this, theQ );
		
		return (result && compareStd( otherQ ));
	}
	
	
	public Enumeration getAnswerEnumeration()
	{
		return answers.elements();
	}
	
	
	public int convertSparkPlugs( tp_requestHandler theHandler, String plugName, String extIdentifier)
	{
		int count= 0;
		
		for (int i=0; i<answers.size(); i++) {
			worksheet_answer theAnswer= (worksheet_answer)answers.elementAt(i);
			if (theAnswer.type != worksheet_answer.ANSWER_TYPE_flash) continue;
			
			worksheet_answer_flash fa= (worksheet_answer_flash)theAnswer;
			if (!fa.getFlashType().equals(plugName)) continue;
			
			answers.setElementAt( worksheet_answer_external.convertFlash( theHandler, fa, extIdentifier ), i );
			count++;
		}

		if (count > 0) updateQuestion( theHandler );
		return count;
	}
	
	public boolean feedbackToSolution( tp_requestHandler theHandler )
	{
		setSolution(explanation);
		explanation= "";
		return true;
	}
	
	public String v6showDetailedGrading( tp_requestHandler theHandler, test theTest,String substitutedText, Map<String, Boolean> statusMap) 
	{
		String theQID= "Q_" + sqlID;		
			
		String resultHTML= "<div class=\"rspStyle\">";
		
		resultHTML += "<input type=\"hidden\" name=\"" + theQID + "\" value=\"-2-2\">";
		v7media.fixAttachedMediaForRender(theHandler, this);

		resultHTML += "<input type=\"hidden\" name=\"" + theQID + "_media\" id=\"" + theQID + "_media\" value=\"" + tp_utils.substitute(theTest.getMyMediaURL(theHandler), "SHOWmedia&amp;media", "SHOWmedia&media") + "," + myAttachedMedia() + "\">";
		
		resultHTML += worksheet_answer.v6showDetailedGrading( theHandler, theTest, substitutedText, this, statusMap);		
		resultHTML += "<br>&nbsp;<br></div>";
		
		return(resultHTML);
	}
	
	public boolean isEligibleForAggregatedGrading(tp_requestHandler theHandler) {
		Vector debugTransactions = tp_utils.listToVector(licenseManager.getFeatureInfo(theHandler, licenseManager.LICENSE_TYPE_HMDEBUG), ",");
		boolean wimbaDebug= (debugTransactions.contains(classware_hm.WIMBA_DEBUG));
		return isEligibleForAggregatedGrading(theHandler, wimbaDebug);
	}
	
	public boolean isEligibleForAggregatedGrading(tp_requestHandler theHandler, boolean wimbaDebug) {
		String scoringType = scoring(theHandler, wimbaDebug);
		if(!"manual".equals(scoringType) || (wimbaDebug && this.wimbaEnabled) || isAlgorithmic(theHandler)){
			return false;
		}
		
		return supportsAggregatedGradingWorksheetAnswers();
	}
	
	public boolean isEligibleForAggregatedGrading(tp_requestHandler theHandler, test testWithXref) {
		Vector debugTransactions = tp_utils.listToVector(licenseManager.getFeatureInfo(theHandler, licenseManager.LICENSE_TYPE_HMDEBUG), ",");
		boolean wimbaDebug= (debugTransactions.contains(classware_hm.WIMBA_DEBUG));
		return isEligibleForAggregatedGrading(theHandler, testWithXref, wimbaDebug);
	}
	
	public boolean isEligibleForAggregatedGrading(tp_requestHandler theHandler, test testWithXref, boolean wimbaDebug) {
		String scoringType = scoring(theHandler, wimbaDebug);
		if(!"manual".equals(scoringType) || (wimbaDebug && this.wimbaEnabled) || isAlgorithmicXref(theHandler, testWithXref)){
			return false;
		}
		
		return supportsAggregatedGradingWorksheetAnswers();
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
		VectorAdapter wk_answers = this.answers;	
		 
		JSONObject answersJson = null;
		if(wk_answers != null && !wk_answers.isEmpty()){
			for (int i = 0; i < wk_answers.size(); i++) {
				worksheet_answer answer = (worksheet_answer) wk_answers.get(i);
				answersJson = answer.populateQuestionJSONForAnswers();
				if(answersJson != null){
					jsonObj.append("worksheet_answers", answersJson);
				}
			}
		}

		return jsonObj;
	}
	
	/**
	 * Name: getCompleteIncompleteStatus()
	 * Description: it returns true if accept any answer as correct is 
	 * 				set in the question level or at least one answer property level.
	 * Returns: boolean
	 */
	public boolean getCompleteIncompleteStatus(){
		if (questionProperties.getBoolean( COMPLETE_INCOMPLETE_GRADING, false )){
			return true;
		}
		
		for (int i=0; i<answers.size(); i++){
			worksheet_answer thisAnswer= (worksheet_answer)answers.elementAt(i);
			
			if(thisAnswer.type == worksheet_answer.ANSWER_TYPE_external 
				|| thisAnswer.type == worksheet_answer.ANSWER_TYPE_flash
				|| thisAnswer.type == worksheet_answer.ANSWER_TYPE_lsi){
				
				return true;
			}
			if(thisAnswer.answerProperties != null){
				boolean completeIncompleteGrading = thisAnswer.answerProperties.getBoolean(question.COMPLETE_INCOMPLETE_GRADING, false);
				if( completeIncompleteGrading ){
					return true;
				}
			}
		}
		return false;
	}
	
	public Map<String, List<String>> getEligibleWorksheetAnswersForWeightAdjustment() {
		Map<String,List<String>> answerMap = new HashMap<String, List<String>>();
		List<String> answerNameList = new ArrayList<String>();
		boolean allEssay = true;
		for (int i=0; i<answers.size(); i++)
		{
			worksheet_answer thisAnswer= (worksheet_answer)answers.elementAt(i);

			if(thisAnswer.type == worksheet_answer.ANSWER_TYPE_essay){
				if(thisAnswer.points == 0 ){
					answerNameList.add(thisAnswer.name);
				}
			}else{
				allEssay = false;
			}
		}
		
		if(answers.size() > 0 && answerNameList.size() > 0){
			if(allEssay){
				answerMap.put(ALL_ESSAY, answerNameList);
			}else{
				answerMap.put(NOT_ALL_ESSAY, answerNameList);
			}
		}
		return answerMap;
	}

	/**This method is designed to update WorkSheet Question blob with given essay answer weight. 
	 * @param theQ
	 * @param nameList
	 * @param updateWeight
	 * @return
	 * @throws Exception
	 */
	public question updateWKQuestionForWeighAdjustment( List<String> nameList, String updateWeight ) throws Exception{
		
	    for( int i =0; i< this.answers.size(); i++){
	    	worksheet_answer thisWkAns = (worksheet_answer)this.answers.elementAt(i);
	    	String ansName = thisWkAns.name;
	    	if(ansName != null && nameList.contains(ansName.trim())){
	    		thisWkAns.points = Integer.parseInt(updateWeight);
	    	}
	    } 
		return this;
	}
	
	/**
	 * This method translates the RINFO JSON to Question specific user response.
	 * @param jsonObject A JSONObject which represents RINFO JSON.
	 * @return A <code>Map<String, String></code> Object
	 * @throws Exception
	 */
	public Map<String, String> translateRinfoToQuestionResponse(JSONObject jsonObject, UserResponseWithPolicyTO userResponseWithPolicyTO) throws Exception {
		Map<String, String> questionResponseMap = null;
		Map<String,String> answerPropertyMap = null;
		String qid = "";
		String Q_PREFIX = "";
		worksheet_answer worksheetAnswerObject = null;
		try{
			if(jsonObject != null){
				qid = jsonObject.getString(CaaConstants.QUESTION_ID_FOR_RINFO);
				Q_PREFIX = new StringBuilder("Q_").append(qid).toString();
				questionResponseMap = new HashMap<String, String>();
				questionResponseMap.put(Q_PREFIX, "-2-2");
				JSONObject wkResponses = jsonObject.getJSONObject(RESPONSE);
				Iterator<String> keys = wkResponses.keys();
				while(keys.hasNext()){
					String answerName = keys.next();
					String answerType = "";
					JSONObject wkResponseObj = wkResponses.getJSONObject(answerName);
					if(wkResponseObj != null){
						answerType = wkResponseObj.getString(CaaConstants.TYPE_IN_RINFO);
						if(StringUtils.isNotBlank(answerName) && StringUtils.isNotBlank(answerType)){
							worksheetAnswerObject = worksheet_answer.new_answer(answerName, answerType);
							answerPropertyMap = worksheetAnswerObject.translateRinfoToWorksheetAnswerResponse(wkResponseObj, qid, userResponseWithPolicyTO);
							questionResponseMap.putAll(answerPropertyMap);
						}
					}
				}
			}
		}catch (Exception e) {
			_logger.error("Exception while translating Rinfo to question response for WORKSHEET type question" , e);
			throw new BusinessException("Exception while translating Rinfo to question response for WORKSHEET type question for given jsonObject : "+jsonObject);
		}
		return questionResponseMap;
	}
	
	public boolean  ifFeatureHasWimbaDebug(){
		LicenseService licenseService = (LicenseService)BeanExtractionUtil.getAppSpringBean("licenseService");
		List<String> debugTransactions =null;
		boolean bool = true;
		try {
			debugTransactions = tp_utils.listToList(licenseService.getLicenseDataByType(licenseManager.LICENSE_TYPE_HMDEBUG), ",");
			bool = debugTransactions.contains(classware_hm.WIMBA_DEBUG);
		} catch (Exception e) {
			_logger.error("Error while accessing featureInfo ",e);
		}
		return (bool);
	}

	/*
	 * (non-Javadoc)
	 * @see com.mcgrawhill.ezto.test.questions.question#itemScoringMode()
	 */
	@Override
	public String itemScoringMode(QuestionMetaDataTO qMetaData)
	{
		 boolean wimbaDebug = ifFeatureHasWimbaDebug();
		
		if(isQuestionPointZero(qMetaData)){
			return("automatic");
		}
		
		if((wimbaDebug && this.wimbaEnabled) || manualScoring(qMetaData)){
			return "manual";
		}
		
		if (completeIncompleteGrading()){
			return "automatic";
		}
		
		for (int i=0; i<answers.size(); i++)
		{
			worksheet_answer thisAnswer= (worksheet_answer)answers.elementAt(i);
			
			//TCS added code for Mangrade changes
			if(thisAnswer.type == worksheet_answer.ANSWER_TYPE_essay && thisAnswer.points > 0 ){
				return "manual";
			}
			if(thisAnswer.type == worksheet_answer.ANSWER_TYPE_avRecording || thisAnswer.type == worksheet_answer.ANSWER_TYPE_videoVoiceover){
				if(thisAnswer.answerProperties != null){
					boolean completeIncompleteGrading = thisAnswer.answerProperties.getBoolean(question.COMPLETE_INCOMPLETE_GRADING, false);
					if( !completeIncompleteGrading ){
						return "manual";
					}
				}
			}
		}

		return("automatic");
	}
	@Override
	public JSONObject getRedactedQinfoJson(UserResponseWithPolicyTO userResponseWithPolicyTO,String testId, String mode) throws Exception {
		JSONArray props = null;
		JSONObject result = super.getRedactedQinfoJson(userResponseWithPolicyTO,testId,mode);
		boolean isReviewMode =  REVIEW_MODE.equals(mode);
		richProperties questionProp = null;
		ResponseTO responseTO = null;
		CustomMap<String, String> testParamMap = null;
		Map<String,String> policyMap = new HashMap<String,String>();
		String pallete = this.questionProperties.getString(question.PALETTE_SUPPORT, "");
		if(userResponseWithPolicyTO != null ){
			if(userResponseWithPolicyTO.getPolicyTO() != null){
				policyMap = userResponseWithPolicyTO.getPolicyTO().getPolicyMap();
			}
			responseTO = userResponseWithPolicyTO.getResponseTO();
			testParamMap = responseTO.getTestParameter();
		}
		if(isReviewMode){
			questionProp = new richProperties(this.questionProperties.toXML());
			questionProp = getPolicyBasedQuestionProperties(questionProp, userResponseWithPolicyTO);
			/*boolean showFeedBack = QuestionUtil.showExplanation(testParamMap, policyMap);
			if(showFeedBack){
				result.put(XML_WS_COMMONFEEDBACK, tp_utils.safeJSON(this.explanation));
			}*/
		}else{
			questionProp = richProperties.newInstance("TestModeRedactedProperties");
			questionProp.setString(question.PALETTE_SUPPORT, pallete);
			questionProp.setBD( "questionPoint", this.questionProperties.getBD(classware_hm.HM_POINTS, "10") );
		}
		props = questionProp.exportJSON();
		if (props != null){
			result.put( "properties", props );
		}
		result.put(XML_WS_ANSWER_SET, worksheet_answer.getRedactedJSONlist(userResponseWithPolicyTO,testId,mode,this,result));	
		
		/*List<String> answersName = new ArrayList<String>();
		if(result.has(STEM)){
			String stem = result.getString(STEM);
			for (int i=0; i<answers.size(); i++) {
				worksheet_answer thisAnswer= (worksheet_answer)answers.elementAt(i);
				answersName.add(thisAnswer.name);
				stem = tp_utils.substitute(stem, "__"+ thisAnswer.name +"__", "<wkans name='"+thisAnswer.name+"' type='"+worksheet_answer.shortTypeString(thisAnswer.type)+"'>");
				stem = tp_utils.substitute(stem, "__" + thisAnswer.name + "(", "<wkans name='"+thisAnswer.name+"' type='"+worksheet_answer.shortTypeString(thisAnswer.type)+"'>");
				
			}
			result.put(STEM, stem);
		}*/
		result = super.getMediaDereferencedJson(result, testId);
		
		return result;
	}
	
	/* (non-Javadoc)
	 * @see com.mcgrawhill.ezto.test.questions.question#translateQuestionResponseToRinfo(com.mcgrawhill.ezto.api.caa.services.transferobject.UserResponseWithPolicyTO, com.mcgrawhill.ezto.api.caa.services.transferobject.QuestionMetaDataTO, java.lang.String)
	 */
	@Override
	public JSONObject translateQuestionResponseToRinfo(UserResponseWithPolicyTO userResponseWithPolicyTO, QuestionMetaDataTO questionMetaDataTO, String mode ,TestTO testTo) throws Exception{
		JSONObject rinfoJson = super.translateQuestionResponseToRinfo(userResponseWithPolicyTO, questionMetaDataTO, mode, testTo);
		ResponseTO responseTO = userResponseWithPolicyTO.getResponseTO();
		boolean isFlashPresent = false;
		boolean isExternalPresent = false;
		String randomStr = "";
		
		for (int i=0; i<this.answers.size(); i++){
			if(((worksheet_answer)answers.get(i)).type == worksheet_answer.ANSWER_TYPE_external){
				isExternalPresent = true;
			}
			if(((worksheet_answer)answers.get(i)).type == worksheet_answer.ANSWER_TYPE_flash ){
				isFlashPresent = true;
			}
		}
		if(isFlashPresent && !isExternalPresent){
			String useRnd= "Q_" + this.sqlID + "_rnd";			
			QuestionWiseResponseTO questionWiseResponseTO = responseTO.getResponseMap().get(this.sqlID);
			if(questionWiseResponseTO != null){
				CustomMap<String, String> questionParam = (CustomMap<String, String>)questionWiseResponseTO.getQuestionParameters();
				randomStr = questionParam.getParam(useRnd);				
			}
		}
		if(isExternalPresent){
			randomStr = QuestionUtil.getEncryptedRandomVariable(responseTO, this);			
		}
		rinfoJson.put("random",randomStr);
		rinfoJson.put("response", worksheet_answer.translateQuestionResponseToRinfo(userResponseWithPolicyTO, questionMetaDataTO, mode, this,testTo));	
		/**
		 * Get feed back
		 */
		FeedBackTO feedBackTO = getQuestionFeedback(userResponseWithPolicyTO, null, mode);
		/**
		 * Create feed back Json
		 */
		JSONObject feedBackJson = createFeebBackJson(feedBackTO, userResponseWithPolicyTO);
		if(feedBackJson != null && feedBackJson.length() > 0){
			/**
			 * Apply tool-tip dereference and media dereference
			 */
			feedBackJson = getMediaDereferencedJson(feedBackJson, responseTO.getTestID());
			/**
			 * put the feedback in rinfo
			 */
			rinfoJson.put(CaaConstants.FEEDBACK, feedBackJson);
		}
		return rinfoJson;
	}
	
	/* (non-Javadoc)
	 * @see com.mcgrawhill.ezto.test.questions.question#computeCorrectAnswer(com.mcgrawhill.ezto.api.caa.services.transferobject.ResponseTO, com.mcgrawhill.ezto.api.caa.services.transferobject.QuestionWiseResponseTO)
	 */
	public void computeCorrectAnswer(ResponseTO responseTO, QuestionWiseResponseTO questionWiseResponseTO,TestTO testTo) throws Exception {
		worksheet_answer.computeCorrectAnswer(responseTO, this,testTo);
	}
	
	/**
	 * @see com.mcgrawhill.ezto.test.questions.question#generateScrambledChoice(Map, Map)
	 */
	public void generateScrambledChoice(CustomMap<String, String> questionResponseMap, Map<String, String> requestMap) throws Exception{
		worksheet_answer.generateScrambledChoice(questionResponseMap, requestMap, this);
	}
  
	public BigDecimal getAnsweredPercentage(CustomMap<String, String> questionParam,String responseKey,QuestionWiseResponseTO questionWiseResponseTO) throws Exception {
		BigDecimal completeNess = new BigDecimal(0.00).setScale(2, RoundingMode.HALF_UP);
		int totolweight = 0;
		int correctWeight = 0;
		for(int index = 0 ; index < answers.size() ; index++){
			worksheet_answer worksheetAns = (worksheet_answer)answers.get(index);
			String ansPrefixKey = responseKey + "_"+ worksheetAns.name;
		    int weight = worksheetAns.getAnswerWeight(questionParam, ansPrefixKey, questionWiseResponseTO);
		    totolweight = totolweight + 1 ;
		    correctWeight =  correctWeight + weight;			
		}
		if(correctWeight > 0 && totolweight > 0){
			
			BigDecimal correctWeightBD =new BigDecimal(correctWeight);
			BigDecimal totolweightBD = new BigDecimal(totolweight);
			correctWeightBD = correctWeightBD.multiply(new BigDecimal(100));
			completeNess = correctWeightBD.divide(totolweightBD, 2, BigDecimal.ROUND_HALF_UP);
			//completeNess = correctWeight*100/totolweight;
		}		
		return completeNess;
	}
	
	@Override
	public void updateCompletionStatus(QuestionWiseResponseTO questionWiseResponseTO , StringBuilder compStatus , String questionid) throws Exception {
		int answeredCount = 0;
		for(int index = 0 ; index < answers.size() ; index++){
			worksheet_answer worksheetAns = (worksheet_answer)answers.get(index);
			String ansPrefixKey = "Q_" + this.sqlID + "_"+ worksheetAns.name;
		    boolean isAnswered = worksheetAns.updateAnswerCompletion(questionWiseResponseTO, compStatus, ansPrefixKey);	
		    if(isAnswered){
		    	answeredCount++;
		    }
		}	
		if(answeredCount == answers.size() && StringUtils.isBlank(referenceTag)){
			String plusQuestionId = new StringBuilder("+").append(questionid).toString();
			if(!(compStatus.toString()).contains(plusQuestionId)){
				compStatus.append(plusQuestionId);
			}			
		}
	}
	
	@Override
	protected FeedBackTO getQuestionFeedback(UserResponseWithPolicyTO userResponseWithPolicyTO,Map<Integer, String> userResponseMap, String mode) throws Exception {
		FeedBackTO feedBackTO = super.getQuestionFeedback(userResponseWithPolicyTO, userResponseMap, mode);
		if(feedBackTO == null){
			return feedBackTO;
		}
		String theFeedback= this.explanation;
		if(StringUtils.isNotBlank(theFeedback)){
			theFeedback = tp_utils.safeJSON(theFeedback);
			feedBackTO.setFeedBack(theFeedback);
		}
		return feedBackTO;
	}

}
