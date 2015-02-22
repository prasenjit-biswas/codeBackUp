package test.com.mcgrawhill.ezto.api.caa.services.utilities;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;

import test.com.mcgrawhill.ezto.api.caa.BaseSpringJunit;

import com.mcgrawhill.ezto.admin.licenseManager;
import com.mcgrawhill.ezto.api.caa.caaconstants.CaaConstants;
import com.mcgrawhill.ezto.api.caa.dao.datatransferobject.AttemptDTO;
import com.mcgrawhill.ezto.api.caa.dao.util.QuestionMetadataUtil;
import com.mcgrawhill.ezto.api.caa.services.CacheService;
import com.mcgrawhill.ezto.api.caa.services.ItemService;
import com.mcgrawhill.ezto.api.caa.services.SecurityService;
import com.mcgrawhill.ezto.api.caa.services.TestService;
import com.mcgrawhill.ezto.api.caa.services.UserResponseService;
import com.mcgrawhill.ezto.api.caa.services.impl.TestServiceImpl;
import com.mcgrawhill.ezto.api.caa.services.transferobject.EaidTO;
import com.mcgrawhill.ezto.api.caa.services.transferobject.EridTO;
import com.mcgrawhill.ezto.api.caa.services.transferobject.PolicyTO;
import com.mcgrawhill.ezto.api.caa.services.transferobject.QuestionMetaDataTO;
import com.mcgrawhill.ezto.api.caa.services.transferobject.QuestionWiseResponseTO;
import com.mcgrawhill.ezto.api.caa.services.transferobject.ResponseTO;
import com.mcgrawhill.ezto.api.caa.services.transferobject.SetActivityTO;
import com.mcgrawhill.ezto.api.caa.services.transferobject.TestTO;
import com.mcgrawhill.ezto.api.caa.services.transferobject.UserResponseWithPolicyTO;
import com.mcgrawhill.ezto.api.caa.services.utilities.AssignmentServiceUtil;
import com.mcgrawhill.ezto.api.caa.services.utilities.LightWeightQuestionFactory;
import com.mcgrawhill.ezto.api.caa.services.utilities.PolicyUtil;
import com.mcgrawhill.ezto.api.caa.services.utilities.QuestionMetadataTOComparator;
import com.mcgrawhill.ezto.api.caa.services.utilities.RequestMap;
import com.mcgrawhill.ezto.api.caa.services.utilities.ResponseUtil;
import com.mcgrawhill.ezto.api.caa.services.utilities.SecurityUtil;
import com.mcgrawhill.ezto.api.caa.services.utilities.TagUtil;
import com.mcgrawhill.ezto.api.caa.services.utilities.TestUtil;
import com.mcgrawhill.ezto.api.caa.services.utilities.UserHelper;
import com.mcgrawhill.ezto.api.caa.services.utilities.UserResponseServiceImplHelper;
import com.mcgrawhill.ezto.api.exception.BusinessException;
import com.mcgrawhill.ezto.api.license.services.LicenseService;
import com.mcgrawhill.ezto.integration.classware_hm;
import com.mcgrawhill.ezto.test.test;
import com.mcgrawhill.ezto.test.questions.question;
import com.mcgrawhill.ezto.test.questions.question_types.lsi;
import com.mcgrawhill.ezto.test.questions.question_types.sectionBreak;
import com.mcgrawhill.ezto.test.questions.question_types.worksheet_answers.worksheet_answer_external;
import com.mcgrawhill.ezto.utilities.CustomMap;
import com.mcgrawhill.ezto.utilities.richProperties;

public class Test_AssignmentServiceUtil extends BaseSpringJunit {
	
	@Autowired
	AssignmentServiceUtil assignmentServiceUtil;
	
	@Autowired
	LicenseService licenseService;
	
	@Autowired
	TestService testService;
	
	private static final Logger _logger = Logger.getLogger(Test_AssignmentServiceUtil.class);
	
	@Spy PolicyUtil policyUtilMock;
	
	@Spy
	@InjectMocks 
	AssignmentServiceUtil assignmentServiceUtilMock;
	
	@Mock CacheService cacheServiceMock;
	
	@Mock SecurityService securityServiceMock;
	
	@Mock LicenseService licenseServiceMock; 
	
	@Mock ResponseUtil responseUtilMock;
	
	@Mock ItemService itemServiceMock;
	
	@Spy TestUtil testUtil;
	
	@Mock UserResponseService userResponseServiceMock;
	
	@Mock TagUtil tagUtilMock;
	
	@Spy SecurityUtil securityUtil;
	
	@Mock LightWeightQuestionFactory lightWeightQuestionFactoryMock;
	
	@Spy TestServiceImpl testServiceImpl;
	
	@Spy UserResponseServiceImplHelper userResponseServiceImplHelperSpy;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

	} 

	/**
	 * This method tests that getAvarJSON method which is responsible for generation AVAR info which includes 
	 * instructor name & email , student name & email , printing info  in AJSON , is working properly or not for valid input
	 * @see AssignmentServiceUtil #getAvarJSON(com.mcgrawhill.ezto.utilities.CustomMap, String) 
	 * @throws Exception
	 */
	//@Test
	public void testgetAvarJSONCase1() throws Exception {
		UserResponseWithPolicyTO userResponseWithPolicyTO = new UserResponseWithPolicyTO();
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String,String> testparamMap = responseTO.getTestParameter();
		try{
			String role = "instructor";
			String sectionID = "1111";
			responseTO.setSectionID(sectionID);
			String ebookURL = "http://www.google.com";
			testparamMap.replaceParam(classware_hm.EBOOKURL, ebookURL);
			userResponseWithPolicyTO.setResponseTO(responseTO);
			JSONObject jsOBJ = assignmentServiceUtil.getURLsJson(role, userResponseWithPolicyTO);
			assertTrue(ebookURL.equals(jsOBJ.getString("ebook")));
		}catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * This method tests that getAvarJSON method which is responsible for generation AVAR info which includes 
	 * instructor name & email , student name & email , printing info  in AJSON , is working properly or not for valid input
	 * @see AssignmentServiceUtil #getAvarJSON(com.mcgrawhill.ezto.utilities.CustomMap, String) 
	 * @throws Exception
	 */
	//@Test
	public void testgetAvarJSONCase2() throws Exception {
		UserResponseWithPolicyTO userResponseWithPolicyTO = new UserResponseWithPolicyTO();
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String,String> testparamMap = responseTO.getTestParameter();
		try{
			String role = "instructor";
			String sectionID = "1111";
			String ebookURL = "http://www.google.com";
			responseTO.setSectionID(sectionID);
			testparamMap.replaceParam(classware_hm.EBOOKURL, ebookURL);
			userResponseWithPolicyTO.setResponseTO(responseTO);
			JSONObject jsOBJ = assignmentServiceUtil.getURLsJson(role, userResponseWithPolicyTO);
			assertTrue(licenseService.getLicenseDataByType(licenseManager.ASK_INSTRUCTOR_URL).equals(jsOBJ.getString("report")));
		}catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * This method tests that getAvarJSON method which is responsible for generation AVAR info which includes 
	 * instructor name & email , student name & email , printing info  in AJSON , is working properly or not for valid input
	 * @see AssignmentServiceUtil #getAvarJSON(com.mcgrawhill.ezto.utilities.CustomMap, String) 
	 * @throws Exception
	 */
	//@Test
	public void testgetAvarJSONCase3() throws Exception {
		UserResponseWithPolicyTO userResponseWithPolicyTO = new UserResponseWithPolicyTO();
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String,String> testparamMap = responseTO.getTestParameter();
		try{
			String role = "instructor";
			String sectionID = "1111";
			String ebookURL = "http://www.google.com";
			responseTO.setSectionID(sectionID);
			testparamMap.replaceParam(classware_hm.EBOOKURL, ebookURL);
			userResponseWithPolicyTO.setResponseTO(responseTO);
			JSONObject jsOBJ = assignmentServiceUtil.getURLsJson(role, userResponseWithPolicyTO);
			assertTrue(licenseService.getLicenseDataByType(licenseManager.LICENSE_TYPE_CONNECT_TUNNEL_HELP).equals(jsOBJ.getString("help")));
		}catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * This method tests that the all the submission related information is generated properly into the AJSON
	 * or not
	 * @see AssignmentServiceUtil # populateAJSONForSubmissionInfo(boolean, java.util.Map, JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testGetSubmissionInfocase1() throws Exception {
		boolean hasSubmission = false;
		Map<String,String> map = new HashMap<String, String>();
		try{
			map.put(CaaConstants.SECTIONID, "11123");
			map.put(CaaConstants.MODE, UserResponseService.REVIEW_MODE);
			map.put(CaaConstants.ROLE, classware_hm.ROLE_INSTRUCTOR);
			String status = assignmentServiceUtil.getSubmissionInfo(hasSubmission, map);
			_logger.info("Status :: " + status);
			assertTrue(CaaConstants.POSTSUBMISSION.equals(status));
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method tests that the all the submission related information is generated properly into the AJSON
	 * or not
	 * @see AssignmentServiceUtil # populateAJSONForSubmissionInfo(boolean, java.util.Map, JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testGetSubmissionInfocase2() throws Exception {
		boolean hasSubmission = true;
		Map<String,String> map = new HashMap<String, String>();
		map.put(CaaConstants.SECTIONID, "11123");
		map.put(CaaConstants.MODE, "");
		map.put(CaaConstants.ROLE, "");
		String status = assignmentServiceUtil.getSubmissionInfo(hasSubmission, map);
		_logger.info("Status :: " + status);
		assertTrue(CaaConstants.POSTSUBMISSION.equals(status));
	}
	
	/**
	 * This method tests that the all the submission related information is generated properly into the AJSON
	 * or not
	 * @see AssignmentServiceUtil # populateAJSONForSubmissionInfo(boolean, java.util.Map, JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testGetSubmissionInfocase3() throws Exception {
		boolean hasSubmission = false;
		Map<String,String> map = new HashMap<String, String>();
		map.put(CaaConstants.SECTIONID, "11123");
		map.put(CaaConstants.MODE, "");
		map.put(CaaConstants.ROLE, "");
		String status = assignmentServiceUtil.getSubmissionInfo(hasSubmission, map);
		_logger.info("Status :: " + status);
		assertTrue(CaaConstants.PRESUBMISSION.equals(status));
	}
	
	/**
	 * This method tests that the method that converts testparmap's score related things into JSON object
	 * is working properly or not for valid input
	 * @see AssignmentServiceUtil #getScoreJSON(Map)
	 * @throws Exception
	 */
	@Test
	public void TestgetScoreJSONcase1() throws Exception {
		JSONObject scoreJSON = null;
		CustomMap<String,String> testParamMap = new CustomMap<String,String>();
		try{
			testParamMap.replaceParam("rawScore", "10");
			testParamMap.replaceParam("rawMaxScore", "100");
			testParamMap.replaceParam("rawPctString", "10%");
			testParamMap.replaceParam("rawScoreString", "10/100");
			scoreJSON = assignmentServiceUtil.getScoreJSON(testParamMap);
			assertTrue("10".equals(scoreJSON.getString("Totalscore")) 
					&& "100".equals(scoreJSON.getString("MaxScore"))
					&& "10%".equals(scoreJSON.getString("PctString")) 
					&& "10/100".equals(scoreJSON.getString("ScoreString")));
		}catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * This method tests that the method that converts testparmap's score related things into JSON object
	 * is working properly or not for valid input
	 * @see AssignmentServiceUtil #getScoreJSON(Map)
	 * @throws Exception
	 */
	@Test
	public void TestgetScoreJSONcase2() throws Exception {
		JSONObject scoreJSON = null;
		CustomMap<String,String> testParamMap = new CustomMap<String,String>();
		try{
			testParamMap.replaceParam("rawScore", "");
			testParamMap.replaceParam("rawMaxScore", "");
			testParamMap.replaceParam("rawPctString", "");
			testParamMap.replaceParam("rawScoreString", "");
			scoreJSON = assignmentServiceUtil.getScoreJSON(testParamMap);
			assertTrue("".equals(scoreJSON.getString("Totalscore")) 
					&& "".equals(scoreJSON.getString("MaxScore"))
					&& "".equals(scoreJSON.getString("PctString")) 
					&& "".equals(scoreJSON.getString("ScoreString")));
		}catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * This method tests that the method that converts testparmap's score related things into JSON object
	 * is working properly or not for valid input
	 * @see AssignmentServiceUtil #getScoreJSON(Map)
	 * @throws Exception
	 */
	@Test
	public void TestgetScoreJSONcase3() throws Exception {
		JSONObject scoreJSON = null;
		CustomMap<String,String> testParamMap = new CustomMap<String,String>();
		try{
			scoreJSON = assignmentServiceUtil.getScoreJSON(testParamMap);
			assertTrue(scoreJSON == null);
		}catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * This method tests that the method generating page correct information is working properly or not 
	 * for valid input
	 * @see AssignmentServiceUtil #getPageCorrectnessMap(CustomMap)
	 * @throws Exception
	 */
	@Test
	public void testgetPageCorrectnessMapcase1() throws Exception {
		Map<String,Boolean> correctMap = null;
		String qlist = "1;2;4,5,6,7,8;9;10";
		Map<String,QuestionWiseResponseTO> questionwiseResponseMap = new  HashMap<String,QuestionWiseResponseTO>();
		List<QuestionMetaDataTO> questionmetaDataList = new ArrayList<QuestionMetaDataTO>();
		CustomMap<String, String> currentTestParamMap = new CustomMap<String,String>();
		currentTestParamMap.replaceParam(CaaConstants.QLIST, qlist);
		currentTestParamMap.replaceParam("Q_" + "1" + CaaConstants.FORMERCORRECT, "true");
		currentTestParamMap.replaceParam("Q_" + "2" + CaaConstants.FORMERCORRECT, "true");
		currentTestParamMap.replaceParam("Q_" + "5" + CaaConstants.FORMERCORRECT, "true");
		currentTestParamMap.replaceParam("Q_" + "6" + CaaConstants.FORMERCORRECT, "true");
		currentTestParamMap.replaceParam("Q_" + "7" + CaaConstants.FORMERCORRECT, "true");
		currentTestParamMap.replaceParam("Q_" + "8" + CaaConstants.FORMERCORRECT, "true");
		currentTestParamMap.replaceParam("Q_" + "9" + CaaConstants.FORMERCORRECT, "true");
		currentTestParamMap.replaceParam("Q_" + "10" + CaaConstants.FORMERCORRECT, "true");
		try{
			correctMap = assignmentServiceUtil.getPageCorrectnessMap(currentTestParamMap, questionwiseResponseMap, questionmetaDataList);
			_logger.info("Correct Map :: " + correctMap);
			assertTrue(true == correctMap.get("1") && true == correctMap.get("2") && true == correctMap.get("4") 
					&& true == correctMap.get("9") && true == correctMap.get("10"));
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method tests that the method generating page correct information is working properly or not 
	 * for valid input
	 * @see AssignmentServiceUtil #getPageCorrectnessMap(CustomMap)
	 * @throws Exception
	 */
	@Test
	public void testgetPageCorrectnessMapcase2() throws Exception {
		Map<String,Boolean> correctMap = null;
		String qlist = "1;2;4,5,6,7,8;9;10";
		Map<String,QuestionWiseResponseTO> questionwiseResponseMap = new  HashMap<String,QuestionWiseResponseTO>();
		List<QuestionMetaDataTO> questionmetaDataList = new ArrayList<QuestionMetaDataTO>();
		CustomMap<String, String> currentTestParamMap = new CustomMap<String,String>();
		currentTestParamMap.replaceParam(CaaConstants.QLIST, qlist);
		currentTestParamMap.replaceParam("Q_" + "1" + CaaConstants.FORMERCORRECT, "true");
		currentTestParamMap.replaceParam("Q_" + "2" + CaaConstants.FORMERCORRECT, "false");
		currentTestParamMap.replaceParam("Q_" + "5" + CaaConstants.FORMERCORRECT, "true");
		currentTestParamMap.replaceParam("Q_" + "6" + CaaConstants.FORMERCORRECT, "true");
		currentTestParamMap.replaceParam("Q_" + "7" + CaaConstants.FORMERCORRECT, "true");
		currentTestParamMap.replaceParam("Q_" + "8" + CaaConstants.FORMERCORRECT, "true");
		currentTestParamMap.replaceParam("Q_" + "9" + CaaConstants.FORMERCORRECT, "true");
		currentTestParamMap.replaceParam("Q_" + "10" + CaaConstants.FORMERCORRECT, "true");
		try{
			correctMap = assignmentServiceUtil.getPageCorrectnessMap(currentTestParamMap, questionwiseResponseMap, questionmetaDataList);
			_logger.info("Correct Map :: " + correctMap);
			assertTrue(true == correctMap.get("1") && false == correctMap.get("2") && true == correctMap.get("4") 
					&& true == correctMap.get("9") && true == correctMap.get("10"));
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method tests that the method generating page correct information is working properly or not 
	 * for valid input
	 * For MultiPart Question , if one question wrong among the page then the full page is incorrect
	 * for that section break question
	 * @see AssignmentServiceUtil #getPageCorrectnessMap(CustomMap)
	 * @throws Exception
	 */
	@Test
	public void testgetPageCorrectnessMapcase3() throws Exception {
		Map<String,Boolean> correctMap = null;
		String qlist = "1;2;4,5,6,7,8;9;10";
		CustomMap<String, String> currentTestParamMap = new CustomMap<String,String>();
		Map<String,QuestionWiseResponseTO> questionwiseResponseMap = new  HashMap<String,QuestionWiseResponseTO>();
		List<QuestionMetaDataTO> questionmetaDataList = new ArrayList<QuestionMetaDataTO>();
		currentTestParamMap.replaceParam(CaaConstants.QLIST, qlist);
		currentTestParamMap.replaceParam("Q_" + "1" + CaaConstants.FORMERCORRECT, "true");
		currentTestParamMap.replaceParam("Q_" + "2" + CaaConstants.FORMERCORRECT, "true");
		currentTestParamMap.replaceParam("Q_" + "5" + CaaConstants.FORMERCORRECT, "false");
		currentTestParamMap.replaceParam("Q_" + "6" + CaaConstants.FORMERCORRECT, "true");
		currentTestParamMap.replaceParam("Q_" + "7" + CaaConstants.FORMERCORRECT, "true");
		currentTestParamMap.replaceParam("Q_" + "8" + CaaConstants.FORMERCORRECT, "true");
		currentTestParamMap.replaceParam("Q_" + "9" + CaaConstants.FORMERCORRECT, "true");
		currentTestParamMap.replaceParam("Q_" + "10" + CaaConstants.FORMERCORRECT, "true");
		try{
			correctMap = assignmentServiceUtil.getPageCorrectnessMap(currentTestParamMap, questionwiseResponseMap, questionmetaDataList);
			_logger.info("Correct Map :: " + correctMap);
			assertTrue(true == correctMap.get("1") && true == correctMap.get("2") && false == correctMap.get("4") 
					&& true == correctMap.get("9") && true == correctMap.get("10"));
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method tests that the method generating page correct information is working properly or not 
	 * for valid input
	 * @see AssignmentServiceUtil #getPageCorrectnessMap(CustomMap)
	 * @throws Exception
	 */
	@Test
	public void testgetPageCorrectnessMapcase4() throws Exception {
		Map<String,Boolean> correctMap = null;
		String qlist = "1;2;4,5,6,7,8;9;10";
		Map<String,QuestionWiseResponseTO> questionwiseResponseMap = new  HashMap<String,QuestionWiseResponseTO>();
		List<QuestionMetaDataTO> questionmetaDataList = new ArrayList<QuestionMetaDataTO>();
		CustomMap<String, String> currentTestParamMap = new CustomMap<String,String>();
		currentTestParamMap.replaceParam(CaaConstants.QLIST, qlist);
		currentTestParamMap.replaceParam("Q_" + "1" + CaaConstants.FORMERCORRECT, "true");
		currentTestParamMap.replaceParam("Q_" + "2" + CaaConstants.FORMERCORRECT, "true");
		currentTestParamMap.replaceParam("Q_" + "5" + CaaConstants.FORMERCORRECT, "true");
		currentTestParamMap.replaceParam("Q_" + "6" + CaaConstants.FORMERCORRECT, "false");
		currentTestParamMap.replaceParam("Q_" + "7" + CaaConstants.FORMERCORRECT, "true");
		currentTestParamMap.replaceParam("Q_" + "8" + CaaConstants.FORMERCORRECT, "true");
		currentTestParamMap.replaceParam("Q_" + "9" + CaaConstants.FORMERCORRECT, "true");
		currentTestParamMap.replaceParam("Q_" + "10" + CaaConstants.FORMERCORRECT, "true");
		try{
			correctMap = assignmentServiceUtil.getPageCorrectnessMap(currentTestParamMap, questionwiseResponseMap, questionmetaDataList);
			_logger.info("Correct Map :: " + correctMap);
			assertTrue(true == correctMap.get("1") && true == correctMap.get("2") && false == correctMap.get("4") 
					&& true == correctMap.get("9") && true == correctMap.get("10"));
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method tests that the method generating page correct information is working properly or not 
	 * for valid input
	 * @see AssignmentServiceUtil #getPageCorrectnessMap(CustomMap)
	 * @throws Exception
	 */
	@Test
	public void testgetPageCorrectnessMapcase5() throws Exception {
		Map<String,Boolean> correctMap = null;
		String qlist = "1;2;4,5,6,7,8;9;10";
		CustomMap<String, String> currentTestParamMap = new CustomMap<String,String>();
		Map<String,QuestionWiseResponseTO> questionwiseResponseMap = new  HashMap<String,QuestionWiseResponseTO>();
		List<QuestionMetaDataTO> questionmetaDataList = new ArrayList<QuestionMetaDataTO>();
		currentTestParamMap.replaceParam(CaaConstants.QLIST, qlist);
		currentTestParamMap.replaceParam("Q_" + "1" + CaaConstants.FORMERCORRECT, "true");
		currentTestParamMap.replaceParam("Q_" + "2" + CaaConstants.FORMERCORRECT, "true");
		currentTestParamMap.replaceParam("Q_" + "5" + CaaConstants.FORMERCORRECT, "true");
		currentTestParamMap.replaceParam("Q_" + "6" + CaaConstants.FORMERCORRECT, "true");
		currentTestParamMap.replaceParam("Q_" + "7" + CaaConstants.FORMERCORRECT, "false");
		currentTestParamMap.replaceParam("Q_" + "8" + CaaConstants.FORMERCORRECT, "true");
		currentTestParamMap.replaceParam("Q_" + "9" + CaaConstants.FORMERCORRECT, "true");
		currentTestParamMap.replaceParam("Q_" + "10" + CaaConstants.FORMERCORRECT, "true");
		try{
			correctMap = assignmentServiceUtil.getPageCorrectnessMap(currentTestParamMap, questionwiseResponseMap, questionmetaDataList);
			_logger.info("Correct Map :: " + correctMap);
			assertTrue(true == correctMap.get("1") && true == correctMap.get("2") && false == correctMap.get("4") 
					&& true == correctMap.get("9") && true == correctMap.get("10"));
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method tests that the method generating page correct information is working properly or not 
	 * for valid input
	 * @see AssignmentServiceUtil #getPageCorrectnessMap(CustomMap)
	 * @throws Exception
	 */
	@Test
	public void testgetPageCorrectnessMapcase6() throws Exception {
		Map<String,Boolean> correctMap = null;
		String qlist = "1;2;4,5,6,7,8;9;10";
		CustomMap<String, String> currentTestParamMap = new CustomMap<String,String>();
		Map<String,QuestionWiseResponseTO> questionwiseResponseMap = new  HashMap<String,QuestionWiseResponseTO>();
		List<QuestionMetaDataTO> questionmetaDataList = new ArrayList<QuestionMetaDataTO>();
		currentTestParamMap.replaceParam(CaaConstants.QLIST, qlist);
		currentTestParamMap.replaceParam("Q_" + "1" + CaaConstants.FORMERCORRECT, "true");
		currentTestParamMap.replaceParam("Q_" + "2" + CaaConstants.FORMERCORRECT, "true");
		currentTestParamMap.replaceParam("Q_" + "5" + CaaConstants.FORMERCORRECT, "true");
		currentTestParamMap.replaceParam("Q_" + "6" + CaaConstants.FORMERCORRECT, "true");
		currentTestParamMap.replaceParam("Q_" + "7" + CaaConstants.FORMERCORRECT, "true");
		currentTestParamMap.replaceParam("Q_" + "8" + CaaConstants.FORMERCORRECT, "false");
		currentTestParamMap.replaceParam("Q_" + "9" + CaaConstants.FORMERCORRECT, "true");
		currentTestParamMap.replaceParam("Q_" + "10" + CaaConstants.FORMERCORRECT, "true");
		try{
			correctMap = assignmentServiceUtil.getPageCorrectnessMap(currentTestParamMap, questionwiseResponseMap, questionmetaDataList);
			_logger.info("Correct Map :: " + correctMap);
			assertTrue(true == correctMap.get("1") && true == correctMap.get("2") && false == correctMap.get("4") 
					&& true == correctMap.get("9") && true == correctMap.get("10"));
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method tests that the method generating page correct information is working properly or not 
	 * for valid input
	 * @see AssignmentServiceUtil #getPageCorrectnessMap(CustomMap)
	 * @throws Exception
	 */
	@Test
	public void testgetPageCorrectnessMapcase7() throws Exception {
		Map<String,Boolean> correctMap = null;
		String qlist = "1;2;4;9;10";
		CustomMap<String, String> currentTestParamMap = new CustomMap<String,String>();
		Map<String,QuestionWiseResponseTO> questionwiseResponseMap = new  HashMap<String,QuestionWiseResponseTO>();
		List<QuestionMetaDataTO> questionmetaDataList = new ArrayList<QuestionMetaDataTO>();
		currentTestParamMap.replaceParam(CaaConstants.QLIST, qlist);
		currentTestParamMap.replaceParam("Q_" + "1" + CaaConstants.FORMERCORRECT, "true");
		currentTestParamMap.replaceParam("Q_" + "2" + CaaConstants.FORMERCORRECT, "true");
		currentTestParamMap.replaceParam("Q_" + "4" + CaaConstants.FORMERCORRECT, "true");
		currentTestParamMap.replaceParam("Q_" + "9" + CaaConstants.FORMERCORRECT, "true");
		currentTestParamMap.replaceParam("Q_" + "10" + CaaConstants.FORMERCORRECT, "true");
		try{
			correctMap = assignmentServiceUtil.getPageCorrectnessMap(currentTestParamMap, questionwiseResponseMap, questionmetaDataList);
			_logger.info("Correct Map :: " + correctMap);
			assertTrue(true == correctMap.get("1") && true == correctMap.get("2") && true == correctMap.get("4") 
					&& true == correctMap.get("9") && true == correctMap.get("10"));
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * Based on the points and the max points , this method checks that the previous response is true or false
	 * @see AssignmentServiceUtil #getEvaluationOfResponse(QuestionWiseResponseTO)
	 * @throws Exception
	 */
	@Test
	public void TestgetevaluationOfResponseCase1() throws Exception {
		String response = "";
		QuestionWiseResponseTO questionWiseResponseTO = null;
		try{
			questionWiseResponseTO = new QuestionWiseResponseTO();
			questionWiseResponseTO.setPoints(10000);
			questionWiseResponseTO.setPointsMax(100000);
			response = assignmentServiceUtil.getEvaluationOfResponse(questionWiseResponseTO);
			assertTrue(CaaConstants.TRUE.equals(response));
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * Based on the points and the max points , this method checks that the previous response is true or false
	 * @see AssignmentServiceUtil #getEvaluationOfResponse(QuestionWiseResponseTO)
	 * @throws Exception
	 */
	@Test
	public void TestgetevaluationOfResponseCase2() throws Exception {
		String response = "";
		QuestionWiseResponseTO questionWiseResponseTO = null;
		try{
			questionWiseResponseTO = new QuestionWiseResponseTO();
			questionWiseResponseTO.setPoints(10000);
			questionWiseResponseTO.setPointsMax(10000);
			response = assignmentServiceUtil.getEvaluationOfResponse(questionWiseResponseTO);
			assertTrue(CaaConstants.TRUE.equals(response));
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * Based on the points and the max points , this method checks that the previous response is true or false
	 * @see AssignmentServiceUtil #getEvaluationOfResponse(QuestionWiseResponseTO)
	 * @throws Exception
	 */
	@Test
	public void TestgetevaluationOfResponseCase3() throws Exception {
		String response = "";
		QuestionWiseResponseTO questionWiseResponseTO = null;
		try{
			questionWiseResponseTO = new QuestionWiseResponseTO();
			questionWiseResponseTO.setPoints(0);
			questionWiseResponseTO.setPointsMax(0);
			response = assignmentServiceUtil.getEvaluationOfResponse(questionWiseResponseTO);
			assertTrue(CaaConstants.TRUE.equals(response));
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * Based on the points and the max points , this method checks that the previous response is true or false
	 * @see AssignmentServiceUtil #getEvaluationOfResponse(QuestionWiseResponseTO)
	 * @throws Exception
	 */
	@Test
	public void TestgetevaluationOfResponseCase4() throws Exception {
		String response = "";
		QuestionWiseResponseTO questionWiseResponseTO = null;
		try{
			questionWiseResponseTO = new QuestionWiseResponseTO();
			questionWiseResponseTO.setPoints(0);
			questionWiseResponseTO.setPointsMax(10);
			response = assignmentServiceUtil.getEvaluationOfResponse(questionWiseResponseTO);
			assertTrue(CaaConstants.FALSE.equals(response));
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method tests that for MAPPLE , WWTB and LSI question types, some fields of testparamMap
	 * are removed properly
	 * @see AssignmentServiceUtil #removeExternalResponse(CustomMap, Map, java.util.List)
	 * @throws Exception
	 */
	@Test
	public void testResetThirdPartyMathAlgoscase1() throws Exception {
		CustomMap<String, String> testParamMap = new CustomMap<String,String>();
		QuestionMetaDataTO questionMetaDataTO = new QuestionMetaDataTO();
		try{
			questionMetaDataTO.setQuestionType(question.QUESTION_TYPE_maple);
			questionMetaDataTO.setQuestionID("TestJUNT");
			String qid = questionMetaDataTO.getQuestionID();
			String theQID = "Q_" + qid;
			String aid = theQID + "_ext";
			testParamMap.replaceParam(aid + "_instanceid","T_1");
			testParamMap.replaceParam(aid + "_state","T_2");
			testParamMap.replaceParam(aid + "_mode","T_3");
			testParamMap.replaceParam(aid + "_eval","T_4");
			testParamMap.replaceParam(theQID + "_started","true");
			assignmentServiceUtil.resetThirdPartyMathAlgos(questionMetaDataTO, testParamMap);
			assertTrue(StringUtils.isBlank(testParamMap.getParam(aid + "_instanceid")) 
					&& StringUtils.isBlank(testParamMap.getParam(aid + "_state"))
					&& StringUtils.isBlank(testParamMap.getParam(aid + "_mode"))
					&& StringUtils.isBlank(testParamMap.getParam(aid + "_eval"))
					&& StringUtils.isBlank(testParamMap.getParam(theQID + "_started")));
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	
	/**
	 * This method tests that for MAPPLE , WWTB and LSI question types, some fields of testparamMap
	 * are removed properly
	 * @see AssignmentServiceUtil #removeExternalResponse(CustomMap, Map, java.util.List)
	 * @throws Exception
	 */
	@Test
	public void testResetThirdPartyMathAlgoscase2() throws Exception {
		CustomMap<String, String> testParamMap = new CustomMap<String,String>();
		QuestionMetaDataTO questionMetaDataTO = new QuestionMetaDataTO();
		try{
			questionMetaDataTO.setQuestionType(question.QUESTION_TYPE_wwtb);
			questionMetaDataTO.setQuestionID("TestJUNT");
			String qid = questionMetaDataTO.getQuestionID();
			String theQID = "Q_" + qid;
			String aid = theQID + "_ext";
			testParamMap.replaceParam(aid + "_instanceid","T_1");
			testParamMap.replaceParam(aid + "_state","T_2");
			testParamMap.replaceParam(aid + "_mode","T_3");
			testParamMap.replaceParam(aid + "_eval","T_4");
			testParamMap.replaceParam(theQID + "_started","true");
			assignmentServiceUtil.resetThirdPartyMathAlgos(questionMetaDataTO, testParamMap);
			assertTrue(StringUtils.isBlank(testParamMap.getParam(aid + "_instanceid")) 
					&& StringUtils.isBlank(testParamMap.getParam(aid + "_state"))
					&& StringUtils.isBlank(testParamMap.getParam(aid + "_mode"))
					&& StringUtils.isBlank(testParamMap.getParam(aid + "_eval"))
					&& StringUtils.isBlank(testParamMap.getParam(theQID + "_started")));
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method tests that for MAPPLE , WWTB and LSI question types, some fields of testparamMap
	 * are removed properly
	 * @see AssignmentServiceUtil #removeExternalResponse(CustomMap, Map, java.util.List)
	 * @throws Exception
	 */
	@Test
	public void testResetThirdPartyMathAlgoscase3() throws Exception {
		CustomMap<String, String> testParamMap = new CustomMap<String,String>();
		QuestionMetaDataTO questionMetaDataTO = new QuestionMetaDataTO();
		try{
			questionMetaDataTO.setQuestionType(question.QUESTION_TYPE_lsi);
			questionMetaDataTO.setQuestionID("TestJUNT");
			String qid = questionMetaDataTO.getQuestionID();
			String theQID = "Q_" + qid;
			String aid = theQID + "_ext";
			testParamMap.replaceParam(aid + "_instanceid","T_1");
			testParamMap.replaceParam(aid + "_state","T_2");
			testParamMap.replaceParam(aid + "_mode","T_3");
			testParamMap.replaceParam(aid + "_eval","T_4");
			testParamMap.replaceParam(theQID + "_started","true");
			testParamMap.replaceParam(theQID + "_seed","T_5");
			assignmentServiceUtil.resetThirdPartyMathAlgos(questionMetaDataTO, testParamMap);
			assertTrue(!StringUtils.isBlank(testParamMap.getParam(aid + "_instanceid")) 
					&& !StringUtils.isBlank(testParamMap.getParam(aid + "_state"))
					&& !StringUtils.isBlank(testParamMap.getParam(aid + "_mode"))
					&& !StringUtils.isBlank(testParamMap.getParam(aid + "_eval"))
					&& StringUtils.isBlank(testParamMap.getParam(theQID + "_started"))
					&& StringUtils.isBlank(testParamMap.getParam(theQID + "_seed")));
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method tests that for MAPPLE , WWTB and LSI question types, some fields of testparamMap
	 * are removed properly
	 * @see AssignmentServiceUtil #removeExternalResponse(CustomMap, Map, java.util.List)
	 * @throws Exception
	 */
	@Test
	public void testResetThirdPartyMathAlgoscase4() throws Exception {
		CustomMap<String, String> testParamMap = new CustomMap<String,String>();
		QuestionMetaDataTO questionMetaDataTO = new QuestionMetaDataTO();
		try{
			questionMetaDataTO.setQuestionType(question.QUESTION_TYPE_fillBlank);
			questionMetaDataTO.setQuestionID("TestJUNT");
			String qid = questionMetaDataTO.getQuestionID();
			String theQID = "Q_" + qid;
			String aid = theQID + "_ext";
			testParamMap.replaceParam(aid + "_instanceid","T_1");
			testParamMap.replaceParam(aid + "_state","T_2");
			testParamMap.replaceParam(aid + "_mode","T_3");
			testParamMap.replaceParam(aid + "_eval","T_4");
			testParamMap.replaceParam(theQID + "_started","true");
			testParamMap.replaceParam(theQID + "_seed","T_5");
			assignmentServiceUtil.resetThirdPartyMathAlgos(questionMetaDataTO, testParamMap);
			assertTrue(!StringUtils.isBlank(testParamMap.getParam(aid + "_instanceid")) 
					&& !StringUtils.isBlank(testParamMap.getParam(aid + "_state"))
					&& !StringUtils.isBlank(testParamMap.getParam(aid + "_mode"))
					&& !StringUtils.isBlank(testParamMap.getParam(aid + "_eval"))
					&& !StringUtils.isBlank(testParamMap.getParam(theQID + "_started"))
					&& !StringUtils.isBlank(testParamMap.getParam(theQID + "_seed")));
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method tests that if the question type is DOC type or worksheet_anser_external then 
	 * it removes some values from its question parameter
	 * @throws Exception
	 */
	@Test
	public void testRemoveExternalResponseCase1() throws Exception {
		QuestionMetaDataTO questionMetaDataTO = new QuestionMetaDataTO();
		QuestionWiseResponseTO questionWiseResponseTO = new QuestionWiseResponseTO();
		String qList = "";
		CustomMap<String,String> questionParamMap = questionWiseResponseTO.getQuestionParameters();
		try{
			questionMetaDataTO.setQuestionTypeIdentifier(question.TYPEID_fillBlank);
			questionMetaDataTO.setQuestionID("TestJUNT");
			String qid = questionMetaDataTO.getQuestionID();
			String theQID = "Q_" + qid;
			String aid = qid + "_ext";
			questionParamMap.replaceParam(aid + "_instanceid","T_1");
			questionParamMap.replaceParam(aid + "_state","T_2");
			questionParamMap.replaceParam(aid + "_mode","T_3");
			questionParamMap.replaceParam(theQID + "_started","true");
			questionParamMap.replaceParam(CaaConstants.QLIST, qList);
			assignmentServiceUtil.removeExternalResponse(questionWiseResponseTO, questionMetaDataTO);
			questionParamMap = questionWiseResponseTO.getQuestionParameters();
			assertTrue(!StringUtils.isBlank(questionParamMap.getParam(aid + "_instanceid")) &&
					!StringUtils.isBlank(questionParamMap.getParam(aid + "_state")) &&
					!StringUtils.isBlank(questionParamMap.getParam(aid + "_mode")) &&
					!StringUtils.isBlank(questionParamMap.getParam(theQID + "_started"))
					);
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method tests that if the question type is DOC type or worksheet_anser_external then 
	 * it removes some values from its question parameter
	 * @throws Exception
	 */
	@Test
	public void testRemoveExternalResponseCase2() throws Exception {
		QuestionMetaDataTO questionMetaDataTO = new QuestionMetaDataTO();
		QuestionWiseResponseTO questionWiseResponseTO = new QuestionWiseResponseTO();
		CustomMap<String,String> questionParamMap = questionWiseResponseTO.getQuestionParameters();
		try{
			questionMetaDataTO.setQuestionTypeIdentifier(question.TYPEID_document);
			questionMetaDataTO.setQuestionID("TestJUNT");
			questionWiseResponseTO.setQuestionID(questionMetaDataTO.getQuestionID());
			String qid = questionMetaDataTO.getQuestionID();
			String theQID = "Q_" + qid;
			String aid = qid + "_ext";
			questionParamMap.replaceParam(aid + "_instanceid","T_1");
			questionParamMap.replaceParam(aid + "_state","T_2");
			questionParamMap.replaceParam(aid + "_mode","T_3");
			questionParamMap.replaceParam(theQID + "_started","true");
			assignmentServiceUtil.removeExternalResponse(questionWiseResponseTO, questionMetaDataTO);
			questionParamMap = questionWiseResponseTO.getQuestionParameters();
			assertTrue(StringUtils.isBlank(questionParamMap.getParam(aid + "_instanceid")) &&
					StringUtils.isBlank(questionParamMap.getParam(aid + "_state")) &&
					StringUtils.isBlank(questionParamMap.getParam(aid + "_mode")) &&
					StringUtils.isBlank(questionParamMap.getParam(theQID + "_started"))
					);
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	
	/**
	 * This method tests that if the question type is DOC type or worksheet_anser_external then 
	 * it removes some values from its question parameter
	 * @see AssignmentServiceUtil#removeExternalResponse(QuestionWiseResponseTO, QuestionMetaDataTO)
	 * @throws Exception
	 */
	//@Test
	public void testRemoveExternalResponseCase3() throws Exception {
		QuestionMetaDataTO questionMetaDataTO = new QuestionMetaDataTO();
		QuestionWiseResponseTO questionWiseResponseTO = new QuestionWiseResponseTO();
		CustomMap<String,String> questionParamMap = questionWiseResponseTO.getQuestionParameters();
		List<Map<String,String>> answerList = new ArrayList<Map<String,String>>();
		Map<String,String> ans1 = new HashMap<String,String>();
		Map<String,String> ans2 = new HashMap<String,String>();
		try{
			ans1.put(QuestionMetadataUtil.TYPE,"EX");
			ans1.put(QuestionMetadataUtil.NAME,"ans1");
			ans1.put(worksheet_answer_external.IDENTIFIER,"JUNIT"+worksheet_answer_external.CHAT_QUESTION);
			
			ans2.put(QuestionMetadataUtil.TYPE,"MC");
			ans2.put(QuestionMetadataUtil.NAME,"ans2");
			
			answerList.add(ans1);
			answerList.add(ans2);
			
			questionMetaDataTO.setAnswers(answerList);
			questionMetaDataTO.setQuestionTypeIdentifier(question.TYPEID_worksheet);
			questionMetaDataTO.setQuestionID("TestJUNT");
			questionWiseResponseTO.setQuestionID(questionMetaDataTO.getQuestionID());
			String aid1 = new StringBuilder("Q_").append(questionWiseResponseTO.getQuestionID()).append("_").append("ans1").toString();
			questionParamMap.replaceParam(aid1 + "_instanceid","T_1");
			questionParamMap.replaceParam(aid1 + "_state","T_2");
			questionParamMap.replaceParam(aid1 + "_mode","T_3");
			questionParamMap.replaceParam(aid1 + "_eval","T_4");
			String aid2 = new StringBuilder("Q_").append(questionWiseResponseTO.getQuestionID()).append("_").append("ans2").toString();
			questionParamMap.replaceParam(aid2 + "_instanceid","T_1");
			questionParamMap.replaceParam(aid2 + "_state","T_2");
			questionParamMap.replaceParam(aid2 + "_mode","T_3");
			questionParamMap.replaceParam(aid2 + "_eval","T_4");
			
			assignmentServiceUtil.removeExternalResponse(questionWiseResponseTO, questionMetaDataTO);
			questionParamMap = questionWiseResponseTO.getQuestionParameters();
			assertTrue(StringUtils.isBlank(questionParamMap.getParam(aid1 + "_instanceid")) &&
					StringUtils.isBlank(questionParamMap.getParam(aid1 + "_state")) &&
					StringUtils.isBlank(questionParamMap.getParam(aid1 + "_mode")) &&
					StringUtils.isBlank(questionParamMap.getParam(aid1+ "_eval"))
					);
			assertTrue(!StringUtils.isBlank(questionParamMap.getParam(aid2 + "_instanceid")) &&
					!StringUtils.isBlank(questionParamMap.getParam(aid2 + "_state")) &&
					!StringUtils.isBlank(questionParamMap.getParam(aid2 + "_mode")) &&
					!StringUtils.isBlank(questionParamMap.getParam(aid2+ "_eval"))
					);
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method tests that if the question type is DOC type or worksheet_anser_external then 
	 * it removes some values from its question parameter
	 * @see AssignmentServiceUtil#removeExternalResponse(QuestionWiseResponseTO, QuestionMetaDataTO)
	 * @throws Exception
	 */
	//@Test
	public void testRemoveExternalResponseCase4() throws Exception {
		QuestionMetaDataTO questionMetaDataTO = new QuestionMetaDataTO();
		QuestionWiseResponseTO questionWiseResponseTO = new QuestionWiseResponseTO();
		CustomMap<String,String> questionParamMap = questionWiseResponseTO.getQuestionParameters();
		List<Map<String,String>> answerList = new ArrayList<Map<String,String>>();
		Map<String,String> ans1 = new HashMap<String,String>();
		Map<String,String> ans2 = new HashMap<String,String>();
		try{
			ans1.put(QuestionMetadataUtil.TYPE,"EX");
			ans1.put(QuestionMetadataUtil.NAME,"ans1");
			ans1.put(worksheet_answer_external.IDENTIFIER,"JUNIT");
			
			ans2.put(QuestionMetadataUtil.TYPE,"MC");
			ans2.put(QuestionMetadataUtil.NAME,"ans2");
			
			answerList.add(ans1);
			answerList.add(ans2);
			
			questionMetaDataTO.setAnswers(answerList);
			questionMetaDataTO.setQuestionTypeIdentifier(question.TYPEID_worksheet);
			questionMetaDataTO.setQuestionID("TestJUNT");
			questionWiseResponseTO.setQuestionID(questionMetaDataTO.getQuestionID());
			String aid1 = new StringBuilder("Q_").append(questionWiseResponseTO.getQuestionID()).append("_").append("ans1").toString();
			questionParamMap.replaceParam(aid1 + "_instanceid","T_1");
			questionParamMap.replaceParam(aid1 + "_state","T_2");
			questionParamMap.replaceParam(aid1 + "_mode","T_3");
			questionParamMap.replaceParam(aid1 + "_eval","T_4");
			String aid2 = new StringBuilder("Q_").append(questionWiseResponseTO.getQuestionID()).append("_").append("ans2").toString();
			questionParamMap.replaceParam(aid2 + "_instanceid","T_1");
			questionParamMap.replaceParam(aid2 + "_state","T_2");
			questionParamMap.replaceParam(aid2 + "_mode","T_3");
			questionParamMap.replaceParam(aid2 + "_eval","T_4");
			
			assignmentServiceUtil.removeExternalResponse(questionWiseResponseTO, questionMetaDataTO);
			questionParamMap = questionWiseResponseTO.getQuestionParameters();
			assertTrue(!StringUtils.isBlank(questionParamMap.getParam(aid1 + "_instanceid")) &&
					!StringUtils.isBlank(questionParamMap.getParam(aid1 + "_state")) &&
					!StringUtils.isBlank(questionParamMap.getParam(aid1 + "_mode")) &&
					!StringUtils.isBlank(questionParamMap.getParam(aid1+ "_eval"))
					);
			assertTrue(!StringUtils.isBlank(questionParamMap.getParam(aid2 + "_instanceid")) &&
					!StringUtils.isBlank(questionParamMap.getParam(aid2 + "_state")) &&
					!StringUtils.isBlank(questionParamMap.getParam(aid2 + "_mode")) &&
					!StringUtils.isBlank(questionParamMap.getParam(aid2+ "_eval"))
					);
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method tests that if the question type is DOC type or worksheet_anser_external then 
	 * it removes some values from its question parameter
	 * @see AssignmentServiceUtil#removeExternalResponse(QuestionWiseResponseTO, QuestionMetaDataTO)
	 * @throws Exception
	 */
	//@Test
	public void testRemoveExternalResponseCase5() throws Exception {
		QuestionMetaDataTO questionMetaDataTO = new QuestionMetaDataTO();
		QuestionWiseResponseTO questionWiseResponseTO = new QuestionWiseResponseTO();
		CustomMap<String,String> questionParamMap = questionWiseResponseTO.getQuestionParameters();
		List<Map<String,String>> answerList = new ArrayList<Map<String,String>>();
		Map<String,String> ans1 = new HashMap<String,String>();
		Map<String,String> ans2 = new HashMap<String,String>();
		try{
			ans1.put(QuestionMetadataUtil.TYPE,"EX");
			ans1.put(QuestionMetadataUtil.NAME,"ans1");
			ans1.put(worksheet_answer_external.IDENTIFIER,"JUNIT"+worksheet_answer_external.WIMBA_QUESTION);
			
			ans2.put(QuestionMetadataUtil.TYPE,"MC");
			ans2.put(QuestionMetadataUtil.NAME,"ans2");
			
			answerList.add(ans1);
			answerList.add(ans2);
			
			questionMetaDataTO.setAnswers(answerList);
			questionMetaDataTO.setQuestionTypeIdentifier(question.TYPEID_worksheet);
			questionMetaDataTO.setQuestionID("TestJUNT");
			questionWiseResponseTO.setQuestionID(questionMetaDataTO.getQuestionID());
			String aid1 = new StringBuilder("Q_").append(questionWiseResponseTO.getQuestionID()).append("_").append("ans1").toString();
			questionParamMap.replaceParam(aid1 + "_instanceid","T_1");
			questionParamMap.replaceParam(aid1 + "_state","T_2");
			questionParamMap.replaceParam(aid1 + "_mode","T_3");
			questionParamMap.replaceParam(aid1 + "_eval","T_4");
			String aid2 = new StringBuilder("Q_").append(questionWiseResponseTO.getQuestionID()).append("_").append("ans2").toString();
			questionParamMap.replaceParam(aid2 + "_instanceid","T_1");
			questionParamMap.replaceParam(aid2 + "_state","T_2");
			questionParamMap.replaceParam(aid2 + "_mode","T_3");
			questionParamMap.replaceParam(aid2 + "_eval","T_4");
			
			assignmentServiceUtil.removeExternalResponse(questionWiseResponseTO, questionMetaDataTO);
			questionParamMap = questionWiseResponseTO.getQuestionParameters();
			assertTrue(StringUtils.isBlank(questionParamMap.getParam(aid1 + "_instanceid")) &&
					StringUtils.isBlank(questionParamMap.getParam(aid1 + "_state")) &&
					StringUtils.isBlank(questionParamMap.getParam(aid1 + "_mode")) &&
					StringUtils.isBlank(questionParamMap.getParam(aid1+ "_eval"))
					);
			assertTrue(!StringUtils.isBlank(questionParamMap.getParam(aid2 + "_instanceid")) &&
					!StringUtils.isBlank(questionParamMap.getParam(aid2 + "_state")) &&
					!StringUtils.isBlank(questionParamMap.getParam(aid2 + "_mode")) &&
					!StringUtils.isBlank(questionParamMap.getParam(aid2+ "_eval"))
					);
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * This method tests that if a response for a question is not present , then it will return an new responseObject 
	 * for that question but if response present then it returns it; is working properly or not
	 * @see AssignmentServiceUtil #getQuestionwiseResponse(String, Map)   
	 * @throws Exception
	 */
	@Test
	public void TestGetQuestionwiseResponseCase1() throws Exception {
		String key = "";
		QuestionWiseResponseTO questionWiseResponseTO = new QuestionWiseResponseTO();
		Map<String,QuestionWiseResponseTO> questionwiseResponseMap = null;
		questionWiseResponseTO.setQuestionID("JQid");
		QuestionWiseResponseTO questionWiseResponseTOExpected = null;
		try{
			key = "Q_JQid";
			questionwiseResponseMap = new HashMap<String,QuestionWiseResponseTO>();
			questionwiseResponseMap.put("JQid", questionWiseResponseTO);
			questionWiseResponseTOExpected = assignmentServiceUtil.getQuestionwiseResponse(key, questionwiseResponseMap);
			assertTrue("JQid".equals(questionWiseResponseTOExpected.getQuestionID()));
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method tests that if a response for a question is not present , then it will return an new responseObject 
	 * for that question but if response present then it returns it; is working properly or not
	 * @see AssignmentServiceUtil #getQuestionwiseResponse(String, Map)   
	 * @throws Exception
	 */
	@Test
	public void TestGetQuestionwiseResponseCase2() throws Exception {
		String key = "";
		QuestionWiseResponseTO questionWiseResponseTO = new QuestionWiseResponseTO();
		Map<String,QuestionWiseResponseTO> questionwiseResponseMap = null;
		questionWiseResponseTO.setQuestionID("JQid");
		QuestionWiseResponseTO questionWiseResponseTOExpected = null;
		try{
			key = "Q_JQid";
			questionwiseResponseMap = new HashMap<String,QuestionWiseResponseTO>();
			questionWiseResponseTOExpected = assignmentServiceUtil.getQuestionwiseResponse(key, questionwiseResponseMap);
			_logger.info(" questionWiseResponseTOExpected "+questionWiseResponseTOExpected);
			assertTrue("JQid".equals(questionWiseResponseTOExpected.getQuestionID()) && questionWiseResponseTOExpected != null);
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method tests that if a response for a question is not present , then it will return an new responseObject 
	 * for that question but if response present then it returns it; is working properly or not
	 * @see AssignmentServiceUtil #getQuestionwiseResponse(String, Map)   
	 * @throws Exception
	 */
	@Test
	public void TestGetQuestionwiseResponseCase3() throws Exception {
		String key = "";
		QuestionWiseResponseTO questionWiseResponseTO = new QuestionWiseResponseTO();
		Map<String,QuestionWiseResponseTO> questionwiseResponseMap = null;
		questionWiseResponseTO.setQuestionID("JQid");
		QuestionWiseResponseTO questionWiseResponseTOExpected = null;
		try{
			questionwiseResponseMap = new HashMap<String,QuestionWiseResponseTO>();
			questionWiseResponseTOExpected = assignmentServiceUtil.getQuestionwiseResponse(key, questionwiseResponseMap);
			assertTrue( questionWiseResponseTOExpected == null);
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method tests that the value in the testParam is properly generated or not
	 * for LSI type question
	 * @see AssignmentServiceUtil #getModifiedValueForlsiQuestion(String, String, List)
	 * @throws Exception
	 */
	@Test
	public void TestGetModifiedValueForlsiQuestionCase1() throws Exception {
		List<QuestionMetaDataTO> questionMetaDataTOs = new ArrayList<QuestionMetaDataTO>();
		Map<String, String> questionProperties = new HashMap<String,String>();
		questionProperties.put(lsi.ITERATION_LEVEL, "1");
		QuestionMetaDataTO questionMetaDataTO = new QuestionMetaDataTO();
		questionMetaDataTO.setQuestionProperties(questionProperties);
		questionMetaDataTO.setQuestionID("JQid");
		questionMetaDataTOs.add(questionMetaDataTO);
		String key = "Q_JQid";
		String value = "ssssample"; 
		try{
			value = assignmentServiceUtil.getModifiedValueForlsiQuestion(key, value, questionMetaDataTOs);
			System.out.println(value);
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method tests that previous BOP related information is properly inserted in into current test parameter or not
	 * @see AssignmentServiceUtil #populateTestParamForPreviousBOPRelatedInfo(String, String, CustomMap, QuestionWiseResponseTO)
	 * @throws Exception
	 */
	@Test
	public void TestPopulateTestParamForPreviousBOPRelatedInfoCase1() throws Exception {
		String key = "Q_JQid_"+CaaConstants.FORMERCORRECT;
		String value = "true";
		CustomMap<String, String> testParamMap = new CustomMap<String,String>();
		QuestionWiseResponseTO questionWiseResponseTO = new QuestionWiseResponseTO();
		try{
			assignmentServiceUtil.populateTestParamForPreviousBOPRelatedInfo(key, value, testParamMap, questionWiseResponseTO);
			assertTrue(value.equals(testParamMap.getParam(key)));
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method tests that previous BOP related information is properly inserted in into current test parameter or not
	 * @see AssignmentServiceUtil #populateTestParamForPreviousBOPRelatedInfo(String, String, CustomMap, QuestionWiseResponseTO)
	 * @throws Exception
	 */
	@Test
	public void TestPopulateTestParamForPreviousBOPRelatedInfoCase2() throws Exception {
		String key = "P_JQid_"+CaaConstants.PAGECORRECT;
		String value = "true";
		CustomMap<String, String> testParamMap = new CustomMap<String,String>();
		QuestionWiseResponseTO questionWiseResponseTO = new QuestionWiseResponseTO();
		try{
			assignmentServiceUtil.populateTestParamForPreviousBOPRelatedInfo(key, value, testParamMap, questionWiseResponseTO);
			assertTrue(value.equals(testParamMap.getParam(key)));
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method tests that previous BOP related information is properly inserted in into current test parameter or not
	 * @see AssignmentServiceUtil #populateTestParamForPreviousBOPRelatedInfo(String, String, CustomMap, QuestionWiseResponseTO)
	 * @throws Exception
	 */
	@Test
	public void TestPopulateTestParamForPreviousBOPRelatedInfoCase3() throws Exception {
		String key = "P_JQid"+CaaConstants.RECORDER;
		String value = "test";
		CustomMap<String, String> testParamMap = new CustomMap<String,String>();
		QuestionWiseResponseTO questionWiseResponseTO = new QuestionWiseResponseTO();
		try{
			assignmentServiceUtil.populateTestParamForPreviousBOPRelatedInfo(key, value, testParamMap, questionWiseResponseTO);
			assertTrue(value.equals(testParamMap.getParam(key)));
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method tests that previous BOP related information is properly inserted in into current test parameter or not
	 * @see AssignmentServiceUtil #populateTestParamForPreviousBOPRelatedInfo(String, String, CustomMap, QuestionWiseResponseTO)
	 * @throws Exception
	 */
	@Test
	public void TestPopulateTestParamForPreviousBOPRelatedInfoCase4() throws Exception {
		String key = "JQid"+CaaConstants.RECORDER;
		String value = "test";
		CustomMap<String, String> testParamMap = new CustomMap<String,String>();
		QuestionWiseResponseTO questionWiseResponseTO = new QuestionWiseResponseTO();
		try{
			assignmentServiceUtil.populateTestParamForPreviousBOPRelatedInfo(key, value, testParamMap, questionWiseResponseTO);
			assertTrue(value.equals(testParamMap.getParam(key)));
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method tests that the unused & duplicate questions are removed from the question list
	 * successfully
	 * @see AssignmentServiceUtil #questionListSanityCheck(String, List)
	 * @throws Exception
	 */
	@Test
	public void TestQuestionListSanityCheckcase1() throws Exception {
		String questionList = "1;2;3;4;5;6,7,8;9;10";
		List<QuestionMetaDataTO> questionMetaDataTOs = new ArrayList<QuestionMetaDataTO>();
		String expectedList = "";
		try{
			for(int i = 0 ; i <= 9 ; i++){
				QuestionMetaDataTO questionMetaDataTO = new QuestionMetaDataTO();
				questionMetaDataTO.setQuestionID(Integer.toString(i));
				questionMetaDataTOs.add(questionMetaDataTO);
			}
			expectedList = assignmentServiceUtil.questionListSanityCheck(questionList, questionMetaDataTOs);
			_logger.info("Expected new QuestionLists is :: " + expectedList);
			assertTrue(!StringUtils.isBlank(expectedList) && !expectedList.contains("10"));
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method tests that the unused & duplicate questions are removed from the question list
	 * successfully
	 * @see AssignmentServiceUtil #questionListSanityCheck(String, List)
	 * @throws Exception
	 */
	@Test
	public void TestQuestionListSanityCheckcase2() throws Exception {
		String questionList = "1;2;3;4;5;6,7,8;9;10;11,12,13;14";
		List<QuestionMetaDataTO> questionMetaDataTOs = new ArrayList<QuestionMetaDataTO>();
		String expectedList = "";
		try{
			for(int i = 0 ; i <= 14 ; i++) {
				if(11 == i) {
					continue;
				}
				QuestionMetaDataTO questionMetaDataTO = new QuestionMetaDataTO();
				questionMetaDataTO.setQuestionID(Integer.toString(i));
				questionMetaDataTOs.add(questionMetaDataTO);
			}
			expectedList = assignmentServiceUtil.questionListSanityCheck(questionList, questionMetaDataTOs);
			_logger.info("Expected new QuestionLists is :: " + expectedList);
			assertTrue(!StringUtils.isBlank(expectedList) && !expectedList.contains("11"));
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method tests that the unused & duplicate questions are removed from the question list
	 * successfully
	 * @see AssignmentServiceUtil #questionListSanityCheck(String, List)
	 * @throws Exception
	 */
	@Test
	public void TestQuestionListSanityCheckcase3() throws Exception {
		String questionList = "1;2;3;4;5;6,7,8;9;10;11,12,13,13;14";
		List<QuestionMetaDataTO> questionMetaDataTOs = new ArrayList<QuestionMetaDataTO>();
		String expectedList = "";
		try{
			for(int i = 0 ; i <= 14 ; i++){
				QuestionMetaDataTO questionMetaDataTO = new QuestionMetaDataTO();
				questionMetaDataTO.setQuestionID(Integer.toString(i));
				questionMetaDataTOs.add(questionMetaDataTO);
			}
			expectedList = assignmentServiceUtil.questionListSanityCheck(questionList, questionMetaDataTOs);
			_logger.info("Expected new QuestionLists is :: " + expectedList);
			assertTrue(!StringUtils.isBlank(expectedList) && !expectedList.contains("13,13"));
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method tests that the unused & duplicate questions are removed from the question list
	 * successfully
	 * @see AssignmentServiceUtil #questionListSanityCheck(String, List)
	 * @throws Exception
	 */
	@Test
	public void TestQuestionListSanityCheckcase4() throws Exception {
		String questionList = "";
		List<QuestionMetaDataTO> questionMetaDataTOs = new ArrayList<QuestionMetaDataTO>();
		String expectedList = "";
		try{
			for(int i = 0 ; i <= 14 ; i++){
				QuestionMetaDataTO questionMetaDataTO = new QuestionMetaDataTO();
				questionMetaDataTO.setQuestionID(Integer.toString(i));
				questionMetaDataTOs.add(questionMetaDataTO);
			}
			expectedList = assignmentServiceUtil.questionListSanityCheck(questionList, questionMetaDataTOs);
			_logger.info("Expected new QuestionLists is :: " + expectedList);
			assertTrue(StringUtils.isBlank(expectedList));
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method checks that previous responses of a user is properly assigned with the current 
	 * testparamMap.Prevoius responses like previous attempt for a question is right or wrong or the page correct 
	 * information is integrated with the current response
	 * @see AssignmentServiceUtil #buildCurrentResponse(ResponseTO, ResponseTO, List, Map)
	 * @throws Exception
	 */
	//@Test
	public void TestBuildCurrentResponseCase1() throws Exception {
		ResponseTO currentResponseTO = null;
		ResponseTO prevResponseTO = null;
		QuestionWiseResponseTO response1 = null;
		QuestionWiseResponseTO response2 = null;
		List<QuestionMetaDataTO> questionList = new ArrayList<QuestionMetaDataTO>();
		Map<String,String> requestMap = null;
		Map<String,QuestionWiseResponseTO> responseMap = null;
		CustomMap<String,String> currentTestParamMap = null;
		TestTO testTO = null;
		try{
			requestMap = new RequestMap<String,String>();
			requestMap.put(CaaConstants.SUBMISSION_ID, "SUB_1");
			prevResponseTO = new ResponseTO();
			response1 = new QuestionWiseResponseTO();
			response2 = new QuestionWiseResponseTO();
			response1.setQuestionID("Q1");
			response1.setPoints(10000);
			response1.setPointsMax(10000);
			response2.setQuestionID("Q2");
			response2.setPoints(10000);
			response2.setPointsMax(10000);
			responseMap = prevResponseTO.getResponseMap();
			responseMap.put("Q1", response1);
			responseMap.put("Q2", response1);
			currentResponseTO = new ResponseTO();
			currentTestParamMap = new CustomMap<String,String>();
			currentTestParamMap.replaceParam(CaaConstants.QLIST, "Q1;Q2");
			currentResponseTO.setTestParameter(currentTestParamMap);
			for(int i = 1 ; i <= 2 ; i++){
				QuestionMetaDataTO questionMetaDataTO = new QuestionMetaDataTO();
				questionMetaDataTO.setQuestionID("Q"+ i);
				questionList.add(questionMetaDataTO);
			}
			testTO = new TestTO();
			testTO.setQuestionMetaDataList(questionList);
			assignmentServiceUtil.buildCurrentResponse(currentResponseTO, prevResponseTO, testTO, requestMap);
			assertTrue(CaaConstants.TRUE.equals(currentTestParamMap.getParam("Q_Q1"+CaaConstants.FORMERCORRECT)));
			assertTrue(CaaConstants.TRUE.equals(currentTestParamMap.getParam("Q_Q2"+CaaConstants.FORMERCORRECT)));
			assertTrue(CaaConstants.TRUE.equals(currentTestParamMap.getParam("Q_Q1_"+CaaConstants.PAGECORRECT)));
			assertTrue(CaaConstants.TRUE.equals(currentTestParamMap.getParam("Q_Q2_"+CaaConstants.PAGECORRECT)));
		}catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * This method checks that previous responses of a user is properly assigned with the current 
	 * testparamMap.Prevoius responses like previous attempt for a question is right or wrong or the page correct 
	 * information is integrated with the current response
	 * @see AssignmentServiceUtil #buildCurrentResponse(ResponseTO, ResponseTO, List, Map)
	 * @throws Exception
	 */
	//@Test
	public void TestBuildCurrentResponseCase2() throws Exception {
		ResponseTO currentResponseTO = null;
		ResponseTO prevResponseTO = null;
		QuestionWiseResponseTO response1 = null;
		QuestionWiseResponseTO response2 = null;
		QuestionWiseResponseTO response3 = null;
		QuestionWiseResponseTO response4 = null;
		QuestionWiseResponseTO response5 = null;
		QuestionWiseResponseTO response6 = null;
		QuestionWiseResponseTO response7 = null;
		List<QuestionMetaDataTO> questionList = new ArrayList<QuestionMetaDataTO>();
		Map<String,String> requestMap = null;
		Map<String,QuestionWiseResponseTO> responseMap = null;
		CustomMap<String,String> currentTestParamMap = null;
		TestTO testTO = null;
		try{
			requestMap = new RequestMap<String,String>();
			requestMap.put(CaaConstants.SUBMISSION_ID, "SUB_1");
			prevResponseTO = new ResponseTO();
			response1 = new QuestionWiseResponseTO();
			response2 = new QuestionWiseResponseTO();
			response3 = new QuestionWiseResponseTO();
			response4 = new QuestionWiseResponseTO();
			response5 = new QuestionWiseResponseTO();
			response6 = new QuestionWiseResponseTO();
			response7 = new QuestionWiseResponseTO();
			response1.setQuestionID("Q1");
			response1.setPoints(10000);
			response1.setPointsMax(10000);
			
			response2.setQuestionID("Q2");
			response2.setPoints(10000);
			response2.setPointsMax(10000);
			
			response3.setQuestionID("Q3");
			response3.setPoints(10000);
			response3.setPointsMax(10000);
			
			response4.setQuestionID("Q4");
			response4.setPoints(10000);
			response4.setPointsMax(10000);
			
			response5.setQuestionID("Q5");
			response5.setPoints(10000);
			response5.setPointsMax(10000);
			
			response6.setQuestionID("Q6");
			response6.setPoints(10000);
			response6.setPointsMax(10000);
			
			response7.setQuestionID("Q7");
			response7.setPoints(0);
			response7.setPointsMax(10000);
			responseMap = prevResponseTO.getResponseMap();
			responseMap.put("Q1", response1);
			responseMap.put("Q2", response1);
			responseMap.put("Q3", response3);
			responseMap.put("Q4", response4);
			responseMap.put("Q5", response5);
			responseMap.put("Q6", response6);
			responseMap.put("Q7", response7);
			currentResponseTO = new ResponseTO();
			currentTestParamMap = new CustomMap<String,String>();
			currentTestParamMap.replaceParam(CaaConstants.QLIST, "Q1;Q2;Q3;Q4,Q5,Q6,Q7");
			currentResponseTO.setTestParameter(currentTestParamMap);
			for(int i = 1 ; i <= 7 ; i++){
				QuestionMetaDataTO questionMetaDataTO = new QuestionMetaDataTO();
				questionMetaDataTO.setQuestionID("Q"+ i);
				questionList.add(questionMetaDataTO);
			}
			testTO = new TestTO();
			testTO.setQuestionMetaDataList(questionList);
			assignmentServiceUtil.buildCurrentResponse(currentResponseTO, prevResponseTO, testTO, requestMap);
			assertTrue(CaaConstants.TRUE.equals(currentTestParamMap.getParam("Q_Q1"+CaaConstants.FORMERCORRECT)));
			assertTrue(CaaConstants.TRUE.equals(currentTestParamMap.getParam("Q_Q2"+CaaConstants.FORMERCORRECT)));
			assertTrue(CaaConstants.TRUE.equals(currentTestParamMap.getParam("Q_Q1_"+CaaConstants.PAGECORRECT)));
			assertTrue(CaaConstants.TRUE.equals(currentTestParamMap.getParam("Q_Q2_"+CaaConstants.PAGECORRECT)));
			assertTrue(CaaConstants.FALSE.equals(currentTestParamMap.getParam("Q_Q4_"+CaaConstants.PAGECORRECT)));
			
		}catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * This method checks that previous responses of a user is properly assigned with the current 
	 * testparamMap.Prevoius responses like previous attempt for a question is right or wrong or the page correct 
	 * information is integrated with the current response
	 * @see AssignmentServiceUtil #buildCurrentResponse(ResponseTO, ResponseTO, List, Map)
	 * @throws Exception
	 */
	@Test
	public void TestBuildCurrentResponseCase3() throws Exception {
		ResponseTO currentResponseTO = null;
		ResponseTO prevResponseTO = null;
		QuestionWiseResponseTO response1 = null;
		QuestionWiseResponseTO response2 = null;
		QuestionWiseResponseTO response3 = null;
		QuestionWiseResponseTO response4 = null;
		QuestionWiseResponseTO response5 = null;
		QuestionWiseResponseTO response6 = null;
		QuestionWiseResponseTO response7 = null;
		List<QuestionMetaDataTO> questionList = new ArrayList<QuestionMetaDataTO>();
		Map<String,String> requestMap = null;
		Map<String,QuestionWiseResponseTO> responseMap = null;
		CustomMap<String,String> currentTestParamMap = null;
		TestTO testTO = null;
		try{
			requestMap = new RequestMap<String,String>();
			requestMap.put(CaaConstants.SUBMISSION_ID, "SUB_1");
			prevResponseTO = new ResponseTO();
			response1 = new QuestionWiseResponseTO();
			response2 = new QuestionWiseResponseTO();
			response3 = new QuestionWiseResponseTO();
			response4 = new QuestionWiseResponseTO();
			response5 = new QuestionWiseResponseTO();
			response6 = new QuestionWiseResponseTO();
			response7 = new QuestionWiseResponseTO();
			response1.setQuestionID("Q1");
			response1.setPoints(10000);
			response1.setPointsMax(10000);
			
			response2.setQuestionID("Q2");
			response2.setPoints(10000);
			response2.setPointsMax(10000);
			
			response3.setQuestionID("Q3");
			response3.setPoints(10000);
			response3.setPointsMax(10000);
			
			response4.setQuestionID("Q4");
			response4.setPoints(10000);
			response4.setPointsMax(10000);
			
			response5.setQuestionID("Q5");
			response5.setPoints(10000);
			response5.setPointsMax(10000);
			
			response6.setQuestionID("Q6");
			response6.setPoints(10000);
			response6.setPointsMax(10000);
			
			response7.setQuestionID("Q7");
			response7.setPoints(0);
			response7.setPointsMax(10000);
			responseMap = prevResponseTO.getResponseMap();
			responseMap.put("Q1", response1);
			responseMap.put("Q2", response1);
			responseMap.put("Q3", response3);
			responseMap.put("Q4", response4);
			responseMap.put("Q5", response5);
			responseMap.put("Q6", response6);
			responseMap.put("Q7", response7);
			currentResponseTO = new ResponseTO();
			currentTestParamMap = new CustomMap<String,String>();
			currentTestParamMap.replaceParam(CaaConstants.QLIST, "Q1;Q2;Q3;Q4,Q5,Q6,Q7");
			currentResponseTO.setTestParameter(currentTestParamMap);
			for(int i = 1 ; i <= 7 ; i++){
				QuestionMetaDataTO questionMetaDataTO = new QuestionMetaDataTO();
				questionMetaDataTO.setQuestionID("Q"+ i);
				questionList.add(questionMetaDataTO);
			}
			testTO = new TestTO();
			QuestionMetaDataTO questionMetaDataTO = new QuestionMetaDataTO();
			question q = new question();
			testTO.setQuestionMetaDataList(questionList);
			Mockito.doNothing().when(policyUtilMock).populatePolicies(Mockito.any(CustomMap.class),Mockito.any(Map.class));
			Mockito.doReturn(questionMetaDataTO).when(itemServiceMock).getQuesionMetaInformationFromTest(Mockito.any(List.class),Mockito.any(String.class));
			Mockito.doReturn(q).when(lightWeightQuestionFactoryMock).getLightWeightQuestion(Mockito.any(String.class));
			assignmentServiceUtilMock.buildCurrentResponse(currentResponseTO, prevResponseTO, testTO, requestMap);
			assertTrue(CaaConstants.TRUE.equals(currentTestParamMap.getParam("Q_Q1"+CaaConstants.FORMERCORRECT)));
			assertTrue(CaaConstants.TRUE.equals(currentTestParamMap.getParam("Q_Q2"+CaaConstants.FORMERCORRECT)));
			assertTrue(CaaConstants.TRUE.equals(currentTestParamMap.getParam("Q_Q1_"+CaaConstants.PAGECORRECT)));
			assertTrue(CaaConstants.TRUE.equals(currentTestParamMap.getParam("Q_Q2_"+CaaConstants.PAGECORRECT)));
			assertTrue(CaaConstants.FALSE.equals(currentTestParamMap.getParam("Q_Q4_"+CaaConstants.PAGECORRECT)));
			
		}catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * This method tests whether {@link AssignmentServiceUtil#getDecodedMap(String)}
	 * returns eaidMap when valid eaid is provided.
	 * @throws Exception
	 */
	@Test
	public void testGetDecodedMap1() throws Exception{
		EaidTO eaidto = new EaidTO();
		eaidto.setAssignmentId("123456789");
		eaidto.setAat("AAT");
		eaidto.setAttemptPk("987654321");
		eaidto.setMode("mode");
		eaidto.setRole("student");
		String eaid = securityUtil.generateEAID(eaidto);
		Map<String,String> eaidMap = assignmentServiceUtil.getDecodedMap(eaid);
		assertTrue(eaidMap.size() == 5);
	}
	
	/**
	 * This method tests whether {@link AssignmentServiceUtil#getDecodedMap(String)}
	 * returns exception when invalid eaid is provided.
	 * @throws Exception
	 */
	@Test
	public void testGetDecodedMap2() throws Exception{
		String eaid = "12234";
		try{
			assignmentServiceUtil.getDecodedMap(eaid);
			assertFalse(true);
		}catch(BusinessException be){
			assertTrue("Invalid EAID".equals(be.getMessage().trim()));
		}
	}
	
	/**
	 * This method tests whether {@link AssignmentServiceUtil#updateURLInformation(Map, ResponseTO)
	 * returns valid map when valid responseTO and requestMap is passed.
	 * @throws Exception
	 */
	@Test
	public void testUpdateURLInformation1() throws Exception{
		Map<String,String> requestMap = new HashMap<String,String>();
		requestMap.put(classware_hm.REFERER, "referer");
		requestMap.put(classware_hm.CW_SERVER, "CW_SERVER");
		requestMap.put(classware_hm.EZTO_INTERNAL, "EZTO_INTERNAL");
		requestMap.put(classware_hm.EZTO_EXTERNAL, "EZTO_EXTERNAL");
		CustomMap<String,String> testParameter = new CustomMap<String,String>();
		ResponseTO responseTO = new ResponseTO();
		responseTO.setTestParameter(testParameter);
		assignmentServiceUtil.updateURLInformation(requestMap, responseTO);
		assertTrue(testParameter.size() == 4);
	}
	
	/**
	 * This method tests whether {@link AssignmentServiceUtil#updateURLInformation(Map, ResponseTO)
	 * returns empty map when valid responseTO and null requestMap is passed.
	 * @throws Exception
	 */
	@Test
	public void testUpdateURLInformation2() throws Exception{
		Map<String,String> requestMap = null;
		CustomMap<String,String> testParameter = new CustomMap<String,String>();
		ResponseTO responseTO = new ResponseTO();
		responseTO.setTestParameter(testParameter);
		assignmentServiceUtil.updateURLInformation(requestMap, responseTO);
		assertTrue(testParameter.size() == 0);
	}
	
	
	/**
	 * This method tests that the pages information in AJSON is populated properly or not
	 * @see AssignmentServiceUtil#getQuestionPagesInfoJSON(List, Map)
	 * @throws Exception
	 */
	@Test
	public void TestGetQuestionPagesInfoJSONCase1() throws Exception {
		Map<String,String> eaidMap = null;
		question SBKquestionMock = new sectionBreak();
		SBKquestionMock.referenceTag = "1234";
		SBKquestionMock.questionProperties = richProperties.newInstance(question.MP_CUSTOM_PRGRESSION);
		SBKquestionMock.questionProperties.setString(question.MP_CUSTOM_PRGRESSION, question.MP_CUSTOM_PRGRESSION_ONEWAY);
		question questionMockOther = new question();
		String testID = "T1";
		String attemptPK = "A1";
		JSONArray jsonArray = null;
		String qList = "1;2,3;4";
		CustomMap<String, String> testparamMap = new CustomMap<String,String>();
		Map<String,String> policyMap = new HashMap<String,String>();
		PolicyTO policyTO = new PolicyTO();
		policyTO.setPolicyMap(policyMap);
		ResponseTO responseTO = new ResponseTO();
		UserResponseWithPolicyTO userResponseWithPolicyTO = new UserResponseWithPolicyTO();
		userResponseWithPolicyTO.setPolicyTO(policyTO);
		try{
			testparamMap.replaceParam(CaaConstants.QLIST, qList);
			responseTO.setTestParameter(testparamMap);
			eaidMap = new HashMap<String,String>();
			eaidMap.put("assignmentid", testID);
			eaidMap.put("attemptpk", attemptPK);
			eaidMap.put("mode", UserResponseService.REVIEW_MODE);

			Mockito.when(cacheServiceMock.getItem("1")).thenReturn(questionMockOther);
			Mockito.when(cacheServiceMock.getItem("2")).thenReturn(SBKquestionMock);
			Mockito.when(cacheServiceMock.getItem("3")).thenReturn(questionMockOther);
			Mockito.when(cacheServiceMock.getItem("4")).thenReturn(questionMockOther);
			
			Mockito.when(responseUtilMock.isQuestionResponseLockedFBQ(responseTO, "1")).thenReturn(true);
			Mockito.when(responseUtilMock.isQuestionResponseLockedFBQ(responseTO, "2")).thenReturn(true);
			Mockito.when(responseUtilMock.isQuestionResponseLockedFBQ(responseTO, "3")).thenReturn(true);
			Mockito.when(responseUtilMock.isQuestionResponseLockedFBQ(responseTO, "4")).thenReturn(true);
			
			EridTO eridTO1  = securityUtil.generateEridTO(testID, attemptPK, "1", UserResponseService.REVIEW_MODE);
			EridTO eridTO2  = securityUtil.generateEridTO(testID, attemptPK, "2", UserResponseService.REVIEW_MODE);
			EridTO eridTO3  = securityUtil.generateEridTO(testID, attemptPK, "3", UserResponseService.REVIEW_MODE);
			EridTO eridTO4  = securityUtil.generateEridTO(testID, attemptPK, "4", UserResponseService.REVIEW_MODE);
			
			Mockito.doReturn(eridTO1).when(securityUtil).generateEridTO(testID, attemptPK, "1", UserResponseService.REVIEW_MODE);
			Mockito.doReturn(eridTO2).when(securityUtil).generateEridTO(testID, attemptPK, "2", UserResponseService.REVIEW_MODE);
			Mockito.doReturn(eridTO3).when(securityUtil).generateEridTO(testID, attemptPK, "3", UserResponseService.REVIEW_MODE);
			Mockito.doReturn(eridTO4).when(securityUtil).generateEridTO(testID, attemptPK, "4", UserResponseService.REVIEW_MODE);
			
			Mockito.when(securityServiceMock.getERID(eridTO1)).thenReturn("ERID1");
			Mockito.when(securityServiceMock.getERID(eridTO2)).thenReturn("ERID2");
			Mockito.when(securityServiceMock.getERID(eridTO3)).thenReturn("ERID3");
			Mockito.when(securityServiceMock.getERID(eridTO4)).thenReturn("ERID4");
			
			userResponseWithPolicyTO.setResponseTO(responseTO);
	    	jsonArray = assignmentServiceUtilMock.getQuestionPagesInfoJSON(eaidMap, userResponseWithPolicyTO);
	    	
	    	_logger.info("jsonArray :: " +jsonArray);
	    	JSONObject jsonObject0 = jsonArray.getJSONObject(0);
	    	JSONObject jsonObject1 = jsonArray.getJSONObject(1);
	    	JSONObject jsonObject2 = jsonArray.getJSONObject(2);

	    	assertTrue(jsonObject0.getString("ptitle").contains("Question 1"));
	    	assertTrue(jsonObject0.getString("erids").contains("ERID1"));
	    	assertTrue(jsonObject0.getString("workflow").contains("normal"));
	    	assertTrue(jsonObject0.getString(CaaConstants.WORKFLOWSTATE).contains("0"));
	    	assertTrue(jsonObject0.getString(CaaConstants.FBQ).equals(CaaConstants.UNREQUESTED));
	    	
	    	assertTrue(jsonObject1.getString("ptitle").contains("Question 2 - 2"));
	    	assertTrue(jsonObject1.getString("erids").contains("ERID2"));
	    	assertTrue(jsonObject1.getString("erids").contains("ERID3"));
	    	assertTrue(jsonObject1.getString("workflow").contains("oneway"));
	    	assertTrue(jsonObject1.getString(CaaConstants.WORKFLOWSTATE).equals("0"));
	    	assertTrue(jsonObject1.getString(CaaConstants.FBQ).equals(CaaConstants.UNREQUESTED));
	    	
	    	assertTrue(jsonObject2.getString("ptitle").contains("Question 3"));
	    	assertTrue(jsonObject2.getString("erids").contains("ERID4"));
	    	assertTrue(jsonObject2.getString("workflow").contains("normal"));
	    	assertTrue(jsonObject2.getString(CaaConstants.WORKFLOWSTATE).equals("0"));
	    	assertTrue(jsonObject2.getString(CaaConstants.FBQ).equals(CaaConstants.UNREQUESTED));
	    	
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method tests that the pages information in AJSON is populated properly or not
	 * @see AssignmentServiceUtil#getQuestionPagesInfoJSON(List, Map)
	 * @throws Exception
	 */
	@Test
	public void TestGetQuestionPagesInfoJSONCase2() throws Exception {
		Map<String,String> eaidMap = null;
		question questionMock = new question();
		questionMock.questionProperties = richProperties.newInstance(question.MP_CUSTOM_PRGRESSION);
		String testID = "T1";
		String attemptPK = "A1";
		JSONArray jsonArray = null;
		String qList = "1;2;3;4";
		CustomMap<String, String> testparamMap = new CustomMap<String,String>();
		Map<String,String> policyMap = new HashMap<String,String>();
		PolicyTO policyTO = new PolicyTO();
		policyTO.setPolicyMap(policyMap);
		ResponseTO responseTO = new ResponseTO();
		UserResponseWithPolicyTO userResponseWithPolicyTO = new UserResponseWithPolicyTO();
		userResponseWithPolicyTO.setPolicyTO(policyTO);
		try{
			testparamMap.replaceParam(classware_hm.POLICY_feedback , "yes");
			testparamMap.replaceParam(CaaConstants.QLIST, qList);
			responseTO.setTestParameter(testparamMap);
			
			eaidMap = new HashMap<String,String>();
			eaidMap.put("assignmentid", testID);
			eaidMap.put("attemptpk", attemptPK);
			eaidMap.put("mode", UserResponseService.REVIEW_MODE);

			
			Mockito.when(cacheServiceMock.getItem("1")).thenReturn(questionMock);
			Mockito.when(cacheServiceMock.getItem("2")).thenReturn(questionMock);
			Mockito.when(cacheServiceMock.getItem("3")).thenReturn(questionMock);
			Mockito.when(cacheServiceMock.getItem("4")).thenReturn(questionMock);
			
			EridTO eridTO1  = securityUtil.generateEridTO(testID, attemptPK, "1", UserResponseService.REVIEW_MODE);
			EridTO eridTO2  = securityUtil.generateEridTO(testID, attemptPK, "2", UserResponseService.REVIEW_MODE);
			EridTO eridTO3  = securityUtil.generateEridTO(testID, attemptPK, "3", UserResponseService.REVIEW_MODE);
			EridTO eridTO4  = securityUtil.generateEridTO(testID, attemptPK, "4", UserResponseService.REVIEW_MODE);
			
			Mockito.doReturn(eridTO1).when(securityUtil).generateEridTO(testID, attemptPK, "1", UserResponseService.REVIEW_MODE);
			Mockito.doReturn(eridTO2).when(securityUtil).generateEridTO(testID, attemptPK, "2", UserResponseService.REVIEW_MODE);
			Mockito.doReturn(eridTO3).when(securityUtil).generateEridTO(testID, attemptPK, "3", UserResponseService.REVIEW_MODE);
			Mockito.doReturn(eridTO4).when(securityUtil).generateEridTO(testID, attemptPK, "4", UserResponseService.REVIEW_MODE);
			
			Mockito.when(securityServiceMock.getERID(eridTO1)).thenReturn("ERID1");
			Mockito.when(securityServiceMock.getERID(eridTO2)).thenReturn("ERID2");
			Mockito.when(securityServiceMock.getERID(eridTO3)).thenReturn("ERID3");
			Mockito.when(securityServiceMock.getERID(eridTO4)).thenReturn("ERID4");
			
			Mockito.when(responseUtilMock.isQuestionResponseLockedFBQ(responseTO, "1")).thenReturn(true);
			Mockito.when(responseUtilMock.isQuestionResponseLockedFBQ(responseTO, "2")).thenReturn(true);
			Mockito.when(responseUtilMock.isQuestionResponseLockedFBQ(responseTO, "3")).thenReturn(true);
			Mockito.when(responseUtilMock.isQuestionResponseLockedFBQ(responseTO, "4")).thenReturn(false);
			
			userResponseWithPolicyTO.setResponseTO(responseTO);
	    	jsonArray = assignmentServiceUtilMock.getQuestionPagesInfoJSON(eaidMap, userResponseWithPolicyTO);
	    	
	    	_logger.info("jsonArray :: " +jsonArray);
	    	
	    	JSONObject jsonObject0 = jsonArray.getJSONObject(0);
	    	JSONObject jsonObject1 = jsonArray.getJSONObject(1);
	    	JSONObject jsonObject2 = jsonArray.getJSONObject(2);
	    	JSONObject jsonObject3 = jsonArray.getJSONObject(3);
	    	
	    	assertTrue(jsonObject0.getString("ptitle").contains("Question 1"));
	    	assertTrue(jsonObject0.getString("erids").contains("ERID1"));
	    	assertTrue(jsonObject0.getString(CaaConstants.WORKFLOW).contains("normal"));
	    	assertTrue(jsonObject0.getString(CaaConstants.WORKFLOWSTATE).contains("0"));
	    	assertTrue(jsonObject0.getString(CaaConstants.FBQ).contains(CaaConstants.REQUESTED));
	    	
	    	assertTrue(jsonObject1.getString("ptitle").contains("Question 2"));
	    	assertTrue(jsonObject1.getString("erids").contains("ERID2"));
	    	assertTrue(jsonObject1.getString(CaaConstants.WORKFLOW).contains("normal"));
	    	assertTrue(jsonObject1.getString(CaaConstants.WORKFLOWSTATE).contains("0"));
	    	assertTrue(jsonObject1.getString(CaaConstants.FBQ).contains(CaaConstants.REQUESTED));
	    	
	    	assertTrue(jsonObject2.getString("ptitle").contains("Question 3"));
	    	assertTrue(jsonObject2.getString("erids").contains("ERID3"));
	    	assertTrue(jsonObject2.getString(CaaConstants.WORKFLOW).contains("normal"));
	    	assertTrue(jsonObject2.getString(CaaConstants.WORKFLOWSTATE).contains("0"));
	    	assertTrue(jsonObject2.getString(CaaConstants.FBQ).contains(CaaConstants.REQUESTED));
	    	
	    	assertTrue(jsonObject3.getString("ptitle").contains("Question 4"));
	    	assertTrue(jsonObject3.getString("erids").contains("ERID4"));
	    	assertTrue(jsonObject3.getString(CaaConstants.WORKFLOW).contains("normal"));
	    	assertTrue(jsonObject3.getString(CaaConstants.WORKFLOWSTATE).contains("0"));
	    	assertTrue(jsonObject3.getString(CaaConstants.FBQ).contains(CaaConstants.UNREQUESTED));
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method tests that the pages information in AJSON is populated properly or not
	 * @see AssignmentServiceUtil#getQuestionPagesInfoJSON(List, Map)
	 * @throws Exception
	 */
	@Test
	public void TestGetQuestionPagesInfoJSONCase3() throws Exception {
		Map<String,String> eaidMap = null;
		question questionMock = new sectionBreak();
		questionMock.referenceTag = "1234";
		questionMock.questionProperties = richProperties.newInstance(question.MP_CUSTOM_PRGRESSION);
		questionMock.questionProperties.setString(question.MP_CUSTOM_PRGRESSION, question.MP_CUSTOM_PRGRESSION_ONEWAY);
		question questionMockOthers = new question();
		String testID = "T1";
		String attemptPK = "A1";
		JSONArray jsonArray = null;
		String qList = "1;2;3,4;5,6";
		CustomMap<String, String> testparamMap = new CustomMap<String,String>();
		Map<String,String> policyMap = new HashMap<String,String>();
		PolicyTO policyTO = new PolicyTO();
		policyTO.setPolicyMap(policyMap);
		ResponseTO responseTO = new ResponseTO();
		UserResponseWithPolicyTO userResponseWithPolicyTO = new UserResponseWithPolicyTO();
		userResponseWithPolicyTO.setPolicyTO(policyTO);
		try{
			testparamMap.replaceParam(classware_hm.POLICY_feedback , "yes");
			testparamMap.replaceParam(CaaConstants.QLIST, qList);
			responseTO.setTestParameter(testparamMap);
			
			eaidMap = new HashMap<String,String>();
			eaidMap.put("assignmentid", testID);
			eaidMap.put("attemptpk", attemptPK);
			eaidMap.put("mode", UserResponseService.REVIEW_MODE);

			Mockito.when(cacheServiceMock.getItem("1")).thenReturn(questionMockOthers);
			Mockito.when(cacheServiceMock.getItem("2")).thenReturn(questionMockOthers);
			Mockito.when(cacheServiceMock.getItem("3")).thenReturn(questionMock);
			Mockito.when(cacheServiceMock.getItem("4")).thenReturn(questionMockOthers);
			Mockito.when(cacheServiceMock.getItem("5")).thenReturn(questionMock);
			Mockito.when(cacheServiceMock.getItem("6")).thenReturn(questionMockOthers);
			
			EridTO eridTO1  = securityUtil.generateEridTO(testID, attemptPK, "1", UserResponseService.REVIEW_MODE);
			EridTO eridTO2  = securityUtil.generateEridTO(testID, attemptPK, "2", UserResponseService.REVIEW_MODE);
			EridTO eridTO3  = securityUtil.generateEridTO(testID, attemptPK, "3", UserResponseService.REVIEW_MODE);
			EridTO eridTO4  = securityUtil.generateEridTO(testID, attemptPK, "4", UserResponseService.REVIEW_MODE);
			EridTO eridTO5  = securityUtil.generateEridTO(testID, attemptPK, "5", UserResponseService.REVIEW_MODE);
			EridTO eridTO6  = securityUtil.generateEridTO(testID, attemptPK, "6", UserResponseService.REVIEW_MODE);
			
			Mockito.doReturn(eridTO1).when(securityUtil).generateEridTO(testID, attemptPK, "1", UserResponseService.REVIEW_MODE);
			Mockito.doReturn(eridTO2).when(securityUtil).generateEridTO(testID, attemptPK, "2", UserResponseService.REVIEW_MODE);
			Mockito.doReturn(eridTO3).when(securityUtil).generateEridTO(testID, attemptPK, "3", UserResponseService.REVIEW_MODE);
			Mockito.doReturn(eridTO4).when(securityUtil).generateEridTO(testID, attemptPK, "4", UserResponseService.REVIEW_MODE);
			Mockito.doReturn(eridTO5).when(securityUtil).generateEridTO(testID, attemptPK, "5", UserResponseService.REVIEW_MODE);
			Mockito.doReturn(eridTO6).when(securityUtil).generateEridTO(testID, attemptPK, "6", UserResponseService.REVIEW_MODE);
			
			Mockito.when(securityServiceMock.getERID(eridTO1)).thenReturn("ERID1");
			Mockito.when(securityServiceMock.getERID(eridTO2)).thenReturn("ERID2");
			Mockito.when(securityServiceMock.getERID(eridTO3)).thenReturn("ERID3");
			Mockito.when(securityServiceMock.getERID(eridTO4)).thenReturn("ERID4");
			Mockito.when(securityServiceMock.getERID(eridTO5)).thenReturn("ERID5");
			Mockito.when(securityServiceMock.getERID(eridTO6)).thenReturn("ERID6");
			
			Mockito.when(responseUtilMock.isQuestionResponseLockedFBQ(responseTO, "1")).thenReturn(true);
			Mockito.when(responseUtilMock.isQuestionResponseLockedFBQ(responseTO, "2")).thenReturn(true);
			Mockito.when(responseUtilMock.isQuestionResponseLockedFBQ(responseTO, "3")).thenReturn(true);
			Mockito.when(responseUtilMock.isQuestionResponseLockedFBQ(responseTO, "4")).thenReturn(true);
			Mockito.when(responseUtilMock.isQuestionResponseLockedFBQ(responseTO, "5")).thenReturn(true);
			Mockito.when(responseUtilMock.isQuestionResponseLockedFBQ(responseTO, "6")).thenReturn(true);
			
			userResponseWithPolicyTO.setResponseTO(responseTO);
	    	jsonArray = assignmentServiceUtilMock.getQuestionPagesInfoJSON(eaidMap, userResponseWithPolicyTO);
	    	
	    	_logger.info("jsonArray :: " +jsonArray);
	    	
	    	JSONObject jsonObject0 = jsonArray.getJSONObject(0);
	    	JSONObject jsonObject1 = jsonArray.getJSONObject(1);
	    	JSONObject jsonObject2 = jsonArray.getJSONObject(2);
	    	JSONObject jsonObject3 = jsonArray.getJSONObject(3);
	    	
	    	
	    	
	    	assertTrue(jsonObject0.getString("ptitle").contains("Question 1"));
	    	assertTrue(jsonObject0.getString("erids").contains("ERID1"));
	    	assertTrue(jsonObject0.getString("workflow").contains("normal"));
	    	assertTrue(jsonObject0.getString(CaaConstants.WORKFLOWSTATE).contains("0"));
	    	assertTrue(jsonObject0.getString(CaaConstants.FBQ).contains(CaaConstants.REQUESTED));
	    	
	    	assertTrue(jsonObject1.getString("ptitle").contains("Question 2"));
	    	assertTrue(jsonObject1.getString("erids").contains("ERID2"));
	    	assertTrue(jsonObject1.getString("workflow").contains("normal"));
	    	assertTrue(jsonObject1.getString(CaaConstants.WORKFLOWSTATE).contains("0"));
	    	assertTrue(jsonObject1.getString(CaaConstants.FBQ).contains(CaaConstants.REQUESTED));
	    	
	    	assertTrue(jsonObject2.getString("ptitle").contains("Question 3 - 3"));
	    	assertTrue(jsonObject2.getString("erids").contains("ERID3"));
	    	assertTrue(jsonObject2.getString("erids").contains("ERID4"));
	    	assertTrue(jsonObject2.getString("workflow").contains(question.MP_CUSTOM_PRGRESSION_ONEWAY));
	    	assertTrue(jsonObject2.getString(CaaConstants.WORKFLOWSTATE).contains(""));
	    	assertTrue(jsonObject2.getString(CaaConstants.FBQ).contains(CaaConstants.REQUESTED));
	    	
	    	assertTrue(jsonObject3.getString("ptitle").contains("Question 4 - 4"));
	    	assertTrue(jsonObject3.getString("erids").contains("ERID5"));
	    	assertTrue(jsonObject3.getString("erids").contains("ERID6"));
	    	assertTrue(jsonObject3.getString("workflow").contains(question.MP_CUSTOM_PRGRESSION_ONEWAY));
	    	assertTrue(jsonObject3.getString(CaaConstants.WORKFLOWSTATE).contains(""));
	    	assertTrue(jsonObject3.getString(CaaConstants.FBQ).contains(CaaConstants.REQUESTED));
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method tests that the pages information in AJSON is populated properly or not
	 * @see AssignmentServiceUtil#getQuestionPagesInfoJSON(List, Map)
	 * @throws Exception
	 */
	@Test
	public void TestGetQuestionPagesInfoJSONCase4() throws Exception {
		Map<String,String> eaidMap = null;
		question questionMock = new sectionBreak();
		questionMock.referenceTag = "1234";
		questionMock.questionProperties = richProperties.newInstance(question.MP_CUSTOM_PRGRESSION);
		questionMock.questionProperties.setString(question.MP_CUSTOM_PRGRESSION, question.MP_CUSTOM_PRGRESSION_ONEWAY);
		question questionMockOthers = new question();
		String testID = "T1";
		String attemptPK = "A1";
		JSONArray jsonArray = null;
		String qList = "1;2;3,4;5,6";
		CustomMap<String, String> testparamMap = new CustomMap<String,String>();
		Map<String,String> policyMap = new HashMap<String,String>();
		PolicyTO policyTO = new PolicyTO();
		policyTO.setPolicyMap(policyMap);
		ResponseTO responseTO = new ResponseTO();
		UserResponseWithPolicyTO userResponseWithPolicyTO = new UserResponseWithPolicyTO();
		userResponseWithPolicyTO.setPolicyTO(policyTO);
		try{
			testparamMap.replaceParam(classware_hm.POLICY_feedback , "yes");
			testparamMap.replaceParam(CaaConstants.QLIST, qList);
			responseTO.setTestParameter(testparamMap);
			
			eaidMap = new HashMap<String,String>();
			eaidMap.put("assignmentid", testID);
			eaidMap.put("attemptpk", attemptPK);
			eaidMap.put("mode", UserResponseService.REVIEW_MODE);

			Mockito.when(cacheServiceMock.getItem("1")).thenReturn(questionMockOthers);
			Mockito.when(cacheServiceMock.getItem("2")).thenReturn(questionMockOthers);
			Mockito.when(cacheServiceMock.getItem("3")).thenReturn(questionMock);
			Mockito.when(cacheServiceMock.getItem("5")).thenReturn(questionMock);
			
			EridTO eridTO1  = securityUtil.generateEridTO(testID, attemptPK, "1", UserResponseService.REVIEW_MODE);
			EridTO eridTO2  = securityUtil.generateEridTO(testID, attemptPK, "2", UserResponseService.REVIEW_MODE);
			EridTO eridTO3  = securityUtil.generateEridTO(testID, attemptPK, "3", UserResponseService.REVIEW_MODE);
			EridTO eridTO4  = securityUtil.generateEridTO(testID, attemptPK, "4", UserResponseService.REVIEW_MODE);
			EridTO eridTO5  = securityUtil.generateEridTO(testID, attemptPK, "5", UserResponseService.REVIEW_MODE);
			EridTO eridTO6  = securityUtil.generateEridTO(testID, attemptPK, "6", UserResponseService.REVIEW_MODE);
			
			Mockito.doReturn(eridTO1).when(securityUtil).generateEridTO(testID, attemptPK, "1", UserResponseService.REVIEW_MODE);
			Mockito.doReturn(eridTO2).when(securityUtil).generateEridTO(testID, attemptPK, "2", UserResponseService.REVIEW_MODE);
			Mockito.doReturn(eridTO3).when(securityUtil).generateEridTO(testID, attemptPK, "3", UserResponseService.REVIEW_MODE);
			Mockito.doReturn(eridTO4).when(securityUtil).generateEridTO(testID, attemptPK, "4", UserResponseService.REVIEW_MODE);
			Mockito.doReturn(eridTO5).when(securityUtil).generateEridTO(testID, attemptPK, "5", UserResponseService.REVIEW_MODE);
			Mockito.doReturn(eridTO6).when(securityUtil).generateEridTO(testID, attemptPK, "6", UserResponseService.REVIEW_MODE);
			
			Mockito.when(securityServiceMock.getERID(eridTO1)).thenReturn("ERID1");
			Mockito.when(securityServiceMock.getERID(eridTO2)).thenReturn("ERID2");
			Mockito.when(securityServiceMock.getERID(eridTO3)).thenReturn("ERID3");
			Mockito.when(securityServiceMock.getERID(eridTO4)).thenReturn("ERID4");
			Mockito.when(securityServiceMock.getERID(eridTO5)).thenReturn("ERID5");
			Mockito.when(securityServiceMock.getERID(eridTO6)).thenReturn("ERID6");
			
			Mockito.when(responseUtilMock.isQuestionResponseLockedFBQ(responseTO, "1")).thenReturn(true);
			Mockito.when(responseUtilMock.isQuestionResponseLockedFBQ(responseTO, "2")).thenReturn(true);
			Mockito.when(responseUtilMock.isQuestionResponseLockedFBQ(responseTO, "3")).thenReturn(true);
			Mockito.when(responseUtilMock.isQuestionResponseLockedFBQ(responseTO, "5")).thenReturn(true);
			
			
			userResponseWithPolicyTO.setResponseTO(responseTO);
	    	jsonArray = assignmentServiceUtilMock.getQuestionPagesInfoJSON(eaidMap, userResponseWithPolicyTO);
	    	
	    	_logger.info("jsonArray :: " +jsonArray);
	    	
	    	JSONObject jsonObject0 = jsonArray.getJSONObject(0);
	    	JSONObject jsonObject1 = jsonArray.getJSONObject(1);
	    	JSONObject jsonObject2 = jsonArray.getJSONObject(2);
	    	JSONObject jsonObject3 = jsonArray.getJSONObject(3);
	    	
	    	assertTrue(jsonObject0.getString("ptitle").contains("Question 1"));
	    	assertTrue(jsonObject0.getString("erids").contains("ERID1"));
	    	assertTrue(jsonObject0.getString("workflow").contains("normal"));
	    	assertTrue(jsonObject0.getString(CaaConstants.WORKFLOWSTATE).contains("0"));
	    	assertTrue(jsonObject0.getString(CaaConstants.FBQ).contains(CaaConstants.REQUESTED));
	    	
	    	assertTrue(jsonObject1.getString("ptitle").contains("Question 2"));
	    	assertTrue(jsonObject1.getString("erids").contains("ERID2"));
	    	assertTrue(jsonObject1.getString("workflow").contains("normal"));
	    	assertTrue(jsonObject1.getString(CaaConstants.WORKFLOWSTATE).contains("0"));
	    	assertTrue(jsonObject1.getString(CaaConstants.FBQ).contains(CaaConstants.REQUESTED));
	    	
	    	assertTrue(jsonObject2.getString("ptitle").contains("Question 3 - 3"));
	    	assertTrue(jsonObject2.getString("erids").contains("ERID3"));
	    	assertTrue(jsonObject2.getString("erids").contains("ERID4"));
	    	assertTrue(jsonObject2.getString("workflow").contains(question.MP_CUSTOM_PRGRESSION_ONEWAY));
	    	assertTrue(jsonObject2.getString(CaaConstants.WORKFLOWSTATE).contains(""));
	    	assertTrue(jsonObject2.getString(CaaConstants.FBQ).contains(CaaConstants.REQUESTED));
	    	
	    	assertTrue(jsonObject3.getString("ptitle").contains("Question 4 - 4"));
	    	assertTrue(jsonObject3.getString("erids").contains("ERID5"));
	    	assertTrue(jsonObject3.getString("erids").contains("ERID6"));
	    	assertTrue(jsonObject3.getString("workflow").contains(question.MP_CUSTOM_PRGRESSION_ONEWAY));
	    	assertTrue(jsonObject3.getString(CaaConstants.WORKFLOWSTATE).contains(""));
	    	assertTrue(jsonObject3.getString(CaaConstants.FBQ).contains(CaaConstants.REQUESTED));
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method tests that the pages information in AJSON is populated properly or not
	 * @see AssignmentServiceUtil#getQuestionPagesInfoJSON(List, Map)
	 * @throws Exception
	 */
	@Test
	public void TestGetQuestionPagesInfoJSONCase5() throws Exception {
		Map<String,String> eaidMap = null;
		question questionMock = new sectionBreak();
		questionMock.referenceTag = "1234";
		questionMock.questionProperties = richProperties.newInstance(question.MP_CUSTOM_PRGRESSION);
		questionMock.questionProperties.setString(question.MP_CUSTOM_PRGRESSION, question.MP_CUSTOM_PRGRESSION_ONEWAY);
		question questionMockOthers = new question();
		String testID = "T1";
		String attemptPK = "A1";
		JSONArray jsonArray = null;
		String qList = "1;2;3,4;5,6";
		CustomMap<String, String> testparamMap = new CustomMap<String,String>();
		Map<String,String> policyMap = new HashMap<String,String>();
		PolicyTO policyTO = new PolicyTO();
		policyTO.setPolicyMap(policyMap);
		ResponseTO responseTO = new ResponseTO();
		UserResponseWithPolicyTO userResponseWithPolicyTO = new UserResponseWithPolicyTO();
		userResponseWithPolicyTO.setPolicyTO(policyTO);
		try{
			testparamMap.replaceParam(classware_hm.POLICY_feedback , "yes");
			testparamMap.replaceParam(CaaConstants.QLIST, qList);
			responseTO.setTestParameter(testparamMap);
			
			eaidMap = new HashMap<String,String>();
			eaidMap.put("assignmentid", testID);
			eaidMap.put("attemptpk", attemptPK);
			eaidMap.put("mode", UserResponseService.REVIEW_MODE);

			Mockito.when(cacheServiceMock.getItem("1")).thenReturn(questionMockOthers);
			Mockito.when(cacheServiceMock.getItem("2")).thenReturn(questionMockOthers);
			Mockito.when(cacheServiceMock.getItem("3")).thenReturn(questionMock);
			Mockito.when(cacheServiceMock.getItem("5")).thenReturn(questionMock);
			
			EridTO eridTO1  = securityUtil.generateEridTO(testID, attemptPK, "1", UserResponseService.REVIEW_MODE);
			EridTO eridTO2  = securityUtil.generateEridTO(testID, attemptPK, "2", UserResponseService.REVIEW_MODE);
			EridTO eridTO3  = securityUtil.generateEridTO(testID, attemptPK, "3", UserResponseService.REVIEW_MODE);
			EridTO eridTO4  = securityUtil.generateEridTO(testID, attemptPK, "4", UserResponseService.REVIEW_MODE);
			EridTO eridTO5  = securityUtil.generateEridTO(testID, attemptPK, "5", UserResponseService.REVIEW_MODE);
			EridTO eridTO6  = securityUtil.generateEridTO(testID, attemptPK, "6", UserResponseService.REVIEW_MODE);
			
			Mockito.doReturn(eridTO1).when(securityUtil).generateEridTO(testID, attemptPK, "1", UserResponseService.REVIEW_MODE);
			Mockito.doReturn(eridTO2).when(securityUtil).generateEridTO(testID, attemptPK, "2", UserResponseService.REVIEW_MODE);
			Mockito.doReturn(eridTO3).when(securityUtil).generateEridTO(testID, attemptPK, "3", UserResponseService.REVIEW_MODE);
			Mockito.doReturn(eridTO4).when(securityUtil).generateEridTO(testID, attemptPK, "4", UserResponseService.REVIEW_MODE);
			Mockito.doReturn(eridTO5).when(securityUtil).generateEridTO(testID, attemptPK, "5", UserResponseService.REVIEW_MODE);
			Mockito.doReturn(eridTO6).when(securityUtil).generateEridTO(testID, attemptPK, "6", UserResponseService.REVIEW_MODE);
			
			Mockito.when(securityServiceMock.getERID(eridTO1)).thenReturn("ERID1");
			Mockito.when(securityServiceMock.getERID(eridTO2)).thenReturn("ERID2");
			Mockito.when(securityServiceMock.getERID(eridTO3)).thenReturn("ERID3");
			Mockito.when(securityServiceMock.getERID(eridTO4)).thenReturn("ERID4");
			Mockito.when(securityServiceMock.getERID(eridTO5)).thenReturn("ERID5");
			Mockito.when(securityServiceMock.getERID(eridTO6)).thenReturn("ERID6");
			
			Mockito.when(responseUtilMock.isQuestionResponseLockedFBQ(responseTO, "1")).thenReturn(false);
			Mockito.when(responseUtilMock.isQuestionResponseLockedFBQ(responseTO, "2")).thenReturn(false);
			Mockito.when(responseUtilMock.isQuestionResponseLockedFBQ(responseTO, "3")).thenReturn(true);
			Mockito.when(responseUtilMock.isQuestionResponseLockedFBQ(responseTO, "5")).thenReturn(true);
			
			userResponseWithPolicyTO.setResponseTO(responseTO);
	    	jsonArray = assignmentServiceUtilMock.getQuestionPagesInfoJSON(eaidMap, userResponseWithPolicyTO);
	    	
	    	_logger.info("jsonArray :: " +jsonArray);
	    	
	    	JSONObject jsonObject0 = jsonArray.getJSONObject(0);
	    	JSONObject jsonObject1 = jsonArray.getJSONObject(1);
	    	JSONObject jsonObject2 = jsonArray.getJSONObject(2);
	    	JSONObject jsonObject3 = jsonArray.getJSONObject(3);
	    	
	    	assertTrue(jsonObject0.getString("ptitle").contains("Question 1"));
	    	assertTrue(jsonObject0.getString("erids").contains("ERID1"));
	    	assertTrue(jsonObject0.getString("workflow").contains("normal"));
	    	assertTrue(jsonObject0.getString(CaaConstants.WORKFLOWSTATE).contains("0"));
	    	assertTrue(jsonObject0.getString(CaaConstants.FBQ).contains(CaaConstants.UNREQUESTED));
	    	
	    	assertTrue(jsonObject1.getString("ptitle").contains("Question 2"));
	    	assertTrue(jsonObject1.getString("erids").contains("ERID2"));
	    	assertTrue(jsonObject1.getString("workflow").contains("normal"));
	    	assertTrue(jsonObject1.getString(CaaConstants.WORKFLOWSTATE).contains("0"));
	    	assertTrue(jsonObject1.getString(CaaConstants.FBQ).contains(CaaConstants.UNREQUESTED));
	    	
	    	assertTrue(jsonObject2.getString("ptitle").contains("Question 3 - 3"));
	    	assertTrue(jsonObject2.getString("erids").contains("ERID3"));
	    	assertTrue(jsonObject2.getString("erids").contains("ERID4"));
	    	assertTrue(jsonObject2.getString("workflow").contains(question.MP_CUSTOM_PRGRESSION_ONEWAY));
	    	assertTrue(jsonObject2.getString(CaaConstants.WORKFLOWSTATE).contains(""));
	    	assertTrue(jsonObject2.getString(CaaConstants.FBQ).contains(CaaConstants.REQUESTED));
	    	
	    	assertTrue(jsonObject3.getString("ptitle").contains("Question 4 - 4"));
	    	assertTrue(jsonObject3.getString("erids").contains("ERID5"));
	    	assertTrue(jsonObject3.getString("erids").contains("ERID6"));
	    	assertTrue(jsonObject3.getString("workflow").contains(question.MP_CUSTOM_PRGRESSION_ONEWAY));
	    	assertTrue(jsonObject3.getString(CaaConstants.WORKFLOWSTATE).contains(""));
	    	assertTrue(jsonObject3.getString(CaaConstants.FBQ).contains(CaaConstants.REQUESTED));
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method tests that the pages information in AJSON is populated properly or not
	 * @see AssignmentServiceUtil#getQuestionPagesInfoJSON(List, Map)
	 * @throws Exception
	 */
	@Test
	public void TestGetQuestionPagesInfoJSONCase6() throws Exception {
		Map<String,String> eaidMap = null;
		question questionMock = new sectionBreak();
		questionMock.referenceTag = "1234";
		questionMock.questionProperties = richProperties.newInstance(question.MP_CUSTOM_PRGRESSION);
		questionMock.questionProperties.setString(question.MP_CUSTOM_PRGRESSION, question.MP_CUSTOM_PRGRESSION_ONEWAY);
		question questionMockOthers = new question();
		String testID = "T1";
		String attemptPK = "A1";
		JSONArray jsonArray = null;
		String qList = "1;2;3,4;5,6";
		CustomMap<String, String> testparamMap = new CustomMap<String,String>();
		Map<String,String> policyMap = new HashMap<String,String>();
		PolicyTO policyTO = new PolicyTO();
		policyTO.setPolicyMap(policyMap);
		ResponseTO responseTO = new ResponseTO();
		UserResponseWithPolicyTO userResponseWithPolicyTO = new UserResponseWithPolicyTO();
		userResponseWithPolicyTO.setPolicyTO(policyTO);
		try{
			testparamMap.replaceParam(classware_hm.POLICY_feedback , "yes");
			testparamMap.replaceParam(CaaConstants.QLIST, qList);
			responseTO.setTestParameter(testparamMap);
			
			eaidMap = new HashMap<String,String>();
			eaidMap.put("assignmentid", testID);
			eaidMap.put("attemptpk", attemptPK);
			eaidMap.put("mode", UserResponseService.REVIEW_MODE);

			Mockito.when(cacheServiceMock.getItem("1")).thenReturn(questionMockOthers);
			Mockito.when(cacheServiceMock.getItem("2")).thenReturn(questionMockOthers);
			Mockito.when(cacheServiceMock.getItem("3")).thenReturn(questionMockOthers);
			Mockito.when(cacheServiceMock.getItem("4")).thenReturn(questionMock);
			Mockito.when(cacheServiceMock.getItem("5")).thenReturn(questionMock);
			
			EridTO eridTO1  = securityUtil.generateEridTO(testID, attemptPK, "1", UserResponseService.REVIEW_MODE);
			EridTO eridTO2  = securityUtil.generateEridTO(testID, attemptPK, "2", UserResponseService.REVIEW_MODE);
			EridTO eridTO3  = securityUtil.generateEridTO(testID, attemptPK, "3", UserResponseService.REVIEW_MODE);
			EridTO eridTO4  = securityUtil.generateEridTO(testID, attemptPK, "4", UserResponseService.REVIEW_MODE);
			EridTO eridTO5  = securityUtil.generateEridTO(testID, attemptPK, "5", UserResponseService.REVIEW_MODE);
			EridTO eridTO6  = securityUtil.generateEridTO(testID, attemptPK, "6", UserResponseService.REVIEW_MODE);
			
			Mockito.doReturn(eridTO1).when(securityUtil).generateEridTO(testID, attemptPK, "1", UserResponseService.REVIEW_MODE);
			Mockito.doReturn(eridTO2).when(securityUtil).generateEridTO(testID, attemptPK, "2", UserResponseService.REVIEW_MODE);
			Mockito.doReturn(eridTO3).when(securityUtil).generateEridTO(testID, attemptPK, "3", UserResponseService.REVIEW_MODE);
			Mockito.doReturn(eridTO4).when(securityUtil).generateEridTO(testID, attemptPK, "4", UserResponseService.REVIEW_MODE);
			Mockito.doReturn(eridTO5).when(securityUtil).generateEridTO(testID, attemptPK, "5", UserResponseService.REVIEW_MODE);
			Mockito.doReturn(eridTO6).when(securityUtil).generateEridTO(testID, attemptPK, "6", UserResponseService.REVIEW_MODE);
			
			Mockito.when(securityServiceMock.getERID(eridTO1)).thenReturn("ERID1");
			Mockito.when(securityServiceMock.getERID(eridTO2)).thenReturn("ERID2");
			Mockito.when(securityServiceMock.getERID(eridTO3)).thenReturn("ERID3");
			Mockito.when(securityServiceMock.getERID(eridTO4)).thenReturn("ERID4");
			Mockito.when(securityServiceMock.getERID(eridTO5)).thenReturn("ERID5");
			Mockito.when(securityServiceMock.getERID(eridTO6)).thenReturn("ERID6");
			
			Mockito.when(responseUtilMock.isQuestionResponseLockedFBQ(responseTO, "1")).thenReturn(false);
			Mockito.when(responseUtilMock.isQuestionResponseLockedFBQ(responseTO, "2")).thenReturn(false);
			Mockito.when(responseUtilMock.isQuestionResponseLockedFBQ(responseTO, "3")).thenReturn(true);
			Mockito.when(responseUtilMock.isQuestionResponseLockedFBQ(responseTO, "5")).thenReturn(true);
			
			userResponseWithPolicyTO.setResponseTO(responseTO);
	    	jsonArray = assignmentServiceUtilMock.getQuestionPagesInfoJSON(eaidMap, userResponseWithPolicyTO);
	    	
	    	_logger.info("jsonArray :: " +jsonArray);
	    	
	    	JSONObject jsonObject0 = jsonArray.getJSONObject(0);
	    	JSONObject jsonObject1 = jsonArray.getJSONObject(1);
	    	JSONObject jsonObject2 = jsonArray.getJSONObject(2);
	    	JSONObject jsonObject3 = jsonArray.getJSONObject(3);
	    	
	    	assertTrue(jsonObject0.getString("ptitle").contains("Question 1"));
	    	assertTrue(jsonObject0.getString("erids").contains("ERID1"));
	    	assertTrue(jsonObject0.getString("workflow").contains("normal"));
	    	assertTrue(jsonObject0.getString(CaaConstants.WORKFLOWSTATE).contains("0"));
	    	assertTrue(jsonObject0.getString(CaaConstants.FBQ).contains(CaaConstants.UNREQUESTED));
	    	
	    	assertTrue(jsonObject1.getString("ptitle").contains("Question 2"));
	    	assertTrue(jsonObject1.getString("erids").contains("ERID2"));
	    	assertTrue(jsonObject1.getString("workflow").contains("normal"));
	    	assertTrue(jsonObject1.getString(CaaConstants.WORKFLOWSTATE).contains("0"));
	    	assertTrue(jsonObject1.getString(CaaConstants.FBQ).contains(CaaConstants.UNREQUESTED));
	    	
	    	assertTrue(jsonObject2.getString("ptitle").contains("Question 3 - 3"));
	    	assertTrue(jsonObject2.getString("erids").contains("ERID3"));
	    	assertTrue(jsonObject2.getString("erids").contains("ERID4"));
	    	assertTrue(jsonObject2.getString("workflow").contains(question.MP_CUSTOM_PRGRESSION_ONEWAY));
	    	assertTrue(jsonObject2.getString(CaaConstants.WORKFLOWSTATE).contains(""));
	    	assertTrue(jsonObject2.getString(CaaConstants.FBQ).contains(CaaConstants.REQUESTED));
	    	
	    	assertTrue(jsonObject3.getString("ptitle").contains("Question 4 - 4"));
	    	assertTrue(jsonObject3.getString("erids").contains("ERID5"));
	    	assertTrue(jsonObject3.getString("erids").contains("ERID6"));
	    	assertTrue(jsonObject3.getString("workflow").contains(question.MP_CUSTOM_PRGRESSION_ONEWAY));
	    	assertTrue(jsonObject3.getString(CaaConstants.WORKFLOWSTATE).contains(""));
	    	assertTrue(jsonObject3.getString(CaaConstants.FBQ).contains(CaaConstants.REQUESTED));
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method tests that the question IDs are properly scrambled or not
	 * @see AssignmentServiceUtil#getscramblePages(String, CustomMap)
	 * @throws Exception
	 */
	@Test
	public void testGetscramblePagesCase1() throws Exception {
		String originalList = "1;2;3;4;5;6,7,8,9;10,11,12";
		CustomMap<String, String> testParamMap = new CustomMap<String,String>();
		try{
			String actualString = assignmentServiceUtil.getScramblePages(originalList, testParamMap);
			_logger.info("ActualString :: "  + actualString);
			assertTrue(!StringUtils.isBlank(testParamMap.getParam(CaaConstants.ITEMID)));
			assertTrue(!StringUtils.isBlank(actualString) && !originalList.equals(actualString));
		}catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * This method tests that the question IDs are properly scrambled or not
	 * @see AssignmentServiceUtil#getscramblePages(String, CustomMap)
	 * @throws Exception
	 */
	@Test
	public void testGetscramblePagesCase2() throws Exception {
		String originalList = "";
		CustomMap<String, String> testParamMap = new CustomMap<String,String>();
		try{
			String actualString = assignmentServiceUtil.getScramblePages(originalList, testParamMap);
			_logger.info("ActualString :: "  + actualString);
			assertTrue(StringUtils.isBlank(testParamMap.getParam(CaaConstants.ITEMID)));
			assertTrue(StringUtils.isBlank(actualString));
		}catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * This method tests that the question IDs are properly scrambled or not
	 * @see AssignmentServiceUtil#getscramblePages(String, CustomMap)
	 * @throws Exception
	 */
	@Test
	public void testGetscramblePagesCase3() throws Exception {
		String originalList = null;
		CustomMap<String, String> testParamMap = new CustomMap<String,String>();
		try{
			String actualString = assignmentServiceUtil.getScramblePages(originalList, testParamMap);
			_logger.info("ActualString :: "  + actualString);
			assertTrue(StringUtils.isBlank(testParamMap.getParam(CaaConstants.ITEMID)));
			assertTrue(StringUtils.isBlank(actualString));
		}catch (Exception e) {
			throw e;
		}
	}

	/**
	 * This method is going to test that AVAR information is populated properly or not 
	 * from the policyMap
	 * @see AssignmentServiceUtil#getAvarJSON(ResponseTO)
	 * @throws Exception
	 */
	@Test
	public void Test_getAvarJSONCase1() throws Exception {
		ResponseTO responseTO = null;
		JSONArray jsonArray = null;
		PolicyTO policyTO = new PolicyTO();
		Map<String,String> policyMap = new HashMap<String,String>();
		CustomMap<String, String> testParamMap = new CustomMap<String,String>();
		try{
			responseTO = new ResponseTO();
			responseTO.setAttemptNo("32");
			testParamMap.replaceParam(classware_hm.AP_INSTRUCTOR_NAME , "instructorName");
			testParamMap.replaceParam(classware_hm.POLICY_LSI_instructoremail, "instructorEmail");
			testParamMap.replaceParam(classware_hm.HEADER_STUDENT_NAME , "studentName");
			testParamMap.replaceParam(classware_hm.POLICY_LSI_studentemail , "studentEmail");
			testParamMap.replaceParam(classware_hm.HEADER_ASSIGNMENT_TITLE, "assignmentTitle");
			testParamMap.replaceParam(classware_hm.HEADER_COURSE_TITLE , "courseTitle");
			testParamMap.replaceParam(classware_hm.HEADER_SECTION_TITLE , "sectionTitle");
			responseTO.setTestParameter(testParamMap);
			policyMap.put("p_policy1", "one");
			policyMap.put("p_policy2", "two");
			policyMap.put("p_policy3", "three");
			policyMap.put("p_policy4", "four");
			policyMap.put("p_policy5", "five");
			policyMap.put("p_policy6", "six");
			policyTO.setPolicyMap(policyMap);
			Mockito.when(responseUtilMock.fetchTestPolicy(responseTO)).thenReturn(policyTO);
			jsonArray = assignmentServiceUtilMock.getAvarJSON(responseTO);
			String jsonStr = jsonArray.toString();
			assertTrue(jsonStr.contains("p_policy6"));
			assertTrue(jsonStr.contains("p_policy5"));
			assertTrue(jsonStr.contains("p_policy4"));
			assertTrue(jsonStr.contains("p_policy3"));
			assertTrue(jsonStr.contains("p_policy2"));
			assertTrue(jsonStr.contains("p_policy1"));
			assertTrue(jsonStr.contains("one"));
			assertTrue(jsonStr.contains("two"));
			assertTrue(jsonStr.contains("three"));
			assertTrue(jsonStr.contains("four"));
			assertTrue(jsonStr.contains("five"));
			assertTrue(jsonStr.contains("six"));
			assertTrue(jsonStr.contains("instructorName"));
			assertTrue(jsonStr.contains("instructorEmail"));
			assertTrue(jsonStr.contains("studentName"));
			assertTrue(jsonStr.contains("studentEmail"));
			assertTrue(jsonStr.contains("assignmentTitle"));
			assertTrue(jsonStr.contains("courseTitle"));
			assertTrue(jsonStr.contains("sectionTitle"));
			_logger.info("AVAR json is :: " + jsonArray);
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method is going to test that AVAR information is populated properly or not 
	 * from the policyMap
	 * @see AssignmentServiceUtil#getAvarJSON(ResponseTO)
	 * @throws Exception
	 */
	@Test
	public void Test_getAvarJSONCase2() throws Exception {
		ResponseTO responseTO = null;
		JSONArray jsonArray = null;
		PolicyTO policyTO = new PolicyTO();
		Map<String,String> policyMap = new HashMap<String,String>();
		CustomMap<String, String> testParamMap = new CustomMap<String,String>();
		try{
			responseTO = new ResponseTO();
			responseTO.setAttemptNo("32");
			testParamMap.replaceParam(classware_hm.HEADER_STUDENT_NAME , "studentName");
			testParamMap.replaceParam(classware_hm.POLICY_LSI_studentemail , "studentEmail");
			testParamMap.replaceParam(classware_hm.HEADER_ASSIGNMENT_TITLE, "assignmentTitle");
			testParamMap.replaceParam(classware_hm.HEADER_COURSE_TITLE , "courseTitle");
			testParamMap.replaceParam(classware_hm.HEADER_SECTION_TITLE , "sectionTitle");
			responseTO.setTestParameter(testParamMap);
			policyMap.put("p_policy1", "one");
			policyMap.put("p_policy2", "two");
			policyMap.put("p_policy3", "three");
			policyMap.put("p_policy4", "four");
			policyMap.put("p_policy5", "five");
			policyMap.put("p_policy6", "six");
			policyTO.setPolicyMap(policyMap);
			Mockito.when(responseUtilMock.fetchTestPolicy(responseTO)).thenReturn(policyTO);
			jsonArray = assignmentServiceUtilMock.getAvarJSON(responseTO);
			String jsonStr = jsonArray.toString();
			assertTrue(jsonStr.contains("p_policy6"));
			assertTrue(jsonStr.contains("p_policy5"));
			assertTrue(jsonStr.contains("p_policy4"));
			assertTrue(jsonStr.contains("p_policy3"));
			assertTrue(jsonStr.contains("p_policy2"));
			assertTrue(jsonStr.contains("p_policy1"));
			assertTrue(jsonStr.contains("one"));
			assertTrue(jsonStr.contains("two"));
			assertTrue(jsonStr.contains("three"));
			assertTrue(jsonStr.contains("four"));
			assertTrue(jsonStr.contains("five"));
			assertTrue(jsonStr.contains("six"));
			assertTrue(!jsonStr.contains("instructorName"));
			assertTrue(!jsonStr.contains("instructorEmail"));
			assertTrue(jsonStr.contains("studentName"));
			assertTrue(jsonStr.contains("studentEmail"));
			assertTrue(jsonStr.contains("assignmentTitle"));
			assertTrue(jsonStr.contains("courseTitle"));
			assertTrue(jsonStr.contains("sectionTitle"));
			_logger.info("AVAR json is :: " + jsonArray);
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method is going to test that AVAR information is populated properly or not 
	 * from the policyMap
	 * @see AssignmentServiceUtil#getAvarJSON(ResponseTO)
	 * @throws Exception
	 */
	@Test
	public void Test_getAvarJSONCase3() throws Exception {
		ResponseTO responseTO = null;
		JSONArray jsonArray = null;
		PolicyTO policyTO = new PolicyTO();
		Map<String,String> policyMap = new HashMap<String,String>();
		CustomMap<String, String> testParamMap = new CustomMap<String,String>();
		try{
			responseTO = new ResponseTO();
			responseTO.setAttemptNo("32");
			testParamMap.replaceParam(classware_hm.AP_INSTRUCTOR_NAME , "instructorName");
			testParamMap.replaceParam(classware_hm.POLICY_LSI_instructoremail, "instructorEmail");
			testParamMap.replaceParam(classware_hm.HEADER_STUDENT_NAME , "studentName");
			testParamMap.replaceParam(classware_hm.POLICY_LSI_studentemail , "studentEmail");
			testParamMap.replaceParam(classware_hm.HEADER_ASSIGNMENT_TITLE, "assignmentTitle");
			testParamMap.replaceParam(classware_hm.HEADER_COURSE_TITLE , "courseTitle");
			testParamMap.replaceParam(classware_hm.HEADER_SECTION_TITLE , "sectionTitle");
			responseTO.setTestParameter(testParamMap);
			policyTO.setPolicyMap(policyMap);
			Mockito.when(responseUtilMock.fetchTestPolicy(responseTO)).thenReturn(policyTO);
			jsonArray = assignmentServiceUtilMock.getAvarJSON(responseTO);
			String jsonStr = jsonArray.toString();
			assertTrue(!jsonStr.contains("p_policy6"));
			assertTrue(!jsonStr.contains("p_policy5"));
			assertTrue(!jsonStr.contains("p_policy4"));
			assertTrue(!jsonStr.contains("p_policy3"));
			assertTrue(!jsonStr.contains("p_policy2"));
			assertTrue(!jsonStr.contains("p_policy1"));
			assertTrue(!jsonStr.contains("one"));
			assertTrue(!jsonStr.contains("two"));
			assertTrue(!jsonStr.contains("three"));
			assertTrue(!jsonStr.contains("four"));
			assertTrue(!jsonStr.contains("five"));
			assertTrue(!jsonStr.contains("six"));
			assertTrue(jsonStr.contains("instructorName"));
			assertTrue(jsonStr.contains("instructorEmail"));
			assertTrue(jsonStr.contains("studentName"));
			assertTrue(jsonStr.contains("studentEmail"));
			assertTrue(jsonStr.contains("assignmentTitle"));
			assertTrue(jsonStr.contains("courseTitle"));
			assertTrue(jsonStr.contains("sectionTitle"));
			_logger.info("AVAR json is :: " + jsonArray);
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method is going to test that AVAR information is populated properly or not 
	 * from the policyMap
	 * @see AssignmentServiceUtil#getAvarJSON(ResponseTO)
	 * @throws Exception
	 */
	@Test
	public void Test_getAvarJSONCase4() throws Exception {
		ResponseTO responseTO = null;
		JSONArray jsonArray = null;
		PolicyTO policyTO = new PolicyTO();
		Map<String,String> policyMap = new HashMap<String,String>();
		CustomMap<String, String> testParamMap = new CustomMap<String,String>();
		try{
			responseTO = new ResponseTO();
			responseTO.setAttemptNo("32");
			testParamMap.replaceParam(classware_hm.AP_INSTRUCTOR_NAME , "instructorName");
			testParamMap.replaceParam(classware_hm.POLICY_LSI_instructoremail, "instructorEmail");
			testParamMap.replaceParam(classware_hm.HEADER_STUDENT_NAME , "studentName");
			testParamMap.replaceParam(classware_hm.POLICY_LSI_studentemail , "studentEmail");
			testParamMap.replaceParam(classware_hm.HEADER_ASSIGNMENT_TITLE, "assignmentTitle");
			testParamMap.replaceParam(classware_hm.HEADER_COURSE_TITLE , "courseTitle");
			testParamMap.replaceParam(classware_hm.HEADER_SECTION_TITLE , "sectionTitle");
			responseTO.setTestParameter(testParamMap);
			policyMap.put("p_policy1", "one");
			policyMap.put("p_policy2", "two");
			policyTO.setPolicyMap(policyMap);
			Mockito.when(responseUtilMock.fetchTestPolicy(responseTO)).thenReturn(policyTO);
			jsonArray = assignmentServiceUtilMock.getAvarJSON(responseTO);
			String jsonStr = jsonArray.toString();
			assertTrue(!jsonStr.contains("p_policy6"));
			assertTrue(!jsonStr.contains("p_policy5"));
			assertTrue(!jsonStr.contains("p_policy4"));
			assertTrue(!jsonStr.contains("p_policy3"));
			assertTrue(jsonStr.contains("p_policy2"));
			assertTrue(jsonStr.contains("p_policy1"));
			assertTrue(jsonStr.contains("one"));
			assertTrue(jsonStr.contains("two"));
			assertTrue(!jsonStr.contains("three"));
			assertTrue(!jsonStr.contains("four"));
			assertTrue(!jsonStr.contains("five"));
			assertTrue(!jsonStr.contains("six"));
			assertTrue(jsonStr.contains("instructorName"));
			assertTrue(jsonStr.contains("instructorEmail"));
			assertTrue(jsonStr.contains("studentName"));
			assertTrue(jsonStr.contains("studentEmail"));
			assertTrue(jsonStr.contains("assignmentTitle"));
			assertTrue(jsonStr.contains("courseTitle"));
			assertTrue(jsonStr.contains("sectionTitle"));
			_logger.info("AVAR json is :: " + jsonArray);
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * @see AssignmentServiceUtil#getQuestionIdsFromQlist(String)
	 * @throws Exception
	 */
	@Test
	public void testGetQuestionIdsFromQlist() throws Exception {
		String originalList = "1;2;3;4;5;6,7,8,9;10,11,12";
		try{
			List<String> qlist = assignmentServiceUtil.getQuestionIdsFromQlist(originalList);
			_logger.info("########## qlist :: " + qlist);
			assertTrue(qlist.contains("1"));
			assertTrue(qlist.contains("2"));
			assertTrue(qlist.contains("3"));
			assertTrue(qlist.contains("4"));
			assertTrue(qlist.contains("5"));
			assertTrue(qlist.contains("6"));
			assertTrue(qlist.contains("7"));
			assertTrue(qlist.contains("8"));
			assertTrue(qlist.contains("9"));
			assertTrue(qlist.contains("10"));
			assertTrue(qlist.contains("11"));
			assertTrue(qlist.contains("12"));
		}catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * @see AssignmentServiceUtil#sanitizeQuestionList(ResponseTO, List)
	 * @throws Exception
	 */
	@Test
	public void testSanitizeQuestionList() throws Exception {
		String originalList = "1;2;6,7,8;10";
		ResponseTO responseTO = new ResponseTO();
		Map<String, QuestionWiseResponseTO> responseMap = new HashMap<String, QuestionWiseResponseTO>();
		try{
			List<String> qlist = assignmentServiceUtil.getQuestionIdsFromQlist(originalList);
			responseTO.setResponseMap(responseMap);
			
			responseMap.put("1", new QuestionWiseResponseTO());
			responseMap.put("2", new QuestionWiseResponseTO());
			responseMap.put("3", new QuestionWiseResponseTO());
			responseMap.put("4", new QuestionWiseResponseTO());
			responseMap.put("5", new QuestionWiseResponseTO());
			responseMap.put("6", new QuestionWiseResponseTO());
			responseMap.put("7", new QuestionWiseResponseTO());
			responseMap.put("8", new QuestionWiseResponseTO());
			responseMap.put("9", new QuestionWiseResponseTO());
			responseMap.put("10", new QuestionWiseResponseTO());
			responseMap.put("11", new QuestionWiseResponseTO());
			responseMap.put("12", new QuestionWiseResponseTO());
			
			responseTO = assignmentServiceUtil.sanitizeQuestionList(responseTO, qlist);
			Map<String, QuestionWiseResponseTO> resultMap = responseTO.getResponseMap();
			
			assertTrue(resultMap.get("1") != null);
			assertTrue(resultMap.get("2") != null);
			assertTrue(resultMap.get("3") == null);
			assertTrue(resultMap.get("4") == null);
			assertTrue(resultMap.get("5") == null);
			assertTrue(resultMap.get("6") != null);
			assertTrue(resultMap.get("7") != null);
			assertTrue(resultMap.get("8") != null);
			assertTrue(resultMap.get("9") == null);
			assertTrue(resultMap.get("10") != null);
			assertTrue(resultMap.get("11") == null);
			assertTrue(resultMap.get("12") == null);
			
		}catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * @see AssignmentServiceUtil #isReviewMode(boolean, String, String)
	 * @throws Exception
	 */
	@Test
	public void isReviewModeCase1() throws Exception {
		boolean isReviewMode = assignmentServiceUtil.isReviewMode(true, "1234", CaaConstants.REVIEW);
		assertTrue(isReviewMode);
	}
	
	/**
	 * @see AssignmentServiceUtil #isReviewMode(boolean, String, String)
	 * @throws Exception
	 */
	@Test
	public void isReviewModeCase2() throws Exception {
		boolean isReviewMode = assignmentServiceUtil.isReviewMode(false, "1234", CaaConstants.REVIEW);
		assertTrue(!isReviewMode);
	}
	
	/**
	 * @see AssignmentServiceUtil #isReviewMode(boolean, String, String)
	 * @throws Exception
	 */
	@Test
	public void isReviewModeCase3() throws Exception {
		boolean isReviewMode = assignmentServiceUtil.isReviewMode(false, classware_hm.ROLE_INSTRUCTOR_ID, CaaConstants.REVIEW);
		assertTrue(isReviewMode);
	}
	
	/**
	 * @see AssignmentServiceUtil #isReviewMode(boolean, String, String)
	 * @throws Exception
	 */
	@Test
	public void isReviewModeCase4() throws Exception {
		boolean isReviewMode = assignmentServiceUtil.isReviewMode(false, classware_hm.ROLE_INSTRUCTOR_ID, "test");
		assertTrue(!isReviewMode);
	}
	
	/**
	 * This method tests that the default url for different section and role is generating properly or not
	 * @see UserHelper # createdefaultUrl(String, String)
	 * @throws Exception
	 */
	@Test
	public void testcreatedefaultUrlcase1() throws Exception {
		String role = "instructor";
		String sectionID = "1111";
		String expectexdUrl = "http://dev3.mhhe.com/connect/hmInstructorSectionHomePortal.do?sectionId=1111";
		CustomMap<String,String> testParamMap = new CustomMap<String,String>();
		testParamMap.replaceParam(classware_hm.CW_SERVER, "http://dev3.mhhe.com/");
		try{
			String url = assignmentServiceUtil.createdefaultUrl(role, sectionID, testParamMap);
			assertTrue(expectexdUrl.equals(url));
		}catch (Exception e) {
			throw e;
		}
	}

	/**
	 * This method tests that the default url for different section and role is generating properly or not
	 * @see UserHelper # createdefaultUrl(String, String)
	 * @throws Exception
	 */
	@Test
	public void testcreatedefaultUrlcase2() throws Exception {
		String role = "instructor";
		String sectionID = "1111";
		CustomMap<String,String> testParamMap = new CustomMap<String,String>();
		try{
			String url = assignmentServiceUtil.createdefaultUrl(role, sectionID, testParamMap);
			assertTrue(false);
		}catch (Exception e) {
			assertTrue("TestParam Map or sectionID is null".equals(e.getMessage()));
		}
	}

	/**
	 * This method validates Connect return URL is forming correctly or not
	 * @see UserHelper #composeReturnURL(com.mcgrawhill.ezto.caa.services.utilities.RequestMap, String)
	 * @throws Exception
	 */
	@Test
	public void testcomposeReturnURLcase1() throws Exception {
		try{
			String returnURL = "http://dev3.mhhe.com:80/connectweb/html/closeWindow.html";
			String defaultURL = "http://dev3.mhhe.com/connect/hmInstructorSectionHomePortal.do?sectionId=1111";
			String url = assignmentServiceUtil.composeReturnURL(returnURL, defaultURL);
			assertTrue(returnURL.equals(url));
		}catch (Exception e) {
			throw e;
		}
	}

	/**
	 * This method validates Connect return URL is forming correctly or not
	 * @see UserHelper #composeReturnURL(com.mcgrawhill.ezto.caa.services.utilities.RequestMap, String)
	 * @throws Exception
	 */
	@Test
	public void testcomposeReturnURLcase2() throws Exception {
		try{
			String returnURL = "";
			String defaultURL = "http://dev3.mhhe.com/connect/hmInstructorSectionHomePortal.do?sectionId=1111";
			String url = assignmentServiceUtil.composeReturnURL(returnURL, defaultURL);
			assertTrue(defaultURL.equals(url));
		}catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * This method validates Connect return URL is forming correctly or not
	 * @see UserHelper #composeReturnURL(com.mcgrawhill.ezto.caa.services.utilities.RequestMap, String)
	 * @throws Exception
	 */
	@Test
	public void testcomposeReturnURLcase3() throws Exception {
		try{
			String returnURL = "null";
			String defaultURL = "http://dev3.mhhe.com/connect/hmInstructorSectionHomePortal.do?sectionId=1111";
			String url = assignmentServiceUtil.composeReturnURL(returnURL, defaultURL);
			assertTrue(defaultURL.equals(url));
		}catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * This method validates Connect return URL is forming correctly or not
	 * @see UserHelper #composeReturnURL(com.mcgrawhill.ezto.caa.services.utilities.RequestMap, String)
	 * @throws Exception
	 */
	@Test
	public void testcomposeReturnURLcase4() throws Exception {
		try{
			String returnURL = null;
			String defaultURL = "http://dev3.mhhe.com/connect/hmInstructorSectionHomePortal.do?sectionId=1111";
			String url = assignmentServiceUtil.composeReturnURL(returnURL, defaultURL);
			assertTrue(defaultURL.equals(url));
		}catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * This method is going to test that testparemer's selected values are updated properly from
	 * requestMap or not
	 * @see AssignmentServiceUtil#updateTestParamforReportView(Map, ResponseTO)
	 * @throws Exception
	 */
	@Test
	public void testUpdateTestParamforReportViewCase1() throws Exception {
		ResponseTO responseTO = new ResponseTO();
		ResponseTO alreadySubmittedResponseTO = new ResponseTO();
		alreadySubmittedResponseTO.setAttemptPK(1l);
		RequestMap<String, String> requestMap = new RequestMap<String,String>();
		AttemptDTO attemptDTO = new AttemptDTO();
		try{
			CustomMap<String, String> testparameter = new CustomMap<String,String>();
			testparameter.replaceParam(classware_hm.ALL_CORRECT, "no");
			alreadySubmittedResponseTO.setTestParameter(testparameter);
			requestMap.put(classware_hm.POLICY_grading , classware_hm.POLICY_grading_feedback);
			requestMap.put(classware_hm.POLICY_solution , "yes");
			Mockito.when(userResponseServiceMock.getSubmittedUserResponse(responseTO)).thenReturn(alreadySubmittedResponseTO); 
			Mockito.when(responseUtilMock.parseStudentTestWiseResponse(alreadySubmittedResponseTO)).thenReturn(attemptDTO);
			assignmentServiceUtilMock.updateTestParamforReportView(requestMap, responseTO);
			assertTrue(1l == responseTO.getAttemptPK());
		}catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * This method is going to test that testparemer's selected values are updated properly from
	 * requestMap or not
	 * @see AssignmentServiceUtil#updateTestParamforReportView(Map, ResponseTO)
	 * @throws Exception
	 */
	@Test
	public void testUpdateTestParamforReportViewCase2() throws Exception {
		ResponseTO responseTO = new ResponseTO();
		ResponseTO alreadySubmittedResponseTO = new ResponseTO();
		alreadySubmittedResponseTO.setAttemptPK(1l);
		RequestMap<String, String> requestMap = new RequestMap<String,String>();
		AttemptDTO attemptDTO = new AttemptDTO();
		try{
			CustomMap<String, String> testparameter = new CustomMap<String,String>();
			testparameter.replaceParam(classware_hm.ALL_CORRECT, classware_hm.ALL_CORRECT);
			alreadySubmittedResponseTO.setTestParameter(testparameter);
			requestMap.put(classware_hm.POLICY_grading , classware_hm.POLICY_grading_feedback);
			requestMap.put(classware_hm.POLICY_solution , "100");
			requestMap.put(classware_hm.POLICY_feedback_allcorrect , "yes");
			Mockito.when(userResponseServiceMock.getSubmittedUserResponse(responseTO)).thenReturn(alreadySubmittedResponseTO); 
			Mockito.when(responseUtilMock.parseStudentTestWiseResponse(alreadySubmittedResponseTO)).thenReturn(attemptDTO);
			assignmentServiceUtilMock.updateTestParamforReportView(requestMap, responseTO);
			assertTrue(1l == responseTO.getAttemptPK());
		}catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * This method is going to test that testparemer's selected values are updated properly from
	 * requestMap or not
	 * @see AssignmentServiceUtil#updateTestParamforReportView(Map, ResponseTO)
	 * @throws Exception
	 */
	@Test
	public void testUpdateTestParamforReportViewCase3() throws Exception {
		ResponseTO responseTO = new ResponseTO();
		ResponseTO alreadySubmittedResponseTO = new ResponseTO();
		alreadySubmittedResponseTO.setAttemptPK(1l);
		RequestMap<String, String> requestMap = new RequestMap<String,String>();
		AttemptDTO attemptDTO = new AttemptDTO();
		try{
			Mockito.when(userResponseServiceMock.getSubmittedUserResponse(responseTO)).thenReturn(null); 
			Mockito.when(responseUtilMock.parseStudentTestWiseResponse(alreadySubmittedResponseTO)).thenReturn(attemptDTO);
			assignmentServiceUtilMock.updateTestParamforReportView(requestMap, responseTO);
			assertTrue(false);
		}catch (BusinessException e) {
			assertTrue("submitted attempt not found".equals(e.getMessage()));
		}
	}
	
	/**
	 * This method is going to test that return URL is formed properly or not
	 * @see AssignmentServiceUtil#getExitURL(ResponseTO)
	 */
	@Test
	public void testgetExitURLCase1() throws Exception {
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = responseTO.getTestParameter();
		try{
			testparameter.put(classware_hm.RETURN_URL, "http://dev3.mhhe.com:80/connectweb/html/closeWindow.html");
			testparameter.put(classware_hm.ROLE , "student");
			responseTO.setSectionID("32342599");
			String returnURL = assignmentServiceUtil.getExitURL(responseTO);
			assertTrue("http://dev3.mhhe.com:80/connectweb/html/closeWindow.html".equals(returnURL));
		}catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * This method is going to test that return URL is formed properly or not
	 * @see AssignmentServiceUtil#getExitURL(ResponseTO)
	 */
	@Test
	public void testgetExitURLCase2() throws Exception {
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = responseTO.getTestParameter();
		try{
			testparameter.put(classware_hm.ROLE , "student");
			testparameter.put(classware_hm.CW_SERVER, "http://dev3.mhhe.com/");
			responseTO.setSectionID("32342599");
			String returnURL = assignmentServiceUtil.getExitURL(responseTO);
			assertTrue("http://dev3.mhhe.com/connect/hmStudentSectionHomePortal.do?sectionId=32342599".equals(returnURL));
		}catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * This method is going to test that return URL is formed properly or not
	 * @see AssignmentServiceUtil#getExitURL(ResponseTO)
	 */
	@Test
	public void testgetExitURLCase3() throws Exception {
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = responseTO.getTestParameter();
		try{
			testparameter.put(classware_hm.ROLE , "instructor");
			testparameter.put(classware_hm.CW_SERVER, "http://ezto-dev.mhecloud.mhhe.com/");
			responseTO.setSectionID("PAC");
			String returnURL = assignmentServiceUtil.getExitURL(responseTO);
			assertTrue("http://ezto-dev.mhecloud.mhhe.com/connect/hmInstructorSectionHomePortal.do?sectionId=PAC".equals(returnURL));
		}catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * This method is going to test that exit URL is formed properly or not from
	 * @throws Exception
	 */
	@Test
	public void testgetExitURLCase4() throws Exception{
		Map<String, String> requestMap = new RequestMap<String,String>();
		try{
			requestMap.put(classware_hm.RETURN_URL, "http://dev3.mhhe.com:80/connectweb/html/closeWindow.html");
			requestMap.put(classware_hm.ROLE , "student");
			requestMap.put(classware_hm.SECTION_ID ,"32342599");
			String returnURL = assignmentServiceUtil.getExitURL(requestMap);
			assertTrue("http://dev3.mhhe.com:80/connectweb/html/closeWindow.html".equals(returnURL));
		}catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * This method is going to test that exit URL is formed properly or not from
	 * @see AssignmentServiceUtil#getExitURL(Map)
	 */
	@Test
	public void testgetExitURLCase5() throws Exception {
		Map<String, String> requestMap = new RequestMap<String,String>();
		try{
			requestMap.put(classware_hm.ROLE , "student");
			requestMap.put(classware_hm.CW_SERVER, "http://dev3.mhhe.com/");
			requestMap.put(classware_hm.SECTION_ID , "32342599");
			String returnURL = assignmentServiceUtil.getExitURL(requestMap);
			assertTrue("http://dev3.mhhe.com/connect/hmStudentSectionHomePortal.do?sectionId=32342599".equals(returnURL));
		}catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * This method is going to test that exit URL is formed properly or not from
	 * @see AssignmentServiceUtil#getExitURL(Map)
	 */
	@Test
	public void testgetExitURLCase6() throws Exception {
		Map<String, String> requestMap = new RequestMap<String,String>();
		try{
			requestMap.put(classware_hm.ROLE , "instructor");
			requestMap.put(classware_hm.CW_SERVER, "http://ezto-dev.mhecloud.mhhe.com/");
			requestMap.put(classware_hm.SECTION_ID , "PAC");
			String returnURL = assignmentServiceUtil.getExitURL(requestMap);
			assertTrue("http://ezto-dev.mhecloud.mhhe.com/connect/hmInstructorSectionHomePortal.do?sectionId=PAC".equals(returnURL));
		}catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * This method is going to test that sectionBreak question for a part question is returned properly or not
	 * @see AssignmentServiceUtil#getSectionBreakQuestion(String, String) 
	 * @throws Exception
	 */
	@Test
	public void testgetSectionBreakQuestionCase1() throws Exception {
		String qList = "";
		String itemID = "";
		try{
			assignmentServiceUtil.getSectionBreakQuestion(qList, itemID);
			assertTrue(false);
		}catch (BusinessException e) {
			assertTrue("QLIST or itemID is blank for qList :: and itemID".equals(e.getMessage()));
		}
	}
	
	/**
	 * This method is going to test that sectionBreak question for a part question is returned properly or not
	 * @see AssignmentServiceUtil#getSectionBreakQuestion(String, String) 
	 * @throws Exception
	 */
	@Test
	public void testgetSectionBreakQuestionCase2() throws Exception {
		String qList = "1;2;3;4,5,6,7";
		String itemID = "7";
		question sectionbreakQ = new sectionBreak();
		try{
			sectionbreakQ.sqlID = "1";
			sectionbreakQ.referenceTag = "1234";
			Mockito.when(cacheServiceMock.getItem("4")).thenReturn(sectionbreakQ);
			question theQ = assignmentServiceUtilMock.getSectionBreakQuestion(qList, itemID);
			assertTrue("1234".equals(theQ.referenceTag));
			assertTrue("1".equals(theQ.sqlID));
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method is going to test that sectionBreak question for a part question is returned properly or not
	 * @see AssignmentServiceUtil#getSectionBreakQuestion(String, String) 
	 * @throws Exception
	 */
	@Test
	public void testgetSectionBreakQuestionCase3() throws Exception {
		String qList = "1;2;3;4,5,6,7";
		String itemID = "7";
		question sectionbreakQ = new sectionBreak();
		try{
			sectionbreakQ.sqlID = "1";
			sectionbreakQ.referenceTag = "1234";
			Mockito.when(cacheServiceMock.getItem("4")).thenReturn(null);
			question theQ = assignmentServiceUtilMock.getSectionBreakQuestion(qList, itemID);
			assertTrue(theQ == null);
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method is going to test that sectionBreak question for a part question is returned properly or not
	 * @see AssignmentServiceUtil#getSectionBreakQuestion(String, String) 
	 * @throws Exception
	 */
	@Test
	public void testgetSectionBreakQuestionCase4() throws Exception {
		String qList = "1;2;3;4,5,6,7";
		String itemID = "1";
		question sectionbreakQ = new sectionBreak();
		try{
			sectionbreakQ.sqlID = "1";
			sectionbreakQ.referenceTag = "1234";
			Mockito.when(cacheServiceMock.getItem("4")).thenReturn(null);
			question theQ = assignmentServiceUtilMock.getSectionBreakQuestion(qList, itemID);
			assertTrue(theQ == null);
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	/**
	 * This method is going to test that one-way restricted key generated properly or not
	 * @see AssignmentServiceUtil#getOneWayRestrictKey(String)
	 * @throws Exception
	 */
	@Test
	public void testgetOneWayRestrictKeyCase1() throws Exception {
		String itemID = "1234";
		try{
			String key = assignmentServiceUtil.getOneWayRestrictKey(itemID);
			assertTrue("Q_1234_onewayrestricted".equals(key));
		}catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * This method is going to test that one-way restricted key generated properly or not
	 * @see AssignmentServiceUtil#getOneWayRestrictKey(String)
	 * @throws Exception
	 */
	@Test
	public void testgetOneWayRestrictKeyCase2() throws Exception {
		String itemID = "";
		try{
			String key = assignmentServiceUtil.getOneWayRestrictKey(itemID);
			assertTrue(StringUtils.isBlank(key));
		}catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * This method is going to test that work flow state is returned properly or not
	 * @see AssignmentServiceUtil#getWorkFlowState(ResponseTO, String)
	 * @throws Exception
	 */
	@Test
	public void testgetWorkFlowStateCase1() throws Exception {
		String qList = "10;11;12;13;5,1,2,3,4,5,6;78,79,80,81,81";
		CustomMap<String, String> testparameter = new CustomMap<String,String>();
		try{
			testparameter.replaceParam("Q_1_onewayrestricted", "true");
			testparameter.replaceParam("Q_2_onewayrestricted", "true");
			testparameter.replaceParam("Q_3_onewayrestricted", "true");
			testparameter.replaceParam("Q_4_onewayrestricted", "true");
			testparameter.replaceParam("Q_5_onewayrestricted", "true");
			testparameter.replaceParam("Q_6_onewayrestricted", "true");
			testparameter.replaceParam(CaaConstants.QLIST, qList);
			ResponseTO responseTO = new ResponseTO();
			responseTO.setTestParameter(testparameter);
			String sectionBreakQid = "5";
			int workFlowState = assignmentServiceUtil.getWorkFlowState(responseTO, sectionBreakQid);
			assertTrue(workFlowState == 6);
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method is going to test that work flow state is returned properly or not
	 * @see AssignmentServiceUtil#getWorkFlowState(ResponseTO, String)
	 * @throws Exception
	 */
	@Test
	public void testgetWorkFlowStateCase2() throws Exception {
		String qList = "10;11;12;13;5,1,2,3,4,5,6;78,79,80,81,81";
		CustomMap<String, String> testparameter = new CustomMap<String,String>();
		try{
			/*testparameter.replaceParam("Q_1_onewayrestricted", "true");
			testparameter.replaceParam("Q_2_onewayrestricted", "true");
			testparameter.replaceParam("Q_3_onewayrestricted", "true");
			testparameter.replaceParam("Q_4_onewayrestricted", "true");
			testparameter.replaceParam("Q_5_onewayrestricted", "true");
			testparameter.replaceParam("Q_6_onewayrestricted", "true");*/
			testparameter.replaceParam(CaaConstants.QLIST, qList);
			ResponseTO responseTO = new ResponseTO();
			responseTO.setTestParameter(testparameter);
			String sectionBreakQid = "5";
			int workFlowState = assignmentServiceUtil.getWorkFlowState(responseTO, sectionBreakQid);
			assertTrue(workFlowState == 0);
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method is going to test that work flow state is returned properly or not
	 * @see AssignmentServiceUtil#getWorkFlowState(ResponseTO, String)
	 * @throws Exception
	 */
	@Test
	public void testgetWorkFlowStateCase3() throws Exception {
		String qList = "10;11;12;13;78,79,80,81,81;5,1,2,3,4,5,6";
		CustomMap<String, String> testparameter = new CustomMap<String,String>();
		try{
			testparameter.replaceParam("Q_1_onewayrestricted", "true");
			testparameter.replaceParam("Q_2_onewayrestricted", "true");
			testparameter.replaceParam("Q_3_onewayrestricted", "true");
			testparameter.replaceParam("Q_4_onewayrestricted", "true");
			testparameter.replaceParam("Q_5_onewayrestricted", "true");
			testparameter.replaceParam("Q_6_onewayrestricted", "true");
			testparameter.replaceParam(CaaConstants.QLIST, qList);
			ResponseTO responseTO = new ResponseTO();
			responseTO.setTestParameter(testparameter);
			String sectionBreakQid = "5";
			int workFlowState = assignmentServiceUtil.getWorkFlowState(responseTO, sectionBreakQid);
			assertTrue(workFlowState == 6);
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method is going to test that work flow state is returned properly or not
	 * @see AssignmentServiceUtil#getWorkFlowState(ResponseTO, String)
	 * @throws Exception
	 */
	@Test
	public void testgetWorkFlowStateCase4() throws Exception {
		String qList = "10;11;12;13;78,79,80,81,81;5,1,2,3,4,5,6";
		CustomMap<String, String> testparameter = new CustomMap<String,String>();
		try{
			testparameter.replaceParam("Q_1_onewayrestricted", "true");
			testparameter.replaceParam("Q_2_onewayrestricted", "true");
			testparameter.replaceParam("Q_3_onewayrestricted", "true");
			testparameter.replaceParam(CaaConstants.QLIST, qList);
			ResponseTO responseTO = new ResponseTO();
			responseTO.setTestParameter(testparameter);
			String sectionBreakQid = "5";
			int workFlowState = assignmentServiceUtil.getWorkFlowState(responseTO, sectionBreakQid);
			assertTrue(workFlowState == 3);
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method is going to test that work flow state is returned properly or not
	 * @see AssignmentServiceUtil#getWorkFlowState(ResponseTO, String)
	 * @throws Exception
	 */
	@Test
	public void testgetWorkFlowStateCase5() throws Exception {
		String qList = "10;11;12;13;78,79,80,81,81;5,1,2,3,4,5,6";
		CustomMap<String, String> testparameter = new CustomMap<String,String>();
		try{
			testparameter.replaceParam("Q_1_onewayrestricted", "true");
			testparameter.replaceParam("Q_2_onewayrestricted", "true");
			testparameter.replaceParam("Q_3_onewayrestricted", "true");
			testparameter.replaceParam(CaaConstants.QLIST, qList);
			ResponseTO responseTO = new ResponseTO();
			responseTO.setTestParameter(testparameter);
			String sectionBreakQid = "5";
			int workFlowState = assignmentServiceUtil.getWorkFlowState(null, sectionBreakQid);
			assertTrue(workFlowState == 0);
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method is going to test that work flow state is returned properly or not
	 * @see AssignmentServiceUtil#getWorkFlowState(ResponseTO, String)
	 * @throws Exception
	 */
	@Test
	public void testgetWorkFlowStateCase6() throws Exception {
		String qList = "10;11;12;13;78,79,80,81,81;5,1,2,3,4,5,6";
		CustomMap<String, String> testparameter = new CustomMap<String,String>();
		try{
			testparameter.replaceParam("Q_1_onewayrestricted", "true");
			testparameter.replaceParam("Q_2_onewayrestricted", "true");
			testparameter.replaceParam("Q_3_onewayrestricted", "true");
			testparameter.replaceParam(CaaConstants.QLIST, qList);
			ResponseTO responseTO = new ResponseTO();
			responseTO.setTestParameter(testparameter);
			String sectionBreakQid = "5";
			int workFlowState = assignmentServiceUtil.getWorkFlowState(responseTO, null);
			assertTrue(workFlowState == 0);
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method is going to test that testParamerter is populating properly or not
	 * @see AssignmentServiceUtil#getTestParamMap(Map) 
	 * @throws Exception
	 */
	@Test
	public void testgetTestParamMapCase1() throws Exception {
		RequestMap<String, String> requestMap = new RequestMap<String,String>();
		try{
			requestMap.put(CaaConstants.SUBMISSION_ID, "1234");
			requestMap.put(classware_hm.HEADER_INSTRUCTIONS, "Instructions");
			CustomMap<String, String> testparameter = assignmentServiceUtil.getTestParamMap(requestMap);
			assertTrue(testparameter != null);
			assertTrue("1234".equals(testparameter.getParam(CaaConstants.FORMER_SUBMISSION)));
			assertTrue("Instructions".equals(testparameter.getParam(classware_hm.HEADER_INSTRUCTIONS)));
		}catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * This method is going to test that testParamerter is populating properly or not
	 * @see AssignmentServiceUtil#getTestParamMap(Map) 
	 * @throws Exception
	 */
	@Test
	public void testgetTestParamMapCase2() throws Exception {
		RequestMap<String, String> requestMap = new RequestMap<String,String>();
		try{
			//requestMap.put(CaaConstants.SUBMISSION_ID, "1234");
			//requestMap.put(classware_hm.HEADER_INSTRUCTIONS, "Instructions");
			CustomMap<String, String> testparameter = assignmentServiceUtil.getTestParamMap(requestMap);
			assertTrue(testparameter != null);
			assertTrue(StringUtils.isBlank(testparameter.getParam(CaaConstants.FORMER_SUBMISSION)));
			assertTrue(StringUtils.isBlank(testparameter.getParam(classware_hm.HEADER_INSTRUCTIONS)));
		}catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * This method is going to test that testParamerter is populating properly or not
	 * @see AssignmentServiceUtil#getTestParamMap(Map) 
	 * @throws Exception
	 */
	@Test
	public void testgetTestParamMapCase3() throws Exception {
		try{
			CustomMap<String, String> testparameter = assignmentServiceUtil.getTestParamMap(null);
			assertTrue(testparameter != null);
			assertTrue(StringUtils.isBlank(testparameter.getParam(CaaConstants.FORMER_SUBMISSION)));
			assertTrue(StringUtils.isBlank(testparameter.getParam(classware_hm.HEADER_INSTRUCTIONS)));
		}catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * This method is going to test that the QLIST is normalized
	 * @see AssignmentServiceUtil#getNormalizedQlist(TestTO, String)
	 * @throws Exception
	 */
	@Test
	public void testGetNormalizedQlistCase1() throws Exception {
		String qldQlist = "1;3,4,5;2;6;7"; 
		QuestionMetaDataTO questionMetaDataTO1 = new QuestionMetaDataTO();
		questionMetaDataTO1.setQuestionID("1");
		questionMetaDataTO1.setDefaultSequenceID(1);
		QuestionMetaDataTO questionMetaDataTO2 = new QuestionMetaDataTO();
		questionMetaDataTO2.setQuestionID("2");
		questionMetaDataTO2.setDefaultSequenceID(2);
		QuestionMetaDataTO questionMetaDataTO3 = new QuestionMetaDataTO();
		questionMetaDataTO3.setQuestionID("3");
		questionMetaDataTO3.setDefaultSequenceID(3);
		QuestionMetaDataTO questionMetaDataTO4 = new QuestionMetaDataTO();
		questionMetaDataTO4.setQuestionID("4");
		questionMetaDataTO4.setDefaultSequenceID(4);
		QuestionMetaDataTO questionMetaDataTO5 = new QuestionMetaDataTO();
		questionMetaDataTO5.setQuestionID("5");
		questionMetaDataTO5.setDefaultSequenceID(5);
		QuestionMetaDataTO questionMetaDataTO6 = new QuestionMetaDataTO();
		questionMetaDataTO6.setQuestionID("6");
		questionMetaDataTO6.setDefaultSequenceID(6);
		QuestionMetaDataTO questionMetaDataTO7 = new QuestionMetaDataTO();
		questionMetaDataTO7.setQuestionID("7");
		questionMetaDataTO7.setDefaultSequenceID(7);
		List<QuestionMetaDataTO> questionMetaDataTOList = new ArrayList<QuestionMetaDataTO>();
		questionMetaDataTOList.add(questionMetaDataTO1);
		questionMetaDataTOList.add(questionMetaDataTO2);
		questionMetaDataTOList.add(questionMetaDataTO3);
		questionMetaDataTOList.add(questionMetaDataTO4);
		questionMetaDataTOList.add(questionMetaDataTO5);
		questionMetaDataTOList.add(questionMetaDataTO6);
		questionMetaDataTOList.add(questionMetaDataTO7);
		Collections.sort(questionMetaDataTOList, new QuestionMetadataTOComparator());
		TestTO testTO = new TestTO();
		testTO.setQuestionMetaDataList(questionMetaDataTOList);
		try{
			String normalizedQlist = assignmentServiceUtil.getNormalizedQlist(testTO, qldQlist);
			assertTrue("1;2;3,4,5;6;7".equals(normalizedQlist));
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}
	
	
	/**
	 * This method is going to test that the QLIST is normalized
	 * @see AssignmentServiceUtil#getNormalizedQlist(TestTO, String)
	 * @throws Exception
	 */
	@Test
	public void testGetNormalizedQlistCase2() throws Exception {
		String qldQlist = "1;3,4,5;2;8;6;7"; 
		QuestionMetaDataTO questionMetaDataTO1 = new QuestionMetaDataTO();
		questionMetaDataTO1.setQuestionID("1");
		questionMetaDataTO1.setDefaultSequenceID(1);
		QuestionMetaDataTO questionMetaDataTO2 = new QuestionMetaDataTO();
		questionMetaDataTO2.setQuestionID("2");
		questionMetaDataTO2.setDefaultSequenceID(2);
		QuestionMetaDataTO questionMetaDataTO3 = new QuestionMetaDataTO();
		questionMetaDataTO3.setQuestionID("3");
		questionMetaDataTO3.setDefaultSequenceID(3);
		QuestionMetaDataTO questionMetaDataTO4 = new QuestionMetaDataTO();
		questionMetaDataTO4.setQuestionID("4");
		questionMetaDataTO4.setDefaultSequenceID(4);
		QuestionMetaDataTO questionMetaDataTO5 = new QuestionMetaDataTO();
		questionMetaDataTO5.setQuestionID("5");
		questionMetaDataTO5.setDefaultSequenceID(5);
		QuestionMetaDataTO questionMetaDataTO6 = new QuestionMetaDataTO();
		questionMetaDataTO6.setQuestionID("6");
		questionMetaDataTO6.setDefaultSequenceID(6);
		QuestionMetaDataTO questionMetaDataTO7 = new QuestionMetaDataTO();
		questionMetaDataTO7.setQuestionID("7");
		questionMetaDataTO7.setDefaultSequenceID(7);
		QuestionMetaDataTO questionMetaDataTO8 = new QuestionMetaDataTO();
		questionMetaDataTO8.setQuestionID("8");
		questionMetaDataTO8.setDefaultSequenceID(8);
		QuestionMetaDataTO questionMetaDataTO9 = new QuestionMetaDataTO();
		questionMetaDataTO9.setQuestionID("9");
		questionMetaDataTO9.setDefaultSequenceID(9);
		List<QuestionMetaDataTO> questionMetaDataTOList = new ArrayList<QuestionMetaDataTO>();
		questionMetaDataTOList.add(questionMetaDataTO1);
		questionMetaDataTOList.add(questionMetaDataTO2);
		questionMetaDataTOList.add(questionMetaDataTO3);
		questionMetaDataTOList.add(questionMetaDataTO4);
		questionMetaDataTOList.add(questionMetaDataTO5);
		questionMetaDataTOList.add(questionMetaDataTO6);
		questionMetaDataTOList.add(questionMetaDataTO7);
		questionMetaDataTOList.add(questionMetaDataTO8);
		questionMetaDataTOList.add(questionMetaDataTO9);
		Collections.sort(questionMetaDataTOList, new QuestionMetadataTOComparator());
		TestTO testTO = new TestTO();
		testTO.setQuestionMetaDataList(questionMetaDataTOList);
		try{
			String normalizedQlist = assignmentServiceUtil.getNormalizedQlist(testTO, qldQlist);
			assertTrue("1;2;3,4,5;6;7;8".equals(normalizedQlist));
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method is going to test that the QLIST is normalized
	 * @see AssignmentServiceUtil#getNormalizedQlist(TestTO, String)
	 * @throws Exception
	 */
	@Test
	public void testGetNormalizedQlistCase3() throws Exception {
		String qldQlist = "1;3,5;2;8;6;7"; 
		QuestionMetaDataTO questionMetaDataTO1 = new QuestionMetaDataTO();
		questionMetaDataTO1.setQuestionID("1");
		questionMetaDataTO1.setDefaultSequenceID(1);
		QuestionMetaDataTO questionMetaDataTO2 = new QuestionMetaDataTO();
		questionMetaDataTO2.setQuestionID("2");
		questionMetaDataTO2.setDefaultSequenceID(2);
		QuestionMetaDataTO questionMetaDataTO3 = new QuestionMetaDataTO();
		questionMetaDataTO3.setQuestionID("3");
		questionMetaDataTO3.setDefaultSequenceID(3);
		QuestionMetaDataTO questionMetaDataTO4 = new QuestionMetaDataTO();
		questionMetaDataTO4.setQuestionID("4");
		questionMetaDataTO4.setDefaultSequenceID(4);
		QuestionMetaDataTO questionMetaDataTO5 = new QuestionMetaDataTO();
		questionMetaDataTO5.setQuestionID("5");
		questionMetaDataTO5.setDefaultSequenceID(5);
		QuestionMetaDataTO questionMetaDataTO6 = new QuestionMetaDataTO();
		questionMetaDataTO6.setQuestionID("6");
		questionMetaDataTO6.setDefaultSequenceID(6);
		QuestionMetaDataTO questionMetaDataTO7 = new QuestionMetaDataTO();
		questionMetaDataTO7.setQuestionID("7");
		questionMetaDataTO7.setDefaultSequenceID(7);
		QuestionMetaDataTO questionMetaDataTO8 = new QuestionMetaDataTO();
		questionMetaDataTO8.setQuestionID("8");
		questionMetaDataTO8.setDefaultSequenceID(8);
		QuestionMetaDataTO questionMetaDataTO9 = new QuestionMetaDataTO();
		questionMetaDataTO9.setQuestionID("9");
		questionMetaDataTO9.setDefaultSequenceID(9);
		List<QuestionMetaDataTO> questionMetaDataTOList = new ArrayList<QuestionMetaDataTO>();
		questionMetaDataTOList.add(questionMetaDataTO1);
		questionMetaDataTOList.add(questionMetaDataTO2);
		questionMetaDataTOList.add(questionMetaDataTO3);
		questionMetaDataTOList.add(questionMetaDataTO4);
		questionMetaDataTOList.add(questionMetaDataTO5);
		questionMetaDataTOList.add(questionMetaDataTO6);
		questionMetaDataTOList.add(questionMetaDataTO7);
		questionMetaDataTOList.add(questionMetaDataTO8);
		questionMetaDataTOList.add(questionMetaDataTO9);
		Collections.sort(questionMetaDataTOList, new QuestionMetadataTOComparator());
		TestTO testTO = new TestTO();
		testTO.setQuestionMetaDataList(questionMetaDataTOList);
		try{
			String normalizedQlist = assignmentServiceUtil.getNormalizedQlist(testTO, qldQlist);
			assertTrue("1;2;3,5;6;7;8".equals(normalizedQlist));
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method is going to test that the QLIST is normalized
	 * @see AssignmentServiceUtil#getNormalizedQlist(TestTO, String)
	 * @throws Exception
	 */
	@Test
	public void testGetNormalizedQlistCase4() throws Exception {
		String qldQlist = "1;3,5;2;8;6;7;9"; 
		QuestionMetaDataTO questionMetaDataTO1 = new QuestionMetaDataTO();
		questionMetaDataTO1.setQuestionID("1");
		questionMetaDataTO1.setDefaultSequenceID(1);
		QuestionMetaDataTO questionMetaDataTO2 = new QuestionMetaDataTO();
		questionMetaDataTO2.setQuestionID("2");
		questionMetaDataTO2.setDefaultSequenceID(2);
		QuestionMetaDataTO questionMetaDataTO3 = new QuestionMetaDataTO();
		questionMetaDataTO3.setQuestionID("3");
		questionMetaDataTO3.setDefaultSequenceID(3);
		QuestionMetaDataTO questionMetaDataTO4 = new QuestionMetaDataTO();
		questionMetaDataTO4.setQuestionID("4");
		questionMetaDataTO4.setDefaultSequenceID(4);
		QuestionMetaDataTO questionMetaDataTO5 = new QuestionMetaDataTO();
		questionMetaDataTO5.setQuestionID("5");
		questionMetaDataTO5.setDefaultSequenceID(5);
		QuestionMetaDataTO questionMetaDataTO6 = new QuestionMetaDataTO();
		questionMetaDataTO6.setQuestionID("6");
		questionMetaDataTO6.setDefaultSequenceID(6);
		QuestionMetaDataTO questionMetaDataTO7 = new QuestionMetaDataTO();
		questionMetaDataTO7.setQuestionID("7");
		questionMetaDataTO7.setDefaultSequenceID(7);
		QuestionMetaDataTO questionMetaDataTO8 = new QuestionMetaDataTO();
		questionMetaDataTO8.setQuestionID("8");
		questionMetaDataTO8.setDefaultSequenceID(8);
		QuestionMetaDataTO questionMetaDataTO9 = new QuestionMetaDataTO();
		questionMetaDataTO9.setQuestionID("9");
		questionMetaDataTO9.setDefaultSequenceID(9);
		QuestionMetaDataTO questionMetaDataTO10 = new QuestionMetaDataTO();
		questionMetaDataTO10.setQuestionID("9");
		questionMetaDataTO10.setDefaultSequenceID(10);
		QuestionMetaDataTO questionMetaDataTO11 = new QuestionMetaDataTO();
		questionMetaDataTO11.setQuestionID("11");
		questionMetaDataTO11.setDefaultSequenceID(11);
		List<QuestionMetaDataTO> questionMetaDataTOList = new ArrayList<QuestionMetaDataTO>();
		questionMetaDataTOList.add(questionMetaDataTO1);
		questionMetaDataTOList.add(questionMetaDataTO2);
		questionMetaDataTOList.add(questionMetaDataTO3);
		questionMetaDataTOList.add(questionMetaDataTO4);
		questionMetaDataTOList.add(questionMetaDataTO5);
		questionMetaDataTOList.add(questionMetaDataTO6);
		questionMetaDataTOList.add(questionMetaDataTO7);
		questionMetaDataTOList.add(questionMetaDataTO8);
		questionMetaDataTOList.add(questionMetaDataTO9);
		questionMetaDataTOList.add(questionMetaDataTO10);
		questionMetaDataTOList.add(questionMetaDataTO11);
		Collections.sort(questionMetaDataTOList, new QuestionMetadataTOComparator());
		TestTO testTO = new TestTO();
		testTO.setQuestionMetaDataList(questionMetaDataTOList);
		try{
			String normalizedQlist = assignmentServiceUtil.getNormalizedQlist(testTO, qldQlist);
			assertTrue("1;2;3,5;6;7;8;9".equals(normalizedQlist));
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method is going to test that the QLIST is normalized
	 * @see AssignmentServiceUtil#getNormalizedQlist(TestTO, String)
	 * @throws Exception
	 */
	@Test
	public void testGetNormalizedQlistCase5() throws Exception {
		String qldQlist = "1;3,5;2;8;6;7;9"; 
		QuestionMetaDataTO questionMetaDataTO3 = new QuestionMetaDataTO();
		questionMetaDataTO3.setQuestionID("3");
		questionMetaDataTO3.setDefaultSequenceID(3);
		QuestionMetaDataTO questionMetaDataTO4 = new QuestionMetaDataTO();
		questionMetaDataTO4.setQuestionID("4");
		questionMetaDataTO4.setDefaultSequenceID(4);
		QuestionMetaDataTO questionMetaDataTO5 = new QuestionMetaDataTO();
		questionMetaDataTO5.setQuestionID("5");
		questionMetaDataTO5.setDefaultSequenceID(5);
		QuestionMetaDataTO questionMetaDataTO6 = new QuestionMetaDataTO();
		questionMetaDataTO6.setQuestionID("6");
		questionMetaDataTO6.setDefaultSequenceID(6);
		QuestionMetaDataTO questionMetaDataTO7 = new QuestionMetaDataTO();
		questionMetaDataTO7.setQuestionID("7");
		questionMetaDataTO7.setDefaultSequenceID(7);
		QuestionMetaDataTO questionMetaDataTO8 = new QuestionMetaDataTO();
		questionMetaDataTO8.setQuestionID("8");
		questionMetaDataTO8.setDefaultSequenceID(8);
		QuestionMetaDataTO questionMetaDataTO9 = new QuestionMetaDataTO();
		questionMetaDataTO9.setQuestionID("9");
		questionMetaDataTO9.setDefaultSequenceID(9);
		QuestionMetaDataTO questionMetaDataTO10 = new QuestionMetaDataTO();
		questionMetaDataTO10.setQuestionID("9");
		questionMetaDataTO10.setDefaultSequenceID(10);
		QuestionMetaDataTO questionMetaDataTO11 = new QuestionMetaDataTO();
		questionMetaDataTO11.setQuestionID("11");
		questionMetaDataTO11.setDefaultSequenceID(11);
		List<QuestionMetaDataTO> questionMetaDataTOList = new ArrayList<QuestionMetaDataTO>();
		questionMetaDataTOList.add(questionMetaDataTO3);
		questionMetaDataTOList.add(questionMetaDataTO4);
		questionMetaDataTOList.add(questionMetaDataTO5);
		questionMetaDataTOList.add(questionMetaDataTO6);
		questionMetaDataTOList.add(questionMetaDataTO7);
		questionMetaDataTOList.add(questionMetaDataTO8);
		questionMetaDataTOList.add(questionMetaDataTO9);
		questionMetaDataTOList.add(questionMetaDataTO10);
		questionMetaDataTOList.add(questionMetaDataTO11);
		Collections.sort(questionMetaDataTOList, new QuestionMetadataTOComparator());
		TestTO testTO = new TestTO();
		testTO.setQuestionMetaDataList(questionMetaDataTOList);
		try{
			String normalizedQlist = assignmentServiceUtil.getNormalizedQlist(testTO, qldQlist);
			assertTrue("3,5;6;7;8;9".equals(normalizedQlist));
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method is going to test that the QLIST is normalized
	 * @see AssignmentServiceUtil#getNormalizedQlist(TestTO, String)
	 * @throws Exception
	 */
	@Test
	public void testGetNormalizedQlistCase6() throws Exception {
		String qldQlist = "1;3,5;2;8;6;7;9"; 
		QuestionMetaDataTO questionMetaDataTO3 = new QuestionMetaDataTO();
		questionMetaDataTO3.setQuestionID("3");
		questionMetaDataTO3.setDefaultSequenceID(3);
		QuestionMetaDataTO questionMetaDataTO4 = new QuestionMetaDataTO();
		questionMetaDataTO4.setQuestionID("4");
		questionMetaDataTO4.setDefaultSequenceID(4);
		QuestionMetaDataTO questionMetaDataTO5 = new QuestionMetaDataTO();
		questionMetaDataTO5.setQuestionID("5");
		questionMetaDataTO5.setDefaultSequenceID(5);
		QuestionMetaDataTO questionMetaDataTO6 = new QuestionMetaDataTO();
		questionMetaDataTO6.setQuestionID("6");
		questionMetaDataTO6.setDefaultSequenceID(6);
		QuestionMetaDataTO questionMetaDataTO7 = new QuestionMetaDataTO();
		questionMetaDataTO7.setQuestionID("7");
		questionMetaDataTO7.setDefaultSequenceID(7);
		QuestionMetaDataTO questionMetaDataTO8 = new QuestionMetaDataTO();
		questionMetaDataTO8.setQuestionID("8");
		questionMetaDataTO8.setDefaultSequenceID(8);
		QuestionMetaDataTO questionMetaDataTO9 = new QuestionMetaDataTO();
		questionMetaDataTO9.setQuestionID("9");
		questionMetaDataTO9.setDefaultSequenceID(9);
		QuestionMetaDataTO questionMetaDataTO10 = new QuestionMetaDataTO();
		questionMetaDataTO10.setQuestionID("9");
		questionMetaDataTO10.setDefaultSequenceID(10);
		QuestionMetaDataTO questionMetaDataTO11 = new QuestionMetaDataTO();
		questionMetaDataTO11.setQuestionID("11");
		questionMetaDataTO11.setDefaultSequenceID(11);
		List<QuestionMetaDataTO> questionMetaDataTOList = new ArrayList<QuestionMetaDataTO>();
		questionMetaDataTOList.add(questionMetaDataTO3);
		questionMetaDataTOList.add(questionMetaDataTO4);
		questionMetaDataTOList.add(questionMetaDataTO5);
		questionMetaDataTOList.add(questionMetaDataTO6);
		questionMetaDataTOList.add(questionMetaDataTO7);
		questionMetaDataTOList.add(questionMetaDataTO8);
		questionMetaDataTOList.add(questionMetaDataTO9);
		questionMetaDataTOList.add(questionMetaDataTO10);
		questionMetaDataTOList.add(questionMetaDataTO11);
		Collections.sort(questionMetaDataTOList, new QuestionMetadataTOComparator());
		TestTO testTO = new TestTO();
		testTO.setQuestionMetaDataList(null);
		try{
			String normalizedQlist = assignmentServiceUtil.getNormalizedQlist(testTO, qldQlist);
			assertTrue(StringUtils.isBlank(normalizedQlist));
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * THis method is going to generate test Object with arranged in the order of sequence number
	 * @return
	 */
	public TestTO getTestObj(){
		QuestionMetaDataTO questionMetaDataTO1 = new QuestionMetaDataTO();
		questionMetaDataTO1.setQuestionID("1");
		questionMetaDataTO1.setDefaultSequenceID(1);
		QuestionMetaDataTO questionMetaDataTO2 = new QuestionMetaDataTO();
		questionMetaDataTO2.setQuestionID("2");
		questionMetaDataTO2.setDefaultSequenceID(2);
		QuestionMetaDataTO questionMetaDataTO3 = new QuestionMetaDataTO();
		questionMetaDataTO3.setQuestionID("3");
		questionMetaDataTO3.setDefaultSequenceID(3);
		QuestionMetaDataTO questionMetaDataTO4 = new QuestionMetaDataTO();
		questionMetaDataTO4.setQuestionID("4");
		questionMetaDataTO4.setDefaultSequenceID(4);
		QuestionMetaDataTO questionMetaDataTO5 = new QuestionMetaDataTO();
		questionMetaDataTO5.setQuestionID("5");
		questionMetaDataTO5.setDefaultSequenceID(5);
		QuestionMetaDataTO questionMetaDataTO6 = new QuestionMetaDataTO();
		questionMetaDataTO6.setQuestionID("6");
		questionMetaDataTO6.setDefaultSequenceID(6);
		QuestionMetaDataTO questionMetaDataTO7 = new QuestionMetaDataTO();
		questionMetaDataTO7.setQuestionID("7");
		questionMetaDataTO7.setDefaultSequenceID(7);
		QuestionMetaDataTO questionMetaDataTO8 = new QuestionMetaDataTO();
		questionMetaDataTO8.setQuestionID("8");
		questionMetaDataTO8.setDefaultSequenceID(8);
		QuestionMetaDataTO questionMetaDataTO9 = new QuestionMetaDataTO();
		questionMetaDataTO9.setQuestionID("9");
		questionMetaDataTO9.setDefaultSequenceID(9);
		QuestionMetaDataTO questionMetaDataTO10 = new QuestionMetaDataTO();
		questionMetaDataTO10.setQuestionID("9");
		questionMetaDataTO10.setDefaultSequenceID(10);
		QuestionMetaDataTO questionMetaDataTO11 = new QuestionMetaDataTO();
		questionMetaDataTO11.setQuestionID("11");
		questionMetaDataTO11.setDefaultSequenceID(11);
		List<QuestionMetaDataTO> questionMetaDataTOList = new ArrayList<QuestionMetaDataTO>();
		questionMetaDataTOList.add(questionMetaDataTO1);
		questionMetaDataTOList.add(questionMetaDataTO2);
		questionMetaDataTOList.add(questionMetaDataTO3);
		questionMetaDataTOList.add(questionMetaDataTO4);
		questionMetaDataTOList.add(questionMetaDataTO5);
		questionMetaDataTOList.add(questionMetaDataTO6);
		questionMetaDataTOList.add(questionMetaDataTO7);
		questionMetaDataTOList.add(questionMetaDataTO8);
		questionMetaDataTOList.add(questionMetaDataTO9);
		questionMetaDataTOList.add(questionMetaDataTO10);
		questionMetaDataTOList.add(questionMetaDataTO11);
		Collections.sort(questionMetaDataTOList, new QuestionMetadataTOComparator());
		TestTO testTO = new TestTO();
		testTO.setQuestionMetaDataList(questionMetaDataTOList);
		return testTO;
	}
	/**
	 * This method is going to test that QLIST is populated properly
	 * or not
	 * @see AssignmentServiceUtil#populateQList(TestTO, UserResponseWithPolicyTO, String)
	 * @throws Exception
	 */
	@Test
	public void testpopulateQListCase1() throws Exception {
		String userID = "12345";
		String qlist = "1;3,5;2;8;6;7";
		String expectedQlist = "1;2;3,5;6;7;8";
		String role = classware_hm.ROLE_INSTRUCTOR_PRIMARY;
		CustomMap<String, String> testParameter = new CustomMap<String,String>();
		Map<String,String> policyMap = new HashMap<String, String>();
		ResponseTO responseTO = new ResponseTO();
		PolicyTO policyTO = new PolicyTO();
		UserResponseWithPolicyTO userResponseWithPolicyTO = new UserResponseWithPolicyTO();
		try{
			testParameter.replaceParam(CaaConstants.QLIST, qlist);
			policyMap.put(classware_hm.POLICY_scrambling, CaaConstants.YES);
			policyTO.setPolicyMap(policyMap);
			responseTO.setTestParameter(testParameter);
			responseTO.setUserID(userID);
			userResponseWithPolicyTO.setPolicyTO(policyTO);
			userResponseWithPolicyTO.setResponseTO(responseTO);
			TestTO testTO = getTestObj();
			assignmentServiceUtilMock.populateQList(testTO, userResponseWithPolicyTO, role);
			assertTrue(expectedQlist.equals(responseTO.getParam(CaaConstants.QLIST)));
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method is going to test that QLIST is populated properly
	 * or not
	 * @see AssignmentServiceUtil#populateQList(TestTO, UserResponseWithPolicyTO, String)
	 * @throws Exception
	 */
	@Test
	public void testpopulateQListCase2() throws Exception {
		String userID = "12345";
		String qlist = "1;3,5;2;8;6;7";
		String expectedQlist = "1;2;3,5;6;7;8";
		String role = classware_hm.ROLE_GRADER;
		CustomMap<String, String> testParameter = new CustomMap<String,String>();
		Map<String,String> policyMap = new HashMap<String, String>();
		ResponseTO responseTO = new ResponseTO();
		PolicyTO policyTO = new PolicyTO();
		UserResponseWithPolicyTO userResponseWithPolicyTO = new UserResponseWithPolicyTO();
		try{
			testParameter.replaceParam(CaaConstants.QLIST, qlist);
			policyMap.put(classware_hm.POLICY_scrambling, CaaConstants.YES);
			policyTO.setPolicyMap(policyMap);
			responseTO.setTestParameter(testParameter);
			responseTO.setUserID(userID);
			userResponseWithPolicyTO.setPolicyTO(policyTO);
			userResponseWithPolicyTO.setResponseTO(responseTO);
			TestTO testTO = getTestObj();
			assignmentServiceUtilMock.populateQList(testTO, userResponseWithPolicyTO, role);
			assertTrue(expectedQlist.equals(responseTO.getParam(CaaConstants.QLIST)));
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method is going to test that QLIST is populated properly
	 * or not
	 * @see AssignmentServiceUtil#populateQList(TestTO, UserResponseWithPolicyTO, String)
	 * @throws Exception
	 */
	@Test
	public void testpopulateQListCase3() throws Exception {
		String userID = classware_hm.ROLE_INSTRUCTOR_ID;
		String qlist = "1;3,5;2;8;6;7";
		String role = classware_hm.ROLE_INSTRUCTOR_PRIMARY;
		CustomMap<String, String> testParameter = new CustomMap<String,String>();
		Map<String,String> policyMap = new HashMap<String, String>();
		ResponseTO responseTO = new ResponseTO();
		PolicyTO policyTO = new PolicyTO();
		UserResponseWithPolicyTO userResponseWithPolicyTO = new UserResponseWithPolicyTO();
		try{
			testParameter.replaceParam(CaaConstants.QLIST, qlist);
			policyMap.put(classware_hm.POLICY_scrambling, CaaConstants.YES);
			policyTO.setPolicyMap(policyMap);
			responseTO.setTestParameter(testParameter);
			responseTO.setUserID(userID);
			userResponseWithPolicyTO.setPolicyTO(policyTO);
			userResponseWithPolicyTO.setResponseTO(responseTO);
			TestTO testTO = getTestObj();
			assignmentServiceUtilMock.populateQList(testTO, userResponseWithPolicyTO, role);
			assertTrue(qlist.equals(responseTO.getParam(CaaConstants.QLIST)));
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method is going to test that QLIST is populated properly
	 * or not
	 * @see AssignmentServiceUtil#populateQList(TestTO, UserResponseWithPolicyTO, String)
	 * @throws Exception
	 */
	@Test
	public void testpopulateQListCase4() throws Exception {
		String userID = "12345";
		String qlist = "1;3,5;2;8;6;7";
		String expectedQlist = "1;2;3,5;6;7;8";
		String role = classware_hm.ROLE_INSTRUCTOR_PRIMARY;
		CustomMap<String, String> testParameter = new CustomMap<String,String>();
		Map<String,String> policyMap = new HashMap<String, String>();
		ResponseTO responseTO = new ResponseTO();
		PolicyTO policyTO = new PolicyTO();
		UserResponseWithPolicyTO userResponseWithPolicyTO = new UserResponseWithPolicyTO();
		try{
			testParameter.replaceParam(CaaConstants.QLIST, qlist);
			policyTO.setPolicyMap(policyMap);
			responseTO.setTestParameter(testParameter);
			responseTO.setUserID(userID);
			userResponseWithPolicyTO.setPolicyTO(policyTO);
			userResponseWithPolicyTO.setResponseTO(responseTO);
			TestTO testTO = getTestObj();
			Mockito.when(tagUtilMock.isPooled(Mockito.anyList())).thenReturn(true);
			assignmentServiceUtilMock.populateQList(testTO, userResponseWithPolicyTO, role);
			assertTrue(expectedQlist.equals(responseTO.getParam(CaaConstants.QLIST)));
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method is going to test that QLIST is populated properly
	 * or not
	 * @see AssignmentServiceUtil#populateQList(TestTO, UserResponseWithPolicyTO, String)
	 * @throws Exception
	 */
	@Test
	public void testpopulateQListCase5() throws Exception {
		String userID = "12345";
		String qlist = "1;3,5;2;8;6;7";
		String expectedQlist = "1;2;3,5;6;7;8";
		String role = classware_hm.ROLE_INSTRUCTOR_PRIMARY;
		CustomMap<String, String> testParameter = new CustomMap<String,String>();
		ResponseTO responseTO = new ResponseTO();
		PolicyTO policyTO = new PolicyTO();
		UserResponseWithPolicyTO userResponseWithPolicyTO = new UserResponseWithPolicyTO();
		try{
			testParameter.replaceParam(CaaConstants.QLIST, qlist);
			policyTO.setPolicyMap(null);
			responseTO.setTestParameter(testParameter);
			responseTO.setUserID(userID);
			userResponseWithPolicyTO.setPolicyTO(policyTO);
			userResponseWithPolicyTO.setResponseTO(responseTO);
			TestTO testTO = getTestObj();
			assignmentServiceUtilMock.populateQList(testTO, userResponseWithPolicyTO, role);
			assertTrue(qlist.equals(responseTO.getParam(CaaConstants.QLIST)));
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method is going to test that QLIST is populated properly
	 * or not
	 * @see AssignmentServiceUtil#populateQList(TestTO, UserResponseWithPolicyTO, String)
	 * @throws Exception
	 */
	@Test
	public void testpopulateQListCase6() throws Exception {
		String userID = "12345";
		String qlist = "1;3,5;2;8;6;7";
		String expectedQlist = "1;2;3,5;6;7;8";
		String role = classware_hm.ROLE_INSTRUCTOR_PRIMARY;
		CustomMap<String, String> testParameter = new CustomMap<String,String>();
		Map<String,String> policyMap = new HashMap<String, String>();
		ResponseTO responseTO = new ResponseTO();
		PolicyTO policyTO = new PolicyTO();
		UserResponseWithPolicyTO userResponseWithPolicyTO = new UserResponseWithPolicyTO();
		try{
			testParameter.replaceParam(CaaConstants.QLIST, qlist);
			policyMap.put(classware_hm.POLICY_scrambling, CaaConstants.YES);
			policyTO.setPolicyMap(policyMap);
			responseTO.setTestParameter(testParameter);
			responseTO.setUserID(userID);
			userResponseWithPolicyTO.setPolicyTO(policyTO);
			userResponseWithPolicyTO.setResponseTO(responseTO);
			TestTO testTO = getTestObj();
			Mockito.when(tagUtilMock.isPooled(Mockito.anyList())).thenReturn(true);
			assignmentServiceUtilMock.populateQList(testTO, userResponseWithPolicyTO, role);
			assertTrue(expectedQlist.equals(responseTO.getParam(CaaConstants.QLIST)));
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method is going to test that QLIST is populated properly
	 * or not
	 * @see AssignmentServiceUtil#populateQList(TestTO, UserResponseWithPolicyTO, String)
	 * @throws Exception
	 */
	@Test
	public void testpopulateQListCase7() throws Exception {
		String userID = "12345";
		String qlist = "1;3,5;2;8;6;7";
		String role = classware_hm.ROLE_INSTRUCTOR_PRIMARY;
		CustomMap<String, String> testParameter = new CustomMap<String,String>();
		Map<String,String> policyMap = new HashMap<String, String>();
		ResponseTO responseTO = new ResponseTO();
		PolicyTO policyTO = new PolicyTO();
		UserResponseWithPolicyTO userResponseWithPolicyTO = new UserResponseWithPolicyTO();
		try{
			testParameter.replaceParam(CaaConstants.QLIST, qlist);
			policyMap.put(classware_hm.POLICY_scrambling, CaaConstants.YES);
			policyTO.setPolicyMap(policyMap);
			responseTO.setTestParameter(testParameter);
			responseTO.setUserID(userID);
			userResponseWithPolicyTO.setPolicyTO(policyTO);
			userResponseWithPolicyTO.setResponseTO(responseTO);
			Mockito.when(tagUtilMock.isPooled(Mockito.anyList())).thenReturn(true);
			assignmentServiceUtilMock.populateQList(null, userResponseWithPolicyTO, role);
			assertTrue(qlist.equals(responseTO.getParam(CaaConstants.QLIST)));
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method is going to test that QLIST is populated properly
	 * or not
	 * @see AssignmentServiceUtil#populateQList(TestTO, UserResponseWithPolicyTO, String)
	 * @throws Exception
	 */
	@Test
	public void testpopulateQListCase8() throws Exception {
		String userID = "12345";
		String qlist = "1;3,5;2;8;6;7";
		String role = classware_hm.ROLE_INSTRUCTOR_PRIMARY;
		CustomMap<String, String> testParameter = new CustomMap<String,String>();
		Map<String,String> policyMap = new HashMap<String, String>();
		ResponseTO responseTO = new ResponseTO();
		PolicyTO policyTO = new PolicyTO();
		UserResponseWithPolicyTO userResponseWithPolicyTO = new UserResponseWithPolicyTO();
		try{
			testParameter.replaceParam(CaaConstants.QLIST, qlist);
			policyMap.put(classware_hm.POLICY_scrambling, CaaConstants.YES);
			policyTO.setPolicyMap(policyMap);
			responseTO.setTestParameter(testParameter);
			responseTO.setUserID(userID);
			userResponseWithPolicyTO.setPolicyTO(policyTO);
			userResponseWithPolicyTO.setResponseTO(null);
			TestTO testTO = getTestObj();
			Mockito.when(tagUtilMock.isPooled(Mockito.anyList())).thenReturn(true);
			assignmentServiceUtilMock.populateQList(testTO, userResponseWithPolicyTO, role);
			assertTrue(qlist.equals(responseTO.getParam(CaaConstants.QLIST)));
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method is going to test that QLIST is populated properly
	 * or not
	 * @see AssignmentServiceUtil#populateQList(TestTO, UserResponseWithPolicyTO, String)
	 * @throws Exception
	 */
	@Test
	public void testpopulateQListCase9() throws Exception {
		String userID = "12345";
		String qlist = "1;3,5;2;8;6;7";
		String expectedQlist = "1;2;3,5;6;7;8";
		String role = classware_hm.ROLE_INSTRUCTOR_PRIMARY;
		CustomMap<String, String> testParameter = new CustomMap<String,String>();
		Map<String,String> policyMap = new HashMap<String, String>();
		ResponseTO responseTO = new ResponseTO();
		PolicyTO policyTO = new PolicyTO();
		UserResponseWithPolicyTO userResponseWithPolicyTO = new UserResponseWithPolicyTO();
		try{
			testParameter.replaceParam(CaaConstants.QLIST, qlist);
			policyMap.put(classware_hm.POLICY_scrambling, CaaConstants.YES);
			policyTO.setPolicyMap(policyMap);
			responseTO.setTestParameter(testParameter);
			responseTO.setUserID(userID);
			userResponseWithPolicyTO.setPolicyTO(null);
			userResponseWithPolicyTO.setResponseTO(responseTO);
			TestTO testTO = getTestObj();
			Mockito.when(tagUtilMock.isPooled(Mockito.anyList())).thenReturn(true);
			assignmentServiceUtilMock.populateQList(testTO, userResponseWithPolicyTO, role);
			assertTrue(expectedQlist.equals(responseTO.getParam(CaaConstants.QLIST)));
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method is going to test that QLIST is populated properly
	 * or not
	 * @see AssignmentServiceUtil#populateQList(TestTO, UserResponseWithPolicyTO, String)
	 * @throws Exception
	 */
	@Test
	public void testpopulateQListCase10() throws Exception {
		String userID = "12345";
		String qlist = "1;3,5;2;8;6;7";
		String expectedQlist = "1;2;3,5;6;7;8";
		String role = classware_hm.ROLE_INSTRUCTOR_PRIMARY;
		CustomMap<String, String> testParameter = new CustomMap<String,String>();
		Map<String,String> policyMap = new HashMap<String, String>();
		ResponseTO responseTO = new ResponseTO();
		PolicyTO policyTO = new PolicyTO();
		UserResponseWithPolicyTO userResponseWithPolicyTO = new UserResponseWithPolicyTO();
		try{
			testParameter.replaceParam(CaaConstants.QLIST, qlist);
			policyMap.put(classware_hm.POLICY_scrambling, CaaConstants.YES);
			policyTO.setPolicyMap(null);
			responseTO.setTestParameter(testParameter);
			responseTO.setUserID(userID);
			userResponseWithPolicyTO.setPolicyTO(policyTO);
			userResponseWithPolicyTO.setResponseTO(responseTO);
			TestTO testTO = getTestObj();
			Mockito.when(tagUtilMock.isPooled(Mockito.anyList())).thenReturn(true);
			assignmentServiceUtilMock.populateQList(testTO, userResponseWithPolicyTO, role);
			assertTrue(expectedQlist.equals(responseTO.getParam(CaaConstants.QLIST)));
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method is going to test that QLIST is populated properly
	 * or not
	 * @see AssignmentServiceUtil#populateQList(TestTO, UserResponseWithPolicyTO, String)
	 * @throws Exception
	 */
	@Test
	public void testpopulateQListCase11() throws Exception {
		String userID = "12345";
		String qlist = "1;3,5;2;8;6;7";
		String expectedQlist = "1;2;3,5;6;7;8";
		String role = classware_hm.ROLE_INSTRUCTOR_PRIMARY;
		CustomMap<String, String> testParameter = new CustomMap<String,String>();
		Map<String,String> policyMap = new HashMap<String, String>();
		ResponseTO responseTO = new ResponseTO();
		PolicyTO policyTO = new PolicyTO();
		UserResponseWithPolicyTO userResponseWithPolicyTO = new UserResponseWithPolicyTO();
		try{
			testParameter.replaceParam(CaaConstants.QLIST, qlist);
			policyMap.put(classware_hm.POLICY_scrambling, CaaConstants.YES);
			policyTO.setPolicyMap(null);
			responseTO.setTestParameter(testParameter);
			responseTO.setUserID(userID);
			userResponseWithPolicyTO.setPolicyTO(policyTO);
			userResponseWithPolicyTO.setResponseTO(responseTO);
			TestTO testTO = getTestObj();
			Mockito.when(tagUtilMock.isPooled(Mockito.anyList())).thenReturn(false);
			assignmentServiceUtilMock.populateQList(testTO, userResponseWithPolicyTO, role);
			assertTrue(qlist.equals(responseTO.getParam(CaaConstants.QLIST)));
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}
	
	/**
	 * This method is going to test that LTI informations are populated properly or not
	 * @see AssignmentServiceUtil#getLtiInfo(String)
	 * @throws Exception
	 */
	@Test
	public void testgetLtiInfoCase1() throws Exception {
		String featureName = "ezto-devs_lti";
		Map<String,String> ltiMap = assignmentServiceUtil.getLtiInfo(featureName);
		assertTrue(ltiMap.size() == 0);
	}
	
	/**
	 * This method is going to test that LTI informations are populated properly or not
	 * @see AssignmentServiceUtil#getLtiInfo(String)
	 * @throws Exception
	 */
	@Test
	public void testgetLtiInfoCase2() throws Exception {
		String featureName = "lti|ezto-devs_lti";
		Map<String,String> ltiMap = assignmentServiceUtil.getLtiInfo(featureName);
		assertTrue(ltiMap.size() == 2);
		assertTrue("lti".equals(ltiMap.get(CaaConstants.NAME)));
		assertTrue("ezto-devs_lti".equals(ltiMap.get(CaaConstants.CONSUMER_ID)));
	}
	
	/**
	 * This method is going to test that LTI informations are populated properly or not
	 * @see AssignmentServiceUtil#getLtiInfo(String)
	 * @throws Exception
	 */
	@Test
	public void testgetLtiInfoCase3() throws Exception {
		Map<String,String> ltiMap = assignmentServiceUtil.getLtiInfo(null);
		assertTrue(ltiMap.size() == 0);
	}
	
	/**
	 * This method is going to test that LTI informations are populated properly or not
	 * @see AssignmentServiceUtil#getLtiInfo(String)
	 * @throws Exception
	 */
	@Test
	public void testgetLtiInfoCase4() throws Exception {
		Map<String,String> ltiMap = assignmentServiceUtil.getLtiInfo("");
		assertTrue(ltiMap.size() == 0);
	}
	
	/**
	 * This method is going to test that a test is mobile safe or not
	 * @see AssignmentServiceUtil#isTestMobileSafe(ResponseTO, TestTO, JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testIsTestMobileSafeCase1() throws Exception {
		ResponseTO responseTO = new ResponseTO();
		TestTO testTO = new TestTO();
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(true);
		assertTrue(assignmentServiceUtilMock.isTestMobileSafe(responseTO, testTO, json));
		assertTrue(!json.has(CaaConstants.MOBILE_UNSAFE_REASON));
	}
	
	/**
	 * This method is going to test that a test is mobile safe or not
	 * @see AssignmentServiceUtil#isTestMobileSafe(ResponseTO, TestTO, JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testIsTestMobileSafeCase2() throws Exception {
		ResponseTO responseTO = new ResponseTO();
		TestTO testTO = new TestTO();
		JSONObject json = new JSONObject();
		testTO.setMobileUnSafeReason(CaaConstants.ASSIGNMENT_UNSUPPORTED_QUESTION);
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		assertTrue(!assignmentServiceUtilMock.isTestMobileSafe(responseTO, testTO, json));
		assertTrue(CaaConstants.ASSIGNMENT_UNSUPPORTED_QUESTION.equals(json.getString(CaaConstants.MOBILE_UNSAFE_REASON)));
	}
	
	/**
	 * This method is going to test that a test is mobile safe or not
	 * @see AssignmentServiceUtil#isTestMobileSafe(ResponseTO, TestTO, JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testIsTestMobileSafeCase3() throws Exception {
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		testparameter.replaceParam(classware_hm.POLICY_feedback, CaaConstants.YES);
		responseTO.setTestParameter(testparameter);
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		assertTrue(!assignmentServiceUtilMock.isTestMobileSafe(responseTO, testTO, json));
		assertTrue(CaaConstants.ASSIGNMENT_POLICY_ALLOW_FEEDBACK.equals(json.getString(CaaConstants.MOBILE_UNSAFE_REASON)));
	}
	
	/**
	 * This method is going to test that a test is mobile safe or not
	 * @see AssignmentServiceUtil#isTestMobileSafe(ResponseTO, TestTO, JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testIsTestMobileSafeCase4() throws Exception {
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		responseTO.setTestParameter(testparameter);
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		assertTrue(assignmentServiceUtilMock.isTestMobileSafe(responseTO, testTO, json));
		assertTrue(!json.has(CaaConstants.MOBILE_UNSAFE_REASON));
	}
	
	/**
	 * This method is going to test that a test is mobile safe or not
	 * @see AssignmentServiceUtil#isTestMobileSafe(ResponseTO, TestTO, JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testIsTestMobileSafeCase5() throws Exception {
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		responseTO.setTestParameter(testparameter);
		TestTO testTO = new TestTO();
		testTO.setMobileReady(false);
		testTO.setMobileUnSafeReason(CaaConstants.ASSIGNMENT_UNSUPPORTED_MEDIA);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		Mockito.doReturn(false).when(testServiceImpl).validateMobileReady(testTO);
		
		assertTrue(!assignmentServiceUtilMock.isTestMobileSafe(responseTO, testTO, json));
		assertTrue(CaaConstants.ASSIGNMENT_UNSUPPORTED_MEDIA.equals(json.getString(CaaConstants.MOBILE_UNSAFE_REASON)));
	}
	
	/**
	 * This method is going to test that a test is mobile safe or not
	 * @see AssignmentServiceUtil#isTestMobileSafe(ResponseTO, TestTO, JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testIsTestMobileSafeCase6() throws Exception {
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		responseTO.setTestParameter(testparameter);
		testparameter.put(classware_hm.POLICY_limit, "60");
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		Mockito.when(licenseServiceMock.isFeaturePresent("enableTimedAssignmentInMobile")).thenReturn(false);
		
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		
		assertFalse(assignmentServiceUtilMock.isTestMobileSafe(responseTO, testTO, json));
		assertTrue(CaaConstants.ASSIGNMENT_POLICY_TIME_LIMIT.equals(json.getString(CaaConstants.MOBILE_UNSAFE_REASON)));
	}

	/**
	 * This method is going to test that a test is mobile safe or not
	 * @see AssignmentServiceUtil#isTestMobileSafe(ResponseTO, TestTO, JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testIsTestMobileSafeCase8() throws Exception {
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		responseTO.setTestParameter(testparameter);
		testparameter.put(classware_hm.POLICY_limit, "no");
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		Mockito.when(licenseServiceMock.isFeaturePresent("enableTimedAssignmentInMobile")).thenReturn(false);
		
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		
		assertTrue(assignmentServiceUtilMock.isTestMobileSafe(responseTO, testTO, json));
		assertTrue(!json.has(CaaConstants.MOBILE_UNSAFE_REASON));
	}
	
	/**
	 * This method is going to test that a test is mobile safe or not
	 * @see AssignmentServiceUtil#isTestMobileSafe(ResponseTO, TestTO, JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testIsTestMobileSafeCase9() throws Exception {
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		responseTO.setTestParameter(testparameter);
		testparameter.put(classware_hm.POLICY_limit, "0");
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		Mockito.when(licenseServiceMock.isFeaturePresent("enableTimedAssignmentInMobile")).thenReturn(false);
		
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		
		assertTrue(assignmentServiceUtilMock.isTestMobileSafe(responseTO, testTO, json));
		assertTrue(!json.has(CaaConstants.MOBILE_UNSAFE_REASON));
	}
	
	/**
	 * This method is going to test that a test is mobile safe or not
	 * @see AssignmentServiceUtil#isTestMobileSafe(ResponseTO, TestTO, JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testIsTestMobileSafeCase10() throws Exception {
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		responseTO.setTestParameter(testparameter);
		testparameter.put(classware_hm.POLICY_limit, "");
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		Mockito.when(licenseServiceMock.isFeaturePresent("enableTimedAssignmentInMobile")).thenReturn(false);
		
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		
		assertTrue(assignmentServiceUtilMock.isTestMobileSafe(responseTO, testTO, json));
		assertTrue(!json.has(CaaConstants.MOBILE_UNSAFE_REASON));
	}
	
	@Test
	public void testIsTestMobileSafeCase12() throws Exception {
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		responseTO.setTestParameter(testparameter);
		testparameter.put(classware_hm.POLICY_limit, "60");
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		Mockito.when(licenseServiceMock.isFeaturePresent("enableTimedAssignmentInMobile")).thenReturn(true);
		
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		
		assertTrue(assignmentServiceUtilMock.isTestMobileSafe(responseTO, testTO, json));
		assertTrue(!json.has(CaaConstants.MOBILE_UNSAFE_REASON));
	}
	
	/**
	 * This method is going to test that a test is mobile safe or not
	 * @see AssignmentServiceUtil#isTestMobileSafe(ResponseTO, TestTO, JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testIsTestMobileSafeCase13() throws Exception {
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		responseTO.setTestParameter(testparameter);
		testparameter.put(classware_hm.POLICY_limit, "abcd");
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		Mockito.when(licenseServiceMock.isFeaturePresent("enableTimedAssignmentInMobile")).thenReturn(false);
		
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		
		assertTrue(assignmentServiceUtilMock.isTestMobileSafe(responseTO, testTO, json));
		assertTrue(!json.has(CaaConstants.MOBILE_UNSAFE_REASON));
	}
	
	/**
	 * This method is going to test that a test is mobile safe or not
	 * @see AssignmentServiceUtil#isTestMobileSafe(ResponseTO, TestTO, JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testIsTestMobileSafeCase14() throws Exception {
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		responseTO.setTestParameter(testparameter);
		testparameter.put(classware_hm.POLICY_limit, "  no  ");
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		Mockito.when(licenseServiceMock.isFeaturePresent("enableTimedAssignmentInMobile")).thenReturn(false);
		
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		
		assertTrue(assignmentServiceUtilMock.isTestMobileSafe(responseTO, testTO, json));
		assertTrue(!json.has(CaaConstants.MOBILE_UNSAFE_REASON));
	}
	
	/**
	 * This method is going to test that a test is mobile safe or not
	 * @see AssignmentServiceUtil#isTestMobileSafe(ResponseTO, TestTO, JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testIsTestMobileSafeCase15() throws Exception {
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		responseTO.setTestParameter(testparameter);
		testparameter.put(classware_hm.POLICY_limit, "    ");
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		Mockito.when(licenseServiceMock.isFeaturePresent("enableTimedAssignmentInMobile")).thenReturn(false);
		
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		
		assertTrue(assignmentServiceUtilMock.isTestMobileSafe(responseTO, testTO, json));
		assertTrue(!json.has(CaaConstants.MOBILE_UNSAFE_REASON));
	}
	
	/**
	 * This method is going to test that a test is mobile safe or not
	 * @see AssignmentServiceUtil#isTestMobileSafe(ResponseTO, TestTO, JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testIsTestMobileSafeCase18() throws Exception {
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		responseTO.setTestParameter(testparameter);
		testparameter.put(classware_hm.POLICY_limit, "60");
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		Mockito.when(licenseServiceMock.isFeaturePresent("enableTimedAssignmentInMobile")).thenReturn(false);
		
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		
		assertFalse(assignmentServiceUtilMock.isTestMobileSafe(responseTO, testTO, json));
		assertTrue(CaaConstants.ASSIGNMENT_POLICY_TIME_LIMIT.equals(json.getString(CaaConstants.MOBILE_UNSAFE_REASON)));
	}
	
	/**
	 * This method is going to test that a test is mobile safe or not
	 * @see AssignmentServiceUtil#isTestMobileSafe(ResponseTO, TestTO, JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testIsTestMobileSafeCase20() throws Exception {
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		responseTO.setTestParameter(testparameter);
		testparameter.put(classware_hm.POLICY_limit, null);
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		Mockito.when(licenseServiceMock.isFeaturePresent("enableTimedAssignmentInMobile")).thenReturn(false);
		
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		
		assertTrue(assignmentServiceUtilMock.isTestMobileSafe(responseTO, testTO, json));
		assertTrue(!json.has(CaaConstants.MOBILE_UNSAFE_REASON));
	}
	
	@Test
	public void testIsTestMobileSafeBOP1() throws Exception {
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		responseTO.setTestParameter(testparameter);
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		Mockito.when(licenseServiceMock.isFeaturePresent("enableTimedAssignmentInMobile")).thenReturn(false);
		
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		
		assertTrue(assignmentServiceUtilMock.isTestMobileSafe(responseTO, testTO, json));
		assertTrue(!json.has(CaaConstants.MOBILE_UNSAFE_REASON));
	}
	
	@Test
	public void testIsTestMobileSafeBOP2() throws Exception {
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		testparameter.replaceParam(classware_hm.FORMER_SUBMISSION, "123");
		responseTO.setTestParameter(testparameter);
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		Mockito.when(licenseServiceMock.isFeaturePresent("enableTimedAssignmentInMobile")).thenReturn(false);
		
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		
		assertFalse(assignmentServiceUtilMock.isTestMobileSafe(responseTO, testTO, json));
		assertTrue(CaaConstants.ASSIGNMENT_POLICY_BOP.equals(json.getString(CaaConstants.MOBILE_UNSAFE_REASON)));
		
	}
	
	@Test
	public void testIsTestMobileSafeBOP3() throws Exception {
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		testparameter.replaceParam(classware_hm.BUILD_ON_PREVIOUS, CaaConstants.TRUE);
		responseTO.setTestParameter(testparameter);		
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		Mockito.when(licenseServiceMock.isFeaturePresent("enableTimedAssignmentInMobile")).thenReturn(false);
		
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		
		assertFalse(assignmentServiceUtilMock.isTestMobileSafe(responseTO, testTO, json));
		assertTrue(CaaConstants.ASSIGNMENT_POLICY_BOP.equals(json.getString(CaaConstants.MOBILE_UNSAFE_REASON)));
	}
	
	@Test
	public void testIsTestMobileSafeBOP4() throws Exception {
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		testparameter.replaceParam(classware_hm.FORMER_SUBMISSION, "123");
		testparameter.replaceParam(classware_hm.BUILD_ON_PREVIOUS, CaaConstants.TRUE);
		responseTO.setTestParameter(testparameter);
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		Mockito.when(licenseServiceMock.isFeaturePresent("enableTimedAssignmentInMobile")).thenReturn(false);
		
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		
		assertFalse(assignmentServiceUtilMock.isTestMobileSafe(responseTO, testTO, json));
		assertTrue(CaaConstants.ASSIGNMENT_POLICY_BOP.equals(json.getString(CaaConstants.MOBILE_UNSAFE_REASON)));
	}
	
	@Test
	public void testIsTestMobileSafeBOP5() throws Exception {
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		testparameter.replaceParam(classware_hm.POLICY_genius, CaaConstants.YES);
		responseTO.setTestParameter(testparameter);
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		Mockito.when(licenseServiceMock.isFeaturePresent("enableTimedAssignmentInMobile")).thenReturn(false);
		
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		
		assertTrue(assignmentServiceUtilMock.isTestMobileSafe(responseTO, testTO, json));
		assertTrue(!json.has(CaaConstants.MOBILE_UNSAFE_REASON));
	}
	
	@Test
	public void testIsTestMobileSafeBOP6() throws Exception {
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		testparameter.replaceParam(classware_hm.FORMER_SUBMISSION, "123");
		testparameter.replaceParam(classware_hm.POLICY_genius, CaaConstants.YES);
		responseTO.setTestParameter(testparameter);
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		Mockito.when(licenseServiceMock.isFeaturePresent("enableTimedAssignmentInMobile")).thenReturn(false);
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		assertTrue(assignmentServiceUtilMock.isTestMobileSafe(responseTO, testTO, json));
		assertTrue(!json.has(CaaConstants.MOBILE_UNSAFE_REASON));
	}
	
	@Test
	public void testIsTestMobileSafeBOP7() throws Exception {
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		testparameter.replaceParam(classware_hm.BUILD_ON_PREVIOUS, CaaConstants.TRUE);
		testparameter.replaceParam(classware_hm.POLICY_genius, CaaConstants.YES);
		responseTO.setTestParameter(testparameter);		
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		Mockito.when(licenseServiceMock.isFeaturePresent("enableTimedAssignmentInMobile")).thenReturn(false);
		
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		
		assertTrue(assignmentServiceUtilMock.isTestMobileSafe(responseTO, testTO, json));
		assertTrue(!json.has(CaaConstants.MOBILE_UNSAFE_REASON));
	}
	
	@Test
	public void testIsTestMobileSafeBOP8() throws Exception {
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		testparameter.replaceParam(classware_hm.FORMER_SUBMISSION, "123");
		testparameter.replaceParam(classware_hm.BUILD_ON_PREVIOUS, CaaConstants.TRUE);
		testparameter.replaceParam(classware_hm.POLICY_genius, CaaConstants.YES);
		responseTO.setTestParameter(testparameter);
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		Mockito.when(licenseServiceMock.isFeaturePresent("enableTimedAssignmentInMobile")).thenReturn(false);
		
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		
		assertTrue(assignmentServiceUtilMock.isTestMobileSafe(responseTO, testTO, json));
		assertTrue(!json.has(CaaConstants.MOBILE_UNSAFE_REASON));
	}
	
	@Test
	public void testIsTestMobileSafeBOP9() throws Exception {
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		testparameter.replaceParam(classware_hm.POLICY_genius, CaaConstants.NO);
		responseTO.setTestParameter(testparameter);
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		Mockito.when(licenseServiceMock.isFeaturePresent("enableTimedAssignmentInMobile")).thenReturn(false);
		
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		
		assertTrue(assignmentServiceUtilMock.isTestMobileSafe(responseTO, testTO, json));
		assertTrue(!json.has(CaaConstants.MOBILE_UNSAFE_REASON));
	}
	
	@Test
	public void testIsTestMobileSafeBOP10() throws Exception {
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		testparameter.replaceParam(classware_hm.FORMER_SUBMISSION, "123");
		testparameter.replaceParam(classware_hm.POLICY_genius, CaaConstants.NO);
		responseTO.setTestParameter(testparameter);
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		Mockito.when(licenseServiceMock.isFeaturePresent("enableTimedAssignmentInMobile")).thenReturn(false);
		
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		
		assertFalse(assignmentServiceUtilMock.isTestMobileSafe(responseTO, testTO, json));
		assertTrue(CaaConstants.ASSIGNMENT_POLICY_BOP.equals(json.getString(CaaConstants.MOBILE_UNSAFE_REASON)));
	}
	
	@Test
	public void testIsTestMobileSafeBOP11() throws Exception {
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		testparameter.replaceParam(classware_hm.BUILD_ON_PREVIOUS, CaaConstants.TRUE);
		testparameter.replaceParam(classware_hm.POLICY_genius, CaaConstants.NO);
		responseTO.setTestParameter(testparameter);		
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		Mockito.when(licenseServiceMock.isFeaturePresent("enableTimedAssignmentInMobile")).thenReturn(false);
		
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		
		assertFalse(assignmentServiceUtilMock.isTestMobileSafe(responseTO, testTO, json));
		assertTrue(CaaConstants.ASSIGNMENT_POLICY_BOP.equals(json.getString(CaaConstants.MOBILE_UNSAFE_REASON)));
	}
	
	@Test
	public void testIsTestMobileSafeBOP12() throws Exception {
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		testparameter.replaceParam(classware_hm.FORMER_SUBMISSION, "123");
		testparameter.replaceParam(classware_hm.BUILD_ON_PREVIOUS, CaaConstants.TRUE);
		testparameter.replaceParam(classware_hm.POLICY_genius, CaaConstants.NO);
		responseTO.setTestParameter(testparameter);
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		Mockito.when(licenseServiceMock.isFeaturePresent("enableTimedAssignmentInMobile")).thenReturn(false);
		
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		
		assertFalse(assignmentServiceUtilMock.isTestMobileSafe(responseTO, testTO, json));
		assertTrue(CaaConstants.ASSIGNMENT_POLICY_BOP.equals(json.getString(CaaConstants.MOBILE_UNSAFE_REASON)));
	}
	
	/**
	 * This method is going to test that deduction policies are set properly in the
	 * instruction JSON.
	 * @see AssignmentServiceUtil#setDeductionPoliciesIntoInstructions(ResponseTO, JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testSetDeductionPoliciesIntoInstructionsCase1() throws Exception {
		TestTO testTO = new TestTO();
		List<QuestionMetaDataTO> questionMetaDataList = new ArrayList<QuestionMetaDataTO>();
		testTO.setQuestionMetaDataList(questionMetaDataList);
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		Map<String,String> policyMap = new HashMap<String, String>();
		policyMap.put(classware_hm.POLICY_deduct_late, "10");
		policyMap.put(classware_hm.POLICY_deduct_late_increment, "hour");
		policyMap.put(classware_hm.POLICY_deduct_hints, "20");
		policyMap.put(classware_hm.POLICY_deduct_resources, "30");
		policyMap.put(classware_hm.POLICY_deduct_checkwork, "40");
		policyMap.put(classware_hm.POLICY_checkwork_limit, "50");
		ResponseTO responseTO = new ResponseTO();
		responseTO.setTestParameter(testparameter);
		Mockito.doReturn("10").when(assignmentServiceUtilMock).getTotalPoints(questionMetaDataList, responseTO);
		JSONObject jsonObject = assignmentServiceUtilMock.setPoliciesIntoInstructions(responseTO, null , policyMap , testTO);
		_logger.info("JSONOBJECT :: " + jsonObject);
		assertTrue(jsonObject != null);
		JSONObject activityJSON = jsonObject.getJSONObject(CaaConstants.ACTIVITY_INFO);
		JSONObject deductJSON = activityJSON.getJSONObject(CaaConstants.DEDUCTIONS);
		assertTrue("10".equals(deductJSON.getString(CaaConstants.LATE_SUBMISSION)));
		assertTrue("hour".equals(deductJSON.getString(CaaConstants.LATE_SUBMISSION_INTERVAL)));
		assertTrue("20".equals(deductJSON.getString(CaaConstants.HINT_DEDUCTION)));
		assertTrue("30".equals(deductJSON.getString(CaaConstants.EBOOK_DEDUCTION)));
		assertTrue("40".equals(deductJSON.getString(CaaConstants.CHECK_WORK_DEDUCTION)));
		assertTrue("50".equals(deductJSON.getString(CaaConstants.CHECK_WORK_LIMIT)));
	}
	
	/**
	 * This method is going to test that deduction policies are set properly in the
	 * instruction JSON.
	 * @see AssignmentServiceUtil#setDeductionPoliciesIntoInstructions(ResponseTO, JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testSetDeductionPoliciesIntoInstructionsCase2() throws Exception {
		TestTO testTO = new TestTO();
		List<QuestionMetaDataTO> questionMetaDataList = new ArrayList<QuestionMetaDataTO>();
		testTO.setQuestionMetaDataList(questionMetaDataList);
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		Map<String,String> policyMap = new HashMap<String, String>();
		policyMap.put(classware_hm.POLICY_deduct_late, "10");
		policyMap.put(classware_hm.POLICY_deduct_late_increment, "hour");
		policyMap.put(classware_hm.POLICY_deduct_hints, "20");
		policyMap.put(classware_hm.POLICY_deduct_resources, "30");
		policyMap.put(classware_hm.POLICY_deduct_checkwork, "40");
		policyMap.put(classware_hm.POLICY_checkwork_limit, "50");
		ResponseTO responseTO = new ResponseTO();
		responseTO.setTestParameter(testparameter);
		JSONObject jsonObject1 = new JSONObject();
		JSONObject activity1 = new JSONObject();
		activity1.put("name", "XXX");
		jsonObject1.put(CaaConstants.ACTIVITY_INFO, activity1);
		Mockito.doReturn("10").when(assignmentServiceUtilMock).getTotalPoints(questionMetaDataList, responseTO);
		JSONObject jsonObject = assignmentServiceUtilMock.setPoliciesIntoInstructions(responseTO, jsonObject1 , policyMap , testTO);
		_logger.info("JSONOBJECT :: " + jsonObject);
		assertTrue(jsonObject != null);
		JSONObject activityJSON = jsonObject.getJSONObject(CaaConstants.ACTIVITY_INFO);
		assertTrue("XXX".equals(activityJSON.getString("name")));
		JSONObject deductJSON = activityJSON.getJSONObject(CaaConstants.DEDUCTIONS);
		assertTrue("10".equals(deductJSON.getString(CaaConstants.LATE_SUBMISSION)));
		assertTrue("hour".equals(deductJSON.getString(CaaConstants.LATE_SUBMISSION_INTERVAL)));
		assertTrue("20".equals(deductJSON.getString(CaaConstants.HINT_DEDUCTION)));
		assertTrue("30".equals(deductJSON.getString(CaaConstants.EBOOK_DEDUCTION)));
		assertTrue("40".equals(deductJSON.getString(CaaConstants.CHECK_WORK_DEDUCTION)));
		assertTrue("50".equals(deductJSON.getString(CaaConstants.CHECK_WORK_LIMIT)));
	}
	
	/**
	 * This method is going to test that deduction policies are set properly in the
	 * instruction JSON.
	 * @see AssignmentServiceUtil#setDeductionPoliciesIntoInstructions(ResponseTO, JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testSetDeductionPoliciesIntoInstructionsCase3() throws Exception {
		TestTO testTO = new TestTO();
		List<QuestionMetaDataTO> questionMetaDataList = new ArrayList<QuestionMetaDataTO>();
		testTO.setQuestionMetaDataList(questionMetaDataList);
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		Map<String,String> policyMap = new HashMap<String, String>();
		policyMap.put(classware_hm.POLICY_deduct_late, "10");
		policyMap.put(classware_hm.POLICY_deduct_late_increment, "hour");
		policyMap.put(classware_hm.POLICY_deduct_hints, "20");
		policyMap.put(classware_hm.POLICY_deduct_resources, "30");
		policyMap.put(classware_hm.POLICY_deduct_checkwork, "40");
		policyMap.put(classware_hm.POLICY_checkwork_limit, "50");
		ResponseTO responseTO = new ResponseTO();
		responseTO.setTestParameter(testparameter);
		JSONObject jsonObject1 = new JSONObject();
		JSONObject activity1 = new JSONObject();
		JSONObject deduct1 = new JSONObject();
		deduct1.put(CaaConstants.LATE_SUBMISSION, "900");
		activity1.put("name", "XXX");
		activity1.put(CaaConstants.DEDUCTIONS, deduct1);
		jsonObject1.put(CaaConstants.ACTIVITY_INFO, activity1);
		Mockito.doReturn("10").when(assignmentServiceUtilMock).getTotalPoints(questionMetaDataList, responseTO);
		JSONObject jsonObject = assignmentServiceUtilMock.setPoliciesIntoInstructions(responseTO, jsonObject1 , policyMap , testTO);
		_logger.info("JSONOBJECT :: " + jsonObject);
		assertTrue(jsonObject != null);
		JSONObject activityJSON = jsonObject.getJSONObject(CaaConstants.ACTIVITY_INFO);
		assertTrue("XXX".equals(activityJSON.getString("name")));
		JSONObject deductJSON = activityJSON.getJSONObject(CaaConstants.DEDUCTIONS);
		assertTrue("10".equals(deductJSON.getString(CaaConstants.LATE_SUBMISSION)));
		assertTrue("hour".equals(deductJSON.getString(CaaConstants.LATE_SUBMISSION_INTERVAL)));
		assertTrue("20".equals(deductJSON.getString(CaaConstants.HINT_DEDUCTION)));
		assertTrue("30".equals(deductJSON.getString(CaaConstants.EBOOK_DEDUCTION)));
		assertTrue("40".equals(deductJSON.getString(CaaConstants.CHECK_WORK_DEDUCTION)));
		assertTrue("50".equals(deductJSON.getString(CaaConstants.CHECK_WORK_LIMIT)));
	}
	
	/**
	 * This method is going to test that remaining time is generated properly or not
	 * @see AssignmentServiceUtil#getRemainingTime(ResponseTO)
	 * @throws Exception
	 */
	@Test
	public void testGetRemainingTimeCase1() throws Exception {
		ResponseTO responseTO = new ResponseTO();
		long remainingTime = assignmentServiceUtilMock.getRemainingTime(responseTO);
		assertTrue(remainingTime == CaaConstants.NO_TIMER);
	}
	
	/**
	 * This method is going to test that remaining time is generated properly or not
	 * @see AssignmentServiceUtil#getRemainingTime(ResponseTO)
	 * @throws Exception
	 */
	@Test
	public void testGetRemainingTimeCase2() throws Exception {
		CustomMap<String, String> testparameter = new CustomMap<String,String>();
		testparameter.put(classware_hm.POLICY_limit, Long.toString(3600l));
		testparameter.put(test.TIMESTAMP, Long.toString(new Date().getTime()));
		ResponseTO responseTO = new ResponseTO();
		responseTO.setTestParameter(testparameter);
		long remainingTime = assignmentServiceUtilMock.getRemainingTime(responseTO);
		System.out.println(remainingTime);
		assertTrue(remainingTime != CaaConstants.NO_TIMER);
		assertTrue(remainingTime == 3600l);
	}
	
	/**
	 * This method is going to test that remaining time is generated properly or not
	 * @see AssignmentServiceUtil#getRemainingTime(ResponseTO)
	 * @throws Exception
	 */
	@Test
	public void testGetRemainingTimeCase3() throws Exception {
		CustomMap<String, String> testparameter = new CustomMap<String,String>();
		testparameter.put(classware_hm.POLICY_limit, Long.toString(3600l));
		testparameter.put(test.TIMESTAMP, Long.toString(1414413540257l));
		ResponseTO responseTO = new ResponseTO();
		responseTO.setTestParameter(testparameter);
		long remainingTime = assignmentServiceUtilMock.getRemainingTime(responseTO);
		assertTrue(remainingTime != CaaConstants.NO_TIMER);
		System.out.println(remainingTime);
		assertTrue(remainingTime < 3600l);
	}
	
	/**
	 * This method is going to test that remaining time is generated properly or not
	 * @see AssignmentServiceUtil#getRemainingTime(ResponseTO)
	 * @throws Exception
	 */
	@Test
	public void testGetRemainingTimeCase4() throws Exception {
		CustomMap<String, String> testparameter = new CustomMap<String,String>();
		testparameter.put(classware_hm.POLICY_limit, Long.toString(1l));
		testparameter.put(test.TIMESTAMP, Long.toString(1414413540257l));
		ResponseTO responseTO = new ResponseTO();
		responseTO.setTestParameter(testparameter);
		long remainingTime = assignmentServiceUtilMock.getRemainingTime(responseTO);
		assertTrue(remainingTime != CaaConstants.NO_TIMER);
		System.out.println(remainingTime);
		assertTrue(remainingTime == 0);
	}
	
	@Test
	public void testPopulateTestTitle() throws Exception {
		String testTitle = "Test123";
		ResponseTO responseTO = new ResponseTO();
		TestTO testTO = new TestTO();
		CustomMap<String, String> testParameter = new CustomMap<String,String>();
		testParameter.put(classware_hm.HEADER_ASSIGNMENT_TITLE, testTitle);
		responseTO.setTestParameter(testParameter);
		
		assignmentServiceUtilMock.populateTestTitle(responseTO, testTO);
		assertTrue(testTitle.equals(testParameter.getParam(classware_hm.HEADER_ASSIGNMENT_TITLE)));
	}
	
	@Test
	public void testPopulateTestTitle1() throws Exception {
		String testTitle = "Test123";
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testParameter = new CustomMap<String,String>();
		responseTO.setTestParameter(testParameter);
		TestTO testTO = new TestTO();
		testTO.setTitle(testTitle);
		
		assignmentServiceUtilMock.populateTestTitle(responseTO, testTO);
		assertTrue(testTitle.equals(testParameter.getParam(classware_hm.HEADER_ASSIGNMENT_TITLE)));
	}
	
	@Test
	public void testPopulateTestTitle2() throws Exception {
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testParameter = new CustomMap<String,String>();
		responseTO.setTestParameter(testParameter);
		TestTO testTO = new TestTO();
		
		assignmentServiceUtilMock.populateTestTitle(responseTO, testTO);
		assertTrue(StringUtils.isBlank(testParameter.getParam(classware_hm.HEADER_ASSIGNMENT_TITLE)));
	}
	
	/**
	 * This method is going to test that attempt information is populated properly or not
	 * @see AssignmentServiceUtil#populateAttemptInfoJSON(ResponseTO, Map, JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testpopulateAttemptInfoJSONCase1() throws Exception {
		TestTO testTO = new TestTO();
		List<QuestionMetaDataTO> questionMetaDataList = new ArrayList<QuestionMetaDataTO>();
		testTO.setQuestionMetaDataList(questionMetaDataList);
		JSONObject activityJSON = new JSONObject();
		Map<String,String> policyMap = new HashMap<String, String>();
		policyMap.put(classware_hm.POLICY_deduct_attempts, "20");
		policyMap.put(classware_hm.POLICY_feedback, "yes");
		policyMap.put(classware_hm.POLICY_genius , "yes");
		policyMap.put(classware_hm.POLICY_duedate,"2014-11-25 06:39:00");
		policyMap.put(classware_hm.POLICY_endtime,"1416915579");
		policyMap.put(classware_hm.POLICY_attempts, "15");
		policyMap.put(classware_hm.POLICY_limit, "15");
		policyMap.put(classware_hm.POLICY_autoSubmit, "yes");
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		testparameter.replaceParam(classware_hm.BUILD_ON_PREVIOUS, "true");
		responseTO.setTestParameter(testparameter);
		responseTO.setAttemptNo("10");
		Mockito.doReturn("10").when(assignmentServiceUtilMock).getTotalPoints(questionMetaDataList, responseTO);
		assignmentServiceUtilMock.populateAttemptInfoJSON(responseTO, policyMap, activityJSON , testTO);
		JSONObject json = activityJSON.getJSONObject(CaaConstants.ATTEMPT_INFO);
		//assertTrue("true".equals(json.getString(CaaConstants.BUILD_ON_PREVIOUS)));
		assertTrue("true".equals(json.getString(CaaConstants.FEED_BACK_BETWEENQ)));
		assertTrue("true".equals(json.getString(CaaConstants.STUDY_MODE)));
		assertTrue("20".equals(json.getString(CaaConstants.LATE_PENALTY)));
		assertTrue("11/25/2014 at 06:39 AM".equals(json.getString(CaaConstants.DUE_DATE)));
		assertTrue("10".equals(json.getString(CaaConstants.POINTS)));
		assertTrue("15".equals(json.getString(CaaConstants.SCORED_ATTEMPT_MAX)));
		assertTrue("true".equals(json.getString(CaaConstants.AUTO_SUBMIT)));
		assertTrue("10".equals(json.getString(CaaConstants.ACTUAL_ATTEMPT_NO)));
		assertTrue("10".equals(json.getString(CaaConstants.DISPLAY_ATTEMPT_NO)));
	}
	
	/**
	 * This method is going to test that attempt information is populated properly or not
	 * @see AssignmentServiceUtil#populateAttemptInfoJSON(ResponseTO, Map, JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testpopulateAttemptInfoJSONCase2() throws Exception {
		TestTO testTO = new TestTO();
		List<QuestionMetaDataTO> questionMetaDataList = new ArrayList<QuestionMetaDataTO>();
		testTO.setQuestionMetaDataList(questionMetaDataList);
		JSONObject activityJSON = new JSONObject();
		Map<String,String> policyMap = new HashMap<String, String>();
		policyMap.put(classware_hm.POLICY_deduct_attempts, "20");
		policyMap.put(classware_hm.POLICY_limit, "15");
		//policyMap.put(classware_hm.POLICY_feedback, "yes");
		//policyMap.put(classware_hm.POLICY_genius , "yes");
		policyMap.put(classware_hm.POLICY_endtime,"1416915579");
		policyMap.put(classware_hm.POLICY_duedate,"2014-11-25 06:39:00");
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		testparameter.replaceParam(classware_hm.BUILD_ON_PREVIOUS, "true");
		responseTO.setTestParameter(testparameter);
		Mockito.doReturn("10").when(assignmentServiceUtilMock).getTotalPoints(questionMetaDataList, responseTO);
		assignmentServiceUtilMock.populateAttemptInfoJSON(responseTO, policyMap, activityJSON , testTO);
		JSONObject json = activityJSON.getJSONObject(CaaConstants.ATTEMPT_INFO);
		//assertTrue("true".equals(json.getString(CaaConstants.BUILD_ON_PREVIOUS)));
		//assertTrue("true".equals(json.getString(CaaConstants.FEED_BACK_BETWEENQ)));
		//assertTrue("true".equals(json.getString(CaaConstants.STUDY_MODE)));
		assertTrue("20".equals(json.getString(CaaConstants.LATE_PENALTY)));
		assertTrue("11/25/2014 at 06:39 AM".equals(json.getString(CaaConstants.DUE_DATE)));
		assertTrue("10".equals(json.getString(CaaConstants.POINTS)));
	}
	
	/**
	 * This method is going to test that attempt information is populated properly or not
	 * @see AssignmentServiceUtil#populateAttemptInfoJSON(ResponseTO, Map, JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testpopulateAttemptInfoJSONCase3() throws Exception {
		TestTO testTO = new TestTO();
		List<QuestionMetaDataTO> questionMetaDataList = new ArrayList<QuestionMetaDataTO>();
		testTO.setQuestionMetaDataList(questionMetaDataList);
		Map<String,String> policyMap = new HashMap<String, String>();
		JSONObject activityJSON = new JSONObject();
		JSONObject attemptJSON = new JSONObject();
		attemptJSON.put(CaaConstants.FEED_BACK_BETWEENQ, false);
		activityJSON.put(CaaConstants.ATTEMPT_INFO, attemptJSON);
		policyMap.put(classware_hm.POLICY_deduct_attempts, "20");
		policyMap.put(classware_hm.POLICY_feedback, "yes");
		policyMap.put(classware_hm.POLICY_limit, "15");
		//policyMap.put(classware_hm.POLICY_genius , "yes");
		policyMap.put(classware_hm.POLICY_endtime,"1416915579");
		policyMap.put(classware_hm.POLICY_duedate,"2014-11-25 06:39:00");
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		testparameter.replaceParam(classware_hm.BUILD_ON_PREVIOUS, "true");
		responseTO.setTestParameter(testparameter);
		Mockito.doReturn("10").when(assignmentServiceUtilMock).getTotalPoints(questionMetaDataList, responseTO);
		assignmentServiceUtilMock.populateAttemptInfoJSON(responseTO, policyMap, activityJSON , testTO);
		JSONObject json = activityJSON.getJSONObject(CaaConstants.ATTEMPT_INFO);
		//assertTrue("true".equals(json.getString(CaaConstants.BUILD_ON_PREVIOUS)));
		assertTrue("true".equals(json.getString(CaaConstants.FEED_BACK_BETWEENQ)));
		//assertTrue("true".equals(json.getString(CaaConstants.STUDY_MODE)));
		assertTrue("20".equals(json.getString(CaaConstants.LATE_PENALTY)));
		assertTrue("11/25/2014 at 06:39 AM".equals(json.getString(CaaConstants.DUE_DATE)));
		assertTrue("10".equals(json.getString(CaaConstants.POINTS)));
	}
	
	/**
	 * This method is going to test that attempt information is populated properly or not
	 * @see AssignmentServiceUtil#populateAttemptInfoJSON(ResponseTO, Map, JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testpopulateAttemptInfoJSONCase4() throws Exception {
		TestTO testTO = new TestTO();
		List<QuestionMetaDataTO> questionMetaDataList = new ArrayList<QuestionMetaDataTO>();
		testTO.setQuestionMetaDataList(questionMetaDataList);
		Map<String,String> policyMap = new HashMap<String, String>();
		JSONObject activityJSON = new JSONObject();
		JSONObject attemptJSON = new JSONObject();
		attemptJSON.put(CaaConstants.FEED_BACK_BETWEENQ, true);
		activityJSON.put(CaaConstants.ATTEMPT_INFO, attemptJSON);
		policyMap.put(classware_hm.POLICY_deduct_attempts, "20");
		policyMap.put(classware_hm.POLICY_feedback, "yes");
		//policyMap.put(classware_hm.POLICY_genius , "yes");
		policyMap.put(classware_hm.POLICY_endtime,"1416915579");
		policyMap.put(classware_hm.POLICY_duedate,"2014-11-25 06:39:00");
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		testparameter.replaceParam(classware_hm.BUILD_ON_PREVIOUS, "true");
		responseTO.setTestParameter(testparameter);
		Mockito.doReturn("10").when(assignmentServiceUtilMock).getTotalPoints(questionMetaDataList, responseTO);
		assignmentServiceUtilMock.populateAttemptInfoJSON(responseTO, policyMap, activityJSON , testTO);
		JSONObject json = activityJSON.getJSONObject(CaaConstants.ATTEMPT_INFO);
		//assertTrue("true".equals(json.getString(CaaConstants.BUILD_ON_PREVIOUS)));
		assertTrue("true".equals(json.getString(CaaConstants.FEED_BACK_BETWEENQ)));
		//assertTrue("true".equals(json.getString(CaaConstants.STUDY_MODE)));
		assertTrue("20".equals(json.getString(CaaConstants.LATE_PENALTY)));
		assertTrue("11/25/2014 at 06:39 AM".equals(json.getString(CaaConstants.DUE_DATE)));
		assertTrue("10".equals(json.getString(CaaConstants.POINTS)));
	}
	
	/**
	 * This method is going to test that attempt information is populated properly or not
	 * @see AssignmentServiceUtil#populateAttemptInfoJSON(ResponseTO, Map, JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testpopulateAttemptInfoJSONCase5() throws Exception {
		TestTO testTO = new TestTO();
		List<QuestionMetaDataTO> questionMetaDataList = new ArrayList<QuestionMetaDataTO>();
		testTO.setQuestionMetaDataList(questionMetaDataList);
		Map<String,String> policyMap = new HashMap<String, String>();
		JSONObject activityJSON = new JSONObject();
		JSONObject attemptJSON = new JSONObject();
		attemptJSON.put(CaaConstants.FEED_BACK_BETWEENQ, true);
		attemptJSON.put(CaaConstants.STUDY_MODE, true);
		activityJSON.put(CaaConstants.ATTEMPT_INFO, attemptJSON);
		policyMap.put(classware_hm.POLICY_deduct_attempts, "20");
		policyMap.put(classware_hm.POLICY_endtime,"1416915579");
		policyMap.put(classware_hm.POLICY_duedate,"2014-11-25 06:39:00");
		policyMap.put(classware_hm.POLICY_feedback, "yes");
		policyMap.put(classware_hm.POLICY_genius, "yes");
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		testparameter.replaceParam(classware_hm.BUILD_ON_PREVIOUS, "true");
		responseTO.setTestParameter(testparameter);
		Mockito.doReturn("10").when(assignmentServiceUtilMock).getTotalPoints(questionMetaDataList, responseTO);
		assignmentServiceUtilMock.populateAttemptInfoJSON(responseTO, policyMap, activityJSON , testTO);
		System.out.println(activityJSON);
		JSONObject json = activityJSON.getJSONObject(CaaConstants.ATTEMPT_INFO);
		//assertTrue("true".equals(json.getString(CaaConstants.BUILD_ON_PREVIOUS)));
		assertTrue("true".equals(json.getString(CaaConstants.FEED_BACK_BETWEENQ)));
		assertTrue("true".equals(json.getString(CaaConstants.STUDY_MODE)));
		assertTrue("20".equals(json.getString(CaaConstants.LATE_PENALTY)));
		assertTrue("11/25/2014 at 06:39 AM".equals(json.getString(CaaConstants.DUE_DATE)));
		assertTrue("10".equals(json.getString(CaaConstants.POINTS)));
	}
	
	/**
	 * This method is going to test that attempt information is populated properly or not
	 * @see AssignmentServiceUtil#populateAttemptInfoJSON(ResponseTO, Map, JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testpopulateAttemptInfoJSONCase6() throws Exception {
		TestTO testTO = new TestTO();
		List<QuestionMetaDataTO> questionMetaDataList = new ArrayList<QuestionMetaDataTO>();
		testTO.setQuestionMetaDataList(questionMetaDataList);
		Map<String,String> policyMap = new HashMap<String, String>();
		JSONObject activityJSON = new JSONObject();
		JSONObject attemptJSON = new JSONObject();
		attemptJSON.put(CaaConstants.FEED_BACK_BETWEENQ, true);
		attemptJSON.put(CaaConstants.STUDY_MODE, true);
		attemptJSON.put(CaaConstants.DUE_DATE, "11/25/2018 at 17:09 AM");
		activityJSON.put(CaaConstants.ATTEMPT_INFO, attemptJSON);
		policyMap.put(classware_hm.POLICY_deduct_attempts, "20");
		policyMap.put(classware_hm.POLICY_endtime,"1416915579");
		policyMap.put(classware_hm.POLICY_feedback, "no");
		policyMap.put(classware_hm.POLICY_genius, "yes");
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		testparameter.replaceParam(classware_hm.BUILD_ON_PREVIOUS, "true");
		responseTO.setTestParameter(testparameter);
		Mockito.doReturn("10").when(assignmentServiceUtilMock).getTotalPoints(questionMetaDataList, responseTO);
		assignmentServiceUtilMock.populateAttemptInfoJSON(responseTO, policyMap, activityJSON , testTO);
		JSONObject json = activityJSON.getJSONObject(CaaConstants.ATTEMPT_INFO);
		//assertTrue("true".equals(json.getString(CaaConstants.BUILD_ON_PREVIOUS)));
		assertTrue("false".equals(json.getString(CaaConstants.FEED_BACK_BETWEENQ)));
		assertTrue("true".equals(json.getString(CaaConstants.STUDY_MODE)));
		assertTrue("20".equals(json.getString(CaaConstants.LATE_PENALTY)));
		assertTrue("11/25/2018 at 17:09 AM".equals(json.getString(CaaConstants.DUE_DATE)));
		assertTrue("10".equals(json.getString(CaaConstants.POINTS)));
	}
	
	/**
	 * This method is going to test that attempt information is populated properly or not
	 * @see AssignmentServiceUtil#populateAttemptInfoJSON(ResponseTO, Map, JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testpopulateAttemptInfoJSONCase7() throws Exception {
		TestTO testTO = new TestTO();
		List<QuestionMetaDataTO> questionMetaDataList = new ArrayList<QuestionMetaDataTO>();
		testTO.setQuestionMetaDataList(questionMetaDataList);
		Map<String,String> policyMap = new HashMap<String, String>();
		JSONObject activityJSON = new JSONObject();
		JSONObject attemptJSON = new JSONObject();
		attemptJSON.put(CaaConstants.FEED_BACK_BETWEENQ, true);
		attemptJSON.put(CaaConstants.STUDY_MODE, true);
		attemptJSON.put(CaaConstants.DUE_DATE, "11/25/2014 at 17:09:39 AM");
		activityJSON.put(CaaConstants.ATTEMPT_INFO, attemptJSON);
		policyMap.put(classware_hm.POLICY_deduct_attempts, "20");
		policyMap.put(classware_hm.POLICY_genius, "yes");
		policyMap.put(classware_hm.POLICY_feedback, "yes");
		//policyMap.put(classware_hm.POLICY_endtime,"1416915579");
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		testparameter.replaceParam(classware_hm.BUILD_ON_PREVIOUS, "true");
		responseTO.setTestParameter(testparameter);
		Mockito.doReturn("10").when(assignmentServiceUtilMock).getTotalPoints(questionMetaDataList, responseTO);
		assignmentServiceUtilMock.populateAttemptInfoJSON(responseTO, policyMap, activityJSON , testTO);
		JSONObject json = activityJSON.getJSONObject(CaaConstants.ATTEMPT_INFO);
		//assertTrue("true".equals(json.getString(CaaConstants.BUILD_ON_PREVIOUS)));
		assertTrue("true".equals(json.getString(CaaConstants.FEED_BACK_BETWEENQ)));
		assertTrue("true".equals(json.getString(CaaConstants.STUDY_MODE)));
		assertTrue("20".equals(json.getString(CaaConstants.LATE_PENALTY)));
		assertTrue("10".equals(json.getString(CaaConstants.POINTS)));
	}
	
	/**
	 * This method is going to test that attempt information is populated properly or not
	 * @see AssignmentServiceUtil#populateAttemptInfoJSON(ResponseTO, Map, JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testpopulateAttemptInfoJSONCase8() throws Exception {
		TestTO testTO = new TestTO();
		List<QuestionMetaDataTO> questionMetaDataList = new ArrayList<QuestionMetaDataTO>();
		testTO.setQuestionMetaDataList(questionMetaDataList);
		Map<String,String> policyMap = new HashMap<String, String>();
		JSONObject activityJSON = new JSONObject();
		JSONObject attemptJSON = new JSONObject();
		attemptJSON.put(CaaConstants.FEED_BACK_BETWEENQ, true);
		attemptJSON.put(CaaConstants.STUDY_MODE, true);
		attemptJSON.put(CaaConstants.DUE_DATE, "");
		activityJSON.put(CaaConstants.ATTEMPT_INFO, attemptJSON);
		policyMap.put(classware_hm.POLICY_deduct_attempts, "20");
		policyMap.put(classware_hm.POLICY_endtime,"1416915579");
		policyMap.put(classware_hm.POLICY_duedate,"2014-11-25 06:39:00");
		policyMap.put(classware_hm.POLICY_genius, "yes");
		policyMap.put(classware_hm.POLICY_feedback, "yes");
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		testparameter.replaceParam(classware_hm.BUILD_ON_PREVIOUS, "true");
		responseTO.setTestParameter(testparameter);
		Mockito.doReturn("10").when(assignmentServiceUtilMock).getTotalPoints(questionMetaDataList, responseTO);
		assignmentServiceUtilMock.populateAttemptInfoJSON(responseTO, policyMap, activityJSON , testTO);
		JSONObject json = activityJSON.getJSONObject(CaaConstants.ATTEMPT_INFO);
		//assertTrue("true".equals(json.getString(CaaConstants.BUILD_ON_PREVIOUS)));
		assertTrue("true".equals(json.getString(CaaConstants.FEED_BACK_BETWEENQ)));
		assertTrue("true".equals(json.getString(CaaConstants.STUDY_MODE)));
		assertTrue("20".equals(json.getString(CaaConstants.LATE_PENALTY)));
		assertTrue("11/25/2014 at 06:39 AM".equals(json.getString(CaaConstants.DUE_DATE)));
		assertTrue("10".equals(json.getString(CaaConstants.POINTS)));
	}
	
	/**
	 * This method is going to test that attempt information is populated properly or not
	 * @see AssignmentServiceUtil#populateAttemptInfoJSON(ResponseTO, Map, JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testpopulateAttemptInfoJSONCase9() throws Exception {
		TestTO testTO = new TestTO();
		List<QuestionMetaDataTO> questionMetaDataList = new ArrayList<QuestionMetaDataTO>();
		testTO.setQuestionMetaDataList(questionMetaDataList);
		JSONObject activityJSON = new JSONObject();
		Map<String,String> policyMap = new HashMap<String, String>();
		policyMap.put(classware_hm.POLICY_deduct_attempts, "20");
		policyMap.put(classware_hm.POLICY_feedback, "yes");
		policyMap.put(classware_hm.POLICY_genius , "yes");
		policyMap.put(classware_hm.POLICY_endtime,"1416915579");
		policyMap.put(classware_hm.POLICY_duedate,"2014-11-25 06:39:00");
		policyMap.put(classware_hm.POLICY_attempts, "-1");
		policyMap.put(classware_hm.POLICY_autoSubmit, "no");
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		testparameter.replaceParam(classware_hm.BUILD_ON_PREVIOUS, "true");
		responseTO.setTestParameter(testparameter);
		responseTO.setAttemptNo("10");
		Mockito.doReturn("10").when(assignmentServiceUtilMock).getTotalPoints(questionMetaDataList, responseTO);
		assignmentServiceUtilMock.populateAttemptInfoJSON(responseTO, policyMap, activityJSON , testTO);
		JSONObject json = activityJSON.getJSONObject(CaaConstants.ATTEMPT_INFO);
		//assertTrue("true".equals(json.getString(CaaConstants.BUILD_ON_PREVIOUS)));
		assertTrue("true".equals(json.getString(CaaConstants.FEED_BACK_BETWEENQ)));
		assertTrue("true".equals(json.getString(CaaConstants.STUDY_MODE)));
		assertTrue("20".equals(json.getString(CaaConstants.LATE_PENALTY)));
		assertTrue("11/25/2014 at 06:39 AM".equals(json.getString(CaaConstants.DUE_DATE)));
		assertTrue("10".equals(json.getString(CaaConstants.POINTS)));
		assertTrue("unlimited".equals(json.getString(CaaConstants.SCORED_ATTEMPT_MAX)));
		assertTrue("false".equals(json.getString(CaaConstants.AUTO_SUBMIT)));
		assertTrue("10".equals(json.getString(CaaConstants.ACTUAL_ATTEMPT_NO)));
		assertTrue("10".equals(json.getString(CaaConstants.DISPLAY_ATTEMPT_NO)));
	}
	
	/**
	 * This method is going to test that attempt information is populated properly or not
	 * @see AssignmentServiceUtil#populateAttemptInfoJSON(ResponseTO, Map, JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testpopulateAttemptInfoJSONCase10() throws Exception {
		TestTO testTO = new TestTO();
		List<QuestionMetaDataTO> questionMetaDataList = new ArrayList<QuestionMetaDataTO>();
		testTO.setQuestionMetaDataList(questionMetaDataList);
		JSONObject activityJSON = new JSONObject();
		Map<String,String> policyMap = new HashMap<String, String>();
		policyMap.put(classware_hm.POLICY_deduct_attempts, "20");
		policyMap.put(classware_hm.POLICY_feedback, "yes");
		policyMap.put(classware_hm.POLICY_genius , "yes");
		policyMap.put(classware_hm.POLICY_duedate,"2014-11-25 06:39:00");
		policyMap.put(classware_hm.POLICY_endtime,"1416915579");
		policyMap.put(classware_hm.POLICY_attempts, "-1");
		policyMap.put(classware_hm.POLICY_limit, "0");
		policyMap.put(classware_hm.POLICY_autoSubmit, "yes");
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		testparameter.replaceParam(classware_hm.BUILD_ON_PREVIOUS, "true");
		responseTO.setTestParameter(testparameter);
		responseTO.setAttemptNo("10");
		Mockito.doReturn("10").when(assignmentServiceUtilMock).getTotalPoints(questionMetaDataList, responseTO);
		assignmentServiceUtilMock.populateAttemptInfoJSON(responseTO, policyMap, activityJSON , testTO);
		JSONObject json = activityJSON.getJSONObject(CaaConstants.ATTEMPT_INFO);
		//assertTrue("true".equals(json.getString(CaaConstants.BUILD_ON_PREVIOUS)));
		assertTrue("true".equals(json.getString(CaaConstants.FEED_BACK_BETWEENQ)));
		assertTrue("true".equals(json.getString(CaaConstants.STUDY_MODE)));
		assertTrue("20".equals(json.getString(CaaConstants.LATE_PENALTY)));
		assertTrue("11/25/2014 at 06:39 AM".equals(json.getString(CaaConstants.DUE_DATE)));
		assertTrue("10".equals(json.getString(CaaConstants.POINTS)));
		assertTrue("unlimited".equals(json.getString(CaaConstants.SCORED_ATTEMPT_MAX)));
		assertTrue("true".equals(json.getString(CaaConstants.AUTO_SUBMIT)));
		assertTrue("10".equals(json.getString(CaaConstants.ACTUAL_ATTEMPT_NO)));
		assertTrue("10".equals(json.getString(CaaConstants.DISPLAY_ATTEMPT_NO)));
	}
	/**
	 * This method is going to test that instructor level information is populated properly or not
	 * @see AssignmentServiceUtil#populateInstructorInfo(ResponseTO, JSONObject) 
	 * @throws Exception
	 */
	@Test
	public void testpopulateInstructorInfoCase1() throws Exception {
		JSONObject activityJSON = new JSONObject();
		JSONObject attemptJSON = new JSONObject();
		attemptJSON.put(CaaConstants.INSTRUCTOR_NAME, "abcd");
		attemptJSON.put(CaaConstants.INSTRUCTOR_INFO, attemptJSON);
		activityJSON.put(CaaConstants.INSTRUCTOR_INFO, attemptJSON);
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		testparameter.replaceParam(classware_hm.AP_INSTRUCTOR_NAME, "mnop");
		testparameter.replaceParam(classware_hm.HEADER_INSTRUCTIONS, "this is one");
		responseTO.setTestParameter(testparameter);
		assignmentServiceUtilMock.populateInstructorInfo(responseTO,activityJSON);
		JSONObject json = activityJSON.getJSONObject(CaaConstants.INSTRUCTOR_INFO);
		assertTrue("abcd".equals(json.get(CaaConstants.INSTRUCTOR_NAME)));
		assertTrue("this is one".equals(json.getString(CaaConstants.INSTRUCTOR_INSTRUCTIONS)));
	}
	
	/**
	 * This method is going to test that instructor level information is populated properly or not
	 * @see AssignmentServiceUtil#populateInstructorInfo(ResponseTO, JSONObject) 
	 * @throws Exception
	 */
	@Test
	public void testpopulateInstructorInfoCase2() throws Exception {
		JSONObject activityJSON = new JSONObject();
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		testparameter.replaceParam(classware_hm.AP_INSTRUCTOR_NAME, "abcd");
		testparameter.replaceParam(classware_hm.HEADER_INSTRUCTIONS, "This is bad");
		responseTO.setTestParameter(testparameter);
		assignmentServiceUtilMock.populateInstructorInfo(responseTO,activityJSON);
		JSONObject json = activityJSON.getJSONObject(CaaConstants.INSTRUCTOR_INFO);
		assertTrue("abcd".equals(json.get(CaaConstants.INSTRUCTOR_NAME)));
		assertTrue("This is bad".equals(json.get(CaaConstants.INSTRUCTOR_INSTRUCTIONS)));
	}
	
	/**
	 * This method is going to test that instructor level information is populated properly or not
	 * @see AssignmentServiceUtil#populateInstructorInfo(ResponseTO, JSONObject) 
	 * @throws Exception
	 */
	@Test
	public void testpopulateInstructorInfoCase3() throws Exception {
		JSONObject activityJSON = new JSONObject();
		JSONObject attemptJSON = new JSONObject();
		attemptJSON.put(CaaConstants.INSTRUCTOR_NAME, "");
		attemptJSON.put(CaaConstants.INSTRUCTOR_INFO, attemptJSON);
		activityJSON.put(CaaConstants.INSTRUCTOR_INFO, attemptJSON);
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		testparameter.replaceParam(classware_hm.AP_INSTRUCTOR_NAME, "mnop");
		responseTO.setTestParameter(testparameter);
		assignmentServiceUtilMock.populateInstructorInfo(responseTO,activityJSON);
		JSONObject json = activityJSON.getJSONObject(CaaConstants.INSTRUCTOR_INFO);
		assertTrue("mnop".equals(json.get(CaaConstants.INSTRUCTOR_NAME)));
	}
	
	/**
	 * This method is going to check that due date is properly generated or not
	 * @see AssignmentServiceUtil#getDueDate(Map)
	 * @throws Exception
	 */
	//@Test
	public void testgetDueDateCase1() throws Exception {
		Map<String,String> policyMap = new HashMap<String, String>();
		policyMap.put(classware_hm.POLICY_endtime, "1417241040");
		assertTrue("11/29/2014 at 01:04 AM".equals(assignmentServiceUtilMock.getDueDate(policyMap)));
	}
	
	/**
	 * @see AssignmentServiceUtil#getDueDate(Map)
	 * @throws Exception
	 */
	@Test
	public void testgetDueDateCase2() throws Exception {
		Map<String,String> policyMap = new HashMap<String, String>();
		//policyMap.put(classware_hm.POLICY_endtime, "1417241040");
		assertTrue(StringUtils.isBlank((assignmentServiceUtilMock.getDueDate(policyMap))));
	}
	
	/**
	 * @see AssignmentServiceUtil#getDueDate(Map)
	 * @throws Exception
	 */
	@Test
	public void testgetDueDateCase3() throws Exception {
		Map<String,String> policyMap = new HashMap<String, String>();
		policyMap.put(classware_hm.POLICY_endtime, "abcd");
		assertTrue(StringUtils.isBlank((assignmentServiceUtilMock.getDueDate(policyMap))));
	}
	
	/**
	 * @see AssignmentServiceUtil#getDueDate(Map)
	 * @throws Exception
	 */
	@Test
	public void testgetDueDateCase4() throws Exception {
		Map<String,String> policyMap = new HashMap<String, String>();
		policyMap.put(classware_hm.POLICY_endtime, "1417241040");
		policyMap.put(classware_hm.POLICY_duedate, "2015-01-29 23:59:00");
		assertTrue("01/29/2015 at 11:59 PM".equals(assignmentServiceUtilMock.getDueDate(policyMap)));
	}
	
	/**
	 * @see AssignmentServiceUtil#getDueDate(Map)
	 * @throws Exception
	 */
	@Test
	public void testgetDueDateCase5() throws Exception {
		Map<String,String> policyMap = new HashMap<String, String>();
		policyMap.put(classware_hm.POLICY_endtime, "1417241040");
		policyMap.put(classware_hm.POLICY_duedate, "2015-01-29 11:59:00");
		assertTrue("01/29/2015 at 11:59 AM".equals(assignmentServiceUtilMock.getDueDate(policyMap)));
	}
	
	/**
	 * This method is going to test that whether the total point is coming properly or not.In this case QLIST contains
	 * 4 question and out of it one is section break which does not hold any point.Questions contains default point which is 10.
	 * @see AssignmentServiceUtil#getTotalPoints(List, ResponseTO)
	 * @throws Exception
	 */
	@Test
	public void testGetTotalPointsCase1() throws Exception {
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		String qlist = "1,2,4;5";
		responseTO.setTestID("123456");
		Mockito.when(itemServiceMock.getFlaggedItems(responseTO.getTestID())).thenReturn(null);
		testparameter.replaceParam(CaaConstants.QLIST, qlist);
		responseTO.setTestParameter(testparameter);
		List<QuestionMetaDataTO> questionMetaDataTOList = new ArrayList<QuestionMetaDataTO>();
		QuestionMetaDataTO questionMetaDataTO1 = new QuestionMetaDataTO();
		questionMetaDataTO1.setQuestionID("1");
		questionMetaDataTO1.setQuestionType(question.QUESTION_TYPE_fillBlank);
		QuestionMetaDataTO questionMetaDataTO2 = new QuestionMetaDataTO();
		questionMetaDataTO2.setQuestionID("2");
		questionMetaDataTO2.setQuestionType(question.QUESTION_TYPE_fillBlank);
		QuestionMetaDataTO questionMetaDataTO3 = new QuestionMetaDataTO();
		questionMetaDataTO3.setQuestionID("3");
		questionMetaDataTO3.setQuestionType(question.QUESTION_TYPE_fillBlank);
		QuestionMetaDataTO questionMetaDataTO4 = new QuestionMetaDataTO();
		questionMetaDataTO4.setQuestionID("4");
		questionMetaDataTO4.setQuestionType(question.QUESTION_TYPE_sectionbreak);
		QuestionMetaDataTO questionMetaDataTO5 = new QuestionMetaDataTO();
		questionMetaDataTO5.setQuestionID("5");
		questionMetaDataTO5.setQuestionType(question.QUESTION_TYPE_fillBlank);
		questionMetaDataTOList.add(questionMetaDataTO1);
		questionMetaDataTOList.add(questionMetaDataTO2);
		questionMetaDataTOList.add(questionMetaDataTO3);
		questionMetaDataTOList.add(questionMetaDataTO4);
		questionMetaDataTOList.add(questionMetaDataTO5);
		String totalPoints = assignmentServiceUtilMock.getTotalPoints(questionMetaDataTOList, responseTO);
		System.out.println(totalPoints);
		assertTrue("30".equals(totalPoints));
	}
	
	/**
	 * This method is going to test that whether the total point is coming properly or not.In this case QLIST contains
	 * 4 question and out of it one is survey question which does not hold any point.Other questions contains default point which is 10.
	 * @see AssignmentServiceUtil#getTotalPoints(List, ResponseTO)
	 * @throws Exception
	 */
	@Test
	public void testGetTotalPointsCase2() throws Exception {
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		String qlist = "1,2,4,5";
		testparameter.replaceParam(CaaConstants.QLIST, qlist);
		responseTO.setTestParameter(testparameter);
		responseTO.setTestID("123456");
		Mockito.when(itemServiceMock.getFlaggedItems(responseTO.getTestID())).thenReturn(null);
		List<QuestionMetaDataTO> questionMetaDataTOList = new ArrayList<QuestionMetaDataTO>();
		QuestionMetaDataTO questionMetaDataTO1 = new QuestionMetaDataTO();
		questionMetaDataTO1.setQuestionID("1");
		questionMetaDataTO1.setQuestionType(question.QUESTION_TYPE_fillBlank);
		QuestionMetaDataTO questionMetaDataTO2 = new QuestionMetaDataTO();
		questionMetaDataTO2.setQuestionID("2");
		questionMetaDataTO2.setQuestionType(question.QUESTION_TYPE_fillBlank);
		QuestionMetaDataTO questionMetaDataTO3 = new QuestionMetaDataTO();
		questionMetaDataTO3.setQuestionID("3");
		questionMetaDataTO3.setQuestionType(question.QUESTION_TYPE_fillBlank);
		QuestionMetaDataTO questionMetaDataTO4 = new QuestionMetaDataTO();
		questionMetaDataTO4.setQuestionID("4");
		questionMetaDataTO4.setQuestionType(question.QUESTION_TYPE_survey);
		QuestionMetaDataTO questionMetaDataTO5 = new QuestionMetaDataTO();
		questionMetaDataTO5.setQuestionID("5");
		questionMetaDataTO5.setQuestionType(question.QUESTION_TYPE_fillBlank);
		questionMetaDataTOList.add(questionMetaDataTO1);
		questionMetaDataTOList.add(questionMetaDataTO2);
		questionMetaDataTOList.add(questionMetaDataTO3);
		questionMetaDataTOList.add(questionMetaDataTO4);
		questionMetaDataTOList.add(questionMetaDataTO5);
		String totalPoints = assignmentServiceUtilMock.getTotalPoints(questionMetaDataTOList, responseTO);
		System.out.println(totalPoints);
		assertTrue("30".equals(totalPoints));
	}
	
	/**
	 * This method is going to test that whether the total point is coming properly or not.In this case QLIST contains
	 * 5 question and out of it one is survey question which does not hold any point.Questions contains default point which is 10.
	 * @see AssignmentServiceUtil#getTotalPoints(List, ResponseTO)
	 * @throws Exception
	 */
	@Test
	public void testGetTotalPointsCase3() throws Exception {
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		String qlist = "1,2,3,4,5";
		testparameter.replaceParam(CaaConstants.QLIST, qlist);
		responseTO.setTestParameter(testparameter);
		responseTO.setTestID("123456");
		Mockito.when(itemServiceMock.getFlaggedItems(responseTO.getTestID())).thenReturn(null);
		List<QuestionMetaDataTO> questionMetaDataTOList = new ArrayList<QuestionMetaDataTO>();
		QuestionMetaDataTO questionMetaDataTO1 = new QuestionMetaDataTO();
		questionMetaDataTO1.setQuestionID("1");
		questionMetaDataTO1.setQuestionType(question.QUESTION_TYPE_fillBlank);
		QuestionMetaDataTO questionMetaDataTO2 = new QuestionMetaDataTO();
		questionMetaDataTO2.setQuestionID("2");
		questionMetaDataTO2.setQuestionType(question.QUESTION_TYPE_fillBlank);
		QuestionMetaDataTO questionMetaDataTO3 = new QuestionMetaDataTO();
		questionMetaDataTO3.setQuestionID("3");
		questionMetaDataTO3.setQuestionType(question.QUESTION_TYPE_fillBlank);
		QuestionMetaDataTO questionMetaDataTO4 = new QuestionMetaDataTO();
		questionMetaDataTO4.setQuestionID("4");
		questionMetaDataTO4.setQuestionType(question.QUESTION_TYPE_survey);
		QuestionMetaDataTO questionMetaDataTO5 = new QuestionMetaDataTO();
		questionMetaDataTO5.setQuestionID("5");
		questionMetaDataTO5.setQuestionType(question.QUESTION_TYPE_fillBlank);
		questionMetaDataTOList.add(questionMetaDataTO1);
		questionMetaDataTOList.add(questionMetaDataTO2);
		questionMetaDataTOList.add(questionMetaDataTO3);
		questionMetaDataTOList.add(questionMetaDataTO4);
		questionMetaDataTOList.add(questionMetaDataTO5);
		String totalPoints = assignmentServiceUtilMock.getTotalPoints(questionMetaDataTOList, responseTO);
		System.out.println(totalPoints);
		assertTrue("40".equals(totalPoints));
	}
	
	/**
	 * This method is going to test that whether the total point is coming properly or not.In this case QLIST contains
	 * 5 question and out of it one is survey question which does not hold any point.But in these questions,one question
	 * contains 20 points.Others contains default point which is 10.
	 * @see AssignmentServiceUtil#getTotalPoints(List, ResponseTO)
	 * @throws Exception
	 */
	@Test
	public void testGetTotalPointsCase4() throws Exception {
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		String qlist = "1,2,3,4,5";
		testparameter.replaceParam(CaaConstants.QLIST, qlist);
		responseTO.setTestParameter(testparameter);
		responseTO.setTestID("123456");
		Mockito.when(itemServiceMock.getFlaggedItems(responseTO.getTestID())).thenReturn(null);
		List<QuestionMetaDataTO> questionMetaDataTOList = new ArrayList<QuestionMetaDataTO>();
		QuestionMetaDataTO questionMetaDataTO1 = new QuestionMetaDataTO();
		questionMetaDataTO1.setQuestionID("1");
		questionMetaDataTO1.setPoints("20");
		questionMetaDataTO1.setQuestionType(question.QUESTION_TYPE_fillBlank);
		QuestionMetaDataTO questionMetaDataTO2 = new QuestionMetaDataTO();
		questionMetaDataTO2.setQuestionID("2");
		questionMetaDataTO2.setQuestionType(question.QUESTION_TYPE_fillBlank);
		QuestionMetaDataTO questionMetaDataTO3 = new QuestionMetaDataTO();
		questionMetaDataTO3.setQuestionID("3");
		questionMetaDataTO3.setQuestionType(question.QUESTION_TYPE_fillBlank);
		QuestionMetaDataTO questionMetaDataTO4 = new QuestionMetaDataTO();
		questionMetaDataTO4.setQuestionID("4");
		questionMetaDataTO4.setQuestionType(question.QUESTION_TYPE_survey);
		QuestionMetaDataTO questionMetaDataTO5 = new QuestionMetaDataTO();
		questionMetaDataTO5.setQuestionID("5");
		questionMetaDataTO5.setQuestionType(question.QUESTION_TYPE_fillBlank);
		questionMetaDataTOList.add(questionMetaDataTO1);
		questionMetaDataTOList.add(questionMetaDataTO2);
		questionMetaDataTOList.add(questionMetaDataTO3);
		questionMetaDataTOList.add(questionMetaDataTO4);
		questionMetaDataTOList.add(questionMetaDataTO5);
		String totalPoints = assignmentServiceUtilMock.getTotalPoints(questionMetaDataTOList, responseTO);
		System.out.println(totalPoints);
		assertTrue("50".equals(totalPoints));
	}
	
	/**
	 * This method is going to test that whether the total point is coming properly or not.In this case QLIST contains
	 * 5 questions.But in these questions,one question contains 20 points.Others contains default point which is 10.
	 * @see AssignmentServiceUtil#getTotalPoints(List, ResponseTO)
	 * @throws Exception
	 */
	@Test
	public void testGetTotalPointsCase5() throws Exception {
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		String qlist = "1,2,3,4,5";
		testparameter.replaceParam(CaaConstants.QLIST, qlist);
		responseTO.setTestParameter(testparameter);
		responseTO.setTestID("123456");
		Mockito.when(itemServiceMock.getFlaggedItems(responseTO.getTestID())).thenReturn(null);
		List<QuestionMetaDataTO> questionMetaDataTOList = new ArrayList<QuestionMetaDataTO>();
		QuestionMetaDataTO questionMetaDataTO1 = new QuestionMetaDataTO();
		questionMetaDataTO1.setQuestionID("1");
		questionMetaDataTO1.setPoints("20");
		questionMetaDataTO1.setQuestionType(question.QUESTION_TYPE_fillBlank);
		QuestionMetaDataTO questionMetaDataTO2 = new QuestionMetaDataTO();
		questionMetaDataTO2.setQuestionID("2");
		questionMetaDataTO2.setQuestionType(question.QUESTION_TYPE_fillBlank);
		QuestionMetaDataTO questionMetaDataTO3 = new QuestionMetaDataTO();
		questionMetaDataTO3.setQuestionID("3");
		questionMetaDataTO3.setQuestionType(question.QUESTION_TYPE_fillBlank);
		QuestionMetaDataTO questionMetaDataTO4 = new QuestionMetaDataTO();
		questionMetaDataTO4.setQuestionID("4");
		questionMetaDataTO4.setQuestionType(question.QUESTION_TYPE_fillBlank);
		QuestionMetaDataTO questionMetaDataTO5 = new QuestionMetaDataTO();
		questionMetaDataTO5.setQuestionID("5");
		questionMetaDataTO5.setQuestionType(question.QUESTION_TYPE_fillBlank);
		questionMetaDataTOList.add(questionMetaDataTO1);
		questionMetaDataTOList.add(questionMetaDataTO2);
		questionMetaDataTOList.add(questionMetaDataTO3);
		questionMetaDataTOList.add(questionMetaDataTO4);
		questionMetaDataTOList.add(questionMetaDataTO5);
		String totalPoints = assignmentServiceUtilMock.getTotalPoints(questionMetaDataTOList, responseTO);
		System.out.println(totalPoints);
		assertTrue("60".equals(totalPoints));
	}
	
	/**
	 * This method is going to test that whether the total point is coming properly or not.In this case QLIST contains
	 * 5 question and out of it one is survey question which does not hold any point.But in these questions,all questions
	 * contains 1 points.
	 * @see AssignmentServiceUtil#getTotalPoints(List, ResponseTO)
	 * @throws Exception
	 */
	@Test
	public void testGetTotalPointsCase6() throws Exception {
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		String qlist = "1,2,3,4,5";
		testparameter.replaceParam(CaaConstants.QLIST, qlist);
		responseTO.setTestParameter(testparameter);
		responseTO.setTestID("123456");
		Mockito.when(itemServiceMock.getFlaggedItems(responseTO.getTestID())).thenReturn(null);
		List<QuestionMetaDataTO> questionMetaDataTOList = new ArrayList<QuestionMetaDataTO>();
		QuestionMetaDataTO questionMetaDataTO1 = new QuestionMetaDataTO();
		questionMetaDataTO1.setQuestionID("1");
		questionMetaDataTO1.setPoints("1");
		questionMetaDataTO1.setQuestionType(question.QUESTION_TYPE_fillBlank);
		QuestionMetaDataTO questionMetaDataTO2 = new QuestionMetaDataTO();
		questionMetaDataTO2.setQuestionID("2");
		questionMetaDataTO2.setPoints("1");
		questionMetaDataTO2.setQuestionType(question.QUESTION_TYPE_fillBlank);
		QuestionMetaDataTO questionMetaDataTO3 = new QuestionMetaDataTO();
		questionMetaDataTO3.setQuestionID("3");
		questionMetaDataTO3.setPoints("1");
		questionMetaDataTO3.setQuestionType(question.QUESTION_TYPE_fillBlank);
		QuestionMetaDataTO questionMetaDataTO4 = new QuestionMetaDataTO();
		questionMetaDataTO4.setQuestionID("4");
		questionMetaDataTO4.setPoints("1");
		questionMetaDataTO4.setQuestionType(question.QUESTION_TYPE_fillBlank);
		QuestionMetaDataTO questionMetaDataTO5 = new QuestionMetaDataTO();
		questionMetaDataTO5.setQuestionID("5");
		questionMetaDataTO5.setPoints("1");
		questionMetaDataTO5.setQuestionType(question.QUESTION_TYPE_fillBlank);
		questionMetaDataTOList.add(questionMetaDataTO1);
		questionMetaDataTOList.add(questionMetaDataTO2);
		questionMetaDataTOList.add(questionMetaDataTO3);
		questionMetaDataTOList.add(questionMetaDataTO4);
		questionMetaDataTOList.add(questionMetaDataTO5);
		String totalPoints = assignmentServiceUtilMock.getTotalPoints(questionMetaDataTOList, responseTO);
		assertTrue("5".equals(totalPoints));
	}
	
	/**
	 * This method is going to test that whether the total point is coming properly or not.Here qlist contains 4 questions.Ecah question
	 * contains only a single point not the default point.
	 * @see AssignmentServiceUtil#getTotalPoints(List, ResponseTO)
	 * @throws Exception
	 */
	@Test
	public void testGetTotalPointsCase7() throws Exception {
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		String qlist = "1,2,4,5";
		testparameter.replaceParam(CaaConstants.QLIST, qlist);
		responseTO.setTestParameter(testparameter);
		responseTO.setTestID("123456");
		Mockito.when(itemServiceMock.getFlaggedItems(responseTO.getTestID())).thenReturn(null);
		List<QuestionMetaDataTO> questionMetaDataTOList = new ArrayList<QuestionMetaDataTO>();
		QuestionMetaDataTO questionMetaDataTO1 = new QuestionMetaDataTO();
		questionMetaDataTO1.setQuestionID("1");
		questionMetaDataTO1.setPoints("1");
		questionMetaDataTO1.setQuestionType(question.QUESTION_TYPE_fillBlank);
		QuestionMetaDataTO questionMetaDataTO2 = new QuestionMetaDataTO();
		questionMetaDataTO2.setQuestionID("2");
		questionMetaDataTO2.setPoints("1");
		questionMetaDataTO2.setQuestionType(question.QUESTION_TYPE_fillBlank);
		QuestionMetaDataTO questionMetaDataTO3 = new QuestionMetaDataTO();
		questionMetaDataTO3.setQuestionID("3");
		questionMetaDataTO3.setPoints("1");
		questionMetaDataTO3.setQuestionType(question.QUESTION_TYPE_fillBlank);
		QuestionMetaDataTO questionMetaDataTO4 = new QuestionMetaDataTO();
		questionMetaDataTO4.setQuestionID("4");
		questionMetaDataTO4.setPoints("1");
		questionMetaDataTO4.setQuestionType(question.QUESTION_TYPE_fillBlank);
		QuestionMetaDataTO questionMetaDataTO5 = new QuestionMetaDataTO();
		questionMetaDataTO5.setQuestionID("5");
		questionMetaDataTO5.setPoints("1");
		questionMetaDataTO5.setQuestionType(question.QUESTION_TYPE_fillBlank);
		questionMetaDataTOList.add(questionMetaDataTO1);
		questionMetaDataTOList.add(questionMetaDataTO2);
		questionMetaDataTOList.add(questionMetaDataTO3);
		questionMetaDataTOList.add(questionMetaDataTO4);
		questionMetaDataTOList.add(questionMetaDataTO5);
		String totalPoints = assignmentServiceUtilMock.getTotalPoints(questionMetaDataTOList, responseTO);
		assertTrue("4".equals(totalPoints));
	}
	
	/**
	 * This method is going to test that whether the total point is coming properly or not.In this case QLIST contains 4 questions.
	 * Each questions contain only a single point>out of the four questions two questions' credits are dropped.So these questions'
	 * point should not contribute while counting the total point
	 * @see AssignmentServiceUtil#getTotalPoints(List, ResponseTO)
	 * @throws Exception
	 */
	@Test
	public void testGetTotalPointsCase8() throws Exception {
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		String qlist = "1,2,4,5";
		testparameter.replaceParam(CaaConstants.QLIST, qlist);
		responseTO.setTestParameter(testparameter);
		responseTO.setTestID("123456");
		Map<String,List<String>> flaggedItems = new HashMap<String, List<String>>();
		List<String> droppedItems = new ArrayList<String>();
		droppedItems.add("1");
		droppedItems.add("2");
		flaggedItems.put("dropped", droppedItems);
		Mockito.when(itemServiceMock.getFlaggedItems(responseTO.getTestID())).thenReturn(flaggedItems);
		List<QuestionMetaDataTO> questionMetaDataTOList = new ArrayList<QuestionMetaDataTO>();
		QuestionMetaDataTO questionMetaDataTO1 = new QuestionMetaDataTO();
		questionMetaDataTO1.setQuestionID("1");
		questionMetaDataTO1.setPoints("1");
		questionMetaDataTO1.setQuestionType(question.QUESTION_TYPE_fillBlank);
		QuestionMetaDataTO questionMetaDataTO2 = new QuestionMetaDataTO();
		questionMetaDataTO2.setQuestionID("2");
		questionMetaDataTO2.setPoints("1");
		questionMetaDataTO2.setQuestionType(question.QUESTION_TYPE_fillBlank);
		QuestionMetaDataTO questionMetaDataTO3 = new QuestionMetaDataTO();
		questionMetaDataTO3.setQuestionID("3");
		questionMetaDataTO3.setPoints("1");
		questionMetaDataTO3.setQuestionType(question.QUESTION_TYPE_fillBlank);
		QuestionMetaDataTO questionMetaDataTO4 = new QuestionMetaDataTO();
		questionMetaDataTO4.setQuestionID("4");
		questionMetaDataTO4.setPoints("1");
		questionMetaDataTO4.setQuestionType(question.QUESTION_TYPE_fillBlank);
		QuestionMetaDataTO questionMetaDataTO5 = new QuestionMetaDataTO();
		questionMetaDataTO5.setQuestionID("5");
		questionMetaDataTO5.setPoints("1");
		questionMetaDataTO5.setQuestionType(question.QUESTION_TYPE_fillBlank);
		questionMetaDataTOList.add(questionMetaDataTO1);
		questionMetaDataTOList.add(questionMetaDataTO2);
		questionMetaDataTOList.add(questionMetaDataTO3);
		questionMetaDataTOList.add(questionMetaDataTO4);
		questionMetaDataTOList.add(questionMetaDataTO5);
		String totalPoints = assignmentServiceUtilMock.getTotalPoints(questionMetaDataTOList, responseTO);
		assertTrue("2".equals(totalPoints));
	}
	
	/**
	 * This method is going to test that whether the total point is coming properly or not>in this case QLIST contains 4 questions.
	 * And two other questions which are not included in the QLIST are dropped.So in this case total point does not vary based on the
	 * dropped credited items 
	 * @see AssignmentServiceUtil#getTotalPoints(List, ResponseTO)
	 * @throws Exception
	 */
	@Test
	public void testGetTotalPointsCase9() throws Exception {
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		String qlist = "1,2,4,5";
		testparameter.replaceParam(CaaConstants.QLIST, qlist);
		responseTO.setTestParameter(testparameter);
		responseTO.setTestID("123456");
		Map<String,List<String>> flaggedItems = new HashMap<String, List<String>>();
		List<String> droppedItems = new ArrayList<String>();
		droppedItems.add("7");
		droppedItems.add("8");
		flaggedItems.put("dropped", droppedItems);
		Mockito.when(itemServiceMock.getFlaggedItems(responseTO.getTestID())).thenReturn(flaggedItems);
		List<QuestionMetaDataTO> questionMetaDataTOList = new ArrayList<QuestionMetaDataTO>();
		QuestionMetaDataTO questionMetaDataTO1 = new QuestionMetaDataTO();
		questionMetaDataTO1.setQuestionID("1");
		questionMetaDataTO1.setPoints("1");
		questionMetaDataTO1.setQuestionType(question.QUESTION_TYPE_fillBlank);
		QuestionMetaDataTO questionMetaDataTO2 = new QuestionMetaDataTO();
		questionMetaDataTO2.setQuestionID("2");
		questionMetaDataTO2.setPoints("1");
		questionMetaDataTO2.setQuestionType(question.QUESTION_TYPE_fillBlank);
		QuestionMetaDataTO questionMetaDataTO3 = new QuestionMetaDataTO();
		questionMetaDataTO3.setQuestionID("3");
		questionMetaDataTO3.setPoints("1");
		questionMetaDataTO3.setQuestionType(question.QUESTION_TYPE_fillBlank);
		QuestionMetaDataTO questionMetaDataTO4 = new QuestionMetaDataTO();
		questionMetaDataTO4.setQuestionID("4");
		questionMetaDataTO4.setPoints("1");
		questionMetaDataTO4.setQuestionType(question.QUESTION_TYPE_fillBlank);
		QuestionMetaDataTO questionMetaDataTO5 = new QuestionMetaDataTO();
		questionMetaDataTO5.setQuestionID("5");
		questionMetaDataTO5.setPoints("1");
		questionMetaDataTO5.setQuestionType(question.QUESTION_TYPE_fillBlank);
		questionMetaDataTOList.add(questionMetaDataTO1);
		questionMetaDataTOList.add(questionMetaDataTO2);
		questionMetaDataTOList.add(questionMetaDataTO3);
		questionMetaDataTOList.add(questionMetaDataTO4);
		questionMetaDataTOList.add(questionMetaDataTO5);
		String totalPoints = assignmentServiceUtilMock.getTotalPoints(questionMetaDataTOList, responseTO);
		assertTrue("4".equals(totalPoints));
	}
	
	/**
	 * This method is going to test that whether the total point is coming properly or not.In this case all questions that are mentioned in the
	 * QLIST are dropped.So the total points should be zero 
	 * @see AssignmentServiceUtil#getTotalPoints(List, ResponseTO)
	 * @throws Exception
	 */
	@Test
	public void testGetTotalPointsCase10() throws Exception {
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		String qlist = "1,2,4,5";
		testparameter.replaceParam(CaaConstants.QLIST, qlist);
		responseTO.setTestParameter(testparameter);
		responseTO.setTestID("123456");
		Map<String,List<String>> flaggedItems = new HashMap<String, List<String>>();
		List<String> droppedItems = new ArrayList<String>();
		droppedItems.add("1");
		droppedItems.add("2");
		droppedItems.add("5");
		droppedItems.add("4");
		flaggedItems.put("dropped", droppedItems);
		Mockito.when(itemServiceMock.getFlaggedItems(responseTO.getTestID())).thenReturn(flaggedItems);
		List<QuestionMetaDataTO> questionMetaDataTOList = new ArrayList<QuestionMetaDataTO>();
		QuestionMetaDataTO questionMetaDataTO1 = new QuestionMetaDataTO();
		questionMetaDataTO1.setQuestionID("1");
		questionMetaDataTO1.setPoints("1");
		questionMetaDataTO1.setQuestionType(question.QUESTION_TYPE_fillBlank);
		QuestionMetaDataTO questionMetaDataTO2 = new QuestionMetaDataTO();
		questionMetaDataTO2.setQuestionID("2");
		questionMetaDataTO2.setPoints("1");
		questionMetaDataTO2.setQuestionType(question.QUESTION_TYPE_fillBlank);
		QuestionMetaDataTO questionMetaDataTO3 = new QuestionMetaDataTO();
		questionMetaDataTO3.setQuestionID("3");
		questionMetaDataTO3.setPoints("1");
		questionMetaDataTO3.setQuestionType(question.QUESTION_TYPE_fillBlank);
		QuestionMetaDataTO questionMetaDataTO4 = new QuestionMetaDataTO();
		questionMetaDataTO4.setQuestionID("4");
		questionMetaDataTO4.setPoints("1");
		questionMetaDataTO4.setQuestionType(question.QUESTION_TYPE_fillBlank);
		QuestionMetaDataTO questionMetaDataTO5 = new QuestionMetaDataTO();
		questionMetaDataTO5.setQuestionID("5");
		questionMetaDataTO5.setPoints("1");
		questionMetaDataTO5.setQuestionType(question.QUESTION_TYPE_fillBlank);
		questionMetaDataTOList.add(questionMetaDataTO1);
		questionMetaDataTOList.add(questionMetaDataTO2);
		questionMetaDataTOList.add(questionMetaDataTO3);
		questionMetaDataTOList.add(questionMetaDataTO4);
		questionMetaDataTOList.add(questionMetaDataTO5);
		String totalPoints = assignmentServiceUtilMock.getTotalPoints(questionMetaDataTOList, responseTO);
		assertTrue("0".equals(totalPoints));
	}
	
	/**
	 * This method is going to test that whether the total point is coming properly or not.In this scenario all the questions mentioned
	 * in the QLIST are dropped except one question which contains a single point.WSo the total point of the assignment should be 1. 
	 * @see AssignmentServiceUtil#getTotalPoints(List, ResponseTO)
	 * @throws Exception
	 */
	@Test
	public void testGetTotalPointsCase11() throws Exception {
		ResponseTO responseTO = new ResponseTO();
		CustomMap<String, String> testparameter = new CustomMap<String, String>();
		String qlist = "1,2,4,5";
		testparameter.replaceParam(CaaConstants.QLIST, qlist);
		responseTO.setTestParameter(testparameter);
		responseTO.setTestID("123456");
		Map<String,List<String>> flaggedItems = new HashMap<String, List<String>>();
		List<String> droppedItems = new ArrayList<String>();
		droppedItems.add("1");
		droppedItems.add("2");
		droppedItems.add("5");
		flaggedItems.put("dropped", droppedItems);
		Mockito.when(itemServiceMock.getFlaggedItems(responseTO.getTestID())).thenReturn(flaggedItems);
		List<QuestionMetaDataTO> questionMetaDataTOList = new ArrayList<QuestionMetaDataTO>();
		QuestionMetaDataTO questionMetaDataTO1 = new QuestionMetaDataTO();
		questionMetaDataTO1.setQuestionID("1");
		questionMetaDataTO1.setPoints("1");
		questionMetaDataTO1.setQuestionType(question.QUESTION_TYPE_fillBlank);
		QuestionMetaDataTO questionMetaDataTO2 = new QuestionMetaDataTO();
		questionMetaDataTO2.setQuestionID("2");
		questionMetaDataTO2.setPoints("1");
		questionMetaDataTO2.setQuestionType(question.QUESTION_TYPE_fillBlank);
		QuestionMetaDataTO questionMetaDataTO3 = new QuestionMetaDataTO();
		questionMetaDataTO3.setQuestionID("3");
		questionMetaDataTO3.setPoints("1");
		questionMetaDataTO3.setQuestionType(question.QUESTION_TYPE_fillBlank);
		QuestionMetaDataTO questionMetaDataTO4 = new QuestionMetaDataTO();
		questionMetaDataTO4.setQuestionID("4");
		questionMetaDataTO4.setPoints("1");
		questionMetaDataTO4.setQuestionType(question.QUESTION_TYPE_fillBlank);
		QuestionMetaDataTO questionMetaDataTO5 = new QuestionMetaDataTO();
		questionMetaDataTO5.setQuestionID("5");
		questionMetaDataTO5.setPoints("1");
		questionMetaDataTO5.setQuestionType(question.QUESTION_TYPE_fillBlank);
		questionMetaDataTOList.add(questionMetaDataTO1);
		questionMetaDataTOList.add(questionMetaDataTO2);
		questionMetaDataTOList.add(questionMetaDataTO3);
		questionMetaDataTOList.add(questionMetaDataTO4);
		questionMetaDataTOList.add(questionMetaDataTO5);
		String totalPoints = assignmentServiceUtilMock.getTotalPoints(questionMetaDataTOList, responseTO);
		assertTrue("1".equals(totalPoints));
	}
	
	
	/**
	 * This method is going to test that a test is mobile safe or not
	 * @see AssignmentServiceUtil#isTestMobileSafe(requestMap, TestTO,  JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testIsTestMobileSafeRequestCase1() throws Exception {
		Map<String, String> requestMap = new HashMap<String, String>();
		TestTO testTO = new TestTO();
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(true);
		assertTrue(assignmentServiceUtilMock.isTestMobileSafe(requestMap, testTO, json));
		assertTrue(!json.has(CaaConstants.MOBILE_UNSAFE_REASON));
	}
	
	/**
	 * This method is going to test that a test is mobile safe or not
	 * @see AssignmentServiceUtil#isTestMobileSafe(requestMap, TestTO,  JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testIsTestMobileSafeRequestCase2() throws Exception {
		Map<String, String> requestMap = new HashMap<String, String>();
		TestTO testTO = new TestTO();
		testTO.setMobileUnSafeReason(CaaConstants.ASSIGNMENT_UNSUPPORTED_MEDIA);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		assertTrue(!assignmentServiceUtilMock.isTestMobileSafe(requestMap, testTO, json));
		assertTrue(CaaConstants.ASSIGNMENT_UNSUPPORTED_MEDIA.equals(json.getString(CaaConstants.MOBILE_UNSAFE_REASON)));
	}
	
	/**
	 * This method is going to test that a test is mobile safe or not
	 * @see AssignmentServiceUtil#isTestMobileSafe(requestMap, TestTO,  JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testIsTestMobileSafeRequestCase3() throws Exception {
		Map<String, String> requestMap = new HashMap<String, String>();
		requestMap.put(classware_hm.POLICY_feedback, CaaConstants.YES);
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		
		assertTrue(!assignmentServiceUtilMock.isTestMobileSafe(requestMap, testTO, json));
		assertTrue(CaaConstants.ASSIGNMENT_POLICY_ALLOW_FEEDBACK.equals(json.getString(CaaConstants.MOBILE_UNSAFE_REASON)));
	}
	
	/**
	 * This method is going to test that a test is mobile safe or not
	 * @see AssignmentServiceUtil#isTestMobileSafe(requestMap, TestTO,  JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testIsTestMobileSafeRequestCase4() throws Exception {
		Map<String, String> requestMap = new HashMap<String, String>();
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		
		assertTrue(assignmentServiceUtilMock.isTestMobileSafe(requestMap, testTO, json));
		assertTrue(!json.has(CaaConstants.MOBILE_UNSAFE_REASON));
	}
	
	/**
	 * This method is going to test that a test is mobile safe or not
	 * @see AssignmentServiceUtil#isTestMobileSafe(requestMap, TestTO,  JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testIsTestMobileSafeRequestCase5() throws Exception {
		Map<String, String> requestMap = new HashMap<String, String>();
		TestTO testTO = new TestTO();
		testTO.setMobileReady(false);
		testTO.setMobileUnSafeReason(CaaConstants.ASSIGNMENT_UNSUPPORTED_QUESTION);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		assertTrue(!assignmentServiceUtilMock.isTestMobileSafe(requestMap, testTO, json));
		assertTrue(CaaConstants.ASSIGNMENT_UNSUPPORTED_QUESTION.equals(json.getString(CaaConstants.MOBILE_UNSAFE_REASON)));
	}
	
	/**
	 * This method is going to test that a test is mobile safe or not
	 * @see AssignmentServiceUtil#isTestMobileSafe(requestMap, TestTO,  JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testIsTestMobileSafeRequestCase6() throws Exception {
		Map<String, String> requestMap = new HashMap<String, String>();
		requestMap.put(classware_hm.POLICY_limit, "60");
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		Mockito.when(licenseServiceMock.isFeaturePresent("enableTimedAssignmentInMobile")).thenReturn(false);
		
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		
		assertFalse(assignmentServiceUtilMock.isTestMobileSafe(requestMap, testTO, json));
		assertTrue(CaaConstants.ASSIGNMENT_POLICY_TIME_LIMIT.equals(json.getString(CaaConstants.MOBILE_UNSAFE_REASON)));
	}

	/**
	 * This method is going to test that a test is mobile safe or not
	 * @see AssignmentServiceUtil#isTestMobileSafe(requestMap, TestTO,  JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testIsTestMobileSafeRequestCase8() throws Exception {
		Map<String, String> requestMap = new HashMap<String, String>();
		requestMap.put(classware_hm.POLICY_limit, "no");
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		Mockito.when(licenseServiceMock.isFeaturePresent("enableTimedAssignmentInMobile")).thenReturn(false);
		
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		
		assertTrue(assignmentServiceUtilMock.isTestMobileSafe(requestMap, testTO, json));
		assertTrue(!json.has(CaaConstants.MOBILE_UNSAFE_REASON));
	}
	
	/**
	 * This method is going to test that a test is mobile safe or not
	 * @see AssignmentServiceUtil#isTestMobileSafe(requestMap, TestTO,  JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testIsTestMobileSafeRequestCase9() throws Exception {
		Map<String, String> requestMap = new HashMap<String, String>();
		requestMap.put(classware_hm.POLICY_limit, "0");
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		Mockito.when(licenseServiceMock.isFeaturePresent("enableTimedAssignmentInMobile")).thenReturn(false);
		
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		
		assertTrue(assignmentServiceUtilMock.isTestMobileSafe(requestMap, testTO, json));
		assertTrue(!json.has(CaaConstants.MOBILE_UNSAFE_REASON));
	}
	
	/**
	 * This method is going to test that a test is mobile safe or not
	 * @see AssignmentServiceUtil#isTestMobileSafe(requestMap, TestTO,  JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testIsTestMobileSafeRequestCase10() throws Exception {
		Map<String, String> requestMap = new HashMap<String, String>();
		requestMap.put(classware_hm.POLICY_limit, "");
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		Mockito.when(licenseServiceMock.isFeaturePresent("enableTimedAssignmentInMobile")).thenReturn(false);
		
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		
		assertTrue(assignmentServiceUtilMock.isTestMobileSafe(requestMap, testTO, json));
		assertTrue(!json.has(CaaConstants.MOBILE_UNSAFE_REASON));
	}
	
	@Test
	public void testIsTestMobileSafeRequestCase12() throws Exception {
		Map<String, String> requestMap = new HashMap<String, String>();
		requestMap.put(classware_hm.POLICY_limit, "60");
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		Mockito.when(licenseServiceMock.isFeaturePresent("enableTimedAssignmentInMobile")).thenReturn(true);
		
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		
		assertTrue(assignmentServiceUtilMock.isTestMobileSafe(requestMap, testTO, json));
		assertTrue(!json.has(CaaConstants.MOBILE_UNSAFE_REASON));
	}
	
	/**
	 * This method is going to test that a test is mobile safe or not
	 * @see AssignmentServiceUtil#isTestMobileSafe(requestMap, TestTO,  JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testIsTestMobileSafeRequestCase13() throws Exception {
		Map<String, String> requestMap = new HashMap<String, String>();
		requestMap.put(classware_hm.POLICY_limit, "abcd");
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		Mockito.when(licenseServiceMock.isFeaturePresent("enableTimedAssignmentInMobile")).thenReturn(false);
		
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		
		assertTrue(assignmentServiceUtilMock.isTestMobileSafe(requestMap, testTO, json));
		assertTrue(!json.has(CaaConstants.MOBILE_UNSAFE_REASON));
	}
	
	/**
	 * This method is going to test that a test is mobile safe or not
	 * @see AssignmentServiceUtil#isTestMobileSafe(requestMap, TestTO,  JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testIsTestMobileSafeRequestCase14() throws Exception {
		Map<String, String> requestMap = new HashMap<String, String>();
		requestMap.put(classware_hm.POLICY_limit, "  no  ");
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		Mockito.when(licenseServiceMock.isFeaturePresent("enableTimedAssignmentInMobile")).thenReturn(false);
		
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		
		assertTrue(assignmentServiceUtilMock.isTestMobileSafe(requestMap, testTO, json));
		assertTrue(!json.has(CaaConstants.MOBILE_UNSAFE_REASON));
	}
	
	/**
	 * This method is going to test that a test is mobile safe or not
	 * @see AssignmentServiceUtil#isTestMobileSafe(requestMap, TestTO,  JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testIsTestMobileSafeRequestCase15() throws Exception {
		Map<String, String> requestMap = new HashMap<String, String>();
		requestMap.put(classware_hm.POLICY_limit, "    ");
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		Mockito.when(licenseServiceMock.isFeaturePresent("enableTimedAssignmentInMobile")).thenReturn(false);
		
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		
		assertTrue(assignmentServiceUtilMock.isTestMobileSafe(requestMap, testTO, json));
		assertTrue(!json.has(CaaConstants.MOBILE_UNSAFE_REASON));
	}
	
	/**
	 * This method is going to test that a test is mobile safe or not
	 * @see AssignmentServiceUtil#isTestMobileSafe(requestMap, TestTO,  JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testIsTestMobileSafeRequestCase18() throws Exception {
		Map<String, String> requestMap = new HashMap<String, String>();
		requestMap.put(classware_hm.POLICY_limit, "60");
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		Mockito.when(licenseServiceMock.isFeaturePresent("enableTimedAssignmentInMobile")).thenReturn(false);
		
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		
		assertFalse(assignmentServiceUtilMock.isTestMobileSafe(requestMap, testTO, json));
		assertTrue(CaaConstants.ASSIGNMENT_POLICY_TIME_LIMIT.equals(json.getString(CaaConstants.MOBILE_UNSAFE_REASON)));
	}
	
	/**
	 * This method is going to test that a test is mobile safe or not
	 * @see AssignmentServiceUtil#isTestMobileSafe(requestMap, TestTO,  JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testIsTestMobileSafeRequestCase20() throws Exception {
		Map<String, String> requestMap = new HashMap<String, String>();
		requestMap.put(classware_hm.POLICY_limit, null);
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		Mockito.when(licenseServiceMock.isFeaturePresent("enableTimedAssignmentInMobile")).thenReturn(false);
		
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		
		assertTrue(assignmentServiceUtilMock.isTestMobileSafe(requestMap, testTO, json));
		assertTrue(!json.has(CaaConstants.MOBILE_UNSAFE_REASON));
	}
	
	/**
	 * This method is going to test that a test is mobile safe or not
	 * @see AssignmentServiceUtil#isTestMobileSafe(requestMap, TestTO,  JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testIsTestMobileSafeRequestCase21() throws Exception {
		Map<String, String> requestMap = new HashMap<String, String>();
		requestMap.put(classware_hm.POLICY_hardlimit, "60");
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		Mockito.when(licenseServiceMock.isFeaturePresent("enableTimedAssignmentInMobile")).thenReturn(false);
		
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		
		assertFalse(assignmentServiceUtilMock.isTestMobileSafe(requestMap, testTO, json));
		assertTrue(CaaConstants.ASSIGNMENT_APPROACHING_DUE_DATE.equals(json.getString(CaaConstants.MOBILE_UNSAFE_REASON)));
	}

	/**
	 * This method is going to test that a test is mobile safe or not
	 * @see AssignmentServiceUtil#isTestMobileSafe(requestMap, TestTO,  JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testIsTestMobileSafeRequestCase22() throws Exception {
		Map<String, String> requestMap = new HashMap<String, String>();
		requestMap.put(classware_hm.POLICY_hardlimit, "no");
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		Mockito.when(licenseServiceMock.isFeaturePresent("enableTimedAssignmentInMobile")).thenReturn(false);
		
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		
		assertTrue(assignmentServiceUtilMock.isTestMobileSafe(requestMap, testTO, json));
		assertTrue(!json.has(CaaConstants.MOBILE_UNSAFE_REASON));
	}
	
	/**
	 * This method is going to test that a test is mobile safe or not
	 * @see AssignmentServiceUtil#isTestMobileSafe(requestMap, TestTO,  JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testIsTestMobileSafeRequestCase23() throws Exception {
		Map<String, String> requestMap = new HashMap<String, String>();
		requestMap.put(classware_hm.POLICY_hardlimit, "0");
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		Mockito.when(licenseServiceMock.isFeaturePresent("enableTimedAssignmentInMobile")).thenReturn(false);
		
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		
		assertTrue(assignmentServiceUtilMock.isTestMobileSafe(requestMap, testTO, json));
		assertTrue(!json.has(CaaConstants.MOBILE_UNSAFE_REASON));
	}
	
	/**
	 * This method is going to test that a test is mobile safe or not
	 * @see AssignmentServiceUtil#isTestMobileSafe(requestMap, TestTO,  JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testIsTestMobileSafeRequestCase24() throws Exception {
		Map<String, String> requestMap = new HashMap<String, String>();
		requestMap.put(classware_hm.POLICY_hardlimit, "");
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		Mockito.when(licenseServiceMock.isFeaturePresent("enableTimedAssignmentInMobile")).thenReturn(false);
		
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		
		assertTrue(assignmentServiceUtilMock.isTestMobileSafe(requestMap, testTO, json));
		assertTrue(!json.has(CaaConstants.MOBILE_UNSAFE_REASON));
	}
	
	@Test
	public void testIsTestMobileSafeRequestCase25() throws Exception {
		Map<String, String> requestMap = new HashMap<String, String>();
		requestMap.put(classware_hm.POLICY_hardlimit, "60");
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		Mockito.when(licenseServiceMock.isFeaturePresent("enableTimedAssignmentInMobile")).thenReturn(true);
		
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		
		assertTrue(assignmentServiceUtilMock.isTestMobileSafe(requestMap, testTO, json));
		assertTrue(!json.has(CaaConstants.MOBILE_UNSAFE_REASON));
	}
	
	/**
	 * This method is going to test that a test is mobile safe or not
	 * @see AssignmentServiceUtil#isTestMobileSafe(requestMap, TestTO,  JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testIsTestMobileSafeRequestCase26() throws Exception {
		Map<String, String> requestMap = new HashMap<String, String>();
		requestMap.put(classware_hm.POLICY_hardlimit, "abcd");
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		Mockito.when(licenseServiceMock.isFeaturePresent("enableTimedAssignmentInMobile")).thenReturn(false);
		
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		
		assertTrue(assignmentServiceUtilMock.isTestMobileSafe(requestMap, testTO, json));
		assertTrue(!json.has(CaaConstants.MOBILE_UNSAFE_REASON));
	}
	
	/**
	 * This method is going to test that a test is mobile safe or not
	 * @see AssignmentServiceUtil#isTestMobileSafe(requestMap, TestTO,  JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testIsTestMobileSafeRequestCase27() throws Exception {
		Map<String, String> requestMap = new HashMap<String, String>();
		requestMap.put(classware_hm.POLICY_hardlimit, "  no  ");
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		Mockito.when(licenseServiceMock.isFeaturePresent("enableTimedAssignmentInMobile")).thenReturn(false);
		
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		
		assertTrue(assignmentServiceUtilMock.isTestMobileSafe(requestMap, testTO, json));
		assertTrue(!json.has(CaaConstants.MOBILE_UNSAFE_REASON));
	}
	
	/**
	 * This method is going to test that a test is mobile safe or not
	 * @see AssignmentServiceUtil#isTestMobileSafe(requestMap, TestTO,  JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testIsTestMobileSafeRequestCase28() throws Exception {
		Map<String, String> requestMap = new HashMap<String, String>();
		requestMap.put(classware_hm.POLICY_hardlimit, "    ");
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		Mockito.when(licenseServiceMock.isFeaturePresent("enableTimedAssignmentInMobile")).thenReturn(false);
		
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		
		assertTrue(assignmentServiceUtilMock.isTestMobileSafe(requestMap, testTO, json));
		assertTrue(!json.has(CaaConstants.MOBILE_UNSAFE_REASON));
	}
	
	/**
	 * This method is going to test that a test is mobile safe or not
	 * @see AssignmentServiceUtil#isTestMobileSafe(requestMap, TestTO,  JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testIsTestMobileSafeRequestCase29() throws Exception {
		Map<String, String> requestMap = new HashMap<String, String>();
		requestMap.put(classware_hm.POLICY_hardlimit, "60");
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		Mockito.when(licenseServiceMock.isFeaturePresent("enableTimedAssignmentInMobile")).thenReturn(false);
		
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		
		assertFalse(assignmentServiceUtilMock.isTestMobileSafe(requestMap, testTO, json));
		assertTrue(CaaConstants.ASSIGNMENT_APPROACHING_DUE_DATE.equals(json.getString(CaaConstants.MOBILE_UNSAFE_REASON)));
	}
	
	/**
	 * This method is going to test that a test is mobile safe or not
	 * @see AssignmentServiceUtil#isTestMobileSafe(requestMap, TestTO,  JSONObject)
	 * @throws Exception
	 */
	@Test
	public void testIsTestMobileSafeRequestCase30() throws Exception {
		Map<String, String> requestMap = new HashMap<String, String>();
		requestMap.put(classware_hm.POLICY_hardlimit, null);
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		Mockito.when(licenseServiceMock.isFeaturePresent("enableTimedAssignmentInMobile")).thenReturn(false);
		
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		
		assertTrue(assignmentServiceUtilMock.isTestMobileSafe(requestMap, testTO, json));
		assertTrue(!json.has(CaaConstants.MOBILE_UNSAFE_REASON));
	}
	
	@Test
	public void testIsTestMobileSafeRequestBOP1() throws Exception {
		Map<String, String> requestMap = new HashMap<String, String>();
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		Mockito.when(licenseServiceMock.isFeaturePresent("enableTimedAssignmentInMobile")).thenReturn(false);
		
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		
		assertTrue(assignmentServiceUtilMock.isTestMobileSafe(requestMap, testTO, json));
		assertTrue(!json.has(CaaConstants.MOBILE_UNSAFE_REASON));
	}
	
	@Test
	public void testIsTestMobileSafeRequestBOP2() throws Exception {
		Map<String, String> requestMap = new HashMap<String, String>();
		requestMap.put(classware_hm.SUBMISSION_ID, "123");
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		Mockito.when(licenseServiceMock.isFeaturePresent("enableTimedAssignmentInMobile")).thenReturn(false);
		
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		
		assertFalse(assignmentServiceUtilMock.isTestMobileSafe(requestMap, testTO, json));
		assertTrue(CaaConstants.ASSIGNMENT_POLICY_BOP.equals(json.getString(CaaConstants.MOBILE_UNSAFE_REASON)));
	}
	
	@Test
	public void testIsTestMobileSafeRequestBOP3() throws Exception {
		Map<String, String> requestMap = new HashMap<String, String>();
		requestMap.put(classware_hm.POLICY_genius, CaaConstants.YES);
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		Mockito.when(licenseServiceMock.isFeaturePresent("enableTimedAssignmentInMobile")).thenReturn(false);
		
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		
		assertTrue(assignmentServiceUtilMock.isTestMobileSafe(requestMap, testTO, json));
		assertTrue(!json.has(CaaConstants.MOBILE_UNSAFE_REASON));
	}
	
	@Test
	public void testIsTestMobileSafeRequestBOP4() throws Exception {
		Map<String, String> requestMap = new HashMap<String, String>();
		requestMap.put(classware_hm.SUBMISSION_ID, "123");
		requestMap.put(classware_hm.POLICY_genius, CaaConstants.YES);
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		Mockito.when(licenseServiceMock.isFeaturePresent("enableTimedAssignmentInMobile")).thenReturn(false);
		
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		
		assertTrue(assignmentServiceUtilMock.isTestMobileSafe(requestMap, testTO, json));
		assertTrue(!json.has(CaaConstants.MOBILE_UNSAFE_REASON));
	}
	
	@Test
	public void testIsTestMobileSafeRequestBOP5() throws Exception {
		Map<String, String> requestMap = new HashMap<String, String>();
		requestMap.put(classware_hm.POLICY_genius, CaaConstants.NO);
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		Mockito.when(licenseServiceMock.isFeaturePresent("enableTimedAssignmentInMobile")).thenReturn(false);
		
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		
		assertTrue(assignmentServiceUtilMock.isTestMobileSafe(requestMap, testTO, json));
		assertTrue(!json.has(CaaConstants.MOBILE_UNSAFE_REASON));
	}
	
	@Test
	public void testIsTestMobileSafeRequestBOP6() throws Exception {
		Map<String, String> requestMap = new HashMap<String, String>();
		requestMap.put(classware_hm.SUBMISSION_ID, "123");
		requestMap.put(classware_hm.POLICY_genius, CaaConstants.NO);
		TestTO testTO = new TestTO();
		testTO.setMobileReady(true);
		JSONObject json = new JSONObject();
		Mockito.when(licenseServiceMock.isDebuggerPresent("disableMobileSafe")).thenReturn(false);
		Mockito.when(licenseServiceMock.isFeaturePresent("enableTimedAssignmentInMobile")).thenReturn(false);
		
		Mockito.doReturn(true).when(testServiceImpl).validateMobileReady(testTO);
		
		assertFalse(assignmentServiceUtilMock.isTestMobileSafe(requestMap, testTO, json));
		assertTrue(CaaConstants.ASSIGNMENT_POLICY_BOP.equals(json.getString(CaaConstants.MOBILE_UNSAFE_REASON)));
	}
	
	@Test
	public void testGetCompStatusOneWayString1() throws Exception{
		CustomMap<String, String> testParamMap = null;
		String qid = "123";
		
		assertTrue(StringUtils.isBlank(assignmentServiceUtil.getCompStatusOneWayString(testParamMap, qid)));
	}
	
	@Test
	public void testGetCompStatusOneWayString2() throws Exception{
		CustomMap<String, String> testParamMap = new CustomMap<String, String>();
		String qid = "";
		
		assertTrue(StringUtils.isBlank(assignmentServiceUtil.getCompStatusOneWayString(testParamMap, qid)));
	}
	
	@Test
	public void testGetCompStatusOneWayString3() throws Exception{
		CustomMap<String, String> testParamMap = new CustomMap<String, String>();
		String qid = "123";
		
		assertTrue(StringUtils.isNotBlank(assignmentServiceUtil.getCompStatusOneWayString(testParamMap, qid)));
		assertTrue(qid.equals(assignmentServiceUtil.getCompStatusOneWayString(testParamMap, qid)));
	}
	
	@Test
	public void testGetCompStatusOneWayString4() throws Exception{
		CustomMap<String, String> testParamMap = new CustomMap<String, String>();
		String compStatusOneway = "123";
		testParamMap.replaceParam(classware_hm.COMPLETION_STATUS_ONEWAY, compStatusOneway);
		String qid = "456";
		
		assertTrue(StringUtils.isNotBlank(assignmentServiceUtil.getCompStatusOneWayString(testParamMap, qid)));
		assertTrue((compStatusOneway+"+"+qid).equals(assignmentServiceUtil.getCompStatusOneWayString(testParamMap, qid)));
	}
	
	@Test
	public void testGetCompStatusOneWayString5() throws Exception{
		CustomMap<String, String> testParamMap = new CustomMap<String, String>();
		String qid = null;
		
		assertTrue(StringUtils.isBlank(assignmentServiceUtil.getCompStatusOneWayString(testParamMap, qid)));
	}
	
	@Test
	public void testGetCompStatusOneWayString6() throws Exception{
		CustomMap<String, String> testParamMap = new CustomMap<String, String>();
		String qid = "   ";
		
		assertTrue(StringUtils.isBlank(assignmentServiceUtil.getCompStatusOneWayString(testParamMap, qid)));
	}
	
	/**
	 * This method is going to test that ">" and "<" sign is replaced properly or not
	 * @see AssignmentServiceUtil#replaceWithHTMLTag(String)
	 * @throws Exception
	 */
	@Test
	public void testreplaceWithHTMLTagCase1() throws Exception {
		String str = assignmentServiceUtil.replaceWithHTMLTag("!@#$%^&amp;*()-_+={}[]\\|<testing>,./?");
		_logger.info("Modified String :: " + str);
		assertTrue(str.equals("!@#$%^&amp;*()-_+={}[]\\|&lt;testing&gt;,./?"));
	}
	
	/**
	 * This method is going to test that ">" and "<" sign is replaced properly or not
	 * @see AssignmentServiceUtil#replaceWithHTMLTag(String)
	 * @throws Exception
	 */
	@Test
	public void testreplaceWithHTMLTagCase2() throws Exception {
		String str = assignmentServiceUtil.replaceWithHTMLTag("");
		assertTrue(str.equals(""));
	}
	
	/**
	 * This method is going to test that ">" and "<" sign is replaced properly or not
	 * @see AssignmentServiceUtil#replaceWithHTMLTag(String)
	 * @throws Exception
	 */
	@Test
	public void testreplaceWithHTMLTagCase3() throws Exception {
		String str = assignmentServiceUtil.replaceWithHTMLTag("xyz");
		assertTrue(str.equals("xyz"));
	}
	
	@Test
	public void testCalculateElapsedTime01() throws Exception{
		SetActivityTO activityTO = null;
		ResponseTO responseTO = new ResponseTO();
		try{
			assignmentServiceUtil.calculateElapsedTime(activityTO, responseTO);
		}catch(Exception ex){
			System.out.println(ex.getMessage());
			assertTrue("activityTO or responseTO is coming as Null".equals(ex.getMessage()));
		}
	}
	
	@Test
	public void testCalculateElapsedTime02() throws Exception{
		SetActivityTO activityTO = new SetActivityTO();
		ResponseTO responseTO = null;
		try{
			assignmentServiceUtil.calculateElapsedTime(activityTO, responseTO);
		}catch(Exception ex){
			System.out.println(ex.getMessage());
			assertTrue("activityTO or responseTO is coming as Null".equals(ex.getMessage()));
		}
	}
	
	@Test
	public void testCalculateElapsedTime03() throws Exception{
		SetActivityTO activityTO = new SetActivityTO();
		ResponseTO responseTO = new ResponseTO();
		try{
			assignmentServiceUtil.calculateElapsedTime(activityTO, responseTO);
		}catch(Exception ex){
			System.out.println(ex.getMessage());
			assertTrue("ItemId is coming as Blank".equals(ex.getMessage()));
		}
	}
	
	@Test
	public void testCalculateElapsedTime04() throws Exception{
		SetActivityTO activityTO = new SetActivityTO();
		ResponseTO responseTO = new ResponseTO();
		responseTO.setAttemptPK(123456);
		activityTO.setItemID("123456");
		try{
			assignmentServiceUtil.calculateElapsedTime(activityTO, responseTO);
		}catch(Exception ex){
			System.out.println(ex.getMessage());
			assertTrue("testParameter is Coming as Null or Empty for attempPK : 123456".equals(ex.getMessage()));
		}
	}
	
	@Test
	public void testCalculateElapsedTime05() throws Exception{
		long attemptPk = 123456l;
		String questionId = "123456";
		SetActivityTO activityTO = new SetActivityTO();
		ResponseTO responseTO = new ResponseTO();
		responseTO.setAttemptPK(attemptPk);
		activityTO.setItemID(questionId);
		CustomMap<String, String> testParam = new CustomMap<String, String>();
		testParam.put("dummyKey", "dummyValue");
		responseTO.getTestParameter().putAll(testParam);
		QuestionWiseResponseTO questionWiseResponseTO = assignmentServiceUtil.calculateElapsedTime(activityTO, responseTO);
		String itemId = responseTO.getTestParameter().getParam(CaaConstants.ITEMID);
		String paamItemStartTime = responseTO.getTestParameter().getParam(CaaConstants.PAAM_ITEM_STARTTIME); 
		System.out.println(" paamItemStartTime : "+paamItemStartTime);
		assertTrue(questionWiseResponseTO == null && questionId.equals(itemId));
	}
	
	@Test
	public void testCalculateElapsedTime06() throws Exception{
		long attemptPk = 123456l;
		String questionId = "123456";
		SetActivityTO activityTO = new SetActivityTO();
		ResponseTO responseTO = new ResponseTO();
		responseTO.setAttemptPK(attemptPk);
		activityTO.setItemID(questionId);
		CustomMap<String, String> testParam = new CustomMap<String, String>();
		testParam.put("dummyKey", "dummyValue");
		testParam.put(CaaConstants.ITEMID, "12345");
		testParam.put(CaaConstants.PAAM_ITEM_STARTTIME, "1423738409223");
		responseTO.getTestParameter().putAll(testParam);
		try{
			assignmentServiceUtil.calculateElapsedTime(activityTO, responseTO);
		}catch(Exception ex){
			System.out.println(ex.getMessage());
			assertTrue("QuestionWiseResponseTO is Coming as Null attempPK : 123456, questionid : 12345".equals(ex.getMessage()));
		}
	}
	
	@Test
	public void testCalculateElapsedTime07() throws Exception{
		long attemptPk = 123456l;
		String currQId = "123456";
		String prevQid = "12345";
		SetActivityTO activityTO = new SetActivityTO();
		ResponseTO responseTO = new ResponseTO();
		QuestionWiseResponseTO questionWiseResponseTO = new QuestionWiseResponseTO();
		responseTO.setAttemptPK(attemptPk);
		activityTO.setItemID(currQId);
		CustomMap<String, String> testParam = new CustomMap<String, String>();
		testParam.put("dummyKey", "dummyValue");
		testParam.put(CaaConstants.ITEMID, "12345");
		testParam.put(CaaConstants.PAAM_ITEM_STARTTIME, "1423738409223");
		responseTO.getTestParameter().putAll(testParam);
		responseTO.getResponseMap().put(prevQid, questionWiseResponseTO);
		Mockito.doReturn(120l).when(userResponseServiceImplHelperSpy).getMaxTimeOnTaskLimit(Mockito.anyLong(), Mockito.anyLong());
		QuestionWiseResponseTO prevQuestionWiseResponseTO = assignmentServiceUtilMock.calculateElapsedTime(activityTO, responseTO);
		String currItemId = responseTO.getTestParameter().getParam(CaaConstants.ITEMID);
		String paamCurrItemStartTime = responseTO.getTestParameter().getParam(CaaConstants.PAAM_ITEM_STARTTIME);
		System.out.println(" paamCurrItemStartTime : "+paamCurrItemStartTime);
		assertTrue(currQId.equals(currItemId) && 120l == prevQuestionWiseResponseTO.getElapsed());
	}
	
	@Test
	public void testCalculateElapsedTime08() throws Exception{
		long attemptPk = 123456l;
		String currQId = "123456";
		String prevQid = "12345";
		SetActivityTO activityTO = new SetActivityTO();
		ResponseTO responseTO = new ResponseTO();
		QuestionWiseResponseTO questionWiseResponseTO = new QuestionWiseResponseTO();
		questionWiseResponseTO.setElapsed(120l);
		responseTO.setElapsedTime(120l);
		responseTO.setAttemptPK(attemptPk);
		activityTO.setItemID(currQId);
		CustomMap<String, String> testParam = new CustomMap<String, String>();
		testParam.put("dummyKey", "dummyValue");
		testParam.put(CaaConstants.ITEMID, "12345");
		testParam.put(CaaConstants.PAAM_ITEM_STARTTIME, "1423738409223");
		responseTO.getTestParameter().putAll(testParam);
		responseTO.getResponseMap().put(prevQid, questionWiseResponseTO);
		Mockito.doReturn(240l).when(userResponseServiceImplHelperSpy).getMaxTimeOnTaskLimit(Mockito.anyLong(), Mockito.anyLong());
		QuestionWiseResponseTO prevQuestionWiseResponseTO = assignmentServiceUtilMock.calculateElapsedTime(activityTO, responseTO);
		String currItemId = responseTO.getTestParameter().getParam(CaaConstants.ITEMID);
		String paamCurrItemStartTime = responseTO.getTestParameter().getParam(CaaConstants.PAAM_ITEM_STARTTIME);
		System.out.println(" paamCurrItemStartTime : "+paamCurrItemStartTime);
		assertTrue(currQId.equals(currItemId) && 240l == prevQuestionWiseResponseTO.getElapsed());
	}
	
	@Test
	public void testCalculateElapsedTime09() throws Exception{
		long attemptPk = 123456l;
		String currQId = "123456";
		String prevQid = "12345";
		SetActivityTO activityTO = new SetActivityTO();
		ResponseTO responseTO = new ResponseTO();
		QuestionWiseResponseTO questionWiseResponseTO = new QuestionWiseResponseTO();
		questionWiseResponseTO.setElapsed(120l);
		responseTO.setElapsedTime(120l);
		responseTO.setAttemptPK(attemptPk);
		activityTO.setItemID(currQId);
		activityTO.setSaveExit(true);
		CustomMap<String, String> testParam = new CustomMap<String, String>();
		testParam.put("dummyKey", "dummyValue");
		testParam.put(CaaConstants.ITEMID, "12345");
		testParam.put(CaaConstants.PAAM_ITEM_STARTTIME, "1423740717321");
		responseTO.getTestParameter().putAll(testParam);
		responseTO.getResponseMap().put(prevQid, questionWiseResponseTO);
		Mockito.doReturn(240l).when(userResponseServiceImplHelperSpy).getMaxTimeOnTaskLimit(Mockito.anyLong(), Mockito.anyLong());
		QuestionWiseResponseTO prevQuestionWiseResponseTO = assignmentServiceUtilMock.calculateElapsedTime(activityTO, responseTO);
		String currItemId = responseTO.getTestParameter().getParam(CaaConstants.ITEMID);
		String paamCurrItemStartTime = responseTO.getTestParameter().getParam(CaaConstants.PAAM_ITEM_STARTTIME);
		System.out.println(" paamCurrItemStartTime : "+paamCurrItemStartTime);
		assertTrue(currQId.equals(currItemId) && 240l == prevQuestionWiseResponseTO.getElapsed());
	}
	
	@Test
	public void testGetMultiPartQuestionMap01() throws Exception{
		String qList =null;
		String itemID = "7";
		try{
			assignmentServiceUtilMock.getMultiPartQuestionMap(qList, itemID);
		}catch (Exception e) {
			assertTrue("QLIST or itemID is blank for qList :: null and itemID :7".equals(e.getMessage()));
		}
	}
	
	@Test
	public void testGetMultiPartQuestionMap02() throws Exception{
		String qList = "1;2;3;4,5,6,7";
		String itemID = "";
		try{
			Map<String, List<String>> multiPartMap = assignmentServiceUtilMock.getMultiPartQuestionMap(qList, itemID);
			assertTrue(multiPartMap == null);
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	@Test
	public void testGetMultiPartQuestionMap03() throws Exception{
		String qList = "1;2;3;4,5,6,7";
		String itemID = "1";
		question sectionbreakQ = new sectionBreak();
		try{
			sectionbreakQ.sqlID = "1";
			sectionbreakQ.referenceTag = "1234";
			Mockito.when(cacheServiceMock.getItem("4")).thenReturn(sectionbreakQ);
			Map<String, List<String>> multiPartMap = assignmentServiceUtilMock.getMultiPartQuestionMap(qList, itemID);
			assertTrue(multiPartMap == null);
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	@Test
	public void testGetMultiPartQuestionMap04() throws Exception{
		String qList = "1;2;3;4,5,6,7";
		String itemID = "7";
		question sectionbreakQ = new sectionBreak();
		try{
			sectionbreakQ.sqlID = "4";
			sectionbreakQ.referenceTag = "1234";
			Mockito.when(cacheServiceMock.getItem("4")).thenReturn(sectionbreakQ);
			Map<String, List<String>> multiPartMap = assignmentServiceUtilMock.getMultiPartQuestionMap(qList, itemID);
			System.out.println(" multiPartMap : "+multiPartMap);
			assertTrue(multiPartMap.get("SB").contains("4") &&
					multiPartMap.get("part").contains("5") &&
					multiPartMap.get("part").contains("6") &&
					multiPartMap.get("part").contains("7"));
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	@Test
	public void testGetMultiPartQuestionMap05() throws Exception{
		String qList = "1;2;3;4,,6,7";
		String itemID = "7";
		question sectionbreakQ = new sectionBreak();
		try{
			sectionbreakQ.sqlID = "4";
			sectionbreakQ.referenceTag = "1234";
			Mockito.when(cacheServiceMock.getItem("4")).thenReturn(sectionbreakQ);
			Map<String, List<String>> multiPartMap = assignmentServiceUtilMock.getMultiPartQuestionMap(qList, itemID);
			System.out.println(" multiPartMap : "+multiPartMap);
			assertTrue(multiPartMap.get("SB").contains("4") &&
					multiPartMap.get("part").contains("6") &&
					multiPartMap.get("part").contains("7"));
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	@Test
	public void testGetMultiPartQuestionMap06() throws Exception{
		String qList = "1;2;3;4,5,6,7";
		String itemID = "17";
		question sectionbreakQ = new sectionBreak();
		try{
			sectionbreakQ.sqlID = "4";
			sectionbreakQ.referenceTag = "1234";
			Mockito.when(cacheServiceMock.getItem("4")).thenReturn(sectionbreakQ);
			Map<String, List<String>> multiPartMap = assignmentServiceUtilMock.getMultiPartQuestionMap(qList, itemID);
			System.out.println(" multiPartMap : "+multiPartMap);
			assertTrue(multiPartMap == null);
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	@Test
	public void testGetMultiPartQuestionMap07() throws Exception{
		String qList = "1;2;3;,,,7";
		String itemID = "17";
		question sectionbreakQ = new sectionBreak();
		try{
			sectionbreakQ.sqlID = "4";
			sectionbreakQ.referenceTag = "1234";
			Mockito.when(cacheServiceMock.getItem("4")).thenReturn(sectionbreakQ);
			Map<String, List<String>> multiPartMap = assignmentServiceUtilMock.getMultiPartQuestionMap(qList, itemID);
			System.out.println(" multiPartMap : "+multiPartMap);
			assertTrue(multiPartMap == null);
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	@Test
	public void testGetMultiPartQuestionMap08() throws Exception{
		String qList = "1;2;3;4,5,6,7;9;10,11,12,13";
		String itemID = "13";
		question sectionbreakQ1 = new sectionBreak();
		question sectionbreakQ2 = new sectionBreak();
		try{
			sectionbreakQ1.sqlID = "4";
			sectionbreakQ1.referenceTag = "1234";
			Mockito.when(cacheServiceMock.getItem("4")).thenReturn(sectionbreakQ1);
			sectionbreakQ2.sqlID = "10";
			sectionbreakQ2.referenceTag = "4321";
			Mockito.when(cacheServiceMock.getItem("10")).thenReturn(sectionbreakQ2);
			Map<String, List<String>> multiPartMap = assignmentServiceUtilMock.getMultiPartQuestionMap(qList, itemID);
			System.out.println(" multiPartMap : "+multiPartMap);
			assertTrue(multiPartMap.get("SB").contains("10") &&
					multiPartMap.get("part").contains("11") &&
					multiPartMap.get("part").contains("12") &&
					multiPartMap.get("part").contains("13"));
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	@Test
	public void testGetMultiPartQuestionMap09() throws Exception{
		String qList = "1;2;3;4,5,6,7;9;10,11,12,13";
		String itemID = "7";
		question sectionbreakQ1 = new sectionBreak();
		question sectionbreakQ2 = new sectionBreak();
		try{
			sectionbreakQ1.sqlID = "4";
			sectionbreakQ1.referenceTag = "1234";
			Mockito.when(cacheServiceMock.getItem("4")).thenReturn(sectionbreakQ1);
			sectionbreakQ2.sqlID = "10";
			sectionbreakQ2.referenceTag = "4321";
			Mockito.when(cacheServiceMock.getItem("10")).thenReturn(sectionbreakQ2);
			Map<String, List<String>> multiPartMap = assignmentServiceUtilMock.getMultiPartQuestionMap(qList, itemID);
			System.out.println(" multiPartMap : "+multiPartMap);
			assertTrue(multiPartMap.get("SB").contains("4") &&
					multiPartMap.get("part").contains("5") &&
					multiPartMap.get("part").contains("6") &&
					multiPartMap.get("part").contains("7"));
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	@Test
	public void testGetMultiPartQuestionMap10() throws Exception{
		String qList = "1;2;3;4,5,6,7;9;10,11,12,13";
		String itemID = "4";
		question sectionbreakQ1 = new sectionBreak();
		question sectionbreakQ2 = new sectionBreak();
		try{
			sectionbreakQ1.sqlID = "4";
			sectionbreakQ1.referenceTag = "1234";
			Mockito.when(cacheServiceMock.getItem("4")).thenReturn(sectionbreakQ1);
			sectionbreakQ2.sqlID = "10";
			sectionbreakQ2.referenceTag = "4321";
			Mockito.when(cacheServiceMock.getItem("10")).thenReturn(sectionbreakQ2);
			Map<String, List<String>> multiPartMap = assignmentServiceUtilMock.getMultiPartQuestionMap(qList, itemID);
			System.out.println(" multiPartMap : "+multiPartMap);
			assertTrue(multiPartMap.get("SB").contains("4") &&
					multiPartMap.get("part").contains("5") &&
					multiPartMap.get("part").contains("6") &&
					multiPartMap.get("part").contains("7"));
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	@Test
	public void testGetMultiPartQuestionMap11() throws Exception{
		String qList = "1;2;3;4";
		String itemID = "4";
		question sectionbreakQ1 = new sectionBreak();
		try{
			sectionbreakQ1.sqlID = "4";
			sectionbreakQ1.referenceTag = "1234";
			Mockito.when(cacheServiceMock.getItem("4")).thenReturn(sectionbreakQ1);
			Map<String, List<String>> multiPartMap = assignmentServiceUtilMock.getMultiPartQuestionMap(qList, itemID);
			System.out.println(" multiPartMap : "+multiPartMap);
			assertTrue(multiPartMap == null);
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	@Test
	public void testGetMultiPartQuestionMap12() throws Exception{
		String qList = "1;2;3;4,5,6,7";
		String itemID = null;
		try{
			Map<String, List<String>> multiPartMap = assignmentServiceUtilMock.getMultiPartQuestionMap(qList, itemID);
			assertTrue(multiPartMap == null);
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	@Test
	public void testGetPartQuestiobIds01() throws Exception{
		String qList = "1;2;3;4";
		String prevItemId = "4";
		ResponseTO responseTO = new ResponseTO();
		responseTO.getTestParameter().replaceParam(CaaConstants.QLIST, qList);
		List<String> partQList = assignmentServiceUtilMock.getPartQuestiobIds(responseTO, prevItemId);
		assertTrue(partQList == null);
	}
	
	@Test
	public void testGetPartQuestiobIds02() throws Exception{
		String qList = "1;2;3;4,5,6,7";
		String prevItemId = "4";
		sectionBreak sectionbreakQ = new sectionBreak();
		sectionbreakQ.sqlID = "4";
		sectionbreakQ.referenceTag = "1234";
		Mockito.when(cacheServiceMock.getItem("4")).thenReturn(sectionbreakQ);
		ResponseTO responseTO = new ResponseTO();
		responseTO.getTestParameter().replaceParam(CaaConstants.QLIST, qList);
		List<String> partQList = assignmentServiceUtilMock.getPartQuestiobIds(responseTO, prevItemId);
		System.out.println(partQList);
		assertTrue(partQList.contains("5") &&
				partQList.contains("6") &&
				partQList.contains("7"));
	}
	
	@Test
	public void testGetPartQuestiobIds03() throws Exception{
		String qList = "1;2;3;4,5,6,7;8;9,10,11,12";
		String prevItemId = "4";
		sectionBreak sectionbreakQ1 = new sectionBreak();
		sectionbreakQ1.sqlID = "4";
		sectionbreakQ1.referenceTag = "1234";
		Mockito.when(cacheServiceMock.getItem("4")).thenReturn(sectionbreakQ1);
		sectionBreak sectionbreakQ2 = new sectionBreak();
		sectionbreakQ2.sqlID = "9";
		sectionbreakQ2.referenceTag = "1234";
		Mockito.when(cacheServiceMock.getItem("9")).thenReturn(sectionbreakQ2);
		ResponseTO responseTO = new ResponseTO();
		responseTO.getTestParameter().replaceParam(CaaConstants.QLIST, qList);
		List<String> partQList = assignmentServiceUtilMock.getPartQuestiobIds(responseTO, prevItemId);
		System.out.println(partQList);
		assertTrue(partQList.contains("5") &&
				partQList.contains("6") &&
				partQList.contains("7"));
	}
	
	@Test
	public void testGetPartQuestiobIds04() throws Exception{
		String qList = "1;2;3;4,5,6,7;8;9,10,11,12";
		String prevItemId = "9";
		sectionBreak sectionbreakQ1 = new sectionBreak();
		sectionbreakQ1.sqlID = "4";
		sectionbreakQ1.referenceTag = "1234";
		Mockito.when(cacheServiceMock.getItem("4")).thenReturn(sectionbreakQ1);
		sectionBreak sectionbreakQ2 = new sectionBreak();
		sectionbreakQ2.sqlID = "9";
		sectionbreakQ2.referenceTag = "1234";
		Mockito.when(cacheServiceMock.getItem("9")).thenReturn(sectionbreakQ2);
		ResponseTO responseTO = new ResponseTO();
		responseTO.getTestParameter().replaceParam(CaaConstants.QLIST, qList);
		List<String> partQList = assignmentServiceUtilMock.getPartQuestiobIds(responseTO, prevItemId);
		System.out.println(partQList);
		assertTrue(partQList.contains("10") &&
				partQList.contains("11") &&
				partQList.contains("12"));
	}
	
	@Test
	public void testGetPartQuestiobIds05() throws Exception{
		String qList = null;
		String prevItemId = "9";
		ResponseTO responseTO = new ResponseTO();
		responseTO.getTestParameter().replaceParam(CaaConstants.QLIST, qList);
		List<String> partQList = assignmentServiceUtilMock.getPartQuestiobIds(responseTO, prevItemId);
		System.out.println(partQList);
		assertTrue(partQList == null);
	}
	
	@Test
	public void testGetPartQuestiobIds06() throws Exception{
		String qList = "1;2;3;4,5,6,7;8;9,10,11,12";
		String prevItemId = "";
		sectionBreak sectionbreakQ1 = new sectionBreak();
		sectionbreakQ1.sqlID = "4";
		sectionbreakQ1.referenceTag = "1234";
		Mockito.when(cacheServiceMock.getItem("4")).thenReturn(sectionbreakQ1);
		sectionBreak sectionbreakQ2 = new sectionBreak();
		sectionbreakQ2.sqlID = "9";
		sectionbreakQ2.referenceTag = "1234";
		Mockito.when(cacheServiceMock.getItem("9")).thenReturn(sectionbreakQ2);
		ResponseTO responseTO = new ResponseTO();
		responseTO.getTestParameter().replaceParam(CaaConstants.QLIST, qList);
		List<String> partQList = assignmentServiceUtilMock.getPartQuestiobIds(responseTO, prevItemId);
		System.out.println(partQList);
		assertTrue(partQList == null);
	}
	
	@Test
	public void testGetPartQuestiobIds07() throws Exception{
		String qList = "1;2;3;4,5,6,7;8;9,10,11,12";
		String prevItemId = "7";
		sectionBreak sectionbreakQ1 = new sectionBreak();
		sectionbreakQ1.sqlID = "4";
		sectionbreakQ1.referenceTag = "1234";
		Mockito.when(cacheServiceMock.getItem("4")).thenReturn(sectionbreakQ1);
		sectionBreak sectionbreakQ2 = new sectionBreak();
		sectionbreakQ2.sqlID = "9";
		sectionbreakQ2.referenceTag = "1234";
		Mockito.when(cacheServiceMock.getItem("9")).thenReturn(sectionbreakQ2);
		ResponseTO responseTO = new ResponseTO();
		responseTO.getTestParameter().replaceParam(CaaConstants.QLIST, qList);
		List<String> partQList = assignmentServiceUtilMock.getPartQuestiobIds(responseTO, prevItemId);
		System.out.println(partQList);
		assertTrue(partQList == null);
	}
	
	@Test
	public void testCalculateElapsedTimeForPartQs01() throws Exception{
		List<QuestionWiseResponseTO> partQWiseResponseTOList = new ArrayList<QuestionWiseResponseTO>();
		long sbElapsedTime = 0l;
		List<QuestionWiseResponseTO> updatePartQWiseResponseTOList = assignmentServiceUtilMock.calculateElapsedTimeForPartQs(partQWiseResponseTOList, sbElapsedTime);
		assertTrue(updatePartQWiseResponseTOList.isEmpty());
	}
	
	@Test
	public void testCalculateElapsedTimeForPartQs02() throws Exception{
		List<QuestionWiseResponseTO> partQWiseResponseTOList = new ArrayList<QuestionWiseResponseTO>();
		QuestionWiseResponseTO questionWiseResponseTO1 = new QuestionWiseResponseTO();
		questionWiseResponseTO1.getQuestionParameters().replaceParam(CaaConstants.VISITED, "true");
		partQWiseResponseTOList.add(questionWiseResponseTO1);
		QuestionWiseResponseTO questionWiseResponseTO2 = new QuestionWiseResponseTO();
		partQWiseResponseTOList.add(questionWiseResponseTO2);
		long sbElapsedTime = 0l;
		List<QuestionWiseResponseTO> updatePartQWiseResponseTOList = assignmentServiceUtilMock.calculateElapsedTimeForPartQs(partQWiseResponseTOList, sbElapsedTime);
		assertTrue(updatePartQWiseResponseTOList.get(0).getElapsed() == 0);
	}
	
	@Test
	public void testCalculateElapsedTimeForPartQs03() throws Exception{
		List<QuestionWiseResponseTO> partQWiseResponseTOList = new ArrayList<QuestionWiseResponseTO>();
		QuestionWiseResponseTO questionWiseResponseTO1 = new QuestionWiseResponseTO();
		questionWiseResponseTO1.getQuestionParameters().replaceParam(CaaConstants.VISITED, "true");
		partQWiseResponseTOList.add(questionWiseResponseTO1);
		QuestionWiseResponseTO questionWiseResponseTO2 = new QuestionWiseResponseTO();
		partQWiseResponseTOList.add(questionWiseResponseTO2);
		long sbElapsedTime = 10l;
		List<QuestionWiseResponseTO> updatePartQWiseResponseTOList = assignmentServiceUtilMock.calculateElapsedTimeForPartQs(partQWiseResponseTOList, sbElapsedTime);
		assertTrue(updatePartQWiseResponseTOList.get(0).getElapsed() == 10);
	}
	
	@Test
	public void testCalculateElapsedTimeForPartQs04() throws Exception{
		List<QuestionWiseResponseTO> partQWiseResponseTOList = new ArrayList<QuestionWiseResponseTO>();
		QuestionWiseResponseTO questionWiseResponseTO1 = new QuestionWiseResponseTO();
		questionWiseResponseTO1.getQuestionParameters().replaceParam(CaaConstants.VISITED, "true");
		partQWiseResponseTOList.add(questionWiseResponseTO1);
		QuestionWiseResponseTO questionWiseResponseTO2 = new QuestionWiseResponseTO();
		questionWiseResponseTO2.getQuestionParameters().replaceParam(CaaConstants.VISITED, "true");
		partQWiseResponseTOList.add(questionWiseResponseTO2);
		long sbElapsedTime = 10l;
		List<QuestionWiseResponseTO> updatePartQWiseResponseTOList = assignmentServiceUtilMock.calculateElapsedTimeForPartQs(partQWiseResponseTOList, sbElapsedTime);
		assertTrue(updatePartQWiseResponseTOList.get(0).getElapsed() == 5
				&& updatePartQWiseResponseTOList.get(1).getElapsed() == 5);
	}
	
	@Test
	public void testCalculateElapsedTimeForPartQs05() throws Exception{
		List<QuestionWiseResponseTO> partQWiseResponseTOList = new ArrayList<QuestionWiseResponseTO>();
		QuestionWiseResponseTO questionWiseResponseTO1 = new QuestionWiseResponseTO();
		questionWiseResponseTO1.getQuestionParameters().replaceParam(CaaConstants.VISITED, "true");
		partQWiseResponseTOList.add(questionWiseResponseTO1);
		QuestionWiseResponseTO questionWiseResponseTO2 = new QuestionWiseResponseTO();
		questionWiseResponseTO2.getQuestionParameters().replaceParam(CaaConstants.VISITED, "true");
		partQWiseResponseTOList.add(questionWiseResponseTO2);
		QuestionWiseResponseTO questionWiseResponseTO3 = new QuestionWiseResponseTO();
		partQWiseResponseTOList.add(questionWiseResponseTO3);
		long sbElapsedTime = 10l;
		List<QuestionWiseResponseTO> updatePartQWiseResponseTOList = assignmentServiceUtilMock.calculateElapsedTimeForPartQs(partQWiseResponseTOList, sbElapsedTime);
		assertTrue(updatePartQWiseResponseTOList.get(0).getElapsed() == 5
				&& updatePartQWiseResponseTOList.get(1).getElapsed() == 5);
	}
	
	@Test
	public void testCalculateElapsedTimeForPartQs06() throws Exception{
		List<QuestionWiseResponseTO> partQWiseResponseTOList = new ArrayList<QuestionWiseResponseTO>();
		QuestionWiseResponseTO questionWiseResponseTO1 = new QuestionWiseResponseTO();
		questionWiseResponseTO1.getQuestionParameters().replaceParam(CaaConstants.VISITED, "true");
		partQWiseResponseTOList.add(questionWiseResponseTO1);
		QuestionWiseResponseTO questionWiseResponseTO2 = new QuestionWiseResponseTO();
		questionWiseResponseTO2.getQuestionParameters().replaceParam(CaaConstants.VISITED, "true");
		partQWiseResponseTOList.add(questionWiseResponseTO2);
		QuestionWiseResponseTO questionWiseResponseTO3 = new QuestionWiseResponseTO();
		questionWiseResponseTO3.getQuestionParameters().replaceParam(CaaConstants.VISITED, "true");
		partQWiseResponseTOList.add(questionWiseResponseTO3);
		long sbElapsedTime = 10l;
		List<QuestionWiseResponseTO> updatePartQWiseResponseTOList = assignmentServiceUtilMock.calculateElapsedTimeForPartQs(partQWiseResponseTOList, sbElapsedTime);
		System.out.println("1 : "+updatePartQWiseResponseTOList.get(0).getElapsed()+", 2 : "+updatePartQWiseResponseTOList.get(1).getElapsed()
				+", 3 : "+updatePartQWiseResponseTOList.get(2).getElapsed());
		assertTrue(updatePartQWiseResponseTOList.get(0).getElapsed() == 3
				&& updatePartQWiseResponseTOList.get(1).getElapsed() == 3
				&& updatePartQWiseResponseTOList.get(2).getElapsed() == 3);
	}
}
