CREATE PROCEDURE usp_createCMR @courseId     VARCHAR(10), @userId INT
AS
  BEGIN
    SET NOCOUNT ON;
    BEGIN TRY
    BEGIN TRANSACTION
    DECLARE @assessmentMethodId INT,
    @cmrId INT;
    -- insert CMR and get id
    INSERT INTO CMR
    VALUES ('Created', @userId, @courseId, DEFAULT,NULL , NULL, NULL, NULL, NULL, NULL);
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
        --find mean and insert
        DECLARE @mean FLOAT = 0;
        SET @mean = (SELECT SUM(value) / COUNT(*)
                     FROM Score
                     WHERE CourseId = @courseId
                           AND AssessmentMethodId = @assessmentMethodId)
        INSERT INTO GradeStatistic
        VALUES (@cmrId, 'Mean', @assessmentMethodId, @mean);
        --find median and insert
        DECLARE @median FLOAT = 0;
        DECLARE @count INT = (SELECT COUNT(*)
                              FROM score
                              WHERE courseId = @courseId
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
                                     AND assessmentMethodId = @assessmentMethodId) AS s
                         WHERE s.Row = @count / 2 + 1
                               OR s.Row = @count / 2)
        ELSE
          SET @median = (SELECT value
                         FROM (SELECT
                                 ROW_NUMBER()
                                 OVER (
                                   ORDER BY value) AS Row,
                                 value
                               FROM Score
                               WHERE courseId = @courseId
                                     AND assessmentMethodId = @assessmentMethodId) AS s
                         WHERE s.Row = ROUND(CAST(@count AS FLOAT) / CAST(2 AS FLOAT), 0))

        INSERT INTO GradeStatistic
        VALUES (@cmrId, 'Median', @assessmentMethodId, @median);

        -- find Standard Deviation and insert
        DECLARE @stDeviation FLOAT = (SELECT ROUND(SQRT(SUM(SQUARE(value - @mean)) / (COUNT(*) - 1)), 2)
                                      FROM Score
                                      WHERE courseId = @courseId
                                            AND assessmentMethodId = @assessmentMethodId)
        INSERT INTO GradeStatistic
        VALUES (@cmrId, 'Standard Deviation', @assessmentMethodId, @stDeviation);


        INSERT INTO GradeDistribution
        VALUES (@cmrId, @assessmentMethodId, '0-9', (SELECT COUNT(*)
                                                       FROM Score
                                                       WHERE courseId = @courseId AND
                                                             assessmentMethodId = @assessmentMethodId AND
                                                             value BETWEEN 0 AND 9));
        INSERT INTO GradeDistribution
        VALUES (@cmrId, @assessmentMethodId, '10-19', (SELECT COUNT(*)
                                                         FROM Score
                                                         WHERE courseId = @courseId AND
                                                               assessmentMethodId = @assessmentMethodId AND
                                                               value BETWEEN 10 AND 19));
        INSERT INTO GradeDistribution
        VALUES (@cmrId, @assessmentMethodId, '20-29', (SELECT COUNT(*)
                                                         FROM Score
                                                         WHERE courseId = @courseId AND
                                                               assessmentMethodId = @assessmentMethodId AND
                                                               value BETWEEN 20 AND 29));
        INSERT INTO GradeDistribution
        VALUES (@cmrId, @assessmentMethodId, '30-39', (SELECT COUNT(*)
                                                         FROM Score
                                                         WHERE courseId = @courseId AND
                                                               assessmentMethodId = @assessmentMethodId AND
                                                               value BETWEEN 30 AND 39));
        INSERT INTO GradeDistribution
        VALUES (@cmrId, @assessmentMethodId, '40-49', (SELECT COUNT(*)
                                                         FROM Score
                                                         WHERE courseId = @courseId AND
                                                               assessmentMethodId = @assessmentMethodId AND
                                                               value BETWEEN 40 AND 49));
        INSERT INTO GradeDistribution
        VALUES (@cmrId, @assessmentMethodId, '50-59', (SELECT COUNT(*)
                                                         FROM Score
                                                         WHERE courseId = @courseId AND
                                                               assessmentMethodId = @assessmentMethodId AND
                                                               value BETWEEN 50 AND 59));
        INSERT INTO GradeDistribution
        VALUES (@cmrId, @assessmentMethodId, '60-69', (SELECT COUNT(*)
                                                         FROM Score
                                                         WHERE courseId = @courseId AND
                                                               assessmentMethodId = @assessmentMethodId AND
                                                               value BETWEEN 60 AND 69));
        INSERT INTO GradeDistribution
        VALUES (@cmrId, @assessmentMethodId, '70-79', (SELECT COUNT(*)
                                                         FROM Score
                                                         WHERE courseId = @courseId AND
                                                               assessmentMethodId = @assessmentMethodId AND
                                                               value BETWEEN 70 AND 79));
        INSERT INTO GradeDistribution
        VALUES (@cmrId, @assessmentMethodId, '80-89', (SELECT COUNT(*)
                                                         FROM Score
                                                         WHERE courseId = @courseId AND
                                                               assessmentMethodId = @assessmentMethodId AND
                                                               value BETWEEN 80 AND 89));
        INSERT INTO GradeDistribution
        VALUES (@cmrId, @assessmentMethodId, '90+', (SELECT COUNT(*)
                                                       FROM Score
                                                       WHERE courseId = @courseId AND
                                                             assessmentMethodId = @assessmentMethodId AND
                                                             value > 90));

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
      DECLARE @ErrorMessage NVARCHAR(4000),
      @ErrorSeverity INT;
      SELECT
          @ErrorMessage = ERROR_MESSAGE(),
          @ErrorSeverity = ERROR_SEVERITY();
      RAISERROR (@ErrorMessage, @ErrorSeverity, 1);
    END CATCH
  END;