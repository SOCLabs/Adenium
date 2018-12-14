package com.adenium.externals.zookeeper

import java.nio.charset.StandardCharsets

import com.adenium.app.config.Conf
import com.adenium.utils.May._
import com.google.common.primitives.Longs
import org.apache.curator.framework.CuratorFramework
import org.apache.zookeeper.CreateMode
import org.apache.zookeeper.KeeperException.{NoNodeException, NodeExistsException}

import scala.collection.JavaConverters._
import scala.collection.immutable.{HashMap, HashSet}
import scala.reflect.ClassTag

/**
  * Zookeeper handling utility with Curaotr
  *
  * Curator n ˈkyoor͝ˌātər: a keeper or custodian of a museum or other collection - A ZooKeeper Keeper.
  *
  * Apache Curator is a Java/JVM client library for Apache ZooKeeper, a distributed coordination service. It includes a highlevel API framework and utilities to make using Apache ZooKeeper much easier and more reliable. It also includes recipes for common use cases and extensions such as service discovery and a Java 8 asynchronous DSL.
  * [[https://curator.apache.org/index.html]]
  */
object ZkUtil {

  def save2Zk( curator: Option[CuratorFramework])(path: String, arr: Option[Array[String]]): Boolean = {

    warn (None)("save2ZK ===============> called..")

    val ret =
      for {

        cur <- warn (curator)("save2ZK ===============> curator is not set")
        ar <- warn(arr)("save2ZK ===============> empty array")
        src = ar.mkString( Conf.zk_sepLine ).getBytes( java.nio.charset.StandardCharsets.UTF_8 )

      } yield setBigPersistent( cur, path, src, Conf.zk_nodebyte)

    ret.isDefined
  }

  ///////////////////////////////////////////////////////
  def readZKLines(cur: CuratorFramework, path2: String, sep: String = Conf.zk_sepLine)
  : Option[ Array[String ] ] = {

    for {
      ab <- readChildren2Bytes( cur, path2 )
      st = new String( ab, StandardCharsets.UTF_8 )
    } yield st.split( sep)
  }

  def readZKLinesWithFunction[ A: ClassTag ]( curator: Option[CuratorFramework] )
                                            ( path2: String, sep: String = Conf.zk_sepItem)
                                            ( f: Array[ String ] => A ): Option[Array[A]] = {
    for {
      cur <- curator
      rows <- readZKLines( cur, path2)
    } yield {
      val ret =
        rows.flatMap { row =>
          maybeWarn2{ f( row.split( sep )) }(s"[ readZKLinesWithFunction ] fail to convert : ($row), ($sep), ${row.split(sep).length}")
        }
      ret
    }
  }

  def readZKArray[ A: ClassTag ]( curator: Option[CuratorFramework] )
                                ( path: String, sep: String = Conf.zk_sepItem)
                                ( f: Array[ String ] => A )
  : Array[ A ] = {

    val ar = readZKLinesWithFunction(curator )(path, sep)(f)
    ar getOrElse {
      throw new Exception(s"[ readZkArray ] check $path")
    }
  }

  def readZKMap[ A, B ]( curator: Option[CuratorFramework] )
                       ( path: String, sep: String = Conf.zk_sepItem)
                       ( f: Array[ String ] => (A, B) )
  : HashMap[ A, B ] = {

    val ar = readZKLinesWithFunction( curator )(path, sep)(f)

    val itms = ar.getOrElse {
      throw new Exception (s"[ readZKMap ] check $path")
    }
    HashMap[ A, B ]( itms: _* )
  }

  def readZKSet(curator: Option[CuratorFramework])
               (path: String)
  : HashSet[ String ] = {

    val ar = curator.flatMap( readZKLines(_, path)) getOrElse {
      throw new Exception (s"[ readZKSet ] check $path")
    }
    HashSet( ar: _* )
  }

