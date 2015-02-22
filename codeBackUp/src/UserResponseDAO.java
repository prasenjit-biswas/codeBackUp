package com.mcgrawhill.ezto.api.caa.dao;

import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;

import com.mcgrawhill.ezto.api.caa.dao.datatransferobject.AttemptDTO;
import com.mcgrawhill.ezto.api.caa.dao.datatransferobject.AttemptDataDTO;
import com.mcgrawhill.ezto.api.caa.services.transferobject.ResponseTO;
/**
 * This <code>UserResponseDAO</code> interface holds all the data access call
 * definition for <code>UserResponse</code> API. This interface 
 * <code>UserResponseDAO</code> has methods to get client secret from DB etc. 
 * 
 * @author TCS
 *
 */

public interface UserResponseDAO {
   
	public final String INSTRUCTOR_PREVIEW_ID = "instructorPreviewID";
	/**This method is designed Insert record in Attempt table.
	 * @param attemptDTO
	 * @throws DataAccessException
	 */
	public void insertAttempt(AttemptDTO attemptDTO) throws DataAccessException;
	
	/**This method is designed to Insert record in AttemptData in batch.
	 * @param attemptDTO A custom object which represents attempt table record
	 * @param attemptDataDTO A collection of custom objects which represents attempdata table records
	 * @throws DataAccessException
	 */
	public void insertAttemptData(AttemptDTO attemptDTO, List<AttemptDataDTO> attemptDataDTO) throws DataAccessException;
	
	/**This method is designed Update Attempt Table.
	 * @param attemptDTO A custom object which represents attempt table record
	 * @throws DataAccessException
	 */
	public void updateAttempt(AttemptDTO attemptDTO) throws DataAccessException;
	
	/**
	 * This method is designed to update Attempt param blob for a specific attemptPK
	 * @param testParam A <code>byte[]</code> representing the attempt test blob
	 * @param attemptpk A <code>long</code> primitive representing an unique attempt record
	 * @throws DataAccessException
	 */
	public void updateAttemptParam(byte[] testParam, long attemptpk) throws DataAccessException;
	
	/**
	 * This method is designed to update Attempt param blob and elapsed time for a specific attemptPK
	 * @param attemptDTO A Custom data transfer object, representing user response
	 * @throws DataAccessException
	 */
	public void updateAttemptElapsedTimeAndParam(AttemptDTO attemptDTO) throws DataAccessException;
	
	/**
	 * This method is designed to update Attempt Table. This will be called only at submission time and score and submission 
	 * related columns are updated
	 * 
	 * @param attemptDTO A custom object which represents attempt table record
	 * @throws DataAccessException
	 */
	public void updateAttemptForSubmission(AttemptDTO attemptDTO) throws DataAccessException;
	
	/**This method is designed Update AttemptData Table in batch.
	 * @param attemptDataDTOList A collection of custom objects which represents attempdata table records
	 * @throws DataAccessException
	 */
	public void updateAttemptData(List<AttemptDataDTO> attemptDataDTOList) throws DataAccessException;
	
	/**This is method is designed to Update AttemptData for without Elapsed Time use case.
	 * @param attemptDataDTO A custom object which represents attempdata table record
	 * @throws DataAccessException
	 */
	public void updateAttemptDataWithoutElapsedTime(AttemptDataDTO attemptDataDTO) throws DataAccessException;
	
	/**
	 * This method retrieves Attempt table information
     * for a particular attempt key
	 * 
	 * @param attemptPk		primary key of the attempt table
	 * @return	AttemptDTO
	 * @throws DataAccessException
	 */
	public AttemptDTO getAttempt(long attemptPk) throws DataAccessException;	
	
	/**
	 * This method retrieves AttemptData table ainformation
     * for a particular attempt key & question id
	 * 
	 * @param attemptPk			primary key of the attempt table
	 * @param questionIdList	List of question SQL id
	 * @return List<AttemptDataDTO>		
	 * @throws DataAccessException
	 */
	public List<AttemptDataDTO> getAttemptData(long attemptPk, List<String> questionIdList) throws DataAccessException;
	/**
	 * This method checks whether a test has any attempt or not , except instructor student view  attempt
	 * @param testID A string representing a testID
	 * @return
	 * @throws DataAccessException
	 */
	public boolean attemptExistForTest(String testID) throws DataAccessException;
	/**
	 * This method gives the mapping of attempt data and its corresponding 
	 * question id , from a particular attempt identified by its attemptpk  
	 * @param attemptPk
	 * @param questionIdList
	 * @return
	 * @throws DataAccessException
	 */
	public Map<String,String> getAttemptDataPkQidMap(long attemptPk) throws DataAccessException ;
	/**
	 * This method returns an attemptPK for a an attempt if it exists in attempt table
	 * @param responseTO
	 * @return
	 * @throws DataAccessException
	 */
	public long getAttemptPK(ResponseTO responseTO) throws DataAccessException;
	
	/**
	 * This method gets testid, activityid, attemptno, userid, 
	 * sectionid within a ResponseTO and returns corresponding in progress attempt object
	 * @param responseTO
	 * @return AttemptDTO
	 * @throws DataAccessException
	 */
	public AttemptDTO getInProgressAttempt(ResponseTO responseTO) throws DataAccessException;
	
	/**
	 * This method retrieve the test parameters and attempt pk for a submitted attempt.
	 * 
	 * @param responseTO
	 * @return AttemptDTO
	 * @throws DataAccessException
	 */
	public AttemptDTO getSubmittedAttempt(ResponseTO responseTO) throws DataAccessException;
	
