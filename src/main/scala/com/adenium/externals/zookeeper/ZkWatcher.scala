package com.adenium.externals.zookeeper

import java.nio.charset.StandardCharsets

import com.adenium.externals.zookeeper.ZkUtil.setPersistent
import com.adenium.utils.Logger
import com.adenium.utils.May._
import org.apache.curator.framework.CuratorFramework
import org.apache.zookeeper.Watcher.Event.EventType
import org.apache.zookeeper.data.Stat
import org.apache.zookeeper.{KeeperException, WatchedEvent, Watcher}

import scala.language.reflectiveCalls

/**
  *
  * Zookeeper watcher with Curator watchedEvent.
  * Zookeeper watcher for monitoring AdeniumControl messages.
  *
  * - Curator WatchedEvent -
  * A super set of all the various Zookeeper events/background methods.
  * IMPORTANT: the methods only return values as specified by the operation that generated them.
  * Many methods will return null [[https://curator.apache.org/apidocs/org/apache/curator/framework/api/CuratorEvent.html]]
  */

object ZkWatcher {

  def onZkChange(cur: CuratorFramework, path: String)(handler: (String, Stat) => Unit) {

    Logger.logInfo("[  watchNodeOrChidlrenChange ] == zknode : " + path)

    def watcher = new Watcher {
      def process(event: WatchedEvent) {
        Logger.logDebug("[ watchNodeOrChidlrenChange ] == callback invoked " + path + "\ttype: " + event.getType)
        event.getType match {
          case EventType.NodeDataChanged | EventType.NodeChildrenChanged => updated()
          case _ => reset()
        }
      }
    }

    def updated() {
      try {
        val stat = new Stat()
        val msg = cur.getData.storingStatIn(stat).forPath(path)

        setPersistent(cur, path, "")

        val str = new String(msg, StandardCharsets.UTF_8)

        if (str.nonEmpty) {
          state("[ Watching ] == arrived msg: " + new String(msg, StandardCharsets.UTF_8))
          handler(str, stat)
        }

        if (str.startsWith("stop zkctrl")) {
          Logger.logWarning("[ Watching ] == stopped by 'stop zkctrl' message : path =" + path)
        } else {
          /// create and attach next msg watcher
          cur.checkExists.usingWatcher(watcher).forPath(path)
        }

      } catch {
        case e: KeeperException =>
          Logger.logWarning("[ watchNodeOrChidlrenChange ] == read node: " + path + "\te: " + e)
          reset()
      }
    }

    def reset() {
      setPersistent(cur, path, "")
      updated()
    }

    reset()
  }

}
