package teammates.test.pageobjects;

import static org.testng.AssertJUnit.assertEquals;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import teammates.common.util.Const;

public class InstructorCourseEditPage extends AppPage {
    
    @FindBy(id = "courseid")
    private WebElement courseIdTextBox;
    
    @FindBy(id = "coursename")
    private WebElement courseNameTextBox;
    
    @FindBy(id = "btnSaveCourse")
    private WebElement saveCourseButton;
    
    @FindBy(id = "courseEditLink")
    private WebElement editCourseLink;
    
    @FindBy(id = "courseDeleteLink")
    private WebElement deleteCourseLink;
    
    @FindBy(id = "btnShowNewInstructorForm")
    private WebElement showNewInstructorFormButton;
    
    @FindBy(id = "instructorname")
    private WebElement newInstructorNameTextBox;
    
    @FindBy(id = "instructoremail")
    private WebElement newInstructorEmailTextBox;
    
    @FindBy(id = "btnAddInstructor")
    private WebElement addInstructorButton;
    
    public InstructorCourseEditPage(Browser browser) {
        super(browser);
    }

    @Override
    protected boolean containsExpectedPageContents() {
        return getPageSource().contains("<h1>Edit Course Details</h1>");
    }

    public String getCourseId() {
        return courseIdTextBox.getAttribute("value");
    }

    public InstructorCourseEditPage verifyIsCorrectPage(String courseId) {
        assertEquals(courseId, this.getCourseId());
        return this;
    }
    
    public void addNewInstructor(String name, String email) {
        clickShowNewInstructorFormButton();
        
        fillNewInstructorName(name);
        fillNewInstructorEmail(email);
        
        addInstructorButton.click();
        waitForPageToLoad();
    }

    public void editInstructor(int instrNum, String name, String email, String role) {
        clickEditInstructorLink(instrNum);
        
        editInstructorName(instrNum, name);
        editInstructorEmail(instrNum, email);
        selectRoleForInstructor(instrNum, role);
        
        saveEditInstructor(instrNum);
        waitForPageToLoad();
    }
    
    public void clickSaveInstructorButton(int instrNum) {
        WebElement button = browser.driver.findElement(By.id("btnSaveInstructor" + instrNum));
        button.click();
        waitForPageToLoad();
    }
    
    public String editInstructorName(int instrNum, String value) {
        WebElement editPanelNameTextBox = getNameField(instrNum);
        fillTextBox(editPanelNameTextBox, value);
        return getTextBoxValue(editPanelNameTextBox);
    }
    
    public String editInstructorEmail(int instrNum, String value) {
        WebElement editPanelEmailTextBox = getEmailField(instrNum);
        fillTextBox(editPanelEmailTextBox, value);
        return getTextBoxValue(editPanelEmailTextBox);
    }
    
    public String fillNewInstructorName(String value) {
        fillTextBox(newInstructorNameTextBox, value);
        return getTextBoxValue(newInstructorNameTextBox);
    }
    
    public String fillNewInstructorEmail(String value) {
        fillTextBox(newInstructorEmailTextBox, value);
        return getTextBoxValue(newInstructorEmailTextBox);
    }
    
    public boolean clickEditInstructorLink(int instrNum) {
        getEditInstructorLink(instrNum).click();
        
        WebElement saveButton = getSaveInstructorButton(instrNum);
        waitForElementVisibility(saveButton);
        
        WebElement editInstructorNameTextBox = getNameField(instrNum);
        WebElement editInstructorEmailTextBox = getEmailField(instrNum);
        
        boolean isEditable = editInstructorNameTextBox.isEnabled()
                             && editInstructorEmailTextBox.isEnabled()
                             && saveButton.isDisplayed();
        
        return isEditable;
    }
    
    public void saveEditInstructor(int instrNum) {
        getSaveInstructorButton(instrNum).click();
    }
    
