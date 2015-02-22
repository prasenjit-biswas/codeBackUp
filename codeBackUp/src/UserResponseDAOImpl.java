package com.mcgrawhill.ezto.api.caa.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import com.mcgrawhill.ezto.api.caa.caaconstants.CaaConstants;
import com.mcgrawhill.ezto.api.caa.dao.CommonDAO;
import com.mcgrawhill.ezto.api.caa.dao.UserResponseDAO;
import com.mcgrawhill.ezto.api.caa.dao.datatransferobject.AttemptDTO;
import com.mcgrawhill.ezto.api.caa.dao.datatransferobject.AttemptDataDTO;
import com.mcgrawhill.ezto.api.caa.dao.util.AttemptDataMapper;
import com.mcgrawhill.ezto.api.caa.dao.util.AttemptMapper;
import com.mcgrawhill.ezto.api.caa.services.transferobject.ResponseTO;
import com.mcgrawhill.ezto.integration.classware_hm;
/**
 * This <code>UserResponseDAOImpl</code> class holds all the data access call
 * definition for <code>User Response</code> API. This class 
 * <code>UserResponseDAOImpl</code> has methods to get client secret from DB etc. 
 * 
 * @author TCS
 *
 */
@Repository
public class UserResponseDAOImpl implements UserResponseDAO {
	
	/** Reference of the <code>JdbcTemplate</code> class */
	@Autowired
	NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	
	/** Reference of the <code>CommonDAO</code> class */
	@Autowired
	CommonDAO commonDAO;
	private static final Logger _logger = Logger.getLogger(UserResponseDAOImpl.class);
	/** List of all SQL that will be used in <code>UserResponse</code> API */
	String ATTEMPT_QUERY = "SELECT attempt_pk,testid,activityid,attemptno,userid,sectionid,totalscore,params,submissionid,submissiontime,maxscore,totalcorrect,pctscore,elapsedtime,groupscores,datasize,createdtime,updatedtime,aat FROM attempt WHERE attempt_pk = :attempt_pk";
	String GET_ATTEMPT_DATA_QUERY = "SELECT attemptdata_pk,questionid,instructorcomment,formulaanswer,feedback,followup,html,itemscore,maxpoints,params,recordedvalue,mangraded,userresponse,elapsed FROM attemptdata WHERE attempt_pk = :attempt_pk";
	String GET_ATTEMPT_DATA_PARAM_FOR_QUESTION_QUERY = "SELECT attemptdata_pk , questionid , params FROM attemptdata WHERE attempt_pk = :attempt_pk ";
	String GET_ATTEMPT_DATA_PK_QID_QUERY = "SELECT attemptdata_pk, questionid FROM attemptdata WHERE attempt_pk = :attempt_pk";

	String INSERT_ATTEMPT_QUERY = "INSERT INTO attempt (attempt_pk, testid, activityid, attemptno, userid, sectionid, totalscore, " +
			  "params, maxscore, totalcorrect, pctscore, elapsedtime, groupscores, datasize, createdtime) " +
			  "VALUES( :attempt_pk, :testid, :activityid, :attemptno, :userid, :sectionid, :totalscore, " +
			  ":params, :maxscore, :totalcorrect, :pctscore, :elapsedtime, :groupscores, :datasize, :createdtime)";

	String INSERT_ATTEMPT_DATA_QUERY = "INSERT INTO attemptdata (attemptdata_pk, attempt_pk, testid, sectionid, activityid, attemptno, userid, " +
					  "questionid, itemscore, params, maxpoints, formulaanswer, recordedvalue, followup, feedback, html, " +
					  "instructorcomment, createdtime, userresponse) " +
					  "VALUES( :attemptdata_pk, :attempt_pk, :testid, :sectionid, :activityid, :attemptno, :userid, " +
					  ":questionid, :itemscore, :params, :maxpoints, :formulaanswer, :recordedvalue, :followup, :feedback, :html, " +
					  ":instructorcomment, :createdtime, :userresponse)"; 

	String UPDATE_ATTEMPT_QUERY = "UPDATE attempt SET params = :params, totalscore = :totalscore, totalcorrect = :totalcorrect, pctscore = :pctscore, " +
				 "groupscores = :groupscores, datasize = :datasize, updatedtime = :updatedtime WHERE attempt_pk = :attempt_pk";
	
	String UPDATE_ATTEMPT_PARAM_QUERY = "UPDATE attempt SET params = :params, datasize = :datasize, updatedtime = :updatedtime WHERE attempt_pk = :attempt_pk";
	
	String UPDATE_ATTEMPT_QUERY_FOR_SUBMISSION = "UPDATE attempt SET params = :params, submissionid = :submissionid, submissiontime = :submissiontime, " +
			"totalscore = :totalscore, maxscore = :maxScore, totalcorrect = :totalcorrect, pctscore = :pctscore, " +
			"groupscores = :groupscores, datasize = :datasize, updatedtime = :updatedtime WHERE attempt_pk = :attempt_pk  and submissionid is null";

	String UPDATE_ATTEMPT_DATA_QUERY = "UPDATE attemptdata SET itemscore = :itemscore , maxpoints = :maxpoints, formulaanswer = :formulaanswer, " +
					  "recordedvalue = :recordedvalue, followup = :followup, feedback = :feedback, html = :html, " +
					  "instructorcomment = :instructorcomment, params = :params, updatedtime = :updatedtime, userresponse = :userresponse " +
					  "WHERE attemptdata_pk = :attemptdata_pk";

