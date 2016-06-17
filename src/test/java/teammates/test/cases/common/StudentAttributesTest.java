package teammates.test.cases.common;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import teammates.common.datatransfer.StudentAttributes;
import teammates.common.datatransfer.StudentAttributes.UpdateStatus;
import teammates.common.util.Config;
import teammates.common.util.Const;
import teammates.common.util.FieldValidator;
import teammates.common.util.StringHelper;
import teammates.storage.entity.Student;
import teammates.test.cases.BaseTestCase;

public class StudentAttributesTest extends BaseTestCase {

    private class StudentAttributesWithModifiableTimestamp extends StudentAttributes {
        
        private void setCreatedAt(Date createdAt) {
            this.createdAt = createdAt;
        }
        
        private void setUpdatedAt(Date updatedAt) {
            this.updatedAt = updatedAt;
        }
        
    }
    
    @BeforeClass
    public static void setupClass() {
        printTestClassHeader();
    }

    @Test
    public void testDefaultTimestamp() {
        
        StudentAttributesWithModifiableTimestamp s = new StudentAttributesWithModifiableTimestamp();
        
        s.setCreatedAt(null);
        s.setUpdatedAt(null);
        
        Date defaultStudentCreationTimeStamp = Const.TIME_REPRESENTS_DEFAULT_TIMESTAMP;
        
        ______TS("success : defaultTimeStamp for createdAt date");
        
        assertEquals(defaultStudentCreationTimeStamp, s.getCreatedAt());
        
        ______TS("success : defaultTimeStamp for updatedAt date");
        
        assertEquals(defaultStudentCreationTimeStamp, s.getUpdatedAt());
    }
    
    @Test
    public void testUpdateStatusEnum() {
        assertEquals(UpdateStatus.ERROR, UpdateStatus.enumRepresentation(0));
        assertEquals(UpdateStatus.NEW, UpdateStatus.enumRepresentation(1));
        assertEquals(UpdateStatus.MODIFIED, UpdateStatus.enumRepresentation(2));
        assertEquals(UpdateStatus.UNMODIFIED, UpdateStatus.enumRepresentation(3));
        assertEquals(UpdateStatus.NOT_IN_ENROLL_LIST, UpdateStatus.enumRepresentation(4));
        assertEquals(UpdateStatus.UNKNOWN, UpdateStatus.enumRepresentation(5));
        assertEquals(UpdateStatus.UNKNOWN, UpdateStatus.enumRepresentation(-1));
    }