    public void clickDisplayedToStudentCheckBox(int instrNum) {
        this.displayedToStudentCheckBox(instrNum).click();
    }
    
    public void selectRoleForInstructor(int instrNum, String role) {
        WebElement roleRadioButton = browser.driver.findElement(By.cssSelector(
                "input[id='instructorroleforinstructor" + instrNum + "'][value='" + role + "']"));
        roleRadioButton.click();
    }
    
    public void clickViewDetailsLinkForInstructor(int instrNum, int viewLinkNum) {
        /*
         *  There are groups of 3 elements:
         *  <input>: radio button
         *  <a>: the details link
         *  <br>: break line
         *  Therefore the formula for the position of the details link of the group i-th (count from 1) is i * 3 - 1
         */
        int cssLinkNum = viewLinkNum * 3 - 1;
        WebElement viewLink =
                browser.driver.findElement(
                        By.cssSelector("#accessControlEditDivForInstr" + instrNum
                                       + " > div.form-group > div.col-sm-9 > a:nth-child(" + cssLinkNum + ")"));
        
        viewLink.click();
        waitForPageToLoad();
    }
    
    public void closeModal() {
        WebElement closeButton = browser.driver.findElement(By.className("close"));
        waitForElementToBeClickable(closeButton);
        closeButton.click();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    public boolean isPrivilegeCheckboxInModalChecked(String privilege) {
        By selector = By.cssSelector("#tunePermissionsDivForInstructorAll input[type='checkbox'][name='"
                                     + privilege + "']");
        WebElement checkbox = browser.driver.findElement(selector);
        return checkbox.isSelected();
    }
    
    public boolean isPrivilegeCheckboxInPermissionDivChecked(int instructorIndex, String privilege) {
        By selector = By.cssSelector("#tunePermissionsDivForInstructor" + instructorIndex
                                     + " input[type='checkbox'][name='" + privilege + "']");
        WebElement checkbox = browser.driver.findElement(selector);
        return checkbox.isSelected();
    }
    
    public void clickCourseLevelPrivilegesLink(int instrNum, int linkNum) {
        WebElement coursePanel = this.courseLevelPanel(instrNum);
        coursePanel.findElements(By.cssSelector("input[type='checkbox']")).get(linkNum - 1).click();
    }
    
    public void clickAddSectionLevelPrivilegesLink(int instrNum) {
        this.addSectionLevelPrivilegesLink(instrNum).click();
    }
    
    public void clickSectionSelectionCheckBox(int instrNum, int sectionLevelIndex, int sectionNum) {
        this.sectionSelectionCheckBox(instrNum, sectionLevelIndex - 1, sectionNum - 1).click();
    }
    
    public void clickViewStudentCheckBoxInSectionLevel(int instrNum, int sectionLevelIndex) {
        this.sectionLevelPanelCheckBox(instrNum, sectionLevelIndex,
                     Const.ParamsNames.INSTRUCTOR_PERMISSION_VIEW_STUDENT_IN_SECTIONS).click();
    }
    
    public void clickViewOthersCommentsCheckBoxInSectionLevel(int instrNum, int sectionLevelIndex) {
        this.sectionLevelPanelCheckBox(instrNum, sectionLevelIndex,
                     Const.ParamsNames.INSTRUCTOR_PERMISSION_VIEW_COMMENT_IN_SECTIONS).click();
    }
    
    public void clickViewSessionResultsCheckBoxInSectionLevel(int instrNum, int sectionLevelIndex) {
        this.sectionLevelPanelCheckBox(instrNum, sectionLevelIndex,
                     Const.ParamsNames.INSTRUCTOR_PERMISSION_VIEW_SESSION_IN_SECTIONS).click();
    }
    
    public void clickModifySessionResultCheckBoxInSectionLevel(int instrNum, int sectionLevelIndex) {
        this.sectionLevelPanelCheckBox(instrNum, sectionLevelIndex,
                     Const.ParamsNames.INSTRUCTOR_PERMISSION_MODIFY_SESSION_COMMENT_IN_SECTIONS).click();
    }
    
    public void clickSessionLevelInSectionLevel(int instrNum, int sectionLevelIndex) {
        String linkId = "toggleSessionLevelInSection" + sectionLevelIndex + "ForInstructor" + instrNum;
        browser.driver.findElement(By.id(linkId)).click();
    }
    
    public boolean isTuneSessionPermissionsDivVisible(int instrNum, int sectionLevelIndex) {
        String sessionPermissionsDivId = "tuneSessionPermissionsDiv" + sectionLevelIndex
                                         + "ForInstructor" + instrNum;
        return isElementVisible(By.id(sessionPermissionsDivId));
    }
    
    public boolean clickShowNewInstructorFormButton() {
        showNewInstructorFormButton.click();
        
        boolean isFormShownCorrectly = newInstructorNameTextBox.isEnabled()
                && newInstructorEmailTextBox.isEnabled()
                && addInstructorButton.isDisplayed();

        return isFormShownCorrectly;
    }
    
    public boolean clickOnNewInstructorAccessLevelViewDetails(String role) {
        WebElement instructorForm = browser.driver.findElement(By.id("formAddInstructor"));
        
        WebElement viewDetailsLink = instructorForm.findElement(By.cssSelector(
                                            "a[onclick=\"showInstructorRoleModal('" + role + "')\"]"));
        viewDetailsLink.click();
        
        WebElement viewDetailsModal = browser.driver.findElement(By.cssSelector(
                                            "div#tunePermissionsDivForInstructorAll"));
        waitForElementVisibility(viewDetailsModal);
        
        if ("display: block;".equals(viewDetailsModal.getAttribute("style"))) {
            closeModal();
            return true;
        }
        return false;
    }
    
    public void clickAddInstructorButton() {
        addInstructorButton.click();
        waitForPageToLoad();
    }

    public void clickInviteInstructorLink(int instrNum) {
        getInviteInstructorLink(instrNum).click();
        waitForPageToLoad();
    }
    
    /**
     * Clicks the button to edit the course.
     */
    public void clickEditCourseLink() {
        editCourseLink.click();
        waitForElementVisibility(saveCourseButton);
    }
    
    /**
     * Clicks the save changes button to save the changes made to the course.
     */
    public void clickSaveCourseButton() {
        saveCourseButton.click();
        waitForPageToLoad();
    }
    
    public void editCourseName(String value) {
        fillTextBox(courseNameTextBox, value);
    }
    
    public InstructorCoursesPage clickDeleteCourseLinkAndConfirm() {
        clickAndConfirm(deleteCourseLink);
        waitForPageToLoad();
        return changePageType(InstructorCoursesPage.class);
    }

    public void clickDeleteCourseLinkAndCancel() {
        clickAndCancel(deleteCourseLink);
    }
    
    /**
     * Click the delete instructor button at position {@code instrNum} and click "Yes" in the follow up dialog
     * @param instrNum is the position of the instructor (e.g. 1, 2, 3, ...)
     */
    public void clickDeleteInstructorLinkAndConfirm(int instrNum) {
        WebElement deleteInstructorLink = getDeleteInstructorLink(instrNum);
        clickAndConfirm(deleteInstructorLink);
        waitForPageToLoad();
    }
    
    /**
     * Click the delete instructor button at position {@code instrNum} and click "No" in the follow up dialog
     * @param instrNum is the position of the instructor (e.g. 1, 2, 3, ...)
     */
    public void clickDeleteInstructorLinkAndCancel(int instrNum) {
        WebElement deleteInstructorLink = getDeleteInstructorLink(instrNum);
        clickAndCancel(deleteInstructorLink);
    }

    public boolean isCustomCheckboxChecked(String privilege, int instrNum) {
        By selector = By.cssSelector("#tunePermissionsDivForInstructor" + instrNum
                                     + " input[type='checkbox'][name='" + privilege + "']");
        WebElement checkbox = browser.driver.findElement(selector);
        return checkbox.isSelected();
    }
    
    /**
     * Checks if the course edit form is enabled.
     * @return true if the course edit form is enabled
     */
    public boolean isCourseEditFormEnabled() {
        return !courseIdTextBox.isEnabled() && courseNameTextBox.isEnabled() && saveCourseButton.isDisplayed();
    }
    
    public void changeCourseIdInForm(int instrNum, String newCourseId) {
        String selector = "$('#edit-" + instrNum + " input[name=\"" + Const.ParamsNames.COURSE_ID + "\"]')";
        String action = ".val('" + newCourseId + "')";
        ((JavascriptExecutor) browser.driver).executeScript(selector + action);
    }
    
    // methods that return WebElements of the page go here
    
    public WebElement getEditInstructorLink(int instrNum) {
        return browser.driver.findElement(By.id("instrEditLink" + instrNum));
    }
    
    private WebElement getInviteInstructorLink(int instrNum) {
        return browser.driver.findElement(By.id("instrRemindLink" + instrNum));
    }
    
    public WebElement getDeleteInstructorLink(int instrNum) {
        return browser.driver.findElement(By.id("instrDeleteLink" + instrNum));
    }
    
    public WebElement getSaveInstructorButton(int instrNum) {
        return browser.driver.findElement(By.id("btnSaveInstructor" + instrNum));
    }

    public WebElement getNameField(int instrNum) {
        return browser.driver.findElement(By.id("instructorname" + instrNum));
    }
    
    public WebElement getEmailField(int instrNum) {
        return browser.driver.findElement(By.id("instructoremail" + instrNum));
    }
    
    public WebElement displayedToStudentCheckBox(int instrNum) {
        return browser.driver.findElement(By.cssSelector("#instructorTable" + instrNum + " input[name='"
                                                         + Const.ParamsNames.INSTRUCTOR_IS_DISPLAYED_TO_STUDENT
                                                         + "']"));
    }
    
    public WebElement courseLevelPanel(int instrNum) {
        String permissionDivId = "tunePermissionsDivForInstructor" + instrNum;
        return browser.driver.findElement(By.id(permissionDivId))
                             .findElement(By.cssSelector("div.form-group>div>div.panel"));
    }
    
    public WebElement addSectionLevelPrivilegesLink(int instrNum) {
        return browser.driver.findElement(By.id("addSectionLevelForInstructor" + instrNum));
    }
    
    public WebElement sectionLevelPanel(int instrNum, int sectionLevelIndex) {
        String permissionDivId = "tuneSectionPermissionsDiv" + sectionLevelIndex + "ForInstructor" + instrNum;
        return browser.driver.findElement(By.id(permissionDivId));
    }
    
    private WebElement sectionSelectionCheckBox(int instrNum, int sectionLevelIndex, int sectionNum) {
        WebElement sectionPanel = this.sectionLevelPanel(instrNum, sectionLevelIndex);
        String cssSelector = "input[name='" + Const.ParamsNames.INSTRUCTOR_SECTION_GROUP
                             + sectionLevelIndex + Const.ParamsNames.INSTRUCTOR_SECTION + sectionNum + "']";
        return sectionPanel.findElement(By.cssSelector(cssSelector));
    }
    
    private WebElement sectionLevelPanelCheckBox(int instrNum, int sectionLevelIndex, String checkBoxName) {
        WebElement sectionPanel = this.sectionLevelPanel(instrNum, sectionLevelIndex);
        String cssSelector = "input[type='checkbox'][name='" + checkBoxName
                             + Const.ParamsNames.INSTRUCTOR_SECTION_GROUP + sectionLevelIndex + "']";
        return sectionPanel.findElement(By.cssSelector(cssSelector));
    }

}
