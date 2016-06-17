package teammates.ui.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import teammates.common.datatransfer.CourseAttributes;
import teammates.common.datatransfer.InstructorAttributes;
import teammates.common.exception.EntityDoesNotExistException;
import teammates.common.util.Assumption;
import teammates.common.util.Const;
import teammates.common.util.Const.StatusMessageColor;
import teammates.common.util.EmailWrapper;
import teammates.common.util.StatusMessage;
import teammates.logic.api.GateKeeper;

/**
 * Action: remind instructor or student to register for a course by sending reminder emails
 */
public class InstructorCourseRemindAction extends Action {
    
    @Override
    public ActionResult execute() throws EntityDoesNotExistException {
        
        String courseId = getRequestParamValue(Const.ParamsNames.COURSE_ID);
        Assumption.assertNotNull(courseId);
        
        String studentEmail = getRequestParamValue(Const.ParamsNames.STUDENT_EMAIL);
        String instructorEmail = getRequestParamValue(Const.ParamsNames.INSTRUCTOR_EMAIL);
        
        InstructorAttributes instructor = logic.getInstructorForGoogleId(courseId, account.googleId);
        CourseAttributes course = logic.getCourse(courseId);
        boolean isSendingToStudent = studentEmail != null;
        boolean isSendingToInstructor = instructorEmail != null;
        if (isSendingToStudent) {
            new GateKeeper().verifyAccessible(
                    instructor, course, Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_STUDENT);
        } else if (isSendingToInstructor) {
            new GateKeeper().verifyAccessible(
                    instructor, course, Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_INSTRUCTOR);
        } else {
            // this is sending registration emails to all students in the course and we will check if the instructor
            // canmodifystudent for course level since for modifystudent privilege there is only course level setting for now
            new GateKeeper().verifyAccessible(
                    instructor, course, Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_STUDENT);
        }
        
        /* Process sending emails and setup status to be shown to user and admin */
        List<EmailWrapper> emailsSent = new ArrayList<EmailWrapper>();
        String redirectUrl = "";
        if (isSendingToStudent) {
            EmailWrapper emailSent = logic.sendRegistrationInviteToStudent(courseId, studentEmail);
            emailsSent.add(emailSent);
            
            statusToUser.add(new StatusMessage(Const.StatusMessages.COURSE_REMINDER_SENT_TO + studentEmail,
                                               StatusMessageColor.SUCCESS));
            redirectUrl = Const.ActionURIs.INSTRUCTOR_COURSE_DETAILS_PAGE;
        } else if (isSendingToInstructor) {
            EmailWrapper emailSent = logic.sendRegistrationInviteToInstructor(courseId, instructorEmail);
            emailsSent.add(emailSent);
            
            statusToUser.add(new StatusMessage(Const.StatusMessages.COURSE_REMINDER_SENT_TO + instructorEmail,
                                               StatusMessageColor.SUCCESS));
            redirectUrl = Const.ActionURIs.INSTRUCTOR_COURSE_EDIT_PAGE;
        } else {
            emailsSent = logic.sendRegistrationInviteForCourse(courseId);
            
            statusToUser.add(new StatusMessage(Const.StatusMessages.COURSE_REMINDERS_SENT, StatusMessageColor.SUCCESS));
            redirectUrl = Const.ActionURIs.INSTRUCTOR_COURSE_DETAILS_PAGE;
        }
        
        statusToAdmin = generateStatusToAdmin(emailsSent, courseId);
        
        /* Create redirection with URL based on type of sending email */
        RedirectResult response = createRedirectResult(redirectUrl);
        response.addResponseParam(Const.ParamsNames.COURSE_ID, courseId);
        
        return response;

    }
    
    private String generateStatusToAdmin(List<EmailWrapper> emailsSent, String courseId) {
        StringBuilder statusToAdmin = new StringBuilder(200);
        statusToAdmin.append("Registration Key sent to the following users in Course <span class=\"bold\">[")
                     .append(courseId)
                     .append("]</span>:<br>");
        
        Iterator<Entry<String, JoinEmailData>> extractedEmailIterator =
                extractEmailDataForLogging(emailsSent).entrySet().iterator();
        
        while (extractedEmailIterator.hasNext()) {
            Entry<String, JoinEmailData> extractedEmail = extractedEmailIterator.next();
            
            String userEmail = extractedEmail.getKey();
            JoinEmailData joinEmailData = extractedEmail.getValue();
            
            statusToAdmin.append(joinEmailData.userName).append("<span class=\"bold\"> (").append(userEmail)
                         .append(")</span>.<br>").append(joinEmailData.regKey).append("<br>");
        }
        
        return statusToAdmin.toString();
    }

    private Map<String, JoinEmailData> extractEmailDataForLogging(List<EmailWrapper> emails) {
        Map<String, JoinEmailData> logData = new TreeMap<String, JoinEmailData>();
        
        for (EmailWrapper email : emails) {
            String recipient = email.getFirstRecipient();
            String userName = extractUserName(email.getContent());
            String regKey = extractRegistrationKey(email.getContent());
            logData.put(recipient, new JoinEmailData(userName, regKey));
        }
        
        return logData;
    }
    
    private String extractUserName(String emailContent) {
        int startIndex = emailContent.indexOf("Hello ") + "Hello ".length();
        int endIndex = emailContent.indexOf(',');
        return emailContent.substring(startIndex, endIndex);
    }
    
    private String extractRegistrationKey(String emailContent) {
        int startIndex = emailContent.indexOf("key=") + "key=".length();
        int endIndex = emailContent.indexOf("\">http://");
        if (endIndex < 0) {
            endIndex = emailContent.indexOf("\">https://");
        }
        return emailContent.substring(startIndex, endIndex);
    }
    
    private class JoinEmailData {
        String userName;
        String regKey;
        
        JoinEmailData(String userName, String regKey) {
            this.userName = userName;
            this.regKey = regKey;
        }
    }
}