	String UPDATE_ATTEMPT_DATA_QUERY_WITHOUT_ELAPSED = "UPDATE attemptdata SET itemscore = :itemscore, maxpoints = :maxpoints, formulaanswer = :formulaanswer," +
									  "recordedvalue = :recordedvalue, followup = :followup, feedback = :feedback, html = :html, " +
									  "instructorcomment = :instructorcomment, params = :params, updatedtime = :updatedtime, " +
									  "userresponse = :userresponse WHERE attemptdata_pk = :attemptdata_pk";
	String UPDATE_ATTEMPT_DATA_QUERY_FOR_SUBMISSION = "UPDATE attemptdata SET itemscore = :itemscore, maxpoints = :maxpoints, formulaanswer = :formulaanswer," +
	  "recordedvalue = :recordedvalue, followup = :followup, feedback = :feedback, html = :html, " +
	  "params = :params, updatedtime = :updatedtime WHERE attemptdata_pk = :attemptdata_pk";
			
	String QUESTION_EXISTS = "SELECT params FROM attemptdata WHERE testid = :testid AND questionid = :questionid AND sectionid = :sectionid AND activityid = :activityid " +
							 "AND userid = :userid AND attemptno = :attemptno";
	
	private final String ATTEMPT_EXISTS_FOR_TEST = "SELECT 1 FROM attempt WHERE testid = :testid AND userid != :userid";
	private final String GET_ATTEMPT_PK_FOR_TEST = "SELECT attempt_pk FROM attempt WHERE testid = :testid AND activityid = :activityid AND attemptno = :attemptno AND userid = :userid AND sectionid = :sectionid";
	
	private final String REMOVE_ATTEMPT = "DELETE FROM attempt WHERE attempt_pk = :attempt_pk";
	private final String REMOVE_ATTEMPTDATA = "DELETE FROM attemptdata WHERE attempt_pk = :attempt_pk";
	private final String ATTEMPT_EXISTS_FOR_QUESTION = "SELECT 1 FROM attempt WHERE testid IN (SELECT testid FROM test_question_xref WHERE questionid = :questionid) AND userid != :userid";
	private final String GET_ATTEMPT_FOR_SUBMISSON_ID = "SELECT attempt_pk,testid,activityid,attemptno,userid,sectionid,totalscore,params,submissionid,submissiontime,maxscore,totalcorrect,pctscore,elapsedtime,groupscores,datasize,createdtime,updatedtime,aat FROM attempt WHERE testid = :testid AND submissionid = :submissionid";
	
	private final String INSERT_CLASSWAREFAILURE_QUERY = "INSERT INTO classwarefailures (uniqueID, creationTime, transactionType, url, data) VALUES (:uniqueID, :creationTime, :transactionType, :url, :data)";
	private final String GET_ATTEMPTPK_AND_TESTID = "SELECT attempt_pk,testid FROM attempt WHERE submissionid = :submissionid";
	
	private final String UPDATE_ATTEMPT_DATA_ELAPSED_QUERY = "UPDATE attemptdata SET elapsed = :elapsed, updatedtime = :updatedtime WHERE attempt_pk = :attempt_pk AND attemptdata_pk =:attemptdata_pk";
	
	private final String GET_SUBMITTED_ATTEMPT_QUERY_FOR_REPORT = "SELECT attempt_pk, params  FROM attempt " +
													"WHERE testid = :testid  AND submissionid = :submissionid";
	
	
	private final String ATTEMPT_QUERY_INSTRUCTOR_PREVIEW = "SELECT params,attempt_pk,submissionid,testid,sectionid,activityid,attemptno,"+
		 				"userid,totalscore,maxscore,totalcorrect,pctscore,groupscores,createdtime,updatedtime"+
		 				" FROM attempt WHERE testid = :testId AND userid = :userId AND activityid = :activityId AND submissionid IS NULL";
	
	private final String UPDATE_ATTEMPT_DATA_PARAM = "UPDATE attemptdata SET params = :params, updatedtime = :updatedtime WHERE attempt_pk = :attempt_pk AND attemptdata_pk =:attemptdata_pk";
	private final String SELECT_ATTEMPT_FOR_UPDATE = "SELECT submissionid FROM attempt WHERE attempt_pk = :attempt_pk FOR UPDATE";
	
	private final String UPDATE_ATTEMPT_PARAM_ELAPSED_QUERY = "UPDATE attempt SET params = :params, datasize = :datasize, updatedtime = :updatedtime, elapsedtime =:elapsedtime WHERE attempt_pk = :attempt_pk";
	
	/** List of all SQL that will be used in <code>UserResponse</code> API */
	
	/** This method is designed Insert record in Attempt table.
	 * (non-Javadoc)
	 * @see com.mcgrawhill.ezto.api.caa.dao.UserResponseDAO#insertAttempt(com.mcgrawhill.ezto.api.caa.dao.datatransferobject.AttemptDTO)
	 */
	@Override
	public void insertAttempt(AttemptDTO attemptDTO) throws DataAccessException {
		Map<String, Object> param = new HashMap<String, Object>();
		attemptDTO.setAttemptPK(commonDAO.getUniqueID());
		param.put("attempt_pk", attemptDTO.getAttemptPK());
		param.put("testid", attemptDTO.getTestID());
		param.put("activityid", attemptDTO.getActivityID());
		param.put("attemptno", attemptDTO.getAttemptNo());
		param.put("userid", attemptDTO.getUserID());
		param.put("sectionid", attemptDTO.getSectionID());
		param.put("totalscore", attemptDTO.getTotalScore());
		param.put("params", attemptDTO.getTestParameter());
		param.put("maxscore", attemptDTO.getMaxScore());
		param.put("totalcorrect", attemptDTO.getTotalCorrect());
		param.put("pctscore", attemptDTO.getPercentageScore());
		param.put("elapsedtime", System.currentTimeMillis());
		param.put("groupscores", attemptDTO.getGroupEvaluations());
		param.put("datasize", attemptDTO.getTestParameter().length);
		param.put("createdtime", System.currentTimeMillis());
		namedParameterJdbcTemplate.update(INSERT_ATTEMPT_QUERY, param);
	}

	
	/** This method is designed to Insert record in AttemptData in batch mode.
	 * (non-Javadoc)
	 * @see com.mcgrawhill.ezto.api.caa.dao.UserResponseDAO#insertAttemptData(com.mcgrawhill.ezto.api.caa.dao.datatransferobject.AttemptDTO, java.util.List)
	 */
	@Override
	public void insertAttemptData(AttemptDTO attemptDTO, List<AttemptDataDTO> attemptDataDTOList) throws DataAccessException {
		@SuppressWarnings("unchecked")
		Map<String, Object>[] paramArr = new HashMap[attemptDataDTOList.size()];
		int i=0;
		for(AttemptDataDTO attemptDataDTO : attemptDataDTOList){
			paramArr[i++]= setAttempDataParamsForInsert(attemptDTO, attemptDataDTO);
		}
		namedParameterJdbcTemplate.batchUpdate(INSERT_ATTEMPT_DATA_QUERY, paramArr);
	}

