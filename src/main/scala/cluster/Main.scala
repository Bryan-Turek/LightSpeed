package cluster

// java imports
import java.net.InetAddress

// akka imports
import akka.actor.ActorSystem
import akka.actor.Props
import akka.cluster.Cluster
import akka.kernel.Bootable

// misc imports
import scala.collection.immutable
import org.slf4j.LoggerFactory
import com.amazonaws.services.opsworks.AWSOpsWorksClient
import com.amazonaws.services.opsworks.model.DescribeInstancesRequest
import com.amazonaws.services.opsworks.model.Instance
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
//import org.fusesource.lmdbjni.Env


class Init extends Bootable {
    val log = LoggerFactory.getLogger(getClass)
    var system: ActorSystem = _
    
    def startup(): Unit = {
        val stackId = System.getProperty("ops-stack-id")
        val selfHostName = InetAddress.getLocalHost.getHostName
        var conf =
            if (stackId eq null)
                ConfigFactory.load
            else {
                // running in EC2 with OpsWorks deployment
                val instances = opsInstances(stackId).sortBy(_.getHostname)
                val ips = instances.take(5).map { i ⇒
                  if (i.getPrivateIp eq null) i.getHostname // not started, but should still be in the seed-nodes
                  else i.getPrivateIp
                }
                instances.collectFirst { case i if (i.getPrivateIp ne null) && (i.getHostname == selfHostName) ⇒ i.getPrivateIp } match {
                  case None ⇒
                    throw new IllegalArgumentException(s"Couldn't find my own [${selfHostName}] private ip in list of instances [${instances}]")
                  case Some(selfIp) ⇒
                    val seedNodesStr = ips.map("akka.tcp://Cluster@" + _ + ":2552").mkString("\"", "\",\"", "\"")
                    log.info(s"[${selfHostName}/${selfIp}] starting with OpsWorks seed-nodes=[${seedNodesStr}]")
                    ConfigFactory.parseString(s"""
                      akka.remote.netty.tcp.hostname="${selfIp}"
                      akka.cluster.seed-nodes=[${seedNodesStr}]
                      """).withFallback(ConfigFactory.load)
                }
            }
        system = ActorSystem("Cluster", conf)
        
        val cluster = Cluster(system)
        
        system.actorOf(Props[MemberListener], name="nodes")
    }
    
    def opsInstances(stackId: String): immutable.IndexedSeq[Instance] = {
        try {
            import scala.collection.JavaConverters._
            val client = new AWSOpsWorksClient
            val req = (new DescribeInstancesRequest).withStackId(stackId)
            val result = client.describeInstances(req)
            result.getInstances.asScala.toVector
        } catch {
            case e: Exception ⇒
            log.warn("OpsWorks not available, due to: {}", e.getMessage)
            Vector.empty
        }
    }
    
    // shutdown the system
    def shutdown(): Unit = {
        if(system ne null){
            system.shutdown()
            system = null
        }   
    }
 
}

/*
    Main
    -   Initializes a startup call that creates the clusters.
*/
object Main {
    def main(args: Array[String]): Unit = {
        (new Init).startup();
    }
}
