# --- !Ups

CREATE TABLE [Role] (
  RoleId      VARCHAR(50) PRIMARY KEY,
  Name        VARCHAR(50) NOT NULL UNIQUE,
  Description VARCHAR(255),
  IsActive    BIT DEFAULT 1
);

CREATE TABLE Permission (
  PermissionId INT PRIMARY KEY IDENTITY (1, 1),
  Name         VARCHAR(50) NOT NULL,
  Path         VARCHAR(50) NOT NULL
);

CREATE TABLE Role_Permission (
  RoleId       VARCHAR(50) FOREIGN KEY REFERENCES [Role] (RoleId),
  PermissionId INT FOREIGN KEY REFERENCES Permission (PermissionId)
);

CREATE TABLE [User] (
  UserId     INT PRIMARY KEY       IDENTITY (1, 1),
  FirstName  VARCHAR(50)  NOT NULL,
  LastName   VARCHAR(50)  NOT NULL,
  Email      VARCHAR(50)  NOT NULL UNIQUE,
  [Password] VARCHAR(255) NOT NULL,
  CreateDate DATETIME              DEFAULT GETDATE(),
  IsActive   BIT                   DEFAULT 1,
  RoleId     VARCHAR(50) FOREIGN KEY REFERENCES [Role] (RoleId)
);

CREATE TABLE Faculty (
  FacultyId INT PRIMARY KEY IDENTITY (1, 1),
  Name      VARCHAR(50) NOT NULL UNIQUE,
  PVCId     INT FOREIGN KEY REFERENCES [User] (UserId),
  DLTId     INT FOREIGN KEY REFERENCES [User] (UserId),
  IsActive  BIT             DEFAULT 1
);

CREATE TABLE Course (
  CourseId VARCHAR(10) PRIMARY KEY,
  Title    VARCHAR(50) NOT NULL,
  FacultyId INT FOREIGN KEY REFERENCES Faculty (FacultyId),
);

CREATE TABLE AcademicSeason (
  AcademicSeasonId INT PRIMARY KEY IDENTITY (1, 1),
  Name             VARCHAR(50) NOT NULL, --2016 of course C00001
  StartDate        DATE        NOT NULL,
  EndDate          DATE        NOT NULL,
);

CREATE TABLE InfoCourseEachAcademicSeason (
  CourseId         VARCHAR(10) FOREIGN KEY REFERENCES Course (CourseId),
  AcademicSeasonId INT FOREIGN KEY REFERENCES AcademicSeason (AcademicSeasonId),
  StudentNumber    INT NOT NULL,
  CLId             INT FOREIGN KEY REFERENCES [User] (UserId),
  CMId             INT FOREIGN KEY REFERENCES [User] (UserId),
  PRIMARY KEY (CourseId, AcademicSeasonId)
);

CREATE TABLE CMR (
  CMRId            INT PRIMARY KEY IDENTITY (1, 1),
  Status           VARCHAR(20) NOT NULL, --REPORT STATUS (Created, Submitted, Approved, Commented, Expired)
  UserCreateId     INT FOREIGN KEY REFERENCES [User] (UserId),
  CourseId         VARCHAR(10) FOREIGN KEY REFERENCES Course (CourseId),
  AcademicSeasonId INT FOREIGN KEY REFERENCES AcademicSeason (AcademicSeasonId),
  CreatedDate      DATETIME        DEFAULT GETDATE(),
  SubmittedDate    DATETIME,
  UserApprovedId   INT,
  ApprovedDate     DATETIME,
  Comment          VARCHAR(255),
  UserCommentedId  INT,
  CommentedDate    DATETIME
);

CREATE TABLE AssessmentMethod (
  AssessmentMethodId INT PRIMARY KEY IDENTITY (1, 1),
  Priority           INT         NOT NULL,
  Name               VARCHAR(50) NOT NULL,
  Description        VARCHAR(255),
  IsActive           BIT             DEFAULT 1
);


