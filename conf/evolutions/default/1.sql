# --- !Ups

CREATE TABLE [User] (
  UserId     INT PRIMARY KEY       IDENTITY (1, 1),
  FirstName  VARCHAR(50)  NOT NULL,
  LastName   VARCHAR(50)  NOT NULL,
  Email      VARCHAR(50)  NOT NULL UNIQUE,
  [Password] VARCHAR(255) NOT NULL DEFAULT ('123'),
  CreateDate DATETIME              DEFAULT GETDATE(),
  IsActive   BIT                   DEFAULT 1
);

CREATE TABLE [Role] (
  RoleId      INT PRIMARY KEY IDENTITY (1, 1),
  ShortName   VARCHAR(50) NOT NULL UNIQUE,
  Name        VARCHAR(50) NOT NULL UNIQUE,
  Description VARCHAR(255),
  IsActive    BIT             DEFAULT 1
);

CREATE TABLE [Function] (
  FunctionId INT PRIMARY KEY,
  Name       VARCHAR(50) NOT NULL,
  Descrition VARCHAR(255),
  IsActive   BIT DEFAULT 1
);

CREATE TABLE Authority (
  RoleId     INT FOREIGN KEY REFERENCES [Role] (RoleId),
  FUNCTIONID INT FOREIGN KEY REFERENCES [Function] (FunctionId),
  PRIMARY KEY (RoleId, FUNCTIONID)
);

CREATE TABLE UserRole (
  UserId INT FOREIGN KEY REFERENCES [User] (UserId),
  RoleId INT FOREIGN KEY REFERENCES [Role] (RoleId),
  PRIMARY KEY (UserId, RoleId)
);

CREATE TABLE Faculty (
  FacultyId INT PRIMARY KEY IDENTITY (1, 1),
  Name      VARCHAR(50) NOT NULL UNIQUE,
  PVCId     INT FOREIGN KEY REFERENCES [User] (UserId),
  DLTId     INT FOREIGN KEY REFERENCES [User] (UserId),
  IsActive  BIT             DEFAULT 1
);

CREATE TABLE Course (
  CourseId        VARCHAR(10) PRIMARY KEY,
  AcademicSession VARCHAR(50) NOT NULL,
  StudentNumber   INT         NOT NULL,
  CreateDate      DATE DEFAULT GETDATE(),
  StartDate       DATE        NOT NULL,
  EndDate         DATE        NOT NULL,
  FacultyId       INT FOREIGN KEY REFERENCES Faculty (FacultyId),
  CLId            INT FOREIGN KEY REFERENCES [User] (UserId),
  CMId            INT FOREIGN KEY REFERENCES [User] (UserId)
);

CREATE TABLE CMR (
  CMRId           INT PRIMARY KEY IDENTITY (1, 1),
  Status          VARCHAR(20) NOT NULL, --REPORT STATUS (CREATED, SUBMITTED, APPROVED, COMMENTED, EXPIRED)
  CreateDate      DATETIME        DEFAULT GETDATE(),
  UserCreateId    INT FOREIGN KEY REFERENCES [User] (UserId),
  CourseId        VARCHAR(10) FOREIGN KEY REFERENCES Course (CourseId),
  UserApprovedId  INT,
  ApprovedDate    DATETIME,
  Comment         VARCHAR(255),
  UserCommentedId INT,
  CommentedDate   DATETIME
);

CREATE TABLE AssessmentMethod (
  AssessmentMethodId INT PRIMARY KEY IDENTITY (1, 1),
  Priority             INT         NOT NULL,
  Name                 VARCHAR(50) NOT NULL,
  Description          VARCHAR(255),
  IsActive             BIT             DEFAULT 1
);


CREATE TABLE GradeStatistic (--Matrix 2d store statistical for CMR
  CMRId                 INT FOREIGN KEY REFERENCES CMR (CMRId) ON DELETE CASCADE,
  StatisticType VARCHAR(50), --row
  AssessmentMethodId  INT FOREIGN KEY REFERENCES AssessmentMethod (AssessmentMethodId), --col
  [Value]               FLOAT, -- cell value store score after calculate from other
  PRIMARY KEY (CMRId, AssessmentMethodId, StatisticType)
);

CREATE TABLE GradeDistribution (--Matrix 2d store Grade distribution data for CMR
  CMRId                  INT FOREIGN KEY REFERENCES CMR (CMRId) ON DELETE CASCADE,
  AssessmentMethodId   INT FOREIGN KEY REFERENCES AssessmentMethod (AssessmentMethodId), --row
  DistributionType VARCHAR(50), --col
  [Value]                INT NOT NULL, -- cell value store number of student after calculate from other
  PRIMARY KEY (CMRId, AssessmentMethodId, DistributionType)
);

CREATE TABLE Score (--Example table score of student
  ScoreId              INT PRIMARY KEY IDENTITY (1, 1),
  [Value]              FLOAT NOT NULL,
  AssessmentMethodId INT FOREIGN KEY REFERENCES AssessmentMethod (AssessmentMethodId),
  CourseId             VARCHAR(10) FOREIGN KEY REFERENCES Course (CourseId)
);

