package com.mcgrawhill.ezto.api.caa.services.impl;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import static com.mcgrawhill.ezto.api.util.DateUtil.getFormattedDate;

import com.mcgrawhill.ezto.admin.licenseManager;
import com.mcgrawhill.ezto.api.caa.caaconstants.CaaConstants;
import com.mcgrawhill.ezto.api.caa.dao.UserResponseDAO;
import com.mcgrawhill.ezto.api.caa.dao.datatransferobject.AttemptDTO;
import com.mcgrawhill.ezto.api.caa.dao.datatransferobject.AttemptDataDTO;
import com.mcgrawhill.ezto.api.caa.services.AssignmentService;
import com.mcgrawhill.ezto.api.caa.services.CacheService;
import com.mcgrawhill.ezto.api.caa.services.ItemService;
import com.mcgrawhill.ezto.api.caa.services.PolicyService;
import com.mcgrawhill.ezto.api.caa.services.ScoringService;
import com.mcgrawhill.ezto.api.caa.services.SecurityService;
import com.mcgrawhill.ezto.api.caa.services.UserResponseService;
import com.mcgrawhill.ezto.api.caa.services.domain.Student;
import com.mcgrawhill.ezto.api.caa.services.domain.User;
import com.mcgrawhill.ezto.api.caa.services.domain.UserFactory;
import com.mcgrawhill.ezto.api.caa.services.transferobject.EaidTO;
import com.mcgrawhill.ezto.api.caa.services.transferobject.EridTO;
import com.mcgrawhill.ezto.api.caa.services.transferobject.PolicyTO;
import com.mcgrawhill.ezto.api.caa.services.transferobject.QuestionMetaDataTO;
import com.mcgrawhill.ezto.api.caa.services.transferobject.QuestionWiseResponseTO;
import com.mcgrawhill.ezto.api.caa.services.transferobject.ResponseTO;
import com.mcgrawhill.ezto.api.caa.services.transferobject.RestTransactionTO;
import com.mcgrawhill.ezto.api.caa.services.transferobject.SetActivityTO;
import com.mcgrawhill.ezto.api.caa.services.transferobject.TestTO;
import com.mcgrawhill.ezto.api.caa.services.transferobject.UserResponseWithPolicyTO;
import com.mcgrawhill.ezto.api.caa.services.utilities.AssignmentServiceUtil;
import com.mcgrawhill.ezto.api.caa.services.utilities.PolicyUtil;
import com.mcgrawhill.ezto.api.caa.services.utilities.ReportUtil;
import com.mcgrawhill.ezto.api.caa.services.utilities.ResponseUtil;
import com.mcgrawhill.ezto.api.caa.services.utilities.SecurityUtil;
import com.mcgrawhill.ezto.api.caa.services.utilities.TagUtil;
import com.mcgrawhill.ezto.api.caa.services.utilities.TestUtil;
import com.mcgrawhill.ezto.api.caa.services.utilities.UserHelper;
import com.mcgrawhill.ezto.api.caa.services.validate.Validator;
import com.mcgrawhill.ezto.api.exception.BusinessException;
import com.mcgrawhill.ezto.api.license.services.LicenseService;
import com.mcgrawhill.ezto.integration.classware_hm;
import com.mcgrawhill.ezto.test.questions.question;
import com.mcgrawhill.ezto.utilities.CustomMap;
import com.mcgrawhill.ezto.utilities.spamalot;
import com.mcgrawhill.ezto.utilities.tp_utils;
/**
 * This <code>AssignmentServiceImpl</code> class holds all the services
 * definition for <code>AssignmentService</code> API. This class 
 * <code>AssignmentService</code> has methods to start an assignment, retrieve AJSON etc.
 * 
 * @author TCS
 *
 */
@Service
public class AssignmentServiceImpl implements AssignmentService {

	@Autowired
	TestUtil testUtil;

	@Autowired
	CacheService cacheService;
	
	@Autowired
	ScoringService scoringService;
	
	@Autowired
	ItemService itemService;

	@Autowired
	UserResponseDAO userResponseDAO;

	@Autowired
	ResponseUtil responseUtil;
	
	@Autowired
	ReportUtil reportUtil;

	@Autowired
	SecurityService securityService;
	
	@Autowired
	UserResponseService userResponseService;
	
	@Autowired
	PolicyService policyService;
	
	@Autowired
	UserFactory userFactory;
	
	@Autowired
	PolicyUtil policyUtil;
	
	@Autowired
	TagUtil tagUtil;
	
	@Autowired
	UserHelper userHelper;
	
	@Autowired
	AssignmentServiceUtil assignmentserviceutil;
	
	@Autowired
	@Qualifier("numaricStringValidator")
	Validator<String> validator;
	
	/** Reference of the <code>UserResponseValidator</code> class */
	@Autowired
	@Qualifier("userSaveResponseValidator")
	Validator<ResponseTO> userSaveResponseValidator;
	
	@Autowired
	@Qualifier("userSaveResponseValidator")
	Validator<ResponseTO> submissionValidator;
	
	@Autowired
	@Qualifier("questionExistanceValidator")
	Validator<TestTO> questionExistanceValidator;
	
	@Autowired
	@Qualifier("validateLSI")
	Validator<TestTO> lsiValidator;
	
	@Autowired
	LicenseService licenseService;
	
	@Autowired
	SecurityUtil securityUtil;
	
	private static final Logger _logger = Logger.getLogger(AssignmentServiceImpl.class);

