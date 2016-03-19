package dao

import javax.inject.{Inject, Singleton}

import models._
import play.api.db.slick.{HasDatabaseConfigProvider, DatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.Future

/**
  * Created by chinhnk on 3/20/16.
  */
trait ScoreComponent{ self: HasDatabaseConfigProvider[JdbcProfile] =>
  import driver.api._
  class Scores(tag: Tag) extends Table[Score](tag, "Score"){
    def scoreId = column[Option[Int]]("ScoreId", O.PrimaryKey, O.AutoInc)
    def value = column[Double]("Value")
    def assessmentMethodId = column[Int]("AssessmentMethodId")
    def courseId = column[String]("CourseId")
    def academicSeasonId = column[Int]("AcademicSeasonId")

    def * = (scoreId, value, assessmentMethodId, courseId, academicSeasonId) <>((Score.apply _).tupled, Score.unapply _)
  }
}
@Singleton
class ScoreDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends HasDatabaseConfigProvider[JdbcProfile] with ScoreComponent {
  import driver.api._
  private lazy val scores = TableQuery[Scores]

  def countScore(courseId: String, academicId: Int): Future[Int] =
    db.run(scores.filter(s => s.courseId === courseId && s.academicSeasonId === academicId).length.result)

  def insert(listScore: Seq[Score]): Future[Option[Int]] = {
    db.run(scores ++= listScore)
  }
}