	/**
	 * This method retrieve the test parameters and attempt pk for a submitted attempt.
	 * 
	 * @param testid
	 * @param useId
	 * @param actID
	 * @return AttemptDTO
	 * @throws DataAccessException
	 */
	public AttemptDTO getInstructorSubmission(String testid,String useId,String actID) throws DataAccessException;
	
	/**
	 * This method deletes all the records from attempt table for a specific attemptPK
	 * @param responseTO
	 * @throws DataAccessException
	 */
	public void dropAttempt(ResponseTO responseTO) throws DataAccessException;
	/**
	 * This method deletes all the question level records from attempt data table for a 
	 * specific attemptPK
	 * @param responseTO
	 * @throws DataAccessException
	 */
	public void dropAttemptData(ResponseTO responseTO) throws DataAccessException;
	/**
	 * This method checks that a question is ever attempted or not.If the question is attempted then 
	 * it returns true but if it could not find any attempt for this question then it returns false
	 * @param testID
	 * @return
	 * @throws DataAccessException
	 */
	public boolean attemptExistForQuestion(String itemID) throws DataAccessException;
	
	/**This method is designed Update AttemptData Table in batch. This method is called to update required columns
	 * at submission time. It does not update columns like user response or elapsed time 
	 * @param attemptDataDTOList A collection of custom objects which represents attempdata table records
	 * @throws DataAccessException
	 */
	public void updateAttemptDataDuringSubmission(List<AttemptDataDTO> attemptDataDTOList) throws DataAccessException;
	
	/**
	 * This method returns a total attempt level information for a unique testID and submissionID
	 * combination
	 * @param testId
	 * @param submissionID
	 * @return
	 * @throws DataAccessException
	 */
	public AttemptDTO getAttempt(String testId , String submissionID) throws DataAccessException ;
	
	/**
	 * This method gives a list of attemptData DTO means all the question level information for
	 * a particular attempt
	 * @param attemptPK
	 * @return
	 * @throws DataAccessException
	 */
	public List<AttemptDataDTO> getAttemptDataList(long attemptPK) throws DataAccessException;
	
	/**
	 * This method retrieved user response [Attempt]
	 * for a specific attempt. This happens when connect fails to 
	 * process the submission of an existing user response
	 *  
	 * @param ResponseTO containing testid, userid, activityid, attemptno & sectionid
	 * @return AttemptDTO
	 * @throws DataAccessException
	 */
	public AttemptDTO getAttempt(ResponseTO responseTO) throws DataAccessException;
	
	/**
	 * This method checks if attempt data record exists
	 * for a specific attempt for instructor role.
	 *  
	 * @param ResponseTO containing testid, userid, activityid, attemptno & sectionid
	 * @throws DataAccessException
	 */
	public void cleanUpAttemptDataRecord(ResponseTO responseTO) throws DataAccessException;
	
	/**
	 * This method persists the failure cases with all required data for different transactions 
	 * in database.
	 * @param theURL
	 * @param theXML
	 * @param transactionType
	 */
	public void recordFaliures(String theURL, byte[] xml, String transactionType) throws DataAccessException;
	
	/**
	 * This method returns a attemptDTO containing attemptPK and testID for a
	 * particular submission id  
	 * @param submissionID
	 * @return
	 * @throws DataAccessException
	 */
	public Map<String,String> getAttemptPKTidMap(long submissionID) throws DataAccessException;
	
	/**
	 * This method updates the elapsed column of attemptdata table for a given attempt_pk and attemptdata_pk.
	 * @param attempt_pk
	 * @param attemptdata_pk
	 * @param timeOnTaskInMiliSecond
	 * @throws DataAccessException
	 */
	public void updateTimeOnTask(long attempt_pk, long attemptdata_pk, long timeOnTaskInMiliSecond ) throws DataAccessException;
	
	/**
	 * This method updates the elapsed column of attempt data table for a given attempt_pk and attemptdata_pk.
	 * @param attemptDataDTOList A custom Object list, representing Question wise student response
	 * @param attempt_pk A long value, representing an attempt uniquely
	 * @throws DataAccessException
	 */
	public void updateTimeOnTask(List<AttemptDataDTO> attemptDataDTOList, long attempt_pk ) throws DataAccessException;
	
	/** This method is designed to update questionParam in attempdata table
	 * @param questionParam A <code>byte[]</code> object containing Question specific user response
	 * @param attemptpk A long Object representing a row in attempt table uniquely 
	 * @param attemptdatapk A long Object representing a row in attemptdata table uniquely 
	 * @throws DataAccessException
	 */
	public void updateAttemptdataQuestionParam(byte[] questionParam, long attemptpk, long attemptdatapk) throws DataAccessException;

	/**
	 * Checks if Submission already present for this attempt(Preventing race condition)
	 * @param attemptDTO
	 * @return
	 * @throws DataAccessException
	 */
	boolean ifSubmissionExists(AttemptDTO attemptDTO) throws DataAccessException;
	
	/**
	 * This method return Attempt Data params for a list of questions
	 * 
	 * @param attemptPk
	 * @param questionIdList
	 * @return List of AttemptDataDTO
	 * @throws DataAccessException
	 */
	public List<AttemptDataDTO> getAttemptDataParamForQuestion(long attemptPk, List<String> questionIdList) throws DataAccessException;
}