INSERT INTO [Role] VALUES ('ADM', 'Administrator', 'Maintain the data of courses, staff, roles', 1);
INSERT INTO [Role] VALUES ('PVC', 'Pro-Vice Chancellor', 'PVC of faculty', 1);
INSERT INTO [Role] VALUES ('DLT', 'Director of Learning and Quality', 'DLT of faculty', 1);
INSERT INTO [Role] VALUES ('CL', 'Course Leader', 'CL of course', 1);
INSERT INTO [Role] VALUES ('CM', 'Course Moderator', 'CM of course', 1);
INSERT INTO [Role] VALUES ('GUEST', 'Guest', 'Guest account for each faculty', 1);

INSERT INTO AssessmentMethod VALUES (0, 'CW1', 'Coursework 1', DEFAULT);
INSERT INTO AssessmentMethod VALUES (1, 'CW2', 'Coursework 2', DEFAULT);
INSERT INTO AssessmentMethod VALUES (2, 'CW3', 'Coursework 3', DEFAULT);
INSERT INTO AssessmentMethod VALUES (3, 'CW4', 'Coursework 4', DEFAULT);
INSERT INTO AssessmentMethod VALUES (4, 'EXAM', 'Exam', DEFAULT);
INSERT INTO AssessmentMethod VALUES (5, 'OVERALL', 'Overall', DEFAULT);

INSERT INTO [User] VALUES ('Administrator', 'Administrator', 'cmr@gmail.com', '123', DEFAULT, 1);
INSERT INTO [User] VALUES ('ChinhCM', 'Nguyen', 'chinhnkgt00494@fpt.edu.vn', '123', DEFAULT, 1);
INSERT INTO [User] VALUES ('ChinhDLT', 'Nguyen', 'chinhngk@gmail.com', '123', DEFAULT, 1);
INSERT INTO [User] VALUES ('Tu', 'Luu', 'tulcgc00706@fpt.edu.vn', '123', DEFAULT, 1);
INSERT INTO [User] VALUES ('Hung', 'Vu', 'hungvmgc00672@fpt.edu.vn', '123', DEFAULT, 1);
INSERT INTO [User] VALUES ('Sani', 'Maina', 'mainagt00402@fpt.edu.vn', '123', DEFAULT, 1);
INSERT INTO [User] VALUES ('Matthew', 'Okafor', 'okaforgt00456@fpt.edu.vn', '123', DEFAULT, 1);
INSERT INTO [User] VALUES ('Fake', 'Fake', 'Fake@gmail.com', '123', DEFAULT, 1);
INSERT INTO [User] VALUES ('Test', 'Test', 'Test@gmailcom', '123', DEFAULT, 1);
INSERT INTO [User] VALUES ('ChinhCL', 'Nguyen', 'nguyenkienchinh91@gmail.com', '123', DEFAULT, 1);

INSERT INTO UserRole VALUES (1, 1); --admin
INSERT INTO UserRole VALUES (10, 4); --ChinhCL - CL
INSERT INTO UserRole VALUES (2, 2); --ChinhCM - CM
INSERT INTO UserRole VALUES (3, 3); --ChinhDLT - DLT
INSERT INTO UserRole VALUES (4, 2); --Tu PVC

INSERT INTO Faculty VALUES ('Computer Science', 4, 3, 1);

INSERT INTO Course VALUES ('C00001', 'Spring', 5, DEFAULT, GETDATE(), GETDATE() + 150, 1, 10, 4);
INSERT INTO Course VALUES ('C00002', 'Spring', 15, DEFAULT, GETDATE(), GETDATE() + 155, 1, 10, 4);

-- 5 student 5 cw1
INSERT INTO Score VALUES (20, 1, 'C00001');
INSERT INTO Score VALUES (70, 1, 'C00001');
INSERT INTO Score VALUES (40, 1, 'C00001');
INSERT INTO Score VALUES (20, 1, 'C00001');
INSERT INTO Score VALUES (90, 1, 'C00001');
-- 5 student 5 cw2
INSERT INTO Score VALUES (40, 2, 'C00001');
INSERT INTO Score VALUES (20, 2, 'C00001');
INSERT INTO Score VALUES (30, 2, 'C00001');
INSERT INTO Score VALUES (60, 2, 'C00001');
INSERT INTO Score VALUES (80, 2, 'C00001');
-- 5 student 5 overall
INSERT INTO Score VALUES (30, 6, 'C00001');
INSERT INTO Score VALUES (45, 6, 'C00001');
INSERT INTO Score VALUES (35, 6, 'C00001');
INSERT INTO Score VALUES (40, 6, 'C00001');
INSERT INTO Score VALUES (85, 6, 'C00001');

# --- !Downs
DROP TABLE Score;
DROP TABLE GradeDistribution;
DROP TABLE GradeStatistic;
DROP TABLE AssessmentMethod;
DROP TABLE CMR;
DROP TABLE Course;
DROP TABLE Faculty;
DROP TABLE UserRole;
DROP TABLE Authority;
DROP TABLE [Function];
DROP TABLE [Role];
DROP TABLE [User];