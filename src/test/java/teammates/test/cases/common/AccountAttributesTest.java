package teammates.test.cases.common;

import static teammates.common.util.Const.EOL;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import teammates.common.datatransfer.AccountAttributes;
import teammates.common.datatransfer.StudentProfileAttributes;
import teammates.common.util.FieldValidator;
import teammates.common.util.Sanitizer;
import teammates.common.util.StringHelper;
import teammates.storage.entity.Account;
import teammates.storage.entity.StudentProfile;
import teammates.test.cases.BaseTestCase;

public class AccountAttributesTest extends BaseTestCase {
    
    //TODO: test toString() method
    
    @BeforeClass
    public static void setupClass() {
        printTestClassHeader();
    }
    
    @Test
    public void testGetInvalidStateInfo() {
        ______TS("valid account");
        
        AccountAttributes account = createValidAccountAttributesObject();
        assertTrue("all valid values", account.isValid());
        
        ______TS("null studentProfile");
        
        account.studentProfile = null;
        try {
            account.isValid();
            signalFailureToDetectException(" - AssertionError");
        } catch (AssertionError ae) {
            assertEquals("Non-null value expected for studentProfile", ae.getMessage());
        }
        
        ______TS("invalid account");
        
        account = createInvalidAccountAttributesObject();
        String expectedError = String.format(FieldValidator.PERSON_NAME_ERROR_MESSAGE, "", FieldValidator.REASON_EMPTY) + EOL
                + String.format(FieldValidator.GOOGLE_ID_ERROR_MESSAGE, "invalid google id",
                                FieldValidator.REASON_INCORRECT_FORMAT) + EOL
                + String.format(FieldValidator.EMAIL_ERROR_MESSAGE, "invalid@email@com",
                                FieldValidator.REASON_INCORRECT_FORMAT) + EOL
                + String.format(FieldValidator.INSTITUTE_NAME_ERROR_MESSAGE,
                                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                                FieldValidator.REASON_TOO_LONG);
        assertFalse("all valid values", account.isValid());
        assertEquals("all valid values", expectedError, StringHelper.toString(account.getInvalidityInfo()));
        
    }
    
    @Test
    public void testGetEntityTypeAsString() {
        AccountAttributes account = createValidAccountAttributesObject();
        assertEquals("Account", account.getEntityTypeAsString());
    }
    
    @Test
    public void testToEntity() {
        AccountAttributes account = createValidAccountAttributesObject();
        Account expectedAccount =
                new Account(account.googleId, account.name, account.isInstructor, account.email,
                            account.institute, (StudentProfile) new StudentProfileAttributes().toEntity());
        Account actualAccount = new AccountAttributes(expectedAccount).toEntity();
        
        assertEquals(expectedAccount.getGoogleId(), actualAccount.getGoogleId());
        assertEquals(expectedAccount.getName(), actualAccount.getName());
        assertEquals(expectedAccount.getEmail(), actualAccount.getEmail());
        assertEquals(expectedAccount.getInstitute(), actualAccount.getInstitute());
        assertEquals(expectedAccount.isInstructor(), actualAccount.isInstructor());
        String expectedProfile = new StudentProfileAttributes(expectedAccount.getStudentProfile()).toString();
        String actualProfile = new StudentProfileAttributes(actualAccount.getStudentProfile()).toString();
        assertEquals(expectedProfile, actualProfile);
    }
    
    @Test
    public void testToString() {
        AccountAttributes account = createValidAccountAttributesObject();
        AccountAttributes account1 = createValidAccountAttributesObject();
        AccountAttributes account2 = createInvalidAccountAttributesObject();
                
        assertEquals(account.toString(), account1.toString());
        assertFalse("different accounts have different toString() values",
                account1.toString().equals(account2.toString()));
    }
    
    @Test
    public void testGetIdentificationString() {
        AccountAttributes account = createValidAccountAttributesObject();
        assertEquals(account.googleId, account.getIdentificationString());
    }
    
    @Test
    public void testSanitizeForSaving() {
        AccountAttributes actualAccount = createAccountAttributesToSanitize();
        AccountAttributes expectedAccount = createAccountAttributesToSanitize();
        actualAccount.sanitizeForSaving();
        
        assertEquals(Sanitizer.sanitizeForHtml(expectedAccount.googleId), actualAccount.googleId);
        assertEquals(Sanitizer.sanitizeForHtml(expectedAccount.name), actualAccount.name);
        assertEquals(Sanitizer.sanitizeForHtml(expectedAccount.email), actualAccount.email);
        assertEquals(Sanitizer.sanitizeForHtml(expectedAccount.institute), actualAccount.institute);
        expectedAccount.studentProfile.sanitizeForSaving();
        assertEquals(expectedAccount.studentProfile.toString(), actualAccount.studentProfile.toString());
    }
    
    @Test
    public void testLegacyAccountEntityToAttributes() {
        Account a = new Account("test.googleId", "name", true, "email@e.com", "institute");
        a.setStudentProfile(null);
        
        AccountAttributes attr = new AccountAttributes(a);
        
        assertEquals(a.getGoogleId(), attr.googleId);
        assertEquals(a.getEmail(), attr.email);
        assertEquals(a.getInstitute(), attr.institute);
        assertEquals(a.getName(), attr.name);
        assertEquals(null, a.getStudentProfile());
        assertEquals(null, attr.studentProfile);
        
    }

    private AccountAttributes createInvalidAccountAttributesObject() {
        
        String googleId = "invalid google id";
        String name = ""; //invalid name
        boolean isInstructor = false;
        String email = "invalid@email@com";
        String institute = StringHelper.generateStringOfLength(FieldValidator.INSTITUTE_NAME_MAX_LENGTH + 1);
        StudentProfileAttributes studentProfile = new StudentProfileAttributes();
        
        return new AccountAttributes(googleId, name, isInstructor, email, institute, studentProfile);
    }

    private AccountAttributes createValidAccountAttributesObject() {

        String googleId = "valid.google.id";
        String name = "valid name";
        boolean isInstructor = false;
        String email = "valid@email.com";
        String institute = "valid institute name";
        
        return new AccountAttributes(googleId, name, isInstructor, email, institute);
    }
    
    private AccountAttributes createAccountAttributesToSanitize() {
        
        AccountAttributes account = new AccountAttributes();
        
        account.googleId = "googleId@gmail.com";
        account.name = "'name'";
        account.institute = "\\/";
        account.email = "&<email>&";
        account.isInstructor = true;
        
        String shortName = "<name>";
        String personalEmail = "'toSanitize@email.com'";
        String profileInstitute = "";
        String nationality = "&\"invalid nationality &";
        String gender = "'\"'other";
        String moreInfo = "<<script> alert('hi!'); </script>";
        String pictureKey = "";
        
        account.studentProfile = new StudentProfileAttributes(account.googleId, shortName, personalEmail,
                profileInstitute, nationality, gender, moreInfo, pictureKey);
        
        return account;
        
    }
    
    @AfterClass
    public static void tearDown() {
        printTestClassFooter();
    }

}