    @Test
    public void testStudentConstructor() {
        String courseId = "anyCoursId";
        StudentAttributes invalidStudent;

        Student expected;
        StudentAttributes studentUnderTest;

        ______TS("Typical case: contains white space");
        expected = generateTypicalStudentObject();
        studentUnderTest = new StudentAttributes("  sect 1 ", "  team 1   ", "   name 1   ",
                                                 "   email@email.com  ", "  comment 1  ", "courseId1");
        verifyStudentContent(expected, studentUnderTest.toEntity());

        ______TS("Typical case: contains google id");
        expected = generateTypicalStudentObject();
        studentUnderTest = new StudentAttributes("googleId.1", "email@email.com", "name 1", "comment 1",
                                                 "courseId1", "team 1", "section 1");
        verifyStudentContentIncludingId(expected, studentUnderTest.toEntity());

        ______TS("Typical case: initialize from entity");
        expected = generateTypicalStudentObject();
        studentUnderTest = new StudentAttributes(expected);
        verifyStudentContentIncludingId(expected, studentUnderTest.toEntity());

        ______TS("Failure case: empty course id");
        invalidStudent = new StudentAttributes("section", "team", "name", "e@e.com", "c", "");
        assertFalse(invalidStudent.isValid());
        assertEquals(String.format(FieldValidator.COURSE_ID_ERROR_MESSAGE,
                                   invalidStudent.course, FieldValidator.REASON_EMPTY),
                     invalidStudent.getInvalidityInfo().get(0));

        ______TS("Failure case: invalid course id");
        invalidStudent = new StudentAttributes("section", "team", "name", "e@e.com", "c", "Course Id with space");
        assertFalse(invalidStudent.isValid());
        assertEquals(String.format(FieldValidator.COURSE_ID_ERROR_MESSAGE,
                                   invalidStudent.course, FieldValidator.REASON_INCORRECT_FORMAT),
                     invalidStudent.getInvalidityInfo().get(0));

        ______TS("Failure case: empty name");
        invalidStudent = new StudentAttributes("sect", "t1", "", "e@e.com",
                                               "c", courseId);
        assertFalse(invalidStudent.isValid());
        assertEquals(invalidStudent.getInvalidityInfo().get(0),
                     String.format(FieldValidator.PERSON_NAME_ERROR_MESSAGE, "", FieldValidator.REASON_EMPTY));

        ______TS("Failure case: empty email");
        invalidStudent = new StudentAttributes("sect", "t1", "n", "", "c", courseId);
        assertFalse(invalidStudent.isValid());
        assertEquals(String.format(FieldValidator.EMAIL_ERROR_MESSAGE, "", FieldValidator.REASON_EMPTY),
                     invalidStudent.getInvalidityInfo().get(0));

        ______TS("Failure case: section name too long");
        String longSectionName = StringHelper
                .generateStringOfLength(FieldValidator.SECTION_NAME_MAX_LENGTH + 1);
        invalidStudent = new StudentAttributes(longSectionName, "t1", "n", "e@e.com", "c", courseId);
        assertFalse(invalidStudent.isValid());
        assertEquals(String.format(FieldValidator.SECTION_NAME_ERROR_MESSAGE, longSectionName,
                                   FieldValidator.REASON_TOO_LONG),
                     invalidStudent.getInvalidityInfo().get(0));

        ______TS("Failure case: team name too long");
        String longTeamName = StringHelper.generateStringOfLength(FieldValidator.TEAM_NAME_MAX_LENGTH + 1);
        invalidStudent = new StudentAttributes("sect", longTeamName, "name", "e@e.com", "c", courseId);
        assertFalse(invalidStudent.isValid());
        assertEquals(String.format(FieldValidator.TEAM_NAME_ERROR_MESSAGE, longTeamName,
                                   FieldValidator.REASON_TOO_LONG),
                     invalidStudent.getInvalidityInfo().get(0));

        ______TS("Failure case: student name too long");
        String longStudentName = StringHelper
                .generateStringOfLength(FieldValidator.PERSON_NAME_MAX_LENGTH + 1);
        invalidStudent = new StudentAttributes("sect", "t1", longStudentName, "e@e.com", "c", courseId);
        assertFalse(invalidStudent.isValid());
        assertEquals(String.format(FieldValidator.PERSON_NAME_ERROR_MESSAGE,
                                   longStudentName, FieldValidator.REASON_TOO_LONG),
                     invalidStudent.getInvalidityInfo().get(0));

        ______TS("Failure case: invalid email");
        invalidStudent = new StudentAttributes("sect", "t1", "name", "ee.com", "c", courseId);
        assertFalse(invalidStudent.isValid());
        assertEquals(String.format(FieldValidator.EMAIL_ERROR_MESSAGE,
                                   "ee.com", FieldValidator.REASON_INCORRECT_FORMAT),
                     invalidStudent.getInvalidityInfo().get(0));

        ______TS("Failure case: comment too long");
        String longComment = StringHelper
                .generateStringOfLength(FieldValidator.STUDENT_ROLE_COMMENTS_MAX_LENGTH + 1);
        invalidStudent = new StudentAttributes("sect", "t1", "name", "e@e.com", longComment, courseId);
        assertFalse(invalidStudent.isValid());
        assertEquals(String.format(FieldValidator.STUDENT_ROLE_COMMENTS_ERROR_MESSAGE,
                                   longComment, FieldValidator.REASON_TOO_LONG),
                     invalidStudent.getInvalidityInfo().get(0));

        // Other invalid parameters cases are omitted because they are already
        // unit-tested in validate*() methods in Common.java
    }

    @Test
    public void testValidate() {
        StudentAttributes s = generateValidStudentAttributesObject();

        assertTrue("valid value", s.isValid());

        s.googleId = "invalid@google@id";
        s.name = "";
        s.email = "invalid email";
        s.course = "";
        s.comments = StringHelper.generateStringOfLength(FieldValidator.STUDENT_ROLE_COMMENTS_MAX_LENGTH + 1);
        s.team = StringHelper.generateStringOfLength(FieldValidator.TEAM_NAME_MAX_LENGTH + 1);

        assertFalse("invalid value", s.isValid());
        String errorMessage = String.format(FieldValidator.GOOGLE_ID_ERROR_MESSAGE, "invalid@google@id",
                                            FieldValidator.REASON_INCORRECT_FORMAT) + Const.EOL
                + String.format(FieldValidator.COURSE_ID_ERROR_MESSAGE, "", FieldValidator.REASON_EMPTY) + Const.EOL
                + String.format(FieldValidator.EMAIL_ERROR_MESSAGE, "invalid email",
                                FieldValidator.REASON_INCORRECT_FORMAT) + Const.EOL
                + String.format(FieldValidator.TEAM_NAME_ERROR_MESSAGE,
                                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                                FieldValidator.REASON_TOO_LONG) + Const.EOL
                + String.format(FieldValidator.STUDENT_ROLE_COMMENTS_ERROR_MESSAGE, s.comments,
                                FieldValidator.REASON_TOO_LONG) + Const.EOL
                + String.format(FieldValidator.PERSON_NAME_ERROR_MESSAGE, "", FieldValidator.REASON_EMPTY);
        assertEquals("invalid value", errorMessage, StringHelper.toString(s.getInvalidityInfo()));
    }

