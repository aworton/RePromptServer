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

package models.dao

import com.mohiva.play.silhouette.api.LoginInfo
import libs.{ AppFactory, UserProfileTestData }
import org.scalatest.{ AsyncFunSpec, BeforeAndAfter, Matchers }

class UserDaoSlickSpec extends AsyncFunSpec with Matchers with BeforeAndAfter
  with AppFactory {

  val userDao: UserDao = fakeApplication().injector.instanceOf[UserDaoSlick]
  val testData = new UserProfileTestData(userDao)

  before {
    testData.before
  }

  after {
    testData.after
  }

  describe("UserDaoSlick") {

    it("should correctly find an existing user by id") {
      for {
        returnedUser <- userDao.save(testData.user1Linked)
        foundUser <- userDao.find(returnedUser.get.id.get)
        result = foundUser.get.id should be(returnedUser.get.id)
      } yield result

    }

    it("should correctly not find a non existing user by id") {

      for {
        returnedUser <- userDao.save(testData.user1Linked)
        deletedUser <- userDao.delete(returnedUser.get.id.get)
        foundUser <- userDao.find(deletedUser + 99999)
        result = foundUser should be(None)
      } yield result

    }

    it("should correctly find an existing user by loginInfo") {
      for {
        returnedUser <- userDao.save(testData.user2Linked)
        foundUser <- userDao.find(returnedUser.get.profiles.head.loginInfo)
        result = foundUser.get.id should be(returnedUser.get.id)
      } yield result
    }

    it("should correctly not find a non existing user by loginInfo") {

      for {
        returnedUser <- userDao.save(testData.user2Linked)
        login = returnedUser.get.profiles.head.loginInfo
        deletedUser <- userDao.delete(returnedUser.get.id.get)
        foundUser <- userDao.find(login)
        result = foundUser should be(None)
      } yield result

    }

    it("should correctly insert a user by id (new user)") {
      //tests save
      val returnedUser = userDao.save(testData.user1Linked)
      returnedUser map {
        result =>
          {
            result match {
              case None => result should not be None
              case Some(usr) => {
                usr.id.isDefined should be(true)
                userDao.delete(usr.id.get)
                usr.id.get should be > 0
              }
            }
          }
      }
    }

    it("should correctly update a user by id (existing user)") {
      //tests save
      val returnedUser = userDao.save(testData.user1Linked)
      returnedUser map {
        result =>
          {
            result should not be None
            result match {
              case None => result should not be None
              case Some(usr) => {
                val returnedUser = userDao.update(usr.copy(firstName = "Malethew"))
                returnedUser map {
                  secondResult =>
                    {
                      secondResult should not be None
                      secondResult match {
                        case None => secondResult should not be None
                        case Some(uzr) => {
                          uzr.id.isDefined should be(true)
                          uzr.id.get should be(usr.id.get)
                          uzr.firstName should be("Malethew")
                        }
                      }
                    }
                }
                usr.id.isDefined should be(true)
                usr.id.get should be > 0
              }
            }
          }
      }
    }

    it("should correctly insert passwordInfo and  OAuthInfo") {
      for {
        insertedUser <- userDao.save(testData.user2Linked)
        checkedUser <- userDao.find(insertedUser.get.id.get)
        result <- checkedUser.get.profiles.head.passwordInfo should be(testData.passInfo2)
        //cleanUp <- userDao.delete(insertedUser.get.id.get)
      } yield result
    }

    it("should correctly link a profile") {
      val returnedUser = userDao.save(testData.user1Linked)
      returnedUser map {
        result =>
          {
            result match {
              case None => result should not be None
              case Some(usr) => {
                //now perform link
                val linkedUser = userDao.link(usr, testData.profile1)
                linkedUser map {
                  r =>
                    {
                      r match {
                        case None => r should not be None
                        case Some(u) => {
                          u.profiles.size should be(1)
                          u.profiles.head should be(testData.profile1)
                          u.profiles.head.userId should be(u.id)
                        }
                      }
                    }
                }

                usr.profiles.size should be(1)
                usr.profiles.head should be(testData.profile1.copy(userId = usr.id))
              }
            }
          }
      }
    }

    it("should correctly save an attached user profile") {
      val returnedUser = userDao.save(testData.user2Linked)
      returnedUser map {
        result =>
          {
            result match {
              case None => result should not be None
              case Some(usr) =>
                userDao.delete(usr.id.get)
                val expected = testData.profile2.copy(userId = usr.id)
                usr.profiles.size should be(1)
                usr.profiles.head should be(expected)
            }
          }
      }
    }

    it("should correctly save multiple attached user profiles") {
      val returnedUser = userDao.save(testData.user3Linked)
      returnedUser map {
        result =>
          {
            result match {
              case None => result should not be None
              case Some(usr) => {
                val expected = testData.profiles3.map(p => p.copy(userId = usr.id))

                usr.profiles.size should be(2)
                //usr.profiles.head should be(expected)
              }
            }
          }
      }
    }
  }

  describe("checkDuplicate(user: User)") {
    it(" should return true if the user is a duplicate") {
      for {
        saved <- userDao.save(testData.user1Linked)
        checked <- userDao.checkDuplicate(saved.getOrElse(testData.user1Linked))
        _ <- userDao.delete(saved.get.id.get)
        assertions = {
          checked should be(true)
        }
      } yield assertions
    }

    it(" should return false if the user is not a duplicate") {
      for {
        checked <- userDao.checkDuplicate(testData.user1Linked)
        assertions = {
          checked should be(false)
        }
      } yield assertions
    }
  }

  describe("confirm(loginInfo: LoginInfo)") {
    it("should set the profile associated with the loginInfo as confirmed and return 1 if changed") {
      for {
        saved <- userDao.save(testData.user1Linked)
        confirmed <- userDao.confirm(testData.user1Linked.profiles.head.loginInfo)
        check <- userDao.find(saved.get.id.get)
        _ <- userDao.delete(saved.get.id.get)
        assertions = {
          saved.isDefined should be(true)
          confirmed should be(1)
          check.isDefined should be(true)
          check.get.profileFor(testData.user1Linked.profiles.head.loginInfo).get.confirmed should be(true)
        }
      } yield assertions
    }

    it("should set the profile associated with the loginInfo as confirmed and return 0 if unchanged") {
      for {
        saved <- userDao.save(testData.user1Linked)
        confirmed <- userDao.confirm(testData.user1Linked.profiles.head.loginInfo)
        doubleConfirmed <- userDao.confirm(testData.user1Linked.profiles.head.loginInfo)
        check <- userDao.find(saved.get.id.get)
        _ <- userDao.delete(saved.get.id.get)
        assertions = {
          saved.isDefined should be(true)
          confirmed should be(1)
          doubleConfirmed should be(0)
          check.isDefined should be(true)
          check.get.profileFor(testData.user1Linked.profiles.head.loginInfo).get.confirmed should be(true)
        }
      } yield assertions
    }

    it("should return 0 if no matching profile found") {
      for {
        saved <- userDao.save(testData.user1Linked)
        confirmed <- userDao.confirm(LoginInfo("credentials", "NotInHere@Address"))
        _ <- userDao.delete(saved.get.id.get)
        assertions = {
          saved.isDefined should be(true)
          confirmed should be(0)
        }
      } yield assertions
    }
  }

  describe("update(profile: Profile)") {
    it("should Update a stored user profile") {
      val newName = "£$Boo"
      val loginInfo = testData.user1Linked.profiles.head.loginInfo
      for {
        user <- userDao.save(testData.user1Linked)
        _ <- userDao.update(user.get.profileFor(loginInfo).get.copy(firstName = Some(newName)))
        retrieved <- userDao.find(user.get.id.get)
        assertions = {
          user.isDefined should be(true)
          retrieved.isDefined should be(true)
          user.get.profileFor(loginInfo).get.firstName should not be Some(newName)
          retrieved.get.profileFor(loginInfo).get.firstName should be(Some(newName))
        }
      } yield assertions
    }
  }

}
