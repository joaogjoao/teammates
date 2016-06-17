package teammates.logic.automated;

import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import teammates.common.datatransfer.CommentSendingState;
import teammates.common.exception.EntityDoesNotExistException;
import teammates.common.util.Assumption;
import teammates.common.util.Const;
import teammates.common.util.Const.ParamsNames;
import teammates.common.util.EmailWrapper;
import teammates.common.util.HttpRequestHelper;
import teammates.logic.core.CommentsLogic;
import teammates.logic.core.EmailGenerator;
import teammates.logic.core.FeedbackResponseCommentsLogic;

/**
 * The mail action that is to execute, when pending comments are cleared
 */
public class PendingCommentClearedMailAction extends EmailAction {
    private String courseId;
    private CommentsLogic commentsLogic = CommentsLogic.inst();
    private FeedbackResponseCommentsLogic frcLogic = FeedbackResponseCommentsLogic.inst();
    
    public PendingCommentClearedMailAction(HttpServletRequest req) {
        super(req);
        initializeNameAndDescription();
        
        courseId = HttpRequestHelper
                .getValueFromRequestParameterMap(req, ParamsNames.EMAIL_COURSE);
        Assumption.assertNotNull(courseId);
    }

    public PendingCommentClearedMailAction(HashMap<String, String> paramMap) {
        super();
        initializeNameAndDescription();
        
        courseId = paramMap.get(ParamsNames.EMAIL_COURSE);
        Assumption.assertNotNull(courseId);
    }

    @Override
    protected void doPostProcessingForSuccesfulSend() throws EntityDoesNotExistException {
        frcLogic.updateFeedbackResponseCommentsSendingState(courseId, CommentSendingState.SENDING, CommentSendingState.SENT);
        commentsLogic.updateCommentsSendingState(courseId, CommentSendingState.SENDING, CommentSendingState.SENT);
    }

    @Override
    protected void doPostProcessingForUnsuccesfulSend() throws EntityDoesNotExistException {
        //recover the pending state when it fails
        frcLogic.updateFeedbackResponseCommentsSendingState(courseId, CommentSendingState.SENDING,
                                                            CommentSendingState.PENDING);
        commentsLogic.updateCommentsSendingState(courseId, CommentSendingState.SENDING, CommentSendingState.PENDING);
    }

    @Override
    protected List<EmailWrapper> prepareMailToBeSent() {
        log.info("Fetching recipient emails for pending comments in course : " + courseId);
        return new EmailGenerator().generatePendingCommentsClearedEmails(courseId);
    }

    private void initializeNameAndDescription() {
        actionName = Const.AutomatedActionNames.AUTOMATED_PENDING_COMMENT_CLEARED_MAIL_ACTION;
        actionDescription = "clear pending comments";
    }
}
