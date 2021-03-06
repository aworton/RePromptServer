// Copyright (C) 2017 Alexander Worton.
// See the LICENCE.txt file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package models.dto

import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import slick.jdbc.GetResult
import slick.jdbc.MySQLProfile.api._
import slick.lifted
import slick.lifted.{ PrimaryKey, ProvenShape }

case class ContentItemDto(
  id: Option[Int],
  packageId: Int,
  imageUrl: Option[String],
  name: String,
  content: String,
  enabled: Boolean = true,
  questions: Option[List[QuestionDto]] = None,
  score: Option[ScoreDto] = None
) extends Dto

/**
 * Companion Object for to hold boiler plate for forms, json conversion, slick
 */
object ContentItemDto {

  def construct(id: Option[Int], packageId: Int, imageUrl: Option[String], name: String,
    content: String) =
    new ContentItemDto(id = id, packageId = packageId, imageUrl = imageUrl, name = name,
      content = content)

  def deconstruct(dto: ContentItemDto): Option[(Option[Int], Int, Option[String], String, String)] = dto match {
    case ContentItemDto(id: Option[Int], packageId: Int, imageUrl: Option[String], name: String,
      content: String, _, _, _) => Some(id, packageId, imageUrl, name, content)
  }

  def formConstruct(id: Option[Int], packageId: Int, imageUrl: Option[String], name: String,
    content: String, enabled: Option[Boolean]) =
    new ContentItemDto(id = id, packageId = packageId, imageUrl = imageUrl, name = name,
      content = content, enabled = enabled.getOrElse(true))

  def formDeconstruct(dto: ContentItemDto): Option[(Option[Int], Int, Option[String], String, String, Option[Boolean])] = dto match {
    case ContentItemDto(id: Option[Int], packageId: Int, imageUrl: Option[String], name: String,
      content: String, enabled: Boolean, _, _) => Some(id, packageId, imageUrl, name, content,
      Some(enabled))
  }

  /**
   * Form definition for data type to bindFromRequest when receiving data
   * @return a form for the dat object
   */
  def form: Form[ContentItemDto] = Form(
    mapping(
      "id" -> optional(number),
      "packageId" -> number,
      "imageUrl" -> optional(text),
      "name" -> nonEmptyText,
      "content" -> text,
      "enabled" -> optional(boolean)
    )(ContentItemDto.formConstruct)(ContentItemDto.formDeconstruct)
  )

  /**
   * Table definition for database mapping via slick
   * @param tag identifies a specific row
   */
  class ContentItemsTable(tag: Tag) extends Table[ContentItemDto](tag, "content_items") {

    def id: lifted.Rep[Option[Int]] = column[Int]("Id", O.PrimaryKey, O.AutoInc).?
    def packageId: lifted.Rep[Int] = column[Int]("PackageId")
    def imageUrl: lifted.Rep[Option[String]] = column[String]("ImageUrl").?
    def name: lifted.Rep[String] = column[String]("Name")
    def content: lifted.Rep[String] = column[String]("Content")
    def pk: PrimaryKey = primaryKey("PRIMARY", id)

    def * : ProvenShape[ContentItemDto] = (id, packageId, imageUrl, name, content) <>
      ((ContentItemDto.construct _).tupled, ContentItemDto.deconstruct)
  }

  /**
   * implicit converter to coerce direct sql query into data object
   */
  implicit val getContentItemResult = GetResult(r =>
    ContentItemDto(
      Some(r.nextInt),
      r.nextInt,
      Some(r.nextString),
      r.nextString,
      r.nextString,
      r.nextBoolean,
      None,
      None
    )
  )

  /**
   * implicit converter to coerce direct sql query into data object
   */
  implicit val getSomeContentItemResult = GetResult(r =>
    Some(ContentItemDto(
      Some(r.nextInt),
      r.nextInt,
      Some(r.nextString),
      r.nextString,
      r.nextString,
      r.nextBoolean,
      None,
      None
    ))
  )

  /**
   * implicit json conversion formatter
   */
  implicit val serializer = Json.format[ContentItemDto]
}

