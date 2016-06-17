package teammates.ui.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import teammates.common.datatransfer.CommentAttributes;
import teammates.common.datatransfer.CommentParticipantType;
import teammates.common.datatransfer.FeedbackSessionAttributes;
import teammates.common.datatransfer.InstructorAttributes;
import teammates.common.datatransfer.StudentAttributes;
import teammates.common.datatransfer.StudentProfileAttributes;
import teammates.common.exception.EntityDoesNotExistException;
import teammates.common.util.Assumption;
import teammates.common.util.Const;
import teammates.common.util.Const.StatusMessageColor;
import teammates.common.util.StatusMessage;
import teammates.logic.api.GateKeeper;

public class InstructorStudentRecordsPageAction extends Action {

    @Override
    public ActionResult execute() throws EntityDoesNotExistException {

        String courseId = getRequestParamValue(Const.ParamsNames.COURSE_ID);
        Assumption.assertNotNull(courseId);

        InstructorAttributes instructor = logic.getInstructorForGoogleId(courseId, account.googleId);
        new GateKeeper().verifyAccessible(instructor, logic.getCourse(courseId));
        
        String studentEmail = getRequestParamValue(Const.ParamsNames.STUDENT_EMAIL);
        Assumption.assertNotNull(studentEmail);

        StudentAttributes student = logic.getStudentForEmail(courseId, studentEmail);

        if (student == null) {
            statusToUser.add(new StatusMessage(Const.StatusMessages.STUDENT_NOT_FOUND_FOR_RECORDS,
                                               StatusMessageColor.DANGER));
            isError = true;
            return createRedirectResult(Const.ActionURIs.INSTRUCTOR_HOME_PAGE);
        }
        
        String showCommentBox = getRequestParamValue(Const.ParamsNames.SHOW_COMMENT_BOX);

        List<CommentAttributes> comments = logic.getCommentsForReceiver(courseId, instructor.email,
                                                                        CommentParticipantType.PERSON, studentEmail);
        Iterator<CommentAttributes> iterator = comments.iterator();
        while (iterator.hasNext()) {
            CommentAttributes c = iterator.next();
            if (!c.giverEmail.equals(instructor.email)) {
                // not covered as this won't happen unless there's error in logic layer
                iterator.remove();
            }
        }

        List<FeedbackSessionAttributes> sessions = logic.getFeedbackSessionsListForInstructor(account.googleId);

        filterFeedbackSessions(courseId, sessions, instructor, student);

        Collections.sort(sessions, FeedbackSessionAttributes.DESCENDING_ORDER);
        CommentAttributes.sortCommentsByCreationTimeDescending(comments);
        
        StudentProfileAttributes studentProfile = null;

        boolean isInstructorAllowedToViewStudent = instructor.isAllowedForPrivilege(student.section,
                                                        Const.ParamsNames.INSTRUCTOR_PERMISSION_VIEW_STUDENT_IN_SECTIONS);
        boolean isStudentWithProfile = !student.googleId.isEmpty();
        if (isInstructorAllowedToViewStudent && isStudentWithProfile) {
            studentProfile = logic.getStudentProfile(student.googleId);
            Assumption.assertNotNull(studentProfile);
        } else {
            if (student.googleId.isEmpty()) {
                statusToUser.add(new StatusMessage(Const.StatusMessages.STUDENT_NOT_JOINED_YET_FOR_RECORDS,
                                                   StatusMessageColor.WARNING));
            } else if (!isInstructorAllowedToViewStudent) {
                statusToUser.add(new StatusMessage(Const.StatusMessages.STUDENT_PROFILE_UNACCESSIBLE_TO_INSTRUCTOR,
                                                   StatusMessageColor.WARNING));
            }
        }

        if (sessions.isEmpty() && comments.isEmpty()) {
            statusToUser.add(new StatusMessage(Const.StatusMessages.INSTRUCTOR_NO_STUDENT_RECORDS,
                                               StatusMessageColor.WARNING));
        }

        List<String> sessionNames = new ArrayList<String>();
        for (FeedbackSessionAttributes fsa : sessions) {
            sessionNames.add(fsa.getFeedbackSessionName());
        }
        
        InstructorStudentRecordsPageData data =
                                        new InstructorStudentRecordsPageData(account, student, courseId,
                                                                             showCommentBox, studentProfile,
                                                                             comments, sessionNames, instructor);

        statusToAdmin = "instructorStudentRecords Page Load<br>"
                      + "Viewing <span class=\"bold\">" + studentEmail + "'s</span> records "
                      + "for Course <span class=\"bold\">[" + courseId + "]</span><br>"
                      + "Number of sessions: " + sessions.size() + "<br>"
                      + "Student Profile: " + (studentProfile == null ? "No Profile"
                                                                      : studentProfile.toString());

        return createShowPageResult(Const.ViewURIs.INSTRUCTOR_STUDENT_RECORDS, data);
    }

    private void filterFeedbackSessions(String courseId, List<FeedbackSessionAttributes> feedbacks,
                                        InstructorAttributes instructor, StudentAttributes student) {
        Iterator<FeedbackSessionAttributes> iterFs = feedbacks.iterator();
        while (iterFs.hasNext()) {
            FeedbackSessionAttributes tempFs = iterFs.next();
            if (!tempFs.getCourseId().equals(courseId)
                    || !instructor.isAllowedForPrivilege(student.section, tempFs.getSessionName(),
                                                         Const.ParamsNames.INSTRUCTOR_PERMISSION_VIEW_SESSION_IN_SECTIONS)) {
                iterFs.remove();
            }
        }
    }

}
