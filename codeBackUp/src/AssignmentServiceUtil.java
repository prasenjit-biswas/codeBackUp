package com.mcgrawhill.ezto.api.caa.services.utilities;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mcgrawhill.ezto.admin.licenseManager;
import com.mcgrawhill.ezto.api.caa.caaconstants.CaaConstants;
import com.mcgrawhill.ezto.api.caa.dao.datatransferobject.AttemptDTO;
import com.mcgrawhill.ezto.api.caa.services.AssignmentService;
import com.mcgrawhill.ezto.api.caa.services.CacheService;
import com.mcgrawhill.ezto.api.caa.services.ItemService;
import com.mcgrawhill.ezto.api.caa.services.ScoringService;
import com.mcgrawhill.ezto.api.caa.services.SecurityService;
import com.mcgrawhill.ezto.api.caa.services.TestService;
import com.mcgrawhill.ezto.api.caa.services.UserResponseService;
import com.mcgrawhill.ezto.api.caa.services.transferobject.EridTO;
import com.mcgrawhill.ezto.api.caa.services.transferobject.PolicyTO;
import com.mcgrawhill.ezto.api.caa.services.transferobject.QuestionMetaDataTO;
import com.mcgrawhill.ezto.api.caa.services.transferobject.QuestionWiseResponseTO;
import com.mcgrawhill.ezto.api.caa.services.transferobject.ResponseTO;
import com.mcgrawhill.ezto.api.caa.services.transferobject.SetActivityTO;
import com.mcgrawhill.ezto.api.caa.services.transferobject.TestTO;
import com.mcgrawhill.ezto.api.caa.services.transferobject.UserResponseWithPolicyTO;
import com.mcgrawhill.ezto.api.exception.BusinessException;
import com.mcgrawhill.ezto.api.license.services.LicenseService;
import com.mcgrawhill.ezto.integration.classware_hm;
import com.mcgrawhill.ezto.integration.hm_grading_r7a;
import com.mcgrawhill.ezto.test.test;
import com.mcgrawhill.ezto.test.questions.question;
import com.mcgrawhill.ezto.test.questions.question_types.lsi;
import com.mcgrawhill.ezto.test.questions.question_types.maple;
import com.mcgrawhill.ezto.test.questions.question_types.wwtb;
import com.mcgrawhill.ezto.utilities.CustomMap;
import com.mcgrawhill.ezto.utilities.tp_utils;

@Component("AssignmentServiceUtil")
public class AssignmentServiceUtil {
	
	@Autowired
	TestUtil testUtil;
	
	@Autowired
	LicenseService licenseService;

	@Autowired
	TagUtil tagUtil;

	@Autowired
	ScoringService scoringService;
	
	@Autowired
	UserHelper userHelper;

	@Autowired
	PolicyUtil policyUtil;

	@Autowired
	AssignmentService assignmentService;

	@Autowired
	ItemService itemService;
	
	@Autowired
	SecurityService securityService;
	
	@Autowired
	CacheService cacheService;
	
	@Autowired
	ResponseUtil responseUtil;
	
	@Autowired
	UserResponseService userResponseService;

	@Autowired
	LightWeightQuestionFactory lightWeightQuestionFactory;
	
	@Autowired
	SecurityUtil securityUtil;
	
	/** Reference of the <code>TestService</code> class */
	@Autowired
	TestService testService;
	
	/** Reference of the <code>UserResponseServiceImplHelper</code> class */
	@Autowired
	UserResponseServiceImplHelper userResponseServiceImplHelper;
	
	private static final Logger _logger = Logger.getLogger(AssignmentServiceUtil.class);

	private List<String> keyList = null;
	
	/**
	 * This method returns an array of JSON containing information about
	 * attemptNo,print policy,EZTO tolerance policy ,accents policy, Section Title , Course Title
	 * Student and professors name & email etc information .
	 * @param testParamMap
	 * @return
	 */
	public JSONArray getAvarJSON(ResponseTO responseTO) throws Exception {
		JSONArray jsAvarArray = new JSONArray();
		String attemptNO = "";
		CustomMap<String, String> testParamMap = null;
		Map<String,String> policyMap = null;
		PolicyTO policyTO = null;
		if(responseTO != null){
			attemptNO = responseTO.getAttemptNo();
			testParamMap = (CustomMap<String, String>)responseTO.getTestParameter();
			policyTO = responseUtil.fetchTestPolicy(responseTO);
			jsAvarArray.put(testUtil.getJsonObject("attemptNo", attemptNO));
			if(testParamMap != null){
				populateAvarArray(testParamMap, jsAvarArray);
				if(policyTO != null){
					policyMap = policyTO.getPolicyMap();
					if(policyMap != null && policyMap.size() > 0){
						for(String policyName : policyMap.keySet()){
							String policyValue = policyMap.get(policyName);
							//added code to remove POLICY_checkwork_limit from avars
							//if connect providing empty value (infinite check my work)
							if(classware_hm.POLICY_checkwork_limit.equals(policyName) && StringUtils.isBlank(policyValue) ){
								continue;
							}
							if(classware_hm.P_INSTRUCTIONS.equals(policyName)){
								continue;
							}
							JSONObject jsObject = testUtil.getJsonObject(policyName , policyValue);
							if(jsObject != null){
								jsAvarArray.put(jsObject);
							}
						}
					}
				}
			}
		}
		return jsAvarArray;
	}
	
	/**
	 * This method returns an array of JSON containing information about
	 * attemptNo, Section Title , Course Title
	 * Student and professors name & email etc information except policies from attempt blob .
	 * @param testParamMap
	 * @return
	 */
	public void populateAvarArray(CustomMap<String, String> testParamMap , JSONArray jsAvarArray) throws Exception {
		if(testParamMap != null){
			if(jsAvarArray == null){
				jsAvarArray = new JSONArray();
			}
			jsAvarArray.put(testUtil.getJsonObject("iname", tp_utils.convertFromUTF8(testParamMap.getParam(classware_hm.AP_INSTRUCTOR_NAME))));
			jsAvarArray.put(testUtil.getJsonObject("iemail", tp_utils.convertFromUTF8(testParamMap.getParam(classware_hm.POLICY_LSI_instructoremail))));
			jsAvarArray.put(testUtil.getJsonObject("sname", tp_utils.convertFromUTF8(testParamMap.getParam(classware_hm.HEADER_STUDENT_NAME))));
			jsAvarArray.put(testUtil.getJsonObject("semail", tp_utils.convertFromUTF8(testParamMap.getParam(classware_hm.POLICY_LSI_studentemail))));
			jsAvarArray.put(testUtil.getJsonObject("atitle", replaceWithHTMLTag(tp_utils.convertFromUTF8(testParamMap.getParam(classware_hm.HEADER_ASSIGNMENT_TITLE)))));
			jsAvarArray.put(testUtil.getJsonObject("ctitle", tp_utils.convertFromUTF8(testParamMap.getParam(classware_hm.HEADER_COURSE_TITLE))));
			jsAvarArray.put(testUtil.getJsonObject("stitle", tp_utils.convertFromUTF8(testParamMap.getParam(classware_hm.HEADER_SECTION_TITLE))));
			jsAvarArray.put(testUtil.getJsonObject("userId", tp_utils.convertFromUTF8(testParamMap.getParam(classware_hm.STUDENT_UID))));
			jsAvarArray.put(testUtil.getJsonObject("instructorId", tp_utils.convertFromUTF8(testParamMap.getParam(classware_hm.INSTRUCTOR_UID))));
			jsAvarArray.put(testUtil.getJsonObject("sectionId", tp_utils.convertFromUTF8(testParamMap.getParam(classware_hm.SECTION_ID))));
			jsAvarArray.put(testUtil.getJsonObject("activityId", tp_utils.convertFromUTF8(testParamMap.getParam(classware_hm.ACTIVITY_ID))));
		}
	}
	/**
	 * This method populates URL related information like EBOOK , 
	 * TEGRITY, REPORT , HELP , EXIT into the AJSON
	 * @param role
	 * @param sectionID
	 * @param ebookURL
	 * @param testparamMap
	 * @return
	 * @throws Exception
	 */
	public JSONObject getURLsJson(String role, UserResponseWithPolicyTO userResponseWithPolicyTO) throws Exception {
		JSONObject jsObjectUrls = new JSONObject();
		String ebookURL = "";
		String tegrity = "";
		String exitURL = "";
		ResponseTO responseTO = null;
		PolicyTO policyTO = null;
		Map<String,String> policyMap = null;
		if(userResponseWithPolicyTO != null){
			responseTO = userResponseWithPolicyTO.getResponseTO();
			policyTO = userResponseWithPolicyTO.getPolicyTO();
			if(policyTO != null){
				policyMap = policyTO.getPolicyMap();
			}
		}
		if(responseTO != null){
			ebookURL = responseTO.getParam(classware_hm.EBOOKURL);
		}
		if(policyMap != null && policyMap.size() > 0){
			tegrity =  policyMap.get(classware_hm.POLICY_tegrity);
		}
		jsObjectUrls.put("ebook", ebookURL);
		jsObjectUrls.put("tegrity",tegrity);
		jsObjectUrls.put("report", licenseService.getLicenseDataByType(licenseManager.ASK_INSTRUCTOR_URL));
		jsObjectUrls.put("help", licenseService.getLicenseDataByType(licenseManager.LICENSE_TYPE_CONNECT_TUNNEL_HELP));
		exitURL = getExitURL(responseTO);
		jsObjectUrls.put("exit",exitURL);
		return jsObjectUrls;
	}
	
	/**
	 * This method is going to compose return URL from several informations from testParameter like role ,server URL 
	 * informations etc 
	 * @param responseTO
	 * @return
	 * @throws Exception
	 */
	public String getExitURL(ResponseTO responseTO) throws Exception {
		String defaultURL = "";
		String exitURL = "";
		String role = "";
		String sectionID = "";
		String returnURL = "";
		CustomMap<String, String> testparameter = null;
		if(responseTO != null){
			returnURL = URLDecoder.decode(responseTO.getParam(classware_hm.RETURN_URL), "UTF-8");
			role = responseTO.getParam(classware_hm.ROLE);
			sectionID = responseTO.getSectionID();
			testparameter = responseTO.getTestParameter();
			defaultURL = createdefaultUrl(role, sectionID , testparameter);
			exitURL = composeReturnURL(returnURL , defaultURL);
		}
		return exitURL;
	}
	
	/**
	 * This method is going to compose return URL from several informations from request object(converted to requestMap) like role ,server URL 
	 * informations etc 
	 * @param responseTO
	 * @return
	 */
	public String getExitURL(Map<String, String> requestMap) {
		String defaultURL = "";
		String exitURL = "";
		String role = "";
		String sectionID = "";
		String returnURL = "";
		try{
			if(requestMap != null && requestMap.size() > 0){
				returnURL = URLDecoder.decode(requestMap.get(classware_hm.RETURN_URL), "UTF-8");
				role = requestMap.get(classware_hm.ROLE);
				sectionID = requestMap.get(classware_hm.SECTION_ID);
				defaultURL = createdefaultUrl(role, sectionID , (RequestMap<String,String>)requestMap);
				exitURL = composeReturnURL(returnURL , defaultURL);
			}
		}catch (Exception e) {
			_logger.error("Exception while constructing return URL");
		}
		return exitURL;
	}
	
	/**
	 * This method populates AJSON for postSubmission and mode related informations
	 * @param responseTO
	 * @param infomap
	 * @param aJsonObj
	 * @throws Exception
	 */
	public String getSubmissionInfo(boolean hasSubmission , Map<String,String> infomap) throws Exception {
		String status = "";
		String mode = infomap.get(CaaConstants.MODE);
		String role = infomap.get(CaaConstants.ROLE);
		_logger.info("#### infomap : " + infomap);
		if(hasSubmission || (UserResponseService.REVIEW_MODE.equals(mode) && testUtil.instructorRole(role))){
			status = CaaConstants.POSTSUBMISSION;
		}else {
			status = CaaConstants.PRESUBMISSION;
		}
		return status;
	}