    @Test
    public void testIsEnrollInfoSameAs() {
        StudentAttributes student = new StudentAttributes(generateTypicalStudentObject());
        StudentAttributes other = new StudentAttributes(generateTypicalStudentObject());

        ______TS("Typical case: Same enroll info");
        assertTrue(student.isEnrollInfoSameAs(other));

        ______TS("Typical case: Compare to null");
        assertFalse(student.isEnrollInfoSameAs(null));

        ______TS("Typical case: Different in email");
        other.email = "other@email.com";
        assertFalse(student.isEnrollInfoSameAs(other));

        ______TS("Typical case: Different in name");
        other = new StudentAttributes(generateTypicalStudentObject());
        other.name = "otherName";
        assertFalse(student.isEnrollInfoSameAs(other));

        ______TS("Typical case: Different in course id");
        other = new StudentAttributes(generateTypicalStudentObject());
        other.course = "otherCourse";
        assertFalse(student.isEnrollInfoSameAs(other));

        ______TS("Typical case: Different in comment");
        other = new StudentAttributes(generateTypicalStudentObject());
        other.comments = "otherComments";
        assertFalse(student.isEnrollInfoSameAs(other));

        ______TS("Typical case: Different in team");
        other = new StudentAttributes(generateTypicalStudentObject());
        other.team = "otherTeam";
        assertFalse(student.isEnrollInfoSameAs(other));

        ______TS("Typical case: Different in section");
        other = new StudentAttributes(generateStudentWithoutSectionObject());
        assertFalse(student.isEnrollInfoSameAs(other));
    }

    @Test
    public void testSortByNameAndThenByEmail() {
        List<StudentAttributes> sortedList = generateTypicalStudentAttributesList();
        StudentAttributes.sortByNameAndThenByEmail(sortedList);
        List<StudentAttributes> unsortedList = generateTypicalStudentAttributesList();
        assertEquals(sortedList.get(0).toEnrollmentString(), unsortedList.get(0).toEnrollmentString());
        assertEquals(sortedList.get(1).toEnrollmentString(), unsortedList.get(3).toEnrollmentString());
        assertEquals(sortedList.get(2).toEnrollmentString(), unsortedList.get(2).toEnrollmentString());
        assertEquals(sortedList.get(3).toEnrollmentString(), unsortedList.get(1).toEnrollmentString());
    }

    @Test
    public void testSortByTeam() {
        List<StudentAttributes> sortedList = generateTypicalStudentAttributesList();
        StudentAttributes.sortByTeamName(sortedList);
        List<StudentAttributes> unsortedList = generateTypicalStudentAttributesList();
        assertEquals(sortedList.get(0).toEnrollmentString(),
                     unsortedList.get(2).toEnrollmentString());
        assertEquals(sortedList.get(1).toEnrollmentString(),
                     unsortedList.get(0).toEnrollmentString());
        assertEquals(sortedList.get(2).toEnrollmentString(),
                     unsortedList.get(1).toEnrollmentString());
        assertEquals(sortedList.get(3).toEnrollmentString(),
                     unsortedList.get(3).toEnrollmentString());
    }

    @Test
    public void testSortBySection() {
        List<StudentAttributes> sortedList = generateTypicalStudentAttributesList();
        StudentAttributes.sortBySectionName(sortedList);
        List<StudentAttributes> unsortedList = generateTypicalStudentAttributesList();
        assertEquals(sortedList.get(0).toEnrollmentString(),
                     unsortedList.get(3).toEnrollmentString());
        assertEquals(sortedList.get(1).toEnrollmentString(),
                     unsortedList.get(0).toEnrollmentString());
        assertEquals(sortedList.get(2).toEnrollmentString(),
                     unsortedList.get(1).toEnrollmentString());
        assertEquals(sortedList.get(3).toEnrollmentString(),
                     unsortedList.get(2).toEnrollmentString());
    }

