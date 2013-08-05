package com.nrinaudo.eshitsuji.web

import com.nrinaudo.eshitsuji.monitor.NameMonitor
import com.nrinaudo.eshitsuji.storage.{Storage, Configuration}
import unfiltered.request._
import unfiltered.filter._, Plan._
import unfiltered.response._
import argonaut._, Argonaut._, integrate.unfiltered.JsonResponse

class AdminPlan(storage: Storage, password: String) extends Plan with grizzled.slf4j.Logging {
  private val conf     = storage.conf
  private val monitors = collection.mutable.HashMap[String, NameMonitor]()

  def register(name: String, monitor: NameMonitor) {
    debug("Registering %s as monitor" format name)
    monitors += (name -> monitor)
  }



  // - JSON serialization ----------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  /** Used to implicitely transform JSON objects to JSON responses. */
  private implicit def toJsonResponse(obj: Json): ResponseFunction[Any] = JsonResponse(obj, PrettyParams.spaces4)

  /** Returns a JSON object listing all available monitors. */
  private def monitorList() =
    ("values" := jArray(monitors map {m => monitor(m._1, m._2)} toList)) ->:
    ("uri"    := "/monitors") ->: jEmptyObject

  /** Returns a JSON object describing the specified monitor. */
  private def monitor(name: String, monitor: NameMonitor) =
    ("name"   := name) ->:
    ("values" := jArray(monitor map {m => author(m, name)} toList)) ->:
    ("uri"    := "/monitors/%s" format name) ->: jEmptyObject

  /** Returns a JSON object describing the specified author. */
  private def author(name: String, monitor: String) =
    ("name" := name) ->:
    ("uri"  := "/monitors/%s/%s" format(monitor, name)) ->: jEmptyObject

  /** Returns a JSON object decribing the specified configuration variable. */
  private def variable(name: String, value: String) =
    ("name"  := name) ->:
    ("value" := value) ->: jEmptyObject



  // - Intent ----------------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  def intent = Authentifier(password) {
      // Configuration: /conf/{name}
      case req @ Path(Seg("conf" :: name :: Nil)) => req match {
        case GET(_)    => conf.get(name) match {
          case Some(v) => variable(name, v)
          case None    => NotFound
        }
        case PUT(_)    => conf(name) = Body.string(req); Accepted
        case DELETE(_) => conf -= name; Accepted
        case _         => MethodNotAllowed
      }

      // Exit: /admin/exit
      case req @ Path(Seg("admin" :: "exit" :: Nil)) => req match {
        case PUT(_) => System.exit(0); Accepted
        case _      => MethodNotAllowed
      }

      // All monitors: /monitors
      case req @ Path(Seg("monitors" :: Nil)) => req match {
        case GET(_) => monitorList
        case _      => MethodNotAllowed
      }

      // One monitor: /monitors/{name}
      case req @ Path(Seg("monitors" :: name :: rest)) => monitors.get(name) match {
        // We have a monitor that matches the specified name.
        case Some(m) => rest match {
          // Resource: /monitors/{name}
          case Nil => req match {
            case GET(_) => monitor(name, m)
            case _      => MethodNotAllowed
          }

          // Specific author: /monitors/{name}/{id}
          case id :: Nil => req match {
            case GET(_)    => author(id, name)
            case PUT(_)    => m += id; Accepted
            case DELETE(_) => m -= id; Accepted
            case _         => MethodNotAllowed
          }
        }

        // The requested monitor does not exist.
        case None => NotFound
      }
  }
}
