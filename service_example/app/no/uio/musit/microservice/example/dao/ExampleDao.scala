/*
 * MUSIT is a museum database to archive natural and cultural history data.
 * Copyright (C) 2016  MUSIT Norway, part of www.uio.no (University of Oslo)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License,
 * or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package no.uio.musit.microservice.example.dao

import no.uio.musit.microservice.example.domain.Example

import play.api.Play
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig}
import slick.driver.JdbcProfile

import scala.concurrent.Future

class ExampleDao extends HasDatabaseConfig[JdbcProfile] {
  import driver.api._

  protected val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

  private val Examples = TableQuery[ExampleTable]

  def all() : Future[Seq[Example]] = db.run(Examples.result)

  def insert(example: Example): Future[Unit] = db.run(Examples += example).map { _ => () }

  private class ExampleTable(tag: Tag) extends Table[Example](tag, "EXAMPLES") {
    def id = column[Long]("ID", O.PrimaryKey) // This is the primary key column
    def email = column[String]("EMAIL")
    def name = column[String]("NAME")
    def * = (id, email, name) <> (Example.tupled, Example.unapply _)
  }
}