	/**
	 * This method returns a JSON object containing all the assignment level scoring related
	 * information
	 * @param responseTO
	 * @param testTO
	 * @return
	 * @throws Exception
	 */
	public JSONObject getScoreJSON(CustomMap<String,String> testParamMap) throws Exception {
		JSONObject scoringJSONObject = null;
		try{
			if(testParamMap != null && testParamMap.size() > 0) {
				scoringJSONObject = new JSONObject();
				scoringJSONObject.put("Totalscore",testParamMap.getParam("rawScore"));
				scoringJSONObject.put("MaxScore",testParamMap.getParam("rawMaxScore"));
				scoringJSONObject.put("PctString",testParamMap.getParam("rawPctString"));
				scoringJSONObject.put("ScoreString",testParamMap.getParam("rawScoreString"));
				scoringJSONObject.put("AutoGradedQ",testParamMap.getParam(classware_hm.AUTOMATICALLY_GRADED));
				scoringJSONObject.put("ManuallyGradedQ",testParamMap.getParam(classware_hm.MANUALLY_GRADED));
				scoringJSONObject.put("DoneManuallyGradedQ",testParamMap.getParam(classware_hm.DONE_MANUALLY_GRADED));
				scoringJSONObject.put("Atleast_one_manual_graded_question",testParamMap.getParam(classware_hm.ATLEAST_ONE_MANUAL_GRADED_QUESTION));
				scoringJSONObject.put("Atleast_one_manual_graded_question_graded",testParamMap.getParam(classware_hm.ATLEAST_ONE_MANUAL_GRADED_QUESTION_GRADED));
				scoringJSONObject.put("All_manual_graded_questions",testParamMap.getParam(classware_hm.ALL_MANUAL_GRADED_QUESTIONS));
				scoringJSONObject.put("Auto_graded_question_student_score",testParamMap.getParam(classware_hm.AUTO_GRADED_QUESTION_STUDENT_SCORE));
				scoringJSONObject.put("Auto_graded_question_total_score",testParamMap.getParam(classware_hm.AUTO_GRADED_QUESTION_TOTAL_SCORE));
				scoringJSONObject.put("Manual_graded_question_student_score",testParamMap.getParam(classware_hm.MANUAL_GRADED_QUESTION_STUDENT_SCORE));
				scoringJSONObject.put("Manual_graded_question_total_score",testParamMap.getParam(classware_hm.MANUAL_GRADED_QUESTION_TOTAL_SCORE));
			}
		}catch (Exception e) {
			throw e; 
		}
		return scoringJSONObject;
	}
	
	/**
	 * This method returns a JSON object containing all the assignment level scoring related
	 * information after calculating the score.
	 * @param responseTO
	 * @param testTO
	 * @return
	 * @throws Exception
	 */
	public JSONObject calculateScoreJSON(ResponseTO responseTO , TestTO testTO ) throws Exception {
		scoringService.calculateScore(responseTO, testTO);
		return getScoreJSON(responseTO.getTestParameter());
	}

	/**
	 * This method populates and repair question list for an attempt if it is not properly arranged
	 * @param testTO
	 * @param bopTestParamMap
	 * @throws Exception
	 */
	/*public void repairQlist(TestTO testTO, Map<String, String> testParamMap) throws Exception {
		List<Map<String,String>> questionTable = null;
		Map<String,String> qInfo = null;
		List<QuestionMetaDataTO> deafultQuestionList = null;
		String idlist = "";
		String refID = "";
		if(testTO != null){
			questionTable = new ArrayList<Map<String,String>>();
			deafultQuestionList = tagUtil.getDefaultGroup(testTO.getTaggingList(),testTO.getQuestionMetaDataList());
			for(QuestionMetaDataTO questionMetaDataTO : deafultQuestionList){
				if(!StringUtils.isBlank(questionMetaDataTO.getRefTag())){
					if(qInfo == null){
						qInfo = new HashMap<String,String>();
						qInfo.put(CaaConstants.TAG, questionMetaDataTO.getRefTag());
					}else{
						String oldTag = (String) qInfo.get(CaaConstants.TAG);
						if (!oldTag.equals(questionMetaDataTO.getRefTag())) {
							questionTable.add(qInfo);
							qInfo = new HashMap<String,String>();
							qInfo.put(CaaConstants.TAG, questionMetaDataTO.getRefTag());
						}
					}
					if (questionMetaDataTO.getQuestionType() == question.QUESTION_TYPE_sectionbreak){
						qInfo.put(CaaConstants.REFERENCE, questionMetaDataTO.getQuestionID());
					}else {
						idlist = (String) qInfo.get(CaaConstants.ID);
						if (StringUtils.isBlank(idlist)){ 
							idlist = "";
						}else{
							idlist += ",";
						}
						idlist += questionMetaDataTO.getQuestionID();
						qInfo.put(CaaConstants.ID, idlist);
					}
				}else {
					if (qInfo != null) {
						questionTable.add(qInfo);
						qInfo = null;
					}
					qInfo = new HashMap<String,String>();
					qInfo.put(CaaConstants.ID, questionMetaDataTO.getQuestionID());
					questionTable.add(qInfo);
					qInfo = null;
				}
			}
			if (qInfo != null) {
				questionTable.add(qInfo);
				qInfo = null;
			}
			String qlistParam = "";
			for (Map<String,String> questionInfo : questionTable) {
				if (!StringUtils.isBlank(qlistParam)){
					qlistParam += ";";
				}
				refID = questionInfo.get(CaaConstants.REFERENCE);
				if (!StringUtils.isBlank(refID)){
					qlistParam += refID + ",";
				}
				if(qInfo != null){
					qlistParam += (String) qInfo.get(CaaConstants.ID);
				}
			}
			testParamMap.put(CaaConstants.QLIST, qlistParam);
		}
	}*/

	/**
	 * This method builds the current response integrating with the previous response because while Build On Previous
	 * policy is set , then previous response has an impact on the current response.
	 * This integration of current response and the previous response is done in this  method
	 * @param currentResponseTO
	 * @param prevResponseTO
	 * @param testTO
	 * @param requestMap
	 * @throws Exception
	 */
	public void buildCurrentResponse(ResponseTO currentResponseTO, ResponseTO prevResponseTO, TestTO testTO, Map<String,String> requestMap) throws Exception {
		CustomMap<String,String> previousTestParamMap = null;
		CustomMap<String, String> currentTestParamMap = null;
		Map<String,QuestionWiseResponseTO> previousQuestionwiseResponseMap = null;
		Map<String, QuestionWiseResponseTO> currentQuestionwiseResponseMap = null;
		QuestionWiseResponseTO questionWiseResponseTO = null;
		List<QuestionMetaDataTO> questionList = testTO.getQuestionMetaDataList();
		String response = "";
		QuestionMetaDataTO questionMetaDataTO = null;
		Map<String,Boolean> correctPageMap = null;
		previousTestParamMap = (CustomMap<String,String>)prevResponseTO.getTestParameter();
		currentTestParamMap = (CustomMap<String, String>)currentResponseTO.getTestParameter();
		previousQuestionwiseResponseMap = prevResponseTO.getResponseMap();
		// Removed report specific policy from previous attempt 
		if(CaaConstants.TRUE.equals(previousTestParamMap.get(classware_hm.BUILD_ON_PREVIOUS))){
			previousTestParamMap.remove(CaaConstants.REPORT_POLICY_feedback_allcorrect);
			previousTestParamMap.remove(CaaConstants.REPORT_POLICY_grading);
			previousTestParamMap.remove(CaaConstants.REPORT_POLICY_solution);
		}
		currentQuestionwiseResponseMap = currentResponseTO.getResponseMap();
		for(String qid : previousQuestionwiseResponseMap.keySet()) {
			QuestionWiseResponseTO currectQuestionWiseResponseTO = new QuestionWiseResponseTO();
			currectQuestionWiseResponseTO.setQuestionID(qid);
			currentQuestionwiseResponseMap.put(qid,currectQuestionWiseResponseTO);
			questionWiseResponseTO = previousQuestionwiseResponseMap.get(qid);
			previousTestParamMap.putAll(questionWiseResponseTO.getQuestionParameters());
			response = getEvaluationOfResponse(questionWiseResponseTO);
			if (CaaConstants.FALSE.equals(response)){
				questionMetaDataTO = itemService.getQuesionMetaInformationFromTest(questionList, qid);
				resetThirdPartyMathAlgos(questionMetaDataTO, currentTestParamMap);
			}
			currentTestParamMap.replaceParam("Q_" + qid + CaaConstants.FORMERCORRECT,response);
			if (!StringUtils.isBlank(questionWiseResponseTO.getComment())) {
				currentTestParamMap.replaceParam("Q_" + qid + "_" + CaaConstants.FORMER_COMMENT, questionWiseResponseTO.getComment());
			}
		}
		populateCurrentResponseFromPreviousResponse(currentResponseTO, prevResponseTO, questionList);
		currentTestParamMap.replaceParam(CaaConstants.QLIST, questionListSanityCheck(currentTestParamMap.getParam(CaaConstants.QLIST),questionList));
		correctPageMap = getPageCorrectnessMap(currentTestParamMap , currentQuestionwiseResponseMap, questionList);
		
		for(String firstQid : correctPageMap.keySet()){
			boolean pageCorrectness = correctPageMap.get(firstQid);
			String correctness = CaaConstants.FALSE;
			if(pageCorrectness){
				correctness = CaaConstants.TRUE;
			}
			currentTestParamMap.replaceParam("Q_" + firstQid + CaaConstants.PAGECORRECTNESS, correctness);
		}
		currentTestParamMap.putAll(getTestParamMap(requestMap));
		policyUtil.populatePolicies(currentTestParamMap, requestMap);
		if(StringUtils.isBlank(currentTestParamMap.getParam(CaaConstants.QLIST))){
			currentTestParamMap.replaceParam(CaaConstants.QLIST, userHelper.prepareQListParam(testTO, null));
		}
	}

	/**
	 * This method populates the keys in a list which will be ignored while migrating previous response
	 * with the current response
	 */
	public void populateKeyList(){
		if(keyList == null || keyList.size() == 0 ) {
			keyList = new ArrayList<String>();
			keyList.add(TestService.TIMESTAMP);
			keyList.add(classware_hm.COMPLETION_STATUS);
			keyList.add(classware_hm.COMPLETION_STATUS_ONEWAY);
			keyList.add(classware_hm.ALL_CORRECT);
			keyList.add(classware_hm.CHECKING_WORK);
			keyList.add(classware_hm.DURING_ASSIGNMENT);
			keyList.add(classware_hm.POST_SUBMISSION);
			keyList.add(classware_hm.PREVIEW_QUESTION_MODE);
			keyList.add(CaaConstants.SUBMISSION);
			keyList.add(classware_hm.DYNAMIC_POLICIES);
			keyList.add(classware_hm.POLICY_endtime);
		}
	}

	/**
	 * This method validates the keys which will be ignored while migrating the
	 * previous response with the new response
	 * @param key
	 * @return
	 * @throws Exception
	 */
	private boolean validateKey(String key) throws Exception{
		boolean flag = false;
		if(keyList == null || keyList.size() == 0 ){
			populateKeyList();
		}
		if(!StringUtils.isBlank(key)){
			if(keyList.contains(key)){
				flag = true;
			}
		}
		return flag;
	}

	/**
	 * This method does a start up validation that if the key starts with some following
	 * information then these keys are ignored
	 * @param key
	 * @return
	 * @throws Exception
	 */
	private boolean startUpvalidation(String key) throws Exception{
		if(!StringUtils.isBlank(key)){
			if (key.startsWith(CaaConstants.RAW) || key.endsWith(CaaConstants.START)){
				return true;
			}
		}
		return false;
	}

	/**
	 * This method does a ends up validation that if the key starts with some following
	 * information then these keys are ignored
	 * @param key
	 * @return
	 * @throws Exception
	 */
	private boolean endupValidation(String key)throws Exception{
		if(!StringUtils.isBlank(key)){
			if(key.endsWith(CaaConstants.ANSWERED) || key.endsWith(CaaConstants.RECORDER) || key.endsWith(CaaConstants.CW) || key.endsWith(CaaConstants.SHOWN)
					|| key.endsWith(CaaConstants.ATTCOUNT) || key.endsWith(CaaConstants.TRYANOTHER) || key.endsWith(CaaConstants.START)){
				return true;
			}else if (key.endsWith(classware_hm.ELAPSED_TIME_SUFFIX) || key.endsWith("_" + hm_grading_r7a.INSTRUCTOR_COMMENT) 
					|| key.endsWith("_" + hm_grading_r7a.INLINE_COMMENT) || key.endsWith("_" + hm_grading_r7a.CORRECT_INCORRECT)){
				return true;
			}
		}
		return false;
	}

