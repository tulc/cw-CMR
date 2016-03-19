CREATE PROCEDURE usp_createCMR @courseId VARCHAR(10), @academicSeasonId INT, @userId INT
AS
  BEGIN
    SET NOCOUNT ON;
    BEGIN TRY
    BEGIN TRANSACTION
    DECLARE @assessmentMethodId INT,
    @cmrId INT;
    -- insert CMR and get id
    INSERT INTO CMR
    VALUES ('Created', @userId, @courseId, @academicSeasonId, DEFAULT, NULL, NULL, NULL, NULL, NULL, NULL);
    SET @cmrId = (SELECT cmrId
                  FROM CMR
                  WHERE cmrId = SCOPE_IDENTITY());

    --begin cursor
    DECLARE assessmentMethodCursor CURSOR FOR
      SELECT assessmentMethodId
      FROM AssessmentMethod
      ORDER BY (Priority) ASC;
    OPEN assessmentMethodCursor;
    FETCH NEXT FROM assessmentMethodCursor
    INTO @assessmentMethodId;
    WHILE @@FETCH_STATUS = 0
      BEGIN

        DECLARE @mean FLOAT = 0, @median FLOAT = 0, @stDeviation FLOAT = 0;

        DECLARE @countScore0To9 INT, @countScore10To19 INT, @countScore20To29 INT, @countScore30To39 INT,
        @countScore40To49 INT, @countScore50To59 INT, @countScore60To69 INT, @countScore70To79 INT,
        @countScore80To89 INT, @countScore90 INT

        IF @assessmentMethodId != 6 --IF NOT 6, 'OVERALL'
          BEGIN
            --find 'Mean'
            SET @mean = (SELECT SUM(value) / COUNT(*)
                         FROM Score
                         WHERE CourseId = @courseId
                               AND AcademicSeasonId = @academicSeasonId
                               AND AssessmentMethodId = @assessmentMethodId)


            --find 'Median'
            DECLARE @count INT = (SELECT COUNT(*)
                                  FROM score
                                  WHERE courseId = @courseId
                                        AND AcademicSeasonId = @academicSeasonId
                                        AND assessmentMethodId = @assessmentMethodId);
            IF @count % 2 = 0
              SET @median = (SELECT SUM(value) / 2
                             FROM (SELECT
                                     ROW_NUMBER()
                                     OVER (
                                       ORDER BY value) AS Row,
                                     value
                                   FROM Score
                                   WHERE courseId = @courseId
                                         AND AcademicSeasonId = @academicSeasonId
                                         AND assessmentMethodId = @assessmentMethodId) AS s
                             WHERE s.Row = @count / 2 + 1
                                   OR s.Row = @count / 2)
            ELSE
              SET @median = (SELECT VALUE
                             FROM (SELECT
                                     ROW_NUMBER()
                                     OVER (
                                       ORDER BY value) AS Row,
                                     value
                                   FROM Score
                                   WHERE courseId = @courseId
                                         AND AcademicSeasonId = @academicSeasonId
                                         AND assessmentMethodId = @assessmentMethodId) AS s
                             WHERE s.Row = ROUND(CAST(@count AS FLOAT) / CAST(2 AS FLOAT), 0))

            -- find Standard Deviation
            SET @stDeviation = (SELECT ROUND(SQRT(SUM(SQUARE(value - @mean)) / (COUNT(*) - 1)), 2)
                                FROM Score
                                WHERE courseId = @courseId
                                      AND AcademicSeasonId = @academicSeasonId
                                      AND assessmentMethodId = @assessmentMethodId)

            --Count VALUE TO Grade Distribution Data
            SET @countScore0To9 = (SELECT COUNT(*)
                                   FROM Score
                                   WHERE courseId = @courseId
                                         AND AcademicSeasonId = @academicSeasonId
                                         AND assessmentMethodId = @assessmentMethodId
                                         AND value BETWEEN 0 AND 9)
            SET @countScore10To19 = (SELECT COUNT(*)
                                     FROM Score
                                     WHERE courseId = @courseId
                                           AND AcademicSeasonId = @academicSeasonId
                                           AND assessmentMethodId = @assessmentMethodId
                                           AND
                                           value BETWEEN 10 AND 19)
            SET @countScore20To29 = (SELECT COUNT(*)
                                     FROM Score
                                     WHERE courseId = @courseId
                                           AND AcademicSeasonId = @academicSeasonId
                                           AND assessmentMethodId = @assessmentMethodId
                                           AND value BETWEEN 20 AND 29)
            SET @countScore30To39 = (SELECT COUNT(*)
                                     FROM Score
                                     WHERE courseId = @courseId
                                           AND AcademicSeasonId = @academicSeasonId
                                           AND assessmentMethodId = @assessmentMethodId
                                           AND value BETWEEN 30 AND 39)
            SET @countScore40To49 = (SELECT COUNT(*)
                                     FROM Score
                                     WHERE courseId = @courseId
                                           AND AcademicSeasonId = @academicSeasonId
                                           AND assessmentMethodId = @assessmentMethodId
                                           AND value BETWEEN 40 AND 49)
            SET @countScore50To59 = (SELECT COUNT(*)
                                     FROM Score
                                     WHERE courseId = @courseId
                                           AND AcademicSeasonId = @academicSeasonId
                                           AND assessmentMethodId = @assessmentMethodId
                                           AND value BETWEEN 50 AND 59)
            SET @countScore60To69 = (SELECT COUNT(*)
                                     FROM Score
                                     WHERE courseId = @courseId
                                           AND AcademicSeasonId = @academicSeasonId
                                           AND assessmentMethodId = @assessmentMethodId
                                           AND value BETWEEN 60 AND 69)
            SET @countScore70To79 = (SELECT COUNT(*)
                                     FROM Score
                                     WHERE courseId = @courseId
                                           AND AcademicSeasonId = @academicSeasonId
                                           AND assessmentMethodId = @assessmentMethodId
                                           AND value BETWEEN 70 AND 79)
            SET @countScore80To89 = (SELECT COUNT(*)
                                     FROM Score
                                     WHERE
                                       courseId = @courseId AND AcademicSeasonId = @academicSeasonId
                                       AND assessmentMethodId = @assessmentMethodId
                                       AND value BETWEEN 80 AND 89)
            SET @countScore90 = (SELECT COUNT(*)
                                 FROM Score
                                 WHERE courseId = @courseId
                                       AND AcademicSeasonId = @academicSeasonId
                                       AND assessmentMethodId = @assessmentMethodId
                                       AND value > 90)
          END
        ELSE --IF IS OVERALL
          BEGIN
            SET @mean = (SELECT AVG(VALUE) FROM GradeStatistic WHERE cmrId = @cmrId AND StatisticType = 'Mean')
            SET @median = (SELECT AVG(VALUE) FROM GradeStatistic WHERE cmrId = @cmrId AND StatisticType = 'Median')
            SET @stDeviation = (SELECT AVG(VALUE) FROM GradeStatistic WHERE cmrId = @cmrId AND StatisticType = 'Standard Deviation')

            SET @countScore0To9 = (SELECT SUM(GradeDistribution.VALUE)
                                   FROM GradeDistribution
                                   WHERE DistributionType = '0-9' AND CMRId = @cmrId)
            SET @countScore10To19 = (SELECT SUM(GradeDistribution.VALUE)
                                     FROM GradeDistribution
                                     WHERE DistributionType = '10-19' AND CMRId = @cmrId)
            SET @countScore20To29 = (SELECT SUM(GradeDistribution.VALUE)
                                     FROM GradeDistribution
                                     WHERE DistributionType = '20-29' AND CMRId = @cmrId)
            SET @countScore30To39 = (SELECT SUM(GradeDistribution.VALUE)
                                     FROM GradeDistribution
                                     WHERE DistributionType = '30-39' AND CMRId = @cmrId)
            SET @countScore40To49 = (SELECT SUM(GradeDistribution.VALUE)
                                     FROM GradeDistribution
                                     WHERE DistributionType = '40-49' AND CMRId = @cmrId)
            SET @countScore50To59 = (SELECT SUM(GradeDistribution.VALUE)
                                     FROM GradeDistribution
                                     WHERE DistributionType = '50-59' AND CMRId = @cmrId)
            SET @countScore60To69 = (SELECT SUM(GradeDistribution.VALUE)
                                     FROM GradeDistribution
                                     WHERE DistributionType = '60-69' AND CMRId = @cmrId)
            SET @countScore70To79 = (SELECT SUM(GradeDistribution.VALUE)
                                     FROM GradeDistribution
                                     WHERE DistributionType = '70-79' AND CMRId = @cmrId)
            SET @countScore80To89 = (SELECT SUM(GradeDistribution.VALUE)
                                     FROM GradeDistribution
                                     WHERE DistributionType = '80-89' AND CMRId = @cmrId)
            SET @countScore90 = (SELECT SUM(GradeDistribution.VALUE)
                                 FROM GradeDistribution
                                 WHERE DistributionType = '90+' AND CMRId = @cmrId)
          END

        INSERT INTO GradeStatistic VALUES (@cmrId, 'Mean', @assessmentMethodId, @mean);
        INSERT INTO GradeStatistic VALUES (@cmrId, 'Median', @assessmentMethodId, @median);
        INSERT INTO GradeStatistic VALUES (@cmrId, 'Standard Deviation', @assessmentMethodId, @stDeviation);

        INSERT INTO GradeDistribution VALUES (@cmrId, @assessmentMethodId, '0-9', @countScore0To9);
        INSERT INTO GradeDistribution VALUES (@cmrId, @assessmentMethodId, '10-19', @countScore10To19);
        INSERT INTO GradeDistribution VALUES (@cmrId, @assessmentMethodId, '20-29', @countScore20To29);
        INSERT INTO GradeDistribution VALUES (@cmrId, @assessmentMethodId, '30-39', @countScore30To39);
        INSERT INTO GradeDistribution VALUES (@cmrId, @assessmentMethodId, '40-49', @countScore40To49);
        INSERT INTO GradeDistribution VALUES (@cmrId, @assessmentMethodId, '50-59', @countScore50To59);
        INSERT INTO GradeDistribution VALUES (@cmrId, @assessmentMethodId, '60-69', @countScore60To69);
        INSERT INTO GradeDistribution VALUES (@cmrId, @assessmentMethodId, '70-79', @countScore70To79);
        INSERT INTO GradeDistribution VALUES (@cmrId, @assessmentMethodId, '80-89', @countScore80To89);
        INSERT INTO GradeDistribution VALUES (@cmrId, @assessmentMethodId, '90+', @countScore90);

        FETCH NEXT FROM assessmentMethodCursor
        INTO @assessmentMethodId;
      END

    CLOSE assessmentMethodCursor;
    DEALLOCATE assessmentMethodCursor;
    COMMIT TRANSACTION
    END TRY
    BEGIN CATCH
      IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION

      -- Return the error information.
      DECLARE @ErrorMessage NVARCHAR(4000), @ErrorSeverity INT;
      SELECT
          @ErrorMessage = ERROR_MESSAGE(),
          @ErrorSeverity = ERROR_SEVERITY();
      RAISERROR (@ErrorMessage, @ErrorSeverity, 1);
    END CATCH
  END;