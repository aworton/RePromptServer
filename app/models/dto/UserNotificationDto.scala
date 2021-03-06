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

import models.User
import play.api.libs.json.{Json, OFormat}

/**
  * User notification data object
  * @param firstName database value
  * @param surName database value
  */
case class UserNotificationDto(
  firstName: String,
  surName: String,
)

/**
  * Companion Object for to hold boiler plate for forms, json conversion, slick
  */
object UserNotificationDto {

  def apply(user: User): UserNotificationDto = {
    UserNotificationDto(user.firstName, s"${user.surName.substring(0,1)}.")
  }

  /**
    * implicit json conversion formatter
    */
  implicit val serializer: OFormat[UserNotificationDto] = Json.format[UserNotificationDto]
}