	/**
	 * This method checks that whether there is any  duplicate  and invalid QIDs are present or not
	 * in the questionList 
	 * @param originalList
	 * @param questionMetaDataTOs
	 * @return
	 * @throws Exception
	 */
	public String questionListSanityCheck(String questionList , List<QuestionMetaDataTO> questionMetaDataTOs) throws Exception {
		String newQuestionList = "";
		Map<String,String> usedIDMap = null;
		String[] pages = null;
		QuestionMetaDataTO theQ = null;
		if(!StringUtils.isBlank(questionList) && questionMetaDataTOs != null && questionMetaDataTOs.size() > 0 ) {
			usedIDMap = new HashMap<String,String>();
			pages = questionList.split(";");
			if(pages != null){
				for (int i = 0;  i < pages.length ; i++) {
					String theItems = "";
					String[] pageItems = pages[i].split(",");
					if(pageItems != null){
						for (int j = 0 ;j < pageItems.length ; j++) {
							String thisItem = pageItems[j];
							if (usedIDMap.get(thisItem) != null) {
								// skip duplicate items
								continue;
							}
							usedIDMap.put(thisItem, thisItem);
							theQ = itemService.getQuesionMetaInformationFromTest(questionMetaDataTOs, thisItem);
							if (theQ == null) {
								// skip non-existent items
								continue; 
							}
							if(!StringUtils.isBlank(theItems)) {
								theItems += ",";
							}
							theItems += thisItem;
						}
						if (theItems.length() > 0) {
							if (!StringUtils.isBlank(newQuestionList)) {
								newQuestionList += ";";
							}
							newQuestionList += theItems;
						}
					}
				}
			}
		}
		return newQuestionList;
	}

	/**
	 * This method resets third part math algos for question types MAPPLE, WWTB , LSI
	 * @param questionMetaDataTO
	 * @param testParamMap
	 * @throws Exception
	 */
	public void resetThirdPartyMathAlgos(QuestionMetaDataTO questionMetaDataTO , CustomMap<String, String> testParamMap) throws Exception {
		if(questionMetaDataTO != null && testParamMap != null && testParamMap.size() > 0 ){
			if (questionMetaDataTO.getQuestionType() == question.QUESTION_TYPE_maple){
				maple.resetThirdPartyMathAlgos(questionMetaDataTO.getQuestionID(), testParamMap);
			}
			if (questionMetaDataTO.getQuestionType() == question.QUESTION_TYPE_wwtb){
				wwtb.resetThirdPartyMathAlgos(questionMetaDataTO.getQuestionID(), testParamMap);
			}
			if (questionMetaDataTO.getQuestionType() == question.QUESTION_TYPE_lsi){
				lsi.resetThirdPartyMathAlgos(questionMetaDataTO.getQuestionID(), testParamMap);
			}
		}
	}

	/**
	 * This method populates current response influenced with previous response for that user
	 * @param currentResponseTO
	 * @param prevResponseTO
	 * @param testTO
	 * @throws Exception
	 */
	public void populateCurrentResponseFromPreviousResponse( ResponseTO currentResponseTO, ResponseTO prevResponseTO, List<QuestionMetaDataTO> questionMetaDataList) throws Exception {
		CustomMap<String,String> previousTestParamMap = null;
		CustomMap<String, String> currentTestParamMap = null;
		Map<String,QuestionWiseResponseTO> previousQuestionwiseResponseMap = null;
		Map<String, QuestionWiseResponseTO> currentQuestionwiseResponseMap = null;
		QuestionWiseResponseTO questionWiseResponseTO = null;
		try{
			previousTestParamMap = (CustomMap<String,String>)prevResponseTO.getTestParameter();
			currentTestParamMap = (CustomMap<String, String>)currentResponseTO.getTestParameter();
			previousQuestionwiseResponseMap = prevResponseTO.getResponseMap();
			currentQuestionwiseResponseMap = currentResponseTO.getResponseMap();
			_logger.info("PREVIOUS Test ParamMap :: " + previousTestParamMap);
			for(String key : previousTestParamMap.keySet()){
				if(validateKey(key) || startUpvalidation(key) || endupValidation(key)){
					continue;
				}
				String value = previousTestParamMap.getParam(key);
				if(!StringUtils.isBlank(value)){
					questionWiseResponseTO = getQuestionwiseResponse(key, currentQuestionwiseResponseMap);
					// look for LSI seeds
					if (key.endsWith(CaaConstants.SEED)){
						value = getModifiedValueForlsiQuestion(key, value,questionMetaDataList);
						populateQuestionParam(key, value, questionWiseResponseTO);
					}else {
						populateTestParamForPreviousBOPRelatedInfo(key, value, currentTestParamMap, questionWiseResponseTO);
					}
				}
			}
		}catch (Exception e) {
			throw e;
		}
	}

	/**
	 * This method populates current question's response from previous response of that user for that question.
	 * If in current scenario , no such response exists for a question ; then new response will be created and inserted into
	 * current response .
	 * @param key
	 * @param currentQuestionwiseResponseMap
	 * @throws Exception
	 */
	public QuestionWiseResponseTO getQuestionwiseResponse( String key, Map<String, QuestionWiseResponseTO> questionwiseResponseMap) throws Exception {
		QuestionWiseResponseTO questionWiseResponseTO = null;
		String[] questionKeys = null;
		String questionID = "";
		if(!StringUtils.isBlank(key)){
			questionKeys = key.split("_");
			questionID = "";
			if(questionKeys != null && questionKeys.length >= 2){
				if("Q".equalsIgnoreCase(questionKeys[0])){
					questionID = questionKeys[1];
				}
			}
			if(!StringUtils.isBlank(questionID) && questionwiseResponseMap != null){
				questionWiseResponseTO = questionwiseResponseMap.get(questionID);
			}
			if(questionWiseResponseTO == null && !StringUtils.isBlank(questionID)){
				questionWiseResponseTO = new QuestionWiseResponseTO();
				questionWiseResponseTO.setQuestionID(questionID);
				questionwiseResponseMap.put(questionID, questionWiseResponseTO);
			}
		}
		return questionWiseResponseTO;
	}

	/**
	 * This method populates a question wise response for an attempt . 
	 * @param key
	 * @param value
	 * @param quesResponseTO
	 * @throws Exception
	 */
	public void populateQuestionParam(String key , String value ,QuestionWiseResponseTO quesResponseTO) throws Exception {
		if(quesResponseTO != null && !StringUtils.isBlank(key) && !StringUtils.isBlank(value)){
			if (value.indexOf(":") == 8 || value.indexOf(":") == 12){
				((CustomMap<String, String>)quesResponseTO.getQuestionParameters()).replaceParam(key, new StringBuilder(value.substring(0, 4)) .append("z000").append(value.substring(8)).toString());
			}
			else {
				((CustomMap<String, String>)quesResponseTO.getQuestionParameters()).replaceParam(key, new StringBuilder(value.substring(0, 4)) .append("z000").append(value.substring(8)).toString());
			}
		}
	}

	/**
	 * This method is used for handling LSI question related informations . This method returns the modified
	 * value based on LSI question ITERATION LEVEL
	 * @param key
	 * @param value
	 * @param questionMetaDataTOs
	 * @throws Exception
	 */
	public String getModifiedValueForlsiQuestion(String key, String value , List<QuestionMetaDataTO> questionMetaDataTOs) throws Exception {
		String findQID[] = null;
		String qid = "";
		QuestionMetaDataTO questionMetaDataTO = null;
		Map<String, String> questionProperties = null;
		if(!StringUtils.isBlank(key)){
			findQID = key.split("_");
			if (findQID!= null && findQID.length >= 1){
				qid = findQID[0];	// snag the Q
				if (findQID.length >= 2){
					qid = findQID[1];		// get the id
					questionMetaDataTO = itemService.getQuesionMetaInformationFromTest(questionMetaDataTOs, qid);
					if (questionMetaDataTO != null){	// get the question
						questionProperties = questionMetaDataTO.getQuestionProperties();
						if (questionProperties!= null && "1".equals(questionProperties.get(lsi.ITERATION_LEVEL))){
							value = value.replaceFirst("/.*?:/","000000000000:");
						}
					}
				}
			}
		}
		return value;
	}

	/**
	 * This method populates current testsparamMap from previous attempt for Former Correct , PageCorrect,
	 * Recorder related informations
	 * @param key
	 * @param value
	 * @param currentTestParamMap
	 * @param questionWiseResponseTO
	 * @throws Exception
	 */
	public void populateTestParamForPreviousBOPRelatedInfo(String key, String value, CustomMap<String, String> testParamMap , QuestionWiseResponseTO questionWiseResponseTO) throws Exception {
		boolean exists = false;
		if(!StringUtils.isBlank(key) && testParamMap != null){
			if(key.startsWith("Q_") || key.startsWith("P_")){
				if(key.contains(CaaConstants.FORMERCORRECT) || key.contains(CaaConstants.PAGECORRECT)){
					exists = true;
					testParamMap.replaceParam(key, value);
				}
				if (key.endsWith(CaaConstants.RECORDER)){
					exists = true;
					testParamMap.replaceParam(key, value);
				}
				if(!exists && questionWiseResponseTO != null){
					((CustomMap<String, String>)questionWiseResponseTO.getQuestionParameters()).replaceParam(key, value);
				}
			}else {
				testParamMap.replaceParam(key, value);
			}
		}
	}

	/**
	 * This method checks that the complete page (For MULTIPART Questions) is correct or not
	 * If correct is returns true else return false
	 * @param currentTestParamMap
	 * @return
	 * @throws Exception
	 */
	public Map<String,Boolean> getPageCorrectnessMap(CustomMap<String, String> currentTestParamMap , Map<String, QuestionWiseResponseTO> questionwiseResponseMap , List<QuestionMetaDataTO> questionmetaDataList) throws Exception {
		boolean pageCorrectness = false;
		String qList[] = null;
		String idset[] = null;
		String questionList = "";
		String firstQuestion = "";
		Map<String,Boolean> pageCorrectMap = null;
		if(currentTestParamMap != null && currentTestParamMap.size() > 0){
			questionList = currentTestParamMap.getParam(CaaConstants.QLIST);
			if(!StringUtils.isBlank(questionList)){
				_logger.info("QList from BLOB :: " + questionList);
				qList = questionList.split(";");
				if(qList != null) {
					pageCorrectMap = new HashMap<String,Boolean>();
					for(int i = 0 ; i < qList.length ; i++) {
						String thisSet = qList[i];
						idset = thisSet.split(",");
						if(idset != null && idset.length > 0) {
							String useToken = idset[0];
							firstQuestion = useToken;
							if(i == 0){
								currentTestParamMap.replaceParam("itemID", useToken);
							}
							if(idset.length > 1){
								for(int counter = 1 ; counter < idset.length ; counter ++) {	
									useToken = idset[counter];
									if(questionwiseResponseMap != null && questionwiseResponseMap.size() > 0){
										removeExternalResponse(questionwiseResponseMap.get(useToken), itemService.getQuesionMetaInformationFromTest(questionmetaDataList, useToken));
									}
									pageCorrectness = isPageCorrect(currentTestParamMap,useToken);
									if(!pageCorrectness){
										break;
									}
								}
							}else {
								if(questionwiseResponseMap != null && questionwiseResponseMap.size() > 0){
									removeExternalResponse(questionwiseResponseMap.get(useToken), itemService.getQuesionMetaInformationFromTest(questionmetaDataList, useToken));
								}
								pageCorrectness = isPageCorrect(currentTestParamMap,useToken);
							}
							pageCorrectMap.put(firstQuestion, pageCorrectness);
						}
					}
				}
			}
		}
		return pageCorrectMap;
	}

	/**
	 * This method checks that previous response for a question is correct or not
	 * @param currentTestParamMap
	 * @param useToken
	 * @return
	 * @throws Exception
	 */
	private boolean isPageCorrect(CustomMap<String, String> currentTestParamMap, String questionID) throws Exception{
		String pageCorrectnessStatus = "";
		boolean pageCorrectness = false;
		pageCorrectnessStatus = currentTestParamMap.getParam("Q_" + questionID + CaaConstants.FORMERCORRECT);
		if(!StringUtils.isBlank(pageCorrectnessStatus)){
			pageCorrectness = "true".equals(pageCorrectnessStatus);
		}
		return pageCorrectness;
	}