	/** This method is designed Update Attempt Table.
	 * (non-Javadoc)
	 * @see com.mcgrawhill.ezto.api.caa.dao.UserResponseDAO#updateAttempt(com.mcgrawhill.ezto.api.caa.dao.datatransferobject.AttemptDTO)
	 */
	@Override
	public void updateAttempt(AttemptDTO attemptDTO) throws DataAccessException {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("params", attemptDTO.getTestParameter());
		param.put("totalscore", attemptDTO.getTotalScore());
		param.put("totalcorrect", attemptDTO.getTotalCorrect());
		param.put("pctscore", attemptDTO.getPercentageScore());
		param.put("groupscores", attemptDTO.getGroupEvaluations());
		param.put("datasize", attemptDTO.getTestParameter().length);
		param.put("updatedtime", System.currentTimeMillis());
		param.put("attempt_pk", attemptDTO.getAttemptPK());
		namedParameterJdbcTemplate.update(UPDATE_ATTEMPT_QUERY, param);
	}

	
	/** This method is designed Update AttemptData Table in batchMode.
	 * (non-Javadoc)
	 * @see com.mcgrawhill.ezto.api.caa.dao.UserResponseDAO#updateAttemptData(java.util.List)
	 */
	@Override
	public void updateAttemptData(List<AttemptDataDTO> attemptDataDTOList) throws DataAccessException {
		@SuppressWarnings("unchecked")
		Map<String, Object>[] paramArr = new HashMap[attemptDataDTOList.size()];
		int i=0;
		for(AttemptDataDTO attemptDataDTO : attemptDataDTOList){
			paramArr[i++]= setAttempDataParamsForUpdate(attemptDataDTO);
		}
		namedParameterJdbcTemplate.batchUpdate(UPDATE_ATTEMPT_DATA_QUERY, paramArr);
	}
	
	/** This method is designed to update AttemptData Table in batchMode.
	 * (non-Javadoc)
	 * @see com.mcgrawhill.ezto.api.caa.dao.UserResponseDAO#updateAttemptDataDuringSubmission(java.util.List)
	 */
	@Override
	public void updateAttemptDataDuringSubmission(List<AttemptDataDTO> attemptDataDTOList) throws DataAccessException {
		@SuppressWarnings("unchecked")
		Map<String, Object>[] paramArr = new HashMap[attemptDataDTOList.size()];
		int i=0;
		for(AttemptDataDTO attemptDataDTO : attemptDataDTOList){
			paramArr[i++]= setAttempDataParamsForUpdate(attemptDataDTO);
		}
		namedParameterJdbcTemplate.batchUpdate(UPDATE_ATTEMPT_DATA_QUERY_FOR_SUBMISSION, paramArr);
	}

	
	/** This Method is designed return Map Object depending on the AttemptDataDTO for Update use case.
	 * @param attemptDataDTO
	 * @return
	 * @throws DataAccessException
	 */
	private Map<String, Object> setAttempDataParamsForUpdate(AttemptDataDTO attemptDataDTO) throws DataAccessException {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("itemscore", attemptDataDTO.getItemScore());
		param.put("maxpoints", attemptDataDTO.getPointsMax());
		param.put("formulaanswer", attemptDataDTO.getFormulaAnswer());
		param.put("recordedvalue", attemptDataDTO.getRecordedValue());
		param.put("followup", attemptDataDTO.getFollowupValue());
		param.put("feedback", attemptDataDTO.getFeedback());
		param.put("html", attemptDataDTO.getHtml());
		param.put("instructorcomment", attemptDataDTO.getInstructorComment());
		param.put("params", attemptDataDTO.getQuestionParameters());
		param.put("updatedtime", System.currentTimeMillis());
		param.put("userresponse", attemptDataDTO.getUserResponse());
		//param.put("elapsed", attemptDataDTO.getElapsed());
		param.put("attemptdata_pk", attemptDataDTO.getAttemptDataPK());
		return param;
	}
	
	
	/** This Method is designed return Map Object depending on the AttemptDataDTO for Insert use case.
	 * @param attemptDTO
	 * @param attemptDataDTO
	 * @return
	 * @throws DataAccessException
	 */
	private Map<String, Object> setAttempDataParamsForInsert(AttemptDTO attemptDTO, AttemptDataDTO attemptDataDTO) throws DataAccessException{
		Map<String, Object> param = new HashMap<String, Object>();
		attemptDataDTO.setAttemptDataPK(commonDAO.getUniqueID());
		param.put("attemptdata_pk", attemptDataDTO.getAttemptDataPK());
		param.put("attempt_pk", attemptDTO.getAttemptPK());
		param.put("testid", attemptDTO.getTestID());
		param.put("sectionid", attemptDTO.getSectionID());
		param.put("activityid", attemptDTO.getActivityID());
		param.put("attemptno", attemptDTO.getAttemptNo());
		param.put("userid", attemptDTO.getUserID());
		param.put("questionid", attemptDataDTO.getQuestionID());
		param.put("itemscore", attemptDataDTO.getItemScore());
		param.put("params", attemptDataDTO.getQuestionParameters());
		param.put("maxpoints", attemptDataDTO.getPointsMax());
		param.put("formulaanswer", attemptDataDTO.getFormulaAnswer());
		param.put("recordedvalue", attemptDataDTO.getRecordedValue());
		param.put("followup", attemptDataDTO.getFollowupValue());
		param.put("feedback", attemptDataDTO.getFeedback());
		param.put("html", attemptDataDTO.getHtml());
		param.put("instructorcomment", attemptDataDTO.getInstructorComment());
		param.put("createdtime", attemptDTO.getSubmissionTime() == 0 ? System.currentTimeMillis() : attemptDTO.getSubmissionTime());
		param.put("userresponse", attemptDataDTO.getUserResponse());
		return param;
	}
	
	
	/** This is method is designed to Update AttemptData for without Elapsed Time use case.
	 *  (non-Javadoc)
	 * @see com.mcgrawhill.ezto.api.caa.dao.UserResponseDAO#updateAttemptDataWithoutElapsedTime(com.mcgrawhill.ezto.api.caa.dao.datatransferobject.AttemptDataDTO)
	 */
	@Override
	public void updateAttemptDataWithoutElapsedTime(AttemptDataDTO attemptDataDTO) throws DataAccessException {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("itemscore", attemptDataDTO.getItemScore());
		param.put("maxpoints", attemptDataDTO.getPointsMax());
		param.put("formulaanswer", attemptDataDTO.getFormulaAnswer());
		param.put("recordedvalue", attemptDataDTO.getRecordedValue());
		param.put("followup", attemptDataDTO.getFollowupValue());
		param.put("feedback", attemptDataDTO.getFeedback());
		param.put("html", attemptDataDTO.getHtml());
		param.put("instructorcomment", attemptDataDTO.getInstructorComment());
		param.put("params", attemptDataDTO.getQuestionParameters());
		param.put("updatedtime", System.currentTimeMillis());
		param.put("userresponse", attemptDataDTO.getUserResponse());
		param.put("attemptdata_pk", attemptDataDTO.getAttemptDataPK());
		namedParameterJdbcTemplate.update(UPDATE_ATTEMPT_DATA_QUERY_WITHOUT_ELAPSED, param);
	}
	