  ////////////////////////////////////////////////////////////////////////////////
  def setPersistent (cur: CuratorFramework, path: String, str: String)
  : Option[String ] = {

    warn {
      setPersistent( cur, path, str.getBytes( StandardCharsets.UTF_8 ) )
    }(s"[ createPersistent ] fails: $path, ${str.take(50)}")

  }

  // NOTE : large ar[Byte] split and save to child nodes
  def setBigPersistent(cur: CuratorFramework, path: String, ar: Array[Byte], size: Int )
  : Array[Option[String ] ] = {

    state (s"setPersistent: == $path : ${ar.take(40)}")
    val bulks = ar.grouped( size).zipWithIndex.toArray  // (data0, 0) ..
    bulks.map { case ( array, idx ) => setPersistent( cur, path + "/" + idx, array) }
  }

  def setPersistent(cur: CuratorFramework, path: String, ar: Array[Byte] )
  : Option[String ] = {

    try {
      Some( cur.setData().forPath( path, ar).toString )
    }
    catch {

      case _: NoNodeException =>
        try {
          createPersistent( cur, path, ar )
        }
        catch {
          case _: NodeExistsException => Some( cur.setData().forPath( path, ar).toString )
          case _: Throwable => None // todo : swallow exception
        }
      case _: Throwable => None // todo : swallow exception
    }
  }

  def createPersistent(cur: CuratorFramework, path: String)
  : Option[String ] = {

    maybeInfo{
      cur
        .create()
        .creatingParentsIfNeeded()
        .withMode(CreateMode.PERSISTENT)
        .forPath(path)
    }(s"[ createPersistent ] fails: $path")

  }

  def createPersistent(cur: CuratorFramework, path: String, str: String)
  : Option[String ] = {

    createPersistent( cur, path, str.getBytes( StandardCharsets.UTF_8))

  }

  def createPersistent(cur: CuratorFramework, path: String, data: Array[Byte])
  : Option[String ] = {

    maybeInfo {
      cur
        .create()
        .creatingParentsIfNeeded()
        .withMode( CreateMode.PERSISTENT )
        .forPath( path, data )
    }(s"[ createPersistent ] fails: $path ${data.take(50)}")

  }

  def readLong (cur: CuratorFramework, path: String)
  : Option[ Long ] =
    maybeInfo { Longs.fromByteArray( cur.getData.forPath( path ) ) }(s"[ readLong ] fails: $path")

  def readBytes (cur: CuratorFramework, path: String)
  : Option[ Array[ Byte ] ] =
    maybeInfo { cur.getData.forPath(path) }(s"[ readBytes ] fails: $path")

  // NOTE : read large data from child nodes
  def readChildren2Bytes(cur: CuratorFramework, path: String)
  : Option[Array[ Byte ] ] = {

    getChildren( cur, path).flatMap { children =>
        maybeWarn2 {
          children
            .map(_.toInt)
            .sorted
            .map { child => child -> readBytes(cur, path + "/" + child) }
            .foldLeft(Array[Byte]()) { case (b, (p, od)) => if (od.isDefined) b ++ od.get else b } // todo
        }(s"[ readChildren2Byte ] check zk path = $path")
      }
  }

  def readString (cur: CuratorFramework, path: String)
  : Option[ String ] =
    maybeInfo {
      new String( cur.getData.forPath(path), StandardCharsets.UTF_8)
    } ( s" ZK readString fails: $path" )

  def getChildren (cur: CuratorFramework, path: String)
  : Option[ Seq[String ] ] = {

    maybeInfo {
      cur.getChildren.forPath( path ).asScala
    } ( s" ZK children not exists: $path" )
  }

  // recursive deleting
  def deletePersistent (cur: CuratorFramework, path: String)
  : Boolean = {

    maybeInfo {
      cur.delete().deletingChildrenIfNeeded().forPath( path)
    }( s" ZK delete fails : $path" ).isDefined
  }

}