CREATE TABLE GradeStatistic (--Matrix 2d store statistical for CMR
  CMRId              INT FOREIGN KEY REFERENCES CMR (CMRId)
    ON DELETE CASCADE,
  StatisticType      VARCHAR(50), --row
  AssessmentMethodId INT FOREIGN KEY REFERENCES AssessmentMethod (AssessmentMethodId), --col
  [Value]            FLOAT, -- cell value store score after calculate from other
  PRIMARY KEY (CMRId, AssessmentMethodId, StatisticType)
);

CREATE TABLE GradeDistribution (--Matrix 2d store Grade distribution data for CMR
  CMRId              INT FOREIGN KEY REFERENCES CMR (CMRId)
    ON DELETE CASCADE,
  AssessmentMethodId INT FOREIGN KEY REFERENCES AssessmentMethod (AssessmentMethodId), --row
  DistributionType   VARCHAR(50), --col
  [Value]            INT NOT NULL, -- cell value store number of student after calculate from other
  PRIMARY KEY (CMRId, AssessmentMethodId, DistributionType)
);

CREATE TABLE Score (--Example table score of student
  ScoreId            INT PRIMARY KEY IDENTITY (1, 1),
  [Value]            FLOAT NOT NULL,
  AssessmentMethodId INT FOREIGN KEY REFERENCES AssessmentMethod (AssessmentMethodId),
  CourseId           VARCHAR(10) FOREIGN KEY REFERENCES Course (CourseId),
  AcademicSeasonId   INT FOREIGN KEY REFERENCES AcademicSeason (AcademicSeasonId)
);

INSERT INTO [Role] VALUES ('ADM', 'Administrator', 'Maintain the data of courses, staff, roles', 1);
INSERT INTO [Role] VALUES ('PVC', 'Pro-Vice Chancellor', 'PVC of faculty', 1);
INSERT INTO [Role] VALUES ('DLT', 'Director of Learning and Quality', 'DLT of faculty', 1);
INSERT INTO [Role] VALUES ('CM', 'Course Moderator', 'CM of course', 1);
INSERT INTO [Role] VALUES ('CL', 'Course Leader', 'CL of course', 1);
INSERT INTO [Role] VALUES ('GUEST', 'Guest', 'Guest account for each faculty', 1);

INSERT INTO Permission VALUES ('index', 'index');
INSERT INTO Permission VALUES ('list courses', 'courses.list');
INSERT INTO Permission VALUES ('view detail cmr report', 'cmr-report.get');
INSERT INTO Permission VALUES ('add new cmr report', 'cmr-report.add');
INSERT INTO Permission VALUES ('delete cmr report', 'cmr-report.delete');
INSERT INTO Permission VALUES ('submit cmr report', 'cmr-report.submit');
INSERT INTO Permission VALUES ('list cmr report', 'cmr-report.list');
INSERT INTO Permission VALUES ('management courses', 'management.create');
INSERT INTO Permission VALUES ('save courses', 'management.courses.save');
INSERT INTO Permission VALUES ('save academicseasons', 'management.academicseasons.save');
INSERT INTO Permission VALUES ('save info', 'management.infocourseeachacademicseason.save');
INSERT INTO Permission VALUES ('user create', 'user.create');
INSERT INTO Permission VALUES ('user save', 'user.save');
INSERT INTO Permission VALUES ('user delete', 'user.delete');
INSERT INTO Permission VALUES ('user list', 'user.list');
INSERT INTO Permission VALUES ('user edit', 'user.edit');
INSERT INTO Permission VALUES ('user update', 'user.update');