	/**
	 * @see UserResponseDAO#getAttempt(long)
	 */
	@Override
	public AttemptDTO getAttempt(long attemptPk) throws DataAccessException {
		List<AttemptDTO> attemptDTOList = null;
		AttemptDTO attemptDTO = null;
		SqlParameterSource namedParameters = new MapSqlParameterSource("attempt_pk", attemptPk);
		attemptDTOList = namedParameterJdbcTemplate.query(ATTEMPT_QUERY, namedParameters, new AttemptMapper());
		if(attemptDTOList != null && attemptDTOList.size() > 0 ){
			attemptDTO = attemptDTOList.get(0);
		}
		return attemptDTO;
	}
	
	/**
	 * @see UserResponseDAO#getAttemptData(long, List)
	 */
	@Override
	public List<AttemptDataDTO> getAttemptData(long attemptPk, List<String> questionIdList) throws DataAccessException {
		Map<String, Object> namedParameters = new HashMap<String, Object>();
		namedParameters.put("attempt_pk", attemptPk);
		
		StringBuilder queryBuilder = new StringBuilder(GET_ATTEMPT_DATA_QUERY);
		if(questionIdList != null &&  questionIdList.size()>0){			
			queryBuilder.append(" AND (");
			int ctr = 0;
			for(String questionId : questionIdList){				
				if(ctr > 0){
					queryBuilder.append(" OR ");
				}
				queryBuilder.append("questionid = :questionid").append(ctr);
				namedParameters.put(("questionid" + ctr), questionId);
				
				ctr++;
			}
			queryBuilder.append(")");
		}
		return this.namedParameterJdbcTemplate.query(queryBuilder.toString(), namedParameters, new AttemptDataMapper());
	}
	
	/**
	 * @see UserResponseDAO#getAttemptData(long, List)
	 */
	@Override
	public Map<String,String> getAttemptDataPkQidMap(long attemptPk) throws DataAccessException {
		Map<String,String> attemptDataPKAndQidMap = null;
		Map<String,Object> namedParameters = new HashMap<String, Object>();
		namedParameters.put("attempt_pk", attemptPk);
		attemptDataPKAndQidMap = namedParameterJdbcTemplate.query(GET_ATTEMPT_DATA_PK_QID_QUERY, namedParameters, new ResultSetExtractor<Map<String,String>>() {
			@Override
			public Map<String,String> extractData(ResultSet rs) throws SQLException {
				Map<String,String> attempDatapkQidMap = new HashMap<String, String>();
				while(rs.next()){
					attempDatapkQidMap.put("attemptdata_pk", rs.getString("ATTEMPTDATA_PK"));
					attempDatapkQidMap.put("questionid", rs.getString("QUESTIONID"));
				}
				return attempDatapkQidMap;
			}
		});
		return attemptDataPKAndQidMap;
	}
	
	/**
	 * @see UserResponseDAO#attemptExist(String)
	 */
	@Override
	public boolean attemptExistForTest(String testID) throws DataAccessException {
		boolean isAttemptExist = false;
		Map<String, String> queryparam = new HashMap<String, String>();
		queryparam.put("testid",testID);
		queryparam.put("userid",INSTRUCTOR_PREVIEW_ID);
		isAttemptExist = namedParameterJdbcTemplate.query(ATTEMPT_EXISTS_FOR_TEST, queryparam,new ResultSetExtractor<Boolean>() {
			@Override
			public Boolean extractData(ResultSet rs) throws SQLException {
				if (rs.next()) {
					return true;
				}
				return false;
			}
		});
		return isAttemptExist;
	}