	/**
	 * This method removes external response from WORKSHEET ANSWER EXTERNAL and DOC type questions
	 * @param currentTestParamMap
	 * @param currentQuestionwiseResponseMap
	 * @param questionMetaDataList
	 * @throws Exception
	 */
	public void removeExternalResponse(QuestionWiseResponseTO questionWiseResponseTO, QuestionMetaDataTO questionMetaDataTO) throws Exception {
		question currentQuestion = null;
		if(questionMetaDataTO != null) {
			currentQuestion = lightWeightQuestionFactory.getLightWeightQuestion(questionMetaDataTO.getQuestionTypeIdentifier());
			if(currentQuestion != null && questionWiseResponseTO != null){
				currentQuestion.removeExternalResponseInfo(questionWiseResponseTO , questionMetaDataTO);
			}
		}
	}

	/**
	 * This method populates populates submission id for former submission and header instructions into a map
	 * and returns that map   
	 * @param currentTestParamMap
	 * @throws Exception
	 */
	public CustomMap<String, String> getTestParamMap(Map<String,String> requestMap) throws Exception {
		CustomMap<String, String> currentTestParamMap = new CustomMap<String, String>();
		if(requestMap != null){
			currentTestParamMap.replaceParam(CaaConstants.FEEDBACKSTATE, "");
			currentTestParamMap.replaceParam(CaaConstants.FORMER_SUBMISSION, String.valueOf(requestMap.get(CaaConstants.SUBMISSION_ID)));
			currentTestParamMap.replaceParam(CaaConstants.CLASSWARE_NOTIFY, "");
			currentTestParamMap.replaceParam(CaaConstants.CLASSWARE_NOTIFY_FAILURES, "");
			currentTestParamMap.replaceParam(classware_hm.HEADER_INSTRUCTIONS, tp_utils.convertFromUTF8(requestMap.get(classware_hm.HEADER_INSTRUCTIONS)));
			currentTestParamMap.replaceParam(classware_hm.P_INSTRUCTIONS, requestMap.get(classware_hm.P_INSTRUCTIONS));
		}
		return currentTestParamMap;
	}

	/**
	 * This method returns whether the previous response is correct or incorrect
	 * @param questionWiseResponseTO
	 * @return
	 * @throws Exception
	 */
	public String getEvaluationOfResponse(QuestionWiseResponseTO questionWiseResponseTO) throws Exception {
		boolean correct = false;
		String response = "";
		if(questionWiseResponseTO != null){
			if(questionWiseResponseTO.getPoints() >= classware_hm.DEFAULT_INTERNAL_POINTS || questionWiseResponseTO.getPointsMax() == 0){
				correct = true;
			}
			if (!correct){
				response = CaaConstants.FALSE;
			}else {
				response = CaaConstants.TRUE;
			}
		}
		return response;
	}
	
	/**
	 * This method decoded EAID and return a map
	 * @see SecurityUtil#decodeEAID(String)
	 * @param eaid
	 * @return
	 * @throws Exception
	 */
	public Map<String,String> getDecodedMap(String eaid) throws Exception {
		Map<String,String> eaidMap = null;
		if(!StringUtils.isBlank(eaid)){
			eaidMap = SecurityUtil.decodeEAID(eaid);
		}
		return eaidMap;
	}
	
	/**
	 * This method populates the pages in in the AJSON from the qList of that test.
	 * And it also populates ERIDs for each question in the pages .For multiPart question , each page contains
	 * multiple question's ERIDs
	 * @param questionMetaDataTOList
	 * @param testID
	 * @param mode
	 * @param attemptPK
	 * @return
	 * @throws Exception
	 */
	public JSONArray getQuestionPagesInfoJSON(Map<String,String> eaidMap , UserResponseWithPolicyTO userResponseWithPolicyTO) throws Exception,BusinessException {
		JSONArray jsArrayPages = null;
		JSONObject jsonPageObj = null;
		List<String> eridList = null;
		String attemptPk = "";
		String testID = "";
		String mode = "";
		StringBuilder pageTitle = null;
		String questionList[] = null;
		String sectionBreakQlist[] = null;
		int j = 0;
		int pageIndex = 1;
		String qList = "";
		ResponseTO responseTO = null;
		EridTO eridTO = null;
		try{
			if(userResponseWithPolicyTO != null){
				responseTO = userResponseWithPolicyTO.getResponseTO();
			}
			if(responseTO != null){
				CustomMap<String,String> testparamMap = (CustomMap<String, String>)responseTO.getTestParameter();
				qList = testparamMap.getParam(CaaConstants.QLIST);
				if(!StringUtils.isBlank(qList) && eaidMap != null){
					attemptPk = eaidMap.get("attemptpk");
					testID = eaidMap.get("assignmentid");
					mode = eaidMap.get("mode");
					jsArrayPages = new JSONArray();
					questionList = qList.split(";");
					for(int i = 0 ; i < questionList.length ; i++) {
						jsonPageObj = new JSONObject();
						pageTitle = new StringBuilder(CaaConstants.QUESTION).append(" ").append(pageIndex);
						boolean isSectionbreakQFound = false;
						eridList = new ArrayList<String>();
						if(!questionList[i].contains(",")){
							jsonPageObj = getJSONForQuestionInfo(questionList[i] , userResponseWithPolicyTO , jsonPageObj);
							if(jsonPageObj != null ){
								eridTO = securityUtil.generateEridTO(testID, attemptPk, questionList[i], mode);
								String erid = securityService.getERID(eridTO);
								if(!StringUtils.isBlank(erid)){
									eridList.add(erid);
								}else{
									throw new BusinessException("ERID can not be NULL for the Question ID :: " + questionList[i]);
								}
							}
						}else {
							sectionBreakQlist = questionList[i].split(",");
							for(j = 0 ; j <sectionBreakQlist.length ; j++) {
								String partQid = sectionBreakQlist[j];
								if(!isSectionbreakQFound){
									question sctnBreakQuestion = cacheService.getItem(partQid);
									if(sctnBreakQuestion != null){
										if((sctnBreakQuestion.type == question.QUESTION_TYPE_sectionbreak) && !StringUtils.isBlank(sctnBreakQuestion.referenceTag)){
											jsonPageObj = getJSONForQuestionInfoForSBQ(partQid , userResponseWithPolicyTO , sctnBreakQuestion , jsonPageObj );
											isSectionbreakQFound = true;
										}
									}else {
										throw new BusinessException("NO question exists for the Question ID :: " + sectionBreakQlist[j]);
									}
								}
								eridTO = securityUtil.generateEridTO(testID, attemptPk, partQid, mode);
								String parQErid = securityService.getERID(eridTO);
								if(!StringUtils.isBlank(parQErid)){
									eridList.add(parQErid);
								}else{
									throw new BusinessException("ERID can not be NULL for the Question ID :: " + partQid);
								}
								if(j != 0){
									pageIndex ++;
								}
							}
							pageTitle.append(" - ").append(--pageIndex);
							j = 0;
						}
						jsonPageObj.put(CaaConstants.PAGE_TITLE, pageTitle.toString());
						jsonPageObj.put("erids",eridList);
						jsArrayPages.put(jsonPageObj);
						pageIndex ++ ;
					}
				}
			}
		}catch (Exception e) {
			_logger.error("Exception while populating the ERID for question ids :: " + qList + "for " + e.getMessage(),e);
			throw e;
		}
		return jsArrayPages;
	}
	
	/**
	 * This method returns a jsonObject having  WORKFLOW ,FBQ for
	 * section break question
	 * @param questionMetaDataTO
	 * @return
	 * @throws BusinessException
	 * @throws Exception
	 */
	private JSONObject getJSONForQuestionInfoForSBQ(String qid , UserResponseWithPolicyTO userResponseWithPolicyTO , question sctnbreakQuestion , JSONObject jsonPageObj) throws BusinessException, Exception{
		String policyFeedback = "";
		String workflowState = "";
		String workflow = "";
		String fbq = CaaConstants.UNREQUESTED;
		PolicyTO policyTO = null;
		ResponseTO responseTO = null;
		Map<String,String> policyMap = null;
		if(userResponseWithPolicyTO != null){
			policyTO = userResponseWithPolicyTO.getPolicyTO();
			responseTO = userResponseWithPolicyTO.getResponseTO();
		if(!StringUtils.isBlank(qid) && sctnbreakQuestion != null && policyTO != null ){
			policyMap = policyTO.getPolicyMap();
			if(policyMap != null && policyMap.size() > 0){
				policyFeedback = policyMap.get(classware_hm.POLICY_feedback);
			}
			if(jsonPageObj == null){
				jsonPageObj = new JSONObject();
			}
			workflow = sctnbreakQuestion.questionProperties.getString(question.MP_CUSTOM_PRGRESSION, question.MP_CUSTOM_PRGRESSION_NONE);
			//If workFlow is "twoWay" then it is forcefully set to "bidirectional"
			if(question.MP_CUSTOM_PRGRESSION_TWOWAY.equals(workflow)){
				workflow = CaaConstants.BIDIRECTIONAL;
			}
			jsonPageObj.put(CaaConstants.WORKFLOW, workflow);
			if((sctnbreakQuestion.type == question.QUESTION_TYPE_sectionbreak) && StringUtils.isNotBlank(sctnbreakQuestion.referenceTag) && question.MP_CUSTOM_PRGRESSION_ONEWAY.equals(workflow)){
				//Generate workFlowState based on the last index visited by the user
				workflowState = Integer.toString(getWorkFlowState(responseTO, sctnbreakQuestion.sqlID));
			}
			jsonPageObj.put(CaaConstants.WORKFLOWSTATE, workflowState);
			if(CaaConstants.YES.equals(policyFeedback) && responseUtil.isQuestionResponseLockedFBQ(responseTO, qid)){
				fbq = CaaConstants.REQUESTED;
			}
			jsonPageObj.put(CaaConstants.FBQ, fbq);
		}
		}
		return jsonPageObj;
	}
	
	/**
	 * This method generates the workflowState for multiPart question based on the maximum value of the 
	 * IRIDs array reached by the student in the current attempt for a particular sectionBreak question
	 * @param responseTO
	 * @return
	 * @throws Exception
	 */
	public int getWorkFlowState(ResponseTO responseTO , String sectionBreakQid) throws Exception {
		int workFlowState = 0; 
		String qList = "";
		if(responseTO != null && StringUtils.isNotBlank(sectionBreakQid)){
			qList = responseTO.getParam(CaaConstants.QLIST);
			if(StringUtils.isNotBlank(qList)){
				String questionIDs[] = qList.split(";");
				for(int i = 0 ; i < questionIDs.length ; i++){
					String qid = questionIDs[i];
					//Checking whether the QID from question list represents a multiPart questions set or not
					if(StringUtils.isNotBlank(qid) && qid.contains(",")){
						//Checking whether the multiPart questions set contains the given item or not
						if(qid.contains(sectionBreakQid)){
							String partQids[] = qid.split(",");
							if(partQids != null){
								//Now iterate from the last question of the multiPart list
								for(int j = partQids.length-1 ; j >= 0 ;j--){
									String oneWayRestrictedKey = getOneWayRestrictKey(partQids[j]);
									//Checks whether "oneWayRestrictedKey" is set or not
									if(CaaConstants.TRUE.equals(responseTO.getParam(oneWayRestrictedKey))){
										//If "oneWayRestrictedKey" key is set for that partQuestion then return its index
										return j;
									}
								}
							}
							return workFlowState;
						}
					}
				}
			}
		}
		return workFlowState;
	}
				
	
	/**
	 * This method returns a jsonObject having  WORKFLOW , WORKFLOWSTATE , FBQ for
	 * all question types except section break
	 * @param questionMetaDataTO
	 * @return
	 * @throws BusinessException
	 * @throws Exception
	 */
	private JSONObject getJSONForQuestionInfo(String qid , UserResponseWithPolicyTO userResponseWithPolicyTO, JSONObject jsonPageObj ) throws BusinessException, Exception{
		String policyFeedback = "";
		String fbq = CaaConstants.UNREQUESTED;
		PolicyTO policyTO = null;
		Map<String, String> policyMap = null;
		ResponseTO responseTO = null;
		
		if(userResponseWithPolicyTO != null){
			responseTO = userResponseWithPolicyTO.getResponseTO();
			policyTO = userResponseWithPolicyTO.getPolicyTO();
		}
		if(policyTO != null){
			policyMap = policyTO.getPolicyMap();
		}
		if(!StringUtils.isBlank(qid) &&  policyMap != null){
			policyFeedback = policyMap.get(classware_hm.POLICY_feedback);
			if(jsonPageObj == null){
				jsonPageObj = new JSONObject();
			}
			jsonPageObj.put(CaaConstants.WORKFLOW, CaaConstants.NORMAL);
			jsonPageObj.put(CaaConstants.WORKFLOWSTATE, "0");
			if(CaaConstants.YES.equals(policyFeedback) && responseUtil.isQuestionResponseLockedFBQ(responseTO, qid)){
				fbq = CaaConstants.REQUESTED;
			}
			jsonPageObj.put(CaaConstants.FBQ, fbq);
		}
		return jsonPageObj;
	}
	/**
	 * This method scrambles the question list and returns a new set of question list with different order
	 * from the input question list that it takes as an argument
	 * @param originalList
	 * @param theHandler
	 * @param partialTO
	 * @return
	 */
	public String getScramblePages(String originalList, CustomMap<String,String> testParamMap) {
		String scrambledQList = "";
		String[] qList = null;
		List<String> scrambledList = null;
		int counter = 0;
		if(!StringUtils.isBlank(originalList)){
			qList = originalList.split(";");
			scrambledList = new ArrayList<String>();
			for(int i = 0 ; i < qList.length ; i++){
				scrambledList.add(qList[i]);
			}
			Collections.shuffle(scrambledList);
			for(String qid : scrambledList) {
				if(counter == 0 && testParamMap != null){
					testParamMap.replaceParam(CaaConstants.ITEMID,qid);
				}
				counter ++;
				if(!StringUtils.isBlank(scrambledQList)){
					scrambledQList = scrambledQList + ";";
				}
				scrambledQList = scrambledQList + qid;
			}
		}
		return scrambledQList;
	}
	