--Permission index
INSERT INTO Role_Permission VALUES ('ADM', 1);
INSERT INTO Role_Permission VALUES ('PVC', 1);
INSERT INTO Role_Permission VALUES ('DLT', 1);
INSERT INTO Role_Permission VALUES ('CM', 1);
INSERT INTO Role_Permission VALUES ('CL', 1);
INSERT INTO Role_Permission VALUES ('GUEST', 1);
--Permission list courses
INSERT INTO Role_Permission VALUES ('ADM', 2);
INSERT INTO Role_Permission VALUES ('PVC', 2);
INSERT INTO Role_Permission VALUES ('DLT', 2);
INSERT INTO Role_Permission VALUES ('CM', 2);
INSERT INTO Role_Permission VALUES ('CL', 2);
--Permission get courses
INSERT INTO Role_Permission VALUES ('PVC', 3);
INSERT INTO Role_Permission VALUES ('DLT', 3);
INSERT INTO Role_Permission VALUES ('CM', 3);
INSERT INTO Role_Permission VALUES ('CL', 3);
--Permission add new cmr
INSERT INTO Role_Permission VALUES ('CL', 4);
--Permission delete cmr
INSERT INTO Role_Permission VALUES ('CL', 5);
--Permission submit cmr
INSERT INTO Role_Permission VALUES ('DLT', 6);
INSERT INTO Role_Permission VALUES ('CM', 6);
INSERT INTO Role_Permission VALUES ('CL', 6);
--Permission list cmr
INSERT INTO Role_Permission VALUES ('PVC', 7);
INSERT INTO Role_Permission VALUES ('DLT', 7);
INSERT INTO Role_Permission VALUES ('CM', 7);
INSERT INTO Role_Permission VALUES ('CL', 7);
--Permission create management page save courses and save academicseasons and save info
INSERT INTO Role_Permission VALUES ('ADM', 8);
INSERT INTO Role_Permission VALUES ('ADM', 9);
INSERT INTO Role_Permission VALUES ('ADM', 10);
INSERT INTO Role_Permission VALUES ('ADM', 11);
--Permission create / save / edit/ delete / user
INSERT INTO Role_Permission VALUES ('ADM', 12);
INSERT INTO Role_Permission VALUES ('ADM', 13);
INSERT INTO Role_Permission VALUES ('ADM', 14);
INSERT INTO Role_Permission VALUES ('ADM', 15);
INSERT INTO Role_Permission VALUES ('ADM', 16);
INSERT INTO Role_Permission VALUES ('ADM', 17);

INSERT INTO AssessmentMethod VALUES (0, 'CW1', 'Coursework 1', DEFAULT);
INSERT INTO AssessmentMethod VALUES (1, 'CW2', 'Coursework 2', DEFAULT);
INSERT INTO AssessmentMethod VALUES (2, 'CW3', 'Coursework 3', DEFAULT);
INSERT INTO AssessmentMethod VALUES (3, 'CW4', 'Coursework 4', DEFAULT);
INSERT INTO AssessmentMethod VALUES (4, 'EXAM', 'Exam', DEFAULT);
INSERT INTO AssessmentMethod VALUES (5, 'OVERALL', 'Overall', DEFAULT);

INSERT INTO [User] VALUES
  ('Administrator', 'Administrator', 'admin', '$2a$10$zxZOazEeNe9EiZxpiVvVoeIln6gPtE1niU6c25cLPSNpa5wlGsOii',
   DEFAULT, 1, 'ADM'); --admin
INSERT INTO [User] VALUES
  ('ChinhPVC', 'Nguyen', 'chinhnkgt00494@fpt.edu.vn', '$2a$10$zxZOazEeNe9EiZxpiVvVoeIln6gPtE1niU6c25cLPSNpa5wlGsOii',
   DEFAULT, 1, 'PVC'); --chinh PVC
INSERT INTO [User] VALUES
  ('ChinhDLT', 'Nguyen', 'chinhngk@gmail.com', '$2a$10$zxZOazEeNe9EiZxpiVvVoeIln6gPtE1niU6c25cLPSNpa5wlGsOii', DEFAULT,
   1, 'DLT'); --chinh DLT