	/*
	 * (non-Javadoc)
	 * @see com.mcgrawhill.ezto.api.caa.dao.UserResponseDAO#getAttemptPK(com.mcgrawhill.ezto.api.caa.services.transferobject.ResponseTO)
	 */
	@Override
	public long getAttemptPK(ResponseTO responseTO) throws DataAccessException {
		long attemptPK;
		Map<String, String> queryparam = new HashMap<String, String>();
		queryparam.put("testid",responseTO.getTestID());
		queryparam.put("activityid",responseTO.getActivityID());
		queryparam.put("attemptno",responseTO.getAttemptNo());
		queryparam.put("userid",responseTO.getUserID());
		queryparam.put("sectionid",responseTO.getSectionID());
		attemptPK = namedParameterJdbcTemplate.query(GET_ATTEMPT_PK_FOR_TEST, queryparam,new ResultSetExtractor<Long>() {
			@Override
			public Long extractData(ResultSet rs) throws SQLException {
				if (rs.next()) {
					return rs.getLong("attempt_pk"); 
				}else{
					return 0l;
				}
			}
		});
		return attemptPK;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.mcgrawhill.ezto.api.caa.dao.UserResponseDAO#getInProgressAttempt(com.mcgrawhill.ezto.api.caa.services.transferobject.ResponseTO)
	 */
	@Override
	public AttemptDTO getInProgressAttempt(ResponseTO responseTO) throws DataAccessException {
		Map<String, String> queryparam = new HashMap<String, String>();
		String GET_ATTEMPT_QUERY_FOR_PREVIEW = "SELECT attempt_pk, params  FROM attempt " +
												"WHERE testid = :testid  AND userid = :userid " +
												"AND activityid = :activityid AND submissionid IS NULL";
		StringBuilder queryBuilder = new StringBuilder(GET_ATTEMPT_QUERY_FOR_PREVIEW);
		AttemptDTO attemptDTO = null;
		
		if(!StringUtils.isBlank(responseTO.getTestID()) && !StringUtils.isBlank(responseTO.getActivityID()) && !StringUtils.isBlank(responseTO.getUserID()) ){
			queryparam.put("testid",responseTO.getTestID());
			queryparam.put("activityid", responseTO.getActivityID());
			queryparam.put("userid",responseTO.getUserID());
			if(	!StringUtils.isBlank(responseTO.getSectionID()) 
				&& !StringUtils.isBlank(responseTO.getAttemptNo())){
				//this is for student attempt
				queryBuilder.append(" AND sectionid = :sectionid AND attemptno = :attemptno");
				queryparam.put("sectionid", responseTO.getSectionID());
				queryparam.put("attemptno", responseTO.getAttemptNo());				
			}
			_logger.info("## queryparam="+queryparam);
			_logger.info("## query="+queryBuilder.toString());
			attemptDTO = namedParameterJdbcTemplate.query(queryBuilder.toString(), queryparam, new ResultSetExtractor<AttemptDTO>(){
				@Override
				public AttemptDTO extractData(ResultSet rs) throws SQLException {
					AttemptDTO attemptDTO = null;
					if (rs.next()) {	
						attemptDTO = new AttemptDTO();
						attemptDTO.setAttemptPK(rs.getLong("ATTEMPT_PK"));
						attemptDTO.setTestParameter(rs.getBytes("params"));
					}
					return attemptDTO;
				}
			});
		}
		return attemptDTO;
	}
	/*
	 * (non-Javadoc)
	 * @see com.mcgrawhill.ezto.api.caa.dao.UserResponseDAO#dropAttempt(com.mcgrawhill.ezto.api.caa.services.transferobject.ResponseTO)
	 */
	@Override
	public void dropAttempt(ResponseTO responseTO) throws DataAccessException {
		Map<String, Object> queryparam = new HashMap<String, Object>();
		queryparam.put("attempt_pk",responseTO.getAttemptPK());
		namedParameterJdbcTemplate.update(REMOVE_ATTEMPT, queryparam);
	}

	/*
	 * (non-Javadoc)
	 * @see com.mcgrawhill.ezto.api.caa.dao.UserResponseDAO#dropAttemptData(com.mcgrawhill.ezto.api.caa.services.transferobject.ResponseTO)
	 */
	@Override
	public void dropAttemptData(ResponseTO responseTO) throws DataAccessException {
		Map<String, Object> queryparam = new HashMap<String, Object>();
		queryparam.put("attempt_pk",responseTO.getAttemptPK());
		namedParameterJdbcTemplate.update(REMOVE_ATTEMPTDATA, queryparam);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.mcgrawhill.ezto.api.caa.dao.UserResponseDAO#updateAttemptForSubmission(AttemptDTO)
	 */
	
	@Override
	public void updateAttemptForSubmission(AttemptDTO attemptDTO)
			throws DataAccessException {
		
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("params", attemptDTO.getTestParameter());
		param.put("submissionid", attemptDTO.getSubmissionID());
		param.put("submissiontime", attemptDTO.getSubmissionTime());
		param.put("params", attemptDTO.getTestParameter());
		param.put("maxScore", attemptDTO.getMaxScore());		
		param.put("totalscore", attemptDTO.getTotalScore()); 
		param.put("totalcorrect", attemptDTO.getTotalCorrect());
		param.put("pctscore", attemptDTO.getPercentageScore());
		param.put("groupscores", attemptDTO.getGroupEvaluations());
		param.put("datasize", attemptDTO.getTestParameter().length);
		param.put("updatedtime", System.currentTimeMillis());
		param.put("attempt_pk", attemptDTO.getAttemptPK());
		namedParameterJdbcTemplate.update(UPDATE_ATTEMPT_QUERY_FOR_SUBMISSION, param);
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.mcgrawhill.ezto.api.caa.dao.UserResponseDAO#attemptExistForQuestion(java.lang.String)
	 */
	@Override
	public boolean attemptExistForQuestion(String itemiD) throws DataAccessException {
		boolean isAttemptExist = false;
		if(!StringUtils.isBlank(itemiD)){
			Map<String, String> queryparam = new HashMap<String, String>();
			queryparam.put("questionid",itemiD);
			queryparam.put("userid",INSTRUCTOR_PREVIEW_ID);
			isAttemptExist = namedParameterJdbcTemplate.query(ATTEMPT_EXISTS_FOR_QUESTION, queryparam,new ResultSetExtractor<Boolean>() {
				@Override
				public Boolean extractData(ResultSet rs) throws SQLException {
					if (rs.next()) {
						return true;
					}
					return false;
				}
			});
		}
		return isAttemptExist;
	}

	@Override
	public void updateAttemptParam(byte[] testParam, long attemptpk) throws DataAccessException {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("params", testParam);
		param.put("datasize", testParam.length);
		param.put("updatedtime", System.currentTimeMillis());
		param.put("attempt_pk", attemptpk);
		namedParameterJdbcTemplate.update(UPDATE_ATTEMPT_PARAM_QUERY, param);
	}
	
	@Override
	public void updateAttemptElapsedTimeAndParam(AttemptDTO attemptDTO) throws DataAccessException {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("params", attemptDTO.getTestParameter());
		param.put("datasize", attemptDTO.getTestParameter().length);
		param.put("updatedtime", System.currentTimeMillis());
		param.put("elapsedtime", attemptDTO.getElapsedTime());
		param.put("attempt_pk", attemptDTO.getAttemptPK());
		namedParameterJdbcTemplate.update(UPDATE_ATTEMPT_PARAM_ELAPSED_QUERY, param);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.mcgrawhill.ezto.api.caa.dao.UserResponseDAO#getAttempt(java.lang.String, java.lang.String)
	 */
	@Override
	public AttemptDTO getAttempt(String testId , String submissionID) throws DataAccessException {
		Map<String, String> queryparam = new HashMap<String, String>();
		List<AttemptDTO> attemptDTOs = null;
		AttemptDTO attemptDTO = null;
		if(!StringUtils.isBlank(testId) && !StringUtils.isBlank(submissionID)){
			queryparam.put("testid",testId);
			queryparam.put("submissionid",submissionID);
			attemptDTOs = namedParameterJdbcTemplate.query(GET_ATTEMPT_FOR_SUBMISSON_ID, queryparam, new AttemptMapper());
			if(attemptDTOs != null && attemptDTOs.size() > 0 ){
				attemptDTO = attemptDTOs.get(0);
			}
		}
		return attemptDTO;
	}

	/*
	 * (non-Javadoc)
	 * @see com.mcgrawhill.ezto.api.caa.dao.UserResponseDAO#getAttemptDataList(java.lang.String)
	 */
	@Override
	public List<AttemptDataDTO> getAttemptDataList(long attemptPK) throws DataAccessException {
		List<AttemptDataDTO> attemptDataDTOList = null;
		Map<String, Object> queryparam = new HashMap<String, Object>();
		queryparam.put("attempt_pk",attemptPK);
		attemptDataDTOList = namedParameterJdbcTemplate.query(GET_ATTEMPT_DATA_QUERY, queryparam, new AttemptDataMapper());
		return attemptDataDTOList;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.mcgrawhill.ezto.api.caa.dao.UserResponseDAO#getAttempt(com.mcgrawhill.ezto.api.caa.services.transferobject.ResponseTO)
	 */
	@Override
	public AttemptDTO getAttempt(ResponseTO responseTO) throws DataAccessException {
		Map<String, String> queryparam = new HashMap<String, String>();
		String CHECK_ALREADY_ATTEMPTED = "SELECT attempt_pk,testid,activityid,attemptno,userid" +
										",sectionid,totalscore,params,submissionid,submissiontime" +
										",maxscore,totalcorrect,pctscore,elapsedtime,groupscores" +
										",datasize,createdtime,updatedtime,aat FROM attempt " +
										"WHERE testid = :testid AND sectionid = :sectionid " +
										"AND activityid = :activityid AND userid = :userid " +
										"AND attemptno = :attemptno";
		AttemptDTO attemptDTO = null;
		queryparam.put("testid",responseTO.getTestID());
		queryparam.put("sectionid", responseTO.getSectionID());
		queryparam.put("activityid", responseTO.getActivityID());
		queryparam.put("userid",responseTO.getUserID());
		queryparam.put("attemptno", responseTO.getAttemptNo());
		
		_logger.info("## queryparam="+queryparam);
		
		List<AttemptDTO> attemptDTOList = namedParameterJdbcTemplate.query(CHECK_ALREADY_ATTEMPTED, queryparam, new AttemptMapper());
		if(attemptDTOList != null && attemptDTOList.size() > 0 ){
			attemptDTO = attemptDTOList.get(0);
		}
		return attemptDTO;
	}

	/*
	 * (non-Javadoc)
	 * @see com.mcgrawhill.ezto.api.caa.dao.UserResponseDAO#cleanUpAttemptDataRecord(com.mcgrawhill.ezto.api.caa.services.transferobject.ResponseTO)
	 */
	@Override
	public void cleanUpAttemptDataRecord(ResponseTO responseTO)throws DataAccessException {
		Map<String, String> queryparam = new HashMap<String, String>();
		queryparam.put("testid",responseTO.getTestID());
		queryparam.put("sectionid",responseTO.getSectionID());
		queryparam.put("activityid",responseTO.getActivityID());
		queryparam.put("userid",classware_hm.ROLE_INSTRUCTOR_ID);
		
		String CHECK_ATTEMPT_DATA_EXISTS = "SELECT attempt_pk FROM attemptdata WHERE testid = :testid AND sectionid = :sectionid AND activityid = :activityid AND userid = :userid";
		Long attemptPk = namedParameterJdbcTemplate.query(CHECK_ATTEMPT_DATA_EXISTS, queryparam,new ResultSetExtractor<Long>() {
			@Override
			public Long extractData(ResultSet rs) throws SQLException {
				Long attemptPk = null;
				if (rs.next()) {
					attemptPk = rs.getLong("attempt_pk");
				}
				return attemptPk;
			}
		});
		
		if(attemptPk != null && attemptPk.longValue() > 0){
			ResponseTO newResponseTO = new ResponseTO();
			newResponseTO.setAttemptPK(attemptPk);
			dropAttemptData(newResponseTO);
		}
	}
	
	/**
	 * This method persists the failure cases with all required data for different transactions 
	 * in database.
	 * @param theURL
	 * @param theXML
	 * @param transactionType
	 */
	public void recordFaliures(String theURL, byte[] xml, String transactionType) throws DataAccessException{
		//:uniqueID, :creationTime, :transactionType, :url, :data
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("uniqueID", commonDAO.getUniqueID());
		param.put("creationTime", System.currentTimeMillis());
		param.put("transactionType", transactionType);
		param.put("url", theURL);
		param.put("data", xml);		
		
		namedParameterJdbcTemplate.update(INSERT_CLASSWAREFAILURE_QUERY, param);
	}

	/*
	 * (non-Javadoc)
	 * @see com.mcgrawhill.ezto.api.caa.dao.UserResponseDAO#getAttempt(java.lang.String)
	 */
	@Override
	public Map<String,String> getAttemptPKTidMap(long submissionID)throws DataAccessException {
		Map<String,String> attemptMap = null;
		Map<String, Long> queryparam = new HashMap<String, Long>();
		queryparam.put("submissionid", submissionID);
		attemptMap = namedParameterJdbcTemplate.query(GET_ATTEMPTPK_AND_TESTID, queryparam, new ResultSetExtractor<Map<String,String>>() {
			@Override
			public Map<String, String> extractData(ResultSet rs)throws SQLException, DataAccessException {
				Map<String,String> attemptMap = new HashMap<String,String>();
				if(rs.next()){
					attemptMap.put(CaaConstants.ATTEMPT_PK, rs.getString("attempt_pk"));
					attemptMap.put(CaaConstants.TESTID, rs.getString("testid"));
				}
				return attemptMap;
			}
		});
		return attemptMap;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.mcgrawhill.ezto.api.caa.dao.UserResponseDAO#updateTimeOnTask(long attempt_pk, long attemptdata_pk, long timeOnTaskInMiliSecond)
	 */
	@Override
	public void updateTimeOnTask(long attempt_pk, long attemptdata_pk, long timeOnTaskInMiliSecond ) throws DataAccessException {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("elapsed", timeOnTaskInMiliSecond);
		param.put("updatedtime", System.currentTimeMillis());
		param.put("attempt_pk", attempt_pk);
		param.put("attemptdata_pk", attemptdata_pk);
		namedParameterJdbcTemplate.update(UPDATE_ATTEMPT_DATA_ELAPSED_QUERY, param);
	}
	
	@Override
	public void updateTimeOnTask(List<AttemptDataDTO> attemptDataDTOList, long attempt_pk) throws DataAccessException {
		Map<String, Object>[] paramArr = new HashMap[attemptDataDTOList.size()];
		int i=0;
		for(AttemptDataDTO attemptDataDTO : attemptDataDTOList){
			paramArr[i++]= setAttempDataParamsForElapsedTmeUpdate(attemptDataDTO, attempt_pk);
		}
		namedParameterJdbcTemplate.batchUpdate(UPDATE_ATTEMPT_DATA_ELAPSED_QUERY, paramArr);
	}
	
	/** This Method is designed return Map Object depending on the AttemptDataDTO for Update use case.
	 * @param attemptDataDTO
	 * @return
	 * @throws DataAccessException
	 */
	private Map<String, Object> setAttempDataParamsForElapsedTmeUpdate(AttemptDataDTO attemptDataDTO, long attempt_pk) throws DataAccessException {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("elapsed", attemptDataDTO.getElapsed());
		param.put("updatedtime", System.currentTimeMillis());
		param.put("attempt_pk", attempt_pk);
		param.put("attemptdata_pk", attemptDataDTO.getAttemptDataPK());
		return param;
	}
	
	/* (non-Javadoc)
	 * @see com.mcgrawhill.ezto.api.caa.dao.UserResponseDAO#getSubmittedAttempt(com.mcgrawhill.ezto.api.caa.services.transferobject.ResponseTO)
	 */
	@Override
	public AttemptDTO getSubmittedAttempt(ResponseTO responseTO) throws DataAccessException {
		Map<String, String> queryparam = new HashMap<String, String>();
		AttemptDTO attemptDTO = null;
		
		if(responseTO != null && !StringUtils.isBlank(responseTO.getTestID()) && !StringUtils.isBlank(""+responseTO.getSubmissionID())){
			queryparam.put("testid",responseTO.getTestID());
			queryparam.put("submissionid", ""+responseTO.getSubmissionID());
			attemptDTO = namedParameterJdbcTemplate.query(GET_SUBMITTED_ATTEMPT_QUERY_FOR_REPORT, queryparam, new ResultSetExtractor<AttemptDTO>(){
				@Override
				public AttemptDTO extractData(ResultSet rs) throws SQLException {
					AttemptDTO attemptDTO = null;
					if (rs.next()) {	
						attemptDTO = new AttemptDTO();
						attemptDTO.setAttemptPK(rs.getLong("ATTEMPT_PK"));
						attemptDTO.setTestParameter(rs.getBytes("params"));
					}
					return attemptDTO;
				}
			});
		}
		return attemptDTO;
	}
	
	@Override
	public AttemptDTO getInstructorSubmission(String testid, String useId, String actID) throws DataAccessException{
		Map<String, String> queryparam = new HashMap<String, String>();
		AttemptDTO attemptDTO = null;
		
		if(StringUtils.isNotBlank(testid)){			
			queryparam.put("testId",testid);
			queryparam.put("userId", useId);
			queryparam.put("activityId", actID);
			
			attemptDTO = namedParameterJdbcTemplate.query(ATTEMPT_QUERY_INSTRUCTOR_PREVIEW, queryparam, new ResultSetExtractor<AttemptDTO>(){
				@Override
				public AttemptDTO extractData(ResultSet rs) throws SQLException {
					AttemptDTO attemptDTO = null;
					if (rs.next()) {	
						attemptDTO = new AttemptDTO();
						attemptDTO.setAttemptPK(rs.getLong("ATTEMPT_PK"));
						attemptDTO.setTestParameter(rs.getBytes("params"));
						attemptDTO.setSubmissionID(rs.getLong("SUBMISSIONID"));
						attemptDTO.setTestID(rs.getString("TESTID"));
						attemptDTO.setSectionID(rs.getString("SECTIONID"));
						attemptDTO.setActivityID(rs.getString("ACTIVITYID"));
						attemptDTO.setAttemptNo(rs.getString("ATTEMPTNO"));
						attemptDTO.setUserID(rs.getString("USERID"));
						attemptDTO.setTotalScore(rs.getInt("TOTALSCORE"));
						attemptDTO.setMaxScore(rs.getInt("MAXSCORE"));
						attemptDTO.setTotalCorrect(rs.getInt("TOTALCORRECT"));
						attemptDTO.setPercentageScore(rs.getInt("PCTSCORE"));
						attemptDTO.setGroupEvaluations(rs.getBytes("GROUPSCORES"));
						attemptDTO.setCreatedTime(rs.getLong("CREATEDTIME"));
						attemptDTO.setUpdatedTime(rs.getLong("UPDATEDTIME"));
					}
					return attemptDTO;
				}
			});
		}
		return attemptDTO;
	}


	/* (non-Javadoc)
	 * @see com.mcgrawhill.ezto.api.caa.dao.UserResponseDAO#updateAttemptdataQuestionParam(byte[], long, long)
	 */
	@Override
	public void updateAttemptdataQuestionParam(byte[] questionParam, long attemptpk, long attemptdatapk) throws DataAccessException {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("params", questionParam);
		param.put("updatedtime", System.currentTimeMillis());
		param.put("attempt_pk", attemptpk);
		param.put("attemptdata_pk", attemptdatapk);
		namedParameterJdbcTemplate.update(UPDATE_ATTEMPT_DATA_PARAM, param);
	}
	
	/* (non-Javadoc)
	 * @see com.mcgrawhill.ezto.api.caa.dao.UserResponseDAO#ifSubmissionExists(AttemptDTO)
	 */
	@Override
	public boolean ifSubmissionExists(AttemptDTO attemptDTO) throws DataAccessException{
		_logger.info("In ifSubmissionExists");
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("attempt_pk", attemptDTO.getAttemptPK());
		Long submissionid = namedParameterJdbcTemplate.queryForObject(SELECT_ATTEMPT_FOR_UPDATE, param, Long.class);
		_logger.info("After query submissionid:" + submissionid);
		if(submissionid != null && submissionid != 0){
			return true;
		}
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.mcgrawhill.ezto.api.caa.dao.UserResponseDAO#getAttemptDataParamForQuestion(long, java.util.List)
	 */
	@Override
	public List<AttemptDataDTO> getAttemptDataParamForQuestion(long attemptPk, List<String> questionIdList) throws DataAccessException {
		Map<String, Object> namedParameters = new HashMap<String, Object>();
		List<AttemptDataDTO> attemptDataDTOList = null;
		namedParameters.put("attempt_pk", attemptPk);
		
		StringBuilder queryBuilder = new StringBuilder(GET_ATTEMPT_DATA_PARAM_FOR_QUESTION_QUERY);
		if(questionIdList != null &&  questionIdList.size()>0){			
			queryBuilder.append(" AND (");
			int ctr = 0;
			for(String questionId : questionIdList){				
				if(ctr > 0){
					queryBuilder.append(" OR ");
				}
				queryBuilder.append("questionid = :questionid").append(ctr);
				namedParameters.put(("questionid" + ctr), questionId);
				
				ctr++;
			}
			queryBuilder.append(")");
		}
		
		attemptDataDTOList = namedParameterJdbcTemplate.query(queryBuilder.toString(), namedParameters, new ResultSetExtractor<List<AttemptDataDTO>>(){
			@Override
			public List<AttemptDataDTO> extractData(ResultSet rs) throws SQLException {
				List<AttemptDataDTO> attemptDataDTOList = new ArrayList<AttemptDataDTO>();
				AttemptDataDTO attemptDataDTO = null;
				while (rs.next()) {	
					attemptDataDTO = new AttemptDataDTO();
					attemptDataDTO.setAttemptDataPK(rs.getLong("attemptdata_pk"));
					attemptDataDTO.setQuestionParameters(rs.getBytes("params"));
					attemptDataDTO.setQuestionID(rs.getString("questionid"));
					
					attemptDataDTOList.add(attemptDataDTO);
				}
				return attemptDataDTOList;
			}
		});
		
		return attemptDataDTOList;
	}
}