	/**
	 * This method will eliminate those question related responses
	 * from responseMap which are not present in qlist
	 * @param responseTO
	 * @param validQuestions
	 */
	public ResponseTO sanitizeQuestionList(ResponseTO responseTO, List<String> validQuestions){
		Map<String, QuestionWiseResponseTO> responseMap = null;
		if(responseTO != null){
			responseMap = responseTO.getResponseMap();
		}
		Iterator<String> questionIterator = null;
		if(responseMap != null){
			questionIterator = responseMap.keySet().iterator();
		}
		if(questionIterator != null && !validQuestions.isEmpty()){
			while(questionIterator.hasNext()){
				String questionId = questionIterator.next();
				if(!validQuestions.contains(questionId)){
					questionIterator.remove();
				}
			}
		}
		
		return responseTO;
	}
	
	/**
	 * This method will return the all the question IDs in a QLIST
	 * @param actualQuestionList
	 * @return List<String> containing all the QUESTIONID 
	 */
	public List<String> getQuestionIdsFromQlist(String actualQuestionList){
		String[] qlist = actualQuestionList.split(",|;");
		List<String> validQuestions = new ArrayList<String>();
		for(int i=0; i<qlist.length; i++){
			validQuestions.add(qlist[i]);
		}
		return validQuestions;
	}
	