INSERT INTO [User] VALUES
  ('Tu', 'Luu', 'tulcgc00706@fpt.edu.vn', '$2a$10$zxZOazEeNe9EiZxpiVvVoeIln6gPtE1niU6c25cLPSNpa5wlGsOii', DEFAULT, 1,
   'CM'); --tu CM
INSERT INTO [User] VALUES
  ('ChinhCL', 'Nguyen', 'nguyenkienchinh91@gmail.com', '$2a$10$zxZOazEeNe9EiZxpiVvVoeIln6gPtE1niU6c25cLPSNpa5wlGsOii',
   DEFAULT, 1, 'CL'); -- chinh CL
INSERT INTO [User] VALUES
  ('Guest', '', 'guest', '$2a$10$zxZOazEeNe9EiZxpiVvVoeIln6gPtE1niU6c25cLPSNpa5wlGsOii', DEFAULT, 1, 'GUEST'); --guest

-- INSERT INTO [User] VALUES ('Hung', 'Vu', 'hungvmgc00672@fpt.edu.vn', '123', DEFAULT, 1);
-- INSERT INTO [User] VALUES ('Sani', 'Maina', 'mainagt00402@fpt.edu.vn', '123', DEFAULT, 1);
-- INSERT INTO [User] VALUES ('Matthew', 'Okafor', 'okaforgt00456@fpt.edu.vn', '123', DEFAULT, 1);
-- INSERT INTO [User] VALUES ('Fake', 'Fake', 'Fake@gmail.com', '123', DEFAULT, 1);
-- INSERT INTO [User] VALUES ('Test', 'Test', 'Test@gmailcom', '123', DEFAULT, 1);

INSERT INTO Faculty VALUES ('Computer Science', 2, 3, 1);

INSERT INTO Course VALUES ('C00001', 'Software Engineering',1);
INSERT INTO Course VALUES ('C00002', 'Database Analyst', 1);

INSERT INTO AcademicSeason VALUES ('Spring 2016', GETDATE(), GETDATE() + 150)

INSERT INTO InfoCourseEachAcademicSeason VALUES ('C00001', 1, 5, 5, 4)
INSERT INTO InfoCourseEachAcademicSeason VALUES ('C00002', 1, 0, 5, 4)
-- 5 student 5 cw1
INSERT INTO Score VALUES (20, 1, 'C00001', 1);
INSERT INTO Score VALUES (70, 1, 'C00001', 1);
INSERT INTO Score VALUES (40, 1, 'C00001', 1);
INSERT INTO Score VALUES (20, 1, 'C00001', 1);
INSERT INTO Score VALUES (90, 1, 'C00001', 1);
-- 5 student 5 cw2
INSERT INTO Score VALUES (40, 2, 'C00001', 1);
INSERT INTO Score VALUES (20, 2, 'C00001', 1);
INSERT INTO Score VALUES (30, 2, 'C00001', 1);
INSERT INTO Score VALUES (60, 2, 'C00001', 1);
INSERT INTO Score VALUES (80, 2, 'C00001', 1);
-- 5 student 5 overall
INSERT INTO Score VALUES (30, 6, 'C00001', 1);
INSERT INTO Score VALUES (45, 6, 'C00001', 1);
INSERT INTO Score VALUES (35, 6, 'C00001', 1);
INSERT INTO Score VALUES (40, 6, 'C00001', 1);

INSERT INTO Score VALUES (85, 6, 'C00001', 1);

# --- !Downs
DROP TABLE Score;
DROP TABLE GradeDistribution;
DROP TABLE GradeStatistic;
DROP TABLE AssessmentMethod;
DROP TABLE CMR;
DROP TABLE InfoCourseEachAcademicSeason;
DROP TABLE AcademicSeason;
DROP TABLE Course;
DROP TABLE Faculty;
DROP TABLE [User];
DROP TABLE Role_Permission;
DROP TABLE Permission;
DROP TABLE [Role];