    @Test
    public void testIsRegistered() {
        StudentAttributes sd = new StudentAttributes("sect 1", "team 1", "name 1", "email@email.com",
                                                     "comment 1", "course1");

        // Id is not given yet
        assertFalse(sd.isRegistered());

        // Id empty
        sd.googleId = "";
        assertFalse(sd.isRegistered());

        // Id given
        sd.googleId = "googleId.1";
        assertTrue(sd.isRegistered());
    }

    @Test
    public void testToString() {
        StudentAttributes sd = new StudentAttributes("sect 1", "team 1", "name 1", "email@email.com",
                                                     "comment 1", "course1");
        assertEquals("Student:name 1[email@email.com]" + Const.EOL, sd.toString());
        assertEquals("    Student:name 1[email@email.com]" + Const.EOL, sd.toString(4));
    }

    @Test
    public void testToEnrollmentString() {
        StudentAttributes sd = new StudentAttributes("sect 1", "team 1", "name 1", "email@email.com",
                                                     "comment 1", "course1");
        assertEquals("sect 1|team 1|name 1|email@email.com|comment 1", sd.toEnrollmentString());
    }

    @Test
    public void testGetRegistrationLink() {
        StudentAttributes sd = new StudentAttributes("sect 1", "team 1", "name 1", "email@email.com",
                                                     "comment 1", "course1");
        sd.key = "testkey";
        String regUrl = Config.getAppUrl(Const.ActionURIs.STUDENT_COURSE_JOIN_NEW)
                                .withRegistrationKey(StringHelper.encrypt("testkey"))
                                .withStudentEmail("email@email.com")
                                .withCourseId("course1")
                                .toString();
        assertEquals(regUrl, sd.getRegistrationUrl());
    }

    @Test
    public void testGetPublicProfilePictureUrl() {
        StudentAttributes sd = new StudentAttributes("sect 1", "team 1", "name 1", "email@email.com",
                                                     "comment 1", "course1");
        String profilePicUrl = Config.getAppUrl(Const.ActionURIs.STUDENT_PROFILE_PICTURE)
                                       .withStudentEmail(StringHelper.encrypt("email@email.com"))
                                       .withCourseId(StringHelper.encrypt("course1"))
                                       .toString();
        assertEquals(profilePicUrl, sd.getPublicProfilePictureUrl());
    }

    @Test
    public void testGetJsonString() {
        StudentAttributes sd = new StudentAttributes("sect 1", "team 1", "name 1", "email@email.com",
                                        "comment 1", "course1");
        assertEquals("{\n  \"name\": \"name 1\",\n  \"lastName\": \"1\",\n  \"email\": \"email@email.com\","
                     + "\n  \"course\": \"course1\",\n  \"comments\": \"comment 1\",\n  \"team\": \"team 1\","
                     + "\n  \"section\": \"sect 1\",\n  \"updateStatus\": \"UNKNOWN\"\n}",
                     sd.getJsonString());
    }

    private Student generateTypicalStudentObject() {
        return new Student("email@email.com", "name 1", "googleId.1", "comment 1", "courseId1", "team 1", "sect 1");
    }

    private Student generateStudentWithoutSectionObject() {
        return new Student("email@email.com", "name 1", "googleId.1", "comment 1", "courseId1", "team 1", null);
    }

    private List<StudentAttributes> generateTypicalStudentAttributesList() {
        List<StudentAttributes> list = new ArrayList<>();
        list.add(new StudentAttributes("sect 2", "team 2", "name 1", "email 1", "comment 1", "courseId"));
        list.add(new StudentAttributes("sect 2", "team 2", "name 4", "email 4", "comment 4", "courseId"));
        list.add(new StudentAttributes("sect 3", "team 1", "name 2", "email 3", "comment 3", "courseId"));
        list.add(new StudentAttributes("sect 1", "team 3", "name 2", "email 2", "comment 2", "courseId"));
        return list;
    }

    private void verifyStudentContent(Student expected, Student actual) {
        assertEquals(expected.getTeamName(), actual.getTeamName());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getEmail(), actual.getEmail());
        assertEquals(expected.getComments(), actual.getComments());
    }

    private void verifyStudentContentIncludingId(Student expected, Student actual) {
        verifyStudentContent(expected, actual);
        assertEquals(expected.getGoogleId(), actual.getGoogleId());
    }

    private StudentAttributes generateValidStudentAttributesObject() {
        StudentAttributes s;
        s = new StudentAttributes();
        s.googleId = "valid.google.id";
        s.name = "valid name";
        s.email = "valid@email.com";
        s.course = "valid-course-id";
        s.comments = "";
        s.team = "valid team";
        s.section = "valid section";
        return s;
    }

    @AfterClass
    public static void tearDown() {
        printTestClassFooter();
    }

}