	/**
	 * This method is decides whether its a test mode or review mode
	 * @return
	 * @throws Exception
	 */
	public boolean isReviewMode(boolean hasSubmission , String userID , String mode) throws Exception {
		if(hasSubmission || classware_hm.ROLE_INSTRUCTOR_ID.equals(userID)){
			if(CaaConstants.REVIEW.equals(mode)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * This method creates a default URL which is required for exiting from assignment view 
	 * mode when any kind of error occurred 
	 * @param requestMap
	 * @param defaultURL
	 * @return
	 * @throws Exception
	 */
	public String createdefaultUrl(String role, String sectionID , CustomMap<String,String> testParamMap) throws Exception {
		String defaultURL = "";
		String sectionHomeURL = "";
		String cwServer = "";
		if(testParamMap != null && testParamMap.size() > 0 && StringUtils.isNotBlank(sectionID)){
			cwServer = testParamMap.getParam(classware_hm.CW_SERVER);
			_logger.info("CW SERVER ::::: " + cwServer);
			sectionHomeURL = getSectionHomeURL(role);
			defaultURL = new StringBuilder(cwServer).append(sectionHomeURL).append("?").append(classware_hm.SECTION_ID).append("=").append(sectionID).toString();
		}else{
			throw new Exception("TestParam Map or sectionID is null");
		}
		return defaultURL;
	}
	
	/**
	 * This method creates a default URL which is required for exiting from assignment view 
	 * mode when any kind of error occurred 
	 * @param requestMap
	 * @param defaultURL
	 * @return
	 * @throws Exception
	 */
	public String createdefaultUrl(String role, String sectionID , RequestMap<String,String> requestMap) throws Exception {
		String defaultURL = "";
		String sectionHomeURL = "";
		String cwServer = "";
		if(requestMap != null && requestMap.size() > 0 && StringUtils.isNotBlank(sectionID)){
			cwServer = requestMap.get(classware_hm.CW_SERVER);
			_logger.info("CW SERVER ::::: " + cwServer);
			sectionHomeURL = getSectionHomeURL(role);
			defaultURL = new StringBuilder(cwServer).append(sectionHomeURL).append("?").append(classware_hm.SECTION_ID).append("=").append(sectionID).toString();
		}else{
			throw new Exception("TestParam Map or sectionID is null");
		}
		return defaultURL;
	}
	
	/**
	 * This method creates a default URL which is required for exiting from assignment view 
	 * mode when any kind of error occurred 
	 * @param requestMap
	 * @param defaultURL
	 * @return
	 * @throws Exception
	 */
	public String composeReturnURL(String returnURL, String defaultURL) throws Exception {
		try{
			if(StringUtils.isNotBlank(returnURL)){
				if (("null").equals(returnURL) || returnURL.endsWith("null")) {
					returnURL = "";
				}
			}
			if (StringUtils.isNotBlank(returnURL)) {
				return returnURL;
			}
		} catch (Exception ex) {
			throw ex;
		}
		return defaultURL;
	}
	
	/**
	 * This method creates section home URL based on role
	 * @param role
	 * @return
	 * @throws Exception
	 */
	private String getSectionHomeURL(String role) throws Exception {
		if (classware_hm.instructorRole(role)){
			return classware_hm.SECTION_RETURN_URL_INSTRUCTOR;
		}else{
			return classware_hm.SECTION_RETURN_URL_STUDENT;
		}
	}
	
	/**
	 * This method is going to update URL related information each time resuming the attempt
	 * @param requestMap
	 * @param responseTO
	 * @throws Exception
	 */
	public void updateURLInformation(Map<String,String> requestMap , ResponseTO responseTO ) throws Exception {
		CustomMap<String,String> testParammeter = null;
		testParammeter = responseTO.getTestParameter();
		if(testParammeter != null && requestMap != null){
			testParammeter.put(classware_hm.REFERER, requestMap.get(classware_hm.REFERER));
			testParammeter.put(classware_hm.CW_SERVER, requestMap.get(classware_hm.CW_SERVER));
			testParammeter.put(classware_hm.EZTO_INTERNAL, requestMap.get(classware_hm.EZTO_INTERNAL));
			testParammeter.put(classware_hm.EZTO_EXTERNAL, requestMap.get(classware_hm.EZTO_EXTERNAL));
		}
	}
	
	/**
	 * This method updates test parameter to add report specific policies.
	 * 
	 * @param requestMap
	 * @param responseTO
	 * @throws BusinessException
	 * @throws Exception
	 */
	public void updateTestParamforReportView(Map<String, String> requestMap, ResponseTO responseTO) throws BusinessException, Exception {
		ResponseTO alreadySubmittedResponseTO = userResponseService.getSubmittedUserResponse(responseTO);		
		CustomMap<String,String> testParamMap = null;
		if(alreadySubmittedResponseTO != null){
			updateURLInformation(requestMap, alreadySubmittedResponseTO);
			testParamMap = alreadySubmittedResponseTO.getTestParameter();
			String fbParam= requestMap.get(classware_hm.POLICY_grading);
			String acParam= requestMap.get(classware_hm.POLICY_feedback_allcorrect);
			String solParam= requestMap.get(classware_hm.POLICY_solution);

			if (classware_hm.ALL_CORRECT.equals(testParamMap.getParam(classware_hm.ALL_CORRECT)) && "100".equals(solParam)){
				testParamMap.put(CaaConstants.REPORT_POLICY_solution, solParam);
			}else if (StringUtils.isNotBlank(solParam)){
				testParamMap.put(CaaConstants.REPORT_POLICY_solution, solParam);
			}

			if (classware_hm.ALL_CORRECT.equals(testParamMap.getParam(classware_hm.ALL_CORRECT)) && StringUtils.isNotBlank(acParam)){
				testParamMap.put(CaaConstants.REPORT_POLICY_grading, acParam);
			}else if(StringUtils.isNotBlank(fbParam)){
				testParamMap.put(CaaConstants.REPORT_POLICY_grading, fbParam);
			}			
		}else{
			_logger.error("submitted attempt not found for submissionid : "+responseTO.getSubmissionID()+" ,testid : "+responseTO.getTestID());
			throw new BusinessException("submitted attempt not found");
		}
		AttemptDTO attemptDTO = responseUtil.parseStudentTestWiseResponse(alreadySubmittedResponseTO);
		userResponseService.updateAttemptParam(attemptDTO.getTestParameter(), alreadySubmittedResponseTO.getAttemptPK());		
		responseTO.setAttemptPK(alreadySubmittedResponseTO.getAttemptPK());
		responseTO.setTestParameter(testParamMap);
	}
	
	/**
	 * This method is going fetch the sectionBreak question for its part question
	 * @param qList
	 * @param itemID
	 * @return
	 * @throws Exception
	 */
	public question getSectionBreakQuestion(String qList , String itemID) throws Exception,BusinessException {
		question sectionBreakQuestion = null;
		if(StringUtils.isNotBlank(qList) && StringUtils.isNotBlank(itemID)){
			String qids[] = qList.split(";");
			if(qids != null){
				for(int i = 0 ; i < qids.length ; i++){
					String qid = qids[i];
					//Checking whether the QID from question list represents a multiPart questions set or not
					if(StringUtils.isNotBlank(qid) && qid.contains(",")){
						//Checking whether the multiPart questions set contains the given item or not
						if(qid.contains(itemID)){
							String partQids[] = qid.split(",");
							if(partQids != null){
								for(int j = 0 ; j < partQids.length ;j++){
									sectionBreakQuestion = cacheService.getItem(partQids[j]);
									//checking whether the question is a multiPart question or not
									if(sectionBreakQuestion != null && sectionBreakQuestion.type == question.QUESTION_TYPE_sectionbreak && StringUtils.isNotBlank(sectionBreakQuestion.referenceTag)){
										return sectionBreakQuestion;
									}
								}
							}
						}
					}
				}
			}
		}else{
			throw new BusinessException("QLIST or itemID is blank for " + "qList :: " + qList + "and itemID" + itemID);
		}
		return sectionBreakQuestion;
	}
	
	/**
	 * This method is going to generate one-way restrict key for an itemID
	 * @param itemID
	 * @return
	 * @throws Exception
	 */
	public String getOneWayRestrictKey(String itemID) throws Exception {
		String key = "";
		StringBuilder keyBuilder = null;
		if(StringUtils.isNotBlank(itemID)){
			keyBuilder = new StringBuilder("Q_");
			keyBuilder.append(itemID).append("_").append(CaaConstants.ONE_WAY_RESTRICTED);
			key = keyBuilder.toString();
		}
		return key;
	}
	
	/**
	 * This method appends the question id to the completion status one way test parameter list and returns it.
	 * @param testParamMap
	 * @param qid
	 * @return
	 * @throws Exception
	 */
	public String getCompStatusOneWayString(CustomMap<String, String> testParamMap, String qid) throws Exception{
		String compStatusOneWayString = "";
		if(testParamMap != null && StringUtils.isNotBlank(qid)){
			compStatusOneWayString = testParamMap.getParam(classware_hm.COMPLETION_STATUS_ONEWAY);
			if(!compStatusOneWayString.contains(qid)){
				if(StringUtils.isNotBlank(compStatusOneWayString)){
					compStatusOneWayString = compStatusOneWayString + "+";
				}
				compStatusOneWayString = compStatusOneWayString + qid;
			}
		}
		
		return compStatusOneWayString;
	}
	
	/**
	 * This method is going to generate a normalized question list based on the role of the user and type of policies.
	 * If role is "instructor" but ID is not "instructorPreviewID" then the question list pattern is changed to the basic
	 * sequence as an INSTRUCTOR always sees the students' response in a normalized way.It also populates it into the 
	 * testParameter.  
	 * @param testTO
	 * @param role
	 * @param defaultQList
	 */
	public void populateQList(TestTO testTO , UserResponseWithPolicyTO userResponseWithPolicyTO , String role) throws Exception{
		String normalizedQlist = "";
		String defaultQList = "";
		ResponseTO responseTO = null;
		PolicyTO policyTO = null;
		Map<String,String> policyMap = null;
		if(userResponseWithPolicyTO != null){
			responseTO = userResponseWithPolicyTO.getResponseTO();
			policyTO = userResponseWithPolicyTO.getPolicyTO();
			if(policyTO != null){
				policyMap = policyTO.getPolicyMap();
			}
			if(responseTO != null && testTO != null){
				defaultQList = responseTO.getParam(CaaConstants.QLIST);
				//Checks whether the ROLE is instructor but the response is for student
				if(testUtil.instructorRole(role) && !classware_hm.ROLE_INSTRUCTOR_ID.equals(responseTO.getUserID())){
					//Checks whether the scrambling policy is set or the assignment is pooled
					if((policyMap != null && CaaConstants.YES.equals(policyMap.get(classware_hm.POLICY_scrambling))) || tagUtil.isPooled(testTO.getTaggingList())){
						//Get the normalized question list
						normalizedQlist = getNormalizedQlist(testTO, defaultQList);
						if(StringUtils.isNotBlank(normalizedQlist)){
							//Update testParameter with the modified QLIST
							responseTO.getTestParameter().replaceParam(CaaConstants.QLIST, normalizedQlist);
						}
					}
				}
			}
		}
	}
	
	/**
	 * This method normalizes the question list based one original sequence number in which
	 * questions are created.It regenerates the question list based on the old question list
	 * but changes its order.
	 * @param testTO
	 * @param oldQlist
	 * @return
	 * @throws Exception
	 */
	public String getNormalizedQlist(TestTO testTO , String oldQlist) throws Exception {
		StringBuilder normalizedQListBuilder = new StringBuilder();
		List<QuestionMetaDataTO> questionMetaDataTOList = null;
		if(StringUtils.isNotBlank(oldQlist) && testTO != null){
			questionMetaDataTOList = testTO.getQuestionMetaDataList();
			//Get the old question list
			List<String> oldQuestionList = new ArrayList<String>(Arrays.asList(oldQlist.split(";")));
			if(oldQuestionList != null){
				//Iterating over the sequenced question object list to get the question id
				if(questionMetaDataTOList != null){
					for(QuestionMetaDataTO questionMetaDataTO : questionMetaDataTOList){
						String qid = questionMetaDataTO.getQuestionID();
						Iterator<String> itr = oldQuestionList.iterator();
						while(itr.hasNext()){
							String listedQid = itr.next();
							//Checks whether the old question list contains the question id or not
							if(StringUtils.isNotBlank(listedQid) && listedQid.contains(qid)){
								if(normalizedQListBuilder.length() == 0){
									normalizedQListBuilder.append(listedQid);
								}else{
									normalizedQListBuilder.append(";").append(listedQid);
								}
								//If question is found then deletes it from old question list
								itr.remove();
								break;
							}
						}
					}
				}
			}
		}
		return normalizedQListBuilder.toString();
	}
	
	/**
	 * This method is going to populate LTI related informations like LTI name & consumer id
	 * @param featureName
	 * @return
	 * @throws Exception
	 */
	public Map<String,String> getLtiInfo(String featureName) throws Exception {
		Map<String,String> ltiMap = new HashMap<String,String>();
		String name = "";
		String consumer_id = "";
		if(StringUtils.isNotBlank(featureName)){
			String ltiInfo[] = featureName.split("\\|");
			if(ltiInfo != null && ltiInfo.length == 2){
				name = ltiInfo[0];
				consumer_id = ltiInfo[1];
				ltiMap.put(CaaConstants.NAME, name);
				ltiMap.put(CaaConstants.CONSUMER_ID, consumer_id);
			}
		}
		return ltiMap;
	}
	
	/**
	 * This method is going to return true or false based on the policies and feature
	 * @param responseTO
	 * @param testTO
	 * @return
	 * @throws Exception
	 */
	public boolean isTestMobileSafe(ResponseTO responseTO, TestTO testTO, JSONObject jsonObject) throws Exception{
		boolean mobileSafe = true;
		if(!licenseService.isDebuggerPresent("disableMobileSafe")){
			CustomMap<String, String> testParamMap = (CustomMap<String, String>)responseTO.getTestParameter();
			String policyFeedback = testParamMap.getParam(classware_hm.POLICY_feedback);
			/**
			 * Added/Modified code for EZAPI-24 (set mobileSafe for true for assignment containing video)
			 * 
			 * If application is getting mobile safe from test Object, which is cached then it could lead
			 * to a bug.
			 * 
			 * Instead of getting mobile safe parameter from test object, it is suggested to calculate 
			 * the same at the time of start Assignment every time.
			 */
			//mobileSafe = testTO.isMobileReady();
			mobileSafe = testService.validateMobileReady(testTO);
			if(mobileSafe){
				if(CaaConstants.YES.equals(policyFeedback)){
					mobileSafe = false;
					jsonObject.put(CaaConstants.MOBILE_UNSAFE_REASON, CaaConstants.ASSIGNMENT_POLICY_ALLOW_FEEDBACK);
				}
			}else{
				jsonObject.put(CaaConstants.MOBILE_UNSAFE_REASON, testTO.getMobileUnSafeReason());
			}
			
			if(mobileSafe && !licenseService.isFeaturePresent("enableBopdAssignmentInMobile")){
				mobileSafe = disableMobileSafeBuildOnPreviousAttempt(testParamMap);
				if(!mobileSafe){
					jsonObject.put(CaaConstants.MOBILE_UNSAFE_REASON, CaaConstants.ASSIGNMENT_POLICY_BOP);
				}
			}
			
			if(mobileSafe && !licenseService.isFeaturePresent("enableTimedAssignmentInMobile")){
				String policyTimeLimit = testParamMap.getParam(classware_hm.POLICY_limit);

				_logger.info("policyTimeLimit ####################" + policyTimeLimit);
				
				if(StringUtils.isNotBlank(policyTimeLimit)){
					try{
						int timeLimit = Integer.parseInt(policyTimeLimit);
						if(timeLimit>0){
							mobileSafe = false;
							jsonObject.put(CaaConstants.MOBILE_UNSAFE_REASON, CaaConstants.ASSIGNMENT_POLICY_TIME_LIMIT);
						}
					}catch(NumberFormatException ne){
						_logger.error("### Invalid Number policyTimeLimit : "+policyTimeLimit);
					}
				}
			}
		}
		return mobileSafe;
	}
	
	/**
	 * This method is going to return true or false based on the policies and feature
	 * @param responseTO
	 * @param testTO
	 * @return
	 * @throws Exception
	 */
	public boolean isTestMobileSafe(Map<String, String> requestMap, TestTO testTO, JSONObject jsonObject) throws Exception{
		boolean mobileSafe = true;
		if(!licenseService.isDebuggerPresent("disableMobileSafe") && requestMap != null){
			String policyFeedback = requestMap.get(classware_hm.POLICY_feedback);
			/**
			 * Added/Modified code for EZAPI-24 (set mobileSafe for true for assignment containing video)
			 * 
			 * If application is getting mobile safe from test Object, which is cached then it could lead
			 * to a bug.
			 * 
			 * Instead of getting mobile safe parameter from test object, it is suggested to calculate 
			 * the same at the time of start Assignment every time.
			 */
			//mobileSafe = testTO.isMobileReady();
			mobileSafe = testService.validateMobileReady(testTO);
			if(mobileSafe){
				if(CaaConstants.YES.equals(policyFeedback)){
					mobileSafe = false;
					jsonObject.put(CaaConstants.MOBILE_UNSAFE_REASON, CaaConstants.ASSIGNMENT_POLICY_ALLOW_FEEDBACK);
				}
			}else{
				jsonObject.put(CaaConstants.MOBILE_UNSAFE_REASON, testTO.getMobileUnSafeReason());
			}
			
			if(mobileSafe && !licenseService.isFeaturePresent("enableBopdAssignmentInMobile")){
				String submissionID = requestMap.get(classware_hm.SUBMISSION_ID);
				String genuisMode = requestMap.get(classware_hm.POLICY_genius); 
				if(StringUtils.isNotBlank(submissionID)  && !CaaConstants.YES.equals(genuisMode)){
					mobileSafe = false;
					jsonObject.put(CaaConstants.MOBILE_UNSAFE_REASON, CaaConstants.ASSIGNMENT_POLICY_BOP);
				}
			}
			
			if(mobileSafe && !licenseService.isFeaturePresent("enableTimedAssignmentInMobile")){
				String policyTimeLimit = requestMap.get(classware_hm.POLICY_limit);
				String policyHardTimeLimit = requestMap.get(classware_hm.POLICY_hardlimit);

				_logger.info("#################### policyTimeLimit : " + policyTimeLimit+" , policyHardTimeLimit : "+policyHardTimeLimit);
				
				if(isNonZeroNumeric(policyTimeLimit)){
					mobileSafe = false;
					jsonObject.put(CaaConstants.MOBILE_UNSAFE_REASON, CaaConstants.ASSIGNMENT_POLICY_TIME_LIMIT);
				}
				if(mobileSafe && isNonZeroNumeric(policyHardTimeLimit)){
					mobileSafe = false;
					jsonObject.put(CaaConstants.MOBILE_UNSAFE_REASON, CaaConstants.ASSIGNMENT_APPROACHING_DUE_DATE);
				}
			}
		}
		return mobileSafe;
	}
	
	/**
	 * verifies whether a particular value is integer or not.
	 * @param value
	 * @return
	 */
	private boolean isNonZeroNumeric(String value){
		boolean result = false;
		if(StringUtils.isNotBlank(value)){
			try{
				int intValue = Integer.parseInt(value);
				if(intValue > 0){
					result = true;
				}
			}catch(NumberFormatException ne){
				_logger.error("### Invalid Number isNonZeroNumeric : " + value);
			}
		}
		
		return result;
	}
	
	/**
	 * This method turns an assignment attempt mobile unsafe 
	 * if the attempt is a build on previous one  
	 * @param CustomMap<String, String>
	 * @return boolean 
	 */
	private boolean disableMobileSafeBuildOnPreviousAttempt(CustomMap<String, String> testParamMap){
		boolean mobileSafe = true;
		String prevAttemptSubmissionId = testParamMap.getParam(classware_hm.FORMER_SUBMISSION);
		String bop = testParamMap.getParam(classware_hm.BUILD_ON_PREVIOUS);
		String genuisMode = testParamMap.getParam(classware_hm.POLICY_genius); 
		_logger.info("Previous attempt sid : " + prevAttemptSubmissionId + " , " + classware_hm.BUILD_ON_PREVIOUS + " : " + bop + " , " + classware_hm.POLICY_genius + " : " + genuisMode);
		if((StringUtils.isNotBlank(prevAttemptSubmissionId) || CaaConstants.TRUE.equals(bop)) && !CaaConstants.YES.equals(genuisMode)){
			mobileSafe = false;
		}
		
		return mobileSafe;
	}
	
	/**
	 * This method is going to set all the policy information into the Instruction JSON by populating them
	 * from the database.
	 * @param responseTO
	 * @param instructionJSON
	 * @throws Exception
	 */
	public JSONObject setPoliciesIntoInstructions(ResponseTO responseTO , JSONObject instructionJSON , Map<String,String> policyMap , TestTO testTO) throws Exception {
		JSONObject activityJSON = null;
		if(responseTO != null && policyMap != null){
			if(instructionJSON == null){
				instructionJSON = new JSONObject();
			}
			if(!instructionJSON.has(CaaConstants.ACTIVITY_INFO)){
				instructionJSON.put(CaaConstants.ACTIVITY_INFO, new JSONObject());
			}
			activityJSON = instructionJSON.getJSONObject(CaaConstants.ACTIVITY_INFO);
			//Populating deduction information into instruction JSON
			popultaeDeductions(responseTO, policyMap, activityJSON);
			//Populating the attemptInfo into the instruction JSON
			populateAttemptInfoJSON(responseTO, policyMap, activityJSON , testTO);
			//Populating instructor information into the instruction JSON
			populateInstructorInfo(responseTO, activityJSON);
			instructionJSON.put(CaaConstants.ACTIVITY_INFO, activityJSON);
		}
		return instructionJSON;
	}
	
	/**
	 * This method is responsible for generating the attempt information JSON from the
	 * attempt level information. 
	 * @param responseTO
	 * @return
	 * @throws Exception
	 */
	public void populateAttemptInfoJSON(ResponseTO responseTO , Map<String,String> policyMap , JSONObject activityJSON , TestTO testTO) throws Exception {
		JSONObject attemptInfoJSON = null;
		if(!activityJSON.has(CaaConstants.ATTEMPT_INFO)){
			attemptInfoJSON = new JSONObject();
			activityJSON.put(CaaConstants.ATTEMPT_INFO, attemptInfoJSON);
		}
		attemptInfoJSON = activityJSON.getJSONObject(CaaConstants.ATTEMPT_INFO);
		if(policyMap != null){
			//Populating deduct attempt policy value
			attemptInfoJSON.put(CaaConstants.LATE_PENALTY, policyMap.get(classware_hm.POLICY_deduct_attempts));
			//populating feed back between question policy
			if(CaaConstants.YES.equals(policyMap.get(classware_hm.POLICY_feedback))){
				attemptInfoJSON.put(CaaConstants.FEED_BACK_BETWEENQ , CaaConstants.TRUE);
			}else{
				attemptInfoJSON.put(CaaConstants.FEED_BACK_BETWEENQ , CaaConstants.FALSE);
			}
			//Populating genius mode
			if(CaaConstants.YES.equals(policyMap.get(classware_hm.POLICY_genius))){
				attemptInfoJSON.put(CaaConstants.STUDY_MODE, CaaConstants.TRUE);
			}else{
				attemptInfoJSON.put(CaaConstants.STUDY_MODE, CaaConstants.FALSE);
			}
			String dueDate = null;
			//populating due date
			if(attemptInfoJSON.has(CaaConstants.DUE_DATE)){
				dueDate = attemptInfoJSON.getString(CaaConstants.DUE_DATE);
				if(StringUtils.isBlank(dueDate)){
					dueDate = getDueDate(policyMap);
					attemptInfoJSON.put(CaaConstants.DUE_DATE, dueDate);
				}
			}else{
				dueDate = getDueDate(policyMap);
				attemptInfoJSON.put(CaaConstants.DUE_DATE, dueDate);
			}
			//populating actual attempt number
			String attemptMax = policyMap.get(classware_hm.POLICY_attempts);
			int maxAttempt = 0;
			try{
				maxAttempt = Integer.parseInt(attemptMax);
				if(maxAttempt < 0){
					attemptMax = CaaConstants.UNLIMITED;
				}
			}catch(NumberFormatException ne){
				_logger.error("attemptMax is not a number :: " + attemptMax);
			}
			attemptInfoJSON.put(CaaConstants.SCORED_ATTEMPT_MAX, attemptMax);
			//populating file attachment info
			//attemptInfoJSON.put(CaaConstants.FILE_ATTACHMENTS, policyMap.get(classware_hm.POLICY_attachments));
			
			//Populating auto submitting informations
			if(CaaConstants.YES.equals(policyMap.get(classware_hm.POLICY_autoSubmit))){
				attemptInfoJSON.put(CaaConstants.AUTO_SUBMIT, CaaConstants.TRUE);
			}else{
				attemptInfoJSON.put(CaaConstants.AUTO_SUBMIT, CaaConstants.FALSE);
			}
			
		}
		if(responseTO != null){
			/*if(CaaConstants.TRUE.equals(responseTO.getParam(classware_hm.BUILD_ON_PREVIOUS)) || StringUtils.isNotBlank(responseTO.getParam(classware_hm.FORMER_SUBMISSION))){
				if(!CaaConstants.YES.equals(responseTO.getParam(classware_hm.POLICY_genius))){
					attemptInfoJSON.put(CaaConstants.BUILD_ON_PREVIOUS , CaaConstants.TRUE);
				}
			}*/
			attemptInfoJSON.put(CaaConstants.BUILD_ON_PREVIOUS , "");
			//populating actual attempt number
			attemptInfoJSON.put(CaaConstants.ACTUAL_ATTEMPT_NO, responseTO.getAttemptNo());
			//populating display attempt no
			attemptInfoJSON.put(CaaConstants.DISPLAY_ATTEMPT_NO, responseTO.getAttemptNo());
			//Populating points
			if(testTO != null){
				attemptInfoJSON.put(CaaConstants.POINTS, getTotalPoints(testTO.getQuestionMetaDataList(), responseTO));
			}
		}
	}
	
	/**
	 * This method is going to populate the instructor information into the JSON
	 * @param responseTO
	 * @param activityJSON
	 * @throws Exception
	 */
	public void populateInstructorInfo(ResponseTO responseTO , JSONObject activityJSON) throws Exception {
		JSONObject instructorJSON = null;
		String instructorName = "";
		if(responseTO != null){
			if(!activityJSON.has(CaaConstants.INSTRUCTOR_INFO)){
				instructorJSON = new JSONObject();
				activityJSON.put(CaaConstants.INSTRUCTOR_INFO, instructorJSON);
			}
			instructorJSON = activityJSON.getJSONObject(CaaConstants.INSTRUCTOR_INFO);
			//Populating instructor name
			if(instructorJSON.has(CaaConstants.INSTRUCTOR_NAME)){
				instructorName = instructorJSON.getString(CaaConstants.INSTRUCTOR_NAME);
				if(StringUtils.isBlank(instructorName)){
					instructorName = tp_utils.convertFromUTF8(responseTO.getParam(classware_hm.AP_INSTRUCTOR_NAME));
					instructorJSON.put(CaaConstants.INSTRUCTOR_NAME, responseTO.getParam(classware_hm.AP_INSTRUCTOR_NAME));
				}
			}else{
				instructorName = tp_utils.convertFromUTF8(responseTO.getParam(classware_hm.AP_INSTRUCTOR_NAME));
				instructorJSON.put(CaaConstants.INSTRUCTOR_NAME, responseTO.getParam(classware_hm.AP_INSTRUCTOR_NAME));
			}
			//populating instructor instructions
			instructorJSON.put(CaaConstants.INSTRUCTOR_INSTRUCTIONS,responseTO.getParam((classware_hm.HEADER_INSTRUCTIONS)));
		}
	}
	
	/**
	 * This method is going to set all the deduction information into the Instruction JSON by populating them
	 * from the database.
	 * @param responseTO
	 * @param instructionJSON
	 * @throws Exception
	 */
	public void popultaeDeductions(ResponseTO responseTO , Map<String,String> policyMap , JSONObject activityJSON) throws Exception {
		JSONObject deductionJSON = null;
		if(!activityJSON.has(CaaConstants.DEDUCTIONS)){
			deductionJSON = new JSONObject();
			activityJSON.put(CaaConstants.DEDUCTIONS, deductionJSON);
		} 
		deductionJSON = activityJSON.getJSONObject(CaaConstants.DEDUCTIONS);
		deductionJSON.put(CaaConstants.LATE_SUBMISSION, policyMap.get(classware_hm.POLICY_deduct_late));
		deductionJSON.put(CaaConstants.LATE_SUBMISSION_INTERVAL, policyMap.get(classware_hm.POLICY_deduct_late_increment));
		deductionJSON.put(CaaConstants.HINT_DEDUCTION, policyMap.get(classware_hm.POLICY_deduct_hints));
		deductionJSON.put(CaaConstants.EBOOK_DEDUCTION, policyMap.get(classware_hm.POLICY_deduct_resources));
		deductionJSON.put(CaaConstants.CHECK_WORK_DEDUCTION, policyMap.get(classware_hm.POLICY_deduct_checkwork));
		deductionJSON.put(CaaConstants.CHECK_WORK_LIMIT, policyMap.get(classware_hm.POLICY_checkwork_limit));
	}
	/**
	 * This method is going to return the remaining time of the assignment based on the 
	 * stored P_TIMELIMIT in the response for a particular user
	 * @param responseTO
	 * @return
	 */
	public long getRemainingTime(ResponseTO responseTO) throws Exception {
		String secondsLimitString = "";
		if(responseTO != null){
			secondsLimitString = responseTO.getParam(classware_hm.POLICY_limit);
			if(StringUtils.isNotBlank(secondsLimitString)){
				try{
					long secondsLimit = Long.parseLong(secondsLimitString);
					long startTime = Long.parseLong(responseTO.getParam(test.TIMESTAMP)) / 1000;
					long now = (new Date()).getTime() / 1000;
					long remainingTime = (Math.round((startTime + secondsLimit) - now));
					if(remainingTime <= 0){
						return 0;
					}
					return remainingTime;
				}catch (NumberFormatException ignore) {
					_logger.warn("Not able to calculate time remaining from time limit ( " + secondsLimitString + " ) for attemptPK :: " + responseTO.getAttemptPK());
				}
			}
		}
		return CaaConstants.NO_TIMER;
	}
	
	/**
	 * This method populates the test title in testParam.
	 * if the test title is not present in testParam, then it checks
	 * the TestTO, get the title & re-populate it in  testParam
	 * 
	 * @param ResponseTO
	 * @param TestTO
	 */
	public void populateTestTitle(ResponseTO responseTO, TestTO testTO){
		String testTitle = null;
		CustomMap<String, String> testParamMap = null;
		if(responseTO != null){
			testParamMap = (CustomMap<String, String>)responseTO.getTestParameter();
			if(testParamMap != null){
				testTitle = testParamMap.getParam(classware_hm.HEADER_ASSIGNMENT_TITLE);
			}
		
			if(StringUtils.isBlank(testTitle)){
				if(testTO != null){
					testTitle = testTO.getTitle();
				}
				
				if(StringUtils.isNotBlank(testTitle)){
					testParamMap.replaceParam(classware_hm.HEADER_ASSIGNMENT_TITLE, testTitle);
				}
			}	
		}
	}
	
	/**
	 * This method is going to calculate the due date based on policy due date
	 * @param policyMap
	 * @return
	 * @throws Exception
	 */
	public String getDueDate(Map<String,String> policyMap) throws Exception {
		String dueDate = null;
		if(policyMap != null){
			dueDate = policyMap.get(classware_hm.POLICY_duedate);
			if(StringUtils.isNotBlank(dueDate)){
				try{
					DateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					TimeZone usEasternTimeZone = TimeZone.getTimeZone("EST5EDT");
					fromFormat.setTimeZone(usEasternTimeZone);
					DateFormat toFormat = new SimpleDateFormat("MM/dd/yyyy 'at' hh:mm a");
					toFormat.setTimeZone(usEasternTimeZone);
					
					Date date = fromFormat.parse(dueDate);
					dueDate = toFormat.format(date);
				}catch(Exception ex){
					_logger.error("Error while formatting due date : " + dueDate + " from format yyyy-MM-dd HH:mm:ss to MM/dd/yyyy 'at' hh:mm a");
				}
				
				return dueDate;
			}
			/*String endTime = policyMap.get(classware_hm.POLICY_endtime);
			if(StringUtils.isNotBlank(endTime)){
				long endTimeMillis = 0l;
				try{
					endTimeMillis = Long.parseLong(endTime) * 1000;
				}catch(Exception ne){
					endTimeMillis = 0l;
					_logger.error("Exception while convertig the endtime " + endTime , ne);
				}
				if(endTimeMillis > 0){
					SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy 'at' HH:mm a");
					TimeZone firstTime = TimeZone.getTimeZone("EST5EDT"); 
					Calendar calendar = Calendar.getInstance();
					calendar.setTimeInMillis(endTimeMillis);
					sdf.setTimeZone(firstTime);
					dueDate = sdf.format(calendar.getTime());
				}
			}*/
		}
		return dueDate;
	}
	
	/**
	 * This method is going return the maximum points that can be earned by a student for an assignment.
	 * This process includes skipping the dropped credited question and those question which are beyond the
	 * scope of the students.Besides this it also excludes section break or survey type question as this type of
	 * question does not hold any point 
	 * @param questions
	 * @param responseTO
	 * @return
	 * @throws Exception
	 */
	public String getTotalPoints(List<QuestionMetaDataTO> questions , ResponseTO responseTO) throws Exception {
		String showTotal = "";
		List<String> droppedCreditedItems = null;
		//Fetching dropped credited items
		Map<String,List<String>> flaggedItems = itemService.getFlaggedItems(responseTO.getTestID());
		if(flaggedItems != null && flaggedItems.size() > 0){
			droppedCreditedItems = flaggedItems.get("dropped");
		}
		BigDecimal totalScore = new BigDecimal("0.00");
		final String defaultPoint = "10";
		String qListStr = responseTO.getParam(CaaConstants.QLIST);
		List<String> qList = Arrays.asList(qListStr.split(",|;"));
		if(StringUtils.isNotBlank(qListStr)){
			for(QuestionMetaDataTO questionMetaDataTO : questions){
				//Skipping questions that are out of scope of the user
				if(!qList.contains(questionMetaDataTO.getQuestionID())){
					continue;
				}
				//Skipping dropped credited items for counting total points
				if (droppedCreditedItems != null && droppedCreditedItems.contains(questionMetaDataTO.getQuestionID())){
					continue;
				}
				//Skipping section break and survey type question for counting total points
				if((questionMetaDataTO.getQuestionType() != question.QUESTION_TYPE_sectionbreak) && (questionMetaDataTO.getQuestionType() != question.QUESTION_TYPE_survey)){
					String point = defaultPoint;
					if(StringUtils.isNotBlank(questionMetaDataTO.getPoints())){
						point = questionMetaDataTO.getPoints();
					}
					BigDecimal qPts = new BigDecimal(point).setScale(2, BigDecimal.ROUND_HALF_UP);
					BigDecimal qMax = new BigDecimal(Integer.toString(classware_hm.DEFAULT_INTERNAL_POINTS) + ".00");
					qMax = qMax.multiply(qPts);
					totalScore = totalScore.add(qMax);
				}
			}
			BigDecimal displayTotal = totalScore.divide(new BigDecimal(Integer.toString(classware_hm.DEFAULT_INTERNAL_POINTS)), 2, BigDecimal.ROUND_HALF_UP);
			showTotal = tp_utils.substitute(displayTotal.toString(), ".00", "");
		}
		return showTotal;
	}
	
	/**
	 * This method is going to find special character like '>' or '<' and replace them with proper HTML tag like
	 * '&gt' or '&lt'.
	 * @param str
	 * @return
	 * @throws Exception
	 */
	public String replaceWithHTMLTag(String str) throws Exception {
		if(StringUtils.isNotBlank(str)){
			str = str.replace("<", "&lt;");
			str = str.replace(">", "&gt;");
		}
		return str;
	}
	
	/**
	 * This method is designed to perform the below enlisted task<br>
	 *	 1> store a new flag in test param to identify the start time of an item visit<br>
	 *	 2> calculate elapsed time of an item and add that in attempt data incrementally.<br>
	 *	 3> in case of Save and Exit,<br>
	 *		a> modify the API to read an extra parameter<br>
	 *		b> Update item and assignment level elapsed time<br>
	 *		c> clear the newly added flag in test param.   
	 * @param activityTO A custom transfer object, containing current itemId, saveExit flag etc
	 * @param responseTO A Custom transfer object, representing user response
	 * @return A <code>QuestionWiseResponseTO</code> object 
	 * @throws Exception
	 */
	public QuestionWiseResponseTO calculateElapsedTime(SetActivityTO activityTO, ResponseTO responseTO) throws Exception{
		/**
		 * validate activityTO, responseTO and ItemId parameters
		 */
		if(activityTO == null || responseTO == null){
			throw new Exception("activityTO or responseTO is coming as Null");
		}
		if(StringUtils.isBlank(activityTO.getItemID())){
			throw new Exception("ItemId is coming as Blank");
		}
		QuestionWiseResponseTO prevQuestionWiseResponseTO = null;
		String prevItemId = null;
		long prevItemStartTime = 0l;
		String currentItemId = activityTO.getItemID();
		long currentTime = System.currentTimeMillis();
		CustomMap<String, String> testParamMap = responseTO.getTestParameter();
		
		if(testParamMap != null && !testParamMap.isEmpty()){
			/**
			 * first check whether test parameter contains previous itemid and startTime 
			 */
			prevItemId = testParamMap.getParam(CaaConstants.ITEMID);
			String prevItemStartTimeStr = testParamMap.getParam(CaaConstants.PAAM_ITEM_STARTTIME);
			if(StringUtils.isNotBlank(prevItemStartTimeStr)){
				prevItemStartTime = Long.parseLong(prevItemStartTimeStr);
			}
			/**
			 * if there is no record for previous itemid and startTime in test parameter then
			 * we assume that this is the first question where user land in test taking mode
			 * Here, we do not calculate elapsed time, we only record current item id and 
			 * start time of the current item visit 
			 * 
			 * 
			 * if there are records for previous item id and startTime for previous item id
			 * then calculate the elapsed time, which is current time - previous item start time
			 * update attempt and attempt data elapsed time incrementally in memory 
			 * 
			 */
			if(StringUtils.isNotBlank(prevItemId) && prevItemStartTime != 0l){
				long elapsedTime = (currentTime - prevItemStartTime);
				//_logger.info(" ###################### prevItemId : "+prevItemId+", prevItemStartTime : "+prevItemStartTime+", elapsedTime : "+elapsedTime);
				prevQuestionWiseResponseTO = responseTO.getResponseMap().get(prevItemId);
				if(prevQuestionWiseResponseTO != null){
					long prevItemElapsedTime = prevQuestionWiseResponseTO.getElapsed();
					long currItemElapsedTime = userResponseServiceImplHelper.getMaxTimeOnTaskLimit(prevItemElapsedTime, elapsedTime);
					prevQuestionWiseResponseTO.setElapsed(currItemElapsedTime);
				}else{
					throw new Exception("QuestionWiseResponseTO is Coming as Null attempPK : "+responseTO.getAttemptPK()+", questionid : "+prevItemId);
				}
			}
			/**
			 * Record current item id and startTime for current item in test parameter
			 */
			testParamMap.replaceParam(CaaConstants.ITEMID, currentItemId);
			testParamMap.replaceParam(CaaConstants.PAAM_ITEM_STARTTIME, String.valueOf(currentTime));
			/**
			 * for saveAndExit, we need to remove the start time record from test parameter
			 */
			if(activityTO.isSaveExit()){
				testParamMap.remove(CaaConstants.PAAM_ITEM_STARTTIME);
			}
		}else{
			throw new Exception("testParameter is Coming as Null or Empty for attempPK : "+responseTO.getAttemptPK());
		}
		return prevQuestionWiseResponseTO;
	}
	
	/**
	 * This method is designed to provide a map object containing section break and its part 
	 * question from the given qlist and item id
	 * 
	 * @param qList A String Object representing the question set in an attempt
	 * @param itemID A String Object representing a question uniquely
	 * @return A <code>Map</code> Object representing sectionBreal and part question associated with the given item id
	 * @throws Exception
	 */
	public Map<String,List<String>> getMultiPartQuestionMap(String qList , String itemID) throws Exception {
		Map<String,List<String>> multiPartMap = null;
		List<String> sbList = null;
		List<String> partQList = null;
		if(StringUtils.isNotBlank(qList)){
			if(StringUtils.isBlank(itemID)){
				return multiPartMap;
			}
			String qids[] = qList.split(";");
			if(qids != null && qids.length > 0){
				for(int i = 0 ; i < qids.length ; i++){
					String qid = qids[i];
					/**
					 * Checking whether the QID from question list represents a multiPart questions set or not 
					 * and the given item is a part of the multi-part or not
					 */
					if(StringUtils.isNotBlank(qid) && qid.contains(",") && qid.contains(itemID)){
						multiPartMap = new HashMap<String,List<String>>();
						sbList = new ArrayList<String>();
						partQList = new ArrayList<String>();
						/**
						 * Checking whether the multiPart questions set contains the given item or not 
						 */
						String multiPartQids[] = qid.split(",");
						if(multiPartQids != null && multiPartQids.length > 0){
							for(int j = 0 ; j < multiPartQids.length ;j++){
								String multipartQid = multiPartQids[j];
								if(StringUtils.isNotBlank(multipartQid)){
									question sectionBreakQuestion = cacheService.getItem(multipartQid);
									/**
									 * checking whether the question is a multiPart question or not 
									 */
									if(sectionBreakQuestion != null && sectionBreakQuestion.type == question.QUESTION_TYPE_sectionbreak && StringUtils.isNotBlank(sectionBreakQuestion.referenceTag)){
										sbList.add(sectionBreakQuestion.sqlID);
									}else{
										if(!partQList.contains(multipartQid)){
											partQList.add(multipartQid);
										}
									}
								}	
							}
							if(!sbList.isEmpty()){
								multiPartMap.put("SB", sbList);
							}
							if(!partQList.isEmpty()){
								multiPartMap.put("part", partQList);
							}
						}
					}
				}
			}
		}else{
			throw new Exception("QLIST or itemID is blank for " + "qList :: " + qList + " and itemID :" + itemID);
		}
		return multiPartMap;
	}
	
	/**
	 * This method returns part questions of a given section break question
	 * @param responseTO A custom Object representing the test level user response
	 * @param prevItemId A String Object representing an unique question
	 * @return A <code>List</code> of String Object, containing part question ids
	 * @throws Exception
	 */
	public List<String> getPartQuestiobIds(ResponseTO responseTO, String prevItemId) throws Exception{
		List<String> partQList =  null;
		Map<String,List<String>> mulitpartMap = null;
		if(responseTO != null && responseTO.getTestParameter() != null && !responseTO.getTestParameter().isEmpty()){
			String qlist = responseTO.getTestParameter().getParam(CaaConstants.QLIST);
			if(StringUtils.isNotBlank(qlist)){
				mulitpartMap = getMultiPartQuestionMap(qlist, prevItemId);
			}
			/**
			 * If mulitpartMap is not null then it can be assumed that the given prevItemId is 
			 * a section break id and then get its part questions
			 * 
			 * it is better to double check the section break question matches with
			 * the given prevItemId
			 */
			if(mulitpartMap != null && !mulitpartMap.isEmpty()){
				List<String> sbList = mulitpartMap.get("SB");
				if(sbList != null && sbList.contains(prevItemId)){
					partQList = mulitpartMap.get("part");
					return partQList;
				}
			}
		}
		return partQList;
	}
	
	/**
	 * This method is designed to calculate elapsed time for part questions.
	 * it takes elapsed of the section break question as a parameter and then devides the
	 * same among all the related part questions which has been visited by the user
	 * @param partQWiseResponseTOList A custom Object representing part Question wise user response list 
	 * @param sbElapsedTime A long value representing elapsed time for section break question
	 * @return A <code>List</code> of <code>QuestionWiseResponseTO</code> object 
	 * @throws Exception
	 */
	public List<QuestionWiseResponseTO> calculateElapsedTimeForPartQs(List<QuestionWiseResponseTO> partQWiseResponseTOList, long sbElapsedTime) throws Exception{
		List<QuestionWiseResponseTO> updatePartQWiseResponseTOList = new ArrayList<QuestionWiseResponseTO>();
		long totalElapsedTimeForPartQ = sbElapsedTime;
		/**
		 * find out the part question list which has been visited by the user
		 */
		for(QuestionWiseResponseTO partQWiseResponseTO : partQWiseResponseTOList){
			if(partQWiseResponseTO != null && partQWiseResponseTO.getQuestionParameters() != null && !partQWiseResponseTO.getQuestionParameters().isEmpty()){
				String visited = partQWiseResponseTO.getQuestionParameters().getParam(CaaConstants.VISITED);
				if("true".equals(visited)){
					updatePartQWiseResponseTOList.add(partQWiseResponseTO);
				}
			}
		}
		/**
		 * devide the section break elapsed time among the visited part Questions
		 */
		if(!updatePartQWiseResponseTOList.isEmpty() && sbElapsedTime != 0l){
			int noOfPartQuestion = updatePartQWiseResponseTOList.size();
			totalElapsedTimeForPartQ = sbElapsedTime/noOfPartQuestion;
			for(QuestionWiseResponseTO partQWiseResponseTO : updatePartQWiseResponseTOList){
				partQWiseResponseTO.setElapsed(totalElapsedTimeForPartQ);
			}
		}
		return updatePartQWiseResponseTOList;
	}
}