	/*
	 * (non-Javadoc)
	 * @see com.mcgrawhill.ezto.api.caa.services.AssignmentService#startAssignment(java.util.Map)
	 */
	@Override
	public JSONObject startAssignment(Map<String, String> requestMap) throws BusinessException, Exception {
		ResponseTO responseTO = null;
		String eaid = "";
		String ltiName = "";
		String consumerId = "";
		JSONObject jsonObject = new JSONObject();
		if(requestMap != null && requestMap.size() > 0){
			if(licenseService.isDebuggerPresent("showConnectRequestParam")){
				_logger.info("Starting assignment with request parameter received from CONNECT :: " + requestMap);
			}
			TestTO testTO = cacheService.getTest(requestMap.get(TESTID));
			if(testTO == null){
				throw new BusinessException("Bad Test Id : " + requestMap.get(TESTID));
			}
			
			String referer = requestMap.get("Referer");
			
			boolean refererSpamalot = StringUtils.isNotBlank(referer) && referer.contains(spamalot.INDEX_URL);
			//This block should execute for all request except spamalot
			if(!refererSpamalot){
				boolean isTestMobileSafe = assignmentserviceutil.isTestMobileSafe(requestMap, testTO, jsonObject);
				if(!isTestMobileSafe){
					jsonObject.put(CaaConstants.NAME, ltiName);
					jsonObject.put(CaaConstants.CONSUMER_ID, consumerId);
					jsonObject.put(CaaConstants.EAID, eaid);
					jsonObject.put(CaaConstants.MOBILE_SAFE, CaaConstants.FALSE);
					jsonObject.put(CaaConstants.ASSIGNMENT_NOT_INITIALIZED, CaaConstants.TRUE);
					return jsonObject;
				}
			}
			
			responseTO = new ResponseTO();
			responseTO.setUserID(requestMap.get(classware_hm.STUDENT_UID));
			responseTO.setActivityID(requestMap.get(classware_hm.ACTIVITY_ID));
			responseTO.setAttemptNo(requestMap.get(classware_hm.ATTEMPT_NO));
			responseTO.setSectionID(requestMap.get(classware_hm.SECTION_ID));
			responseTO.setTestID(requestMap.get(TESTID));
			
			questionExistanceValidator.validate(testTO);
			lsiValidator.validate(testTO);
			
			//Get User depending on the role
			User user = userFactory.getUser(requestMap.get(classware_hm.ROLE));
			user.test(requestMap, responseTO, testTO);
			
			if(responseTO != null){
				EaidTO eaidTO = securityUtil.generateEaidTO(responseTO.getTestID(), String.valueOf(responseTO.getAttemptPK()), null, null, null);
				eaid = securityService.getEAID(eaidTO); 
			}
			//Checking whether the test is mobile safe or not
			boolean mobileSafe = assignmentserviceutil.isTestMobileSafe(responseTO, testTO, jsonObject);
			
			//Checking for LTI related info from feature
			String ltiInfo = licenseService.getLicenseDataByType(licenseManager.LTI_KEY);
			//Generating LTI info Map
			Map<String,String> ltiMap = assignmentserviceutil.getLtiInfo(ltiInfo);
			if(ltiMap != null && ltiMap.size() > 0){
				consumerId = ltiMap.get(CaaConstants.CONSUMER_ID);
				ltiName = ltiMap.get(CaaConstants.NAME);
			}
			//If consumer-id is null then assign blank value
			if(StringUtils.isBlank(consumerId)){
				consumerId = "";
			}
			//If LTI-name is null then assign blank value
			if(StringUtils.isBlank(ltiName)){
				ltiName = "";
			}
			jsonObject.put(CaaConstants.NAME, ltiName);
			jsonObject.put(CaaConstants.CONSUMER_ID, consumerId);
			jsonObject.put(CaaConstants.EAID, eaid);
			jsonObject.put(CaaConstants.MOBILE_SAFE, String.valueOf(mobileSafe));
		}
		return jsonObject;
	}
	
	
	/* (non-Javadoc)
	 * @see com.mcgrawhill.ezto.api.caa.services.AssignmentService#getEaidForAssignmentReportView(java.util.Map)
	 */
	@Override
	public JSONObject getEaidForAssignmentReportView(Map<String, String> requestMap) throws BusinessException, Exception {
		long sid =0l;
		boolean mobileSafe = true;
		JSONObject jsonObject = new JSONObject();
		ResponseTO responseTO = new ResponseTO();
		String testId = requestMap.get(TESTID);
		String submissionId = requestMap.get(CaaConstants.SUBMISSION_ID);
		
		TestTO testTO = cacheService.getTest(testId);	
		
		lsiValidator.validate(testTO);	
				
		if(StringUtils.isBlank(submissionId) || StringUtils.isBlank(testId)){
			throw new BusinessException("No submissionid  or testId found ");
		}else{
			try{
				sid = Long.parseLong(submissionId);
			}catch(NumberFormatException ne){
				_logger.error("Invalid submission id found, submissionId : "+submissionId,ne);
				throw new BusinessException("Invalid submission id found ");
			}
		}
		responseTO.setSubmissionID(sid);
		responseTO.setTestID(testId);
		assignmentserviceutil.updateTestParamforReportView(requestMap, responseTO);
		EaidTO eaidTO = securityUtil.generateEaidTO(responseTO.getTestID(), String.valueOf(responseTO.getAttemptPK()), null,  CaaConstants.REVIEW, requestMap.get(CaaConstants.ROLE));
		String eaid = securityService.getEAID(eaidTO);		
		mobileSafe = assignmentserviceutil.isTestMobileSafe(responseTO, testTO, jsonObject);
		jsonObject.put(CaaConstants.MOBILE_SAFE, String.valueOf(mobileSafe));
		jsonObject.put(CaaConstants.EAID, eaid);

		return jsonObject;
	}
	
	

	
	/*
	 * (non-Javadoc)
	 * @see com.mcgrawhill.ezto.api.caa.services.AssignmentService#getAjsonInfo(java.lang.String, boolean)
	 */
	@Override
	public JSONObject getAjsonInfo(String eaid, boolean isSubmissionTime) throws Exception {
		JSONObject jsonObject = null;
		try{
			if(!StringUtils.isBlank(eaid)){
				
					jsonObject = getAjsonInfoForExisting(eaid, isSubmissionTime);
				}
		}catch (Exception e) {
			throw e;
		}
		return jsonObject;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.mcgrawhill.ezto.api.caa.services.AssignmentService#getAjsonInfo(java.lang.String)
	 */
	@Override
	public JSONObject getAjsonInfo(String eaid) throws Exception {
		return  getAjsonInfo(eaid, false);
	}
	
	/**
	 * This method returns an AJSON object for an existing attempt.AJSON contains informations 
	 * regarding an unique attempt.
	 * @param attemptPK
	 * @param testID
	 * @param testTO
	 * @return
	 * @throws Exception
	 */
	public JSONObject getAjsonInfoForExisting(String eaid , boolean isSubmissionTime) throws Exception {
		JSONObject aJsonObj = null;
		JSONArray jsArrayPages = null;
		CustomMap<String,String> testParamMap = null;
		UserResponseWithPolicyTO userResponseWithPolicyTO = null;
		ResponseTO responseTO = null;
		PolicyTO policyTO = null;
		Map<String, String> policyMap = null;
		String lastVisitedItemID = "";
		String role = "";
		Map<String,String> infoMap = null;
		JSONObject scoreJsonObject = null;
		Map<String,String> eaidMap = null;
		String attemptPk = "";
		String testID = "";
		String mode = "";
		TestTO testTO = null;
		String unchangedEAID = "";
		String pageindex = "";
		String policyGrading = "";
		boolean isreviewMode = false;
		boolean isPolicyScoreplusOn = false;
		boolean isPolicyFeedBackOn = false;
		String userRole = "";
		try{
			unchangedEAID = eaid;
			eaidMap = assignmentserviceutil.getDecodedMap(eaid);
			if(eaidMap != null && eaidMap.size() > 0 ){
				attemptPk = eaidMap.get(CaaConstants.ATTEMPT_PK);
				testID = eaidMap.get(CaaConstants.ASSIGNMENTID);
				mode = eaidMap.get(CaaConstants.MODE);
				userRole = eaidMap.get(CaaConstants.ROLE);
				if(!StringUtils.isBlank(testID) && !StringUtils.isBlank(attemptPk)){
					if(!StringUtils.isBlank(attemptPk) && Long.parseLong(attemptPk) > 0 && !StringUtils.isBlank(testID)) {
						infoMap = new HashMap<String, String>();
						userResponseWithPolicyTO = userResponseService.getUserResponseWithPolicy(Long.parseLong(attemptPk));
						responseTO = userResponseWithPolicyTO.getResponseTO();
						policyTO = userResponseWithPolicyTO.getPolicyTO();
						if(policyTO != null){
							policyMap = policyTO.getPolicyMap();
						}
						if(responseTO != null && policyMap != null){
							testParamMap = responseTO.getTestParameter();
							if(testParamMap != null && testParamMap.size() > 0){
								//Checks whether the instructor is viewing student response or not
								if(StringUtils.isNotBlank(userRole) && testUtil.instructorRole(userRole) && !classware_hm.ROLE_INSTRUCTOR_ID.equals(responseTO.getUserID())){
									testTO = cacheService.getTest(responseTO.getTestID());
									//Populate the test Parameter with the modified normalized QLIST
									assignmentserviceutil.populateQList(testTO, userResponseWithPolicyTO, userRole);
									_logger.info("Modified QLIST while instructor is viewing student's attempt :: " + responseTO.getParam(CaaConstants.QLIST));
								}
								aJsonObj = new JSONObject();
								aJsonObj.put(CaaConstants.EAID, unchangedEAID);
								policyGrading = policyMap.get(classware_hm.POLICY_grading);								
								isreviewMode = assignmentserviceutil.isReviewMode(responseTO.isSubmission(), responseTO.getUserID(), mode);
								if(!isreviewMode || (StringUtils.isNotBlank(policyGrading) && !classware_hm.POLICY_grading_none.equals(policyGrading))){
									role = testParamMap.getParam(classware_hm.ROLE);
									isPolicyScoreplusOn = classware_hm.POLICY_grading_scoreplus.equals(policyMap.get(classware_hm.POLICY_grading));
									isPolicyFeedBackOn = classware_hm.POLICY_grading_feedback.equals(policyMap.get(classware_hm.POLICY_grading));
									if(!isreviewMode || (isPolicyFeedBackOn || isPolicyScoreplusOn)){
										jsArrayPages =  assignmentserviceutil.getQuestionPagesInfoJSON(eaidMap , userResponseWithPolicyTO);
										//lastVisitedItemID = testParamMap.getParam(CaaConstants.LAST_VISITED_ITEM);										
										/* This is added for CONNECTA-1431 last Visited Item is blank if the assignment is attempted and save and exit 
										 * from classic connect. In PAAM first question is opening.
										 */
										lastVisitedItemID = testParamMap.getParam(CaaConstants.ITEMID);
										_logger.info("############################# lastVisitedItemID retrieved : "+ lastVisitedItemID);
										
										if(!StringUtils.isBlank(lastVisitedItemID)){
											EridTO eridTO = securityUtil.generateEridTO(testID, attemptPk, lastVisitedItemID, mode);
											pageindex = securityService.getERID(eridTO);
										}
									}
									aJsonObj.put(CaaConstants.ITEMGROUPS, jsArrayPages);
									aJsonObj.put(CaaConstants.LASTITEMVISITED, pageindex); 
									boolean mobileSafe = true;
									if(testTO == null){
										testTO = cacheService.getTest(testID);
									}
									if(!licenseService.isDebuggerPresent("disableMobileSafe")){
										mobileSafe = assignmentserviceutil.isTestMobileSafe(responseTO, testTO, aJsonObj);
									}
									aJsonObj.put(CaaConstants.MOBILE_SAFE, String.valueOf(mobileSafe));
									if(testParamMap != null && testParamMap.size() > 0) {
										aJsonObj.put(CaaConstants.URLS, assignmentserviceutil.getURLsJson(role, userResponseWithPolicyTO));
										assignmentserviceutil.populateTestTitle(responseTO, testTO);
										aJsonObj.put(CaaConstants.AVARS, assignmentserviceutil.getAvarJSON(responseTO));
										aJsonObj.put(CaaConstants.INSTRUCTIONS,testParamMap.getParam(classware_hm.HEADER_INSTRUCTIONS));
										//Get the P_INSTRUCTIONS value 
										String p_instructions = testParamMap.getParam(classware_hm.P_INSTRUCTIONS);
										JSONObject pInstructionJson = null;
										try{
											if(StringUtils.isNotBlank(p_instructions)){
												//convert the entities to character and form JSON Object
												pInstructionJson = new JSONObject(tp_utils.entitiesToCharacters(p_instructions));
											}
										}catch(Exception je){
											pInstructionJson = null;
											_logger.error("Problem in parsing P_INSTRUCTIONS JSON String : "+p_instructions, je);
										}
										//Setting deductions policies into the instructions object
										pInstructionJson = assignmentserviceutil.setPoliciesIntoInstructions(responseTO, pInstructionJson , policyMap , testTO);
										//Add the JSON Object with AJSON
										aJsonObj.put(classware_hm.P_INSTRUCTIONS, pInstructionJson);
									}
									
									if(isreviewMode){
										if(isSubmissionTime){
											scoreJsonObject = assignmentserviceutil.getScoreJSON(testParamMap);
										}else {
											if(testTO == null){
												testTO = cacheService.getTest(testID);
											}
											scoreJsonObject = assignmentserviceutil.calculateScoreJSON(responseTO, testTO);
										}
										
										Long submissionTime = responseTO.getSubmissionTime();
										if(submissionTime != null && submissionTime != 0){
											aJsonObj.put(CaaConstants.ATTEMPT_SUBMISSION_TIME, getFormattedDate(submissionTime,"yyyy-MM-dd HH:mm:ss", "EST5EDT"));
										}
									}
									aJsonObj.put(CaaConstants.SCORE,scoreJsonObject);
									//Generating the remaining time based on policy time limit
									long timeRemaining = assignmentserviceutil.getRemainingTime(responseTO);
									if(CaaConstants.NO_TIMER != timeRemaining){
										aJsonObj.put(CaaConstants.TIMEREMAINING, Long.toString(timeRemaining));
									}
									infoMap.put(CaaConstants.ROLE, role);
									infoMap.put(CaaConstants.SECTIONID, responseTO.getSectionID());
									infoMap.put(CaaConstants.MODE, mode);
									aJsonObj.put(CaaConstants.STATUS, assignmentserviceutil.getSubmissionInfo(responseTO.isSubmission(), infoMap));
								}else{
									aJsonObj.put(CaaConstants.MESSAGE, CaaConstants.POST_SUBMISSION_MESSAGE);
								}
							}
						}
					}
				}
			}
		}catch (Exception e) {
			throw e;
		}
		return aJsonObj;
	}

	/* (non-Javadoc)
	 * @see com.mcgrawhill.ezto.api.caa.services.AssignmentService#submitAttempt(java.lang.String )
	 */
	@Override	
	@Transactional(propagation = Propagation.REQUIRED, readOnly=false, rollbackFor=Exception.class)	
	public void submitAttempt(String eaid) throws BusinessException, Exception {
		Map<String, String> eaidMap = SecurityUtil.decodeEAID(eaid);
		Long attemptPk = Long.valueOf(eaidMap.get(UserResponseService.ATTEMPT_PK));
		String assignmentID = eaidMap.get(UserResponseService.ASSIGNMENT_ID);
		
		UserResponseWithPolicyTO userResponseWithPolicy = userResponseService.getUserResponse(attemptPk, null);
		ResponseTO responseTO = userResponseWithPolicy.getResponseTO();
		submissionValidator.validate(responseTO);
		
		final TestTO test = cacheService.getTest(assignmentID);
		List<QuestionWiseResponseTO> attemptDataUpdateList = scoringService.evaluateAssignment(userResponseWithPolicy.getResponseTO(), test, userResponseWithPolicy.getPolicyTO());
		final Map<String, List<String>> flaggedItems = itemService.getFlaggedItems(test.getTestID());
		scoringService.calculateScore(userResponseWithPolicy.getResponseTO(), test, flaggedItems);
		String userRole = responseUtil.getUserRole(classware_hm.ROLE, responseTO);
		if(responseTO != null) {
			//Get User depending on the role
			User user = userFactory.getUser(userRole);
			//Check whether user request for test or review mode
			user.submit(responseTO);
			//update database with updated responseTO
			AttemptDTO attemptDTO = responseUtil.parseStudentTestWiseResponse(responseTO);
			if(userResponseDAO.ifSubmissionExists(attemptDTO)){
				throw new BusinessException("HM010 - It seems that this assignment attempt has already been submitted");
			}
			userResponseDAO.updateAttemptForSubmission(attemptDTO);
			if(attemptDataUpdateList != null && !attemptDataUpdateList.isEmpty()){
				userResponseDAO.updateAttemptDataDuringSubmission(responseUtil.parseStudentQuestionWiseResponse(attemptDataUpdateList));
			}
			//TODO This code needs to be moved to a proper place
			if(user instanceof Student){
				reportSubmission(responseTO, userResponseWithPolicy.getPolicyTO(), test.getQuestionMetaDataList(), flaggedItems, false);
			}
		}
	}
	/**
	 * (non-Javadoc)
	 * @see com.mcgrawhill.ezto.api.caa.services.AssignmentService#reportSubmission(ResponseTO response, PolicyTO policy, List<QuestionMetaDataTO> questionMetaDataList, 
			Map<String, List<String>> flaggedItems, boolean isResubmission)
	 */
	@Override
	public void reportSubmission(ResponseTO response, PolicyTO policy, List<QuestionMetaDataTO> questionMetaDataList, 
			Map<String, List<String>> flaggedItems, boolean isResubmission) throws Exception {
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		CustomMap<String, String> testParams = (CustomMap<String, String>)response.getTestParameter();
		
		Element results = new Element("results");

		List<String> creditedItems = null;
		List<String> droppedItems = null;
		List<String> manualItems = null;
		
		if(flaggedItems != null && !flaggedItems.isEmpty()){
			creditedItems = flaggedItems.get("fullCredit");
			droppedItems = flaggedItems.get("dropped");
			manualItems = flaggedItems.get("manualScore");			
		}
		
		String qlist = testParams.getParam("qlist");	
		Document xmlSnippet = new Document(results);
		
		double totElapsed = 0.00;		
		List<String> usedIdList = new ArrayList<String>();
		
		int ctr = 0;
		if(questionMetaDataList != null && !questionMetaDataList.isEmpty()){
			for(QuestionMetaDataTO qMetadata:questionMetaDataList){		
				String qid = qMetadata.getQuestionID();
				if (usedIdList.contains(qid)){
					continue; // only include one in reports in case of dup ids
				}
				usedIdList.add(qid);

				if (droppedItems != null && droppedItems.contains(qid)){				
					continue;
				}

				if (qlist.indexOf(qid) < 0){
					continue; // do not report unoffered questions
				}

				if (qMetadata.getQuestionType() == question.QUESTION_TYPE_sectionbreak && (qMetadata.getRefTag().length() != 0)){
					continue;		
				}

				QuestionWiseResponseTO questionWiseResponseTO = null;

				questionWiseResponseTO = response.getResponseMap().get(qMetadata.getQuestionID());

				if(questionWiseResponseTO == null){
					questionWiseResponseTO = new QuestionWiseResponseTO();
					response.getResponseMap().put(qMetadata.getQuestionID(), questionWiseResponseTO);
					reportUtil.repairResponse(questionWiseResponseTO, qMetadata, policy, response, qid);
				}

				String elapsed = String.valueOf(questionWiseResponseTO.getElapsed());
				double elapsedInSecond = 0.00;
				Element item = new Element("item");
				results.addContent(item);
				item.setAttribute("id", Integer.toString(++ctr));

				BigDecimal qAwd = new BigDecimal("1.00");

				qAwd = reportUtil.setAnsweredFlag(item, qMetadata, qAwd, testParams);

				BigDecimal qPts = new BigDecimal(Integer.toString(questionWiseResponseTO.getPoints()));
				if (creditedItems != null && creditedItems.contains(qid))
					qPts = new BigDecimal(Integer.toString(questionWiseResponseTO.getPointsMax()));

				qPts = qPts.multiply(qAwd);
				qPts = qPts.divide(new BigDecimal(Integer.toString(CaaConstants.DEFAULT_INTERNAL_POINTS) + ".00"), 2, BigDecimal.ROUND_UP);

				if (qMetadata.getQuestionType() == question.QUESTION_TYPE_sectionbreak) {
					reportUtil.setItemAttributesForSectionBreak(item);
				} else {
					item.setAttribute("correct", qPts.toString());
					item.setAttribute("possible", qAwd.toString());				
					reportUtil.setScoringAttribute(item, response.getTestParameter(), qMetadata, flaggedItems);

					if(elapsed != null && !"".equals(elapsed)){
						elapsedInSecond = Double.parseDouble(elapsed)/1000;
						totElapsed = totElapsed + elapsedInSecond;
					}
				}

				item.setAttribute("nativeid", qid);
				item.setAttribute("elapsed", String.valueOf(twoDForm.format(elapsedInSecond)) );
			}
		}

		results.setAttribute("elapsed", String.valueOf(twoDForm.format(totElapsed))); // ELAPSED TIME in seconds for the whole assignment
		final String xml = reportUtil.buildXml(xmlSnippet);
		if(!licenseService.isFeaturePresent("connectJsonSubmission")){
			reportUtil.pushSubmissionXml(response.getTestParameter(), xml, response.getSubmissionID(), isResubmission);
		}else{
			String theURL = licenseService.getLicenseDataByType(licenseManager.OAUTH_SUBMISSION_URL);
			String sharedSecreteKey = licenseService.getLicenseDataByType(licenseManager.OAUTH_KEY_OPEN_API);
			String sharedKey = "";
			String secretKey = "";
			
			StringTokenizer theTokens = new StringTokenizer(sharedSecreteKey, ";");
			while(theTokens.hasMoreTokens()){
				sharedKey = theTokens.nextToken();
				secretKey = theTokens.nextToken();
			}
						
			RestTransactionTO restTransactionTO = new RestTransactionTO(CaaConstants.SUBMISSION_ACTION_SUBMISSIONREPORT, CaaConstants.SUBMISSION_ORIGINATING_USERSUBMISSION);
			JSONObject restJson = responseUtil.convertOldXmlToNewJson(response, xml, restTransactionTO);
			try{
				reportUtil.pushSubmissionJson(response.getTestParameter(), theURL, restJson, sharedKey, secretKey);
			}catch(Exception e){
				_logger.error("Exception submiting results data for sid " + response.getSubmissionID() + " to Spark for JSON : " + restJson, e);
				userResponseService.recordFaliures(theURL.toString(), restJson.toString(), classware_hm.TRANSACTION_SUBMIT_JSON);
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.mcgrawhill.ezto.api.caa.services.AssignmentService#setActivityPosition(java.lang.String, java.lang.String)
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED, readOnly=false, rollbackFor=Exception.class)
	public SetActivityTO setActivityPosition(SetActivityTO activityTO)throws BusinessException, Exception {
		ResponseTO responseTO = null;
		/**
		 * Modified code for EZAPI-91 Modify activity position API to calculate elapsed time of an item and assignment
		 */
		String erid = null;
		String eaid = null;
		if(activityTO == null || StringUtils.isBlank(activityTO.getErid()) || StringUtils.isBlank(activityTO.getEaid()) ){
			throw new Exception("EAID or ERID is coming as Null");
		}
		erid = activityTO.getErid();
		eaid = activityTO.getEaid();
		//extract eridMap from coming erid
		Map<String, String> eridMap = SecurityUtil.decodeERID(erid);
		//Get the mode information from eaid
		String eaidMode = SecurityUtil.decodeEAID(eaid).get(CaaConstants.MODE);
		//Get the mode information from erid
		String eridMode = eridMap.get(CaaConstants.MODE);
		//Get the attemptPK from eridMap
		Long attemptPK = Long.valueOf(eridMap.get(CaaConstants.ATTEMPT_PK));
		//Get the itemID from eridMap
		String itemID = eridMap.get(CaaConstants.ITEM_ID);
		SetActivityTO setActivityTO = null;
		//Check whether the itemID is null or empty
		if(StringUtils.isBlank(itemID)){
			_logger.error("ItemID is coming as Null or Empty erid "+erid);
			throw new BusinessException("ItemID is coming as Null or Empty");
		}
		//Check whether the mode is review or not. if review then we are not updating the lastVisitedItem attribute 
		if(CaaConstants.REVIEW.equalsIgnoreCase(eaidMode) || CaaConstants.REVIEW.equalsIgnoreCase(eridMode)){
			_logger.info("Submission has been done for this assignment for attept_pk : "+attemptPK);
			throw new BusinessException("Submission has been done for this assignment");
		}
		//Get existing response for database for attemptPK and provided question id
		if(attemptPK != 0){
			try{
				responseTO = userResponseService.getUserResponse(attemptPK);
				/**
				 * Modified code for EZAPI-91 Modify activity position API to calculate elapsed time of an item and assignment
				 * 
				 * Get the Question level user responses
				 * 
				 * Here, we first see if the test param has any item id.
				 * if there is any item id then it must be recorded for the previous item.
				 * We only need that question wise student reponse for that item id.
				 * 
				 * if we do not found any item id for in the test param that signifies this is the first
				 * question user has landed. in this case we do not need any elapsed time to modify.
				 * 
				 * in case of save And exit, test param should have current question id saved as item id
				 * in test param. in this case we only have to modify the elapsed time of the current question
				 */
				if(responseTO != null && responseTO.getTestParameter() != null && !responseTO.getTestParameter().isEmpty()){
					String prevItemId = responseTO.getTestParameter().getParam(CaaConstants.ITEMID);
					if(StringUtils.isNotBlank(prevItemId)){
						List<String> questionIdList = new ArrayList<String>();
						questionIdList.add(prevItemId);
						List<AttemptDataDTO> attemptDataDTOList = userResponseDAO.getAttemptData(attemptPK, questionIdList);
						if(attemptDataDTOList != null && !attemptDataDTOList.isEmpty()){
							List<QuestionWiseResponseTO> questionWiseResponseTOList = responseUtil.createStudentQuestionWiseResponse(attemptDataDTOList);
							if(questionWiseResponseTOList != null && questionWiseResponseTOList.size() == 1){
								QuestionWiseResponseTO questionWiseResponseTO = questionWiseResponseTOList.get(0);
								if(questionWiseResponseTO != null){
									responseTO.getResponseMap().put(prevItemId, questionWiseResponseTO);
								}
							}
						}
					}
				}
			}catch(Exception ex){
				_logger.error("Response does not exist to update for given attemptPK : "+attemptPK);
				throw new BusinessException("Response does not exist to update for given EAID or ERID");
			}
		}
		//update the lastVisitedItem attribute of the test param of attempt table with provided question id 
		if(responseTO != null){
			//validate responseTO for which is coming from database
			userSaveResponseValidator.validate(responseTO);
			String prevItemId = responseTO.getParam(CaaConstants.ITEMID);
			_logger.info("############################## Setting last visited Item id : "+itemID);
			/**
			 * Modified for EZAPI-91 Modify activity position API to calculate elapsed time of an item and assignment
			 */
			activityTO.setItemID(itemID);
			QuestionWiseResponseTO prevQuestionWiseResponseTO = assignmentserviceutil.calculateElapsedTime(activityTO, responseTO);
			//Set the "oneWayRestrict" flag 
			setOneWayRestrictFlag(responseTO, itemID);
			//Fetching the remaining time
			long remainingTime = assignmentserviceutil.getRemainingTime(responseTO);
			if(remainingTime != CaaConstants.NO_TIMER){
				setActivityTO = new SetActivityTO();
				setActivityTO.setTimeremaining(remainingTime);
			}
			//Get the attempt, attempt data table representation (AttemptDTO and AttemptDataDTO) from updated responseTO and QuestionWiseResponseTO
			AttemptDTO attemptDTO = responseUtil.parseStudentTestWiseResponse(responseTO);
			AttemptDataDTO prevAttemptDataDTO = responseUtil.parseStudentQuestionWiseResponse(prevQuestionWiseResponseTO);
			//update the database
			userResponseDAO.updateAttemptParam(attemptDTO.getTestParameter(),attemptDTO.getAttemptPK());
			if(prevAttemptDataDTO != null){
				List<AttemptDataDTO> attemptDataDTOList = new ArrayList<AttemptDataDTO>();
				attemptDataDTOList.add(prevAttemptDataDTO);
				userResponseDAO.updateTimeOnTask(attemptDataDTOList, attemptDTO.getAttemptPK());
			}
			
			/**
			 * Write code for calculating elapsed time for part questions of a section break question.
			 * Here, elapsed Time for section break question already has been calculated. 
			 * Now, we need to do the following work
			 *  1. Get all the part questions if the previously stored item id is a section break(decide it form qlist)
			 *  (Here, previously stored item id means before update by the current questions id)
			 *  2. Get the updated elapsed time for the section break question
			 *  3. Divide section break elapsed time among all the part questions
			 *  4. Update the part question elapsed time in database
			 */
			if(prevAttemptDataDTO != null){
				long elapsedTime = prevAttemptDataDTO.getElapsed();
				List<String> partQList = assignmentserviceutil.getPartQuestiobIds(responseTO, prevItemId);
				if(partQList != null && !partQList.isEmpty()){
					List<AttemptDataDTO> attemptDataDTOList = userResponseDAO.getAttemptData(attemptPK, partQList);
					if(attemptDataDTOList != null && !attemptDataDTOList.isEmpty()){
						List<QuestionWiseResponseTO> partQWiseResponseTOList = responseUtil.createStudentQuestionWiseResponse(attemptDataDTOList);
						List<QuestionWiseResponseTO> updatePartQWiseResponseTOList =assignmentserviceutil.calculateElapsedTimeForPartQs(partQWiseResponseTOList, elapsedTime);
						if(updatePartQWiseResponseTOList != null && !updatePartQWiseResponseTOList.isEmpty()){
							List<AttemptDataDTO> prevAttemptDataDTOList = responseUtil.parseStudentQuestionWiseResponse(updatePartQWiseResponseTOList);
							if(prevAttemptDataDTOList != null && !prevAttemptDataDTOList.isEmpty()){
								userResponseDAO.updateTimeOnTask(prevAttemptDataDTOList, attemptDTO.getAttemptPK());
							}
						}
					}
				}
			}
			_logger.info("Lats Visited Item update done for questionid : "+itemID+" and attemptPK : "+attemptPK);
		}else{
			_logger.error("Response does not exist to update for given attemptPK : "+attemptPK);
			throw new BusinessException("Response does not exist to update for given  EAID or ERID");
		}
		return setActivityTO;
	}

	/*
	 * (non-Javadoc)
	 * @see com.mcgrawhill.ezto.api.caa.services.AssignmentService#getReturnURL(java.lang.String)
	 */
	@Override
	public String getExitURL(String eaid) {
		String returnURL = "";
		String attemptPK = "";
		Map<String,String> eaidMap = null;
		try{
			//Generate decoded map from eaid
			eaidMap = assignmentserviceutil.getDecodedMap(eaid);
			attemptPK = eaidMap.get(CaaConstants.ATTEMPT_PK);
			//Get Responses of the user based on the unique attempPk
			ResponseTO responseTO = userResponseService.getUserResponse(Long.parseLong(attemptPK));
			if(responseTO != null){
				//Generate the exit url from the response
				returnURL = assignmentserviceutil.getExitURL(responseTO);
			}
		}catch (Exception e) {
			_logger.error("Exception while constructing the Return URL for attemptPK :: " + attemptPK , e);
		}
		return returnURL;
	}

	/* (non-Javadoc)
	 * @see com.mcgrawhill.ezto.api.caa.services.AssignmentService#getCompletionStatus(java.lang.String)
	 */
	@Override
	public JSONObject getCompletionStatus(String eaid) throws BusinessException, Exception {
		UserResponseWithPolicyTO userResponseWithPolicyTO = null;
		JSONObject returnCompJson = new JSONObject();
		JSONObject compJSON = new JSONObject();
		returnCompJson.put("compstatus", compJSON);
		//decode eaid
		Map<String, String> eaidMap = SecurityUtil.decodeEAID(eaid);
		_logger.info("getCompletionStatus method has been called for detail eaidMap : "+eaidMap);
		//Get testid from eaidMap
		String testId = eaidMap.get(CaaConstants.ASSIGNMENTID).trim();
		//Get attemptPk from eaidMap
		Long attemptPk = Long.valueOf(eaidMap.get(CaaConstants.ATTEMPT_PK).trim());
		//Get mode information from eaidMap
		String mode = eaidMap.get(CaaConstants.MODE);
		//if testid is Empty or Null throw Exception
		if(StringUtils.isBlank(testId)){
			throw new BusinessException("testId is coming as Null or Empty for given eaid : "+eaid);
		}
		//Get Test meta data information for given testid
		TestTO testTO = cacheService.getTest(testId);
		if(testTO != null && testTO.getQuestionMetaDataList() != null){
			//Get the question ids which are associated with the test 
			try{
				//Get the user response information 
				userResponseWithPolicyTO = userResponseService.getUserResponse(attemptPk, null);
			}catch(Exception e){
				_logger.error("Response does not exist for given attemptPK : "+attemptPk+", testid "+testId);
				throw new BusinessException("Response does not exist to render for given EAID");
			}
			if(userResponseWithPolicyTO != null && userResponseWithPolicyTO.getResponseTO() != null){
				ResponseTO responseTO = userResponseWithPolicyTO.getResponseTO();
				String qListStr = responseTO.getParam(CaaConstants.QLIST);
				List<String> qList = null;
				if(StringUtils.isNotBlank(qListStr)){
					qList = Arrays.asList(qListStr.split(",|;"));
				}else{
					_logger.error("qlist is coming as null or empty for given attemptPK : " + attemptPk + ", testid : " + testId + ", qListStr : " + qListStr);
					throw new Exception("qlist is coming as null or empty");
				}
				
				Map<String, QuestionWiseResponseTO> responseMap = responseTO.getResponseMap();
				if(responseMap != null && !responseMap.isEmpty()){
					//iterate over the question id list
					if(qList != null && !qList.isEmpty()){
						for(String questionID : qList){
							if(StringUtils.isNotBlank(questionID)){
								//Get the Question specific completion status Json
								JSONObject questionSpecificJSON = responseUtil.getQuestionSpecificCompletionStatus(testTO, responseTO, questionID, mode);
								//Get the erid for the given question
								EridTO eridTO = securityUtil.generateEridTO(testId, String.valueOf(attemptPk), questionID, mode);
								String erid = securityService.getERID(eridTO);
								compJSON.put(erid, questionSpecificJSON);
							}else{
								_logger.error("Question id is Empty or NULL for given attemptPK : "+attemptPk+", testid "+testId+", qListStr : "+qListStr);
								throw new Exception("Question id is Empty or NULL");
							}
						}
					}
				}else{
					_logger.error("Question specific information not found in Response for given attemptPK : "+attemptPk+", testid "+testId+", questionIdList : "+qListStr);
					throw new Exception("Question specific information not found in Response");
				}
			}else{
				_logger.error("Response does not exist for given attemptPK : "+attemptPk+", testid "+testId);
				throw new BusinessException("Response does not exist to render for given EAID");
			}
		}else{
			_logger.error("No test found for testid : "+testId);
			throw new Exception("No test found for testid : "+testId);
		}
		return returnCompJson;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.mcgrawhill.ezto.api.caa.services.AssignmentService#isOneWayRestrictFlagNeedToSet(com.mcgrawhill.ezto.api.caa.services.transferobject.ResponseTO, java.lang.String)
	 */
	@Override
	public boolean isOneWayRestrictFlagNeedToSet(ResponseTO responseTO , String itemID) throws Exception , BusinessException {
		String oneWayRestrictKey = "";
		question theQ = null;
		boolean result = false;
		if(StringUtils.isNotBlank(itemID) && responseTO != null ){
			//Generating the "oneWayRestrict" key for the given question
			oneWayRestrictKey = assignmentserviceutil.getOneWayRestrictKey(itemID);
			//Checking whether the "oneWayRestrictKey" is generated or not
			if(StringUtils.isNotBlank(oneWayRestrictKey)){
				//Checking whether flag contains same question information is already present in test parameter
				if(!CaaConstants.TRUE.equals(responseTO.getParam(oneWayRestrictKey))){
					theQ = cacheService.getItem(itemID);
					//Checking whether the multi part set is one way restricted
					result = responseUtil.isMultiPartOneWayRestricted(theQ, responseTO);
				}else{
					throw new BusinessException("This part-question information for qid :: " + itemID + " is already set true in testParameter");
				}
			}else{
				throw new BusinessException("One-way restricted key for the question having qid :: " + itemID + " is not generated");
			}
		}else {
			throw new BusinessException("ItemID or the response of the user is blank so one way restricted key is not generated");
		}
		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.mcgrawhill.ezto.api.caa.services.AssignmentService#setOneWayRestrictFlag(com.mcgrawhill.ezto.api.caa.services.transferobject.ResponseTO, java.lang.String)
	 */
	@Override
	public void setOneWayRestrictFlag(ResponseTO responseTO , String itemID) throws Exception , BusinessException {
		question theQ = null;
		boolean result = false;
		String oneWayRestrictedItemId = null;
		List<String> questionIdList = null;
		if(StringUtils.isNotBlank(itemID) && responseTO != null ){
			theQ = cacheService.getItem(itemID);
			//Checking whether the multi part set is one way restricted
			result = responseUtil.isMultiPartOneWayRestricted(theQ, responseTO);
			if(result){
				// get the question which is preceding the item
				oneWayRestrictedItemId = responseUtil.getPrecedingMultiPartItem(responseTO, itemID);
				if(StringUtils.isNotBlank(oneWayRestrictedItemId)){
					String oneWayRestrictKey = assignmentserviceutil.getOneWayRestrictKey(oneWayRestrictedItemId);
					responseTO.getTestParameter().replaceParam(oneWayRestrictKey, CaaConstants.TRUE);
					
					String compStatusOneWayString = assignmentserviceutil.getCompStatusOneWayString(responseTO.getTestParameter(), oneWayRestrictedItemId);
					if(StringUtils.isNotBlank(compStatusOneWayString)){
						responseTO.getTestParameter().replaceParam(classware_hm.COMPLETION_STATUS_ONEWAY, compStatusOneWayString);
					}
					
					questionIdList = new ArrayList<String>();
					questionIdList.add(oneWayRestrictedItemId);
					List<AttemptDataDTO> attemptDataDTOList = userResponseDAO.getAttemptDataParamForQuestion(responseTO.getAttemptPK(), questionIdList);					
					List<QuestionWiseResponseTO> questionWiseResponseTOList = responseUtil.createStudentQuestionWiseResponse(attemptDataDTOList);					
					
					for(QuestionWiseResponseTO questionWiseResponseTO : questionWiseResponseTOList){
						if(oneWayRestrictedItemId.equals(questionWiseResponseTO.getQuestionID())){
							userResponseService.updateVisitedinAttamptDataQuestionParam(questionWiseResponseTO, oneWayRestrictedItemId, responseTO.getAttemptPK());
							break;
						}
					}
				}
			}
		}else {
			throw new BusinessException("ItemID or the response of the user is blank so one way restricted key is not generated");
		}
	}
	
	/**
	 * (non-Javadoc)
	 * @see com.mcgrawhill.ezto.api.caa.services.AssignmentService#validateDataForLTI(Map)
	 */
	public boolean validateLTIParameters(Map<String,String> requestMap) throws BusinessException, Exception{
		boolean result = false;
		String theURL = licenseService.getLicenseDataByType(licenseManager.OAUTH_LTI_URL);
		if(StringUtils.isBlank(theURL)){
			throw new BusinessException("OAUTH_LTI_URL is not present");
		}
		if(reportUtil.validateLaunchInLTI(theURL,requestMap)){
			result = true;
		}else{
			_logger.error("LTI launch validation failed for request : " + requestMap);			
		}
		return result;
	}
}